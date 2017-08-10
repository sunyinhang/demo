package com.haiercash.payplatform.common.service.impl;

import com.haiercash.payplatform.common.utils.HttpUtil;
import com.haiercash.payplatform.common.service.AppServerService;
import com.haiercash.payplatform.service.BaseService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Created by yuanli on 2017/7/26.
 */
@Service
public class AppServerServiceImpl extends BaseService implements AppServerService {
    public Log logger = LogFactory.getLog(getClass());

//    @Value("${app.rest.APPSERVER}")
//    protected String appserverurl;

    @Value("${app.rest.APPSERVERNOAUTH}")
    protected String appservernoauth;

    /**
     * 获取省市区
     *
     * @param token
     * @param params
     * @return
     */
    public Map<String, Object> getAreaInfo(String token, Map<String, Object> params) {
        String url = appservernoauth + "/app/appserver/pub/crm/findDmAreaInfo";
        logger.info("获取省市区接口，请求地址：" + url);
        logger.info("获取省市区接口，请求数据：" + params);
        Map<String, Object> resultmap = HttpUtil.restGetMap(url, token, params);
        logger.info("获取省市区接口，返回数据" + resultmap);
        return resultmap;
    }

    /**
     * 获取卡信息
     *
     * @param cardNo
     * @return
     */
    public Map<String, Object> getBankInfo(String cardNo) {
        String url = appservernoauth + "/app/appserver/crm/cust/getBankInfo?cardNo=" + cardNo;
        logger.info("获取卡信息接口，请求地址：" + url);
        Map<String, Object> map = HttpUtil.restGetMap(url);
        logger.info("获取卡信息接口，返回数据：" + map);
        return map;
    }

    /**
     * 发送短信验证码
     *
     * @param token
     * @param params
     * @return
     */
    public Map<String, Object> sendMessage(String token, Map<String, Object> params) {
        String url = appservernoauth + "/app/appserver/smsSendVerify";
        logger.info("发送短信验证码接口，请求地址：" + url);
        logger.info("发送短信验证码接口，请求数据：" + params);
        Map<String, Object> resultmap = HttpUtil.restGetMap(url, token, params);
        logger.info("发送短信验证码接口，返回数据" + resultmap);
        return resultmap;
    }

    /**
     * 保存身份证信息
     *
     * @param token
     * @param params
     * @return
     */
    public Map<String, Object> saveCardMsg(String token, Map<String, Object> params) {
        String url = appservernoauth + "/app/appserver/saveCardMsg";
        logger.info("保存身份证信息接口，请求地址：" + url);
        logger.info("保存身份证信息接口，请求数据：" + params);
        Map<String, Object> resultmap = HttpUtil.restPostMap(url, token, params);
        logger.info("保存身份证信息接口，返回数据" + resultmap);
        return resultmap;
    }

    /**
     * 校验短信验证码
     *
     * @param token
     * @param params
     * @return
     */
    public Map<String, Object> smsVerify(String token, Map<String, Object> params) {
        String url = appservernoauth + "/app/appserver/smsVerify";
        logger.info("校验短信验证码接口，请求地址：" + url);
        logger.info("校验短信验证码接口，请求数据：" + params);
        Map<String, Object> resultmap = HttpUtil.restPostMap(url, token, params);
        logger.info("校验短信验证码接口，返回数据" + resultmap);
        return resultmap;
    }

    /**
     * 验证并新增实名认证信息
     *
     * @param token
     * @param params
     * @return
     */
    public Map<String, Object> fCiCustRealThreeInfo(String token, Map<String, Object> params) {
        String url = appservernoauth + "/app/appserver/crm/cust/fCiCustRealThreeInfo";
        logger.info("验证并新增实名认证信息接口，请求地址：" + url);
        logger.info("验证并新增实名认证信息接口，请求数据：" + params);
        Map<String, Object> resultmap = HttpUtil.restPostMap(url, token, params);
        logger.info("验证并新增实名认证信息接口，返回数据" + resultmap);
        return resultmap;
    }

