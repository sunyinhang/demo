package com.haiercash.core.threading;

import com.haiercash.core.reflect.ReflectionUtils;

/**
 * Created by 许崇雷 on 2018-02-06.
 */
public final class ThreadLocalsHolder {
    private static final String THREAD_LOCALS = "threadLocals";
    private static final String INHERITABLE_THREAD_LOCALS = "inheritableThreadLocals";
    private final Thread thread;
    private final Object threadLocals;
    private final Object inheritableThreadLocals;

    public ThreadLocalsHolder() {
        this(Thread.currentThread());
    }

    public ThreadLocalsHolder(Thread thread) {
        this.thread = thread;
        this.threadLocals = ReflectionUtils.getField(this.thread, THREAD_LOCALS);
        this.inheritableThreadLocals = ReflectionUtils.getField(this.thread, INHERITABLE_THREAD_LOCALS);
    }

    public Thread getThread() {
        return thread;
    }

    public Object getThreadLocals() {
        return threadLocals;
    }

    public Object getInheritableThreadLocals() {
        return inheritableThreadLocals;
    }

    public void copyTo(Thread thread) {
        ReflectionUtils.setField(thread, THREAD_LOCALS, this.threadLocals);
        ReflectionUtils.setField(thread, INHERITABLE_THREAD_LOCALS, this.inheritableThreadLocals);
    }

    public void restore() {
        this.copyTo(this.thread);
    }
}
