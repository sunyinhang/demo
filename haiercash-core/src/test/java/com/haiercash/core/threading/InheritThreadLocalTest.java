package com.haiercash.core.threading;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 许崇雷 on 2018-02-01.
 */
public class InheritThreadLocalTest {
    private static final ThreadLocal<List<String>> THREAD_LOCAL = InheritThreadLocal.withInitial(ArrayList::new);

    @Test
    public void withInitial() throws InterruptedException {
        List<String> list = THREAD_LOCAL.get();
        Assert.assertNotNull(list);
        Assert.assertEquals(0, list.size());
        list.add("hello world");
        Thread thd = new Thread(() -> {
            List<String> list2 = THREAD_LOCAL.get();
            Assert.assertNotNull(list2);
            Assert.assertEquals(1, list2.size());
            Assert.assertEquals("hello world", list2.get(0));
            list2.add("hello again");
        });
        thd.setDaemon(true);
        thd.start();
        thd.join();

        List<String> list3 = THREAD_LOCAL.get();
        Assert.assertEquals(list, list3);
        Assert.assertEquals(2, list3.size());
        Assert.assertEquals("hello world", list3.get(0));
        Assert.assertEquals("hello again", list3.get(1));
    }
}
