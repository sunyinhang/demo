package com.haiercash.payplatform.pc.shunguang.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.haiercash.commons.redis.Session;
import com.haiercash.payplatform.config.EurekaServer;
import com.haiercash.payplatform.common.dao.AppOrdernoTypgrpRelationDao;
import com.haiercash.payplatform.common.data.AppOrder;
import com.haiercash.payplatform.common.data.AppOrdernoTypgrpRelation;
import com.haiercash.payplatform.service.AppServerService;
import com.haiercash.payplatform.service.CmisApplService;
import com.haiercash.payplatform.service.CrmManageService;
import com.haiercash.payplatform.service.HaierDataService;
import com.haiercash.payplatform.utils.*;
import com.haiercash.payplatform.pc.shunguang.service.SaveOrderService;
import com.haiercash.payplatform.pc.shunguang.service.SgInnerService;
import com.haiercash.payplatform.service.BaseService;
import com.haiercash.payplatform.service.OrderService;
import com.haiercash.payplatform.utils.ConstUtil;
import com.haiercash.payplatform.utils.HttpUtil;
import com.haiercash.payplatform.utils.RestUtil;
import org.json.JSONObject;
//import com.alibaba.fastjson.JSONArray;
//import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by use on 2017/8/16.
 */
@Service
public class SaveOrderServiceImpl extends BaseService implements SaveOrderService {

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
    private SgInnerService sgInnerService;
    @Autowired
    private HaierDataService haierDataService;
    @Autowired
    private CrmManageService crmManageService;

    @Value("${app.shunguang.sg_merch_no}")
    protected String sg_merch_no;
    @Value("${app.shunguang.sg_store_no}")
    protected String sg_store_no;
    @Value("${app.shunguang.sg_user_id}")
    protected String sg_user_id;
    @Value("${app.shunguang.sg_shopkeeper}")
    protected String sg_shopkeeper;
    @Value("${app.shunguang.sg_consumer}")
    protected String sg_consumer;


