package com.haiercash.payplatform.service.impl;

import com.haiercash.payplatform.config.EurekaServer;
import com.haiercash.payplatform.service.AppServerService;
import com.haiercash.payplatform.utils.AppServerUtils;
import com.haiercash.payplatform.utils.HttpUtil;
import com.haiercash.payplatform.service.BaseService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * Created by yuanli on 2017/7/26.
 */
@Service
public class AppServerServiceImpl extends BaseService implements AppServerService {
    public Log logger = LogFactory.getLog(getClass());

//    @Value("${app.rest.APPSERVER}")
//    protected String appserverurl;

    /**
     * 获取省市区
     *
     * @param token
     * @param params
     * @return
     */
    public Map<String, Object> getAreaInfo(String token, Map<String, Object> params) {
        String url = AppServerUtils.getAppServerUrl() + "/app/appserver/pub/crm/findDmAreaInfo";
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
        String url = AppServerUtils.getAppServerUrl() + "/app/appserver/crm/cust/getBankInfo?cardNo=" + cardNo;
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
        String url = AppServerUtils.getAppServerUrl() + "/app/appserver/smsSendVerify";
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
        String url = AppServerUtils.getAppServerUrl() + "/app/appserver/saveCardMsg";
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
        String url = AppServerUtils.getAppServerUrl() + "/app/appserver/smsVerify";
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
        String url = AppServerUtils.getAppServerUrl() + "/app/appserver/crm/cust/fCiCustRealThreeInfo";
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
        String url = AppServerUtils.getAppServerUrl() + "/app/appserver/uauth/updateMobile";
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
        String channel = (String) params.get("channel");
        String channelNo = (String) params.get("channelNo");
        String custNo = (String) params.get("custNo");// 客户编号
        String attachType = (String) params.get("attachType");// 影像类型
        String attachName = (String) params.get("attachName");// 人脸照片
        String md5 = (String) params.get("md5");//文件md5码
        String filePath = params.get("filePath").toString();//路径
        String id = (String) params.get("id");
        //String applSeq = (String) params.get("applSeq");
        String idNo = (String) params.get("idNo");

        String url;
        if (StringUtils.isEmpty(id) && StringUtils.isEmpty(idNo)) {
            url = AppServerUtils.getAppServerUrl() + "/app/appserver/attachUploadPersonByFilePath?custNo=" + custNo + "&attachType=" + attachType
                    + "&attachName=" + attachName + "&md5=" + md5 + "&filePath=" + filePath;
        } else if (StringUtils.isEmpty(id)) {
            url = AppServerUtils.getAppServerUrl() + "/app/appserver/attachUploadPersonByFilePath?custNo=" + custNo + "&attachType=" + attachType
                    + "&attachName=" + attachName + "&md5=" + md5 + "&filePath=" + filePath + "&idNo=" + idNo;
        } else if (StringUtils.isEmpty(idNo)) {
            url = AppServerUtils.getAppServerUrl() + "/app/appserver/attachUploadPersonByFilePath?custNo=" + custNo + "&attachType=" + attachType
                    + "&attachName=" + attachName + "&md5=" + md5 + "&filePath=" + filePath + "&id=" + id;
        } else {
            url = AppServerUtils.getAppServerUrl() + "/app/appserver/attachUploadPersonByFilePath?custNo=" + custNo + "&attachType=" + attachType
                    + "&attachName=" + attachName + "&md5=" + md5 + "&filePath=" + filePath + "&id=" + id + "&idNo=" + idNo;
        }

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
        String url = AppServerUtils.getAppServerUrl() + "/app/appserver/faceCheckByFaceValue";
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
        String url = AppServerUtils.getAppServerUrl() + "/app/appserver/uauth/validateUserFlag";
        logger.info("用户支付密码手势密码验证是否设置接口，请求地址：" + url);
        logger.info("用户支付密码手势密码验证是否设置接口，请求数据：" + params);
        Map<String, Object> resultmap = HttpUtil.restGetMap(url, token, params);
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
        String url = AppServerUtils.getAppServerUrl() + "/app/appserver/ifNeedFaceChkByTypCde";
        logger.info("通过贷款品种判断是否需要进行人脸识别接口，请求地址：" + url);
        logger.info("通过贷款品种判断是否需要进行人脸识别接口，请求数据：" + params);
        Map<String, Object> resultmap = HttpUtil.restGetMap(url, token, params);
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
        String url = AppServerUtils.getAppServerUrl() + "/app/appserver/getAllCustExtInfo";
        logger.info("查询CRM中客户扩展信息（二）接口，请求地址：" + url);
        logger.info("查询CRM中客户扩展信息（二）接口，请求数据：" + params);
        Map<String, Object> resultmap = HttpUtil.restGetMap(url, token, params);
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
        String url = AppServerUtils.getAppServerUrl() + "/app/appserver/uauth/payPasswd";
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
        String url = AppServerUtils.getAppServerUrl() + "/app/appserver/cmis/ifEdAppl";
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
        String url = AppServerUtils.getAppServerUrl() + "/app/appserver/crm/cust/getCustIsPass";
        logger.info("查询客户准入资格url:" + url);
        logger.info("查询客户准入资格请求数据：" + paramMap);
        Map<String, Object> custispassmap = HttpUtil.restGetMap(url, token, paramMap);
        logger.info("查询客户准入资格返回数据：" + custispassmap);
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
        String verifyMobile = (String) edapplInfoMap.get("verifyMobile");
        String verifyNo = (String) edapplInfoMap.get("verifyNo");
        String url = "";
        if("46".equals(this.getChannelNo())){
            if(verifyMobile != null && !"".equals(verifyMobile) && verifyNo!= null && !"".equals(verifyNo)){
                url = AppServerUtils.getAppServerUrl() + "/app/appserver/customer/getEdApplInfo/needVerify";
            }else{
                url = AppServerUtils.getAppServerUrl() + "/app/appserver/customer/getEdApplInfo";
            }
        }else{
            url = AppServerUtils.getAppServerUrl() + "/app/appserver/customer/getEdApplInfo";
        }

        logger.info("额度申请接口请求地址：" + url);
        logger.info("额度申请接口请求数据：" + edapplInfoMap);
        Map<String, Object> edapplInfomap = HttpUtil.restGetMap(url, token, edapplInfoMap);
        logger.info("额度申请接口返回数据：" + edapplInfomap);
        return edapplInfomap;
    }


