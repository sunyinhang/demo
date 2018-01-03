package com.haiercash.spring.scheduling.core;

import com.haiercash.spring.scheduling.util.RunnableUtils;
import org.springframework.scheduling.config.CronTask;
import org.springframework.scheduling.support.CronTrigger;

/**
 * Created by 许崇雷 on 2017-12-04.
 */
public final class CronTaskWrapper extends CronTask {
    public CronTaskWrapper(CronTask cronTask) {
        super(RunnableUtils.getRunnable(cronTask.getRunnable()), (CronTrigger) cronTask.getTrigger());
    }
}
