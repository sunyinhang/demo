package com.haiercash.spring.scheduling.core;

import com.haiercash.spring.context.ThreadContext;
import com.haiercash.spring.context.TraceContext;
import com.haiercash.spring.trace.scheduling.IncomingLog;
import org.springframework.scheduling.support.ScheduledMethodRunnable;
import org.springframework.util.Assert;

/**
 * Created by 许崇雷 on 2017-12-04.
 */
public final class ContextRunnable implements Runnable {
    private final ScheduledMethodRunnable runnable;

    public ContextRunnable(ScheduledMethodRunnable runnable) {
        Assert.notNull(runnable, "runnable can not be null");
        this.runnable = runnable;
    }

    @Override
    public void run() {
        ThreadContext.init(null, null, null);
        TraceContext.init();
        String action = this.runnable.getMethod().toGenericString();
        IncomingLog.writeBeginLog(action);
        long begin = System.currentTimeMillis();
        try {
            this.runnable.run();
            IncomingLog.writeEndLog(action, System.currentTimeMillis() - begin);
        } catch (Exception e) {
            IncomingLog.writeErrorLog(action, e, System.currentTimeMillis() - begin);
        } finally {
            TraceContext.reset();
            ThreadContext.reset();
        }
    }
}
