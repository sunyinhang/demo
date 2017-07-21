package com.haiercash.appserver.web;

import com.haiercash.appserver.service.AppCertMsgService;
import com.haiercash.common.data.AppCertMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Objects;

/**
 * Created by yinjun on 2017/2/20.
 */
@RestController
public class AppCertMsgController extends BaseController {
    public AppCertMsgController() {
        super("20");
    }

    @Autowired
    AppCertMsgService appCertMsgService;

    @RequestMapping(value = "/app/appserver/saveCardMsg", method = RequestMethod.POST)
    public Map<String, Object> saveCardMsg(@RequestBody AppCertMsg appCertMsg) {
        String channel = super.getChannel();
        String channelNo = super.getChannelNO();
        if (Objects.equals(channel, "13")) {
            //channelNo = "05";//chanelNo为空，并且chanel为13或者14的
            channelNo = "app_person";
        } else if (Objects.equals(channel, "14")) {
            channelNo = "app_merch";
        } else if (Objects.equals(channel, "16")) {
            //   channelNo = "31";//chanelNo为空并且chanel为16的
            channelNo = "app_xcd";
        }
        try {
            return appCertMsgService.saveCardMsg(appCertMsg, channelNo);

        } catch (Exception e) {
            logger.error("身份证信息保存失败==>可能是已存储过该用户信息");
            logger.error("异常信息：" + e.getMessage());
            return fail("90", "身份证信息保存失败！");
        }
    }
}
