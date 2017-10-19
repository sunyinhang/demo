package com.haiercash.payplatform.servlet;

import com.haiercash.payplatform.controller.BaseController;
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
    private String token;
    private String channel;
    private String channelNo;
    private Stack<BaseController> controllerStack = new Stack<>();

    //构造函数
    RequestContextData() {
    }

    //region getter

    public boolean isInited() {
        return inited;
    }

    public String getToken() {
        return token;
    }

    public String getChannel() {
        return channel;
    }

    public String getChannelNo() {
        return channelNo;
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

    public void init(String token, String channel, String channelNo) {
//        Assert.hasText(token, "token 不能为空");
//        Assert.hasText(channel, "channel 不能为空");
//        Assert.hasText(channelNo, "channelNo 不能为空");
        this.token = token;
        this.channel = channel;
        this.channelNo = channelNo;
    }

    public void completeInit() {
        this.inited = true;
    }

    public void enterController(BaseController controller) {
        Assert.notNull(controller, "controller can not be null");
        this.controllerStack.push(controller);
    }

    public void exitController() {
        this.controllerStack.pop();
    }
}
