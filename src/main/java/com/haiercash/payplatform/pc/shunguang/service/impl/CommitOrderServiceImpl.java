package com.haiercash.payplatform.pc.shunguang.service.impl;

import com.haiercash.commons.redis.Session;
import com.haiercash.commons.util.*;
import com.haiercash.payplatform.common.config.EurekaServer;
import com.haiercash.payplatform.common.dao.AppOrdernoTypgrpRelationDao;
import com.haiercash.payplatform.common.data.AppOrder;
import com.haiercash.payplatform.common.data.AppOrdernoTypgrpRelation;
import com.haiercash.payplatform.common.service.AppServerService;
import com.haiercash.payplatform.common.service.CmisApplService;
import com.haiercash.payplatform.common.service.GmService;
import com.haiercash.payplatform.common.utils.*;
import com.haiercash.payplatform.pc.shunguang.service.CommitOrderService;
import com.haiercash.payplatform.pc.shunguang.service.SgInnerService;
import com.haiercash.payplatform.service.AcquirerService;
import com.haiercash.payplatform.service.BaseService;
import com.haiercash.payplatform.service.OrderService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by yuanli on 2017/8/9.
 */
@Service
public class CommitOrderServiceImpl extends BaseService implements CommitOrderService {
    @Autowired
    private Session session;
    @Autowired
    private AppServerService appServerService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private AppOrdernoTypgrpRelationDao appOrdernoTypgrpRelationDao;
    @Autowired
    private CmisApplService cmisApplService;
    @Autowired
    private AcquirerService acquirerService;
    @Autowired
    private GmService gmService;

    /**
     * 订单提交
     * @param map
     * @return
     */
    @Override
    public Map<String, Object> commitOrder(Map<String, Object> map) {
        return null;
    }


