package com.haiercash.spring.rest.cmis;

import com.bestvike.linq.exception.InvalidOperationException;
import com.haiercash.core.lang.BeanUtils;
import com.haiercash.core.lang.DateUtils;
import com.haiercash.core.lang.RandomUtils;
import com.haiercash.spring.context.ThreadContext;
import com.haiercash.spring.rest.cmis.v1.CmisRequest;
import com.haiercash.spring.rest.cmis.v1.CmisRequestHead;
import com.haiercash.spring.rest.cmis.v1.CmisRequestRoot;
import com.haiercash.spring.rest.cmis.v2.CmisRequest2;

import java.util.Date;

/**
 * Created by 许崇雷 on 2017-11-29.
 */
public final class CmisRequestBuilder {
    private final CmisVersion version;
    private final String serno;
    private final String tradeCode;
    private final String tradeDate;
    private final String tradeTime;
    private String tradeType;
    private String sysFlag;
    private String channelNo;
    private String cooprCode;
    private Object body;

    private CmisRequestBuilder(CmisVersion version, String tradeCode) {
        Date now = DateUtils.now();
        this.version = version;
        this.serno = String.valueOf(new Date().getTime()) + RandomUtils.nextInt(100);
        this.tradeCode = tradeCode;
        this.tradeDate = DateUtils.toDateString(now);
        this.tradeTime = DateUtils.toTimeString(now);
        this.sysFlag = ThreadContext.getChannel();
        this.channelNo = ThreadContext.getChannelNo();
    }

    public static CmisRequestBuilder newBuilder(String tradeCode) {
        CmisVersion version = CmisVersion.forTradeCode(tradeCode);
        return new CmisRequestBuilder(version, tradeCode);
    }

    public CmisRequestBuilder tradeType(String tradeType) {
        this.tradeType = tradeType;
        return this;
    }

    public CmisRequestBuilder sysFlag(String sysFlag) {
        this.sysFlag = sysFlag;
        return this;
    }

    public CmisRequestBuilder channelNo(String channelNo) {
        this.channelNo = channelNo;
        return this;
    }

    public CmisRequestBuilder cooprCode(String cooprCode) {
        this.cooprCode = cooprCode;
        return this;
    }

    public CmisRequestBuilder body(Object body) {
        this.body = body;
        return this;
    }

    public ICmisRequest build() {
        switch (this.version) {
            case V1: {
                CmisRequestHead head = new CmisRequestHead();
                head.setSerno(this.serno);
                head.setTradeCode(this.tradeCode);
                head.setTradeDate(this.tradeDate);
                head.setTradeTime(this.tradeTime);
                head.setTradeType(this.tradeType);
                head.setSysFlag(this.sysFlag);
                head.setChannelNo(this.channelNo);
                head.setCooprCode(this.cooprCode);
                CmisRequestRoot root = new CmisRequestRoot();
                root.setHead(head);
                root.setBody(this.body);
                CmisRequest request = new CmisRequest();
                request.setRequest(root);
                return request;
            }
            case V2: {
                CmisRequest2 request = new CmisRequest2();
                request.put("serno", this.serno);
                request.put("tradeCode", this.tradeCode);
                request.put("tradeDate", this.tradeDate);
                request.put("tradeTime", this.tradeTime);
                request.put("tradeType", this.tradeType);
                request.put("sysFlag", this.sysFlag);
                request.put("channelNo", this.channelNo);
                request.put("cooprCode", this.cooprCode);
                request.putAll(BeanUtils.beanToMap(this.body));
                return request;
            }
            default:
                throw new InvalidOperationException("not supported cmis version");
        }
    }
}
