package com.haiercash.spring.rest.common;

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
public final class CommonResponse<TBody> implements IResponse<TBody> {
    @JSONField(ordinal = 1)
    private CommonResponseHead head;

    @JSONField(ordinal = 2)
    private TBody body;

    CommonResponse() {
    }

    public static <TBody> CommonResponse<TBody> fail(String retFlag, String retMsg) {
        CommonResponseHead head = new CommonResponseHead();
        head.setRetFlag(retFlag);
        head.setRetMsg(retMsg);
        CommonResponse<TBody> response = new CommonResponse<>();
        response.setHead(head);
        return response;
    }

    public static <TBody> CommonResponse<TBody> success() {
        return fail(ConstUtil.SUCCESS_CODE, ConstUtil.SUCCESS_MSG);
    }

    public static <TBody> CommonResponse<TBody> success(TBody body) {
        CommonResponse<TBody> response = success();
        if (body != null)
            response.setBody(body);
        return response;
    }

    @JSONField(serialize = false, deserialize = false)
    @Override
    public String getRetFlag() {
        CommonResponseHead head = this.getHead();
        if (head == null)
            return StringUtils.EMPTY;
        return head.getRetFlag();
    }

    @JSONField(serialize = false, deserialize = false)
    @Override
    public String getRetMsg() {
        CommonResponseHead head = this.getHead();
        if (head == null)
            return StringUtils.EMPTY;
        return head.getRetMsg();
    }

    @JSONField(serialize = false, deserialize = false)
    @Override
    public String getSerNo() {
        return null;
    }

    @Override
    public CommonResponseHead getHead() {
        return this.head;
    }

    @Override
    public TBody getBody() {
        return this.body;
    }

    @JSONField(serialize = false, deserialize = false)
    @Override
    public boolean isSuccess(boolean needBody) {
        String retFlag = this.getRetFlag();
        boolean retFlagOK = Objects.equals(retFlag, ConstUtil.SUCCESS_CODE) || Objects.equals(retFlag, ConstUtil.SUCCESS_CODE2);
        return needBody ? retFlagOK && this.getBody() != null : retFlagOK;
    }
}
