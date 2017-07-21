package com.haiercash.appserver.service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.haiercash.appserver.util.HttpUploadFile;
import com.haiercash.appserver.web.AttachController;
import com.haiercash.common.config.EurekaServer;
//import com.haiercash.appserver.config.EurekaServer;
import com.haiercash.common.data.*;
import com.haiercash.common.service.BaseService;
import com.haiercash.commons.cmis.CmisTradeCode;
import com.haiercash.commons.util.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Base64Utils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import redis.clients.jedis.Jedis;
import sun.misc.BASE64Decoder;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by yuanli on 2017/4/11.
 */
@Service
public class QiaoRongService extends BaseService {
    private static Log logger = LogFactory.getLog(MerchFaceService.class);

    @Autowired
    UAuthCASignRequestRepository uAuthCASignRequestRepository;
    @Autowired
    AttachService attachService;
    @Autowired
    private PersonFaceService personFaceService;
    @Autowired
    CrmService crmService;
    @Autowired
    CmisApplService cmisApplService;
    @Autowired
    UauthService uauthService;
    @Autowired
    private AppCmisInfoRepository appCmisInfoRepository;
    @Autowired
    MoxieInfoRepository moxieInfoRepository;

    private static long IMAGE_MAXSIZE;
    private static int limitAmount = 30000;

//    private static String face_api_key = "D9svZuIdQ6uCqqBk4UMB2xiTLgKtVFLS";
//    private static String face_api_secret = "PYDHtPUnDzBcXvQsyDSyfFIBjuWI5w8K";
//    private static String face_getresult_url = "https://api.megvii.com/faceid/lite/get_result";//获取face++结果
//    private static String face_DataImg_url = "/testshare01/crm/image/";//face++图片保存路径

    //private static String app_url = CommonProperties.get("other.domain").toString();//
    //private static String back_url = "http://139.196.45.193:20011";//回调乔融地址（通知CA签章结束）

    @Value("${common.qrdz.back_url}")
    protected String back_url;

    @Value("${common.face.face_api_key}")
    protected String face_api_key;

    @Value("${common.face.face_api_secret}")
    protected String face_api_secret;

    @Value("${common.face.face_getresult_url}")
    protected String face_getresult_url;

    @Value("${common.face.face_DataImg_url}")
    protected String face_DataImg_url;

    @Value("${common.face.face_gettoken_url}")
    protected String face_gettoken_url;

    /**
     * 实名认证
     * @param code
     * @param name
     * @param phone
     * @param idNo
     * @param cardnumber
     * @param totalamount
     * @return
     */
    public Map<String, Object>  checkCaFourKeysInfo(String code, String name, String phone, String idNo, String cardnumber, String totalamount){
        //1.验证并新增实名认证
        Map<String, Object> returnmap = new HashMap<String, Object>();

        try {
            Map<String, Object> resultMap = check(code);
            if (resultMap == null){
                logger.info("乔融豆子crm进行实名认证返回信息"+fail("11", "签章信息查询错误"));
                return fail("11", "签章信息查询错误");
            }
            //实名认证失败
            if(!HttpUtil.isSuccess(resultMap)){
                HashMap<String, Object> head = (HashMap) resultMap.get("head");
                String retFlag = (String) head.get("retFlag");
                if("11".equals(retFlag) || "12".equals(retFlag)) {
                    logger.info("乔融豆子crm进行实名认证返回信息"+fail(retFlag, "四要素认证不通过"));
                    return fail(retFlag, "四要素认证不通过");
                }
                logger.info("乔融豆子crm进行实名认证返回信息"+fail((String)head.get("retFlag"),(String)head.get("retMsg")));
                return fail((String)head.get("retFlag"),(String)head.get("retMsg"));
            }
            //实名认证成功
            logger.info("乔融豆子crm进行实名认证成功："+success(resultMap.get("body")));

            //2.判断是否已注册
            Map<String, Object> uauthmap = uauthService.isRegister(EncryptUtil.simpleEncrypt(phone));
            String retflag = (String) ((Map)uauthmap.get("head")).get("retFlag");
            if(!"00000".equals(retflag)){
                String retmsg = (String) ((Map)uauthmap.get("head")).get("retMsg");
                return fail(retflag, retmsg);
            }
            String registerflag = (String) ((Map)uauthmap.get("body")).get("isRegister");
            //未注册，跳转注册页面
            if("N".equals(registerflag)){
                returnmap.put("flag","01");
                return success(returnmap);//跳转注册页面
            }
            //已注册或已占用
            if(!"Y".equals(registerflag)){
                return fail("01", "手机已被注册！请联系客服修改。客服电话：400777");
            }

            //3.手机号已注册，判断是否需要人脸识别啊
            logger.info("乔融豆子查询是否需要进行人脸识别根据custName和idNo查询custNo");
            Map<String, Object> crmmap = getCustInfoByCustNameAndCertNo(name, idNo);
            logger.info("乔融豆子查询是否需要进行人脸识别查询到的crm客户信息"+crmmap.toString());
            Map<String, Object> mapBody = HttpUtil.json2Map(crmmap.get("body").toString());
            String custNo = mapBody.get("custNo").toString();
            logger.info("乔融豆子查询是否需要进行人脸识别根据custName和idNo查询custNo："+custNo);
            String typCde = getloan(code);
            Map<String, Object> map = personFaceService.ifNeedFaceChkByTypCde(typCde, "33", custNo, name, idNo);

            JSONObject jsonObj = new JSONObject(map);
            logger.info("乔融豆子****是否需要人脸识别接口返回"+jsonObj);
            JSONObject head = jsonObj.getJSONObject("head");
            String retFlag = head.getString("retFlag");
            //人脸识别失败
            if (!"00000".equals(retFlag)) {
                return fail((String)head.get("retFlag"),(String)head.get("retMsg"));
            }
            JSONObject body = jsonObj.getJSONObject("body");
            String code0 = body.getString("code"); //结果标识码
            logger.info("结果标识码"+code0);
            if("00".equals(code0)) {//00：已经通过了人脸识别（得分合格），不需要再做人脸识别
                logger.info("乔融豆子***已经通过了人脸识别（得分合格），不需要再做人脸识别");
                double amount = Double.parseDouble(totalamount);

                UAuthCASignRequest carequest = uAuthCASignRequestRepository.findOne(code);
                String applseq = carequest.getApplseq();
                Map<String, Object> mapmoxie = getMoxieByApplseq(applseq);
                String isFundFlag = (String) mapmoxie.get("isFund");
                String isBankFlag = (String) mapmoxie.get("isBank");
                String isCarrierFlag = (String) mapmoxie.get("isCarrier");
                //判断是否已做过运营商
                if("Y".equals(isCarrierFlag)){//已做过运营商
                    //判断金额
                    if(amount >= limitAmount){
                        //判断是否做过公积金网银
                        if("Y".equals(isFundFlag) || "Y".equals(isBankFlag)){//已做过公积金、网银认证  跳转合同展示
                            logger.info("乔融豆子***已经通过了人脸识别（得分合格），跳转合同展示");
                            returnmap.put("flag", "03");
                            return success(returnmap);//跳转合同展示页面
                        }else{//未做过公积金、网银认证  跳转魔蝎认证页面
                            logger.info("乔融豆子***已经通过了人脸识别（得分合格），跳转魔蝎");
                            returnmap.put("flag", "02");
                            return success(returnmap);//跳转魔蝎页面
                        }
                    }else{
                        logger.info("乔融豆子***已经通过了人脸识别（得分合格），跳转合同展示");
                        returnmap.put("flag", "03");
                        return success(returnmap);//跳转合同展示页面
                    }
                }else{//没有做运营商验证  跳转运营商
                    logger.info("乔融豆子***已经通过了人脸识别（得分合格），跳转运营商");
                    returnmap.put("flag", "07");
                    return success(returnmap);
                }
            } else if("01".equals(code0)){//01：未通过人脸识别，剩余次数为0，不能再做人脸识别，录单终止
                //终止
                logger.info("乔融豆子***未通过人脸识别，剩余次数为0，不能再做人脸识别，录单终止");
                returnmap.put("flag", "04");
                return success(returnmap);
            } else if("02".equals(code0)){//02：未通过人脸识别，剩余次数为0，不能再做人脸识别，但可以上传替代影像
                //跳转替代影像
                logger.info("乔融豆子***未通过人脸识别，剩余次数为0，不能再做人脸识别，但可以上传替代影像");
                returnmap.put("flag", "05");
                return success(returnmap);//跳转替代影像
            } else if("10".equals(code0)) {//10：未通过人脸识别，可以再做人脸识别
                logger.info("乔融豆子***未通过人脸识别，可以再做人脸识别");
                Map<String, String> facemap = new HashMap<String, String>();
                //可以做人脸识别
                facemap.put("api_key", face_api_key);
                facemap.put("api_secret", face_api_secret);
                facemap.put("comparison_type", "1");
                facemap.put("face_image_type", "raw_image");
                facemap.put("idcard_name", name);
                facemap.put("idcard_number", idNo);
                facemap.put("return_url", CommonProperties.get("other.domain").toString()
                        + "/app/appserver/faceReturnUrl?code=" + code);
                facemap.put("biz_no", "00222111");// TODO!!!!待定
                facemap.put("notify_url", CommonProperties.get("other.domain").toString()
                        + "/app/appserver/faceNotifyUrl?code=" + code);
                facemap.put("idcard_mode", "0");
                Map<String, String> faceIDInterface = faceIDInterface(facemap);
                logger.info("fece++返回：" + faceIDInterface);
                //
                if (!"".equals(faceIDInterface.get("token")) && faceIDInterface.get("token") != null) {
                    String face_token = faceIDInterface.get("token");
                    String biz_id = faceIDInterface.get("biz_id");
                    logger.info("biz_id:" + biz_id);

                    Jedis jedis1 = RedisUtil.getJedis();
                    Map m1 = jedis1.hgetAll(code);
                    m1.put("biz_id", biz_id);
                    jedis1.hmset(code, m1);
                    jedis1.close();

                    returnmap.put("facetoken", face_token);
                    returnmap.put("flag", "06");
                    return success(returnmap);//跳转人脸识别
                } else {
                    logger.info("face++获取token失败");
                    return fail("04", "网络通讯异常");
                }
            }else{
                logger.info("人脸识别，返回标志码无效");
                return fail("04", "网络通讯异常");
            }

        } catch (Exception e){
            logger.error("实名认证异常信息" + e, e);
            return fail("99", "网络通讯异常");
        }

    }


