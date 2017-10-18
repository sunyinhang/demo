package com.haiercash.payplatform.utils;

import com.bestvike.lang.StringUtils;
import com.haiercash.payplatform.config.EurekaServer;
import com.haiercash.payplatform.filter.RequestContext;
import lombok.experimental.UtilityClass;

/**
 * Created by 许崇雷 on 2017-10-10.
 */
@UtilityClass
public class AppServerUtils {
    public static String getAppServerUrl() {
        //消息推送适用
        if(!RequestContext.exists()){
            return EurekaServer.APPSERVERNOAUTHNEW;
        }
        //
        String channelNo = RequestContext.data().getChannelNo();
        //
        if(StringUtils.isEmpty(channelNo)){
            return EurekaServer.APPSERVERNOAUTHNEW;
        }
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
