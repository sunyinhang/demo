package com.haiercash.spring.mail.bugreport;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by 许崇雷 on 2017-11-21.
 */
@Configuration
@EnableConfigurationProperties(BugReportProperties.class)
public class BugReportAutoConfiguration {
    private final BugReportProperties properties;

    public BugReportAutoConfiguration(BugReportProperties properties) {
        this.properties = properties;
    }

    @Bean
    BugReportMailFactory bugReportMailFactory() {
        return new BugReportMailFactory(this.properties);
    }

    @Bean
    BugReportThread bugReportThread() {
        BugReportThread thread = new BugReportThread(this.properties);
        thread.setDaemon(true);//后台线程
        thread.setName("bugReport");
        thread.start();
        return thread;
    }
}
