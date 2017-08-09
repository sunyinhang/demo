package com.haiercash.payplatform.service;

import java.util.Map;

import com.haiercash.payplatform.common.data.AppOrder;

/**
 * order service interface.
 * @author Liu qingxiang
 * @since v1.0.0
 */
public interface OrderService {
    /**
     * 将apporder订单信息转换为订单系统保存接单map.
     *
     * @param appOrder 订单信息
     * @return Map
     */
    Map<String, Object> order2OrderMap(AppOrder appOrder, Map<String, Object> map);

    /**
     * 取消订单.
     * 只适用于订单状态暂存且为申请流水号时调用.
     * @param formId 订单id
     * @return
     */
    Map<String,  Object> cancelOrder(String formId);


    /**
     * 查询订单商品列表.
     * @param formId   订单id
     * @return Map
     */
    Map<String, Object> getGoodsList(String formId);


}

