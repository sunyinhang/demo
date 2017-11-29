package com.haiercash.spring.mail.bugreport;

/**
 * Created by 许崇雷 on 2017-11-21.
 */
public final class BugReportUtils {
    private static BugReportMailFactory getBugReportMailFactory() {
        return BugReportProvider.getBugReportMailFactory();
    }

    private static BugReportThread getBugReportThread() {
        return BugReportProvider.getBugReportThread();
    }

    public static void sendAsync(BugReportLevel level, String content) {
        if (level == null)
            level = BugReportLevel.DEFAULT_LEVEL;
        getBugReportThread().sendAsync(level, getBugReportMailFactory().createMail(level, content));
    }
}
