package com.haiercash.spring.rest.cmisacq;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * Created by 许崇雷 on 2017-10-09.
 */
@Data
public final class CmisAcqResponseRoot<TBody> {
    @JSONField(ordinal = 1)
    private CmisAcqResponseHead head;

    @JSONField(ordinal = 2)
    private TBody body;
}
