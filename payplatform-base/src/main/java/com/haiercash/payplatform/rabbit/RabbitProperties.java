package com.haiercash.payplatform.rabbit;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Created by 许崇雷 on 2017-11-06.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "spring.rabbitmq")
public class RabbitProperties {
}
