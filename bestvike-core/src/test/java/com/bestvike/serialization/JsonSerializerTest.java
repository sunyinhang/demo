package com.bestvike.serialization;

import com.bestvike.lang.Convert;
import com.bestvike.linq.Linq;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * Created by 许崇雷 on 2017-09-22.
 */
public class JsonSerializerTest {
    private static final Person PERSON = new Person(123, "测试", Convert.toDate("2017-01-02 03:04:05"));

    private static final String JSON = "{\"birthday\":\"2017-01-02 03:04:05\",\"name\":\"测试\",\"uid\":123}";
    private static final Person[] PERSON_ARRAY = new Person[]{PERSON};
    private static final String JSON_ARRAY = "[{\"birthday\":\"2017-01-02 03:04:05\",\"name\":\"测试\",\"uid\":123}]";

    @Test
    public void testSerialize() {
        String json = JsonSerializer.serialize(PERSON);
        Assert.assertEquals(JSON, json);
    }

    @Test
    public void testDeserialize() {
        Person person = JsonSerializer.deserialize(JSON, Person.class);
        Assert.assertEquals(PERSON, person);
    }

    @Test
    public void testSerializeArray() {
        String jsonArray = JsonSerializer.serialize(PERSON_ARRAY);
        Assert.assertEquals(JSON_ARRAY, jsonArray);
    }

    @Test
    public void testDeserializeArray() {
        List<Person> personList = JsonSerializer.deserializeArray(JSON_ARRAY, Person.class);
        Person[] personArray = Linq.asEnumerable(personList).toArray(Person.class);
        Assert.assertArrayEquals(PERSON_ARRAY, personArray);
    }
}
