package com.haiercash.spring.rest.acq;

import com.alibaba.fastjson.annotation.JSONField;
import com.haiercash.spring.trace.rest.ErrorHandler;
import lombok.Data;

/**
 * Created by 许崇雷 on 2017-10-09.
 */
@Data
public final class AcqResponseHead {
    @JSONField(ordinal = 1)
    private String serno;

    @JSONField(ordinal = 2)
    private String retFlag;

    @JSONField(ordinal = 3)
    private String retMsg;

    AcqResponseHead() {
    }

    public void setRetFlag(String retFlag) {
        this.retFlag = ErrorHandler.getRetFlag(retFlag);
    }
}
