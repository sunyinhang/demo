package com.haiercash.spring.rest.cmis.v2;

import com.haiercash.core.lang.BeanUtils;
import com.haiercash.core.lang.Convert;
import com.haiercash.core.serialization.JsonSerializer;
import com.haiercash.core.serialization.fastjson.StringObjectMapDeserializer;
import com.haiercash.spring.rest.cmis.ICmisResponse;
import com.haiercash.spring.trace.rest.ErrorHandler;

import java.lang.reflect.Type;
import java.util.HashMap;

/**
 * Created by 许崇雷 on 2018-01-09.
 */
public final class CmisResponse2<TBody> extends HashMap<String, Object> implements ICmisResponse<TBody> {
    static {
        JsonSerializer.getGlobalConfig().getParserConfig().putDeserializer(CmisResponse2.class, StringObjectMapDeserializer.instance);
    }

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
    public ICmisResponse<TBody> afterPropertiesSet(Type bodyType) {
        if (this.body == null) {
            this.put("retFlag", ErrorHandler.getRetFlag(this.getRetFlag()));
            this.body = BeanUtils.mapToBean(this, bodyType);
        }
        return this;
    }
}
