package com.haiercash.payplatform.common.service;

import com.haiercash.payplatform.common.data.AppOrder;

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
}
