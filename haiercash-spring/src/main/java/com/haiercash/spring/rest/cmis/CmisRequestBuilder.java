package com.haiercash.spring.rest.cmis;

import com.bestvike.linq.exception.InvalidOperationException;
import com.haiercash.core.lang.BeanUtils;
import com.haiercash.core.lang.Convert;
import com.haiercash.core.lang.DateUtils;
import com.haiercash.core.lang.RandomUtils;
import com.haiercash.core.lang.StringUtils;
import com.haiercash.spring.context.ThreadContext;
import com.haiercash.spring.rest.IRequest;
import com.haiercash.spring.rest.cmis.v1.CmisRequest;
import com.haiercash.spring.rest.cmis.v1.CmisRequestHead;
import com.haiercash.spring.rest.cmis.v1.CmisRequestRoot;
import com.haiercash.spring.rest.cmis.v2.CmisRequest2;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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

    public static IRequest build(Map<String, Object> map) {
        Assert.notNull(map, "map can not be null");

        //单层 Map
        String tradeCode = Convert.toString(map.get("tradeCode"));
        if (StringUtils.isNotEmpty(tradeCode)) {
            CmisVersion version = CmisVersion.forTradeCode(tradeCode);
            switch (version) {
                case V1: {
                    Map<String, Object> bodyMap = new HashMap<>(map);//复制
                    CmisRequestHead head = new CmisRequestHead();
                    head.setSerno(Convert.toString(bodyMap.remove("serno")));
                    head.setTradeCode(Convert.toString(bodyMap.remove("tradeCode")));
                    head.setTradeDate(Convert.toString(bodyMap.remove("tradeDate")));
                    head.setTradeTime(Convert.toString(bodyMap.remove("tradeTime")));
                    head.setTradeType(Convert.toString(bodyMap.remove("tradeType")));
                    head.setSysFlag(Convert.toString(bodyMap.remove("sysFlag")));
                    head.setChannelNo(Convert.toString(bodyMap.remove("channelNo")));
                    head.setCooprCode(Convert.toString(bodyMap.remove("cooprCode")));
                    CmisRequestRoot root = new CmisRequestRoot();
                    root.setHead(head);
                    root.setBody(bodyMap);
                    CmisRequest request = new CmisRequest();
                    request.setRequest(root);
                    return request;
                }
                case V2: {
                    CmisRequest2 request = new CmisRequest2();
                    request.putAll(map);
                    return request;
                }
                default:
                    throw new InvalidOperationException("not supported cmis version");
            }
        }

        //如果为三层 Map > request > head body 去掉外层
        Map<String, Object> requestMap = map.get("request") instanceof Map ? ((Map) map.get("request")) : map;//如果为三层
        if (requestMap.get("head") instanceof Map) {
            Map<String, Object> headMap = (Map<String, Object>) requestMap.get("head");
            Map<String, Object> bodyMap = (Map<String, Object>) requestMap.get("body");
            tradeCode = Convert.toString(headMap.get("tradeCode"));
            CmisVersion version = CmisVersion.forTradeCode(tradeCode);
            switch (version) {
                case V1: {
                    CmisRequestHead head = new CmisRequestHead();
                    head.setSerno(Convert.toString(headMap.remove("serno")));
                    head.setTradeCode(Convert.toString(headMap.get("tradeCode")));
                    head.setTradeDate(Convert.toString(headMap.get("tradeDate")));
                    head.setTradeTime(Convert.toString(headMap.get("tradeTime")));
                    head.setTradeType(Convert.toString(headMap.get("tradeType")));
                    head.setSysFlag(Convert.toString(headMap.get("sysFlag")));
                    head.setChannelNo(Convert.toString(headMap.get("channelNo")));
                    head.setCooprCode(Convert.toString(headMap.get("cooprCode")));
                    CmisRequestRoot root = new CmisRequestRoot();
                    root.setHead(head);
                    root.setBody(bodyMap);
                    CmisRequest request = new CmisRequest();
                    request.setRequest(root);
                    return request;
                }
                case V2: {
                    CmisRequest2 request = new CmisRequest2();
                    request.putAll(headMap);
                    request.putAll(bodyMap);
                    return request;
                }
                default:
                    throw new InvalidOperationException("not supported cmis version");
            }
        }

        throw new InvalidOperationException("错误的格式");
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
