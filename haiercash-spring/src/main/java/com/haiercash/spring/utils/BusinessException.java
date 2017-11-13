package com.haiercash.spring.utils;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by use on 2017/8/15.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public final class BusinessException extends RuntimeException {
    private String retFlag;
    private String retMsg;

    public BusinessException(String retFlag, String retMsg) {
        super(retFlag + ":" + retMsg);
        this.retFlag = retFlag;
        this.retMsg = retMsg;
    }
}
