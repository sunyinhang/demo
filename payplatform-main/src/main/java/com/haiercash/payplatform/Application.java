package com.haiercash.payplatform;

import com.haiercash.payplatform.client.RestTemplateEx;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.web.client.RestTemplate;

/**
 * 应用程序入口
 */
@SpringBootApplication
@EnableRedisHttpSession
@EnableZuulProxy
@EnableScheduling
@EnableEurekaClient
@ComponentScan(basePackages = {"com.haiercash.commons", "com.haiercash.payplatform"},
        excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com.haiercash.commons.config.*"))
public class Application {
    @Primary
    @LoadBalanced
    @Bean
    RestTemplate restTemplate() {
        return new RestTemplateEx();
    }

    @Bean(name = "restTemplateNormal")
    RestTemplate restTemplateNormal() {
        return new RestTemplateEx();
    }

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
        context.addApplicationListener((ApplicationListener<ContextClosedEvent>) event -> System.out.println("应用程序正常退出..."));
    }
}
