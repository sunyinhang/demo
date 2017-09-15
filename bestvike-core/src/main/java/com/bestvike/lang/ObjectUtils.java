package com.bestvike.lang;

/**
 * @author 许崇雷
 * @date 2017/5/26
 */
public class ObjectUtils extends org.apache.commons.lang3.ObjectUtils {
    /**
     * 是否为 null 或空
     *
     * @param obj 要判断的对象
     * @return 为 null 或空返回 true,否则返回 false
     */
    public static boolean isEmpty(Object obj) {
        return StringUtils.EMPTY.equals(toString(obj));
    }

    /**
     * 对象转换为字符串,如果结果为 null 则返回 empty.
     *
     * @param obj
     * @return
     */
    public static String toString(Object obj) {
        return toString(obj, StringUtils.EMPTY);
    }

    /**
     * 对象转换为字符串,如果结果为 null 或 empty 则返回 {@code defaultStr}
     *
     * @param obj
     * @param defaultStr
     * @return
     */
    public static String toString(Object obj, String defaultStr) {
        if (obj == null)
            return defaultStr;
        String str = obj.toString();
        return str == null || str.length() == 0 ? defaultStr : str;
    }
}
