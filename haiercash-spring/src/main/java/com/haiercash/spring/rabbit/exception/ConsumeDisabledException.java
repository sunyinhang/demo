package com.haiercash.spring.rabbit.exception;

import com.haiercash.core.threading.ThreadUtils;

/**
 * Created by 许崇雷 on 2017-12-01.
 */
public final class ConsumeDisabledException extends RuntimeException {
    public static final String MESSAGE = "该队列未启用消费";
    private static final int DELAY_MILLIS = 20 * 1000;

    public ConsumeDisabledException() {
        super(MESSAGE);
        ThreadUtils.sleep(DELAY_MILLIS);
    }
}
