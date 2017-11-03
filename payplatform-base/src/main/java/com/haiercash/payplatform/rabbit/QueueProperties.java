package com.haiercash.payplatform.rabbit;

import lombok.Data;

/**
 * Created by 许崇雷 on 2017-11-01.
 */
@Data
public class QueueProperties {
    private String exchange;
    private String routingKey;
    private String queue;
}
