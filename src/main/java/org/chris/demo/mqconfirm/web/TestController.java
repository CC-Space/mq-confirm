/*
 * Copyright (c) 2005-2018 , FPX and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */
package org.chris.demo.mqconfirm.web;

import lombok.extern.slf4j.Slf4j;
import org.chris.demo.mqconfirm.service.ITestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author caizq
 * @date 2019/2/2
 * @since v1.0.0
 */
@Slf4j
@RestController("/")
public class TestController {
    @Autowired
    private ITestService testService;

    @RequestMapping("/testNoConfirm")
    public String testNoConfirm() {
        return testService.testNoConfirm();
    }
    @RequestMapping("/testSync")
    public String testSync() {
        return testService.testSync();
    }

    @RequestMapping("/testMultiMsgInOneTx")
    public String testTx() {
        return testService.testMultiMsgInOneTx();
    }
    @RequestMapping("/testOneMsgInOneTx")
    public String testOneMsgInOneTx() {
        String msg = "事务消息";
        Long start = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            testService.testOneMsgInOneTx(msg);
        }
        log.info("耗时:{} ms", System.currentTimeMillis() - start);
        return "sueccess";
    }

    @RequestMapping("/testAsync")
    public String testAsync() {
        return testService.testAsync();
    }
}
