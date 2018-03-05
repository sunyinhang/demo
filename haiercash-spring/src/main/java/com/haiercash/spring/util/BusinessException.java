package com.haiercash.spring.util;

import lombok.Getter;

/**
 * Created by 许崇雷 on 2018-03-05.
 */
@Getter
public final class BusinessException extends RuntimeException {
    private String retFlag;
    private String retMsg;

    public BusinessException(String retFlag, String retMsg) {
        super(retFlag + ":" + retMsg);
        this.retFlag = retFlag;
        this.retMsg = retMsg;
    }
}
