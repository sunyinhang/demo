package com.haiercash.core.lang;

import com.haiercash.core.collection.ThreadLocalHashPool;
import org.apache.commons.lang.NullArgumentException;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by 许崇雷 on 2016/6/17.
 * 类型转换
 */
public final class DateUtils extends org.apache.commons.lang3.time.DateUtils {
    private static final String YYYY_MM_DD = "yyyy-MM-dd";
    private static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
    private static final String YYYY_MM_DD_HH_MM_SS_SSS = "yyyy-MM-dd HH:mm:ss.SSS";
    private static final String HH_MM_SS = "HH:mm:ss";
    private static final ThreadLocalHashPool<String, SimpleDateFormat> FORMAT_POOL = ThreadLocalHashPool.withInitial(SimpleDateFormat::new);

    /**
     * 获取 SimpleDateFormat 实例
     *
     * @return yyyy-MM-dd 格式
     */
    public static SimpleDateFormat dateFormat() {
        return FORMAT_POOL.get(YYYY_MM_DD);
    }

    /**
     * 获取 SimpleDateFormat 实例
     *
     * @return yyyy-MM-dd HH:mm:ss 格式
     */
    public static SimpleDateFormat dateTimeFormat() {
        return FORMAT_POOL.get(YYYY_MM_DD_HH_MM_SS);
    }

    /**
     * 获取 SimpleDateFormat 实例
     *
     * @return yyyy-MM-dd HH:mm:ss.SSS 格式
     */
    public static SimpleDateFormat dateTimeMsFormat() {
        return FORMAT_POOL.get(YYYY_MM_DD_HH_MM_SS_SSS);
    }

    /**
     * 获取 SimpleDateFormat 实例
     *
     * @return HH:mm:ss 格式
     */
    public static SimpleDateFormat timeFormat() {
        return FORMAT_POOL.get(HH_MM_SS);
    }

    /**
     * 获取 SimpleDateFormat 实例
     *
     * @param format 格式
     * @return 时间格式
     */
    public static SimpleDateFormat getFormat(String format) {
        return FORMAT_POOL.get(format);
    }

    /**
     * 获取操作系统当前时间戳
     *
     * @return
     */
    public static Timestamp now() {
        return new Timestamp(System.currentTimeMillis());
    }

    /**
     * 获取操作系统当前日期
     *
     * @return
     */
    @SuppressWarnings("deprecation")
    public static Timestamp nowDate() {
        Timestamp now = now();
        return new Timestamp(now.getYear(), now.getMonth(), now.getDate(), 0, 0, 0, 0);
    }

    /**
     * 获取操作系统当前时间戳的字符串格式 yyyy-MM-dd
     *
     * @return
     */
    public static String nowDateString() {
        return DateUtils.toDateString(now());
    }

    /**
     * 获取操作系统当前时间戳的字符串格式 yyyy-MM-dd HH:mm:ss
     *
     * @return
     */
    public static String nowDateTimeString() {
        return DateUtils.toDateTimeString(now());
    }

    /**
     * 获取操作系统当前时间戳的字符串格式 yyyy-MM-dd HH:mm:ss.SSS
     *
     * @return
     */
    public static String nowDateTimeMsString() {
        return DateUtils.toDateTimeMsString(now());
    }

    /**
     * 获取操作系统当前时间戳的字符串格式 HH:mm:ss
     *
     * @return
     */
    public static String nowTimeString() {
        return DateUtils.toTimeString(now());
    }

    /**
     * 获取操作系统当前时间戳的字符串格式
     *
     * @param format
     * @return
     */
    public static String nowString(String format) {
        return DateUtils.toString(now(), format);
    }

    /**
     * 时间转换为 yyyy-MM-dd 字符串
     *
     * @param value
     * @return
     */
    public static String toDateString(Date value) {
        return value == null ? null : dateFormat().format(value);
    }

    /**
     * 时间转换为 yyyy-MM-dd HH:mm:ss 字符串
     *
     * @param value
     * @return
     */
    public static String toDateTimeString(Date value) {
        return value == null ? null : dateTimeFormat().format(value);
    }

    /**
     * 时间转换为 yyyy-MM-dd HH:mm:ss.SSS 字符串
     *
     * @param value
     * @return
     */
    public static String toDateTimeMsString(Date value) {
        return value == null ? null : dateTimeMsFormat().format(value);
    }

    /**
     * 时间转换为 HH:mm:ss 字符串
     *
     * @param value
     * @return
     */
    public static String toTimeString(Date value) {
        return value == null ? null : timeFormat().format(value);
    }

    /**
     * 时间转换为指定格式的字符串
     *
     * @param value
     * @param format
     * @return
     */
    public static String toString(Date value, String format) {
        if (format == null)
            throw new NullArgumentException("format");
        return value == null ? null : getFormat(format).format(value);
    }

    /**
     * yyyy-MM-dd 格式字符串转换为时间
     *
     * @param value
     * @return
     */
    public static Date fromDateString(String value) {
        try {
            return value == null ? null : dateFormat().parse(value);
        } catch (ParseException e) {
            throw new ClassCastException("can not convert \"" + value + "\" to Date");
        }
    }

    /**
     * yyyy-MM-dd HH:mm:ss 格式字符串转换为时间
     *
     * @param value
     * @return
     */
    public static Date fromDateTimeString(String value) {
        try {
            return value == null ? null : dateTimeFormat().parse(value);
        } catch (ParseException e) {
            throw new ClassCastException("can not convert \"" + value + "\" to Date");
        }
    }

    /**
     * yyyy-MM-dd HH:mm:ss.SSS 格式字符串转换为时间
     *
     * @param value
     * @return
     */
    public static Date fromDateTimeMsString(String value) {
        try {
            return value == null ? null : dateTimeMsFormat().parse(value);
        } catch (ParseException e) {
            throw new ClassCastException("can not convert \"" + value + "\" to Date");
        }
    }

    /**
     * 字符串转换为时间
     *
     * @param value
     * @param format
     * @return
     */
    public static Date fromString(String value, String format) {
        if (format == null)
            throw new NullArgumentException("format");
        try {
            return value == null ? null : getFormat(format).parse(value);
        } catch (ParseException e) {
            throw new ClassCastException("can not convert \"" + value + "\" to Date");
        }
    }

    /**
     * 对象自动转换为时间
     *
     * @param value 支持 Date/Long/yyyy-MM-dd/yyyy-MM-dd HH:mm:ss/yyyy-MM-dd HH:mm:ss.SSS
     * @return
     */
    public static Date fromString(Object value) {
        if (value instanceof Date)
            return (Date) value;
        String str = ObjectUtils.toString(value);
        if (StringUtils.isEmpty(str))
            return null;
        Long num = null;
        try {
            num = Long.parseLong(str);
        } catch (Exception ignored) {
        }
        try {
            return num == null ? (str.length() < 11 ? dateFormat().parse(str) : (str.length() < 20 ? dateTimeFormat().parse(str) : dateTimeMsFormat().parse(str))) : new Date(num);
        } catch (Exception e) {
            throw new ClassCastException("can not convert \"" + str + "\" to Date");
        }
    }
}
