package com.haiercash.payplatform.rest.common;

import com.alibaba.fastjson.annotation.JSONField;
import com.bestvike.lang.StringUtils;
import com.haiercash.payplatform.rest.IResponse;
import com.haiercash.payplatform.utils.ConstUtil;
import lombok.Data;

import java.util.Objects;

/**
 * Created by 许崇雷 on 2017-10-08.
 */
@Data
public class CommonResponse<TBody> implements IResponse<TBody> {
    private CommonResponseHead head;
    private TBody body;

    public static <TBody> CommonResponse<TBody> create(String retFlag, String retMsg) {
        CommonResponseHead head = new CommonResponseHead();
        head.setRetFlag(retFlag);
        head.setRetMsg(retMsg);
        CommonResponse<TBody> response = new CommonResponse<>();
        response.setHead(head);
        return response;
    }

    @JSONField(serialize = false, deserialize = false)
    @Override
    public boolean isSuccess(boolean needBody) {
        String retFlag = this.getRetFlag();
        boolean retFlagOK = Objects.equals(retFlag, ConstUtil.SUCCESS_CODE) || Objects.equals(retFlag, ConstUtil.SUCCESS_CODE2);
        return needBody ? retFlagOK && this.getBody() != null : retFlagOK;
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

    @Override
    public CommonResponseHead getHead() {
        return this.head;
    }

    @Override
    public TBody getBody() {
        return this.body;
    }
}
