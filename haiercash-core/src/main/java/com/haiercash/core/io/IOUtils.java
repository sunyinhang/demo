package com.haiercash.core.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by 许崇雷 on 2017-10-21.
 */
public final class IOUtils extends org.apache.commons.io.IOUtils {
    public static int read(InputStream inputStream, byte[] buffer) throws IOException {
        int length = buffer.length;
        int count = 0;
        int readed;
        while ((readed = inputStream.read(buffer, count, length - count)) > 0)
            count += readed;
        return count;
    }
}
