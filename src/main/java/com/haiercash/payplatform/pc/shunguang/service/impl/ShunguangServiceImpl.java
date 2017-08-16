package com.haiercash.payplatform.pc.shunguang.service.impl;

import com.haiercash.commons.redis.Cache;
import com.haiercash.commons.util.StringUtil;
import com.haiercash.payplatform.common.config.EurekaServer;
import com.haiercash.payplatform.common.dao.CooperativeBusinessDao;
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
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
            params = this.decryptData(data, super.getChannelNo());
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
            Object object = jsonArray.get(j);
            Map verifyheadjson = (HashMap<String, Object>) object;
            String cOrderSn = (String) verifyheadjson.get("cOrderSn");
            String topLevel = (String) verifyheadjson.get("topLevel");
            String model = (String) verifyheadjson.get("model");
        }

        logger.info("测试");

        return success();
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
        String retFlag = (new JSONObject(resultMap.get("head"))).getString("retFlag").toString();
        if("U0157".equals(retFlag)){//U0157：未查到该集团用户的信息
            //用户未注册   进行注册
            Map<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put("mobile", EncryptUtil.simpleEncrypt(custPhoneNo)); //海尔集团登录用户名
            paramMap.put("externUid", EncryptUtil.simpleEncrypt(uidHaier)); //海尔集团userId
            //paramMap.put("userName", EncryptUtil.simpleEncrypt(userName == null ? "" : userName));
            Map usermap = appServerService.saveUauthUsersByHaier(paramMap);
            String userretFlag = (new JSONObject(usermap.get("head"))).getString("retFlag").toString();
            if("00000".equals(userretFlag)){
                 //注册成功
                uidLocal = (new JSONObject(usermap.get("body"))).getString("userId").toString();//统一认证内userId
                phoneNo = (new JSONObject(usermap.get("body"))).getString("mobile").toString();//统一认绑定手机号
            }else if("U0160".equals(userretFlag)){
                //U0160:该用户已注册，无法注册
                //跳转登录页面进行登录
                cache.set(token, cachemap);
                String backurl = "login.html?token="+token;//TODO!!!!!!
                map.put("backurl", backurl);
                return success(map);
            }else{
                //注册失败
                String userretmsg = (new JSONObject(usermap.get("head"))).getString("retMsg").toString();
                return fail(ConstUtil.ERROR_CODE, userretmsg);
            }
        }
        //集团uid已在统一认证做过绑定
        uidLocal = (new JSONObject(resultMap.get("body"))).getString("userId").toString();//统一认证内userId
        phoneNo = (new JSONObject(resultMap.get("body"))).getString("mobile").toString();//统一认绑定手机号
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
        String custretflag = (new JSONObject(custresult.get("head"))).getString("retFlag").toString();
        if(!"00000".equals(custretflag) && !"C1120".equals(custretflag)){//查询实名信息失败
            String custretMsg = (new JSONObject(custresult.get("head"))).getString("retMsg").toString();
            return fail(ConstUtil.ERROR_CODE, custretflag);
        }
        if("C1120".equals(custretflag)){//C1120  客户信息不存在  跳转无额度页面
            cache.set(token, cachemap);
            String backurl = "login.html?token="+token;//TODO!!!!!!
            map.put("backurl", backurl);
            return success(map);
        }
        String certType = (new JSONObject(custresult.get("body"))).getString("certType").toString();//证件类型
        String certNo = (new JSONObject(custresult.get("body"))).getString("certNo").toString();//身份证号
        String custNo = (new JSONObject(custresult.get("body"))).getString("custNo").toString();//客户编号
        String custName = (new JSONObject(custresult.get("body"))).getString("custName").toString();//客户名称
        String cardNo = (new JSONObject(custresult.get("body"))).getString("cardNo").toString();//银行卡号
        String bankNo = (new JSONObject(custresult.get("body"))).getString("acctBankNo").toString();//银行代码
        String bankName = (new JSONObject(custresult.get("body"))).getString("acctBankName").toString();//银行名称

        cachemap.put("custNo", custNo);//客户编号
        cachemap.put("custName", custName);//客户姓名
        cachemap.put("cardNo", cardNo);//银行卡号
        cachemap.put("bankCode", bankNo);//银行代码
        cachemap.put("bankName", bankName);//银行名称
        cachemap.put("idNo", certNo);//身份证号
        cachemap.put("idType", certType);

        //6.查询客户额度
        Map<String, Object> edMap = new HashMap<String, Object>();
        edMap.put("userId", uidLocal);//内部userId
        edMap.put("channelNo", channelNo);
        Map edresult = appServerService.checkEdAppl(token, edMap);
        if (!HttpUtil.isSuccess(edresult) ) {//额度校验失败
            String retmsg = (new JSONObject(custresult.get("head"))).getString("retMsg").toString();
            return fail(ConstUtil.ERROR_CODE, retmsg);
        }
        //获取自主支付可用额度金额
        String crdNorAvailAmt = (String)(new JSONObject(custresult.get("body"))).getString("crdNorAvailAmt");
        if (crdNorAvailAmt != null && !"".equals(crdNorAvailAmt) ){
            //跳转有额度页面
            String backurl = "login.html?token="+token;//TODO!!!!!!
            map.put("backurl", backurl);
            return success(map);
        }
        //审批状态判断
        String outSts = (String)(new JSONObject(custresult.get("body"))).getString("outSts");
        if("01".equals(outSts)) {//额度正在审批中
            String backurl = "login.html?token="+token;//TODO!!!!!!
            map.put("backurl", backurl);
            return success(map);
        }else if("22".equals(outSts)) {//审批被退回
            String backurl = "login.html?token="+token;//TODO!!!!!!
            map.put("backurl", backurl);
            return success(map);
        }else if("25".equals(outSts)) {//审批被拒绝
            String backurl = "login.html?token="+token;//TODO!!!!!!
            map.put("backurl", backurl);
            return success(map);
        }else {//没有额度  额度激活
            String backurl = "login.html?token="+token;//TODO!!!!!!
            map.put("backurl", backurl);
            return success(map);
        }

