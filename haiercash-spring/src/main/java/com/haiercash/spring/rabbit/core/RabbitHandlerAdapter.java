package com.haiercash.spring.rabbit.core;

import com.haiercash.core.lang.Convert;
import com.haiercash.spring.context.ThreadContext;
import com.haiercash.spring.context.TraceContext;
import com.haiercash.spring.rabbit.RabbitRetryMessage;
import com.haiercash.spring.rabbit.RabbitUtils;
import com.haiercash.spring.rabbit.exception.ConsumeDisabledException;
import com.haiercash.spring.trace.rabbit.IncomingLog;
import org.springframework.amqp.rabbit.listener.adapter.DelegatingInvocableHandler;
import org.springframework.amqp.rabbit.listener.adapter.HandlerAdapter;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
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
    public Object invoke(Message<?> message, Object... providedArgs) {
        ThreadContext.init(null, null, null);
        TraceContext.init();
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
            //重试
            MessageHeaders headers = message.getHeaders();
            int retry = Convert.defaultInteger(headers.get(RabbitRetryMessage.RETRY_NAME));
            if (retry < RabbitRetryMessage.RETRY_COUNT) {
                retry++;
                IncomingLog.writeWarnLog(action, message, retry, e, System.currentTimeMillis() - begin);
                String queue = (String) headers.get(AmqpHeaders.CONSUMER_QUEUE);
                RabbitRetryMessage retryMessage = new RabbitRetryMessage(message, retry);
                RabbitUtils.retry(queue, retryMessage);
                return null;
            }
            //超过最大重试次数
            IncomingLog.writeErrorLog(action, message, e, System.currentTimeMillis() - begin);
            return null;
        } finally {
            TraceContext.reset();
            ThreadContext.reset();
        }
    }
}
