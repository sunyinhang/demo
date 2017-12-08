package com.haiercash.spring.rest.cmisacq;

import com.alibaba.fastjson.annotation.JSONField;
import com.haiercash.spring.rest.IRequest;
import lombok.Data;

/**
 * Created by 许崇雷 on 2017-11-29.
 */
@Data
public final class CmisAcqRequest implements IRequest {
    @JSONField(ordinal = 1)
    private CmisAcqRequestHead head;

    @JSONField(ordinal = 2)
    private Object body;

    CmisAcqRequest() {
    }
}
