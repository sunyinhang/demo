package com.haiercash.payplatform.service.impl;

import com.haiercash.commons.redis.Session;
import com.haiercash.payplatform.common.dao.AppOrdernoTypgrpRelationDao;
import com.haiercash.payplatform.common.dao.SignContractInfoDao;
import com.haiercash.payplatform.common.data.AppOrder;
import com.haiercash.payplatform.common.data.AppOrdernoTypgrpRelation;
import com.haiercash.payplatform.common.data.SignContractInfo;
import com.haiercash.payplatform.config.EurekaServer;
import com.haiercash.payplatform.service.*;
import com.haiercash.payplatform.utils.*;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by yuanli on 2017/9/20.
 */
@Service
public class CommonPageServiceImpl extends BaseService implements CommonPageService {
    @Value("${app.other.appServer_page_url}")
    protected String appServer_page_url;
    @Autowired
    private Session session;
    @Autowired
    private CmisApplService cmisApplService;
    @Autowired
    private AcquirerService acquirerService;
    @Autowired
    private GmService gmService;
    @Autowired
    private AppServerService appServerService;
    @Autowired
    private SignContractInfoDao signContractInfoDao;
    @Autowired
    private AppOrdernoTypgrpRelationDao appOrdernoTypgrpRelationDao;
    @Autowired
    private OrderService orderService;

