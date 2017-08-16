package com.haiercash.payplatform.common.utils;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;

public abstract class CmisUtil {
    public static Log logger = LogFactory.getLog(CmisUtil.class);
    private static String SUCCESS_CODE = "00000";
    private static String SUCCESS_CODE2 = "0000";
    private static String SUCCESS_MSG = "处理成功";
    public static String ERROR_INTERNAL_CODE = "99";
    public static String ERROR_INTERNAL_MSG = "网络通讯异常";

    @Value("${app.rest.HCPORTAL}")
    public static String hcportal;
    public CmisUtil() {
    }

    public static Map<String, Object> success() {
        HashMap resultMap = new HashMap();
        resultMap.put("head", new CmisHead(SUCCESS_CODE, SUCCESS_MSG));
        return resultMap;
    }

    public static Map<String, Object> success(Object result) {
        HashMap responseMap = new HashMap();
        HashMap resultMap = new HashMap();
        resultMap.put("head", new CmisHead(SUCCESS_CODE, SUCCESS_MSG));
        resultMap.put("body", result);
        responseMap.put("response", resultMap);
        return responseMap;
    }

    public static Map<String, Object> fail(String retFlag, String retMsg) {
        HashMap resultMap = new HashMap();
        resultMap.put("head", new CmisHead(retFlag, retMsg));
        return resultMap;
    }

    /** @deprecated */
    @Deprecated
    public static boolean isSuccess(Map<String, Object> resultMap) {
        return resultMap != null && resultMap.get("head") != null && ((Map)resultMap.get("head")).get("retFlag") != null?((Map)resultMap.get("head")).get("retFlag").equals(SUCCESS_CODE) || ((Map)resultMap.get("head")).get("retFlag").equals(SUCCESS_CODE2):false;
    }

    public static HashMap<String, Object> makeHeadMap(String tradeCode, String tradeType, Map<String, Object> params) {
        HashMap headMap = new HashMap();
        headMap.put("tradeCode", tradeCode);
        headMap.put("serno", (new Date()).getTime() + "" + (int)(Math.random() * 100.0D));
        if(StringUtils.isEmpty(params.get("sysFlag"))) {
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
        if(StringUtils.isEmpty(params.get("channelNo"))) {
            headMap.put("channelNo", "05");
        } else {
            headMap.put("channelNo", params.get("channelNo"));
        }

        headMap.put("cooprCode", "");
        return headMap;
    }

    public static HashMap<String, Object> makeBodyMap(HashMap<String, Object> map) {
        Calendar tradeDate = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        map.put("signTime", sdf.format(tradeDate.getTime()));
        map.put("registTime", sdf.format(tradeDate.getTime()));
        return map;
    }

    private static HashMap<String, Object> makeParamMap(String tradeCode, String tradeType, HashMap<String, Object> map) {
        HashMap requestMap = new HashMap();
        requestMap.put("head", makeHeadMap(tradeCode, tradeType, map));
        requestMap.put("body", makeBodyMap(map));
        HashMap paramMap = new HashMap();
        paramMap.put("request", requestMap);
        return paramMap;
    }

    public static HashMap<String, Object> makeParamMap(String tradeCode, HashMap<String, Object> map) {
        return makeParamMap(tradeCode, "", map);
    }

    public static Map<String, Object> getCmisResponse(String tradeCode, String token, String tradeType, HashMap<String, Object> map) {
        HashMap requestMap = makeParamMap(tradeCode, tradeType, map);
        logger.debug("Cmis request:");
        logger.debug(new JSONObject(requestMap));
        Map responseMap = HttpUtil.restPostMap(hcportal + "/pub/cmisfront", requestMap);
        logger.debug("Cmis response:");
        logger.debug(new JSONObject(responseMap));
        return responseMap;
    }

    public static Map<String, Object> getCmisResponse(String tradeCode, String token, HashMap<String, Object> map) {
        return getCmisResponse(tradeCode, token, "", map);
    }

    public static Map<String, Object> getHxxdResponse(String url, String token, Map<String, Object> map) {
        return HttpUtil.restPostMapOrigin(url, token, makeHxxdAndHsSysMap(map));
    }

    private static Map<String, Object> makeHxxdAndHsSysMap(Map<String, Object> map) {
        HashMap sendMap = new HashMap();
        sendMap.put("msgbody", map);
        return sendMap;
    }

    public static boolean getIsSucceed(Map<String, Object> response) {
        try {
            Map e = (Map)response.get("response");
            Map mapHead = (Map)e.get("head");
            return mapHead.get("retFlag").equals(SUCCESS_CODE) || mapHead.get("retFlag").equals(SUCCESS_CODE2);
        } catch (Exception var3) {
            return false;
        }
    }

    public static String getErrMsg(Map<String, Object> response) {
        try {
            Map e = (Map)response.get("response");
            Map mapHead = (Map)e.get("head");
            return (String)mapHead.get("retMsg");
        } catch (Exception var3) {
            return "";
        }
    }

    public static Map<String, Object> getBody(Map<String, Object> response) {
        try {
            Map e = (Map)response.get("response");
            return (Map)e.get("body");
        } catch (Exception var2) {
            return new HashMap();
        }
    }

    public static Map<String, Object> getDataMap(Map<String, Object> response, String key) {
        try {
            Map e = getBody(response);
            return (Map)e.get(key);
        } catch (Exception var3) {
            return new HashMap();
        }
    }

    public static Object getData(Map<String, Object> response, String key) {
        try {
            Map e = getBody(response);
            return e.get(key);
        } catch (Exception var3) {
            return "";
        }
    }
}
