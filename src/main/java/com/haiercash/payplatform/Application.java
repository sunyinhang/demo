package com.haiercash.payplatform;

import com.haiercash.commons.properties.RestProfileProperties;
import com.haiercash.payplatform.common.config.EurekaServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.ErrorPage;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.web.client.RestTemplate;

/**
 * 2017-02-28: 增加扫描范围，启用cmis数据源配置
 */

@SpringBootApplication
@EnableRedisHttpSession
@ServletComponentScan
@ComponentScan(basePackages = {"com.haiercash.commons", "com.haiercash.payplatform"})
@EnableEurekaClient
@EnableScheduling
@Configuration
@EnableAutoConfiguration
@EnableConfigurationProperties({RestProfileProperties.class, EurekaServer.class})
public class Application extends SpringBootServletInitializer {
    @LoadBalanced
    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
        context.addApplicationListener((ApplicationListener<ContextClosedEvent>) event -> {
            System.out.println("应用程序正常退出...");
        });
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(Application.class);
    }

    @Bean
    public EmbeddedServletContainerCustomizer containerCustomizer() {

        return (x -> {
            ErrorPage error401Page = new ErrorPage(HttpStatus.FORBIDDEN, "/403.html");
            ErrorPage error404Page = new ErrorPage(HttpStatus.NOT_FOUND, "/404.html");
            ErrorPage error500Page = new ErrorPage(HttpStatus.INTERNAL_SERVER_ERROR, "/500.html");
            x.addErrorPages(error401Page, error404Page, error500Page);

        });
    }


}
