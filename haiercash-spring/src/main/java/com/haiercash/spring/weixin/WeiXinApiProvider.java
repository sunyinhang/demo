package com.haiercash.spring.weixin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created by 许崇雷 on 2017-11-20.
 */
@Component
public final class WeiXinApiProvider {
    private static WeiXinApi weiXinApi;
    @Autowired
    private WeiXinApi weiXinApiInstance;

    public static WeiXinApi getWeiXinApi() {
        return weiXinApi;
    }

    @PostConstruct
    private void init() {
        weiXinApi = this.weiXinApiInstance;
    }
}
