package com.haiercash.spring.rest.cmis.v1;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * Created by 许崇雷 on 2017-10-09.
 */
@Data
public final class CmisResponseRoot<TBody> {
    @JSONField(ordinal = 1)
    private CmisResponseHead head;

    @JSONField(ordinal = 2)
    private TBody body;

    CmisResponseRoot() {
    }
}
