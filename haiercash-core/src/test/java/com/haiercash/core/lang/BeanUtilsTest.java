package com.haiercash.core.lang;

import com.alibaba.fastjson.TypeReference;
import com.haiercash.core.reflect.GenericType;
import com.haiercash.core.serialization.JsonSerializer;
import com.haiercash.spring.rest.IResponse;
import com.haiercash.spring.rest.common.CommonResponse;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

/**
 * Created by 许崇雷 on 2018-02-07.
 */
public class BeanUtilsTest {
    private static final String JSON = "{\"head\":{\"retFlag\":\"00000\",\"retMsg\":\"处理成功\"},\"body\":{\"name\":\"hello 中国\"}}";

    @Test
    public void beanToMap() {
        IResponse<Map> response = JsonSerializer.deserialize(JSON, new TypeReference<CommonResponse<Map>>() {
        });
        Map<String, Object> map = BeanUtils.beanToMap(response);
        //noinspection unchecked
        Map<String, Object> head = (Map<String, Object>) map.get("head");
        Assert.assertEquals("00000", head.get("retFlag"));
        Assert.assertEquals("处理成功", head.get("retMsg"));
        Assert.assertTrue(Map.class.isAssignableFrom(map.get("body").getClass()));
    }

    @Test
    public void mapToBean() {
        Map<String, Object> map = JsonSerializer.deserializeMap(JSON);
        IResponse<?> response = BeanUtils.mapToBean(map, CommonResponse.class);
        Assert.assertEquals("00000", response.getRetFlag());
        Assert.assertEquals("处理成功", response.getRetMsg());
        Assert.assertTrue(Map.class.isAssignableFrom(response.getBody().getClass()));
    }

    @Test
    @SuppressWarnings("Duplicates")
    public void mapToBeanTypeRef() {
        Map<String, Object> map = JsonSerializer.deserializeMap(JSON);
        IResponse<Map> response = BeanUtils.mapToBean(map, new TypeReference<CommonResponse<Map>>() {
        });
        Assert.assertEquals("00000", response.getRetFlag());
        Assert.assertEquals("处理成功", response.getRetMsg());
        Assert.assertEquals("hello 中国", response.getBody().get("name"));
    }

    @Test
    @SuppressWarnings("Duplicates")
    public void mapToBeanType() {
        Map<String, Object> map = JsonSerializer.deserializeMap(JSON);
        IResponse<Map> response = BeanUtils.mapToBean(map, new GenericType<CommonResponse<Map>>() {
        });
        Assert.assertEquals("00000", response.getRetFlag());
        Assert.assertEquals("处理成功", response.getRetMsg());
        Assert.assertEquals("hello 中国", response.getBody().get("name"));
    }
}
