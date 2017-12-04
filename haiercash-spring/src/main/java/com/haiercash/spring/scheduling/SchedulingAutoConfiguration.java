package com.haiercash.spring.scheduling;

import com.bestvike.linq.Linq;
import com.haiercash.spring.scheduling.core.CronTaskWrapper;
import com.haiercash.spring.scheduling.core.IntervalTaskWrapper;
import com.haiercash.spring.scheduling.core.TriggerTaskWrapper;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.CronTask;
import org.springframework.scheduling.config.IntervalTask;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.config.TriggerTask;

/**
 * Created by 许崇雷 on 2017-12-04.
 */
@Configuration
public class SchedulingAutoConfiguration implements SchedulingConfigurer {
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        this.setTaskScheduler(taskRegistrar);
        this.wrapTasks(taskRegistrar);
    }

    private void setTaskScheduler(ScheduledTaskRegistrar taskRegistrar) {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(64);
        taskScheduler.setThreadNamePrefix("task-");
        taskScheduler.setWaitForTasksToCompleteOnShutdown(true);
        taskScheduler.setAwaitTerminationSeconds(60);
        taskScheduler.initialize();
        taskRegistrar.setTaskScheduler(taskScheduler);
    }

    private void wrapTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setTriggerTasksList(Linq.asEnumerable(taskRegistrar.getTriggerTaskList()).select(task -> (TriggerTask) new TriggerTaskWrapper(task)).toList());
        taskRegistrar.setCronTasksList(Linq.asEnumerable(taskRegistrar.getCronTaskList()).select(task -> (CronTask) new CronTaskWrapper(task)).toList());
        taskRegistrar.setFixedRateTasksList(Linq.asEnumerable(taskRegistrar.getFixedRateTaskList()).select(task -> (IntervalTask) new IntervalTaskWrapper(task)).toList());
        taskRegistrar.setFixedDelayTasksList(Linq.asEnumerable(taskRegistrar.getFixedDelayTaskList()).select(task -> (IntervalTask) new IntervalTaskWrapper(task)).toList());
    }
}
