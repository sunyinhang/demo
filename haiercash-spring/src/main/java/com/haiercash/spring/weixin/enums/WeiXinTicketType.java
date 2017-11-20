package com.haiercash.spring.weixin.enums;

/**
 * Created by 许崇雷 on 2017-11-20.
 */
public enum WeiXinTicketType {
    jsapi("jsapi");

    private final String value;

    WeiXinTicketType(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }
}
