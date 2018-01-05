package com.haiercash.payplatform.common.aop;

import com.haiercash.payplatform.common.annotation.FlowNode;
import com.haiercash.payplatform.utils.FlowNodeLogger;
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

/**
 * 流程处理日志拦截器.
 * @author Qingxiang.Liu
 * @since v1.3.0
 */
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public final class FlowNodeInterceptor {
    @Pointcut("execution(* com.haiercash..*.*(..)) && @annotation(com.haiercash.payplatform.common.annotation.FlowNode)")
    private void doFlowNodePointcut() {
    }

    @Around(value = "doFlowNodePointcut()")
    public Object doFlowNode(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed();

        Signature signature = joinPoint.getSignature();
        if (!(signature instanceof MethodSignature))
            return result;
        Method method = ((MethodSignature) signature).getMethod();
        FlowNode flowNode = method.getAnnotation(FlowNode.class);
        FlowNodeLogger.info(flowNode, String.valueOf(result));
        return result;
    }
}