    /**
     * 6.1.29.(POST)订单协议确认
     *
     * @param token
     * @param reqSignMap
     * @return
     */
    public Map<String, Object> updateOrderAgreement(String token, Map<String, Object> reqSignMap) {
        String url = AppServerUtils.getAppServerUrl() + "/app/appserver/apporder/updateOrderAgreement";
        logger.info("订单协议确认接口请求地址：" + url);
        logger.info("订单协议确认接口请求数据：" + reqSignMap);
        Map<String, Object> result = HttpUtil.restPutMap(url, token, reqSignMap);
        logger.info("订单协议确认接口返回数据：" + result);
        return result;
    }


    /**
     * 6.1.30.(POST)订单合同确认
     *
     * @param token
     * @param reqConMap
     * @return
     */
    public Map<String, Object> updateOrderContract(String token, Map<String, Object> reqConMap) {
        String url = AppServerUtils.getAppServerUrl() + "/app/appserver/apporder/updateOrderContract";
        logger.info("订单合同确认接口请求地址：" + url);
        logger.info("订单合同确认接口请求数据：" + reqConMap);
        Map<String, Object> result = HttpUtil.restPostMap(url, token, reqConMap);
        logger.info("订单合同确认接口返回数据：" + result);
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
        String url = AppServerUtils.getAppServerUrl() + "/app/appserver/apporder/updateOrderContract";
        logger.info("订单合同确认接口请求地址：" + url);
        logger.info("订单合同确认接口请求数据：" + commitmMap);
        String result = HttpUtil.restGetMap(url, token, commitmMap).toString();
        logger.info("订单合同确认接口返回数据：" + result);
        return result;
    }

    //3.4.15.	(PUT)支付密码修改(知道原密码)
    public Map<String, Object> updatePayPasswd(String token, Map<String, Object> paramMap) {
        String url = AppServerUtils.getAppServerUrl() + "/app/appserver/uauth/updatePayPasswd";
        logger.info("支付密码修改接口请求地址：" + url);
        logger.info("支付密码修改接口请求参数：" + paramMap);
        Map<String, Object> map = HttpUtil.restPutMap(url, token, paramMap);
        logger.info("支付密码修改接口返回数据：" + map);
        return map;
    }

    //(PUT)实名认证修改密码
    public Map<String, Object> updPwdByIdentity(String token, Map paramMap) {
        String url = AppServerUtils.getAppServerUrl() + "/app/appserver/uauth/custVerifyUpdatePayPwd";
        logger.info("实名认证修改密码接口请求地址：" + url);
        logger.info("实名认证修改密码接口请求参数：" + paramMap);
        Map<String, Object> map = HttpUtil.restPutMap(url, token, paramMap);
        logger.info("实名认证修改密码接口返回数据：" + map);
        return map;
    }

    //(GET)确认支付密码验证
    public Map<String, Object> validatePayPasswd(String token, Map<String, Object> paramMap) {
        String url = AppServerUtils.getAppServerUrl() + "/app/appserver/uauth/validatePayPasswd";
        logger.info("确认支付密码验证接口请求地址：" + url);
        logger.info("确认支付密码验证接口请求参数：" + paramMap);
        Map<String, Object> result = HttpUtil.restGetMap(url, token, paramMap);
        logger.info("确认支付密码验证接口返回数据：" + result);
        return result;
    }

