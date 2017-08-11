package com.haiercash.payplatform.common.controller;

import com.haiercash.commons.redis.Cache;
import com.haiercash.payplatform.common.service.*;
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
public class CommonPageController extends BaseController {
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
    @Autowired
    private PayPasswdService payPasswdService;
    @Autowired
    private AppServerService appServerService;


    /**
     * OCR获取身份信息
     *
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
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "/api/payment/savaIdentityInfo", method = RequestMethod.POST)
    public Map<String, Object> savaIdentityInfo(@RequestBody Map<String, Object> map) {
        return ocrIdentityService.savaIdentityInfo(super.initParam(map));
    }

    /**
     * 获取省市区
     *
     * @param params
     * @return
     */
    @RequestMapping(value = "/api/payment/getArea", method = RequestMethod.GET)
    public Map<String, Object> getArea(@RequestParam Map<String, Object> params) {
        return ocrIdentityService.getArea(super.initParam(params));
    }

    /**
     * 获取卡信息
     *
     * @param cardNo
     * @return
     */
    @RequestMapping(value = "/api/payment/getCardInfo", method = RequestMethod.GET)
    public Map<String, Object> getCardInfo(@RequestParam String cardNo) {
        return ocrIdentityService.getCardInfo(cardNo);
    }

    /**
     * 发送短信验证码(1)
     *
     * @param params
     * @return
     */
    @RequestMapping(value = "/api/payment/sendMessage", method = RequestMethod.GET)
    public Map<String, Object> sendMessage(@RequestParam Map<String, Object> params) {
        return ocrIdentityService.sendMessage(super.initParam(params));
    }

    /**
     * 发送短信验证码（2）
     *
     * @param params
     * @return
     */
    @RequestMapping(value = "/api/payment/sendMsg", method = RequestMethod.GET)
    public Map<String, Object> sendMsg(@RequestParam Map<String, Object> params) {
        return ocrIdentityService.sendMsg(super.initParam(params));
    }

    /**
     * 实名绑卡
     *
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/payment/realAuthentication", method = RequestMethod.POST)
    public Map<String, Object> realAuthentication(@RequestBody Map<String, Object> map) throws Exception {
        return ocrIdentityService.realAuthentication(super.initParam(map));
    }

    /**
     * 人脸识别
     *
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
     *
     * @return
     */
    @RequestMapping(value = "/api/payment/resetPayPasswd", method = RequestMethod.POST)
    public Map<String, Object> resetPayPasswd(@RequestBody Map<String, Object> map) {
        return payPasswdService.resetPayPasswd(super.getToken(), super.getChannelNo(), super.getChannel(), map);
    }

    /**
     * 协议展示：(1)展示注册协议(2)个人征信(3)借款合同
     *
     * @param params
     * @return
     */
    @RequestMapping(value = "/api/payment/treatyShow", method = RequestMethod.POST)
    public Map<String, Object> treatyShow(@RequestBody Map<String, Object> params) throws Exception {
        return ocrIdentityService.treatyShowServlet(super.getToken(), params);
    }


    /**
     * 获取客户个人扩展信息
     *
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/payment/getAllCustExtInfo", method = RequestMethod.POST)
    public Map<String, Object> getAllCustExtInfo() throws Exception {
        return custExtInfoService.getAllCustExtInfoAndDocCde(super.getToken(), super.getChannel(), super.getChannelNo());
    }

    /**
     * 页面缓存
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/api/payment/cache", method = RequestMethod.POST)
    public Map<String, Object> cache(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        return payPasswdService.cache(params, request);
    }

    /**
     * 修改支付密码（记得支付密码）
     *
     * @param params
     * @return
     */
    @RequestMapping(value = "/api/payment/updatePayPasswd", method = RequestMethod.POST)
    public Map<String, Object> updatePayPasswd(@RequestBody Map<String, Object> params) {
        return payPasswdService.updatePayPasswd(super.getToken(), params, super.getChannel(), super.getChannelNo());
    }

    /**
     * 实名认证找回密码
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/api/payment/updPwdByIdentity", method = RequestMethod.POST)
    public Map<String, Object> updPwdByIdentity(@RequestBody HttpServletRequest request) {
        return payPasswdService.updPwdByIdentity(request);
    }

    /**
     * 确认支付密码（额度申请）
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "/api/payment/paymentPwdConfirm", method = RequestMethod.POST)
    public Map<String, Object> paymentPwdConfirm(@RequestBody Map<String,Object> map) {
        return payPasswdService.paymentPwdConfirm(super.getToken(), super.getChannel(), super.getChannelNo(), map);
    }


    /**
     * 额度申请提交
     *
     * @param verifyNo
     * @param payPasswd
     * @return
     */
    @RequestMapping(value = "/api/payment/edApply", method = RequestMethod.GET)
    public Map<String, Object> edApply(@RequestParam(value = "verifyNo") String verifyNo,
                                       @RequestParam(value = "password") String payPasswd) {
        return payPasswdService.edApply(super.getToken(), verifyNo, payPasswd, super.getChannel(), super.getChannelNo());
    }


    /**
     * 查询贷款详情
     *
     * @return
     */
    @RequestMapping(value = "/api/payment/queryLoanDetailInfo", method = RequestMethod.GET)
    public Map<String, Object> queryLoanDetailInfo() {
        return payPasswdService.queryLoanDetailInfo(super.getToken());
    }


    /**
     * 贷款详情页面:按贷款申请查询分期账单
     *
     * @return
     */
    @RequestMapping(value = "/api/payment/queryApplListBySeq", method = RequestMethod.GET)
    public Map<String, Object> queryApplListBySeq() {
        return payPasswdService.queryApplListBySeq(super.getToken(), super.getChannel(), super.getChannelNo());
    }


    /**
     * 贷款详情页面:还款总额
     *
     * @return
     */
    @RequestMapping(value = "/api/payment/queryApplAmtBySeqAndOrederNo", method = RequestMethod.GET)
    public Map<String, Object> queryApplAmtBySeqAndOrederNo() {
        return payPasswdService.queryApplAmtBySeqAndOrederNo(super.getToken(), super.getChannel(), super.getChannelNo());
    }

    /**
     * 查询额度
     *
     * @return
     */
    @RequestMapping(value = "/api/payment/edCheck", method = RequestMethod.GET)
    public Map<String, Object> edCheck() {
        return payPasswdService.edCheck(super.getToken());
    }

    /**
     * 根据流水号查询额度审批进度
     *
     * @return
     */
    @RequestMapping(value = "/api/payment/queryApprovalProcessInfo", method = RequestMethod.GET)
    public Map<String, Object> queryApprProcessByCust() {
        return payPasswdService.approvalProcessInfo(super.getToken(), super.getChannel(), super.getChannelNo());
    }

    /**
     * 保存客户个人扩展信息
     *
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/payment/saveAllCustExtInfo", method = RequestMethod.POST)
    public Map<String, Object> saveAllCustExtInfo(@RequestBody Map<String, Object> params) throws Exception {
        return custExtInfoService.saveAllCustExtInfo(super.getToken(), super.getChannel(), super.getChannelNo(),params);
    }

}
