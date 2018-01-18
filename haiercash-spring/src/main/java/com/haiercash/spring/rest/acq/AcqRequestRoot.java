package com.haiercash.spring.rest.acq;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * Created by 许崇雷 on 2017-11-29.
 */
@Data
public final class AcqRequestRoot {
    @JSONField(ordinal = 1)
    private AcqRequestHead head;

    @JSONField(ordinal = 2)
    private Object body;
}
