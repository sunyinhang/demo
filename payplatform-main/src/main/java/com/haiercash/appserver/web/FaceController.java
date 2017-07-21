package com.haiercash.appserver.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.haiercash.common.config.EurekaServer;
import com.haiercash.appserver.service.CmisApplService;
import com.haiercash.appserver.service.MerchFaceService;
import com.haiercash.appserver.service.PersonFaceService;
import com.haiercash.appserver.util.ConstUtil;
import com.haiercash.appserver.util.HttpClient;
import com.haiercash.appserver.util.face.FaceImagePo;
import com.haiercash.commons.util.*;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sun.misc.BASE64Encoder;

import javax.imageio.stream.FileImageInputStream;
import java.io.*;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 人脸识别
 */
@RestController
@EnableRedisHttpSession
public class FaceController extends BaseController {
    public static String MODULE_NO = "81";

    public FaceController() {
        super(MODULE_NO);
    }

    @Autowired
    CmisApplService cmisApplService;
    @Autowired
    private MerchFaceService merchFaceService;
    @Autowired
    private PersonFaceService personFaceService;
    @Value("${common.address.gateIp}")
    private String gateIp;


    /**
     * 人脸识别（接口版本2）
     *
     * @param idNumber 身份证号
     * @param name     姓名
     * @param mobile   手机号
     * @param source   来源：1-商户版，2-个人版 16-星巢贷
     * @param file     人脸照片文件流
     * @param MD5      人脸照片MD5校验码，如果不传此参数，不进行MD5校验
     * @return
     */
    @RequestMapping(value = "/app/appserver/faceCheck2", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> faceCheck2(@RequestParam("idNumber") String idNumber, @RequestParam("name") String name,
                                          @RequestParam("mobile") String mobile, @RequestParam("source") String source,
                                          @RequestParam MultipartFile file, @RequestParam("MD5") String MD5,
                                          String applSeq, String commonCustNo, String custNo) {
        try {
            // 数据解密
            idNumber = EncryptUtil.simpleDecrypt(idNumber);
            name = EncryptUtil.simpleDecrypt(name);
            mobile = EncryptUtil.simpleDecrypt(mobile);

            //2. 校验图片
            String photo = personFaceService.verifyPhoto(file, MD5);
            logger.info("photo:" + photo);
            if (StringUtils.isEmpty(photo)) {
                Map<String, Object> dataMap = new HashMap<>();
                dataMap.put("isOK", "N");
                dataMap.put("isRetry", "N");//不需要重新采集人脸照片，APP重新调接口就行
                dataMap.put("isResend", "Y");
                logger.error("网络通讯异常(照片上传失败)，请联系客服(4000187777)或稍后重试");
                return fail("01", "网络通讯异常(照片上传失败)，请联系客服(4000187777)或稍后重试", dataMap);
            }
//            if (StringUtils.isEmpty(faceImgTempPath)) {
//                logger.error("faceImgTemp路径尚未配置");
//                throw new Exception("faceImgTemp路径尚未配置");
//            }
//            File newFile = new File(faceImgTempPath + file.getOriginalFilename());
//            if (!newFile.exists()) {
//                newFile.createNewFile();
//            }
//            file.transferTo(newFile);

            if ("1".equals(source)) {
                return merchFaceService.faceCheck(name, idNumber, mobile, null, MD5, source, applSeq, commonCustNo, photo);
            } else if ("16".equals(source)) {//星巢贷
                return personFaceService.XCDFaceCheck(name, idNumber, mobile, null, MD5, source, applSeq, custNo, photo);
            } else {
                return personFaceService.faceCheck(name, idNumber, mobile, null, MD5, source, applSeq, custNo, photo);
            }
        } catch (Exception e) {
            logger.error("人脸识别接口异常：" + e.getMessage());
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("isOK", "N");
            dataMap.put("isRetry", "N");
            dataMap.put("isResend", "Y");
            return fail(RestUtil.ERROR_INTERNAL_CODE, "网络通讯异常(99)，请联系客服(4000187777)或稍后重试", dataMap);
        }
    }

    @RequestMapping(value = "/app/appserver/faceCheckByFilePath", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> faceCheckByFilePath(@RequestParam("idNumber") String idNumber, @RequestParam("name") String name,
                                                   @RequestParam("mobile") String mobile, @RequestParam("source") String source,
                                                   @RequestParam String filePath, @RequestParam("MD5") String MD5,
                                                   String applSeq, String commonCustNo, String custNo) {
        try {
            // 数据解密
            idNumber = EncryptUtil.simpleDecrypt(idNumber);
            name = EncryptUtil.simpleDecrypt(name);
            mobile = EncryptUtil.simpleDecrypt(mobile);

            if (StringUtils.isEmpty(filePath)) {
                return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "文件路径不能为空");
            }

            //校验图片
            File file = new File(filePath);
            if (!file.exists()) {
                logger.error("文件不存在 filePath:" + filePath);
                throw new Exception("文件不存在 filePath:" + filePath);
            }
            String photo = personFaceService.verifyPhotoByFile(file, MD5);
            logger.info("photo:" + photo);
            if (StringUtils.isEmpty(photo)) {
                Map<String, Object> dataMap = new HashMap<>();
                dataMap.put("isOK", "N");
                dataMap.put("isRetry", "N");//不需要重新采集人脸照片，APP重新调接口就行
                dataMap.put("isResend", "Y");
                logger.error("网络通讯异常(照片上传失败)，请联系客服(4000187777)或稍后重试");
                return fail("01", "网络通讯异常(照片上传失败)，请联系客服(4000187777)或稍后重试", dataMap);
            }

            logger.info("source:" + source + ",channelNo:" + super.getChannelNO());
            if ("11".equals(source) && "34".equals(super.getChannelNO())) {//集团大数据
                source = super.getChannelNO();
            }
            if ("1".equals(source)) {
                return merchFaceService.faceCheck(name, idNumber, mobile, file, MD5, source, applSeq, commonCustNo, photo);
            } else if ("16".equals(source)) {//星巢贷
                return personFaceService.XCDFaceCheck(name, idNumber, mobile, file, MD5, source, applSeq, custNo, photo);
            } else {
                return personFaceService.faceCheck(name, idNumber, mobile, file, MD5, source, applSeq, custNo, photo);
            }
        } catch (Exception e) {
            logger.error("人脸识别接口异常：" + e.getMessage());
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("isOK", "N");
            dataMap.put("isRetry", "N");
            dataMap.put("isResend", "Y");
            return fail(RestUtil.ERROR_INTERNAL_CODE, "网络通讯异常(99)，请联系客服(4000187777)或稍后重试", dataMap);
        }
    }

    /**
     * 人脸识别
     *
     * @param idNumber
     * @param name
     * @param file
     * @return
     * @throws IOException
     */
    @Deprecated
    @RequestMapping(value = "/app/appserver/faceCheck", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> faceCheck(@RequestParam("idNumber") String idNumber, @RequestParam("name") String name,
                                         @RequestParam("mobile") String mobile, @RequestParam("source") String source,
                                         String applSeq,
                                         @RequestParam MultipartFile file, String MD5) {
        // 数据解密
        idNumber = EncryptUtil.simpleDecrypt(idNumber);
        name = EncryptUtil.simpleDecrypt(name);
        mobile = EncryptUtil.simpleDecrypt(mobile);
        Boolean isOK = false;
        Boolean isRetry = false;
        String msg = "";
        Integer faceValue = -1;
        Integer faceCount = 0;
        if (!"1".equals(source)) {//商户版不做限制
            // 已经做过人脸识别的，避免重复调用接口
            Map<String, Integer> hisMap = getFaceValueHis(name, idNumber);
            faceValue = hisMap.get("faceValue");
            faceCount = hisMap.get("faceCount");
        }
        if (faceValue > 0) {
            msg = "人脸识别校验通过。检测分值：" + faceValue;
            isOK = true;
        } else if (faceValue == 0) {
            msg = "人脸识别校验不通过。检测分值：" + faceValue;
        } else {
            // 保存到临时文件
            String fn = "./" + UUID.randomUUID().toString() + ".jpg";
            try {
                FileOutputStream fos = new FileOutputStream(fn);
                fos.write(file.getBytes());
                IOUtils.closeQuietly(fos);
                faceCount++;
                //MD5校验
                if (!StringUtils.isEmpty(MD5)) {
                    FileInputStream fis = new FileInputStream(fn);
                    byte[] buffer = new byte[1024 * 1024];
                    int byteRead;
                    MessageDigest messagedigest = MessageDigest.getInstance("MD5");
                    while ((byteRead = fis.read(buffer)) != -1) {
                        messagedigest.update(buffer, 0, byteRead);
                    }
                    String md5 = EncryptUtil.MD5(messagedigest.digest());
                    IOUtils.closeQuietly(fis);
                    if (!md5.equals(MD5)) {
                        return fail("01", "图片校验失败!");
                    }
                }

                Map<String, Object> checkMap = faceCheckProxy(idNumber, name, fn, faceCount, mobile, source, applSeq);
                ResultHead head = (ResultHead) checkMap.get("head");
                if (head.getRetFlag().endsWith("99")) {
                    //接口调用异常，返回失败
                    return fail("99", head.getRetMsg());
                }
                isOK = head.getRetFlag().equals("00000");
                msg = isOK ? checkMap.get("body").toString() : head.getRetMsg();
            } catch (Exception e) {
                logger.error("人脸识别失败：IO异常 ==> " + e.getMessage());
                msg = "人脸识别失败：IO异常";
            } finally {
                // 删除临时文件
                File f = new File(fn);
                if (f.exists()) {
                    f.delete();
                }
            }
        }
        //返回检测结果和检测次数
        Map<String, Object> resultMap = new HashMap();
        resultMap.put("isOK", isOK);
        resultMap.put("msg", msg);
        resultMap.put("count", faceCount);
        Map<String, Object> ret = success(resultMap);
        if (!isOK && "1".equals(source)) {
            //兼容商户版旧版本（830以前版本），如果人脸识别不通过，retFlag返回错误码
            ResultHead retHead = (ResultHead) ret.get("head");
            retHead.setRetFlag("A8120");
            retHead.setRetMsg(msg);
        }
        return ret;
    }

    /**
     * 通过外联平台转发接口请求
     *
     * @param idNumber
     * @param name
     * @param photo
     * @return
     */
    @Deprecated
    private Map<String, Object> faceCheckProxy(String idNumber, String name, String photo, Integer faceCount,
                                               String mobile, String source, String applSeq) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String URL = gateIp + "/app/face/facecompare";

            byte[] data;
            FileImageInputStream input;
            input = new FileImageInputStream(new File(photo));
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int numBytesRead;
            while ((numBytesRead = input.read(buf)) != -1) {
                output.write(buf, 0, numBytesRead);
            }
            data = output.toByteArray();
            output.close();
            input.close();

            BASE64Encoder encode = new BASE64Encoder();
            String s = encode.encode(data);
            s = URLEncoder.encode(s, "UTF-8");
            FaceImagePo faceImagePo = new FaceImagePo();
            faceImagePo.setAppno(UUID.randomUUID().toString().replace("-", ""));// 申请流水号
            faceImagePo.setApptime(new SimpleDateFormat("yyyyMMdd").format(new Date()));// 申请时间
            faceImagePo.setFilestream(s);
            faceImagePo.setIdentityCardNo(idNumber);
            faceImagePo.setPersonalName(name);
            faceImagePo.setFilestreamname("1465460909935.jpg");
            String requestJson = objectMapper.writeValueAsString(faceImagePo);
            logger.debug("通过外联平台进行人脸识别。URL：" + URL + "，参数：" + faceImagePo);
            String resp = HttpClient.sendJson(URL, requestJson);
            logger.debug("外联平台人脸识别返回：" + resp);

            return parseCheckResult(resp, idNumber, name, faceCount, mobile, source, applSeq);
        } catch (Exception e) {
            logger.error("调用人脸识别接口异常：" + e.getMessage());
            return fail("99", "调用人脸识别接口异常，请重试");
        }
    }

