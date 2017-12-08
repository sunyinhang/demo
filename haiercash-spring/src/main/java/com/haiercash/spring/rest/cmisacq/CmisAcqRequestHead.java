package com.haiercash.spring.rest.cmisacq;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * Created by 许崇雷 on 2017-11-29.
 */
@Data
public final class CmisAcqRequestHead {
    @JSONField(ordinal = 1)
    private String tradeCode;

    @JSONField(ordinal = 2)
    private String serno;

    @JSONField(ordinal = 3)
    private String sysFlag;

    @JSONField(ordinal = 4)
    private String tradeType;

    @JSONField(ordinal = 5)
    private String tradeDate;

    @JSONField(ordinal = 6)
    private String tradeTime;

    @JSONField(ordinal = 7)
    private String channelNo;

    @JSONField(ordinal = 8)
    private String cooprCode;

    CmisAcqRequestHead() {
    }
}
