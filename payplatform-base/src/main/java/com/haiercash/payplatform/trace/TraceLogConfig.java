package com.haiercash.payplatform.trace;

import com.bestvike.io.CharsetNames;

import java.nio.charset.Charset;

/**
 * Created by 许崇雷 on 2017-10-21.
 */
public final class TraceLogConfig {
    public static final int MAX_DISPLAY = 1024 * 15;//最大显示字节数,必须小于缓冲区
    public static final int BUFFER_SIZE = 1024 * 16;//缓冲区字节数
    public static final String BODY_PARSE_FAIL = "内容转换为字符串失败";
    public static final String BODY_OVER_FLOW = "(...内容过大，无法显示)";
    public static final Charset DEFAULT_CHARSET = Charset.forName(CharsetNames.UTF_8);
    public static final ThreadLocal<byte[]> BUFFER = ThreadLocal.withInitial(() -> new byte[BUFFER_SIZE]);
}
