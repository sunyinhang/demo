package com.haiercash.spring.redis;

import com.haiercash.core.lang.StringUtils;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.concurrent.TimeUnit;

/**
 * Created by 许崇雷 on 2017-11-01.
 */
@Data
@ConfigurationProperties(prefix = "spring.redis")
public final class RedisProperties {
    private String globalKeyPrefix;
    private Integer defaultValueExpire;

    public String addPrefix(String key) {
        return StringUtils.isEmpty(this.globalKeyPrefix)
                ? (key == null ? StringUtils.EMPTY : key)
                : (StringUtils.isEmpty(key) ? globalKeyPrefix : (globalKeyPrefix + ":" + key));
    }

    public String removePrefix(String key) {
        return StringUtils.isEmpty(key)
                ? StringUtils.EMPTY
                : (StringUtils.startsWith(key, this.globalKeyPrefix) ? key.substring(this.globalKeyPrefix.length(), key.length()) : key);
    }

    public boolean valueExpireEnabled() {
        return this.defaultValueExpire != null && this.defaultValueExpire > 0;
    }

    public TimeUnit getTimeUnit() {
        return TimeUnit.SECONDS;
    }
}