    /**
     * 解析处理人脸识别返回结果
     *
     * @param jo
     * @param idNumber
     * @param name
     * @return
     */
    @Deprecated
    private Map<String, Object> parseCheckResult(JSONObject jo, String idNumber, String name, Integer faceCount,
                                                 String mobile, String source, String applSeq) {
        if (!"0000".equals(jo.get("code"))) {
            return fail("99", "调用人脸识别接口异常，请重试");
        }

        JSONObject result = new JSONObject(jo.getString("message"));
        String code = result.getString("code");
        if (code.equals("1001")) {
            JSONObject entity = result.getJSONObject("entity");
            String score = entity.getString("score");
            Integer faceThreshold = "1".equals(source) ? (Integer) CommonProperties.get("other.faceThreshold2")
                    : (Integer) CommonProperties.get("other.faceThreshold");

            if (!"1".equals(source)) { //商户版不更新人脸识别结果
                // 调用crm人脸识别结果保存接口
                String crmUrl = EurekaServer.CRM + "/app/crm/cust/updateFaceValue";
                Map<String, Object> crmParams = new HashMap<>();
                crmParams.put("identifyNo", idNumber);
                crmParams.put("custName", name);
                crmParams.put("faceValue", score);
                crmParams.put("faceCount", faceCount);
                String resultJson = HttpUtil.restPost(crmUrl, super.getToken(), JSONObject.valueToString(crmParams), 200);
                logger.debug("调用crm人脸识别结果保存接口: " + resultJson);
            }
            //调用风险采集接口把人脸识别认知提交给信贷系统
            //String riskUrl = CommonProperties.get("address.gateUrl") + "/app/appserver/updateRiskInfo";
            Map<String, Object> riskParams = new HashMap();
            riskParams.put("idNo", EncryptUtil.simpleEncrypt(idNumber));
            riskParams.put("name", EncryptUtil.simpleEncrypt(name));
            riskParams.put("mobile", EncryptUtil.simpleEncrypt(mobile));
            riskParams.put("dataTyp", "01"); //01-人脸识别分值
            riskParams.put("source", source);
            if ("34".equals(source)) {//集团大数据
                riskParams.put("channel", "11");
                riskParams.put("channelNo", "34");
            }
            List<String> valueList = new ArrayList();
            valueList.add(EncryptUtil.simpleEncrypt(score));
            riskParams.put("content", valueList);
            //  String riskResult = HttpUtil.restPost(riskUrl, super.getToken(), JSONObject.valueToString(riskParams), 200);
            Map<String, Object> riskResult = cmisApplService.updateRiskInfo(riskParams);
            logger.debug("上传人脸识别信息到信贷系统：" + riskResult);

            if (Integer.valueOf(score) >= faceThreshold) {
                return success("人脸识别校验通过。检测分值：" + score);
            } else {
                return fail("11", "人脸识别校验不通过。检测分值：" + score);
            }
        } else if (code.equals("2011") || code.equals("2012") || code.equals("2014")) {
            return fail("12", "人脸识别校验不通过。返回码：" + code);
        } else {
            return fail("99", "调用人脸识别接口异常，请重试。返回码：" + code);
        }
    }

