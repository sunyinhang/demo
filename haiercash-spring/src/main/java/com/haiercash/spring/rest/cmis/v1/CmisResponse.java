package com.haiercash.spring.rest.cmis.v1;

import com.alibaba.fastjson.annotation.JSONField;
import com.haiercash.core.lang.StringUtils;
import com.haiercash.spring.rest.cmis.ICmisResponse;
import com.haiercash.spring.util.ConstUtil;
import lombok.Data;

import java.lang.reflect.Type;
import java.util.Objects;

/**
 * Created by 许崇雷 on 2017-10-08.
 */
@Data
public final class CmisResponse<TBody> implements ICmisResponse<TBody> {
    @JSONField(ordinal = 1)
    private CmisResponseRoot<TBody> response;

    CmisResponse() {
    }

    public static <TBody> CmisResponse<TBody> fail(String retFlag, String retMsg) {
        CmisResponseHead head = new CmisResponseHead();
        head.setRetFlag(retFlag);
        head.setRetMsg(retMsg);
        CmisResponseRoot<TBody> root = new CmisResponseRoot<>();
        root.setHead(head);
        CmisResponse<TBody> response = new CmisResponse<>();
        response.setResponse(root);
        return response;
    }

    public static <TBody> CmisResponse<TBody> success() {
        return fail(ConstUtil.SUCCESS_CODE, ConstUtil.SUCCESS_MSG);
    }

    public static <TBody> CmisResponse<TBody> success(TBody body) {
        CmisResponse<TBody> response = success();
        if (body != null)
            response.getResponse().setBody(body);
        return response;
    }

    @JSONField(serialize = false, deserialize = false)
    @Override
    public String getSerNo() {
        CmisResponseHead head = this.getHead();
        if (head == null)
            return StringUtils.EMPTY;
        return head.getSerno();
    }

    @JSONField(serialize = false, deserialize = false)
    @Override
    public String getRetFlag() {
        CmisResponseHead head = this.getHead();
        if (head == null)
            return StringUtils.EMPTY;
        return head.getRetFlag();
    }

    @JSONField(serialize = false, deserialize = false)
    @Override
    public String getRetMsg() {
        CmisResponseHead head = this.getHead();
        if (head == null)
            return StringUtils.EMPTY;
        return head.getRetMsg();
    }

    @JSONField(serialize = false, deserialize = false)
    @Override
    public CmisResponseHead getHead() {
        CmisResponseRoot response = this.response;
        if (response == null)
            return null;
        return response.getHead();
    }

    @JSONField(serialize = false, deserialize = false)
    @Override
    public TBody getBody() {
        CmisResponseRoot<TBody> response = this.response;
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

    @Override
    public ICmisResponse<TBody> init(Type bodyType) {
        return this;
    }
}
