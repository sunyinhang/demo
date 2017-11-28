package com.haiercash.core.reflect;

import com.bestvike.linq.exception.ArgumentNullException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by 许崇雷 on 2017-11-28.
 */
public final class ReflectionUtils {
    private ReflectionUtils() {
    }

    private static Field getFieldInfoCore(Class<?> clazz, String filedName) {
        if (clazz == null)
            throw new ArgumentNullException("clazz", "clazz can not be null");
        try {
            Field field = clazz.getDeclaredField(filedName);
            field.setAccessible(true);
            return field;
        } catch (Exception e) {
            return null;
        }
    }

    private static Method getMethodInfoCore(Class<?> clazz, String methodName, Class<?>[] parameterTypes) {
        if (clazz == null)
            throw new ArgumentNullException("clazz", "clazz can not be null");
        try {
            Method method = clazz.getDeclaredMethod(methodName, parameterTypes);
            method.setAccessible(true);
            return method;
        } catch (Exception e) {
            return null;
        }
    }

    //获取构造函数
    public static <T> Constructor<T> getConstructorInfo(Class<T> clazz, Class<?>[] parameterTypes) {
        if (clazz == null)
            throw new ArgumentNullException("clazz", "clazz can not be null");
        try {
            Constructor<T> constructor = clazz.getConstructor(parameterTypes);
            constructor.setAccessible(true);
            return constructor;
        } catch (Exception e) {
            return null;
        }
    }

    //获取字段,可获取父类私有字段
    public static Field getFieldInfo(Class<?> clazz, String filedName) {
        while (!clazz.equals(Object.class)) {
            Field field = getFieldInfoCore(clazz, filedName);
            if (field == null) {
                clazz = clazz.getSuperclass();
                continue;
            }
            return field;
        }
        return null;
    }

    //获取方法,可获取父类私有方法
    public static Method getMethodInfo(Class<?> clazz, String methodName, Class<?>[] parameterTypes) {
        while (!clazz.equals(Object.class)) {
            Method method = getMethodInfoCore(clazz, methodName, parameterTypes);
            if (method == null) {
                clazz = clazz.getSuperclass();
                continue;
            }
            return method;
        }
        return null;
    }

    //创建实例
    public static <T> T newInstance(Class<T> clazz) {
        return newInstance(clazz, null, null);
    }

    //创建实例
    public static <T> T newInstance(Class<T> clazz, Class<?>[] parameterTypes, Object[] args) {
        Constructor<T> constructor = getConstructorInfo(clazz, parameterTypes);
        if (constructor == null)
            throw new RuntimeException("no such constructor");
        try {
            return constructor.newInstance(args);
        } catch (Exception e) {
            throw new RuntimeException("create instance fail", e);
        }
    }

    //获取字段值
    @SuppressWarnings("unchecked")
    public static <T> T getField(Object target, String fieldName) {
        if (target == null)
            throw new ArgumentNullException("target", "target can not be null");
        Class<?> clazz = target.getClass();
        Field field = getFieldInfo(clazz, fieldName);
        if (field == null)
            throw new RuntimeException("no such field");
        try {
            return (T) field.get(target);
        } catch (Exception e) {
            throw new RuntimeException("get field value fail", e);
        }
    }

    //设置字段值
    @SuppressWarnings("unchecked")
    public static void setField(Object target, String fieldName, Object value) {
        if (target == null)
            throw new ArgumentNullException("target", "target can not be null");
        Class<?> clazz = target.getClass();
        Field field = getFieldInfo(clazz, fieldName);
        if (field == null)
            throw new RuntimeException("no such field");
        try {
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("set field value fail", e);
        }
    }

    //调用方法
    public static <T> T invoke(Object target, String methodName) {
        return invoke(target, methodName, null, null);
    }

    //调用方法
    @SuppressWarnings("unchecked")
    public static <T> T invoke(Object target, String methodName, Class<?>[] parameterTypes, Object[] args) {
        if (target == null)
            throw new ArgumentNullException("target", "target can not be null");
        Class<?> clazz = target.getClass();
        Method method = getMethodInfo(clazz, methodName, parameterTypes);
        if (method == null)
            throw new RuntimeException("no such method");
        try {
            return (T) method.invoke(target, args);
        } catch (Exception e) {
            throw new RuntimeException("invoke method fail", e);
        }
    }
}
