package com.haiercash.spring.aop;

import com.haiercash.spring.context.ThreadContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 定时任务拦截器
 * Created by 许崇雷 on 2017-10-19.
 */
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public final class ScheduledInterceptor {
    private final Log logger = LogFactory.getLog(RabbitInterceptor.class);

    @Pointcut("execution(* com.haiercash..*.*(..)) && @annotation(org.springframework.scheduling.annotation.Scheduled)")
    private void doScheduledPointcut() {
    }

    @Around(value = "doScheduledPointcut()")
    public Object doScheduled(ProceedingJoinPoint joinPoint) throws Throwable {
        //打印日志
        String action = joinPoint.getSignature().toLongString();
        this.logger.info("==>Scheduled:" + action);

        //进入 RabbitHandler
        ThreadContext.reset();
        try {
            return joinPoint.proceed();
        } finally {
            ThreadContext.reset();
        }
    }
}
