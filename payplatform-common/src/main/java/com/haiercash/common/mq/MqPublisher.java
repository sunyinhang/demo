package com.haiercash.common.mq;

import com.alibaba.fastjson.JSON;
import com.haiercash.common.data.CaSignPushRepository;
import com.haiercash.common.data.MessageStoreRepository;
import com.haiercash.common.mq.rabbit.core.HcMessage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * 消息发布功能类
 * Created by Administrator on 2017/2/22.
 */
@Component
public class MqPublisher {

    Log logger = LogFactory.getLog(this.getClass());

    private RabbitTemplate rabbitTemplate;
    private CaSignPushRepository caSignPushRepository;
    private MessageStoreRepository messageStoreRepository;

    @Value("${spring.rabbitmq.exchange}")
    private String exchangeName;

    @Autowired
    public MqPublisher(RabbitTemplate rabbitTemplate, MessageStoreRepository messageStoreRepository, CaSignPushRepository caSignPushRepository) {
        this.rabbitTemplate = rabbitTemplate;
        this.messageStoreRepository = messageStoreRepository;
        this.caSignPushRepository = caSignPushRepository;
        this.rabbitTemplate.setConfirmCallback(new MqConfirmCallback(rabbitTemplate, messageStoreRepository, caSignPushRepository));
    }


    public void sendCmisMessage(Map<String, Object> paramMap, String msgId, String channelNo) {
        String applSeq = StringUtils.isEmpty(paramMap.get("applSeq")) ? "" : paramMap.get("applSeq").toString();
        rabbitTemplate.convertAndSend(exchangeName, "CMISMSG." + channelNo, JSON.toJSONString(paramMap), new CorrelationData("cmis|" + msgId));
    }

    public void sendMessage(HcMessage hcMessage) {
        rabbitTemplate.convertAndSend(StringUtils.isEmpty(hcMessage.getExchangeName()) ? "APPMSG_TOPIC_EXCHANGE" : hcMessage.getExchangeName(), hcMessage.getRoutingKey(), hcMessage.getMessage(), new CorrelationData(hcMessage.getBusinessInfo()));
    }

    public void sendCaSignMessage(Map<String, Object> paramMap) {
        String channelNo = StringUtils.isEmpty(paramMap.get("channelNo")) ? "" : paramMap.get("channelNo").toString();
        String id = StringUtils.isEmpty(paramMap.get("id")) ? "" : paramMap.get("id").toString();
        //rabbitTemplate.convertAndSend(exchangeName,"CASIGNMSG.#", JSON.toJSONString(paramMap), new CorrelationData("caSign|" + id));
        rabbitTemplate.convertAndSend(exchangeName,"CASIGNMSG."+channelNo, JSON.toJSONString(paramMap), new CorrelationData("caSign|" + id));
    }
}