    //(post)查询贷款详情（根据申请流水号）  收单
    public Map<String, Object> queryApplLoanDetail(Map<String, Object> paramMap) {
        String url = EurekaServer.ACQUIRER + "/api/appl/getApplInfo";
        logger.info("查询贷款详情（根据申请流水号）接口请求地址：" + url);
        logger.info("查询贷款详情（根据申请流水号）接口请求参数：" + paramMap);
        Map<String, Object> queryApplLoanDetailMap = HttpUtil.restPostMap(url, paramMap);
        logger.info("查询贷款详情（根据申请流水号）接口返回数据" + queryApplLoanDetailMap);
        return queryApplLoanDetailMap;
    }
    //(GET)查询贷款详情（根据申请流水号）  APP后台
    public Map<String, Object> queryApplLoanDetail(String token,Map<String, Object> paramMap) {
        String url = AppServerUtils.getAppServerUrl() + "/app/appserver/apporder/queryAppLoanAndGoods";
        logger.info("查询贷款详情（根据申请流水号）接口请求地址：" + url);
        logger.info("查询贷款详情（根据申请流水号）接口请求参数：" + paramMap);
        Map<String, Object> queryApplLoanDetailMap = HttpUtil.restGetMap(url, token, paramMap);
        logger.info("查询贷款详情（根据申请流水号）接口返回数据" + queryApplLoanDetailMap);
        return queryApplLoanDetailMap;
    }
    //(GET)按贷款申请查询分期账单
    public Map<String, Object> queryApplListBySeq(String token, Map<String, Object> paramMap) {
        String url = AppServerUtils.getAppServerUrl() + "/app/appserver/queryApplListBySeq";
        logger.info("查询贷款详情（根据申请流水号）接口请求地址：" + url);
        logger.info("查询贷款详情（根据申请流水号）接口请求参数：" + paramMap);
        Map<String, Object> queryApplListBySeqMap = HttpUtil.restGetMap(url, token, paramMap);
        logger.info("查询贷款详情（根据申请流水号）接口返回数据：" + queryApplListBySeqMap);
        return queryApplListBySeqMap;
    }

    //7.1.(POST) 欠款查询(参照核算接口5.1)
    public Map<String, Object> getQFCheck(String token, Map<String, Object> paramMap) {
        String url = AppServerUtils.getAppServerUrl() + "/app/appserver/customer/getQFCheck";
        logger.info("欠款查询接口请求地址：" + url);
        logger.info("欠款查询接口请求参数：" + paramMap);
        Map<String, Object> qfMap = HttpUtil.restPostMap(url, token, paramMap);
        logger.info("欠款查询接口返回数据：" + qfMap);
        return qfMap;
    }

    //(GET)全部还款试算（含息费、手续费、本金）
    public Map<String, Object> refundTrialAll(String token, Map<String, Object> paramMap) {
        String url = AppServerUtils.getAppServerUrl() + "/app/appserver/newZdhkMoney";
        logger.info("全部还款试算接口请求地址：" + url);
        logger.info("全部还款试算接口请求参数：" + paramMap);
        Map<String, Object> refundTrialmap = HttpUtil.restPostMap(url, token, paramMap);
        logger.info("全部还款试算接口返回数据：" + refundTrialmap);
        return refundTrialmap;
    }

    //7.4.(POST)主动还款金额查询
    public Map<String, Object> checkZdhkMoney(String token, Map<String, Object> paramMap) {
        String url = AppServerUtils.getAppServerUrl() + "/app/appserver/customer/checkZdhkMoney";
        logger.info("主动还款金额查询接口请求地址：" + url);
        logger.info("主动还款金额查询接口请求参数：" + paramMap);
        Map<String, Object> checkZdhkMoneymap = HttpUtil.restPostMap(url, token, paramMap);
        logger.info("主动还款金额查询接口返回数据：" + checkZdhkMoneymap);
        return checkZdhkMoneymap;
    }

    // 3.1.16.(GET)额度查询
    public Map<String, Object> getEdCheck(String token, Map<String, Object> paramMap) {
        String url = AppServerUtils.getAppServerUrl() + "/app/appserver/getEdCheck";
        logger.info("额度查询接口请求地址：" + url);
        logger.info("额度查询接口请求参数：" + paramMap);
        Map<String, Object> edCheckmap = HttpUtil.restGetMap(url, token, paramMap);
        logger.info("额度查询接口返回数据：" + edCheckmap);
        return edCheckmap;
    }
    //6.1.133.	(GET)获取个人中心信息
    public Map<String, Object> getBillCheck(String token, Map<String, Object> paramMap) {
        String url = AppServerUtils.getAppServerUrl() + "/app/appserver/getPersonalCenterInfo";
        logger.info("个人中心信息接口请求地址：" + url);
        logger.info("个人中心信息接口请求参数：" + paramMap);
        Map<String, Object> billCheckMap = HttpUtil.restGetMap(url, token, paramMap);
        logger.info("个人中心信息返回数据：" + billCheckMap);
        return billCheckMap;
    }

