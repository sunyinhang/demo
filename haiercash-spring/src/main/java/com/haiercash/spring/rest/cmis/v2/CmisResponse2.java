package com.haiercash.spring.rest.cmis.v2;

import com.haiercash.core.lang.BeanUtils;
import com.haiercash.core.lang.Convert;
import com.haiercash.spring.rest.cmis.ICmisResponse;
import com.haiercash.spring.trace.rest.ErrorHandler;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by 许崇雷 on 2018-01-09.
 */
public final class CmisResponse2<TBody> extends HashMap<String, Object> implements ICmisResponse<TBody> {
    private TBody body;

    @Override
    public String getSerNo() {
        return Convert.toString(this.get("serno"));
    }

    @Override
    public String getRetFlag() {
        return Convert.toString(this.get("retFlag"));
    }

    @Override
    public String getRetMsg() {
        return Convert.toString(this.get("retMsg"));
    }

    @Override
    public Object getHead() {
        return this;
    }

    @Override
    public TBody getBody() {
        return this.body;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ICmisResponse<TBody> afterPropertiesSet(Type bodyType) {
        if (this.body == null) {
            this.put("retFlag", ErrorHandler.getRetFlag(this.getRetFlag()));
            if (bodyType instanceof ParameterizedType)
                bodyType = ((ParameterizedType) bodyType).getRawType();
            this.body = bodyType == Map.class || bodyType == HashMap.class ? (TBody) this : BeanUtils.mapToBean(this, bodyType);
        }
        return this;
    }
}
