package com.haiercash.core.collection;

import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

/**
 * 枚举器工具
 *
 * @author 许崇雷
 * @date 2017/7/4
 */
public final class EnumerationUtils {
    /**
     * 枚举器转为迭代器
     *
     * @param enumeration 枚举器
     * @param <T>         类型
     * @return 迭代器
     */
    public static <T> Iterator<T> toIterator(Enumeration<? extends T> enumeration) {
        Assert.notNull(enumeration, "enumeration can not be null");
        return new Iterator<T>() {
            @Override
            public boolean hasNext() {
                return enumeration.hasMoreElements();
            }

            @Override
            public T next() {
                return enumeration.nextElement();
            }
        };
    }

    /**
     * 枚举器转为列表
     *
     * @param enumeration 枚举器
     * @param <T>         类型
     * @return 列表
     */
    public static <T> List<T> toList(Enumeration<? extends T> enumeration) {
        return toList(enumeration, 10);
    }

    /**
     * 枚举器转为列表
     *
     * @param enumeration   枚举器
     * @param estimatedSize 预计大小
     * @param <T>           类型
     * @return 列表
     */
    public static <T> List<T> toList(Enumeration<? extends T> enumeration, int estimatedSize) {
        if (enumeration == null)
            throw new NullPointerException("enumeration must not be null");
        if (estimatedSize < 1)
            throw new IllegalArgumentException("Estimated size must be greater than 0");

        List<T> list = new ArrayList<>(estimatedSize);
        while (enumeration.hasMoreElements())
            list.add(enumeration.nextElement());
        return list;
    }

    /**
     * 枚举器追加元素
     *
     * @param first 枚举器
     * @param args  要追加的元素
     * @param <T>   类型
     * @return 新的枚举器
     */
    public static <T> Enumeration<T> append(Enumeration<? extends T> first, T... args) {
        return concat(first, ArrayUtils.asEnumeration(args));
    }

    /**
     * 连接两个枚举器
     *
     * @param first  第一个枚举器
     * @param second 第二个枚举器
     * @param <T>    类型
     * @return 连接后的枚举器
     */
    public static <T> Enumeration<T> concat(Enumeration<? extends T> first, Enumeration<? extends T> second) {
        Assert.notNull(first, "first can not be null");
        Assert.notNull(second, "second can not be null");
        return new Enumeration<T>() {
            @Override
            public boolean hasMoreElements() {
                return first.hasMoreElements() || second.hasMoreElements();
            }

            @Override
            public T nextElement() {
                return first.hasMoreElements() ? first.nextElement() : second.nextElement();
            }
        };
    }
}
