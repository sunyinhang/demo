package com.haiercash.appserver.config;

import com.haiercash.appserver.service.AppOrderService;
import com.haiercash.common.util.ThreadLocalFactory;
import com.haiercash.appserver.util.annotation.RequestCheck;
import com.haiercash.common.data.AppOrder;
import com.haiercash.commons.util.RedisUtil;
import com.sun.star.uno.RuntimeException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * request interceptor.
 * <p>
 *     拦截所有带有@RequestCheck注解的方法，获取请求参数中的userId，
 *     根据userId获取用户token，与请求中的token进行比较，如果不相同，
 *     则认定为非法请求，返回错误。
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

    @Autowired
    private AppOrderService appOrderService;

    private static final byte[] userStore = RedisUtil.serialize("__token_app_userStore");

    @Pointcut("execution(* com.haiercash..*.*(..)) && @annotation(com.haiercash.appserver.util.annotation.RequestCheck)")
    private void requestNeedCheck() {

    }

    @Pointcut("execution(* com.haiercash..*.*(..)) && @annotation(org.springframework.web.bind.annotation.RequestMapping)")
    private void sysFlagAndChannelNo() {

    }

    @Before(value = "sysFlagAndChannelNo()")
    public void beforeRequest(JoinPoint joinPoint) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest();
        String sysFlag = request.getHeader("channel");
        String channelNo = request.getHeader("channel_no");
        ThreadLocal threadLocal = ThreadLocalFactory.getThreadLocal();
        threadLocal.remove();
        Map<String, Object> threadMap = new HashMap<>();
        threadMap.put("channel", sysFlag);
        threadMap.put("channelNo", channelNo);
        String custNo = request.getParameter("custNo");
        if (StringUtils.isEmpty(channelNo) && !StringUtils.isEmpty(custNo)) {
            AppOrder appOrder = new AppOrder();
            appOrderService.getChannelNoAndWhiteType(custNo, appOrder);
            channelNo = appOrder.getChannelNo();
            threadMap.put("channelNo", channelNo);
        }

        // 如果校验channelNo为空，则设置为默认值05-app门店.
        if (StringUtils.isEmpty(sysFlag)) {
            threadMap.put("channel", "04");
        }

        if (StringUtils.isEmpty(channelNo)) {
            threadMap.put("channelNo", "05");
        }
        threadLocal.set(threadMap);
    }

    @Before(value = "requestNeedCheck()")
    public void before(JoinPoint joinPoint) throws Throwable {
        if (!checkAuth) {
            return;
        }

        // 校验是否验证token。
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        if (method != null) {
            RequestCheck annotation = methodSignature.getMethod().getAnnotation(RequestCheck.class);
            if (!annotation.value()) {
                return;
            }
        }

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest();
        String userId = request.getParameter("userId");
        String token = request.getHeader("access_token");
        Object[] args = joinPoint.getArgs();
        logger.info("拦截器userId:" + userId + ", token:" + token);
        if (!StringUtils.isEmpty(token)) {
            byte[] key = RedisUtil.serialize(token);
            String redisUserId = (String) RedisUtil.unserialize(RedisUtil.hget(this.userStore, key));
            logger.info("拦截器redisUserId:" + redisUserId);
            if (!StringUtils.isEmpty(userId)) {
                if (!userId.equals(redisUserId)) {
                    throw new RuntimeException("非法请求:用户token与用户id校验失败");
                }
            } else {
                for (Object arg : args) {
                    if (arg instanceof Map) {
                        Map<String, Object> params = (Map<String, Object>) arg;
                        String var1 = String.valueOf(params.get("userId"));
                        String var2 = String.valueOf(params.get("user_id"));
                        logger.info("拦截器var1:" + var1 + ", var2:" + var2);
                        if (!"null".equals(var1.toLowerCase()) && !StringUtils.isEmpty(var1)) {
                            if (!var1.equals(redisUserId)) {
                                throw new RuntimeException("非法请求:用户token与用户id校验失败");
                            }
                        }
                        if (!"null".equals(var2.toLowerCase()) && !StringUtils.isEmpty(var1)) {
                            if (!var2.equals(redisUserId)) {
                                throw new RuntimeException("非法请求:用户token与用户id校验失败");
                            }
                        }

                    }
                }
            }

        }
    }
}
