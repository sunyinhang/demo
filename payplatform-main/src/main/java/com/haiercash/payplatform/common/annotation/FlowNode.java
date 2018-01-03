package com.haiercash.payplatform.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <h5>
 * 贷款流程日志记录注解.
 * </h5>
 * 使用方式demo：
 * <pre> {@code
 * public class ProgressDemo{
 *     @code @FlowNode(name="贷款", node="实名认证")
 *      public String lain(String x, double y) {
 *           doSomething();
 *           return String;
 *      }
 *}
 * }</pre>
 * @author Qingxiang.Li
 * @since v1.3.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FlowNode {
    String flow();

    String node();
}
