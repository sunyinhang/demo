package com.haiercash.core.serialization.xml;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.haiercash.core.lang.Convert;
import com.haiercash.core.lang.StringUtils;
import com.haiercash.core.reflect.ReflectionUtils;
import org.w3c.dom.Element;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * JSON格式转换器。
 *
 * @author carver.gu
 * @since 1.0, Apr 11, 2010
 */
public final class XmlConverter {
    private XmlConverter() {
    }

    public static <T> T deserialize(String xml, Class<T> clazz) {
        Element root = DomUtils.getRootElementFromString(xml);
        return convert(root, clazz);
    }

    private static <T> T convert(final Element element, Class<T> clazz) {
        if (element == null)
            return null;
        return convert(new DefaultReader(element), clazz);
    }

    private static <T> T convert(Reader reader, Class<T> clazz) {
        try {
            T result = clazz.newInstance();
            Field[] fields = ReflectionUtils.getDeclaredFieldInfos(clazz);
            for (Field field : fields) {
                int modifiers = field.getModifiers();
                if (Modifier.isStatic(modifiers) || Modifier.isFinal(modifiers))
                    continue;
                //获取字段名
                String fieldName = field.getName();
                JacksonXmlProperty xmlProperty = field.getAnnotation(JacksonXmlProperty.class);
                if (xmlProperty != null && StringUtils.isNotEmpty(xmlProperty.localName()))
                    fieldName = xmlProperty.localName();
                //xml node 不存在
                if (!reader.hasReturnField(fieldName))
                    continue;
                field.setAccessible(true);
                Class<?> typeClass = field.getType();
                Object rawValue = reader.getPrimitiveObject(fieldName);
                Object value = Convert.nullType(rawValue, typeClass);
                if (value != null) {
                    field.set(result, value);
                } else if (List.class.isAssignableFrom(typeClass)) {
                    Type fieldType = field.getGenericType();
                    if (fieldType instanceof ParameterizedType) {
                        ParameterizedType paramType = (ParameterizedType) fieldType;
                        Type[] genericTypes = paramType.getActualTypeArguments();
                        if (genericTypes != null && genericTypes.length > 0) {
                            if (genericTypes[0] instanceof Class<?>) {
                                Class<?> subType = (Class<?>) genericTypes[0];
                                String itemName = "*";
                                JacksonXmlElementWrapper xmlElementWrapper = field.getAnnotation(JacksonXmlElementWrapper.class);
                                if (xmlElementWrapper != null && StringUtils.isNotEmpty(xmlElementWrapper.localName()))
                                    itemName = xmlElementWrapper.localName();
                                List<?> list = reader.getListObjects(fieldName, itemName, subType);
                                if (list != null)
                                    field.set(result, list);
                            }
                        }
                    }
                } else {
                    Object obj = reader.getObject(fieldName, typeClass);
                    if (obj != null)
                        field.set(result, obj);
                }
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static final class DefaultReader implements Reader {
        private final Element element;

        DefaultReader(Element element) {
            this.element = element;
        }

        public boolean hasReturnField(Object name) {
            Element childE = DomUtils.getChildElement(this.element, (String) name);
            return childE != null;
        }

        public Object getPrimitiveObject(Object name) {
            return DomUtils.getElementValue(this.element, (String) name);
        }

        public Object getObject(Object name, Class<?> type) {
            Element childE = DomUtils.getChildElement(this.element, (String) name);
            return childE == null ? null : convert(childE, type);
        }

        public List<?> getListObjects(Object listName, Object itemName, Class<?> subType) {
            Element listE = DomUtils.getChildElement(this.element, (String) listName);
            if (listE == null)
                return null;
            List<Object> list = new ArrayList<>();
            List<Element> itemEs = DomUtils.getChildElements(listE, (String) itemName);
            for (Element itemE : itemEs) {
                String rawValue = DomUtils.getElementValue(itemE);
                Object value = Convert.nullType(rawValue, subType);
                if (value == null)
                    value = convert(itemE, subType);
                if (value != null)
                    list.add(value);
            }
            return list;
        }
    }
}
