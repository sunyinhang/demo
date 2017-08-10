package com.haiercash.payplatform.common.filter;

import com.haiercash.payplatform.common.controller.BaseController;
import org.springframework.util.Assert;

import java.util.Stack;

/**
 * 请求上下文数据
 *
 * @author 许崇雷
 * @date 2017/7/1
 */
public final class RequestContextData {
    private boolean inited;
    private String channel;
    private String channelNo;
    private String token;
    private boolean needVerify;
    private boolean executedVerify;
    private Stack<BaseController> controllerStack = new Stack<>();

    //构造函数
    RequestContextData() {
    }

    //region getter

    public boolean isInited() {
        return inited;
    }

    public String getChannel() {
        return channel;
    }

    public String getChannelNo() {
        return channelNo;
    }

    public String getToken() {return token;}

    public boolean isNeedVerify() {
        return needVerify;
    }

    public boolean isExecutedVerify() {
        return executedVerify;
    }

    public BaseController getEntryController() {
        return this.controllerStack.empty() ? null : this.controllerStack.firstElement();
    }

    public BaseController getExecutingController() {
        return this.controllerStack.empty() ? null : this.controllerStack.lastElement();
    }

    public BaseController[] getControllerStack() {
        return this.controllerStack.toArray(new BaseController[this.controllerStack.size()]);
    }

    public String getEntryModuleNo() {
        BaseController controller = this.getEntryController();
        return controller == null ? null : controller.getModuleNo();
    }

    public String getExecutingModuleNo() {
        BaseController controller = this.getExecutingController();
        return controller == null ? null : controller.getModuleNo();
    }

    //endregion

    public void initChannel(String channel, String channelNo) {
//        Assert.notNull(channel, "channel can not be null");
//        Assert.notNull(channelNo, "channelNo can not be null");
        this.channel = channel;
        this.channelNo = channelNo;
    }

    public void initToken(String token) {
        this.token = token;
    }

    public void completeInit() {
        this.inited = true;
    }

    public void setNeedVerify(boolean needVerify) {
        this.needVerify = needVerify;
    }

    public void setExecutedVerify() {
        this.executedVerify = true;
    }

    public void enterController(BaseController controller) {
        Assert.notNull(controller, "controller can not be null");
        this.controllerStack.push(controller);
    }

    public void exitController() {
        this.controllerStack.pop();
    }
}
