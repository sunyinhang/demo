package com.haiercash.spring.converter;

import com.haiercash.core.serialization.XmlSerializer;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;

/**
 * Created by 许崇雷 on 2018-01-10.
 */
public final class JacksonXmlHttpMessageConverterEx extends MappingJackson2XmlHttpMessageConverter {
    public JacksonXmlHttpMessageConverterEx() {
        super(XmlSerializer.getXmlMapper());
    }
}
