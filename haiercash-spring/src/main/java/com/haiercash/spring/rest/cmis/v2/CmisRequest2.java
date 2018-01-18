package com.haiercash.spring.rest.cmis.v2;

import com.haiercash.core.lang.Convert;
import com.haiercash.spring.rest.cmis.ICmisRequest;

import java.util.HashMap;

/**
 * Created by 许崇雷 on 2018-01-09.
 */
public final class CmisRequest2 extends HashMap<String, Object> implements ICmisRequest {
    @Override
    public String getSerNo() {
        return Convert.toString(this.get("serno"));
    }

    @Override
    public String getTradeCode() {
        return Convert.toString(this.get("tradeCode"));
    }
}
