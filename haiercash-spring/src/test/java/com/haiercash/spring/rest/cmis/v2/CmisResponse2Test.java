package com.haiercash.spring.rest.cmis.v2;

import com.haiercash.core.serialization.JsonSerializer;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by 许崇雷 on 2018-01-11.
 */
public class CmisResponse2Test {

    @Test
    public void test() {
        CmisResponse2 response2 = new CmisResponse2();
        response2.put("retFlg", "00000");
        String json = JsonSerializer.serialize(response2);
        Assert.assertEquals("{}", json);
    }
}