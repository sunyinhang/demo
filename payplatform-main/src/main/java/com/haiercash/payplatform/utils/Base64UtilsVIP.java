package com.haiercash.payplatform.utils;


import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Random;


/** */

/**
 * <p>
 * BASE64编码解码工具包
 * </p>
 * <p>
 * 依赖javabase64-1.3.1.jar
 * </p>
 *
 * @author 苏扬
 * @version 1.0
 * @date 2015-6-26
 */
public class Base64UtilsVIP {

    /** */
    /**
     * 文件读取缓冲区大小
     */
    private static final int CACHE_SIZE = 1024;

    // 加密
    public static String DesEncrypt(String sSrc, String sKey, String ivs) throws Exception {
        byte[] raw = sKey.getBytes("utf-8");
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "DES");
        Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");//"算法/模式/补码方式"
        IvParameterSpec iv = new IvParameterSpec(ivs.getBytes());//CBC模式使用
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
        byte[] encrypted = cipher.doFinal(sSrc.getBytes("utf-8"));
        return new Base64().encodeToString(encrypted);//此处使用BASE64做转码功能，同时能起到2次加密的作用。

    }

    public static String productKey() {
        Random ran = new Random();
        return 10000000 + ran.nextInt(90000000) + "";
    }

} 
