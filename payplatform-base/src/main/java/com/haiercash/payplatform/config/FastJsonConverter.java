package com.haiercash.payplatform.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.serializer.ValueFilter;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import com.haiercash.commons.support.JSONObjectSerializer;
import org.apache.ibatis.utils.PaginationJsonSerializer;
import org.apache.ibatis.utils.PaginationList;
import org.json.JSONObject;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

/**
 * Created by 许崇雷 on 2017-09-15.
 */
@Configuration
@ConditionalOnClass({JSON.class, FastJsonHttpMessageConverter.class})
@ConditionalOnProperty(
        name = {"spring.http.converters.preferred-json-mapper"},
        havingValue = "fastjson-payplatform",
        matchIfMissing = true
)
public class FastJsonConverter extends FastJsonHttpMessageConverter {
    public FastJsonConverter() {
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
        fastJsonConfig.setDateFormat("yyyy-MM-dd HH:mm:ss");
        SerializeConfig.getGlobalInstance().put(PaginationList.class, new PaginationJsonSerializer());
        SerializeConfig.getGlobalInstance().put(JSONObject.class, new JSONObjectSerializer());
        this.setFastJsonConfig(fastJsonConfig);
    }
}