    /**
     * 上传替代影像
     * @param handImg
     * @param code
     * @return
     */
    public Map<String, Object> fileUpload(MultipartFile handImg, String code) {
        // 判断文件是否为空
        logger.info("*************************乔融豆子人脸识别接口**********************");
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            //String code = request.getParameter("code");

            if (handImg.getSize() > getImageMaxSize()) {
                return fail("11", "文件大小不能超过5M");
            }

            Jedis jedis1 = RedisUtil.getJedis();
            Map m1 = jedis1.hgetAll(code);
            String name = (String) m1.get("name");
            String idNo = (String) m1.get("idNo");
            String totalamount = (String) m1.get("totalamount");
            logger.info("申请金额为："+totalamount);
            jedis1.close();

            Map<String,Object> crmmap = getCustInfoByCustNameAndCertNo(name, idNo);
            Map<String, Object> mapBody = HttpUtil.json2Map(crmmap.get("body").toString());

            String custNo = mapBody.get("custNo").toString();
            String attachType = "App01";
            String attachName = "本人手持身份证与本人面部合照照片";

            String filePath = attachService.getPersonFilePath(custNo, attachType);
            String originalFilename = handImg.getOriginalFilename();
            String fileName = attachService.getFileName(originalFilename);


            if (!handImg.isEmpty()) {
                InputStream is = handImg.getInputStream();
                String MD5 = DigestUtils.md5Hex(IOUtils.toByteArray(is));//

                String myMd5 = multipart2File(handImg, filePath, fileName);
                logger.info("myMd5:" + myMd5);
                if (myMd5 == null) {
                    return fail("11", "文件保存失败");
                }
                if (!myMd5.equals(MD5)) {
                    logger.debug(String.format("文件md5校验失败: %s :: %s", MD5, myMd5));
                    return fail("11", "文件md5校验失败");
                }
                UAuthCASignRequest carequest = uAuthCASignRequestRepository.findOne(code);
                String applSeq = carequest.getApplseq();
                //图片上传
                AttachFile attachFile = attachService
                        .saveAttachFile(custNo, attachType, attachName, MD5, filePath, fileName, "", applSeq);

                double amount = Double.parseDouble(totalamount);
                Map<String, Object> faceMap = new HashMap<String, Object>();
                if(amount >= limitAmount){
                    logger.info("乔融豆子***替代影像上传成功，跳转魔蝎");
                    faceMap.put("flag", "02");
                    return success(faceMap);//跳转魔蝎页面
                }else{
                    logger.info("乔融豆子***替代影像上传成功，跳转合同展示");
                    faceMap.put("flag", "03");
                    return success(faceMap);//跳转合同展示页面
                }
            }else{
                logger.info("图像信息为空");
                return fail("11", "图像信息为空");
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.info("异常信息：" + e.getMessage(), e);
            return fail("11", "文件上传接口异常");
        }
    }

