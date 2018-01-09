package com.haiercash.spring.rest.cmis.v1;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * Created by 许崇雷 on 2017-11-29.
 */
@Data
public final class CmisRequestHead {
    @JSONField(ordinal = 1)
    private String serno;

    @JSONField(ordinal = 2)
    private String tradeCode;

    @JSONField(ordinal = 3)
    private String tradeDate;

    @JSONField(ordinal = 4)
    private String tradeTime;

    @JSONField(ordinal = 5)
    private String tradeType;

    @JSONField(ordinal = 6)
    private String sysFlag;

    @JSONField(ordinal = 7)
    private String channelNo;

    @JSONField(ordinal = 8)
    private String cooprCode;
}
