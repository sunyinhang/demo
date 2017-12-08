package com.haiercash.spring.rest.cmisacq;

import com.alibaba.fastjson.annotation.JSONField;
import com.haiercash.core.lang.StringUtils;
import com.haiercash.spring.rest.IResponse;
import com.haiercash.spring.utils.ConstUtil;
import lombok.Data;

import java.util.Objects;

/**
 * Created by 许崇雷 on 2017-10-08.
 */
@Data
public final class CmisAcqResponse<TBody> implements IResponse<TBody> {
    @JSONField(ordinal = 1)
    private CmisAcqResponseRoot<TBody> response;

    CmisAcqResponse() {
    }

    public static <TBody> CmisAcqResponse<TBody> fail(String retFlag, String retMsg) {
        CmisAcqResponseHead head = new CmisAcqResponseHead();
        head.setRetFlag(retFlag);
        head.setRetMsg(retMsg);
        CmisAcqResponseRoot<TBody> root = new CmisAcqResponseRoot<>();
        root.setHead(head);
        CmisAcqResponse<TBody> response = new CmisAcqResponse<>();
        response.setResponse(root);
        return response;
    }

    public static <TBody> CmisAcqResponse<TBody> success() {
        return fail(ConstUtil.SUCCESS_CODE, ConstUtil.SUCCESS_MSG);
    }

    public static <TBody> CmisAcqResponse<TBody> success(TBody body) {
        CmisAcqResponse<TBody> response = success();
        if (body != null)
            response.getResponse().setBody(body);
        return response;
    }

    @JSONField(serialize = false, deserialize = false)
    @Override
    public String getRetFlag() {
        CmisAcqResponseHead head = this.getHead();
        if (head == null)
            return StringUtils.EMPTY;
        return head.getRetFlag();
    }

    @JSONField(serialize = false, deserialize = false)
    @Override
    public String getRetMsg() {
        CmisAcqResponseHead head = this.getHead();
        if (head == null)
            return StringUtils.EMPTY;
        return head.getRetMsg();
    }

    @JSONField(serialize = false, deserialize = false)
    @Override
    public String getSerNo() {
        CmisAcqResponseHead head = this.getHead();
        if (head == null)
            return StringUtils.EMPTY;
        return head.getSerno();
    }

    @JSONField(serialize = false, deserialize = false)
    @Override
    public CmisAcqResponseHead getHead() {
        CmisAcqResponseRoot response = this.response;
        if (response == null)
            return null;
        return response.getHead();
    }

    @JSONField(serialize = false, deserialize = false)
    @Override
    public TBody getBody() {
        CmisAcqResponseRoot<TBody> response = this.response;
        if (response == null)
            return null;
        return response.getBody();
    }

    @JSONField(serialize = false, deserialize = false)
    @Override
    public boolean isSuccess(boolean needBody) {
        String retFlag = this.getRetFlag();
        boolean retFlagOK = Objects.equals(retFlag, ConstUtil.SUCCESS_CODE) || Objects.equals(retFlag, ConstUtil.SUCCESS_CODE2);
        return needBody ? retFlagOK && this.getBody() != null : retFlagOK;
    }
}
