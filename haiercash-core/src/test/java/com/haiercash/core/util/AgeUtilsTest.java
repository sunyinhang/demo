package com.haiercash.core.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

/**
 * Created by 许崇雷 on 2017-12-26.
 */
public class AgeUtilsTest {
    @Test
    public void getAge() {
        Date birthday = new Date(2000 - 1900, 1, 29);
        int age = AgeUtils.getAge(birthday, new Date(2001 - 1900, 1, 27));
        Assert.assertEquals(0, age);

        age = AgeUtils.getAge(birthday, new Date(2001 - 1900, 1, 28));
        Assert.assertEquals(1, age);

        age = AgeUtils.getAge(birthday, new Date(2004 - 1900, 1, 28));
        Assert.assertEquals(3, age);

        age = AgeUtils.getAge(birthday, new Date(2004 - 1900, 1, 29));
        Assert.assertEquals(4, age);
    }
}