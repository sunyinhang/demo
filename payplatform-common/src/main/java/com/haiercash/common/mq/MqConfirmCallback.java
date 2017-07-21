package com.haiercash.common.mq;

import com.haiercash.common.data.CaSignPush;
import com.haiercash.common.data.CaSignPushRepository;
import com.haiercash.common.data.MessageStore;
import com.haiercash.common.data.MessageStoreRepository;
import com.haiercash.common.mq.rabbit.core.HcMessage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;

import java.util.Date;

/**
 * 这里直接注入HcRabbitTemplate会导致循环引用，创建bean失败。
 * 通过实现ApplicationContextAware接口注入ApplicationContext，来获取HcRabbitTemplate Bean实例对象
 *
 */
public class MqConfirmCallback implements RabbitTemplate.ConfirmCallback {
    private Log log = LogFactory.getLog(this.getClass());

    private RabbitTemplate rabbitTemplate = null;
    private CaSignPushRepository caSignPushRepository;
    private MessageStoreRepository messageStoreRepository;

    /**
     * 配置发送消息的rabbitTemplate，因为是构造方法，所以不用注解Spring也会自动注入（应该是新版本的特性）
     * @param rabbitTemplate
     */
    public MqConfirmCallback(RabbitTemplate rabbitTemplate, MessageStoreRepository messageStoreRepository, CaSignPushRepository caSignPushRepository){
        this.rabbitTemplate = rabbitTemplate;
        this.caSignPushRepository = caSignPushRepository;
        this.messageStoreRepository = messageStoreRepository;
        //设置消费回调
        this.rabbitTemplate.setConfirmCallback(this);
    }

    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        HcMessage message = new HcMessage();
        message.setBusinessInfo(correlationData.getId());
        this.processConfirm(message, ack, cause);
    }

    protected void processConfirm(HcMessage message, boolean ack, String cause) {
        if (ack) {
            doMessageSuccess(message);
        } else {
            doMessageFailed(message, cause);
        }
    }

    private void doMessageSuccess(HcMessage message) {
        log.info(message.getBusinessInfo() + " - 消息发送成功");
        if ("caSign".equals(message.getBusinessType())) {
            CaSignPush caSignPush = caSignPushRepository.findOne(message.getBusinessId());
            caSignPush.setPushTime(new Date());
            caSignPush.setFlag("2");
            caSignPushRepository.save(caSignPush);
        }
        if ("cmis".equals(message.getBusinessType())) {
            MessageStore messageStore = messageStoreRepository.findOne(message.getBusinessId());
            messageStore.setOutplat("1");
            messageStoreRepository.save(messageStore);
        }
    }

    private void doMessageFailed(HcMessage message, String cause) {
        log.error(message.getBusinessInfo() + " - 消息发送失败，失败原因：" + cause);
        // 失败重发：运行时会一直重发，应用关闭后消息丢失
        /*if (this.rabbitTemplate != null) {
            this.resend(message.getRabbitMessage());
        } else {
            log.error("rabbitTemplate对象为null，无法自动重发失败的消息：" + message.getBusinessInfo());
        }*/
    }

    /**
     * 重发消息
     * @param message
     */
    public void resend(Message message) {
        MessageProperties properties = message.getMessageProperties();
        try {
            rabbitTemplate.send(properties.getReceivedExchange(), properties.getReceivedRoutingKey(), message, new CorrelationData(properties.getMessageId()));
        } catch (AmqpException var4) {
            this.log.error("RabbitMQ消息重发异常：" + var4.getMessage());
        }

    }
}
