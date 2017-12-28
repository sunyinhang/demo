package com.haiercash.payplatform.pc.shunguang.service.impl;

import com.haiercash.core.collection.MapUtils;
import com.haiercash.core.lang.Base64Utils;
import com.haiercash.core.lang.Convert;
import com.haiercash.mybatis.util.DateUtil;
import com.haiercash.payplatform.common.dao.CooperativeBusinessDao;
import com.haiercash.payplatform.common.dao.SgRegionsDao;
import com.haiercash.payplatform.common.dao.SgReturngoodsLogDao;
import com.haiercash.payplatform.common.data.AppOrder;
import com.haiercash.payplatform.common.data.AppOrderGoods;
import com.haiercash.payplatform.common.data.CooperativeBusiness;
import com.haiercash.payplatform.common.data.SgRegions;
import com.haiercash.payplatform.common.data.SgReturngoodsLog;
import com.haiercash.payplatform.config.CommonConfig;
import com.haiercash.payplatform.config.OutreachConfig;
import com.haiercash.payplatform.config.ShunguangConfig;
import com.haiercash.payplatform.pc.shunguang.service.SgInnerService;
import com.haiercash.payplatform.pc.shunguang.service.ShunguangService;
import com.haiercash.payplatform.service.AcquirerService;
import com.haiercash.payplatform.service.AppServerService;
import com.haiercash.payplatform.service.CrmService;
import com.haiercash.payplatform.service.HaierDataService;
import com.haiercash.payplatform.service.OrderManageService;
import com.haiercash.payplatform.utils.AcqTradeCode;
import com.haiercash.payplatform.utils.DesUtil;
import com.haiercash.payplatform.utils.EncryptUtil;
import com.haiercash.payplatform.utils.FormatUtil;
import com.haiercash.payplatform.utils.RSAUtils;
import com.haiercash.spring.redis.RedisUtils;
import com.haiercash.spring.rest.client.JsonClientUtils;
import com.haiercash.spring.service.BaseService;
import com.haiercash.spring.util.ConstUtil;
import com.haiercash.spring.util.HttpUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * shunguang service impl.
 *
 * @author yuan li
 * @since v1.0.1
 */
@Service
public class ShunguangServiceImpl extends BaseService implements ShunguangService {
    @Autowired
    private AppServerService appServerService;
    @Autowired
    private CooperativeBusinessDao cooperativeBusinessDao;
    @Autowired
    private HaierDataService haierDataService;
    @Autowired
    private CrmService crmService;
    @Autowired
    private SgRegionsDao sgRegionsDao;
    @Autowired
    private SgInnerService sgInnerService;
    @Autowired
    private OrderManageService orderManageService;
    @Autowired
    private OutreachConfig outreachConfig;
    @Autowired
    private AcquirerService acquirerService;
    @Autowired
    private CommonConfig commonConfig;
    @Autowired
    private SgReturngoodsLogDao shunGuangthLogDao;
    @Autowired
    private ShunguangConfig shunguangConfig;

    public static Map<String, Object> getAcqHead(String tradeCode, String sysFlag, String channelNo, String cooprCode, String tradeType) {
        Map<String, Object> headMap = new HashMap<>();
        Date now = new Date();
        headMap.put("serno", UUID.randomUUID().toString().replaceAll("-", ""));
        headMap.put("tradeDate", DateUtil.formatDate(now, "yyyy-MM-dd"));
        headMap.put("tradeTime", DateUtil.formatDate(now, "HH:mm:ss"));
        headMap.put("tradeCode", tradeCode);
        headMap.put("sysFlag", sysFlag);
        headMap.put("channelNo", channelNo);
        headMap.put("cooprCode", StringUtils.isEmpty(cooprCode) ? "" : cooprCode);
        headMap.put("tradeType", StringUtils.isEmpty(tradeType) ? "" : tradeType);
        return headMap;
    }

    @Override
    public Map<String, Object> saveStoreInfo(Map<String, Object> storeInfo) {
        logger.info("微店主信息推送");
        Map<String, Object> result = this.savePeopleInfo(storeInfo);
        if (!HttpUtil.isSuccess(result)) {
            logger.error("保存微店主客户信息失败,处理结果:" + result);
        }
        return result;
    }

    @Override
    public Map<String, Object> saveOrdinaryUserInfo(Map<String, Object> ordinaryInfo) {
        logger.info("普通用户消息推送");
        Map<String, Object> result = this.savePeopleInfo(ordinaryInfo);
        if (!HttpUtil.isSuccess(result)) {
            logger.error("保存普通用户信息失败,处理结果:" + result);
        }
        return result;
    }

    /**
     * 通用保存用户信息方法.
     *
     * @param info 用户信息
     * @return 处理结果Map
     */
    private Map<String, Object> savePeopleInfo(Map<String, Object> info) {
        String channelNo = info.get("channelNo").toString();
        String tradeCode = (String) info.get("tradeCode");//交易编码
        String data = (String) info.get("data");//交易信息
        String key = (String) info.get("key");

        logger.info("交易编码：" + tradeCode);
        String params;
        try {
            params = this.decryptData(data, channelNo, key);
        } catch (Exception e) {
            logger.error(e);
            return fail("01", "请求数据校验失败");
        }
        logger.info("客户推送接口请求数据：" + params);
        JSONObject json = new JSONObject(params);
        String userid = (String) json.get("userid");
        String body = json.get("body").toString();
        logger.info("消息推送会员userId:" + userid);

        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("userId", userid);
        requestParams.put("channelno", channelNo);
        requestParams.put("applseq", null);
        requestParams.put("cardnumber", null);
        requestParams.put("data", new JSONObject(body));

        String url = outreachConfig.getUrl() + "/Outreachplatform/api/externalData/savaExternalData";
        logger.info("推送外联风险信息，请求地址：" + url);
        String resData = JsonClientUtils.postForString(url, requestParams);
        logger.info("推送外联风险信息，返回数据：" + resData);
        JSONObject result = new JSONObject(resData);
        if (!"0000".equals(result.get("code"))) {
            return fail("02", ConstUtil.ERROR_INFO);
        }
        return success();
    }

