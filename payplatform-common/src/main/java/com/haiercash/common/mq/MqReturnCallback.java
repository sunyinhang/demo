package com.haiercash.common.mq;

import com.haiercash.common.mq.rabbit.core.HcMessage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;



public class MqReturnCallback implements RabbitTemplate.ReturnCallback {
    private RabbitTemplate rabbitTemplate;

    /**
     * 配置发送消息的rabbitTemplate，因为是构造方法，所以不用注解Spring也会自动注入（应该是新版本的特性）
     * @param rabbitTemplate
     */
    public MqReturnCallback(RabbitTemplate rabbitTemplate){
        this.rabbitTemplate = rabbitTemplate;
        //设置消费回调
        this.rabbitTemplate.setReturnCallback(this);
    }

    Log logger = LogFactory.getLog(this.getClass());
    protected void processReturn(HcMessage message, int replyCode, String replyText) {
        logger.info("消息发送失败: " + message.getBusinessInfo() + ", 返回码：" + replyCode
                + ", 失败原因：" + replyText + ", Exchange: " + message.getExchangeName()
                + ", RoutingKey: " + message.getRoutingKey());
        setMessageFailed(message.getBusinessType(), message.getBusinessId(), replyCode, replyText);
    }

    /**
     * 更新对应的消息记录为发送失败，并记录失败原因
     *
     * @param businessType
     * @param businessId
     * @param code
     * @param message
     */
    private void setMessageFailed(String businessType, String businessId, int code, String message) {
        logger.error("消息发送失败: " + businessType + ", " + businessId + ", " + code + ", " + message);
    }

    @Override
    public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
        HcMessage hcMessage = new HcMessage();
        hcMessage.setRabbitMessage(message);
        hcMessage.setExchangeName(exchange);
        hcMessage.setRoutingKey(routingKey);
        this.processReturn(hcMessage, replyCode, replyText);
    }
}
