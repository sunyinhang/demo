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
        String action = this.getMethodAsString(message.getPayload());
        IncomingLog.writeBeginLog(action, message);
        long begin = System.currentTimeMillis();
        try {
            Object result = super.invoke(message, providedArgs);
            IncomingLog.writeEndLog(action, message, System.currentTimeMillis() - begin);
            return result;
        } catch (ConsumeDisabledException e) {
            IncomingLog.writeDisabledLog(action, message, System.currentTimeMillis() - begin);
            throw e;
        } catch (Exception e) {
            IncomingLog.writeErrorLog(action, message, e, System.currentTimeMillis() - begin);
            throw e;
        } finally {
            ThreadContext.reset();
        }
    }
}
