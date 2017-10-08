package com.haiercash.payplatform.rest.cmis;

import com.alibaba.fastjson.JSONObject;
import com.bestvike.lang.Convert;
import com.bestvike.lang.StringUtils;
import com.haiercash.payplatform.rest.IRestResponse;
import com.haiercash.payplatform.utils.ConstUtil;

import java.util.Map;
import java.util.Objects;

/**
 * Created by 许崇雷 on 2017-10-08.
 */
public class CmisResponse extends JSONObject implements IRestResponse {
    public static CmisResponse create(String retFlag, String retMsg) {
        JSONObject head = new JSONObject();
        head.put("retFlag", retFlag);
        head.put("retMsg", retMsg);
        JSONObject response = new JSONObject();
        response.put("head", head);
        CmisResponse json = new CmisResponse();
        json.put("response", response);
        return json;
    }

    @Override
    public boolean isSuccess(boolean needBody) {
        String retFlag = this.getRetFlag();
        boolean retFlagOK = Objects.equals(retFlag, ConstUtil.SUCCESS_CODE) || Objects.equals(retFlag, ConstUtil.SUCCESS_CODE2);
        return needBody ? retFlagOK && this.getBody() != null : retFlagOK;
    }

    @Override
    public String getRetFlag() {
        Map head = this.getHead();
        if (head == null)
            return StringUtils.EMPTY;
        return Convert.toString(head.get("retFlag"));
    }

    @Override
    public String getRetMsg() {
        Map head = this.getHead();
        if (head == null)
            return StringUtils.EMPTY;
        return Convert.toString(head.get("retMsg"));
    }

    public Map getHead() {
        Map response = (Map) this.get("response");
        if (response == null)
            return null;
        return (Map) response.get("head");
    }

    public Map getBody() {
        Map response = (Map) this.get("response");
        if (response == null)
            return null;
        return (Map) response.get("body");
    }
}
