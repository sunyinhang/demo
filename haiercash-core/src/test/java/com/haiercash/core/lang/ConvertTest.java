package com.haiercash.core.lang;

import org.junit.Assert;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.Date;

/**
 * Created by 许崇雷 on 2017-11-30.
 */
public class ConvertTest {
    @Test
    public void toType() {
        String str = "2017-1-2 01:02:03";
        Date date = Convert.toType(str, Date.class);
        Assert.assertEquals(Date.class, date.getClass());
        Timestamp timestamp = Convert.toType(str, Timestamp.class);
        Assert.assertEquals(Timestamp.class, timestamp.getClass());
    }
}
