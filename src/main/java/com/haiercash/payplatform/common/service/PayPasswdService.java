package com.haiercash.payplatform.common.service;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Created by Administrator on 2017/8/7.
 */

public interface PayPasswdService {
    //支付密码设置
    public Map<String, Object> resetPayPasswd(String token, String payPasswd, String verifyNo, String channelNo, String channel);

    //页面缓存
    public Map<String, Object> cache(HttpServletRequest request);

    //修改支付密码（记得支付密码）
    public Map<String, Object> updatePayPasswd(String token, String oldpassword, String newpassword, String channel, String channelNO);

    //实名认证找回密码
    public Map<String, Object> updPwdByIdentity(HttpServletRequest request);

    //确认支付密码（额度申请）
    public Map<String, Object> paymentPwdConfirm(String token, String channel, String channelNo, String payPasswd);

}
