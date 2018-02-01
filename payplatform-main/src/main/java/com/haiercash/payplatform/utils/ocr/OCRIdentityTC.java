package com.haiercash.payplatform.utils.ocr;

import com.haiercash.core.lang.StringUtils;
import com.haiercash.core.serialization.JsonSerializer;
import com.haiercash.spring.rest.IResponse;
import com.haiercash.spring.rest.common.CommonResponse;
import com.idcard.Demo;
import com.idcard.GlobalData;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


/**
 * Created by Yubing on 2017/3/3.
 * 天诚OCR身份证信息识别
 */
public class OCRIdentityTC {
    private static final String SPACE = " ";
    public static final Demo engineOCR = new Demo();
    public final Log logger = LogFactory.getLog(getClass());

    public IResponse<Map> ocrIdCard(byte[] buffer) {
        try {
            if (buffer == null)
                return CommonResponse.fail("0001", "所上传的照片数据为空");

            String result = execOrc(buffer);
            if (StringUtils.isEmpty(result)) {
                logger.info("集团大数据，OCR(TC)身份证信息识别请求返回结果为空！");
                return CommonResponse.fail("9999", "返回数据为空");
            } //封装成想要格式
            Map<String, Object> resJson = JsonSerializer.deserializeMap(result);
            String name = StringUtils.remove((String) resJson.get("NAME"), SPACE);
            String gender = StringUtils.remove((String) resJson.get("SEX"), SPACE);
            String id_card_number = StringUtils.remove((String) resJson.get("NUM"), SPACE);
            String birthday = StringUtils.remove((String) resJson.get("BIRTHDAY"), SPACE);//    //要特殊处理一下！！
            //====日期格式化====
            if (!StringUtils.isEmpty(birthday)) {
                String birthD = birthday.replaceAll("[^\\d]", "");
                StringBuilder birthStrB = new StringBuilder(birthD);
                if (birthStrB.length() >= 8) {
                    birthday = birthStrB.insert(4, "-").insert(7, "-").toString();
                }
            }
            //=====日期格式化结束===
            String race = StringUtils.remove((String) resJson.get("FOLK"), SPACE);
            String address = StringUtils.remove((String) resJson.get("ADDRESS"), SPACE);
            String issued_by = StringUtils.remove((String) resJson.get("ISSUE"), SPACE);
            String valid_data = StringUtils.remove((String) resJson.get("PERIOD"), SPACE);   //有可能会识别出错，现不检验格式
            boolean isFront = StringUtils.isNotEmpty(name);//是否正面
            //============
            Map<String, Object> cards = new HashMap<>();
            cards.put("name", name);
            cards.put("gender", gender);
            cards.put("id_card_number", id_card_number);
            cards.put("birthday", birthday);  //要特殊处理一下
            cards.put("race", race);
            cards.put("address", address);
            cards.put("issued_by", issued_by);
            cards.put("valid_date", valid_data);
            cards.put("side", isFront ? "front" : "back");
            //============
            logger.info("调用第三方(TC)OCR身份证识别，处理成功");
            return CommonResponse.success(cards);
        }catch (Exception e){
            logger.error("调用第三方(TC)OCR身份证识别，出现异常："+e.getMessage(),e);
            return CommonResponse.fail("9999", "出现异常" + e.getMessage());
        }
    }

    private String execOrc(byte[] pImgBuff) {
        String strResult = null;
        int ret;
        String osName = System.getProperty("os.name").toLowerCase(Locale.US);
        if (!StringUtils.isEmpty(osName) && osName.startsWith("win")) { //window
            String timeKey = "ed969133dd0eece08b478d9478ff3c06";
            ret = Demo.Start(timeKey);
        } else { //linux
            ret = Demo.Start(engineOCR.Byte2String(Demo.GetEngineTimeKey()));//初始化
        }
        if (ret == 100) {
            logger.info("天诚OCR身份证识别：该版本为试用版本，时间过期，请联系技术员\n");
        }
        else if (ret != 1) {
            logger.info("天诚OCR身份证识别：引擎初始化失败，请联系技术员\n");
        }

        logger.info("天诚OCR身份证识别,timeKey:" + engineOCR.Byte2String(Demo.GetEngineTimeKey()) + ",Version:" + engineOCR.Byte2String(Demo.GetVersion())
                + ",UserTimes:" + engineOCR.Byte2String(Demo.GetUseTimeString()));

        Demo.SetParam(GlobalData.T_SET_HEADIMG, 1);
        Demo.SetParam(GlobalData.T_SET_HEADIMGBUFMODE, 1);

        byte[] jsonbuf = Demo.RECOCROFMEM(GlobalData.TIDCARD2, pImgBuff, pImgBuff.length);
        if(jsonbuf != null)
        {
            try {
                strResult = new String(jsonbuf,"GBK");//此处只能GBK 编码
                //logger.info("天诚OCR身份证识别成功,返回值:" + strResult);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                logger.info("天诚OCR身份证识别：识别出现异常："+e.getMessage());
            }
           // com.idcard.StringManager.SaveJPGFile("d:/ImageFile/001.jpg", stringManager.headimg.getBytes());// 人头像保存
        }else{
            logger.info("天诚OCR身份证识别：返回值为空！");
        }
        return strResult;
    }
}
