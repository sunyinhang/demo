package com.haiercash.payplatform.service.impl;

import com.haiercash.core.collection.MapUtils;
import com.haiercash.core.lang.Base64Utils;
import com.haiercash.core.lang.Convert;
import com.haiercash.core.lang.StringUtils;
import com.haiercash.core.serialization.URLSerializer;
import com.haiercash.payplatform.common.annotation.FlowNode;
import com.haiercash.payplatform.config.OutreachConfig;
import com.haiercash.payplatform.config.StorageConfig;
import com.haiercash.payplatform.service.AppServerService;
import com.haiercash.payplatform.service.FaceService;
import com.haiercash.payplatform.utils.AppServerUtils;
import com.haiercash.payplatform.utils.EncryptUtil;
import com.haiercash.payplatform.utils.ImgUtils;
import com.haiercash.spring.config.EurekaServer;
import com.haiercash.spring.redis.RedisUtils;
import com.haiercash.spring.rest.IResponse;
import com.haiercash.spring.rest.common.CommonRestUtils;
import com.haiercash.spring.service.BaseService;
import com.haiercash.spring.util.ConstUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.stream.FileImageOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by yuanli on 2017/8/1.
 */
@Service
public class FaceServiceImpl extends BaseService implements FaceService {
    public final Log logger = LogFactory.getLog(getClass());
    @Autowired
    private AppServerService appServerService;
    @Autowired
    private StorageConfig storageConfig;
    @Autowired
    private OutreachConfig outreachConfig;

    private static void createDir(String destDirName) {
        if (!destDirName.endsWith(File.separator))
            destDirName = destDirName + File.separator;
        File dir = new File(destDirName);
        dir.mkdirs();
    }

