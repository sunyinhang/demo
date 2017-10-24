package com.haiercash.payplatform.aop;

import com.bestvike.lang.Convert;
import com.bestvike.lang.StringUtils;
import com.haiercash.payplatform.context.ThreadContext;
import com.haiercash.payplatform.controller.BaseController;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
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
    private static final String KEY_TOKEN = "token";
    private static final String KEY_CHANNEL = "channel";
    private static final String KEY_CHANNEL_NO = "channelNo";
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
        //获取实例
        Object target = joinPoint.getTarget();
        if (!(target instanceof BaseController))
            throw new RuntimeException(String.format("%s must extends %s", target.getClass(), BaseController.class));
        BaseController controller = (BaseController) target;
        //获取方法
        Signature signature = joinPoint.getSignature();
        if (!(signature instanceof MethodSignature))
            throw new RuntimeException("join point signature must be " + MethodSignature.class);
        MethodSignature methodSignature = (MethodSignature) signature;
        //获取参数
        Object[] args = joinPoint.getArgs();
        Class[] argTypes = methodSignature.getParameterTypes();
        for (int i = 0; i < argTypes.length; i++) {
            if (Map.class.isAssignableFrom(argTypes[i]))
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
        if (ThreadContext.exists()) {
            //Header
            String tokenHeader = ThreadContext.getToken();
            String channelHeader = ThreadContext.getChannel();
            String channelNoHeader = ThreadContext.getChannelNo();
            //Body
            String tokenBody = Convert.toString(mapArg.get(KEY_TOKEN));
            String channelBody = Convert.toString(mapArg.get(KEY_CHANNEL));
            String channelNoBody = Convert.toString(mapArg.get(KEY_CHANNEL_NO));
            //Merge
            String tokenMerge = StringUtils.defaultIfEmpty(tokenHeader, tokenBody);
            String channelMerge = StringUtils.defaultIfEmpty(channelHeader, channelBody);
            String channelNoMerge = StringUtils.defaultIfEmpty(channelNoHeader, channelNoBody);
            //修改
            ThreadContext.modify(tokenMerge, channelMerge, channelNoMerge);
            mapArg.put(KEY_TOKEN, tokenMerge);
            mapArg.put(KEY_CHANNEL, channelMerge);
            mapArg.put(KEY_CHANNEL_NO, channelNoMerge);
            //日志
            this.logger.info(String.format("常用参数(Header) token:%s channel:%s channelNo:%s", tokenHeader, channelHeader, channelNoHeader));
            this.logger.info(String.format("常用参数(  Body) token:%s channel:%s channelNo:%s", tokenBody, channelBody, channelNoBody));
            this.logger.info(String.format("常用参数( Merge) token:%s channel:%s channelNo:%s", tokenMerge, channelMerge, channelNoMerge));
        }
        return mapArg;
    }
}
