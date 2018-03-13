package com.haiercash.core.time;

import com.haiercash.core.lang.DateUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

/**
 * Created by 许崇雷 on 2018-03-13.
 */
public class DayTimeSpanTest {
    private static final DayTimeSpan PROHIBIT = new DayTimeSpan(new DayTime(21, 0, 0), new DayTime(true, 6, 0, 0));
    private static final DayTimeSpan ALLOW = new DayTimeSpan(new DayTime(6, 0, 0), new DayTime(21, 0, 0));
    private static final Date TIME_0 = DateUtils.fromDateTimeString("2018-01-01 00:00:00");
    private static final Date TIME_5 = DateUtils.fromDateTimeString("2018-01-01 05:00:00");
    private static final Date TIME_5_59 = DateUtils.fromDateTimeString("2018-01-01 05:59:59");
    private static final Date TIME_6 = DateUtils.fromDateTimeString("2018-01-01 06:00:00");
    private static final Date TIME_9 = DateUtils.fromDateTimeString("2018-01-01 09:00:00");
    private static final Date TIME_12 = DateUtils.fromDateTimeString("2018-01-01 12:00:00");
    private static final Date TIME_15 = DateUtils.fromDateTimeString("2018-01-01 15:00:00");
    private static final Date TIME_20_59 = DateUtils.fromDateTimeString("2018-01-01 20:59:59");
    private static final Date TIME_21 = DateUtils.fromDateTimeString("2018-01-01 21:00:00");
    private static final Date TIME_22 = DateUtils.fromDateTimeString("2018-01-01 22:00:00");

    @Test
    @SuppressWarnings("ConstantConditions")
    public void contains() {
        Assert.assertTrue(PROHIBIT.contains(TIME_0));
        Assert.assertTrue(PROHIBIT.contains(TIME_5));
        Assert.assertTrue(PROHIBIT.contains(TIME_5_59));
        Assert.assertFalse(PROHIBIT.contains(TIME_6));
        Assert.assertFalse(PROHIBIT.contains(TIME_9));
        Assert.assertFalse(PROHIBIT.contains(TIME_12));
        Assert.assertFalse(PROHIBIT.contains(TIME_15));
        Assert.assertFalse(PROHIBIT.contains(TIME_20_59));
        Assert.assertTrue(PROHIBIT.contains(TIME_21));
        Assert.assertTrue(PROHIBIT.contains(TIME_22));

        Assert.assertFalse(ALLOW.contains(TIME_0));
        Assert.assertFalse(ALLOW.contains(TIME_5));
        Assert.assertFalse(ALLOW.contains(TIME_5_59));
        Assert.assertTrue(ALLOW.contains(TIME_6));
        Assert.assertTrue(ALLOW.contains(TIME_9));
        Assert.assertTrue(ALLOW.contains(TIME_12));
        Assert.assertTrue(ALLOW.contains(TIME_15));
        Assert.assertTrue(ALLOW.contains(TIME_20_59));
        Assert.assertFalse(ALLOW.contains(TIME_21));
        Assert.assertFalse(ALLOW.contains(TIME_22));
    }
}
