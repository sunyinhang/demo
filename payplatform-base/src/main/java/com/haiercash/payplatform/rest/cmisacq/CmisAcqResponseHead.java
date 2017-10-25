package com.haiercash.payplatform.rest.cmisacq;

import com.haiercash.payplatform.servlet.ErrorHandler;
import lombok.Data;

/**
 * Created by 许崇雷 on 2017-10-09.
 */
@Data
public final class CmisAcqResponseHead {
    private String retFlag;
    private String retMsg;
    private String serno;

    public void setRetFlag(String retFlag) {
        this.retFlag = ErrorHandler.getRetFlag(retFlag);
    }
}
