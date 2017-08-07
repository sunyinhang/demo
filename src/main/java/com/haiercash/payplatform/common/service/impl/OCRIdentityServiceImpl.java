package com.haiercash.payplatform.common.service.impl;

import com.amazonaws.util.IOUtils;
import com.haiercash.commons.redis.Cache;
import com.haiercash.commons.util.EncryptUtil;
import com.haiercash.payplatform.common.entity.ReturnMessage;
import com.haiercash.payplatform.common.service.OCRIdentityService;
import com.haiercash.payplatform.common.utils.ConstUtil;
import com.haiercash.payplatform.common.utils.ocr.OCRIdentityTC;
import com.haiercash.payplatform.service.AppServerService;
import com.sun.jersey.core.util.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.haiercash.payplatform.common.utils.RestUtil.fail;
import static com.haiercash.payplatform.common.utils.RestUtil.success;

/**
 * Created by yuanli on 2017/7/31.
 */
@Service
public class OCRIdentityServiceImpl implements OCRIdentityService {
    public Log logger = LogFactory.getLog(getClass());
    @Autowired
    private Cache cache;
    @Autowired
    private AppServerService appServerService;

    @Value("${app.other.haierDataImg_url}")
    protected String haierDataImg_url;

    @Value("${app.other.appServer_page_url}")
    protected String appServer_page_url;

    public Map<String, Object> ocrIdentity(MultipartFile ocrImg, HttpServletRequest request, HttpServletResponse response) throws Exception {
        logger.info("OCR身份信息获取*************开始");
        //图片非空判断
        if (ocrImg.isEmpty()) {
            logger.info("图片为空");
            return fail("01", "图片为空");
        }
        //token非空判断
        String token = request.getParameter("token");
        if (StringUtils.isEmpty(token)) {
            logger.info("获取token为空");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.FAILED_INFO);
        }
        //缓存数据获取
        Map<String, Object> cacheMap = cache.get(token);
        if (cacheMap.isEmpty()) {
            logger.info("Jedis数据获取失败");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        //TODO!!!!
        String userId = (String) cacheMap.get("userId");
        if (StringUtils.isEmpty(userId)) {
            logger.info("jedis获取数据失效");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.FAILED_INFO);
        }
        //
        InputStream input = ocrImg.getInputStream();
        InputStream instream0 = ocrImg.getInputStream();
        //调用OCR
        OCRIdentityTC ocrIdentityTC = new OCRIdentityTC();
        ReturnMessage returnMessage = ocrIdentityTC.OCRIDUpload(input);
        if (StringUtils.isEmpty(returnMessage)) {
            return fail("02", "OCR读取身份信息失败");
        }
        logger.info("OCR返回信息：" + returnMessage.toString());
        String code = returnMessage.getCode();
        if (!"0000".equals(code)) {
            return fail("02", returnMessage.getMessage());
        }

        //获取OCR返回信息进行redis存储
        Object retObj = returnMessage.getRetObj();
        logger.info("测试：" + retObj);
//        Map<String, Object> m = (Map) retObj;
//        logger.info(m);
        JSONObject cardsResJson = new JSONObject(retObj.toString());
//        JSONObject cardsResJson = new JSONObject((new JSONObject(returnMessage.getRetObj())).get("cards"));
        cacheMap.put("name", (String) cardsResJson.get("name"));//姓名
        cacheMap.put("gender", (String) cardsResJson.get("gender"));//性别
        cacheMap.put("birthday", (String) cardsResJson.get("birthday"));//出生年月日
        cacheMap.put("race", (String) cardsResJson.get("race"));//民族
        cacheMap.put("address", (String) cardsResJson.get("address"));//地址
        cacheMap.put("idCard", (String) cardsResJson.get("id_card_number"));//身份证号
        cacheMap.put("issued", (String) cardsResJson.get("issued_by"));//签发机关
        cacheMap.put("validDate", (String) cardsResJson.get("valid_date"));//有效期
        String cardSide = (String) cardsResJson.get("side");
        //OCR调用成功，进行图片本地上传
        byte[] data = null;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int numBytesRead = 0;
        while ((numBytesRead = instream0.read(buf)) != -1) {
            output.write(buf, 0, numBytesRead);
        }
        data = output.toByteArray();
        output.close();
        instream0.close();

