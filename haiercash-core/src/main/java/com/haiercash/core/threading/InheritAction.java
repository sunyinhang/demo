package com.haiercash.core.threading;

/**
 * Created by 许崇雷 on 2018-02-06.
 */
public abstract class InheritAction {
    private final ThreadLocalsHolder callerThreadLocalsHolder;

    public InheritAction() {
        this.callerThreadLocalsHolder = new ThreadLocalsHolder();
    }

    protected ThreadLocalsHolder backup() {
        ThreadLocalsHolder threadLocalsHolder = new ThreadLocalsHolder();
        this.callerThreadLocalsHolder.copyTo(threadLocalsHolder.getThread());
        return threadLocalsHolder;
    }

    protected void restore(ThreadLocalsHolder threadLocalsHolder) {
        threadLocalsHolder.restore();
    }
}
