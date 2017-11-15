package com.haiercash.spring.weixin.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by 许崇雷 on 2017-11-15.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public final class WeiXinToken extends WeiXinResponse {
    private String access_token;
    private Integer expires_in;
}
