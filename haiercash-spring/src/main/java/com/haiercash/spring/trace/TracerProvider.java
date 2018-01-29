package com.haiercash.spring.trace;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created by 许崇雷 on 2018-01-27.
 */
@Component
public final class TracerProvider {
    private static ITracer tracer;
    @Autowired
    private ITracer tracerInstance;

    private TracerProvider() {
    }

    public static ITracer getTracer() {
        return tracer;
    }

    @PostConstruct
    private void init() {
        tracer = this.tracerInstance;
    }
}
