/*
 * Copyright (c) 2005-2018.  FPX and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */
package org.chris.demo.mqconfirm.commom.rabbitmq.provider;

/**
 * MQ发消息使用Demo
 * @author caizq
 * @date 2019/2/2
 * @since v1.0.0
 */
public interface IChrisDemoProvider {


    void sendSyncConfirmDemoMsg(String demoMsg);

    void sendTxDemoMsg(String demoMsg);

    void sendAsyncConfirmDemoMsg(String demoMsg);

    void sendNoConfirmDemoMsg(String demoMsg);
}
