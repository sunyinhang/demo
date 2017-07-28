package com.haiercash.payplatform.common.utils;

import com.haiercash.commons.util.DateUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by use on 2017/7/27.工具类
 * 请求收单
 */
public class AcqUtil {
    private static Log logger = LogFactory.getLog(AcqUtil.class);

    public AcqUtil() {
    }

    public static Map<String, Object> getAcqHead(String tradeCode, String sysFlag, String channelNo, String cooprCode, String tradeType) {
        HashMap headMap = new HashMap();
        Date now = new Date();
        headMap.put("serno", now.getTime() + "" + (int)(Math.random() * 100.0D));
        headMap.put("tradeDate", DateUtil.formatDate(now, "yyyy-MM-dd"));
        headMap.put("tradeTime", DateUtil.formatDate(now, "HH:mm:ss"));
        headMap.put("tradeCode", tradeCode);
        headMap.put("sysFlag", sysFlag);
        headMap.put("channelNo", channelNo);
        headMap.put("cooprCode", StringUtils.isEmpty(cooprCode)?"":cooprCode);
        headMap.put("tradeType", StringUtils.isEmpty(tradeType)?"":tradeType);
        return headMap;
    }

    public static Map<String, Object> getAcqResponse(String url, Map<String, Object> headMap, Map<String, Object> paramMap) {
        HashMap completeMap = new HashMap();
        completeMap.put("head", headMap);
        completeMap.put("body", paramMap);
        HashMap requestMap = new HashMap();
        requestMap.put("request", completeMap);
        String requestJson = JSONObject.valueToString(requestMap);
        logger.info("==>ACQ  url:" + url + ", 请求参数:" + requestJson);
        String returnJson = HttpUtil.restPost(url, "", requestJson, 200);
        logger.info("<==ACQ  返回参数:" + returnJson);
        return HttpUtil.json2DeepMap(returnJson);
    }

    public static Map<String, Object> getAcqResponse(String url, String tradeCode, String sysFlag, String channelNo, String cooprCode, String tradeType, Map<String, Object> paramMap) {
        Map headMap = getAcqHead(tradeCode, sysFlag, channelNo, cooprCode, tradeType);
        return getAcqResponse(url, headMap, paramMap);
    }
}
