package com.haiercash.spring.rabbit;

import lombok.Data;

/**
 * Created by 许崇雷 on 2017-11-01.
 */
@Data
public final class RabbitInfo {
    private String exchange;
    private String routingKey;
    private String queue;
}