    //(GET)根据流水号查询额度审批进度
    public Map<String, Object> approvalProcessInfo(String token, Map<String, Object> paramMap) {
        String url = AppServerUtils.getAppServerUrl() + "/app/appserver/cmis/approvalProcessBySeq";
        logger.info("根据流水号查询额度审批进度接口请求地址：" + url);
        logger.info("根据流水号查询额度审批进度接口请求参数：" + paramMap);
        Map<String, Object> approvalProcessInfomap = HttpUtil.restGetMap(url, token, paramMap);
        logger.info("根据流水号查询额度审批进度接口返回数据：" + approvalProcessInfomap);
        return approvalProcessInfomap;
    }

    //根据集团用户ID查询用户信息
    public String queryHaierUserInfo(String params) {
        String url = AppServerUtils.getAppServerUrl() + "/app/appserver/uauth/queryHaierUserInfo?externUid=" + params;
        logger.info("根据集团用户ID查询用户信息接口，请求地址：" + url);
        logger.info("根据集团用户ID查询用户信息接口，请求数据：" + params);
        String result = HttpUtil.restGet(url);
        logger.info("根据集团用户ID查询用户信息接口，返回数据" + result);
        return result;
    }

    //3.4.21.	(POST)集团用户注册统一认证账户
    public Map<String, Object> saveUauthUsersByHaier(Map<String, Object> params) {
        String url = AppServerUtils.getAppServerUrl() + "/app/appserver/uauth/saveUauthUsersByHaier";
        logger.info("集团用户注册统一认证账户接口, 请求地址：" + url);
        logger.info("集团用户注册统一认证账户接口, 请求数据：" + params);
        Map<String, Object> result = HttpUtil.restPostMap(url, params);
        logger.info("集团用户注册统一认证账户接口, 返回数据：" + result);
        return result;
    }

    //6.1.124.	(POST)保存第三方系统token
    public Map<String, Object> saveThirdPartToken(Map<String, Object> params) {
        String url = AppServerUtils.getAppServerUrl() + "/app/appserver/saveThirdPartToken";
        logger.info("保存第三方系统token接口, 请求地址：" + url);
        logger.info("保存第三方系统token接口, 请求数据：" + params);
        Map<String, Object> result = HttpUtil.restPostMap(url, params);
        logger.info("保存第三方系统token接口, 返回数据：" + result);
        return result;
    }

    //3.1.29.	(GET)查询客户实名认证信息（根据USERID）(APP_PERSON)(CRM17)
    public Map<String, Object> queryPerCustInfo(String token, Map<String, Object> paramMap) {
        String url = AppServerUtils.getAppServerUrl() + "/app/appserver/crm/cust/queryPerCustInfo";
        logger.info("查询客户实名认证信息（根据USERID）接口，请求地址：" + url);
        logger.info("查询客户实名认证信息（根据USERID）接口，请求数据：" + paramMap);
        Map<String, Object> resultmap = HttpUtil.restGetMap(url, token, paramMap);
        logger.info("查询客户实名认证信息（根据USERID）接口，返回数据" + resultmap);
        return resultmap;
    }

    //6.1.102.	(GET)额度申请校验
    public Map<String, Object> checkEdAppl(String token, Map<String, Object> paramMap) {
        String url = AppServerUtils.getAppServerUrl() + "/app/appserver/validate/checkEdAppl";
        logger.info("额度申请校验接口，请求地址：" + url);
        logger.info("额度申请校验接口，请求数据：" + paramMap);
        Map<String, Object> resultmap = HttpUtil.restGetMap(url, token, paramMap);
        logger.info("额度申请校验接口，返回数据" + resultmap);
        return resultmap;
    }

    //3.4.22.	(POST) 验证并绑定集团用户（已绑定的不可用）
    public Map<String, Object> validateAndBindHaierUser(String token, Map<String, Object> paramMap) {
        String url = AppServerUtils.getAppServerUrl() + "/app/appserver/uauth/validateAndBindHaierUser";
        logger.info(" 验证并绑定集团用户接口, 请求地址：" + url);
        logger.info(" 验证并绑定集团用户接口, 请求数据：" + paramMap);
        Map<String, Object> result = HttpUtil.restPostMap(url, paramMap);
        logger.info(" 验证并绑定集团用户接口, 返回数据：" + result);
        return result;
    }