    @Override
    public Map<String, Object> payApply(Map<String, Object> map) {
        logger.info("白条支付申请接口*******************开始");
        Map cachemap = new HashMap<String, Object>();
        String channelNo = map.get("channelNo").toString();
        String data = (String) map.get("data");//交易信息
        String key = (String) map.get("key");

        String params;
        try {
            params = this.decryptData(data, channelNo, key);
        } catch (Exception e) {
            logger.error(e);
            return fail("01", "请求数据校验失败");
        }
        logger.info("支付申请接口请求数据：" + params);
        JSONObject json = new JSONObject(params);
        //json参数非空判断
        if (StringUtils.isEmpty(json.get("token")) || StringUtils.isEmpty(json.get("userType"))
                || StringUtils.isEmpty(json.get("URL")) || StringUtils.isEmpty(json.get("body"))) {
            logger.info("贷款申请**必传参数非空校验失败");
            return fail(ConstUtil.ERROR_CODE, "贷款申请，必传参数非空校验失败");
        }

        String token = (String) json.get("token");
        String userType = (String) json.get("userType");
        String body = json.get("body").toString();//本比订单信息
        String URL = (String) json.get("URL");
        JSONObject bodyjson = new JSONObject(body);

        String userId = sgInnerService.getuserId(token);
        if (StringUtils.isEmpty(userId)) {
            logger.info("根据用户中心token获取统一认证userId失败");
            return fail(ConstUtil.ERROR_CODE, "获取内部注册信息失败");
        }

        //根据userId获取客户编号
        Map<String, Object> custMap = new HashMap<>();
        custMap.put("userId", userId);
        custMap.put("channel", ConstUtil.CHANNEL);
        custMap.put("channelNo", channelNo);
        Map<String, Object> custInforesult = appServerService.queryPerCustInfo(token, custMap);
        if (!HttpUtil.isSuccess(custInforesult)) {
            logger.info("订单提交，获取实名信息失败");
            return fail(ConstUtil.ERROR_CODE, "获取实名信息失败");
        }
        String payresultstr = com.alibaba.fastjson.JSONObject.toJSONString(custInforesult);
        com.alibaba.fastjson.JSONObject custresult = com.alibaba.fastjson.JSONObject.parseObject(payresultstr).getJSONObject("body");
        String custName = (String) custresult.get("custName");
        String certNo = (String) custresult.get("certNo");

        //bodyjson参数非空判断
        if (StringUtils.isEmpty(bodyjson.get("orderSn")) || StringUtils.isEmpty(json.get("userType"))
                || StringUtils.isEmpty(json.get("URL")) || StringUtils.isEmpty(json.get("body"))) {
            logger.info("贷款申请**必传参数非空校验失败");
            return fail(ConstUtil.ERROR_CODE, "贷款申请，必传参数非空校验失败");
        }

        String orderSn = bodyjson.getString("orderSn");//商城订单号
        String loanType = bodyjson.getString("loanType");//贷款品种编码
        String payAmt = bodyjson.getString("payAmt");//订单实付金额
        double payAmount = Double.parseDouble(payAmt);
        if (payAmount < 600) {
            logger.info("单笔支付金额需大于等于600元");
            return fail(ConstUtil.ERROR_CODE, "单笔支付金额需大于等于600元");
        }
        String province;//省
        String city;//市
        String country = bodyjson.getString("country");//区
        String detailAddress = bodyjson.getString("detailAddress");//详细地址
        String ordermessage = bodyjson.get("ordermessage").toString();//网单信息
        JSONArray jsonArray = new JSONArray(ordermessage);
        List<AppOrderGoods> appOrderGoodsList = new ArrayList<>();
        Double totalcOrderAmt = 0.0;
        for (int j = 0; j < jsonArray.length(); j++) {
            JSONObject jsonm = new JSONObject(jsonArray.get(j).toString());
            String cOrderSn = (String) jsonm.get("cOrderSn");//网单编号
            String topLevel = (String) jsonm.get("topLevel");//一级类目
            String model = (String) jsonm.get("model");//型号
            String sku = (String) jsonm.get("sku");//商品品类编码
            String price = (String) jsonm.get("price");//价格
            String num = (String) jsonm.get("num");//数量
            String cOrderAmt = (String) jsonm.get("cOrderAmt");//网单金额
            String cOrderPayAmt = (String) jsonm.get("cOrderPayAmt");//网单实付金额

            totalcOrderAmt = totalcOrderAmt + Double.parseDouble(cOrderAmt);

            AppOrderGoods appOrderGoods = new AppOrderGoods();
            appOrderGoods.setcOrderSn(cOrderSn);//
            appOrderGoods.setGoodsName(model);//商品名称
            appOrderGoods.setGoodsNum(num);//商品数量
            appOrderGoods.setGoodsPrice(price);//单价
            appOrderGoods.setGoodsModel(model);//商品类型
            appOrderGoods.setBrandName(topLevel);//商品品牌
            appOrderGoods.setSkuCode(sku);//sku码
            appOrderGoodsList.add(appOrderGoods);
        }
        //首付金额
        Double fstPay = totalcOrderAmt - Double.parseDouble(payAmt);

        //

        SgRegions sgRegions = sgRegionsDao.selectByRegionId(country);
        country = sgRegions.getGbCode();//区编码
        province = country.substring(0, 2) + "0000";//省编码
        city = country.substring(0, 4) + "00";//市编码

        AppOrder appOrder = new AppOrder();
        appOrder.setMallOrderNo(orderSn);//商城订单号
        appOrder.setTypCde(loanType);//贷款品种代码
        appOrder.setApplyAmt(payAmt);//借款总额
        appOrder.setDeliverAddr(detailAddress);//送货地址
        appOrder.setDeliverProvince(province);//送货地址省
        appOrder.setDeliverCity(city);//送货地址市
        appOrder.setDeliverArea(country);//送货地址区
        appOrder.setAppOrderGoodsList(appOrderGoodsList);
        appOrder.setFstPay(fstPay.toString());//首付金额


        //根据商城订单号查询订单接口
        //若flag   N  则为新订单   outsts   91(取消)  新单
        //         Y  applseq  formId   outsts 00  90  修改   其他  成功
        //根据商城订单号查询订单信息
        Map<String, Object> mallordermap = orderManageService.getOrderStsByMallOrder(orderSn);
        if (!HttpUtil.isSuccess(mallordermap)) {
            return mallordermap;
        }
        Map bodymap = (Map<String, Object>) mallordermap.get("body");
        String formId = (String) bodymap.get("formId");
        String flag = (String) bodymap.get("flag");
        String applSeq = (String) bodymap.get("applSeq");
        String sysSts = (String) bodymap.get("sysSts");
        logger.info("applSeq1:" + applSeq);

        String f = "0";
        if ("N".equals(flag) || "91".equals(sysSts)) {//作为新单处理

        } else if ("00".equals(sysSts) || "90".equals(sysSts)) {//作为修改订单处理
            cachemap.put("updatemallflag", "1");//修改标识
            cachemap.put("updatemalloderNo", formId);//要修改的订单编号
            appOrder.setApplSeq(applSeq);
            logger.info("applSeq2:" + applSeq);
        } else {//订单已提交成功
            f = "1";
        }


        //
        cachemap.put("userType", userType);//01:微店主  02:消费者
        cachemap.put("paybackurl", URL);//支付申请回调url
        cachemap.put("apporder", appOrder);//
        cachemap.put("name", custName);
        cachemap.put("idNo", certNo);
        cachemap.put("userId", userId);
        RedisUtils.setExpire(token, cachemap);
        logger.info("name:" + custName + "  idNo:" + certNo + "  userId:" + userId);
        Map returnmap = new HashMap<>();
        String backurl;
        if ("1".equals(f)) {//订单已提交成功
            backurl = commonConfig.getGateUrl() + "/sgbt/#!/payByBt/loanResult.html?utm_source=sgbt&utm_medium=sgbt&utm_campaign=sgbt&utm_content=sgbt&utm_term=sgbt&token=" + token + "&applSeq=" + applSeq;
        } else {
            backurl = commonConfig.getGateUrl() + "/sgbt/#!/payByBt/btInstalments.html?utm_source=sgbt&utm_medium=sgbt&utm_campaign=sgbt&utm_content=sgbt&utm_term=sgbt&token=" + token;
        }
        logger.info("支付跳转页面：" + backurl);
        returnmap.put("backurl", backurl);
        return success(returnmap);
    }

