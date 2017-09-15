package com.haiercash.payplatform.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.Ordered;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executor;

/**
 * Enable Schedule async excute;
 * @author zhou wushuang
 * @since v1.0.1
 */
@Component
@EnableScheduling
@EnableAsync(
        mode = AdviceMode.PROXY,
        proxyTargetClass = true,
        order = Ordered.HIGHEST_PRECEDENCE
)
@ComponentScan(
        basePackages = {"com.haiercash.payplatform"}
)
public class ScheduleAsyncConfig implements AsyncConfigurer, SchedulingConfigurer{

    Log logger = LogFactory.getLog(ScheduleAsyncConfig.class);

    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(64);
        scheduler.setThreadNamePrefix("task-");
        scheduler.setAwaitTerminationSeconds(60);
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.initialize();
        return scheduler;
    }

    @Override
    public Executor getAsyncExecutor() {
        Executor executor = this.taskScheduler();
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        AsyncUncaughtExceptionHandler exceptionHandler = (ex, method, params) -> {
            logger.error("定时任务捕获到异常: " + ex.getMessage());
            logger.error("定时任务异常方法：" + method.getName() + ", 参数:" + params);
        };
        return exceptionHandler;
    }


    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        TaskScheduler scheduler = this.taskScheduler();
        taskRegistrar.setTaskScheduler(scheduler);
    }
}
