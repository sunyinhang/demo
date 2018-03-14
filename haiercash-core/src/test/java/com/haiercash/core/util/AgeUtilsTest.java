package com.haiercash.core.util;

import com.haiercash.core.lang.DateUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

/**
 * Created by 许崇雷 on 2017-12-26.
 */
public class AgeUtilsTest {
    @Test
    public void getAge() {
        Date birthday = DateUtils.fromDateString("2000-01-29");
        int age = AgeUtils.getAge(birthday, DateUtils.fromDateString("2001-01-27"));
        Assert.assertEquals(0, age);

        age = AgeUtils.getAge(birthday, DateUtils.fromDateString("2001-01-28"));
        Assert.assertEquals(1, age);

        age = AgeUtils.getAge(birthday, DateUtils.fromDateString("2004-01-28"));
        Assert.assertEquals(3, age);

        age = AgeUtils.getAge(birthday, DateUtils.fromDateString("2004-01-29"));
        Assert.assertEquals(4, age);
    }
}
