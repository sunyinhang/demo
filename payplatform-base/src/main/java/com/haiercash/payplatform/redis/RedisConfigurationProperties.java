package com.haiercash.payplatform.redis;

import com.bestvike.lang.StringUtils;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Created by 许崇雷 on 2017-11-01.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "spring.redis")
public class RedisConfigurationProperties {
    private String globalKeyPrefix;
    private Integer defaultValueExpire;

    public String getKey(String key) {
        String globalKeyPrefix = StringUtils.isEmpty(this.globalKeyPrefix)
                ? StringUtils.EMPTY
                : (this.globalKeyPrefix + ":");
        return key == null ? globalKeyPrefix : (globalKeyPrefix + key);
    }

    public boolean useValueExpire() {
        return this.defaultValueExpire != null && this.defaultValueExpire > 0;
    }

    public TimeUnit getTimeUnit() {
        return TimeUnit.SECONDS;
    }
}
