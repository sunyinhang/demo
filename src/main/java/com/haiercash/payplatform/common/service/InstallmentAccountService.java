package com.haiercash.payplatform.common.service;

import java.util.Map;

/**
 * 账单分期相关接口
 * Created by ljy on 2017/8/17.
 */
public interface InstallmentAccountService {
    //查询全部贷款信息列表
    public Map<String, Object> queryAllLoanInfo(String token, String channelNo, String channel, Map<String,Object> map);
    //查询待提交订单列表
    public Map<String, Object> QueryPendingLoanInfo(String token, String channelNo, String channel, Map<String,Object> map);
    //待还款信息查询(全部)
    public Map<String, Object> queryPendingRepaymentInfo(String token, String channelNo, String channel, Map<String,Object> map);
    //查询已提交贷款申请列表
    public Map<String,Object> queryApplLoanInfo(String token, String channelNo, String channel, Map<String, Object> map);
    //6.1.8.	 (DELETE)删除订单
    public Map<String, Object> deleteOrderInfo(String token, String channelNo, String channel, Map<String, Object> map);
}
