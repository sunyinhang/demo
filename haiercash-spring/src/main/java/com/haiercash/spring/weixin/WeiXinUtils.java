package com.haiercash.spring.weixin;

import com.haiercash.spring.weixin.entity.WeiXinSignature;
import com.haiercash.spring.weixin.entity.WeiXinToken;

/**
 * Created by 许崇雷 on 2017-11-15.
 */
public final class WeiXinUtils {
    private static WeiXinApi getWeiXinApi() {
        return WeiXinApiProvider.getWeiXinApi();
    }

    public static WeiXinToken getCachedToken() {
        return getWeiXinApi().getCachedToken();
    }

    public static WeiXinSignature sign(String url) {
        return getWeiXinApi().sign(url);
    }
}
