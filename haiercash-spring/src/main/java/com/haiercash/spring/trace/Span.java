package com.haiercash.spring.trace;

import com.haiercash.core.lang.Environment;
import com.haiercash.core.lang.StringUtils;
import org.springframework.util.Assert;

/**
 * Created by 许崇雷 on 2018-01-25.
 */
public final class Span implements ISpan {
    private static final String ROOT_SPAN_ID = StringUtils.EMPTY;
    private static final String ROOT_PARENT_SPAN_ID = StringUtils.EMPTY;
    private static final String SEPARATOR_SPAN_ID_INDEX = Environment.Dot;
    private static final String SEPARATOR_TRACE_ID_SPAN_ID = Environment.Dot;
    private final String traceId;
    private final String spanId;
    private final String parentSpanId;
    private int index;

    public Span(String traceId, String spanId, String parentSpanId) {
        Assert.hasLength(traceId, "traceId can not be empty");
        this.traceId = traceId;
        this.spanId = StringUtils.isEmpty(spanId) ? ROOT_SPAN_ID : spanId;
        this.parentSpanId = StringUtils.isEmpty(parentSpanId) ? ROOT_PARENT_SPAN_ID : parentSpanId;
    }

    @Override
    public ISpan continueSpan() {
        this.index++;
        return new Span(this.traceId, StringUtils.isEmpty(this.spanId) ? String.valueOf(this.index) : this.spanId + SEPARATOR_SPAN_ID_INDEX + this.index, this.spanId);
    }

    @Override
    public String getTraceId() {
        return this.traceId;
    }

    @Override
    public String getSpanId() {
        return this.spanId;
    }

    @Override
    public String getParentSpanId() {
        return this.parentSpanId;
    }

    @Override
    public String getTraceSpanId() {
        return StringUtils.isEmpty(this.spanId) ? this.traceId : this.traceId + SEPARATOR_TRACE_ID_SPAN_ID + this.spanId;
    }
}
