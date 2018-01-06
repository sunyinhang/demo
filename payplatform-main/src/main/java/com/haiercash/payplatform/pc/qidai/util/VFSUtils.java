package com.haiercash.payplatform.pc.qidai.util;


import com.haiercash.core.lang.Environment;
import com.haiercash.core.lang.StringUtils;

/**
 * Created by 许崇雷 on 2018-01-05.
 */
public final class VFSUtils {
    public static String ftp(String host, String username, String password, String fileName) {
        if (StringUtils.isEmpty(fileName))
            fileName = Environment.Slash;
        return String.format("ftp://%s:%s@%s%s", username, password, host, fileName.startsWith(Environment.Slash) ? fileName : (Environment.Slash + fileName));
    }

    public static String ftp(String host, int port, String username, String password, String fileName) {
        if (StringUtils.isEmpty(fileName))
            fileName = Environment.Slash;
        return String.format("ftp://%s:%s@%s:%d%s", username, password, host, port, fileName.startsWith(Environment.Slash) ? fileName : (Environment.Slash + fileName));
    }

    public static String sftp(String host, String username, String password, String fileName) {
        if (StringUtils.isEmpty(fileName))
            fileName = Environment.Slash;
        return String.format("sftp://%s:%s@%s%s", username, password, host, fileName.startsWith(Environment.Slash) ? fileName : (Environment.Slash + fileName));
    }

    public static String sftp(String host, int port, String username, String password, String fileName) {
        if (StringUtils.isEmpty(fileName))
            fileName = Environment.Slash;
        return String.format("sftp://%s:%s@%s:%d%s", username, password, host, port, fileName.startsWith(Environment.Slash) ? fileName : (Environment.Slash + fileName));
    }
}
