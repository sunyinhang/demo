package com.haiercash.core.util;

import org.springframework.util.Assert;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by 许崇雷 on 2017-12-26.
 */
public final class AgeUtils {
    public static int getAge(Date birthday, Date now) {
        Assert.notNull(birthday, "birthday can not be null");
        Assert.notNull(now, "now can not be null");

        Calendar cBirthday = Calendar.getInstance();
        cBirthday.setTime(new Date(birthday.getYear(), birthday.getMonth(), birthday.getDate()));
        Calendar cNow = Calendar.getInstance();
        cNow.setTime(new Date(now.getYear(), now.getMonth(), now.getDate()));
        int age = cNow.get(Calendar.YEAR) - cBirthday.get(Calendar.YEAR);
        cBirthday.add(Calendar.YEAR, age);
        return cBirthday.compareTo(cNow) > 0 ? age - 1 : age;
    }
}
