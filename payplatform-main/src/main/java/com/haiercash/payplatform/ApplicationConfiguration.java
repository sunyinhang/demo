package com.haiercash.payplatform;

import com.haiercash.payplatform.client.RestTemplateEx;
import com.haiercash.payplatform.client.RestTemplateSupportedType;
import com.haiercash.payplatform.rabbit.RabbitTemplateEx;
import com.haiercash.payplatform.redis.RedisTemplateEx;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.client.RestTemplate;

/**
 * Created by 许崇雷 on 2017-11-03.
 */
public class ApplicationConfiguration {
    //region restTemplate

    @Primary
    @LoadBalanced
    @Bean
    private RestTemplate restTemplate() {
        return new RestTemplateEx(RestTemplateSupportedType.JSON);
    }

    @Bean(name = "restTemplateJson")
    private RestTemplate restTemplateJson() {
        return new RestTemplateEx(RestTemplateSupportedType.JSON);
    }

    @Bean(name = "restTemplateXml")
    private RestTemplate restTemplateXml() {
        return new RestTemplateEx(RestTemplateSupportedType.XML);
    }

    //endregion


    //region redisTemplate

    @Primary
    @Bean
    private StringRedisTemplate redisTemplate() {
        return new RedisTemplateEx();
    }

    //endregion


    //region rabbitTemplate

    @Primary
    @Bean
    private RabbitTemplate rabbitTemplate() {
        return new RabbitTemplateEx();
    }

    //endregion
}
