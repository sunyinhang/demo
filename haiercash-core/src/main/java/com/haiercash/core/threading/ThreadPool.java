package com.haiercash.core.threading;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * Created by 许崇雷 on 2018-01-20.
 */
public final class ThreadPool {
    private static final ThreadPoolTaskExecutor EXECUTOR;
    private static final ThreadPoolTaskScheduler SCHEDULER;

    static {
        EXECUTOR = new ThreadPoolTaskExecutor();
        EXECUTOR.setCorePoolSize(8);
        EXECUTOR.setMaxPoolSize(64);
        EXECUTOR.setQueueCapacity(1024);
        EXECUTOR.setThreadNamePrefix("executor-");
        EXECUTOR.setWaitForTasksToCompleteOnShutdown(true);
        EXECUTOR.setAwaitTerminationSeconds(60);
        EXECUTOR.initialize();
    }

    static {
        SCHEDULER = new ThreadPoolTaskScheduler();
        SCHEDULER.setPoolSize(64);
        SCHEDULER.setThreadNamePrefix("scheduler-");
        SCHEDULER.setWaitForTasksToCompleteOnShutdown(true);
        SCHEDULER.setAwaitTerminationSeconds(60);
        SCHEDULER.initialize();
    }

    public static ThreadPoolTaskExecutor getExecutor() {
        return EXECUTOR;
    }

    public static ThreadPoolTaskScheduler getScheduler() {
        return SCHEDULER;
    }
}