//        Map m = new HashMap<>();
//        String urlback = "https://www.baidu.com/";//TODO!!!!!!
//        m.put("backurl", urlback);
//        return success(m);
    }

    //7.白条额度申请状态查询    Sg-10006    checkEdAppl
    public Map<String, Object> checkEdAppl(Map<String, Object> map) {
        logger.info("白条额度申请状态查询接口*******************开始");
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
        logger.info("额度申请状态数据解析返回数据：" + params);
        JSONObject json = new JSONObject(params);
        String token = (String) json.get("token");
        Map<String, Object> cacheEdmap = cache.get(token);
        String userId = (String) cacheEdmap.get("userId");
        Map<String, Object> cacheedmap = new HashMap<>();
        cacheedmap.put("channel", "11");
        cacheedmap.put("channelNo", channelNo);
        cacheedmap.put("userId", userId);
        Map<String, Object> mapcache = appServerService.checkEdAppl(token, cacheedmap);
        logger.info("额度申请校验接口返回数据：" + mapcache);
        if (!HttpUtil.isSuccess(mapcache)) {
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        }
        JSONObject head = new JSONObject(mapcache.get("head"));
        String retFlag = head.getString("retFlag");
        String retMsg = head.getString("retMsg");
        if ("00000".equals(retFlag)) {
            JSONObject body = new JSONObject(mapcache.get("body"));
            String applType = body.getString("applType");
            String retmsg = "01";//未申请
            if ("1".equals(applType)) {
                logger.info("没有额度申请");
                return fail(ConstUtil.ERROR_CODE, retmsg);
            } else if ("2".equals(applType)) {
                HashMap<Object, Object> mapone = new HashMap<>();
                mapone.put("apprvCrdAmt", body.getString("apprvCrdAmt"));//审批总额度
                mapone.put("applyDt", body.getString("applyDt"));//申请时间
                mapone.put("operateTime", body.getString("operateTime"));//审批时间
                mapone.put("appOutAdvice", body.getString("appOutAdvice"));//审批意见
                mapone.put("applSeq", body.getString("applSeq"));//申请流水号
                String outSts = body.getString("outSts");
                if ("01".equals(outSts)) {//APP 审批中  01
                    mapone.put("outSts", "02");//顺逛 审批中  02
                    logger.info("返回顺狂数据：" + mapone);
                    return success(mapone);
                } else if ("27".equals(outSts)) {// APP 通过
                    mapone.put("outSts", "03");//顺狂  通过
                    logger.info("返回顺狂数据：" + mapone);
                    return success(mapone);
                } else if ("25".equals(outSts)) {//APP 拒绝
                    mapone.put("outSts", "04");//顺逛 拒绝
                    logger.info("返回顺狂数据：" + mapone);
                    return success(mapone);
                } else {
                    logger.info("APP返回的状态与顺逛无法对应");
                    String retmsgo = "当前返回的状态不符合";
                    return fail(ConstUtil.ERROR_CODE, retmsgo);
                }
            } else {
                logger.info("返回的申请类型为空：applType" + applType);
                return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
            }
        } else {
            return fail(ConstUtil.ERROR_CODE, retMsg);
        }
    }


    //9 白条额度进行贷款支付结果主动查询接口
    public Map<String, Object> queryAppLoanAndGoods(Map<String, Object> map) throws Exception {
        logger.info("白条额度进行贷款支付结果主动查询接口*******************开始");
        Map cachemap = new HashMap<String, Object>();
        String applyNo = (String) map.get("applyNo");//交易流水号
        String channelNo = (String) map.get("channelNo");
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
        String orderNo = (String) json.get("orderNo");//订单号 非必输
        String applSeq = (String) json.get("applseq");//支付流水号  必输
        if (StringUtils.isEmpty(applSeq)) {
            logger.info("获取信息失败,为空:applSeq" + applSeq);
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }

        HashMap<String, Object> applmap = new HashMap<>();
        applmap.put("channel", "11");
        applmap.put("channelNo", channelNo);
        applmap.put("applSeq", applSeq);
        Map<String, Object> queryApplmap = appServerService.queryApplLoanDetail(token, applmap);
        logger.info("查询贷款详情接口，响应数据：" + map);
        if (map == null || "".equals(map)) {
            logger.info("网络异常,查询贷款详情接口,响应数据为空！" + map);
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        }
        JSONObject head = new JSONObject(queryApplmap.get("head"));
        JSONObject body = new JSONObject(queryApplmap.get("body"));
        String code = head.getString("retFlag");
        String message = head.getString("retMsg");
        if ("00000".equals(code)) {//查询贷款详情成功
            logger.info("查询贷款详情接口，响应数据：" + body.toString());
            String applSeq1 = body.getString("applSeq");//申请流水号
            String contNo = body.getString("contNo");//合同号
            String loanNo = body.getString("loanNo");//借据号
            String applyAmt = body.getString("applyAmt");//申请金额
            String apprvAmt = body.getString("apprvAmt");//核准金额（审批金额）
            //String = body.getString("repayApplAcNam");//还款账号户名
            String repayApplCardNo = body.getString("repayApplCardNo");//还款卡号
            String repayAccBankCde = body.getString("repayAccBankCde");//还款开户银行代码
            String repayAcProvince = body.getString("repayAcProvince");//还款账户所在省
            String repayAcCity = body.getString("repayAcCity");//还款账户所在市
            String applyDt = body.getString("applyDt");//申请注册日期
            // String = body.getString("loanActvDt");//放款日期
            //String = body.getString("apprvTnr");//贷款期数
            String mtdCde = body.getString("mtdCde");//还款方式
            //String  = body.getString("dueDay");//还款日
            // String applSeq1 = body.getString("lastDueDay");//到期日
            //String applSeq1 = body.getString("outSts");//审批状态
            String demo = body.getString("apprvAmt");//拒绝原因
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
            ifParamsIsNull(mapone);
            logger.info("查询贷款详情接口返回数据是：" + mapone);
            return success(mapone);
        } else {
            logger.info("查询贷款详情接口，响应数据返回状态码不正确：" + code);
            return fail(ConstUtil.ERROR_CODE, message);
        }
    }

    //10.  白条额度进行贷款支付结果主动查询接口    Sg-10009
    public Map<String, Object> queryAppLoanAndGoodsOne(Map<String, Object> map) throws Exception {
        String channelNo = (String) map.get("channelNo");
        String userId = (String) map.get("userId");//海尔集团用户ID
        String data = (String) map.get("data");
        if (StringUtils.isEmpty(userId)) {
            logger.info("获取的参数为空：userId" + userId);
            return fail(ConstUtil.ERROR_CODE, ConstUtil.FAILED_INFO);
        }
        Map<String, Object> userIdOne = getUserId(userId, channelNo);//获取用户userId
        String userIdone = (String) userIdOne.get("body");//用户信息
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
        //jsonObject.put("accBankCde", custBody.get("accBankCde"));//开户行名  需要确认
        jsonObject.put("accBankName", custBody.get("accBankName"));//开户行号
        jsonObject.put("certNo", custBody.get("certNo"));//身份证号
        jsonObject.put("phonenumber", custBody.get("mobile"));//手机号码
        return success(jsonObject);
    }

    //11.  白条额度进行主动查询接口    Sg-10010
    public Map<String, Object> edcheck(Map<String, Object> map) {
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
        if (StringUtils.isEmpty(token)) {
            logger.info("获取token失败token:" + token);
            return fail(ConstUtil.ERROR_CODE, ConstUtil.FAILED_INFO);
        }
        Map<String, Object> cacheMap = cache.get(token);
        if (StringUtils.isEmpty(cacheMap)) {
            logger.info("Jedis获取缓存失败");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        String idNo = (String) cacheMap.get("idNo");//客户证件号码
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
        JSONObject head = new JSONObject(edCheck.get("head"));
        String retFlag = (String) head.get("retFlag");
        String retMsg = (String) head.get("retMsg");
        if ("00000".equals(retFlag)) {
            //com.alibaba.fastjson.JSONObject limitRes = jsonObject.getJSONObject("body");
            JSONObject limitRes = new JSONObject(edCheck.get("body"));
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

    /*
     * 判断参数是否有空值
	 */
    private void ifParamsIsNull(Map<String, String> map) throws Exception {
        Set<String> keys = map.keySet();
        for (String key : keys) {
            if (map.get(key) == null || "".equals(map.get(key)) || "null".equals(map.get(key))) {
                throw new Exception("参数" + key + "不能为空！");
            }
        }
    }

    public Map<String, Object> getUserId(String userId, String channelNo) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("channel", "11");
        map.put("channelNo", channelNo);
        Map<String, Object> userIdmap = appServerService.getUserId(null, map);
        if (!HttpUtil.isSuccess(userIdmap)) {
            logger.info("调集团用户id查询用户信息接口返回信息错误userIdmap" + userIdmap);
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        }
        JSONObject body = new JSONObject(map.get("body"));
        String userIdone = body.getString("userId");
        HashMap<Object, Object> mapone = new HashMap<>();
        mapone.put("userId", userIdone);
        return success(mapone);
    }

}
