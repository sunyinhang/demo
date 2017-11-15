package com.haiercash.spring.weixin.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.haiercash.core.lang.Convert;
import com.haiercash.core.lang.StringUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by 许崇雷 on 2017-11-15.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public final class WeiXinTicket extends WeiXinResponse {
    private String ticket;
    private Integer expires_in;
    private String genTime;//获取到的时候自己加上

    @JSONField(serialize = false, deserialize = false)
    public boolean isValid() {
        return !StringUtils.isEmpty(this.genTime) && System.currentTimeMillis() < Convert.toDate(this.genTime).getTime() + 3600 * 1000;
    }
}