    //(GET)查询贷款品种所需的影像列表(不包含共同还款人影像)
    public Map<String, Object> pLoanTypImages(String token, Map<String, Object> paramMap) {
        String url = AppServerUtils.getAppServerUrl() + "/app/appserver/cmis/pLoanTypImages";
        logger.info("查询贷款品种所需的影像列表接口，请求地址：" + url);
        logger.info("查询贷款品种所需的影像列表接口，请求数据：" + paramMap);
        Map<String, Object> map = HttpUtil.restGetMap(url, token, paramMap);
        logger.info("查询贷款品种所需的影像列表接口，返回数据：" + map);
        return map;
    }

    //(GET)影像列表按类型查询-个人版
    public Map<String, Object> attachTypeSearchPerson(String token, Map<String, Object> paramMap) {
        String url = AppServerUtils.getAppServerUrl() + "/app/appserver/attachTypeSearchPerson";
        logger.info("影像列表按类型查询接口，请求地址：" + url);
        logger.info("影像列表按类型查询接口，请求数据：" + paramMap);
        Map<String, Object> map = HttpUtil.restGetMap(url, token, paramMap);
        logger.info("影像列表按类型查询接口，返回数据：" + map);
        return map;
    }

    //(GET)根据影像文件ID查询影像文件的路径
    public Map<String, Object> getFilePathByFileId(String token, Map<String, Object> paramMap) {
        String url = AppServerUtils.getAppServerUrl() + "/app/appserver/getFilePathByFileId";
        logger.info("根据影像文件ID查询影像文件的路径接口，请求地址：" + url);
        logger.info("根据影像文件ID查询影像文件的路径接口，请求数据：" + paramMap);
        Map<String, Object> map = HttpUtil.restGetMap(url, token, paramMap);
        logger.info("根据影像文件ID查询影像文件的路径接口，返回数据：" + map);
        return map;
    }

    //(POST)修改保存客户所有扩展信息(CRM85)
    public Map<String, Object> saveAllCustExtInfo(String token, Map<String, Object> paramMap) {
        String url = AppServerUtils.getAppServerUrl() + "/app/appserver/crm/cust/saveAllCustExtInfo";
        logger.info("修改保存客户所有扩展信息接口，请求地址：" + url);
        logger.info("修改保存客户所有扩展信息接口，请求数据：" + paramMap);
        Map<String, Object> map = HttpUtil.restPostMap(url, token, paramMap);
        logger.info("修改保存客户所有扩展信息接口，返回数据：" + map);
        return map;
    }

    //(POST)新增/修改 联系人(CRM6)
    public Map<String, Object> saveCustFCiCustContact(String token, Map<String, Object> paramMap) {
        String url = AppServerUtils.getAppServerUrl() + "/app/appserver/crm/saveCustFCiCustContact";
        logger.info("新增/修改 联系人接口，请求地址：" + url);
        logger.info("新增/修改 联系人接口，请求数据：" + paramMap);
        Map<String, Object> map = HttpUtil.restPostMap(url, token, paramMap);
        logger.info("新增/修改 联系人接口，返回数据：" + map);
        return map;
    }

    //(post)信息完整查询接口
    @Override
    public Map<String, Object> checkIfMsgComplete(String token, Map<String, Object> paramMap) {
        String tag = (String) paramMap.get("tag");
        String businessType = (String) paramMap.get("businessType");
        if (StringUtils.isEmpty(tag) && StringUtils.isEmpty(businessType)) {
            return null;
        }
        String url = AppServerUtils.getAppServerUrl() + "/app/appserver/" + tag + "/" + businessType + "/checkIfMsgComplete";
        logger.info("信息完整查询接口，请求地址：" + url);
        logger.info("信息完整查询接口，请求参数：" + paramMap);
        Map<String, Object> map = HttpUtil.restPostMap(url, token, paramMap);
        logger.info("信息完整查询接口，返回数据：" + map);
        return map;
    }

    //1.27(GET) 根据集团用户id查询用户信息
    public Map<String, Object> getUserId(String token, Map<String, Object> paramMap) {
        String url = EurekaServer.UAUTH + "/app/uauth/queryHaierUserInfo";
        logger.info("根据集团用户id查询用户信息，请求地址：" + url);
        logger.info("根据集团用户id查询用户信息，请求数据：" + paramMap);
        Map<String, Object> map = HttpUtil.restGetMap(url, token, paramMap);
        logger.info("根据集团用户id查询用户信息，返回数据" + map);
        return map;
    }

    //(DELETE)影像删除
    @Override
    public Map<String, Object> attachDelete(String token, Map<String, Object> paramMap) {
        int id = (int) paramMap.get("id");
        if(StringUtils.isEmpty(id)){
            return null;
        }
        String url = AppServerUtils.getAppServerUrl() + "/app/appserver/attachDeletePerson?id="+id;
        logger.info("影像删除接口，请求地址：" + url);
        String param = HttpUtil.restDelete(url, null,200);
        Map<String, Object> stringObjectMap = HttpUtil.json2Map(param);
        return stringObjectMap;
    }

