package com.haiercash.spring.weixin;

import com.haiercash.spring.weixin.entity.WeiXinSignature;
import com.haiercash.spring.weixin.entity.WeiXinTicket;
import com.haiercash.spring.weixin.entity.WeiXinToken;
import com.haiercash.spring.weixin.enums.WeiXinGrantType;
import com.haiercash.spring.weixin.enums.WeiXinTicketType;
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

    public static WeiXinToken getToken(WeiXinGrantType grantType) {
        return weiXinApi.getToken(grantType);
    }

    public static WeiXinTicket getTicket(WeiXinGrantType grantType, WeiXinTicketType ticketType) {
        return weiXinApi.getTicket(grantType, ticketType);
    }

    public static WeiXinSignature sign(String url) {
        return weiXinApi.sign(url);
    }

    @PostConstruct
    private void init() {
        weiXinApi = this.weiXinApiInstance;
    }
}
