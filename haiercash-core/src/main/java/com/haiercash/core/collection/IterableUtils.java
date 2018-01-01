package com.haiercash.core.collection;

import org.springframework.util.Assert;

import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author 许崇雷
 * @date 2017/6/8
 */
public final class IterableUtils {
    /**
     * iterable 转换为 List
     *
     * @param iterable 可迭代对象
     * @param <T>      列表实例
     * @return List
     */
    public static <T> List<T> toList(Iterable<T> iterable) {
        return toList(iterable, 10);
    }

    /**
     * iterable 转换为 List
     *
     * @param iterable      可迭代对象
     * @param estimatedSize 容器初始大小
     * @param <T>           类型
     * @return 列表实例
     */
    public static <T> List<T> toList(Iterable<T> iterable, int estimatedSize) {
        if (iterable == null)
            throw new NullPointerException("iterable must not be null");
        return IteratorUtils.toList(iterable.iterator(), estimatedSize);
    }

    /**
     * iterable 转换为 stream
     *
     * @param iterable 可迭代对象
     * @param <T>      类型
     * @return stream 对象
     */
    public static <T> Stream<T> toStream(Iterable<T> iterable) {
        return toStream(iterable, false);
    }

    /**
     * iterable 转换为并行 stream
     *
     * @param iterable 可迭代对象
     * @param <T>      类型
     * @return 并行 stream
     */
    public static <T> Stream<T> toParallelStream(Iterable<T> iterable) {
        return toStream(iterable, true);
    }

    /**
     * iterable 转换为 stream
     *
     * @param iterable 可迭代对象
     * @param parallel 是否并行
     * @param <T>      类型
     * @return stream 对象
     */
    public static <T> Stream<T> toStream(Iterable<T> iterable, boolean parallel) {
        if (iterable == null)
            throw new NullPointerException("iterable must not be null");
        return StreamSupport.stream(iterable.spliterator(), parallel);
    }

    /**
     * 枚举器追加元素
     *
     * @param first 枚举器
     * @param args  要追加的元素
     * @param <T>   类型
     * @return 新的枚举器
     */
    @SafeVarargs
    public static <T> Iterable<T> append(Iterable<? extends T> first, T... args) {
        return concat(first, ArrayUtils.asIterable(args));
    }

    /**
     * 连接两个可迭代对象
     *
     * @param first  第一个可迭代对象
     * @param second 第二个可迭代对象
     * @param <T>    类型
     * @return 连接后的可迭代对象
     */
    public static <T> Iterable<T> concat(Iterable<? extends T> first, Iterable<? extends T> second) {
        Assert.notNull(first, "first can not be null");
        Assert.notNull(second, "second can not be null");
        return () -> IteratorUtils.concat(first.iterator(), second.iterator());
    }
}
