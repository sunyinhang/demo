package com.haiercash.spring.scheduling.core;

import com.haiercash.spring.context.ThreadContext;
import com.haiercash.spring.trace.scheduling.ErrorHandler;
import org.springframework.util.Assert;

/**
 * Created by 许崇雷 on 2017-12-04.
 */
public final class ContextRunnable implements Runnable {
    private final Runnable runnable;

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
            ErrorHandler.handleException(e);
        } finally {
            ThreadContext.reset();
        }
    }
}
