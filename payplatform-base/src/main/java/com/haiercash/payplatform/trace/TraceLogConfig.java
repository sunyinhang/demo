package com.haiercash.payplatform.trace;

import com.bestvike.io.CharsetNames;

import java.nio.charset.Charset;

/**
 * Created by 许崇雷 on 2017-10-21.
 */
public final class TraceLogConfig {
    public static final int MAX_DISPLAY = 1024 * 6;//最大显示长度,必须小于缓冲区大小
    public static final int BUFFER_SIZE = 1024 * 8;//缓冲区大小
    public static final String BODY_PARSE_FAIL = "内容转换为字符串失败";
    public static final String BODY_OVER_FLOW = "(...内容过大，无法显示)";
    public static final Charset DEFAULT_CHARSET = Charset.forName(CharsetNames.UTF_8);
}
