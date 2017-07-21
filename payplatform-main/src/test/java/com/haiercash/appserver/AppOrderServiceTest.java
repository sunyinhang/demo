package com.haiercash.appserver;

/**
 * App Order service test interface.
 * @author Liu qingxiang
 * @since v1.2.0
 * @see com.haiercash.appserver.apporder.AppOrderController
 */
public interface AppOrderServiceTest {

    /**
     * 保存订单.
     */
    void saveAppOrder();

    /**
     * 校验业务信息是否完整.
     */
    void checkIfMsgComplete() throws Exception;

}
