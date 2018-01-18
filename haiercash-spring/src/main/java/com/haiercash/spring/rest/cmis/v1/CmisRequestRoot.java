package com.haiercash.spring.rest.cmis.v1;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * Created by 许崇雷 on 2017-11-29.
 */
@Data
public final class CmisRequestRoot {
    @JSONField(ordinal = 1)
    private CmisRequestHead head;

    @JSONField(ordinal = 2)
    private Object body;
}
