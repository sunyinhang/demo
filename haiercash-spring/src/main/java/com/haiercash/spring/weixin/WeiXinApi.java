package com.haiercash.spring.weixin;

import com.haiercash.core.lang.DateUtils;
import com.haiercash.spring.client.RestTemplateProvider;
import com.haiercash.spring.redis.RedisLock;
import com.haiercash.spring.redis.RedisUtils;
import com.haiercash.spring.utils.BusinessException;
import com.haiercash.spring.weixin.entity.WeiXinSignature;
import com.haiercash.spring.weixin.entity.WeiXinTicket;
import com.haiercash.spring.weixin.entity.WeiXinToken;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by 许崇雷 on 2017-11-15.
 */
@Component
public final class WeiXinApi {
    private static final String LOCK_TICKET = "WEIXIN:LOCK_TICKET";
    private static final int LOCK_TICKET_TIMEOUT = 8;
    private static final TimeUnit LOCK_TICKET_TIMEUNIT = TimeUnit.SECONDS;
    private static final String KEY_TICKET = "WEIXIN:TICKET";
    private static final String TICKET_TYPE = "jsapi";
    private static final String URL_GET_ACCESS_TOKEN = "https://api.weixin.qq.com/cgi-bin/token";
    private static final String URL_GET_TICKET = "https://api.weixin.qq.com/cgi-bin/ticket/getticket";

    @Autowired
    private WeiXinProperties properties;

    private static RestTemplate getRestTemplate() {
        return RestTemplateProvider.getRestTemplateJson();
    }

    private static String createTimestamp() {
        return Long.toString(System.currentTimeMillis() / 1000);
    }

    private WeiXinToken getToken() {
        Map<String, Object> params = new HashMap<>();
        params.put("grant_type", "client_credential");
        params.put("appid", this.properties.getAppid());
        params.put("secret", this.properties.getSecret());
        WeiXinToken token = getRestTemplate().getForObject(URL_GET_ACCESS_TOKEN, WeiXinToken.class, params);
        if (!token.isSuccess())
            throw new BusinessException(token.getErrorcodeStr(), token.getErrormsg());
        return token;
    }

    private WeiXinTicket getTicket(String type) {
        RedisLock lock = new RedisLock(LOCK_TICKET);
        if (!lock.lock(LOCK_TICKET_TIMEOUT, LOCK_TICKET_TIMEUNIT))
            throw new RuntimeException("从 redis 获取微信 ticket 加锁失败");
        try {
            WeiXinTicket ticket = RedisUtils.get(KEY_TICKET, WeiXinTicket.class);
            if (ticket != null && ticket.isValid())
                return ticket;
            //先获取 token
            WeiXinToken token = this.getToken();
            //获取 ticket 参数
            Map<String, Object> params = new HashMap<>();
            params.put("access_token", token.getAccess_token());
            params.put("type", type);
            ticket = getRestTemplate().getForObject(URL_GET_TICKET, WeiXinTicket.class, params);
            if (!ticket.isSuccess())
                throw new BusinessException(ticket.getErrorcodeStr(), ticket.getErrormsg());
            ticket.setGenTime(DateUtils.nowString());
            RedisUtils.set(KEY_TICKET, ticket);
            return ticket;
        } finally {
            lock.unlock();
        }
    }

    public WeiXinSignature sign(String url) {
        String timestamp = createTimestamp();
        WeiXinTicket ticket = this.getTicket(TICKET_TYPE);
        String noncestr = UUID.randomUUID().toString();
        String str = String.format("jsapi_ticket=%s&noncestr=%s&timestamp=%s&url=%s", ticket.getTicket(), noncestr, timestamp, url);
        String signature = DigestUtils.sha1Hex(str);
        return new WeiXinSignature(timestamp, noncestr, signature);
    }
}
