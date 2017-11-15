package com.haiercash.spring.weixin;

import com.haiercash.spring.weixin.entity.WeiXinSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created by 许崇雷 on 2017-11-15.
 */
@Component
public class WeiXinUtils {
    private static WeiXinApi weiXinApi;
    @Autowired
    private WeiXinApi weiXinApiInstance;

    public static WeiXinSignature sign(String url) {
        return weiXinApi.sign(url);
    }

    @PostConstruct
    private void init() {
        weiXinApi = this.weiXinApiInstance;
    }
}
