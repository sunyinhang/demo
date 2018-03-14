package com.haiercash.core.time;

import com.haiercash.core.lang.DateUtils;
import lombok.Data;
import org.springframework.util.Assert;

import java.util.Calendar;
import java.util.Date;

/**
 * 不含日期的时间, 可表示次日
 * Created by 许崇雷 on 2018-03-13.
 */
@Data
public final class Time implements Comparable<Time> {
    private final boolean tomorrow;
    private final int hour;
    private final int minute;
    private final int second;
    private final int millisecond;

    /**
     * 构造函数
     *
     * @param tomorrow    是否明天
     * @param hour        时
     * @param minute      分
     * @param second      秒
     * @param millisecond 毫秒
     */
    public Time(boolean tomorrow, int hour, int minute, int second, int millisecond) {
        Assert.state(hour >= 0 && hour < 24, "hour must in [0,24)");
        Assert.state(minute >= 0 && minute < 60, "minute must in [0,60)");
        Assert.state(second >= 0 && second < 60, "second must in [0,60)");
        Assert.state(millisecond >= 0 && millisecond < 1000, "millisecond must in [0,1000)");
        this.tomorrow = tomorrow;
        this.hour = hour;
        this.minute = minute;
        this.second = second;
        this.millisecond = millisecond;
    }

    /**
     * 构造函数
     *
     * @param tomorrow 是否明天
     * @param hour     时
     * @param minute   分
     * @param second   秒
     */
    public Time(boolean tomorrow, int hour, int minute, int second) {
        this(tomorrow, hour, minute, second, 0);
    }

    /**
     * 构造函数
     *
     * @param hour        时
     * @param minute      分
     * @param second      秒
     * @param millisecond 毫秒
     */
    public Time(int hour, int minute, int second, int millisecond) {
        this(false, hour, minute, second, millisecond);
    }

    /**
     * 构造函数
     *
     * @param hour   时
     * @param minute 分
     * @param second 秒
     */
    public Time(int hour, int minute, int second) {
        this(false, hour, minute, second, 0);
    }

    /**
     * 构造函数
     *
     * @param date 时间
     */
    public Time(Date date) {
        Assert.notNull(date, "date can not be null");
        Calendar calendar = DateUtils.toCalendar(date);
        this.tomorrow = false;
        this.hour = calendar.get(Calendar.HOUR_OF_DAY);
        this.minute = calendar.get(Calendar.MINUTE);
        this.second = calendar.get(Calendar.SECOND);
        this.millisecond = calendar.get(Calendar.MILLISECOND);
    }

    /**
     * 获取今天的同一时刻
     *
     * @return 今天的同一时刻
     */
    public Time todayTime() {
        return this.tomorrow ? new Time(false, this.hour, this.minute, this.second, this.millisecond) : this;
    }

    /**
     * 获取明天的同一时刻
     *
     * @return 明天的同一时刻
     */
    public Time tomorrowTime() {
        return this.tomorrow ? this : new Time(true, this.hour, this.minute, this.second, this.millisecond);
    }

    /**
     * 比较
     *
     * @param that 其他示例
     * @return 比较结果
     */
    @Override
    public int compareTo(Time that) {
        Assert.notNull(that, "that can not be null");

        int tomorrowCom = Boolean.compare(this.tomorrow, that.tomorrow);
        if (tomorrowCom != 0)
            return tomorrowCom;

        int hourCom = Integer.compare(this.hour, that.hour);
        if (hourCom != 0)
            return hourCom;

        int minuteCom = Integer.compare(this.minute, that.minute);
        if (minuteCom != 0)
            return minuteCom;

        int secondCom = Integer.compare(this.second, that.second);
        if (secondCom != 0)
            return secondCom;

        return Integer.compare(this.millisecond, that.millisecond);
    }
}
