package com.haiercash.spring.feign.core;

import com.bestvike.linq.Linq;
import com.haiercash.core.lang.Convert;
import com.haiercash.core.reflect.ReflectionUtils;
import com.haiercash.core.serialization.URLSerializer;
import feign.Contract;
import feign.Feign;
import feign.InvocationHandlerFactory;
import feign.MethodMetadata;
import feign.Param;
import feign.RequestTemplate;
import feign.Target;
import feign.Util;
import org.springframework.http.HttpMethod;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by 许崇雷 on 2018-02-09.
 */
public final class RestReflectiveFeign extends Feign {
    private final ParseHandlersByName targetToHandlersByName;
    private final InvocationHandlerFactory factory;

    RestReflectiveFeign(ParseHandlersByName targetToHandlersByName, InvocationHandlerFactory factory) {
        this.targetToHandlersByName = targetToHandlersByName;
        this.factory = factory;
    }

    /**
     * creates an api binding to the {@code target}. As this invokes reflection, care should be taken
     * to cache the result.
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T newInstance(Target<T> target) {
        Map<String, InvocationHandlerFactory.MethodHandler> nameToHandler = this.targetToHandlersByName.apply(target);
        Map<Method, InvocationHandlerFactory.MethodHandler> methodToHandler = new LinkedHashMap<>();
        List<DefaultMethodHandler> defaultMethodHandlers = new LinkedList<>();
        final Class<T> type = target.type();
        for (Method method : type.getMethods()) {
            if (method.getDeclaringClass() == Object.class)
                continue;
            if (Util.isDefault(method)) {
                DefaultMethodHandler handler = new DefaultMethodHandler(method);
                defaultMethodHandlers.add(handler);
                methodToHandler.put(method, handler);
                continue;
            }
            String name = Feign.configKey(type, method);
            InvocationHandlerFactory.MethodHandler handler = nameToHandler.get(name);
            methodToHandler.put(method, new DelegateMethodHandler(name, type, method, handler));
        }
        InvocationHandler handler = this.factory.create(target, methodToHandler);
        T proxy = (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler);
        for (DefaultMethodHandler defaultMethodHandler : defaultMethodHandlers)
            defaultMethodHandler.bindTo(proxy);
        return proxy;
    }


    static final class ParseHandlersByName {
        private final Contract contract;
        private final SynchronousMethodHandler.Factory factory;

        ParseHandlersByName(Contract contract, SynchronousMethodHandler.Factory factory) {
            this.contract = contract;
            this.factory = factory;
        }

        Map<String, InvocationHandlerFactory.MethodHandler> apply(Target target) {
            List<MethodMetadata> metadataList = this.contract.parseAndValidatateMetadata(target.type());
            Map<String, InvocationHandlerFactory.MethodHandler> result = new LinkedHashMap<>();
            for (MethodMetadata metadata : metadataList) {
                FeignRequest.Factory requestFactory;
                if (!metadata.formParams().isEmpty() && metadata.template().bodyTemplate() == null)
                    requestFactory = new BuildFormEncodedTemplateFromArgs(target, metadata);
                else if (metadata.bodyIndex() != null)
                    requestFactory = new BuildEncodedTemplateFromArgs(target, metadata);
                else
                    requestFactory = new BuildTemplateByResolvingArgs(target, metadata);
                result.put(metadata.configKey(), this.factory.create(requestFactory));
            }
            return result;
        }
    }


    private static class BuildTemplateByResolvingArgs implements FeignRequest.Factory {
        private static final Method RESOLVE = ReflectionUtils.getMethodInfo(RequestTemplate.class, "resolve", new Class<?>[]{Map.class, Map.class}, false);
        protected final Target<?> target;
        protected final MethodMetadata metadata;
        protected final Map<Integer, Param.Expander> indexToExpander = new LinkedHashMap<>();

        private BuildTemplateByResolvingArgs(Target<?> target, MethodMetadata metadata) {
            this.target = target;
            this.metadata = metadata;
            if (metadata.indexToExpander() != null) {
                this.indexToExpander.putAll(metadata.indexToExpander());
                return;
            }
            if (metadata.indexToExpanderClass().isEmpty())
                return;
            for (Map.Entry<Integer, Class<? extends Param.Expander>> indexToExpanderClass : metadata.indexToExpanderClass().entrySet()) {
                try {
                    this.indexToExpander.put(indexToExpanderClass.getKey(), indexToExpanderClass.getValue().newInstance());
                } catch (InstantiationException | IllegalAccessException e) {
                    throw new IllegalStateException(e);
                }
            }
        }

        @Override
        public FeignRequest create(Object[] argv) {
            RequestTemplate mutable = new RequestTemplate(this.metadata.template());

            //url 参数
            Integer urlIndex = this.metadata.urlIndex();
            if (urlIndex != null) {
                Object url = argv[urlIndex];
                Util.checkArgument(url != null, "URI parameter %s was null", urlIndex);
                mutable.insert(0, Convert.toString(url));
            }

            //构建参数字典
            Map<String, Object> variables = new LinkedHashMap<>();
            for (Map.Entry<Integer, Collection<String>> entry : this.metadata.indexToName().entrySet()) {
                int index = entry.getKey();
                Object value = argv[index];
                if (value == null)// Null values are skipped.
                    continue;
                if (this.indexToExpander.containsKey(index))
                    value = this.expandElements(this.indexToExpander.get(index), value);
                for (String name : entry.getValue())
                    variables.put(name, value);
            }

            //解析 url, body, query, headers
            AtomicReference<Object> bodyRef = new AtomicReference<>();
            RequestTemplate template = this.resolve(argv, mutable, bodyRef, variables);
            if (this.metadata.queryMapIndex() != null)
                template = this.addQueryMapQueryParameters(argv, template);
            if (this.metadata.headerMapIndex() != null)
                template = this.addHeaderMapHeaders(argv, template);

            //构建请求
            FeignRequest request = new FeignRequest(this.target.url() + template.url(), HttpMethod.resolve(template.method()), bodyRef.get(), this.metadata.returnType());
            final Map<String, String> uriVariables = request.getUriVariables();
            for (Map.Entry<String, Collection<String>> entry : template.queries().entrySet()) {
                Collection<String> value = entry.getValue();
                if (!value.isEmpty())
                    uriVariables.put(entry.getKey(), URLSerializer.decode(Linq.asEnumerable(value).first()));
            }
            final MultiValueMap<String, String> headers = request.getHeaders();
            for (Map.Entry<String, Collection<String>> entry : template.headers().entrySet()) {
                Collection<String> value = entry.getValue();
                if (!value.isEmpty())
                    headers.set(entry.getKey(), Linq.asEnumerable(value).first());
            }
            return request;
        }

        protected RequestTemplate resolve(Object[] argv, RequestTemplate mutable, AtomicReference<Object> bodyRef, Map<String, Object> variables) {
            // Resolving which variable names are already encoded using their indices
            Map<String, Boolean> variableToEncoded = new LinkedHashMap<>();
            for (Map.Entry<Integer, Boolean> entry : this.metadata.indexToEncoded().entrySet()) {
                Collection<String> names = this.metadata.indexToName().get(entry.getKey());
                for (String name : names)
                    variableToEncoded.put(name, entry.getValue());
            }

            try {
                return (RequestTemplate) RESOLVE.invoke(mutable, variables, variableToEncoded);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }

        private Object expandElements(Param.Expander expander, Object value) {
            return (value instanceof Iterable)
                    ? this.expandIterable(expander, (Iterable) value)
                    : expander.expand(value);
        }

        private List<String> expandIterable(Param.Expander expander, Iterable value) {
            List<String> values = new ArrayList<>();
            for (Object element : value) {
                if (element != null)
                    values.add(expander.expand(element));
            }
            return values;
        }

        @SuppressWarnings("unchecked")
        private RequestTemplate addQueryMapQueryParameters(Object[] argv, RequestTemplate mutable) {
            Map<Object, Object> queryMap = (Map<Object, Object>) argv[this.metadata.queryMapIndex()];
            for (Map.Entry<Object, Object> entry : queryMap.entrySet()) {
                Util.checkState(entry.getKey().getClass() == String.class, "QueryMap key must be a String: %s", entry.getKey());
                Collection<String> values = new ArrayList<>();
                boolean encoded = this.metadata.queryMapEncoded();
                Object value = entry.getValue();
                if (value instanceof Iterable<?>) {
                    for (Object valueElement : ((Iterable<?>) value))
                        values.add(valueElement == null ? null : encoded ? valueElement.toString() : URLSerializer.encode(valueElement.toString()));
                } else {
                    values.add(value == null ? null : encoded ? value.toString() : URLSerializer.encode(value.toString()));
                }
                mutable.query(true, encoded ? (String) entry.getKey() : URLSerializer.encode(Convert.toString(entry.getKey())), values);
            }
            return mutable;
        }

        @SuppressWarnings("unchecked")
        private RequestTemplate addHeaderMapHeaders(Object[] argv, RequestTemplate mutable) {
            Map<Object, Object> headerMap = (Map<Object, Object>) argv[this.metadata.headerMapIndex()];
            for (Map.Entry<Object, Object> entry : headerMap.entrySet()) {
                Util.checkState(entry.getKey().getClass() == String.class, "HeaderMap key must be a String: %s", entry.getKey());
                Collection<String> values = new ArrayList<>();
                Object value = entry.getValue();
                if (value instanceof Iterable<?>) {
                    for (Object valueElement : ((Iterable<?>) value))
                        values.add(valueElement == null ? null : valueElement.toString());
                } else {
                    values.add(value == null ? null : value.toString());
                }
                mutable.header((String) entry.getKey(), values);
            }
            return mutable;
        }
    }


    private static class BuildFormEncodedTemplateFromArgs extends BuildTemplateByResolvingArgs {
        private BuildFormEncodedTemplateFromArgs(Target<?> target, MethodMetadata metadata) {
            super(target, metadata);
        }

        @Override
        protected RequestTemplate resolve(Object[] argv, RequestTemplate mutable, AtomicReference<Object> bodyRef, Map<String, Object> variables) {
            Map<String, Object> bodyMap = new LinkedHashMap<>();
            for (Map.Entry<String, Object> entry : variables.entrySet()) {
                if (this.metadata.formParams().contains(entry.getKey()))
                    bodyMap.put(entry.getKey(), entry.getValue());
            }
            bodyRef.set(bodyMap);
            return super.resolve(argv, mutable, bodyRef, variables);
        }
    }


    private static class BuildEncodedTemplateFromArgs extends BuildTemplateByResolvingArgs {
        private BuildEncodedTemplateFromArgs(Target<?> target, MethodMetadata metadata) {
            super(target, metadata);
        }

        @Override
        protected RequestTemplate resolve(Object[] argv, RequestTemplate mutable, AtomicReference<Object> bodyRef, Map<String, Object> variables) {
            int bodyIndex = this.metadata.bodyIndex();
            Object body = argv[bodyIndex];
            Util.checkArgument(body != null, "Body parameter %s was null", bodyIndex);
            bodyRef.set(body);
            return super.resolve(argv, mutable, bodyRef, variables);
        }
    }
}
