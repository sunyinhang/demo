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

}