    @Override
    public Map<String, Object> edApply(Map<String, Object> map) {
        logger.info("白条额度申请接口*******************开始");
        Map cachemap = new HashMap<String, Object>();
        String channelNo = map.get("channelNo").toString();
        String data = (String) map.get("data");//交易信息
        String key = (String) map.get("key");

        String params;
        try {
            params = this.decryptData(data, channelNo, key);
        } catch (Exception e) {
            logger.error(e);
            return fail("01", "请求数据校验失败");
        }
        logger.info("额度申请接口请求数据：" + params);
        JSONObject json = new JSONObject(params);

        if (StringUtils.isEmpty(json.get("token")) || StringUtils.isEmpty(json.get("userType"))
                || StringUtils.isEmpty(json.get("URL")) || StringUtils.isEmpty(json.get("custmessage"))) {
            logger.info("额度申请**必传参数非空校验失败");
            return fail(ConstUtil.ERROR_CODE, "额度申请，必传参数非空校验失败");
        }

        String token = (String) json.get("token");
        String userType = (String) json.get("userType");
        String URL = (String) json.get("URL");//额度回调url
        String custmessage = json.get("custmessage").toString();
        JSONObject custjson = new JSONObject(custmessage);
        String idNoHaier = "";
        if (custjson.has("idNo")) {//第三方推送的身份证号
            idNoHaier = (String) custjson.get("idNo");
            cachemap.put("idNoHaier", idNoHaier);
        }

        //1.根据token获取客户信息
        String userjsonstr = haierDataService.userinfo(token);
        if (userjsonstr == null || "".equals(userjsonstr)) {
            logger.info("验证客户信息接口调用失败");
            return fail(ConstUtil.ERROR_CODE, "验证客户信息失败");
        }
        //{"error_description":"Invalid access token: asadada","error":"invalid_token"}
        //{"user_id":1000030088,"phone_number":"18525369183","phone_number_verified":true,"created_at":1499304958000,"updated_at":1502735413000}
        JSONObject userjson = new JSONObject(userjsonstr);
        if (!userjson.has("user_id")) {
            return fail(ConstUtil.ERROR_CODE, "没有获取到客户信息");
        }
        Object uid = userjson.get("user_id");//会员id
        if (StringUtils.isEmpty(uid)) {
            String error = userjson.get("error").toString();
            return fail(ConstUtil.ERROR_CODE, error);
        }
        String uidHaier = uid.toString();
        String custPhoneNo = (String) userjson.get("phone_number");

        cachemap.put("edbackurl", URL);
        cachemap.put("token", token);
        cachemap.put("uidHaier", uidHaier);//会员id
        cachemap.put("userType", userType);//01:微店主  02:消费者

        //2.查看是否绑定手机号
        if (custPhoneNo.isEmpty()) {
            return fail(ConstUtil.ERROR_CODE, "客户未进行手机号绑定");
        }

        //3.查看外部uid绑定   /app/appserver/uauth/queryHaierUserInfo?externUid
        //未绑定   去注册    1.注册成功（OK）   2.嗨付已注册未与集团绑定   （走登录）
        //已绑定   去统一认证查询内部userid
        String uidLocal;//统一认证userid
        String phoneNo;//统一认证绑定手机号
        Map returnmap = new HashMap<String, Object>();//返回的map
        if (uidHaier.isEmpty()) {
            return fail(ConstUtil.ERROR_CODE, "未获取到userId");
        }
        logger.info("海尔会员ID：" + uidHaier);
        String userInforesult = appServerService.queryHaierUserInfo(EncryptUtil.simpleEncrypt(uidHaier));
        if (StringUtils.isEmpty(userInforesult)) {
            return fail(ConstUtil.ERROR_CODE, "根据集团用户ID查询用户信息失败");
        }
        Map<String, Object> resultMap = HttpUtil.json2Map(userInforesult);
        String head = resultMap.get("head").toString();
        Map<String, Object> headMap = HttpUtil.json2Map(head);
        String retFlag = headMap.get("retFlag").toString();
        switch (retFlag) {
            case "U0157": //U0157：未查到该集团用户的信息
                //用户未注册   进行注册
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("mobile", EncryptUtil.simpleEncrypt(custPhoneNo)); //海尔集团登录用户名

                paramMap.put("externUid", EncryptUtil.simpleEncrypt(uidHaier)); //海尔集团userId

                //paramMap.put("userName", EncryptUtil.simpleEncrypt(userName == null ? "" : userName));
                Map usermap = appServerService.saveUauthUsersByHaier(paramMap);
                String userretFlag = ((Map<String, Object>) (usermap.get("head"))).get("retFlag").toString();
                switch (userretFlag) {
                    case "00000":
                        //注册成功
                        uidLocal = usermap.get("body").toString();//统一认证内userId

                        phoneNo = custPhoneNo;//统一认绑定手机号

//                //验证并绑定集团用户
//                Map<String, Object> bindMap = new HashMap<String, Object>();
//                bindMap.put("externUid", EncryptUtil.simpleEncrypt(uidHaier));
//                bindMap.put("userId", EncryptUtil.simpleEncrypt(uidLocal));
//                bindMap.put("password", EncryptUtil.simpleEncrypt(password));
//                Map usermap = appServerService.validateAndBindHaierUser(token, bindMap);
//                if(!HttpUtil.isSuccess(usermap)){
//                    return fail(ConstUtil.ERROR_CODE, "会员绑定失败");
//                }
                        break;
                    case "U0160":
                        //U0160:该用户已注册，无法注册
                        //跳转登录页面进行登录
                        RedisUtils.setExpire(token, cachemap);
                        String backurl = commonConfig.getGateUrl() + "/sgbt/#!/login/login.html?utm_source=sgbt&utm_medium=sgbt&utm_campaign=sgbt&utm_content=sgbt&utm_term=sgbt&token=" + token;
                        returnmap.put("backurl", backurl);
                        logger.info("页面跳转到：" + backurl);
                        return success(returnmap);
                    default:
                        //注册失败
                        String userretmsg = ((Map<String, Object>) (usermap.get("head"))).get("retMsg").toString();
                        return fail(ConstUtil.ERROR_CODE, userretmsg);
                }
                break;
            case "00000":
                //集团uid已在统一认证做过绑定
                String body = resultMap.get("body").toString();
                //Map<String, Object> bodyMap = HttpUtil.json2Map(body);
                JSONObject bodyMap = new JSONObject(body);
                uidLocal = bodyMap.get("userId").toString();//统一认证内userId

                phoneNo = bodyMap.get("mobile").toString();//统一认绑定手机号


                break;
            default:
                String retMsg = headMap.get("retMsg").toString();
                return fail(ConstUtil.ERROR_CODE, retMsg);
        }

        cachemap.put("userId", uidLocal);//统一认证userId
        cachemap.put("phoneNo", phoneNo);//绑定手机号
        RedisUtils.setExpire(token, cachemap);
        logger.info("进行token绑定");
        //4.token绑定
        Map<String, Object> bindMap = new HashMap<>();
        bindMap.put("userId", uidLocal);//内部userId
        bindMap.put("token", token);
        bindMap.put("channel", "11");
        bindMap.put("channelNo", channelNo);
        Map bindresult = appServerService.saveThirdPartToken(bindMap);
        if (!HttpUtil.isSuccess(bindresult)) {//绑定失败
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        }

        //5.查询实名信息
        Map<String, Object> custMap = new HashMap<>();
        custMap.put("userId", uidLocal);//内部userId
        custMap.put("channel", "11");
        custMap.put("channelNo", channelNo);
        Map custresult = appServerService.queryPerCustInfo(token, custMap);
        String custretflag = ((Map<String, Object>) (custresult.get("head"))).get("retFlag").toString();
        if (!"00000".equals(custretflag) && !"C1220".equals(custretflag)) {//查询实名信息失败
            String custretMsg = ((Map<String, Object>) (custresult.get("head"))).get("retMsg").toString();
            return fail(ConstUtil.ERROR_CODE, custretMsg);
        }
        if ("C1220".equals(custretflag)) {//C1120  客户信息不存在  跳转无额度页面
            logger.info("token:" + token);
            logger.info("跳转额度激活，cachemap：" + cachemap.toString());
            RedisUtils.setExpire(token, cachemap);
            String backurl = commonConfig.getGateUrl() + "/sgbt/#!/applyQuota/amountNot.html?utm_source=sgbt&utm_medium=sgbt&utm_campaign=sgbt&utm_content=sgbt&utm_term=sgbt&token=" + token;
            returnmap.put("backurl", backurl);
            logger.info("页面跳转到：" + backurl);
            return success(returnmap);
        }
        String certType = ((Map<String, Object>) (custresult.get("body"))).get("certType").toString();//证件类型
        String certNo = ((Map<String, Object>) (custresult.get("body"))).get("certNo").toString();//身份证号
        String custNo = ((Map<String, Object>) (custresult.get("body"))).get("custNo").toString();//客户编号
        String custName = ((Map<String, Object>) (custresult.get("body"))).get("custName").toString();//客户名称
        String cardNo = ((Map<String, Object>) (custresult.get("body"))).get("cardNo").toString();//银行卡号
        String bankNo = ((Map<String, Object>) (custresult.get("body"))).get("acctBankNo").toString();//银行代码
        String bankName = ((Map<String, Object>) (custresult.get("body"))).get("acctBankName").toString();//银行名称
        //顺逛传送身份证与客户实名身份证不一致
        if (!StringUtils.isEmpty(idNoHaier)) {
            idNoHaier = idNoHaier.toUpperCase();
        }
        certNo = certNo.toUpperCase();
        logger.info("接收到的身份证：" + idNoHaier + "    实名身份证：" + certNo);
        if (!StringUtils.isEmpty(idNoHaier) && !idNoHaier.equals(certNo)) {
            logger.info("顺逛传送身份证与客户实名身份证不一致");
            return fail(ConstUtil.ERROR_CODE, "顺逛白条实名认证必须和顺逛实名认证一致！");
        } else {
            cachemap.put("custNo", custNo);//客户编号
            cachemap.put("name", custName);//客户姓名
            cachemap.put("cardNo", cardNo);//银行卡号
            cachemap.put("bankCode", bankNo);//银行代码
            cachemap.put("bankName", bankName);//银行名称
            cachemap.put("idNo", certNo);//身份证号
            cachemap.put("idCard", certNo);//身份证号
            cachemap.put("idType", certType);
            RedisUtils.setExpire(token, cachemap);
            String backurl = commonConfig.getGateUrl() + "/sgbt/#!/applyQuota/quotaMerge.html?utm_source=sgbt&utm_medium=sgbt&utm_campaign=sgbt&utm_content=sgbt&utm_term=sgbt&token=" + token;
            returnmap.put("backurl", backurl);
            return success(returnmap);
        }
    }

