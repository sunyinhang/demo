package com.haiercash.spring.util;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by 许崇雷 on 2017-12-05.
 */
public class RestUtilTest {
    @Test
    public void success() {
        Map<String, Object> body = new HashMap<>();
        body.put("userId", "222");
        Map map = RestUtil.success(body);
        JSONObject jsonObject = new JSONObject((Map) map.get("head"));//此处必须拆箱为 Map
        String retFlag = (String) jsonObject.get("retFlag");
        Assert.assertEquals("00000", retFlag);
    }
}
