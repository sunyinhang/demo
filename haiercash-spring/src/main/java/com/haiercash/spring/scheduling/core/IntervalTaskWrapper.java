package com.haiercash.spring.scheduling.core;

import org.springframework.scheduling.config.IntervalTask;

/**
 * Created by 许崇雷 on 2017-12-04.
 */
public final class IntervalTaskWrapper extends IntervalTask {
    public IntervalTaskWrapper(IntervalTask intervalTask) {
        super(new ContextRunnable(intervalTask.getRunnable()), intervalTask.getInterval(), intervalTask.getInitialDelay());
    }
}
