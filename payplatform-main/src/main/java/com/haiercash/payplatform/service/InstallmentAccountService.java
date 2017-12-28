package com.haiercash.payplatform.service;

import java.util.Map;

/**
 * 账单分期相关接口
 * Created by ljy on 2017/8/17.
 */
public interface InstallmentAccountService {
    //查询全部贷款信息列表
    Map<String, Object> queryAllLoanInfo(String token, String channelNo, String channel, Map<String, Object> map);
    //查询待提交订单列表
    Map<String, Object> QueryPendingLoanInfo(String token, String channelNo, String channel, Map<String, Object> map);
    //待还款信息查询(全部)
    Map<String, Object> queryPendingRepaymentInfo(String token, String channelNo, String channel, Map<String, Object> map);
    //查询已提交贷款申请列表
    Map<String, Object> queryApplLoanInfo(String token, String channelNo, String channel, Map<String, Object> map);
    //订单详情查询
    Map<String, Object> queryOrderInfo(String token, String channelNo, String channel, Map<String, Object> map);

    //贷款详情查询（现金贷）
    Map<String, Object> orderQueryXjd(String token, String channelNo, String channel, Map<String, Object> params) throws Exception;
}
