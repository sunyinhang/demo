package com.haiercash.payplatform.utils;

import com.alibaba.fastjson.annotation.JSONField;
import com.haiercash.payplatform.servlet.ErrorHandler;
import lombok.Data;

/**
 * Created by use on 2017/7/25.
 */
@Data
public final class ResultHead {
    @JSONField(ordinal = 1)
    private String retFlag;

    @JSONField(ordinal = 2)
    private String retMsg;

    public void setRetFlag(String retFlag) {
        this.retFlag = ErrorHandler.getRetFlag(retFlag);
    }
}
