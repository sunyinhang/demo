package com.haiercash.spring.trace;

import com.haiercash.core.io.CharsetNames;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Created by 许崇雷 on 2017-10-21.
 */
public final class TraceConfig {
    public static final int DISPLAY_SIZE = 1024 * 15;//最大显示字节数,必须小于缓冲区
    public static final int BUFFER_SIZE = 1024 * 16;//缓冲区字节数
    public static final String HEADER_SEPARATOR = "; ";
    public static final String BODY_PARSE_FAIL = "内容转换为字符串失败";
    public static final String BODY_OVER_FLOW = "(...内容过大，无法显示)";
    public static final String BODY_RESOURCE = "(...资源文件，无法显示)";
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    public static final String DEFAULT_CHARSET_NAME = CharsetNames.UTF_8;
    public static final ThreadLocal<byte[]> BUFFER = ThreadLocal.withInitial(() -> new byte[BUFFER_SIZE]);
}
