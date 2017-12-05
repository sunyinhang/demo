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
        Map<String, Object> head = new HashMap<>();
        head.put("retFlag", retFlag);
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

    public static boolean isSuccess(Map<String, Object> result) {
        return HttpUtil.isSuccess(result);
    }
}
