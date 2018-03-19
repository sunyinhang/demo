package com.haiercash.core.time;

import com.haiercash.core.collection.ThreadLocalHashPool;
import com.haiercash.core.lang.ObjectUtils;
import com.haiercash.core.lang.StringUtils;
import org.apache.commons.lang.NullArgumentException;
import org.springframework.util.Assert;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 日期工具
 * Created by 许崇雷 on 2016/6/17.
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
     * @return 当前时间戳
     */
    public static Timestamp now() {
        return new Timestamp(System.currentTimeMillis());
    }

    /**
     * 获取操作系统当前日期
     *
     * @return 当前日期
     */
    public static Timestamp nowDate() {
        Calendar calendar = nowCalendar();
        truncateToDate(calendar);
        return new Timestamp(calendar.getTimeInMillis());
    }

    /**
     * 获取当前时间
     *
     * @return 当前日历
     */
    public static Calendar nowCalendar() {
        Calendar calendar = Calendar.getInstance();
        calendar.setLenient(false);
        return calendar;
    }

    /**
     * 获取操作系统当前时间戳的字符串格式 yyyy-MM-dd
     *
     * @return 字符串
     */
    public static String nowDateString() {
        return DateUtils.toDateString(now());
    }

    /**
     * 获取操作系统当前时间戳的字符串格式 yyyy-MM-dd HH:mm:ss
     *
     * @return 字符串
     */
    public static String nowDateTimeString() {
        return DateUtils.toDateTimeString(now());
    }

    /**
     * 获取操作系统当前时间戳的字符串格式 yyyy-MM-dd HH:mm:ss.SSS
     *
     * @return 字符串
     */
    public static String nowDateTimeMsString() {
        return DateUtils.toDateTimeMsString(now());
    }

    /**
     * 获取操作系统当前时间戳的字符串格式 HH:mm:ss
     *
     * @return 字符串
     */
    public static String nowTimeString() {
        return DateUtils.toTimeString(now());
    }

    /**
     * 获取操作系统当前时间戳的字符串格式
     *
     * @param format 格式
     * @return 字符串
     */
    public static String nowString(String format) {
        return DateUtils.toString(now(), format);
    }

    /**
     * 时间转换为 yyyy-MM-dd 字符串
     *
     * @param value 时间对象
     * @return 字符串
     */
    public static String toDateString(Date value) {
        return value == null ? null : dateFormat().format(value);
    }

    /**
     * 时间转换为 yyyy-MM-dd HH:mm:ss 字符串
     *
     * @param value 时间对象
     * @return 字符串
     */
    public static String toDateTimeString(Date value) {
        return value == null ? null : dateTimeFormat().format(value);
    }

    /**
     * 时间转换为 yyyy-MM-dd HH:mm:ss.SSS 字符串
     *
     * @param value 时间对象
     * @return 字符串
     */
    public static String toDateTimeMsString(Date value) {
        return value == null ? null : dateTimeMsFormat().format(value);
    }

    /**
     * 时间转换为 HH:mm:ss 字符串
     *
     * @param value 时间对象
     * @return 字符串
     */
    public static String toTimeString(Date value) {
        return value == null ? null : timeFormat().format(value);
    }

    /**
     * 时间转换为指定格式的字符串
     *
     * @param value  时间对象
     * @param format 格式
     * @return 字符串
     */
    public static String toString(Date value, String format) {
        if (format == null)
            throw new NullArgumentException("format");
        return value == null ? null : getFormat(format).format(value);
    }

    /**
     * yyyy-MM-dd 格式字符串转换为时间
     *
     * @param value 字符串
     * @return 时间对象
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
     * @param value 字符串
     * @return 时间对象
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
     * @param value 字符串
     * @return 时间对象
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
     * @param value  字符串
     * @param format 时间格式
     * @return 时间对象
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
     * @return 时间对象
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
     * 转换为 Calendar
     *
     * @param value 时间
     * @return 日历对象
     */
    public static Calendar toCalendar(Date value) {
        Calendar calendar = nowCalendar();
        calendar.setTime(value);
        return calendar;
    }

    /**
     * 截断到日期,时分秒毫秒设置为 0
     *
     * @param calendar 日历对象
     */
    public static void truncateToDate(Calendar calendar) {
        Assert.notNull(calendar, "calendar can not be null");
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    /**
     * 修改指定时间的时分秒毫秒,如果指定为第二天则天加一天
     *
     * @param value 时间
     * @param time  新的时分秒
     * @return 新的时间对象
     */
    public static Date set(Date value, Time time) {
        Assert.notNull(value, "value can not be null");
        Assert.notNull(time, "time can not be null");
        Calendar calendar = nowCalendar();
        calendar.setTime(value);
        if (time.isTomorrow())
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, time.getHour());
        calendar.set(Calendar.MINUTE, time.getMinute());
        calendar.set(Calendar.SECOND, time.getSecond());
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
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
        Calendar calendar = nowCalendar();
        calendar.setTime(value);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        calendar.set(Calendar.MILLISECOND, millisecond);
        return calendar.getTime();
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
        Assert.notNull(value, "value can not be null");
        Calendar calendar = nowCalendar();
        calendar.setTime(value);
        calendar.add(Calendar.MILLISECOND, (int) timeSpan.getTotalMilliseconds());
        return calendar.getTime();
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
        Calendar calendar = nowCalendar();
        calendar.setTime(value);
        calendar.add(Calendar.DAY_OF_MONTH, day);
        calendar.add(Calendar.HOUR_OF_DAY, hour);
        calendar.add(Calendar.MINUTE, minute);
        calendar.add(Calendar.SECOND, second);
        calendar.add(Calendar.MILLISECOND, millisecond);
        return calendar.getTime();
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
