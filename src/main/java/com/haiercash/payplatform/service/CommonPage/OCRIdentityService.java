package com.haiercash.payplatform.service.CommonPage;

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

}
