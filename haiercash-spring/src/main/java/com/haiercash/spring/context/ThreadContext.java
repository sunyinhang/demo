package com.haiercash.spring.context;

import com.haiercash.core.lang.StringUtils;
import com.haiercash.spring.controller.BaseController;
import com.haiercash.spring.trace.TraceId;
import org.slf4j.MDC;
import org.springframework.util.Assert;

import java.util.Stack;

/**
 * 线程上下文
 * Created by 许崇雷 on 2017-10-19.
 */
public final class ThreadContext {
    //线程本地存储
    private static final ThreadLocal<ThreadContextData> contexts = ThreadLocal.withInitial(ThreadContextData::new);

    //region property

    public static boolean exists() {
        return contexts.get().exists;
    }

    public static String getTraceId() {
        return contexts.get().traceId;
    }

    public static String getToken() {
        return contexts.get().token;
    }

    public static String getChannel() {
        return contexts.get().channel;
    }

    public static String getChannelNo() {
        return contexts.get().channelNo;
    }

    public static BaseController getEntryController() {
        Stack<BaseController> controllerStack = contexts.get().controllerStack;
        return controllerStack.empty() ? null : controllerStack.firstElement();
    }

    public static BaseController getExecutingController() {
        Stack<BaseController> controllerStack = contexts.get().controllerStack;
        return controllerStack.empty() ? null : controllerStack.lastElement();
    }

    public static String getEntryModuleNo() {
        BaseController controller = getEntryController();
        return controller == null ? StringUtils.EMPTY : controller.getModuleNo();
    }

    public static String getExecutingModuleNo() {
        BaseController controller = getExecutingController();
        return controller == null ? StringUtils.EMPTY : controller.getModuleNo();
    }

    //endregion

    public static void init(String token, String channel, String channelNo) {
        //可在此处添加验证
        String traceId = TraceId.generate();
        MDC.put(TraceId.NAME, traceId);
        if (RequestContext.exists())
            RequestContext.getResponse().setHeader(TraceId.NAME_HEADER, traceId);
        ThreadContextData data = contexts.get();
        data.exists = true;
        data.traceId = traceId;
        data.token = token == null ? StringUtils.EMPTY : token;
        data.channel = channel == null ? StringUtils.EMPTY : channel;
        data.channelNo = channelNo == null ? StringUtils.EMPTY : channelNo;
        data.controllerStack.clear();
    }

    public static void modify(String token, String channel, String channelNo) {
        ThreadContextData data = contexts.get();
        data.token = token == null ? StringUtils.EMPTY : token;
        data.channel = channel == null ? StringUtils.EMPTY : channel;
        data.channelNo = channelNo == null ? StringUtils.EMPTY : channelNo;
    }

    public static void reset() {
        ThreadContextData data = contexts.get();
        data.exists = false;
        data.traceId = StringUtils.EMPTY;
        data.token = StringUtils.EMPTY;
        data.channel = StringUtils.EMPTY;
        data.channelNo = StringUtils.EMPTY;
        data.controllerStack.clear();
        MDC.remove(TraceId.NAME);
    }

    public static void enterController(BaseController controller) {
        Assert.notNull(controller, "controller can not be null");
        contexts.get().controllerStack.push(controller);
    }

    public static void exitController() {
        contexts.get().controllerStack.pop();
    }

    private static final class ThreadContextData {
        private final Stack<BaseController> controllerStack = new Stack<>();
        private boolean exists;
        private String traceId;
        private String token;
        private String channel;
        private String channelNo;
    }
}
