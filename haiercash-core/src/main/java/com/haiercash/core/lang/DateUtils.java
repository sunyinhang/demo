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
        return value == null ? null : FORMAT_POOL.get(YYYY_MM_DD).format(value);
    }

    /**
     * 时间转换为 yyyy-MM-dd HH:mm:ss 字符串
     *
     * @param value
     * @return
     */
    public static String toDateTimeString(Date value) {
        return value == null ? null : FORMAT_POOL.get(YYYY_MM_DD_HH_MM_SS).format(value);
    }

    /**
     * 时间转换为 yyyy-MM-dd HH:mm:ss.SSS 字符串
     *
     * @param value
     * @return
     */
    public static String toDateTimeMsString(Date value) {
        return value == null ? null : FORMAT_POOL.get(YYYY_MM_DD_HH_MM_SS_SSS).format(value);
    }

    /**
     * 时间转换为 HH:mm:ss 字符串
     *
     * @param value
     * @return
     */
    public static String toTimeString(Date value) {
        return value == null ? null : FORMAT_POOL.get(HH_MM_SS).format(value);
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
        return value == null ? null : FORMAT_POOL.get(format).format(value);
    }

    /**
     * yyyy-MM-dd 格式字符串转换为时间
     *
     * @param value
     * @return
     */
    public static Date fromDateString(String value) {
        try {
            return value == null ? null : FORMAT_POOL.get(YYYY_MM_DD).parse(value);
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
            return value == null ? null : FORMAT_POOL.get(YYYY_MM_DD_HH_MM_SS).parse(value);
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
            return value == null ? null : FORMAT_POOL.get(YYYY_MM_DD_HH_MM_SS_SSS).parse(value);
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
            return value == null ? null : FORMAT_POOL.get(format).parse(value);
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
            return num == null ? (str.length() < 11 ? FORMAT_POOL.get(YYYY_MM_DD).parse(str) : (str.length() < 20 ? FORMAT_POOL.get(YYYY_MM_DD_HH_MM_SS).parse(str) : FORMAT_POOL.get(YYYY_MM_DD_HH_MM_SS_SSS).parse(str))) : new Date(num);
        } catch (Exception e) {
            throw new ClassCastException("can not convert \"" + str + "\" to Date");
        }
    }
}
