package com.haiercash.spring.mail.bugreport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created by 许崇雷 on 2017-11-21.
 */
@Component
public final class BugReportProvider {
    private static BugReportMailFactory bugReportMailFactory;
    private static BugReportThread bugReportThread;
    @Autowired
    private BugReportMailFactory bugReportMailFactoryInstance;
    @Autowired
    private BugReportThread bugReportThreadInstance;

    public static BugReportMailFactory getBugReportMailFactory() {
        return bugReportMailFactory;
    }

    public static BugReportThread getBugReportThread() {
        return bugReportThread;
    }

    @PostConstruct
    private void init() {
        bugReportMailFactory = this.bugReportMailFactoryInstance;
        bugReportThread = this.bugReportThreadInstance;
    }
}