    //用户是否注册
    @Override
    public Map<String, Object> isRegister(String token, Map<String, Object> paramMap) {
        String url = AppServerUtils.getAppServerUrl() + "/app/appserver/uauth/isRegister";
        logger.info("用户是否注册接口，请求地址：" + url);
        logger.info("用户是否注册接口，请求数据：" + paramMap);
        Map<String, Object> map = HttpUtil.restGetMap(url, token, paramMap);
        logger.info("用户是否注册接口，返回数据：" + map);
        return map;
    }

    //客户登录密码设置、修改（验证码）
    @Override
    public Map<String, Object> custUpdatePwd(String token, Map<String, Object> paramMap) {
        String url = AppServerUtils.getAppServerUrl() + "/app/appserver/uauth/custUpdatePwd";
        logger.info("客户登录密码设置、修改（验证码）接口，请求地址：" + url);
        Map<String, Object> map = HttpUtil.restPutMap(url, token, paramMap);
        return map;
    }

    //查询全部贷款信息列表-个人版
    @Override
    public Map<String, Object> getDateAppOrderPerson(String token, Map<String, Object> paramMap) {
        String url = AppServerUtils.getAppServerUrl() + "/app/appserver/apporder/getDateAppOrderPerson";
        logger.info("查询全部贷款信息列表-个人版接口，请求地址：" + url);
        Map<String, Object> map = HttpUtil.restGetMap(url, token, paramMap);
        return map;
    }

    //查询待提交订单列表
    @Override
    public Map<String, Object> getWtjAppOrderCust(String token, Map<String, Object> paramMap) {
        String url = AppServerUtils.getAppServerUrl() + "/app/appserver/apporder/getWtjAppOrderCust";
        logger.info("查询待提交订单列表接口，请求地址：" + url);
        Map<String, Object> map = HttpUtil.restGetMap(url, token, paramMap);
        return map;
    }

    //待还款信息查询
    @Override
    public Map<String, Object> queryApplAllByIdNo(String token, Map<String, Object> paramMap) {
        String url = AppServerUtils.getAppServerUrl() + "/app/appserver/apporder/queryApplListByIdNo";
        logger.info("待还款信息查询接口，请求地址：" + url);
        Map<String, Object> map = HttpUtil.restGetMap(url, token, paramMap);
        return map;
    }


    //查询已提交贷款申请列表
    @Override
    public Map<String, Object> queryApplListPerson(String token, Map<String, Object> paramMap) {
        String url = AppServerUtils.getAppServerUrl() + "/app/appserver/cmis/queryApplListPerson";
        logger.info("查询已提交贷款申请列表接口，请求地址：" + url);
        Map<String, Object> map = HttpUtil.restGetMap(url, token, paramMap);
        return map;
    }
    //查询订单详情（暂不用）
    @Override
    public Map<String, Object> queryOrderInfo(String token, Map<String, Object> paramMap) {
        String url = AppServerUtils.getAppServerUrl() + "/app/appserver/apporder/getAppOrderAndGoods";
        logger.info("查询订单详情接口，请求地址：" + url);
        Map<String, Object> map = HttpUtil.restGetMap(url, token, paramMap);
        logger.info("查询订单详情接口，响应数据：" + map.toString());
        return map;
    }

    //删除订单
    @Override
    public Map<String, Object> deleteAppOrder(String token, Map<String, Object> paramMap) {
        String orderNo = (String) paramMap.get("orderNo");
        if(StringUtils.isEmpty(orderNo)){
            return null;
        }
        String url = AppServerUtils.getAppServerUrl() + "/app/appserver/apporder/deleteAppOrder?orderNo="+orderNo;
        logger.info("删除订单接口，请求地址：" + url);
        Map<String, Object> stringObjectMap = HttpUtil.restPostMap(url, token, paramMap);
        return stringObjectMap;
    }

    //6.2.12.	(GET) 录单校验（个人版）
    @Override
    public Map<String, Object> getCustInfoAndEdInfoPerson(String token, Map<String, Object> paramMap) {
        String url = AppServerUtils.getAppServerUrl() + "/app/appserver/apporder/getCustInfoAndEdInfoPerson";
        logger.info("录单校验接口，请求地址：" + url);
        logger.info("录单校验接口，请求数据：" + paramMap);
        Map<String, Object> resultmap = HttpUtil.restGetMap(url, token, paramMap);
        logger.info("录单校验接口，返回数据" + resultmap);
        return resultmap;
    }

    //6.1.53.	(GET) 是否允许申请贷款
    @Override
    public Map<String, Object> queryBeyondContral(String token, Map<String, Object> paramMap) {
        String url = AppServerUtils.getAppServerUrl() + "/app/appserver/apporder/queryBeyondContral";
        logger.info("是否允许申请贷款接口，请求地址：" + url);
        logger.info("是否允许申请贷款接口，请求数据：" + paramMap);
        Map<String, Object> resultmap = HttpUtil.restGetMap(url, token, paramMap);
        logger.info("是否允许申请贷款接口，返回数据" + resultmap);
        return resultmap;
    }

