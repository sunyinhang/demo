package com.haiercash.payplatform.common.service;

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
    //6.1.116.	查询CRM中客户扩展信息（二）接口
    public Map<String, Object> getAllCustExtInfo(String token, Map<String, Object> params);
    //(PUT)支付密码设置接口
    public Map<String,Object> resetPayPasswd(String token,Map<String,Object> paramMap);
    //6.1.86.(GET) 查询是否可以提交额度申请
    public Map<String,Object> ifEdAppl(String token,Map<String,Object> paramMap);
    //(GET)3.1.25查询客户的准入资格
    public Map<String,Object> getCustIsPass(String token,Map<String, Object> paramMap);
    //(GET)额度申请
   public Map<String,Object> getEdApplInfo(String token,Map<String,Object>edapplInfoMap);
    //6.1.29.(POST)订单协议确认
    public String updateOrderAgreement(String token,Map<String,Object> reqSignMap);
    //6.1.30.(POST)订单合同确认
    public String updateOrderContract(String token,Map<String,Object> reqConMap);
    //6.1.14.(GET)订单提交
    public String commitAppOrder(String token,Map<String,Object> commitmMap);
    //3.4.15.	(PUT)支付密码修改(知道原密码)
    public String updatePayPasswd(String token,Map<String, Object> paramMap);
    //实名认证修改密码
    public String updPwdByIdentity(String token,Map<String,Object> paramMap);
    // 验证支付密码
    public String  validatePayPasswd(String token,Map<String,Object> map);
    //(GET)查询贷款详情（根据申请流水号）
    public Map<String,Object> queryApplLoanDetail(String token,Map<String, Object> paramMap);
    //(GET)查询贷款详情（根据申请流水号）
    public Map<String,Object> queryApplListBySeq(String token,Map<String,Object> queryApplListMap);
}
