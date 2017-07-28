package com.haiercash.payplatform.service.shunguang;

import com.haiercash.commons.redis.Cache;
import com.haiercash.payplatform.common.entity.ReturnMessage;
import com.haiercash.payplatform.service.BaseService;
import com.haiercash.payplatform.common.utils.ConstUtil;
import com.haiercash.payplatform.common.utils.ocr.OCRIdentityTC;
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
import java.util.Map;
import java.util.UUID;

/**
 * Created by yuanli on 2017/7/27.
 */
@Service
public class OCRIdentityService extends BaseService {
    public Log logger = LogFactory.getLog(getClass());
    @Autowired
    private Cache cache;

    @Value("${app.other.haierDataImg_url}")
    protected String haierDataImg_url;

    public Map<String, Object> ocrIdentity(MultipartFile ocrImg, HttpServletRequest request, HttpServletResponse response) throws Exception{
        logger.info("OCR身份信息获取*************开始");
        //图片非空判断
        if (ocrImg.isEmpty()) {
            return fail("01", "图片为空");
        }
        //token非空判断
        String token = request.getParameter("token");
        if(StringUtils.isEmpty(token)){
            logger.info("获取token为空");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.FAILED_INFO);
        }
        //缓存数据获取
        Map<String, Object> cacheMap = cache.get(token);
        if(cacheMap.isEmpty()){
            logger.info("Jedis数据获取失败");
        }
        //TODO!!!!
        String userId = (String) cacheMap.get("userId");
        if(StringUtils.isEmpty(userId)){
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
        if(!"0000".equals(code)){
            return fail("02", returnMessage.getMessage());
        }

        //获取OCR返回信息进行redis存储
        JSONObject cardsResJson = new JSONObject((new JSONObject(returnMessage.getRetObj())).get("cards"));
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
        String certImagePath = saveImage2Disk(userId,imageStr);

        if(!StringUtils.isEmpty(cardSide) && "front".equals(cardSide)) {
            logger.info("身份证正面存储路径为------------------："+certImagePath);
            cacheMap.put("certImagePathA",certImagePath);
        }else if(!StringUtils.isEmpty(cardSide) && "back".equals(cardSide)){
            logger.info("身份证反面存储路径为------------------："+certImagePath);
            cacheMap.put("certImagePathB",certImagePath);
        }

        cache.set(token, cacheMap);
        logger.info("OCR身份信息获取*************结束");
        return success();
    }

    private String saveImage2Disk(String userId,String imageStr) throws IOException {
        String imgPath="";
        try {
            String fn = haierDataImg_url + "/certImage/"+userId;
            File path = new File(fn);
            if (!path.exists()){
                path.mkdirs();
            }
            logger.info("身份证正反面照片缓存路径:"+fn);
            String uuId = UUID.randomUUID().toString().replaceAll("-", "");
            imgPath = path+ "/"+uuId+ ".jpg";
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

        }catch (IOException e) {
            e.printStackTrace();
            logger.info("身份证照片保存失败:"+e.getMessage());
        }
        return imgPath;
    }

    public Map<String, Object> savaIdentityInfo(Map<String, Object> map){
        String token = (String) map.get("token");
        String name = (String) map.get("name");

        if(StringUtils.isEmpty(token) || StringUtils.isEmpty(name)){
            logger.info("token:" + token + "  name:" + name);
            logger.info("前台获取请求参数有误");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.FAILED_INFO);
        }

        Map<String, Object> cacheMap = cache.get(token);
        cacheMap.put("name", name);

        cache.set(token, cacheMap);

        return success();
    }
}
