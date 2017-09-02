package com.haiercash.payplatform.pc.shunguang.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.haiercash.commons.redis.Session;
import com.haiercash.payplatform.common.config.EurekaServer;
import com.haiercash.payplatform.common.dao.AppOrdernoTypgrpRelationDao;
import com.haiercash.payplatform.common.dao.CooperativeBusinessDao;
import com.haiercash.payplatform.common.data.AppOrder;
import com.haiercash.payplatform.common.data.AppOrdernoTypgrpRelation;
import com.haiercash.payplatform.common.data.CooperativeBusiness;
import com.haiercash.payplatform.common.service.AppServerService;
import com.haiercash.payplatform.common.service.CmisApplService;
import com.haiercash.payplatform.common.service.GmService;
import com.haiercash.payplatform.common.service.OrderManageService;
import com.haiercash.payplatform.common.utils.*;
import com.haiercash.payplatform.pc.shunguang.service.CommitOrderService;
import com.haiercash.payplatform.pc.shunguang.service.SgInnerService;
import com.haiercash.payplatform.service.AcquirerService;
import com.haiercash.payplatform.service.BaseService;
import com.haiercash.payplatform.service.OrderService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;
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
    @Autowired
    private SgInnerService sgInnerService;
    @Autowired
    private CooperativeBusinessDao cooperativeBusinessDao;
    @Autowired
    private OrderManageService orderManageService;
    @Value("${app.shunguang.sg_typLevelTwo}")
    protected String sg_typLevelTwo;

    /**
     * 订单提交
     * @param map
     * @return
     */
    @Override
    public Map<String, Object> commitOrder(Map<String, Object> map)  throws Exception{
        logger.info("订单提交****************开始");
        String channel = (String) map.get("channel");
        String channelNo = (String) map.get("channelNo");
        String token = (String) map.get("token");
        String orderNo = (String) map.get("orderNo");
        String applSeq = (String) map.get("applSeq");
        String paypwd = (String) map.get("paypwd");
        BigDecimal longitude = new BigDecimal(0);
        BigDecimal latitude = new BigDecimal(0);
        if(!StringUtils.isEmpty(map.get("longitude"))){
            longitude = (BigDecimal)map.get("longitude");//经度
        }
        if(!StringUtils.isEmpty(map.get("latitude"))){
            latitude = (BigDecimal)map.get("latitude");//维度
        }
        String area = (String) map.get("area");//区域
        //缓存获取（放开）
        Map<String, Object> cacheMap = session.get(token, Map.class);
        if (cacheMap == null || "".equals(cacheMap)) {
            logger.info("Jedis数据获取失败");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
//        String key0 = "applSeq" + applSeq;
//        if(cacheMap.containsKey(key0)){
//            return success();
//        }
        ObjectMapper objectMapper = new ObjectMapper();
        AppOrder appOrder = null;
        String typCde = "";
        try {
            logger.info("缓存数据获取");
            appOrder = objectMapper.readValue(cacheMap.get("apporder").toString(), AppOrder.class);
            logger.info("提交订单信息appOrder:" + appOrder);
            if(appOrder == null){
                return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
            }
            typCde = appOrder.getTypCde();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //参数非空校验
        if(StringUtils.isEmpty(channel) || StringUtils.isEmpty(channelNo) || StringUtils.isEmpty(token)
                || StringUtils.isEmpty(orderNo) || StringUtils.isEmpty(applSeq)){
            logger.info("channel:" + channel + "  channelNo:" + channelNo + "   token:" + token
                + "  orderNo:" + orderNo + "  applSeq:" + applSeq /*+ "  longitude:" + longitude + "  latitude:" + latitude + "  area:" + area*/);
            logger.info("前台获取数据有误");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        }

        //根据用户中心token获取统一认证userId
        String userId = sgInnerService.getuserId(token);
        if(StringUtils.isEmpty(userId)){
            logger.info("根据用户中心token获取统一认证userId失败");
            return fail(ConstUtil.ERROR_CODE, "获取内部注册信息失败");
        }
        //TODO!!!!
        //String userId = cacheMap.get("userId").toString();

        //根据userId获取客户编号
        logger.info("获取客户实名信息");
        Map<String, Object> custMap = new HashMap<String, Object>();
        custMap.put("userId", userId);
        custMap.put("channel", channel);
        custMap.put("channelNo", channelNo);
        Map<String, Object> custInforesult = appServerService.queryPerCustInfo(token, custMap);
        if (!HttpUtil.isSuccess(custInforesult) ) {
            logger.info("订单提交，获取实名信息失败");
            return fail(ConstUtil.ERROR_CODE, "获取实名信息失败");
        }
        String payresultstr = com.alibaba.fastjson.JSONObject.toJSONString(custInforesult);
        com.alibaba.fastjson.JSONObject custresult = com.alibaba.fastjson.JSONObject.parseObject(payresultstr).getJSONObject("body");
        String custNo = (String) custresult.get("custNo");
        String custName = (String) custresult.get("custName");
        String certNo = (String) custresult.get("certNo");
        String mobile = (String) custresult.get("mobile");
        logger.info("订单提交，获取客户实名信息成功");

        //1.支付密码验证
        HashMap<String, Object> pwdmap = new HashMap<>();
        String userIdEncrypt = EncryptUtil.simpleEncrypt(userId);
        String payPasswdEncrypt = EncryptUtil.simpleEncrypt(paypwd);
        pwdmap.put("userId", userIdEncrypt);
        pwdmap.put("payPasswd", payPasswdEncrypt);
        pwdmap.put("channel", channel);
        pwdmap.put("channelNo", channelNo);
        Map<String, Object> resmap = appServerService.validatePayPasswd(token, pwdmap);
        if(!HttpUtil.isSuccess(resmap)){
            logger.info("订单提交，支付密码验证失败");
            return fail("error", "支付密码校验失败");
        }
        logger.info("订单提交，支付密码验证成功");

        //2.合同签订
        Map<String, Object> contractmap =  signContract(custName, certNo, applSeq, mobile, typCde, channelNo, token);
        if(!HttpUtil.isSuccess(contractmap)){
            logger.info("订单提交，合同签订失败");
            return contractmap;
        }
        logger.info("订单提交，合同签订成功");

        //3.影像上传
        Map<String, Object> uploadimgmap = new HashMap<String, Object>();
        uploadimgmap.put("custNo", custNo);//客户编号
        uploadimgmap.put("applSeq", applSeq);//订单号
        uploadimgmap.put("channel", channel);
        uploadimgmap.put("channelNo", channelNo);
        Map<String,Object> uploadimgresultmap = appServerService.uploadImg2CreditDep(token, uploadimgmap);
        if(!HttpUtil.isSuccess(uploadimgresultmap)){
            logger.info("订单提交，影像上传失败失败");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        }
        logger.info("订单提交，影像上传成功");


        //5.订单提交
        // 获取订单对象
        AppOrdernoTypgrpRelation relation = appOrdernoTypgrpRelationDao.selectByOrderNo(orderNo);
        if (relation == null) {
            logger.debug("订单编号为" + orderNo + "的订单不存在！");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        }
        applSeq = relation.getApplSeq();

        //风险信息上送
        ArrayList<Map<String, Object>> arrayList = new ArrayList<>();
        ArrayList<String> listOne = new ArrayList<>();
        ArrayList<String> listTwo = new ArrayList<>();
        HashMap<String, Object> hashMap = new HashMap<String, Object>();
        HashMap<String, Object> hashMapOne = new HashMap<String, Object>();
        HashMap<String, Object> hashMapTwo = new HashMap<String, Object>();
        String longLatitude = "经度" + longitude + "维度" + latitude;
        logger.info("经维度解析前:" + longLatitude);
        String longLatitudeEncrypt = com.haiercash.commons.util.EncryptUtil.simpleEncrypt(longLatitude);
        logger.info("经维度解析后:" + longLatitudeEncrypt);
        listOne.add(longLatitudeEncrypt);
        hashMapOne.put("idNo", com.haiercash.commons.util.EncryptUtil.simpleEncrypt(certNo));
        hashMapOne.put("name", com.haiercash.commons.util.EncryptUtil.simpleEncrypt(custName));
        hashMapOne.put("mobile", com.haiercash.commons.util.EncryptUtil.simpleEncrypt(mobile));
        hashMapOne.put("dataTyp", "04");
        hashMapOne.put("source", "2");
        hashMapOne.put("applSeq", applSeq);
        hashMapOne.put("Reserved6", applSeq);
        hashMapOne.put("content", listOne);
        listTwo.add(com.haiercash.commons.util.EncryptUtil.simpleEncrypt(area));
        hashMapTwo.put("idNo", com.haiercash.commons.util.EncryptUtil.simpleEncrypt(certNo));
        hashMapTwo.put("name", com.haiercash.commons.util.EncryptUtil.simpleEncrypt(custName));
        hashMapTwo.put("mobile", com.haiercash.commons.util.EncryptUtil.simpleEncrypt(mobile));
        hashMapTwo.put("dataTyp", "A504");
        hashMapTwo.put("source", "2");
        hashMapTwo.put("applSeq", applSeq);
        hashMapTwo.put("Reserved6", applSeq);
        hashMapTwo.put("content", listTwo);
        arrayList.add(hashMapOne);
        arrayList.add(hashMapTwo);
        hashMap.put("list", arrayList);
//        hashMap.put("channel", channel);
//        hashMap.put("channelNo", channelNo);
        Map<String, Object> stringObjectMap = appServerService.updateListRiskInfo(token, hashMap);
        if (stringObjectMap == null) {
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        }
//        Map setcustTagHeadMap = (HashMap<String, Object>) stringObjectMap.get("head");
        Map<String, Object> setcustTagMapFlag = (HashMap<String, Object>) stringObjectMap.get("response");
        Map<String, Object> setcustTagHeadMap = (Map<String, Object>) setcustTagMapFlag.get("head");
        String setcustTagHeadMapFlag = (String) setcustTagHeadMap.get("retFlag");
        if (!"00000".equals(setcustTagHeadMapFlag)) {
            String retMsg = (String) setcustTagHeadMap.get("retMsg");
            return fail(ConstUtil.ERROR_CODE, retMsg);
        }

        Map<String, Object> result = commitAppOrder(orderNo, applSeq, "1", null, null, relation.getTypGrp());
        logger.info("订单提交，返回数据：" + result);
        //签章成功进行redis存储
//        String key = "applSeq" + applSeq;
//        cacheMap.put(key, key);
//        session.set(token, cacheMap);


        return result;
    }


    private String encrypt(String data, String channelNo,String tradeCode) throws Exception {
        //byte[] bytes = key.getBytes();
        //获取渠道私钥
        logger.info("获取渠道" + channelNo + "私钥");
        CooperativeBusiness cooperativeBusiness = cooperativeBusinessDao.selectBycooperationcoed(channelNo);
        if (cooperativeBusiness == null) {
            throw new RuntimeException("渠道" + channelNo + "私钥获取失败");
        }
        String publicKey = cooperativeBusiness.getRsapublic();//获取私钥

        //1.生成随机密钥
        String password = DesUtil.productKey();
        //2.des加密
        String desData = Base64Utils.encode(DesUtil.encrypt(data.getBytes(), password));
        //3.加密des的key
        String password_ = Base64Utils.encode(RSAUtils.encryptByPublicKey(password.getBytes(), publicKey));
        org.json.JSONObject reqjson = new org.json.JSONObject();
        reqjson.put("applyNo", UUID.randomUUID().toString().replace("-", ""));
        reqjson.put("channelNo", super.getChannelNo());
        reqjson.put("tradeCode", tradeCode);
        reqjson.put("data", desData);
        reqjson.put("key", password_);
        return reqjson.toString();
    }


    public Map<String, Object> commitAppOrder(String orderNo, String applSeq, String opType, String msgCode,
                                              String expectCredit, String typGrp) {

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
        if ("13".equals(super.getChannel()) || "11".equals(super.getChannel())) {
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
        }
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

    //合同签订
    public Map<String, Object> signContract(String custName, String custIdCode, String applseq, String phone, String typCde,
                                            String channelNo, String token) {
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("typCdeList", typCde);
        Map<String, Object> loanmap = appServerService.pLoanTypList(token, paramMap);
        if(!HttpUtil.isSuccess(loanmap)){
            return loanmap;
        }
        List<Map<String, Object>> loanbody = (List<Map<String, Object>>) loanmap.get("body");
        String typLevelTwo = "";
        for (int i = 0; i < loanbody.size(); i++) {
            Map<String, Object> m = loanbody.get(i);
            typLevelTwo = m.get("levelTwo").toString();
        }
        logger.info("贷款品种小类：" + typLevelTwo);
        //合同签订
        JSONObject order = new JSONObject();
        order.put("custName", custName);// 客户姓名
        order.put("idNo", custIdCode);// 客户身份证号
        order.put("indivMobile", phone);// 客户手机号码
        order.put("applseq", applseq);// 请求流水号
        order.put("typLevelTwo", typLevelTwo);// typLevelTwo 贷款品种小类
        order.put("typCde", typCde);// 贷款品种代码

        JSONObject orderJson = new JSONObject();// 订单信息json串
        orderJson.put("order", order.toString());

        Map map = new HashMap();// 征信
        map.put("custName", custName);// 客户姓名
        map.put("custIdCode", custIdCode);// 客户身份证号
        map.put("applseq", applseq);// 请求流水号
        map.put("signType", "SHUNGUANG_H5");// 签章类型
        map.put("flag", "0");//1 代表合同  0 代表 协议
        map.put("orderJson", orderJson.toString());
        map.put("sysFlag", "11");// 系统标识：支付平台
        map.put("channelNo", channelNo);
        Map camap = appServerService.caRequest(null, map);

        //征信签章
        JSONObject orderZX = new JSONObject();
        orderZX.put("custName", custName);// 客户姓名
        orderZX.put("idNo", custIdCode);// 客户身份证号
        orderZX.put("indivMobile", phone);// 客户手机号码
        orderZX.put("applseq", applseq);// 请求流水号

        JSONObject orderZXJson = new JSONObject();// 订单信息json串
        orderZXJson.put("order", orderZX.toString());

        Map reqZXJson = new HashMap();// 征信
        reqZXJson.put("custName", custName);// 客户姓名
        reqZXJson.put("custIdCode", custIdCode);// 客户身份证号
        reqZXJson.put("applseq", applseq);// 请求流水号
        reqZXJson.put("signType", "credit");// 签章类型
        reqZXJson.put("flag", "0");//1 代表合同  0 代表 协议
        reqZXJson.put("orderJson", orderZXJson.toString());
        reqZXJson.put("sysFlag", "11");// 系统标识：支付平台
        map.put("channelNo", channelNo);
        Map zxmap = appServerService.caRequest(token, reqZXJson);

        //合同与征信签章都成功
        if(HttpUtil.isSuccess(camap) && HttpUtil.isSuccess(zxmap)){
            logger.info("订单提交，签章成功");
            return success();
        } else {
            return fail(ConstUtil.ERROR_CODE, "签章失败");
        }
    }
}
