package com.haiercash.core.time;

import lombok.Data;
import org.springframework.util.Assert;

import java.util.Date;

/**
 * 时间范围
 * Created by 许崇雷 on 2018-03-13.
 */
@Data
public final class TimeRange {
    private final Time begin;
    private final Time end;

    /**
     * 构造函数
     *
     * @param begin 起始时间
     * @param end   截止时间
     */
    public TimeRange(Time begin, Time end) {
        Assert.notNull(begin, "begin can not be null");
        Assert.notNull(end, "end can not be null");
        this.begin = begin;
        this.end = end;
    }

    /**
     * 时间范围是否包含指定时间
     *
     * @param time 时间
     * @return 包含返回 true, 否则返回 false
     */
    public boolean contains(Time time) {
        Assert.notNull(time, "time can not be null");
        Time todayTime = time.todayTime();
        Time tomorrowTime = time.tomorrowTime();
        return this.begin.compareTo(todayTime) <= 0 && todayTime.compareTo(this.end) < 0
                || this.begin.compareTo(tomorrowTime) <= 0 && tomorrowTime.compareTo(this.end) < 0;
    }

    /**
     * 时间范围是否包含指定时间
     *
     * @param date 时间
     * @return 包含返回 true, 否则返回 false
     */
    public boolean contains(Date date) {
        return this.contains(new Time(date));
    }
}
