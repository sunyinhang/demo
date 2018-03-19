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
public final class WeiXinTicket extends WeiXinResponse {
    private static final int EXPIRE = 60;//超时时间,分钟
    private String ticket;
    private Integer expires_in;
    private Date genTime;//获取到的时候自己加上

    @JSONField(serialize = false, deserialize = false)
    public boolean isValid() {
        return this.genTime != null && System.currentTimeMillis() <= this.genTime.getTime() + EXPIRE * 60 * 1000;
    }
}
