package com.haiercash.spring.client.configure;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import com.haiercash.spring.client.HttpConvertersProperties;
import com.haiercash.spring.client.converter.FastJsonHttpMessageConverterEx;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by 许崇雷 on 2017-09-15.
 */
@Configuration
public class FastJsonHttpMessageConvertersConfiguration {
    @Configuration
    @ConditionalOnClass({JSON.class, FastJsonHttpMessageConverter.class})
    @ConditionalOnProperty(name = HttpConvertersProperties.PREFERRED_JSON_MAPPER_PROPERTY, havingValue = HttpMessageConvertersAutoConfiguration.PREFERRED_JSON_MAPPER_FASTJSON, matchIfMissing = false)
    protected static class FastJsonHttpMessageConverterConfiguration {
        @Bean
        @ConditionalOnMissingBean(value = FastJsonHttpMessageConverterEx.class)
        public FastJsonHttpMessageConverterEx fastJsonHttpMessageConverter() {
            return new FastJsonHttpMessageConverterEx();
        }
    }
}
