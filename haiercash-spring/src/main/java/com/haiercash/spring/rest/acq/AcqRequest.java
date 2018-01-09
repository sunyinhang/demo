package com.haiercash.spring.rest.acq;

import com.alibaba.fastjson.annotation.JSONField;
import com.haiercash.spring.rest.IRequest;
import lombok.Data;

/**
 * Created by 许崇雷 on 2018-01-08.
 */
@Data
public final class AcqRequest implements IRequest {
    @JSONField(ordinal = 1)
    private AcqRequestRoot request;
}
