package com.haiercash.appserver;

import com.haiercash.appserver.config.CloudConfiguration;
import com.haiercash.common.config.EurekaServer;
import com.haiercash.commons.util.CommonProperties;
import com.haiercash.commons.util.RedisProperties;
import com.haiercash.commons.util.SignProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableRedisHttpSession
@EnableZuulProxy
@EnableConfigurationProperties({ SignProperties.class, RedisProperties.class, CommonProperties.class,
        EurekaServer.class })
@ComponentScan(basePackages = { "com.haiercash.common.data.*", "com.haiercash.commons.aop",
        "com.haiercash.commons.util", "com.haiercash.appserver" })
@RibbonClient(name = "HCPortal", configuration = CloudConfiguration.class)
@EnableEurekaClient
public class Application {
    @LoadBalanced
    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(Application.class, args);
       /* ScheduledReporter reporter = (ScheduledReporter) ctx.getBean("influxdbReporter");
        reporter.start(1, TimeUnit.SECONDS);*/
    }

}
