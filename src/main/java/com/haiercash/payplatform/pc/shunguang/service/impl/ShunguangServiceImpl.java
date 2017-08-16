package com.haiercash.payplatform.pc.shunguang.service.impl;

import com.haiercash.commons.redis.Cache;
import com.haiercash.commons.util.StringUtil;
import com.haiercash.payplatform.common.config.EurekaServer;
import com.haiercash.payplatform.common.dao.CooperativeBusinessDao;
import com.haiercash.payplatform.common.data.AppOrder;
import com.haiercash.payplatform.common.data.CooperativeBusiness;
import com.haiercash.payplatform.common.service.CrmService;
import com.haiercash.payplatform.common.service.AppServerService;
import com.haiercash.payplatform.common.service.HaierDataService;
import com.haiercash.payplatform.common.utils.*;
import com.haiercash.payplatform.pc.shunguang.service.ShunguangService;
import com.haiercash.payplatform.service.BaseService;
import com.netflix.ribbon.proxy.annotation.Http;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * shunguang service impl.
 *
 * @author yuan li
 * @since v1.0.1
 */
@Service
public class ShunguangServiceImpl extends BaseService implements ShunguangService {
    public Log logger = LogFactory.getLog(getClass());
    @Autowired
    private Cache cache;
    @Autowired
    private AppServerService appServerService;
    @Autowired
    private CooperativeBusinessDao cooperativeBusinessDao;
    @Autowired
    private HaierDataService haierDataService;
    @Autowired
    private CrmService crmService;
    @Value("${app.other.haiercashpay_web_url}")
    protected String haiercashpay_web_url;

    @Override
    public Map<String, Object> saveStoreInfo(Map<String, Object> storeInfo) {

        Map<String, Object> result = this.savePeopleInfo(storeInfo);
        if (!HttpUtil.isSuccess(result)) {
            logger.error("保存微店主客户信息失败,处理结果:" + result);
        }
        return result;
    }

    @Override
    public Map<String, Object> saveOrdinaryUserInfo(Map<String, Object> ordinaryInfo) {
        Map<String, Object> result = this.savePeopleInfo(ordinaryInfo);
        if (!HttpUtil.isSuccess(result)) {
            logger.error("保存普通用户信息失败,处理结果:" + result);
        }
        return result;
    }

    /**
     * 通用保存用户信息方法.
     * @param info 用户信息
     * @return 处理结果Map
     */
    private Map<String, Object> savePeopleInfo(Map<String, Object> info) {
            String userId = (String) info.get("userId");
        String data = (String) info.get("data");

        String params;
        try {
            params = this.decryptData(data, info.get("channelNo").toString());
        } catch (Exception e) {
            logger.error(e);
            return fail("01", "请求数据校验失败");
        }

        // 获取实名认证信息
        Map<String, Object> custInfo = crmService.queryPerCustInfoByUserId(userId);
        if (!HttpUtil.isSuccess(custInfo)) {
            return custInfo;
        }
        logger.info("用户" + userId + "实名信息:" + custInfo);

        Map<String, Object> custBody = (Map<String, Object>) custInfo.get("body");

        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("channelno", info.get("channelNo"));
        requestParams.put("applseq", null);
        requestParams.put("cardnumber", custBody.get("certNo"));
        requestParams.put("data", new JSONObject(params));
        Map<String, Object> result = HttpUtil
                .restPostMap(this.outplatUrl + "/Outreachplatform/api/externalData/savaExternalData", requestParams);

        if (!"0000".equals(result.get("code"))) {
            return fail("02", (String) result.get("message"));
        }
        return success();
    }

