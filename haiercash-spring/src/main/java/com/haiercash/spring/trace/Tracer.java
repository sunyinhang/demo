package com.haiercash.spring.trace;

import com.haiercash.core.collection.ArrayUtils;
import com.haiercash.core.collection.iterator.CharSequenceIterable;
import com.haiercash.core.lang.CharUtils;
import com.haiercash.core.lang.Environment;
import com.haiercash.core.lang.StringUtils;
import com.haiercash.core.net.HostInfo;
import com.haiercash.core.time.DateUtils;
import com.haiercash.spring.boot.ApplicationUtils;
import com.haiercash.spring.context.RequestContext;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 调用链 Id 生成器
 * Created by 许崇雷 on 2017-10-14.
 */
public final class Tracer implements ITracer {
    private static final String TRACE_ID_SEPARATOR = Environment.Minus;
    private static final String DATE_FORMAT = "yyyyMMdd" + TRACE_ID_SEPARATOR + "HHmmss";
    private static final int APPLICATION_NAME_LENGTH = 3;
    private static final int TRACE_ID_MIN_LENGTH = 10;
    private static final int TRACE_ID_MAX_LENGTH = 50;
    private static final int SPAN_ID_MIN_LENGTH = 1;
    private static final int SPAN_ID_MAX_LENGTH = 50;
    private final String applicationName;
    private final String ipAddressLast;
    private final ISequence sequence;

    public Tracer(ISequence sequence) {
        Assert.notNull(sequence, "sequence can not be null");

        this.sequence = sequence;
        String appName = ApplicationUtils.getProperties().getName();
        this.applicationName = (appName.length() <= APPLICATION_NAME_LENGTH ? appName : appName.substring(0, APPLICATION_NAME_LENGTH)).toUpperCase();
        String[] ipAddresses = HostInfo.getIpAddress();
        if (ArrayUtils.isEmpty(ipAddresses)) {
            this.ipAddressLast = HostInfo.getHostName();
            return;
        }
        String ipAddress = ipAddresses[0];
        String[] bits = StringUtils.split(ipAddress, new char[]{Environment.DotChar}, true);
        this.ipAddressLast = bits[bits.length - 1];
    }

    //无效 traceId 返回 true,防止 header 注入
    private static boolean isInvalidTraceId(String traceId) {
        if (traceId == null || traceId.length() < TRACE_ID_MIN_LENGTH || traceId.length() > TRACE_ID_MAX_LENGTH)
            return true;
        for (Character ch : new CharSequenceIterable(traceId)) {
            if (CharUtils.isAsciiAlphanumeric(ch)
                    || ch.equals(Environment.DotChar)
                    || ch.equals(Environment.MinusChar)
                    || ch.equals(Environment.UnderlineChar))
                continue;
            return true;
        }
        return false;
    }

    //无效 spanId 返回 true,防止 header 注入
    private static boolean isInvalidSpanId(String spanId) {
        if (spanId == null || spanId.length() < SPAN_ID_MIN_LENGTH || spanId.length() > SPAN_ID_MAX_LENGTH)
            return true;
        for (Character ch : new CharSequenceIterable(spanId)) {
            if (CharUtils.isAsciiAlphanumeric(ch)
                    || ch.equals(Environment.DotChar)
                    || ch.equals(Environment.MinusChar)
                    || ch.equals(Environment.UnderlineChar))
                continue;
            return true;
        }
        return false;
    }

    //生成 traceId
    private String createTraceId() {
        @SuppressWarnings("StringBufferReplaceableByString")
        StringBuilder builder = new StringBuilder(50);
        builder.append(this.applicationName)//程序名前三位
                .append(TRACE_ID_SEPARATOR)
                .append(this.ipAddressLast)//IP 地址最后一个数字 0-255
                .append(TRACE_ID_SEPARATOR)
                .append(DateUtils.toString(DateUtils.now(), DATE_FORMAT))//年月日-时分秒毫秒
                .append(TRACE_ID_SEPARATOR)
                .append(this.sequence.getAndIncrement());//序号
        return builder.toString();
    }

    @Override
    public ISpan createSpan() {
        String traceId = null;
        String spanId = null;
        String parentSpanId = null;
        boolean requestContextExists = RequestContext.exists();
        if (requestContextExists) {
            HttpServletRequest request = RequestContext.getRequest();
            traceId = request.getHeader(TraceHeaders.TRACE_ID_NAME);
            spanId = request.getHeader(TraceHeaders.SPAN_ID_NAME);
            parentSpanId = request.getHeader(TraceHeaders.PARENT_SPAN_ID_NAME);
        }
        if (isInvalidTraceId(traceId))
            traceId = this.createTraceId();
        if (isInvalidSpanId(spanId))
            spanId = null;
        if (isInvalidSpanId(parentSpanId))
            parentSpanId = null;
        ISpan span = new Span(traceId, spanId, parentSpanId);
        if (requestContextExists) {
            HttpServletResponse response = RequestContext.getResponse();
            response.setHeader(TraceHeaders.TRACE_ID_NAME, span.getTraceId());
            response.setHeader(TraceHeaders.SPAN_ID_NAME, span.getSpanId());
            response.setHeader(TraceHeaders.PARENT_SPAN_ID_NAME, span.getParentSpanId());
        }
        return span;
    }

    @Override
    public ISpan createContinueSpan(ISpan span) {
        return span == null || span == ISpan.EMPTY ? this.createSpan() : span.continueSpan();
    }
}