    /**
     * 判断是否需要进行人脸识别
     * @param code
     * @return
     * @throws Exception
     */
    public Map<String, Object> isNeedFaceCheck(String code) throws Exception {
        logger.info("乔融豆子查询是否需要进行人脸识别code:"+code);
        String typCde = getloan(code);
        UAuthCASignRequest request = uAuthCASignRequestRepository.findOne(code);
        logger.info("乔融豆子查询是否需要进行人脸识别查询订单信息:"+request.toString());
        String orderJson = request.getOrderJson();
        JSONObject json = new JSONObject(orderJson);
        JSONObject jsonObject = (JSONObject)json.get("order");

        String custName = jsonObject.get("custName").toString();
        String idNo = jsonObject.get("idNo").toString();
        logger.info("乔融豆子查询是否需要进行人脸识别根据custName和idNo查询custNo");
        Map<String, Object> crmmap = getCustInfoByCustNameAndCertNo(custName, idNo);
        logger.info("乔融豆子查询是否需要进行人脸识别查询到的crm客户信息"+crmmap.toString());
        Map<String, Object> mapBody = HttpUtil.json2Map(crmmap.get("body").toString());
        String custNo = mapBody.get("custNo").toString();
        logger.info("乔融豆子查询是否需要进行人脸识别根据custName和idNo查询custNo："+custNo);
        Map<String, Object> map = personFaceService.ifNeedFaceChkByTypCde(typCde, "33", custNo, custName, idNo);
        //{head=com.haiercash.commons.util.ResultHead@b09c277, body={code=00, flag=N, isPass=Y}}
        logger.info("乔融豆子查询是否需要进行人脸识别调用app接口返回"+map);


        JSONObject jsonObj = new JSONObject(map);
        JSONObject head = jsonObj.getJSONObject("head");
        String retFlag = head.getString("retFlag");

        if (!"00000".equals(retFlag)) {
            return fail((String)head.get("retFlag"),(String)head.get("retMsg"));
        }
        Map<String, String> faceMap = new HashMap<String, String>();
        JSONObject body = jsonObj.getJSONObject("body");
        String code0 = body.getString("code"); //结果标识码
        logger.info("结果标识码"+code0);
        if("00".equals(code0)) {//00：已经通过了人脸识别（得分合格），不需要再做人脸识别
            logger.info("乔融豆子***已经通过了人脸识别（得分合格），不需要再做人脸识别");
            Jedis jedis1 = RedisUtil.getJedis();
            Map m1 = jedis1.hgetAll(code);
            jedis1.close();
            String totalamount = (String) m1.get("totalamount");
            double amount = Double.parseDouble(totalamount);

            UAuthCASignRequest carequest = uAuthCASignRequestRepository.findOne(code);
            String applseq = carequest.getApplseq();
            Map<String, Object> mapmoxie = getMoxieByApplseq(applseq);
            String isFundFlag = (String) mapmoxie.get("isFund");
            String isBankFlag = (String) mapmoxie.get("isBank");
            String isCarrierFlag = (String) mapmoxie.get("isCarrier");

            //判断是否已做过运营商
            if("Y".equals(isCarrierFlag)){//已做过运营商
                //判断金额
                if(amount >= limitAmount){
                    //判断是否做过公积金网银
                    if("Y".equals(isFundFlag) || "Y".equals(isBankFlag)){//已做过公积金、网银认证  跳转合同展示
                        logger.info("乔融豆子***已经通过了人脸识别（得分合格），跳转合同展示");
                        faceMap.put("flag", "03");
                        return success(faceMap);//跳转合同展示页面
                    }else{//未做过公积金、网银认证  跳转魔蝎认证页面
                        logger.info("乔融豆子***已经通过了人脸识别（得分合格），跳转魔蝎");
                        faceMap.put("flag", "02");
                        return success(faceMap);//跳转魔蝎页面
                    }
                }else{
                    logger.info("乔融豆子***已经通过了人脸识别（得分合格），跳转合同展示");
                    faceMap.put("flag", "03");
                    return success(faceMap);//跳转合同展示页面
                }
            }else{//没有做运营商验证  跳转运营商
                logger.info("乔融豆子***已经通过了人脸识别（得分合格），跳转运营商");
                faceMap.put("flag", "07");
                return success(faceMap);
            }


//            if(amount >= limitAmount){
//                logger.info("乔融豆子***已经通过了人脸识别（得分合格），跳转魔蝎");
//                faceMap.put("flag", "02");
//                return success(faceMap);//跳转魔蝎页面
//            }else{
//                logger.info("乔融豆子***已经通过了人脸识别（得分合格），跳转合同展示");
//                faceMap.put("flag", "03");
//                return success(faceMap);//跳转合同展示页面
//            }
        }else if("01".equals(code0)){//01：未通过人脸识别，剩余次数为0，不能再做人脸识别，录单终止
            //终止
            logger.info("乔融豆子***未通过人脸识别，剩余次数为0，不能再做人脸识别，录单终止");
            faceMap.put("flag", "04");
            return success(faceMap);
        }else if("02".equals(code0)){//02：未通过人脸识别，剩余次数为0，不能再做人脸识别，但可以上传替代影像
            //跳转替代影像
            logger.info("乔融豆子***未通过人脸识别，剩余次数为0，不能再做人脸识别，但可以上传替代影像");
            faceMap.put("flag", "05");
            return success(faceMap);
        }else if("10".equals(code0)) {//10：未通过人脸识别，可以再做人脸识别
            logger.info("乔融豆子***未通过人脸识别，可以再做人脸识别");
            Map<String, String> facemap = new HashMap<String, String>();
            //可以做人脸识别
            facemap.put("api_key", face_api_key);//TODO!!!!
            facemap.put("api_secret", face_api_secret);//TODO!!!!
            facemap.put("comparison_type", "1");
            facemap.put("face_image_type", "raw_image");
            facemap.put("idcard_name", custName);
            facemap.put("idcard_number", idNo);
            facemap.put("return_url", CommonProperties.get("other.domain").toString()
                    + "/app/appserver/faceReturnUrl?code=" + code);
            facemap.put("biz_no", "00222111");// 待定
            facemap.put("notify_url", CommonProperties.get("other.domain").toString()
                    + "/app/appserver/faceNotifyUrl?code=" + code);
            facemap.put("idcard_mode", "0");
            Map<String, String> faceIDInterface = faceIDInterface(facemap);
            logger.info("fece++返回："+faceIDInterface);
            //
            if (!"".equals(faceIDInterface.get("token")) && faceIDInterface.get("token") != null) {
                String face_token = faceIDInterface.get("token");
                String biz_id = faceIDInterface.get("biz_id");
                logger.info("biz_id:"+biz_id);

                Jedis jedis1 = RedisUtil.getJedis();
                Map m1 = jedis1.hgetAll(code);
                m1.put("biz_id", biz_id);
                jedis1.hmset(code, m1);
                jedis1.close();

                faceMap.put("facetoken", face_token);
                faceMap.put("flag", "06");
                return success(faceMap);//跳转人脸识别
            } else {
                logger.info("face++获取token失败");
                return fail("04","网络通讯异常");
            }
        }

        return map;
    }

    /**
     * 调用face++获取token
     * @param paramMap
     * @return
     */
    public Map<String, String> faceIDInterface(Map<String, String> paramMap) {
        Map<String, String> map = new HashMap<String, String>();
        String token = "";
        AtomicReference<String> error_message = new AtomicReference<>("");
        String biz_id = "";
        try {
            logger.info("face++人脸识别接口,获取token开始");
            paramMap.put("face_token", face_gettoken_url);
            String testUploadImage = HttpUploadFile.testUploadImage(paramMap);
            JSONObject jasonObject = new JSONObject(testUploadImage);
            if (jasonObject.has("token")) {
                token = jasonObject.get("token").toString();
                biz_id = jasonObject.get("biz_id").toString();
            } else {
                error_message.set(jasonObject.get("error_message").toString());
            }
            map.put("token", token);
            map.put("biz_id", biz_id);
            map.put("error_message", error_message.get());
            logger.info("face++人脸识别接口,获取token，返回数据："+map);
        } catch (Exception e) {
            logger.error("调用face++人脸识别接口接口：出现异常！异常信息：" + e, e);
            e.printStackTrace();
        }
        return map;
    }

    /**
     * 获取face++结果
     * @param paramMap
     * @return
     */
    public String faceIdGetResult(Map<String, Object> paramMap) {
        Map<String, Object> map = new HashMap<String, Object>();
        String resData = "";
        String param = "";
        try {
            String api_key = paramMap.get("api_key").toString();
            String api_secret = paramMap.get("api_secret").toString();
            String biz_id = paramMap.get("biz_id").toString();
            String return_image = paramMap.get("return_image").toString();
            map.put("api_key", api_key);
            map.put("api_secret", api_secret);
            map.put("biz_id", biz_id);
            //ifParamsIsNull(map);
            param = "api_key=" + api_key + "&api_secret=" + api_secret + "&biz_id=" + biz_id + "&return_image="
                    + return_image;
            logger.info("获取FaceID当前结果接口,请求数据：" + paramMap.toString());
            String url = face_getresult_url;//TODO!!!!
            logger.info("获取FaceID当前结果接口的Url为：" + url);
            resData = HttpUploadFile.sendGet(url, param);
        } catch (Exception e) {
            logger.error("获取FaceID当前结果接口，调用app后台接口，出现异常：" + e.getMessage(), e);
        }
        return resData;

    }


    public Map<String, Object> getFaceIDResult(String code) throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("api_key", face_api_key);
        map.put("api_secret", face_api_secret);
        Jedis jedis1 = RedisUtil.getJedis();
        Map m1 = jedis1.hgetAll(code);
        String biz_id = (String) m1.get("biz_id");
        String totalamount = (String) m1.get("totalamount");
        double amount = Double.parseDouble(totalamount);
        jedis1.close();
        map.put("biz_id", biz_id);//TODO!!!!
        map.put("return_image", "4");
        String res = faceIdGetResult(map);
        if (res == null || "".equals(res)) {
            logger.info("face++获取返回结果为空");
            return fail("01", "face++获取返回结果为空");
        }

