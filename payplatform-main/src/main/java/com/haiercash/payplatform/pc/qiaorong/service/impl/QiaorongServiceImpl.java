package com.haiercash.payplatform.pc.qiaorong.service.impl;

import com.haiercash.commons.redis.Session;
import com.haiercash.commons.util.EncryptUtil;
import com.haiercash.payplatform.config.EurekaServer;
import com.haiercash.payplatform.pc.qiaorong.service.QiaorongService;
import com.haiercash.payplatform.service.AppServerService;
import com.haiercash.payplatform.service.BaseService;
import com.haiercash.payplatform.service.CmisApplService;
import com.haiercash.payplatform.utils.ConstUtil;
import com.haiercash.payplatform.utils.HttpUtil;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;

/**
 * Created by yuanli on 2017/9/25.
 */
@Service
public class QiaorongServiceImpl extends BaseService implements QiaorongService {

    @Autowired
    private Session session;
    @Autowired
    private CmisApplService cmisApplService;
    @Autowired
    private AppServerService appServerService;

    private static int limitAmount = 30000;
    /*
    合同初始化
     */
    @Override
    public Map<String, Object> contractInit(Map<String, Object> map) {
        logger.info("合同展示页面初始化*******开始");
        String applseq = (String) map.get("applseq");
        Map<String, Object> cachemap = new HashMap<String, Object>();
        logger.info("申请流水号：" + applseq);
        if(StringUtils.isEmpty(applseq)){
            logger.info("申请流水号为空");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
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

        String uuid = UUID.randomUUID().toString().replace("-", "");
        resultMap.put("token", uuid);//token


        cachemap.put("name", appOrderMapCmis.get("CUST_NAME"));
        cachemap.put("phone", appOrderMapCmis.get("INDIV_MOBILE"));
        cachemap.put("idNo", appOrderMapCmis.get("ID_NO"));
        cachemap.put("applseq", applseq);
        cachemap.put("totalamount", appOrderMapCmis.get("APPLY_AMT"));
        cachemap.put("typCde", typCde);//
        session.set(uuid, cachemap);


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
        String phone = (String) cacheMap.get("phone");
        String idNo = (String) cacheMap.get("idNo");//身份证
        String cardnumber = (String) cacheMap.get("cardnumber");//银行卡
        String totalamount = cacheMap.get("totalamount").toString();
        String applseq = (String) cacheMap.get("applseq");
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
        identityMap.put("userId", phone); //客户userId
//        identityMap.put("acctProvince", acctProvince); //开户行省代码
//        identityMap.put("acctCity", acctCity); //开户行市代码
        Map<String, Object> identityresultmap = appServerService.fCiCustRealThreeInfo(token, identityMap);
        Map identityheadjson = (Map<String, Object>) identityresultmap.get("head");
        String identityretFlag = (String) identityheadjson.get("retFlag");
        if (!"00000".equals(identityretFlag)) {
            String retMsg = (String) identityheadjson.get("retMsg");
            return fail(ConstUtil.ERROR_CODE, retMsg);
        }
        Map identitybodyjson = (HashMap<String, Object>) identityresultmap.get("body");
        //信息保存
        String custNo = (String) identitybodyjson.get("custNo");

        //2.判断是否已注册
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("channelNo",channelNo);
        paramMap.put("channel",channel);
        paramMap.put("mobile", EncryptUtil.simpleEncrypt(phone));
        Map<String, Object> registerMap = appServerService.isRegister(token, paramMap);
        if(registerMap == null){
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        }
        Map resultmapjsonMap = (Map<String, Object>) registerMap.get("head");
        String resultmapFlag = (String) resultmapjsonMap.get("retFlag");
        if(!"00000".equals(resultmapFlag)){
            String retMsg = (String) resultmapjsonMap.get("retMsg");
            return fail(ConstUtil.ERROR_CODE, retMsg);
        }
        Map resultmapbodyMap = (Map<String, Object>) registerMap.get("body");
        String isRegister = (String)resultmapbodyMap.get("isRegister");
        if("N".equals(isRegister)){
            returnmap.put("flag","01");
            return success(returnmap);//跳转注册页面
        }
        if(!"Y".equals(isRegister)){
            return fail("01", "手机已被注册！请联系客服修改。客服电话：400777");
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
        JSONObject headjson = new JSONObject(resultmap.get("head"));
        String retFlag = headjson.getString("retFlag");
        String retMsg = headjson.getString("retMsg");
        if(!"00000".equals(retFlag)){
            return fail(ConstUtil.ERROR_CODE, retMsg);
        }
        JSONObject body = new JSONObject(resultmap.get("body"));
        String code = body.getString("code"); //结果标识码
        if("00".equals(code)){//人脸识别通过
            logger.info("已经通过了人脸识别（得分合格），不需要再做人脸识别");
            double amount = Double.parseDouble(totalamount);

            Map<String, Object> moxiemap = new HashMap<String, Object>();
            moxiemap.put("applseq", applseq);
            Map<String, Object> mapmoxie = appServerService.getMoxieByApplseq(token, moxiemap);
            JSONObject headjsonmoxie = new JSONObject(mapmoxie.get("head"));
            String retFlagmoxie = headjsonmoxie.getString("retFlag");
            String retMsgmoxie = headjsonmoxie.getString("retMsg");
            if(!"00000".equals(retFlagmoxie)){
                return fail(ConstUtil.ERROR_CODE, retMsgmoxie);
            }
            JSONObject bodymoxie = new JSONObject(mapmoxie.get("body"));
            String isFundFlag = (String) bodymoxie.get("isFund");
            String isBankFlag = (String) bodymoxie.get("isBank");
            String isCarrierFlag = (String) bodymoxie.get("isCarrier");

            //判断金额是否需要做魔蝎
            if(amount >= limitAmount){
                //判断是否做过公积金网银
                if("Y".equals(isFundFlag) || "Y".equals(isBankFlag)){//已做过公积金、网银认证  跳转合同展示
                    logger.info("已经通过了人脸识别（得分合格），跳转合同展示");
                    returnmap.put("flag", "05");
                    return success(returnmap);//跳转合同展示页面
                }else{//未做过公积金、网银认证  跳转魔蝎认证页面
                    logger.info("已经通过了人脸识别（得分合格），跳转魔蝎");
                    returnmap.put("flag", "04");
                    return success(returnmap);//跳转魔蝎页面
                }
            }else{
                logger.info("已经通过了人脸识别（得分合格），跳转合同展示");
                returnmap.put("flag", "05");
                return success(returnmap);//跳转合同展示页面
            }

        }else if("01".equals(code)){//01：未通过人脸识别，剩余次数为0，不能再做人脸识别，录单终止
            //终止
            logger.info("未通过人脸识别，剩余次数为0，不能再做人脸识别，录单终止");
            return fail(ConstUtil.ERROR_CODE, "不能再做人脸识别，录单终止!");
        }else if("02".equals(code)){//02：未通过人脸识别，剩余次数为0，不能再做人脸识别，但可以上传替代影像
            //跳转替代影像
            logger.info("未通过人脸识别，剩余次数为0，不能再做人脸识别，但可以上传替代影像");
            returnmap.put("flag", "03");// 手持身份证
            return success(map);
        }else if("10".equals(code)){//10：未通过人脸识别，可以再做人脸识别
            //可以做人脸识别
            logger.info("未通过人脸识别，可以再做人脸识别");
            returnmap.put("flag", "02");// 人脸识别
            return success(map);
        }

        return null;
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

}
