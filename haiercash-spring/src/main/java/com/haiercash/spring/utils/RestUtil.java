package com.haiercash.spring.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by use on 2017/7/25.
 */
public final class RestUtil {
    private RestUtil() {
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
        return fail(ConstUtil.SUCCESS_CODE, ConstUtil.SUCCESS_MSG);
    }

    public static Map<String, Object> success(Object result) {
        Map<String, Object> resultMap = fail(ConstUtil.SUCCESS_CODE, ConstUtil.SUCCESS_MSG);
        resultMap.put("body", result);
        return resultMap;
    }

    public static boolean isSuccess(Map<String, Object> resultMap) {
        return HttpUtil.isSuccess(resultMap);
    }
}
