package com.haiercash.payplatform.utils;

import com.haiercash.payplatform.config.EurekaServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public final class CmisUtil {
    @Value("${app.rest.HCPORTAL}")
    private static String hcportal;

    private CmisUtil() {
    }

    public static HashMap<String, Object> makeHeadMap(String tradeCode, String tradeType, Map<String, Object> params) {
        HashMap headMap = new HashMap();
        headMap.put("tradeCode", tradeCode);
        headMap.put("serno", (new Date()).getTime() + "" + (int) (Math.random() * 100.0D));
        if (StringUtils.isEmpty(params.get("sysFlag"))) {
            headMap.put("sysFlag", "04");
        } else {
            headMap.put("sysFlag", params.get("sysFlag"));
        }

        headMap.put("tradeType", tradeType);
        Calendar tradeDate = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        headMap.put("tradeDate", sdf.format(tradeDate.getTime()));
        sdf.applyPattern("HH:mm:ss");
        headMap.put("tradeTime", sdf.format(tradeDate.getTime()));
        if (StringUtils.isEmpty(params.get("channelNo"))) {
            headMap.put("channelNo", "05");
        } else {
            headMap.put("channelNo", params.get("channelNo"));
        }

        headMap.put("cooprCode", "");
        return headMap;
    }

    public static Map<String, Object> makeBodyMap(Map<String, Object> map) {
        Calendar tradeDate = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        map.put("signTime", sdf.format(tradeDate.getTime()));
        map.put("registTime", sdf.format(tradeDate.getTime()));
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
        Map<String, Object> responseMap = HttpUtil.restPostMap(EurekaServer.CMISFRONTSERVER + "/pub/cmisfront", "", requestMap);
        return responseMap;
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
