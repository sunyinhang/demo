package com.haiercash.spring.rest.cmis.v2;

import com.haiercash.core.reflect.GenericType;
import com.haiercash.core.serialization.JsonSerializer;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

/**
 * Created by 许崇雷 on 2018-01-11.
 */
public class CmisResponse2Test {
    private static final String TEST_JSON = "{\"retFlag\":\"00000\"}";

    @Test
    public void test() {
        CmisResponse2<Object> res = new CmisResponse2<>();
        res.put("retFlag", "00000");
        String json = JsonSerializer.serialize(res);
        Assert.assertEquals(TEST_JSON, json);
        CmisResponse2<Object> res2 = JsonSerializer.deserialize(TEST_JSON, new GenericType<CmisResponse2<Map<String, Object>>>() {
        });
        Assert.assertEquals("00000", res2.getRetFlag());
    }
}
