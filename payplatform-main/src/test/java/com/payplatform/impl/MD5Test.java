package com.payplatform.impl;

import com.haiercash.core.io.IOUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Created by yuanli on 2017/12/1.
 */
public class MD5Test {

    @Test
    public  void  testMd5() throws IOException {
        StringBuilder stringBuffer = new StringBuilder();
        for(int i=0;i<10000;i++)
            stringBuffer.append("11111111111两节课的减肥了肯德基法律框架");
        byte[] buf=stringBuffer.toString().getBytes(StandardCharsets.UTF_8);
        InputStream stream=new ByteArrayInputStream(buf);
        String MD5 = DigestUtils.md5Hex(stream);
        String md2=DigestUtils.md5Hex(buf);
        Assert.assertEquals(md2, MD5);
    }


    @Test
    public  void  testMd5_2() throws IOException {
        InputStream stream=new FileInputStream(new File("C:/1.jpg"));
        byte[] buf= IOUtils.toByteArray(stream);
        stream=new ByteArrayInputStream(buf);//
        String MD5 = DigestUtils.md5Hex(stream);
        String md2=DigestUtils.md5Hex(buf);
        Assert.assertEquals(md2, MD5);
    }


}
