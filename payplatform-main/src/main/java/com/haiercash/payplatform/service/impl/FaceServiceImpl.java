package com.haiercash.payplatform.service.impl;

import com.haiercash.payplatform.redis.RedisUtils;
import com.haiercash.payplatform.rest.client.JsonClientUtils;
import com.haiercash.payplatform.service.AppServerService;
import com.haiercash.payplatform.service.BaseService;
import com.haiercash.payplatform.service.FaceService;
import com.haiercash.payplatform.utils.ConstUtil;
import com.haiercash.payplatform.utils.EncryptUtil;
import com.haiercash.payplatform.utils.ImageUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import sun.misc.BASE64Encoder;

import javax.imageio.stream.FileImageOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by yuanli on 2017/8/1.
 */
@Service
public class FaceServiceImpl extends BaseService implements FaceService {
    public Log logger = LogFactory.getLog(getClass());
    @Value("${app.other.outplatform_url}")
    protected String outplatform_url;
    @Value("${app.other.face_DataImg_url}")
    protected String face_DataImg_url;
    @Value("${app.other.haierDataImg_url}")
    protected String haierDataImg_url;
    @Autowired
    private AppServerService appServerService;

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

    //人脸识别
    @Override
    public Map<String, Object> uploadFacePic(MultipartFile faceImg, HttpServletRequest request, HttpServletResponse response) throws Exception {
        logger.info("人脸识别*****************开始");
        if (faceImg.isEmpty()) {
            logger.info("图片为空");
            return fail(ConstUtil.ERROR_CODE, "图片为空");
        }
        //前台参数获取
        String token = request.getHeader("token");
        String channel = request.getHeader("channel");
        String channelNo = request.getHeader("channelNo");
        String edflag = request.getParameter("edflag");//1:额度申请  传1
        if (StringUtils.isEmpty(token) || StringUtils.isEmpty(channel) || StringUtils.isEmpty(channelNo)) {
            logger.info("token：" + token + "   channel:" + channel + "    channelNo:" + channelNo);
            logger.info("前台传入数据有误");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.FAILED_INFO);
        }
        //缓存数据获取
        Map<String, Object> cacheMap = RedisUtils.getExpireMap(token);
        if (cacheMap == null || "".equals(cacheMap)) {
            logger.info("Jedis数据获取失败");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        String typCde = (String) cacheMap.get("typCde");// 贷款品种
        String idNumber = (String) cacheMap.get("idCard");// 身份证号
        String name = (String) cacheMap.get("name");// 姓名
        String mobile = (String) cacheMap.get("phoneNo");// 手机号
        String custNo = (String) cacheMap.get("custNo");
        String userId = (String) cacheMap.get("userId");
        if (StringUtils.isEmpty(idNumber) || StringUtils.isEmpty(name) || StringUtils.isEmpty(mobile)
                || StringUtils.isEmpty(custNo) || StringUtils.isEmpty(userId)) {
            logger.info("idNumber:" + idNumber + "  name:" + name + "  mobile:" + mobile + "   custNo:" + custNo + "    userId:" + userId);
            logger.info("redis获取数据为空");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        }

        String appno = UUID.randomUUID().toString().replace("-", "");
        String filestreamname = custNo + ".jpg";
        //图片处理
        InputStream inputStream = faceImg.getInputStream();
        InputStream inputStream1 = faceImg.getInputStream();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int numBytesRead = 0;
        while ((numBytesRead = inputStream.read(buf)) != -1) {
            output.write(buf, 0, numBytesRead);
        }
        byte[] data = output.toByteArray();
        output.close();
        inputStream.close();

        BASE64Encoder encode = new BASE64Encoder();
        String base64 = encode.encode(data);
        String filestream = URLEncoder.encode(base64, "UTF-8");
        //调用外联活体人脸识别
        String url = outplatform_url + "/Outreachplatform/api/face/isface";
        logger.info("调用外联人脸识别接口，请求地址：" + url);
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("personalName", name);//客户姓名
        jsonMap.put("identityCardNo", idNumber);//身份证号
        jsonMap.put("filestream", filestream);//识别图像文件流
        jsonMap.put("appno", appno);//申请编号
        jsonMap.put("filestreamname", filestreamname);//文件名
        jsonMap.put("organization", "02");//机构号(国政通)
//        if ("46".equals(channelNo) || "49".equals(channelNo)) { //顺逛
//            jsonMap.put("organization", "02");//机构号(国政通)
//        } else {//乔融  现金贷
//            jsonMap.put("organization", "01");//机构号(海鑫洺)
//        }
        //xmllog.info("调用外联人脸识别接口，请求数据：" + json.toString());
        String resData = JsonClientUtils.postForString(url, jsonMap);
        logger.info("调用外联人脸识别接口，返回数据：" + resData);
        //resData = "{\"code\":\"0000\",\"message\":\"{\\\"user_check_result\\\":\\\"3\\\",\\\"msg\\\":\\\"比对服务处理成功\\\",\\\"requestId\\\":\\\"d77b77852fe728c13b7114dbd5c448d9\\\",\\\"code\\\":\\\"1001\\\",\\\"entity\\\":{\\\"score\\\":\\\"83.23\\\",\\\"desc\\\":\\\"是同一个人\\\"}}\",\"data\":null}";
        //{"code":"0000","data":[],"message":"{\"msg\":\"账号密码不匹配\",\"code\":\"-1\"}"}
        //{"code":"0000","data":[],"message":"{\"msg\":\"未检测到脸\",\"code\":\"-2\"}"}
        //{"code":"0000","message":"{\"msg\":\"请求参数错误，缺少必要的参数\",\"code\":\"9990\"}","data":null}
        //{"code":"0000","message":"{\"user_check_result\":\"3\",\"msg\":\"比对服务处理成功\",\"requestId\":\"61a9af6a6f19316f33809cf90235740b\",\"code\":\"1001\",\"entity\":{\"score\":\"57.24\",\"desc\":\"不是同一个人\"}}","data":null}
        //{"code":"0000","message":"{\"user_check_result\":\"3\",\"msg\":\"比对服务处理成功\",\"requestId\":\"d77b77852fe728c13b7114dbd5c448d9\",\"code\":\"1001\",\"entity\":{\"score\":\"83.23\",\"desc\":\"是同一个人\"}}","data":null}
        //人脸分值
        String score = "0";
        JSONObject jsonob = new JSONObject(resData);
        String code0 = jsonob.getString("code");
        if (!"0000".equals(code0)) {
            logger.info("调用外联人脸识别失败");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        }
        String message = jsonob.getString("message");
        JSONObject jsonmsg = new JSONObject(message);
        String code = jsonmsg.getString("code");
        if ("1001".equals(code)) {
            String entity = jsonmsg.get("entity").toString();
            JSONObject jsonn = new JSONObject(entity);
            score = jsonn.get("score").toString();
            //score = new JSONObject(jsonmsg.getString("entity")).getString("score");
        }

        //人脸识别成功。将图片上送到app后台
//        StringBuffer filePath = new StringBuffer(face_DataImg_url).append(custNo).append(File.separator).append(ConstUtil.ATTACHTYPE_DOC065)
//                .append(File.separator);
//        String fileName = UUID.randomUUID().toString().replaceAll("-", "");
//        filePath = filePath.append(fileName).append(".jpg"); // 测试打开
//        InputStream is = new BufferedInputStream(new FileInputStream(String.valueOf(filePath)));
//        String md5Code = DigestUtils.md5Hex(is);
        StringBuffer filePath = new StringBuffer(face_DataImg_url).append(custNo).append(File.separator).append(ConstUtil.ATTACHTYPE_DOC065)
                .append(File.separator);// File.separator
        createDir(String.valueOf(filePath));
        String fileName = UUID.randomUUID().toString().replaceAll("-", "");
        filePath = filePath.append(fileName).append(".jpg"); // 测试打开
        FileImageOutputStream outImag = new FileImageOutputStream(new File(String.valueOf(filePath)));
        byte[] bufferOut = new byte[1024];
        int bytes = 0;
        while ((bytes = inputStream1.read(bufferOut)) != -1) {
            outImag.write(bufferOut, 0, bytes);
        }
        outImag.close();
        inputStream1.close();
        //图片压缩
        ImageUtil.zipImageFile(new File(filePath.toString()), new File(filePath.toString()), 425, 638, 0.7f);
        //生成MD5
        InputStream is = new BufferedInputStream(new FileInputStream(String.valueOf(filePath)));
        String MD5 = DigestUtils.md5Hex(is);
        is.close();

        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("channel", channel);
        paramMap.put("channelNo", channelNo);
        paramMap.put("custNo", custNo);// 客户编号
        paramMap.put("attachType", ConstUtil.ATTACHTYPE_DOC065);// 影像类型
        paramMap.put("attachName", ConstUtil.ATTACHTYPE_DOC065_DESC);// 人脸照片
        paramMap.put("md5", MD5);//文件md5码
        paramMap.put("filePath", filePath.toString());
        //paramMap.put("fileStream", inputStream1);
        String applSeq = (String) cacheMap.get("applSeq");
        //paramMap.put("applSeq", applSeq);
        //影像上传
        Map<String, Object> uploadresultmap = appServerService.attachUploadPersonByFilePath(token, paramMap);
        Map uploadheadjson = (Map<String, Object>) uploadresultmap.get("head");
        String uploadretFlag = (String) uploadheadjson.get("retFlag");
        if (!"00000".equals(uploadretFlag)) {
            String retMsg = (String) uploadheadjson.get("retMsg");
            return fail(ConstUtil.ERROR_CODE, retMsg);
        }
        //通过人脸分数判断人脸识别是否通过
        File file = new File(String.valueOf(filePath));
        InputStream instream = new BufferedInputStream(new FileInputStream(String.valueOf(filePath)));
        String MD5code = DigestUtils.md5Hex(instream);
        Map<String, Object> checkMap = new HashMap<String, Object>();
        checkMap.put("faceValue", score);//人脸分数
        checkMap.put("typCde", typCde);// 贷款品种 从redis中获取
        checkMap.put("custNo", EncryptUtil.simpleEncrypt(custNo));
        checkMap.put("idNumber", EncryptUtil.simpleEncrypt(idNumber));
        checkMap.put("name", EncryptUtil.simpleEncrypt(name));
        checkMap.put("mobile", EncryptUtil.simpleEncrypt(mobile));
        checkMap.put("source", channel);
        checkMap.put("filePath", filePath);
        checkMap.put("md5", MD5code);
        checkMap.put("token", token);
        if ("1".equals(edflag)) {
            checkMap.put("isEdAppl", "Y");//是否是额度申请或提额
        }
        checkMap.put("channel", channel);
        checkMap.put("channelNo", channelNo);
        Map<String, Object> checkresultmap = appServerService.faceCheckByFaceValue(token, checkMap);
        Map checkheadjson = (Map<String, Object>) checkresultmap.get("head");
        String checkretFlag = (String) checkheadjson.get("retFlag");
        String checkretMsg = (String) checkheadjson.get("retMsg");
        if ("00000".equals(checkretFlag)) {//
            //人脸识别成功
            //判断是否已经设置过支付密码
            if ("33".equals(channelNo)) {//如果是乔融则不进行支付密码校验
                Map<String, Object> m = new HashMap<String, Object>();
                m.put("faceFlag", "1");
                return success(m);
            }
            Map m = validateUserFlag(userId, token, channel, channelNo, cacheMap);
            return m;
        } else {
            Map checkbodyjson = (Map<String, Object>) checkresultmap.get("body");
            if ("N".equals(checkbodyjson.get("isRetry")) &&
                    "N".equals(checkbodyjson.get("isOK")) &&
                    "N".equals(checkbodyjson.get("isResend"))) {
                //跳转到手持身份证
                Map<String, Object> m = new HashMap<String, Object>();
                m.put("faceFlag", "2");
                return success(m);
//                logger.info("您人脸识别失败多次，暂时不允许继续申请，谢谢您的关注");
//                return fail(ConstUtil.ERROR_CODE,"您人脸识别失败多次，暂时不允许继续申请，谢谢您的关注");
            } else {
                //可以继续做人脸，跳转到人脸页面
                Map<String, Object> m = new HashMap<String, Object>();
                m.put("faceFlag", "3");
                return success(m);
            }
        }

    }

