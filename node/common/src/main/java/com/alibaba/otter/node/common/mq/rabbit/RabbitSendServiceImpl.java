package com.alibaba.otter.node.common.mq.rabbit;

import com.alibaba.otter.node.common.mq.MqSendService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;

public class RabbitSendServiceImpl implements MqSendService, RabbitTemplate.ConfirmCallback {

    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitSendServiceImpl.class);

    private AmqpTemplate amqpTemplate;

    public void setAmqpTemplate(AmqpTemplate amqpTemplate) {
        this.amqpTemplate = amqpTemplate;
    }

    /**
     * 发送消息
     *
     * @param destination 队列
     * @param pattern     mq 模式
     * @param message     实体对象转换成byte[]后的数据
     */
    @Override
    public void sendMessage(String destination, Integer pattern, byte[] message) {
        if (destination.contains("_queue_key")) {
            amqpTemplate.convertAndSend(destination.replace("_queue_key", ""), destination, message, (msg) -> {
                msg.getMessageProperties().setHeader("traceId", createTraceId());
                return msg;
            });
        } else {
            LOGGER.error("=sendMessage=>destination={} is not contains _queue_key", destination);
        }
    }


    /**
     * Confirmation callback.
     * 消息的回调，主要是实现RabbitTemplate.ConfirmCallback接口
     *
     * @param correlationData correlation data for the callback.
     * @param ack             true for ack, false for nack
     */
    @Override
    public void confirm(CorrelationData correlationData, boolean ack) {
        if (ack) {
            LOGGER.info("Send Message Success！");
        } else {
            LOGGER.info("Send Message Fail.");

        }
    }
}
