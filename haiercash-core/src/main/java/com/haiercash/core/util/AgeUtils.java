package com.haiercash.core.util;

import com.haiercash.core.lang.DateUtils;
import org.springframework.util.Assert;

import java.util.Calendar;
import java.util.Date;

/**
 * 年龄工具
 * Created by 许崇雷 on 2017-12-26.
 */
public final class AgeUtils {
    /**
     * 获取指定生日的人在指定日期的年龄(周岁)
     *
     * @param birthday 生日
     * @param now      指定日期
     * @return 周岁年龄(从 0 开始)
     */
    public static int getAge(Date birthday, Date now) {
        Assert.notNull(birthday, "birthday can not be null");
        Assert.notNull(now, "now can not be null");

        Calendar cBirthday = DateUtils.toCalendar(birthday);
        DateUtils.truncateToDate(cBirthday);
        Calendar cNow = DateUtils.toCalendar(now);
        DateUtils.truncateToDate(cNow);
        int age = cNow.get(Calendar.YEAR) - cBirthday.get(Calendar.YEAR);
        cBirthday.add(Calendar.YEAR, age);
        return cBirthday.compareTo(cNow) > 0 ? age - 1 : age;
    }
}
