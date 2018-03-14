package com.haiercash.spring.weixin;

import com.haiercash.core.time.DateUtils;
import com.haiercash.spring.redis.RedisLock;
import com.haiercash.spring.redis.RedisUtils;
import com.haiercash.spring.rest.client.JsonClientUtils;
import com.haiercash.spring.weixin.entity.WeiXinSignature;
import com.haiercash.spring.weixin.entity.WeiXinTicket;
import com.haiercash.spring.weixin.entity.WeiXinToken;
import com.haiercash.spring.weixin.enums.WeiXinGrantType;
import com.haiercash.spring.weixin.enums.WeiXinTicketType;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by 许崇雷 on 2017-11-15.
 */
public final class WeiXinApi {
    public static final String URL_GET_TOKEN = "https://api.weixin.qq.com/cgi-bin/token";
    public static final String URL_GET_TICKET = "https://api.weixin.qq.com/cgi-bin/ticket/getticket";
    public static final String URL_GET_MEDIA = "https://api.weixin.qq.com/cgi-bin/media/get";
    private static final int LOCK_TICKET_WAIT = 5000;
    private static final int LOCK_TICKET_TIMEOUT = 10;
    private static final TimeUnit LOCK_TICKET_TIMEUNIT = TimeUnit.SECONDS;
    private static final String KEY_TOKEN = "WEIXIN:TOKEN";
    private static final String KEY_TICKET = "WEIXIN:TICKET";
    private final WeiXinProperties properties;

    public WeiXinApi(WeiXinProperties properties) {
        this.properties = properties;
    }

    private static String timestamp() {
        return Long.toString(System.currentTimeMillis() / 1000);
    }

    private WeiXinToken getToken(WeiXinGrantType grantType) {
        Map<String, Object> params = new HashMap<>();
        params.put("grant_type", grantType.value());
        params.put("appid", this.properties.getAppid());
        params.put("secret", this.properties.getSecret());
        WeiXinToken token = JsonClientUtils.getForObject(URL_GET_TOKEN, WeiXinToken.class, params);
        token.assertSuccess();
        token.setGenTime(DateUtils.now());
        return token;
    }

    private WeiXinTicket getTicket(WeiXinGrantType grantType, WeiXinTicketType ticketType) {
        RedisLock lock = new RedisLock(KEY_TICKET, LOCK_TICKET_WAIT);
        if (!lock.lock(LOCK_TICKET_TIMEOUT, LOCK_TICKET_TIMEUNIT))
            throw new RuntimeException("从 redis 获取微信 ticket 加锁失败");
        try {
            WeiXinTicket ticket = RedisUtils.get(KEY_TICKET, WeiXinTicket.class);
            if (ticket != null && ticket.isValid())
                return ticket;
            //先获取 token
            WeiXinToken token = this.getToken(grantType);
            //获取 ticket 参数
            Map<String, Object> params = new HashMap<>();
            params.put("access_token", token.getAccess_token());
            params.put("type", ticketType.value());
            ticket = JsonClientUtils.getForObject(URL_GET_TICKET, WeiXinTicket.class, params);
            ticket.assertSuccess();
            ticket.setGenTime(DateUtils.now());
            RedisUtils.set(KEY_TOKEN, token);
            RedisUtils.set(KEY_TICKET, ticket);
            return ticket;
        } finally {
            lock.unlock();
        }
    }

    public WeiXinToken getCachedToken() {
        return RedisUtils.get(KEY_TOKEN, WeiXinToken.class);
    }

    public WeiXinSignature sign(String url) {
        String timestamp = timestamp();
        WeiXinTicket ticket = this.getTicket(WeiXinGrantType.client_credential, WeiXinTicketType.jsapi);
        String noncestr = UUID.randomUUID().toString();
        String str = String.format("jsapi_ticket=%s&noncestr=%s&timestamp=%s&url=%s", ticket.getTicket(), noncestr, timestamp, url);
        String signature = DigestUtils.sha1Hex(str);
        return new WeiXinSignature(this.properties.getAppid(), timestamp, noncestr, signature);
    }
}
