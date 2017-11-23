package com.haiercash.core;

import com.haiercash.core.io.CharsetNames;
import org.junit.Test;

import java.io.UnsupportedEncodingException;

/**
 * Created by 许崇雷 on 2017-10-19.
 */
public class CountTest {
    @Test
    public void bytesCount() throws UnsupportedEncodingException {
        String text = "";
        byte[] bytes = text.getBytes(CharsetNames.UTF_8);
        System.out.println("字节数:" + bytes.length);
    }
}
