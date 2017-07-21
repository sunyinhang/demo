package com.haiercash.appserver.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.haiercash.appserver.util.HttpClient;
import com.haiercash.appserver.util.face.FaceImagePo;
import com.haiercash.common.config.EurekaServer;
import com.haiercash.common.data.AttachFile;
import com.haiercash.common.service.BaseService;
import com.haiercash.commons.util.CommonProperties;
import com.haiercash.commons.util.EncryptUtil;
import com.haiercash.commons.util.HttpUtil;
import com.haiercash.commons.util.RestUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.utils.DateUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import sun.misc.BASE64Encoder;

import javax.imageio.stream.FileImageInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 人脸识别基类
 *
 * @author liuhongbin
 * @date 2016/9/21
 * @description:
 **/
@Service
public abstract class FaceService extends BaseService {
    private static Log logger = LogFactory.getLog(FaceService.class);

    public static String MODULE_NO = "81";
    //个人版人脸识别阈值
    @Value("${common.other.faceThreshold}")
    protected Integer faceThresholdPerson;
    //商户版人脸识别阈值
    @Value("${common.other.faceThreshold2}")
    protected Integer faceThresholdMerchant;
    //人脸识别最大次数
    @Value("${common.other.faceCountLimit}")
    protected Integer faceCountLimit;
    //face++人脸识别最大次数
    @Value("${common.other.bigDataFaceCountLimit}")
    protected Integer bigDataFaceCountLimit;

    @Value("${common.address.gateIp}")
    protected String gateIp;

    //星巢贷人脸默认的贷款品种
    @Value("${common.xcd.typCde}")
    protected String xcdDefaultTypCde;
    //星巢贷人脸识别阈值
    @Value("${common.xcd.faceThreshold}")
    protected Integer xcdFaceThreshold;

    //美分期人脸默认的贷款品种
    @Value("${common.meifenqi.typCde}")
    protected String meifenqiDefaultTypCde;

    //乔融豆子人脸默认的贷款品种
    @Value("${common.qrdz.typCde}")
    protected String qrdzDefaultTypCde;

    /**
     * 影像文件存放地址
     */
    private static String IMAGE_CRM_FOLDER;
    /**
     * crm标识，默认为：crm
     */
    private static String CRM_FLAG;
    /**
     * 影像文件缓存地址，默认为：imageCache
     */
    private static String IMAGE_CACHE;

    @Autowired
    protected CmisApplService cmisApplService;
    @Autowired
    protected AttachService attachService;

    public FaceService() {
        super(MODULE_NO);
    }

    /**
     * 人脸识别主方法
     *
     * @param name     姓名
     * @param idNumber 身份证号
     * @param mobile   手机号
     * @param file     人脸照片文件流
     * @param md5      人脸照片md5校验码
     * @param source   版本类型：1-商户版，2-个人版，16-星巢贷(同个人版) 34-集团大数据；处理流程有所区别
     * @return
     */
    public Map<String, Object> faceCheck(String name, String idNumber, String mobile,
                                         File file, String md5, String source, String applSeq, String commonCustNo, String photo) throws Exception {
        Map<String, Object> dataMap = new HashMap<>();
        //1. 判断历史分值

        Integer faceValue = 0;//人脸识别结果
        Integer faceCount = 0;
        Double faceVal = Double.valueOf("0");//人脸识别分值
        if ("2".equals(source) || "3".equals(source) || "16".equals(source) || "34".equals(source)) { //个人版判断历史分值，商户版不判断
            Map<String, Object> hisMap = checkFaceValueHis(name, idNumber);
            logger.info("checkFaceValueHis返回结果：" + hisMap);
            faceValue = Integer.valueOf(hisMap.get("faceValue").toString());
            faceCount = Integer.valueOf(hisMap.get("faceCount").toString());
            faceVal = Double.valueOf(hisMap.get("faceVal").toString());
            if ("16".equals(source)) {//16-星巢贷
                if (faceVal >= xcdFaceThreshold) { //1 - 通过
                    dataMap.put("isOK", "Y");
                    dataMap.put("isRetry", "N");
                    dataMap.put("isResend", "N");
                    logger.info("校验通过");
                    return success("校验通过", dataMap);
                } else if (faceVal < xcdFaceThreshold && faceCount > faceCountLimit) { //2 - 未通过
                    dataMap.put("isOK", "N");
                    dataMap.put("isRetry", "N");
                    dataMap.put("isResend", "N");
                    logger.error("12,人脸识别已超过上限次数，暂不能继续办理，详询4000187777" + dataMap);
                    return fail("12", "人脸识别已超过上限次数，暂不能继续办理，详询4000187777", dataMap);
                }
            } else {//嗨付
                if (faceValue == 1) { //1 - 通过
                    dataMap.put("isOK", "Y");
                    dataMap.put("isRetry", "N");
                    dataMap.put("isResend", "N");
                    return success("校验通过", dataMap);
                } else if (faceValue == 2 && faceCount > faceCountLimit) { //2 - 未通过
                    dataMap.put("isOK", "N");
                    dataMap.put("isRetry", "N");
                    dataMap.put("isResend", "N");
                    logger.error("12,人脸识别已超过上限次数，暂不能继续办理，详询4000187777" + dataMap);
                    return fail("12", "人脸识别已超过上限次数，暂不能继续办理，详询4000187777", dataMap);
                }
            }
        }

//        //2. 校验图片
//        String photo = verifyPhoto(file, md5);
//        if (StringUtils.isEmpty(photo)) {
//            dataMap.put("isOK", "N");
//            dataMap.put("isRetry", "N");//不需要重新采集人脸照片，APP重新调接口就行
//            dataMap.put("isResend", "Y");
//            logger.error("网络通讯异常(照片上传失败)，请联系客服(4000187777)或稍后重试");
//            return fail("01", "网络通讯异常(照片上传失败)，请联系客服(4000187777)或稍后重试", dataMap);
//        }

        try {
            //无申请流水号:采用人脸配置的第一个厂商
            String defaultTypCode = "APP-DEFAULT";
            String faceType = "P";//默认个人版
            if ("1".equals(source)) {//1-商户版
                faceType = "B";
            } else if ("16".equals(source)) {//星巢贷
                defaultTypCode = xcdDefaultTypCde;
            }
            Map<String, Object> result = getDefaultFacedOrg(defaultTypCode, faceType);
            if (!HttpUtil.isSuccess(result)) {
                logger.error("人脸机构查询失败(" + defaultTypCode + ")");
                dataMap.put("isOK", "N");
                dataMap.put("isRetry", "N");
                dataMap.put("isResend", "Y");
                logger.error(RestUtil.ERROR_INTERNAL_CODE + ",网络通讯异常(" + RestUtil.ERROR_INTERNAL_CODE + ")，请联系客服(4000187777)或稍后重试" + dataMap);
                return fail(RestUtil.ERROR_INTERNAL_CODE, "网络通讯异常(" + RestUtil.ERROR_INTERNAL_CODE + ")，请联系客服(4000187777)或稍后重试", dataMap);
            }
            // 解析人脸识别规则
            List<Map<String, Object>> bodyList = (List<Map<String, Object>>) result.get("body");
            logger.info("bodyList:" + bodyList);
            bodyList = getFaceConfigList(bodyList, source);
            if (bodyList == null || bodyList.size() == 0) {
                logger.error("bodyList为空");
                throw new Exception("bodyList为空");
            }
            String orgName = "";//机构代码
            String providerDesc = "";//机构名称
            for (Map<String, Object> infoMap : bodyList) {
                orgName = (String) infoMap.get("ORG_CHOICE");
                providerDesc = (String) infoMap.get("COM_DESC");
                break;
            }
            logger.info("人脸识别（无申请流水号）采用机构代码:" + orgName + ",机构名称:" + providerDesc);
            //3. 人脸识别比对
            Map<String, Object> checkMap = faceCheckProxy(name, idNumber, photo, orgName);

            //4. 处理比对结果
            checkMap.put("faceCount", faceCount);//用于计算人脸识别次数
            checkMap.put("source", source);//数据来源，处理是否提交crm
            checkMap.put("name", name);//姓名
            checkMap.put("idNumber", idNumber);//身份证号
            checkMap.put("mobile", mobile);//手机号，用于向信贷提交人脸分值
            checkMap.put("fileName", photo);//人脸照片文件名（含路径），用于保存个人影像
            checkMap.put("md5", md5);//人脸照片md5，用于保存个人影像
            checkMap.put("applSeq", StringUtils.isEmpty(applSeq) ? "" : applSeq);//启用applSeq字段,用来存储申请编号reserved6
            return faceCheckDone(checkMap, true, orgName, providerDesc);
        } finally {
            // 删除暂存的人脸识别图片
            File f = new File(photo);
            if (f.exists()) {
                f.delete();
            }
        }
    }

