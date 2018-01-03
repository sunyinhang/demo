package com.haiercash.payplatform.common.aop;

import com.haiercash.core.collection.MapUtils;
import com.haiercash.core.serialization.JsonSerializer;
import com.haiercash.payplatform.common.annotation.Progress;
import com.haiercash.payplatform.common.data.ProgressLog;
import com.haiercash.spring.context.ThreadContext;
import com.haiercash.spring.redis.RedisUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * 流程处理日志拦截器.
 * @author Qingxiang.Liu
 * @since v1.3.0
 */
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public final class ProgressInterceptor {
    private final Log logger = LogFactory.getLog(ProgressInterceptor.class);

    @Pointcut("execution(* com.haiercash..*.*(..)) && @annotation(com.haiercash.payplatform.common.annotation.Progress)")
    private void progressPointcut() {
    }

    @Around(value = "progressPointcut()")
    public Object doProgress(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed();

        Signature signature = joinPoint.getSignature();
        if (!(signature instanceof MethodSignature))
            return result;
        Method method = ((MethodSignature) signature).getMethod();
        Progress progress = method.getAnnotation(Progress.class);
        Map<String, Object> cacheMap = RedisUtils.getExpireMap(ThreadContext.getToken());
        if (MapUtils.isEmpty(cacheMap) || cacheMap.get("idCard") == null)
            return result;
        ProgressLog progressLog = new ProgressLog();
        progressLog.setName(String.valueOf(cacheMap.get("name")));
        progressLog.setIdCard(String.valueOf(cacheMap.get("idCard")));
        progressLog.setProgress(progress.progress());
        progressLog.setNode(progress.node());
        progressLog.setNextNode(progress.nextNode());
        logger.info("流程日志:" + JsonSerializer.serialize(progressLog));
        // TODO 保存日志
        return result;
    }
}
