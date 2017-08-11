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
     * 提交订单接口.
     *  订单状态	    系统编号	    贷款类型	    操作内容	提交后订单状态
     * 00-待提交	13-商户版APP	01-耐用消费品	贷款流水号+状态	10-已提交
     * 00-待提交	13-商户版APP	02-一般消费品	贷款流水号+状态	10-已提交
     * 00-待提交	14-个人版APP	01-耐用消费品	状态	        11-待确认
     * 00-待提交	14-个人版APP	02-一般消费品	贷款流水号+状态	10-已提交
     * 11-待确认	14-个人版APP	01-耐用消费品	贷款流水号+状态	10-已提交
     * 00-待提交	    其他	        任意	    贷款流水号+状态	10-已提交
     * @param formId 订单id
     * @param type  1 提交给商户  0 提交订单
     * @return
     */
    Map<String,  Object> submitOrder(String formId, String type);

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