    //上传手持身份证
    public Map<String, Object> uploadPersonPic(MultipartFile faceImg, HttpServletRequest request, HttpServletResponse response) throws Exception {
        logger.info("上传手持身份证**********************开始");
        if (faceImg.isEmpty()) {
            logger.info("图片为空");
            return fail(ConstUtil.ERROR_CODE, "图片为空");
        }
        //前台参数获取
        String token = request.getHeader("token");
        String channel = request.getHeader("channel");
        String channelNo = request.getHeader("channelNo");
        String edflag = request.getParameter("edflag");//1:额度申请
        if (StringUtils.isEmpty(token) || StringUtils.isEmpty(channel) || StringUtils.isEmpty(channelNo)) {
            logger.info("token：" + token + "   channel:" + channel + "    channelNo:" + channelNo);
            logger.info("前台传入数据有误");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.FAILED_INFO);
        }
        //缓存数据获取
        Map<String, Object> cacheMap = RedisUtils.getExpireMap(token);
        if (cacheMap == null || "".equals(cacheMap)) {
            logger.info("Jedis数据获取失败");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        String typCde = (String) cacheMap.get("typCde");// 贷款品种
        String idNumber = cacheMap.get("idCard").toString();// 身份证号
        String name = cacheMap.get("name").toString();// 姓名
        //String mobile = cacheMap.get("phoneNo").toString();// 手机号
        String custNo = cacheMap.get("custNo").toString();
        String userId = cacheMap.get("userId").toString();
        if (StringUtils.isEmpty(idNumber) || StringUtils.isEmpty(name)
                || StringUtils.isEmpty(custNo) || StringUtils.isEmpty(userId)) {
            logger.info("idNumber:" + idNumber + "  name:" + name + "   custNo:" + custNo + "    userId:" + userId);
            logger.info("redis获取数据为空");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        }

        //
        InputStream inputStream = faceImg.getInputStream();
        StringBuffer filePath = new StringBuffer(face_DataImg_url).append(custNo).append(File.separator).append(ConstUtil.ATTACHTYPE_APP01)
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

        InputStream is = new BufferedInputStream(new FileInputStream(String.valueOf(filePath)));
        String MD5 = DigestUtils.md5Hex(is);
        is.close();
        //
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("channel", channel);
        paramMap.put("channelNo", channelNo);
        paramMap.put("custNo", custNo);// 客户编号
        paramMap.put("attachType", ConstUtil.ATTACHTYPE_APP01);// 影像类型
        paramMap.put("attachName", ConstUtil.ATTACHTYPE_APP01_DESC);
        paramMap.put("md5", MD5);
        paramMap.put("filePath", filePath);
        String applSeq = (String) cacheMap.get("applSeq");
        //paramMap.put("applSeq", applSeq);
        //影像上传
        Map<String, Object> uploadresultmap = appServerService.attachUploadPersonByFilePath(token, paramMap);
        Map uploadheadjson = (Map<String, Object>) (uploadresultmap.get("head"));
        String uploadretFlag = (String) uploadheadjson.get("retFlag");
        if (!"00000".equals(uploadretFlag)) {
            String retMsg = (String) uploadheadjson.get("retMsg");
            return fail(ConstUtil.ERROR_CODE, retMsg);
        }

        if ("33".equals(channelNo)) {
            return success();
        }

        //上传替代影像成功
        //判断是否已经设置过支付密码
        Map<String, Object> m = validateUserFlag(userId, token, channel, channelNo, cacheMap);
        return m;
    }

