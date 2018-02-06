package com.haiercash.core.threading;

import com.bestvike.linq.Linq;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by 许崇雷 on 2018-02-06.
 */
public class ThreadPoolTest {
    private static final ThreadLocal<List<String>> INHERIT_THREAD_LOCAL = InheritThreadLocal.withInitial(ArrayList::new);
    private static final ThreadLocal<List<String>> THREAD_LOCAL = ThreadLocal.withInitial(ArrayList::new);

    @Test
    public void testInherit() throws InterruptedException {
        List<String> list = THREAD_LOCAL.get();
        Assert.assertNotNull(list);
        Assert.assertEquals(0, list.size());
        list.add("hello world");

        Thread thread = new Thread(() -> {
            List<String> list2 = THREAD_LOCAL.get();
            Assert.assertNotNull(list2);
            Assert.assertEquals(0, list2.size());
            list2.add("hello moon");
            Future<?> future = ThreadPool.submit(() -> {
                List<String> list3 = THREAD_LOCAL.get();
                Assert.assertEquals(list2, list3);
                Assert.assertNotEquals(list, list3);
            });
            while (!future.isDone())
                ThreadUtils.sleep(100);
        });
        thread.start();
        thread.join();
    }

    @Test
    public void testInherit2() throws InterruptedException {
        List<String> list = INHERIT_THREAD_LOCAL.get();
        Assert.assertNotNull(list);
        Assert.assertEquals(0, list.size());
        list.add("hello world");

        Thread thread = new Thread(() -> {
            List<String> list2 = INHERIT_THREAD_LOCAL.get();
            Assert.assertNotNull(list2);
            Assert.assertEquals(list, list2);
            Future<?> future = ThreadPool.submit(() -> {
                List<String> list3 = INHERIT_THREAD_LOCAL.get();
                Assert.assertEquals(list2, list3);
                Assert.assertEquals(list, list3);
            });
            while (!future.isDone())
                ThreadUtils.sleep(100);
        });
        thread.start();
        thread.join();
    }

    @Test
    public void testParallelStream() {
        List<String> list = INHERIT_THREAD_LOCAL.get();
        Assert.assertNotNull(list);
        Assert.assertEquals(0, list.size());
        list.add("hello world");

        Integer[] numbers = new Integer[100];
        for (int i = 0; i < 100; i++) {
            numbers[i] = i;
        }
        List<Integer> numbers2 = Stream.of(numbers).parallel()
                .map(a -> {
                    List<String> list2 = INHERIT_THREAD_LOCAL.get();
                    Assert.assertEquals(list, list2);
                    return a * 2;
                })
                .collect(Collectors.toList());

        List<Integer> numbers3 = Linq.range(0, 100).select(a -> a * 2).toList();
        Assert.assertTrue(Linq.asEnumerable(numbers2).sequenceEqual(Linq.asEnumerable(numbers3)));
    }
}
