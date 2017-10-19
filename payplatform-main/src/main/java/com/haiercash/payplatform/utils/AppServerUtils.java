package com.haiercash.payplatform.utils;

import com.haiercash.payplatform.config.EurekaServer;
import com.haiercash.payplatform.context.ThreadContext;
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
                return EurekaServer.APPSERVERNOAUTH;
            case "46": //顺逛白条
                return EurekaServer.APPSERVERNOAUTHNEW;
            default:
                return EurekaServer.APPSERVERNOAUTHNEW;
        }
    }
}
