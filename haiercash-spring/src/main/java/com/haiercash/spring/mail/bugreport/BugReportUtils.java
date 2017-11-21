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

    public static void sendAsync(String content) {
        getBugReportThread().sendAsync(getBugReportMailFactory().createMail(content));
    }
}
