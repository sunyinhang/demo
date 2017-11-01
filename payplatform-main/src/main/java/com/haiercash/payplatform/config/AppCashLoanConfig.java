package com.haiercash.payplatform.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

/**
 * Created by 许崇雷 on 2017-10-18.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.cashLoan")
public class AppCashLoanConfig {
    StringRedisTemplate redisTemplate;
    private List<String> whiteTagIds;
}
