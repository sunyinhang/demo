package com.haiercash.core.serialization;

import com.alibaba.fastjson.JSONArray;
import com.bestvike.linq.Linq;
import com.haiercash.core.lang.Convert;
import lombok.Data;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;

;

/**
 * Created by 许崇雷 on 2017-09-22.
 */
public class JsonSerializerTest {
    private static final Person PERSON = new Person(123, "测试", Convert.toDate("2017-01-02 03:04:05"));
    private static final String JSON = "{\"uid\":123,\"name\":\"测试\",\"birthday\":\"2017-01-02 03:04:05\"}";

    private static final Person[] PERSON_ARRAY = new Person[]{PERSON};
    private static final String JSON_ARRAY = "[{\"uid\":123,\"name\":\"测试\",\"birthday\":\"2017-01-02 03:04:05\"}]";

    @Test
    public void testSerialize() {
        for (int i = 0; i < 20; i++) {
            String json = JsonSerializer.serialize(PERSON);
            Person person = JsonSerializer.deserialize(json, Person.class);
            Assert.assertEquals(JSON, json);
            Assert.assertEquals(PERSON, person);
        }
    }

    @Test
    public void testSerializeArray() {
        for (int i = 0; i < 20; i++) {
            String jsonArray = JsonSerializer.serialize(PERSON_ARRAY);
            List<Person> personList = JsonSerializer.deserializeArray(jsonArray, Person.class);
            Person[] personArray = Linq.asEnumerable(personList).toArray(Person.class);
            Assert.assertEquals(JSON_ARRAY, jsonArray);
            Assert.assertArrayEquals(PERSON_ARRAY, personArray);
        }
    }

    @Test
    public void testNull() {
        String nullJson = JsonSerializer.serialize(null);
        Object object = JsonSerializer.deserialize(nullJson, Object.class);
        Assert.assertNull(object);
        Object object2 = JsonSerializer.deserialize(null, Object.class);
        Assert.assertNull(object2);
    }

    @Test
    public void testNum() {
        Long num = 100L;
        String numJson = JsonSerializer.serialize(num);
        Long num2 = JsonSerializer.deserialize(numJson, Long.class);
        Assert.assertEquals("100", numJson);
        Assert.assertEquals(num, num2);
    }

    @Test
    public void deserialize() {
        String json = JsonSerializer.serialize("test");
        String object = (String) JsonSerializer.deserialize(json);
        Assert.assertEquals("test", object);

        String[] array = {"test", "test2"};
        json = JsonSerializer.serialize(array);
        JSONArray jsonArray = (JSONArray) JsonSerializer.deserialize(json);
        Assert.assertTrue(Linq.asEnumerable(array).sequenceEqual(Linq.asEnumerable(jsonArray).select(a -> (String) a)));
    }

    @Test
    public void testByFieldName() {
        Mx mx = new Mx();
        mx.setPID("111");
        mx.setACCT_FEE_AMT("hello 中国");
        String json = JsonSerializer.serialize(mx);
        Map<String, Object> map = JsonSerializer.deserializeMap(json);
        Assert.assertEquals("111", map.get("pID"));
        Assert.assertEquals("hello 中国", map.get("ACCT_FEE_AMT"));
        Mx mx2 = JsonSerializer.deserialize(json, Mx.class);
        Assert.assertEquals("111", mx2.getPID());
        Assert.assertEquals("hello 中国", mx2.getACCT_FEE_AMT());
    }

    @Data
    static class Mx {
        private String pID;
        private String ACCT_FEE_AMT;
    }
}
