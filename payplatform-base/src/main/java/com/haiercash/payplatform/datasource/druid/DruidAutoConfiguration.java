package com.haiercash.payplatform.datasource.druid;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.XADataSourceAutoConfiguration;
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
@AutoConfigureBefore({DataSourceAutoConfiguration.class, XADataSourceAutoConfiguration.class})
public class DruidAutoConfiguration {
    @Bean
    @Primary
    @ConditionalOnMissingBean(name = "transactionManager")
    PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
