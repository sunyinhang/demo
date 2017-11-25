package com.haiercash.spring.mybatis;

import com.haiercash.core.lang.Convert;
import org.mybatis.mapper.MapperScannerConfigurer;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
    private static final String BASE_PACKAGE = "base-package";
    private static final String BASE_PACKAGE_ = "basePackage";
    private static final String UNDERSCORE_TO_CAMEL_CASE = "underscore-to-camel-case";
    private static final String UNDERSCORE_TO_CAMEL_CASE_ = "underscoreToCamelCase";
    private static final String SQL_SESSION_FACTORY_BEAN_NAME = "sqlSessionFactory";
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
        String basePackage = environment.getProperty(getPropertyName(BASE_PACKAGE));
        if (basePackage == null)
            basePackage = environment.getProperty(getPropertyName(BASE_PACKAGE_));
        String underscoreToCamelCase = environment.getProperty(getPropertyName(UNDERSCORE_TO_CAMEL_CASE));
        if (underscoreToCamelCase == null)
            underscoreToCamelCase = environment.getProperty(getPropertyName(UNDERSCORE_TO_CAMEL_CASE_));
        this.basePackage = basePackage;
        this.underscoreToCamelCase = Convert.defaultBoolean(underscoreToCamelCase, false);
    }

    @Bean
    @Primary
    @ConditionalOnProperty(prefix = PREFIX, name = BASE_PACKAGE)
    @ConditionalOnMissingBean
    MapperScannerConfigurer mapperScannerConfigurer() {
        MapperScannerConfigurer mapperScannerConfigurer = new MapperScannerConfigurer();
        mapperScannerConfigurer.setBasePackage(this.basePackage);
        mapperScannerConfigurer.setCamelhumpToUnderline(this.underscoreToCamelCase);
        mapperScannerConfigurer.setSqlSessionFactoryBeanName(SQL_SESSION_FACTORY_BEAN_NAME);
        return mapperScannerConfigurer;
    }
}
