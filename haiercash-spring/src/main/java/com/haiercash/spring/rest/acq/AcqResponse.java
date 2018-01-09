package com.haiercash.spring.rest.acq;

import com.alibaba.fastjson.annotation.JSONField;
import com.haiercash.core.lang.StringUtils;
import com.haiercash.spring.rest.IResponse;
import com.haiercash.spring.util.ConstUtil;
import lombok.Data;

import java.util.Objects;

/**
 * Created by 许崇雷 on 2017-10-08.
 */
@Data
public final class AcqResponse<TBody> implements IResponse<TBody> {
    @JSONField(ordinal = 1)
    private AcqResponseRoot<TBody> response;

    AcqResponse() {
    }

    public static <TBody> AcqResponse<TBody> fail(String retFlag, String retMsg) {
        AcqResponseHead head = new AcqResponseHead();
        head.setRetFlag(retFlag);
        head.setRetMsg(retMsg);
        AcqResponseRoot<TBody> root = new AcqResponseRoot<>();
        root.setHead(head);
        AcqResponse<TBody> response = new AcqResponse<>();
        response.setResponse(root);
        return response;
    }

    public static <TBody> AcqResponse<TBody> success() {
        return fail(ConstUtil.SUCCESS_CODE, ConstUtil.SUCCESS_MSG);
    }

    public static <TBody> AcqResponse<TBody> success(TBody body) {
        AcqResponse<TBody> response = success();
        if (body != null)
            response.getResponse().setBody(body);
        return response;
    }

    @JSONField(serialize = false, deserialize = false)
    @Override
    public String getSerNo() {
        AcqResponseHead head = this.getHead();
        if (head == null)
            return StringUtils.EMPTY;
        return head.getSerno();
    }

    @JSONField(serialize = false, deserialize = false)
    @Override
    public String getRetFlag() {
        AcqResponseHead head = this.getHead();
        if (head == null)
            return StringUtils.EMPTY;
        return head.getRetFlag();
    }

    @JSONField(serialize = false, deserialize = false)
    @Override
    public String getRetMsg() {
        AcqResponseHead head = this.getHead();
        if (head == null)
            return StringUtils.EMPTY;
        return head.getRetMsg();
    }

    @JSONField(serialize = false, deserialize = false)
    @Override
    public AcqResponseHead getHead() {
        AcqResponseRoot response = this.response;
        if (response == null)
            return null;
        return response.getHead();
    }

    @JSONField(serialize = false, deserialize = false)
    @Override
    public TBody getBody() {
        AcqResponseRoot<TBody> response = this.response;
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
