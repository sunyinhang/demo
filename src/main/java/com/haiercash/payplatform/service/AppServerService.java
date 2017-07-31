package com.haiercash.payplatform.service;

import java.util.Map;

/**
 * Created by yuanli on 2017/7/26.
 */
public interface AppServerService {
    //获取省市区
    public Map<String, Object> getAreaInfo(String token, Map<String, Object> params);
    //获取卡信息
    public Map<String, Object> getBankInfo(String cardNo);
    //发送短信验证码
    public Map<String, Object> sendMessage(String token, Map<String, Object> params);
    //2.6.	(POST)校验短信验证码(免token)
    public Map<String, Object> smsVerify(String token, Map<String, Object> params);
    //6.1.120.	(POST)保存身份证信息
    public Map<String, Object> saveCardMsg(String token, Map<String, Object> params);
    //3.1.7.(POST)验证并新增实名认证信息(CRM66)
    public Map<String, Object> fCiCustRealThreeInfo(String token, Map<String, Object> params);
    //3.4.12.	(PUT) 修改绑定手机号
    public Map<String, Object> updateMobile(String token, Map<String, Object> params);
    //6.2.15.	(POST)影像上传-个人版（上传共享盘文件路径）
    public Map<String, Object> attachUploadPersonByFilePath(String token, Map<String, Object> params);
}
