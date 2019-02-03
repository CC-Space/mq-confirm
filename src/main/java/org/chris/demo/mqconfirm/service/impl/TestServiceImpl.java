/*
 * Copyright (c) 2005-2018 , FPX and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */
package org.chris.demo.mqconfirm.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.chris.demo.mqconfirm.commom.rabbitmq.provider.IChrisDemoProvider;
import org.chris.demo.mqconfirm.service.ITestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author caizq
 * @date 2019/2/2
 * @since v1.0.0
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class TestServiceImpl implements ITestService {
    @Autowired
    private IChrisDemoProvider chrisDemoProvider;
    final int TRY_COUNT = 1000;


    //    * 报错:因为每个connection最多支持2048个channel,当channel达到2048时,当同时使用Channel超过这个值后可能会报The channelMax limit is reached.Try it later
    //* ↑ 上面问题只能通过同步/异步补偿机制重试
    //*
    //        * 因为我们测试的是性能  所以这里用同步方案:失败递归睡眠重试发送(可以保证顺序)

    /**
     * 本地单机1000条性能测试
     * 1.耗时:23872 ms
     * 2.耗时:22985 ms
     * 3.耗时:22290 ms
     *
     * @return
     */
    @Override
    public String testSync() {
        String msg = "同步消息";
        Long start = System.currentTimeMillis();
        for (int i = 0; i < TRY_COUNT; i++) {
            chrisDemoProvider.sendSyncConfirmDemoMsg(msg + i);
        }
        log.info("耗时:{} ms", System.currentTimeMillis() - start);
        return "sueccess";
    }

    /**
     * 本地单机1000条性能测试
     * 1.耗时:64 ms
     * 2.耗时:45 ms
     * 3.耗时:42 ms
     * <p>
     * 本地单机50000条性能测试
     * 1.耗时:2188 ms
     * 2.耗时:2283 ms
     * 3.耗时:2166 ms
     *
     * @return
     */
    @Override
    public String testMultiMsgInOneTx() {
        String msg = "事务消息";
        Long start = System.currentTimeMillis();
        for (int i = 0; i < TRY_COUNT; i++) {
            chrisDemoProvider.sendTxDemoMsg(msg + i);
        }
        log.info("耗时:{} ms", System.currentTimeMillis() - start);
        return "sueccess";
    }

    /**
     * 43QPS
     * 本地单机1000条性能测试
     * 1.耗时:23212 ms
     * 2.耗时:23701 ms
     * 3.耗时:22020 ms
     *
     * @param msg
     * @return
     */
    @Override
    public String testOneMsgInOneTx(String msg) {
        chrisDemoProvider.sendTxDemoMsg(msg);
        //int i = 1/0;
        return "sueccess";
    }

    /**
     * 10000~15000QPS
     * 本地单机1000条性能测试
     * 1.耗时:65 ms
     * 2.耗时:43 ms
     * 3.耗时:43 ms
     * <p>
     * 本地单机50000条性能测试
     * 1.耗时:5362 ms
     * 2.耗时:5191 ms
     * 3.耗时:5811 ms
     *
     * @return
     */
    @Override
    public String testAsync() {
        String msg = "异步消息";
        Long start = System.currentTimeMillis();
        for (int i = 0; i < TRY_COUNT; i++) {
            chrisDemoProvider.sendAsyncConfirmDemoMsg(msg + i);
        }
        log.info("耗时:{} ms", System.currentTimeMillis() - start);
        return "sueccess";
    }

    /**
     * 本地单机1000条性能测试
     * 1.耗时:161 ms
     * 2.耗时:57 ms
     * 3.耗时:56 ms
     *
     * 本地单机50000条性能测试
     * 1.耗时:3444 ms
     * 2.耗时:4045 ms
     * 3.耗时:3769 ms
     * @return
     */
    @Override
    public String testNoConfirm() {
        String msg = "异步消息";
        Long start = System.currentTimeMillis();
        for (int i = 0; i < TRY_COUNT; i++) {
            chrisDemoProvider.sendNoConfirmDemoMsg(msg + i);
        }
        log.info("耗时:{} ms", System.currentTimeMillis() - start);
        return "sueccess";
    }
}
