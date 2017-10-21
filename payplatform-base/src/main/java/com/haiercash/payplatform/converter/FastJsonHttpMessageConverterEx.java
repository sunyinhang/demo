package com.haiercash.payplatform.converter;

import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.serializer.ValueFilter;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import com.haiercash.commons.support.JSONObjectSerializer;
import com.haiercash.payplatform.config.HttpMessageConvertersAutoConfiguration;
import org.apache.ibatis.utils.PaginationJsonSerializer;
import org.apache.ibatis.utils.PaginationList;
import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * Created by 许崇雷 on 2017-09-27.
 */
public final class FastJsonHttpMessageConverterEx extends FastJsonHttpMessageConverter {
    public FastJsonHttpMessageConverterEx() {
        FastJsonConfig fastJsonConfig = new FastJsonConfig();
        fastJsonConfig.setSerializerFeatures(SerializerFeature.WriteMapNullValue);
        ValueFilter valueFilter = (object, name, value) -> {
            if (value == null)
                return null;
            String type = value.getClass().getSimpleName();
            switch (type) {
                case "BigDecimal":
                    BigDecimal valueD = (BigDecimal) value;
                    if (valueD.compareTo(BigDecimal.valueOf(1.0E16D)) >= 0)
                        return String.valueOf(value);
                    break;
                case "Integer":
                    Integer valueN = (Integer) value;
                    if (valueN > 1000000000)
                        return String.valueOf(value);
                    break;
                case "Long":
                    Long valueL = (Long) value;
                    if (valueL > 1000000000L)
                        return String.valueOf(value);
                    break;
                default:
                    break;
            }
            return value;
        };
        fastJsonConfig.setSerializeFilters(valueFilter);
        fastJsonConfig.setDateFormat(HttpMessageConvertersAutoConfiguration.DATE_FORMAT);
        SerializeConfig.getGlobalInstance().put(PaginationList.class, new PaginationJsonSerializer());
        SerializeConfig.getGlobalInstance().put(JSONObject.class, new JSONObjectSerializer());
        this.setFastJsonConfig(fastJsonConfig);
    }

    @Override
    protected void addDefaultHeaders(HttpHeaders headers, Object o, MediaType contentType) throws IOException {
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        super.addDefaultHeaders(headers, o, contentType);
    }
}
