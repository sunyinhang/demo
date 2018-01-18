package com.haiercash.payplatform.service.impl;

import com.haiercash.core.collection.CollectionUtils;
import com.haiercash.core.collection.MapUtils;
import com.haiercash.core.lang.Base64Utils;
import com.haiercash.core.lang.Convert;
import com.haiercash.core.lang.DateUtils;
import com.haiercash.payplatform.common.dao.AppOrdernoTypgrpRelationDao;
import com.haiercash.payplatform.common.dao.AppointmentRecordDao;
import com.haiercash.payplatform.common.dao.CooperativeBusinessDao;
import com.haiercash.payplatform.common.dao.EntrySettingDao;
import com.haiercash.payplatform.common.dao.SAreaDao;
import com.haiercash.payplatform.common.dao.SignContractInfoDao;
import com.haiercash.payplatform.common.data.AppOrder;
import com.haiercash.payplatform.common.data.AppOrdernoTypgrpRelation;
import com.haiercash.payplatform.common.data.AppointmentRecord;
import com.haiercash.payplatform.common.data.CooperativeBusiness;
import com.haiercash.payplatform.common.data.EntrySetting;
import com.haiercash.payplatform.common.data.SArea;
import com.haiercash.payplatform.common.data.SignContractInfo;
import com.haiercash.payplatform.common.entity.ThirdTokenVerifyResult;
import com.haiercash.payplatform.config.CommonConfig;
import com.haiercash.payplatform.config.OutreachConfig;
import com.haiercash.payplatform.pc.cashloan.service.ThirdTokenVerifyService;
import com.haiercash.payplatform.service.AcquirerService;
import com.haiercash.payplatform.service.AppServerService;
import com.haiercash.payplatform.service.CmisApplService;
import com.haiercash.payplatform.service.CommonPageService;
import com.haiercash.payplatform.service.CrmService;
import com.haiercash.payplatform.service.GmService;
import com.haiercash.payplatform.service.OrderService;
import com.haiercash.payplatform.utils.CmisTradeCode;
import com.haiercash.payplatform.utils.CmisUtil;
import com.haiercash.payplatform.utils.EncryptUtil;
import com.haiercash.payplatform.utils.FormatUtil;
import com.haiercash.payplatform.utils.RSAUtils;
import com.haiercash.spring.boot.ApplicationUtils;
import com.haiercash.spring.config.EurekaServer;
import com.haiercash.spring.redis.RedisUtils;
import com.haiercash.spring.rest.IResponse;
import com.haiercash.spring.rest.client.JsonClientUtils;
import com.haiercash.spring.rest.common.CommonResponse;
import com.haiercash.spring.service.BaseService;
import com.haiercash.spring.util.BusinessException;
import com.haiercash.spring.util.ConstUtil;
import com.haiercash.spring.util.HttpUtil;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Created by yuanli on 2017/9/20.
 */
@Service
public class CommonPageServiceImpl extends BaseService implements CommonPageService {
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
    @Autowired
    private CommonConfig commonConfig;
    @Autowired
    private CooperativeBusinessDao cooperativeBusinessDao;
    @Autowired
    private CrmService crmService;
    @Autowired
    private AppointmentRecordDao appointmentRecordDao;
    @Autowired
    private EntrySettingDao entrySettingDao;
    @Autowired
    private OutreachConfig outreachConfig;
    @Autowired
    private SAreaDao sAreaDao;

