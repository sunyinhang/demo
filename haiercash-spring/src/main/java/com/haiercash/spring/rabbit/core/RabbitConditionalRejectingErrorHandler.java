package com.haiercash.spring.rabbit.core;

import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler;
import org.springframework.amqp.rabbit.listener.FatalExceptionStrategy;
import org.springframework.amqp.rabbit.listener.exception.ListenerExecutionFailedException;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.messaging.handler.annotation.support.MethodArgumentTypeMismatchException;

/**
 * Created by 许崇雷 on 2017-12-01.
 */
public final class RabbitConditionalRejectingErrorHandler extends ConditionalRejectingErrorHandler {
    private final FatalExceptionStrategy exceptionStrategy;

    public RabbitConditionalRejectingErrorHandler() {
        this.exceptionStrategy = new RabbitExceptionStrategy();
    }

    public RabbitConditionalRejectingErrorHandler(FatalExceptionStrategy exceptionStrategy) {
        this.exceptionStrategy = exceptionStrategy;
    }

    @Override
    public void handleError(Throwable throwable) {
        //isFatal() true 说明错误非常严重. 如果设置了死信队列将被发往死信队列, 否则将被丢弃
        if (!this.causeChainContainsARADRE(throwable) && this.exceptionStrategy.isFatal(throwable))
            throw new AmqpRejectAndDontRequeueException("Error Handler converted exception to fatal", throwable);
    }

    private boolean causeChainContainsARADRE(Throwable throwable) {
        Throwable cause = throwable.getCause();
        while (cause != null) {
            if (cause instanceof AmqpRejectAndDontRequeueException)
                return true;
            cause = cause.getCause();
        }
        return false;
    }

    public static final class RabbitExceptionStrategy extends ConditionalRejectingErrorHandler.DefaultExceptionStrategy {
        @Override
        public boolean isFatal(Throwable throwable) {
            return throwable instanceof ListenerExecutionFailedException && this.isCauseFatal(throwable.getCause());
        }

        private boolean isCauseFatal(Throwable cause) {
            return cause instanceof MessageConversionException
                    || cause instanceof org.springframework.messaging.converter.MessageConversionException
                    || cause instanceof MethodArgumentNotValidException
                    || cause instanceof MethodArgumentTypeMismatchException
                    || cause instanceof NoSuchMethodException
                    || cause instanceof ClassCastException
                    || this.isUserCauseFatal(cause);
        }

        @Override
        protected boolean isUserCauseFatal(Throwable cause) {
            return false;
        }
    }
}
