package com.haiercash.payplatform.utils;

import com.haiercash.spring.config.EurekaServer;
import com.haiercash.spring.context.ThreadContext;
import lombok.experimental.UtilityClass;

/**
 * Created by 许崇雷 on 2017-10-10.
 */
@UtilityClass
public class AppServerUtils {
    public static String getAppServerUrl() {
        String channelNo = ThreadContext.getChannelNo();
        switch (channelNo) {
            case "33":  //乔融
                return EurekaServer.APPSERVERNOAUTHNEW;
            case "46": //顺逛白条
                return EurekaServer.APPSERVERNOAUTHNEW;
            case "53"://vipabc
                return EurekaServer.APPSERVER;
            default:
                return EurekaServer.APPSERVERNOAUTHNEW;
        }
    }
}
