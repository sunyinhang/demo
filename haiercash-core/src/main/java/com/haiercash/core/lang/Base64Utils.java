package com.haiercash.core.lang;

import com.haiercash.core.io.CharsetNames;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.io.UnsupportedEncodingException;

/**
 * base64 编码/解码
 *
 * @author 许崇雷
 * @date 2017/7/16
 */
public final class Base64Utils {
    private static final ThreadLocal<BASE64Encoder> BASE64_ENCODER = ThreadLocal.withInitial(BASE64Encoder::new);
    private static final ThreadLocal<BASE64Decoder> BASE64_DECODER = ThreadLocal.withInitial(BASE64Decoder::new);

    /**
     * base64 编码
     *
     * @param value 原始数据
     * @return base64 字符串
     */
    public static String encode(byte[] value) {
        return StringUtils.stripNewLine(BASE64_ENCODER.get().encode(value));
    }

    /**
     * base64 解码
     *
     * @param value base64 字符串
     * @return 原始数据
     */
    public static byte[] decode(String value) {
        try {
            return BASE64_DECODER.get().decodeBuffer(value);
        } catch (Exception e) {
            throw new RuntimeException("指定的字符串不能用 base64 解码", e);
        }
    }

    /**
     * 对字符串 base64 编码
     *
     * @param value 原始字符串,utf-8 编码
     * @return base64 字符串
     */
    public static String encodeString(String value) {
        return encodeString(value, CharsetNames.UTF_8);
    }

    /**
     * 对字符串 base64 编码
     *
     * @param value       原始字符串
     * @param charsetName 原始字符串编码
     * @return base64 字符串
     */
    public static String encodeString(String value, String charsetName) {
        try {
            return encode(value.getBytes(charsetName));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("不支持的编码", e);
        }
    }

    /**
     * base64 解码并转为字符串
     *
     * @param value base64 字符串
     * @return 原始字符串, utf-8 编码
     */
    public static String decodeString(String value) {
        return decodeString(value, CharsetNames.UTF_8);
    }

    /**
     * base64 解码并转为字符串
     *
     * @param value       base64 字符串
     * @param charsetName 原始字符串编码
     * @return 原始字符串
     */
    public static String decodeString(String value, String charsetName) {
        try {
            return new String(decode(value), charsetName);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("不支持的编码", e);
        }
    }
}
