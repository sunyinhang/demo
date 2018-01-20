package com.haiercash.spring.scheduling.util;

import com.haiercash.spring.scheduling.core.ContextRunnable;
import org.springframework.scheduling.support.ScheduledMethodRunnable;

import java.lang.reflect.Method;

/**
 * Created by 许崇雷 on 2018-01-03.
 */
public final class RunnableUtils {
    private static final String EXCLUDE_PACKAGE = "org.springframework.";

    public static Runnable getRunnable(Runnable runnable) {
        if (runnable instanceof ScheduledMethodRunnable) {
            Method method = ((ScheduledMethodRunnable) runnable).getMethod();
            String packageName = method.getDeclaringClass().getPackage().getName();
            return packageName.startsWith(EXCLUDE_PACKAGE) ? runnable : new ContextRunnable((ScheduledMethodRunnable) runnable);
        }
        return runnable;
    }
}
