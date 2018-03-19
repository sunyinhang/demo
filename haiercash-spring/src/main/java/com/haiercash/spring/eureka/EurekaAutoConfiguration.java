package com.haiercash.spring.eureka;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Created by 许崇雷 on 2018-01-09.
 */
@Configuration
@EnableConfigurationProperties(EurekaProperties.class)
public class EurekaAutoConfiguration {
}
