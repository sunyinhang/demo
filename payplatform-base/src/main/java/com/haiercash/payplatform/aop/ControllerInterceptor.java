package com.haiercash.payplatform.aop;

import com.haiercash.payplatform.context.ThreadContext;
import com.haiercash.payplatform.controller.BaseController;
import com.haiercash.payplatform.utils.RestUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * 控制器拦截器
 * Created by 许崇雷 on 2017-10-19.
 */
@Aspect
@Component
public class ControllerInterceptor {
    private final Log logger = LogFactory.getLog(ControllerInterceptor.class);

    @Pointcut("execution(* com.haiercash..*.*(..)) && (@annotation(org.springframework.web.bind.annotation.RequestMapping)" +
            " || @annotation(org.springframework.web.bind.annotation.GetMapping)" +
            " || @annotation(org.springframework.web.bind.annotation.DeleteMapping)" +
            " || @annotation(org.springframework.web.bind.annotation.PostMapping)" +
            " || @annotation(org.springframework.web.bind.annotation.PutMapping))")
    private void doControllerPointcut() {
    }

    @Around(value = "doControllerPointcut()")
    public Object doController(ProceedingJoinPoint joinPoint) throws Throwable {
        //获取目标 Controller
        Object target = joinPoint.getTarget();
        if (!(target instanceof BaseController)) {
            String msg = String.format("%s not extends %s", target.getClass(), BaseController.class);
            this.logger.error(msg);
            return RestUtil.fail("P9999", msg);
        }
        //进入 Controller
        BaseController controller = (BaseController) target;
        ThreadContext.enterController(controller);//进入
        try {
            return joinPoint.proceed();
        } finally {
            ThreadContext.exitController();//退出
        }
    }
}