    //人脸识别
    @Override
    @FlowNode(flow = "贷款", node = "人脸识别")
    public Map<String, Object> uploadFacePic(byte[] faceBytes, HttpServletRequest request, HttpServletResponse response) throws Exception {
        logger.info("人脸识别*****************开始");
        //前台参数获取
        String token = request.getHeader("token");
        String channel = request.getHeader("channel");
        String channelNo = request.getHeader("channelNo");
        String edflag = request.getParameter("edflag");//1:额度申请  传1
        if (StringUtils.isEmpty(token) || StringUtils.isEmpty(channel) || StringUtils.isEmpty(channelNo)) {
            logger.info("token：" + token + "   channel:" + channel + "    channelNo:" + channelNo);
            logger.info("前台传入数据有误");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }

        //缓存数据获取
        Map<String, Object> cacheMap = RedisUtils.getExpireMap(token);
        if (MapUtils.isEmpty(cacheMap)) {
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
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }

        String providerNo = "";//人脸机构
        if("33".equals(channelNo)){//乔融查询人脸机构配置
            logger.info("查询人脸机构配置");
            Map<String, Object> params = new HashMap<>();
            params.put("typCde", typCde);
            String urlface = AppServerUtils.getAppServerUrl() + "/app/appserver/getCmisFacedOrg";
            IResponse<Map> response2 = CommonRestUtils.getForMap(urlface, params);
            response2.assertSuccessNeedBody();
            Map map1 = response2.getBody();
            List list = (List) map1.get("faceConfigList");
            if(list.size() == 0){
                return fail(ConstUtil.ERROR_CODE, "贷款品种"+typCde+"没有配置人脸机构");
            }
            Map m = (Map) list.get(0);
            providerNo = Convert.toString(m.get("providerNo"));
        }

        //人脸识别
        String appno = UUID.randomUUID().toString().replace("-", "");
        String filestreamname = custNo + ".jpg";
        String filestream = URLSerializer.encode(Base64Utils.encode(faceBytes));
        String url = EurekaServer.OUTREACHPLATFORM + "/Outreachplatform/api/face/isface";
        logger.info("调用外联人脸识别接口，请求地址：" + url);
        LinkedHashMap<String, Object> jsonMap = new LinkedHashMap<>();
        jsonMap.put("personalName", name);//客户姓名
        jsonMap.put("identityCardNo", idNumber);//身份证号
        jsonMap.put("appno", appno);//申请编号
        jsonMap.put("filestreamname", filestreamname);//文件名
        if("33".equals(channelNo)){
            jsonMap.put("organization", providerNo);
        }else{
            jsonMap.put("organization", "02");//机构号(国政通)
        }
        jsonMap.put("filestream", filestream);//识别图像文件流
        String resData = CommonRestUtils.postForString(url, jsonMap);
        logger.info("调用外联人脸识别接口，返回数据：" + resData);
        //人脸分值
        String score = "0";
        String status = "";//face++是否为同一人的判断标志
        JSONObject jsonob = new JSONObject(resData);
        String code0 = jsonob.getString("code");
        if (!"0000".equals(code0)) {
            logger.info("调用外联人脸识别失败");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        String message = jsonob.getString("message");
        JSONObject jsonmsg = new JSONObject(message);
        String code = jsonmsg.getString("code");
        if ("1001".equals(code)) {
            String entity = jsonmsg.get("entity").toString();
            JSONObject jsonn = new JSONObject(entity);
            score = jsonn.get("score").toString();
            if("33".equals(channelNo) && "03".equals(providerNo)){//33渠道  face++厂商
                status = jsonn.get("status").toString();//01:同一人   02：不同人
            }
        }

        //人脸识别成功后落盘
        String dir = storageConfig.getFacePath() + File.separator + custNo + File.separator + ConstUtil.ATTACHTYPE_DOC065 + File.separator;
        createDir(dir);
        final String filePath = dir + appno + ".jpg";
        try (OutputStream outputStream = new FileOutputStream(filePath)) {
            outputStream.write(faceBytes);
        }

        //乔融图片压缩
        if("33".equals(channelNo)){
            int IMAGE_MAXSIZE = 5 * 1024 * 1024;
            logger.info(name + "人脸照片大小："+faceBytes.length);
            if(faceBytes.length > IMAGE_MAXSIZE){
                logger.info(name + "人脸照片压缩");
                ImgUtils.zipImageFile(new File(filePath), new File(filePath), 525, 738, 0.7f);
            }
        }

        //调用 appserver
        String md5ForUpload;
        try (FileInputStream inputStream = new FileInputStream(filePath)) {
            md5ForUpload = DigestUtils.md5Hex(inputStream);
        }
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("channel", channel);
        paramMap.put("channelNo", channelNo);
        paramMap.put("custNo", custNo);// 客户编号
        paramMap.put("attachType", ConstUtil.ATTACHTYPE_DOC065);// 影像类型
        paramMap.put("attachName", ConstUtil.ATTACHTYPE_DOC065_DESC);// 人脸照片
        paramMap.put("md5", md5ForUpload);//文件md5码
        paramMap.put("filePath", filePath);
        //影像上传
        Map<String, Object> uploadresultmap = appServerService.attachUploadPersonByFilePath(token, paramMap);
        Map uploadheadjson = (Map<String, Object>) uploadresultmap.get("head");
        String uploadretFlag = (String) uploadheadjson.get("retFlag");
        if (!"00000".equals(uploadretFlag)) {
            String retMsg = (String) uploadheadjson.get("retMsg");
            return fail(ConstUtil.ERROR_CODE, retMsg);
        }

        //通过人脸分数判断人脸识别是否通过
        String md5ForFaceCheck;
        try (InputStream inputStream = new FileInputStream(filePath)) {
            md5ForFaceCheck = DigestUtils.md5Hex(inputStream);
        }
        Map<String, Object> checkMap = new HashMap<>();
        checkMap.put("faceValue", score);//人脸分数
        checkMap.put("typCde", typCde);// 贷款品种 从redis中获取
        checkMap.put("custNo", EncryptUtil.simpleEncrypt(custNo));
        checkMap.put("idNumber", EncryptUtil.simpleEncrypt(idNumber));
        checkMap.put("name", EncryptUtil.simpleEncrypt(name));
        checkMap.put("mobile", EncryptUtil.simpleEncrypt(mobile));
        checkMap.put("source", channel);
        checkMap.put("filePath", filePath);
        checkMap.put("md5", md5ForFaceCheck);
        checkMap.put("token", token);
        if ("1".equals(edflag)) {
            checkMap.put("isEdAppl", "Y");//是否是额度申请或提额
        }
        checkMap.put("channel", channel);
        checkMap.put("channelNo", channelNo);
        if("33".equals(channelNo)){//乔融增加厂商号
            checkMap.put("providerNo", providerNo);
        }
        Map<String, Object> checkresultmap = appServerService.faceCheckByFaceValue(token, checkMap);
        Map checkheadjson = (Map<String, Object>) checkresultmap.get("head");
        String checkretFlag = (String) checkheadjson.get("retFlag");
        String checkretMsg = (String) checkheadjson.get("retMsg");

        if ("00000".equals(checkretFlag)) {//
            //人脸识别成功
            //判断是否已经设置过支付密码
            if ("33".equals(channelNo) && !"03".equals(providerNo)) {//是乔融且不是face++厂商
                Map<String, Object> m = new HashMap<>();
                m.put("faceFlag", "1");
                return success(m);
            }
            if("33".equals(channelNo) && "03".equals(providerNo)){//是乔融且是face++厂商
                if("01".equals(status)){//01同一人，返回成功   redis存储faceflag  Y
                    cacheMap.put("faceflag", "Y");
                    RedisUtils.setExpire(token, cacheMap);
                    RedisUtils.expire(token, 24, TimeUnit.HOURS);
                    Map<String, Object> m = new HashMap<>();
                    m.put("faceFlag", "1");
                    return success(m);
                } else {//02不同人，若redis存储次数等于5次终止，不足5次可继续
                    Integer facecount = Convert.asInteger(cacheMap.get("facecount"));
                    if(facecount == null){
                        facecount = 0;
                    }
                    if(facecount == 5){
                        return fail(ConstUtil.ERROR_CODE, "人脸识别，剩余次数为0，录单终止!");
                    }
                    facecount = facecount + 1;
                    cacheMap.put("faceflag", "N");
                    cacheMap.put("facecount", facecount);
                    RedisUtils.setExpire(token, cacheMap);
                    RedisUtils.expire(token, 24, TimeUnit.HOURS);
                    Map<String, Object> m = new HashMap<>();
                    if(facecount == 5){//人脸次数达到上限录单终止
                        m.put("faceFlag", "4");
                    }else{//人脸次数未达到上限可继续做人脸
                        m.put("faceFlag", "3");
                    }
                    return success(m);
                }
            }

            //支付宝支用环节人脸成功后不进行支付密码是否设置判断
            if("60".equals(channelNo) && !"1".equals(edflag)){
                return success();
            }

            //进行支付密码校验
            return validateUserFlag(userId, token, channel, channelNo, cacheMap);
        }

        Map checkbodyjson = (Map<String, Object>) checkresultmap.get("body");
        if ("N".equals(checkbodyjson.get("isRetry")) &&
                "N".equals(checkbodyjson.get("isOK")) &&
                "N".equals(checkbodyjson.get("isResend"))) {
            //跳转到手持身份证
            Map<String, Object> m = new HashMap<>();
            m.put("faceFlag", "2");
            return success(m);
        } else {
            //可以继续做人脸，跳转到人脸页面
            Map<String, Object> m = new HashMap<>();
            m.put("faceFlag", "3");
            return success(m);
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
        if (StringUtils.isEmpty(token) || StringUtils.isEmpty(channel) || StringUtils.isEmpty(channelNo)) {
            logger.info("token：" + token + "   channel:" + channel + "    channelNo:" + channelNo);
            logger.info("前台传入数据有误");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        //缓存数据获取
        Map<String, Object> cacheMap = RedisUtils.getExpireMap(token);
        if (MapUtils.isEmpty(cacheMap)) {
            logger.info("Jedis数据获取失败");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        String idNumber = cacheMap.get("idCard").toString();// 身份证号
        String name = cacheMap.get("name").toString();// 姓名
        String custNo = cacheMap.get("custNo").toString();
        String userId = cacheMap.get("userId").toString();
        if (StringUtils.isEmpty(idNumber) || StringUtils.isEmpty(name)
                || StringUtils.isEmpty(custNo) || StringUtils.isEmpty(userId)) {
            logger.info("idNumber:" + idNumber + "  name:" + name + "   custNo:" + custNo + "    userId:" + userId);
            logger.info("redis获取数据为空");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }

        //
        InputStream inputStream = faceImg.getInputStream();
        StringBuffer filePath = new StringBuffer(storageConfig.getFacePath()).append(File.separator).append(custNo).append(File.separator).append(ConstUtil.ATTACHTYPE_APP01)
                .append(File.separator);// File.separator
        createDir(String.valueOf(filePath));
        String fileName = UUID.randomUUID().toString().replaceAll("-", "");
        filePath = filePath.append(fileName).append(".jpg"); // 测试打开
        FileImageOutputStream outImag = new FileImageOutputStream(new File(String.valueOf(filePath)));
        byte[] bufferOut = new byte[1024];
        int bytes;
        while ((bytes = inputStream.read(bufferOut)) != -1) {
            outImag.write(bufferOut, 0, bytes);
        }
        outImag.close();
        inputStream.close();

        InputStream is = new BufferedInputStream(new FileInputStream(String.valueOf(filePath)));
        String MD5 = DigestUtils.md5Hex(is);
        is.close();
        //
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("channel", channel);
        paramMap.put("channelNo", channelNo);
        paramMap.put("custNo", custNo);// 客户编号
        paramMap.put("attachType", ConstUtil.ATTACHTYPE_APP01);// 影像类型
        paramMap.put("attachName", ConstUtil.ATTACHTYPE_APP01_DESC);
        paramMap.put("md5", MD5);
        paramMap.put("filePath", filePath);
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
        return validateUserFlag(userId, token, channel, channelNo, cacheMap);
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
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        //缓存数据获取及非空判断
        Map<String, Object> cacheMap = RedisUtils.getExpireMap(token);
        if (MapUtils.isEmpty(cacheMap)) {
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
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        //判断是否需要做人脸识别
        Map<String, Object> paramMap = new HashMap<>();
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
        JSONObject headjson = new JSONObject((Map) resultmap.get("head"));
        String retFlag = headjson.getString("retFlag");
        String retMsg = headjson.getString("retMsg");
        if (!"00000".equals(retFlag)) {
            return fail(ConstUtil.ERROR_CODE, retMsg);
        }
        JSONObject body = new JSONObject((Map) resultmap.get("body"));
        String code = body.getString("code"); //结果标识码
        switch (code) {
            case "00": //人脸识别通过
                validateUserFlag(userId, token, channel, channelNo, cacheMap);

                break;
            case "01": //01：未通过人脸识别，剩余次数为0，不能再做人脸识别，录单终止
                //终止
                logger.info("未通过人脸识别，剩余次数为0，不能再做人脸识别，录单终止");
                return fail(ConstUtil.ERROR_CODE, "不能再做人脸识别，录单终止!");
            case "02": {//02：未通过人脸识别，剩余次数为0，不能再做人脸识别，但可以上传替代影像
                //跳转替代影像
                logger.info("未通过人脸识别，剩余次数为0，不能再做人脸识别，但可以上传替代影像");
                Map<String, Object> map = new HashMap<>();
                map.put("faceFlag", "2");// 手持身份证

                return success(map);
            }
            case "10": {//10：未通过人脸识别，可以再做人脸识别
                //可以做人脸识别
                logger.info("未通过人脸识别，可以再做人脸识别");
                Map<String, Object> map = new HashMap<>();
                map.put("faceFlag", "3");// 人脸识别

                return success(map);
            }
        }

        return success();
    }

    private Map<String, Object> validateUserFlag(String userId, String token, String channel, String channelNo, Map cacheMap) {
        logger.info("验证是否设置过支付密码*******************开始");
        Map<String, Object> pwdmap = new HashMap<>();
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
        Map<String, Object> bodyjson = (Map<String, Object>) resultmap.get("body");
        String payPasswdFlag = (String) bodyjson.get("payPasswdFlag");
        if (payPasswdFlag.equals("1")) {// 1：已设置
            Map<String, Object> m = new HashMap<>();
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
            Map<String, Object> m = new HashMap<>();
            m.put("faceFlag", "0");
            return success(m);
        }
    }

}
