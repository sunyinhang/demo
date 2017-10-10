package com.haiercash.payplatform.utils;

import com.haiercash.payplatform.config.EurekaServer;
import com.haiercash.payplatform.filter.RequestContext;
import lombok.experimental.UtilityClass;

/**
 * Created by 许崇雷 on 2017-10-10.
 */
@UtilityClass
public class AppServerUtils {
    public String getAppServerUrl() {
        String channelNo = RequestContext.data().getChannelNo();
        switch (channelNo) {
            case "11":
            case "23":
                return EurekaServer.APPSERVER;

            default:
                return EurekaServer.APPSERVERNOAUTHNEW;
        }
    }
}
