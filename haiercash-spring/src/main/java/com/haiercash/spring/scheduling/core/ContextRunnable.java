package com.haiercash.spring.scheduling.core;

import com.haiercash.core.lang.ThrowableUtils;
import com.haiercash.spring.context.ThreadContext;
import com.haiercash.spring.mail.bugreport.BugReportLevel;
import com.haiercash.spring.mail.bugreport.BugReportUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;

/**
 * Created by 许崇雷 on 2017-12-04.
 */
public final class ContextRunnable implements Runnable {
    private final Runnable runnable;
    private final Log logger = LogFactory.getLog(ContextRunnable.class);

    public ContextRunnable(Runnable runnable) {
        Assert.notNull(runnable, "runnable can not be null");
        this.runnable = runnable;
    }

    @Override
    public void run() {
        ThreadContext.init(null, null, null);
        try {
            runnable.run();
        } catch (Exception e) {
            this.handleError(e);
        } finally {
            ThreadContext.reset();
        }
    }

    private void handleError(Exception e) {
        String msg = ThrowableUtils.getString(e);
        this.logger.error(msg);
        //错误报告
        BugReportUtils.sendAsync(BugReportLevel.ERROR, msg);
    }
}
