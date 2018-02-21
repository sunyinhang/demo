package com.haiercash.spring.trace;

import com.haiercash.core.lang.StringUtils;

/**
 * Created by 许崇雷 on 2018-01-25.
 */
public interface ISpan {
    ISpan EMPTY = new ISpan() {
        @Override
        public ISpan continueSpan() {
            return this;
        }

        @Override
        public String getTraceId() {
            return StringUtils.EMPTY;
        }

        @Override
        public String getSpanId() {
            return StringUtils.EMPTY;
        }

        @Override
        public String getParentSpanId() {
            return StringUtils.EMPTY;
        }

        @Override
        public String getTraceSpanId() {
            return StringUtils.EMPTY;
        }
    };

    ISpan continueSpan();

    String getTraceId();

    String getSpanId();

    String getParentSpanId();

    String getTraceSpanId();
}
