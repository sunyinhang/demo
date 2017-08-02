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
    //6.1.126.	+(POST) 通过人脸分数判断人脸是否通过
    public Map<String, Object> faceCheckByFaceValue(String token, Map<String, Object> params);
    //3.4.1.	(GET)用户支付密码手势密码验证是否设置
    public Map<String, Object> validateUserFlag(String token, Map<String, Object> params);
    //6.1.125.	(GET)通过贷款品种判断是否需要进行人脸识别
    public Map<String, Object> ifNeedFaceChkByTypCde(String token, Map<String, Object> params);
}
