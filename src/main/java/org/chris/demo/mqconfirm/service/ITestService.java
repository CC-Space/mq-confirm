package org.chris.demo.mqconfirm.service;

/**
 * @author caizq
 * @date 2019/2/2
 * @since v1.0.0
 */
public interface ITestService {
    String testSync();

    String testMultiMsgInOneTx();

    String testOneMsgInOneTx(String msg);

    String testAsync();

    String testNoConfirm();
}
