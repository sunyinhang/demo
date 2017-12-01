package com.haiercash.spring.rabbit.exception;

import com.haiercash.core.threading.ThreadUtils;

/**
 * Created by 许崇雷 on 2017-12-01.
 */
public class ConsumeDisabledException extends RuntimeException {
    public static final String MSG = "该队列未启用消费";
    private static final int DELAY_MILLS = 20 * 1000;

    public ConsumeDisabledException() {
        super(MSG);
        ThreadUtils.sleep(DELAY_MILLS);
    }
}
