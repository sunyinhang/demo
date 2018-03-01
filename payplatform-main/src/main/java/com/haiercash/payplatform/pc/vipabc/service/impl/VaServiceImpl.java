package com.haiercash.payplatform.pc.vipabc.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.haiercash.core.collection.MapUtils;
import com.haiercash.core.lang.Base64Utils;
import com.haiercash.core.lang.Environment;
import com.haiercash.core.lang.StringUtils;
import com.haiercash.payplatform.common.dao.VipAbcOrderDao;
import com.haiercash.payplatform.config.StorageConfig;
import com.haiercash.payplatform.pc.vipabc.service.VaService;
import com.haiercash.payplatform.service.AppServerService;
import com.haiercash.payplatform.utils.DesUtilvip;
import com.haiercash.payplatform.utils.EncryptUtil;
import com.haiercash.payplatform.utils.RSAUtils;
import com.haiercash.spring.redis.RedisUtils;
import com.haiercash.spring.rest.client.JsonClientUtils;
import com.haiercash.spring.service.BaseService;
import com.haiercash.spring.util.ConstUtil;
import com.haiercash.spring.weixin.WeiXinUtils;
import com.haiercash.spring.weixin.entity.WeiXinToken;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.haiercash.payplatform.service.impl.CustExtInfoServiceImpl.createDir;


/**
 * Created by 赵先鲁 on 2018/2/27.
 */
@Service
public class VaServiceImpl extends BaseService implements VaService {

