package com.haiercash.payplatform.context;

import com.bestvike.lang.StringUtils;
import com.haiercash.payplatform.controller.BaseController;
import com.haiercash.payplatform.diagnostics.TraceID;
import org.springframework.util.Assert;

import java.util.Stack;

/**
 * 线程上下文
 * Created by 许崇雷 on 2017-10-19.
 */
public final class ThreadContext {
    //线程本地存储
    private final static ThreadLocal<ThreadContextData> contexts = ThreadLocal.withInitial(ThreadContextData::new);

    //region property

    public static boolean exists() {
        return contexts.get().exists;
    }

    public static String getTraceID() {
        return contexts.get().traceID;
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
        ThreadContextData data = new ThreadContextData();
        return data.controllerStack.empty() ? null : data.controllerStack.firstElement();
    }

    public static BaseController getExecutingController() {
        ThreadContextData data = new ThreadContextData();
        return data.controllerStack.empty() ? null : data.controllerStack.lastElement();
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
        String traceID = RequestContext.exists() ? RequestContext.getRequest().getHeader(TraceID.NAME) : null;
        if (StringUtils.isEmpty(traceID))
            traceID = TraceID.generate();
        ThreadContextData data = contexts.get();
        data.exists = true;
        data.traceID = traceID;
        data.token = token;
        data.channel = channel;
        data.channelNo = channelNo;
        data.controllerStack.clear();
    }

    public static void reset() {
        ThreadContextData data = contexts.get();
        data.exists = false;
        data.traceID = null;
        data.token = null;
        data.channel = null;
        data.channelNo = null;
        data.controllerStack.clear();
    }

    public static void enterController(BaseController controller) {
        Assert.notNull(controller, "controller can not be null");
        contexts.get().controllerStack.push(controller);
    }

    public static void exitController() {
        contexts.get().controllerStack.pop();
    }

    private static final class ThreadContextData {
        private boolean exists;
        private String traceID;
        private String token;
        private String channel;
        private String channelNo;
        private Stack<BaseController> controllerStack = new Stack<>();
    }
}
