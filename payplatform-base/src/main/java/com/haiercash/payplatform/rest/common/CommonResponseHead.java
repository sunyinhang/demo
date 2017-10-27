package com.haiercash.payplatform.rest.common;

import com.alibaba.fastjson.annotation.JSONField;
import com.haiercash.payplatform.servlet.ErrorHandler;
import lombok.Data;

/**
 * Created by 许崇雷 on 2017-10-09.
 */
@Data
public final class CommonResponseHead {
    @JSONField(ordinal = 1)
    private String retFlag;

    @JSONField(ordinal = 2)
    private String retMsg;

    public void setRetFlag(String retFlag) {
        this.retFlag = ErrorHandler.getRetFlag(retFlag);
    }
}
