package com.haiercash.spring.trace;

/**
 * Created by 许崇雷 on 2017-12-27.
 */
public final class TracerUtils {
    private static ITracer getTracer() {
        return TracerProvider.getTracer();
    }

    public static ISpan createSpan() {
        return getTracer().createSpan();
    }

    public static ISpan createContinueSpan(ISpan span) {
        return getTracer().createContinueSpan(span);
    }
}
