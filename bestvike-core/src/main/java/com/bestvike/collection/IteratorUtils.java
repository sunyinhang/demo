package com.bestvike.collection;

import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

/**
 * 迭代器工具
 *
 * @author 许崇雷
 * @date 2017/6/8
 */
public final class IteratorUtils {
    /**
     * 迭代器转为枚举器
     *
     * @param iterator 迭代器
     * @param <T>      类型
     * @return 枚举器
     */
    public static <T> Enumeration<T> toEnumeration(Iterator<? extends T> iterator) {
        Assert.notNull(iterator, "iterator can not be null");
        return new Enumeration<T>() {
            @Override
            public boolean hasMoreElements() {
                return iterator.hasNext();
            }

            @Override
            public T nextElement() {
                return iterator.next();
            }
        };
    }

    /**
     * 迭代器转为列表
     *
     * @param iterator 迭代器
     * @param <T>      类型
     * @return 列表
     */
    public static <T> List<T> toList(Iterator<? extends T> iterator) {
        return toList(iterator, 10);
    }

    /**
     * 迭代器转为列表
     *
     * @param iterator      迭代器
     * @param estimatedSize 预计大小
     * @param <T>           类型
     * @return 列表
     */
    public static <T> List<T> toList(Iterator<? extends T> iterator, int estimatedSize) {
        if (iterator == null)
            throw new NullPointerException("Iterator must not be null");
        if (estimatedSize < 1)
            throw new IllegalArgumentException("Estimated size must be greater than 0");

        List<T> list = new ArrayList<>(estimatedSize);
        while (iterator.hasNext())
            list.add(iterator.next());
        return list;
    }

    /**
     * 迭代器追加元素
     *
     * @param first 迭代器
     * @param args  要追加的元素
     * @param <T>   类型
     * @return 新的迭代器
     */
    public static <T> Iterator<T> append(Iterator<? extends T> first, T... args) {
        return concat(first, ArrayUtils.asIterator(args));
    }

    /**
     * 连接两个迭代器
     *
     * @param first  第一个迭代器
     * @param second 第二个迭代器
     * @param <T>    类型
     * @return 连接后的迭代器
     */
    public static <T> Iterator<T> concat(Iterator<? extends T> first, Iterator<? extends T> second) {
        Assert.notNull(first, "first can not be null");
        Assert.notNull(second, "second can not be null");
        return new Iterator<T>() {
            @Override
            public boolean hasNext() {
                return first.hasNext() || second.hasNext();
            }

            @Override
            public T next() {
                return first.hasNext() ? first.next() : second.next();
            }
        };
    }
}
