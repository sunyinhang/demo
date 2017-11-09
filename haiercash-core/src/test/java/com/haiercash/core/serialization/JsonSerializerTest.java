package com.haiercash.core.serialization;

import com.bestvike.linq.Linq;
import com.haiercash.core.lang.Convert;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

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
}
