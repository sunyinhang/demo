package com.haiercash.spring.mail.bugreport;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by 许崇雷 on 2017-11-23.
 */
public class BugReportLevelTest {
    @Test
    public void testBugReportLevel() {
        Assert.assertEquals("INFO", BugReportLevel.INFO.toString());
        Assert.assertEquals("INFO", BugReportLevel.INFO.name());
        Assert.assertEquals(0, BugReportLevel.INFO.ordinal());
        Assert.assertEquals(1, BugReportLevel.WARN.ordinal());
        Assert.assertEquals(2, BugReportLevel.ERROR.ordinal());
        Assert.assertEquals("消息", BugReportLevel.INFO.getDescription());
    }
}
