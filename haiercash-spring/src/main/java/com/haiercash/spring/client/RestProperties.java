package com.haiercash.spring.client;

import com.haiercash.core.collection.CollectionUtils;
import lombok.Data;
import org.apache.tomcat.util.collections.CaseInsensitiveKeyMap;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Arrays;
import java.util.List;

/**
 * Created by 许崇雷 on 2017-11-06.
 */
@Data
@ConfigurationProperties(prefix = "spring.rest")
public final class RestProperties {
    private static final Object PRESENT = new Object();
    private static final List<String> SYSTEM_IGNORE_HEADERS = Arrays.asList("Accept", "Accept-Encoding", "Accept-Language", "Content-Type", "Content-Length", "Cookie", "Set-Cookie", "Authorization", "Connection", "Host", "User-Agent");
    private Integer connectTimeout;
    private Integer readTimeout;
    private Boolean routeHeadersEnabled;
    private List<String> ignoreHeaders;

    public void config(RestTemplateEx restTemplate) {
        restTemplate.routeHeadersEnabled = this.routeHeadersEnabled;
        CaseInsensitiveKeyMap<Object> set = new CaseInsensitiveKeyMap<>();
        for (String headerName : SYSTEM_IGNORE_HEADERS)
            set.put(headerName, PRESENT);
        if (!CollectionUtils.isEmpty(this.ignoreHeaders))
            for (String headerName : this.ignoreHeaders)
                set.put(headerName, PRESENT);
        restTemplate.ignoreHeaders = set;
    }
}
