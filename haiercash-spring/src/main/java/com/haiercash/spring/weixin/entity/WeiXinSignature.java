package com.haiercash.spring.weixin.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by 许崇雷 on 2017-11-15.
 */
@Data
@AllArgsConstructor
public final class WeiXinSignature {
    private String timestamp;
    private String noncestr;
    private String signature;
}
