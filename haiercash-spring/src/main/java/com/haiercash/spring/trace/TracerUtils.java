package com.haiercash.spring.trace;

/**
 * Created by 许崇雷 on 2017-12-27.
 */
public final class TracerUtils {
    private static ITracer getTracer() {
        return TracerProvider.getTracer();
    }

    public static ISpan createSpan() {
        ITracer tracer = getTracer();
        return tracer == null ? ISpan.EMPTY : tracer.createSpan();
    }

    public static ISpan createContinueSpan(ISpan span) {
        ITracer tracer = getTracer();
        return tracer == null ? ISpan.EMPTY : tracer.createContinueSpan(span);
    }
}
