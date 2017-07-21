package com.haiercash.common.util;

import java.util.Random;

/**
 * 随机数生成工具类
 *
 * @date 2016/12/19
 */
public class RandomCodeUtil {

    /**
     * 生成n位随机数
     *
     * @return
     */
    public static String getRandomNumber(int length) {
        String val = "";
        Random random = new Random();

        for (int i = 0; i < length; ++i) {
            val = val + String.valueOf(random.nextInt(10));
        }
        return val;
    }
}
