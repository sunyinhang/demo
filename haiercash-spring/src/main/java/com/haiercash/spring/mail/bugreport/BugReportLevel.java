package com.haiercash.spring.mail.bugreport;

/**
 * Created by 许崇雷 on 2017-11-23.
 */
public enum BugReportLevel {
    INFO("消息"),
    WARN("警告"),
    ERROR("错误");

    public static final BugReportLevel DEFAULT_LEVEL = BugReportLevel.ERROR;

    private final String description;

    BugReportLevel(String description) {
        this.description = description;
    }

    public static BugReportLevel forName(String name) {
        if (name == null)
            return DEFAULT_LEVEL;
        try {
            return BugReportLevel.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return DEFAULT_LEVEL;
        }
    }

    public String getDescription() {
        return this.description;
    }
}
