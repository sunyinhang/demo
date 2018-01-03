package com.haiercash.payplatform.common.data;

import lombok.Data;

/**
 * progress model.
 *
 * @author Qingxiang.Liu
 * @since v1.3.0
 */
@Data
public class FlowNodeLog {
    private String name;
    private String idCard;
    private String flow;
    private String node;
}
