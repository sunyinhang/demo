package com.haiercash.spring.rabbit;

import com.haiercash.core.lang.Cloneable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by 许崇雷 on 2017-11-01.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public final class RabbitInfo extends Cloneable<RabbitInfo> {
    private String exchange;
    private String routingKey;
    private String queue;
}