    //6.1.146.	(POST) 上传影像到信贷系统
    @Override
    public Map<String, Object> uploadImg2CreditDep(String token, Map<String, Object> paramMap) {
        String url = AppServerUtils.getAppServerUrl() + "/app/appserver/apporder/uploadImg2CreditDep";
        logger.info("上传影像到信贷系统接口, 请求地址：" + url);
        logger.info("上传影像到信贷系统接口, 请求数据：" + paramMap);
        Map<String, Object> result = HttpUtil.restPostMap(url, paramMap);
        logger.info("上传影像到信贷系统接口, 返回数据：" + result);
        return result;
    }

    //6.1.103.	(POST)批量还款试算(免token)
    @Override
    public Map<String, Object> getBatchPaySs(String token, Map<String, Object> paramMap) {
        String url = AppServerUtils.getAppServerUrl() + "/app/appserver/customer/getBatchPaySs";
        logger.info("批量还款试算接口, 请求地址：" + url);
        logger.info("批量还款试算接口, 请求数据：" + paramMap);
        Map<String, Object> result = HttpUtil.restPostMap(url, paramMap);
        logger.info("批量还款试算接口, 返回数据：" + result);
        return result;
    }

    //6.1.17.	(POST)还款试算
    @Override
    public Map<String, Object> getPaySs(String token, Map<String, Object> paramMap) {
        String url = AppServerUtils.getAppServerUrl() + "/app/appserver/customer/getPaySs";
        logger.info("还款试算接口, 请求地址：" + url);
        logger.info("还款试算接口, 请求数据：" + paramMap);
        Map<String, Object> result = HttpUtil.restPostMap(url,token, paramMap);
        logger.info("还款试算接口, 返回数据：" + result);
        return result;
    }

    //3.1.79.(POST)额度申请进度查询（最新的进度 根据idNo查询）
    public Map<String, Object> getEdApplProgress(String token, Map<String, Object> paramMap) {
        String url = AppServerUtils.getAppServerUrl() + "/app/appserver/apporder/getEdApplProgress";
        logger.info("额度申请进度查询（最新的进度 根据idNo查询）,请求地址：" + url);
        Map<String, Object> map = HttpUtil.restPostMap(url, token, paramMap);
        logger.info("额度申请进度查询（最新的进度 根据idNo查询）返回数据:"+map);
        return map;
    }

    //1.32(GET) 根据统一认证userid查询用户信息(查集团userId)
    public Map<String, Object> findUserByUserid(String token, Map<String, Object> paramMap) {
        String url = EurekaServer.UAUTH + "/app/uauth/findUserByUserid";
        logger.info("根据统一认证userid查询用户信息,请求地址：" + url);
        Map<String, Object> map = HttpUtil.restGetMap(url, token, paramMap);
        logger.info("根据统一认证userid查询用户信息返回信息："+map);
        return map;
    }

    //48、(GET)根据身份证号查询客户基本信息和实名认证信息(userId)
    public Map<String, Object> getCustInfoByCertNo(String token, Map<String, Object> paramMap) {
        String url = EurekaServer.CRM + "/app/crm/cust/getCustInfoByCertNo";
        logger.info("根据身份证号查询客户基本信息和实名认证信息(userId),请求地址："+url);
        Map<String, Object> map = HttpUtil.restGetMap(url, token, paramMap);
        logger.info("根据身份证号查询客户基本信息和实名认证信息(userId)返回数据："+map);
        return map;
    }
    //OM-1108 根据applSeq查询商城订单号
    public Map<String,Object> getorderNo(String token,Map<String, Object> paramMap){
        String url = EurekaServer.ORDER + "/api/order/getMallOrderNoByApplSeq";
        logger.info("根据applSeq查询商城订单号："+paramMap);
        Map<String, Object> map = HttpUtil.restGetMap(url, token, paramMap);
        logger.info("根据applSeq查询商城订单号返回数据："+map);
        return map;
    }


    //3.4.21.	(POST)集团用户注册统一认证账户
    @Override
    public Map<String, Object> saveUauthUsers(String token, Map<String, Object> params) {
        String url = AppServerUtils.getAppServerUrl() + "/app/appserver/uauth/saveUauthUsers";
        logger.info("用户注册接口, 请求地址：" + url);
        logger.info("用户注册接口, 请求数据：" + params);
        Map<String, Object> result = HttpUtil.restPostMap(url, params);
        logger.info("用户注册接口, 返回数据：" + result);
        return result;
    }

