package com.haiercash.spring.trace;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by 许崇雷 on 2018-01-27.
 */
@Configuration
public class TraceAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(ISequence.class)
    public ISequence sequence() {
        return new Sequence();
    }

    @Bean
    @ConditionalOnMissingBean(ITracer.class)
    public ITracer tracer(ISequence sequence) {
        return new Tracer(sequence);
    }
}
