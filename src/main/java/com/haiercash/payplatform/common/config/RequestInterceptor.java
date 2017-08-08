package com.haiercash.payplatform.common.config;

import com.haiercash.payplatform.common.controller.BaseController;
import com.haiercash.payplatform.common.filter.RequestContext;
import com.haiercash.payplatform.common.filter.RequestContextData;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * request interceptor.
 * <p>
 * 拦截所有带有@RequestCheck注解的方法，获取请求参数中的userId，
 * 根据userId获取用户token，与请求中的token进行比较，如果不相同，
 * 则认定为非法请求，返回错误。
 * </p>
 *
 * @author liu qingxiang
 * @since v1.5.6
 */
@Aspect
@Component
public class RequestInterceptor {
    public Log logger = LogFactory.getLog(this.getClass());

    @Value("${common.app.checkAuth}")
    private Boolean checkAuth;


    @Pointcut("execution(* com.haiercash..*.*(..)) && @annotation(org.springframework.web.bind.annotation.RequestMapping)")
    private void doRequestPointcut() {
    }

    @Around(value = "doRequestPointcut()")
    public Object doRequest(ProceedingJoinPoint joinPoint) throws Throwable {
        //获取上下文数据
        RequestContext rc = RequestContext.get();
        RequestContextData rcData = rc.getData();
        //获取目标 controller
        Object target = joinPoint.getTarget();
        if (!(target instanceof BaseController)) {
            logger.error(String.format("%s not extends %s", target.getClass(), BaseController.class));
            return null;
        }
        BaseController controller = (BaseController) target;
        //初始化数据,如果扩展 rcData 数据,在此添加
        if (!rcData.isInited()) {
            HttpServletRequest request = rc.getRequest();
            this.initChannel(rc);
            rcData.completeInit();//完成初始化
        }
        //进入 controller
        rcData.enterController(controller);//进入
        try {
            return joinPoint.proceed();
        } finally {
            rcData.exitController();//退出
        }
    }

    //初始化渠道
    private void initChannel(RequestContext rc) {
        HttpServletRequest request = rc.getRequest();
        String channel = request.getHeader("channel");
        String channelNo = request.getHeader("channel_no");
        if (StringUtils.isEmpty(channel))
            channel = "04";
        if (StringUtils.isEmpty(channelNo))
            channelNo = "05";
        rc.getData().initChannel(channel, channelNo);
    }

}