    //6.1.133.	(GET)获取个人\信息
    public Map<String, Object> getPersonalCenterInfo(String token,Map<String, Object> paramMap){
        String url = AppServerUtils.getAppServerUrl() + "/app/appserver/getPersonalCenterInfo";
        logger.info("获取个人信息接口,请求地址："+url);
        logger.info("获取个人信息接口,请求参数"+paramMap);
        Map<String, Object> result = HttpUtil.restGetMap(url, token,paramMap);
        logger.info("查询客户实名认证信息接口, 返回数据：" + result);
        return result;


    }

    //6.1.31.	(GET)影像下载
    @Override
    public Map<String, Object> attachPic(String token, Map<String, Object> paramMap) {
        String url = AppServerUtils.getAppServerUrl() + "/app/appserver/attachPic?attachId="+(Integer)paramMap.get("attachId");
        logger.info("影像下载口, 请求地址：" + url);
        logger.info("影像下载口, 请求数据：" + paramMap);
//        Map<String, Object> result = HttpUtil.restGetMap(url, token,paramMap);
        Map<String, Object> result = HttpUtil.restGetMap(url);
        logger.info("影像下载口, 返回数据：" + result);
        return result;
    }

    //6.1.104.	(POST)提交签章请求
    public Map<String, Object> caRequest(String token, Map<String, Object> map){
        String url = EurekaServer.APPCA + "/app/appserver/caRequest";
        logger.info("提交签章请求接口, 请求地址：" + url);
        logger.info("提交签章请求接口, 请求数据：" + map);
        Map<String, Object> result = HttpUtil.restPostMap(url, map);
        logger.info("提交签章请求接口, 返回数据：" + result);
        return result;
    }

    //6.1.130.	(GET) 查询贷款品种信息列表
    public Map<String, Object> pLoanTypList(String token, Map<String, Object> paramMap) {
        String url = AppServerUtils.getAppServerUrl() + "/app/appserver/cmis/pLoanTypList";
        logger.info("查询贷款品种信息列表，请求地址：" + url);
        logger.info("查询贷款品种信息列表，请求数据：" + paramMap);
        Map<String, Object> map = HttpUtil.restGetMap(url, token, paramMap);
        logger.info("查询贷款品种信息列表，返回数据" + map);
        return map;
    }

    @Override
    public Map<String, Object> updateListRiskInfo(String token, Map<String, Object> paramMap) {
        String url = AppServerUtils.getAppServerUrl() + "/app/appserver/updateListRiskInfo";
        logger.info("外部风险信息采集，请求地址：" + url);
        logger.info("外部风险信息采集，请求数据：" + paramMap);
        Map<String, Object> map = HttpUtil.restPostMap(url, token, paramMap);
        logger.info("外部风险信息采集，返回数据：" + map);
        return map;
    }
    //3.4.11(GET) 实名认证
    public Map<String, Object> identify(String token, Map<String, Object> paramMap) {
        String url = AppServerUtils.getAppServerUrl() + "/app/appserver/uauth/identify";
        logger.info("修改密码实名认证，请求地址：" + url);
        logger.info("修改密码实名认证，请求数据：" + paramMap);
        Map<String, Object> map = HttpUtil.restGetMap(url, token, paramMap);
        logger.info("修改密码实名认证，返回数据" + map);
        return map;
    }

    @Override
    public Map<String, Object> getMoxieByApplseq(String token, Map<String, Object> paramMap) {
        String url = EurekaServer.APPSERVERNOAUTHNEW + "/app/appserver/getMoxieByApplseq";
        logger.info("根据申请流水号查询是否做过魔蝎认证，请求地址：" + url);
        logger.info("根据申请流水号查询是否做过魔蝎认证，请求数据：" + paramMap);
        Map<String, Object> map = HttpUtil.restGetMap(url, token, paramMap);
        logger.info("根据申请流水号查询是否做过魔蝎认证，返回数据" + map);
        return map;
    }

    //3.1.13(GET)查询所有贷款用途列表(APP)
    @Override
    public Map<String, Object> getPurpose(String token, Map<String, Object> paramMap) {
        String url = AppServerUtils.getAppServerUrl() + "/app/appserver/crm/cust/getPurpose";
        logger.info("查询所有贷款用途列表，请求地址：" + url);
        logger.info("查询所有贷款用途列表，请求数据：" + paramMap);
        Map<String, Object> map = HttpUtil.restGetMap(url, token, paramMap);
        logger.info("查询所有贷款用途列表，返回数据" + map);
        return map;
    }

    public Map<String, Object> updateRiskInfo(String token, Map<String, Object> paramMap) {
        String url = EurekaServer.APPSERVERNOAUTHNEW + "/app/appserver/updateRiskInfo";
        logger.info("外部风险信息采集接口，请求地址：" + url);
        logger.info("外部风险信息采集接口，请求数据：" + paramMap);
        Map<String, Object> map = HttpUtil.restPostMap(url, token, paramMap);
        logger.info("外部风险信息采集接口，返回数据：" + map);
        return map;
    }
}
