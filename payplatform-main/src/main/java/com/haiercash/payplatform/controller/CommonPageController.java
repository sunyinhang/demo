package com.haiercash.payplatform.controller;

import com.haiercash.commons.util.FileUtil;
import com.haiercash.payplatform.rest.IResponse;
import com.haiercash.payplatform.service.AppServerService;
import com.haiercash.payplatform.service.CommonPageService;
import com.haiercash.payplatform.service.CustExtInfoService;
import com.haiercash.payplatform.service.FaceService;
import com.haiercash.payplatform.service.InstallmentAccountService;
import com.haiercash.payplatform.service.LimitService;
import com.haiercash.payplatform.service.OCRIdentityService;
import com.haiercash.payplatform.service.PayPasswdService;
import com.haiercash.payplatform.service.RegisterService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;


/**
 * Created by yuanli on 2017/7/24.
 */
@RestController
public class CommonPageController extends BaseController {
    //模块编码  01
    private static String MODULE_NO = "01";
    public Log logger = LogFactory.getLog(getClass());
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
    @Autowired
    private LimitService limitService;
    @Autowired
    private RegisterService registerService;
    @Autowired
    private InstallmentAccountService InstallmentAccountService;
    @Autowired
    private CommonPageService commonPageService;

    public CommonPageController() {
        super(MODULE_NO);
    }

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
        return ocrIdentityService.savaIdentityInfo(map);
    }

    /**
     * 获取省市区
     *
     * @param params
     * @return
     */
    @RequestMapping(value = "/api/payment/getArea", method = RequestMethod.GET)
    public Map<String, Object> getArea(@RequestParam Map<String, Object> params) {
        return ocrIdentityService.getArea(params);
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
        return ocrIdentityService.sendMessage(params);
    }

    /**
     * 发送短信验证码（2）
     *
     * @param params
     * @return
     */
    @RequestMapping(value = "/api/payment/sendMsg", method = RequestMethod.GET)
    public Map<String, Object> sendMsg(@RequestParam Map<String, Object> params) {
        return ocrIdentityService.sendMsg(params);
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
        return ocrIdentityService.realAuthentication(map);
    }

    /**
     * 实名绑卡(标准化现金贷)
     *
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/payment/realAuthenticationForXjd", method = RequestMethod.POST)
    public Map<String, Object> realAuthenticationForXjd(@RequestBody Map<String, Object> map) throws Exception {
        return ocrIdentityService.realAuthenticationForXjd(map);
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
     * 上传替代影像
     *
     * @param faceImg
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/payment/uploadPersonPic", method = RequestMethod.POST)
    public Map<String, Object> uploadPersonPic(@RequestBody MultipartFile faceImg, HttpServletRequest request, HttpServletResponse response) throws Exception {
        return faceService.uploadPersonPic(faceImg, request, response);
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
     * 获取客户个人扩展信息（包含影像）
     *
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/payment/getAllCustExtInfo", method = RequestMethod.POST)
    public Map<String, Object> getAllCustExtInfo() throws Exception {
        return custExtInfoService.getAllCustExtInfoAndDocCde(super.getToken(), super.getChannel(), super.getChannelNo());
    }

    /**
     * 获取客户个人扩展信息
     *
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/payment/getAllCustExtInfoNoXx", method = RequestMethod.POST)
    public Map<String, Object> getAllCustExtInfoNoXx() throws Exception {
        return custExtInfoService.getAllCustExtInfo(super.getToken(), super.getChannel(), super.getChannelNo());
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
     * @param params
     * @return
     */
    @RequestMapping(value = "/api/payment/updPwdByIdentity", method = RequestMethod.POST)
    public Map<String, Object> updPwdByIdentity(@RequestBody Map<String, Object> params) {
        return payPasswdService.updPwdByIdentity(params);
    }

    /**
     * 确认支付密码（额度申请）
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "/api/payment/paymentPwdConfirm", method = RequestMethod.POST)
    public Map<String, Object> paymentPwdConfirm(@RequestBody Map<String, Object> map) {
        return payPasswdService.paymentPwdConfirm(super.getToken(), super.getChannel(), super.getChannelNo(), map);
    }


//    /**
//     * 额度申请提交
//     *
//     * @param verifyNo
//     * @param payPasswd
//     * @return
//     */
//    @RequestMapping(value = "/api/payment/edApply", method = RequestMethod.GET)
//    public Map<String, Object> edApply(@RequestParam(value = "verifyNo") String verifyNo,
//                                       @RequestParam(value = "password") String payPasswd) {
//        return payPasswdService.edApply(super.getToken(), verifyNo, payPasswd, super.getChannel(), super.getChannelNo());
//    }


    /**
     * 查询贷款详情
     *
     * @return
     */
    @RequestMapping(value = "/api/payment/queryLoanDetailInfo", method = RequestMethod.GET)
    public Map<String, Object> queryLoanDetailInfo(@RequestParam(value = "applSeq") String applSeq) {
        return payPasswdService.queryLoanDetailInfo(super.getToken(), applSeq);
    }


    /**
     * 贷款详情页面:按贷款申请查询分期账单
     *
     * @return
     */
    @RequestMapping(value = "/api/payment/queryApplListBySeq", method = RequestMethod.GET)
    public Map<String, Object> queryApplListBySeq(@RequestParam(value = "applSeq") String applSeq) {
        return payPasswdService.queryApplListBySeq(super.getToken(), super.getChannel(), super.getChannelNo(), applSeq);
    }


    /**
     * 贷款详情页面:还款总额
     *
     * @return
     */
    @RequestMapping(value = "/api/payment/queryApplAmtBySeqAndOrederNo", method = RequestMethod.GET)
    public Map<String, Object> queryApplAmtBySeqAndOrederNo(@RequestParam(value = "applSeq") String applSeq) {
        return payPasswdService.queryApplAmtBySeqAndOrederNo(super.getToken(), super.getChannel(), super.getChannelNo(), applSeq);
    }

    /**
     * 查询额度
     *
     * @return
     */
    @RequestMapping(value = "/api/payment/edCheck", method = RequestMethod.POST)
    public Map<String, Object> edCheck() {
        return payPasswdService.edCheck(super.getToken());
    }

    /**
     * @Title billCheck
     * @Description: 个人中心信息(账单查询)
     * @author yu jianwei
     * @date 2017/9/14 13:13
     */
    @RequestMapping(value = "/api/payment/billCheck", method = RequestMethod.POST)
    public Map<String, Object> billCheck() {
        return payPasswdService.billCheck(super.getToken());
    }

    /**
     * 根据流水号查询额度审批进度
     *
     * @return
     */
    @RequestMapping(value = "/api/payment/queryApprovalProcessInfo", method = RequestMethod.GET)
    public Map<String, Object> queryApprProcessByCust(@RequestParam Map<String, Object> params) {
        return payPasswdService.approvalProcessInfo(super.getToken(), super.getChannel(), super.getChannelNo(), params);
    }

    /**
     * 根据流水号查询贷款审批进度
     *
     * @return
     */
    @RequestMapping(value = "/api/payment/queryDkProcessInfo", method = RequestMethod.GET)
    public Map<String, Object> queryDkProcessInfo(@RequestParam Map<String, Object> params) {
        return payPasswdService.queryDkProcessInfo(super.getToken(), super.getChannel(), super.getChannelNo(), params);
    }

    /**
     * 保存客户个人扩展信息
     *
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/payment/saveAllCustExtInfo", method = RequestMethod.POST)
    public Map<String, Object> saveAllCustExtInfo(@RequestBody Map<String, Object> params) throws Exception {
        return custExtInfoService.saveAllCustExtInfo(super.getToken(), super.getChannel(), super.getChannelNo(), params);
    }


    /**
     * 保存客户个人扩展信息(现金贷)
     *
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/payment/saveAllCustExtInfoForXjd", method = RequestMethod.POST)
    public Map<String, Object> saveAllCustExtInfoForXjd(@RequestBody Map<String, Object> params) throws Exception {
        return custExtInfoService.saveAllCustExtInfoForXjd(super.getToken(), super.getChannel(), super.getChannelNo(), params);
    }

    /**
     * 扩展信息上传影像
     *
     * @param iconImg
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/payment/upIconPic", method = RequestMethod.POST)
    public Map<String, Object> upIconPic(@RequestBody MultipartFile iconImg, HttpServletRequest request, HttpServletResponse response) throws Exception {
        return custExtInfoService.upIconPic(iconImg, request, response);
    }

    /**
     * 扩展信息删除影像
     *
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/payment/attachDelete", method = RequestMethod.POST)
    public Map<String, Object> attachDelete(@RequestBody Map<String, Object> params) throws Exception {
        return custExtInfoService.attachDelete(super.getToken(), super.getChannel(), super.getChannelNo(), params);
    }

    /**
     * 额度激活（判断跳转哪个页面）
     *
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/payment/creditLineApply", method = RequestMethod.POST)
    public Map<String, Object> CreditLineApply() throws Exception {
        return limitService.CreditLineApply(super.getToken(), super.getChannel(), super.getChannelNo());
    }

    /**
     * 判断用户是否注册
     *
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/payment/isRegister", method = RequestMethod.POST)
    public Map<String, Object> isRegister(@RequestBody Map<String, Object> params) throws Exception {
        return registerService.isRegister(super.getToken(), super.getChannel(), super.getChannelNo(), params);
    }

    /**
     * 判断用户是否注册 不需要token
     *
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/payment/isRegisterNotoken", method = RequestMethod.POST)
    public Map<String, Object> isRegisterNotoken(@RequestBody Map<String, Object> params) throws Exception {
        return registerService.isRegisterNotoken(super.getChannel(), super.getChannelNo(), params);
    }

    /**
     * 登陆密码设置
     *
     * @return
     */
    @RequestMapping(value = "/api/payment/landPasswd", method = RequestMethod.POST)
    public Map<String, Object> landPasswd(@RequestBody Map<String, Object> map) {
        return payPasswdService.landPasswd(super.getToken(), super.getChannelNo(), super.getChannel(), map);
    }

    /**
     * 查询全部贷款信息列表
     *
     * @return
     */
    @RequestMapping(value = "/api/payment/queryAllLoanInfo", method = RequestMethod.POST)
    public Map<String, Object> QueryAllLoanInfo(@RequestBody Map<String, Object> map) {
        return InstallmentAccountService.queryAllLoanInfo(super.getToken(), super.getChannelNo(), super.getChannel(), map);
    }

    /**
     * 查询待提交订单列表
     *
     * @return
     */
    @RequestMapping(value = "/api/payment/queryPendingLoanInfo", method = RequestMethod.POST)
    public Map<String, Object> QueryPendingLoanInfo(@RequestBody Map<String, Object> map) {
        return InstallmentAccountService.QueryPendingLoanInfo(super.getToken(), super.getChannelNo(), super.getChannel(), map);
    }

    /**
     * 查询待还款订单列表
     *
     * @return
     */
    @RequestMapping(value = "/api/payment/queryPendingRepaymentInfo", method = RequestMethod.POST)
    public Map<String, Object> queryPendingRepaymentInfo(@RequestBody Map<String, Object> map) {
        return InstallmentAccountService.queryPendingRepaymentInfo(super.getToken(), super.getChannelNo(), super.getChannel(), map);
    }

    /**
     * 查询已提交贷款申请列表
     *
     * @return
     */
    @RequestMapping(value = "/api/payment/queryApplLoanInfo", method = RequestMethod.POST)
    public Map<String, Object> queryApplLoanInfo(@RequestBody Map<String, Object> map) {
        return InstallmentAccountService.queryApplLoanInfo(super.getToken(), super.getChannelNo(), super.getChannel(), map);
    }

    /**
     * 查询订单详情（订单提交之后可查询）
     *
     * @return
     */
    @RequestMapping(value = "/api/payment/queryOrderInfo", method = RequestMethod.POST)
    public Map<String, Object> queryOrderInfo(@RequestBody Map<String, Object> map) {
        return InstallmentAccountService.queryOrderInfo(super.getToken(), super.getChannelNo(), super.getChannel(), map);
    }


    /**
     * 获取绑定手机号
     *
     * @return
     */
    @RequestMapping(value = "/api/payment/getPhoneNo", method = RequestMethod.GET)
    public Map<String, Object> getPhoneNo() {
        return ocrIdentityService.getPhoneNo(super.getToken());
    }

    /**
     * 获取个人中心信息（本月应还）
     *
     * @return
     */
    @RequestMapping(value = "/api/payment/getPersonalCenterInfo", method = RequestMethod.POST)
    public Map<String, Object> getPersonalCenterInfo() {
        return payPasswdService.getPersonalCenterInfo(super.getToken(), super.getChannelNo(), super.getChannel());
    }

    /**
     * 影像下载(暂时无用)
     *
     * @return
     */
    @RequestMapping(value = "/api/payment/attachPic", method = RequestMethod.GET)
    public void attachPic(HttpServletRequest request, HttpServletResponse response, @RequestParam("filePath") String filePath) throws IOException {
        FileUtil.download(request, response, filePath);
        // return custExtInfoService.attachPic(super.getToken(), super.getChannelNo(), super.getChannel(), filePath);
    }

    /**
     * 查询返回实名认证需要的数据
     *
     * @return
     */
    @RequestMapping(value = "/api/payment/queryCustNameByUId", method = RequestMethod.GET)
    public Map<String, Object> queryCustNameByUId() {
        return payPasswdService.queryCustNameByUId(super.getToken());
    }

    @RequestMapping(value = "/api/payment/report", method = RequestMethod.POST)
    public void report(@RequestBody String error) {
        logger.error(error);
    }

    /**
     * 合同展示（1.签章合同   2.征信合同  3.注册协议）
     *
     * @param params
     * @return
     */
    @RequestMapping(value = "/api/payment/showcontract", method = RequestMethod.GET)
    public Map<String, Object> contract(@RequestParam Map<String, Object> params) throws Exception {
        return commonPageService.showcontract(params);
    }

    /**
     * @Title saveUauthUsers
     * @Description: 用户注册
     * @author yu jianwei
     * @date 2017/10/9 9:45
     */
    @RequestMapping(value = "/api/payment/saveUauthUsers", method = RequestMethod.POST)
    public Map<String, Object> saveUauthUsers(@RequestBody Map<String, Object> params) throws Exception {
        return registerService.saveUauthUsers("", params);
    }

    /**
     * @Title validateUsers
     * @Description: 用户登陆
     * @author ljy
     * @date 2017/10/12 9:45
     */
    @RequestMapping(value = "/api/payment/validateUsers", method = RequestMethod.POST)
    public Map<String, Object> validateUsers(@RequestBody Map<String, Object> params) throws Exception {
        return registerService.validateUsers(super.getChannel(), super.getChannelNo(), params);
    }

    /**
     * @Title custUpdatePwd
     * @Description: 客户登录密码设置、修改（验证码）
     * @author yu jianwei
     * @date 2017/10/13 15:26
     */
    @RequestMapping(value = "/api/payment/custUpdatePwd", method = RequestMethod.POST)
    public Map<String, Object> custUpdatePwd(@RequestBody Map<String, Object> params) throws Exception {
        return registerService.custUpdatePwd(params);
    }

    /**
     * @Title getBankCard
     * @Description: 根据客户编码获取银行卡信息
     * @author
     * @date 2017/10/17 15:26
     */
    @RequestMapping(value = "/api/payment/getBankCard", method = RequestMethod.POST)
    public Map<String, Object> getBankCard() throws Exception {
        return custExtInfoService.getBankCard(super.getToken(), super.getChannelNo(), super.getChannel());
    }

    /**
     * @Title getLoanType
     * @Description: 获取贷款品种相关信息及银行卡信息
     * @author
     * @date 2017/10/17 15:26
     */
    @RequestMapping(value = "/api/payment/getLoanTypeAndBankInfo", method = RequestMethod.POST)
    public IResponse<Map> getLoanTypeAndBankInfo() throws Exception {
        return custExtInfoService.getLoanTypeAndBankInfo(super.getToken(), super.getChannel(), super.getChannelNo());
    }

    /**
     * @Title getPaySs
     * @Description: 还款式算
     * @author
     * @date 2017/10/17 15:26
     */
    @RequestMapping(value = "/api/payment/getPaySs", method = RequestMethod.POST)
    public Map<String, Object> getPaySs(@RequestBody Map<String, Object> param) throws Exception {
        return custExtInfoService.getPaySs(super.getToken(), super.getChannelNo(), super.getChannel(), param);
    }

    /**
     * @Title
     * @Description:
     * @author
     * @date 2017/10/17 15:26
     */
    @RequestMapping(value = "/api/payment/getCustWhiteListCmis", method = RequestMethod.POST)
    public Map<String, Object> getCustWhiteListCmis() throws Exception {
        return custExtInfoService.getCustYsxEd(super.getToken(), super.getChannelNo(), super.getChannel());
    }

    /**
     * 3.1.13(GET)查询所有贷款用途列表(APP)
     *
     * @param params
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/payment/getPurpose", method = RequestMethod.GET)
    public Map<String, Object> getPurpose(@RequestParam Map<String, Object> params) {
        return commonPageService.getPurpose(params);
    }

    /**
     * @Title 贷款详情查询（现金贷）
     * @Description:
     * @author
     * @date 2017/10/21 15:26
     */
    @RequestMapping(value = "/api/payment/orderQueryXjd", method = RequestMethod.POST)
    public Map<String, Object> orderQueryXjd(@RequestBody Map<String, Object> params) throws Exception {
        return InstallmentAccountService.orderQueryXjd(super.getToken(), super.getChannelNo(), super.getChannel(), params);
    }

    /**
     * 海尔员工支付密码设置
     *
     * @return
     */
    @RequestMapping(value = "/api/payment/resetPayPasswdForHaier", method = RequestMethod.POST)
    public Map<String, Object> resetPayPasswdForHaier(@RequestBody Map<String, Object> map) {
        return payPasswdService.resetPayPasswdForHaier(super.getToken(), super.getChannelNo(), super.getChannel(), map);
    }

    /**
     * 第三方用户绑定页面
     *
     * @return
     */
    @RequestMapping(value = "/api/payment/validateAndBindOtherUser", method = RequestMethod.POST)
    public Map<String, Object> validateAndBindOtherUser(@RequestBody Map<String, Object> map) throws Exception {
        return registerService.validateAndBindOtherUser(super.getToken(), super.getChannel(), super.getChannelNo(), map);
    }

}
