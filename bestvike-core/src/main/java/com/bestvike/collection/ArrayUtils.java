package com.bestvike.collection;

import org.springframework.util.Assert;

import java.lang.reflect.Type;
import java.util.Enumeration;
import java.util.Iterator;

/**
 * @author 许崇雷
 * @date 2017/6/7
 */
public class ArrayUtils extends org.apache.commons.lang3.ArrayUtils {
    /**
     * 空的类型数组
     */
    public static final Type[] EMPTY_TYPE_ARRAY = new Type[0];

    /**
     * 数组转换为枚举对象
     *
     * @param args 数组
     * @param <T>  类型
     * @return 枚举对象
     */
    public static <T> Enumeration<T> asEnumeration(T... args) {
        Assert.notNull(args, "args can not be null");
        return new Enumeration<T>() {
            private int index = 0;

            @Override
            public boolean hasMoreElements() {
                return this.index < args.length;
            }

            @Override
            public T nextElement() {
                return args[this.index++];
            }
        };
    }

    /**
     * 数组转为迭代器
     *
     * @param args 数组
     * @param <T>  类型
     * @return 迭代器
     */
    public static <T> Iterator<T> asIterator(T... args) {
        Assert.notNull(args, "args can not be null");
        return new Iterator<T>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return this.index < args.length;
            }

            @Override
            public T next() {
                return args[this.index++];
            }
        };
    }

    /**
     * 数组转为可迭代对象
     *
     * @param args 数组
     * @param <T>  类型
     * @return 可迭代对象
     */
    public static <T> Iterable<T> asIterable(T... args) {
        Assert.notNull(args, "args can not be null");
        return () -> asIterator(args);
    }
}