    @Override
    public Map<String, Object> saveOrder(Map<String, Object> map) {
        logger.info("订单保存****************开始");
        //前端传入参数获取(放开)
        String token = (String) map.get("token");
        String channel = (String) map.get("channel");
        String channelNo = (String) map.get("channelNo");
        String applyTnr = (String) map.get("applyTnr");//借款期限
        String applyTnrTyp = (String) map.get("applyTnrTyp");//期限类型（若天则传D）
        String updflag = (String) map.get("flag");//1.待提交返显
        String orderNo = (String) map.get("orderNo");//待提交时必传
        String areaCode = (String) map.get("areaCode");//区编码
        //非空判断
        if(StringUtils.isEmpty(token) || StringUtils.isEmpty(channel) || StringUtils.isEmpty(channelNo)
                || StringUtils.isEmpty(applyTnr) || StringUtils.isEmpty(applyTnrTyp)){
            logger.info("token:" + token + "  channel:" + channel + "   channelNo:" + channelNo
                       + "   applyTnr:" + applyTnr + "   applyTnrTyp" + applyTnrTyp + "   updflag:" + updflag + "  orderNo:" + orderNo);
            logger.info("前台获取数据有误");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        }

        //appOrder缓存获取（放开）
        Map<String, Object> cacheMap = session.get(token, Map.class);
        if (cacheMap == null || "".equals(cacheMap)) {
            logger.info("Jedis数据获取失败");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }


        ObjectMapper objectMapper = new ObjectMapper();
        AppOrder appOrder = null;
        try {
            logger.info("缓存数据获取");
            appOrder = objectMapper.readValue(cacheMap.get("apporder").toString(), AppOrder.class);
            logger.info("提交订单信息appOrder:" + appOrder);
            if(appOrder == null){
                return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        //根据token获取统一认证userid
        String userId = sgInnerService.getuserId(token);
        if(StringUtils.isEmpty(userId)){
            logger.info("根据用户中心token获取统一认证userId失败");
            return fail(ConstUtil.ERROR_CODE, "获取内部注册信息失败");
        }
        //TODO!!!!
        //String userId = cacheMap.get("userId").toString();

        //获取客户信息
        logger.info("订单保存，根据userId获取客户信息");
        Map<String, Object> custMap = new HashMap<String, Object>();
        custMap.put("userId", userId);
        custMap.put("channel", channel);
        custMap.put("channelNo", channelNo);
        Map<String, Object> custInforesult = appServerService.queryPerCustInfo(token, custMap);
        if (!HttpUtil.isSuccess(custInforesult) ) {
            return fail(ConstUtil.ERROR_CODE, "获取实名信息失败");
        }
        String payresultstr = com.alibaba.fastjson.JSONObject.toJSONString(custInforesult);
        com.alibaba.fastjson.JSONObject custresult = com.alibaba.fastjson.JSONObject.parseObject(payresultstr).getJSONObject("body");
        String custName = (String) custresult.get("custName");
        String custNo = (String) custresult.get("custNo");
        logger.info("客户编号：" +custNo + "   客户姓名：" + custName);
        String certNo = (String) custresult.get("certNo");
        String mobile = (String) custresult.get("mobile");

        //获取客户标签
        logger.info("获取标签");
        Map tagmap = new HashMap<>();
        tagmap.put("custName", custName);//姓名
        tagmap.put("idTyp", "20");//证件类型
        tagmap.put("idNo", certNo);//证件号码
        Map tagmapresult = crmManageService.getCustTag("", tagmap);
        if(!HttpUtil.isSuccess(tagmapresult)){
            return tagmapresult;
        }
        //TODO!!!!
        String userType = (String) cacheMap.get("userType");
        //String userType = "01";
        String tagId = "";
        if("01".equals(userType)){//微店主
            tagId = sg_shopkeeper;
        }
        if("02".equals(userType)){//消费者
            tagId = sg_consumer;
        }
        //
        Boolean b = false;
        List<Map<String, Object>> body = (List<Map<String, Object>>) tagmapresult.get("body");
        for (int i = 0; i < body.size(); i++) {
            Map<String, Object> m = body.get(i);
            String tagid = m.get("tagId").toString();
            if(tagid.equals(tagId)){
                b = true;
            }
        }
        //若不存在进行添加  /app/crm/cust/setCustTag
        if(!b){
            logger.info("打标签");
            Map addtagmap = new HashMap<>();
            addtagmap.put("certNo", certNo);//身份证号
            addtagmap.put("tagId", tagId);//自定义标签ID
            Map addtagmapresult = crmManageService.setCustTag("", addtagmap);
            if(!HttpUtil.isSuccess(addtagmapresult)){
                return addtagmapresult;
            }
        }


        //获取订单金额  总利息 金额
        logger.info("订单保存，获取订单金额，总利息金额");
        //applyTnrTyp = applyTnr;
        Map<String, Object> payMap = new HashMap<String, Object>();
        payMap.put("typCde", appOrder.getTypCde());
        payMap.put("apprvAmt", appOrder.getApplyAmt());
        payMap.put("applyTnrTyp", applyTnrTyp);
        payMap.put("applyTnr", applyTnr);
        payMap.put("channel", channel);
        payMap.put("channelNo", channelNo);
        Map<String, Object> payresultMap =  appServerService.getPaySs(token, payMap);
        if (!HttpUtil.isSuccess(payresultMap) ) {//额度校验失败
            String retmsg = (String) ((HashMap<String, Object>)(payresultMap.get("head"))).get("retMsg");
            return fail(ConstUtil.ERROR_CODE, retmsg);
        }
        String payresult = com.alibaba.fastjson.JSONObject.toJSONString(payresultMap);
        com.alibaba.fastjson.JSONObject payBody = com.alibaba.fastjson.JSONObject.parseObject(payresult).getJSONObject("body");
        logger.info("payBody:" + payBody);
        String totalAmt = payBody.get("totalAmt").toString();
        String totalNormInt = payBody.get("totalNormInt").toString();//订单保存（totalNormInt）
        String totalFeeAmt = payBody.get("totalFeeAmt").toString();//订单保存总利息金额（totalAmt）

        //获取订单信息
        logger.info("订单保存，获取订单信息");
        appOrder.setVersion("1");//接口版本号  固定传’1’
        appOrder.setSource("11");//订单来源
        appOrder.setChannelNo((String)map.get("channelNo"));//渠道编号
        appOrder.setApplyTnr(applyTnr);//借款期限
        appOrder.setApplyTnrTyp(applyTnrTyp);//借款期限类型
        appOrder.setTotalnormint(totalNormInt);//总利息金额
        appOrder.setTotalfeeamt(totalFeeAmt);//费用总额
        appOrder.setMerchNo(sg_merch_no);//商户编号
        appOrder.setCooprCde(sg_store_no);//门店编号
        appOrder.setCrtUsr(sg_user_id);//销售代表用户ID（）
        appOrder.setTypGrp("01");//贷款类型  01:商品贷  02  现金贷
        appOrder.setSource(ConstUtil.SOURCE);//订单来源
        appOrder.setWhiteType("SHH");//白名单类型
        appOrder.setFormType("10");//10:线下订单   20:线上订单
        appOrder.setCustNo(custNo);//客户编号
        appOrder.setCustName(custName);//客户姓名
        appOrder.setIdTyp("20");//证件类型
        appOrder.setIdNo(certNo);//客户证件号码
        appOrder.setUserId(userId);//录单用户ID
        appOrder.setChannelNo(channelNo);
        //appOrder.setFstPay("0");//首付金额
        String updatemallflag = (String) cacheMap.get("updatemallflag");
        if("1".equals(updflag) || "1".equals(updatemallflag)){//待提交订单
            if("1".equals(updflag)){
                if(StringUtils.isEmpty(orderNo)){
                    logger.info("前台传入参数有误");
                    return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
                }
                appOrder.setOrderNo(orderNo);
            }else{
                logger.info("退回及待提交进行订单保存");
                orderNo = (String) cacheMap.get("updatemalloderNo");
                appOrder.setOrderNo(orderNo);
            }
        }
        logger.info("订单信息：" + appOrder);

        //0.准入资格校验
        logger.info("进行准入资格校验");
        Map<String, Object> ispassmap = new HashMap<String, Object>();
        ispassmap.put("custName", custName);//姓名
        ispassmap.put("certNo", certNo);//身份证
        ispassmap.put("phonenumber", mobile);//手机号
        ispassmap.put("userId", userId);//登录用户名
        ispassmap.put("channel", channel);
        ispassmap.put("channelNo", channelNo);
        Map<String, Object> ispassresult = appServerService.getCustIsPass(token, ispassmap);
        if (!HttpUtil.isSuccess(ispassresult) ) {//准入资格校验失败
            String retmsg = (String) ((HashMap<String, Object>)(ispassresult.get("head"))).get("retMsg");
            return fail(ConstUtil.ERROR_CODE, retmsg);
        }
        String isPass = (String) ((HashMap<String, Object>)(ispassresult.get("body"))).get("isPass");
        if("-1".equals(isPass)){
            return fail(ConstUtil.ERROR_CODE, "没有准入资格");
        }

        //1.录单校验（所在城市开通服务）
        //获取市代码
        String cityCode = "";
        String provinceCode = "";
        if(StringUtils.isEmpty(areaCode)){
            cityCode = "370000";
            provinceCode = "370200";
        }else{
            logger.info("获取业务发生地省市区");
            Map<String, Object > citymap = new HashMap<String, Object>();
            citymap.put("areaCode", areaCode);
            citymap.put("flag", "parent");
            citymap.put("channel", channel);
            citymap.put("channelNo", channelNo);
            cityCode = this.getCode(token, citymap);
            if(StringUtils.isEmpty(cityCode)){
                logger.info("获取市编码失败");
                return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
            }
            //获取省代码
            Map<String, Object > provincemap = new HashMap<String, Object>();
            provincemap.put("areaCode", cityCode);
            provincemap.put("flag", "parent");
            provincemap.put("channel", channel);
            provincemap.put("channelNo", channelNo);
            provinceCode = this.getCode(token, provincemap);
            if(StringUtils.isEmpty(provinceCode)){
                logger.info("获取省编码失败");
                return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
            }
        }
        //录单校验
        logger.info("进行录单校验");
        Map<String, Object> ordercheakmap = new HashMap<String, Object>();
        ordercheakmap.put("userId", userId);
        ordercheakmap.put("provinceCode", provinceCode);
        ordercheakmap.put("cityCode", cityCode);
        ordercheakmap.put("channel", channel);
        ordercheakmap.put("channelNo", channelNo);
        Map<String, Object> ordercheakresult = appServerService.getCustInfoAndEdInfoPerson(token, ordercheakmap);
        if (!HttpUtil.isSuccess(ordercheakresult) ) {//录单校验失败
            String retmsg = (String) ((HashMap<String, Object>)(ordercheakresult.get("head"))).get("retMsg");
            return fail(ConstUtil.ERROR_CODE, retmsg);
        }

        //2.是否允许申请贷款
        logger.info("查看是否允许申请贷款");
        String typCde = appOrder.getTypCde();
        SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd");
        String date = dateFormater.format(new Date());
        Map<String, Object> queryordermap = new HashMap<String, Object>();
        queryordermap.put("typCde", typCde);
        queryordermap.put("date", date);
        queryordermap.put("channel", channel);
        queryordermap.put("channelNo", channelNo);
        Map<String, Object> queryorderresult = appServerService.queryBeyondContral(token, queryordermap);
        if (!HttpUtil.isSuccess(queryorderresult) ) {//是否允许申请贷款失败
            String retmsg = (String) ((HashMap<String, Object>)(queryorderresult.get("head"))).get("retMsg");
            return fail(ConstUtil.ERROR_CODE, retmsg);
        }
        String flag = (String) ((HashMap<String, Object>)(queryorderresult.get("body"))).get("flag");
        if(!"Y".equals(flag)){
            return fail(ConstUtil.ERROR_CODE, "不允许申请贷款");
        }

        //3.订单保存
        Map<String, Object> ordermap = saveAppOrderInfo(appOrder);
        cacheMap.put("ordermap", ordermap);
        session.set(token, cacheMap);
        logger.info("订单保存结果：" + ordermap.toString());
        if (!HttpUtil.isSuccess(ordermap) ) {//订单保存失败
            logger.info("订单保存失败");
            Map resultHead = (LinkedHashMap<String, Object>)(ordermap.get("head"));
            String retmsg = resultHead.get("retMsg").toString();
            //String retmsg = resultHead.getRetMsg();
            //String retmsg = (String) ((HashMap<String, Object>)(ordermap.get("head"))).get("retMsg");
            return fail(ConstUtil.ERROR_CODE, retmsg);
        }

        return ordermap;
    }

    public String getCode(String token, Map<String, Object> citymap) {
        String cityCode = "";
        // 根据区号获取市
        Map<String, Object> result = appServerService.getAreaInfo(token, citymap);
        String retFlag = (String) ((HashMap<String, Object>)result.get("head")).get("retFlag");
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


    public Map<String, Object> saveAppOrderInfo(AppOrder appOrder) {
        appOrder.setApplyDt(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        appOrder.setIsConfirmAgreement("0");// 0-未确认
        appOrder.setIsConfirmContract("0");// 0-未确认
        appOrder.setProPurAmt("0");// 商品总额，默认为0
        appOrder.setIsCustInfoCompleted("N");// 个人信息是否完整 默认为N 否

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
        String autoFlag = appOrder.getTypGrp().equals("02") ? "N" : "S";

        String orderNo = "";
        String applSeq = "";
        if ("02".equals(appOrder.getTypGrp())) {
//            Map<String, Object> resultResponseMap = acquirerService.cashLoan(appOrder, null);
//            if (CmisUtil.getIsSucceed(resultResponseMap)) {
//                Map<String, Object> bodyMap = (Map<String, Object>) ((Map<String, Object>) resultResponseMap
//                        .get("response")).get("body");
//                applSeq = (String) bodyMap.get("applSeq");
//                orderNo = (String) bodyMap.get("applSeq");
//                this.saveRelation(applSeq, appOrder.getTypGrp(), applSeq, appOrder.getCustNo());
//            } else {
//                return (Map<String, Object>) resultResponseMap.get("response");
//            }
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

        return success();
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
            Map<String, Object> hkssBodyMap = (HashMap<String, Object>) hkss_json.get("body");
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
}