    public Map<String, Object> commitAppOrder(String orderNo, String applSeq, String opType, String msgCode,
                                              String expectCredit, String typGrp, List<Map<String, Object>> riskList) {

        //整理风险信息
//        String riskJson = "";
//        if (!StringUtils.isEmpty(riskList) && riskList.size() > 0){
//            Map<String, Object> dealRiskListMap =  dealRiskList(riskList);
//            if (!HttpUtil.isSuccess(dealRiskListMap)){
//                return dealRiskListMap;
//            }
//            riskJson = (String) ((Map)dealRiskListMap.get("body")).get("riskJson");
//        }
        // 去收单查该订单的信息
        AppOrder apporder = acquirerService.getAppOrderFromAcquirer(applSeq, super.getChannelNo());
        if (apporder == null) {
            return fail("06", "贷款详情获取失败");
        }

        logger.info("订单提交业务类型:" + applSeq + ", typgrp:" + apporder.getTypGrp());
        // 检验可用额度是否满足申请金额
        if (!judgeApplAmt(apporder.getIdTyp(), apporder.getIdNo(), apporder.getApplyAmt(), super.getToken())) {
            logger.info("对不起，您的剩余额度低于借款金额，建议您可以在额度恢复后再借款");
            return fail("07", "对不起，您的剩余额度低于借款金额，建议您可以在额度恢复后再借款");
        }

        // 商户版直接提交核心
        if ("13".equals(super.getChannel())) {
            opType = "1";
            // 现金贷直接提交核心
        } else if ("02".equals(apporder.getTypGrp())) {
            opType = "1";
        } else {
            // 个人版商品贷，提交方式后台控制.
            if (!"13".equals(super.getChannel()) && !StringUtils.isEmpty(apporder.getGoodsCode())) {
                // 校验当前商品提交给商户还是直接提交核心
                Map<String, Object> needAndConfirm = gmService.getIsNeedSendAndIsConfirm(apporder.getGoodsCode());
                if (!HttpUtil.isSuccess(needAndConfirm)) {
                    return needAndConfirm;
                }
                Map<String, Object> needAndConfirmBody = (Map<String, Object>) needAndConfirm.get("body");
                String isConfirm = needAndConfirmBody.get("isConfirm").toString();
                // 如果是被退回订单，不提交给商户
                boolean isReturnOrder = "22".equals(apporder.getStatus());
                if ("Y".equals(isConfirm) && !isReturnOrder) {
                    opType = "2";
                } else {
                    opType = "1";
                }
            }
            // 个人版自定义商品需商户确认.
            if (!"13".equals(super.getChannel()) && StringUtils.isEmpty(apporder.getGoodsCode())) {
                opType = "2";
            }
        }
        logger.info("提交订单方式：" + applSeq + ", type:" + opType);
        apporder.setOrderNo(orderNo);
        apporder.setTypGrp(typGrp);
        apporder.setSource(super.getChannel());
        apporder.setChannelNo(super.getChannelNo());
        // 获取客户实名认证信息
        Map<String, Object> custInfo = cmisApplService
                .getSmrzInfoByCustNameAndIdNo(apporder.getCustName(), apporder.getIdNo());
        if (custInfo == null || !HttpUtil.isSuccess(custInfo)) {
            logger.info("获取用户实名信息失败, custName:" + apporder.getCustName() + ",idNo:" + apporder.getIdNo());
            return fail("03", "获取实名信息失败");
        }
        custInfo = HttpUtil.json2DeepMap(custInfo.get("body").toString());
        // 用户编号
        apporder.setCustNo(custInfo.get("custNo").toString());



        /**
         * 处理白名单类型及准入资格
         * 1、调crm 28接口，查询未实名认证客户的准入资格，如果返回不准入，则返回订单提交失败信息，提示不准入。
         * 2、调crm61接口查询白名单最高级别，如果跟订单的白名单级别不符，则将订单的白名单级别更新为查询出的最高级别
         * 3、将订单对象的白名单更新成查询出的最高级别并实例化到数据库
         * **/
        //获取准入资格
        Map<String, Object> isPassMap = this
                .getCustIsPassFromCrm(apporder.getCustName(), apporder.getIdNo(), apporder.getIndivMobile());
        logger.info("准入资格方法返回：" + isPassMap);
        JSONObject head = (JSONObject) isPassMap.get("head");
        String retFlag = head.getString("retFlag");
        String retMsg = head.getString("retMsg");
        if (!"00000".equals(retFlag)) {
            return fail(retFlag, retMsg);//返回crm的错误码
        }
        JSONObject bodyJson = (JSONObject) isPassMap.get("body");
        String isPass = String.valueOf(bodyJson.get("isPass"));
        if ("-1".equals(isPass)) {
            logger.info("准入资格校验失败，失败原因：" + isPassMap);
            return fail("86", "准入资格校验失败！");
        }

        // 订单号、短信验证码（来源为个人、贷款类型为一般消费品时，必填） 3为支付平台，处理方式同个人版
        // 星巢贷用户不校验短信验证码
        boolean isPerson = "14".equals(super.getChannel()) || StringUtils.isEmpty(super.getChannel());
        boolean isBigData = "34".equals(super.getChannelNo());
        boolean isEnoughSpend = "42".equals(super.getChannelNo());
        boolean isChannelNeedCode = isPerson || isBigData || isEnoughSpend;
        if (isChannelNeedCode && "02".equals(typGrp)) {
            String url = EurekaServer.CMISPROXY + "/api/appl/queryIsWsLoanTyp?typCde=" + apporder.getTypCde();
            String json = HttpUtil.restGet(url, super.getToken());
            Map<String, Object> typWsMap = HttpUtil.json2Map(json);
            if ("0".equals(typWsMap.get("result"))) {
                if (null == msgCode || "".equals(msgCode)) {
                    return fail("05", "短信验证码不能为空!");
                }
                //直接取订单中的手机号
                //个人版的手机号校验规则：验证的时候取绑定的手机号，并把绑定的手机号更新至订单。
                this.updateAppOrderMobile(apporder, getToken());
                //TODO！！！！校验短信验证码
//                String checkVerifyNoResult = FileSignUtil.checkVerifyNo(apporder.getIndivMobile(), msgCode);
//                if (!"00000".equals(checkVerifyNoResult)) {
//                    return fail("06", checkVerifyNoResult);
//                }
            }
        }

        // 查询当前用户信息
        //String clientId = "A0000055B0FB82";// TODO 这里暂时使用固定值，不会影响业务数据

        if (opType.equals("1")) {//3为支付平台，处理方式同个人版

            logger.debug("贷款申请提交, " + apporder.getApplSeq());
            apporder.setExpectCredit(expectCredit);//期望额度
            Map<String, Object> result = cmisApplService.commitBussiness(apporder.getApplSeq(), apporder);
            logger.debug("订单提交commitBussiness方法返回：" + result);
            return success(result);
//            HashMap<String, Object> hm = new HashMap<>();
//            if (HttpUtil.isSuccess(result)) {
//                try {
//                    if (!StringUtils.isEmpty(super.getChannel()) && super.getChannel().equals("16")) {
//                        // 如果是星巢贷的订单，应通知信贷更新营销人员信息
//                        // 判断渠道进件成功
//                        logger.debug(
//                                "星巢贷业务，营销人员信息：promCde=" + apporder.getPromCde() + "/promDesc=" + apporder
//                                        .getPromDesc()
//                                        + "/promPhone="
//                                        + apporder.getPromPhone());
//                        Map<String, Object> redStarRiskInfo = cmisApplService.updateRedStarRiskInfo(apporder);
//                        logger.debug("星巢贷业务通知信贷更新营销人员信息结果：" + redStarRiskInfo);
//                    }

//                    // 提交合同签章
//                    logger.debug("提交合同签章请求开始:" + orderNo);
//                    Map<String, Object> resultMap = caSignService.caSignRequest(orderNo, clientId, "1");
//                    logger.debug("提交合同签章请求结束:" + orderNo);
//                    logger.debug("合同签章结束后返回：" + resultMap);
//                    if ("err".equals(resultMap.get("resultCode"))) {
//                        logger.error("提交合同签章失败:" + resultMap.get("resultMsg"));
//                    }
//
//                    hm.put("orderNo", orderNo);
//                    hm.put("isDeleteAppl", "0");// 避免调用贷款取消接口
//                } catch (Exception e) {
//                    logger.error("提交合同签章并删除订单发生未知异常:" + e.getMessage());
//                    throw e;
//                }
                // 返回申请流水号
//                result.clear();
                /* 提交订单成功年后删除共同还款人替代影像信息
                if (apporder.getSource().equals("1")) {
                    attachService.deleteCommonReplaceImage(apporder.getApplSeq(), apporder.getCommonCustNo(), apporder.getTypCde());
                }*/
//                result.put("applSeq", apporder.getApplSeq());
//                result.put("applCde", apporder.getApplCde());
//                logger.debug("提交订单成功:" + orderNo);
//                return success(result);
//            } else {
//                Map<String, Object> mapHead = (Map) result.get("head");
//                return fail("15", "提交订单失败，" + mapHead.get("retMsg"));
//            }
        }
//        else if (opType.equals("2")) {
//            Map<String, Object> submitResult = orderService.submitOrder(orderNo, "1", riskJson);
//            if (!HttpUtil.isSuccess(submitResult)) {
//                logger.info("订单提交到商户失败,applSeq:" + applSeq + ",result:" + submitResult);
//            }
//            return submitResult;
//        }
        else {
            return fail("05", "订单提交类型不正确");
        }
    }

