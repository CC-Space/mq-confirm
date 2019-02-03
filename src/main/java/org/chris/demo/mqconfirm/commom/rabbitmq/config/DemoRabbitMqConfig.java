/*
 * Copyright (c) 2005-2018.  FPX and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */

package org.chris.demo.mqconfirm.commom.rabbitmq.config;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
//import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.amqp.rabbit.transaction.RabbitTransactionManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * MQ统一配置
 *
 * @author caizq
 * @date 2019/2/2
 * @since v1.0.0
 */
@Slf4j
@Configuration
public class DemoRabbitMqConfig {

    /**
     * @return
     */
    //@Bean
    //@Primary
    private ConnectionFactory demoMQConnectionFactory(boolean needTx
    ) {
        CachingConnectionFactory connectionFactory = getConnectionFactory("127.0.0.1:5672", "guest", "guest", "/");
        connectionFactory.setRequestedHeartBeat(12);

        //connectionFactory.getRabbitConnectionFactory().setAutomaticRecoveryEnabled(true); 默认true
        //connectionFactory.setChannelCheckoutTimeout(1000*60);//缓存池满等待时间 默认无限
        if (needTx) {
            connectionFactory.setPublisherConfirms(false);
        }
        return connectionFactory;
    }

    @Bean
    @Scope("prototype")
    public RabbitTemplate demoTxTemplate(@Qualifier("defaultDemoMQConnectionFactory") ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setChannelTransacted(true);
        return template;
    }
    @Bean
    @Scope("prototype")
    public RabbitTemplate demoNoConfirmTemplate(@Qualifier("defaultDemoMQConnectionFactory") ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        return template;
    }

    //@Bean
    //@Scope("prototype")
    public RabbitTemplate demoSyncConfirmTemplate() {
        ConnectionFactory connectionFactory = demoMQConnectionFactory(false);
        RabbitTemplate template = new RabbitTemplate(connectionFactory);

        //配置重试规则
        /*RetryTemplate retryTemplate = new RetryTemplate();
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(500);
        backOffPolicy.setMultiplier(10.0);
        backOffPolicy.setMaxInterval(10000);
        retryTemplate.setBackOffPolicy(backOffPolicy);
        template.setRetryTemplate(retryTemplate);*/
        return template;
    }
    @Bean
    @Scope("prototype")
    public Channel demoSyncConfirmChannel() throws IOException {
        ConnectionFactory connectionFactory = demoMQConnectionFactory(false);
        Channel channel = connectionFactory.createConnection().createChannel(false);
        channel.confirmSelect();

        return channel;
    }
    @Bean
    @Scope("prototype")
    public RabbitTemplate demoAsyncConfirmTemplate() {
        ConnectionFactory connectionFactory = demoMQConnectionFactory(false);
        RabbitTemplate demoAsyncConfirmTemplate = new RabbitTemplate(connectionFactory);
        //connectionFactory.setChannelCheckoutTimeout(1000*60);//缓存池满等待时间

        /*
        //设置Mandatory  mandatory必须设置true,return callback才生效  文档写明:only applies if a ReturnCallback had been provided.
        demoAsyncConfirmTemplate.setMandatory(true);
        //发送成功确认
        demoAsyncConfirmTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                if (ack) {
                    //log.info("消息确认成功");
                } else {
                    //处理丢失的消息（nack）
                    log.info("消息确认失败");
                }
            }
        });
        ////转发成功确认
        demoAsyncConfirmTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            @Override
            public void returnedMessage(Message message, int replyCode, String replyText,
                                        String exchange, String routingKey) {
                //重新发布
                RepublishMessageRecoverer recoverer = new RepublishMessageRecoverer(demoSyncConfirmTemplate(),"CHRIS_ERROR_DEMO_X_TEST", "CHRIS_ERROR_DEMO_R_TEST");
                Throwable cause = new Exception(new Exception("route_fail_and_republish"));
                recoverer.recover(message,cause);
                log.info("Returned Message："+replyText);
            }
        });

        //配置重试规则
        RetryTemplate retryTemplate = new RetryTemplate();
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(500);
        backOffPolicy.setMultiplier(10.0);
        backOffPolicy.setMaxInterval(10000);
        retryTemplate.setBackOffPolicy(backOffPolicy);
        demoAsyncConfirmTemplate.setRetryTemplate(retryTemplate);*/
        return demoAsyncConfirmTemplate;
    }

