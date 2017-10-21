package com.haiercash.payplatform.aop;

import com.haiercash.payplatform.context.ThreadContext;
import com.haiercash.payplatform.controller.BaseController;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 控制器拦截器
 * Created by 许崇雷 on 2017-10-19.
 */
@Aspect
@Component
public final class ControllerInterceptor {
    @Pointcut("execution(* com.haiercash..*.*(..)) && (@annotation(org.springframework.web.bind.annotation.RequestMapping)" +
            " || @annotation(org.springframework.web.bind.annotation.GetMapping)" +
            " || @annotation(org.springframework.web.bind.annotation.DeleteMapping)" +
            " || @annotation(org.springframework.web.bind.annotation.PostMapping)" +
            " || @annotation(org.springframework.web.bind.annotation.PutMapping))")
    private void doControllerPointcut() {
    }

    @Around(value = "doControllerPointcut()")
    public Object doController(ProceedingJoinPoint joinPoint) throws Throwable {
        //获取实例
        Object target = joinPoint.getTarget();
        if (!(target instanceof BaseController))
            throw new RuntimeException(String.format("%s must extends %s", target.getClass(), BaseController.class));
        BaseController controller = (BaseController) target;
        //获取方法
        if (!(joinPoint.getSignature() instanceof MethodSignature))
            throw new RuntimeException("join point signature must be " + MethodSignature.class);
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        //获取参数
        Object[] args = joinPoint.getArgs();
        Class[] types = methodSignature.getParameterTypes();
        for (int i = 0; i < types.length; i++) {
            if (Map.class.isAssignableFrom(types[i]))
                args[i] = this.putThreadVars((Map) args[i]);
        }
        //执行
        ThreadContext.enterController(controller);//进入
        try {
            return joinPoint.proceed(args);
        } finally {
            ThreadContext.exitController();//退出
        }
    }

    private Map putThreadVars(Map mapArg) {
        if (mapArg == null)
            mapArg = new HashMap<String, Object>();
        mapArg.put("token", ThreadContext.getToken());
        mapArg.put("channel", ThreadContext.getChannel());
        mapArg.put("channelNo", ThreadContext.getChannelNo());
        return mapArg;
    }
}
