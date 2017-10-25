package com.haiercash.payplatform.utils;

import lombok.Data;

/**
 * Created by use on 2017/8/15.
 */
@Data
public class BusinessException extends RuntimeException {
    private String retFlag;
    private String retMsg;

    public BusinessException(String retFlag, String retMsg) {
        super(retFlag + ":" + retMsg);
        this.retFlag = retFlag;
        this.retMsg = retMsg;
    }
}
