package com.haiercash.payplatform.aop;

import com.bestvike.lang.StringUtils;
import com.haiercash.payplatform.context.ThreadContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * 队列消费拦截器
 * Created by 许崇雷 on 2017-10-19.
 */
@Aspect
@Component
public class RabbitInterceptor {
    private final Log logger = LogFactory.getLog(RabbitInterceptor.class);

    @Pointcut("execution(* com.haiercash..*.*(..)) && @annotation(org.springframework.amqp.rabbit.annotation.RabbitHandler)")
    private void doRabbitPointcut() {
    }

    @Around(value = "doRabbitPointcut()")
    public Object doRabbit(ProceedingJoinPoint joinPoint) throws Throwable {
        //打印日志
        String action = joinPoint.getSignature().toLongString();
        Object[] args = joinPoint.getArgs();
        this.logger.info("==>Rabbit:" + action + "@" + StringUtils.join(args, "; "));

        //进入 RabbitHandler
        ThreadContext.reset();
        try {
            return joinPoint.proceed();
        } finally {
            ThreadContext.reset();
        }
    }
}
