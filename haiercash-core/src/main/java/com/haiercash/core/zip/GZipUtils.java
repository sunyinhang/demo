package com.haiercash.core.zip;

import com.haiercash.core.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created by 许崇雷 on 2018-01-12.
 */
public final class GZipUtils {
    private GZipUtils() {
    }

    public static byte[] compress(byte[] buffer) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(outputStream)) {
                gzipOutputStream.write(buffer);
            }
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] decompress(byte[] bytes) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
             GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream)) {
            return IOUtils.toByteArray(gzipInputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
