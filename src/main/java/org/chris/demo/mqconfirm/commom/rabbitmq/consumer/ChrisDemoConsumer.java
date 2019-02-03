/*
 * Copyright (c) 2005-2018 , FPX and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */
package org.chris.demo.mqconfirm.commom.rabbitmq.consumer;


import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
//import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 消费实现
 * @author caizq
 * @date 2019/2/2
 * @since v1.0.0
 */
@Slf4j
@Configuration
@ConditionalOnExpression("${consumer.enable:false}")
public class ChrisDemoConsumer {

    private static final String DEFAULT_CHARSET = "UTF-8";

    @Resource
    private ConnectionFactory wmsMQConnectionFactory;


    /**
     * WOS->WMS回传队列
     * mq消费者
     *
     * @return container
     */
    @Bean
    public SimpleMessageListenerContainer wosMessageContainer() {
        Map<String, Object> map = new HashMap<>(2);
        map.put("x-dead-letter-exchange", "CHRIS_DEMO_X_TEST_DEAD");
        map.put("x-dead-letter-routing-key", "CHRIS_DEMO_R_TEST_DEAD");
        Queue queue = new Queue("CHRIS_DEMO_Q_TEST", true, false, false, map);

        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(wmsMQConnectionFactory);
        container.setQueues(queue);
        container.setExposeListenerChannel(true);
        container.setMaxConcurrentConsumers(3);
        container.setConcurrentConsumers(3);
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        container.setMessageListener((ChannelAwareMessageListener) (message, channel) -> {
            final long deliveryTag = message.getMessageProperties().getDeliveryTag();
            final String body = new String(message.getBody(), DEFAULT_CHARSET);
            boolean isReject = true;
            try {
                // 消费数据
                log.info("Success consume", body);
                //确认消息成功消费
                channel.basicAck(deliveryTag, false);
                isReject = false;
            } catch (Exception e) {
                log.error("wos callback consumer error! body:{}", body, e);
            } finally {
                if (isReject) {
                    channel.basicReject(deliveryTag, false);
                }
            }
        });
        return container;
    }


}
