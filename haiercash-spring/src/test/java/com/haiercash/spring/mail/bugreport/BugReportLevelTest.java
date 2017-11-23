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
        Assert.assertEquals("INFO", BugReportLevel.INFO.getName());
        Assert.assertEquals("消息", BugReportLevel.INFO.getDescription());
    }
}
