package com.haiercash.payplatform.pc.qiaorong.service.impl;

import com.bestvike.lang.Base64Utils;
import com.haiercash.commons.redis.Session;
import com.haiercash.commons.util.EncryptUtil;
import com.haiercash.payplatform.common.dao.CooperativeBusinessDao;
import com.haiercash.payplatform.common.dao.SignContractInfoDao;
import com.haiercash.payplatform.common.data.CooperativeBusiness;
import com.haiercash.payplatform.common.data.SignContractInfo;
import com.haiercash.payplatform.config.EurekaServer;
import com.haiercash.payplatform.pc.moxie.service.MoxieService;
import com.haiercash.payplatform.pc.qiaorong.service.QiaorongService;
import com.haiercash.payplatform.service.AppServerService;
import com.haiercash.payplatform.service.BaseService;
import com.haiercash.payplatform.service.CmisApplService;
import com.haiercash.payplatform.utils.*;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * Created by yuanli on 2017/9/25.
 */
@Service
public class QiaorongServiceImpl extends BaseService implements QiaorongService {

    private static int limitAmount = 30000;
    @Value("${app.other.moxie_apikey}")
    protected String moxie_apikey;
    @Value("${app.other.haiercashpay_web_url}")
    protected String haiercashpay_web_url;
    @Autowired
    private Session session;
    @Autowired
    private CmisApplService cmisApplService;
    @Autowired
    private AppServerService appServerService;
    @Autowired
    private MoxieService moxieService;
    @Autowired
    private CooperativeBusinessDao cooperativeBusinessDao;
    @Autowired
    private SignContractInfoDao signContractInfoDao;

    /*
    ca签章
     */
    @Override
    public Map<String, Object> cacontract(Map<String, Object> map) throws Exception {
        //logger.info("输出：" + map);
        String channelNo = (String) map.get("channelNo");
        String data = (String) map.get("data");
        if(StringUtils.isEmpty(channelNo)){
            logger.info("渠道编码不能为空");
            return fail(ConstUtil.ERROR_CODE, "渠道编码不能为空");
        }
        if(StringUtils.isEmpty(data)){
            logger.info("请求数据不能为空");
            return fail(ConstUtil.ERROR_CODE, "请求数据不能为空");
        }

        String params = decryptData(data, channelNo);
        logger.info("CA签章接收到的数据：" + params);
        JSONObject camap = new JSONObject(params);
//        ObjectMapper objectMapper = new ObjectMapper();
//        Map camap = objectMapper.readValue(params, Map.class);
        String applSeq = (String) camap.get("applSeq");
        String typ_cde = (String) camap.get("typ_cde");
        String callbackUrl = (String) camap.get("callbackUrl");
        if(StringUtils.isEmpty(applSeq)){
            return fail(ConstUtil.ERROR_CODE, "申请流水号不能为空");
        }
        if(StringUtils.isEmpty(typ_cde)){
            return fail(ConstUtil.ERROR_CODE, "贷款品种不能为空");
        }
        if(StringUtils.isEmpty(callbackUrl)){
            return fail(ConstUtil.ERROR_CODE, "回调接口不能为空");
        }

        String uuid = UUID.randomUUID().toString().replace("-", "");
        Map cachemap = new HashMap();
        cachemap.put("callbackUrl", callbackUrl);
        session.set(uuid, cachemap);

        String backurl = haiercashpay_web_url + "qr/#!/installment.html?token=" + uuid + "&applseq=" + applSeq;
        logger.info("签章跳转页面地址：" + backurl);
        Map ResultMap = new HashMap();
        ResultMap.put("backurl", backurl);
        return success(ResultMap);


    }

