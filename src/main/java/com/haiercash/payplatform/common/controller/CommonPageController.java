package com.haiercash.payplatform.common.controller;

import com.haiercash.commons.redis.Cache;
import com.haiercash.payplatform.common.service.CustExtInfoService;
import com.haiercash.payplatform.common.service.FaceService;
import com.haiercash.payplatform.common.service.OCRIdentityService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;


/**
 * Created by yuanli on 2017/7/24.
 */
@RestController
public class CommonPageController extends BasePageController {
    public Log logger = LogFactory.getLog(getClass());
    //模块编码  01
    private static String MODULE_NO = "01";

    public CommonPageController() {
        super(MODULE_NO);
    }

    @Autowired
    private Cache cache;
    @Autowired
    private OCRIdentityService ocrIdentityService;
    @Autowired
    private FaceService faceService;
    @Autowired
    private CustExtInfoService custExtInfoService;

    /**
     * OCR获取身份信息
     * @param identityCard
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/payment/ocrIdentity", method = RequestMethod.POST)
    public Map<String, Object> ocrIdentity(@RequestBody MultipartFile identityCard, HttpServletRequest request, HttpServletResponse response) throws Exception {
        return ocrIdentityService.ocrIdentity(identityCard, request, response);
    }

    /**
     * 保存实名信息
     * @param map
     * @return
     */
    @RequestMapping(value = "/api/payment/savaIdentityInfo", method = RequestMethod.POST)
    public Map<String, Object> savaIdentityInfo(@RequestBody Map<String, Object> map){
        return ocrIdentityService.savaIdentityInfo(map);
    }

    /**
     * 获取省市区
     * @param params
     * @return
     */
    @RequestMapping(value = "/api/payment/getArea", method = RequestMethod.GET)
    public Map<String, Object> getArea(@RequestParam Map<String, Object> params){
        return ocrIdentityService.getArea(super.initParam(params));
    }

    /**
     * 获取卡信息
     * @param cardNo
     * @return
     */
    @RequestMapping(value = "/api/payment/getCardInfo", method = RequestMethod.GET)
    public Map<String, Object> getCardInfo(@RequestParam String cardNo){
        return ocrIdentityService.getCardInfo(cardNo);
    }

    /**
     * 发送短信验证码(1)
     * @param token
     * @return
     */
    @RequestMapping(value = "/api/payment/sendMessage", method = RequestMethod.GET)
    public Map<String, Object> sendMessage(@RequestParam(value = "token") String token,
                                           @RequestParam(value = "channel") String channel,
                                           @RequestParam(value = "channelNo") String channelNo){
        return ocrIdentityService.sendMessage(token, channel, channelNo);
    }

    /**
     * 发送短信验证码（2）
     * @param phone
     * @return
     */
    @RequestMapping(value = "/api/payment/sendMsg", method = RequestMethod.GET)
    public Map<String, Object> sendMsg(@RequestParam(value = "phone") String phone,
                                       @RequestParam(value = "channel") String channel,
                                       @RequestParam(value = "channelNo") String channelNo){
        return ocrIdentityService.sendMsg(phone, channel, channelNo);
    }

    /**
     * 实名绑卡
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/payment/realAuthentication", method = RequestMethod.POST)
    public Map<String, Object> realAuthentication(@RequestBody Map<String, Object> map) throws Exception{
        return ocrIdentityService.realAuthentication(map);
    }

    /**
     * 人脸识别
     * @param faceImg
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/payment/uploadFacePic", method = RequestMethod.POST)
    public Map<String, Object> uploadFacePic(@RequestBody MultipartFile faceImg, HttpServletRequest request, HttpServletResponse response) throws Exception {
        return faceService.uploadFacePic(faceImg, request, response);
    }
    /**
     * 支付密码设置
     * @param token
     * @param payPasswd
     * @return
     */
    @RequestMapping(value = "/api/payment/resetPayPasswd",method = RequestMethod.GET)
    public Map<String,Object> resetPayPasswd(@RequestParam(value = "token") String token,
                                             @RequestParam(value = "payPasswd") String payPasswd){
        return ocrIdentityService.resetPayPasswd(token,payPasswd);
    }

    /**
     * 协议展示：(1)展示注册协议(2)个人征信(3)借款合同
     * @param token
     * @param flag
     * @return
     */
    @RequestMapping(value = "/api/payment/treatyShow")
    public Map<String,Object> treatyShow(@RequestParam(value = "token") String token,
                                         @RequestParam(value="flag") String flag)throws Exception{
        return ocrIdentityService.treatyShowServlet(token,flag);
    }


    /**
     * 获取客户个人扩展信息
     * @param token
     * @param channel
     * @param channelNo
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/payment/getAllCustExtInfo", method = RequestMethod.POST)
    public Map<String, Object> getAllCustExtInfo(@RequestParam(value = "token") String token,
                                                 @RequestParam(value = "channel") String channel,
                                                 @RequestParam(value = "channelNo")  String channelNo )throws Exception {
        return custExtInfoService.getAllCustExtInfo(token, channel, channelNo);
    }


}
