package com.haiercash.payplatform.common.service;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Created by Administrator on 2017/8/7.
 */

public interface PayPasswdService {
    //支付密码设置
    public Map<String, Object> resetPayPasswd(String token, String channelNo, String channel,Map<String,Object> map);

    //页面缓存
    public Map<String, Object> cache(Map<String, Object> params, HttpServletRequest request);

    //修改支付密码（记得支付密码）
    public Map<String, Object> updatePayPasswd(String token, Map<String,Object> params, String channel, String channelNo);

    //实名认证找回密码
    public Map<String, Object> updPwdByIdentity(Map<String, Object> params);

    //确认支付密码（额度申请）
    public Map<String, Object> paymentPwdConfirm(String token, String channel, String channelNo, Map<String,Object> map);

    //额度申请提交
    public Map<String, Object> edApply(String token, String verifyNo, String payPasswd, String channel, String channelNo);

    //贷款详情查询
    public Map<String, Object> queryLoanDetailInfo(String token);

    //贷款详情页面:按贷款申请查询分期账单
    public Map<String, Object> queryApplListBySeq(String token, String channel, String channelNo);

    //贷款详情页面:还款总额
    public Map<String, Object> queryApplAmtBySeqAndOrederNo(String token, String channel, String channelNo);

    //查询额度
    public Map<String, Object> edCheck(String token);

    //根据流水号查询额度审批进度
   public Map<String,Object> approvalProcessInfo(String token, String channel, String channelNo);


}

