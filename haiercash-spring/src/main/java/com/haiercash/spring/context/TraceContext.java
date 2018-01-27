package com.haiercash.spring.context;

import com.haiercash.spring.trace.ISpan;
import com.haiercash.spring.trace.TraceHeaders;
import com.haiercash.spring.trace.TracerUtils;
import org.slf4j.MDC;

import java.util.Stack;

/**
 * Created by 许崇雷 on 2018-01-26.
 */
public final class TraceContext {
    //线程本地存储
    private static final ThreadLocal<Stack<ISpan>> contexts = ThreadLocal.withInitial(Stack::new);

    //region property

    public static boolean exists() {
        return !contexts.get().isEmpty();
    }

    private static ISpan getSpan() {
        Stack<ISpan> stack = contexts.get();
        return stack.isEmpty() ? ISpan.EMPTY : stack.peek();
    }

    public static String getTraceId() {
        return getSpan().getTraceId();
    }

    public static String getSpanId() {
        return getSpan().getSpanId();
    }

    public static String getParentSpanId() {
        return getSpan().getParentSpanId();
    }

    public static String getTraceSpanId() {
        return getSpan().getTraceSpanId();
    }

    //endregion

    public static void init() {
        ISpan span = TracerUtils.createSpan();
        Stack<ISpan> stack = contexts.get();
        stack.clear();
        stack.push(span);
        MDC.put(TraceHeaders.TRACE_ID_NAME, span.getTraceId());
        MDC.put(TraceHeaders.SPAN_ID_NAME, span.getSpanId());
        MDC.put(TraceHeaders.PARENT_SPAN_ID_NAME, span.getParentSpanId());
        MDC.put(TraceHeaders.TRACE_SPAN_ID_NAME, span.getTraceSpanId());
    }

    public static void reset() {
        contexts.get().clear();
        MDC.remove(TraceHeaders.TRACE_ID_NAME);
        MDC.remove(TraceHeaders.SPAN_ID_NAME);
        MDC.remove(TraceHeaders.PARENT_SPAN_ID_NAME);
        MDC.remove(TraceHeaders.TRACE_SPAN_ID_NAME);
    }

    public static void beginSpan() {
        Stack<ISpan> stack = contexts.get();
        ISpan span = stack.isEmpty() ? TracerUtils.createSpan() : TracerUtils.createContinueSpan(stack.peek());
        stack.push(span);
        MDC.put(TraceHeaders.TRACE_ID_NAME, span.getTraceId());
        MDC.put(TraceHeaders.SPAN_ID_NAME, span.getSpanId());
        MDC.put(TraceHeaders.PARENT_SPAN_ID_NAME, span.getParentSpanId());
        MDC.put(TraceHeaders.TRACE_SPAN_ID_NAME, span.getTraceSpanId());
    }

    public static void endSpan() {
        Stack<ISpan> stack = contexts.get();
        if (stack.size() <= 1) {
            stack.clear();
            MDC.remove(TraceHeaders.TRACE_ID_NAME);
            MDC.remove(TraceHeaders.SPAN_ID_NAME);
            MDC.remove(TraceHeaders.PARENT_SPAN_ID_NAME);
            MDC.remove(TraceHeaders.TRACE_SPAN_ID_NAME);
            return;
        }
        stack.pop();
        ISpan span = stack.peek();
        MDC.put(TraceHeaders.TRACE_ID_NAME, span.getTraceId());
        MDC.put(TraceHeaders.SPAN_ID_NAME, span.getSpanId());
        MDC.put(TraceHeaders.PARENT_SPAN_ID_NAME, span.getParentSpanId());
        MDC.put(TraceHeaders.TRACE_SPAN_ID_NAME, span.getTraceSpanId());
    }
}
