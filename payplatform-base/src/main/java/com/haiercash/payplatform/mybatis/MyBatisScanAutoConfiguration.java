package com.haiercash.payplatform.mybatis;

import com.bestvike.lang.Convert;
import org.mybatis.mapper.MapperScannerConfigurer;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;

/**
 * Created by 许崇雷 on 2017-11-07.
 */
@Configuration
public class MyBatisScanAutoConfiguration implements ApplicationContextAware {
    private static final String PREFIX = "spring.mybatis";
    private String basePackage;
    private Boolean underscoreToCamelCase;

    private static String getPropertyName(String name) {
        return PREFIX + "." + name;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (applicationContext == null)
            return;
        Environment environment = applicationContext.getEnvironment();
        String basePackage = environment.getProperty(getPropertyName("base-package"));
        if (basePackage == null)
            basePackage = environment.getProperty(getPropertyName("basePackage"));
        String underscoreToCamelCase = environment.getProperty(getPropertyName("underscore-to-camel-case"));
        if (underscoreToCamelCase == null)
            underscoreToCamelCase = environment.getProperty(getPropertyName("underscoreToCamelCase"));
        this.basePackage = basePackage;
        this.underscoreToCamelCase = Convert.defaultBoolean(underscoreToCamelCase, false);
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean
    MapperScannerConfigurer mapperScannerConfigurer() {
        MapperScannerConfigurer mapperScannerConfigurer = new MapperScannerConfigurer();
        mapperScannerConfigurer.setBasePackage(this.basePackage);
        mapperScannerConfigurer.setCamelhumpToUnderline(this.underscoreToCamelCase);
        mapperScannerConfigurer.setSqlSessionFactoryBeanName("sqlSessionFactory");
        return mapperScannerConfigurer;
    }
}
