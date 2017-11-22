package com.haiercash.spring.mail.bugreport;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Created by 许崇雷 on 2017-11-21.
 */
@Data
@ConfigurationProperties(prefix = "spring.mail.bug-report")
public final class BugReportProperties {
    private Boolean enabled;
    private Integer queueSize = 10;
    private Integer sendIntervalSeconds;
    private String subject;
    private List<String> to;
    private List<String> cc;
    private List<String> bcc;
}
