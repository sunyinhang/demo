package com.haiercash.payplatform.pc.shunguang.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.haiercash.commons.redis.Session;
import com.haiercash.payplatform.common.dao.AppOrdernoTypgrpRelationDao;
import com.haiercash.payplatform.common.data.AppOrder;
import com.haiercash.payplatform.common.data.AppOrderGoods;
import com.haiercash.payplatform.common.data.AppOrdernoTypgrpRelation;
import com.haiercash.payplatform.service.AppServerService;
import com.haiercash.payplatform.service.CmisApplService;
import com.haiercash.payplatform.service.HaierDataService;
import com.haiercash.payplatform.service.OrderManageService;
import com.haiercash.payplatform.utils.ConstUtil;
import com.haiercash.payplatform.utils.EncryptUtil;
import com.haiercash.payplatform.utils.HttpUtil;
import com.haiercash.payplatform.pc.shunguang.service.SgInnerService;
import com.haiercash.payplatform.service.AcquirerService;
import com.haiercash.payplatform.service.BaseService;
import com.haiercash.payplatform.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yuanli on 2017/8/9.
 */
@Service
public class SgInnerServiceImpl extends BaseService implements SgInnerService {
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
    private HaierDataService haierDataService;
    @Autowired
    private OrderManageService orderManageService;
    @Autowired
    private AcquirerService acquirerService;
    @Value("${app.other.haiercashpay_web_url}")
    protected String haiercashpay_web_url;