        JSONObject json = new JSONObject(res);
//			“NOT_STARTED”：get_token 之后，并没有调用过 do 方法，还没有开始验证；
//			“PROCESSING”：正在进行 FaceID Lite 验证；
//			“OK”：完成了 FaceID Lite 验证（OK并不表示通过了实名验证，是流程正常结束）；
//			“FAILED”：验证流程未完成或出现异常；
//			"CANCELLED"：用户主动取消了验证流程；
//			"TIMEOUT"：流程超时。
        String status = json.getString("status");
        logger.info("face++响应状态status：" + status);
        Map<String,String> flagMap = new HashMap<String,String>();
        if ("OK".equals(status)) {
            String result = json.getJSONObject("liveness_result").getString("result");
            logger.info("face++响应结果result：" + result);
            if ("PASS".equals(result)) {
                JSONObject json1 = json.getJSONObject("verify_result");
                JSONObject json2 = json1.getJSONObject("result_faceid");
                String confidence = String.valueOf(json2.get("confidence"));

                JSONObject json3 = json2.getJSONObject("thresholds");
                String thresholds = String.valueOf(json3.get("1e-5"));

                //String confidence = json.getJSONObject("verify_result").getJSONObject("result_faceid").getString("confidence");// 分数
                //String thresholds = json.getJSONObject("verify_result").getJSONObject("result_faceid").getJSONObject("thresholds").getString("1e-5");// 置信度阈值
                BigDecimal confidence_bd = new BigDecimal(confidence);
                BigDecimal thresholds_bd = new BigDecimal(thresholds);
                if (confidence_bd.compareTo(thresholds_bd) == -1) {
                    confidence = "0";
                } else {
                    confidence = "50";
                }
                logger.info("获取Face++返回分数值：" + confidence_bd + ",置信度阈值:" + thresholds_bd + ",核心分数：" + confidence);
                String image_best = json.getJSONObject("images").getString("image_best").toString();// 最佳图片
                image_best = image_best.split(",")[1];
                Map<String, Object> paramMap = new HashMap<String, Object>();
                paramMap.put("channel", "11");
                paramMap.put("channelNo", "33");
                String custNo = getCustNoByCode(code);
                paramMap.put("custNo", custNo);// 客户编号
                paramMap.put("attachType", "DOC014");// 影像类型
                paramMap.put("attachName", "Face++返回影像");
                //paramMap.put("token", accessToken);
                paramMap.put("path", face_DataImg_url);
                paramMap.put("fileStream", new ByteArrayInputStream(Base64.decode(image_best.getBytes())));
                logger.info("face++****************影像上传开始");
                //影像上传
                Map<String, Object> attachUploadPerson = attachUploadPerson(paramMap);
                logger.info("face++****************影像上传结果：" + attachUploadPerson);
                if (attachUploadPerson == null || "".equals(attachUploadPerson)) {
                    logger.info("影像上传，返回信息为空");
                    return fail("01", "影像上传，返回信息为空");
                }

                //成功
                if ("00000".equals(attachUploadPerson.get("retFlag"))) {
                    String filePath = (String) attachUploadPerson.get("filePath");
                    // 调用人脸识别接口
                    File file = new File(String.valueOf(filePath));
                    InputStream is = new BufferedInputStream(new FileInputStream(String.valueOf(filePath)));
                    String MD5 = DigestUtils.md5Hex(IOUtils.toByteArray(is));

                    Double faceValue = Double.valueOf(confidence);
                    String typCde = getloan(code);
                    Map<String, Object> resmap = getCustInfo(code);//根据code获取身份基本信息
                    String name = (String) resmap.get("custName");
                    String idNumber = (String) resmap.get("idNo");
                    String mobile = (String) resmap.get("indivMobile");
                    String photo = personFaceService.verifyPhotoByFile(file, MD5);
                    Map<String, Object> fecemap = personFaceService.faceCheckByFaceValue(faceValue, typCde, custNo, name, idNumber, mobile, MD5, "33", photo);
                    //
                    JSONObject jsonObj = new JSONObject(fecemap);
                    JSONObject head = jsonObj.getJSONObject("head");
                    JSONObject body = jsonObj.getJSONObject("body");
                    //人脸识别成功
                    if ("00000".equals(head.getString("retFlag"))) {
                        UAuthCASignRequest carequest = uAuthCASignRequestRepository.findOne(code);
                        String applseq = carequest.getApplseq();
                        Map<String, Object> mapmoxie = getMoxieByApplseq(applseq);
                        String isFundFlag = (String) mapmoxie.get("isFund");
                        String isBankFlag = (String) mapmoxie.get("isBank");
                        String isCarrierFlag = (String) mapmoxie.get("isCarrier");
                        //判断是否已做过运营商
                        if("Y".equals(isCarrierFlag)){//已做过运营商
                            //判断金额
                            if(amount >= limitAmount){
                                //判断是否做过公积金网银
                                if("Y".equals(isFundFlag) || "Y".equals(isBankFlag)){//已做过公积金、网银认证  跳转合同展示
                                    logger.info("乔融豆子***已经通过了人脸识别（得分合格），跳转合同展示");
                                    flagMap.put("faceFlag", "5");
                                    return success(flagMap);//跳转合同展示页面
                                }else{//未做过公积金、网银认证  跳转魔蝎认证页面
                                    logger.info("乔融豆子***已经通过了人脸识别（得分合格），跳转魔蝎");
                                    flagMap.put("faceFlag", "4");
                                    return success(flagMap);//跳转魔蝎页面
                                }
                            }else{
                                logger.info("乔融豆子***已经通过了人脸识别（得分合格），跳转合同展示");
                                flagMap.put("faceFlag", "5");
                                return success(flagMap);//跳转合同展示页面
                            }
                        }else{//没有做运营商验证  跳转运营商
                            logger.info("乔融豆子***已经通过了人脸识别（得分合格），跳转运营商");
                            flagMap.put("faceFlag", "6");
                            return success(flagMap);
                        }

                        //成功跳转下一页面(魔蝎)
//                        if(amount >= limitAmount){
//                            flagMap.put("faceFlag", "4");//跳转魔蝎
//                            return success(flagMap);
//                        } else {
//                            flagMap.put("faceFlag", "5");//跳转合同
//                            return success(flagMap);
//                        }
                    }else if ("N".equals(body.getString("isRetry")) && "N".equals(body.getString("isOK"))
                            && "N".equals(body.getString("isResend"))) {
                        //人脸识别次数已达上限   跳转手持身份证
                        flagMap.put("faceFlag", "2");// 跳转手持身份证
                        return success(flagMap);
                    }else{
                        //人脸识别失败 失败后重新进行face验证
                        Map<String, String> facemap = new HashMap<String, String>();
                        facemap.put("api_key", face_api_key);//TODO!!!!
                        facemap.put("api_secret", face_api_secret);//TODO!!!!
                        facemap.put("comparison_type", "1");
                        facemap.put("face_image_type", "raw_image");
                        facemap.put("idcard_name", name);
                        facemap.put("idcard_number", idNumber);
                        facemap.put("return_url", CommonProperties.get("other.domain").toString()
                                + "/app/appserver/faceReturnUrl?code=" + code);
                        facemap.put("biz_no", "00222111");// 待定
                        facemap.put("notify_url", CommonProperties.get("other.domain").toString()
                                + "/app/appserver/faceNotifyUrl?code=" + code);
                        facemap.put("idcard_mode", "0");
                        Map<String, String> faceIDInterface = faceIDInterface(facemap);
                        logger.info("fece++返回："+faceIDInterface);
                        if (!"".equals(faceIDInterface.get("token")) && faceIDInterface.get("token") != null) {
                            String face_token = faceIDInterface.get("token");
                            String biz_id1 = faceIDInterface.get("biz_id");
                            logger.info("biz_id:"+biz_id1);
                            Jedis jedis2 = RedisUtil.getJedis();
                            Map m2 = jedis2.hgetAll(code);
                            m2.put("biz_id", biz_id1);
                            jedis2.hmset(code, m2);
                            jedis2.close();

                            flagMap.put("facetoken", face_token);
                            flagMap.put("faceFlag", "3");
                            return success(flagMap);//跳转人脸识别
                        } else {
                            logger.info("face++获取token失败");
                            return fail("04","网络通讯异常");
                        }
                    }
                }else {
                    String retcode = (String) attachUploadPerson.get("retFlag");
                    String retmsg = (String) attachUploadPerson.get("retMsg");
                    logger.info("影像上传，返回信息：" + retmsg);
                    return fail(retcode, retmsg);
                }
            }


        } else if ("CANCELLED".equals(status) || "NOT_STARTED".equals(status)){
            //返回当前页
            flagMap.put("faceFlag", "1");//
            return success(flagMap);//跳转合同展示页面
        }else {
            //跳转手持身份证
            flagMap.put("faceFlag", "2");// 跳转手持身份证
            return success(flagMap);
        }

