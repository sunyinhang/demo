package com.haiercash.spring.rest.acq;

import com.bestvike.linq.exception.InvalidOperationException;
import com.haiercash.core.lang.DateUtils;
import com.haiercash.core.lang.RandomUtils;
import com.haiercash.core.lang.StringUtils;
import com.haiercash.spring.context.ThreadContext;
import com.haiercash.spring.rest.IRequest;

import java.util.Date;

/**
 * Created by 许崇雷 on 2017-11-29.
 */
public final class AcqRequestBuilder {
    private final String serno;
    private final String tradeCode;
    private final String tradeDate;
    private final String tradeTime;
    private String tradeType;
    private String sysFlag;
    private String channelNo;
    private String cooprCode;
    private Object body;

    private AcqRequestBuilder(String tradeCode) {
        Date now = DateUtils.now();
        this.serno = String.valueOf(new Date().getTime()) + RandomUtils.nextInt(100);
        this.tradeCode = tradeCode;
        this.tradeDate = DateUtils.toDateString(now);
        this.tradeTime = DateUtils.toTimeString(now);
        this.sysFlag = ThreadContext.getChannel();
        this.channelNo = ThreadContext.getChannelNo();
    }

    public static AcqRequestBuilder newBuilder(String tradeCode) {
        if (StringUtils.isEmpty(tradeCode))
            throw new InvalidOperationException("traceCode can not be empty");
        return new AcqRequestBuilder(tradeCode);
    }

    public AcqRequestBuilder tradeType(String tradeType) {
        this.tradeType = tradeType;
        return this;
    }

    public AcqRequestBuilder sysFlag(String sysFlag) {
        this.sysFlag = sysFlag;
        return this;
    }

    public AcqRequestBuilder channelNo(String channelNo) {
        this.channelNo = channelNo;
        return this;
    }

    public AcqRequestBuilder cooprCode(String cooprCode) {
        this.cooprCode = cooprCode;
        return this;
    }

    public AcqRequestBuilder body(Object body) {
        this.body = body;
        return this;
    }

    public IRequest build() {
        AcqRequestHead head = new AcqRequestHead();
        head.setSerno(this.serno);
        head.setTradeCode(this.tradeCode);
        head.setTradeDate(this.tradeDate);
        head.setTradeTime(this.tradeTime);
        head.setTradeType(this.tradeType);
        head.setSysFlag(this.sysFlag);
        head.setChannelNo(this.channelNo);
        head.setCooprCode(this.cooprCode);
        AcqRequestRoot root = new AcqRequestRoot();
        root.setHead(head);
        root.setBody(this.body);
        AcqRequest request = new AcqRequest();
        request.setRequest(root);
        return request;
    }
}
