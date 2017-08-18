package com.haiercash.payplatform.pc.shunguang.service.impl;

import com.haiercash.commons.redis.Session;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.haiercash.commons.redis.Cache;
import com.haiercash.commons.util.*;
import com.haiercash.payplatform.common.config.EurekaServer;
import com.haiercash.payplatform.common.dao.AppOrdernoTypgrpRelationDao;
import com.haiercash.payplatform.common.data.AppOrder;
import com.haiercash.payplatform.common.data.AppOrderGoods;
import com.haiercash.payplatform.common.data.AppOrdernoTypgrpRelation;
import com.haiercash.payplatform.common.enums.OrderEnum;
import com.haiercash.payplatform.common.service.AppServerService;
import com.haiercash.payplatform.common.service.CmisApplService;
import com.haiercash.payplatform.common.service.GmService;
import com.haiercash.payplatform.common.utils.*;
import com.haiercash.payplatform.common.utils.EncryptUtil;
import com.haiercash.payplatform.pc.shunguang.service.SgInnerService;
import com.haiercash.payplatform.service.BaseService;
import com.haiercash.payplatform.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * Created by yuanli on 2017/8/9.
 */
@Service
public class SgInnerServiceImpl extends BaseService implements SgInnerService{
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

    @Override
    public Map<String, Object> userlogin(Map<String, Object> map) {
        logger.info("登录页面********************开始");
        String uidLocal = (String) map.get("userId");
        String password = (String) map.get("password");
        String token = (String) map.get("token");
        String channel = (String) map.get("channel");
        String channelNo = (String) map.get("channelNo");
        if(StringUtils.isEmpty(uidLocal) || StringUtils.isEmpty(password) || StringUtils.isEmpty(token)
                || StringUtils.isEmpty(channel) || StringUtils.isEmpty(channelNo)){
            logger.info("userId:" + uidLocal + "   token:" + token + "   channelNo:" + channelNo + "   channel:" + channel);
            logger.info("前台获取请求参数有误");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.FAILED_INFO);
        }
        //获取缓存数据
        Map<String, Object> cacheMap = session.get(token, Map.class);
        if (cacheMap.isEmpty()) {
            logger.info("Jedis数据获取失败");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        //获取会员uid
        String uidHaier = (String) cacheMap.get("uidHaier");
        if(StringUtils.isEmpty(uidHaier)){
            logger.info("uidHaier:" + uidHaier);
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        //验证并绑定集团用户
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("externUid", EncryptUtil.simpleEncrypt(uidHaier));
        paramMap.put("userId", EncryptUtil.simpleEncrypt(uidLocal));
        paramMap.put("password", EncryptUtil.simpleEncrypt(password));
        Map usermap = appServerService.validateAndBindHaierUser(token, paramMap);
        if(!HttpUtil.isSuccess(usermap)){
            return fail(ConstUtil.ERROR_CODE, "会员绑定失败");
        }
        //获取绑定手机号
        String phoneNo = (String) ((HashMap<String, Object>)(usermap.get("body"))).get("mobile");
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
        String custretflag = (String) ((HashMap<String, Object>)(custresult.get("head"))).get("retFlag");
        if(!"00000".equals(custretflag) && !"C1120".equals(custretflag)){//查询实名信息失败
            String custretMsg = (String) ((HashMap<String, Object>)(custresult.get("head"))).get("retMsg");
            return fail(ConstUtil.ERROR_CODE, custretMsg);
        }
        if("C1120".equals(custretflag)){//C1120  客户信息不存在  跳转无额度页面
            String backurl = "login.html?token="+token;//TODO!!!!!!
            map.put("backurl", backurl);
            return success(map);
        }
        String certType = (String) ((HashMap<String, Object>)(custresult.get("body"))).get("certType");//证件类型
        String certNo = (String) ((HashMap<String, Object>)(custresult.get("body"))).get("certNo");//身份证号
        String custNo = (String) ((HashMap<String, Object>)(custresult.get("body"))).get("custNo");//客户编号
        String custName = (String) ((HashMap<String, Object>)(custresult.get("body"))).get("custName");//客户名称
        String cardNo = (String) ((HashMap<String, Object>)(custresult.get("body"))).get("cardNo");//银行卡号
        String bankNo = (String) ((HashMap<String, Object>)(custresult.get("body"))).get("acctBankNo");//银行代码
        String bankName = (String) ((HashMap<String, Object>)(custresult.get("body"))).get("acctBankName");//银行名称

        cacheMap.put("custNo", custNo);//客户编号
        cacheMap.put("custName", custName);//客户姓名
        cacheMap.put("cardNo", cardNo);//银行卡号
        cacheMap.put("bankCode", bankNo);//银行代码
        cacheMap.put("bankName", bankName);//银行名称
        cacheMap.put("idNo", certNo);//身份证号
        cacheMap.put("idType", certType);
        //6.查询客户额度
        Map<String, Object> edMap = new HashMap<String, Object>();
        edMap.put("userId", uidLocal);//内部userId
        edMap.put("channelNo", channelNo);
        Map edresult = appServerService.checkEdAppl(token, edMap);
        if (!HttpUtil.isSuccess(edresult) ) {//额度校验失败
            String retmsg = (String) ((HashMap<String, Object>)(edresult.get("head"))).get("retMsg");
            return fail(ConstUtil.ERROR_CODE, retmsg);
        }
        //获取自主支付可用额度金额
        String crdNorAvailAmt = (String) ((HashMap<String, Object>)(edresult.get("body"))).get("crdNorAvailAmt");
        if (crdNorAvailAmt != null && !"".equals(crdNorAvailAmt) ){
            //跳转有额度页面
            String backurl = "login.html?token="+token;//TODO!!!!!!
            map.put("backurl", backurl);
            return success(map);
        }
        //审批状态判断
        String outSts = (String) ((HashMap<String, Object>)(edresult.get("body"))).get("outSts");
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
    }

    @Override
    public Map<String, Object> initPayApply(Map<String, Object> map) {
        String token = (String) map.get("token");
        String channel = (String) map.get("channel");
        String channelNo = (String) map.get("channelNo");

//        Map<String, Object> cacheMap = cache.get(token);
//        if (cacheMap == null || "".equals(cacheMap)) {
//            logger.info("Jedis数据获取失败");
//            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
//        }
//        AppOrder appOrder = (AppOrder) cacheMap.get("apporder");//获取订单信息
//        String payAmt = appOrder.getApplyAmt();//申请金额
//        String typCde = appOrder.getTypCde();//贷款品种

        String payAmt = "3000";//申请金额
        String typCde = "17035a";

        Map<String, Object> paySsMap = new HashMap<String, Object>();
        paySsMap.put("typCde", typCde);
        paySsMap.put("apprvAmt", payAmt);
        paySsMap.put("channel", channel);
        paySsMap.put("channelNo", channelNo);
        Map<String, Object> payssresultMap = appServerService.getBatchPaySs(token, paySsMap);
        if (!HttpUtil.isSuccess(payssresultMap) ) {//额度校验失败
            String retmsg = (String) ((HashMap<String, Object>)(payssresultMap.get("head"))).get("retMsg");
            return fail(ConstUtil.ERROR_CODE, retmsg);
        }
        //{head={retFlag=00000, retMsg=处理成功}, body={info=[{psPerdNo=12, instmAmt=259.0}, {psPerdNo=6, instmAmt=518.0}]}}
        String result = JSONObject.toJSONString(payssresultMap);
        JSONObject custBody = JSONObject.parseObject(result).getJSONObject("body");
        JSONArray jsonarray = custBody.getJSONArray("info");
        String instmAmt = "";
        String psPerdNo = "";
        for (int i = 0; i < jsonarray.size(); i++) {
            Object object = jsonarray.get(0);
            JSONObject json = JSONObject.parseObject(JSONObject.toJSONString(object));
            instmAmt = json.getString("instmAmt");
            psPerdNo = json.getString("psPerdNo");
        }

        Map<String, Object> payMap = new HashMap<String, Object>();
        payMap.put("typCde", typCde);
        payMap.put("apprvAmt", payAmt);
        payMap.put("applyTnrTyp", psPerdNo);
        payMap.put("applyTnr", psPerdNo);
        Map<String, Object> payresultMap =  appServerService.getPaySs(token, payMap);
        if (!HttpUtil.isSuccess(payresultMap) ) {//额度校验失败
            String retmsg = (String) ((HashMap<String, Object>)(payresultMap.get("head"))).get("retMsg");
            return fail(ConstUtil.ERROR_CODE, retmsg);
        }
        String payresult = JSONObject.toJSONString(payresultMap);
        JSONObject payBody = JSONObject.parseObject(payresult).getJSONObject("body");
        logger.info("payBody:" + payBody);
        String totalAmt = payBody.get("totalAmt").toString();

        Map retrunmap = new HashMap();
        retrunmap.put("payAmt", payAmt);
        retrunmap.put("payMtd", ((HashMap<String, Object>)(payssresultMap.get("body"))).get("info"));
        retrunmap.put("totalAmt", totalAmt);
        return success(retrunmap);
    }


}
