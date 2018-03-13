package com.haiercash.core.lang;

import com.haiercash.core.collection.ThreadLocalHashPool;
import com.haiercash.core.time.DayTime;
import org.apache.commons.lang.NullArgumentException;
import org.springframework.util.Assert;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
            return num == null ? str.length() < 11 ? dateFormat().parse(str) : str.length() < 20 ? dateTimeFormat().parse(str) : dateTimeMsFormat().parse(str) : new Date(num);
        } catch (Exception e) {
            throw new ClassCastException("can not convert \"" + str + "\" to Date");
        }
    }

    /**
     * 修改指定时间的时分秒毫秒,如果指定为第二天则天加一天
     *
     * @param value   时间
     * @param dayTime 新的时分秒
     * @return 新的时间对象
     */
    public static Date set(Date value, DayTime dayTime) {
        Assert.notNull(value, "value can not be null");
        Assert.notNull(dayTime, "dayTime can not be null");
        // getInstance() returns a new object, so this method is thread safe.
        Calendar c = Calendar.getInstance();
        c.setLenient(false);
        c.setTime(value);
        if (dayTime.isTomorrow())
            c.add(Calendar.DAY_OF_MONTH, 1);
        c.set(Calendar.HOUR_OF_DAY, dayTime.getHour());
        c.set(Calendar.MINUTE, dayTime.getMinute());
        c.set(Calendar.SECOND, dayTime.getSecond());
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    /**
     * 修改指定时间的时分秒毫秒
     *
     * @param value       时间
     * @param hour        新的时
     * @param minute      新的分
     * @param second      新的秒
     * @param millisecond 新的毫秒
     * @return 新的时间对象
     */
    public static Date set(Date value, int hour, int minute, int second, int millisecond) {
        Assert.notNull(value, "value can not be null");
        // getInstance() returns a new object, so this method is thread safe.
        Calendar c = Calendar.getInstance();
        c.setLenient(false);
        c.setTime(value);
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, second);
        c.set(Calendar.MILLISECOND, millisecond);
        return c.getTime();
    }

    /**
     * 修改指定时间的时分秒毫秒
     *
     * @param value  时间
     * @param hour   新的时
     * @param minute 新的分
     * @param second 新的秒
     * @return 新的时间对象
     */
    public static Date set(Date value, int hour, int minute, int second) {
        return set(value, hour, minute, second, 0);
    }

    /**
     * 时间增加指定的时间跨度
     *
     * @param value    时间
     * @param timeSpan 时间跨度
     * @return 新的时间对象
     */
    public static Date add(Date value, TimeSpan timeSpan) {
        return addMilliseconds(value, (int) timeSpan.getTotalMilliseconds());
    }

    /**
     * 指定的时间追加时分秒毫秒
     *
     * @param value       时间
     * @param day         追加的天
     * @param hour        追加的时
     * @param minute      追加的分
     * @param second      追加的秒
     * @param millisecond 追加的毫秒
     * @return 新的时间对象
     */
    public static Date add(Date value, int day, int hour, int minute, int second, int millisecond) {
        Assert.notNull(value, "value can not be null");
        // getInstance() returns a new object, so this method is thread safe.
        Calendar c = Calendar.getInstance();
        c.setLenient(false);
        c.setTime(value);
        c.add(Calendar.DAY_OF_MONTH, day);
        c.add(Calendar.HOUR_OF_DAY, hour);
        c.add(Calendar.MINUTE, minute);
        c.add(Calendar.SECOND, second);
        c.add(Calendar.MILLISECOND, millisecond);
        return c.getTime();
    }

    /**
     * 指定的时间追加时分秒毫秒
     *
     * @param value       时间
     * @param hour        追加的时
     * @param minute      追加的分
     * @param second      追加的秒
     * @param millisecond 追加的毫秒
     * @return 新的时间对象
     */
    public static Date add(Date value, int hour, int minute, int second, int millisecond) {
        return add(value, 0, hour, minute, second, millisecond);
    }

    /**
     * 指定的时间追加时分秒毫秒
     *
     * @param value  时间
     * @param hour   追加的时
     * @param minute 追加的分
     * @param second 追加的秒
     * @return 新的时间对象
     */
    public static Date add(Date value, int hour, int minute, int second) {
        return add(value, 0, hour, minute, second, 0);
    }
}
