package com.haiercash.payplatform.utils;

import com.haiercash.payplatform.servlet.ErrorHandler;
import lombok.Data;

/**
 * Created by use on 2017/7/25.
 */
@Data
public final class ResultHead {
    private String retFlag;
    private String retMsg;

    public void setRetFlag(String retFlag) {
        this.retFlag = ErrorHandler.getRetFlag(retFlag);
    }
}
