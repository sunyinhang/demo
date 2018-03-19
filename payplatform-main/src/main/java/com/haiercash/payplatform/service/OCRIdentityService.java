package com.haiercash.payplatform.service;

import com.haiercash.spring.rest.IResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * Created by yuanli on 2017/7/27.
 */
public interface OCRIdentityService {
    //OCR身份信息获取
    IResponse<Map> ocrIdentity(OcrPathType ocrPathType, MultipartFile ocrImg) throws Exception;

    //保存OCR信息
    Map<String, Object> savaIdentityInfo(Map<String, Object> map);

    //获取省市区
    Map<String, Object> getArea(Map<String, Object> map);

    //获取卡信息
    Map<String, Object> getCardInfo(String cardNo);

    //发送短信验证码
    Map<String, Object> sendMessage(Map<String, Object> map);

    //发送短信验证码
    Map<String, Object> sendMsg(Map<String, Object> map);

    //实名认证
    IResponse<Map> realAuthentication(Map<String, Object> map) throws IOException;

    //实名认证(标准现金贷)
    IResponse<Map> realAuthenticationForXjd(Map<String, Object> map) throws IOException;

    //协议展示：(1)展示注册协议(2)个人征信(3)借款合同
    Map<String, Object> treatyShowServlet(String token, Map<String, Object> params) throws Exception;

    //获取绑定手机号
    Map<String, Object> getPhoneNo(String token);


    enum OcrPathType {
        //按 userId 存储
        ByUserId,
        //按识别的 idNo 存储
        ByIdNo
    }
}