        return success();
    }

    public String getCustNoByCode(String code){
        logger.info("乔融豆子查询是否需要进行人脸识别code:"+code);
        //String typCde = getloan(code);
        UAuthCASignRequest request = uAuthCASignRequestRepository.findOne(code);
        logger.info("乔融豆子查询是否需要进行人脸识别查询订单信息:"+request.toString());
        String orderJson = request.getOrderJson();
        JSONObject json = new JSONObject(orderJson);
        JSONObject jsonObject = (JSONObject)json.get("order");

        String custName = jsonObject.get("custName").toString();
        String idNo = jsonObject.get("idNo").toString();
        logger.info("乔融豆子查询是否需要进行人脸识别根据custName和idNo查询custNo");
        Map<String, Object> crmmap = this.getCustInfoByCustNameAndCertNo(custName, idNo);
        logger.info("乔融豆子查询是否需要进行人脸识别查询到的crm客户信息"+crmmap.toString());
        Map<String, Object> mapBody = HttpUtil.json2Map(crmmap.get("body").toString());
        String custNo = mapBody.get("custNo").toString();
        return custNo;
    }

    /**
     * 合同签署
     * @param map
     * @return
     */
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public Map<String, Object> loanconfirm(Map<String, String> map){
        logger.info("乔融豆子**********合同签章***************开始");
        String code = (String) map.get("code");

        //1.个人借款合同
        String url = EurekaServer.APPCA + "/api/" + code + "/confirm";
        logger.info("唯一码signCode:"+code+" ,个人借款合同签章请求地址url:"+url);
        String result = HttpUtil.restPut(url, super.getToken(), null, 200);
        logger.info("个人借款合同签章返回result:" + result);
        if(StringUtils.isEmpty(result)){
            logger.info("签章系统通信失败");
            return fail("18", "签章系统通信失败");
        }
        Map<String, Object> resultMap = HttpUtil.json2Map(result);
        if(StringUtils.isEmpty(resultMap) || resultMap.isEmpty()){
            logger.info("签章系统通信失败");
            return fail("18", "签章系统通信失败");
        }
        JSONObject head = (JSONObject) resultMap.get("head");
        if (!head.get("retFlag").equals("00000")) {
            logger.info("签章系统通信失败" + String.valueOf(head.get("retMsg")));
            return fail(String.valueOf(head.get("retFlag")),String.valueOf(head.get("retMsg")));
        }
        logger.info("乔融豆子合同签章***个人借款合同签章成功");

        //基础信息获取
        UAuthCASignRequest uauthca = uAuthCASignRequestRepository.findOne(code);
        String applseq = uauthca.getApplseq();
        Jedis jedis2 = RedisUtil.getJedis();
        Map m2= jedis2.hgetAll(code);
        String name = (String) m2.get("name");
        String phone = (String) m2.get("phone");
        String idNo = (String) m2.get("idNo");
        jedis2.close();


        String loginNum = (String) map.get("loginNum");
        String loginEvent = (String) map.get("loginEvent");
        String lendNum = (String) map.get("lendNum");
        String lendEvent = (String) map.get("lendEvent");


        //2.征信合同
        JSONObject orderZX = new JSONObject();
        orderZX.put("custName", name);// 客户姓名
        orderZX.put("idNo", idNo);// 客户身份证号
        orderZX.put("indivMobile", phone);// 客户手机号码
        orderZX.put("applseq", applseq);// 请求流水号

        JSONObject orderZXJson = new JSONObject();// 订单信息json串
        orderZXJson.put("order", orderZX.toString());

        UAuthCASignRequest request = new UAuthCASignRequest();
        request.setSignCode(UUID.randomUUID().toString().replace("-", ""));
        request.setCustName(name);
        request.setCustIdCode(idNo);
        request.setSignType("credit");
        request.setFlag("0");
        request.setApplseq(applseq);
        request.setOrderJson(orderZXJson.toString());
        request.setState("0");
        request.setTimes(0);
        request.setSubmitDate(new Date());
        request.setSysFlag("11");
        uAuthCASignRequestRepository.save(request);
        logger.info("乔融豆子合同签章***征信合同保存成功");


        //3.百融反欺诈
        List<List<Map<String, String>>> content = new ArrayList<>();
        List<Map<String, String>> contentList = new ArrayList<>();
        //登录事件
        Map<String, String> mapLoginEvent = new HashMap();
        mapLoginEvent.put("content", EncryptUtil.simpleEncrypt(loginNum));
        mapLoginEvent.put("reserved6", applseq);
        mapLoginEvent.put("reserved7", "antifraud_login");
        contentList.add(mapLoginEvent);
        //贷款事件
        Map<String, String> mapLendEvent = new HashMap();
        mapLendEvent.put("content", EncryptUtil.simpleEncrypt(lendNum));
        mapLendEvent.put("reserved6", applseq);
        mapLendEvent.put("reserved7", "antifraud_lend");
        contentList.add(mapLendEvent);
        content.add(contentList);

        Map<String, Object> riskmap = new LinkedHashMap<>();
        riskmap.put("idNo", EncryptUtil.simpleEncrypt(idNo));
        riskmap.put("name", EncryptUtil.simpleEncrypt(name));
        riskmap.put("mobile", EncryptUtil.simpleEncrypt(phone));
        riskmap.put("dataTyp", "A501");
        riskmap.put("source", "2");
        riskmap.put("content", content);
        riskmap.put("reserved7", "antifraud_lend");
        riskmap.put("applSeq", applseq);
        riskmap.put("channel", "11");

        AppCmisInfo appCmisInfo = new AppCmisInfo();
        appCmisInfo.setFlag("0");
        appCmisInfo.setInsertTime(new Date());
        appCmisInfo.setRequestMap(riskmap);
        appCmisInfo.setTradeCode(CmisTradeCode.TRADECODE_WWRISK);
        appCmisInfo.setId(UUID.randomUUID().toString().replace("-", ""));
        appCmisInfoRepository.save(appCmisInfo);
        logger.info("乔融豆子合同签章***百融反欺诈成功");


        //4.接口回调
        //http://139.196.45.193:20011/haier/haierCashEntranceCallback?applseq=1325077
        String backurl = back_url + "/haier/haierCashEntranceCallback?applseq=" + applseq;
        logger.info("乔融豆子*******签章回调地址：" + backurl);
        String resData = HttpUploadFile.sendGet(backurl, "");
        logger.info("乔融豆子*******签章回调接口返回数据："+resData);
        if(resData == null || "".equals(resData)){
            return fail("18", "回调接口调用失败");
        }
        JSONObject jb = new JSONObject(resData);
        JSONObject jb0 = (JSONObject) jb.get("head");
        String retFlag = (String)jb0.get("retFlag");
        if(!"00000".equals(retFlag)){
            String retMsg = (String)jb0.get("retMsg");
            return fail("18", retMsg);
        }
        logger.info("乔融豆子合同签章***合同回调成功");

        Jedis jedis = RedisUtil.getJedis();
        jedis.del(code);
        jedis.close();

        logger.info("乔融豆子**********合同签章***************结束");
        return success();
    }

    public Map<String, Object> getMoxieByApplseq(String applseq){
        logger.info("根据申请流水号查询是否做过魔蝎认证**applseq:"+applseq);
        Map<String, Object> map = new HashMap<String, Object>();
        MoxieInfo moxieFund = moxieInfoRepository.getMoxieInfo(applseq, "01");//公积金
        MoxieInfo moxieBank = moxieInfoRepository.getMoxieInfo(applseq, "02");//网银
        MoxieInfo moxieCarrier = moxieInfoRepository.getMoxieInfo(applseq, "03");//运营商
        map.put("isFund","N");
        map.put("isBank","N");
        map.put("isCarrier","N");
        //查询是否做公积金
        if(moxieFund != null){
            map.put("isFund","Y");
        }
        //是否做过网银
        if(moxieBank != null){
            map.put("isBank","Y");
        }
        //是否做过运营商
        if(moxieCarrier != null){
            map.put("isCarrier","Y");
        }
        logger.info("魔蝎认证查询数据："+map.toString());
        return map;
    }

    public void brRegister(String code, String regNum){
        logger.info("乔融豆子******百融注册事件********开始");
        UAuthCASignRequest request = uAuthCASignRequestRepository.findOne(code);
        String applseq = request.getApplseq();

        Jedis jedis2 = RedisUtil.getJedis();
        Map m2= jedis2.hgetAll(code);
        String name = (String) m2.get("name");
        String phone = (String) m2.get("phone");
        String idNo = (String) m2.get("idNo");
        jedis2.close();

        List<String> list = new ArrayList<>();
        list.add(EncryptUtil.simpleEncrypt(regNum));

        Map<String, Object> riskmap = new LinkedHashMap<>();
        riskmap.put("idNo", EncryptUtil.simpleEncrypt(idNo));
        riskmap.put("name", EncryptUtil.simpleEncrypt(name));
        riskmap.put("mobile", EncryptUtil.simpleEncrypt(phone));
        riskmap.put("dataTyp", "A501");
        riskmap.put("source", "2");
        riskmap.put("content", list);
        riskmap.put("reserved7", "antifraud_login");
        riskmap.put("applSeq", applseq);
        riskmap.put("channel", "11");

        AppCmisInfo appCmisInfo = new AppCmisInfo();
        appCmisInfo.setFlag("0");
        appCmisInfo.setInsertTime(new Date());
        appCmisInfo.setRequestMap(riskmap);
        appCmisInfo.setTradeCode(CmisTradeCode.TRADECODE_WWRISK);
        appCmisInfo.setId(UUID.randomUUID().toString().replace("-", ""));
        appCmisInfoRepository.save(appCmisInfo);
        logger.info("乔融豆子******百融注册事件********结束");
    }

    public Map<String, Object> attachUploadPerson(Map<String, Object> paramMap) throws Exception {
        String result = null;
        Map<String, Object> map = new HashMap<String, Object>();

        String channel = (String) paramMap.get("channel");
        String channelNo = (String) paramMap.get("channelNo");
        String custNo = (String) paramMap.get("custNo");
        String attachType = (String) paramMap.get("attachType");
        String attachName = (String) paramMap.get("attachName");

        //FileItem fileItem = (FileItem) paramMap.get("identityCard"); // identityCard
        String path = (String) paramMap.get("path");// 共享盘个人影像文件路径
        InputStream input = (InputStream) paramMap.get("fileStream");
        JSONObject json = new JSONObject();
        // 检验参数
        if (input == null) {
            logger.info("file不能为空");
            throw new Exception("file不能为空！");
        }
        Map<String, Object> checkParamMap = new HashMap<>();
        checkParamMap.put("channel", channel);
        checkParamMap.put("channelNo", channelNo);
        checkParamMap.put("custNo", custNo);
        checkParamMap.put("attachType", attachType);
        checkParamMap.put("attachName", attachName);
        checkParamMap.put("path", path);
        checkParamMap.put("token", paramMap.get("token"));

        Map<String, Object> reqestMap = checkParamMap;

        reqestMap.put("commonCustNo","");
        reqestMap.put("id", "");
        reqestMap.put("applSeq", "");
        reqestMap.put("idNo", "");

        InputStream inputStream = null;
        InputStream inputStream0 = null;
        try {
            inputStream = input;
            StringBuffer filePath = new StringBuffer(path).append(custNo).append(File.separator).append(attachType)
                    .append(File.separator);// File.separator
            createDir(String.valueOf(filePath));
            String fileName = UUID.randomUUID().toString().replaceAll("-", "");
            filePath = filePath.append(fileName).append(".jpg"); // 测试打开
            FileImageOutputStream outImag = new FileImageOutputStream(new File(String.valueOf(filePath)));
            byte[] bufferOut = new byte[1024];
            int bytes = 0;
            while ((bytes = inputStream.read(bufferOut)) != -1) {
                outImag.write(bufferOut, 0, bytes);
            }
            outImag.close();
            inputStream.close();
            if (input != null) {
                input.close();
            }

            InputStream is = new BufferedInputStream(new FileInputStream(String.valueOf(filePath)));
            String MD5 = DigestUtils.md5Hex(IOUtils.toByteArray(is));
            is.close();
            reqestMap.put("md5", MD5);

            reqestMap.put("filePath", String.valueOf(filePath));

//            String url = BusinessConstance.DataConfigMap.get("appServer_token_url").toString()
//                    + "/app/appserver/attachUploadPersonByFilePath";


            File file = new File(String.valueOf(filePath));
            if (!file.exists()) {
                logger.error("文件不存在 filePath:" + filePath);
                map.put("retFlag", "11");
                map.put("retMsg", "文件不存在");
                //JSONObject jb = new JSONObject(fail("11", "文件不存在"));
                return map;
            }

            if (file.length() > getImageMaxSize()) {
                logger.error("文件大小不能超过5M");
                map.put("retFlag", "11");
                map.put("retMsg", "文件大小不能超过5M");
                return map;
            }

            String newFilePath = attachService.getPersonFilePath(custNo, attachType);
            String newFileName = attachService.getFileName(String.valueOf(filePath));
            logger.info("filePath:" + newFilePath + ",fileName:" + newFileName);
            FileUtils.copyFile(file, new File(newFilePath + newFileName));


            String myMd5;
            inputStream0 = new BufferedInputStream(new FileInputStream(String.valueOf(filePath)));
            myMd5 = DigestUtils.md5Hex(IOUtils.toByteArray(inputStream0));
            IOUtils.closeQuietly(inputStream0);

            logger.info("myMd5:" + myMd5);
            if (myMd5 == null) {
                logger.error("文件保存失败");
                map.put("retFlag", "11");
                map.put("retMsg", "文件保存失败");
                return map;
            }
            if (!myMd5.equals(MD5)) {
                logger.debug(String.format("文件md5校验失败: %s :: %s", MD5, myMd5));
                //JSONObject jb = new JSONObject(fail("11", "文件md5校验失败"));
                map.put("retFlag", "11");
                map.put("retMsg", "文件md5校验失败");
                return map;
            }

            AttachFile attachFile;
//            if (StringUtils.isEmpty(applSeq)) {
                attachFile = attachService
                        .saveAttachFile(custNo, attachType, attachName, MD5, newFilePath, newFileName, "");
//            }
//            else {
//                attachFile = attachService
//                        .saveAttachFile(custNo, attachType, attachName, md5, newFilePath, newFileName, commonCustNo, applSeq);
//            }

            //Map<String, Object> resultMap = new HashMap<>();
            map.put("retFlag", "00000");
            map.put("retMsg", "处理成功");
            map.put("id", attachFile.getId());
            map.put("filePath", String.valueOf(filePath));

            /*JSONObject jb = new JSONObject(success(resultMap));

            result = jb.toString();
            logger.info("影像上传接口，返回信息：" + jb);
            json.put("info", jb);
            json.put("filePath", String.valueOf(filePath));*/
            logger.info("影像上传返回 ：" + map.toString());
        } catch (Exception e) {
            logger.error("渠道编码" + channel + "的影像上传接口，调用app后台接口，出现异常：" + e.getMessage(), e);
            IOUtils.closeQuietly(inputStream0);
        }
        return map;
    }

    public static void createDir(String destDirName) {
        File dir = new File(destDirName);
        if (dir.exists()) {
            return;
        }
        if (!destDirName.endsWith(File.separator)) {
            destDirName = destDirName + File.separator;
        }
        // 创建目录
        if (dir.mkdirs()) {
            return;
        }
    }

    public long getImageMaxSize() {
        if (IMAGE_MAXSIZE == 0) {
            IMAGE_MAXSIZE = Long.parseLong(CommonProperties.get("file.imageMaxSize").toString());
            if (IMAGE_MAXSIZE == 0) {
                IMAGE_MAXSIZE = 5 * 1024 * 1024;
            }
        }
        return IMAGE_MAXSIZE;
    }

    /*public String getloan(String code){
        UAuthCASignRequest request = uAuthCASignRequestRepository.findOne(code);
        String applseq = request.getApplseq();
        String loan = getloanByapplseq(applseq);
        return loan;
    }*/

    /*public String getloanByapplseq(String applseq){
        String url = EurekaServer.CMISPROXY + "/api/appl/queryApplFull?applSeq=" + applseq;
        String json = HttpUtil.restGet(url, super.getToken());
        logger.info("通过cmis请求的订单信息的url" + url);
        if (StringUtils.isEmpty(json)) {
            return null;
        }
        Map<String, Object> resultMap = HttpUtil.json2Map(json);
        logger.info("通过cmis请求的订单信息的返回 信息" + resultMap);
        String loan = String.valueOf(resultMap.get("LOAN_TYP"));
        logger.info("贷款品种编码为："+loan);
        return loan;
    }*/

    public Map<String, Object> getCustInfo(String code) {
        UAuthCASignRequest request = uAuthCASignRequestRepository.findOne(code);
        String orderJson = request.getOrderJson();
        JSONObject json = new JSONObject(orderJson);
        JSONObject jsonObject = (JSONObject)json.get("order");

        if(StringUtils.isEmpty(jsonObject.get("custName")) || StringUtils.isEmpty(jsonObject.get("idNo"))
                || StringUtils.isEmpty(jsonObject.get("repayApplCardNo"))
                || StringUtils.isEmpty(jsonObject.get("indivMobile"))){
            return null;
        }
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("custName", String.valueOf(jsonObject.get("custName")));
        resultMap.put("idNo", String.valueOf(jsonObject.get("idNo")));
        resultMap.put("indivMobile", String.valueOf(jsonObject.get("indivMobile")));
        return resultMap;
    }

    public String getloan(String code){
        UAuthCASignRequest request = uAuthCASignRequestRepository.findOne(code);
        String applseq = request.getApplseq();
        String loan = getloanByapplseq(applseq);
        return loan;
    }

    public String getloanByapplseq(String applseq){
        String url = EurekaServer.CMISPROXY + "/api/appl/queryApplFull?applSeq=" + applseq;
        String json = HttpUtil.restGet(url, super.getToken());
        logger.info("通过cmis请求的订单信息的url" + url);
        if (StringUtils.isEmpty(json)) {
            return null;
        }
        Map<String, Object> resultMap = HttpUtil.json2Map(json);
        logger.info("通过cmis请求的订单信息的返回 信息" + resultMap);
        String loan = String.valueOf(resultMap.get("LOAN_TYP"));
        logger.info("贷款品种编码为："+loan);
        return loan;
    }

    /**
     * 验证并新增实名认证信息
     * @param code
     * @return
     */
    public Map<String, Object> check(String code) {
        UAuthCASignRequest request = uAuthCASignRequestRepository.findOne(code);
        String orderJson = request.getOrderJson();
        JSONObject json = new JSONObject(orderJson);
        JSONObject jsonObject = (JSONObject)json.get("order");

        if(StringUtils.isEmpty(jsonObject.get("custName")) || StringUtils.isEmpty(jsonObject.get("idNo"))
                || StringUtils.isEmpty(jsonObject.get("repayApplCardNo"))
                || StringUtils.isEmpty(jsonObject.get("indivMobile"))){
            return null;
        }
        CustomerInfoBean customerInfoBean = new CustomerInfoBean();
        customerInfoBean.setApptCustName(String.valueOf(jsonObject.get("custName")));
        customerInfoBean.setApptIdNo(String.valueOf(jsonObject.get("idNo")));
        customerInfoBean.setRepayApplCardNo(String.valueOf(jsonObject.get("repayApplCardNo")));
        customerInfoBean.setIndivMobile(String.valueOf(jsonObject.get("indivMobile")));
        customerInfoBean.setAppInAdvice("33");
        customerInfoBean.setUserId(String.valueOf(jsonObject.get("indivMobile")));
        Map<String, Object> resultMap = crmService.checkAndAddFourKeysRealInfo(customerInfoBean);
        return resultMap;
    }


    private static String multipart2File(InputStream stream) {
        String myMd5;
        try {
            byte[] buffer = new byte[1024 * 1024];
            int byteRead;
            MessageDigest messagedigest = MessageDigest.getInstance("MD5");
            while ((byteRead = stream.read(buffer)) != -1) {
                messagedigest.update(buffer, 0, byteRead);
            }
            myMd5 = EncryptUtil.MD5(messagedigest.digest());
            stream.close();
            return myMd5;
        } catch (IOException e) {
            return null;
        } catch (NoSuchAlgorithmException e) {
            return null;
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void fileSave(String attachPath, String attachNameNew, InputStream instream) throws IOException {

        FileOutputStream outStream = null;
        try {
            File file_ = new File(attachPath);
            if (!file_.exists()) {
                file_.mkdirs();
            }
            String url = attachPath + attachNameNew;
            outStream = new FileOutputStream(attachPath + attachNameNew);
            byte b[] = new byte[1024];
            int n;
            while ((n = instream.read(b)) != -1) {
                outStream.write(b, 0, n);
            }
            outStream.flush();
            outStream.close();
            instream.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (outStream != null) {
                try {
                    outStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    logger.error("魔蝎接口保存文件关闭流失败！");
                }
            }
            if (instream != null) {
                try {
                    instream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    logger.error("魔蝎接口保存文件关闭流失败！");
                }
            }
        }

    }

    public static String zipImageFile(File oldFile, File newFile, int width, int height, float quality) {
        if (oldFile == null) {
            return null;
        }
        try {
            /** 对服务器上的临时文件进行处理 */
            Image srcFile = ImageIO.read(oldFile);
            int w = srcFile.getWidth(null);
            int h = srcFile.getHeight(null);
            double bili;
            if (width > 0) {
                bili = width / (double) w;
                height = (int) (h * bili);
            } else {
                if (height > 0) {
                    bili = height / (double) h;
                    width = (int) (w * bili);
                }
            }

            String srcImgPath = newFile.getAbsoluteFile().toString();
            System.out.println(srcImgPath);
            String subfix = "jpg";
            subfix = srcImgPath.substring(srcImgPath.lastIndexOf(".") + 1, srcImgPath.length());

            BufferedImage buffImg = null;
            if (subfix.equals("png")) {
                buffImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            } else {
                buffImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            }

            Graphics2D graphics = buffImg.createGraphics();
            graphics.setBackground(new Color(255, 255, 255));
            graphics.setColor(new Color(255, 255, 255));
            graphics.fillRect(0, 0, width, height);
            graphics.drawImage(srcFile.getScaledInstance(width, height, Image.SCALE_SMOOTH), 0, 0, null);

            ImageIO.write(buffImg, subfix, new File(srcImgPath));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return newFile.getAbsolutePath();
    }

    public static String getMontnTime() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy_MM");
        return df.format(new Date());
    }

    public Map<String,Object> getCustInfoByCustNameAndCertNo(String custName, String idNO){
        String url = EurekaServer.CRM + "/pub/crm/cust/getCustInfoByCustNameAndCertNo" + "?custName=" + custName + "&certNo=" +idNO;
        logger.info("乔融豆子根据custName和idNO请求crm:" + url);
        String json = HttpUtil.restGet(url, null);
        Map<String, Object> crmResultMap = HttpUtil.json2Map(json);
        logger.info("乔融豆子根据custName和idNO请求crm返回结果" + crmResultMap);
        return crmResultMap;
    }

    /**
     * 根据身份证号查询客户信息
     * @param idNo
     * @return
     */
    public Map<String,Object> getCustInfoByCertNo(String idNo){
        String url = EurekaServer.CRM + "/app/crm/cust/getCustInfoByCertNo?certNo=" +idNo;
        logger.info("根据身份证号查询客户信息请求crm:" + url);
        String json = HttpUtil.restGet(url, null);
        Map<String, Object> crmResultMap = HttpUtil.json2Map(json);
        logger.info("根据身份证号查询客户信息请求crm返回结果" + crmResultMap);
        return crmResultMap;
    }

    public Map<String, Object> fileUpload(MultipartFile handImg, HttpServletRequest request, HttpServletResponse response) {
        // 判断文件是否为空
        logger.info("*************************乔融豆子人脸识别接口**********************");
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            String idNo = request.getParameter("idNo");
            String custName = request.getParameter("custName");
            String code = request.getParameter("code");
            //custName解密
            BASE64Decoder decoder = new BASE64Decoder();
//            byte[] b = decoder.decodeBuffer(custName);
//            custName = new String(b, "utf-8");
//            logger.info("乔融豆子人脸识别接口:" + "idNo:" + idNo + "custName:" + custName);
            if (!handImg.isEmpty()) {
                String filename = handImg.getOriginalFilename();
                String fileTradeDate = getMontnTime();
                String attachPath = String.valueOf(CommonProperties.get("file.dzImageFolder")) + "/" + fileTradeDate + "/";//下载路径
                String fileExt = filename.substring(filename.lastIndexOf(".") + 1); // 文件后缀名(去掉点号)
                filename = UUID.randomUUID().toString().replace("-", "") + "." + fileExt;
                InputStream stream = handImg.getInputStream();
                //保存
                fileSave(attachPath, filename, stream);
                logger.info("乔融豆子人脸识别接口图片保存成功");
                //压缩
                String newpath = attachPath + filename;
                zipImageFile(new File(newpath), new File(newpath), 425, 638, 0.7f);
                logger.info("乔融豆子人脸识别接口图片压缩成功");
//              InputStream stream1 = new FileInputStream(new File(newpath));
//              String md5 = multipart2File(stream1);

                //调用APP人脸识别
                InputStream is = new BufferedInputStream(new FileInputStream(String.valueOf(newpath)));
                String MD5 = DigestUtils.md5Hex(IOUtils.toByteArray(is));//
                //校验图片
                File file = new File(newpath);
                if (!file.exists()) {
                    logger.error("文件不存在 filePath:" + newpath);
                    throw new Exception("文件不存在 filePath:" + newpath);
                }
                String photo = personFaceService.verifyPhotoByFile(file, MD5);
                if (StringUtils.isEmpty(photo)) {
                    Map<String, Object> dataMap = new HashMap<>();
                    dataMap.put("isOK", "N");
                    dataMap.put("isRetry", "N");//不需要重新采集人脸照片，APP重新调接口就行
                    dataMap.put("isResend", "Y");
                    logger.error("网络通讯异常(照片上传失败)，请联系客服(4000187777)或稍后重试");
                    map.put("retFlag", "00099");
                    map.put("retMsg", "网络通讯异常(照片上传失败)，请联系客服(4000187777)或稍后重试");
                    return map;
                }

                Map<String,Object> crmmap = getCustInfoByCustNameAndCertNo(custName, idNo);
                if(crmmap == null){
                    logger.info("根据客户姓名和身份证查询不到客户信息");
                    map.put("retFlag", "00099");
                    map.put("retMsg", "无此客户信息");
                    return map;
                }
                logger.info("乔融豆子人脸识别接口crm返回信息：" + crmmap);
                Map<String, Object> mapBody = HttpUtil.json2Map(crmmap.get("body").toString());
                String custNo = mapBody.get("custNo").toString();
                String mobile = mapBody.get("mobile").toString();
                UAuthCASignRequest uauthca = uAuthCASignRequestRepository.findOne(code);
                //String applSeq = uauthca.getApplseq();
                logger.info("乔融豆子人脸识别接口调用app后台人脸识别接口开始");

                String typCde = getloan(code);
                Map<String, Object> outResultMap = personFaceService.faceCheckByTypCde(custName, idNo, mobile, MD5, "33", typCde, custNo, photo);
                logger.info("乔融豆子人脸识别接口调用app后台人脸识别接口返回：" + outResultMap);

                String retFlag = ((ResultHead)outResultMap.get("head")).getRetFlag();
                if ("00000".equals(retFlag)) {
                    map.put("retFlag", "00000");
                    map.put("retMsg", "人脸验证成功");
                } else {
                    map.put("retFlag", "00099");
                    map.put("retMsg", "人脸验证失败");
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.info("异常信息：" + e.getMessage(), e);
            map.put("retFlag", "00099");
            map.put("retMsg", "人脸接口异常");
        }
        return map;
    }

    public String multipart2File(MultipartFile multipartFile, String filePath, String fileName) {
        String myMd5;
        FileOutputStream fs = null;
        InputStream stream = null;
        try {
            File dir = new File(filePath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            fs = new FileOutputStream(filePath + fileName);
            stream = multipartFile.getInputStream();
            byte[] buffer = new byte[1024 * 1024];
            int byteRead;
            MessageDigest messagedigest = MessageDigest.getInstance("MD5");
            while ((byteRead = stream.read(buffer)) != -1) {
                fs.write(buffer, 0, byteRead);
                fs.flush();
                messagedigest.update(buffer, 0, byteRead);
            }
            myMd5 = EncryptUtil.MD5(messagedigest.digest());
            fs.close();
            stream.close();
            return myMd5;
        } catch (IOException e) {
            logger.error(e.getMessage());
            return null;
        } catch (NoSuchAlgorithmException e) {
            logger.error(e.getMessage());
            return null;
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    logger.error("保存上传的文件，并生成MD5码关闭流失败！");
                }
            }
            if (fs != null) {
                try {
                    fs.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    logger.error("保存上传的文件，并生成MD5码关闭流失败！");
                }
            }
        }
    }

}
