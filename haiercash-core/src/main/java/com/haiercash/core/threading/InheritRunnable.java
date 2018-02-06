package com.haiercash.core.threading;

import org.springframework.util.Assert;

/**
 * Created by 许崇雷 on 2018-02-01.
 */
public final class InheritRunnable extends InheritAction implements Runnable {
    private final Runnable runnable;

    public InheritRunnable(Runnable runnable) {
        Assert.notNull(runnable, "runnable can not be null");
        this.runnable = runnable;
    }

    @Override
    public void run() {
        final ThreadLocalsHolder threadLocalsHolder = this.backup();
        try {
            this.runnable.run();
        } finally {
            this.restore(threadLocalsHolder);
        }
    }
}