    @Override
    public Map<String, Object> userlogin(Map<String, Object> map) {
        logger.info("登录页面********************开始");
        String uidLocal = (String) map.get("userId");
        String password = (String) map.get("password");
        String token = (String) map.get("token");
        String channel = (String) map.get("channel");
        String channelNo = (String) map.get("channelNo");
        if (StringUtils.isEmpty(uidLocal) || StringUtils.isEmpty(password) || StringUtils.isEmpty(token)
                || StringUtils.isEmpty(channel) || StringUtils.isEmpty(channelNo)) {
            logger.info("userId:" + uidLocal + "   token:" + token + "   channelNo:" + channelNo + "   channel:" + channel);
            logger.info("前台获取请求参数有误");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.FAILED_INFO);
        }
        //获取缓存数据
        Map<String, Object> cacheMap = session.get(token, Map.class);
        if (cacheMap == null || "".equals(cacheMap)) {
            logger.info("Jedis数据获取失败");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        //获取会员uid
        String uidHaier = (String) cacheMap.get("uidHaier");
        if (StringUtils.isEmpty(uidHaier)) {
            logger.info("uidHaier:" + uidHaier);
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        //验证并绑定集团用户
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("externUid", EncryptUtil.simpleEncrypt(uidHaier));
        paramMap.put("userId", EncryptUtil.simpleEncrypt(uidLocal));
        paramMap.put("password", EncryptUtil.simpleEncrypt(password));
        Map<String, Object> usermap = appServerService.validateAndBindHaierUser(token, paramMap);
        if (!HttpUtil.isSuccess(usermap)) {
            String retMsg = (String) ((HashMap<String, Object>) (usermap.get("head"))).get("retMsg");
            return fail(ConstUtil.ERROR_CODE, retMsg);
        }
        //获取绑定手机号
        String phoneNo = (String) ((HashMap<String, Object>) (usermap.get("body"))).get("mobile");
        cacheMap.put("userId", uidLocal);//统一认证userId
        cacheMap.put("phoneNo", phoneNo);//绑定手机号
        //4.token绑定
        Map<String, Object> bindMap = new HashMap<String, Object>();
        bindMap.put("userId", uidLocal);//内部userId
        bindMap.put("token", token);
        Map bindresult = appServerService.saveThirdPartToken(bindMap);
        if (!HttpUtil.isSuccess(bindresult)) {//绑定失败
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        }

        //5.查询实名信息
        Map<String, Object> custMap = new HashMap<String, Object>();
        custMap.put("userId", uidLocal);//内部userId
        Map custresult = appServerService.queryPerCustInfo(token, custMap);
        String custretflag = (String) ((HashMap<String, Object>) (custresult.get("head"))).get("retFlag");
        if (!"00000".equals(custretflag) && !"C1220".equals(custretflag)) {//查询实名信息失败
            String custretMsg = (String) ((HashMap<String, Object>) (custresult.get("head"))).get("retMsg");
            return fail(ConstUtil.ERROR_CODE, custretMsg);
        }
        if ("C1220".equals(custretflag)) {//C1120  客户信息不存在  跳转无额度页面
            session.set(token, cacheMap);
            String backurl = haiercashpay_web_url + "sgbt/#!/applyQuota/amountNot.html?token=" + token;
            map.put("backurl", backurl);
            logger.info("页面跳转到：" + backurl);
            return success(map);
        }
        String certType = (String) ((HashMap<String, Object>) (custresult.get("body"))).get("certType");//证件类型
        String certNo = (String) ((HashMap<String, Object>) (custresult.get("body"))).get("certNo");//身份证号
        String custNo = (String) ((HashMap<String, Object>) (custresult.get("body"))).get("custNo");//客户编号
        String custName = (String) ((HashMap<String, Object>) (custresult.get("body"))).get("custName");//客户名称
        String cardNo = (String) ((HashMap<String, Object>) (custresult.get("body"))).get("cardNo");//银行卡号
        String bankNo = (String) ((HashMap<String, Object>) (custresult.get("body"))).get("acctBankNo");//银行代码
        String bankName = (String) ((HashMap<String, Object>) (custresult.get("body"))).get("acctBankName");//银行名称

//        cacheMap.put("custNo", custNo);//客户编号
//        cacheMap.put("name", custName);//客户姓名
//        cacheMap.put("cardNo", cardNo);//银行卡号
//        cacheMap.put("bankCode", bankNo);//银行代码
//        cacheMap.put("bankName", bankName);//银行名称
//        cacheMap.put("idNo", certNo);//身份证号
//        cacheMap.put("idType", certType);

        cacheMap.put("custNo", custNo);//客户编号
        cacheMap.put("name", custName);//客户姓名
        cacheMap.put("cardNo", cardNo);//银行卡号
        cacheMap.put("bankCode", bankNo);//银行代码
        cacheMap.put("bankName", bankName);//银行名称
        cacheMap.put("idNo", certNo);//身份证号
        cacheMap.put("idCard", certNo);//身份证号
        cacheMap.put("idType", certType);
        session.set(token, cacheMap);
        //6.查询客户额度
        Map<String, Object> edMap = new HashMap<String, Object>();
        edMap.put("userId", uidLocal);//内部userId
        edMap.put("channel", "11");
        edMap.put("channelNo", channelNo);
        Map edresult = appServerService.checkEdAppl(token, edMap);
        if (!HttpUtil.isSuccess(edresult)) {//额度校验失败
            String retmsg = (String) ((HashMap<String, Object>) (edresult.get("head"))).get("retMsg");
            return fail(ConstUtil.ERROR_CODE, retmsg);
        }
        //获取自主支付可用额度金额
        String crdNorAvailAmt = (String) ((HashMap<String, Object>) (edresult.get("body"))).get("crdNorAvailAmt");
        if (crdNorAvailAmt != null && !"".equals(crdNorAvailAmt)) {
            //跳转有额度页面
            String backurl = haiercashpay_web_url + "sgbt/#!/payByBt/myAmount.html?token=" + token;
            map.put("backurl", backurl);
            logger.info("页面跳转到：" + backurl);
            return success(map);
        }
        //审批状态判断
        String outSts = (String) ((HashMap<String, Object>) (edresult.get("body"))).get("outSts");
        if ("01".equals(outSts)) {//额度正在审批中
            String backurl = haiercashpay_web_url + "sgbt/#!/applyQuota/applyIn.html?token=" + token;
            map.put("backurl", backurl);
            logger.info("页面跳转到：" + backurl);
            return success(map);
        } else if ("22".equals(outSts)) {//审批被退回
            String crdSeq = (String) ((HashMap<String, Object>) (edresult.get("body"))).get("crdSeq");
            cacheMap.put("crdSeq", crdSeq);
            session.set(token, cacheMap);
            String backurl = haiercashpay_web_url + "sgbt/#!/applyQuota/applyReturn.html?token=" + token;
            map.put("backurl", backurl);
            logger.info("页面跳转到：" + backurl);
            return success(map);
        } else if ("25".equals(outSts)) {//审批被拒绝
            String backurl = haiercashpay_web_url + "sgbt/#!/applyQuota/applyFail.html?token=" + token;
            map.put("backurl", backurl);
            logger.info("页面跳转到：" + backurl);
            return success(map);
        } else {//没有额度  额度激活
            String backurl = haiercashpay_web_url + "sgbt/#!/applyQuota/amountActive.html?token=" + token;
            map.put("backurl", backurl);
            logger.info("页面跳转到：" + backurl);
            return success(map);
        }
    }

    @Override
    public Map<String, Object> initPayApply(Map<String, Object> map) {
        logger.info("白条分期页面加载*******************开始");
        String token = (String) map.get("token");
        String channel = (String) map.get("channel");
        String channelNo = (String) map.get("channelNo");
        String flag = (String) map.get("flag");//1.待提交返显
        String orderNo = (String) map.get("orderNo");//待提交时必传
        Map retrunmap = new HashMap();

        Map<String, Object> cacheMap = session.get(token, Map.class);
        if (cacheMap == null || "".equals(cacheMap)) {
            logger.info("Jedis数据获取失败");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        String updatemallflag = (String) cacheMap.get("updatemallflag");

        String payAmt = "";//申请金额
        String typCde = "";//贷款品种
        String psPerdNo = "";//借款期限
        if ("1".equals(flag) || "1".equals(updatemallflag)) {//待提交返显
            logger.info("待提交订单*********数据加载");

            if ("1".equals(flag)) {
                if (StringUtils.isEmpty(orderNo)) {
                    logger.info("前台传入参数有误");
                    return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
                }
            } else {
                logger.info("退回及待提交进行订单修改");
                orderNo = (String) cacheMap.get("updatemalloderNo");
            }
            //
            AppOrdernoTypgrpRelation AppOrdernoTypgrpRelation = appOrdernoTypgrpRelationDao.selectByOrderNo(orderNo);
            if (AppOrdernoTypgrpRelation == null) {
                logger.info("没有获取到订单信息");
                return fail(ConstUtil.ERROR_CODE, "没有获取到订单信息");
            }
            //获取申请流水号
            String applseq = AppOrdernoTypgrpRelation.getApplSeq();
            logger.info("得到申请流水号：" + applseq);
            //根据applSeq查询商城订单号和网单号
            Map m = orderManageService.getMallOrderNoByApplSeq(applseq);
            if (!HttpUtil.isSuccess(m)) {
                return m;
            }
            Map ordermap = (HashMap<String, Object>) m.get("body");
            String mallOrderNo = (String) ordermap.get("mallOrderNo");
            List<Map<String, Object>> body = (List<Map<String, Object>>) ordermap.get("goodsList");
            List<AppOrderGoods> appOrderGoodsList = new ArrayList<AppOrderGoods>();
            for (int i = 0; i < body.size(); i++) {
                Map<String, Object> good = body.get(i);
                //
                AppOrderGoods appOrderGoods = new AppOrderGoods();
                appOrderGoods.setGoodsBrand((String) good.get("goodsBrand"));//商品品牌
                appOrderGoods.setGoodsKind((String) good.get("goodsModel"));//商品类型
                appOrderGoods.setGoodsName((String) good.get("goodsName"));//商品名称
                appOrderGoods.setGoodsNum(good.get("goodsNum").toString());//商品数量
                appOrderGoods.setGoodsPrice(good.get("goodsPrice").toString());//单价
                appOrderGoods.setcOrderSn((String) good.get("cOrderSn"));//网单号
                appOrderGoods.setGoodsModel((String) good.get("goodsModel"));
                appOrderGoods.setBrandName((String) good.get("goodsBrand"));
                //
                appOrderGoodsList.add(appOrderGoods);
            }
            AppOrder appOrder = new AppOrder();
            appOrder.setMallOrderNo(mallOrderNo);//商城订单号
            appOrder.setAppOrderGoodsList(appOrderGoodsList);
            //根据申请流水号获取送货信息
            Map mapAddress = orderManageService.getAddressByFormId(orderNo);
            if (!HttpUtil.isSuccess(mapAddress)) {
                return mapAddress;
            }
            Map adAddrmap = (HashMap<String, Object>) mapAddress.get("body");
            String adAddr = (String) adAddrmap.get("adAddr");//送货详细地址
            String adProvince = (String) adAddrmap.get("adProvince");//送货地址省
            String adCity = (String) adAddrmap.get("adCity");//送货地址市
            String adArea = (String) adAddrmap.get("adArea");//送货地址区
            //
            appOrder.setDeliverAddr(adAddr);//送货地址
            appOrder.setDeliverProvince(adProvince);//送货地址省
            appOrder.setDeliverCity(adCity);//送货地址市
            appOrder.setDeliverArea(adArea);//送货地址区

            //查询订单详情
            Map<String, Object> mapLoanDetail = acquirerService.getOrderFromAcquirer(applseq, channel, channelNo, null, null, "2");
            if (!HttpUtil.isSuccess(mapLoanDetail)) {
                return mapLoanDetail;
            }
            Map bodyLoanDetail = (HashMap<String, Object>) mapLoanDetail.get("body");
            payAmt = bodyLoanDetail.get("apply_amt").toString();//借款金额
            typCde = (String) bodyLoanDetail.get("typ_cde");//贷款品种
            String applyTnr = (String) bodyLoanDetail.get("apply_tnr");//借款期限
            String applyTnrTyp = (String) bodyLoanDetail.get("apply_tnr_typ");//借款期限类型
            String fst_pay = bodyLoanDetail.get("fst_pay").toString();//首付金额

            appOrder.setTypCde(typCde);//贷款品种代码
            appOrder.setApplyAmt(payAmt);//借款总额
            appOrder.setFstPay(fst_pay);//首付金额

            cacheMap.put("apporder", appOrder);
            session.set(token, cacheMap);

            psPerdNo = applyTnr;
            retrunmap.put("applyTnr", applyTnr);
            retrunmap.put("applyTnrTyp", applyTnrTyp);
        } else {
            logger.info("新订单********数据加载");
            ObjectMapper objectMapper = new ObjectMapper();
            AppOrder appOrder = null;
            try {
                appOrder = objectMapper.readValue(cacheMap.get("apporder").toString(), AppOrder.class);
                logger.info("appOrder0:" + appOrder);
                if (appOrder == null) {
                    return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            payAmt = appOrder.getApplyAmt();//申请金额
            typCde = appOrder.getTypCde();//贷款品种
        }

        logger.info("获取期数以及金额");
        //批量还款试算获取期数*金额
        Map<String, Object> paySsMap = new HashMap<String, Object>();
        paySsMap.put("typCde", typCde);
        paySsMap.put("apprvAmt", payAmt);
        paySsMap.put("channel", channel);
        paySsMap.put("channelNo", channelNo);
        boolean boo = false;
        Map<String, Object> payssresultMap = appServerService.getBatchPaySs(token, paySsMap);
        if (!HttpUtil.isSuccess(payssresultMap)) {//额度校验失败
            String retflag = (String) ((HashMap<String, Object>) (payssresultMap.get("head"))).get("retFlag");
            if ("A1101".equals(retflag)) {//贷款类型为天
                boo = true;
            } else {
                String retmsg = (String) ((HashMap<String, Object>) (payssresultMap.get("head"))).get("retMsg");
                return fail(ConstUtil.ERROR_CODE, retmsg);
            }
        }
        if (!boo) {//贷款类型按月
            //{head={retFlag=00000, retMsg=处理成功}, body={info=[{psPerdNo=12, instmAmt=259.0}, {psPerdNo=6, instmAmt=518.0}]}}
            String result = JSONObject.toJSONString(payssresultMap);
            JSONObject custBody = JSONObject.parseObject(result).getJSONObject("body");
            JSONArray jsonarray = custBody.getJSONArray("info");
            if (!"1".equals(flag)) {//待提交
                for (int i = 0; i < jsonarray.size(); i++) {
                    Object object = jsonarray.get(0);
                    JSONObject json = JSONObject.parseObject(JSONObject.toJSONString(object));
                    //instmAmt = json.getString("instmAmt");
                    psPerdNo = json.getString("psPerdNo");
                    break;
                }
            }
        }
        //还款试算获取金额
        Map<String, Object> payMap = new HashMap<String, Object>();
        payMap.put("typCde", typCde);
        payMap.put("apprvAmt", payAmt);
        if (boo) {//贷款类型按天
            payMap.put("applyTnrTyp", "D");
            payMap.put("applyTnr", "30");
        } else {
            payMap.put("applyTnrTyp", psPerdNo);
            payMap.put("applyTnr", psPerdNo);
        }
        payMap.put("channel", channel);
        payMap.put("channelNo", channelNo);
        Map<String, Object> payresultMap = appServerService.getPaySs(token, payMap);
        if (!HttpUtil.isSuccess(payresultMap)) {//额度校验失败
            String retmsg = (String) ((HashMap<String, Object>) (payresultMap.get("head"))).get("retMsg");
            return fail(ConstUtil.ERROR_CODE, retmsg);
        }
        String payresult = JSONObject.toJSONString(payresultMap);
        JSONObject payBody = JSONObject.parseObject(payresult).getJSONObject("body");
        logger.info("payBody:" + payBody);
        String totalAmt = payBody.get("repaymentTotalAmt").toString();
//        String totalNormInt = payBody.get("totalNormInt").toString();//订单保存（totalNormInt）
//        String totalFeeAmt = payBody.get("totalFeeAmt").toString();//订单保存总利息金额（totalAmt）


        retrunmap.put("payAmt", payAmt);
        retrunmap.put("totalAmt", totalAmt);
        if (boo) {//贷款类型按天
            retrunmap.put("payMtd", "");
        } else {
            retrunmap.put("payMtd", ((HashMap<String, Object>) (payssresultMap.get("body"))).get("info"));
        }

        logger.info("白条分期页面加载*******************结束");
        return success(retrunmap);
    }


    @Override
    public Map<String, Object> gettotalAmt(Map<String, Object> map) {
        logger.info("获取应还款总额******************开始");
        String token = (String) map.get("token");
        String channel = (String) map.get("channel");
        String channelNo = (String) map.get("channelNo");
        String applyTnr = (String) map.get("applyTnr");
        String applyTnrTyp = (String) map.get("applyTnrTyp");

        Map<String, Object> cacheMap = session.get(token, Map.class);
        if (cacheMap == null || "".equals(cacheMap)) {
            logger.info("Jedis数据获取失败");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        ObjectMapper objectMapper = new ObjectMapper();
        AppOrder appOrder = null;
        try {
            appOrder = objectMapper.readValue(cacheMap.get("apporder").toString(), AppOrder.class);
            logger.info("appOrder0:" + appOrder);
            if (appOrder == null) {
                return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String payAmt = appOrder.getApplyAmt();//申请金额
        String typCde = appOrder.getTypCde();//贷款品种

        Map<String, Object> payMap = new HashMap<String, Object>();
        payMap.put("typCde", typCde);
        payMap.put("apprvAmt", payAmt);
        payMap.put("applyTnrTyp", applyTnrTyp);
        payMap.put("applyTnr", applyTnr);
        payMap.put("channel", channel);
        payMap.put("channelNo", channelNo);
        Map<String, Object> payresultMap = appServerService.getPaySs(token, payMap);
        if (!HttpUtil.isSuccess(payresultMap)) {//额度校验失败
            String retmsg = (String) ((HashMap<String, Object>) (payresultMap.get("head"))).get("retMsg");
            return fail(ConstUtil.ERROR_CODE, retmsg);
        }
        String payresult = JSONObject.toJSONString(payresultMap);
        JSONObject payBody = JSONObject.parseObject(payresult).getJSONObject("body");
        logger.info("payBody:" + payBody);
        String totalAmt = payBody.get("repaymentTotalAmt").toString();

        appOrder.setApplyTnr(applyTnr);//借款期限
        appOrder.setApplyTnrTyp(applyTnr);//借款期限类型
        cacheMap.put("apporder", appOrder);
        session.set(token, cacheMap);

        Map retrunmap = new HashMap();
        retrunmap.put("totalAmt", totalAmt);
        logger.info("获取应还款总额******************开始");
        return success(retrunmap);
    }

    @Override
    public String getuserId(String token) {
        String userId = "";
        //1.根据token获取客户信息custNo
        String userjsonstr = haierDataService.userinfo(token);
        if (userjsonstr == null || "".equals(userjsonstr)) {
            logger.info("验证客户信息接口调用失败");
            return userId;
        }
        //{"error_description":"Invalid access token: asadada","error":"invalid_token"}
        //{"user_id":1000030088,"phone_number":"18525369183","phone_number_verified":true,"created_at":1499304958000,"updated_at":1502735413000}
        org.json.JSONObject userjson = new org.json.JSONObject(userjsonstr);
        if (!userjson.has("user_id")) {
            logger.info("没有获取到客户信息");
            return userId;
        }
        Object uid = userjson.get("user_id");//会员id
        if (StringUtils.isEmpty(uid)) {
            String error = userjson.get("error").toString();
            return userId;
        }
        String uidHaier = uid.toString();
        Map<String, Object> cacheMap = session.get(token, Map.class);
        if (cacheMap == null || "".equals(cacheMap)) {
            cacheMap = new HashMap<String, Object>();
        }
        cacheMap.put("uidHaier", uidHaier);
        session.set(token, cacheMap);
        String userInforesult = appServerService.queryHaierUserInfo(EncryptUtil.simpleEncrypt(uidHaier));
        Map<String, Object> resultMap = HttpUtil.json2Map(userInforesult);
        String head = resultMap.get("head").toString();
        Map<String, Object> headMap = HttpUtil.json2Map(head);
        String retFlag = headMap.get("retFlag").toString();
        if ("00000".equals(retFlag)) {
            //集团uid已在统一认证做过绑定
            String body = resultMap.get("body").toString();
            //Map<String, Object> bodyMap = HttpUtil.json2Map(body);
            org.json.JSONObject bodyMap = new org.json.JSONObject(body);
            userId = bodyMap.get("userId").toString();
            return userId;
        } else {
            logger.info("会员验证失败");
            return userId;
        }
    }

    //获取额度回调地址
    @Override
    public Map<String, Object> getedbackurl() {
        logger.info("额度回调*************开始");
        String token = super.getToken();
        Map<String, Object> cacheMap = session.get(token, Map.class);
        if (cacheMap == null || "".equals(cacheMap)) {
            logger.info("Jedis数据获取失败");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        String edbackurl = (String) cacheMap.get("edbackurl");
        logger.info("额度回调地址：" + edbackurl);
        Map m = new HashMap();
        m.put("edbackurl", edbackurl);
        logger.info("额度回调*************结束");
        return success(m);
    }

    //获取贷款回调地址
    @Override
    public Map<String, Object> getpaybackurl() {
        logger.info("贷款回调*************开始");
        String token = super.getToken();
        Map<String, Object> cacheMap = session.get(token, Map.class);
        if (cacheMap == null || "".equals(cacheMap)) {
            logger.info("Jedis数据获取失败");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        String paybackurl = (String) cacheMap.get("paybackurl");
        logger.info("贷款回调地址：" + paybackurl);
        Map m = new HashMap();
        m.put("paybackurl", paybackurl);
        logger.info("贷款回调*************开始");
        return success(m);
    }

    /**
     * @Title approveStatus
     * @Description:额度检验 审批状态判断
     * @author yu jianwei
     * @date 2017/9/14 16:07
     */
    public Map<String, Object> approveStatus(String token) throws Exception {
        if (StringUtils.isEmpty(token)) {
            logger.info("获取token失败token:" + token);
            return fail(ConstUtil.ERROR_CODE, ConstUtil.FAILED_INFO);
        }
        Map<String, Object> cachemap = session.get(token, Map.class);
        if (StringUtils.isEmpty(cachemap)) {
            logger.info("Redis获取缓存失败");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        String channel = super.getChannel();//系统标识
        String channelNo = super.getChannelNo();//渠道编码
        String uidLocal = String.valueOf(cachemap.get("userId"));
        if (StringUtils.isEmpty(channelNo) || StringUtils.isEmpty(channel) || StringUtils.isEmpty(uidLocal)) {
            logger.info("获取的数据为空：uidLocal=" + uidLocal + "  ,channel=" + channel + "  ,channelNO=" + channelNo);
            String retMsg = "获取的数据为空";
            return fail(ConstUtil.ERROR_CODE, retMsg);
        }
        Map returnmap = new HashMap<String, Object>();
        //6.查询客户额度
        Map<String, Object> edMap = new HashMap<String, Object>();
        edMap.put("userId", uidLocal);//内部userId
        edMap.put("channel", channel);
        edMap.put("channelNo", channelNo);
        Map edresult = appServerService.checkEdAppl(token, edMap);
        if (!HttpUtil.isSuccess(edresult)) {//额度校验失败
            String retmsg = ((HashMap<String, Object>) (edresult.get("head"))).get("retMsg").toString();
            return fail(ConstUtil.ERROR_CODE, retmsg);
        }
        String flag = "";//页面跳转标识
        //获取自主支付可用额度金额
        String crdNorAvailAmt = (String) ((HashMap<String, Object>) (edresult.get("body"))).get("crdNorAvailAmt");
        if (crdNorAvailAmt != null && !"".equals(crdNorAvailAmt)) {
            flag = "04";  //跳转有额度页面
            logger.info("=============跳转有额度页面=============");
//            String backurl = haiercashpay_web_url + "sgbt/#!/payByBt/myAmount.html?token=" + token;
//            returnmap.put("backurl", backurl);
            returnmap.put("flag", flag);
            return success(returnmap);
        }
        //审批状态判断
        String outSts = (String) ((HashMap<String, Object>) (edresult.get("body"))).get("outSts");
        logger.info("审批判断页面跳转码" + outSts);
        if ("22".equals(outSts)) {//审批被退回
            String crdSeq = (String) ((HashMap<String, Object>) (edresult.get("body"))).get("crdSeq");
            cachemap.put("crdSeq", crdSeq);
            session.set(token, cachemap);
            flag = "03";
        } else if ("25".equals(outSts)) {//审批被拒绝
            flag = "02";
        } else if ("01".equals(outSts)) {//额度正在审批中
            flag = "01";
        } else {//没有额度
            flag = "03";
        }
        returnmap.put("flag", flag);
        return success(returnmap);
    }
}
