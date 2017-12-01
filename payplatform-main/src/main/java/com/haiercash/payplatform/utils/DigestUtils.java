package com.haiercash.payplatform.utils;

import com.haiercash.core.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by 许崇雷 on 2017-12-01.
 */
public final class DigestUtils {
    private DigestUtils() {
    }

    //注意不要优化该代码, apache 对流的 md5 有 bug.必须转换为 byte[] 后才正确
    public static String md5(InputStream stream) {
        try {
            return org.apache.commons.codec.digest.DigestUtils.md5Hex(IOUtils.toByteArray(stream));
        } catch (IOException e) {
            throw new RuntimeException("md5 失败", e);
        }
    }

    public static String md5(String value) {
        return org.apache.commons.codec.digest.DigestUtils.md5Hex(value);
    }
}
