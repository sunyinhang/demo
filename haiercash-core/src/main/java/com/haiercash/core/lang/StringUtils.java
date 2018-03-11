package com.haiercash.core.lang;

import com.haiercash.core.collection.ArrayUtils;
import com.haiercash.core.collection.iterator.CharSequenceIterable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * @author 许崇雷
 * @date 2017/5/26
 */
public final class StringUtils extends org.apache.commons.lang3.StringUtils {
    /**
     * 获取 "\r" 字符串
     */
    public static final String CR = "\r";
    /**
     * 获取 "\n" 字符串
     */
    public static final String LF = "\n";
    private static final int TrimHead = 0;
    private static final int TrimTail = 1;
    private static final int TrimBoth = 2;

    //去字符串两侧空白
    private static String trimCore(String value, int trimType) {
        if (value == null)
            return EMPTY;

        //end will point to the first non-trimmed character on the right
        //start will point to the first non-trimmed character on the Left
        int len = value.length();
        int end = len - 1;
        int start = 0;

        //Trim specified characters.
        if (trimType != TrimTail) {
            for (start = 0; start < len; start++) {
                if (!Character.isWhitespace(value.charAt(start)))
                    break;
            }
        }

        if (trimType != TrimHead) {
            for (end = len - 1; end >= start; end--) {
                if (!Character.isWhitespace(value.charAt(end)))
                    break;
            }
        }

        return value.substring(start, end + 1);
    }

    //去字符串两侧特定字符
    private static String trimCore(String value, char[] trimChars, int trimType) {
        if (value == null)
            return EMPTY;
        //end will point to the first non-trimmed character on the right
        //start will point to the first non-trimmed character on the Left
        int len = value.length();
        int end = len - 1;
        int start = 0;

        //Trim specified characters.
        if (trimType != TrimTail) {
            for (start = 0; start < len; start++) {
                if (!ArrayUtils.contains(trimChars, value.charAt(start)))
                    break;
            }
        }

        if (trimType != TrimHead) {
            for (end = len - 1; end >= start; end--) {
                if (!ArrayUtils.contains(trimChars, value.charAt(end)))
                    break;
            }
        }

        return value.substring(start, end + 1);
    }

    /**
     * 创建字符串
     *
     * @param c     字符
     * @param count 重复次数
     * @return 字符串
     */
    public static String create(char c, int count) {
        char[] cs = new char[count];
        Arrays.fill(cs, c);
        return new String(cs);
    }

    /**
     * 去除字符串两侧字符
     *
     * @param value     字符串
     * @param trimChars 去除的字符,不指定表示去除空白
     * @return 结果
     */
    public static String trim(String value, char... trimChars) {
        return trimChars == null || trimChars.length <= 0 ? trimCore(value, TrimBoth) : trimCore(value, trimChars, TrimBoth);
    }

    /**
     * 去除字符串开头字符
     *
     * @param value     字符串
     * @param trimChars 去除的字符,不指定表示去除空白
     * @return 结果
     */
    public static String trimStart(String value, char... trimChars) {
        return trimChars == null || trimChars.length <= 0 ? trimCore(value, TrimHead) : trimCore(value, trimChars, TrimHead);
    }

    /**
     * 去除结尾字符
     *
     * @param value     字符串
     * @param trimChars 去除的字符,不指定表示去除空白
     * @return 结果
     */
    public static String trimEnd(String value, char... trimChars) {
        return trimChars == null || trimChars.length <= 0 ? trimCore(value, TrimTail) : trimCore(value, trimChars, TrimTail);
    }

    /**
     * 分割字符串,保留空白元素
     *
     * @param value     值
     * @param separator 分隔符
     * @return 分割后字符串数组
     */
    public static String[] split(String value, char[] separator) {
        return split(value, separator, false);
    }

    /**
     * 分割字符串
     *
     * @param value              值
     * @param separator          分隔符
     * @param removeEmptyEntries 是否移除空白元素
     * @return 分割后字符串数组
     */
    public static String[] split(String value, char[] separator, boolean removeEmptyEntries) {
        if (value == null)
            throw new NullPointerException("value is null");
        if (separator == null)
            throw new NullPointerException("separator is null");

        List<String> list = new ArrayList<>();
        StringBuilder temp = new StringBuilder();
        if (removeEmptyEntries) {
            for (char c : new CharSequenceIterable(value)) {
                if (ArrayUtils.contains(separator, c)) {
                    if (temp.length() > 0) {
                        list.add(temp.toString());
                        temp.setLength(0);
                    }
                } else {
                    temp.append(c);
                }
            }
            //最后一个冲刷
            if (temp.length() > 0) {
                list.add(temp.toString());
                temp.setLength(0);
            }
        } else {
            for (char c : new CharSequenceIterable(value)) {
                if (ArrayUtils.contains(separator, c)) {
                    list.add(temp.toString());
                    temp.setLength(0);
                } else {
                    temp.append(c);
                }
            }
            //最后一个冲刷
            list.add(temp.toString());
            temp.setLength(0);
        }
        return list.toArray(new String[0]);
    }

