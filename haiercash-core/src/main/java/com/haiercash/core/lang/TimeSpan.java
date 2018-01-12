package com.haiercash.core.lang;

import java.util.Date;

/**
 * Created by 许崇雷 on 2016/5/27.
 * 时间跨度
 */
public final class TimeSpan {
    public static final long MillisecondsPerSecond = 1000;
    public static final long MillisecondsPerMinute = MillisecondsPerSecond * 60;
    public static final long MillisecondsPerHour = MillisecondsPerMinute * 60;
    public static final long MillisecondsPerDay = MillisecondsPerHour * 24;
    private static final double SecondsPerMillisecond = 1.0d / MillisecondsPerSecond;
    private static final double MinutesPerMillisecond = 1.0d / MillisecondsPerMinute;
    private static final double HoursPerMillisecond = 1.0d / MillisecondsPerHour;
    private static final double DaysPerMillisecond = 1.0d / MillisecondsPerDay;

    private final long milliseconds;//总差异毫秒数

    /**
     * 构造函数
     *
     * @param timeLeft  被减数
     * @param timeRight 减数
     */
    public TimeSpan(Date timeLeft, Date timeRight) {
        this.milliseconds = timeLeft.getTime() - timeRight.getTime();
    }

    /**
     * 获取差别的天数
     *
     * @return
     */
    public int getDays() {
        return (int) (this.milliseconds / MillisecondsPerDay);
    }

    /**
     * 获取差别的小时数
     *
     * @return
     */
    public int getHours() {
        return (int) ((this.milliseconds / MillisecondsPerHour) % 24);
    }

    /**
     * 获取差别的分钟数
     *
     * @return
     */
    public int getMinutes() {
        return (int) ((this.milliseconds / MillisecondsPerMinute) % 60);
    }

    /**
     * 获取差别的秒数
     *
     * @return
     */
    public int getSeconds() {
        return (int) ((this.milliseconds / MillisecondsPerSecond) % 60);
    }

    /**
     * 获取差别的毫秒数
     *
     * @return
     */
    public int getMilliseconds() {
        return (int) (this.milliseconds % 1000);
    }

    /**
     * 获取总计差别多少天
     *
     * @return
     */
    public double getTotalDays() {
        return ((double) this.milliseconds) * DaysPerMillisecond;
    }

    /**
     * 获取总计差别的小时数
     *
     * @return
     */
    public double getTotalHours() {
        return (double) this.milliseconds * HoursPerMillisecond;
    }

    /**
     * 获取总计差别的分钟数
     *
     * @return
     */
    public double getTotalMinutes() {
        return (double) this.milliseconds * MinutesPerMillisecond;
    }

    /**
     * 获取总计差别的秒数
     *
     * @return
     */
    public double getTotalSeconds() {
        return (double) this.milliseconds * SecondsPerMillisecond;
    }

    /**
     * 获取总计差别的毫秒数
     *
     * @return
     */
    public double getTotalMilliseconds() {
        return this.milliseconds;
    }
}
