package com.haiercash.spring.rabbit.core;

import com.haiercash.spring.context.ThreadContext;
import com.haiercash.spring.rabbit.exception.ConsumeDisabledException;
import com.haiercash.spring.trace.rabbit.IncomingLog;
import org.springframework.amqp.rabbit.listener.adapter.DelegatingInvocableHandler;
import org.springframework.amqp.rabbit.listener.adapter.HandlerAdapter;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.invocation.InvocableHandlerMethod;

/**
 * Created by 许崇雷 on 2017-11-30.
 */
public final class RabbitHandlerAdapter extends HandlerAdapter {
    public RabbitHandlerAdapter(InvocableHandlerMethod invokerHandlerMethod) {
        super(invokerHandlerMethod);
    }

    public RabbitHandlerAdapter(DelegatingInvocableHandler delegatingHandler) {
        super(delegatingHandler);
    }

    @Override
    public Object invoke(Message<?> message, Object... providedArgs) throws Exception {
        ThreadContext.init(null, null, null);
        IncomingLog.writeRequestLog(message);
        long begin = System.currentTimeMillis();
        boolean disabled = false;
        Exception exception = null;
        try {
            return super.invoke(message, providedArgs);
        } catch (ConsumeDisabledException e) {
            disabled = true;
            throw e;
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            IncomingLog.writeResponseLog(message, disabled, exception, System.currentTimeMillis() - begin);
            ThreadContext.reset();
        }
    }
}
