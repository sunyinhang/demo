package com.haiercash.core.lang;

import com.haiercash.core.time.DayTime;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

/**
 * Created by 许崇雷 on 2018-03-13.
 */
public class DateUtilsTest {
    private static final Date DATE = DateUtils.fromDateTimeString("2018-01-02 03:04:05");

    @Test
    public void set() {
        Date date = DateUtils.set(DATE, new DayTime(true, 5, 6, 7));
        Assert.assertEquals("2018-01-03 05:06:07.000", DateUtils.toDateTimeMsString(date));
    }

    @Test
    public void set1() {
        Date date = DateUtils.set(DATE, 5, 6, 7, 789);
        Assert.assertEquals("2018-01-02 05:06:07.789", DateUtils.toDateTimeMsString(date));
    }

    @Test
    public void add() {
        Date date = DateUtils.add(DATE, new TimeSpan(1, 2, 3, 4, 123));
        Assert.assertEquals("2018-01-03 05:07:09.123", DateUtils.toDateTimeMsString(date));
    }

    @Test
    public void add1() {
        Date date = DateUtils.add(DATE, 1, 2, 3, 4, 123);
        Assert.assertEquals("2018-01-03 05:07:09.123", DateUtils.toDateTimeMsString(date));
    }
}
