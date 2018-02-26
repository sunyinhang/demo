package com.haiercash.spring.client.converter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import com.alibaba.fastjson.util.IOUtils;
import com.haiercash.core.reflect.ReflectionUtils;
import com.haiercash.core.serialization.JsonSerializer;
import com.haiercash.spring.support.JSONObjectSerializer;
import org.apache.ibatis.utils.PaginationJsonSerializer;
import org.apache.ibatis.utils.PaginationList;
import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

/**
 * Created by 许崇雷 on 2017-09-27.
 */
public final class FastJsonHttpMessageConverterEx extends FastJsonHttpMessageConverter {
    private static final Method ALLOCATE_BYTES_METHOD = ReflectionUtils.getMethodInfo(JSON.class, "allocateBytes", new Class<?>[]{int.class}, true);
    private static final Method ALLOCATE_CHARS_METHOD = ReflectionUtils.getMethodInfo(JSON.class, "allocateChars", new Class<?>[]{int.class}, true);

    static {
        SerializeConfig serializeConfig = JsonSerializer.getGlobalConfig().getSerializeConfig();
        serializeConfig.put(PaginationList.class, new PaginationJsonSerializer());
        serializeConfig.put(JSONObject.class, new JSONObjectSerializer());
    }

    public FastJsonHttpMessageConverterEx() {
        this.setFastJsonConfig(JsonSerializer.getGlobalConfig());
    }

    private static String readStream(InputStream is, Charset charset) throws IOException, InvocationTargetException, IllegalAccessException {
        //read bytes
        byte[] bytes = (byte[]) ALLOCATE_BYTES_METHOD.invoke(null, 1024 * 64);
        int offset = 0;
        int readCount;
        while ((readCount = is.read(bytes, offset, bytes.length - offset)) != -1) {
            offset += readCount;
            if (offset == bytes.length) {
                byte[] newBytes = new byte[bytes.length * 3 / 2];
                System.arraycopy(bytes, 0, newBytes, 0, bytes.length);
                bytes = newBytes;
            }
        }

        //decode
        if (charset == null)
            charset = IOUtils.UTF8;
        if (charset == IOUtils.UTF8) {
            char[] chars = (char[]) ALLOCATE_CHARS_METHOD.invoke(null, bytes.length);
            int chars_len = IOUtils.decodeUTF8(bytes, 0, offset, chars);
            return chars_len < 0 ? null : new String(chars, 0, chars_len);
        }
        return new String(bytes, 0, offset, charset);
    }

    private Object readType(Type type, HttpInputMessage inputMessage) {
        try {
            FastJsonConfig fastJsonConfig = this.getFastJsonConfig();
            String json = readStream(inputMessage.getBody(), fastJsonConfig.getCharset());
            return JSON.parseObject(json, type, fastJsonConfig.getParserConfig(), null, Feature.of(fastJsonConfig.getFeatures()), fastJsonConfig.getFeatures());
        } catch (JSONException ex) {
            throw new HttpMessageNotReadableException("JSON parse error: " + ex.getMessage(), ex);
        } catch (IOException ex) {
            throw new HttpMessageNotReadableException("I/O error while reading input message", ex);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            throw new HttpMessageNotReadableException("Invoke method error by reflect", ex);
        }
    }

    @Override
    public Object read(Type type, Class<?> contextClass, HttpInputMessage inputMessage) throws HttpMessageNotReadableException {
        return this.readType(this.getType(type, contextClass), inputMessage);
    }

    @Override
    protected Object readInternal(Class<?> clazz, HttpInputMessage inputMessage) throws HttpMessageNotReadableException {
        return this.readType(this.getType(clazz, null), inputMessage);
    }

    @Override
    protected void addDefaultHeaders(HttpHeaders headers, Object o, MediaType contentType) throws IOException {
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        super.addDefaultHeaders(headers, o, contentType);
    }
}
