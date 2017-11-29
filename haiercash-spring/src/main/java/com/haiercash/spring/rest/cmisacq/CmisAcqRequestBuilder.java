package com.haiercash.spring.rest.cmisacq;

import com.haiercash.core.lang.DateUtils;
import com.haiercash.spring.context.ThreadContext;

import java.util.Date;

/**
 * Created by 许崇雷 on 2017-11-29.
 */
public final class CmisAcqRequestBuilder {
    private String tradeCode;
    private String serno;
    private String sysFlag;
    private String tradeType;
    private String tradeDate;
    private String tradeTime;
    private String channelNo;
    private String cooprCode;
    private Object body;

    private CmisAcqRequestBuilder() {
        this.serno = String.valueOf(new Date().getTime()) + (int) (Math.random() * 100.0D);
        this.sysFlag = ThreadContext.getChannel();
        Date now = DateUtils.now();
        this.tradeDate = DateUtils.toDateString(now);
        this.tradeTime = DateUtils.toTimeString(now);
        this.channelNo = ThreadContext.getChannelNo();
    }

    public static CmisAcqRequestBuilder newBuilder(String tradeCode) {
        CmisAcqRequestBuilder builder = new CmisAcqRequestBuilder();
        builder.tradeCode = tradeCode;
        return builder;
    }

    public CmisAcqRequestBuilder sysFlag(String sysFlag) {
        this.sysFlag = sysFlag;
        return this;
    }

    public CmisAcqRequestBuilder tradeType(String tradeType) {
        this.tradeType = tradeType;
        return this;
    }

    public CmisAcqRequestBuilder channelNo(String channelNo) {
        this.channelNo = channelNo;
        return this;
    }

    public CmisAcqRequestBuilder cooprCode(String cooprCode) {
        this.cooprCode = cooprCode;
        return this;
    }

    public CmisAcqRequestBuilder body(Object body) {
        this.body = body;
        return this;
    }

    public CmisAcqRequest build() {
        CmisAcqRequestHead head = new CmisAcqRequestHead();
        head.setTradeCode(this.tradeCode);
        head.setSerno(this.serno);
        head.setSysFlag(this.sysFlag);
        head.setTradeType(this.tradeType);
        head.setTradeDate(this.tradeDate);
        head.setTradeTime(this.tradeTime);
        head.setChannelNo(this.channelNo);
        head.setCooprCode(this.cooprCode);
        CmisAcqRequest request = new CmisAcqRequest();
        request.setHead(head);
        request.setBody(this.body);
        return request;
    }
}