    @Override
    public Map<String, Object> payApply(Map<String, Object> map) throws Exception {
        logger.info("白条支付申请接口*******************开始");
        AppOrder appOrder = new AppOrder();
        Map cachemap = new HashMap<String, Object>();
        String applyNo = (String) map.get("applyNo");//交易流水号
        String channelNo = map.get("channelNo").toString();
        String tradeCode = (String) map.get("tradeCode");//交易编码
        String data = (String) map.get("data");//交易信息

        String params;
        try {
            params = this.decryptData(data, channelNo);
        } catch (Exception e) {
            logger.error(e);
            return fail("01", "请求数据校验失败");
        }
        logger.info("支付申请接口请求数据：" + params);
        JSONObject json = new JSONObject(params);
        String token = (String) json.get("token");
        String userType = (String) json.get("userType");
        String body = json.get("body").toString();//本比订单信息
        //String URL = (String) json.get("URL");
        JSONObject bodyjson = new JSONObject(body);
        String orderSn = bodyjson.getString("orderSn");//订单号
        String loanType = bodyjson.getString("loanType");//贷款品种编码
        String payAmt = bodyjson.getString("payAmt");//订单实付金额
        String province = bodyjson.getString("province");//省
        String city = bodyjson.getString("city");//市
        String country = bodyjson.getString("country");//区
        String detailAddress = bodyjson.getString("detailAddress");//详细地址
        String orderDate = bodyjson.getString("orderDate");//下单时间
        String ordermessage = bodyjson.get("ordermessage").toString();//网单信息
        JSONArray jsonArray = new JSONArray(ordermessage);
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
        }

