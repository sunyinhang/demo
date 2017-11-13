package com.haiercash.core.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

/**
 * Created by 许崇雷 on 2016/6/27.
 */
public final class StreamReader extends InputStreamReader {
    protected InputStream stream;

    /**
     * Creates an InputStreamReader that uses the default charset.
     *
     * @param in An InputStream
     */
    public StreamReader(InputStream in) {
        super(in);
        this.stream = in;
    }

    /**
     * Creates an InputStreamReader that uses the named charset.
     *
     * @param in          An InputStream
     * @param charsetName The name of a supported
     *                    {@link Charset </code>charset<code>}
     * @throws UnsupportedEncodingException If the named charset is not supported
     */
    public StreamReader(InputStream in, String charsetName) throws UnsupportedEncodingException {
        super(in, charsetName);
        this.stream = in;
    }

    /**
     * Creates an InputStreamReader that uses the given charset. </p>
     *
     * @param in An InputStream
     * @param cs A charset
     * @spec JSR-51
     * @since 1.4
     */
    public StreamReader(InputStream in, Charset cs) {
        super(in, cs);
        this.stream = in;
    }

    /**
     * Creates an InputStreamReader that uses the given charset decoder.  </p>
     *
     * @param in  An InputStream
     * @param dec A charset decoder
     * @spec JSR-51
     * @since 1.4
     */
    public StreamReader(InputStream in, CharsetDecoder dec) {
        super(in, dec);
        this.stream = in;
    }

    /**
     * 读取流中字符串 性能测试见:http://stackoverflow.com/questions/309424/read-convert-an-inputstream-to-a-string\
     *
     * @return
     * @throws IOException
     */
    public String readToEnd() throws IOException {
        if (this.stream == null)
            throw new IOException("流已关闭");

        int length;
        byte[] buffer = new byte[1024];
        try (ByteArrayOutputStream result = new ByteArrayOutputStream()) {
            while ((length = this.stream.read(buffer)) != -1)
                result.write(buffer, 0, length);
            return result.toString(this.getEncoding());
        }
    }

    /**
     * 关闭读取的流和阅读器
     *
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        super.close();
        if (this.stream != null) {
            this.stream.close();
            this.stream = null;
        }
    }
}
