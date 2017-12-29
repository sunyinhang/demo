package com.haiercash.spring.aop;

import com.bestvike.linq.Linq;
import com.haiercash.core.lang.Convert;
import com.haiercash.core.lang.StringUtils;
import com.haiercash.spring.context.ThreadContext;
import com.haiercash.spring.controller.BaseController;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 控制器拦截器
 * Created by 许崇雷 on 2017-10-19.
 */
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public final class ControllerInterceptor {
    private static final String NAME_TOKEN = "token";
    private static final String NAME_CHANNEL = "channel";
    private static final String NAME_CHANNEL_NO = "channelNo";
    private static final String GET_TOKEN = "getToken";
    private static final String GET_CHANNEL = "getChannel";
    private static final String GET_CHANNEL_NO = "getChannelNo";
    private static final String SET_TOKEN = "setToken";
    private static final String SET_CHANNEL = "setChannel";
    private static final String SET_CHANNEL_NO = "setChannelNo";
    private final Log logger = LogFactory.getLog(ControllerInterceptor.class);

    private static Object newInstance(Class<?> clazz) {
        try {
            return clazz.newInstance();
        } catch (Exception e) {
            return null;
        }
    }

    private static Object invokeGetMethod(Object target, String methodName) {
        try {
            Method method = Linq.asEnumerable(target.getClass().getMethods())
                    .firstOrDefault(m -> StringUtils.equals(m.getName(), methodName)
                            && m.getParameterCount() == 0
                            && m.getReturnType() != Void.TYPE);
            if (method == null)
                return null;
            return method.invoke(target);
        } catch (Exception e) {
            return null;
        }
    }

    private static void invokeSetMethod(Object target, String methodName, String value) {
        try {
            Method method = Linq.asEnumerable(target.getClass().getMethods())
                    .firstOrDefault(m -> StringUtils.equals(m.getName(), methodName)
                            && m.getParameterCount() == 1
                            && m.getParameterTypes()[0].isAssignableFrom(String.class));
            if (method == null)
                return;
            method.invoke(target, value);
        } catch (Exception ignored) {
        }
    }

    private CommonArgs getCommonArgs(Parameter[] params, Object[] args) {
        String token = ThreadContext.getToken();
        String channel = ThreadContext.getChannel();
        String channelNo = ThreadContext.getChannelNo();
        if (StringUtils.isEmpty(token) || StringUtils.isEmpty(channel) || StringUtils.isEmpty(channelNo)) {
            for (int i = 0; i < params.length; i++) {
                Parameter param = params[i];
                if (param.getAnnotation(RequestBody.class) == null)
                    continue;
                Class<?> paramType = param.getType();
                if (Map.class.isAssignableFrom(paramType)) {
                    Map map = (Map) args[i];
                    if (map == null) {
                        if (paramType.isAssignableFrom(HashMap.class))
                            args[i] = new HashMap<String, Object>();
                        else if (paramType.isAssignableFrom(LinkedHashMap.class))
                            args[i] = new LinkedHashMap<String, Object>();
                        continue;
                    }
                    if (StringUtils.isEmpty(token))
                        token = Convert.toString(map.get(NAME_TOKEN));
                    if (StringUtils.isEmpty(channel))
                        channel = Convert.toString(map.get(NAME_CHANNEL));
                    if (StringUtils.isEmpty(channelNo))
                        channelNo = Convert.toString(map.get(NAME_CHANNEL_NO));
                } else {
                    Object arg = args[i];
                    if (arg == null) {
                        args[i] = newInstance(paramType);
                        continue;
                    }
                    if (StringUtils.isEmpty(token))
                        token = Convert.toString(invokeGetMethod(arg, GET_TOKEN));
                    if (StringUtils.isEmpty(channel))
                        channel = Convert.toString(invokeGetMethod(arg, GET_CHANNEL));
                    if (StringUtils.isEmpty(channelNo))
                        channelNo = Convert.toString(invokeGetMethod(arg, GET_CHANNEL_NO));
                }
            }
        }
        return new CommonArgs(token, channel, channelNo);
    }

    @SuppressWarnings("unchecked")
    private void setCommonArgs(Parameter[] params, String[] paramNames, Object[] args, CommonArgs commonArgs) {
        String token = commonArgs.getToken();
        String channel = commonArgs.getChannel();
        String channelNo = commonArgs.getChannelNo();
        //上下文
        ThreadContext.modify(token, channel, channelNo);
        //参数
        for (int i = 0; i < params.length; i++) {
            Class<?> paramType = params[i].getType();
            String paramName = paramNames[i];
            switch (paramName) {
                case NAME_TOKEN:
                    if (paramType.isAssignableFrom(String.class))
                        args[i] = token;
                    break;
                case NAME_CHANNEL:
                    if (paramType.isAssignableFrom(String.class))
                        args[i] = channel;
                    break;
                case NAME_CHANNEL_NO:
                    if (paramType.isAssignableFrom(String.class))
                        args[i] = channelNo;
                    break;
                default:
                    Object arg = args[i];
                    if (arg == null)
                        break;
                    if (arg instanceof Map) {
                        Map<String, Object> map = (Map<String, Object>) arg;
                        map.put(NAME_TOKEN, token);
                        map.put(NAME_CHANNEL, channel);
                        map.put(NAME_CHANNEL_NO, channelNo);
                    } else {
                        invokeSetMethod(arg, SET_TOKEN, token);
                        invokeSetMethod(arg, SET_CHANNEL, channel);
                        invokeSetMethod(arg, SET_CHANNEL_NO, channelNo);
                    }
                    break;
            }
        }
    }

    private CommonArgs updateArgs(Parameter[] params, String[] paramNames, Object[] args) {
        if (!ThreadContext.exists())
            throw new RuntimeException("must init thread context first");
        CommonArgs commonArgs = this.getCommonArgs(params, args);//合并参数
        this.setCommonArgs(params, paramNames, args, commonArgs);//更新参数
        return commonArgs;
    }

    @Pointcut("execution(* com.haiercash..*.*(..)) && (@annotation(org.springframework.web.bind.annotation.RequestMapping)" +
            " || @annotation(org.springframework.web.bind.annotation.GetMapping)" +
            " || @annotation(org.springframework.web.bind.annotation.DeleteMapping)" +
            " || @annotation(org.springframework.web.bind.annotation.PostMapping)" +
            " || @annotation(org.springframework.web.bind.annotation.PutMapping))")
    private void doControllerPointcut() {
    }

    @Around(value = "doControllerPointcut()")
    public Object doController(ProceedingJoinPoint joinPoint) throws Throwable {
        //获取实例
        Object target = joinPoint.getTarget();
        if (!(target instanceof BaseController))
            throw new RuntimeException(String.format("%s must extends %s", target.getClass(), BaseController.class));
        BaseController controller = (BaseController) target;
        //获取方法
        Signature signature = joinPoint.getSignature();
        if (!(signature instanceof MethodSignature))
            throw new RuntimeException("join point signature must be " + MethodSignature.class);
        MethodSignature methodSignature = (MethodSignature) signature;
        //更新参数
        Parameter[] params = methodSignature.getMethod().getParameters();
        String[] paramNames = methodSignature.getParameterNames();
        Object[] args = joinPoint.getArgs();
        CommonArgs commonArgs = this.updateArgs(params, paramNames, args);
        //执行
        ThreadContext.enterController(controller);//进入
        String action = signature.toLongString();
        this.logger.info(String.format("==>@RequestMapping Begin: %s token: %s channel: %s channelNo: %s", action, commonArgs.getToken(), commonArgs.getChannel(), commonArgs.getChannelNo()));
        long begin = System.currentTimeMillis();
        try {
            return joinPoint.proceed(args);
        } finally {
            this.logger.info(String.format("==>@RequestMapping End: %s Took: %d", action, System.currentTimeMillis() - begin));
            ThreadContext.exitController();//退出
        }
    }

    @Data
    @AllArgsConstructor
    private static final class CommonArgs {
        private String token;
        private String channel;
        private String channelNo;
    }
}
