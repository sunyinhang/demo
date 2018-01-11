package com.haiercash.spring.rest.acq;

import com.alibaba.fastjson.annotation.JSONField;
import com.haiercash.core.lang.StringUtils;
import com.haiercash.spring.rest.IResponse;
import lombok.Data;

/**
 * Created by 许崇雷 on 2017-10-08.
 */
@Data
public final class AcqResponse<TBody> implements IResponse<TBody> {
    @JSONField(ordinal = 1)
    private AcqResponseRoot<TBody> response;

    AcqResponse() {
    }

    @Override
    public String getSerNo() {
        AcqResponseHead head = this.getHead();
        if (head == null)
            return StringUtils.EMPTY;
        return head.getSerno();
    }

    @Override
    public String getRetFlag() {
        AcqResponseHead head = this.getHead();
        if (head == null)
            return StringUtils.EMPTY;
        return head.getRetFlag();
    }

    @Override
    public String getRetMsg() {
        AcqResponseHead head = this.getHead();
        if (head == null)
            return StringUtils.EMPTY;
        return head.getRetMsg();
    }

    @Override
    public AcqResponseHead getHead() {
        AcqResponseRoot response = this.response;
        if (response == null)
            return null;
        return response.getHead();
    }

    @Override
    public TBody getBody() {
        AcqResponseRoot<TBody> response = this.response;
        if (response == null)
            return null;
        return response.getBody();
    }
}
