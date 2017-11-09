package com.haiercash.spring.zuul;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Created by 许崇雷 on 2017-11-09.
 */
@Data
@ConfigurationProperties(prefix = "zuul")
public class ZuulProperties {
    //该配置项只为消除警告，其实际应用在 spring 框架中
    private Boolean setContentLength;
}