    /**
     * 修改绑定手机号
     *
     * @param token
     * @param params
     * @return
     */
    public Map<String, Object> updateMobile(String token, Map<String, Object> params) {
        String url = appservernoauth + "/app/appserver/uauth/updateMobile";
        logger.info("修改绑定手机号接口，请求地址：" + url);
        logger.info("修改绑定手机号接口，请求数据：" + params);
        Map<String, Object> resultmap = HttpUtil.restPutMap(url, token, params);
        logger.info("修改绑定手机号接口，返回数据" + resultmap);
        return resultmap;
    }

    /**
     * 影像上传-个人版（上传共享盘文件路径）
     *
     * @param token
     * @param params
     * @return
     */
    public Map<String, Object> attachUploadPersonByFilePath(String token, Map<String, Object> params) {
        String url = appservernoauth + "/app/appserver/attachUploadPersonByFilePath";
        logger.info("影像上传-个人版（上传共享盘文件路径）接口，请求地址：" + url);
        logger.info("影像上传-个人版（上传共享盘文件路径）接口，请求数据：" + params);
        Map<String, Object> resultmap = HttpUtil.restPostMap(url, token, params);
        logger.info("影像上传-个人版（上传共享盘文件路径）接口，返回数据" + resultmap);
        return resultmap;
    }

    /**
     * 通过人脸分数判断人脸是否通过
     *
     * @param token
     * @param params
     * @return
     */
    public Map<String, Object> faceCheckByFaceValue(String token, Map<String, Object> params) {
        String url = appservernoauth + "/app/appserver/faceCheckByFaceValue";
        logger.info("通过人脸分数判断人脸是否通过接口，请求地址：" + url);
        logger.info("通过人脸分数判断人脸是否通过接口，请求数据：" + params);
        Map<String, Object> resultmap = HttpUtil.restPostMap(url, token, params);
        logger.info("通过人脸分数判断人脸是否通过接口，返回数据" + resultmap);
        return resultmap;
    }

    /**
     * 用户支付密码手势密码验证是否设置
     *
     * @param token
     * @param params
     * @return
     */
    public Map<String, Object> validateUserFlag(String token, Map<String, Object> params) {
        String url = appservernoauth + "/app/appserver/uauth/validateUserFlag";
        logger.info("用户支付密码手势密码验证是否设置接口，请求地址：" + url);
        logger.info("用户支付密码手势密码验证是否设置接口，请求数据：" + params);
        Map<String, Object> resultmap = HttpUtil.restPostMap(url, token, params);
        logger.info("用户支付密码手势密码验证是否设置接口，返回数据" + resultmap);
        return resultmap;
    }

    /**
     * 通过贷款品种判断是否需要进行人脸识别
     *
     * @param token
     * @param params
     * @return
     */
    public Map<String, Object> ifNeedFaceChkByTypCde(String token, Map<String, Object> params) {
        String url = appservernoauth + "/app/appserver/ifNeedFaceChkByTypCde";
        logger.info("通过贷款品种判断是否需要进行人脸识别接口，请求地址：" + url);
        logger.info("通过贷款品种判断是否需要进行人脸识别接口，请求数据：" + params);
        Map<String, Object> resultmap = HttpUtil.restPostMap(url, token, params);
        logger.info("通过贷款品种判断是否需要进行人脸识别接口，返回数据" + resultmap);
        return resultmap;
    }


    /**
     * 查询CRM中客户扩展信息（二）接口
     *
     * @param token
     * @param params
     * @return
     */
    @Override
    public Map<String, Object> getAllCustExtInfo(String token, Map<String, Object> params) {
        String url = appservernoauth + "/app/appserver/getAllCustExtInfo";
        logger.info("查询CRM中客户扩展信息（二）接口，请求地址：" + url);
        logger.info("查询CRM中客户扩展信息（二）接口，请求数据：" + params);
        Map<String, Object> resultmap = HttpUtil.restPostMap(url, token, params);
        logger.info("查询CRM中客户扩展信息（二）接口，返回数据" + resultmap);
        return resultmap;
    }

    /**
     * (PUT)支付密码设置接口
     *
     * @param token
     * @param paramMap
     * @return
     */
    public Map<String, Object> resetPayPasswd(String token, Map<String, Object> paramMap) {
        String url = appservernoauth + "/app/appserver/uauth/payPasswd";
        logger.info("支付密码设置接口的请求地址：" + url);
        logger.info("支付密码设置接口的请求数据：" + paramMap);
        Map<String, Object> restputmap = HttpUtil.restPutMap(url, token, paramMap);
        logger.info("支付密码设置接口返回数据：" + restputmap);
        return restputmap;
    }