    @Autowired
    private VipAbcOrderDao vipAbcOrderDao;
    @Autowired
    private AppServerService appServerService;
    @Autowired
    private StorageConfig storageConfig;

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
        String sKey = key;
        String desData = EncryptUtil.DesEncrypt(reqData, sKey, iv);
        //3.加密des的key
        password_ = Base64Utils.encode(RSAUtils.encryptByPublicKey((productKey + productKey).getBytes(), publicKey));
        JSONObject reqjson = new JSONObject();
        reqjson.put("applyNo", StringUtils.remove(UUID.randomUUID().toString(), Environment.MinusChar));
        reqjson.put("channelNo", "53");
        reqjson.put("tradeCode", "vipabc-10002");
        reqjson.put("data", desData);
        reqjson.put("key", password_);
        logger.info("vipabc主动查询接口返回的报文是：" + reqjson);
        return success(reqjson);

    }

    @Override
    public Map<String, Object> weixinuploadOtherPerson(String token, String channel, String channelNo, Map<String, Object> map) throws IOException {
        logger.info("VIPABC上传学生证开始");
        String retFlag = null;
        Map<String, Object> redisMap = null;
        String message = null;
        Map<String, Object> attachMap = new HashMap<String, Object>(); // 文件上传map(收款确认单)

        String media_id = (String) map.get("media_id");
        logger.info("vipabc上传学生证media_id参数为：" + media_id);
        WeiXinToken wxToken = WeiXinUtils.getCachedToken();
        String access_token = wxToken.getAccess_token();
        logger.info("vipabc上传学生证access_token参数为：" + access_token);
        String wxurl = "https://api.weixin.qq.com/cgi-bin/media/get";
//            String wxurl=DataParams.wxurl;//微信地址
        Map<String, Object> params = new HashMap<>();
        params.put("access_token", access_token);
        params.put("media_id", media_id);
        logger.info("请求的下载VIPABC上传学生证的参数为：" + params);
        byte[] img = JsonClientUtils.getForObject(wxurl, byte[].class, params);
        logger.info("返回的结果为" + img);
        InputStream in = new ByteArrayInputStream(img);
        if (StringUtils.isEmpty(token)) {
            logger.info("VIPABC,上传学生证,token为空！");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        //
        redisMap = RedisUtils.getExpireMap(token);
        if (redisMap == null || redisMap.isEmpty()) {
            logger.info("分期详情加载，redis缓存数据获取失败");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        String custNo = redisMap.get("custNo").toString();
        String applSeq = redisMap.get("applSeq").toString();
        // 查询是否已有收款确认单，无则删除
        Map<String, Object> paramYXMap = new HashMap<>();
        paramYXMap.put("attachType", "DOC51");
        paramYXMap.put("custNo", custNo);
        paramYXMap.put("channel", channel);
        paramYXMap.put("channelNo", channelNo);
        Map<String, Object> result = appServerService.attachTypeSearchPerson(token, paramYXMap);
        if (result == null || result.isEmpty()) {
            logger.info("查询影像信息，接口返回为空");
            return fail(ConstUtil.ERROR_CODE, "查询影像信息，接口返回为空");
        }
        String retflag = ((Map) result.get("head")).get("retFlag").toString();
        if (!"00000".equals(retflag)) {
            String msg = ((Map) result.get("head")).get("retMsg").toString();
            logger.info(msg);
            return fail(retflag, msg);
        }
        // 删除
        List list = new ArrayList();
        List<Map<String, Object>> listBody = ((List<Map<String, Object>>) result.get("body"));
        // [{"attachType":"App01","applSeq":null,"count":1,"attachName":"本人手持身份证与本人面部合照照片","id":23960,"md5":"6807451f211355faa3d3ca05f20308e1"},{"attachType":"DOC014","applSeq":null,"count":19,"attachName":"Face++返回影像","id":23881,"md5":"68600f94a35501042cff32d4dd2dedae"},{"attachType":"DOC045","applSeq":null,"count":12,"attachName":"收款确认单","id":23600,"md5":"c882e40833e92508f5b647fea5c06cd2"},{"attachType":"DOC065","applSeq":null,"count":1,"attachName":"人脸照片","id":23903,"md5":"68600f94a35501042cff32d4dd2dedae"}]
        for (int j = 0; j < listBody.size(); j++) {

            String attachType = listBody.get(j).get("attachType").toString();
            if ("DOC51".equals(attachType)) {
                String picId = listBody.get(j).get("id").toString();
                list.add(picId);
            }
        }
        if (list.size() > 0) {
            for (int j = 0; j < list.size(); j++) {
                Integer id = Integer.parseInt(list.get(j).toString());
                Map<String, Object> map1 = new HashMap<String, Object>();
                map1.put("id", id);
                map1.put("token", token);
                map1.put("channel", channel);
                map1.put("channelNo", channelNo);
                Map<String, Object> deleteResult = appServerService.attachDelete(token, map1);
                logger.info(deleteResult);
            }
        }

        //
        // 调用影像上传接口
        // 获取共享盘个人影像文件路径
        String path = "/testshare01/00/Data/haierData/";
        //String appno = UUID.randomUUID().toString().replace("-", "");
        String appno = StringUtils.remove(UUID.randomUUID().toString(), Environment.MinusChar);
        String dir = storageConfig.getFacePath() + File.separator + custNo + File.separator + ConstUtil.ATTACHTYPE_DOC065 + File.separator;
        createDir(dir);
        final String filePath = dir + appno + ".jpg";
        String md5ForUpload;
        try (FileInputStream inputStream = new FileInputStream(filePath)) {
            md5ForUpload = DigestUtils.md5Hex(inputStream);
        }
        attachMap.put("md5", md5ForUpload);
        attachMap.put("filePath", filePath);
        attachMap.put("applSeq", applSeq);
        attachMap.put("attachType", "DOC51");// 文件类型
        attachMap.put("attachName", "学生证");// 文件名称
        attachMap.put("custNo", custNo);
        attachMap.put("path", path);// //共享盘个人影像文件路径
        // 应该是配置到统一文件中
        attachMap.put("channel", channel);
        attachMap.put("channelNo", channelNo);
        attachMap.put("fileName", custNo);
        logger.info("path:" + path + "           fileName:" + custNo);
        Map<String, Object> attachresult = appServerService.attachUploadPersonByFilePath(token, attachMap);
        Map<String, Object> jsonObject2 = (Map<String, Object>) attachresult.get("head");
        retFlag = (String) jsonObject2.get("retFlag");
        message = (String) jsonObject2.get("retMsg");
        if (!"00000".equals(retFlag)) {// 文件上传成功返回 00000
            // //成功后从这里调用face++接口
            logger.info("vipabc,保存客户信息,调用app上传学生证失败！retMsg:" + message);
            return fail(retFlag, message);
        }
        retFlag = "0000";
        logger.info("VIPABC上传学生证结束");
        return fail(retFlag, message);
    }

    @Override
    public Map<String, Object> weixinuploadOtherPersonOther(String token, String channel, String channelNo, Map<String, Object> map) throws Exception {
        logger.info("VIPABC上传学生证封面开始");
        String retFlag = null;

        String message = null;
        Map<String, Object> attachMap = new HashMap<String, Object>(); // 文件上传map(收款确认单)
        String media_id = (String) map.get("media_id");
        logger.info("vipabc上传学生证封面media_id参数为：" + media_id);

        WeiXinToken wxToken = WeiXinUtils.getCachedToken();
        String access_token = wxToken.getAccess_token();
        logger.info("vipabc上传学生证封面access_token参数为：" + access_token);

        //           String wxurl=DataParams.wxurl;//微信地址
        String wxurl = "https://api.weixin.qq.com/cgi-bin/media/get";
        Map<String, Object> params = new HashMap<>();
        params.put("access_token", access_token);
        params.put("media_id", media_id);
        logger.info("请求的下载VIPABC上传学生证封面参数为：" + params);
        byte[] img = JsonClientUtils.getForObject(wxurl, byte[].class, params);
        logger.info("返回的结果为" + img);
        InputStream in = new ByteArrayInputStream(img);
        if (StringUtils.isEmpty(token)) {
            logger.info("VIPABC,上传学生证封面,token为空！");
            return fail(ConstUtil.ERROR_CODE, "VIPABC,上传学生证封面,token为空！");
        }

        Map<String, Object> redisMap = RedisUtils.getExpireMap(token);
        if (redisMap == null || redisMap.isEmpty()) {
            logger.info("VIPABC,上传学生证封面,获取redisMap为空！");
            return fail(ConstUtil.ERROR_CODE, "VIPABC,上传学生证封面,获取redisMap为空！");
        }
        String custNo = redisMap.get("custNo").toString();
        String applSeq = redisMap.get("applSeq").toString();
        // 查询是否已有收款确认单，无则删除
        Map<String, Object> paramMap = new HashedMap();
        paramMap.put("channel", channel);
        paramMap.put("channelNo", channelNo);
        paramMap.put("custNo", custNo);
        paramMap.put("attachType", "DOC086");
        Map<String, Object> result = appServerService.attachTypeSearchPerson(token, paramMap);
        if (result == null || result.isEmpty()) {
            logger.info("查询影像信息，接口返回为空");
            return fail(ConstUtil.ERROR_CODE, "查询影像信息，接口返回为空");
        }
        String retflag = ((Map<String, Object>) result.get("head")).get("retFlag").toString();
        if (!"00000".equals(retflag)) {
            String msg = ((Map<String, Object>) result.get("head")).get("retFlag").toString();
            logger.info(msg);
            return fail(ConstUtil.ERROR_CODE, msg);
        }
        // 删除
        List<String> list = new ArrayList();
        List<Map<String, Object>> listMap = (List<Map<String, Object>>) result.get("body");
        // [{"attachType":"App01","applSeq":null,"count":1,"attachName":"本人手持身份证与本人面部合照照片","id":23960,"md5":"6807451f211355faa3d3ca05f20308e1"},{"attachType":"DOC014","applSeq":null,"count":19,"attachName":"Face++返回影像","id":23881,"md5":"68600f94a35501042cff32d4dd2dedae"},{"attachType":"DOC045","applSeq":null,"count":12,"attachName":"收款确认单","id":23600,"md5":"c882e40833e92508f5b647fea5c06cd2"},{"attachType":"DOC065","applSeq":null,"count":1,"attachName":"人脸照片","id":23903,"md5":"68600f94a35501042cff32d4dd2dedae"}]
        for (Map<String, Object> m : listMap) {
            String attachType = m.get("attachType").toString();
            if ("DOC086".equals(attachType)) {
                String picId = m.get("id").toString();
                list.add(picId);
            }
        }
        if (list.size() > 0) {
            for (int j = 0; j < list.size(); j++) {
                Integer id = Integer.parseInt(list.get(j));
                Map<String, Object> map1 = new HashMap<String, Object>();
                map1.put("id", id);
                map1.put("token", token);
                map1.put("channel", channel);
                map1.put("channelNo", channelNo);
                Map<String, Object> deleteResult = appServerService.attachDelete(token, map1);
                logger.info(deleteResult);
            }
        }

        //
        // 调用影像上传接口
        // 获取共享盘个人影像文件路径
        String appno = StringUtils.remove(UUID.randomUUID().toString(), Environment.MinusChar);
        String dir = storageConfig.getFacePath() + File.separator + custNo + File.separator + ConstUtil.ATTACHTYPE_DOC065 + File.separator;
        createDir(dir);
        final String filePath = dir + appno + ".jpg";
        String md5ForUpload;
        try (FileInputStream inputStream = new FileInputStream(filePath)) {
            md5ForUpload = DigestUtils.md5Hex(inputStream);
        }
        attachMap.put("md5", md5ForUpload);
        attachMap.put("filePath", filePath);

        String path = "/testshare01/00/Data/haierData/";
        attachMap.put("applSeq", applSeq);
        attachMap.put("attachType", "DOC086");// 文件类型
        attachMap.put("attachName", "学生证封面");// 文件名称
        attachMap.put("path", path);// //共享盘个人影像文件路径
        // 应该是配置到统一文件中
        attachMap.put("channel", channel);
        attachMap.put("channelNo", channelNo);
        attachMap.put("custNo", custNo);//
        attachMap.put("token", token);
        attachMap.put("fileName", custNo);
        logger.info("path:" + path + "           fileName:" + custNo);
        Map<String, Object> attachresult = appServerService.attachUploadPersonByFilePath(token, attachMap);
        Map<String, Object> jsonObject2 = (Map<String, Object>) attachresult.get("head");
        retFlag = (String) jsonObject2.get("retFlag");
        message = (String) jsonObject2.get("retMsg");
        if (!"00000".equals(retFlag)) {// 文件上传成功返回 00000
            // //成功后从这里调用face++接口
            logger.info("vipabc,保存客户信息,调用app上传学生证封面失败！retMsg:" + message);
            return fail(retFlag, message);
        }
        retFlag = "0000";
        logger.info("VIPABC上传学生证封面结束");
        return fail(retFlag, message);


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
