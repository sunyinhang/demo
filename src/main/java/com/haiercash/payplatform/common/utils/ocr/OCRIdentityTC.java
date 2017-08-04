package com.haiercash.payplatform.common.utils.ocr;

import com.haiercash.payplatform.common.entity.ReturnMessage;
import com.idcard.Demo;
import com.idcard.GlobalData;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.util.StringUtils;

import java.io.*;

import static java.lang.System.out;

/**
 * Created by Yubing on 2017/3/3.
 * 天诚OCR身份证信息识别
 */
public class OCRIdentityTC {

    public static Demo engineOCR = new Demo();
    public Log logger = LogFactory.getLog(getClass());
    
    public ReturnMessage OCRIDUpload(InputStream image_files) throws IOException{

        ReturnMessage returnMessage = new ReturnMessage();

        InputStream image_file = image_files;

        try {
            if (StringUtils.isEmpty(image_file)) {
                returnMessage.setCode("0001");
                returnMessage.setMessage("所上传的照片数据为空");
            }
            byte[] pImgBuff = GetImgByte(image_file);
            String result = getOCR(pImgBuff);

            if (StringUtils.isEmpty(result)) {
                returnMessage.setCode("9999");
                returnMessage.setMessage("返回数据为空");
                logger.info("集团大数据，OCR(TC)身份证信息识别请求返回结果为空！");
            } else { //封装成想要格式
                JSONObject resJson = new JSONObject(result);
                String name = ((String) resJson.get("NAME")).replaceAll(" ","");
                String gender = ((String) resJson.get("SEX")).replaceAll(" ","");
                String id_card_number = ((String) resJson.get("NUM")).replaceAll(" ","");
                String birthday = ((String) resJson.get("BIRTHDAY")).replaceAll(" ","");   //要特殊处理一下！！
                //====日期格式化====
                if(!StringUtils.isEmpty(birthday)){
                    //Date orig = new SimpleDateFormat("yyyy年MM月dd日").parse(birthday);//定义起始日期
                    //SimpleDateFormat current = new SimpleDateFormat("yyyy-MM-dd");//定义起始日期
                    //birthday=current.format(orig);
                    String birthD=birthday.replaceAll("[^\\d]","");
                    StringBuilder birthStrB=new StringBuilder(birthD);
                    if (birthStrB.length()>=8){
                        birthday=birthStrB.insert(4,"-").insert(7,"-").toString();
                    }
                }
                //=====日期格式化结束===
                String race = ((String) resJson.get("FOLK")).replaceAll(" ","");
                String address = ((String) resJson.get("ADDRESS")).replaceAll(" ","");
                String issued_by = ((String) resJson.get("ISSUE")).replaceAll(" ","");
                String valid_data = ((String) resJson.get("PERIOD")).replaceAll(" ","");  //有可能会识别出错，现不检验格式

                String side="";
                if(!StringUtils.isEmpty(name))
                    side="front";
                else if(!StringUtils.isEmpty(issued_by))side="back";
                //============
                JSONObject cards = new JSONObject();

                cards.put("name", name);
                cards.put("gender", gender);
                cards.put("id_card_number", id_card_number);
                cards.put("birthday", birthday);  //要特殊处理一下
                cards.put("race", race);
                cards.put("address", address);
                cards.put("issued_by", issued_by);
                cards.put("valid_date", valid_data);
                cards.put("side", side);
                //============
                JSONObject reponseJson = new JSONObject();
                reponseJson.put("cards", cards);
                returnMessage.setCode("0000");
                returnMessage.setMessage("处理成功");
                returnMessage.setRetObj(reponseJson);

                System.out.println("调用第三方(TC)OCR身份证识别，处理成功");
            }
        }catch (Exception e){
            returnMessage.setCode("9999");
            returnMessage.setMessage("出现异常"+e.getMessage());
            logger.error("调用第三方(TC)OCR身份证识别，出现异常："+e.getMessage(),e);

        }
        logger.info("调用第三方(TC)OCR身份证识别，处理成功");
        return returnMessage;

    }

    public byte[] GetImgByte(InputStream image_files) throws IOException {

        byte[] data = null;
        InputStream input = image_files;
        //input = (InputStream) imgFileMap.get("image_file");//new FileImageInputStream(new File(image_file));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try{
            byte[] buf = new byte[1024];
            int numBytesRead = 0;
            while ((numBytesRead = input.read(buf)) != -1) {
                output.write(buf, 0, numBytesRead);
            }
            data = output.toByteArray();

        }catch (Exception e){
            //logger.info("图片Byte获取失败");
            return null;

        }finally{
            output.close();
            input.close();
            return data;
        }


    }
    public String getOCR(byte[] pImgBuff){

        String strResult = null;
        int ret = engineOCR.Start(engineOCR.Byte2String(engineOCR.GetEngineTimeKey()));//初始化
        if (ret == 100) {
            out.println("天诚OCR身份证识别：该版本为试用版本，时间过期，请联系技术员\n");
            logger.info("天诚OCR身份证识别：该版本为试用版本，时间过期，请联系技术员\n");
        }
        else if (ret != 1) {
            out.println("天诚OCR身份证识别：引擎初始化失败，请联系技术员\n");
            logger.info("天诚OCR身份证识别：引擎初始化失败，请联系技术员\n");
        }

        logger.info("天诚OCR身份证识别,timeKey:"+engineOCR.Byte2String(engineOCR.GetEngineTimeKey())+",Version:"+engineOCR.Byte2String(engineOCR.GetVersion())
                +",UserTimes:" + engineOCR.Byte2String(engineOCR.GetUseTimeString()));

        byte [] jsonbuf = engineOCR.RECOCROFMEM(GlobalData.TIDCARD2,pImgBuff,pImgBuff.length);
        if(jsonbuf != null)
        {
            try {
                strResult = new String(jsonbuf,"GBK");//此处只能GBK 编码
                logger.info("天诚OCR身份证识别成功,返回值:" + strResult);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                logger.info("天诚OCR身份证识别：识别出现异常："+e.getMessage());
            }
           // com.idcard.StringManager.SaveJPGFile("d:/ImageFile/001.jpg", stringManager.headimg.getBytes());// 人头像保存
        }else{
            logger.info("天诚OCR身份证识别：返回值为空！");
            out.print("buf == null\n");
        }
        return strResult;
    }
}
