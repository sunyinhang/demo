package com.haiercash.payplatform.service;

import java.util.Map;

/**
 * Created by use on 2017/8/24.
 * 调用订单接口
 */
public interface OrderManageService {
    //OM-1108 根据applSeq查询商城订单号和网单号
    Map<String, Object> getMallOrderNoByApplSeq(String applSeq);
    //OM-1107 根据formId查询订单送货地址详情（单条）
    Map<String, Object> getAddressByFormId(String formId);
    //根据商城订单号查询订单信息
    Map<String, Object> getOrderStsByMallOrder(String mallOrderNo);
}