    @Deprecated
    private Map<String, Object> parseCheckResult(String json, String idNumber, String name, Integer faceCount,
                                                 String mobile, String source, String applSeq) {
        JSONObject result = new JSONObject(json);
        return parseCheckResult(result, idNumber, name, faceCount, mobile, source, applSeq);
    }

    /**
     * 获取人脸识别阈值(供CRM用)
     *
     * @return
     */
    @RequestMapping(value = "/app/appserver/getFaceThreshold", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> getFaceThreshold() {
        Integer faceThreshold = (Integer) CommonProperties.get("other.faceThreshold");
        Map<String, Object> faceThresholdMap = new HashMap<>();
        faceThresholdMap.put("faceThreshold", faceThreshold);
        return faceThresholdMap;
    }

    /**
     * 从CRM获取人脸识别历史结果
     *
     * @param name
     * @param idNo
     * @return 接口失败或从未做过实名认证，返回-1；历史为通过，返回100；历史为不通过，返回0
     */
    @Deprecated
    private Map<String, Integer> getFaceValueHis(String name, String idNo) {
        Integer faceValue = -1;
        Integer faceCount = 0;
        String url = EurekaServer.CRM + "/app/crm/cust/queryMerchCustInfo?custName=" + name + "&certNo=" + idNo;
        Map<String, Object> result = HttpUtil.restGetMap(url);
        if (HttpUtil.isSuccess(result)) {
            Map<String, Object> bodyMap = (Map<String, Object>) result.get("body");
            String value = (String) bodyMap.get("faceValue");
            faceCount = bodyMap.containsKey("faceCount") ? Integer.valueOf(bodyMap.get("faceCount").toString()) : 3;
            if (value.equals("1")) {
                faceValue = 100;
            } else if (value.equals("2")) {
                faceValue = faceCount >= 3 ? 0 : -1;//如果次数不超过3次，则允许再次进行人脸识别
            } else {
                faceValue = -1;
            }
        }

        Map<String, Integer> resultMap = new HashMap();
        resultMap.put("faceValue", faceValue);
        resultMap.put("faceCount", faceCount);
        return resultMap;
    }

    /**
     * 判断是否需要进行人脸识别
     *
     * @param orderNo
     * @param source       1-APP商户版 2-APP个人版 不上送为商户版
     * @param commonCustNo 申请人,不传此参数；共同还款人，必传此参数---个人版无此业务，此参数未用
     * @return
     */
    @RequestMapping(value = "/app/appserver/ifNeedFaceCheckByTypCde", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> ifNeedFaceCheckByTypCde(String orderNo, String source, String commonCustNo, String custNo, String custName, String idNo) {
        try {
            if ("1".equals(source) || StringUtils.isEmpty(source)) {//商户版
                if (StringUtils.isEmpty(orderNo)) {//订单号必传
                    return fail("80", "订单号不能为空！");
                }
                return merchFaceService.ifNeedFaceCheckByTypCde(orderNo, source, commonCustNo);
            } else {//个人版
                if (StringUtils.isEmpty(orderNo)) { // 额度申请和提额时，订单号可以为空
                    if (StringUtils.isEmpty(custNo)) {
                        return fail("80", "客户编号不能为空！");
                    }
                    if (StringUtils.isEmpty(custName)) {
                        return fail("80", "姓名不能为空！");
                    }
                    if (StringUtils.isEmpty(idNo)) {
                        return fail("80", "证件号不能为空！");
                    }
                }
                logger.info("source:" + source + ",channelNo:" + super.getChannelNO());
                if ("11".equals(source) && "34".equals(super.getChannelNO())) {//集团大数据
                    source = super.getChannelNO();
                }
                return personFaceService.ifNeedFaceCheckByTypCde(orderNo, source, custNo, custName, idNo);
            }

        } catch (Exception e) {
            logger.error("判断是否需要进行人脸识别接口异常：" + e.getMessage());
            return fail(RestUtil.ERROR_INTERNAL_CODE, "网络通讯异常(99)，请联系客服(4000187777)或稍后重试");
        }
    }


    /**
     * 获取人脸识别阈值（新）
     *
     * @param typCde    额度申请和提额（个人版）可不填，其他必填
     * @param source    来源 1-商户版（默认） 2-个人版
     * @param isEdApply 是否额度申请和提额（个人版必填） Y-是 N-否(默认)
     * @param custNo    额度申请和提额必填
     * @return
     */
    @RequestMapping(value = "/app/appserver/getFaceThreshold2", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> getFaceThreshold(String typCde, @RequestParam("source") String source, String isEdApply, String custNo) {
        return personFaceService.getFaceThreshold(typCde, source, isEdApply, custNo);
    }

    /**
     * 通过人脸分数 判断人脸是否通过
     *
     * @param map source:1-APP商户版 2-APP个人版 16-星巢贷 34-集团大数据 33-乔融豆子
     *            filePath:文件路径（含文件名）
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/app/appserver/faceCheckByFaceValue", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> faceCheckByFaceValue(@RequestBody Map<String, Object> map) throws Exception {

//        ("faceValue") String faceValue, @RequestParam("typCde") String typCde,
//        @RequestParam("custNo") String custNo, @RequestParam("name") String name, @RequestParam("idNumber") String idNumber, @RequestParam("mobile") String mobile,
//        @RequestParam("md5") String md5, @RequestParam("filePath") String filePath, @RequestParam("source") String source
        logger.info("====faceCheckByFaceValue params:" + map.toString());
        String faceValueStr = (String) map.get("faceValue");
        String typCde = (String) map.get("typCde");
        String custNo = (String) map.get("custNo");
        String name = (String) map.get("name");
        String idNumber = (String) map.get("idNumber");
        String mobile = (String) map.get("mobile");
        String md5 = (String) map.get("md5");
        String filePath = (String) map.get("filePath");
        String source = (String) map.get("source");

        if (StringUtils.isEmpty(faceValueStr)) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "人脸分数不能为空");
        }
        if (StringUtils.isEmpty(typCde)) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "贷款品种不能为空");
        }
        if (StringUtils.isEmpty(custNo)) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "客户编号不能为空");
        }
        if (StringUtils.isEmpty(name)) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "姓名不能为空");
        }
        if (StringUtils.isEmpty(idNumber)) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "证件号不能为空");
        }
        if (StringUtils.isEmpty(mobile)) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "手机号码不能为空");
        }
        if (StringUtils.isEmpty(md5)) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "md5不能为空");
        }
        if (StringUtils.isEmpty(source)) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "source不能为空");
        }

        Double faceValue = Double.valueOf(faceValueStr);
        // 数据解密
        custNo = EncryptUtil.simpleDecrypt(custNo);
        name = EncryptUtil.simpleDecrypt(name);
        idNumber = EncryptUtil.simpleDecrypt(idNumber);
        mobile = EncryptUtil.simpleDecrypt(mobile);
        //校验图片
        File file = new File(filePath);
        if (!file.exists()) {
            logger.error("文件不存在 filePath:" + filePath);
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "文件不存在 filePath:" + filePath);
        }
        String photo = personFaceService.verifyPhotoByFile(file, md5);
        logger.info("photo:" + photo);
        if (StringUtils.isEmpty(photo)) {
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("isOK", "N");
            dataMap.put("isRetry", "N");//不需要重新采集人脸照片，APP重新调接口就行
            dataMap.put("isResend", "Y");
            logger.error("网络通讯异常(照片上传失败)，请联系客服(4000187777)或稍后重试");
            return fail("01", "网络通讯异常(照片上传失败)，请联系客服(4000187777)或稍后重试", dataMap);
        }
        logger.info("source:" + source + ",channelNo:" + super.getChannelNO());
        if ("11".equals(source)) {//集团大数据、美分期
            if ("34".equals(super.getChannelNO()) || "35".equals(super.getChannelNO())) {
                source = super.getChannelNO();
            }
        }
        return personFaceService.faceCheckByFaceValue(faceValue, typCde, custNo, name, idNumber, mobile, md5, source, photo);
    }


    /**
     * 通过贷款品种判断是否需要进行人脸识别
     *
     * @param typCde
     * @param source   1-APP商户版 2-APP个人版 16-星巢贷 34-集团大数据 35-美分期
     * @param custNo
     * @param name
     * @param idNumber
     * @return
     */
    @RequestMapping(value = "/app/appserver/ifNeedFaceChkByTypCde", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> ifNeedFaceChkByTypCde(@RequestParam("typCde") String typCde, @RequestParam("source") String source,
                                                     @RequestParam("custNo") String custNo, @RequestParam("name") String name, @RequestParam("idNumber") String idNumber) throws Exception {
        logger.info("source:" + source + ",channelNo:" + super.getChannelNO());
        if ("11".equals(source)) {//集团大数据、美分期
            if ("34".equals(super.getChannelNO()) || "35".equals(super.getChannelNO()))
                source = super.getChannelNO();
        }
        return personFaceService.ifNeedFaceChkByTypCde(typCde, source, custNo, name, idNumber);
    }

    /**
     * 根据贷款品种获取替代影像
     *
     * @param typCde
     * @param source
     * @return
     */
    @RequestMapping(value = "/app/appserver/getReplacedFiles", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> getReplacedFiles(String typCde, @RequestParam("source") String source) {
        logger.info("typCde:" + typCde + ",source:" + source + ", channelNo:" + super.getChannelNO());
        if (StringUtils.isEmpty(source)) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "数据来源不能为空");
        }
        if ("11".equals(source) && "34".equals(super.getChannelNO())) {//集团大数据
            source = super.getChannelNO();
        }
        Map<String, Object> result = personFaceService.getReplacedFiles(typCde, source);

        if (!HttpUtil.isSuccess(result)) {
            logger.error("===查询可替代资料失败(APP-DEFAULT)===");
            logger.error(RestUtil.ERROR_INTERNAL_CODE + result.toString());
            return fail(RestUtil.ERROR_INTERNAL_CODE, result.toString());
        }
        Map<String, Object> resultMap = new HashMap<>();
        String attachTypes = result.get("attachTypes").toString();
        List<Map<String, Object>> attachListResult = (List<Map<String, Object>>) result.get("attachList");
        resultMap.put("attachList", attachListResult);
        return success(resultMap);
    }
}