    /**
     * 处理人脸识别对比结果
     *
     * @param checkMap 对比结果信息
     * @param isPerson 是否个人版
     * @return
     */
    protected Map<String, Object> faceCheckDone(Map<String, Object> checkMap, Boolean isPerson, String providerNo, String providerDesc) {
        Double faceValue = Double.valueOf("0");
        Integer faceCount;
        String source = String.valueOf(checkMap.get("source"));
        String name = String.valueOf(checkMap.get("name"));
        String idNumber = String.valueOf(checkMap.get("idNumber"));
        String mobile = String.valueOf(checkMap.get("mobile"));
        String fileName = String.valueOf(checkMap.get("fileName"));
        String md5 = String.valueOf(checkMap.get("md5"));
        String applSeq = String.valueOf(checkMap.get("applSeq"));
        Map<String, Object> dataMap = new HashMap<>();
        boolean isException = false;
        String code = checkMap.get("code").toString();
        if ("0".equals(code)) { //0-成功（计费）
            //需要校验分值faceValue，记录次数faceCount
            faceValue = Double.valueOf(checkMap.get("score").toString());
            faceCount = Integer.valueOf(checkMap.get("faceCount").toString()) + 1;
        } else if ("1".equals(code)) { //1-失败，不可重试（计费）
            //直接记0分，最大次数，不允许再次做人脸识别
            faceValue = Double.valueOf("0");
            faceCount = faceCountLimit + 1;
        } else if ("2".equals(code)) { //2-失败，可重试
            //记录次数
            dataMap.put("isOK", "N");
            dataMap.put("isRetry", "N");
            dataMap.put("isResend", "Y");
            String errCode = String.valueOf(checkMap.get("errCode")); //公安接口返回的错误码，4位数字
            String errMsg = String.valueOf(checkMap.get("errMsg")); //公安接口返回的错误码，4位数字
            logger.error("errCode:" + errCode + ",errMsg:" + errMsg);
            faceCount = Integer.valueOf(checkMap.get("faceCount").toString()) + 1;
            isException = true;
        } else { //异常，可重试
            //记录次数
            dataMap.put("isOK", "N");
            dataMap.put("isRetry", "N");
            dataMap.put("isResend", "Y");
            logger.error("errCode:" + RestUtil.ERROR_INTERNAL_CODE + ",errMsg:" + RestUtil.ERROR_INTERNAL_MSG);
            faceCount = Integer.valueOf(checkMap.get("faceCount").toString()) + 1;
            isException = true;
        }

        //// 人脸识别接口扣费的情况，会往下走 //// 人脸异常的情况下，也记录次数

        //5. 保存人脸分值到crm
        if (isPerson) {//个人版保存，商户版不保存
            saveFaceValueCrm(name, idNumber, faceValue, faceCount, providerNo, providerDesc);
        }

        //6. 上传人脸分值到信贷
        // 默认传得分，没有得分把公安接口返回的错误消息传过去
        String content = "0".equals(code) ? (faceValue.toString() + "(" + providerNo + ")") : String.valueOf(checkMap.get("errMsg"));
        saveFaceValueCmis(name, idNumber, mobile, source, content, applSeq);

        //7. 保存人脸影像
        saveFaceAttach(name, idNumber, fileName, md5, isPerson);

        logger.info("isException:" + isException);
        if (!isException) {
            //处理返回值
            Boolean isOK = false;
            if ("0".equals(code) && "1".equals(source)) {
                isOK = faceValue >= faceThresholdMerchant;
            } else if ("0".equals(code) && ("2".equals(source) || "34".equals(source))) {
                isOK = faceValue >= faceThresholdPerson;
            } else if ("0".equals(code) && "16".equals(source)) {
                isOK = faceValue >= xcdFaceThreshold;
            }

            Boolean isRetry = !isOK && (faceCount < faceCountLimit);//是否允许重试
            dataMap.put("isOK", isOK ? "Y" : "N");
            dataMap.put("isRetry", isRetry ? "Y" : "N");
            dataMap.put("isResend", "N");//计费接口，不允许重发
            if (isOK) {
                return success("人脸识别通过", dataMap);
            } else if (isRetry) {
                logger.error("11,校验失败，您还剩余" + (faceCountLimit - faceCount) + "次机会" + dataMap);
                return fail("11", "校验失败，您还剩余" + (faceCountLimit - faceCount) + "次机会", dataMap);
            } else {
                logger.error("11,人脸识别已超过上限次数，暂不能继续办理，详询4000187777" + dataMap);
                return fail("11", "人脸识别已超过上限次数，暂不能继续办理，详询4000187777", dataMap);
            }
        } else {
            Integer remainCountTem = faceCountLimit - faceCount;
            if (remainCountTem > 0) {
                logger.error("11,校验失败，您还剩余" + remainCountTem + "次机会" + dataMap);
                return fail("11", "校验失败，您还剩余" + remainCountTem + "次机会", dataMap);
            } else {
                dataMap.put("isOK", "N");
                dataMap.put("isRetry", "N");
                dataMap.put("isResend", "N");
                logger.error("11,人脸识别已超过上限次数，暂不能继续办理，详询4000187777" + dataMap);
                return fail("11", "人脸识别已超过上限次数，暂不能继续办理，详询4000187777", dataMap);
            }
        }
    }

