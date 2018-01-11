package com.haiercash.spring.rest.cmis.v1;

import com.alibaba.fastjson.annotation.JSONField;
import com.haiercash.spring.rest.cmis.ICmisRequest;
import lombok.Data;

/**
 * Created by 许崇雷 on 2018-01-08.
 */
@Data
public final class CmisRequest implements ICmisRequest {
    @JSONField(ordinal = 1)
    private CmisRequestRoot request;

    @Override
    public String getSerNo() {
        if (this.request == null)
            return null;
        CmisRequestHead head = this.request.getHead();
        return head == null ? null : head.getSerno();
    }

    @Override
    public String getTradeCode() {
        if (this.request == null)
            return null;
        CmisRequestHead head = this.request.getHead();
        return head == null ? null : head.getTradeCode();
    }
}
