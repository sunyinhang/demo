package com.haiercash.payplatform.common.enums;

/**
 * Created by 许崇雷 on 2017-10-07.
 */
public enum CooperativeMsgEnum {
    INACTIVE("00"),//未激活
    ACTIVE("01");//激活

    private final String value;

    public String value() {
        return this.value;
    }

    CooperativeMsgEnum(String value) {
        this.value = value;
    }
}
