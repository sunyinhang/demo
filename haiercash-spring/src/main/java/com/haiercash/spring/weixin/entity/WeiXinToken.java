package com.haiercash.spring.weixin.entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * Created by 许崇雷 on 2017-11-15.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public final class WeiXinToken extends WeiXinResponse {
    private String access_token;
    private Integer expires_in;//有效秒
    private Date genTime;//获取到的时候自己加上

    @JSONField(serialize = false, deserialize = false)
    public boolean isValid() {
        return this.genTime != null && this.expires_in != null && System.currentTimeMillis() <= this.genTime.getTime() + this.expires_in * 1000;
    }
}
