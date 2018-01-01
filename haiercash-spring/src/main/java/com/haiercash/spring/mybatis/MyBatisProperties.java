package com.haiercash.spring.mybatis;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Created by 许崇雷 on 2017-11-06.
 */
@Data
@ConfigurationProperties(prefix = "spring.mybatis")
public final class MyBatisProperties {
    private String dialect;
    private String basePackage;
    private String aliasesPackage;
    private List<String> mapperLocations;
    private Boolean underscoreToCamelCase;
}
