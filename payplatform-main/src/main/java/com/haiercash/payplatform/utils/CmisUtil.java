package com.haiercash.payplatform.utils;

import com.haiercash.core.lang.RandomUtils;
import com.haiercash.core.time.DateUtils;
import com.haiercash.spring.eureka.EurekaServer;
import com.haiercash.spring.util.ConstUtil;
import com.haiercash.spring.util.HttpUtil;

import java.util.HashMap;
import java.util.Map;

public final class CmisUtil {
    private CmisUtil() {
    }

    public static HashMap<String, Object> makeHeadMap(String tradeCode, String tradeType, Map<String, Object> params) {
        HashMap<String, Object> headMap = new HashMap<>();
        headMap.put("tradeCode", tradeCode);
        headMap.put("serno", String.valueOf(System.currentTimeMillis()) + RandomUtils.nextInt(100));
        headMap.put("sysFlag", params.get("sysFlag"));
        headMap.put("tradeType", tradeType);
        headMap.put("tradeDate", DateUtils.nowDateString());
        headMap.put("tradeTime", DateUtils.nowTimeString());
        headMap.put("channelNo", params.get("channelNo"));
        headMap.put("cooprCode", "");
        return headMap;
    }

    public static Map<String, Object> makeBodyMap(Map<String, Object> map) {
        String date = DateUtils.nowDateString();
        map.put("signTime", date);
        map.put("registTime", date);
        return map;
    }

    private static HashMap<String, Object> makeParamMap(String tradeCode, String tradeType, Map<String, Object> map) {
        HashMap requestMap = new HashMap();
        requestMap.put("head", makeHeadMap(tradeCode, tradeType, map));
        requestMap.put("body", makeBodyMap(map));
        HashMap paramMap = new HashMap();
        paramMap.put("request", requestMap);
        return paramMap;
    }

    public static Map<String, Object> getCmisResponse(String tradeCode, String token, String tradeType, Map<String, Object> map) {
        HashMap requestMap = makeParamMap(tradeCode, tradeType, map);
        return (Map<String, Object>) HttpUtil.restPostMap(EurekaServer.CMISFRONTSERVER + "/pub/cmisfront", "", requestMap);
    }

    public static Map<String, Object> getCmisResponse(String tradeCode, String token, Map<String, Object> map) {
        return getCmisResponse(tradeCode, token, "", map);
    }

    public static boolean isSuccess(Map<String, Object> response) {
        try {
            Map e = (Map) response.get("response");
            Map mapHead = (Map) e.get("head");
            return mapHead.get("retFlag").equals(ConstUtil.SUCCESS_CODE) || mapHead.get("retFlag").equals(ConstUtil.SUCCESS_CODE2);
        } catch (Exception var3) {
            return false;
        }
    }
}
