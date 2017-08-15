package com.haiercash.payplatform.tasks.rabbitmq;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 核心消息处理.
 *
 * @author Liu qingxiang
 * @since v1.0.1
 */
@Component
@RabbitListener(queues = "${rabbitmq.queue.cmis}")
public class CmisMseeageHandler {
    private static final int MAX_TRY_COUNT = 10;
    private Log logger = LogFactory.getLog(CmisMseeageHandler.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private boolean validNodeMessage(Object nodeMessage) {
        if (nodeMessage == null) {
            this.logger.warn("节点类数据为 null,无法消费");
            return false;
        }
        return true;
    }

    //消费节点类类活动的消息
    @RabbitHandler
    public void consumeNodeMessage(String json) {
        // 消费消息

    }
}
