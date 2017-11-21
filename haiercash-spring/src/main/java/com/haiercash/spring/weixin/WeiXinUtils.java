package com.haiercash.spring.weixin;

import com.haiercash.spring.weixin.entity.WeiXinSignature;
import com.haiercash.spring.weixin.entity.WeiXinTicket;
import com.haiercash.spring.weixin.entity.WeiXinToken;
import com.haiercash.spring.weixin.enums.WeiXinGrantType;
import com.haiercash.spring.weixin.enums.WeiXinTicketType;

/**
 * Created by 许崇雷 on 2017-11-15.
 */
public final class WeiXinUtils {
    private static WeiXinApi getWeiXinApi() {
        return WeiXinApiProvider.getWeiXinApi();
    }

    public static WeiXinToken getToken(WeiXinGrantType grantType) {
        return getWeiXinApi().getToken(grantType);
    }

    public static WeiXinTicket getTicket(WeiXinGrantType grantType, WeiXinTicketType ticketType) {
        return getWeiXinApi().getTicket(grantType, ticketType);
    }

    public static WeiXinSignature sign(String url) {
        return getWeiXinApi().sign(url);
    }
}
