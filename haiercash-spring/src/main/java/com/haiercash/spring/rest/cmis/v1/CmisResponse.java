package com.haiercash.spring.rest.cmis.v1;

import com.alibaba.fastjson.annotation.JSONField;
import com.haiercash.core.lang.StringUtils;
import com.haiercash.spring.rest.cmis.ICmisResponse;
import lombok.Data;

/**
 * Created by 许崇雷 on 2017-10-08.
 */
@Data
public final class CmisResponse<TBody> implements ICmisResponse<TBody> {
    @JSONField(ordinal = 1)
    private CmisResponseRoot<TBody> response;

    CmisResponse() {
    }

    @Override
    public String getSerNo() {
        CmisResponseHead head = this.getHead();
        if (head == null)
            return StringUtils.EMPTY;
        return head.getSerno();
    }

    @Override
    public String getRetFlag() {
        CmisResponseHead head = this.getHead();
        if (head == null)
            return StringUtils.EMPTY;
        return head.getRetFlag();
    }

    @Override
    public String getRetMsg() {
        CmisResponseHead head = this.getHead();
        if (head == null)
            return StringUtils.EMPTY;
        return head.getRetMsg();
    }

    @Override
    public CmisResponseHead getHead() {
        CmisResponseRoot response = this.response;
        if (response == null)
            return null;
        return response.getHead();
    }

    @Override
    public TBody getBody() {
        CmisResponseRoot<TBody> response = this.response;
        if (response == null)
            return null;
        return response.getBody();
    }
}