    public boolean judgeApplAmt(String idTyp, String idNo, String applyAmt, String token) {
        boolean resultFlag = false;
        logger.info("====检验可用额度是否满足申请金额====");
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("idTyp", idTyp);
        map.put("idNo", idNo);
        map.put("sysFlag", super.getChannel());
        map.put("channelNo", super.getChannelNo());
        logger.info("检验参数:" + map + ",申请金额:" + applyAmt);

        //调用信贷系统失败或者返回失败 直接返回true
        Map<String, Object> result = CmisUtil.getCmisResponse(CmisTradeCode.TRADECODE_ED_CHECK, token, map);
        /**
         * { "response": { "head": { "retMsg": "交易成功！", "retFlag": "00000" },
         * "body": { "crdComUsedAmt": 6001, "crdComAvailAmt": 0, "crdComAmt":
         * 10000, "crdNorUsedAmt": 6001, "crdAmt": 10000, "crdNorAvailAmt": 0,
         * "crdSts": 30, "crdNorAmt": 10000, "contDt": "2018-03-19" } } }
         */
        logger.info("信贷100016返回" + result);
        if (result == null) {
            logger.info("信贷系统额度查询失败");
            return true;
        }
        HashMap<String, Object> responseMap = (HashMap<String, Object>) result.get("response");
        if (!CmisUtil.getIsSucceed(result)) {
            return true;
        }
        HashMap<String, Object> bodyMap = (HashMap<String, Object>) responseMap.get("body");
        applyAmt = String.valueOf(StringUtils.isEmpty("applyAmt") ? 0 : applyAmt);
        String crdComAvailAmt = String.valueOf(StringUtils.isEmpty(bodyMap.get("crdComAvailAmt")) ? 0 : bodyMap.get("crdComAvailAmt"));
        logger.info("可用额度:" + crdComAvailAmt);
        if (new BigDecimal(crdComAvailAmt).compareTo(new BigDecimal(applyAmt)) >= 0) {
            logger.info("可用额度满足申请金额");
            return true;
        }
        logger.info("可用额度不满足申请金额");
        return false;
    }

