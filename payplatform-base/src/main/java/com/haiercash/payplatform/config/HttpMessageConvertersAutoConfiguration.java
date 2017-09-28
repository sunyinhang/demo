package com.haiercash.payplatform.config;

/**
 * Created by 许崇雷 on 2017-09-27.
 */
public class HttpMessageConvertersAutoConfiguration {
    public static final String PREFERRED_MAPPER_PROPERTY = "spring.http.converters.preferred-json-mapper";
    public static final String PREFERRED_MAPPER_PROPERTY_JACKSON = "jackson";
    public static final String PREFERRED_MAPPER_PROPERTY_GSON = "gson";
    public static final String PREFERRED_MAPPER_PROPERTY_FASTJSON = "fastjson";

    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
}