    //7.白条额度申请状态查询    Sg-10006    checkEdAppl
    public Map<String, Object> checkEdAppl(Map<String, Object> map) {
        logger.info("白条额度申请状态查询接口*******************开始");
        String channelNo = map.get("channelNo").toString();
        String data = (String) map.get("data");//交易信息
        String key = (String) map.get("key");
        String params;
        try {
            params = this.decryptData(data, channelNo, key);
        } catch (Exception e) {
            logger.error(e);
            return fail("01", "请求数据校验失败");
        }
        logger.info("额度申请状态数据解析返回数据：" + params);
        JSONObject json = new JSONObject(params);
        String token = (String) json.get("token");
        //1.根据token获取客户信息
        String userjsonstr = haierDataService.userinfo(token);
        if (userjsonstr == null || "".equals(userjsonstr)) {
            logger.info("验证客户信息接口调用失败");
            return fail(ConstUtil.ERROR_CODE, "验证客户信息失败");
        }
//        {"error_description":"Invalid access token: asadada","error":"invalid_token"}
//        {"user_id":1000030088,"phone_number":"18525369183","phone_number_verified":true,"created_at":1499304958000,"updated_at":1502735413000}
        JSONObject userjson = new JSONObject(userjsonstr);
        if (!userjson.has("user_id")) {
            return fail(ConstUtil.ERROR_CODE, "没有获取到客户信息");
        }
        Object uid = userjson.get("user_id");//会员id
        if (StringUtils.isEmpty(uid)) {
            String error = userjson.get("error").toString();
            return fail(ConstUtil.ERROR_CODE, error);
        }
        String uidHaier = uid.toString();//1000030088
        //String uidHaier = "100003008";
        Map<String, Object> userIdOne = getUserId(uidHaier);//获取用户userId
        JSONObject jsonObject = new JSONObject((Map) userIdOne.get("head"));
        String retMsg1 = (String) jsonObject.get("retMsg");
        if ("01".equals(retMsg1)) {
            logger.info("没有额度申请(根据集团userId查询统一认证userId为空)");
            HashMap<Object, Object> map1 = new HashMap<>();
            map1.put("outSts", "01");
            return success(map1);
        }
        Object body1 = userIdOne.get("body");//用户信息
        Map<String, Object> body11 = (Map) body1;
        String userIdone = (String) body11.get("userId");
        Map<String, Object> cacheedmap = new HashMap<>();
        cacheedmap.put("channel", "11");
        cacheedmap.put("channelNo", channelNo);
        cacheedmap.put("userId", userIdone);
        Map<String, Object> mapcache = appServerService.checkEdAppl(token, cacheedmap);
        logger.info("额度申请校验接口返回数据：" + mapcache);
        if (!HttpUtil.isSuccess(mapcache)) {
            Map<String, Object> head = (Map) mapcache.get("head");
            String retFlag = (String) head.get("retFlag");
            if ("A1183".equals(retFlag)) {//实名认证信息查询失败！未知的实名信息
                logger.info("没有额度申请(实名认证信息查询失败)");
                HashMap<Object, Object> map1 = new HashMap<>();
                map1.put("outSts", "01");
                return success(map1);
            }
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        }
        Object head2 = mapcache.get("head");
        Map<String, Object> retinfo = (Map) head2;
        String retFlag = (String) retinfo.get("retFlag");
        String retMsg = (String) retinfo.get("retMsg");
        if ("00000".equals(retFlag)) {
            Map<String, Object> headinfo = (Map) (mapcache.get("body"));
            String applType = (String) headinfo.get("applType");
            String flag = (String) headinfo.get("flag");
            //applType="2";
            String retmsg = "01";//未申请
            if ("1".equals(applType) || ("".equals(applType) && "Y".equals(flag))) {
                logger.info("没有额度申请");
                HashMap<Object, Object> map1 = new HashMap<>();
                map1.put("outSts", "01");
                return success(map1);
            } else if ("2".equals(applType)) {
                HashMap<String, Object> mapinfo = new HashMap<>();
                mapinfo.put("userId", userIdone);//15275126181
                mapinfo.put("channelNo", channelNo);
                mapinfo.put("channel", "11");
                //String idNo = (String) userjson.get("idNo");//客户证件号码
                Map<String, Object> map1 = appServerService.queryPerCustInfo(token, mapinfo);//3.1.29.(GET)查询客户实名认证信息（根据userid）(APP_person)(CRM17)
                if (StringUtils.isEmpty(map1)) {
                    logger.info("查询客户实名认证信息接口返回数据为空" + map1);
                    return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
                }
                Map<String, Object> headin = (Map) map1.get("head");
                if (!"00000".equals(headin.get("retFlag"))) {
                    logger.info("查询客户实名认证信息接口,返回错误：" + headin.get("retMsg"));
                    //String retmsgo = "当前返回的状态不符合";
                    return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
                }
                Map<String, Object> bodyinfo = (Map) map1.get("body");
                String idNo = (String) bodyinfo.get("certNo");
                logger.info("获取的客户证件号码idNo:" + idNo);
                // String idNo="372926199009295116";
                String idTyp = "20";//证件类型  身份证：20
                if (StringUtils.isEmpty(idNo) || StringUtils.isEmpty(channelNo)) {
                    logger.info("获取的数据为空：idNo=" + idNo + "  ,channelNo" + channelNo);
                    String retMsgin = "获取的数据为空";
                    return fail(ConstUtil.ERROR_CODE, retMsgin);
                }
                HashMap<String, Object> edCheckmap = new HashMap<>();
                edCheckmap.put("idNo", idNo);
                edCheckmap.put("channel", "11");
                edCheckmap.put("channelNo", channelNo);
                edCheckmap.put("idTyp", idTyp);
                Map<String, Object> edApplProgress = appServerService.getEdApplProgress(null, edCheckmap);//(POST)额度申请进度查询（最新的进度 根据idNo查询）
//                String edappl = com.alibaba.fastjson.JSONObject.toJSONString(edApplProgress);
//                com.alibaba.fastjson.JSONObject jsonObjectone = com.alibaba.fastjson.JSONObject.parseObject(edappl);
//                com.alibaba.fastjson.JSONObject body = jsonObjectone.getJSONObject("body");
                Map<String, Object> head = (Map) edApplProgress.get("head");
                if (!"00000".equals(head.get("retFlag"))) {
                    logger.info("额度申请进度查询（最新的进度 根据idNo查询）,错误信息：" + head.get("retMsg"));
                    return fail(ConstUtil.ERROR_CODE, (String) head.get("retMsg"));
                }
                Map<String, Object> body = (Map) edApplProgress.get("body");
                HashMap<Object, Object> mapone = new HashMap<>();
                mapone.put("apprvCrdAmt", body.get("apprvCrdAmt"));//审批总额度
                mapone.put("applyDt", body.get("applyDt"));//申请时间
                mapone.put("operateTime", body.get("operateTime"));//审批时间
                mapone.put("appOutAdvice", body.get("appOutAdvice"));//审批意见
                mapone.put("applSeq", body.get("applSeq"));//申请流水号
                String outSts = body.get("outSts").toString();
                //outSts="01";
                switch (outSts) {
                    case "01":
                    case "22": //APP 审批中  01
                        mapone.put("outSts", "02");//顺逛 审批中  02

                        logger.info("返回顺逛数据：" + mapone);
                        return success(mapone);
                    case "27": // APP 通过
                        mapone.put("outSts", "03");//顺逛  通过

                        logger.info("返回顺逛数据：" + mapone);
                        return success(mapone);
                    case "25": //APP 拒绝
                        mapone.put("outSts", "04");//顺逛 拒绝

                        logger.info("返回顺逛数据：" + mapone);
                        return success(mapone);
                    default:
                        logger.info("APP接口返回的状态是:" + outSts + "    ,与顺逛无法对应");
                        //String retmsgo = "当前返回的状态不符合";
                        return fail(ConstUtil.ERROR_CODE, outSts);
                }
            } else if ("".equals(flag)) {
                Map flagmap = new HashMap<String, Object>();
                flagmap.put("outSts", "03");//顺逛 通过
                logger.info("返回顺逛数据：" + flagmap);
                return success(flagmap);
            } else {
                logger.info("返回的申请类型为空：applType" + applType);
                return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
            }
        } else {
            return fail(ConstUtil.ERROR_CODE, retMsg);
        }
    }

