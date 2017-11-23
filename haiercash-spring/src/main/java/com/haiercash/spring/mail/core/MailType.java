package com.haiercash.spring.mail.core;

/**
 * Created by 许崇雷 on 2016/9/5.
 */
public enum MailType {
    /**
     * 普通文本
     */
    PLAIN("text/plain;charset=UTF-8"),
    /**
     * Html 文本
     */
    HTML("text/html;charset=UTF-8");

    //值
    private final String value;

    /**
     * 构造函数
     *
     * @param value
     */
    MailType(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }

    /**
     * 转换为字符串
     *
     * @return
     */
    @Override
    public String toString() {
        return this.value;
    }
}
