package com.haiercash.payplatform.rest.cmisacq;

import com.bestvike.lang.StringUtils;
import com.haiercash.payplatform.rest.IRestResponse;
import com.haiercash.payplatform.utils.ConstUtil;
import lombok.Data;
import lombok.ToString;

import java.util.Objects;

/**
 * Created by 许崇雷 on 2017-10-08.
 */
@Data
public class CmisAcqResponse<TBody> implements IRestResponse<CmisAcqResponseHead, TBody> {
    private CmisAcqResponseRoot<TBody> response;

    public static <TBody> CmisAcqResponse<TBody> create(String retFlag, String retMsg) {
        CmisAcqResponseHead head = new CmisAcqResponseHead();
        head.setRetFlag(retFlag);
        head.setRetMsg(retMsg);
        CmisAcqResponseRoot<TBody> root = new CmisAcqResponseRoot<>();
        root.setHead(head);
        CmisAcqResponse<TBody> response = new CmisAcqResponse<>();
        response.setResponse(root);
        return response;
    }

    @Override
    public boolean isSuccess(boolean needBody) {
        String retFlag = this.getRetFlag();
        boolean retFlagOK = Objects.equals(retFlag, ConstUtil.SUCCESS_CODE) || Objects.equals(retFlag, ConstUtil.SUCCESS_CODE2);
        return needBody ? retFlagOK && this.getBody() != null : retFlagOK;
    }

    @Override
    public String getRetFlag() {
        CmisAcqResponseHead head = this.getHead();
        if (head == null)
            return StringUtils.EMPTY;
        return head.getRetFlag();
    }

    @Override
    public String getRetMsg() {
        CmisAcqResponseHead head = this.getHead();
        if (head == null)
            return StringUtils.EMPTY;
        return head.getRetMsg();
    }

    @Override
    public CmisAcqResponseHead getHead() {
        CmisAcqResponseRoot response = this.response;
        if (response == null)
            return null;
        return response.getHead();
    }

    @Override
    public TBody getBody() {
        CmisAcqResponseRoot<TBody> response = this.response;
        if (response == null)
            return null;
        return response.getBody();
    }
}