    public Map<String, Object> getCustIsPassFromCrm(String custName, String idNo, String phone) {
        // 现金贷有部分品种需要商户确认的，商户确认提交不需要短信验证码
        String url = EurekaServer.CRM + "/app/crm/cust/getCustIsPass?custName=" + custName + "&certNo=" + idNo
                + "&phonenumber=" + phone;
        logger.info("CRM 28接口请求地址：" + url);
        String json = HttpUtil.restGet(url, super.getToken());
        logger.info("CRM 28接口返回：" + json);
        if (StringUtils.isEmpty(json)) {
            logger.info("CRM28接口【查询未实名认证客户的准入资格】请求获得结果为空！");
            return fail("10", "CRM28返回为空！");
        }
        Map<String, Object> isPassMap = HttpUtil.json2Map(json);
        return isPassMap;
    }

    //根据原始订单的客户信息，更新订单手机号为绑定手机号，并保存数据库
    public void updateAppOrderMobile(AppOrder order, String token) {
        logger.info("订单信息order=" + order);
        String custName = order.getCustName();
        String idNo = order.getIdNo();
        String mobile = getBindMobileByCustNameAndIdNo(custName, idNo, token);
        if (StringUtils.isEmpty(mobile)) {
            logger.info("客户绑定手机号查询失败，现读取实名认证手机号！");
            mobile = getMobileBySmrz(null, custName, idNo, token);
            if (StringUtils.isEmpty(mobile)) {
                logger.info("实名认证手机号查询也失败！！不再处理订单手机号！");

            } else {
                logger.info("订单最终处理结果为实名认证手机号，实名认证手机号查询为：" + mobile);
            }
        }
        logger.info("个人版订单最终更新的手机号为：" + mobile);

        //若手机号不为空，则将该手机号重新保存至数据库
        if (!StringUtils.isEmpty(mobile)) {
            order.setIndivMobile(mobile);
        }
    }

    /**
     * 根据客户姓名及身份证号查询统一认证手机号
     *
     * @param custName
     * @param idNo
     * @param token
     * @return
     */
    public String getBindMobileByCustNameAndIdNo(String custName, String idNo, String token) {
        //若身份证号或客户姓名为空，则返回null
        if (StringUtils.isEmpty(custName) || StringUtils.isEmpty(idNo)) {
            return null;
        }
        String url =
                EurekaServer.CRM + "/app/crm/cust/getUserIdByCustNameAndCertNo" + "?custName=" + custName + "&certNo="
                        + idNo;
        logger.info("CRM(74)==》请求url==" + url);
        String json = HttpUtil.restGet(url, token);
        logger.info("CRM(74)==》返回：" + json);
        if (StringUtils.isEmpty(json)) {
            logger.info("CRM(74)==》(getUserIdByCustNameAndCertNo)接口返回异常！请求处理被迫停止！");
            return null;
        }
        json = json.replaceAll("null", "\"\"");
        Map<String, Object> map = HttpUtil.json2Map(json);
        if (HttpUtil.isSuccess(map)) {
            Map<String, Object> mapBody = HttpUtil.json2Map(map.get("body").toString());
            logger.info("CRM(74)==》body体：" + mapBody);
            String userId = StringUtils.isEmpty(mapBody.get("userId")) ? null : mapBody.get("userId").toString();
            if (StringUtils.isEmpty(userId)) {
                logger.info("CRM(74)接口查询失败！userId查询为空！");
                return null;
            }
            return this.getBindMobileByUserId(userId, token);
        }
        return null;
    }

