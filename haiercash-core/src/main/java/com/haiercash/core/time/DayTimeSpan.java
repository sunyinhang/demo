package com.haiercash.core.time;

import lombok.Data;
import org.springframework.util.Assert;

import java.util.Date;

/**
 * 每天的时间段
 * Created by 许崇雷 on 2018-03-13.
 */
@Data
public final class DayTimeSpan {
    private final DayTime begin;
    private final DayTime end;

    public DayTimeSpan(DayTime begin, DayTime end) {
        Assert.notNull(begin, "begin can not be null");
        Assert.notNull(end, "end can not be null");
        this.begin = begin;
        this.end = end;
    }

    public boolean contains(DayTime time) {
        Assert.notNull(time, "time can not be null");
        DayTime todayTime = time.todayTime();
        DayTime tomorrowTime = time.tomorrowTime();
        return this.begin.compareTo(todayTime) <= 0 && todayTime.compareTo(this.end) < 0
                || this.begin.compareTo(tomorrowTime) <= 0 && tomorrowTime.compareTo(this.end) < 0;
    }

    @SuppressWarnings("deprecation")
    public boolean contains(Date date) {
        return this.contains(new DayTime(date.getHours(), date.getMinutes(), date.getSeconds()));
    }
}
