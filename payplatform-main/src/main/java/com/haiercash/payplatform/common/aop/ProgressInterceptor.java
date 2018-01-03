package com.haiercash.payplatform.common.aop;

import com.haiercash.core.collection.MapUtils;
import com.haiercash.payplatform.common.annotation.Progress;
import com.haiercash.payplatform.common.data.ProgressLog;
import com.haiercash.spring.context.ThreadContext;
import com.haiercash.spring.redis.RedisUtils;
import com.haiercash.spring.util.ConstUtil;
import com.haiercash.spring.util.RestUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;

import static com.haiercash.spring.util.RestUtil.fail;

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
    public Object doProgress(ProceedingJoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        boolean ifReturn = false;
        Progress progress = null;
        if (method == null) {
            ifReturn = true;
        } else {
            progress = method.getAnnotation(Progress.class);
            if (progress == null ) {
                ifReturn = true;
            }
        }

        StringBuffer argsBuffer = new StringBuffer();
        Object[] args = joinPoint.getArgs();
        for (int i = 0; i < args.length; i ++) {
            argsBuffer.append(", ").append(args[i]);
        }

        Object result = null;
        if (ifReturn) {
            try {
                result = joinPoint.proceed();
            } catch (Throwable throwable) {
                logger.error(throwable);
                result = fail("99999", "服务异常");
            }
        }

        ProgressLog progressLog = new ProgressLog();
        Map<String, Object> cacheMap = RedisUtils.getExpireMap(ThreadContext.getToken());
        if (MapUtils.isEmpty(cacheMap)) {
            logger.info("Jedis数据获取失败");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        progressLog.setName(String.valueOf(cacheMap.get("name")));
        progressLog.setIdCard(String.valueOf(cacheMap.get("idCard")));
        progressLog.setProgress(progress.progress());
        progressLog.setNode(progress.node());
        progressLog.setNextNode(progress.nextNode());
        // TODO 保存日志
        return result;
    }
}
