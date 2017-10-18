package com.haiercash.payplatform.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.List;

/**
 * Created by 许崇雷 on 2017-10-18.
 */
@Data
@Configuration
@ConfigurationProperties
@PropertySource(value = "classpath:config/cashLoanConfig.yml")
public class CashLoanConfig {
    private List<String> tagIds;
}
