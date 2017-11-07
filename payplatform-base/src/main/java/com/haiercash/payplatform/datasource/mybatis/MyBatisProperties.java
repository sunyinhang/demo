package com.haiercash.payplatform.datasource.mybatis;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Created by 许崇雷 on 2017-11-06.
 */
@Data

@ConfigurationProperties(prefix = "spring.datasource.mybatis")
public class MyBatisProperties {
    private String dialect;
    private String basePackage;
    private String aliasesPackage;
    private List<String> mapperLocations;
    private Boolean underscoreToCamelCase;
}
