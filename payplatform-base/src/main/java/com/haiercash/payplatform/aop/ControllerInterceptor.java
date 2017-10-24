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
    private static final String NAME_TOKEN = "token";
    private static final String NAME_CHANNEL = "channel";
    private static final String NAME_CHANNEL_NO = "channelNo";
    private final Log logger = LogFactory.getLog(ControllerInterceptor.class);

    private void updateArgs(Class[] paramTypes, String[] paramNames, Object[] args) {
        if (!ThreadContext.exists())
            throw new RuntimeException("must init thread context first");
        //合并参数
        String token = ThreadContext.getToken();
        String channel = ThreadContext.getChannel();
        String channelNo = ThreadContext.getChannelNo();
        if (StringUtils.isEmpty(token) || StringUtils.isEmpty(channel) || StringUtils.isEmpty(channelNo)) {
            for (int i = 0; i < paramTypes.length; i++) {
                Class paramType = paramTypes[i];
                if (!Map.class.isAssignableFrom(paramType))
                    continue;
                Map map = (Map) args[i];
                if (map == null) {
                    map = new HashMap<String, Object>();
                    args[i] = map;
                    continue;
                }
                if (StringUtils.isEmpty(token))
                    token = Convert.toString(map.get(NAME_TOKEN));
                if (StringUtils.isEmpty(channel))
                    channel = Convert.toString(map.get(NAME_CHANNEL));
                if (StringUtils.isEmpty(channelNo))
                    channelNo = Convert.toString(map.get(NAME_CHANNEL_NO));
            }
        }
        //更新参数
        ThreadContext.modify(token, channel, channelNo);
        for (int i = 0; i < paramTypes.length; i++) {
            Class paramType = paramTypes[i];
            String paramName = paramNames[i];
            switch (paramName) {
                case NAME_TOKEN:
                    if (CharSequence.class.isAssignableFrom(paramType))
                        args[i] = token;
                    break;
                case NAME_CHANNEL:
                    if (CharSequence.class.isAssignableFrom(paramType))
                        args[i] = channel;
                    break;
                case NAME_CHANNEL_NO:
                    if (CharSequence.class.isAssignableFrom(paramType))
                        args[i] = channelNo;
                    break;
                default:
                    if (Map.class.isAssignableFrom(paramType)) {
                        Map map = (Map) args[i];
                        map.put(NAME_TOKEN, token);
                        map.put(NAME_CHANNEL, channel);
                        map.put(NAME_CHANNEL_NO, channelNo);
                    }
                    break;
            }
        }
        //打印日志
        this.logger.info(String.format("常用参数 token:%s channel:%s channelNo:%s", token, channel, channelNo));
    }

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
        //更新参数
        Class[] paramTypes = methodSignature.getParameterTypes();
        String[] paramNames = methodSignature.getParameterNames();
        Object[] args = joinPoint.getArgs();
        this.updateArgs(paramTypes, paramNames, args);
        //执行
        ThreadContext.enterController(controller);//进入
        try {
            return joinPoint.proceed(args);
        } finally {
            ThreadContext.exitController();//退出
        }
    }
}