    //9 白条额度进行贷款支付结果主动查询接口
    public Map<String, Object> queryAppLoanAndGoods(Map<String, Object> map) {
        logger.info("白条额度进行贷款支付结果主动查询接口*******************开始");
        String channelNo = (String) map.get("channelNo");
        String tradeCode = (String) map.get("tradeCode");//交易编码
        String data = (String) map.get("data");//交易信息
        String key = (String) map.get("key");

        String params;
        try {
            params = this.decryptData(data, channelNo, key);
        } catch (Exception e) {
            logger.error(e);
            return fail("01", "请求数据校验失败");
        }
        logger.info("支付申请接口请求数据：" + params);
        JSONObject json = new JSONObject(params);
        String token = (String) json.get("token");
        //1.根据token获取客户信息
        String userjsonstr = haierDataService.userinfo(token);
        if (userjsonstr == null || "".equals(userjsonstr)) {
            logger.info("验证客户信息接口调用失败");
            return fail(ConstUtil.ERROR_CODE, "验证客户信息失败");
        }
        JSONObject userjson = new JSONObject(userjsonstr);
        if (!userjson.has("user_id")) {
            return fail(ConstUtil.ERROR_CODE, "没有获取到客户信息");
        }
        Object uid = userjson.get("user_id");//会员id
        String applSeq = (String) json.get("applseq");//支付流水号  必输
        if (StringUtils.isEmpty(uid)) {
            String error = userjson.get("error").toString();
            return fail(ConstUtil.ERROR_CODE, error);
        }
        if (StringUtils.isEmpty(applSeq)) {
            logger.info("获取信息失败,为空:applSeq" + applSeq);
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        Map<String, Object> acqHead = getAcqHead(tradeCode, "11", channelNo, "", "");
        HashMap<String, Object> maphead = new HashMap<>();
        HashMap<String, Object> maprequest = new HashMap<>();
        HashMap<Object, Object> mapappl = new HashMap<>();
        mapappl.put("applSeq", applSeq);
        maphead.put("head", acqHead);
        maphead.put("body", mapappl);
        maprequest.put("request", maphead);
        Map<String, Object> queryApplmap = appServerService.queryApplLoanDetail(maprequest);
        logger.info("查询贷款详情接口，响应数据：" + map);
        if (MapUtils.isEmpty(queryApplmap)) {//response
            logger.info("网络异常,查询贷款详情接口,响应数据为空！" + map);
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        }
        Map<String, Object> response = (Map) queryApplmap.get("response");
        Map<String, Object> head = (Map) response.get("head");
        Map<String, Object> body = (Map) response.get("body");
        String code = (String) head.get("retFlag");
        String message = (String) head.get("retMsg");
        if ("00000".equals(code)) {//查询贷款详情成功
            logger.info("查询贷款详情接口，响应数据：" + body.toString());
            String applSeq1 = (String) body.get("applSeq");//申请流水号
            String contNo = (String) body.get("contNo");//合同号
            String loanNo = (String) body.get("loanNo");//借据号
            Object applyAmto = body.get("applyAmt");//申请金额applyAmt
            String applyAmt = applyAmto.toString();
            Object apprvAmto = body.get("apprvAmt");//核准金额（审批金额）apprvAmt
            String apprvAmt = apprvAmto.toString();
            String repayApplCardNo = (String) body.get("repayApplCardNo");//还款卡号
            String repayAccBankCde = (String) body.get("repayAccBankCde");//还款开户银行代码
            String repayAcProvince = (String) body.get("repayAcProvince");//还款账户所在省
            String repayAcCity = (String) body.get("repayAcCity");//还款账户所在市
            String applyDt = (String) body.get("applyDt");//申请注册日期
            String mtdCde = (String) body.get("mtdCde");//还款方式
            String demo = (String) body.get("demo");//拒绝原因
            HashMap<String, String> mapone = new HashMap<>();
            mapone.put("applSeq", applSeq1);
            mapone.put("contNo", contNo);
            mapone.put("loanNo", loanNo);
            mapone.put("applyAmt", applyAmt);
            mapone.put("apprvAmt", apprvAmt);
            mapone.put("repayApplCardNo", repayApplCardNo);
            mapone.put("repayAccBankCde", repayAccBankCde);
            mapone.put("repayAcProvince", repayAcProvince);
            mapone.put("repayAcCity", repayAcCity);
            mapone.put("applyDt", applyDt);
            mapone.put("mtdCde", mtdCde);
            mapone.put("demo", demo);
            logger.info("查询贷款详情接口返回数据是：" + mapone);
            return success(mapone);
        } else {
            logger.info("查询贷款详情接口，响应数据返回状态码不正确：" + code);
            return fail(ConstUtil.ERROR_CODE, message);
        }
    }

    //10.  白条支付实名认证同步接口    Sg-10009
    public Map<String, Object> queryAppLoanAndGoodsOne(Map<String, Object> map) {
        logger.info("白条支付实名认证同步接口************开始");
        String userId = (String) map.get("userId");//海尔集团用户ID
        if (StringUtils.isEmpty(userId)) {
            logger.info("获取的参数为空：userId" + userId);
            return fail(ConstUtil.ERROR_CODE, ConstUtil.FAILED_INFO);
        }
        Map<String, Object> userIdOne = getUserIdif(userId);//获取用户userId
        Object body1 = userIdOne.get("body");//用户信息
        Map<String, Object> body11 = (Map) body1;
        String userIdone = (String) body11.get("userId");
        // 获取实名认证信息
        Map<String, Object> custInfo = crmService.queryPerCustInfoByUserId(userIdone);
        if (!HttpUtil.isSuccess(custInfo)) {
            return custInfo;
        }
        logger.info("用户" + userId + "实名信息:" + custInfo);
        Map<String, Object> custBody = (Map<String, Object>) custInfo.get("body");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("custName", custBody.get("custName"));//姓名
        jsonObject.put("cardNo", custBody.get("cardNo"));//银行卡号
        jsonObject.put("accBankCde", custBody.get("acctBankNo"));//开户行号  需要确认
        jsonObject.put("accBankName", custBody.get("acctBankName"));//开户行名
        jsonObject.put("certNo", custBody.get("certNo"));//身份证号
        jsonObject.put("phonenumber", custBody.get("mobile"));//手机号码
        return success(jsonObject);
    }

    //11.  白条额度进行主动查询接口    Sg-10010
    public Map<String, Object> edcheck(Map<String, Object> map) {
        logger.info("白条额度进行主动查询接口*********************开始");
        String channelNo = map.get("channelNo").toString();
        String data = (String) map.get("data");//交易信息
        String key = (String) map.get("key");
        String params;
        try {
            params = this.decryptData(data, channelNo, key);
        } catch (Exception e) {
            logger.error(e);
            return fail("01", "请求数据校验失败");
        }
        logger.info("支付申请接口请求数据：" + params);
        JSONObject json = new JSONObject(params);
        String token = (String) json.get("token");
        if (StringUtils.isEmpty(token)) {
            logger.info("获取token失败token:" + token);
            return fail(ConstUtil.ERROR_CODE, ConstUtil.FAILED_INFO);
        }
        //1.根据token获取客户信息
        String userjsonstr = haierDataService.userinfo(token);
        if (userjsonstr == null || "".equals(userjsonstr)) {
            logger.info("验证客户信息接口调用失败");
            return fail(ConstUtil.ERROR_CODE, "验证客户信息失败");
        }
        JSONObject userjson = new JSONObject(userjsonstr);
        if (!userjson.has("user_id")) {
            return fail(ConstUtil.ERROR_CODE, "没有获取到客户信息");
        }
        Object uid = userjson.get("user_id");//会员id
        if (StringUtils.isEmpty(uid)) {
            String error = userjson.get("error").toString();
            return fail(ConstUtil.ERROR_CODE, error);
        }
        String uidHaier = uid.toString();//1000030088
        //String  uidHaier = "1000030088";
        Map<String, Object> userIdOne = getUserIdif(uidHaier);//获取用户userId
        Object body1 = userIdOne.get("body");//用户信息
        Map<String, Object> body11 = (Map) body1;
        String userIdone = (String) body11.get("userId");

        HashMap<String, Object> mapinfo = new HashMap<>();
        mapinfo.put("userId", userIdone);//15275126181
        mapinfo.put("channelNo", channelNo);
        mapinfo.put("channel", "11");
        //String idNo = (String) userjson.get("idNo");//客户证件号码
        Map<String, Object> map1 = appServerService.queryPerCustInfo(token, mapinfo);//3.1.29.(GET)查询客户实名认证信息（根据userid）(APP_person)(CRM17)
        Map<String, Object> headinfo = (Map) map1.get("head");
        String retMsginfo = (String) headinfo.get("retMsg");
        if (StringUtils.isEmpty(map1)) {
            logger.info("查询客户实名认证信息接口返回数据为空" + map1);
            return fail(ConstUtil.ERROR_CODE, retMsginfo);
        }
        Map<String, Object> bodyinfo = (Map) map1.get("body");
        String idNo = (String) bodyinfo.get("certNo");
        logger.info("获取的客户证件号码idNo:" + idNo);
        // String idNo="372926199009295116";
        String idTyp = "20";//证件类型  身份证：20
        if (StringUtils.isEmpty(idNo) || StringUtils.isEmpty(channelNo)) {
            logger.info("获取的数据为空：idNo=" + idNo + "  ,channelNo" + channelNo);
            String retMsg = "获取的数据为空";
            return fail(ConstUtil.ERROR_CODE, retMsg);
        }
        HashMap<String, Object> edCheckmap = new HashMap<>();
        edCheckmap.put("idNo", idNo);
        edCheckmap.put("channel", "11");
        edCheckmap.put("channelNo", channelNo);
        edCheckmap.put("idTyp", idTyp);
        Map<String, Object> edCheck = appServerService.getEdCheck(token, edCheckmap);// 获取额度剩余额度=crdComAvailAnt+crdNorAvailAmt
        if (StringUtils.isEmpty(edCheck)) {
            logger.info("调用接口返回的数据为空");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        }
        JSONObject jsonObject = new JSONObject(edCheck);
//        String limit = com.alibaba.fastjson.JSONObject.toJSONString(edCheck);
//        //JSONObject jsonObject = new JSONObject(limit);
//        com.alibaba.fastjson.JSONObject jsonObject = com.alibaba.fastjson.JSONObject.parseObject(limit);
//        com.alibaba.fastjson.JSONObject head = jsonObject.getJSONObject("head");
        Map<String, Object> head = (Map) edCheck.get("head");
        String retFlag = (String) head.get("retFlag");
        String retMsg = (String) head.get("retMsg");
        if ("00000".equals(retFlag)) {
            //com.alibaba.fastjson.JSONObject limitRes = jsonObject.getJSONObject("body");
            JSONObject limitRes = jsonObject.getJSONObject("body");
            double crdComAvailAnt = limitRes.getDouble("crdComAvailAnt");// 剩余额度（受托支付可用额度金额）
            //double crdNorAvailAmt = limitRes.getDouble("crdNorAvailAmt");// 自主支付可用额度金额(现金)
            //double crdAmt = limitRes.getDouble("crdAmt");// 总额度
            double crdComAmt = limitRes.getDouble("crdComAmt");
            //crdComAvailAntSum = crdComAvailAnt+crdNorAvailAmt;可用额度
            JSONObject jb = new JSONObject();
            jb.put("crdComAvailAnt", crdComAvailAnt);
            //jb.put("crdNorAvailAmt", crdNorAvailAmt);
            //jb.put("crdAmt", crdAmt);
            jb.put("crdComAmt", crdComAmt);
            // jb.put("crdComAvailAntSum", crdComAvailAntSum);
            return success(jb);
        } else {
            return fail(ConstUtil.ERROR_CODE, retMsg);
        }
    }

    private String decryptData(String data, String channelNo, String key) throws Exception {
        //获取渠道公钥
        logger.info("获取渠道" + channelNo + "公钥");
        CooperativeBusiness cooperativeBusiness = cooperativeBusinessDao.selectBycooperationcoed(channelNo);
        if (cooperativeBusiness == null) {
            throw new RuntimeException("渠道" + channelNo + "公钥获取失败");
        }
        String publicKey = cooperativeBusiness.getRsapublic();//获取公钥

        //请求数据解析
//        String ss = new String(RSAUtils.decryptByPublicKey(Base64Utils.decode(key), publicKey));
        //String params = new String(DesUtil.decrypt(Base64Utils.decode(data), new String(RSAUtils.decryptByPublicKey(Base64Utils.decode(key), publicKey))));
        //String params = new String(RSAUtils.decryptByPublicKey(Base64Utils.decode(data), publicKey));

        return new String(DesUtil.decrypt(Base64Utils.decode(data), new String(RSAUtils.decryptByPublicKey(Base64Utils.decode(key), publicKey))));
    }

    public Map<String, Object> getUserId(String userId) {// Sg-10006接口专用   根据集团userId查统一认证userId
        HashMap<String, Object> map = new HashMap<>();
        map.put("externUid", EncryptUtil.simpleEncrypt(userId));
        //map.put("channel", "11");
        //map.put("channelNo", channelNo);
        Map<String, Object> userIdmap = appServerService.getUserId(null, map);
        if (!HttpUtil.isSuccess(userIdmap)) {
            Map<String, Object> head = (Map) userIdmap.get("head");
            String retFlag = (String) head.get("retFlag");
            if ("U0157".equals(retFlag)) {//未查到该集团用户的信息
                String retmsg = "01";//未申请
                return fail(ConstUtil.ERROR_CODE, retmsg);
            }
            logger.info("调集团用户id查询用户信息接口返回信息错误userIdmap" + userIdmap);
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        }
        Object body1 = userIdmap.get("body");
        Map<String, Object> body11 = (Map) body1;
        String userIdone = (String) body11.get("userId");
        HashMap<Object, Object> mapone = new HashMap<>();
        mapone.put("userId", userIdone);
        return success(mapone);
    }

    public Map<String, Object> getUserIdif(String userId) {//根据集团userId查统一认证userId
        HashMap<String, Object> map = new HashMap<>();
        map.put("externUid", EncryptUtil.simpleEncrypt(userId));
        //map.put("channel", "11");
        //map.put("channelNo", channelNo);
        Map<String, Object> userIdmap = appServerService.getUserId(null, map);
        if (!HttpUtil.isSuccess(userIdmap)) {
            logger.info("调集团用户id查询用户信息接口返回信息错误userIdmap" + userIdmap);
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        }
        Object body1 = userIdmap.get("body");
        Map<String, Object> body11 = (Map) body1;
        String userIdone = (String) body11.get("userId");
        HashMap<Object, Object> mapone = new HashMap<>();
        mapone.put("userId", userIdone);
        return success(mapone);
    }

    //额度测试入口
    public Map<String, Object> edApplytest(Map<String, Object> map) {
        String userId = map.get("userId").toString();
        //String phone = map.get("phone").toString();
        String token = map.get("token").toString();
        String channelNo = map.get("channelNo").toString();
        Map cachemap = new HashMap<String, Object>();
        Map returnmap = new HashMap<String, Object>();
        //查询是否已注册

        Map<String, Object> paramMap = new HashMap<>();
        String userIdEncrypt = EncryptUtil.simpleEncrypt(userId);
        paramMap.put("channelNo", channelNo);
        paramMap.put("channel", "11");
        paramMap.put("mobile", userIdEncrypt);
        Map<String, Object> m = appServerService.isRegister(token, paramMap);
        if (!HttpUtil.isSuccess(m)) {
            return m;
        }
        Map bodyMap = (Map<String, Object>) m.get("body");
        String isRegister = bodyMap.get("isRegister").toString();
        if (!"Y".equals(isRegister)) {//未注册
            return fail(ConstUtil.ERROR_CODE, "账号未注册");
        }
        cachemap.put("userId", userId);
        cachemap.put("phoneNo", userId);//绑定手机号
        cachemap.put("userType", "01");////01:微店主  02:消费者
        cachemap.put("edbackurl", "www.baidu.com");


        //4.token绑定
        Map<String, Object> bindMap = new HashMap<>();
        bindMap.put("userId", userId);//内部userId
        bindMap.put("token", token);
        bindMap.put("channel", "11");
        bindMap.put("channelNo", channelNo);
        Map bindresult = appServerService.saveThirdPartToken(bindMap);
        if (!HttpUtil.isSuccess(bindresult)) {//绑定失败
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        }

        //5.查询实名信息
        Map<String, Object> custMap = new HashMap<>();
        custMap.put("userId", userId);//内部userId
        custMap.put("channel", "11");
        custMap.put("channelNo", channelNo);
        Map custresult = appServerService.queryPerCustInfo(token, custMap);
        String custretflag = ((Map<String, Object>) (custresult.get("head"))).get("retFlag").toString();
        if (!"00000".equals(custretflag) && !"C1220".equals(custretflag)) {//查询实名信息失败
            String custretMsg = ((Map<String, Object>) (custresult.get("head"))).get("retMsg").toString();
            return fail(ConstUtil.ERROR_CODE, custretMsg);
        }
        if ("C1220".equals(custretflag)) {//C1120  客户信息不存在  跳转无额度页面
            RedisUtils.setExpire(token, cachemap);

            ///
            Map<String, Object> cacheMap = RedisUtils.getExpireMap(token);
            logger.info("cacheMap:" + cacheMap);


            String backurl = commonConfig.getGateUrl() + "/sgbt/#!/applyQuota/amountNot.html?token=" + token;
            returnmap.put("backurl", backurl);
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
        RedisUtils.setExpire(token, cachemap);

        // 查询有无额度 by lihua
        HashMap<String, Object> edCheckmap = new HashMap<>();
        edCheckmap.put("idNo", certNo);
        edCheckmap.put("channel", "11");
        edCheckmap.put("channelNo", channelNo);
        edCheckmap.put("idTyp", certType);
        Map<String, Object> edCheck = appServerService.getEdCheck(token, edCheckmap);// 获取额度剩余额度=crdComAvailAnt+crdNorAvailAmt
        if (StringUtils.isEmpty(edCheck)) {
            logger.info("调用接口返回的数据为空");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        }

        //6.查询客户额度
        Map<String, Object> edMap = new HashMap<>();
        edMap.put("userId", userId);//内部userId
        edMap.put("channel", "11");
        edMap.put("channelNo", channelNo);
        Map edresult = appServerService.checkEdAppl(token, edMap);
        if (!HttpUtil.isSuccess(edresult)) {//额度校验失败
            String retmsg = ((Map<String, Object>) (edresult.get("head"))).get("retMsg").toString();
            return fail(ConstUtil.ERROR_CODE, retmsg);
        }
        //获取自主支付可用额度金额
        String crdNorAvailAmt = (String) ((Map<String, Object>) (edresult.get("body"))).get("crdNorAvailAmt");
        if (crdNorAvailAmt != null && !"".equals(crdNorAvailAmt)) {
            //跳转有额度页面
            String backurl = commonConfig.getGateUrl() + "/sgbt/#!/payByBt/myAmount.html?token=" + token;
            returnmap.put("backurl", backurl);
            return success(returnmap);
        }
        //审批状态判断
        String outSts = (String) ((Map<String, Object>) (edresult.get("body"))).get("outSts");
        switch (outSts) {
            case "01": {//额度正在审批中
                String backurl = commonConfig.getGateUrl() + "/sgbt/#!/applyQuota/applyIn.html?token=" + token;
                returnmap.put("backurl", backurl);
                return success(returnmap);
            }
            case "22": {//审批被退回
                String crdSeq = (String) ((Map<String, Object>) (edresult.get("body"))).get("crdSeq");
                cachemap.put("crdSeq", crdSeq);
                RedisUtils.setExpire(token, cachemap);
                String backurl = commonConfig.getGateUrl() + "/sgbt/#!/applyQuota/applyReturn.html?token=" + token;
                returnmap.put("backurl", backurl);
                return success(returnmap);
            }
            case "25": {//审批被拒绝
                String backurl = commonConfig.getGateUrl() + "/sgbt/#!/applyQuota/applyFail.html?token=" + token;
                returnmap.put("backurl", backurl);
                return success(returnmap);
            }
            default: {//没有额度  额度激活
                String backurl = commonConfig.getGateUrl() + "/sgbt/#!/applyQuota/amountActive.html?token=" + token;
                returnmap.put("backurl", backurl);
                return success(returnmap);
            }
        }

    }

    //贷款测试入口
    @Override
    public Map<String, Object> payApplytest(AppOrder appOrder) {
        String token = super.getToken();
        if (StringUtils.isEmpty(token)) {
            return fail(ConstUtil.ERROR_CODE, "请在header中传入token");
        }
        Map<String, Object> cachemap = RedisUtils.getExpireMap(token);
        if (MapUtils.isEmpty(cachemap)) {
            cachemap = new HashMap<>();
        }
        cachemap.put("apporder", appOrder);
        cachemap.put("userId", appOrder.getUserId());
        RedisUtils.setExpire(token, cachemap);

        cachemap.put("userType", "01");//01:微店主  02:消费者
        cachemap.put("paybackurl", "www.baidu.com");//支付申请回调url
        cachemap.put("apporder", appOrder);
        RedisUtils.setExpire(token, cachemap);
        Map returnmap = new HashMap<>();
        String backurl = commonConfig.getGateUrl() + "/sgbt/#!/payByBt/btInstalments.html?token=" + token;
        returnmap.put("backurl", backurl);
        return success(returnmap);

        ///return success();
    }

    /**
     * @Title returnGoods
     * @Description: 退货接口
     * @author yu jianwei
     * @date 2017/11/6 17:45
     */
    @Override
    public Map<String, Object> returnGoods(Map<String, Object> map) {
        logger.info("===============退货开始==================");
        String channelNo = String.valueOf(map.get("channelNo"));
        String data = String.valueOf(map.get("data"));//交易信息
        String key = String.valueOf(map.get("key"));
        try {
            String params = decryptData(data, channelNo, key);
            Map<String, Object> paramMap = HttpUtil.json2Map(params);
            Map<String, Object> returnMap = acquirerService.returnGoods(AcqTradeCode.ACQ_RETURNGODDS_TREADECODE, ConstUtil.CHANNEL, channelNo, "", "", paramMap);
            if (returnMap == null) {
                return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
            }
            return (Map<String, Object>) returnMap.get("response");
        } catch (Exception e) {
            logger.error(e);
            return fail("01", "请求数据校验失败");
        }
    }

    /**
     * @Title getReturnGoodsInfo
     * @Description: 查询退货详情
     * @author yu jianwei
     * @date 2017/12/15 11:14
     */
    @Override
    public Map<String, Object> getReturnGoodsInfo(Map<String, Object> map) {
        logger.info("===============查询退货详情开始==================");
        String channelNo = String.valueOf(map.get("channelNo"));
        String data = String.valueOf(map.get("data"));//交易信息
        String key = String.valueOf(map.get("key"));
        try {
            String params = decryptData(data, channelNo, key);
            Map<String, Object> paramMap = HttpUtil.json2Map(params);
            Map<String, Object> returnMap = acquirerService.getReturnGoodsInfo(AcqTradeCode.ACQ_RETURNGODDS_TREADECODE, ConstUtil.CHANNEL, channelNo, "", "", paramMap);
            if (returnMap == null) {
                return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
            }
            return (Map<String, Object>) returnMap.get("response");
        } catch (Exception e) {
            logger.error(e);
            return fail("01", "请求数据校验失败");
        }
    }

    //顺逛退货消息推送
    public Map<String, Object> shunguangth(Map<String, Object> map) {
        logger.info("从收单获取退货通知信息为：" + map);
        HashMap sg = new HashMap<>();
        HashMap sgtwo = new HashMap<>();
        HashMap sgsrs = new HashMap<>();
        String serno;

        String url_ts = shunguangConfig.getTsUrl();
        logger.info("Sg-10011退货通知推送地址：" + url_ts);

        Map m = (Map) map.get("request");
        Map headMap = (Map) m.get("head");
        Map bodyMap = (Map) m.get("body");
        //获取头部信息
        String channelNo = Convert.toString(headMap.get("channelNo"));
        serno = Convert.toString(headMap.get("serno"));
        //
        String msgType = Convert.toString(bodyMap.get("msgType"));//推送类型
        String applSeq = Convert.toString(bodyMap.get("applSeq"));//申请流水号
        String loanNo = Convert.toString(bodyMap.get("loanNo"));//借据号
        String businessId = Convert.toString(bodyMap.get("businessId"));//业务流水号
        String mallOrderNo = Convert.toString(bodyMap.get("mallOrderNo"));//商城订单号
        String custName = Convert.toString(bodyMap.get("custName"));//客户姓名
        String businessType = Convert.toString(bodyMap.get("businessType"));//业务类型
        String idNo = Convert.toString(bodyMap.get("idNo"));//身份证号
        String content = Convert.toString(bodyMap.get("content"));//提示描述
        String status = Convert.toString(bodyMap.get("status"));//状态
        SgReturngoodsLog sgReturngoodsLog = shunGuangthLogDao.getByMallOrderNo(mallOrderNo);

        SgReturngoodsLog ts = new SgReturngoodsLog();
        SimpleDateFormat tm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        String time = tm.format(date);
        ts.setLogId(UUID.randomUUID().toString().replace("-", ""));
        ts.setMsgTyp(msgType);
        ts.setApplSeq(applSeq);
        ts.setMallOrderNo(mallOrderNo);
        ts.setLoanNo(loanNo);
        ts.setIdNo(idNo);
        ts.setCustName(custName);
        ts.setBusinessId(businessId);
        ts.setBusinessType(businessType);
        ts.setStatus(status);
        ts.setChannelNo(channelNo);
        ts.setContent(content);
        ts.setTime(time);
        ts.setChannelNo(channelNo);
        try {
            HashMap<String, Object> sgts = new HashMap<>();
            sgts.put("msgTyp", msgType);//
            sgts.put("applSeq", applSeq);//
            sgts.put("mallOrderNo", mallOrderNo);
            sgts.put("loanNo", loanNo);
            sgts.put("idNo", idNo);
            sgts.put("custName", custName);
            sgts.put("businessId", businessId);
            sgts.put("businessType", businessType);
            sgts.put("status", status);
            sgts.put("content", content);
            String sgString = com.alibaba.fastjson.JSONObject.toJSONString(sgts);

            HashMap<String, Object> encrypt = encrypt(sgString, channelNo, "Sg-10011");//数据的加密数据  /json/ious/refundNotify.json
            String result = JsonClientUtils.postForString(url_ts + "/paycenter/json/ious/refundNotify.json", encrypt);
            Map resultMap = HttpUtil.json2DeepMap(result);
            Map headmap = (Map) resultMap.get("head");
            String retMsg = Convert.toString(headmap.get("retMsg"));
            String retFlag = Convert.toString(headmap.get("retFlag"));
            logger.info("实时推送流水号：" + applSeq + "   响应数据：" + retMsg);

            if ("00000".equals(retFlag)) {
                ts.setFlag("Y");//推送成功
                sg.put("retFlag", "00000");
                sg.put("retMsg", "处理成功");
                sg.put("serno", serno);
            } else {
                ts.setFlag("N");//推送失败
                sg.put("retFlag", "00099");
                sg.put("retMsg", retMsg);
                sg.put("serno", serno);
            }

            //根据商城订单号查询退货推送信息
            if (sgReturngoodsLog == null) {
                //首次推送
                ts.setTimes("1");
                shunGuangthLogDao.insert(ts);
            } else {
                int n = Convert.toInteger(sgReturngoodsLog.getTimes());
                n = n + 1;
                ts.setTimes(Convert.toString(n));
                shunGuangthLogDao.updateByMallOrderNo(ts);
            }

        } catch (Exception e) {
            if (sgReturngoodsLog != null) {
                int n = Convert.toInteger(sgReturngoodsLog.getTimes());
                if (n == 2) {//第3次推送失败  则结束推送
                    ts.setFlag("N");//推送失败
                    n = n + 1;
                    ts.setTimes(Convert.toString(n));
                    shunGuangthLogDao.updateByMallOrderNo(ts);
                    sg.put("retFlag", "00000");
                    sg.put("retMsg", "处理成功");
                    sg.put("serno", serno);
                    logger.error("退货实时推送接口(JSON格式)， 出现异常 :" + e.getMessage(), e);
                    sgtwo.put("head", sg);
                    sgtwo.put("body", "");
                    sgsrs.put("response", sgtwo);
                    return sgsrs;
                }
            }

            String retMsg = e.getMessage();
            sg.put("retFlag", "00099");
            sg.put("retMsg", retMsg);
            sg.put("serno", serno);
            logger.error("退货实时推送接口(JSON格式)， 出现异常 :" + retMsg, e);
        }

        sgtwo.put("head", sg);
        sgtwo.put("body", "");
        sgsrs.put("response", sgtwo);
        return sgsrs;
    }

    /**
     * @Title pushMessage
     * @Description: 手动推送消息
     * @author yu jianwei
     * @date 2017/12/25 18:23
     */
    @Override
    public Map<String, Object> pushMessage() {
        logger.info("=========将推送失败的收单获取退货通知从信息数据库推送至顺逛开始=============");
        try {
            String url_ts = shunguangConfig.getTsUrl();
            String channelNo = super.getChannelNo();
            logger.info("获取信息的渠道为==>" + channelNo);
            List<SgReturngoodsLog> dataList = shunGuangthLogDao.selectDataByFlag("N", channelNo);
            if (dataList.size() == 0) {
                return fail("00002", "暂无发送失败的数据");
            }
            dataList.forEach((SgReturngoodsLog data) -> {
                String dataStr = com.alibaba.fastjson.JSONObject.toJSONString(data);
                com.alibaba.fastjson.JSONObject dataJs = com.alibaba.fastjson.JSONObject.parseObject(dataStr);
                dataJs.remove("logId");
                String sgString = com.alibaba.fastjson.JSONObject.toJSONString(dataJs);
                String tradeCode = "Sg-10011";
                HashMap<String, Object> encrypt = encrypt(sgString, channelNo, tradeCode);//数据的加密数据  /json/ious/refundNotify.json
                logger.info("推送的报文是：" + encrypt);
                String result = JsonClientUtils.postForString(url_ts + "/paycenter/json/ious/refundNotify.json", encrypt);
                String resultjson = result.substring(result.indexOf("{"), result.lastIndexOf("}") + 1);
                logger.info("实时推送接口(JSON格式)，第三方返回的结果数据：" + resultjson);
                com.alibaba.fastjson.JSONObject jsonsObj = com.alibaba.fastjson.JSONObject.parseObject(resultjson);
                com.alibaba.fastjson.JSONObject headone = jsonsObj.getJSONObject("head");
                String retflag = headone.getString("retFlag");
                Date date = new Date();
                String locId = data.getLogId();
                logger.info("数据库信息Id==>" + locId);
                if ("00000".equals(retflag)) {
                    logger.info("数据推送成功");
                    shunGuangthLogDao.updateFlagById(FormatUtil.formatDate(date), "Y", locId);
                } else {
                    logger.info("数据推送失败");
                    shunGuangthLogDao.updateTimesById(FormatUtil.formatDate(date), locId);
                }
            });
            return success();
        } catch (Exception e) {
            String retMsg = e.getMessage();
            logger.error("退货数据库信息推送接口(JSON格式)， 出现异常 :" + retMsg, e);
            return fail("00001", "系统内部异常");
        }
    }

    private HashMap<String, Object> encrypt(String data, String channelNo, String tradeCode) {
        logger.info("获取渠道" + channelNo + "公钥");
        HashMap<String, Object> map = new HashMap<>();
        try {
            CooperativeBusiness cooperativeBusiness = cooperativeBusinessDao.selectBycooperationcoed(channelNo);
            if (cooperativeBusiness == null) {
                throw new RuntimeException("渠道" + channelNo + "公钥获取失败");
            }
            String publicKey = cooperativeBusiness.getRsapublic();//获取公钥
            String password = DesUtil.productKey();
            //2.des加密
            String desData = Base64Utils.encode(DesUtil.encrypt(data.getBytes(), password));
            //3.加密des的key
            String password_ = Base64Utils.encode(RSAUtils.encryptByPublicKey(password.getBytes(), publicKey));
            map.put("applyNo", UUID.randomUUID().toString().replace("-", ""));
            map.put("channelNo", "46");
            map.put("tradeCode", tradeCode);
            map.put("data", desData);
            map.put("key", password_);
        } catch (Exception e) {
            logger.error("加解密出现异常" + e.getMessage());
        }
        return map;
    }
}
