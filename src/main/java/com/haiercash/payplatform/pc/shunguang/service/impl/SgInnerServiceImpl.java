package com.haiercash.payplatform.pc.shunguang.service.impl;

import com.haiercash.commons.redis.Cache;
import com.haiercash.payplatform.common.service.AppServerService;
import com.haiercash.payplatform.common.utils.ConstUtil;
import com.haiercash.payplatform.common.utils.EncryptUtil;
import com.haiercash.payplatform.common.utils.HttpUtil;
import com.haiercash.payplatform.pc.shunguang.service.SgInnerService;
import com.haiercash.payplatform.service.BaseService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yuanli on 2017/8/9.
 */
@Service
public class SgInnerServiceImpl extends BaseService implements SgInnerService{
    @Autowired
    private Cache cache;
    @Autowired
    private AppServerService appServerService;

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
        Map<String, Object> cacheMap = cache.get(token);
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
            String retmsg = (String) ((HashMap<String, Object>)(custresult.get("head"))).get("retMsg");
            return fail(ConstUtil.ERROR_CODE, retmsg);
        }
        //获取自主支付可用额度金额
        String crdNorAvailAmt = (String) ((HashMap<String, Object>)(custresult.get("body"))).get("crdNorAvailAmt");
        if (crdNorAvailAmt != null && !"".equals(crdNorAvailAmt) ){
            //跳转有额度页面
            String backurl = "login.html?token="+token;//TODO!!!!!!
            map.put("backurl", backurl);
            return success(map);
        }
        //审批状态判断
        String outSts = (String) ((HashMap<String, Object>)(custresult.get("body"))).get("outSts");
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
}