    /**
     * 合同展示
     * @param map
     * @return
     */
    @Override
    public Map<String, Object> showcontract(Map<String, Object> map) throws Exception{
        String flag = (String) map.get("flag");

        String token = super.getToken();
        if(StringUtils.isEmpty(flag) || StringUtils.isEmpty(token)){
            logger.info("前台传入参数为空");
            logger.info("flag:" + flag + "  token:" + token);
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        }

        Map<String, Object> cacheMap = session.get(token, Map.class);
        if (cacheMap == null || "".equals(cacheMap)) {
            logger.info("Jedis数据获取失败");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        String custName = (String) cacheMap.get("custName");
        String custNo = (String) cacheMap.get("custNo");
        String certNo = (String) cacheMap.get("certNo");

        Map result = new HashMap();
        String url = "";
        //征信协议展示
        if("1".equals(flag)){
            String name = new String(Base64.encode(custName.getBytes()), "UTF-8");
            name= URLEncoder.encode(name,"UTF-8");
            url = "/app/appserver/edCredit?custName=" + name + "&certNo=" + certNo;
            result.put("url", url);
        }
        //签章协议展示
        if("2".equals(flag)){
            String applseq = (String) map.get("applseq");
            if(StringUtils.isEmpty(applseq)){
                logger.info("applseq:" + applseq);
                return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
            }
            url = "/app/appserver/contract?custNo=" + custNo + "&applseq=" + applseq;
            result.put("url", url);
        }
        //注册协议展示
        if("3".equals(flag)){
            String name = new String(Base64.encode(custName.getBytes()), "UTF-8");
            name= URLEncoder.encode(name,"UTF-8");
            url = "/app/appserver/register?custName=" + name;
            result.put("url", url);
        }
        logger.info("合同跳转url：" + url);
        return success(result);
    }

    @Override
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
            return result;
            //return success(result);
        }
        else {
            return fail("05", "订单提交类型不正确");
        }
    }

    @Override
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

        SignContractInfo signContractInfo = signContractInfoDao.getSignContractInfo(typCde);
        if(signContractInfo == null){
            return fail(ConstUtil .ERROR_CODE, "贷款品种"+ typCde +"没有配置签章类型");
        }
        String signType = signContractInfo.getSigntype();//签章类型
        Map map = new HashMap();// 合同
        map.put("custName", custName);// 客户姓名
        map.put("custIdCode", custIdCode);// 客户身份证号
        map.put("applseq", applseq);// 请求流水号
        map.put("signType", signType);// 签章类型
        map.put("flag", "0");//1 代表合同  0 代表 协议
        map.put("orderJson", orderJson.toString());
        map.put("sysFlag", "11");// 系统标识：支付平台
        map.put("channelNo", channelNo);
        Map camap = appServerService.caRequest(null, map);

        if ("46".equals(channelNo)) {//46：顺逛走征信
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
            if (!HttpUtil.isSuccess(zxmap)) {
                return fail(ConstUtil.ERROR_CODE, "征信签章失败");
            }
        }


        //合同与征信签章都成功
        if (HttpUtil.isSuccess(camap)) {
            logger.info("订单提交，签章成功");
            return success();
        } else {
            return fail(ConstUtil.ERROR_CODE, "签章失败");
        }
    }

    @Override
    public Map<String, Object> saveAppOrderInfo(AppOrder appOrder) {

        appOrder.setApplyDt(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        appOrder.setIsConfirmAgreement("0");// 0-未确认
        appOrder.setIsConfirmContract("0");// 0-未确认
        appOrder.setProPurAmt("0");// 商品总额，默认为0
        appOrder.setIsCustInfoCompleted("N");// 个人信息是否完整 默认为N 否

        // 计算首付比例
        this.calcFstPct(appOrder);
        // 把门店信息写入订单
        this.updateStoreInfo(appOrder, super.getToken());
        // 把销售代表信息写入订单
        this.updateSalesInfo(appOrder, super.getToken());
        // 把客户实名信息写入订单。注意：订单可能修改放款支行信息
        String accBchCde = appOrder.getAccAcBchCde();
        String accBchName = appOrder.getAccAcBchName();
        this.updateCustRealInfo(appOrder, super.getToken());

        if (!StringUtils.isEmpty(accBchCde) && !StringUtils.isEmpty(accBchName)) {
            // 把还款银行卡支行信息保存到crm
            String url = EurekaServer.CRM + "/app/crm/cust/updateAccBch";
            Map<String, String> params = new HashMap<>();
            params.put("custNo", appOrder.getCustNo());
            params.put("cardNo", appOrder.getApplCardNo());
            params.put("accBchCde", accBchCde);
            params.put("accBchName", accBchName);
            logger.debug("把还款银行卡支行信息保存到crm: " + params);
            String json = HttpUtil.restPut(url, super.getToken(), JSONObject.valueToString(params), 200);
            logger.debug("把还款银行卡支行信息保存到crm, 返回参数json==" + json);
            Map<String, Object> custExtInfoMap = HttpUtil.json2Map(json);
            if (custExtInfoMap == null) {
                return fail("99", "把还款银行卡支行信息保存到crm：未知错误");
            }/* else if (!RestUtil.isSuccess(custExtInfoMap)) {
                return fail("99", "crm系统设置还款银行卡支行信息：" + CmisUtil.getErrMsg(custExtInfoMap));
            }*/
            // 把支行信息写回订单
            appOrder.setAccAcBchCde(accBchCde);
            appOrder.setAccAcBchName(accBchName);
        }

        // 把贷款品种信息写入订单
        this.updateTypInfo(appOrder, super.getToken());
        // 还款方式
        String url2 = EurekaServer.CMISPROXY + "/api/pLoanTyp/typMtd?typCde=" + appOrder.getTypCde();
        String json2 = HttpUtil.restGet(url2, super.getToken());
        if (StringUtils.isEmpty(json2)) {
            return fail("01", "查询还款方式失败");
        } else {
            List<Map<String, Object>> typResultList = HttpUtil.json2List(json2);
            appOrder.setMtdCde(typResultList.get(0).get("mtdCde").toString());
        }

        boolean ifAccessEd = false;
        try {
            logger.info("个人版保存订单校验银行卡限额策略, custNo:" + appOrder.getCustNo());
            ifAccessEd = this.ifAccessEd(appOrder);
        } catch (Exception e) {
            return fail("47", "用户卡信息中不存在该银行卡");
        }
        if (!ifAccessEd) {
            return fail("46", "您的每月最高还款额已超过扣款卡的单次最大扣款限额，建议更换还款卡！");
        }
        // 个人版：扫码分期提交给商户(S)，现金贷提交给信贷系统(N)
        //String autoFlag = appOrder.getTypGrp().equals("02") ? "N" : "S";

        String orderNo = "";
        String applSeq = "";
        if ("02".equals(appOrder.getTypGrp())) {//现金贷

            // 现金贷
            AppOrdernoTypgrpRelation relation = appOrdernoTypgrpRelationDao.selectByOrderNo(appOrder.getOrderNo());
            if (relation == null) {
                return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "要更新的订单不存在！");
            }

            // 收单系统获取订单详情
            //AppOrder appOrder0 = acquirerService.getAppOrderFromAcquirer(relation.getApplSeq(), super.getChannelNo());

            Map<String, Object> resultResponseMap = acquirerService.cashLoan(appOrder, relation);
            if (CmisUtil.isSuccess(resultResponseMap)) {
                Map<String, Object> bodyMap = (Map<String, Object>) ((Map<String, Object>) resultResponseMap
                        .get("response")).get("body");
                applSeq = (String) bodyMap.get("applSeq");
                orderNo = (String) bodyMap.get("applSeq");
                this.saveRelation(applSeq, appOrder.getTypGrp(), applSeq, appOrder.getCustNo());
                Map responsemap = (Map<String, Object>) resultResponseMap.get("response");
                Map bodymap = (Map<String, Object>) responsemap.get("body");
                return success(bodymap);
            } else {
                return (Map<String, Object>) resultResponseMap.get("response");
            }
        } else {
            Map<String, Object> resultMap = orderService.saveOrUpdateAppOrder(appOrder, null);
            logger.info("订单保存结果输出：" + resultMap);
            if (HttpUtil.isSuccess(resultMap)) {
                Map<String, Object> bodyMap = (Map<String, Object>) resultMap.get("body");
                orderNo = (String) bodyMap.get("orderNo");
                applSeq = (String) bodyMap.get("applSeq");
                this.saveRelation(orderNo, appOrder.getTypGrp(), applSeq, appOrder.getCustNo());
                return resultMap;
            } else {
                return resultMap;
            }
        }

        //return success();
    }

    @Override
    public String getCode(String token, Map<String, Object> citymap) {
        String cityCode = "";
        // 根据区号获取市
        Map<String, Object> result = appServerService.getAreaInfo(token, citymap);
        String retFlag = (String) ((Map<String, Object>)result.get("head")).get("retFlag");
        if (!"00000".equals(retFlag)) {
            return cityCode;
        }
        List<Map<String, Object>> body = (List<Map<String, Object>>) result.get("body");

        for (int i = 0; i < body.size(); i++) {
            Map<String, Object> m = body.get(i);
            cityCode = m.get("areaCode").toString();
        }
        return cityCode;
    }

    /**
     * 查询贷款用途
     * @param params
     * @return
     */
    @Override
    public Map<String, Object> getPurpose(Map<String, Object> params) {
        String token = super.getToken();
        Map<String, Object> resultMap = appServerService.getPurpose(token, params);
        return resultMap;
    }


    /**
     * 把门店信息写入订单
     *
     * @param order 订单对象
     */
    public void updateStoreInfo(AppOrder order, String token) {
        String storeNo = order.getCooprCde();
        if (StringUtils.isEmpty(storeNo)) {
            logger.info("本订单的门店编号（storeNo）为空，请求处理被迫停止！");
            return;
        }

		/*
         * 接口返回值参考： {"head":{"retFlag":"00000","retMsg":"处理成功"},
		 * "body":{"storeNo":"DZHW01","merchNo":"8800125101","storeName":
		 * "银川市大展宏伟工贸有限公司",
		 * "storePhoneZone":"","storePhone":"","storePhoneSub":"","cumNo":
		 * "01376256"}}
		 */
        String url = EurekaServer.CRM + "/app/crm/cust/getStoreInfo" + "?storeNo=" + storeNo;
        String json = HttpUtil.restGet(url, token);
        if (StringUtils.isEmpty(json)) {
            logger.info("CRM==>门店信息（getStoreInfo）接口查询失败！");
            return;
        }
        json = json.replaceAll("null", "\"\"");
        Map<String, Object> map = HttpUtil.json2Map(json);
        if (HttpUtil.isSuccess(map)) {
            Map<String, Object> mapBody = HttpUtil.json2Map(map.get("body").toString());
            order.setCooprName(mapBody.get("storeName").toString());
            order.setContZone(mapBody.get("storePhoneZone").toString());
            order.setContTel(mapBody.get("storePhone").toString());
            order.setContSub(mapBody.get("storePhoneSub").toString());
            order.setOperatorCde(mapBody.get("cumNo").toString());
        }
    }

    /**
     * 把销售代表信息写入订单
     *
     * @param order 订单对象
     */
    public void updateSalesInfo(AppOrder order, String token) {
        String salesNo = order.getCrtUsr();
        if (StringUtils.isEmpty(salesNo)) {
            logger.info("销售代表编号（salesNo）为空，销售代表信息写入失败！");
            return;
        }

		/*
         * 接口返回值参考： {"head":{"retFlag":"00000","retMsg":"处理成功"},
		 * "body":{"mobileNum":"","userName":"陈琳琳","userId":"chenlinlin"}}
		 */
        String url = EurekaServer.CRM + "/app/crm/cust/getSalesInfo" + "?userid=" + salesNo;
        String json = HttpUtil.restGet(url, token);
        if (StringUtils.isEmpty(json)) {
            logger.info("CRM==>销售代表信息查询（getSalesInfo）接口返回异常！请求被迫停止处理！");
            return;
        }
        json = json.replaceAll("null", "\"\"");
        Map<String, Object> map = HttpUtil.json2Map(json);
        if (HttpUtil.isSuccess(map)) {
            Map<String, Object> mapBody = HttpUtil.json2Map(map.get("body").toString());
            order.setSalerName(mapBody.get("userName").toString());
            order.setSalerMobile(mapBody.get("mobileNum").toString());
        }
    }

    /**
     * 把实名认证信息写入订单
     *
     * @param order 订单对象
     */
    public Map<String, Object> updateCustRealInfo(AppOrder order, String token) {
        String custNo = order.getCustNo();
        if (StringUtils.isEmpty(custNo)) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "custNo为空，上传实名认证信息失败");
        }

        String url = EurekaServer.CRM + "/app/crm/cust/getCustRealInfo" + "?custNo=" + custNo;
        logger.info("CRM 实名信息接口请求url==" + url);
        String json = HttpUtil.restGet(url, token);
        logger.info("CRM 实名信息接口返回：" + json);
        if (StringUtils.isEmpty(json)) {
            logger.info("CRM实名认证信息（getCustRealInfo）接口返回异常！请求处理被迫停止！");
            return fail(RestUtil.ERROR_INTERNAL_CODE, "CRM系统通信失败");
        }
        if (!HttpUtil.isSuccess(json)) {
            return HttpUtil.json2DeepMap(json);
        }
        json = json.replaceAll("null", "\"\"");
        Map<String, Object> map = HttpUtil.json2DeepMap(json);
        if (HttpUtil.isSuccess(map)) {
            Map<String, Object> mapBody = (Map<String, Object>) map.get("body");
            order.setIdTyp(StringUtils.isEmpty(mapBody.get("certType")) ? "" : (String) mapBody.get("certType"));
            order.setIdNo(StringUtils.isEmpty(mapBody.get("certNo")) ?
                    "" :
                    mapBody.get("certNo").toString().toUpperCase());//身份证号
            order.setCustName(
                    StringUtils.isEmpty(mapBody.get("custName")) ? "" : mapBody.get("custName").toString());//客户姓名
            //order.setIndivMobile(mapBody.get("mobile").toString());
            //客户手机号处理逻辑
            //（1）如果版本号未传，则默认为0，订单手机号为实名认证手机号，忽略手机号参数；
            //（2）如果版本号大于等于1，订单手机号为传入的手机号参数；==》(程序代码中此处不需要做处理)
            //（3）如果版本号大于等于1且传入的手机号参数为空，则根据客户姓名和身份证号查询绑定手机号
            if (StringUtils.isEmpty(order.getVersion()) || "0".equals(order.getVersion())) {
                logger.info("旧版本==》查询实名认证手机号为：" + String.valueOf(mapBody.get("mobile")));
                order.setVersion("0");
                order.setIndivMobile(String.valueOf(mapBody.get("mobile")));
            } else if (Integer.parseInt(order.getVersion()) >= 1 && StringUtils.isEmpty(order.getIndivMobile())) {
                String mobile = this.getBindMobileByCustNameAndIdNo(order.getCustName(), order.getIdNo(), token);
                //如果查询的绑定手机号不为空，则用绑定手机号，若为空，则用实名认证手机号
                if (!StringUtils.isEmpty(mobile)) {
                    order.setIndivMobile(mobile);
                } else {
                    order.setIndivMobile(String.valueOf(mapBody.get("mobile")));
                }
            }
            // 放款账号信息
            order.setApplAcTyp("01");// 01、个人账户
            this.setFkNo(order, token);
            order.setApplAcNam(mapBody.get("custName").toString());
            order.setAccAcProvince(mapBody.get("acctProvince").toString());
            order.setAccAcCity(mapBody.get("acctCity").toString());
            order.setRepayApplAcNam(mapBody.get("custName").toString());
            this.setHkNo(order, token);
            if (StringUtils.isEmpty(order.getApplCardNo())) {
                logger.info("放款卡号为空！可能会导致数据异常！！");
            }
            if (String.valueOf(order.getApplCardNo()).equals(order.getRepayApplCardNo())) {
                // 目前还款卡信息没有开户省市，如果还款卡号与放款卡号一致，开户省市也一致
                order.setRepayAcProvince(order.getAccAcProvince());
                order.setRepayAcCity(order.getAccAcCity());
            }
        }
        // 如果从实名认证中依然获取不到开户行的省市信息，则将其写死
        if (StringUtils.isEmpty(order.getRepayAcProvince()) || StringUtils.isEmpty(order.getRepayAcCity())) {
            order.setRepayAcProvince("370000");
            order.setRepayAcCity("370200");
        }
        if (StringUtils.isEmpty(order.getAccAcProvince()) || StringUtils.isEmpty(order.getAccAcCity())) {
            order.setAccAcProvince("370000");
            order.setAccAcCity("370200");
        }
        return success();
    }


    /**
     * 把贷款品种信息写入订单
     *
     * @param order 订单对象
     */
    public void updateTypInfo(AppOrder order, String token) {
        if (order.getTypCde() != null) {
            // 从贷款品种详情接口中查询贷款品种版本号（typVer） 贷款品种流水号（typSeq）
            Map<String, Object> pLoanTypMap = cmisApplService.findPLoanTyp(order.getTypCde());
            if (!HttpUtil.isSuccess(pLoanTypMap)) {
                return;
            }
            Map<String, Object> typResultMap = (Map<String, Object>) pLoanTypMap.get("body");

            String typVer = (Integer) typResultMap.get("typVer") + "";
            String typSeq = (Integer) typResultMap.get("typSeq") + "";
            order.setTypVer(typVer);
            order.setTypSeq(typSeq);
            // 还款间隔（loanFreq） 每期还款日（dueDayOpt） 还款日(dueDay) 进件通路(docChannel)
            order.setLoanFreq((String) typResultMap.get("typFreq"));
            order.setDueDayOpt((String) typResultMap.get("dueDayOpt"));
            order.setDueDay(typResultMap.get("dueDay").toString());
            order.setDocChannel((String) typResultMap.get("docChannel"));
            // 贷款品种名称
            order.setTypDesc((String) typResultMap.get("typDesc"));
            // 贷款品种类别
            order.setTypLevelTwo((String) typResultMap.get("levelTwo"));
            // 还款方式种类
            order.setPayMtd((String) typResultMap.get("payMtd"));
            order.setPayMtdDesc((String) typResultMap.get("payMtdDesc"));
            /**
             pLoanTypFstPct;//贷款品种详情的最低首付比例(fstPct)
             pLoanTypMinAmt; //单笔最小贷款金额(minAmt)
             pLoanTypMaxAmt;//单笔最大贷款金额(maxAmt)
             pLoanTypGoodMaxNum;//同笔贷款同型号商品数量上限(goodMaxNum)
             pLoanTypTnrOpt;//借款期限(tnrOpt)
             */
            if (!StringUtils.isEmpty(order.getVersion()) && Integer.parseInt(order.getVersion()) >= 2) {
                order.setpLoanTypFstPct(StringUtils.isEmpty(typResultMap.get("fstPct")) ?
                        null :
                        new BigDecimal(String.valueOf(typResultMap.get("fstPct"))).doubleValue());
                order.setpLoanTypMinAmt(StringUtils.isEmpty(typResultMap.get("minAmt")) ?
                        null :
                        new BigDecimal(String.valueOf(typResultMap.get("minAmt"))).doubleValue());
                order.setpLoanTypMaxAmt(StringUtils.isEmpty(typResultMap.get("maxAmt")) ?
                        null :
                        new BigDecimal(String.valueOf(typResultMap.get("maxAmt"))).doubleValue());
                order.setpLoanTypGoodMaxNum(StringUtils.isEmpty(typResultMap.get("goodMaxNum")) ?
                        0 :
                        (Integer) typResultMap.get("goodMaxNum"));
                order.setpLoanTypTnrOpt((String) typResultMap.get("tnrOpt"));
            }
        }
    }

    //银行卡限额策略
    public boolean ifAccessEd(AppOrder order) {

        logger.info("银行卡限额策略,custNo：" + order.getCustNo());
        String custNo = order.getCustNo();
        String hkNo = order.getRepayApplCardNo();
        String payMtd = order.getPayMtd();//还款方式  09表示随借随还，其余的为分期
        logger.info("此用户还款方式为：patyMtd=" + payMtd);
        BigDecimal hkssFirstEd;//比对的金额
        //随借随还的比对金额为申请金额 即applyAmt
        if (Objects.equals("09", payMtd)) {
            hkssFirstEd = new BigDecimal(order.getApplyAmt());
        } else {
            //还款试算
            HashMap<String, Object> hm = new HashMap<>();
            hm.put("typCde", order.getTypCde());
            hm.put("apprvAmt", order.getApplyAmt());
            hm.put("applyTnrTyp", order.getApplyTnrTyp());
            hm.put("applyTnr", order.getApplyTnr());
            hm.put("fstPay", order.getFstPay());
            hm.put("mtdCde", order.getMtdCde());
            Map<String, Object> hkss_json = cmisApplService.getHkssReturnMap(hm, super.getGateUrl(), super.getToken());
            logger.info("还款试算service返回hkss:" + hkss_json);
            Map<String, Object> hkssBodyMap = (Map<String, Object>) hkss_json.get("body");
            Map<String, Object> first = (Map) ((List) hkssBodyMap.get("mx")).get(0);//获取第0期的费用
            hkssFirstEd = new BigDecimal(String.valueOf(first.get("instmAmt")));
        }
        //////////////////////
        //从crm查询指定客户的所有银行卡
        Map<String, Object> bankCard = HttpUtil
                .restGetMap(EurekaServer.CRM + "/app/crm/cust/getBankCard?custNo=" + custNo);
        logger.info("客户编号为" + custNo + "的用户的银行卡列表为：" + bankCard);
        Map bodyMap = (Map) bankCard.get("body");
        List<Map<String, Object>> bankList = (List<Map<String, Object>>) bodyMap.get("info");
        boolean flag = false;
        if (bankList.size() > 0) {
            for (Map<String, Object> bank : bankList) {
                // 银行卡号
                if (((String) bank.get("cardNo")).equals(hkNo)) {
                    String singleCollLimited = String.valueOf(bank.get("singleCollLimited"));
                    if (Objects.equals("-1", singleCollLimited)) {
                        logger.info("该银行卡不限额，直接返回成功：orderNo=" + order.getOrderNo() + ";hkNo=" + hkNo);
                        flag = true;
                        return flag;

                    } else {
                        //单笔限额
                        BigDecimal maxEd = new BigDecimal(singleCollLimited);//使用单笔代收金额
                        logger.info("银行卡限额：maxEd=" + maxEd + ",比对的金额：hkssFirstEd=" + hkssFirstEd);
                        if (maxEd.compareTo(hkssFirstEd) >= 0) {
                            return true;
                        } else {
                            logger.info("超过银行卡最大限额:最大额度=" + maxEd + ";测算的每期额度：" + hkssFirstEd);
                            return false;
                        }
                    }
                    //      break;
                }
            }
            return flag;
        } else {
            logger.info("该用户的银行卡列表信息与订单银行卡信息不符,抛出非法用户的异常");
            //throw new Exception("22", "非法用户！");
            return false;//TODO!!!!
        }
    }

    /**
     * 保存订单号与贷款类型关系
     *
     * @param orderNo
     * @param typGrp
     */
    private void saveRelation(String orderNo, String typGrp, String applSeq, String custNo) {
        AppOrdernoTypgrpRelation relation = appOrdernoTypgrpRelationDao.selectByOrderNo(orderNo);
        if (relation == null) {
            if (!StringUtils.isEmpty(applSeq)) {
                relation = appOrdernoTypgrpRelationDao.selectByApplSeq(applSeq);
            }
            if (relation == null) {
                // 新订单
                relation = new AppOrdernoTypgrpRelation();
            } else {
                // 重复创建的情况
                String orderno = relation.getOrderNo();
                if(!StringUtils.isEmpty(orderNo)){
                    appOrdernoTypgrpRelationDao.deleteByOrderNo(orderNo);
                }
            }
            relation.setOrderNo(orderNo);
            relation.setTypGrp(typGrp);
            relation.setApplSeq(applSeq);
            relation.setCustNo(custNo);
            relation.setIsConfirmAgreement("0"); // 未确认
            relation.setIsConfirmContract("0");  // 未确认
            relation.setIsCustInfoComplete("N"); // 未确认
            relation.setInsertTime(new Date());
            relation.setChannel(super.getChannel());
            relation.setChannelNo(super.getChannelNo());
            appOrdernoTypgrpRelationDao.insert(relation);
        } else {
            relation.setChannel(super.getChannel());
            relation.setChannelNo(super.getChannelNo());
            relation.setTypGrp(typGrp);
            relation.setApplSeq(applSeq);
            appOrdernoTypgrpRelationDao.updateByPrimaryKey(relation);
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
    public String getBindMobileByUserId1(String userId, String token) {
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
     * 设置放款卡号
     *
     * @return
     */
    private void setFkNo(AppOrder order, String token) {
        // 获取放款卡号
        String fkNo = order.getApplCardNo();
        if (StringUtils.isEmpty(fkNo)) {
            String custNo = order.getCustNo();
            String url = EurekaServer.CRM + "/app/crm/cust/getBankCard?custNo=" + custNo;
            String json = HttpUtil.restGet(url, token);
            logger.info("CRM getBankCard接口请求URL：" + url);
            logger.info("CRM getBankCard接口返回" + json);
            if (StringUtils.isEmpty(json)) {
                logger.info("银行卡信息获取失败！请求处理停止！");
                return;
            }
            Map<String, Object> bankmap = HttpUtil.json2DeepMap(json);
            if (!HttpUtil.isSuccess(bankmap)) {
                logger.info("银行卡信息获取失败！请求处理停止！" + bankmap);
                return;
            }
            Map<String, Object> infoMap = (Map<String, Object>) bankmap.get("body");
            List<Map<String, Object>> bankList = (List<Map<String, Object>>) infoMap.get("info");
            if (bankList == null || bankList.size() == 0) {
                logger.info("银行卡列表获取失败！！");
            }
            // 是否更新的标识位
            // boolean flag = false;
            for (Map<String, Object> bank : bankList) {
                // 银行卡号
                if (((String) bank.get("isRealnameCard")).equals("Y")) {
                    String bankName = (String) bank.get("bankName");
                    String bankCode = (String) bank.get("bankCode");
                    String accBchCde = (String) bank.get("accBchCde");
                    String accBchName = (String) bank.get("accBchName");
                    String cardNo = (String) bank.get("cardNo");
                    String acctProvince = (String) bank.get("acctProvince");//开户省
                    String acctCity = (String) bank.get("acctCity");//开户市
                    // order.setRepayApplCardNo(cardNo);
                    // order.setRepayAccBankCde(bankCode);
                    // order.setRepayAccBankName(bankName);
                    // order.setRepayAccBchCde(accBchCde);
                    // order.setRepayAccBchName(accBchName);
                    order.setApplCardNo(cardNo);
                    order.setAccBankCde(bankCode);
                    order.setAccBankName(bankName);
                    order.setAccAcBchCde(accBchCde);
                    order.setAccAcBchName(accBchName);
                    order.setRepayAcProvince(acctProvince);
                    order.setRepayAcCity(acctCity);
                    // 放款卡省、放款卡市、放款卡开户名从实名认证中获取
                    // order.setRepayApplAcNam(mapBody.get("custName").toString());
                    // order.setRepayAcProvince(mapBody.get("acctProvince").toString());
                    // order.setRepayAcCity(mapBody.get("acctCity").toString());
                    break;
                }
            }
        } else {
            // 根据卡号，从银行卡列表中查询
            String url = EurekaServer.CRM + "/app/crm/cust/getCustBankCardByCardNo?cardNo="
                    + fkNo;
            String json = HttpUtil.restGet(url, token);
            logger.info("CRM getCustBankCardByCardNo接口请求url==" + url);
            logger.info("CRM ==>getCustBankCardByCardNo接口返回" + json);
            if (StringUtils.isEmpty(json)) {
                logger.info("CRM getCustBankCardByCardNo接口查询银行卡列表查询失败！");
                return;
            }
            Map<String, Object> bankmap = HttpUtil.json2Map(json);
            logger.info("bankmap==" + bankmap);
            if (HttpUtil.isSuccess(bankmap)) {
                Map<String, Object> bank = HttpUtil.json2Map(bankmap.get("body").toString());
                String bankName = (String) bank.get("bankName");
                String bankCode = (String) bank.get("bankCode");
                String accBchCde = (String) bank.get("accBchCde");
                String accBchName = (String) bank.get("accBchName");
                String acctProvince = (String) bank.get("acctProvince");//开户省
                String acctCity = (String) bank.get("acctCity");//开户市
                order.setAccBankCde(bankCode);
                order.setAccBankName(bankName);
                order.setAccAcBchCde(accBchCde);
                order.setAccAcBchName(accBchName);
                order.setRepayAcProvince(acctProvince);
                order.setRepayAcCity(acctCity);
            }
        }
    }

    /**
     * 设置还款卡号
     *
     * @return
     */
    public void setHkNo(AppOrder order, String token) {
        // 后去还款卡号
        String hkNo = order.getRepayApplCardNo();
        // 判断还款卡号是否已传，如果为空，则从银行卡列表中查询
        if (StringUtils.isEmpty(hkNo)) {
            String custNo = order.getCustNo();
            String url = EurekaServer.CRM + "/app/crm/cust/getBankCard?custNo=" + custNo;
            logger.info("CRM  getBankCard 请求url==" + url);
            String json = HttpUtil.restGet(url, token);
            if (StringUtils.isEmpty(json)) {
                logger.info("CRM==>还款银行卡列表查询失败！请求处理已停止！");
                return;
            }
            Map<String, Object> bankmap = HttpUtil.json2DeepMap(json);
            if (!HttpUtil.isSuccess(json)) {
                logger.info("CRM==>还款银行卡列表查询失败！" + bankmap.get("head"));
                return;
            }
            Map<String, Object> infoMap = (Map<String, Object>) bankmap.get("body");
            List<Map<String, Object>> bankList = (List<Map<String, Object>>) infoMap.get("info");
            // boolean flag = false;
            for (Map<String, Object> bank : bankList) {
                // 银行卡号
                if (((String) bank.get("isDefaultCard")).equals("Y")) {
                    String bankName = FormatUtil.getStrDealNull(bank.get("bankName"));
                    String bankCode = FormatUtil.getStrDealNull(bank.get("bankCode"));
                    String accBchCde = FormatUtil.getStrDealNull(bank.get("accBchCde"));
                    String accBchName = FormatUtil.getStrDealNull(bank.get("accBchName"));
                    String cardNo = FormatUtil.getStrDealNull(bank.get("cardNo"));
                    String acctProvince = FormatUtil.getStrDealNull(bank.get("acctProvince"));//开户省
                    String acctCity = FormatUtil.getStrDealNull(bank.get("acctCity"));//开户市
                    String repayAccMobile = FormatUtil.getStrDealNull(bank.get("mobile"));
                    order.setRepayApplCardNo(cardNo);
                    order.setRepayAccBankCde(bankCode);
                    order.setRepayAccBankName(bankName);
                    order.setRepayAccBchCde(accBchCde);
                    order.setRepayAccBchName(accBchName);
                    order.setRepayAcProvince(acctProvince);
                    order.setRepayAcCity(acctCity);
                    order.setRepayAccMobile(repayAccMobile);//还款卡手机号

                    // 还款卡省、还款卡市、还款卡开户名不做处理了
                    // order.setRepayAcProvince(mapBody.get("acctProvince").toString());
                    // order.setRepayAcCity(mapBody.get("acctCity").toString());
                    //                     order.setRepayApplAcNam(mapBody.get("custName").toString());
                    break;
                }
            }
        } else {
            // 根据卡号，从银行卡列表中查询
            String url = EurekaServer.CRM + "/app/crm/cust/getCustBankCardByCardNo?cardNo="
                    + hkNo;
            logger.info("CRM==》getCustBankCardByCardNo接口请求url==" + url);
            String json = HttpUtil.restGet(url, token);
            if (StringUtils.isEmpty(json)) {
                logger.info("CRM==>getCustBankCardByCardNo接口返回结果异常！请求处理停止！");
                return;
            }
            Map<String, Object> bankmap = HttpUtil.json2Map(json);
            logger.info("bankmap==" + bankmap);
            if (HttpUtil.isSuccess(bankmap)) {
                Map<String, Object> bank = HttpUtil.json2Map(bankmap.get("body").toString());
                String bankName = (String) bank.get("bankName");
                String bankCode = (String) bank.get("bankCode");
                String accBchCde = (String) bank.get("accBchCde");
                String accBchName = (String) bank.get("accBchName");
                String acctProvince = (String) bank.get("acctProvince");//开户省
                String acctCity = (String) bank.get("acctCity");//开户市
                order.setRepayAccBankCde(bankCode);
                order.setRepayAccBankName(bankName);
                order.setRepayAccBchCde(accBchCde);
                order.setRepayAccBchName(accBchName);
                order.setRepayAcProvince(acctProvince);
                order.setRepayAcCity(acctCity);
                order.setRepayAccMobile((String) bank.get("mobile"));//还款卡手机号

            }
        }
        logger.debug("还款卡手机号：" + order.getRepayAccMobile());
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
        Map<String, Object> responseMap = (Map<String, Object>) result.get("response");
        if (!CmisUtil.isSuccess(result)) {
            return true;
        }
        Map<String, Object> bodyMap = (Map<String, Object>) responseMap.get("body");
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
//    public String getBindMobileByCustNameAndIdNo(String custName, String idNo, String token) {
//        //若身份证号或客户姓名为空，则返回null
//        if (StringUtils.isEmpty(custName) || StringUtils.isEmpty(idNo)) {
//            return null;
//        }
//        String url =
//                EurekaServer.CRM + "/app/crm/cust/getUserIdByCustNameAndCertNo" + "?custName=" + custName + "&certNo="
//                        + idNo;
//        logger.info("CRM(74)==》请求url==" + url);
//        String json = HttpUtil.restGet(url, token);
//        logger.info("CRM(74)==》返回：" + json);
//        if (StringUtils.isEmpty(json)) {
//            logger.info("CRM(74)==》(getUserIdByCustNameAndCertNo)接口返回异常！请求处理被迫停止！");
//            return null;
//        }
//        json = json.replaceAll("null", "\"\"");
//        Map<String, Object> map = HttpUtil.json2Map(json);
//        if (HttpUtil.isSuccess(map)) {
//            Map<String, Object> mapBody = HttpUtil.json2Map(map.get("body").toString());
//            logger.info("CRM(74)==》body体：" + mapBody);
//            String userId = StringUtils.isEmpty(mapBody.get("userId")) ? null : mapBody.get("userId").toString();
//            if (StringUtils.isEmpty(userId)) {
//                logger.info("CRM(74)接口查询失败！userId查询为空！");
//                return null;
//            }
//            return this.getBindMobileByUserId(userId, token);
//        }
//        return null;
//    }

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


    public void calcFstPct(AppOrder order) {
        if (org.springframework.util.StringUtils.isEmpty(order.getFstPay())) {
            order.setFstPay("0.0");
            order.setFstPct("0.0");
        } else if (!org.springframework.util.StringUtils.isEmpty(order.getProPurAmt())) {
            // 首付
            BigDecimal fstPay_big = new BigDecimal(order.getFstPay());
            BigDecimal propurAmt_big = new BigDecimal(order.getProPurAmt());
            BigDecimal fstPct_big = BigDecimal.ZERO;
            if (propurAmt_big.compareTo(BigDecimal.ZERO) != 0) {
                fstPct_big = fstPay_big.divide(propurAmt_big, 4, BigDecimal.ROUND_HALF_UP);
            }
            order.setFstPct(fstPct_big.toString());
        }
        logger.info("首付金额：" + order.getFstPay() + "首付比例：" + order.getFstPct());
    }
}
