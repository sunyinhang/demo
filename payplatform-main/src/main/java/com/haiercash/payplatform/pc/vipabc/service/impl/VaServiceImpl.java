package com.haiercash.payplatform.pc.vipabc.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.haiercash.core.collection.MapUtils;
import com.haiercash.core.lang.Base64Utils;
import com.haiercash.core.lang.Environment;
import com.haiercash.core.lang.StringUtils;
import com.haiercash.payplatform.common.dao.VipAbcOrderDao;
import com.haiercash.payplatform.pc.vipabc.service.VaService;
import com.haiercash.payplatform.service.AppServerService;
import com.haiercash.payplatform.utils.DesUtilvip;
import com.haiercash.payplatform.utils.EncryptUtil;
import com.haiercash.payplatform.utils.RSAUtils;
import com.haiercash.spring.service.BaseService;
import com.haiercash.spring.util.ConstUtil;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


/**
 * Created by 赵先鲁 on 2018/2/27.
 */
@Service
public class VaServiceImpl extends BaseService implements VaService {

    @Autowired
    private VipAbcOrderDao vipAbcOrderDao;
    @Autowired
    private AppServerService appServerService;

    @Override
    public Map<String, Object> queryAppLoanAndGood(String token, String channel, String channelNo, Map<String, Object> paramsMap) throws Exception {
        logger.info("VIPABC获取第三方数据开始");
        String applyNo = (String) paramsMap.get("applyNo");
        String tradeCode = (String) paramsMap.get("tradeCode");
        String password_ = (String) paramsMap.get("key");
        String channleNo = paramsMap.get("channelNo") + "";
        String jsonStr = (String) paramsMap.get("data");
        logger.info("----------------接口请求数据：-----------------");
        logger.info(
                "applyNo:" + applyNo + "||tradeCode:" + tradeCode + "||channleNo:" + channleNo + "||json:" + jsonStr);
        logger.info("----------------接口请求数据：-----------------");
        if (StringUtils.isEmpty(jsonStr)) {
            logger.info("第三方发送的请求报文信息不能为空！！！");
            return fail(ConstUtil.ERROR_CODE, "请确认发送的报文信息是否符合条件！");
        }
        String privatekey = "MIIBVQIBADANBgkqhkiG9w0BAQEFAASCAT8wggE7AgEAAkEAokf1Ipb9k0mOWXRkpOzF2w8H6v4SMNLxc/93YSaCdytHzFVtySn2KSy4czRyTDluTzg/7lgfaYh/xwNGgFInKQIDAQABAkEAiQL/8KhSh5w+1z/yCVzu37idEsZYTWiL+0fhXuDInhtLmU7Tmt6VQwn4rq7tmrYIb+Q15E6jtIbyHU3rszVP0QIhAPDC56TTK+knob2fzBPtN9+VSi8bBok5Y4wO84EWqot3AiEArI1snWhHOYspGbELAI3ieHj83Nag/3TrrkgUK8kISl8CIBcsa2dt+/gBHIxH6TixyIL4t585FrP2liJQ/hcau2eZAiAnJgsPh3opZxZTGuTpIkfQl3qfTB7I9qkGKJpS+NBltwIhAJobifVL7NAsWnyTq2jvT+boQttAGZGO87U/lRPXL1PS";
        //           String privatekey =DataParams.privatekey;
        byte[] decode = Base64Utils.decode(password_);
        byte[] decryptByPublicKey = RSAUtils.decryptByPrivateKey(decode, privatekey);
        byte[] decrypt = DesUtilvip.decrypt(Base64Utils.decode(jsonStr), new String(decryptByPublicKey));
        String params = new String(decrypt);
        JSONObject fromObject = JSONObject.parseObject(params);
        logger.info("解析的第三方的数据为：" + fromObject);

        String orderSn = fromObject.getString("orderNo");//商城订单号
//        orderSn="5021356600008598600000553150";
        String applSeq = vipAbcOrderDao.queryvipabcapplSeq(orderSn);
//        String applSeq="1709844";
        logger.info("获取的applseq是：" + applSeq);
        Map<String, Object> map = new HashMap<>();
        map.put("channelNo", "53");
        map.put("channel", "11");
        map.put("applSeq", applSeq);
        map.put("token", "");
        Map<String, Object> queryApplmap = appServerService.queryApplLoanDetail(token, map);
        logger.info("查询贷款详情接口，响应数据：" + map);
        if (MapUtils.isEmpty(queryApplmap)) {//response
            logger.info("网络异常,查询贷款详情接口,响应数据为空！" + map);
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        Map<String, Object> headMap = (Map<String, Object>) queryApplmap.get("head");
        String retFlag2 = (String) headMap.get("retFlag");
        if (!"0000".equals(retFlag2)) {
            String retMsg2 = (String) headMap.get("retMsg");
            return fail(retFlag2, retMsg2);
        }

        Map<String, Object> map1 = (Map<String, Object>) queryApplmap.get("body");
        logger.info("查询贷款详情接口，响应数据：" + map1.toString());
        String contNo = (String) map1.get("contNo");//合同号
        String loanNo = (String) map1.get("loanNo");//借据号
        String applyAmt = (String) map1.get("applyAmt");//申请金额
        String apprvAmt = (String) map1.get("apprvAmt");//核准金额
        String repayAccBankName = (String) map1.get("repayAccBankName");//还款账号户名
        String repayApplCardNo = (String) map1.get("repayApplCardNo");//还款卡号
        String repayAccBankCde = (String) map1.get("repayAccBankCde");//还款开户银行代码
        String repayAcProvince = (String) map1.get("repayAcProvince");//还款账户所在省
        String repayAcCity = (String) map1.get("repayAcCity");//还款账户所在市
        String applyDt = (String) map1.get("applyDt");//申请注册日期
        String outSts = (String) map1.get("outSts");//贷款状态
        String appOutAdvice = (String) map1.get("appOutAdvice");//退回原因

//			HashMap<String, String> mapone = new HashMap<>();
        JSONObject mapone = new JSONObject();
//			HashMap<String, Object> hashMap = new HashMap<>();
        mapone.put("applSeq", applSeq);
        mapone.put("contNo", contNo);
        mapone.put("loanNo", loanNo);
        mapone.put("applyAmt", applyAmt);
        mapone.put("apprvAmt", apprvAmt);
        mapone.put("repayApplAcNam", repayAccBankName);
        mapone.put("repayApplCardNo", repayApplCardNo);
        mapone.put("repayAccBankCde", repayAccBankCde);
        mapone.put("repayAcProvince", repayAcProvince);
        mapone.put("repayAcCity", repayAcCity);
        mapone.put("applyDt", applyDt);
        String outSts_end = "";
        if ("04".equals(outSts) || "05".equals(outSts) || "06".equals(outSts) || "23".equals(outSts) || "24".equals(outSts)) {
            outSts_end = "02";
        } else if ("02".equals(outSts)) {//审批拒绝
            outSts_end = "03";
        } else if ("03".equals(outSts)) {//贷款取消
            outSts_end = "04";
        } else if ("22".equals(outSts)) {//审批退回
            outSts_end = "06";
        }
        if ("".equals(outSts_end)) {
            outSts_end = outSts;
        }

        mapone.put("outSts", outSts_end);
        mapone.put("demo", appOutAdvice);
        logger.info("VIP推送给第三方请求数据加密前：" + mapone);
        String reqData = mapone.toString();
        String productKey = EncryptUtil.productKey();
        String key = productKey;
        String iv = productKey;
        String publicKey = "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAKJH9SKW/ZNJjll0ZKTsxdsPB+r+EjDS8XP/d2EmgncrR8xVbckp9iksuHM0ckw5bk84P+5YH2mIf8cDRoBSJykCAwEAAQ==";
        //               String publicKey=DataParams.publicKey;
        String desData = EncryptUtil.DesEncrypt(reqData, key, iv);
        //3.加密des的key
        password_ = Base64Utils.encode(RSAUtils.encryptByPublicKey((productKey + productKey).getBytes(), publicKey));
        Map<String, Object> reqjson = new HashMap<>();
        reqjson.put("applyNo", StringUtils.remove(UUID.randomUUID().toString(), Environment.MinusChar));
        reqjson.put("channelNo", channelNo);
        reqjson.put("tradeCode", "vipabc-10002");
        reqjson.put("data", desData);
        reqjson.put("key", password_);
        logger.info("vipabc主动查询接口返回的报文是：" + reqjson);
        return success(reqjson);

    }

    /**
     * 删除订单
     *
     * @param token
     * @param channel
     * @param channelNo
     * @param map
     * @return
     */
    @Override
    public Map<String, Object> vipAbcDeleteOrderInfo(HttpServletRequest request, String token, String channel, String channelNo, Map<String, Object> map) throws Exception {
        logger.info("删除订单接口，开始");
        String retflag = "";
        String retmsg = "";
        String applyNo = "";
        String tradeCode = "";
        String password_ = "";
        String orderNo = "";

        // 1.获取JSON数据
        StringBuffer jsonStrBuff = new StringBuffer();
        String line = null;

        BufferedReader jsonreader = request.getReader();
        while ((line = jsonreader.readLine()) != null) {
            jsonStrBuff.append(line);
        }
        if (!(jsonStrBuff.toString().length() > 0)) {
            logger.info("第三方发送的报文信息不能为空！！！");
            return fail(ConstUtil.ERROR_CODE, "请确认发送的报文信息是否符合条件！");
        }
//						String json_ = URLDecoder.decode(jsonStrBuff.toString(), "UTF-8");
        String json_ = URLEncoder.encode(jsonStrBuff.toString(), "UTF-8");
        json_ = URLDecoder.decode(json_, "UTF-8");

        logger.info("---------------------HaiercashPayApplyForJson-------------------------:");
        logger.info("*********HaiercashPayApplyForJson request***************");
        logger.info(json_.toString());
        logger.info("*********HaiercashPayApplyForJson request***************");

        JSONObject fromObject3 = JSONObject.parseObject(json_);
        applyNo = (String) fromObject3.get("applyNo");
        String jsonStr = (String) fromObject3.get("data");
        password_ = (String) fromObject3.get("key");
        tradeCode = (String) fromObject3.get("tradeCode");


        //			password_="O3woqjqmU8PVx3y8FwVu3QF4bAgq77PKs0h+874ayYnn3NYe+Ny65R7lwOQJOKLV7r0wTmSiZVMixWOjQx1GVg==";
//						jsonStr="imb4E25L4GlVoA5zl+qqGDSVNasaHjsZIa3FGZ3+D78I+n+3Boj/7bqaNzB/FdIiY1a/L7BTlNhNSJr2fglKhIVVAxnR8mleaiVrD8jNADEe+K4BoHvWABIrC/lvorzDL1rjVPHTGRCKA//7S8PkwjMDjftq8c3YWiZg0Qs0ueaX+cJBmH+hJCwRVRrxSPKAoUnMWeFDeaBLTL+lcAixAWM/GREbN5mHcuIfWtb/ECb+PZArKXdMqVcEXhNo3QNX5skXVVTjK9IwxgCynnXhz+DuNAwqwcJeuiFvnWzXfYbHvK0Sy0YKTgu4k0HkCGGOjO2CNuDFixJS+UUxexg0i0b8RxPbfYXv4yBUGJ5YtGtFcO/8JhPKizVHvLDjBRCr";
        logger.info("----------------接口请求数据：-----------------");
        logger.info(
                "applyNo:" + applyNo + "||tradeCode:" + tradeCode + "||channelNo:" + channelNo + "||json:" + jsonStr);
        logger.info("----------------接口请求数据：-----------------");
        if (jsonStr == null || "".equals(jsonStr)) {
            logger.info("第三方发送的请求报文信息不能为空！！！");
            return fail(ConstUtil.ERROR_CODE, "请确认发送的报文信息是否符合条件！");
        }
//				    String publicKey = "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAKJH9SKW/ZNJjll0ZKTsxdsPB+r+EjDS8XP/d2EmgncrR8xVbckp9iksuHM0ckw5bk84P+5YH2mIf8cDRoBSJykCAwEAAQ==";
//					jsonStr=new String(RSAUtils.decryptByPublicKey(Base64Utils.decode(jsonStr),publicKey));
        String privatekey = "MIIBVQIBADANBgkqhkiG9w0BAQEFAASCAT8wggE7AgEAAkEAokf1Ipb9k0mOWXRkpOzF2w8H6v4SMNLxc/93YSaCdytHzFVtySn2KSy4czRyTDluTzg/7lgfaYh/xwNGgFInKQIDAQABAkEAiQL/8KhSh5w+1z/yCVzu37idEsZYTWiL+0fhXuDInhtLmU7Tmt6VQwn4rq7tmrYIb+Q15E6jtIbyHU3rszVP0QIhAPDC56TTK+knob2fzBPtN9+VSi8bBok5Y4wO84EWqot3AiEArI1snWhHOYspGbELAI3ieHj83Nag/3TrrkgUK8kISl8CIBcsa2dt+/gBHIxH6TixyIL4t585FrP2liJQ/hcau2eZAiAnJgsPh3opZxZTGuTpIkfQl3qfTB7I9qkGKJpS+NBltwIhAJobifVL7NAsWnyTq2jvT+boQttAGZGO87U/lRPXL1PS";
//            String privatekey =DataParams.privatekey;
        byte[] decode = Base64Utils.decode(password_);
        byte[] decryptByPublicKey = RSAUtils.decryptByPrivateKey(decode, privatekey);
        byte[] decrypt = DesUtilvip.decrypt(Base64Utils.decode(jsonStr), new String(decryptByPublicKey));
        String params = new String(decrypt);
        JSONObject fromObject = JSONObject.parseObject(params);
        logger.info("解析的第三方的数据为：" + fromObject);

        orderNo = (String) fromObject.get("orderNo");//第三方订单号orderNo
        orderNo = vipAbcOrderDao.queryviporderno(orderNo);

        if (orderNo == null || "".equals(orderNo)) {
            retflag = "100001";
            logger.info("订单号不能为空");
            return fail(retflag, "订单号不能为空");
        }

        Map<String, Object> req = new HashMap<>();
        req.put("channelNo", channelNo);
        req.put("channel", channel);
        req.put("orderNo", orderNo);
        logger.info("删除订单接口，请求数据：" + req.toString());
        Map<String, Object> res = appServerService.deleteAppOrder(token, req);//删除订单接口
        logger.info("删除订单接口，响应数据：" + res);
        if (res == null || res.isEmpty()) {
            retflag = "100002";
            logger.info("网络异常，app后台,删除订单接口,响应数据为空！");
            return fail(retflag, "网络异常，app后台,删除订单接口,响应数据为空！");
        }
        Map<String, Object> headMap = (Map<String, Object>) res.get("head");
        String retFlag = (String) headMap.get("retFlag");
        retmsg = (String) headMap.get("retMsg");
        if (retFlag.equals("00000")) {
            retflag = "00000";
            Map<String, Object> bodyMap = (Map<String, Object>) res.get("body");
            String mString = bodyMap.get("msg") + "";
            logger.info("删除订单接口，结束");
            return fail(retflag, mString);

        } else {
            retflag = retFlag;
            return fail(retflag, retmsg);
        }
    }

}
