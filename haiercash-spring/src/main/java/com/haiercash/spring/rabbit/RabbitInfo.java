package com.haiercash.spring.rabbit;

import com.haiercash.core.lang.Cloneable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Created by 许崇雷 on 2017-11-01.
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public final class RabbitInfo extends Cloneable<RabbitInfo> {
    private String exchange;
    private String routingKey;
    private String queue;

    public RabbitInfo(String exchange, String routingKey) {
        this.exchange = exchange;
        this.routingKey = routingKey;
    }
}