        BASE64Encoder encode = new BASE64Encoder();
        String imageStr = encode.encode(data);
        String certImagePath = saveImage2Disk(userId, imageStr);

        if (!StringUtils.isEmpty(cardSide) && "front".equals(cardSide)) {
            logger.info("身份证正面存储路径为------------------：" + certImagePath);
            cacheMap.put("certImagePathA", certImagePath);
        } else if (!StringUtils.isEmpty(cardSide) && "back".equals(cardSide)) {
            logger.info("身份证反面存储路径为------------------：" + certImagePath);
            cacheMap.put("certImagePathB", certImagePath);
        }

        cache.set(token, cacheMap);
        logger.info("OCR身份信息获取*************结束");
        return success();
    }

    private String saveImage2Disk(String userId, String imageStr) throws IOException {
        String imgPath = "";
        try {
            String fn = haierDataImg_url + "/certImage/" + userId;
            File path = new File(fn);
            if (!path.exists()) {
                path.mkdirs();
            }
            logger.info("身份证正反面照片缓存路径:" + fn);
            String uuId = UUID.randomUUID().toString().replaceAll("-", "");
            imgPath = path + "/" + uuId + ".jpg";
            String filestream = imageStr;
            BASE64Decoder decoder = new BASE64Decoder();

            byte[] bt = null;
            bt = decoder.decodeBuffer(filestream);
            InputStream inputStream = new ByteArrayInputStream(bt);


            FileOutputStream fos = new FileOutputStream(imgPath);
            byte[] bytes = new byte[1024 * 1024];
            int readLen;
            while ((readLen = inputStream.read(bytes)) != -1) {
                fos.write(bytes, 0, readLen);
            }
            fos.close();
            inputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
            logger.info("身份证照片保存失败:" + e.getMessage());
        }
        return imgPath;
    }

    public Map<String, Object> savaIdentityInfo(Map<String, Object> map) {
        logger.info("OCR信息保存（下一步）***********开始");
        String token = (String) map.get("token");
        String name = (String) map.get("name");

        if (StringUtils.isEmpty(token) || StringUtils.isEmpty(name)) {
            logger.info("token:" + token + "  name:" + name);
            logger.info("前台获取请求参数有误");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.FAILED_INFO);
        }

        Map<String, Object> cacheMap = cache.get(token);
        if (cacheMap.isEmpty()) {
            logger.info("Jedis数据获取失败");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        cacheMap.put("name", name);
        cache.set(token, cacheMap);
        logger.info("OCR信息保存（下一步）***********结束");
        return success();
    }

    //获取省市区
    public Map<String, Object> getArea(Map<String, Object> map) {
        logger.info("获取省市区*****************开始");
        String token = (String) map.get("token");
        String areaCode = (String) map.get("areaCode");
        String channel = (String) map.get("channel");
        String channelNo = (String) map.get("channelNo");

        if (StringUtils.isEmpty(token) || StringUtils.isEmpty(channel) || StringUtils.isEmpty(channelNo)) {
            logger.info("token:" + token + "   channel:" + channel + "   channelNo:" + channelNo);
            logger.info("前台获取请求参数有误");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.FAILED_INFO);
        }

        String flag = "children"; //查询标志
        if (areaCode == null || "".equals(areaCode)) {
            areaCode = "";
            flag = "province";
        }

        Map<String, Object> reqmap = new HashMap<String, Object>();
        map.put("areaCode", areaCode);
        map.put("flag", flag);
        map.put("channel", channel);
        map.put("channelNo", channelNo);

        Map<String, Object> resultmap = appServerService.getAreaInfo(token, reqmap);
        logger.info("获取省市区*****************结束");
        return resultmap;
    }

    //获取卡信息
    public Map<String, Object> getCardInfo(String cardNo) {
        logger.info("获取卡信息*****************开始");
        if (StringUtils.isEmpty(cardNo)) {
            logger.info("cardNo:" + cardNo);
            logger.info("前台获取请求参数有误");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.FAILED_INFO);
        }

        Map<String, Object> resultmap = appServerService.getBankInfo(cardNo);
        logger.info("获取卡信息*****************结束");
        return resultmap;
    }

    //发送短信验证码
    public Map<String, Object> sendMessage(String token, String channel, String channelNo) {
        logger.info("发送短信验证码***************开始");
        if (StringUtils.isEmpty(token) || StringUtils.isEmpty(channel) || StringUtils.isEmpty(channelNo)) {
            logger.info("token:" + token);
            logger.info("前台获取请求参数有误");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.FAILED_INFO);
        }

        Map<String, Object> cacheMap = cache.get(token);
        if (cacheMap.isEmpty()) {
            logger.info("Jedis数据获取失败");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }

        String phone = (String) cacheMap.get("phoneNo");////得到绑定手机号
        String phoneEncrypt = EncryptUtil.simpleEncrypt(phone);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("channel", channel);
        map.put("channelNo", channelNo);
        map.put("phone", phoneEncrypt);
        map.put("token", token);

        Map<String, Object> resultmap = appServerService.sendMessage(token, map);
        logger.info("发送短信验证码*******************结束");
        return resultmap;
    }

    //发送短信验证码
    public Map<String, Object> sendMsg(String phone, String channel, String channelNo) {
        logger.info("发送短信验证码***************开始");
        if (StringUtils.isEmpty(phone)) {
            logger.info("token:" + phone);
            logger.info("前台获取请求参数有误");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.FAILED_INFO);
        }

        String phoneEncrypt = EncryptUtil.simpleEncrypt(phone);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("channel", channel);
        map.put("channelNo", channelNo);
        map.put("phone", phoneEncrypt);

        Map<String, Object> resultmap = appServerService.sendMessage(null, map);
        logger.info("发送短信验证码***************开始");
        return resultmap;
    }

    //实名认证
    public Map<String, Object> realAuthentication(Map<String, Object> map) throws Exception {
        logger.info("实名认证*********************开始");
        //1.前台参数获取
        String token = (String) map.get("token");
        String verifyNo = (String) map.get("verifyNo");
        String cityCode = (String) map.get("cityCode");//省市区编码
        String cardnumber = (String) map.get("cardnumber");//卡号
        String mobile = (String) map.get("mobile");//手机号
        String channel = (String) map.get("channel");
        String channelNo = (String) map.get("channelNo");
        //2.前台参数非空判断
        if (StringUtils.isEmpty(token) || StringUtils.isEmpty(verifyNo) || StringUtils.isEmpty(cityCode) ||
                StringUtils.isEmpty(cardnumber) || StringUtils.isEmpty(mobile) ||
                StringUtils.isEmpty(channel) || StringUtils.isEmpty(channelNo)) {
            logger.info("token:" + token + "  verifyNo:" + verifyNo + "  cityCode:" + cityCode +
                    "  cardnumber:" + cardnumber + "  mobile:" + mobile + "   channel:" + channel + "   channelNo:" + channelNo);
            logger.info("前台获取请求参数有误");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.FAILED_INFO);
        }
        //3.jedis缓存数据获取
        Map<String, Object> cacheMap = cache.get(token);
        if (cacheMap.isEmpty()) {
            logger.info("Jedis数据获取失败");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        String name = (String) cacheMap.get("name");//扫描客户姓名
        String birthDt = (String) cacheMap.get("birthday");//扫描出生年月日
        String gender = (String) cacheMap.get("gender");//扫描性别
        String regAddr = (String) cacheMap.get("address");//扫描详细地址
        String validDate = (String) cacheMap.get("validDate");//有效期
        String certOrga = (String) cacheMap.get("issued");//扫描身份证签发机关
        String ethnic = (String) cacheMap.get("race");//扫描民族
        String idCard = (String) cacheMap.get("idCard");//扫描身份证号
        String userId = (String) cacheMap.get("userId");//userId
        //4.缓存数据非空判断
        if (StringUtils.isEmpty(name) || StringUtils.isEmpty(birthDt) || StringUtils.isEmpty(gender)
                || StringUtils.isEmpty(regAddr) || StringUtils.isEmpty(validDate) || StringUtils.isEmpty(certOrga)
                || StringUtils.isEmpty(ethnic) || StringUtils.isEmpty(idCard) || StringUtils.isEmpty(userId)) {
            logger.info("name:" + name + "  birthDt:" + birthDt + "  gender:" + gender + "  validDate:" + validDate + "  certOrga:" + certOrga
                    + "   ethnic:" + ethnic + "    idCard:" + idCard + "   userId:" + userId);
            logger.info("Jedis缓存获取信息失败");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.FAILED_INFO);
        }
        //5.校验短信验证码
        Map<String, Object> verifyNoMap = new HashMap<String, Object>();
        verifyNoMap.put("phone", mobile);
        verifyNoMap.put("verifyNo", verifyNo);
        verifyNoMap.put("token", token);
        verifyNoMap.put("channel", channel);
        verifyNoMap.put("channelNo", channelNo);
        Map<String, Object> verifyresultmap = appServerService.smsVerify(token, verifyNoMap);
        JSONObject verifyheadjson = new JSONObject((String) verifyresultmap.get("head"));
        String verifyretFlag = (String) verifyheadjson.get("retFlag");
        if (!"00000".equals(verifyretFlag)) {//校验短信验证码失败
            String retMsg = (String) verifyheadjson.get("retMsg");
            return fail(ConstUtil.ERROR_CODE, retMsg);
        }
        //6.OCR信息保存
        String certStrDt = "";//扫描身份证有效期限开始日期
        String certEndDt = "";//扫描身份证有效期限结束日期
        String vDate = validDate.replaceAll("[-.]", "");
        if (vDate.length() >= 8) {
            certStrDt = vDate.substring(0, 8); //取前8位作为起始日期
            certEndDt = vDate.substring(8);  //剩余的作为截止日期
        }
        Map<String, Object> ocrMap = new HashMap<String, Object>();
        ocrMap.put("custName", name);
        ocrMap.put("afterCustName", "");//TODO!!!!  修改后客户姓名
        ocrMap.put("birthDt", birthDt);
        ocrMap.put("gender", gender);
        ocrMap.put("regAddr", regAddr);
        ocrMap.put("validDate", validDate);
        ocrMap.put("certStrDt", certStrDt);
        ocrMap.put("certEndDt", certEndDt);
        ocrMap.put("certOrga", certOrga);
        ocrMap.put("ethnic", ethnic);
        ocrMap.put("certNo", idCard);
        ocrMap.put("token", token);
        ocrMap.put("channel", channel);
        ocrMap.put("channelNo", channelNo);
        Map<String, Object> ocrresultmap = appServerService.saveCardMsg(token, ocrMap);
        JSONObject ocrheadjson = new JSONObject((String) ocrresultmap.get("head"));
        String ocrretFlag = (String) ocrheadjson.get("retFlag");
        if (!"00000".equals(ocrretFlag)) {//身份证信息保存失败
            String retMsg = (String) ocrheadjson.get("retMsg");
            return fail(ConstUtil.ERROR_CODE, retMsg);
        }
        //7.验证并新增实名认证信息
        String[] officeArea_split = cityCode.split(",");
        String acctProvince = (String) officeArea_split[0];//省代码
        String acctCity = (String) officeArea_split[1];//市代码
        Map<String, Object> identityMap = new HashMap<String, Object>();
        identityMap.put("token", token);
        identityMap.put("channel", channel);
        identityMap.put("channelNo", channelNo);
        identityMap.put("custName", name); //客户姓名 √
        identityMap.put("certNo", idCard); //身份证号 √
        identityMap.put("cardNo", cardnumber); //银行卡号 √
        identityMap.put("mobile", mobile); //手机号 √
        identityMap.put("dataFrom", channelNo); //数据来源 √
        identityMap.put("threeParamVal", ConstUtil.THREE_PARAM_VAL_N); //是否需要三要素验证
        identityMap.put("userId", userId); //客户userId
        identityMap.put("acctProvince", acctProvince); //开户行省代码
        identityMap.put("acctCity", acctCity); //开户行市代码
        Map<String, Object> identityresultmap = appServerService.fCiCustRealThreeInfo(token, identityMap);
        JSONObject identityheadjson = new JSONObject((String) identityresultmap.get("head"));
        String identityretFlag = identityheadjson.getString("retFlag");
        if (!"00000".equals(identityretFlag)) {
            String retMsg = identityheadjson.getString("retMsg");
            return fail(ConstUtil.ERROR_CODE, retMsg);
        }
        JSONObject identitybodyjson = new JSONObject((String) identityresultmap.get("body"));
        //信息保存
        String custNo = identitybodyjson.getString("custNo");
        String cardNo = identitybodyjson.getString("cardNo");
        String bankCode = identitybodyjson.getString("acctBankNo");
        String bankName = identitybodyjson.getString("acctBankName");
        String idNo = identitybodyjson.getString("certNo");
        String idType = identitybodyjson.getString("certType");
        String cardPhone = identitybodyjson.getString(mobile);

        cacheMap.put("custNo", custNo);
        cacheMap.put("cardNo", cardNo);
        cacheMap.put("bankCode", bankCode);
        cacheMap.put("bankName", bankName);
        cacheMap.put("idNo", idNo);
        cacheMap.put("idType", idType);
        cacheMap.put("cardPhone", cardPhone);
        cache.set(token, cacheMap);

        //绑定手机号修改为实名认证手机号
        String phone = cacheMap.get("phoneNo").toString();//得到绑定手机号(TODO!!!!)
        Map<String, Object> updmobilemap = new HashMap<String, Object>();
        updmobilemap.put("userId", EncryptUtil.simpleEncrypt(userId));
        updmobilemap.put("oldMobile", EncryptUtil.simpleEncrypt(phone));//旧手机号
        updmobilemap.put("newMobile", EncryptUtil.simpleEncrypt(mobile));//新手机号
        updmobilemap.put("verifyNo", EncryptUtil.simpleEncrypt(verifyNo));
        updmobilemap.put("channel", channel);
        updmobilemap.put("channelNo", channelNo);
        Map<String, Object> updmobileresultmap = appServerService.updateMobile(token, updmobilemap);
        JSONObject updmobileheadjson = new JSONObject((String) updmobileresultmap.get("head"));
        String updmobileretflag = updmobileheadjson.getString("retFlag");
        if (!"00000".equals(updmobileretflag)) {
            String retMsg = updmobileheadjson.getString("retMsg");
            return fail(ConstUtil.ERROR_CODE, retMsg);
        }

        //OCR图片路径上送
        Map<String, String> pathMap = new HashMap<String, String>();
        pathMap.put("certImagePathA", (String) cacheMap.get("certImagePathA"));//正面共享盘位置
        pathMap.put("certImagePathB", (String) cacheMap.get("certImagePathB"));//反面共享盘位置
        for (Map.Entry<String, String> entry : pathMap.entrySet()) {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            if ("certImagePathA".equals(entry.getKey())) {//正面
                paramMap.put("attachType", ConstUtil.CERT_FILE_TYPE_A);//影像类型
                paramMap.put("attachName", ConstUtil.CERT_FILE_NAME_A);//影像名称
            } else if ("certImagePathB".equals(entry.getKey())) {//反面
                paramMap.put("attachType", ConstUtil.CERT_FILE_TYPE_B);
                paramMap.put("attachName", ConstUtil.CERT_FILE_NAME_B);
            }
            String filePath = entry.getValue();//文件路径
            InputStream is = new BufferedInputStream(new FileInputStream(String.valueOf(filePath)));
            //获取MD5码
            boolean isA = "certImagePathA".equals(entry.getKey());
            String md5Code = DigestUtils.md5Hex(IOUtils.toByteArray(is));
            paramMap.put("token", token);
            paramMap.put("md5", md5Code);//文件md5码
            paramMap.put("custNo", custNo);
            paramMap.put("filePath", filePath);//文件路径
            paramMap.put("idNo", idCard);//身份证号
            paramMap.put("commonCustNo", null);//共同还款人编号，传null
            logger.info("实名绑卡，上传身份证" + (isA ? "正" : "反") + "面，请求参数：" + paramMap);
            Map<String, Object> uploadresultmap = appServerService.attachUploadPersonByFilePath(token, paramMap);
            JSONObject uploadheadjson = new JSONObject(uploadresultmap.get("head"));
            String uploadretFlag = uploadheadjson.getString("retFlag");
            if (!"00000".equals(uploadretFlag)) {
                String retMsg = uploadheadjson.getString("retMsg");
                return fail(ConstUtil.ERROR_CODE, retMsg);
            }
        }

        logger.info("实名认证***********************结束");
        return success();
    }

    //支付密码设置
    public Map<String, Object> resetPayPasswd(String token, String payPasswd) {
        logger.info("支付密码设置************开始");
        if (StringUtils.isEmpty(token)) {
            logger.info("token:" + token);
            logger.info("从前端获取的的token为空");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.FAILED_INFO);
        }
        if (StringUtils.isEmpty(payPasswd)) {
            logger.info("payPasswd:" + payPasswd);
            logger.info("从前端获取的支付密码为空");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.FAILED_INFO);
        }
        Map<String, Object> cacheMap = cache.get(token);
        if (cacheMap.isEmpty()) {
            logger.info("Jedis获取失败");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        cacheMap.put("pageFlag", "0");
        cacheMap.put("payPasswd", payPasswd);
        cache.set(token, cacheMap);
        logger.info("支付密码设置************结束");
        return success();
    }

    /**
     * 协议展示：(1)展示注册协议(2)个人征信(3)借款合同
     *
     * @param token
     * @param flag
     * @return
     */
    public Map<String, Object> treatyShowServlet(String token, String flag) throws Exception {
        String realmName = null;
        HashMap<String, Object> map = new HashMap<>();
        if (StringUtils.isEmpty(token)) {
            logger.info("从前端获取的token：" + token);
            logger.info("从前端获取的token为空");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        if (StringUtils.isEmpty(flag)) {
            logger.info("从前端获取的flag:" + flag);
            logger.info("从前端h获取的flag为空");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.FAILED_INFO);
        }
        Map<String, Object> cacheMap = cache.get(token);
        if (cacheMap.isEmpty()) {
            logger.info("Jedis获取失败");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        if ("contract".equals(flag)) {
            String custNo = cacheMap.get("custNo") + "";
            String applSeq = cacheMap.get("applSeq") + "";
            if (!StringUtils.isEmpty(custNo) && !StringUtils.isEmpty(applSeq)) {
                realmName = appServer_page_url + "app/appserver/contract?custNo=" + custNo + "&applSeq=" + applSeq;
                logger.info("------------个人借款合同地址---------" + realmName);
                map.put("realmName", realmName);
            }
        } else if ("credit".equals(flag)) {
            String custName = cacheMap.get("custName") + "";
            String certNo = cacheMap.get("idNo") + "";
            if (!StringUtils.isEmpty(custName) && !StringUtils.isEmpty(certNo)) {
                String custNameB = new String(Base64.encode(custName.getBytes()), "UTF-8");
                realmName = realmName + "app/appserver/register?orderNo=" + custNameB + "&certNo=" + certNo;
                logger.info("--------------征信查询------------" + realmName);
                map.put("realmName", realmName);
            }
        } else if ("register".equals(flag)) {
            String orderNo = cacheMap.get("orderNo") + "";
            if (!StringUtils.isEmpty(orderNo)) {
                realmName = realmName + "app/appserver/register?orderNo=" + orderNo;
                logger.info("------------注册协议------------" + realmName);
                map.put("realmName", realmName);
            }
        } else if ("person".equals(flag)) {
            realmName = realmName + "static/agreement/PersonInfo.html";
            logger.info("----------个人信息协议-----------" + realmName);
        }
        return success(map);
    }
}
