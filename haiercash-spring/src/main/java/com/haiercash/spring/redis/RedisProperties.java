package com.haiercash.spring.redis;

import com.bestvike.linq.Linq;
import com.haiercash.core.lang.StringUtils;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by 许崇雷 on 2017-11-01.
 */
@Data
@ConfigurationProperties(prefix = "spring.redis")
public final class RedisProperties {
    private String globalKeyPrefix;
    private Integer defaultValueExpire;

    public String getKey(String key) {
        return StringUtils.isEmpty(this.globalKeyPrefix)
                ? (key == null ? StringUtils.EMPTY : key)
                : (StringUtils.isEmpty(key) ? globalKeyPrefix : (globalKeyPrefix + ":" + key));
    }

    public String[] getKeys(String[] keys) {
        return keys == null ? null : Linq.asEnumerable(keys).select(this::getKey).toArray(String.class);
    }

    public Collection<String> getKeys(Collection<String> keys) {
        return keys == null ? null : Linq.asEnumerable(keys).select(this::getKey).toList();
    }

    public <T> Map<? extends String, T> getKeyMap(Map<? extends String, T> map) {
        return map == null ? null : Linq.asEnumerable(map).toMap(entry -> this.getKey(entry.getKey()), Map.Entry::getValue);
    }

    public boolean valueExpireEnabled() {
        return this.defaultValueExpire != null && this.defaultValueExpire > 0;
    }

    public TimeUnit getTimeUnit() {
        return TimeUnit.SECONDS;
    }
}
