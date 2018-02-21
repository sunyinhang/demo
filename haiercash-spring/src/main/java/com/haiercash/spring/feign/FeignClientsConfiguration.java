package com.haiercash.spring.feign;

import com.haiercash.spring.feign.core.RestFeign;
import feign.Feign;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * Created by 许崇雷 on 2018-02-09.
 */
@Configuration
public class FeignClientsConfiguration {
    @Bean
    @Scope("prototype")
    @ConditionalOnMissingBean
    public Feign.Builder feignBuilder() {
        return RestFeign.builder();
    }
}
