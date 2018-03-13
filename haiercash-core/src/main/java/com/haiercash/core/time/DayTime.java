package com.haiercash.core.time;

import lombok.Data;
import org.springframework.util.Assert;

/**
 * 每天中的某个时刻
 * Created by 许崇雷 on 2018-03-13.
 */
@Data
public final class DayTime implements Comparable<DayTime> {
    private static final int SECONDS_PER_DAY = 60 * 60 * 24;
    private static final int SECONDS_PER_HOUR = 60 * 60;
    private static final int SECONDS_PER_MINUTE = 60;
    private final boolean tomorrow;
    private final int hour;
    private final int minute;
    private final int second;

    /**
     * 构造函数
     *
     * @param tomorrow 是否明天
     * @param hour     时
     * @param minute   分
     * @param second   秒
     */
    public DayTime(boolean tomorrow, int hour, int minute, int second) {
        Assert.state(hour >= 0 && hour < 24, "hour must in [0,24)");
        Assert.state(minute >= 0 && minute < 60, "minute must in [0,60)");
        Assert.state(second >= 0 && second < 60, "second must in [0,60)");
        this.tomorrow = tomorrow;
        this.hour = hour;
        this.minute = minute;
        this.second = second;
    }

    /**
     * 构造函数
     *
     * @param hour   时
     * @param minute 分
     * @param second 秒
     */
    public DayTime(int hour, int minute, int second) {
        this(false, hour, minute, second);
    }

    /**
     * 获取今天的同一时刻
     *
     * @return 今天的同一时刻
     */
    public DayTime todayTime() {
        return this.tomorrow ? new DayTime(false, this.hour, this.minute, this.second) : this;
    }

    /**
     * 获取明天的同一时刻
     *
     * @return 明天的同一时刻
     */
    public DayTime tomorrowTime() {
        return this.tomorrow ? this : new DayTime(true, this.hour, this.minute, this.second);
    }

    /**
     * 比较
     *
     * @param that 其他示例
     * @return 比较结果
     */
    @Override
    public int compareTo(DayTime that) {
        Assert.notNull(that, "that can not be null");
        int tomorrowCom;
        int hourCom;
        int minuteCom;
        return (tomorrowCom = Boolean.compare(this.tomorrow, that.tomorrow)) == 0
                ? ((hourCom = Integer.compare(this.hour, that.hour)) == 0 ? ((minuteCom = Integer.compare(this.minute, that.minute)) == 0 ? Integer.compare(this.second, that.second) : minuteCom) : hourCom)
                : tomorrowCom;
    }
}
