package com.haiercash.payplatform.utils;

import com.haiercash.core.lang.Convert;
import com.haiercash.core.lang.RandomUtils;
import com.haiercash.core.time.DateUtils;
import com.haiercash.spring.util.ConstUtil;
import com.haiercash.spring.util.HttpUtil;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by use on 2017/7/27.工具类
 * 请求收单
 */
public class AcqUtil {
    private AcqUtil() {
    }

    public static Map<String, Object> getAcqHead(String tradeCode, String sysFlag, String channelNo, String cooprCode, String tradeType) {
        HashMap headMap = new HashMap();
        headMap.put("serno", String.valueOf(System.currentTimeMillis()) + RandomUtils.nextInt(100));
        headMap.put("tradeDate", DateUtils.nowDateString());
        headMap.put("tradeTime", DateUtils.nowTimeString());
        headMap.put("tradeCode", tradeCode);
        headMap.put("sysFlag", sysFlag);
        headMap.put("channelNo", channelNo);
        headMap.put("cooprCode", Convert.toString(cooprCode));
        headMap.put("tradeType", Convert.toString(tradeType));
        return headMap;
    }

    public static Map<String, Object> getAcqResponse(String url, Map<String, Object> headMap, Map<String, Object> paramMap) {
        HashMap completeMap = new HashMap();
        completeMap.put("head", headMap);
        completeMap.put("body", paramMap);
        HashMap requestMap = new HashMap();
        requestMap.put("request", completeMap);
        String requestJson = JSONObject.valueToString(requestMap);
        String returnJson = HttpUtil.restPost(url, "", requestJson, 200);
        return HttpUtil.json2DeepMap(returnJson);
    }

    public static Map<String, Object> getAcqResponse(String url, String tradeCode, String sysFlag, String channelNo, String cooprCode, String tradeType, Map<String, Object> paramMap) {
        Map headMap = getAcqHead(tradeCode, sysFlag, channelNo, cooprCode, tradeType);
        return getAcqResponse(url, headMap, paramMap);
    }

    public static boolean isSuccess(Map<String, Object> response) {
        try {
            Map<String, Object> mapRes = (Map) response.get("response");
            Map<String, Object> mapHead = (Map) mapRes.get("head");
            return mapHead.get("retFlag").equals(ConstUtil.SUCCESS_CODE) || mapHead.get("retFlag").equals(ConstUtil.SUCCESS_CODE2);
        } catch (Exception var3) {
            return false;
        }
    }
}