    /**
     * 是否需要做人脸
     *
     * @param params
     * @return
     */
    public Map<String, Object> ifNeedDoFace(Map<String, Object> params) {
        logger.info("是否需要人脸识别*******************开始");
        //前端页面数据获取及非空判断
        String token = (String) params.get("token");
        String edflag = (String) params.get("edflag");
        String channel = (String) params.get("channel");
        String channelNo = (String) params.get("channelNo");
        if (StringUtils.isEmpty(token) || StringUtils.isEmpty(edflag)
                || StringUtils.isEmpty(channel) || StringUtils.isEmpty(channelNo)) {
            logger.info("前端获取数据有误");
            logger.info("token:" + token + "   edflag:" + edflag + "   channel:" + channel + "   channelNo:" + channelNo);
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        }
        //缓存数据获取及非空判断
        Map<String, Object> cacheMap = RedisUtils.getExpireMap(token);
        if (cacheMap == null || "".equals(cacheMap)) {
            logger.info("Jedis数据获取失败");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        String typCde = (String) cacheMap.get("typCde");// 贷款品种
        String idNumber = cacheMap.get("idCard").toString();// 身份证号
        String name = cacheMap.get("name").toString();// 姓名
        String mobile = cacheMap.get("phoneNo").toString();// 手机号
        String custNo = cacheMap.get("custNo").toString();
        String userId = cacheMap.get("userId").toString();
        if (StringUtils.isEmpty(idNumber) || StringUtils.isEmpty(name) || StringUtils.isEmpty(mobile)
                || StringUtils.isEmpty(custNo) || StringUtils.isEmpty(userId)) {
            logger.info("idNumber:" + idNumber + "  name:" + name + "  mobile:" + mobile + "   custNo:" + custNo + "    userId:" + userId);
            logger.info("redis获取数据为空");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        }
        //判断是否需要做人脸识别
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("typCde", typCde);
        paramMap.put("source", channel);
        paramMap.put("custNo", custNo);
        paramMap.put("name", name);
        paramMap.put("idNumber", idNumber);
        paramMap.put("token", token);
        paramMap.put("channel", channel);
        paramMap.put("channelNo", channelNo);
        if ("1".equals(edflag)) {
            paramMap.put("isEdAppl", "Y");//是否是额度申请或提额
        }
        Map<String, Object> resultmap = appServerService.ifNeedFaceChkByTypCde(token, paramMap);
        JSONObject headjson = new JSONObject(resultmap.get("head"));
        String retFlag = headjson.getString("retFlag");
        String retMsg = headjson.getString("retMsg");
        if (!"00000".equals(retFlag)) {
            return fail(ConstUtil.ERROR_CODE, retMsg);
        }
        JSONObject body = new JSONObject(resultmap.get("body"));
        String code = body.getString("code"); //结果标识码
        if ("00".equals(code)) {//人脸识别通过
            validateUserFlag(userId, token, channel, channelNo, cacheMap);

        } else if ("01".equals(code)) {//01：未通过人脸识别，剩余次数为0，不能再做人脸识别，录单终止
            //终止
            logger.info("未通过人脸识别，剩余次数为0，不能再做人脸识别，录单终止");
            return fail(ConstUtil.ERROR_CODE, "不能再做人脸识别，录单终止!");
        } else if ("02".equals(code)) {//02：未通过人脸识别，剩余次数为0，不能再做人脸识别，但可以上传替代影像
            //跳转替代影像
            logger.info("未通过人脸识别，剩余次数为0，不能再做人脸识别，但可以上传替代影像");
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("faceFlag", "2");// 手持身份证
            return success(map);
        } else if ("10".equals(code)) {//10：未通过人脸识别，可以再做人脸识别
            //可以做人脸识别
            logger.info("未通过人脸识别，可以再做人脸识别");
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("faceFlag", "3");// 人脸识别
            return success(map);
        }

        return success();
    }

    private Map<String, Object> validateUserFlag(String userId, String token, String channel, String channelNo, Map cacheMap) {
        logger.info("验证是否设置过支付密码*******************开始");
        Map<String, Object> pwdmap = new HashMap<String, Object>();
        pwdmap.put("userId", EncryptUtil.simpleEncrypt(userId));
        pwdmap.put("channel", channelNo);
        pwdmap.put("channelNo", channelNo);
        Map<String, Object> resultmap = appServerService.validateUserFlag(token, pwdmap);
        Map headjson = (Map<String, Object>) resultmap.get("head");
        String retFlag = (String) headjson.get("retFlag");
        String retMsg = (String) headjson.get("retMsg");
        if (!"00000".equals(retFlag)) {
            return fail(ConstUtil.ERROR_CODE, retMsg);
        }
        logger.info("上传手持身份证**********************成功");
        Map bodyjson = (Map<String, Object>) resultmap.get("body");
        String payPasswdFlag = (String) bodyjson.get("payPasswdFlag");
        if (payPasswdFlag.equals("1")) {// 1：已设置
            Map<String, Object> m = new HashMap<String, Object>();
            String preAmountFlag = (String) cacheMap.get("preAmountFlag");
            if ("1".equals(preAmountFlag)) {
                logger.info("已设置支付密码，跳转借款页面");
                m.put("faceFlag", "4");
            } else {
                cacheMap.put("payPasswdFlag", "1");
                RedisUtils.setExpire(token, cacheMap);
                logger.info("已设置支付密码，跳转支付密码验证页面");
                m.put("faceFlag", "1");
            }
            return success(m);
        } else {//未设置支付密码
            cacheMap.put("payPasswdFlag", "0");
            RedisUtils.setExpire(token, cacheMap);
            logger.info("未设置支付密码，跳转支付密码设置页面");
            Map<String, Object> m = new HashMap<String, Object>();
            m.put("faceFlag", "0");
            return success(m);
        }
    }

}