    /**
     * 查询是否可以提交额度申请接口
     *
     * @param token
     * @param paramMap
     * @return
     */
    public Map<String, Object> ifEdAppl(String token, Map<String, Object> paramMap) {
        String url = appservernoauth + "/app/appserver/cmis/ifEdAppl";
        logger.info("查询是否可以提交额度申请接口请求地址：" + url);
        logger.info("查询是否可以提交额度申请接口请求数据：" + paramMap);
        Map<String, Object> ifedapplmap = HttpUtil.restGetMap(url, token, paramMap);
        logger.info("查询是否可以提交额度申请接口响应数据" + ifedapplmap);
        return ifedapplmap;
    }


    /**
     * 3.1.25查询客户的准入资格
     *
     * @param token
     * @param paramMap
     * @return
     */
    public Map<String, Object> getCustIsPass(String token, Map<String, Object> paramMap) {
        String url = appservernoauth + "/app/appserver/crm/cust/getCustIsPass";
        logger.info("查询客户准入资格url:" + url);
        logger.info("查询客户准入资格请求数据：" + paramMap);
        Map<String, Object> custispassmap = HttpUtil.restGetMap(url, token, paramMap);
        return custispassmap;
    }


    /**
     * (GET)额度申请
     *
     * @param token
     * @param edapplInfoMap
     * @return
     */
    public Map<String, Object> getEdApplInfo(String token, Map<String, Object> edapplInfoMap) {
        String url = appservernoauth + "/app/appserver/customer/getEdApplInfo";
        logger.info("额度申请接口请求地址：" + url);
        logger.info("额度申请接口请求数据：" + edapplInfoMap);
        Map<String, Object> edapplInfomap = HttpUtil.restGetMap(url, token, edapplInfoMap);
        return edapplInfomap;
    }


    /**
     * 6.1.29.(POST)订单协议确认
     *
     * @param token
     * @param reqSignMap
     * @return
     */
    public String updateOrderAgreement(String token, Map<String, Object> reqSignMap) {
        String url = appservernoauth + "/app/appserver/uauth/payPasswd";
        logger.info("顺逛订单协议确认接口请求地址：" + url);
        logger.info("顺逛订单协议确认接口请求数据：" + reqSignMap);
        String result = HttpUtil.restPutMap(url, token, reqSignMap).toString();
        return result;
    }


    /**
     * 6.1.30.(POST)订单合同确认
     *
     * @param token
     * @param reqConMap
     * @return
     */
    public String updateOrderContract(String token, Map<String, Object> reqConMap) {
        String url = appservernoauth + "/app/appserver/apporder/updateOrderContract";
        logger.info("顺逛订单合同确认接口请求地址：" + url);
        logger.info("顺逛订单合同确认接口请求数据：" + reqConMap);
        String result = HttpUtil.restPostMap(url, token, reqConMap).toString();
        return result;
    }


    /**
     * 6.1.30.(POST)订单合同确认
     *
     * @param token
     * @param commitmMap
     * @return
     */
    public String commitAppOrder(String token, Map<String, Object> commitmMap) {
        String url = appservernoauth + "/app/appserver/apporder/updateOrderContract";
        logger.info("顺逛订单合同确认接口请求地址：" + url);
        logger.info("顺逛订单合同确认接口请求数据：" + commitmMap);
        String result = HttpUtil.restGetMap(url, token, commitmMap).toString();
        return result;
    }

    //3.4.15.	(PUT)支付密码修改(知道原密码)
    public String updatePayPasswd(String token, Map<String, Object> paramMap) {
        String url = appservernoauth + "/app/appserver/uauth/updatePayPasswd";
        logger.info("支付密码修改接口请求地址：" + url);
        logger.info("支付密码修改接口请求参数：" + paramMap);
        String result = HttpUtil.restPutMap(url, token, paramMap).toString();
        logger.info("支付密码修改接口请求参数：" + result);
        return result;
    }

