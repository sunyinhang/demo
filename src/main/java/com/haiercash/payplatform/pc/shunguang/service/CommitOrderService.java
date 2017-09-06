package com.haiercash.payplatform.pc.shunguang.service;

import java.util.Map;

/**
 * shunguang service interface.
 * @author yuan li
 * @since v1.0.1
 */
public interface CommitOrderService {

    /**
     * 订单提交
     * @param map
     * @return
     */
    Map<String, Object> commitOrder(Map<String, Object> map)  throws Exception;

}
