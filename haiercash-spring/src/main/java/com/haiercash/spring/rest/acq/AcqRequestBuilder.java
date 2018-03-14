package com.haiercash.spring.rest.acq;

import com.bestvike.linq.exception.InvalidOperationException;
import com.haiercash.core.lang.Convert;
import com.haiercash.core.lang.RandomUtils;
import com.haiercash.core.lang.StringUtils;
import com.haiercash.core.serialization.JsonSerializer;
import com.haiercash.core.time.DateUtils;
import com.haiercash.spring.context.ThreadContext;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

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
    private String autoFlag;
    private String applCde;
    private Long applSeq;
    private Object body;

    private AcqRequestBuilder(String tradeCode) {
        this.serno = String.valueOf(System.currentTimeMillis()) + RandomUtils.nextInt(100);
        this.tradeCode = tradeCode;
        this.tradeDate = DateUtils.nowDateString();
        this.tradeTime = DateUtils.nowTimeString();
        this.sysFlag = ThreadContext.getChannel();
        this.channelNo = ThreadContext.getChannelNo();
    }

    public static AcqRequestBuilder newBuilder(String tradeCode) {
        if (StringUtils.isEmpty(tradeCode))
            throw new InvalidOperationException("traceCode can not be empty");
        return new AcqRequestBuilder(tradeCode);
    }

    public static IAcqRequest build(Map<String, Object> map) {
        Assert.notNull(map, "map can not be null");

        //单层 Map
        String tradeCode = Convert.toString(map.get("tradeCode"));
        if (StringUtils.isNotEmpty(tradeCode)) {
            Map<String, Object> bodyMap = new HashMap<>(map);//复制
            AcqRequestHead head = new AcqRequestHead();
            head.setSerno(Convert.toString(bodyMap.remove("serno")));
            head.setTradeCode(Convert.toString(bodyMap.remove("tradeCode")));
            head.setTradeDate(Convert.toString(bodyMap.remove("tradeDate")));
            head.setTradeTime(Convert.toString(bodyMap.remove("tradeTime")));
            head.setTradeType(Convert.toString(bodyMap.remove("tradeType")));
            head.setSysFlag(Convert.toString(bodyMap.remove("sysFlag")));
            head.setChannelNo(Convert.toString(bodyMap.remove("channelNo")));
            head.setCooprCode(Convert.toString(bodyMap.remove("cooprCode")));
            head.setAutoFlag(Convert.toString(bodyMap.remove("autoFlag")));
            head.setApplCde(Convert.toString(bodyMap.remove("applCde")));
            head.setApplSeq(Convert.nullLong(bodyMap.remove("applSeq")));
            AcqRequestRoot root = new AcqRequestRoot();
            root.setHead(head);
            root.setBody(bodyMap);
            AcqRequest request = new AcqRequest();
            request.setRequest(root);
            return request;
        }

        //如果为三层 Map > request > head body 去掉外层
        Map<String, Object> requestMap = map.get("request") instanceof Map ? (Map) map.get("request") : map;//如果为三层
        if (requestMap.get("head") instanceof Map) {
            Map<String, Object> headMap = (Map<String, Object>) requestMap.get("head");
            Map<String, Object> bodyMap = (Map<String, Object>) requestMap.get("body");
            AcqRequestHead head = new AcqRequestHead();
            head.setSerno(Convert.toString(headMap.get("serno")));
            head.setTradeCode(Convert.toString(headMap.get("tradeCode")));
            head.setTradeDate(Convert.toString(headMap.get("tradeDate")));
            head.setTradeTime(Convert.toString(headMap.get("tradeTime")));
            head.setTradeType(Convert.toString(headMap.get("tradeType")));
            head.setSysFlag(Convert.toString(headMap.get("sysFlag")));
            head.setChannelNo(Convert.toString(headMap.get("channelNo")));
            head.setCooprCode(Convert.toString(headMap.get("cooprCode")));
            head.setAutoFlag(Convert.toString(headMap.get("autoFlag")));
            head.setApplCde(Convert.toString(headMap.get("applCde")));
            head.setApplSeq(Convert.nullLong(headMap.get("applSeq")));
            AcqRequestRoot root = new AcqRequestRoot();
            root.setHead(head);
            root.setBody(bodyMap);
            AcqRequest request = new AcqRequest();
            request.setRequest(root);
            return request;
        }

        throw new InvalidOperationException("错误的格式");
    }

    public static IAcqRequest build(String json) {
        return build(JsonSerializer.deserializeMap(json));
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

    public AcqRequestBuilder autoFlag(String autoFlag) {
        this.autoFlag = autoFlag;
        return this;
    }

    public AcqRequestBuilder applCde(String applCde) {
        this.applCde = applCde;
        return this;
    }

    public AcqRequestBuilder applSeq(Long applSeq) {
        this.applSeq = applSeq;
        return this;
    }

    public AcqRequestBuilder body(Object body) {
        this.body = body;
        return this;
    }

    public IAcqRequest build() {
        AcqRequestHead head = new AcqRequestHead();
        head.setSerno(this.serno);
        head.setTradeCode(this.tradeCode);
        head.setTradeDate(this.tradeDate);
        head.setTradeTime(this.tradeTime);
        head.setTradeType(this.tradeType);
        head.setSysFlag(this.sysFlag);
        head.setChannelNo(this.channelNo);
        head.setCooprCode(this.cooprCode);
        head.setAutoFlag(this.autoFlag);
        head.setApplCde(this.applCde);
        head.setApplSeq(this.applSeq);
        AcqRequestRoot root = new AcqRequestRoot();
        root.setHead(head);
        root.setBody(this.body);
        AcqRequest request = new AcqRequest();
        request.setRequest(root);
        return request;
    }
}
