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
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
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

    @Override
    public Map<String, Object> saveStoreInfo(Map<String, Object> storeInfo) {
        String userId = (String) storeInfo.get("userId");
        String data = (String) storeInfo.get("data");

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
        requestParams.put("channelno", super.getChannelNo());
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
        Object uid = userjson.get("user_id");
        if(StringUtils.isEmpty(uid)){
            String error = userjson.get("error").toString();
            return fail(ConstUtil.ERROR_CODE, error);
        }
        String uidHaier = uid.toString();
        String custPhoneNo = (String) userjson.get("phone_number");
        String userName = (String) userjson.get("username");

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
