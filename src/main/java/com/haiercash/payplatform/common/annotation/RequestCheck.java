package com.haiercash.payplatform.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * request check annotation.
 * <p>
 *     需要校验请求token合法性的接口，注入该注解。
 * </p>
 * @author Liu qingxiang
 * @since v1.5.6
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestCheck {
    boolean value() default true;
}
