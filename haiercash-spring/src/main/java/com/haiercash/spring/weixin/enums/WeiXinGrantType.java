package com.haiercash.spring.weixin.enums;

/**
 * Created by 许崇雷 on 2017-11-20.
 */
public enum WeiXinGrantType {
    client_credential("client_credential");

    private final String value;

    WeiXinGrantType(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }
}
