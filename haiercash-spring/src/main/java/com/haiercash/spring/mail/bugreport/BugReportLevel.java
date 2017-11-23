package com.haiercash.spring.mail.bugreport;

/**
 * Created by 许崇雷 on 2017-11-23.
 */
public enum BugReportLevel {
    UNKNOWN("UNKNOWN", "未知"),
    INFO("INFO", "消息"),
    WARN("WARN", "警告"),
    ERROR("ERROR", "错误");

    private final String name;
    private final String description;

    BugReportLevel(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
