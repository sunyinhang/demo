package com.haiercash.spring.aop;

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
 * 队列消费拦截器
 * Created by 许崇雷 on 2017-10-19.
 */
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public final class RabbitInterceptor {
    private final Log logger = LogFactory.getLog(RabbitInterceptor.class);

    @Pointcut("execution(* com.haiercash..*.*(..)) && @annotation(org.springframework.amqp.rabbit.annotation.RabbitListener)" +
            " || @annotation(org.springframework.amqp.rabbit.annotation.RabbitHandler))")
    private void doRabbitPointcut() {
    }

    @Around(value = "doRabbitPointcut()")
    public Object doRabbit(ProceedingJoinPoint joinPoint) throws Throwable {
        String action = joinPoint.getSignature().toLongString();
        this.logger.info("==>@RabbitHandler Begin: " + action);
        long begin = System.currentTimeMillis();
        try {
            return joinPoint.proceed();
        } finally {
            this.logger.info(String.format("==>@RabbitHandler End: %s Took: %d", action, System.currentTimeMillis() - begin));
        }
    }
}
