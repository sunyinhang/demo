package com.haiercash.spring.datasource;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * Created by 许崇雷 on 2017-11-06.
 */
@Configuration
@AutoConfigureAfter(DruidDataSourceAutoConfigure.class)
public class DataSourceAutoConfiguration {
    @Bean
    @Primary
    @ConditionalOnMissingBean(name = "transactionManager")
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