    //(PUT)实名认证修改密码
    public String updPwdByIdentity(String token, Map paramMap) {
        String url = appservernoauth + "/app/appserver/uauth/custVerifyUpdatePwd";
        logger.info("实名认证修改密码接口请求地址：" + url);
        logger.info("实名认证修改密码接口请求参数：" + paramMap);
        String result = HttpUtil.restPutMap(url, token, paramMap).toString();
        logger.info("实名认证修改密码接口请求参数：" + result);
        return result;
    }

    //(GET)确认支付密码验证
    public String validatePayPasswd(String token, Map<String, Object> paramMap) {
        String url = appservernoauth + "/app/appserver/uauth/validatePayPasswd";
        logger.info("确认支付密码验证接口请求地址：" + url);
        logger.info("确认支付密码验证接口请求参数：" + paramMap);
        String result = HttpUtil.restGetMap(url, token, paramMap).toString();
        logger.info("确认支付密码验证接口请求参数：" + result);
        return result;
    }

    //(GET)查询贷款详情（根据申请流水号）
    public Map<String, Object> queryApplLoanDetail(String token, Map<String, Object> paramMap) {
        String url = appservernoauth + "/app/appserver/apporder/queryAppLoanAndGoods";
        logger.info("查询贷款详情（根据申请流水号）接口请求地址：" + url);
        logger.info("查询贷款详情（根据申请流水号）接口请求参数：" + paramMap);
        Map<String, Object> queryApplLoanDetailMap = HttpUtil.restGetMap(url, token, paramMap);
        logger.info("查询贷款详情（根据申请流水号）接口返回数据" + queryApplLoanDetailMap);
        return queryApplLoanDetailMap;
    }

    //(GET)按贷款申请查询分期账单
    public Map<String, Object> queryApplListBySeq(String token, Map<String, Object> paramMap) {
        String url = appservernoauth + "/app/appserver/queryApplListBySeq";
        logger.info("查询贷款详情（根据申请流水号）接口请求地址：" + url);
        logger.info("查询贷款详情（根据申请流水号）接口请求参数：" + paramMap);
        Map<String, Object> queryApplListBySeqMap = HttpUtil.restGetMap(url, token, paramMap);
        logger.info("查询贷款详情（根据申请流水号）接口返回数据：" + queryApplListBySeqMap);
        return queryApplListBySeqMap;
    }

    //7.1.(POST) 欠款查询(参照核算接口5.1)
    public String getQFCheck(String token, Map<String, Object> paramMap) {
        String url = appservernoauth + "/app/appserver/customer/getQFCheck";
        logger.info("欠款查询接口请求地址：" + url);
        logger.info("欠款查询接口请求参数：" + paramMap);
        String qfMap = HttpUtil.restPostMap(url, token, paramMap).toString();
        logger.info("欠款查询接口返回数据：" + qfMap);
        return qfMap;
    }

    //(GET)全部还款试算（含息费、手续费、本金）
    public Map<String, Object> refundTrialAll(String token, Map<String, Object> paramMap) {
        String url = appservernoauth + "/app/appserver/newZdhkMoney";
        logger.info("全部还款试算接口请求地址：" + url);
        logger.info("全部还款试算接口请求参数：" + paramMap);
        Map<String, Object> refundTrialmap = HttpUtil.restGetMap(url, token, paramMap);
        logger.info("全部还款试算接口返回数据：" + refundTrialmap);
        return refundTrialmap;
    }

    //7.4.(POST)主动还款金额查询
    public String checkZdhkMoney(String token, Map<String, Object> paramMap) {
        String url = appservernoauth + "/app/appserver/customer/checkZdhkMoney";
        logger.info("主动还款金额查询接口请求地址：" + url);
        logger.info("主动还款金额查询接口请求参数：" + paramMap);
        String checkZdhkMoneymap = HttpUtil.restPostMap(url, token, paramMap).toString();
        logger.info("主动还款金额查询接口返回数据：" + checkZdhkMoneymap);
        return checkZdhkMoneymap;
    }

    // 3.1.16.(GET)额度查询
    public String getEdCheck(String token, Map<String, Object> paramMap) {
        String url = appservernoauth + "/app/appserver/getEdCheck";
        logger.info("额度查询接口请求地址：" + url);
        logger.info("额度查询接口请求参数：" + paramMap);
        String edCheckmap = HttpUtil.restGetMap(url, token, paramMap).toString();
        logger.info("额度查询接口返回数据：" + edCheckmap);
        return edCheckmap;
    }

