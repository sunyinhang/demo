package com.haiercash.spring.scheduling.core;

import com.haiercash.spring.scheduling.util.RunnableUtils;
import org.springframework.scheduling.config.TriggerTask;

/**
 * Created by 许崇雷 on 2017-12-04.
 */
public final class TriggerTaskWrapper extends TriggerTask {
    public TriggerTaskWrapper(TriggerTask triggerTask) {
        super(RunnableUtils.getRunnable(triggerTask.getRunnable()), triggerTask.getTrigger());
    }
}
