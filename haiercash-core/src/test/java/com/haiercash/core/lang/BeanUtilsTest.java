package com.haiercash.core.lang;

import com.alibaba.fastjson.TypeReference;
import com.haiercash.core.reflect.GenericType;
import com.haiercash.core.serialization.JsonSerializer;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

/**
 * Created by 许崇雷 on 2018-02-07.
 */
public class BeanUtilsTest {
    private static final String JSON = "{\"head\":{\"retFlag\":\"00000\",\"retMsg\":\"处理成功\"},\"body\":{\"name\":\"hello 中国\"}}";

    @Test
    @SuppressWarnings("unchecked")
    public void beanToMap() {
        DemoBean<Map> response = JsonSerializer.deserialize(JSON, new TypeReference<DemoBean<Map>>() {
        });
        Map<String, Object> map = BeanUtils.beanToMap(response);
        Assert.assertEquals(2, map.size());
        Map<String, Object> head = (Map<String, Object>) map.get("head");
        Assert.assertEquals("00000", head.get("retFlag"));
        Assert.assertEquals("处理成功", head.get("retMsg"));
        Map<String, Object> body = (Map<String, Object>) map.get("body");
        Assert.assertEquals(1, body.size());
        Assert.assertEquals("hello 中国", body.get("name"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void mapToBean() {
        Map<String, Object> map = JsonSerializer.deserializeMap(JSON);
        Assert.assertEquals(2, map.size());
        DemoBean<?> response = BeanUtils.mapToBean(map, DemoBean.class);
        Assert.assertEquals("00000", response.getRetFlag());
        Assert.assertEquals("处理成功", response.getRetMsg());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        Assert.assertEquals(1, body.size());
        Assert.assertEquals("hello 中国", body.get("name"));
    }

    @Test
    @SuppressWarnings({"Duplicates", "unchecked"})
    public void mapToBeanTypeRef() {
        Map<String, Object> map = JsonSerializer.deserializeMap(JSON);
        Assert.assertEquals(2, map.size());
        DemoBean<Map> response = BeanUtils.mapToBean(map, new TypeReference<DemoBean<Map>>() {
        });
        Assert.assertEquals("00000", response.getRetFlag());
        Assert.assertEquals("处理成功", response.getRetMsg());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        Assert.assertEquals(1, body.size());
        Assert.assertEquals("hello 中国", body.get("name"));
    }

    @Test
    @SuppressWarnings({"Duplicates", "unchecked"})
    public void mapToBeanType() {
        Map<String, Object> map = JsonSerializer.deserializeMap(JSON);
        Assert.assertEquals(2, map.size());
        DemoBean<Map> response = BeanUtils.mapToBean(map, new GenericType<DemoBean<Map>>() {
        });
        Assert.assertEquals("00000", response.getRetFlag());
        Assert.assertEquals("处理成功", response.getRetMsg());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        Assert.assertEquals(1, body.size());
        Assert.assertEquals("hello 中国", body.get("name"));
    }
}
