package com.haiercash.core.collection;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

/**
 * Created by 许崇雷 on 2016/8/27.
 * 线程本地池,每个线程每个Key对应一个Value
 * 线程安全
 */
public abstract class ThreadLocalHashPool<TKey, TValue> {
    //容器
    private final ThreadLocalHashMap<TKey, TValue> threadLocalMap = new ThreadLocalHashMap<>();

    /**
     * 创建线程本地池
     *
     * @param function 创建对象的方法
     * @param <K>      键类型
     * @param <V>      值类型
     * @return 池
     */
    public static <K, V> ThreadLocalHashPool<K, V> withInitial(Function<? super K, ? extends V> function) {
        return new FuncThreadLocalHashPool<>(function);
    }

    /**
     * 初始化当前线程指定键对应的值,值不允许为 null
     *
     * @param key 键
     * @return 初始值
     */
    protected abstract TValue initialValue(TKey key);

    /**
     * 根据 key 从当前线程获取一个值,该值不会为 null
     *
     * @param key 键
     * @return 当前线程 key 对应值
     */
    public TValue get(TKey key) {
        return this.threadLocalMap.computeIfAbsent(key, this::initialValue);
    }

    /**
     * 根据 key 从当前线程移除一个值,并返回该值
     *
     * @param key 键
     * @return 移除的值
     */
    public TValue remove(TKey key) {
        return this.threadLocalMap.remove(key);
    }

    /**
     * 清理当前线程内所有键值对
     */
    public void clear() {
        this.threadLocalMap.clear();
    }

    /**
     * 获取当前线程键集合
     *
     * @return 当前线程键集合
     */
    public Set<TKey> keySet() {
        return this.threadLocalMap.keySet();
    }

    /**
     * 获取当前线程值集合
     *
     * @return 当前线程值集合
     */
    public Collection<TValue> values() {
        return this.threadLocalMap.values();
    }

    /**
     * 获取当前线程键值对集合
     *
     * @return 当前线程键值对集合
     */
    public Set<Map.Entry<TKey, TValue>> entrySet() {
        return this.threadLocalMap.entrySet();
    }

    /**
     * 实现线程本地池
     *
     * @param <TKey>   键类型
     * @param <TValue> 值类型
     */
    static final class FuncThreadLocalHashPool<TKey, TValue> extends ThreadLocalHashPool<TKey, TValue> {
        //创建对象的方法
        private final Function<? super TKey, ? extends TValue> function;

        /**
         * 构造函数
         *
         * @param function 创建对象的方法
         */
        FuncThreadLocalHashPool(Function<? super TKey, ? extends TValue> function) {
            this.function = Objects.requireNonNull(function);
        }

        /**
         * 初始化当前线程指定键对应的值,值不允许为 null
         *
         * @param key 键
         * @return 初始值
         */
        @Override
        protected TValue initialValue(TKey key) {
            return this.function.apply(key);
        }
    }
}
