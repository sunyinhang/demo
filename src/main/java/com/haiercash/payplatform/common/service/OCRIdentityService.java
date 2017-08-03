package com.haiercash.payplatform.common.service;

import org.springframework.validation.ObjectError;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Created by yuanli on 2017/7/27.
 */
public interface OCRIdentityService{

    //OCR身份信息获取
    public Map<String, Object> ocrIdentity(MultipartFile ocrImg, HttpServletRequest request, HttpServletResponse response) throws Exception;

    //保存OCR信息
    public Map<String, Object> savaIdentityInfo(Map<String, Object> map);

    //获取省市区
    public Map<String, Object> getArea(Map<String, Object> map);

    //获取卡信息
    public Map<String, Object> getCardInfo(String cardNo);

    //发送短信验证码
    public Map<String, Object> sendMessage(String token, String channel, String channelNo);

    //发送短信验证码
    public Map<String, Object> sendMsg(String phone, String channel, String channelNo);

    //实名认证
    public Map<String, Object> realAuthentication(Map<String, Object> map) throws Exception;

    //支付密码设置
    public Map<String ,Object> resetPayPasswd(String token,String payPasswd);

    //协议展示：(1)展示注册协议(2)个人征信(3)借款合同
    public Map<String,Object> treatyShowServlet(String token,String flag) throws Exception;
}
