package com.haiercash.spring.util;

import com.haiercash.spring.trace.rest.ErrorHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by use on 2017/7/25.
 */
public final class RestUtil {
    private RestUtil() {
    }

    public static Map<String, Object> fail(String retFlag, String retMsg) {
        Map<String, Object> head = new HashMap<>();
        head.put("retFlag", ErrorHandler.getRetFlag(retFlag));
        head.put("retMsg", retMsg);
        HashMap<String, Object> result = new HashMap<>();
        result.put("head", head);
        return result;
    }

    public static Map<String, Object> success() {
        return fail(ConstUtil.SUCCESS_CODE, ConstUtil.SUCCESS_MSG);
    }

    public static Map<String, Object> success(Object body) {
        Map<String, Object> result = fail(ConstUtil.SUCCESS_CODE, ConstUtil.SUCCESS_MSG);
        result.put("body", body);
        return result;
    }
}
