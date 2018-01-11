package com.haiercash.spring.client.converter;

import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import com.haiercash.core.serialization.JsonSerializer;
import com.haiercash.spring.support.JSONObjectSerializer;
import org.apache.ibatis.utils.PaginationJsonSerializer;
import org.apache.ibatis.utils.PaginationList;
import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.IOException;

/**
 * Created by 许崇雷 on 2017-09-27.
 */
public final class FastJsonHttpMessageConverterEx extends FastJsonHttpMessageConverter {
    static {
        SerializeConfig serializeConfig = JsonSerializer.getGlobalConfig().getSerializeConfig();
        serializeConfig.put(PaginationList.class, new PaginationJsonSerializer());
        serializeConfig.put(JSONObject.class, new JSONObjectSerializer());
    }

    public FastJsonHttpMessageConverterEx() {
        this.setFastJsonConfig(JsonSerializer.getGlobalConfig());
    }

    @Override
    protected void addDefaultHeaders(HttpHeaders headers, Object o, MediaType contentType) throws IOException {
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        super.addDefaultHeaders(headers, o, contentType);
    }
}
