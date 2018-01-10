package com.haiercash.spring.client;

import com.haiercash.core.lang.StringUtils;
import com.haiercash.spring.client.converter.HttpMessageConvertersAutoConfiguration;
import com.haiercash.spring.converter.FastJsonHttpMessageConverterEx;
import com.haiercash.spring.converter.JacksonXmlHttpMessageConverterEx;
import com.haiercash.spring.converter.StringHttpMessageConverterEx;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.http.converter.xml.SourceHttpMessageConverter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 许崇雷 on 2017-11-06.
 */
@Data
@ConfigurationProperties(prefix = "spring.http.converters")
public final class HttpConvertersProperties {
    public static final String PREFERRED_JSON_MAPPER_PROPERTY = "spring.http.converters.preferredJsonMapper";
    private String preferredJsonMapper;
    private String preferredXmlMapper;

    public void config(RestTemplateEx restTemplate) {
        switch (restTemplate.supportedType) {
            case JSON:
                restTemplate.setMessageConverters(this.createJsonConverters());
                break;
            case XML:
                restTemplate.setMessageConverters(this.createXmlConverters());
                break;
            default:
                throw new RuntimeException("Unexpected supportedType of RestTemplateEx");
        }
    }

    //创建数据转换器 Json
    private List<HttpMessageConverter<?>> createJsonConverters() {
        List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
        messageConverters.add(new ByteArrayHttpMessageConverter());
        messageConverters.add(new StringHttpMessageConverterEx(MediaType.APPLICATION_JSON_UTF8));
        messageConverters.add(new ResourceHttpMessageConverter());
        messageConverters.add(new SourceHttpMessageConverter());
        messageConverters.add(new AllEncompassingFormHttpMessageConverter());
        String preferredJsonMapper = this.preferredJsonMapper == null ? StringUtils.EMPTY : this.preferredJsonMapper.toLowerCase();
        switch (preferredJsonMapper) {
            case HttpMessageConvertersAutoConfiguration.PREFERRED_JSON_MAPPER_JACKSON:
                messageConverters.add(new MappingJackson2HttpMessageConverter());
                break;
            case HttpMessageConvertersAutoConfiguration.PREFERRED_JSON_MAPPER_GSON:
                messageConverters.add(new GsonHttpMessageConverter());
                break;
            case HttpMessageConvertersAutoConfiguration.PREFERRED_JSON_MAPPER_FASTJSON:
                messageConverters.add(new FastJsonHttpMessageConverterEx());
                break;
            default:
                messageConverters.add(new MappingJackson2HttpMessageConverter());
                break;
        }
        return messageConverters;
    }

    //创建数据转换器 Xml
    private List<HttpMessageConverter<?>> createXmlConverters() {
        List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
        messageConverters.add(new ByteArrayHttpMessageConverter());
        messageConverters.add(new StringHttpMessageConverterEx(MediaType.APPLICATION_XML));
        messageConverters.add(new ResourceHttpMessageConverter());
        messageConverters.add(new SourceHttpMessageConverter());
        messageConverters.add(new AllEncompassingFormHttpMessageConverter());
        String preferredXmlMapper = this.preferredXmlMapper == null ? StringUtils.EMPTY : this.preferredXmlMapper.toLowerCase();
        switch (preferredXmlMapper) {
            case HttpMessageConvertersAutoConfiguration.PREFERRED_XML_MAPPER_JACKSON:
                messageConverters.add(new JacksonXmlHttpMessageConverterEx());
                break;
            case HttpMessageConvertersAutoConfiguration.PREFERRED_XML_MAPPER_JAXB:
                messageConverters.add(new Jaxb2RootElementHttpMessageConverter());
                break;
            default:
                messageConverters.add(new JacksonXmlHttpMessageConverterEx());
                break;
        }
        return messageConverters;
    }
}