        //TODO!!!!
        //appOrder.http://testpm.haiercash.com/sgbt/#!/applyQuota/checkIdCard.html?token=f294c5ad-1b63-4340-8ddb-7de9d0366ed7
        cachemap.put("apporder", appOrder);
        cache.set(token, cachemap);
        Map returnmap = new HashMap<>();
        String backurl = haiercashpay_web_url + "sgbt/#!/payByBt/btInstalments.html?token="+token;//TODO!!!!!!
        returnmap.put("backurl", backurl);
        return success(returnmap);
    }

    @Override
    public Map<String, Object> edApply(Map<String, Object> map) throws Exception {
        logger.info("白条额度申请接口*******************开始");
        Map cachemap = new HashMap<String, Object>();
        String applyNo = (String) map.get("applyNo");//交易流水号
        String channelNo = map.get("channelNo").toString();
        String tradeCode = (String) map.get("tradeCode");//交易编码
        String data = (String) map.get("data");//交易信息

        String params;
        try {
            params = this.decryptData(data, channelNo);
        } catch (Exception e) {
            logger.error(e);
            return fail("01", "请求数据校验失败");
        }
        logger.info("额度申请接口请求数据：" + params);
        JSONObject json = new JSONObject(params);
        String token = (String) json.get("token");
        String userType = (String) json.get("userType");
        String custmessage = json.get("custmessage").toString();
        JSONObject custjson = new JSONObject(custmessage);
        String name = (String) custjson.get("name");
        String idNo = (String) custjson.get("idNo");
        String mobile = (String) custjson.get("mobile");
        String bankCode = (String) custjson.get("bankCode");

        //1.根据token获取客户信息
        JSONObject userjson = haierDataService.userinfo(token);
        if (userjson == null || "".equals(userjson)) {
            logger.info("验证客户信息接口调用失败");
            return fail(ConstUtil.ERROR_CODE, "验证客户信息失败");
        }
        //{"error_description":"Invalid access token: asadada","error":"invalid_token"}
        Object uid = userjson.get("user_id");//会员id
        if(StringUtils.isEmpty(uid)){
            String error = userjson.get("error").toString();
            return fail(ConstUtil.ERROR_CODE, error);
        }
        String uidHaier = uid.toString();
        String custPhoneNo = (String) userjson.get("phone_number");
        String userName = (String) userjson.get("username");
        cachemap.put("token", token);
        cachemap.put("uidHaier", uidHaier);//会员id

        //2.查看是否绑定手机号
        if(custPhoneNo.isEmpty()){
            return fail(ConstUtil.ERROR_CODE, "客户未进行手机号绑定");
        }

        //3.查看外部uid绑定   /app/appserver/uauth/queryHaierUserInfo?externUid
        //未绑定   去注册    1.注册成功（OK）   2.嗨付已注册未与集团绑定   （走登录）
        //已绑定   去统一认证查询内部userid
        String uidLocal = "";//统一认证userid
        String phoneNo = "";//统一认证绑定手机号
        Map returnmap = new HashMap<String, Object>();//返回的map
        if(uidHaier.isEmpty()){
            return fail(ConstUtil.ERROR_CODE, "未获取到userId");
        }
        String userInforesult = appServerService.queryHaierUserInfo(EncryptUtil.simpleEncrypt(uidHaier));
        if (!HttpUtil.isSuccess(userInforesult) ) {
            return fail(ConstUtil.ERROR_CODE, "根据集团用户ID查询用户信息失败");
        }
        Map<String, Object> resultMap = HttpUtil.json2Map(userInforesult);
        String retFlag = ((HashMap<String, Object>)(resultMap.get("head"))).get("retFlag").toString();
        if("U0157".equals(retFlag)){//U0157：未查到该集团用户的信息
            //用户未注册   进行注册
            Map<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put("mobile", EncryptUtil.simpleEncrypt(custPhoneNo)); //海尔集团登录用户名
            paramMap.put("externUid", EncryptUtil.simpleEncrypt(uidHaier)); //海尔集团userId
            //paramMap.put("userName", EncryptUtil.simpleEncrypt(userName == null ? "" : userName));
            Map usermap = appServerService.saveUauthUsersByHaier(paramMap);
            String userretFlag = ((HashMap<String, Object>)(usermap.get("head"))).get("retFlag").toString();
            if("00000".equals(userretFlag)){
                 //注册成功
                uidLocal = ((HashMap<String, Object>)(usermap.get("body"))).get("userId").toString();//统一认证内userId
                phoneNo = ((HashMap<String, Object>)(usermap.get("body"))).get("mobile").toString();//统一认绑定手机号
            }else if("U0160".equals(userretFlag)){
                //U0160:该用户已注册，无法注册
                //跳转登录页面进行登录
                cache.set(token, cachemap);
                String backurl = haiercashpay_web_url + "sgbt/#!/login/login.html?token="+token;
                returnmap.put("backurl", backurl);
                return success(returnmap);
            }else{
                //注册失败
                String userretmsg = ((HashMap<String, Object>)(usermap.get("head"))).get("retMsg").toString();
                return fail(ConstUtil.ERROR_CODE, userretmsg);
            }
        }
        //集团uid已在统一认证做过绑定
        uidLocal = ((HashMap<String, Object>)(resultMap.get("body"))).get("userId").toString();//统一认证内userId
        phoneNo = ((HashMap<String, Object>)(resultMap.get("body"))).get("mobile").toString();//统一认绑定手机号
        cachemap.put("userId", uidLocal);//统一认证userId
        cachemap.put("phoneNo", phoneNo);//绑定手机号
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
        String custretflag = ((HashMap<String, Object>)(custresult.get("head"))).get("retFlag").toString();
        if(!"00000".equals(custretflag) && !"C1120".equals(custretflag)){//查询实名信息失败
            String custretMsg = ((HashMap<String, Object>)(custresult.get("head"))).get("retMsg").toString();
            return fail(ConstUtil.ERROR_CODE, custretflag);
        }
        if("C1120".equals(custretflag)){//C1120  客户信息不存在  跳转无额度页面
            cache.set(token, cachemap);
            String backurl = haiercashpay_web_url + "sgbt/#!/applyQuota/amountNot.html?token="+token;
            returnmap.put("backurl", backurl);
            return success(returnmap);
        }
        String certType = ((HashMap<String, Object>)(custresult.get("body"))).get("certType").toString();//证件类型
        String certNo = ((HashMap<String, Object>)(custresult.get("body"))).get("certNo").toString();//身份证号
        String custNo = ((HashMap<String, Object>)(custresult.get("body"))).get("custNo").toString();//客户编号
        String custName = ((HashMap<String, Object>)(custresult.get("body"))).get("custName").toString();//客户名称
        String cardNo = ((HashMap<String, Object>)(custresult.get("body"))).get("cardNo").toString();//银行卡号
        String bankNo = ((HashMap<String, Object>)(custresult.get("body"))).get("acctBankNo").toString();//银行代码
        String bankName = ((HashMap<String, Object>)(custresult.get("body"))).get("acctBankName").toString();//银行名称

        cachemap.put("custNo", custNo);//客户编号
        cachemap.put("custName", custName);//客户姓名
        cachemap.put("cardNo", cardNo);//银行卡号
        cachemap.put("bankCode", bankNo);//银行代码
        cachemap.put("bankName", bankName);//银行名称
        cachemap.put("idNo", certNo);//身份证号
        cachemap.put("idType", certType);
        cache.set(token, cachemap);
        //6.查询客户额度
        Map<String, Object> edMap = new HashMap<String, Object>();
        edMap.put("userId", uidLocal);//内部userId
        edMap.put("channelNo", channelNo);
        Map edresult = appServerService.checkEdAppl(token, edMap);
        if (!HttpUtil.isSuccess(edresult) ) {//额度校验失败
            String retmsg = ((HashMap<String, Object>)(custresult.get("head"))).get("retMsg").toString();
            return fail(ConstUtil.ERROR_CODE, retmsg);
        }
        //获取自主支付可用额度金额
        String crdNorAvailAmt = (String)((HashMap<String, Object>)(custresult.get("body"))).get("crdNorAvailAmt");
        if (crdNorAvailAmt != null && !"".equals(crdNorAvailAmt) ){
            //跳转有额度页面
            String backurl = haiercashpay_web_url + "sgbt/#!/payByBt/myAmount.html?token="+token;
            returnmap.put("backurl", backurl);
            return success(returnmap);
        }
        //审批状态判断
        String outSts = (String)((HashMap<String, Object>)(custresult.get("body"))).get("outSts");
        if("01".equals(outSts)) {//额度正在审批中
            String backurl = haiercashpay_web_url + "sgbt/#!/applyQuota/applyIn.html?token="+token;
            returnmap.put("backurl", backurl);
            return success(returnmap);
        }else if("22".equals(outSts)) {//审批被退回
            String backurl = haiercashpay_web_url + "sgbt/#!/applyQuota/applyReturn.html?token="+token;
            returnmap.put("backurl", backurl);
            return success(returnmap);
        }else if("25".equals(outSts)) {//审批被拒绝
            String backurl = haiercashpay_web_url + "sgbt/#!/applyQuota/applyFail.html?token="+token;
            returnmap.put("backurl", backurl);
            return success(returnmap);
        }else {//没有额度  额度激活
            String backurl = haiercashpay_web_url + "sgbt/#!/applyQuota/amountActive.html?token="+token;
            returnmap.put("backurl", backurl);
            return success(returnmap);
        }

//        Map m = new HashMap<>();
//        String urlback = "https://www.baidu.com/";//TODO!!!!!!
//        m.put("backurl", urlback);
//        return success(m);
    }


    private String decryptData(String data, String channelNo) throws Exception {
        //获取渠道公钥
        logger.info("获取渠道" + channelNo + "公钥");
        CooperativeBusiness cooperativeBusiness = cooperativeBusinessDao.selectBycooperationcoed(channelNo);
        if (cooperativeBusiness == null) {
            throw new RuntimeException("渠道" + channelNo + "公钥获取失败");
        }
        String publicKey = cooperativeBusiness.getRsapublic();//获取公钥

        //请求数据解析
        String params = new String(RSAUtils.decryptByPublicKey(Base64Utils.decode(data), publicKey));
        return params;
    }
}