    /**
     * 连接字符串
     *
     * @param separator  连接符
     * @param value      待连接数组
     * @param startIndex 数组开始索引
     * @param count      连接数量
     * @return 连接后字符串
     */
    public static String join(String separator, String[] value, int startIndex, int count) {
        //Range check the array
        if (value == null)
            throw new NullPointerException("value");

        if (startIndex < 0)
            throw new IndexOutOfBoundsException("startIndex");
        if (count < 0)
            throw new IndexOutOfBoundsException("count");

        if (startIndex > value.length - count)
            throw new IndexOutOfBoundsException("startIndex");

        //If count is 0, that skews a whole bunch of the calculations below, so just special case that.
        if (count == 0)
            return EMPTY;

        //Treat null as empty string.
        if (separator == null)
            separator = EMPTY;

        int jointLength = 0;
        //Figure out the total length of the strings in value
        int endIndex = startIndex + count - 1;
        for (int stringToJoinIndex = startIndex; stringToJoinIndex <= endIndex; stringToJoinIndex++) {
            if (value[stringToJoinIndex] != null)
                jointLength += value[stringToJoinIndex].length();
        }

        //Add enough room for the separator.
        jointLength += (count - 1) * separator.length();

        // Note that we may not catch all overflows with this check (since we could have wrapped around the 4gb range any number of times
        // and landed back in the positive range.) The input array might be modifed from other threads,
        // so we have to do an overflow check before each append below anyway. Those overflows will get caught down there.
        if (jointLength < 0 || jointLength + 1 < 0)
            throw new OutOfMemoryError();

        //If this is an empty string, just return.
        if (jointLength == 0)
            return EMPTY;

        StringBuilder jointString = new StringBuilder(jointLength);
        // Append the first string first and then append each following string prefixed by the separator.
        jointString.append(value[startIndex]);
        for (int stringToJoinIndex = startIndex + 1; stringToJoinIndex <= endIndex; stringToJoinIndex++) {
            jointString.append(separator);
            jointString.append(value[stringToJoinIndex]);
        }
        return jointString.toString();
    }

    /**
     * 连接字符串
     *
     * @param separator 连接符
     * @param value     待连接数组
     * @return 连接后字符串
     */
    public static String join(String separator, String... value) {
        if (value == null)
            throw new NullPointerException("value");
        return join(separator, value, 0, value.length);
    }

    /**
     * 连接字符串
     *
     * @param separator 连接符
     * @param values    待连接字符串
     * @return 连接后字符串
     */
    public static String join(String separator, Iterable<String> values) {
        if (values == null)
            throw new NullPointerException("values");
        if (separator == null)
            separator = EMPTY;

        Iterator<String> en = values.iterator();
        if (!en.hasNext())
            return EMPTY;

        StringBuilder result = new StringBuilder();
        String first = en.next();
        if (first != null)
            result.append(first);

        String next;
        while (en.hasNext()) {
            result.append(separator);
            next = en.next();
            if (next != null)
                result.append(next);
        }
        return result.toString();
    }

    /**
     * 首字母大写
     *
     * @param value 值
     * @return 处理后字符串
     */
    public static String capitalize(String value) {
        //为空
        if (isEmpty(value))
            return EMPTY;

        //首字母大写
        char[] chars = value.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        return new String(chars);
    }

    /**
     * 首字母小写,前两个字符都是大写的情况例外.例如 URL => URL
     *
     * @param value 值
     * @return 处理后字符串
     */
    public static String decapitalize(String value) {
        //为空
        if (isEmpty(value))
            return EMPTY;

        //前两个字母大写的保持原样,例如:URL
        if (value.length() > 1 && Character.isUpperCase(value.charAt(0)) && Character.isUpperCase(value.charAt(1)))
            return value;

        //首字母小写
        char[] chars = value.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        return new String(chars);
    }

    /**
     * 过时,使用 decapitalize(String) 方法
     *
     * @param str
     * @return
     */
    @Deprecated
    public static String uncapitalize(String str) {
        return org.apache.commons.lang3.StringUtils.uncapitalize(str);
    }

    /**
     * 去除字符串中的换行符
     *
     * @param value 值
     * @return 处理后字符串
     */
    public static String stripNewLine(String value) {
        if (isEmpty(value))
            return value;
        StringBuilder builder = new StringBuilder(value.length());
        for (char c : new CharSequenceIterable(value)) {
            switch (c) {
                case CharUtils.CR:
                case CharUtils.LF:
                    break;
                default:
                    builder.append(c);
                    break;
            }
        }
        return builder.toString();
    }
}
