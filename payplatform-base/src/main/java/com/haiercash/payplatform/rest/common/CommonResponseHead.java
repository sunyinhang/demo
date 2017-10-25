package com.haiercash.payplatform.rest.common;

import com.haiercash.payplatform.servlet.ErrorHandler;
import lombok.Data;

/**
 * Created by 许崇雷 on 2017-10-09.
 */
@Data
public final class CommonResponseHead {
    private String retFlag;
    private String retMsg;

    public void setRetFlag(String retFlag) {
        this.retFlag = ErrorHandler.getRetFlag(retFlag);
    }
}
