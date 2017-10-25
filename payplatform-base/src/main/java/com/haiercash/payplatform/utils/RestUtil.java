package com.haiercash.payplatform.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by use on 2017/7/25.
 */
public final class RestUtil {
    public static Log logger = LogFactory.getLog(RestUtil.class);
    public static String ERROR_INTERNAL_CODE = ConstUtil.ERROR_CODE;
    public static String ERROR_INTERNAL_MSG = ConstUtil.ERROR_INFO;
    private static String SUCCESS_CODE = ConstUtil.SUCCESS_CODE;
    private static String SUCCESS_MSG = ConstUtil.SUCCESS_MSG;

    public RestUtil() {
    }

    public static Map<String, Object> fail(String retFlag, String retMsg) {
        HashMap<String, Object> resultMap = new HashMap<>();
        ResultHead head = new ResultHead();
        head.setRetFlag(retFlag);
        head.setRetMsg(retMsg);
        resultMap.put("head", head);
        return resultMap;
    }

    public static Map<String, Object> success() {
        return fail(SUCCESS_CODE, SUCCESS_MSG);
    }

    public static Map<String, Object> success(Object result) {
        Map<String, Object> resultMap = fail(SUCCESS_CODE, SUCCESS_MSG);
        resultMap.put("body", result);
        return resultMap;
    }

    public static boolean isSuccess(Map<String, Object> resultMap) {
        return HttpUtil.isSuccess(resultMap);
    }
}
