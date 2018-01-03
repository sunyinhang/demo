package com.haiercash.payplatform.common.data;

import lombok.Data;

/**
 * progress model.
 * @author Qingxiang.Liu
 * @since v1.3.0
 */
@Data
public class ProgressLog {

    private String name;
    private String idCard;
    private String progress;
    private String node;
    private String nextNode;
}
