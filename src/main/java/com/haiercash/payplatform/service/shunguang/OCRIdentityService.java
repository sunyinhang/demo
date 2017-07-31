package com.haiercash.payplatform.service.shunguang;

import com.amazonaws.util.IOUtils;
import com.haiercash.commons.redis.Cache;
import com.haiercash.commons.util.EncryptUtil;
import com.haiercash.payplatform.common.entity.ReturnMessage;
import com.haiercash.payplatform.service.AppServerService;
import com.haiercash.payplatform.service.BaseService;
import com.haiercash.payplatform.common.utils.ConstUtil;
import com.haiercash.payplatform.common.utils.ocr.OCRIdentityTC;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.io.FileInputStream;

/**
 * Created by yuanli on 2017/7/27.
 */
public interface OCRIdentityService{

    //OCR身份信息获取
    public Map<String, Object> ocrIdentity(MultipartFile ocrImg, HttpServletRequest request, HttpServletResponse response) throws Exception;

    //保存OCR信息
    public Map<String, Object> savaIdentityInfo(Map<String, Object> map);

    //获取省市区
    public Map<String, Object> getArea(Map<String, Object> map);

    //获取卡信息
    public Map<String, Object> getCardInfo(String cardNo);

    //发送短信验证码
    public Map<String, Object> sendMessage(String token);

    //发送短信验证码
    public Map<String, Object> sendMsg(String phone);

    //实名认证
    public Map<String, Object> realAuthentication(Map<String, Object> map) throws Exception;

}
