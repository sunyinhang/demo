package com.haiercash.payplatform.controller;

import com.haiercash.commons.redis.Cache;
import com.haiercash.payplatform.service.shunguang.OCRIdentityService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by yuanli on 2017/7/24.
 */
@RestController
public class ShunGuangController extends BasePageController {
    public Log logger = LogFactory.getLog(getClass());
    //模块编码  01
    private static String MODULE_NO = "01";

    public ShunGuangController() {
        super(MODULE_NO);
    }

    @Autowired
    private Cache cache;
    @Autowired
    private OCRIdentityService ocrIdentityService;

    /**
     * OCR获取身份信息
     * @param ocrImg
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/payment/shunguang/ocrIdentity", method = RequestMethod.POST)
    public Map<String, Object> ocrIdentity(@RequestBody MultipartFile ocrImg, HttpServletRequest request, HttpServletResponse response) throws Exception {
        return ocrIdentityService.ocrIdentity(ocrImg, request, response);
    }

    /**
     * 保存实名信息
     * @param map
     * @return
     */
    @RequestMapping(value = "/api/payment/shunguang/savaIdentityInfo", method = RequestMethod.POST)
    public Map<String, Object> savaIdentityInfo(@RequestBody Map<String, Object> map){
        return ocrIdentityService.savaIdentityInfo(map);
    }

    /**
     * 获取省市区
     * @param params
     * @return
     */
    @RequestMapping(value = "/api/payment/shunguang/getArea", method = RequestMethod.GET)
    public Map<String, Object> getArea(@RequestParam Map<String, Object> params){
        return ocrIdentityService.getArea(params);
    }

    /**
     * 获取卡信息
     * @param cardNo
     * @return
     */
    @RequestMapping(value = "/api/payment/shunguang/getCardInfo", method = RequestMethod.GET)
    public Map<String, Object> getCardInfo(@RequestParam String cardNo){
        return ocrIdentityService.getCardInfo(cardNo);
    }

    /**
     * 发送短信验证码(1)
     * @param token
     * @return
     */
    @RequestMapping(value = "/api/payment/shunguang/sendMessage", method = RequestMethod.GET)
    public Map<String, Object> sendMessage(@RequestParam String token){
        return ocrIdentityService.sendMessage(token);
    }

    /**
     * 发送短信验证码（2）
     * @param phone
     * @return
     */
    @RequestMapping(value = "/api/payment/shunguang/sendMsg", method = RequestMethod.GET)
    public Map<String, Object> sendMsg(@RequestParam String phone){
        return ocrIdentityService.sendMsg(phone);
    }

    /**
     * 实名绑卡
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/payment/shunguang/realAuthentication", method = RequestMethod.POST)
    public Map<String, Object> realAuthentication(@RequestBody Map<String, Object> map) throws Exception{
        return ocrIdentityService.realAuthentication(map);
    }
}