    /**
     * 通过用户Id查询统一认证手机号
     *
     * @param userId
     * @param token
     * @return
     */
    public String getBindMobileByUserId(String userId, String token) {
        if (StringUtils.isEmpty(userId)) {
            return null;
        }
        //加密后的str传入接口
        String str = com.haiercash.commons.util.EncryptUtil.simpleEncrypt(userId);
        String url = EurekaServer.UAUTH + "/app/uauth/getMobile" + "?userId=" + str;
        logger.info("统一认证1.21==》请求url==" + url);
        String json = HttpUtil.restGet(url, token);
        logger.info("统一认证1.21==》返回：" + json);
        if (StringUtils.isEmpty(json)) {
            logger.info("统一认证1.21==》(getMobile)接口返回异常！请求处理被迫停止！");
            return null;
        }
        json = json.replaceAll("null", "\"\"");
        Map<String, Object> map = HttpUtil.json2Map(json);
        if (HttpUtil.isSuccess(map)) {
            Map<String, Object> mapBody = HttpUtil.json2Map(map.get("body").toString());
            if (!StringUtils.isEmpty(mapBody.get("mobile"))) {
                logger.info("用户绑定手机号为：" + mapBody.get("mobile").toString());
                return mapBody.get("mobile").toString();
            }
        }
        return null;
    }

    /**
     * 获取实名认证的手机号
     *
     * @param userId
     * @param custName
     * @param idNo
     * @param token
     * @return
     */
    public String getMobileBySmrz(String userId, String custName, String idNo, String token) {
        String mobile = "";//要返回的手机号
        String cust_url = "";
        /** 调用实名认证接口5.13，查询手机号和客户号 **/
        //若用户id
        if (!StringUtils.isEmpty(userId)) {
            cust_url = EurekaServer.CRM + "/app/crm/cust/queryPerCustInfo" + "?userId=" + userId;
        } else if ((!StringUtils.isEmpty(custName)) && (!StringUtils.isEmpty(idNo))) {
            //若客户姓名和身份证号不为空，
            cust_url = EurekaServer.CRM + "/app/crm/cust/queryMerchCustInfo?certNo=" + idNo + "&custName="
                    + custName;
        }
        logger.info("CRM 实名认证接口请求地址：" + cust_url);
        String cust_json = HttpUtil.restGet(cust_url, super.getToken());
        if (StringUtils.isEmpty(cust_json)) {
            logger.info("CRM  该订单的实名认证信息接口查询失败，返回异常！");
            mobile = "";
        }
        Map<String, Object> custMap = HttpUtil.json2Map(cust_json);
        logger.info("CRM 实名认证（17或13）接口返回custMap==" + custMap);
        JSONObject custHeadObject = (JSONObject) custMap.get("head");
        String retFlag = custHeadObject.getString("retFlag");
        if (!"00000".equals(retFlag)) {
            logger.info("实名认证手机号查询失败，返回空！");
            return "";
        }
        Map<String, Object> custBodyMap = HttpUtil.json2Map(custMap.get("body").toString());
        mobile = StringUtils.isEmpty(custBodyMap.get("mobile")) ? null : custBodyMap.get("mobile").toString();
        if (StringUtils.isEmpty(mobile)) {
            logger.info("实名认证手机号查询失败！！");
            logger.info("统一认证绑定手机号及实名认证手机号查询全都失败！返回空");
        }
        logger.info("实名认证：mobile:" + mobile);
        return mobile;
    }

}
