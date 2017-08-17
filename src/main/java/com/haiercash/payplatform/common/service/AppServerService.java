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
    public Map<String,Object> updatePayPasswd(String token,Map<String, Object> paramMap);
    //实名认证修改密码
    public  Map<String,Object> updPwdByIdentity(String token,Map<String,Object> paramMap);
    // 验证支付密码
    public  Map<String,Object>  validatePayPasswd(String token,Map<String,Object> map);
    //(GET)查询贷款详情（根据申请流水号）
    public Map<String,Object> queryApplLoanDetail(String token,Map<String, Object> paramMap);
    //(GET)按贷款申请查询分期账单
    public Map<String,Object> queryApplListBySeq(String token,Map<String,Object> queryApplListMap);
    //7.1.(POST) 欠款查询(参照核算接口5.1)
    public Map<String, Object> getQFCheck(String token,Map<String,Object> qfmap);
    //(GET)全部还款试算（含息费、手续费、本金）
    public Map<String,Object> refundTrialAll(String token,Map<String, Object> paramMap);
    //7.4.(POST)主动还款金额查询
    public Map<String, Object> checkZdhkMoney(String token,Map<String, Object> paramMap);
    // 3.1.16.(GET)额度查询
    public Map<String,Object> getEdCheck(String token,Map<String, Object> paramMap);
    //(GET)根据流水号查询额度审批进度
    public Map<String,Object> approvalProcessInfo(String token, Map<String, Object> paramMap);
    //3.4.20.	(GET) 根据集团用户ID查询用户信息
    public String queryHaierUserInfo(String params);
    //3.4.21.	(POST)集团用户注册统一认证账户
    public Map<String,Object> saveUauthUsersByHaier(Map<String,Object> params);
    //6.1.124.	(POST)保存第三方系统token
    public Map<String,Object> saveThirdPartToken(Map<String,Object> params);
    //3.1.29.	(GET)查询客户实名认证信息（根据USERID）(APP_PERSON)(CRM17)
    public Map<String,Object> queryPerCustInfo(String token,Map<String, Object> paramMap);
    //6.1.102.	(GET)额度申请校验
    public Map<String,Object> checkEdAppl(String token,Map<String, Object> paramMap);
    //3.4.22.	(POST) 验证并绑定集团用户（已绑定的不可用）
    public Map<String,Object> validateAndBindHaierUser(String token,Map<String, Object> paramMap);
    //6.1.24.	(GET)查询贷款品种所需的影像列表(不包含共同还款人影像)
    public Map<String, Object> pLoanTypImages(String token,Map<String, Object> paramMap);
    //6.2.5.	(GET)影像列表按类型查询-个人版
    public Map<String, Object> attachTypeSearchPerson(String token,Map<String, Object> paramMap);
    //6.1.127.	(GET)根据影像文件ID查询影像文件的路径
    public Map<String, Object> getFilePathByFileId(String token,Map<String, Object> paramMap);
    //3.1.1(POST)修改保存客户所有扩展信息(CRM85)
    public Map<String, Object> saveAllCustExtInfo(String token,Map<String, Object> paramMap);
    //3.1.3. (POST)新增/修改 联系人(CRM6)
    public Map<String, Object> saveCustFCiCustContact(String token,Map<String, Object> paramMap);
    //6.1.108.	(post)信息完整查询接口
    public Map<String, Object> checkIfMsgComplete(String token,Map<String, Object> paramMap);
    //1.27(GET) 根据集团用户id查询用户信息
    public Map<String, Object>  getUserId(String token,Map<String, Object> paramMap);
    //6.1.28.	 (DELETE)影像删除
    public Map<String, Object> attachDelete(String token,Map<String, Object> paramMap);
    //3.4.8.	 (GET)用户是否注册
    public Map<String, Object> isRegister(String token,Map<String, Object> paramMap);
}
