package com.bestvike.io;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

/**
 * Created by 许崇雷 on 2016/6/27.
 */
public final class StreamWriter extends OutputStreamWriter {
    /**
     * Creates an OutputStreamWriter that uses the named charset.
     *
     * @param out         An OutputStream
     * @param charsetName The name of a supported
     *                    {@link Charset </code>charset<code>}
     * @throws UnsupportedEncodingException If the named encoding is not supported
     */
    public StreamWriter(OutputStream out, String charsetName) throws UnsupportedEncodingException {
        super(out, charsetName);
    }

    /**
     * Creates an OutputStreamWriter that uses the default character encoding.
     *
     * @param out An OutputStream
     */
    public StreamWriter(OutputStream out) {
        super(out);
    }

    /**
     * Creates an OutputStreamWriter that uses the given charset. </p>
     *
     * @param out An OutputStream
     * @param cs  A charset
     * @spec JSR-51
     * @since 1.4
     */
    public StreamWriter(OutputStream out, Charset cs) {
        super(out, cs);
    }

    /**
     * Creates an OutputStreamWriter that uses the given charset encoder.  </p>
     *
     * @param out An OutputStream
     * @param enc A charset encoder
     * @spec JSR-51
     * @since 1.4
     */
    public StreamWriter(OutputStream out, CharsetEncoder enc) {
        super(out, enc);
    }
}