    /*
    合同初始化
     */
    @Override
    public Map<String, Object> contractInit(Map<String, Object> map) {
        logger.info("合同展示页面初始化*******开始");
        String applseq = (String) map.get("applseq");
        String token = (String) map.get("token");
        logger.info("申请流水号：" + applseq);
        if(StringUtils.isEmpty(applseq)){
            logger.info("申请流水号为空");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        }
        if(StringUtils.isEmpty(token)){
            logger.info("token不能为空");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        }

        Map<String, Object> cachemap = session.get(token, Map.class);
        if (cachemap == null || "".equals(cachemap)) {
            logger.info("Jedis数据获取失败");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        // 去cmis查询订单信息
        Map<String, Object> appOrderMapCmis = this.getAppOrderMapFromCmis(applseq);
        if (appOrderMapCmis == null || appOrderMapCmis.isEmpty()) {
            logger.info("未查询到贷款信息");
            return fail(ConstUtil.ERROR_CODE, "未查询到贷款信息");
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("totalamount", appOrderMapCmis.get("APPLY_AMT"));//应还总额
        resultMap.put("loannumber", appOrderMapCmis.get("APPLY_TNR"));//剩余期数
        List<String> goodsNameList = new ArrayList<>();
        List<Map<String, Object>> goods = (List<Map<String, Object>>) appOrderMapCmis.get("goods");
        if (goods != null && !goods.isEmpty()) {
            for (Map<String, Object> good : goods) {
                goodsNameList.add((String) good.get("GOODS_NAME"));
            }
        }
        String typCde = String.valueOf(appOrderMapCmis.get("LOAN_TYP"));//贷款品种

        resultMap.put("goods", goodsNameList);//分期产品
        resultMap.put("name", appOrderMapCmis.get("CUST_NAME"));//姓名
        resultMap.put("phone", appOrderMapCmis.get("INDIV_MOBILE"));//手机号
        resultMap.put("idnumber", appOrderMapCmis.get("ID_NO"));//身份证号
        //还款帐号
        List<Map<String, Object>> cardList = (ArrayList<Map<String, Object>>) appOrderMapCmis.get("accInfo");
        for (Map<String, Object> card : cardList) {
            // 获取卡类型
            String type = String.valueOf(card.get("APPL_AC_KIND"));
            if ("02".equals(type) || "2".equals(type)) {
                resultMap.put("cardnumber", card.get("APPL_AC_NO"));//还款卡号
                cachemap.put("cardnumber", card.get("APPL_AC_NO"));
            }
        }

        //resultMap.put("cardnumber",appOrderMapCmis.get(""));
        //调用6.17接口 查询每期应还款
        HashMap<String, Object> loanRequestMap = new HashMap<>();
        loanRequestMap.put("typCde", appOrderMapCmis.get("LOAN_TYP"));
        loanRequestMap.put("apprvAmt", appOrderMapCmis.get("APPLY_AMT"));
        loanRequestMap.put("applyTnrTyp", appOrderMapCmis.get("APPLY_TNR_TYP"));
        loanRequestMap.put("applyTnr", appOrderMapCmis.get("APPLY_TNR"));
        loanRequestMap.put("fstPay", appOrderMapCmis.get("FST_PAY"));
        logger.debug("loanRequestMap==" + loanRequestMap);
        Map<String, Object> paySsResultMap = cmisApplService.getHkssReturnMap(loanRequestMap, getGateUrl(), getToken());

        if (paySsResultMap != null) {
            Map<String, Object> body = (Map<String, Object>) paySsResultMap.get("body");
            List<Map<String, Object>> mx = (List<Map<String, Object>>) body.get("mx");
            if (mx != null) {

                Map<String, Object> perMx = mx.size() > 1 ? mx.get(1) : mx.get(0);
                String perMxString = String.valueOf(perMx.get("instmAmt"));
                resultMap.put("loanpayment", perMxString);//每期应还金额
            }
        }

        cachemap.put("name", appOrderMapCmis.get("CUST_NAME"));
        cachemap.put("phoneNo", appOrderMapCmis.get("INDIV_MOBILE"));
        cachemap.put("idCard", appOrderMapCmis.get("ID_NO"));
        cachemap.put("applSeq", applseq);
        cachemap.put("totalamount", appOrderMapCmis.get("APPLY_AMT"));
        cachemap.put("typCde", typCde);//
        cachemap.put("userId", appOrderMapCmis.get("INDIV_MOBILE"));
        session.set(token, cachemap);


        logger.info(resultMap);
        logger.info("合同展示页面初始化*******结束");
        return success(resultMap);
    }

    @Override
    public Map<String, Object> checkFourKeys(Map<String, Object> map) {
        logger.info("实名四要素验证****************开始");
        String token = (String) map.get("token");
        String channel = (String) map.get("channel");
        String channelNo = (String) map.get("channelNo");
        if(StringUtils.isEmpty(token) || StringUtils.isEmpty(channel) || StringUtils.isEmpty(channelNo)){
            logger.info("前台传入参数有误");
            logger.info("token:" + token + "  channel:" + channel + "  channelNo:" + channelNo);
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        }
        Map<String, Object> returnmap = new HashMap<String, Object>();
        //缓存数据获取
        Map<String, Object> cacheMap = session.get(token, Map.class);
        if (cacheMap == null || "".equals(cacheMap)) {
            logger.info("Jedis数据获取失败");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        //获取数据
        String name = (String) cacheMap.get("name");
        String phone = (String) cacheMap.get("phoneNo");
        String idNo = (String) cacheMap.get("idCard");//身份证
        String cardnumber = (String) cacheMap.get("cardnumber");//银行卡
        String totalamount = cacheMap.get("totalamount").toString();
        String applseq = (String) cacheMap.get("applSeq");
        String typCde = (String) cacheMap.get("typCde");
        //1.新增并验证实名信息
        Map<String, Object> identityMap = new HashMap<String, Object>();
        identityMap.put("token", token);
        identityMap.put("channel", channel);
        identityMap.put("channelNo", channelNo);
        identityMap.put("custName", name); //客户姓名 √
        identityMap.put("certNo", idNo); //身份证号 √
        identityMap.put("cardNo", cardnumber); //银行卡号 √
        identityMap.put("mobile", phone); //手机号 √
        identityMap.put("dataFrom", channelNo); //数据来源 √
        identityMap.put("threeParamVal", ConstUtil.THREE_PARAM_VAL_N); //是否需要三要素验证
//        identityMap.put("userId", phone); //客户userId
//        identityMap.put("acctProvince", acctProvince); //开户行省代码
//        identityMap.put("acctCity", acctCity); //开户行市代码
        Map<String, Object> identityresultmap = appServerService.fCiCustRealThreeInfo(token, identityMap);
        Map identityheadjson = (Map<String, Object>) identityresultmap.get("head");
        String identityretFlag = (String) identityheadjson.get("retFlag");
        if (!"00000".equals(identityretFlag)) {
            String retMsg = (String) identityheadjson.get("retMsg");
            return fail(ConstUtil.ERROR_CODE, retMsg);
        }
        Map identitybodyjson = (Map<String, Object>) identityresultmap.get("body");
        //信息保存
        String custNo = (String) identitybodyjson.get("custNo");
        String custName = (String) identitybodyjson.get("custName");
        String certNo = (String) identitybodyjson.get("certNo");

        cacheMap.put("custNo", custNo);
        cacheMap.put("custName", custName);
        cacheMap.put("certNo", certNo);
        session.set(token, cacheMap);

//        //2.判断是否已注册
//        Map<String, Object> paramMap = new HashMap<String, Object>();
//        paramMap.put("channelNo",channelNo);
//        paramMap.put("channel",channel);
//        paramMap.put("mobile", EncryptUtil.simpleEncrypt(phone));
//        Map<String, Object> registerMap = appServerService.isRegister(token, paramMap);
//        if(registerMap == null){
//            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
//        }
//        Map resultmapjsonMap = (Map<String, Object>) registerMap.get("head");
//        String resultmapFlag = (String) resultmapjsonMap.get("retFlag");
//        if(!"00000".equals(resultmapFlag)){
//            String retMsg = (String) resultmapjsonMap.get("retMsg");
//            return fail(ConstUtil.ERROR_CODE, retMsg);
//        }
//        Map resultmapbodyMap = (Map<String, Object>) registerMap.get("body");
//        String isRegister = (String)resultmapbodyMap.get("isRegister");
//        if("N".equals(isRegister)){
//            returnmap.put("flag","01");//跳转注册页面
//            return success(returnmap);//跳转注册页面
//        }
//        if(!"Y".equals(isRegister)){
//            return fail("01", "手机已被注册！请联系客服修改。客服电话：400777");
//        }

        //3.手机号已注册，判断是否需要人脸识别
        Map<String, Object> faceparamMap = new HashMap<String, Object>();
        faceparamMap.put("typCde", typCde);
        faceparamMap.put("source", channel);
        faceparamMap.put("custNo", custNo);
        faceparamMap.put("name", name);
        faceparamMap.put("idNumber", idNo);
        faceparamMap.put("token", token);
        faceparamMap.put("channel", channel);
        faceparamMap.put("channelNo", channelNo);
        Map<String, Object> resultmap = appServerService.ifNeedFaceChkByTypCde(token, faceparamMap);
        Map headjson = (Map) resultmap.get("head");
        String retFlag = (String) headjson.get("retFlag");
        String retMsg = (String) headjson.get("retMsg");
        if(!"00000".equals(retFlag)){
            return fail(ConstUtil.ERROR_CODE, retMsg);
        }
        Map body = (Map) resultmap.get("body");
        String code = (String) body.get("code"); //结果标识码
        if("00".equals(code)){//人脸识别通过
            logger.info("已经通过了人脸识别（得分合格），不需要再做人脸识别");
            double amount = Double.parseDouble(totalamount);

            logger.info("已经通过了人脸识别（得分合格），跳转合同展示");
            returnmap.put("flag", "05");//跳转合同
            return success(returnmap);//跳转合同展示页面

//            Map<String, Object> moxiemap = new HashMap<String, Object>();
//            moxiemap.put("applseq", applseq);
//            Map<String, Object> mapmoxie = appServerService.getMoxieByApplseq(token, moxiemap);
//            Map headjsonmoxie = (Map) mapmoxie.get("head");
//            String retFlagmoxie = (String) headjsonmoxie.get("retFlag");
//            String retMsgmoxie = (String) headjsonmoxie.get("retMsg");
//            if(!"00000".equals(retFlagmoxie)){
//                return fail(ConstUtil.ERROR_CODE, retMsgmoxie);
//            }
//            Map bodymoxie = moxieService.getMoxieByApplseq(applseq);
//            String isFundFlag = (String) bodymoxie.get("isFund");
//            String isBankFlag = (String) bodymoxie.get("isBank");
//            String isCarrierFlag = (String) bodymoxie.get("isCarrier");
//
//            //判断金额是否需要做魔蝎
//            if(amount >= limitAmount){
//                //判断是否做过公积金网银
//                if("Y".equals(isFundFlag) || "Y".equals(isBankFlag)){//已做过公积金、网银认证  跳转合同展示
//                    logger.info("已经通过了人脸识别（得分合格），跳转合同展示");
//                    returnmap.put("flag", "05");//跳转合同
//                    return success(returnmap);//跳转合同展示页面
//                }else{//未做过公积金、网银认证  跳转魔蝎认证页面
//                    logger.info("已经通过了人脸识别（得分合格），跳转魔蝎");
//                    returnmap.put("flag", "04");//跳转魔蝎认证
//                    return success(returnmap);//跳转魔蝎页面
//                }
//            }else{
//                logger.info("已经通过了人脸识别（得分合格），跳转合同展示");
//                returnmap.put("flag", "05");//跳转合同
//                return success(returnmap);//跳转合同展示页面
//            }

        }else if("01".equals(code)){//01：未通过人脸识别，剩余次数为0，不能再做人脸识别，录单终止
            //终止
            logger.info("未通过人脸识别，剩余次数为0，不能再做人脸识别，录单终止");
            return fail(ConstUtil.ERROR_CODE, "不能再做人脸识别，录单终止!");
        }else if("02".equals(code)){//02：未通过人脸识别，剩余次数为0，不能再做人脸识别，但可以上传替代影像
            //跳转替代影像
            logger.info("未通过人脸识别，剩余次数为0，不能再做人脸识别，但可以上传替代影像");
            returnmap.put("flag", "03");// 手持身份证
            return success(returnmap);
        }else if("10".equals(code)){//10：未通过人脸识别，可以再做人脸识别
            //可以做人脸识别
            logger.info("未通过人脸识别，可以再做人脸识别");
            returnmap.put("flag", "02");// 人脸识别
            return success(returnmap);
        }

        return returnmap;
    }

    /*
    注册
     */
    @Override
    public Map<String, Object> register(Map<String, Object> map) {
        String token = super.getToken();
        String channel = super.getChannel();
        String channelNo = super.getChannelNo();
        String password = (String) map.get("password");
        String registerNum = (String) map.get("registerNum");
        String registerEvent = (String) map.get("registerEvent");

        if(StringUtils.isEmpty(token) || StringUtils.isEmpty(channel) || StringUtils.isEmpty(channelNo) || StringUtils.isEmpty(password)){
            logger.info("前台传入参数有误");
            logger.info("token:" + token + "  channel:" + channel + "  channelNo:" + channelNo + "  password" + password);
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        }
        Map<String, Object> returnmap = new HashMap<String, Object>();
        //缓存数据获取
        Map<String, Object> cacheMap = session.get(token, Map.class);
        if (cacheMap == null || "".equals(cacheMap)) {
            logger.info("Jedis数据获取失败");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        //
        String name = (String) cacheMap.get("name");
        String idNo = (String) cacheMap.get("idCard");//身份证
        String totalamount = cacheMap.get("totalamount").toString();
        String applseq = (String) cacheMap.get("applSeq");
        String typCde = (String) cacheMap.get("typCde");
        String custNo = (String) cacheMap.get("custNo");
        String phone = (String) cacheMap.get("phoneNo");

        //注册事件
        Map<String, Object> riskmap = new HashMap<String, Object>();
        List<List<Map<String, String>>> content = new ArrayList<>();
        List<Map<String, String>> contentList = new ArrayList<>();


        logger.info("百融注册事件：registerNum：" + registerNum + "********registerEvent:" + registerEvent);
        if(!StringUtils.isEmpty(registerNum)){
            Map<String, String> mapLoginEvent = new HashMap();
            mapLoginEvent.put("content", EncryptUtil.simpleEncrypt(registerNum));
            mapLoginEvent.put("reserved6", applseq);
            mapLoginEvent.put("reserved7", "antifraud_login");
            contentList.add(mapLoginEvent);

            content.add(contentList);
            riskmap.put("content", content);
            riskmap.put("reserved7", "antifraud_register");

            riskmap.put("applSeq", applseq);

            riskmap.put("idNo", EncryptUtil.simpleEncrypt(idNo));
            riskmap.put("name", EncryptUtil.simpleEncrypt(name));
            riskmap.put("mobile", EncryptUtil.simpleEncrypt(phone));
            riskmap.put("dataTyp", "A501");
            riskmap.put("source", "2");
            riskmap.put("token", token);
            riskmap.put("channel", channel);
            riskmap.put("channelNo", channelNo);
            appServerService.updateRiskInfo("", map);
        }



        //注册
        Map<String, Object> registermap = new HashMap<String, Object>();
        registermap.put("mobile", EncryptUtil.simpleEncrypt(phone));
        registermap.put("password", EncryptUtil.simpleEncrypt(password));
        registermap.put("deviceId", EncryptUtil.simpleEncrypt(UUID.randomUUID().toString().replace("-", "")));

        String url = EurekaServer.UAUTH + "/app/uauth/saveUauthUsers";
        logger.info("用户注册接口, 请求地址：" + url);
        logger.info("用户注册接口, 请求数据：" + registermap);
        Map<String, Object> resultregistermap = HttpUtil.restPostMap(url, registermap);
        logger.info("用户注册接口, 返回数据：" + resultregistermap);

        Map headmap = (Map<String, Object>) resultregistermap.get("head");
        String retFlag = (String) headmap.get("retFlag");
        if (!"00000".equals(retFlag)) {
            String retMsg = (String) headmap.get("retMsg");
            return fail(ConstUtil.ERROR_CODE, retMsg);
        }

        //3.手机号已注册，判断是否需要人脸识别
        Map<String, Object> faceparamMap = new HashMap<String, Object>();
        faceparamMap.put("typCde", typCde);
        faceparamMap.put("source", channel);
        faceparamMap.put("custNo", custNo);
        faceparamMap.put("name", name);
        faceparamMap.put("idNumber", idNo);
        faceparamMap.put("token", token);
        faceparamMap.put("channel", channel);
        faceparamMap.put("channelNo", channelNo);
        Map<String, Object> resultmap = appServerService.ifNeedFaceChkByTypCde(token, faceparamMap);
        Map headjson = (Map) resultmap.get("head");
        String faceretFlag = (String) headjson.get("retFlag");
        String retMsg = (String) headjson.get("retMsg");
        if(!"00000".equals(faceretFlag)){
            return fail(ConstUtil.ERROR_CODE, retMsg);
        }
        Map body = (Map) resultmap.get("body");
        String code = (String) body.get("code"); //结果标识码
        if("00".equals(code)){//人脸识别通过
            logger.info("已经通过了人脸识别（得分合格），不需要再做人脸识别");
            double amount = Double.parseDouble(totalamount);

            logger.info("已经通过了人脸识别（得分合格），跳转合同展示");
            returnmap.put("flag", "05");//跳转合同
            return success(returnmap);//跳转合同展示页面


//            Map bodymoxie = moxieService.getMoxieByApplseq(applseq);
//            String isFundFlag = (String) bodymoxie.get("isFund");
//            String isBankFlag = (String) bodymoxie.get("isBank");
//            String isCarrierFlag = (String) bodymoxie.get("isCarrier");
//
//            //判断金额是否需要做魔蝎
//            if(amount >= limitAmount){
//                //判断是否做过公积金网银
//                if("Y".equals(isFundFlag) || "Y".equals(isBankFlag)){//已做过公积金、网银认证  跳转合同展示
//                    logger.info("已经通过了人脸识别（得分合格），跳转合同展示");
//                    returnmap.put("flag", "05");//跳转合同
//                    return success(returnmap);//跳转合同展示页面
//                }else{//未做过公积金、网银认证  跳转魔蝎认证页面
//                    logger.info("已经通过了人脸识别（得分合格），跳转魔蝎");
//                    returnmap.put("flag", "04");//跳转魔蝎认证
//                    return success(returnmap);//跳转魔蝎页面
//                }
//            }else{
//                logger.info("已经通过了人脸识别（得分合格），跳转合同展示");
//                returnmap.put("flag", "05");//跳转合同
//                return success(returnmap);//跳转合同展示页面
//            }

        }else if("01".equals(code)){//01：未通过人脸识别，剩余次数为0，不能再做人脸识别，录单终止
            //终止
            logger.info("未通过人脸识别，剩余次数为0，不能再做人脸识别，录单终止");
            return fail(ConstUtil.ERROR_CODE, "不能再做人脸识别，录单终止!");
        }else if("02".equals(code)){//02：未通过人脸识别，剩余次数为0，不能再做人脸识别，但可以上传替代影像
            //跳转替代影像
            logger.info("未通过人脸识别，剩余次数为0，不能再做人脸识别，但可以上传替代影像");
            returnmap.put("flag", "03");// 手持身份证
            return success(returnmap);
        }else if("10".equals(code)){//10：未通过人脸识别，可以再做人脸识别
            //可以做人脸识别
            logger.info("未通过人脸识别，可以再做人脸识别");
            returnmap.put("flag", "02");// 人脸识别
            return success(returnmap);
        }


        return returnmap;
    }

    @Override
    public Map<String, Object> isNeedMoxie(Map<String, Object> map) {
        String token = super.getToken();
        String channel = super.getChannel();
        String channelNo = super.getChannelNo();
        if(StringUtils.isEmpty(token) || StringUtils.isEmpty(channel) || StringUtils.isEmpty(channelNo)){
            logger.info("前台传入参数有误");
            logger.info("token:" + token + "  channel:" + channel + "  channelNo:" + channelNo);
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        }
        Map<String, Object> returnmap = new HashMap<String, Object>();
        //缓存数据获取
        Map<String, Object> cacheMap = session.get(token, Map.class);
        if (cacheMap == null || "".equals(cacheMap)) {
            logger.info("Jedis数据获取失败");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }

        String totalamount = cacheMap.get("totalamount").toString();
        String applseq = (String) cacheMap.get("applSeq");

        double amount = Double.parseDouble(totalamount);

        logger.info("已经通过了人脸识别（得分合格），跳转合同展示");
        returnmap.put("flag", "05");//跳转合同
        return success(returnmap);//跳转合同展示页面

//        Map bodymoxie = moxieService.getMoxieByApplseq(applseq);
//        String isFundFlag = (String) bodymoxie.get("isFund");
//        String isBankFlag = (String) bodymoxie.get("isBank");
//        String isCarrierFlag = (String) bodymoxie.get("isCarrier");
//
//        //判断金额是否需要做魔蝎
//        if(amount >= limitAmount){
//            //判断是否做过公积金网银
//            if("Y".equals(isFundFlag) || "Y".equals(isBankFlag)){//已做过公积金、网银认证  跳转合同展示
//                logger.info("已经通过了人脸识别（得分合格），跳转合同展示");
//                returnmap.put("flag", "05");//跳转合同
//                return success(returnmap);//跳转合同展示页面
//            }else{//未做过公积金、网银认证  跳转魔蝎认证页面
//                logger.info("已经通过了人脸识别（得分合格），跳转魔蝎");
//                returnmap.put("flag", "04");//跳转魔蝎认证
//                return success(returnmap);//跳转魔蝎页面
//            }
//        }else{
//            logger.info("已经通过了人脸识别（得分合格），跳转合同展示");
//            returnmap.put("flag", "05");//跳转合同
//            return success(returnmap);//跳转合同展示页面
//        }

    }

    @Override
    public Map<String, Object> getMoxieByApplseq(Map<String, Object> map) {
        String token = super.getToken();
        String channel = super.getChannel();
        String channelNo = super.getChannelNo();
        if(StringUtils.isEmpty(token) || StringUtils.isEmpty(channel) || StringUtils.isEmpty(channelNo)){
            logger.info("前台传入参数有误");
            logger.info("token:" + token + "  channel:" + channel + "  channelNo:" + channelNo);
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        }
        Map<String, Object> returnmap = new HashMap<String, Object>();
        //缓存数据获取
        Map<String, Object> cacheMap = session.get(token, Map.class);
        if (cacheMap == null || "".equals(cacheMap)) {
            logger.info("Jedis数据获取失败");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }

        String totalamount = cacheMap.get("totalamount").toString();
        String applseq = (String) cacheMap.get("applSeq");

        double amount = Double.parseDouble(totalamount);

        Map bodymoxie = moxieService.getMoxieByApplseq(applseq);
        return success(bodymoxie);
    }

    @Override
    public Map<String, Object> getMoxieInfo(Map<String, Object> map) {
        String token = super.getToken();
        String channel = super.getChannel();
        String channelNo = super.getChannelNo();
        if(StringUtils.isEmpty(token) || StringUtils.isEmpty(channel) || StringUtils.isEmpty(channelNo)){
            logger.info("前台传入参数有误");
            logger.info("token:" + token + "  channel:" + channel + "  channelNo:" + channelNo);
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        }
        Map<String, Object> returnmap = new HashMap<String, Object>();
        //缓存数据获取
        Map<String, Object> cacheMap = session.get(token, Map.class);
        if (cacheMap == null || "".equals(cacheMap)) {
            logger.info("Jedis数据获取失败");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }

        String totalamount = cacheMap.get("totalamount").toString();
        String applseq = (String) cacheMap.get("applSeq");
        String idNo = (String) cacheMap.get("idCard");//身份证

        String apiKey = moxie_apikey;
        String userId = idNo + applseq;

        Map resultmap = new HashMap();
        resultmap.put("userId", userId);
        resultmap.put("apiKey", apiKey);

        return success(resultmap);
    }

    @Override
    public Map<String, Object> loanContract(Map<String, Object> map) {
        //1.基础信息获取
        String token = super.getToken();
        String channel = super.getChannel();
        String channelNo = super.getChannelNo();
        String loginNum = (String) map.get("loginNum");
        String loginEvent = (String) map.get("loginEvent");
        String lendNum = (String) map.get("lendNum");
        String lendEvent = (String) map.get("lendEvent");
        String verifyNo = (String) map.get("verifyNo");//验证码
        logger.info("百融风险信息：");
        logger.info("loginNum:" + loginNum + "  loginEvent:" + loginEvent + "  lendNum:" + lendNum + "  lendEvent:" + lendEvent);
        if(StringUtils.isEmpty(token) || StringUtils.isEmpty(channel) || StringUtils.isEmpty(channelNo) || StringUtils.isEmpty(verifyNo)){
            logger.info("前台传入参数有误");
            logger.info("token:" + token + "  channel:" + channel + "  channelNo:" + channelNo + "  verifyNo" + verifyNo);
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        }
        //缓存数据获取
        Map<String, Object> cacheMap = session.get(token, Map.class);
        if (cacheMap == null || "".equals(cacheMap)) {
            logger.info("Jedis数据获取失败");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }

        String applseq = (String) cacheMap.get("applSeq");
        String name = (String) cacheMap.get("name");
        String idNo = (String) cacheMap.get("idCard");//身份证
        String typCde = (String) cacheMap.get("typCde");
        String custNo = (String) cacheMap.get("custNo");
        String phone = (String) cacheMap.get("phoneNo");
        String callbackUrl = (String) cacheMap.get("callbackUrl");

        //短信验证码校验
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("phone", phone);
        paramMap.put("verifyNo", verifyNo);
        paramMap.put("channel", channel);
        paramMap.put("channelNo", channelNo);
        Map<String, Object> stringObjectMap = appServerService.smsVerify(token, paramMap);
        if (stringObjectMap == null) {
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        }
        Map resultmapjsonMap = (Map<String, Object>) stringObjectMap.get("head");
        String resultmapFlag = (String) resultmapjsonMap.get("retFlag");
        if (!"00000".equals(resultmapFlag)) {
            return fail(ConstUtil.ERROR_CODE, "短信验证码校验失败");
        }
        Map<String, Object> returnmap = new HashMap<String, Object>();


        //2.图片上传
        Map<String, Object> uploadimgmap = new HashMap<String, Object>();
        uploadimgmap.put("custNo", custNo);//客户编号
        uploadimgmap.put("applSeq", applseq);//订单号
        uploadimgmap.put("channel", channel);
        uploadimgmap.put("channelNo", channelNo);
        Map<String,Object> uploadimgresultmap = appServerService.uploadImg2CreditDep(token, uploadimgmap);
        if(!HttpUtil.isSuccess(uploadimgresultmap)){
            logger.info("订单提交，影像上传失败失败");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        }
        logger.info("订单提交，影像上传成功");



        //3.个人借款合同

        //获取贷款品种小类
        Map<String, Object> loanparamMap = new HashMap<String, Object>();
        loanparamMap.put("typCdeList", typCde);
        Map<String, Object> loanmap = appServerService.pLoanTypList(token, loanparamMap);
        if(!HttpUtil.isSuccess(loanmap)){
            return loanmap;
        }
        List<Map<String, Object>> loanbody = (List<Map<String, Object>>) loanmap.get("body");
        String typLevelTwo = "";
        for (int i = 0; i < loanbody.size(); i++) {
            Map<String, Object> m = loanbody.get(i);
            typLevelTwo = m.get("levelTwo").toString();
        }

        JSONObject order = new JSONObject();
        order.put("custName", name);// 客户姓名
        order.put("idNo", idNo);// 客户身份证号
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
        Map contractmap = new HashMap();//
        contractmap.put("custName", name);// 客户姓名
        contractmap.put("custIdCode", idNo);// 客户身份证号
        contractmap.put("applseq", applseq);// 请求流水号
        contractmap.put("signType", signType);// 签章类型
//        if("17057a".equals(typCde)){//不同的贷款品种对应不同的签章类型
//            contractmap.put("signType", "DOUZIPERSONAL");// 签章类型
//        }else if("17105a".equals(typCde)){
//            contractmap.put("signType", "DOUZIBUSINESS");// 签章类型
//        }
        contractmap.put("flag", "0");//1 代表合同  0 代表 协议
        contractmap.put("orderJson", orderJson.toString());
        contractmap.put("sysFlag", "11");// 系统标识：支付平台
        contractmap.put("channelNo", channelNo);
        Map camap = appServerService.caRequest(null, contractmap);

        //4.征信借款合同
        JSONObject orderZX = new JSONObject();
        orderZX.put("custName", name);// 客户姓名
        orderZX.put("idNo", idNo);// 客户身份证号
        orderZX.put("indivMobile", phone);// 客户手机号码
        orderZX.put("applseq", applseq);// 请求流水号

        JSONObject orderZXJson = new JSONObject();// 订单信息json串
        orderZXJson.put("order", orderZX.toString());

        Map reqZXJson = new HashMap();// 征信
        reqZXJson.put("custName", name);// 客户姓名
        reqZXJson.put("custIdCode", idNo);// 客户身份证号
        reqZXJson.put("applseq", applseq);// 请求流水号
        reqZXJson.put("signType", "credit");// 签章类型
        reqZXJson.put("flag", "0");//1 代表合同  0 代表 协议
        reqZXJson.put("orderJson", orderZXJson.toString());
        reqZXJson.put("sysFlag", "11");// 系统标识：支付平台
        reqZXJson.put("channelNo", channelNo);
        Map zxmap = appServerService.caRequest(token, reqZXJson);



        //5.注册合同
        JSONObject orderRegister = new JSONObject();
        orderRegister.put("custName", name);// 客户姓名
        orderRegister.put("idNo", idNo);// 客户身份证号
        orderRegister.put("indivMobile", phone);// 客户手机号码
        orderRegister.put("applseq", applseq);// 请求流水号

        JSONObject orderZCJson = new JSONObject();// 订单信息json串
        orderZCJson.put("order", orderRegister.toString());

        Map reqZCJson = new HashMap();// 征信
        reqZCJson.put("custName", name);// 客户姓名
        reqZCJson.put("custIdCode", idNo);// 客户身份证号
        reqZCJson.put("applseq", applseq);// 请求流水号
        reqZCJson.put("signType", "register");// 签章类型
        reqZCJson.put("flag", "0");//1 代表合同  0 代表 协议
        reqZCJson.put("orderJson", orderZCJson.toString());
        reqZCJson.put("sysFlag", "11");// 系统标识：支付平台
        reqZCJson.put("channelNo", channelNo);
        Map zcmap = appServerService.caRequest(token, reqZCJson);

        //6.百融风险信息推送
        logger.info("百融登录事件：loginEvent："+loginEvent+"********loginEventNum:"+loginNum + "  lendEvent" + lendEvent + "  lendNum:" + lendNum);
        if(!StringUtils.isEmpty(loginNum) && !StringUtils.isEmpty(lendNum)){
            Map<String, Object> riskmap = new HashMap<String, Object>();
            List<List<Map<String, String>>> content = new ArrayList<>();
            List<Map<String, String>> contentList = new ArrayList<>();

            //登录事件
            Map<String, String> mapLoginEvent = new HashMap();
            mapLoginEvent.put("content", EncryptUtil.simpleEncrypt(loginNum));
            mapLoginEvent.put("reserved6", applseq);
            mapLoginEvent.put("reserved7", "antifraud_login");
            contentList.add(mapLoginEvent);
            //借款事件
            Map<String, String> mapLendEvent = new HashMap();
            mapLendEvent.put("content", EncryptUtil.simpleEncrypt(lendNum));
            mapLendEvent.put("reserved6", applseq);
            mapLendEvent.put("reserved7", "antifraud_lend");
            contentList.add(mapLendEvent);

            content.add(contentList);
            riskmap.put("content", content);
            riskmap.put("reserved7", "antifraud_lend");

            riskmap.put("applSeq", applseq);

            riskmap.put("idNo", EncryptUtil.simpleEncrypt(idNo));
            riskmap.put("name", EncryptUtil.simpleEncrypt(name));
            riskmap.put("mobile", EncryptUtil.simpleEncrypt(phone));
            riskmap.put("dataTyp", "A501");
            riskmap.put("source", "2");
            riskmap.put("token", token);
            riskmap.put("channel", channel);
            riskmap.put("channelNo", channelNo);
            appServerService.updateRiskInfo("", riskmap);
        }


        //7.接口回调
        //String callbackUrl3 = "";
        String backurl = callbackUrl + "?applseq=" + applseq;
        logger.info("乔融豆子*******签章回调地址：" + backurl);
        String resData = HttpClient.sendGetUrl(backurl);
        logger.info("乔融豆子*******签章回调接口返回数据："+resData);
//        if(resData == null || "".equals(resData)){
//            return fail("18", "回调接口调用失败");
//        }

        //8.订单提交
        // 调信贷贷款申请接口.
        HashMap<String, Object> mapSubmit = new HashMap<>();
        mapSubmit.put("applSeq", applseq);
        mapSubmit.put("flag", "1");//0：贷款取消  1:申请提交   2：合同提交
        mapSubmit.put("sysFlag", "11");
        mapSubmit.put("channel", channelNo);//渠道编码
        Map<String, Object> responseMap = CmisUtil.getCmisResponse(CmisTradeCode.TRADECODE_DK_CANCEL, null, mapSubmit);
        logger.info("信贷100026提交接口返回" + responseMap);
        if (responseMap == null) {
            logger.info("贷款提交失败,信贷系统贷款提交返回信息为空");
            return fail(ConstUtil.ERROR_CODE, "贷款提交失败");
        }
        Map<String, Object> response = (Map<String, Object>) responseMap.get("response");

        return response;
    }


    /**
     * 根据流水号获取贷款信息
     * @param applSeq
     * @return
     */
    public Map<String, Object> getAppOrderMapFromCmis(String applSeq) {
        /** 从核心数据库查询贷款详情 **/
        String url = EurekaServer.CMISPROXY + "/api/appl/queryApplFull?applSeq=" + applSeq;
        String json = HttpUtil.restGet(url, super.getToken());
        logger.info("通过cmis请求的订单信息的url" + url);
        logger.info("通过cmis获得的订单信息：" + json);
        if (StringUtils.isEmpty(json)) {
            return null;
        }
        Map<String, Object> resultMap = HttpUtil.json2Map(json);
        return resultMap;
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
        //String params = new String(DesUtil.decrypt(Base64Utils.decode(data), new String(RSAUtils.decryptByPublicKey(Base64Utils.decode(key), publicKey))));
        //String params = new String(RSAUtils.decryptByPublicKey(Base64Utils.decode(data), publicKey));
        //String params = new String(DesUtil.decrypt(Base64Utils.decode(data), new String(RSAUtils.decryptByPublicKey(Base64Utils.decode(key), publicKey))));

        return params;
    }
}
