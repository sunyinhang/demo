package com.haiercash.spring.trace;

/**
 * Created by 许崇雷 on 2018-01-27.
 */
public interface ITracer {
    ISpan createSpan();

    ISpan createContinueSpan(ISpan span);
}
