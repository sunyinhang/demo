package com.haiercash.spring.trace;

import com.haiercash.spring.boot.ApplicationProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

/**
 * Created by 许崇雷 on 2018-01-27.
 */
@Configuration
public class TraceAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public ISequence sequence() {
        return new Sequence();
    }

    @Bean
    @DependsOn(ApplicationProvider.BEAN_NAME)
    @ConditionalOnMissingBean
    public ITracer tracer(ISequence sequence) {
        return new Tracer(sequence);
    }
}
