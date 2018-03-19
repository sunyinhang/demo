package com.haiercash.spring.rest.cmis.v2;

import com.bestvike.linq.Linq;
import com.haiercash.core.reflect.GenericType;
import com.haiercash.core.serialization.JsonSerializer;
import com.haiercash.spring.client.converter.FastJsonHttpMessageConverterEx;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Created by 许崇雷 on 2018-01-11.
 */
public class CmisResponse2Test {
    private static final String TEST_JSON = "{\"retFlag\":\"00000\"}";
    private static final Type TYPE = new GenericType<CmisResponse2<Map<String, Object>>>() {
    };

    private static void testConverter(HttpMessageConverter converter) throws IOException {
        CmisResponse2<Object> origin = new CmisResponse2<>();
        origin.put("retFlag", "00000");
        //fastjson 转换器
        byte[] bytes = TEST_JSON.getBytes(StandardCharsets.UTF_8);
        HttpHeaders headers = new HttpHeaders();
        //
        OutputStream outputStream = new ByteArrayOutputStream();
        HttpOutputMessage outputMessage = new HttpOutputMessage() {
            @Override
            public OutputStream getBody() {
                return outputStream;
            }

            @Override
            public HttpHeaders getHeaders() {
                return headers;
            }
        };
        converter.write(origin, MediaType.APPLICATION_JSON_UTF8, outputMessage);
        byte[] bytes2 = ((ByteArrayOutputStream) outputMessage.getBody()).toByteArray();
        Assert.assertTrue(Linq.asEnumerable(bytes).sequenceEqual(Linq.asEnumerable(bytes2)));
        //
        InputStream inputStream = new ByteArrayInputStream(bytes);
        HttpInputMessage inputMessage = new HttpInputMessage() {
            @Override
            public InputStream getBody() {
                return inputStream;
            }

            @Override
            public HttpHeaders getHeaders() {
                return headers;
            }
        };
        CmisResponse2<Map> res2 = (CmisResponse2<Map>) converter.read(CmisResponse2.class, inputMessage);
        Assert.assertEquals("00000", res2.getRetFlag());
    }

    @Test
    public void test() throws IOException {
        //序列化
        CmisResponse2<Object> origin = new CmisResponse2<>();
        origin.put("retFlag", "00000");
        String json = JsonSerializer.serialize(origin);
        Assert.assertEquals(TEST_JSON, json);
        //反序列化
        CmisResponse2<Object> res = JsonSerializer.deserialize(TEST_JSON, TYPE);
        Assert.assertEquals("00000", res.getRetFlag());
        //fastjson 转换器
        testConverter(new FastJsonHttpMessageConverterEx());
        testConverter(new MappingJackson2HttpMessageConverter());
    }
}
