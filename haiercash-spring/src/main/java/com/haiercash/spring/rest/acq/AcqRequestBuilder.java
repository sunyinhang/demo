package com.haiercash.spring.rest.acq;

import com.bestvike.linq.exception.InvalidOperationException;
import com.haiercash.core.lang.Convert;
import com.haiercash.core.lang.DateUtils;
import com.haiercash.core.lang.RandomUtils;
import com.haiercash.core.lang.StringUtils;
import com.haiercash.core.serialization.JsonSerializer;
import com.haiercash.spring.context.ThreadContext;
import org.springframework.util.Assert;

import java.util.Date;
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
            AcqRequestRoot root = new AcqRequestRoot();
            root.setHead(head);
            root.setBody(bodyMap);
            AcqRequest request = new AcqRequest();
            request.setRequest(root);
            return request;
        }

        //如果为三层 Map > request > head body 去掉外层
        Map<String, Object> requestMap = map.get("request") instanceof Map ? ((Map) map.get("request")) : map;//如果为三层
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
        AcqRequestRoot root = new AcqRequestRoot();
        root.setHead(head);
        root.setBody(this.body);
        AcqRequest request = new AcqRequest();
        request.setRequest(root);
        return request;
    }
}
