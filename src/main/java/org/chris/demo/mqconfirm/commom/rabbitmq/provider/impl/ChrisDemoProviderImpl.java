/*
 * Copyright (c) 2005-2018 , FPX and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */
package org.chris.demo.mqconfirm.commom.rabbitmq.provider.impl;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmListener;
import com.rabbitmq.client.MessageProperties;
import lombok.extern.slf4j.Slf4j;
import org.chris.demo.mqconfirm.commom.rabbitmq.provider.IChrisDemoProvider;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * MQ provider Demo
 *
 * @author caizq
 * @date 2019/2/2
 * @since v1.0.0
 */
@Slf4j
@Component
@ConditionalOnExpression("${provider.enable:false}")
public class ChrisDemoProviderImpl implements IChrisDemoProvider {

    @Resource(name = "demoNoConfirmTemplate")
    private RabbitTemplate demoNoConfirmTemplate;
    @Resource(name = "demoAsyncConfirmTemplate")
    private RabbitTemplate demoAsyncConfirmTemplate;
    @Resource(name = "demoTxTemplate")
    private RabbitTemplate demoTxTemplate;
    @Resource(name = "demoSyncConfirmChannel")
    private Channel demoSyncConfirmChannel;

    @Override
    public void sendSyncConfirmDemoMsg(String demoMsg) {
        try {
            demoSyncConfirmChannel.basicPublish("CHRIS_DEMO_X_TEST", "CHRIS_DEMO_R_TEST", MessageProperties.PERSISTENT_TEXT_PLAIN, demoMsg.getBytes());
            //等待回复，如果回复true
            if (demoSyncConfirmChannel.waitForConfirms()) {
                //log.info("发送成功");
            } else {
                log.error("发送确认失败");
            }
        } catch (Exception e) {
            log.error("发送失败", e);
        }
    }

    @Override
    public void sendTxDemoMsg(String demoMsg) {
        try {
            demoTxTemplate.convertAndSend("CHRIS_DEMO_X_TEST", "CHRIS_DEMO_R_TEST", demoMsg);
        } catch (Exception e) {
            log.error("Msg send error", e);
            //throw SystemRuntimeException.buildSysException(EnumCommomSysErrorCode.MQ_ERROR, e, jobDTO);
            //或者:  throw SystemRuntimeException.buildSysException(EnumCommomSysErrorCode.MQ_ERROR,e,stu);
        }
        //int i = 1/0; 测试报错回滚情况
    }

    @Override
    public void sendAsyncConfirmDemoMsg(String demoMsg) {
        try {
            demoAsyncConfirmTemplate.convertAndSend("CHRIS_DEMO_X_TEST", "CHRIS_DEMO_R_TEST", demoMsg);
        } catch (Exception e) {
            log.error("消息发送失败", e);
            //throw SystemRuntimeException.buildSysException(EnumCommomSysErrorCode.MQ_ERROR, e, jobDTO);
            //或者:  throw SystemRuntimeException.buildSysException(EnumCommomSysErrorCode.MQ_ERROR,e,stu);
        }
    }

    @Override
    public void sendNoConfirmDemoMsg(String demoMsg) {
        demoNoConfirmTemplate.convertAndSend("CHRIS_DEMO_X_TEST", "CHRIS_DEMO_R_TEST", demoMsg);
    }
}
