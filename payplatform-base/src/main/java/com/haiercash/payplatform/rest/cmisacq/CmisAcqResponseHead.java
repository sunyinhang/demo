package com.haiercash.payplatform.rest.cmisacq;

import com.alibaba.fastjson.annotation.JSONField;
import com.haiercash.payplatform.servlet.ErrorHandler;
import lombok.Data;

/**
 * Created by 许崇雷 on 2017-10-09.
 */
@Data
public final class CmisAcqResponseHead {
    @JSONField(ordinal = 1)
    private String retFlag;

    @JSONField(ordinal = 2)
    private String retMsg;

    @JSONField(ordinal = 3)
    private String serno;

    public void setRetFlag(String retFlag) {
        this.retFlag = ErrorHandler.getRetFlag(retFlag);
    }
}
