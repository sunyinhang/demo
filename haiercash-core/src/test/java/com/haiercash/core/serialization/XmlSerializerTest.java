package com.haiercash.core.serialization;

import com.bestvike.linq.Linq;
import com.haiercash.core.lang.Convert;
import com.haiercash.core.lang.DateUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by 许崇雷 on 2018-01-08.
 */
public class XmlSerializerTest {
    private static final Person PERSON = new Person(123, "测试", Convert.toDate("2017-01-02 03:04:05"));
    private static final Map<String, Object> PERSON_MAP = new LinkedHashMap<>();

    static {
        PERSON_MAP.put("uid", PERSON.getUid());
        PERSON_MAP.put("name", PERSON.getName());
        PERSON_MAP.put("birthday", PERSON.getBirthday());
    }


    @Test
    public void serialize() {
        String xml = XmlSerializer.serialize(PERSON, "request");
        String xml2 = XmlSerializer.serialize(PERSON_MAP, "request");
        Assert.assertEquals(xml, xml2);
    }

    @Test
    public void deserialize() {
        String xml = XmlSerializer.serialize(PERSON);
        Person person = XmlSerializer.deserialize(xml, Person.class);
        Assert.assertEquals(PERSON, person);
    }


    @Test
    public void deserializeArray() {
        Person[] list = new Person[1];
        list[0] = PERSON;
        String xml = XmlSerializer.serialize(list);
        List<Person> list2 = XmlSerializer.deserializeArray(xml, Person.class);
        if (!Linq.asEnumerable(list).sequenceEqual(Linq.asEnumerable(list2)))
            Assert.fail();
    }

    @Test
    public void deserializeMap() {
        String xml = XmlSerializer.serialize(PERSON_MAP);
        Map<String, Object> map = XmlSerializer.deserializeMap(xml);
        Assert.assertEquals(PERSON_MAP.get("uid").toString(), map.get("uid"));
        Assert.assertEquals(PERSON_MAP.get("name"), map.get("name"));
        Assert.assertEquals(DateUtils.toDateTimeString((Date) PERSON_MAP.get("birthday")), map.get("birthday"));
    }
}