    /**
     * 合同展示
     *
     * @param map
     * @return
     */
    @Override
    public Map<String, Object> showcontract(Map<String, Object> map) throws Exception {
        String flag = (String) map.get("flag");

        String token = super.getToken();
        if (StringUtils.isEmpty(flag) || StringUtils.isEmpty(token)) {
            logger.info("前台传入参数为空");
            logger.info("flag:" + flag + "  token:" + token);
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }

        Map<String, Object> cacheMap = RedisUtils.getExpireMap(token);
        if (MapUtils.isEmpty(cacheMap)) {
            logger.info("Jedis数据获取失败");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        String custName = (String) cacheMap.get("custName");
        String custNo = (String) cacheMap.get("custNo");
        String certNo = (String) cacheMap.get("certNo");

        Map result = new HashMap();
        String url = "";
        //征信协议展示
        if ("1".equals(flag)) {
            String name = Base64Utils.encodeString(custName);
            name = URLEncoder.encode(name, "UTF-8");
            url = "/app/appserver/edCredit?custName=" + name + "&certNo=" + certNo;
            result.put("url", url);
        }
        //签章协议展示
        if ("2".equals(flag)) {
            String applseq = (String) map.get("applseq");
            if (StringUtils.isEmpty(applseq)) {
                logger.info("applseq:" + applseq);
                return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
            }
            url = "/app/appserver/contract?custNo=" + custNo + "&applseq=" + applseq;
            result.put("url", url);
        }
        //注册协议展示
        if ("3".equals(flag)) {
            String name = new String(Base64.encode(custName.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
            name = URLEncoder.encode(name, "UTF-8");
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
        if ("46".equals(getChannelNo())) {
            if (!judgeApplAmt(apporder.getIdTyp(), apporder.getIdNo(), apporder.getApplyAmt(), super.getToken())) {
                logger.info("对不起，您的剩余额度低于借款金额，建议您可以在额度恢复后再借款");
                return fail("07", "对不起，您的剩余额度低于借款金额，建议您可以在额度恢复后再借款");
            }
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
        } else {
            return fail("05", "订单提交类型不正确");
        }
    }

    @Override
    //合同签订
    public Map<String, Object> signContract(String custName, String custIdCode, String applseq, String phone, String typCde,
                                            String channelNo, String token) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("typCdeList", typCde);
        Map<String, Object> loanmap = appServerService.pLoanTypList(token, paramMap);
        if (!HttpUtil.isSuccess(loanmap)) {
            return loanmap;
        }
        List<Map<String, Object>> loanbody = (List<Map<String, Object>>) loanmap.get("body");
        String typLevelTwo = "";
        for (Map<String, Object> m : loanbody) {
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
        if (signContractInfo == null) {
            return fail(ConstUtil.ERROR_CODE, "贷款品种" + typCde + "没有配置签章类型");
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

        boolean ifAccessEd;
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

        String orderNo;
        String applSeq;
        if ("02".equals(appOrder.getTypGrp())) {//现金贷

            // 现金贷
            AppOrdernoTypgrpRelation relation = null;
            String orderno = appOrder.getOrderNo();
            logger.info("订单编号为：" + orderno);
            if (!StringUtils.isEmpty(appOrder.getOrderNo())) {//待提交
                relation = appOrdernoTypgrpRelationDao.selectByOrderNo(appOrder.getOrderNo());
                if (relation == null) {
                    return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "要更新的订单不存在！");
                }
            }
            // 收单系统获取订单详情
            //AppOrder appOrder0 = acquirerService.getAppOrderFromAcquirer(relation.getApplSeq(), super.getChannelNo());

            Map<String, Object> resultResponseMap = acquirerService.cashLoan(appOrder, relation);
            if (CmisUtil.isSuccess(resultResponseMap)) {
                Map<String, Object> bodyMap = (Map<String, Object>) ((Map<String, Object>) resultResponseMap
                        .get("response")).get("body");
                applSeq = (String) bodyMap.get("applSeq");
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
    public Map<String, Object> getCode(String token, Map<String, Object> citymap) {
        Map<String, Object> resultMap = new HashMap<>();
        String cityCode = "";
        String areaType = "";
        // 根据区号获取市
        Map<String, Object> result = appServerService.getAreaInfo(token, citymap);
        String retFlag = (String) ((Map<String, Object>) result.get("head")).get("retFlag");
        String retMsg = (String) ((Map<String, Object>) result.get("head")).get("retMsg");
        if (!"00000".equals(retFlag)) {
            return fail(ConstUtil.ERROR_CODE, retMsg);
        }
        List<Map<String, Object>> body = (List<Map<String, Object>>) result.get("body");

        for (Map<String, Object> m : body) {
            cityCode = m.get("areaCode").toString();
            areaType = m.get("areaType").toString();
        }
        resultMap.put("cityCode", cityCode);
        resultMap.put("areaType", areaType);
        return success(resultMap);
    }

    /**
     * 查询贷款用途
     *
     * @param params
     * @return
     */
    @Override
    public Map<String, Object> getPurpose(Map<String, Object> params) {
        String token = super.getToken();
        return appServerService.getPurpose(token, params);
    }

    /*
    实名验证
     */
    @Override
    public Map<String, Object> identity(Map<String, Object> map) throws Exception {
        logger.info("四要素验证***************开始");
        String channelNo = (String) map.get("channelNo");
        String data = (String) map.get("data");
        if (StringUtils.isEmpty(channelNo)) {
            logger.info("渠道编码不能为空");
            return fail(ConstUtil.ERROR_CODE, "渠道编码不能为空");
        }
        if (StringUtils.isEmpty(data)) {
            logger.info("请求数据不能为空");
            return fail(ConstUtil.ERROR_CODE, "请求数据不能为空");
        }

        String params = decryptData(data, channelNo);
        logger.info("四要素验证接收到的数据：" + params);
        JSONObject camap = new JSONObject(params);
        //
        String custName = camap.getString("custName");
        String cardNo = camap.getString("cardNo");
        String bankCode = camap.getString("bankCode");
        String certNo = camap.getString("certNo");
        String mobile = camap.getString("mobile");
        if (StringUtils.isEmpty(custName) || StringUtils.isEmpty(cardNo) || StringUtils.isEmpty(bankCode)
                || StringUtils.isEmpty(certNo) || StringUtils.isEmpty(mobile)) {
            return fail(ConstUtil.ERROR_CODE, "必传项不能为空");
        }
        String url = outreachConfig.getUrl() + "/Outreachplatform/api/chinaPay/identifyByFlag";
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("accountName", custName);
        jsonMap.put("accountNo", cardNo);
        jsonMap.put("bankCode", bankCode);
        jsonMap.put("id", certNo);
        jsonMap.put("cardPhone", mobile);
        jsonMap.put("flag", "1");//四要素
        jsonMap.put("channelNo", channelNo);
        jsonMap.put("days", "0");
        logger.info("实名认证(外联)参数==>" + jsonMap.toString());
        String resData = JsonClientUtils.postForString(url, jsonMap);
        logger.info("实名认证(外联)响应数据==>" + resData);
        if (resData == null || "".equals(resData)) {
            logger.info("实名认证(外联)响应数据为空");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        JSONObject jb = new JSONObject(resData);
        String retFlag = jb.getString("RET_CODE");
        if (!"00000".equals(retFlag)) {
            return fail(ConstUtil.ERROR_CODE, "四要素验证失败");
        }
        return success();
    }

    @Override
    public String decryptData(String data, String channelNo) throws Exception {
        //获取渠道公钥
        logger.info("获取渠道" + channelNo + "公钥");
        CooperativeBusiness cooperativeBusiness = cooperativeBusinessDao.selectBycooperationcoed(channelNo);
        if (cooperativeBusiness == null) {
            throw new RuntimeException("渠道" + channelNo + "公钥获取失败");
        }
        String publicKey = cooperativeBusiness.getRsapublic();//获取公钥

        //请求数据解析
        //String params = new String(DesUtil.decrypt(Base64Utils.decode(data), new String(RSAUtils.decryptByPublicKey(Base64Utils.decode(key), publicKey))));
        //String params = new String(RSAUtils.decryptByPublicKey(Base64Utils.decode(data), publicKey));
        //String params = new String(DesUtil.decrypt(Base64Utils.decode(data), new String(RSAUtils.decryptByPublicKey(Base64Utils.decode(key), publicKey))));

        return new String(RSAUtils.decryptByPublicKey(Base64Utils.decode(data), publicKey), StandardCharsets.UTF_8);
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
            return fail(ConstUtil.ERROR_CODE, "CRM系统通信失败");
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

            String typVer = typResultMap.get("typVer") + "";
            String typSeq = typResultMap.get("typSeq") + "";
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
                order.setpLoanTypGoodMaxNum(StringUtils.isEmpty(typResultMap.get("goodMaxNum")) ? 0 : Convert.toInteger(typResultMap.get("goodMaxNum")));
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
            Map<String, Object> hkss_json = cmisApplService.getHkssReturnMap(hm, commonConfig.getGateUrl(), super.getToken());
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
                if (bank.get("cardNo").equals(hkNo)) {
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
                if (!StringUtils.isEmpty(orderNo)) {
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
        String str = EncryptUtil.simpleEncrypt(userId);
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
            if (CollectionUtils.isEmpty(bankList)) {
                logger.info("银行卡列表获取失败！！");
                return;
            }
            // 是否更新的标识位
            // boolean flag = false;
            for (Map<String, Object> bank : bankList) {
                // 银行卡号
                if (bank.get("isRealnameCard").equals("Y")) {
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
                if (bank.get("isDefaultCard").equals("Y")) {
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
        HashMap<String, Object> map = new HashMap<>();
        map.put("idTyp", idTyp);
        map.put("idNo", idNo);
        map.put("sysFlag", super.getChannel());
        map.put("channelNo", super.getChannelNo());
        logger.info("检验参数:" + map + ",申请金额:" + applyAmt);

        //调用信贷系统失败或者返回失败 直接返回true
        /**
         * { "response": { "head": { "retMsg": "交易成功！", "retFlag": "00000" },
         * "body": { "crdComUsedAmt": 6001, "crdComAvailAmt": 0, "crdComAmt":
         * 10000, "crdNorUsedAmt": 6001, "crdAmt": 10000, "crdNorAvailAmt": 0,
         * "crdSts": 30, "crdNorAmt": 10000, "contDt": "2018-03-19" } } }
         */
        Map<String, Object> result = CmisUtil.getCmisResponse(CmisTradeCode.TRADECODE_ED_CHECK, token, map);
        logger.info("信贷100016返回" + result);
        if (result == null) {
            logger.info("信贷系统额度查询失败");
            return true;
        }
        Map<String, Object> responseMap = (Map<String, Object>) result.get("response");
        if (!CmisUtil.isSuccess(result)) {
            return true;
        }
        Map<String, Object> _repbodyMap = (Map<String, Object>) responseMap.get("body");
        Map<String, Object> speCrdList = (Map<String, Object>) _repbodyMap.get("speCrdList");
        Map<String, Object> bodyMap = (Map<String, Object>) speCrdList.get("speCrdInfo");
        applyAmt = String.valueOf(StringUtils.isEmpty(applyAmt) ? 0 : applyAmt);
        String crdComAvailAmt = String.valueOf(StringUtils.isEmpty(bodyMap.get("crdComAvailAmt")) ? 0 : bodyMap.get("crdComAvailAmt"));//商品贷用crdComAvailAmt   现金贷用crdNorAvailAmt
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
        return HttpUtil.json2Map(json);
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
        String str = EncryptUtil.simpleEncrypt(userId);
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
        String mobile;//要返回的手机号
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

    @Override
    public Map<String, Object> queryApplReraidPlanByloanNo(Map<String, Object> params) {
        logger.info("============查询还款计划开始===========");
        Map<String, Object> resultMap = crmService.queryApplReraidPlanByloanNo(params);
//        ResultHead resultHead = (ResultHead) resultMap.get("head");
//        String retFlag = resultHead.getRetFlag();
//        if (!"00000".equals(retFlag)) {
//            return resultMap;
//        }
        logger.info("============查询还款计划结束===========");
        return resultMap;
    }

    @Override
    public IResponse appointment(String phone, String name, String education, String location) {
        AppointmentRecord record = new AppointmentRecord();
        record.setId(UUID.randomUUID().toString());
        record.setPhone(phone);
        record.setName(name);
        record.setEducation(education);
        record.setLocation(location);
        record.setCreatedate(DateUtils.nowDateTimeString());
        appointmentRecordDao.insert(record);
        return CommonResponse.success();
    }

    /**
     * @Title joinActivityRedirect
     * @Description: 现金贷 登陆页面初始化 页面跳转
     * @author yu jianwei
     * @date 2017/11/20 10:56
     */
    private Map<String, Object> joinActivityRedirect(EntrySetting setting) {
        logger.info("现金贷申请接口*******************开始");
        Map<String, Object> cachemap = new HashMap<>();
        Map<String, Object> returnmap = new HashMap<>();//返回的map
        String channelNo = this.getChannelNo();
        String thirdToken = this.getToken();
        String verifyUrl = setting.getVerifyUrlThird() + thirdToken;
        String uidLocal;//统一认证userid
        String phoneNo;//统一认证绑定手机号
        logger.info("验证第三方 token:" + verifyUrl);
        //验证客户信息
        ThirdTokenVerifyService thirdTokenVerifyService;
        try {
            thirdTokenVerifyService = ApplicationUtils.getBean(setting.getVerifyUrlService(), ThirdTokenVerifyService.class);
        } catch (Exception e) {
            throw new BusinessException(ConstUtil.ERROR_CODE, "错误的 thirdTokenVerifyService 名称:'" + setting.getVerifyUrlService() + "'");
        }
        ThirdTokenVerifyResult thirdInfo = thirdTokenVerifyService.verify(setting, thirdToken);
        String userId__ = thirdInfo.getUserId();
        String phoneNo_ = thirdInfo.getPhoneNo();
        cachemap.put("uidHaier", userId__);
        cachemap.put("haieruserId", phoneNo_);
        //从后台查询用户信息
        Map<String, Object> userInfo = queryUserByExternUid(channelNo, userId__);
        String retFlag = HttpUtil.getRetFlag(userInfo);
        if (Objects.equals(retFlag, "00000")) {
            //集团uid已在统一认证做过绑定
            String body = userInfo.get("body").toString();
            JSONObject bodyMap = new JSONObject(body);
            uidLocal = bodyMap.get("userId").toString();//统一认证内userId
            phoneNo = bodyMap.get("mobile").toString();//统一认绑定手机号
        } else {
            returnmap.put("flag", "1");//跳转活动页
            return success(returnmap);
        }
        cachemap.put("userId", uidLocal);//统一认证userId
        cachemap.put("phoneNo", phoneNo);//绑定手机号
        logger.info("进行token绑定");
        //4.token绑定
        Map<String, Object> bindMap = new HashMap<>();
        bindMap.put("userId", uidLocal);//内部userId
        bindMap.put("token", thirdToken);
        bindMap.put("channel", "11");
        bindMap.put("channelNo", channelNo);
        Map<String, Object> bindresult = appServerService.saveThirdPartToken(bindMap);
        if (!HttpUtil.isSuccess(bindresult)) {//绑定失败
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        //5.查询实名信息
        Map<String, Object> custMap = new HashMap<>();
        custMap.put("userId", uidLocal);//内部userId
        custMap.put("channel", "11");
        custMap.put("channelNo", channelNo);
        Map custresult = appServerService.queryPerCustInfo(thirdToken, custMap);
        String custretflag = ((Map<String, Object>) (custresult.get("head"))).get("retFlag").toString();
        if (!"00000".equals(custretflag) && !"C1220".equals(custretflag)) {//查询实名信息失败
            String custretMsg = ((Map<String, Object>) (custresult.get("head"))).get("retMsg").toString();
            return fail(ConstUtil.ERROR_CODE, custretMsg);
        }
        if ("C1220".equals(custretflag)) {//C1120  客户信息不存在  跳转无额度页面
            logger.info("token:" + thirdToken);
            returnmap.put("flag", "1");//活动页
            return success(returnmap);
        }
        String certType = ((Map<String, Object>) (custresult.get("body"))).get("certType").toString();//证件类型
        String certNo = ((Map<String, Object>) (custresult.get("body"))).get("certNo").toString();//身份证号
        String custNo = ((Map<String, Object>) (custresult.get("body"))).get("custNo").toString();//客户编号
        String custName = ((Map<String, Object>) (custresult.get("body"))).get("custName").toString();//客户名称
        String cardNo = ((Map<String, Object>) (custresult.get("body"))).get("cardNo").toString();//银行卡号
        String bankNo = ((Map<String, Object>) (custresult.get("body"))).get("acctBankNo").toString();//银行代码
        String bankName = ((Map<String, Object>) (custresult.get("body"))).get("acctBankName").toString();//银行名称

        cachemap.put("custNo", custNo);//客户编号
        cachemap.put("name", custName);//客户姓名
        cachemap.put("cardNo", cardNo);//银行卡号
        cachemap.put("bankCode", bankNo);//银行代码
        cachemap.put("bankName", bankName);//银行名称
        cachemap.put("idNo", certNo);//身份证号
        cachemap.put("idCard", certNo);//身份证号
        cachemap.put("idType", certType);
        RedisUtils.setExpire(thirdToken, cachemap);
        String tag = "SHH";
        String typCde = "";//贷款品种
        Map<String, Object> cacheedmap = new HashMap<>();
        cacheedmap.put("channel", "11");
        cacheedmap.put("channelNo", channelNo);
        cacheedmap.put("userId", uidLocal);
        Map<String, Object> mapcache = appServerService.checkEdAppl(thirdToken, cacheedmap);
        logger.info("额度申请校验接口返回数据：" + mapcache);
        if (!HttpUtil.isSuccess(mapcache)) {
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        Object head2 = mapcache.get("head");
        Map<String, Object> retinfo = (Map<String, Object>) head2;
        String retFlag_ = (String) retinfo.get("retFlag");
        if ("00000".equals(retFlag_)) {
            Map<String, Object> headinfo = (Map<String, Object>) (mapcache.get("body"));
            String applType = (String) headinfo.get("applType");
            String flag = (String) headinfo.get("flag");
            String outSts_ = (String) headinfo.get("outSts");
            if ("1".equals(applType) || (StringUtils.isEmpty(applType) && "Y".equals(flag))) {
                if ("22".equals(outSts_)) {//退回
                    String crdSeq = (String) headinfo.get("crdSeq");
                    cachemap.put("crdSeq", crdSeq);
                    RedisUtils.setExpire(thirdToken, cachemap);
                }
                returnmap.put("flag", "1");//活动页
                return success(returnmap);
            } else if ("2".equals(applType)) {
                HashMap<String, Object> edCheckmap = new HashMap<>();
                edCheckmap.put("idNo", certNo);
                edCheckmap.put("channel", "11");
                edCheckmap.put("channelNo", channelNo);
                edCheckmap.put("idTyp", certType);
                Map<String, Object> edApplProgress = appServerService.getEdApplProgress(null, edCheckmap);//(POST)额度申请进度查询（最新的进度 根据idNo查询）
                Map<String, Object> head = (Map<String, Object>) edApplProgress.get("head");
                if (!"00000".equals(head.get("retFlag"))) {
                    logger.info("额度申请进度查询（最新的进度 根据idNo查询）,错误信息：" + head.get("retMsg"));
                    return fail(ConstUtil.ERROR_CODE, (String) head.get("retMsg"));
                }
                Map<String, Object> body = (Map<String, Object>) edApplProgress.get("body");
                Integer crdSeqInt = (Integer) body.get("applSeq");
                String crdSeq = Integer.toString(crdSeqInt);
                cachemap.put("crdSeq", crdSeq);
                RedisUtils.setExpire(thirdToken, cachemap);
                String outSts = body.get("outSts").toString();
                if ("27".equals(outSts)) {
                    returnmap.put("flag", "2");//通过  我的额度
                }
            } else if (StringUtils.isEmpty(flag)) {
                returnmap.put("flag", "2");//通过  我的额度
            }
        }
        returnmap.put("token", thirdToken);
        return success(returnmap);
    }

    public void calcFstPct(AppOrder order) {
        if (StringUtils.isEmpty(order.getFstPay())) {
            order.setFstPay("0.0");
            order.setFstPct("0.0");
        } else if (!StringUtils.isEmpty(order.getProPurAmt())) {
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

    private Map<String, Object> queryUserByExternUid(String externCompanyNo, String externUid) {
        Map<String, Object> map = new HashMap<>();
        String externCompanyNo_ = EncryptUtil.simpleEncrypt(externCompanyNo);
        String externUid_ = EncryptUtil.simpleEncrypt(externUid);
        map.put("externCompanyNo", externCompanyNo_);
        map.put("externUid", externUid_);
        Map<String, Object> stringObjectMap = appServerService.queryUserByExternUid(this.getToken(), map);
//        String response = appServerService.queryHaierUserInfo(EncryptUtil.simpleEncrypt(outUserId));
        if (stringObjectMap == null)
            throw new BusinessException(ConstUtil.ERROR_CODE, "根据第三方（非海尔集团）id查询用户信息失败");
        return stringObjectMap;
    }


    private Map<String, Object> saveUserByExternUid(String externCompanyNo, String externUid, String linkMobile) {
        Map<String, Object> map = new HashMap<>();
        String externCompanyNo_ = EncryptUtil.simpleEncrypt(externCompanyNo);
        String externUid_ = EncryptUtil.simpleEncrypt(externUid);
        String linkMobile_ = EncryptUtil.simpleEncrypt(linkMobile);
        map.put("externCompanyNo", externCompanyNo_);
        map.put("externUid", externUid_);
        map.put("linkMobile", linkMobile_);
        Map<String, Object> stringObjectMap = appServerService.saveUserByExternUid(this.getToken(), map);
        if (stringObjectMap == null)
            throw new BusinessException(ConstUtil.ERROR_CODE, "根据第三方（非海尔集团）id查询用户信息失败");
        return stringObjectMap;
    }

    /**
     * @Title joinActivity
     * @Description: 现金贷 登陆页面初始化 页面跳转
     * @author yu jianwei
     * @date 2017/11/20 10:56
     */
    @Override
    public Map<String, Object> joinActivity() {
        String channelNo = this.getChannelNo();
        EntrySetting setting = entrySettingDao.selectBychanelNo(channelNo);
        if (setting == null) {
            return fail(ConstUtil.ERROR_CODE, "没有配置相应渠道数据！");
        }
        String loginType = setting.getLoginType();
        switch (loginType) {
            case "01":
                Map<String, Object> map = new HashMap<>();
                map.put("flag", "1");
                return success(map);//跳转活动页

            case "02":
                String thirdToken = this.getToken();
                if (com.haiercash.core.lang.StringUtils.isEmpty(thirdToken))
                    return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "token 登陆,但是未传递 token");
                return this.joinActivityRedirect(setting);

            default:
                String msg = "错误的登陆类型:" + loginType;
                logger.warn(msg);
                return fail(ConstUtil.ERROR_CODE, msg);
        }
    }

    /**
     * @Title personalEd
     * @Description: 查询个人额度状态
     * @author yu jianwei
     * @date 2017/11/21 17:11
     */
    @Override
    public Map<String, Object> personalEd() {
        logger.info("查询个人额度状态*******************开始");
        Map<String, Object> returnmap = new HashMap<>();//返回的map
        String channelNo = this.getChannelNo();
        String token = this.getToken();
        String userId;//统一认证userid
        logger.info("token:" + token);
        Map<String, Object> cacheMap = RedisUtils.getExpireMap(token);
        if (MapUtils.isEmpty(cacheMap)) {
            logger.info("Jedis数据获取失败");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        userId = cacheMap.get("userId") + "";
        String certNo = cacheMap.get("idNo") + "";
        String idType = cacheMap.get("idType") + "";
        Map<String, Object> cacheedmap = new HashMap<>();
        cacheedmap.put("channel", "11");
        cacheedmap.put("channelNo", channelNo);
        cacheedmap.put("userId", userId);
        Map<String, Object> mapcache = appServerService.checkEdAppl(token, cacheedmap);
        logger.info("额度申请校验接口返回数据：" + mapcache);
        if (!HttpUtil.isSuccess(mapcache)) {
            Map<String, Object> head = (Map<String, Object>) mapcache.get("head");
            String _retFlag_ = (String) head.get("retFlag");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        Object head2 = mapcache.get("head");
        Map<String, Object> retinfo = (Map<String, Object>) head2;
        String retFlag_ = (String) retinfo.get("retFlag");
        if ("00000".equals(retFlag_)) {
            Map<String, Object> headinfo = (Map<String, Object>) (mapcache.get("body"));
            String applType = (String) headinfo.get("applType");
            String flag = (String) headinfo.get("flag");
            String _outSts = (String) headinfo.get("outSts");
            String retmsg = "01";//未申请
            if ("25".equals(_outSts)) {
                returnmap.put("flag", "3");// 拒绝
                returnmap.put("token", token);
                return success(returnmap);
            } else if ("22".equals(_outSts)) {
                returnmap.put("flag", "2");//退回
                returnmap.put("token", token);
                return success(returnmap);
            }

            if ("1".equals(applType) || (StringUtils.isEmpty(applType) && "Y".equals(flag))) {
                //没有额度申请
            } else if ("2".equals(applType)) {
                HashMap<String, Object> edCheckmap = new HashMap<>();
                edCheckmap.put("idNo", certNo);
                edCheckmap.put("channel", "11");
                edCheckmap.put("channelNo", channelNo);
                edCheckmap.put("idTyp", idType);
                Map<String, Object> edApplProgress = appServerService.getEdApplProgress(null, edCheckmap);//(POST)额度申请进度查询（最新的进度 根据idNo查询）
                Map<String, Object> head = (Map<String, Object>) edApplProgress.get("head");
                if (!"00000".equals(head.get("retFlag"))) {
                    logger.info("额度申请进度查询（最新的进度 根据idNo查询）,错误信息：" + head.get("retMsg"));
                    return fail(ConstUtil.ERROR_CODE, (String) head.get("retMsg"));
                }
                Map<String, Object> body = (Map<String, Object>) edApplProgress.get("body");
                String outSts = body.get("outSts").toString();
                switch (outSts) {
                    case "27":
                        returnmap.put("flag", "1");//通过  我的额度
                        break;
                    case "25":
                        returnmap.put("flag", "3");// 拒绝
                        break;
                    case "22":
                        returnmap.put("flag", "2");// 退回
                        break;
                    default: //审批中
                        returnmap.put("flag", "4");// 审批中
                        break;
                }
            } else if ("".equals(flag)) {
                returnmap.put("flag", "1");//通过  我的额度
            }
        }
        returnmap.put("token", token);
        return success(returnmap);
    }

    /**
     * 获取地理位置
     *
     * @param provinceName
     * @param cityName
     * @return
     * @throws Exception
     */
    @Override
    public Map<String, Object> getAreaCode(String provinceName, String cityName, String districtName) {
        Map<String, Object> map = new HashMap<>();
        String province = "";
        String city = "";
        if (StringUtils.isEmpty(cityName)) {
            //根据省编码和区名称查询城市编码
            SArea province_sArea = sAreaDao.getByCodeAndType(provinceName, "province");
            if (StringUtils.isEmpty(province_sArea)) {
                return fail(ConstUtil.ERROR_CODE, "所在城市省暂未查到");
            }
            province = province_sArea.getAreaCode();//省编码
            SArea sArea1 = sAreaDao.searchCityCode(province, districtName);
            if (!StringUtils.isEmpty(sArea1)) {//判断是否有城市编码
                city = sArea1.getAreaCode();
                map.put("province", province);
                map.put("city", city);
            } else {
                cityName = districtName;
                //查询类型为城市的城市编码
                SArea city1 = sAreaDao.getByCodeAndType(cityName, "city");
                if (!StringUtils.isEmpty(city1)) {
                    city = city1.getAreaCode();
                    map.put("province", province);
                    map.put("city", city);
                } else {
                    return fail(ConstUtil.ERROR_CODE, "尚未获取您的位置");
                }
            }

        } else {
            if (StringUtils.isEmpty(provinceName)) {
                //根据城市名查询城市编码
                List<SArea> sAreas = sAreaDao.selectByName(cityName);
                if (StringUtils.isEmpty(sAreas)) {
                    return fail(ConstUtil.ERROR_CODE, "所在城市暂未查到");
                }
                for (SArea sArea : sAreas) {
                    if ("province".equals(sArea.getAreaType())) {
                        province = sArea.getAreaCode();
                    } else if ("city".equals(sArea.getAreaType())) {
                        city = sArea.getAreaCode();
                    }
                }
                map.put("province", province);
                map.put("city", city);
            } else {
                //根据省、市名称查询城市
                SArea province_sArea = sAreaDao.getByCodeAndType(provinceName, "province");
                if (StringUtils.isEmpty(province_sArea)) {
                    return fail(ConstUtil.ERROR_CODE, "所在城市暂未查到");
                }
                province = province_sArea.getAreaCode();//省编码
                SArea sArea = sAreaDao.selectByCodeAndAreaParentCode(cityName, province, "city");
                //判断是否有城市编码
                if (!StringUtils.isEmpty(sArea)) {
                    city = sArea.getAreaCode();
                    map.put("province", province);
                    map.put("city", city);
                } else {
                    if (StringUtils.isEmpty(districtName)) {
                        return fail(ConstUtil.ERROR_CODE, "所在城市暂未查到");
                    }
                    //根据省编码和区名称查询是否有城市编码
                    SArea sArea1 = sAreaDao.searchCityCode(province, districtName);
                    if (!StringUtils.isEmpty(sArea1)) {//判断是否有城市编码
                        city = sArea1.getAreaCode();
                        map.put("province", province);
                        map.put("city", city);
                    } else {
                        cityName = districtName;
                        //查询类型为城市的城市编码
                        SArea city1 = sAreaDao.getByCodeAndType(cityName, "city");
                        if (!StringUtils.isEmpty(city1)) {
                            city = city1.getAreaCode();
                            map.put("province", province);
                            map.put("city", city);
                        } else {
                            return fail(ConstUtil.ERROR_CODE, "所在城市暂未查到");
                        }
                    }

                }
            }
        }
        if (StringUtils.isEmpty(province)) {
            //根据城市编码查询省名称
            SArea city1 = sAreaDao.selectByCodeAndAreaType(city, "city");
        }
        return success(map);
    }

}