    @Bean
    public RabbitAdmin demoMQRabbitAdmin() {
        ConnectionFactory connectionFactory = demoMQConnectionFactory(false);
        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);

        //定义Demo队列
        initDemoQueue(rabbitAdmin);
        //定义Error队列
        initErrorDemoQueue(rabbitAdmin);

        return rabbitAdmin;
    }

    private void initDemoQueue(RabbitAdmin rabbitAdmin) {
        //定义队列
        Map<String, Object> map = new HashMap<>(2);
        map.put("x-dead-letter-exchange", "CHRIS_DEMO_X_TEST_DEAD");
        map.put("x-dead-letter-routing-key", "CHRIS_DEMO_R_TEST_DEAD");
        Queue queue = new Queue("CHRIS_DEMO_Q_TEST", true, false, false, map);
        DirectExchange exchange = new DirectExchange("CHRIS_DEMO_X_TEST", true, false);
        rabbitAdmin.declareQueue(queue);
        rabbitAdmin.declareExchange(exchange);
        rabbitAdmin.declareBinding(BindingBuilder.bind(queue).to(exchange).with("CHRIS_DEMO_R_TEST"));
        //定义预报的死信队列(若配置转发生产者不需要定义死信队列)
        Queue deadQueue = new Queue("CHRIS_DEMO_Q_TEST_DEAD", true, false, false);
        DirectExchange deadExchange = new DirectExchange("CHRIS_DEMO_X_TEST_DEAD", true, false);
        rabbitAdmin.declareQueue(deadQueue);
        rabbitAdmin.declareExchange(deadExchange);
        rabbitAdmin.declareBinding(BindingBuilder.bind(deadQueue).to(deadExchange).with("CHRIS_DEMO_R_TEST_DEAD"));
    }
    private void initErrorDemoQueue(RabbitAdmin rabbitAdmin) {
        //定义队列
        Map<String, Object> map = new HashMap<>(2);
        map.put("x-dead-letter-exchange", "CHRIS_ERROR_DEMO_X_TEST_DEAD");
        map.put("x-dead-letter-routing-key", "CHRIS_ERROR_DEMO_R_TEST_DEAD");
        Queue queue = new Queue("CHRIS_ERROR_DEMO_Q_TEST", true, false, false, map);
        DirectExchange exchange = new DirectExchange("CHRIS_ERROR_DEMO_X_TEST", true, false);
        rabbitAdmin.declareQueue(queue);
        rabbitAdmin.declareExchange(exchange);
        rabbitAdmin.declareBinding(BindingBuilder.bind(queue).to(exchange).with("CHRIS_ERROR_DEMO_R_TEST"));
    }

    /**
     * 初始化RabbitMQ 连接池
     *
     * @param addresses
     * @param username
     * @param password
     * @param virtualHost
     * @return
     */
    private CachingConnectionFactory getConnectionFactory(String addresses, String username, String password, String virtualHost) {
        //这里指定localhost是因为linux下取不到默认配置
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory("localhost");
        connectionFactory.setAddresses(addresses);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        connectionFactory.setVirtualHost(virtualHost);
        connectionFactory.setPublisherConfirms(true);
        return connectionFactory;
    }


    @Bean
    @Primary
    public ConnectionFactory defaultDemoMQConnectionFactory(
    ) {
        CachingConnectionFactory connectionFactory = getConnectionFactory("127.0.0.1:5672", "guest", "guest", "/");
        connectionFactory.setRequestedHeartBeat(12);
        connectionFactory.setPublisherConfirms(false);
        return connectionFactory;
    }
    /**
     * 支持事务
     * @return
     */
    @Bean
    @ConditionalOnMissingClass("org.springframework.orm.jpa.JpaTransactionManager")
    public RabbitTransactionManager rabbitTransactionManager(@Qualifier("defaultDemoMQConnectionFactory") ConnectionFactory connectionFactory) {
        return new RabbitTransactionManager(connectionFactory);
    }
}
