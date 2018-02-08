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
        Assert.assertEquals(2, map.size());
        //noinspection unchecked
        Map<String, Object> head = (Map<String, Object>) map.get("head");
        Assert.assertEquals("00000", head.get("retFlag"));
        Assert.assertEquals("处理成功", head.get("retMsg"));
        //noinspection unchecked
        Map<String, Object> body = (Map<String, Object>) map.get("body");
        Assert.assertEquals(1, body.size());
        Assert.assertEquals("hello 中国", body.get("name"));
    }

    @Test
    public void mapToBean() {
        Map<String, Object> map = JsonSerializer.deserializeMap(JSON);
        Assert.assertEquals(2, map.size());
        IResponse<?> response = BeanUtils.mapToBean(map, CommonResponse.class);
        Assert.assertEquals("00000", response.getRetFlag());
        Assert.assertEquals("处理成功", response.getRetMsg());
        //noinspection unchecked
        Map<String, Object> body = ((Map<String, Object>) response.getBody());
        Assert.assertEquals(1, body.size());
        Assert.assertEquals("hello 中国", body.get("name"));
    }

    @Test
    @SuppressWarnings("Duplicates")
    public void mapToBeanTypeRef() {
        Map<String, Object> map = JsonSerializer.deserializeMap(JSON);
        Assert.assertEquals(2, map.size());
        IResponse<Map> response = BeanUtils.mapToBean(map, new TypeReference<CommonResponse<Map>>() {
        });
        Assert.assertEquals("00000", response.getRetFlag());
        Assert.assertEquals("处理成功", response.getRetMsg());
        //noinspection unchecked
        Map<String, Object> body = ((Map<String, Object>) response.getBody());
        Assert.assertEquals(1, body.size());
        Assert.assertEquals("hello 中国", body.get("name"));
    }

    @Test
    @SuppressWarnings("Duplicates")
    public void mapToBeanType() {
        Map<String, Object> map = JsonSerializer.deserializeMap(JSON);
        Assert.assertEquals(2, map.size());
        IResponse<Map> response = BeanUtils.mapToBean(map, new GenericType<CommonResponse<Map>>() {
        });
        Assert.assertEquals("00000", response.getRetFlag());
        Assert.assertEquals("处理成功", response.getRetMsg());
        //noinspection unchecked
        Map<String, Object> body = ((Map<String, Object>) response.getBody());
        Assert.assertEquals(1, body.size());
        Assert.assertEquals("hello 中国", body.get("name"));
    }
}
