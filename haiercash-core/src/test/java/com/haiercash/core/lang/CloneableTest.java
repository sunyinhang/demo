package com.haiercash.core.lang;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by 许崇雷 on 2017-11-24.
 */
public class CloneableTest {
    @Test
    public void testClone() {
        Student student1 = new Student(1, "jim");
        Student student2 = student1.clone();
        student2.setId(2);
        student2.setName("john");

        Assert.assertEquals(1, student1.getId());
        Assert.assertEquals(2, student2.getId());
        Assert.assertEquals("jim", student1.getName());
        Assert.assertEquals("john", student2.getName());
    }
}