    //(GET)根据流水号查询额度审批进度
    public String approvalProcessInfo(String token, Map<String, Object> paramMap) {
        String url = appservernoauth + "/app/appserver/cmis/approvalProcessBySeq";
        logger.info("根据流水号查询额度审批进度接口请求地址：" + url);
        logger.info("根据流水号查询额度审批进度接口请求参数：" + paramMap);
        String approvalProcessInfomap = HttpUtil.restGetMap(url, token, paramMap).toString();
        logger.info("根据流水号查询额度审批进度接口返回数据：" + approvalProcessInfomap);
        return approvalProcessInfomap;

    }

    //根据集团用户ID查询用户信息
    public String queryHaierUserInfo(String params) {
        String url = appservernoauth + "/app/appserver/uauth/queryHaierUserInfo?externUid=" + params;
        logger.info("根据集团用户ID查询用户信息接口，请求地址：" + url);
        logger.info("根据集团用户ID查询用户信息接口，请求数据：" + params);
        String result = HttpUtil.restGet(url);
        logger.info("根据集团用户ID查询用户信息接口，返回数据" + result);
        return result;
    }

    //3.4.21.	(POST)集团用户注册统一认证账户
    public Map<String, Object> saveUauthUsersByHaier(Map<String, Object> params) {
        String url = appservernoauth + "/app/appserver/uauth/saveUauthUsersByHaier";
        logger.info("集团用户注册统一认证账户接口, 请求地址：" + url);
        logger.info("集团用户注册统一认证账户接口, 请求数据：" + params);
        Map<String, Object> result = HttpUtil.restPostMap(url, params);
        logger.info("集团用户注册统一认证账户接口, 返回数据：" + result);
        return result;
    }

    //6.1.124.	(POST)保存第三方系统token
    public Map<String, Object> saveThirdPartToken(Map<String, Object> params) {
        String url = appservernoauth + "/app/appserver/saveThirdPartToken";
        logger.info("保存第三方系统token接口, 请求地址：" + url);
        logger.info("保存第三方系统token接口, 请求数据：" + params);
        Map<String, Object> result = HttpUtil.restPostMap(url, params);
        logger.info("保存第三方系统token接口, 返回数据：" + result);
        return result;
    }

    //3.1.29.	(GET)查询客户实名认证信息（根据USERID）(APP_PERSON)(CRM17)
    public Map<String, Object> queryPerCustInfo(String token, Map<String, Object> paramMap) {
        String url = appservernoauth + "/app/appserver/crm/cust/queryPerCustInfo";
        logger.info("查询客户实名认证信息（根据USERID）接口，请求地址：" + url);
        logger.info("查询客户实名认证信息（根据USERID）接口，请求数据：" + paramMap);
        Map<String, Object> resultmap = HttpUtil.restGetMap(url, token, paramMap);
        logger.info("查询客户实名认证信息（根据USERID）接口，返回数据" + resultmap);
        return resultmap;
    }

    //6.1.102.	(GET)额度申请校验
    public Map<String, Object> checkEdAppl(String token, Map<String, Object> paramMap) {
        String url = appservernoauth + "/app/appserver/validate/checkEdAppl";
        logger.info("额度申请校验接口，请求地址：" + url);
        logger.info("额度申请校验接口，请求数据：" + paramMap);
        Map<String, Object> resultmap = HttpUtil.restGetMap(url, token, paramMap);
        logger.info("额度申请校验接口，返回数据" + resultmap);
        return resultmap;
    }

    //3.4.22.	(POST) 验证并绑定集团用户（已绑定的不可用）
    public Map<String, Object> validateAndBindHaierUser(String token, Map<String, Object> paramMap) {
        String url = appservernoauth + "app/appserver/uauth/validateAndBindHaierUser";
        logger.info(" 验证并绑定集团用户接口, 请求地址：" + url);
        logger.info(" 验证并绑定集团用户接口, 请求数据：" + paramMap);
        Map<String, Object> result = HttpUtil.restPostMap(url, paramMap);
        logger.info(" 验证并绑定集团用户接口, 返回数据：" + result);
        return result;
    }

}