    /**
     * 调用外联平台的人脸识别接口
     *
     * @param name         姓名
     * @param idNumber     身份证号
     * @param photo        照片文件路径
     * @param organization 机构代码
     * @return Map {
     * code: 返回码，0-成功，1-失败，不可重试（计费），2-失败，可重试，9-异常
     * message: code=9时为空字符串，其他为人脸识别接口返回消息
     * score: code=0时有效
     * }
     */
    protected Map<String, Object> faceCheckProxy(String name, String idNumber, String photo, String organization) {
        Map<String, Object> resultMap = new HashMap<>();
        FileImageInputStream input = null;
        ByteArrayOutputStream output = null;

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String URL = gateIp + "/app/face/facecompare";

            byte[] data;
            input = new FileImageInputStream(new File(photo));
            output = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int numBytesRead;
            while ((numBytesRead = input.read(buf)) != -1) {
                output.write(buf, 0, numBytesRead);
            }
            data = output.toByteArray();
            IOUtils.closeQuietly(output);
            IOUtils.closeQuietly(input);

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
            faceImagePo.setOrganization(organization);
            String requestJson = objectMapper.writeValueAsString(faceImagePo);
            logger.debug("faceCheckProxy - 通过外联平台进行人脸识别。URL：" + URL + "，参数：" + faceImagePo);
            String resp = HttpClient.sendJson(URL, requestJson);
            logger.debug("faceCheckProxy - 外联平台人脸识别返回：" + resp);
            return parseCheckResult(resp);
        } catch (Exception e) {
            IOUtils.closeQuietly(output);
            IOUtils.closeQuietly(input);
            logger.error("faceCheckProxy - 调用人脸识别接口异常：" + e.getMessage());
            resultMap.put("code", "9");
            resultMap.put("errMsg", "");
            resultMap.put("errCode", "");
        }
        return resultMap;
    }

    /**
     * 解析处理人脸识别返回结果
     *
     * @param json 待解析的JSON串
     * @return 结果码和消息：0-成功，1-失败且计费，2-失败不计费
     */
    protected Map<String, Object> parseCheckResult(String json) {
        JSONObject jo = new JSONObject(json);
        /**
         * 成功返回的json:
         * {"code":"0000","message":"{\"msg\":\"正常返回\",\"code\":\"1001\",
         * \"requestId\":\"7825fea899d44673b74c6efd96d466d7\",
         * \"time\":6,\"entity\":{\"score\":\"56\",\"desc\":\"系统判断为同一人\"}}",
         * "data":null}
         *
         * 失败返回的json:
         * {"code":"0000","message":"{\"msg\":\"参数错误，身份证号格式错误\",\"code\":\"2005\",
         * \"requestId\":\"e929b00e1d9145459f2af7181fdb9697\",
         * \"time\":0,\"entity\":null}",
         * "data":null}
         */
        Map<String, Object> resultMap = new HashMap<>();
        if (!"0000".equals(jo.get("code"))) {
            resultMap.put("code", "9");
            resultMap.put("errMsg", "");
            resultMap.put("errCode", "");
            return resultMap;
        }

        JSONObject message = new JSONObject(jo.getString("message"));
        String code = message.getString("code");
        String msg = message.getString("msg");
        String score = "0";
        if (!message.isNull("entity")) {
            JSONObject entity = message.getJSONObject("entity");
            score = entity.getString("score");
        }
        resultMap.put("score", score);
        resultMap.put("errMsg", msg);
        resultMap.put("errCode", code);//错误码，失败时需要在提示信息显示

        /**
         * 以下为计费的接口，避免同一人重复调；其他返回码为失败且不计费
         * 1001	（计费）正常返回
         * 2011	（计费）身份证号一致但姓名不一致
         * 2012	（计费）姓名与身份证号一致，但身份证照片不存在
         * 2014	（计费）验证身份信息不在查询范围内
         */
        if (code.equals("1001")) {
            resultMap.put("code", "0");
        } else if (code.equals("2011") || code.equals("2012") || code.equals("2014")) {
            resultMap.put("code", "1");
        } else {
            resultMap.put("code", "2");
        }
        return resultMap;
    }

    /**
     * 从CRM获取人脸识别历史结果
     *
     * @param name 姓名
     * @param idNo 身份证号
     * @return Map {
     * faceValue: 历史分值，0 - 未做过，1 - 通过，2 - 未通过，接口失败返回0
     * faceCount: 历史次数，接口失败返回0
     * }
     */
    protected Map<String, Object> checkFaceValueHis(String name, String idNo) throws Exception {
        Integer faceValue = 0;//人脸识别结果
        Integer faceCount = 0;
        Integer faceVal = 0;//人脸识别分值
        String providerNo = "";//人脸识别厂家号
        String providerDesc = "";//人脸识别厂家名称
        String url = EurekaServer.CRM + "/app/crm/cust/queryMerchCustInfo?custName=" + name + "&certNo=" + idNo;
        Map<String, Object> hisMap = HttpUtil.restGetMap(url);
        logger.info("从CRM获取人脸识别历史结果:" + hisMap.toString());
        if (HttpUtil.isSuccess(hisMap)) {
            Map<String, Object> bodyMap = (Map<String, Object>) hisMap.get("body");
            if (bodyMap.containsKey("faceValue")) {
                faceValue = Integer.valueOf(bodyMap.get("faceValue").toString());
            }
            if (bodyMap.containsKey("faceCount")) {
                faceCount = Integer.valueOf(bodyMap.get("faceCount").toString());
            }
            if (bodyMap.containsKey("faceVal")) {
                double faceValueTemp = StringUtils.isEmpty(bodyMap.get("faceVal")) ? 0 : Double.valueOf(String.valueOf(bodyMap.get("faceVal")));
                faceVal = Integer.valueOf(new DecimalFormat("0").format(faceValueTemp));
            }
            if (bodyMap.containsKey("providerNo")) {
                providerNo = StringUtils.isEmpty(bodyMap.get("providerNo")) ? "" : String.valueOf(bodyMap.get("providerNo"));
            }
            if (bodyMap.containsKey("providerDesc")) {
                providerDesc = StringUtils.isEmpty(bodyMap.get("providerDesc")) ? "" : String.valueOf(bodyMap.get("providerDesc"));
            }
        } else {
            logger.info("从CRM获取人脸识别历史结果失败");
            throw new Exception("获取人脸识别历史结果失败");
        }

        Map<String, Object> resultMap = new HashMap();
        resultMap.put("faceValue", faceValue);
        resultMap.put("faceCount", faceCount);
        resultMap.put("faceVal", faceVal);
        resultMap.put("providerNo", providerNo);
        resultMap.put("providerDesc", providerDesc);
        return resultMap;
    }

    /**
     * 人脸照片MD5校验
     *
     * @param file 文件流
     * @param md5  md5码
     * @return 校验成功返回缓存的文件路径，失败返回空字符串
     */
    public String verifyPhoto(MultipartFile file, String md5) {
        // 缓存的文件路径: /testshare01/crm/imageCache/2017-03-29/
        String time = DateUtils.formatDate(new Date(), "yyyy-MM-dd");
        StringBuffer filePathBf = new StringBuffer(getImageCrmFolder()).append(File.separator)
                .append(getCrmFlag()).append(File.separator)
                .append(getImageCache()).append(File.separator)
                .append(time).append(File.separator);
        logger.debug("filePathBf:" + filePathBf);
        File dir = new File(filePathBf.toString());
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String fn = filePathBf + UUID.randomUUID().toString() + ".jpg";
//        String fn = "./" + UUID.randomUUID().toString() + ".jpg";
        logger.info("缓存的文件路径:" + fn);
        FileOutputStream fos = null;
        FileInputStream fis = null;
        try {
            fos = new FileOutputStream(fn);
            fos.write(file.getBytes());
            IOUtils.closeQuietly(fos);
            //MD5校验
            if (!StringUtils.isEmpty(md5)) {
                fis = new FileInputStream(fn);
                byte[] buffer = new byte[1024 * 1024];
                int byteRead;
                MessageDigest messagedigest = MessageDigest.getInstance("MD5");
                while ((byteRead = fis.read(buffer)) != -1) {
                    messagedigest.update(buffer, 0, byteRead);
                }
                String fileMd5 = EncryptUtil.MD5(messagedigest.digest());
                logger.debug("verifyPhoto - 图片文件MD5码：" + fileMd5);
                IOUtils.closeQuietly(fis);
                if (md5.equals(fileMd5)) {
                    return fn;
                } else {
                    logger.error("verifyPhoto - MD5校验失败：（传参）" + md5 + "，（实际）" + fileMd5);
                    throw new Exception("人脸照片MD5校验失败");
                }
            } else {
                //没有传md5码，不做校验（兼容旧版本）
                return fn;
            }
        } catch (Exception e) {
            logger.error("verifyPhoto - 人脸照片校验失败：" + e.getMessage());
            IOUtils.closeQuietly(fos);
            IOUtils.closeQuietly(fis);
            // 校验失败，删除临时文件（成功时需要保留临时文件，用于影像保存）
            File f = new File(fn);
            if (f.exists()) {
                f.delete();
            }
            return "";
        }
    }

    /**
     * 人脸照片MD5校验
     *
     * @param file 文件流
     * @param md5  md5码
     * @return 校验成功返回缓存的文件路径，失败返回空字符串
     */
    public String verifyPhotoByFile(File file, String md5) {
        // 缓存的文件路径: /testshare01/crm/imageCache/2017-03-29/
        String time = DateUtils.formatDate(new Date(), "yyyy-MM-dd");
        StringBuffer filePathBf = new StringBuffer(getImageCrmFolder()).append(File.separator)
                .append(getCrmFlag()).append(File.separator)
                .append(getImageCache()).append(File.separator)
                .append(time).append(File.separator);
        logger.debug("filePathBf:" + filePathBf);
        File dir = new File(filePathBf.toString());
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String fn = filePathBf + UUID.randomUUID().toString() + ".jpg";
//        String fn = "./" + UUID.randomUUID().toString() + ".jpg";
        logger.info("缓存的文件路径:" + fn);
        FileOutputStream fos = null;
        FileInputStream fi = null;
        FileInputStream fis = null;
        try {
            fos = new FileOutputStream(fn);
            fi = new FileInputStream(file);
            long fileSize = file.length();
            byte[] bytes = new byte[1024 * 1024];
            int readLen;
            while ((readLen = fi.read(bytes)) != -1) {
                fos.write(bytes, 0, readLen);
            }
            IOUtils.closeQuietly(fos);
            IOUtils.closeQuietly(fi);
            //MD5校验
            if (!StringUtils.isEmpty(md5)) {
                fis = new FileInputStream(fn);
                byte[] buffer = new byte[1024 * 1024];
                int byteRead;
                MessageDigest messagedigest = MessageDigest.getInstance("MD5");
                while ((byteRead = fis.read(buffer)) != -1) {
                    messagedigest.update(buffer, 0, byteRead);
                }
                String fileMd5 = EncryptUtil.MD5(messagedigest.digest());
                logger.debug("verifyPhoto - 图片文件MD5码：" + fileMd5);
                IOUtils.closeQuietly(fis);
                if (md5.equals(fileMd5)) {
                    return fn;
                } else {
                    logger.error("verifyPhoto - MD5校验失败：（传参）" + md5 + "，（实际）" + fileMd5);
                    throw new Exception("人脸照片MD5校验失败");
                }
            } else {
                //没有传md5码，不做校验（兼容旧版本）
                return fn;
            }
        } catch (Exception e) {
            logger.error("verifyPhoto - 人脸照片校验失败：" + e.getMessage());
            IOUtils.closeQuietly(fos);
            IOUtils.closeQuietly(fi);
            IOUtils.closeQuietly(fis);
            // 校验失败，删除临时文件（成功时需要保留临时文件，用于影像保存）
            File f = new File(fn);
            if (f.exists()) {
                f.delete();
            }
            return "";
        }
    }

    /**
     * 把人脸识别结果保存到crm
     *
     * @param name      姓名
     * @param idNumber  身份证号
     * @param score     人脸识别得分
     * @param faceCount 人脸识别次数
     */
    protected void saveFaceValueCrm(String name, String idNumber, Double score, Integer faceCount, String providerNo, String providerDesc) {
        try {
            // 调用crm人脸识别结果保存接口
            String crmUrl = EurekaServer.CRM + "/app/crm/cust/updateFaceValue";
            Map<String, Object> crmParams = new HashMap<>();
            crmParams.put("identifyNo", idNumber);
            crmParams.put("custName", name);
            crmParams.put("faceValue", score.toString());
            crmParams.put("faceCount", faceCount);
            crmParams.put("providerNo", providerNo);
            crmParams.put("providerDesc", providerDesc);
            logger.debug("saveFaceValueCrm - 调用crm人脸识别结果保存接口: url:" + crmUrl + ",params:" + crmParams);
            Map<String, Object> crmResponse = HttpUtil.restPostMap(crmUrl, super.getToken(), crmParams);
            logger.debug("saveFaceValueCrm - 调用crm人脸识别结果保存接口: " + crmResponse);
        } catch (Exception e) {
            logger.error("saveFaceValueCrm - crm保存人脸识别结果异常：" + e.getMessage());
        }
    }

    /**
     * 上传人脸识别结果到信贷系统
     *
     * @param name     姓名
     * @param idNumber 身份证号
     * @param mobile   手机号
     * @param source   来源：1-商户版，2-个人版
     * @param score    人脸识别得分，或识别错误消息
     */
    protected void saveFaceValueCmis(String name, String idNumber, String mobile, String source, String score, String applSeq) {
        try {
            //调用风险采集接口把人脸识别认知提交给信贷系统
            Map<String, Object> riskParams = new HashMap();
            riskParams.put("idNo", EncryptUtil.simpleEncrypt(idNumber));
            riskParams.put("name", EncryptUtil.simpleEncrypt(name));
            riskParams.put("mobile", EncryptUtil.simpleEncrypt(mobile));
            riskParams.put("dataTyp", "01"); //01-人脸识别分值
            riskParams.put("source", source);
            riskParams.put("applSeq", StringUtils.isEmpty(applSeq) ? "" : applSeq);
            List<String> valueList = new ArrayList();
            valueList.add(EncryptUtil.simpleEncrypt(score));
            riskParams.put("content", valueList);
            Map<String, Object> riskResult = cmisApplService.updateRiskInfo(riskParams);
            logger.debug("saveFaceValueCmis - 上传人脸识别结果到信贷系统：" + riskResult);
        } catch (Exception e) {
            logger.error("saveFaceValueCmis - 信贷上传人脸识别结果异常：" + e.getMessage());
        }
    }

    protected void saveFaceAttach(String name, String idNumber, String fileName, String md5, boolean isPerson) {
        try {
            // 从crm查询客户编号
            String crmUrl = EurekaServer.CRM + "/pub/crm/cust/getCustInfoByCustNameAndCertNo?custName=" + name
                    + "&certNo=" + idNumber;
            Map<String, Object> crmResponse = HttpUtil.restGetMap(crmUrl);
            Map<String, Object> bodyMap = (Map<String, Object>) crmResponse.get("body");
            String custNo = String.valueOf(bodyMap.get("custNo"));
            logger.debug("saveFaceAttach - 客户编号：" + custNo);

            // 保存到个人影像
            AttachFile attachFile = attachService.saveFacePhoto(custNo, md5, fileName, isPerson);
            logger.debug("saveFaceAttach - 人脸照片已保存：" + attachFile);
        } catch (Exception e) {
            logger.error("saveFaceAttach - 保存人脸影像失败：" + e.getMessage());
        }
    }

    /**
     * 根据贷款品种，查询人脸机构信息(人脸识别规则)
     *
     * @param typCde
     * @param source 1-APP商户版 2-APP个人版 16-星巢贷 34-集团大数据 35-美分期 33-乔融豆子
     * @return
     */
    public Map<String, Object> getFacedOrg(String typCde, String source) {
        ///api/appl/getFacedOrg?typCde=16074a&faceType=B
        String faceType;
        logger.debug("getFacedOrg参数：source:" + source + ",typCde:" + typCde);
        typCde = StringUtils.isEmpty(typCde) ? "APP-DEFAULT" : typCde;
        String defaultTypCode;
        if ("2".equals(source) || "16".equals(source) || "34".equals(source) || "35".equals(source) || "33".equals(source) || "14".equals(source)) {//个人版（星巢贷）
            faceType = "P";
            if ("16".equals(source)) {//星巢贷
                return getDefaultFacedOrg(xcdDefaultTypCde, "P");
            } else {//嗨付 集团大数据
                defaultTypCode = "APP-DEFAULT";
                if ("35".equals(source)) {//美分期
                    if (StringUtils.isEmpty(meifenqiDefaultTypCde)) {
                        logger.error("美分期 人脸默认贷款品种尚未配置！");
                        return null;
                    }
                    defaultTypCode = meifenqiDefaultTypCde;
                }
                if ("33".equals(source)) {//乔融豆子
                    if (StringUtils.isEmpty(qrdzDefaultTypCde)) {
                        logger.error("乔融豆子 人脸默认贷款品种尚未配置！");
                        return null;
                    }
                    defaultTypCode = qrdzDefaultTypCde;
                }
            }
        } else {//商户版
            faceType = "B";
            defaultTypCode = "APP-DEFAULT";
        }

        String url = EurekaServer.CMISPROXY + "/api/appl/getFacedOrg?typCde=" + typCde + "&faceType=" + faceType;
        logger.debug("查询人脸机构信息url:" + url);
        String json = HttpUtil.restGet(url, super.getToken());
        Map<String, Object> result = HttpUtil.json2Map(json);
        logger.debug("人脸机构查询结果:" + result);
        if (!HttpUtil.isSuccess(result)) {
            logger.debug("==未配置此贷款品种(" + typCde + ")，查找默认的(" + defaultTypCode + ")==");
            url = EurekaServer.CMISPROXY + "/api/appl/getFacedOrg?typCde=" + defaultTypCode + "&faceType=" + faceType;
            logger.debug("查询人脸机构信息url:" + url);
            json = HttpUtil.restGet(url, super.getToken());
            result = HttpUtil.json2Map(json);
            logger.debug("人脸机构查询结果(" + defaultTypCode + "):" + result);
        }
        return result;
    }

    /**
     * 获取人脸规则（星巢贷）
     *
     * @return
     */
    public Map<String, Object> getXCDFacedOrg() {
        String faceType = "P";
        String defaultTypCode = xcdDefaultTypCde;
        String url = EurekaServer.CMISPROXY + "/api/appl/getFacedOrg?typCde=" + defaultTypCode + "&faceType=" + faceType;
        logger.debug("星巢贷查询人脸机构信息url:" + url);
        String json = HttpUtil.restGet(url, super.getToken());
        Map<String, Object> result = HttpUtil.json2Map(json);
        logger.debug("人脸机构查询结果:" + result);
        return result;
    }

    public Map<String, Object> getDefaultFacedOrg(String defaultTypCode, String faceType) {
        String url = EurekaServer.CMISPROXY + "/api/appl/getFacedOrg?typCde=" + defaultTypCode + "&faceType=" + faceType;
        logger.debug("查询人脸机构信息(默认贷款品种)url:" + url);
        String json = HttpUtil.restGet(url, super.getToken());
        Map<String, Object> result = HttpUtil.json2Map(json);
        logger.debug("人脸机构查询结果:" + result);
        return result;
    }

    /**
     * 查询可替代资料
     *
     * @param typCde
     * @param source 1-APP商户版 2-APP个人版 16-星巢贷 34-集团大数据 35-美分期 33-乔融豆子
     * @return
     */
    public Map<String, Object> getReplacedFiles(String typCde, String source) {
        Map<String, Object> dataMap = new HashMap<>();
        //返回替代影像信息
        logger.info("====查询替代影像信息=====");
        logger.info("==source:" + source + ",typCde:" + typCde);
        String typeCode1;//FACE_B_DOC---商户版；FACE_P_DOC---个人版
        String typeCode2;//人脸识别类型	P-个人版APP  B-商户版APP
        String defaultTypCode;
        if ("2".equals(source) || "16".equals(source) || "34".equals(source) || "35".equals(source) || "33".equals(source) || "14".equals(source)) {//2-个人版
            typeCode1 = "FACE_P_DOC";
            typeCode2 = "P";
            if ("16".equals(source)) {//星巢贷
                defaultTypCode = xcdDefaultTypCde;
            } else {//嗨付 集团大数据
                defaultTypCode = "APP-DEFAULT";
                if ("35".equals(source)) {
                    if (StringUtils.isEmpty(meifenqiDefaultTypCde)) {
                        logger.error("美分期 人脸默认贷款品种尚未配置！");
                        return null;
                    }
                    defaultTypCode = meifenqiDefaultTypCde;
                }
                if ("33".equals(source)) {//乔融豆子
                    if (StringUtils.isEmpty(qrdzDefaultTypCde)) {
                        logger.error("乔融豆子 人脸默认贷款品种尚未配置！");
                        return null;
                    }
                    defaultTypCode = qrdzDefaultTypCde;
                }
            }
        } else {//1-商户版
            typeCode1 = "FACE_B_DOC";
            typeCode2 = "B";
            defaultTypCode = "APP-DEFAULT";
        }
        String attachUrl;
        String attachJson;
        Map<String, Object> attachResult;
        if ("16".equals(source)) {//星巢贷
            attachUrl = EurekaServer.CMISPROXY + "/api/appl/getReplacedFiles?typCde=" + defaultTypCode + "&typeCode1=" + typeCode1 + "&typeCode2=" + typeCode2;
            logger.debug("星巢贷可替代资料查询url:" + attachUrl);
            attachJson = HttpUtil.restGet(attachUrl, super.getToken());
            attachResult = HttpUtil.json2Map(attachJson);
            logger.debug("查询可替代资料返回结果(" + defaultTypCode + "):" + attachResult);
            if (!HttpUtil.isSuccess(attachResult)) {
                return attachResult;
            }
        } else {//嗨付
            typCde = StringUtils.isEmpty(typCde) ? "APP-DEFAULT" : typCde;
            attachUrl = EurekaServer.CMISPROXY + "/api/appl/getReplacedFiles?typCde=" + typCde + "&typeCode1=" + typeCode1 + "&typeCode2=" + typeCode2;
            logger.debug("可替代资料查询url:" + attachUrl);
            attachJson = HttpUtil.restGet(attachUrl, super.getToken());
            attachResult = HttpUtil.json2Map(attachJson);
            logger.debug("查询可替代资料返回结果:" + attachResult);
            if (!HttpUtil.isSuccess(attachResult)) {
                logger.error("===查询可替代资料失败===");
                logger.debug("==未配置此贷款品种(" + typCde + ")，查找默认的(" + defaultTypCode + ")==");
                attachUrl = EurekaServer.CMISPROXY + "/api/appl/getReplacedFiles?typCde=" + defaultTypCode + "&typeCode1=" + typeCode1 + "&typeCode2=" + typeCode2;
                logger.debug("可替代资料查询url:" + attachUrl);
                attachJson = HttpUtil.restGet(attachUrl, super.getToken());
                attachResult = HttpUtil.json2Map(attachJson);
                logger.debug("查询可替代资料返回结果(" + defaultTypCode + "):" + attachResult);
                if (!HttpUtil.isSuccess(attachResult)) {
                    return attachResult;
                }
            }
        }

        List<Map<String, Object>> attachList = (List<Map<String, Object>>) attachResult.get("body");
        logger.debug("查询可替代资料返回列表:" + attachList);
        List<Map<String, Object>> attachListResult = new ArrayList<>();
        String attachTypes = "";
        for (Map<String, Object> attachMap : attachList) {
            Map<String, Object> map = new HashMap<>();
            String docCde = StringUtils.isEmpty(attachMap.get("COM_CDE")) ? "" : (String) attachMap.get("COM_CDE");
            map.put("docCde", docCde);//影像代码
            attachTypes = attachTypes + docCde + ",";
            map.put("docDesc", StringUtils.isEmpty(attachMap.get("COM_DESC")) ? "" : attachMap.get("COM_DESC"));//影像名称
            attachListResult.add(map);
        }
        attachResult.put("attachList", attachListResult);
        attachResult.put("attachTypes", attachTypes);
        logger.debug("返回替代影像列表:" + attachListResult);
        return attachResult;
    }

    /**
     * 处理人脸识别对比结果（新）
     *
     * @param checkMap   对比结果信息
     * @param isPerson   是否个人版
     * @param orgScore   人脸识别阈值
     * @param updateFlag 是否更改人脸分值和次数 1-修改 0-不修改(默认)
     * @return
     */
    protected Map<String, Object> faceCheckDone2(Map<String, Object> checkMap, Boolean isPerson, Integer orgScore, String updateFlag, String providerNo, String providerDesc) {

        Double faceValue = Double.valueOf("0");
        Integer faceCount;
        String source = String.valueOf(checkMap.get("source"));
        String name = String.valueOf(checkMap.get("name"));
        String idNumber = String.valueOf(checkMap.get("idNumber"));
        String mobile = String.valueOf(checkMap.get("mobile"));
        String fileName = String.valueOf(checkMap.get("fileName"));
        String md5 = String.valueOf(checkMap.get("md5"));
        String applSeq = String.valueOf(checkMap.get("applSeq"));
        String orgName = String.valueOf(checkMap.get("orgName"));
        String setOrgScore = String.valueOf(checkMap.get("setOrgScore"));
        Map<String, Object> dataMap = new HashMap<>();
        String code = checkMap.get("code").toString();
        boolean isException = false;

        if ("0".equals(code)) { //0-成功（计费）
            //需要校验分值faceValue，记录次数faceCount
            faceValue = Double.valueOf(checkMap.get("score").toString());
            logger.debug("人脸分数faceValue==" + faceValue);
//            faceValue = Integer.valueOf(new DecimalFormat("0").format(scoreTemp));
            faceCount = Integer.valueOf(checkMap.get("faceCount").toString()) + 1;
        } else if ("1".equals(code)) { //1-失败，不可重试（计费）
            //直接记0分，最大次数，不允许再次做人脸识别
            faceValue = Double.valueOf("0");
            faceCount = faceCountLimit + 1;
        } else if ("2".equals(code)) { //2-失败，可重试
            //记录次数
            dataMap.put("isOK", "N");
            dataMap.put("isRetry", "Y");
            dataMap.put("isResend", "N");
            String errCode = String.valueOf(checkMap.get("errCode")); //公安接口返回的错误码，4位数字
            String errMsg = String.valueOf(checkMap.get("errMsg")); //公安接口返回的错误码，4位数字
            logger.error("errCode:" + errCode + ",errMsg:" + errMsg);
            faceCount = Integer.valueOf(checkMap.get("faceCount").toString()) + 1;
            isException = true;
        } else { //异常，可重试
            //记录次数
            dataMap.put("isOK", "N");
            dataMap.put("isRetry", "N");
            dataMap.put("isResend", "Y");
            logger.error("errCode:" + RestUtil.ERROR_INTERNAL_CODE + ",errMsg:" + RestUtil.ERROR_INTERNAL_MSG);
            faceCount = Integer.valueOf(checkMap.get("faceCount").toString()) + 1;
            isException = true;
        }

        //// 人脸识别接口扣费的情况，会往下走 ////人脸异常的情况下，也记录次数
        if ("1".equals(updateFlag)) {
            //5. 保存人脸分值到crm
            if (isPerson) {//个人版保存，商户版不保存
                saveFaceValueCrm(name, idNumber, faceValue, faceCount, providerNo, providerDesc);
            }

            //6. 上传人脸分值到信贷
            // 默认传得分，没有得分把公安接口返回的错误消息传过去
            String content = "0".equals(code) ? (faceValue.toString() + "(" + orgName + ")") : String.valueOf(checkMap.get("errMsg"));
            saveFaceValueCmis(name, idNumber, mobile, source, content, applSeq);

            //7. 保存人脸影像
            saveFaceAttach(name, idNumber, fileName, md5, isPerson);
        }

        logger.info("isException:" + isException);
        if (!isException) {
            //处理返回值
            Boolean isOK = false;
            if ("0".equals(code)) {
                logger.debug("判断是否设分:" + setOrgScore);
                if ("Y".equals(setOrgScore)) {//设分
                    logger.debug("人脸分数:" + faceValue + ",阈值:" + orgScore);
                    isOK = faceValue >= orgScore;
                } else {//不设分
                    isOK = true;//外联接口成功了就有分
                }
            }

            Boolean isRetry = !isOK && (faceCount < faceCountLimit);//是否允许重试
            dataMap.put("isOK", isOK ? "Y" : "N");
            dataMap.put("isRetry", isRetry ? "Y" : "N");
            dataMap.put("isResend", "N");//计费接口，不允许重发

            if (isOK) {
                return success("人脸识别通过", dataMap);
            } else if (isRetry) {
                logger.error("11,校验失败，您还剩余" + (faceCountLimit - faceCount) + "次机会" + dataMap);
                return fail("11", "校验失败，您还剩余" + (faceCountLimit - faceCount) + "次机会", dataMap);
            } else {
                logger.error("11,人脸识别已超过上限次数，暂不能继续办理，详询4000187777" + dataMap);
                return fail("11", "人脸识别已超过上限次数，暂不能继续办理，详询4000187777", dataMap);
            }
        } else {
            Integer remainCountTem = faceCountLimit - faceCount;
            if (remainCountTem > 0) {
                logger.error("11,校验失败，您还剩余" + (faceCountLimit - faceCount) + "次机会" + dataMap);
                return fail("11", "校验失败，您还剩余" + (faceCountLimit - faceCount) + "次机会", dataMap);
            } else {
                dataMap.put("isOK", "N");
                dataMap.put("isRetry", "N");
                dataMap.put("isResend", "N");
                logger.error("11,人脸识别已超过上限次数，暂不能继续办理，详询4000187777" + dataMap);
                return fail("11", "人脸识别已超过上限次数，暂不能继续办理，详询4000187777", dataMap);
            }
        }
    }


    protected Map<String, Object> faceCheckDoneByFaceValue(Map<String, Object> checkMap, Integer orgScore, String updateFlag, String providerNo, String providerDesc) {

        String source = String.valueOf(checkMap.get("source"));
        String name = String.valueOf(checkMap.get("name"));
        String idNumber = String.valueOf(checkMap.get("idNumber"));
        String mobile = String.valueOf(checkMap.get("mobile"));
        String fileName = String.valueOf(checkMap.get("fileName"));
        String md5 = String.valueOf(checkMap.get("md5"));
        String applSeq = String.valueOf(checkMap.get("applSeq"));
        String orgName = String.valueOf(checkMap.get("orgName"));
        String setOrgScore = String.valueOf(checkMap.get("setOrgScore"));
        Map<String, Object> dataMap = new HashMap<>();

        //需要校验分值faceValue，记录次数faceCount
        Double faceValue = Double.valueOf(checkMap.get("score").toString());
        logger.debug("人脸分数faceValue:" + faceValue + ",faceCount:" + checkMap.get("faceCount"));
        Integer faceCount = Integer.valueOf(checkMap.get("faceCount").toString()) + 1;


        //// 人脸识别接口扣费的情况，会往下走 ////
        if ("1".equals(updateFlag)) {//修改数据
            //5. 保存人脸分值到crm
            saveFaceValueCrm(name, idNumber, faceValue, faceCount, providerNo, providerDesc);
            //6. 上传人脸分值到信贷
            // 默认传得分，没有得分把公安接口返回的错误消息传过去
            String content = faceValue.toString() + "(" + orgName + ")";
            saveFaceValueCmis(name, idNumber, mobile, source, content, applSeq);
            //7. 保存人脸影像
            saveFaceAttach(name, idNumber, fileName, md5, true);
        }

        //处理返回值
        Boolean isOK;
        logger.debug("判断是否设分:" + setOrgScore);
        if ("Y".equals(setOrgScore)) {//设分
            logger.debug("人脸分数:" + faceValue + ",阈值:" + orgScore);
            isOK = faceValue >= orgScore;
        } else {//不设分
            isOK = true;//外联接口成功了就有分
        }

        Boolean isRetry = !isOK && (faceCount < faceCountLimit);//是否允许重试
        dataMap.put("isOK", isOK ? "Y" : "N");
        dataMap.put("isRetry", isRetry ? "Y" : "N");
        dataMap.put("isResend", "N");//计费接口，不允许重发

        if (isOK) {
            return success("人脸识别通过", dataMap);
        } else if (isRetry) {
            Integer remainCount = faceCount < faceCountLimit ? (faceCountLimit - faceCount) : 0;
            if (remainCount > 0) {
                logger.error("11,校验失败，您还剩余" + (faceCountLimit - faceCount) + "次机会" + dataMap);
                return fail("11", "校验失败，您还剩余" + (faceCountLimit - faceCount) + "次机会", dataMap);
            } else {
                logger.error("11,人脸识别已超过上限次数，暂不能继续办理，详询4000187777" + dataMap);
                return fail("11", "人脸识别已超过上限次数，暂不能继续办理，详询4000187777", dataMap);
            }
        } else {
            logger.error("11,人脸识别已超过上限次数，暂不能继续办理，详询4000187777" + dataMap);
            return fail("11", "人脸识别已超过上限次数，暂不能继续办理，详询4000187777", dataMap);
        }

    }

    /**
     * 获取人脸厂商
     *
     * @param list
     * @param source 34-集团大数据 35-美分期 33-乔融豆子
     * @return
     */
    public List<Map<String, Object>> getFaceConfigList(List<Map<String, Object>> list, String source) {
        logger.info("===getFaceConfigList source:" + source);
        if ("34".equals(source) || "35".equals(source) || "33".equals(source)) {//集团大数据、美分期 获取face++厂商
            List<Map<String, Object>> bodyListTem = new ArrayList<>();
            for (Map<String, Object> infoMap : list) {
                if ("03".equals(infoMap.get("ORG_CHOICE")) || "03".equals(infoMap.get("orgChoice"))) {
                    logger.info("获取face++厂商,source:" + source);
                    bodyListTem.add(infoMap);
                    break;
                }
            }
            list = bodyListTem;
        } else {//嗨付等 去掉face++厂商
            for (Map<String, Object> infoMap : list) {
                if ("03".equals(infoMap.get("ORG_CHOICE")) || "03".equals(infoMap.get("orgChoice"))) {
                    logger.info("去掉face++厂商,source:" + source);
                    list.remove(infoMap);
                    break;
                }
            }
        }
        logger.debug("整理后的bodyList:" + list);
        return list;
    }

    /**
     * 获取人脸最大次数
     *
     * @param source 34-集团大数据 35-美分期 33-乔融豆子
     * @return
     */
    public Integer getFaceCountLimit(String source) {
        Integer faceCountMax;
        if ("34".equals(source) || "35".equals(source) || "33".equals(source)) {//集团大数据、美分期
            faceCountMax = bigDataFaceCountLimit;
        } else {
            faceCountMax = faceCountLimit;
        }
        logger.info("source:" + source + ",人脸最大次数:" + faceCountMax);
        return faceCountMax;
    }

    private String getImageCrmFolder() {
        if (IMAGE_CRM_FOLDER == null) {
            IMAGE_CRM_FOLDER = CommonProperties.get("file.imageCrmFolder").toString();
        }
        return IMAGE_CRM_FOLDER;
    }

    private String getCrmFlag() {
        if (CRM_FLAG == null) {
            CRM_FLAG = CommonProperties.get("file.imageCrmFlag").toString();
            if (CRM_FLAG == null) {
                CRM_FLAG = "crm";
            }
        }
        return CRM_FLAG;
    }

    private String getImageCache() {
        if (IMAGE_CACHE == null) {
            IMAGE_CACHE = CommonProperties.get("file.imageCache").toString();
            if (IMAGE_CACHE == null) {
                IMAGE_CACHE = "imageCache";
            }
        }
        return IMAGE_CACHE;
    }

    //保存之前人脸厂商 人脸识别的结果(个人版)
    public Map<String, Object> saveLastFaceResult(String code, Map<String, Object> lastResult, Map<String, Object> lastCheckMap, Integer orgScore, String providerNo, String providerDesc) {
        Map<String, Object> lastFaceResult = new HashMap<>();
        lastFaceResult.put("lastCode", code);
        lastFaceResult.put("lastResult", lastResult);
        lastFaceResult.put("lastCheckMap", lastCheckMap);
        lastFaceResult.put("orgScore", orgScore);
        lastFaceResult.put("providerNo", providerNo);
        lastFaceResult.put("providerDesc", providerDesc);
        logger.info("保存之前人脸厂商识别结果:" + lastFaceResult);
        return lastFaceResult;
    }

    //保存之前人脸厂商 人脸识别的结果(商户版)
    public Map<String, Object> saveLastFaceResultMerch(String code, Map<String, Object> lastResult, Map<String, Object> lastResultInfo) {
        Map<String, Object> lastFaceResult = new HashMap<>();
        lastFaceResult.put("lastCode", code);
        lastFaceResult.put("lastResult", lastResult);
        lastFaceResult.put("lastResultInfo", lastResultInfo);
        logger.info("保存之前人脸厂商识别结果:" + lastFaceResult);
        return lastFaceResult;
    }
}