package com.haiercash.appserver.web;

import com.haiercash.common.config.EurekaServer;
import com.haiercash.appserver.service.AppManageService;
import com.haiercash.appserver.service.CrmService;
import com.haiercash.appserver.util.annotation.RequestCheck;
import com.haiercash.common.util.ConstUtil;
import com.haiercash.commons.util.HttpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Objects;

/**
 * CRM接口调用.
 * Created by yinjun on 2016/9/19.
 */
@RestController
public class CrmController extends BaseController {

    private static String MODULE_NO = "15";

    public CrmController() {
        super(MODULE_NO);
    }

    @Autowired
    private CrmService crmService;

    @Autowired
    private AppManageService appManageService;


    /**
     * @param custNo 客户编号
     * @param flag   是否需要传联系人 Y 需要  N 不需要
     * @return
     */
    @RequestMapping(value = "/app/appserver/getCustExtInfo", method = RequestMethod.GET)
    public Map<String, Object> getCustExtInfo(String custNo, String flag) {
        if (StringUtils.isEmpty(custNo)) {
            return fail("01", "客户编号不能为空");
        }
        // 默认传联系人信息
        if (StringUtils.isEmpty(flag)) {
            flag = "Y";
        }
        return crmService.getCustExtInfo(custNo, flag);
    }

    /**
     * 查询客户全部扩展信息(2017年1月5日添加  crm接口变动)
     *
     * @param custNo 客户编号
     * @param flag   是否需要传联系人 Y 需要  N 不需要
     * @return
     */
    @RequestMapping(value = "/app/appserver/getAllCustExtInfo", method = RequestMethod.GET)
    public Map<String, Object> getAllCustExtInfo(String custNo, String flag) {
        if (StringUtils.isEmpty(custNo)) {
            return fail("01", "客户编号不能为空");
        }
        // 默认传联系人信息
        if (StringUtils.isEmpty(flag)) {
            flag = "Y";
        }
        return crmService.getAllCustExtInfo(custNo, flag);
    }

    /**
     * 查询销售代表关联的所有门店列表(商户版APP).
     *
     * @param userId 销售代表用户id
     * @return Map
     */
    @RequestMapping(value = "/app/appserver/crm/cust/getStores", method = RequestMethod.GET)
    @RequestCheck
    public Map<String, Object> getStores(String userId) {
        if (StringUtils.isEmpty(userId)) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "用户编号不能为空");
        }
        //return HttpUtil.json2Map(HttpUtil.restGet(this.getGateUrl() + "/app/crm/cust/getStores?userId=" + userId));
        String json = HttpUtil.restGet(EurekaServer.CRM + "/app/crm/cust/getStores?userId=" + userId);
        return HttpUtil.json2DeepMap(json);
    }

    /**
     * 查询所有贷款用途列表.
     *
     * @return Map
     */
    @RequestMapping(value = "/app/appserver/crm/cust/getPurpose", method = RequestMethod.GET)
    public Map<String, Object> getPurpose() {
        // return HttpUtil.json2Map(HttpUtil.restGet(this.getGateUrl() + "/app/crm/cust/getPurpose"));
        return HttpUtil.restGetMap(EurekaServer.CRM + "/app/crm/cust/getPurpose", HttpStatus.OK.value());
    }

    /**
     * 查询销售代表已邀请的客户列表.
     *
     * @return
     */
    @RequestMapping(value = "/app/appserver/crm/cust/getInvitedCust", method = RequestMethod.GET)
    @RequestCheck
    public Map<String, Object> getInvitedCust(@RequestParam String userId, @RequestParam String page, @RequestParam String pageNum, String startDate, String endDate) {
//        return HttpUtil.json2Map(HttpUtil.restGet(this.getGateUrl() + "/app/crm/cust/getInvitedCust?"
//                            + "userId=" + params.get("userId")
//                            + "&page=" + params.get("page")
//                            + "&pageNum=" + params.get("pageNum")
//                            + "&startDate=" + params.get("startDate")
//                            + "&endDate=" + params.get("endDate")));
        logger.info("请求开始==>");
        String url = EurekaServer.CRM + "/app/crm/cust/getInvitedCust?";
        if (!StringUtils.isEmpty(userId)) {
            url += "userId=" + userId;
        }
        if (!StringUtils.isEmpty(page)) {
            url += "&page=" + page;
        }
        if (!StringUtils.isEmpty(pageNum)) {
            url += "&pageNum=" + pageNum;
        }
        if (!StringUtils.isEmpty(startDate)) {
            url += "&startDate=" + startDate;
        }
        if (!StringUtils.isEmpty(endDate)) {
            url += "&endDate=" + endDate;
        }
        logger.info("请求url" + url);
        return HttpUtil.restGetMap(url);

    }

    /**
     * 新增客户邀请原因.
     *
     * @return Map
     */
    @RequestMapping(value = "/app/appserver/crm/cust/addInvitedCust", method = RequestMethod.POST)
    @RequestCheck
    public Map<String, Object> addInvitedCust(@RequestBody Map<String, Object> params) {

        if (StringUtils.isEmpty(params.get("userId"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "销售代表编号不能为空");
        }
        if (StringUtils.isEmpty(params.get("custName"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "客户姓名不能为空");
        }
        if (StringUtils.isEmpty(params.get("identifyCode")) && StringUtils.isEmpty(params.get("certNo"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "身份证号不能为空");
        }
        if (StringUtils.isEmpty(params.get("mobile"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "手机号不能为空");
        }
        if (StringUtils.isEmpty(params.get("inviteTagid"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "受邀原因ID不能为空");
        }
        Map<String, Object> result = HttpUtil.restPostMap(EurekaServer.CRM + "/app/crm/cust/addInvitedCust", params);
        logger.info(result);
        return result;
    }

    /**
     * 查询商户邀请权限.
     *
     * @param merchNo 商户编号
     * @return
     */
    @RequestMapping(value = "/app/appserver/crm/cust/getMerchTags", method = RequestMethod.GET)
    public Map<String, Object> getMerchTags(String merchNo) {
        if (StringUtils.isEmpty(merchNo)) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "商户编号不能为空");
        }
        return HttpUtil.restGetMap(EurekaServer.CRM + "/app/crm/cust/getMerchTags?merchNo=" + merchNo, HttpStatus.OK.value());
    }

    /**
     * 查询用户银行卡信息.
     *
     * @param custNo
     * @return
     */
    @RequestMapping(value = "/app/appserver/crm/cust/getBankCard", method = RequestMethod.GET)
    public Map<String, Object> getBankCard(String custNo) {
        if (StringUtils.isEmpty(custNo)) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "客户编号不能为空");
        }
        return crmService.getBankCard(custNo);
    }

    /**
     * 新增银行卡信息.
     *
     * @param params
     * @return
     */
    @RequestMapping(value = "/app/appserver/crm/cust/saveBankCard", method = RequestMethod.POST)
    public Map<String, Object> saveBankCard(@RequestBody Map<String, Object> params) {

        if (StringUtils.isEmpty(params.get("custNo"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "客户编号不能为空");
        }
        if (StringUtils.isEmpty(params.get("custName"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "姓名不能为空");
        }
        if (StringUtils.isEmpty(params.get("cardNo"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "银行卡号不能为空");
        }
        if (StringUtils.isEmpty(params.get("certNo"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "身份证号不能为空");
        }
        if (StringUtils.isEmpty(params.get("phonenumber"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "手机号码不能为空");
        }
        return HttpUtil.restPostMap(EurekaServer.CRM + "/app/crm/cust/saveBankCard", params);
    }

    /**
     * 封装CRM8接口：(GET)查询联系人列表（根据客户编号）(APP)
     *
     * @param custNo
     * @return
     */
    @RequestMapping(value = "/app/appserver/crm/cust/findCustFCiCustContactByCustNo", method = RequestMethod.GET)
    public Map<String, Object> findCustFCiCustContactByCustNo(String custNo) {
        if (StringUtils.isEmpty(custNo)) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "客户编号不能为空");
        }
        return HttpUtil.restGetMap(EurekaServer.CRM + "/app/crm/cust/findCustFCiCustContactByCustNo?custNo=" + custNo, HttpStatus.OK.value());
    }

    /**
     * 封装CRM78、(POST)修改银行卡信息（根据客户编号银行卡号）(APP)
     *
     * @param params
     * @return
     */
    @RequestMapping(value = "/app/appserver/crm/cust/updateBankCardInfo", method = RequestMethod.POST)
    public Map<String, Object> updateBankCardInfo(@RequestBody Map<String, Object> params) {

        if (StringUtils.isEmpty(params.get("custNo"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "客户编号不能为空");
        }
        if (StringUtils.isEmpty(params.get("cardNo"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "银行卡号不能为空");
        }
        if (StringUtils.isEmpty(params.get("mobile"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "手机号码不能为空");
        }
        return HttpUtil.restPostMap(EurekaServer.CRM + "/app/crm/cust/updateBankCardInfo", params);
    }

    /**
     * 封装CRM35、(PUT)修改默认还款银行卡(APP)
     *
     * @param params
     * @return
     */
    @RequestMapping(value = "/app/appserver/crm/cust/saveDefaultBankCard", method = RequestMethod.PUT)
    public Map<String, Object> saveDefaultBankCard(@RequestBody Map<String, Object> params) {
        if (StringUtils.isEmpty(params.get("custNo"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "客户编号不能为空");
        }
        if (StringUtils.isEmpty(params.get("cardNo"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "银行卡号不能为空");
        }
        Map<String, Object> result = HttpUtil.restPutMap(EurekaServer.CRM + "/app/crm/cust/saveDefaultBankCard", super.getToken(), params);

        logger.info("调用CRM接口返回结果：" + result);
        if (StringUtils.isEmpty(result)) {
            return fail("77", "修改默认还款银行卡失败");
        }
        return result;
    }

    /**
     * 封装CRM60、(GET)查询默认还款银行卡（根据客户编号）(APP)
     *
     * @param custNo
     * @return
     */
    @RequestMapping(value = "/app/appserver/crm/cust/getDefaultBankCard", method = RequestMethod.GET)
    public Map<String, Object> getDefaultBankCard(String custNo) {
        if (StringUtils.isEmpty(custNo)) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "客户编号不能为空");
        }
        return HttpUtil.restGetMap(EurekaServer.CRM + "/app/crm/cust/getDefaultBankCard?custNo=" + custNo, HttpStatus.OK.value());
    }

    /**
     * 封装CRM76、(GET)查询指定银行卡的所有信息(APP)
     *
     * @param cardNo
     * @return
     */
    @RequestMapping(value = "/app/appserver/crm/cust/getAllBankInfo", method = RequestMethod.GET)
    public Map<String, Object> getAllBankInfo(String cardNo) {
        if (StringUtils.isEmpty(cardNo)) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "银行卡号不能为空");
        }
        return HttpUtil.restGetMap(EurekaServer.CRM + "/app/crm/cust/getAllBankInfo?cardNo=" + cardNo, HttpStatus.OK.value());
    }

    /**
     * 封装CRM72、(GET)查询已认证客户的贷款品种及小类(APP)
     *
     * @param params
     * @return
     */
    @RequestMapping(value = "/app/appserver/crm/cust/getCustLoanCodeAndLel", method = RequestMethod.GET)
    public Map<String, Object> getCustLoanCodeAndLel(@RequestParam Map<String, Object> params) {
        if (StringUtils.isEmpty(params.get("custNo"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "客户编号不能为空");
        }

        StringBuffer url = new StringBuffer(EurekaServer.CRM).append("/app/crm/cust/getCustLoanCodeAndLel?")
                .append("custNo=").append(params.get("custNo"));
        String typGrp;
        if (!StringUtils.isEmpty(params.get("typGrp"))) {
            typGrp = (String) params.get("typGrp");
            url.append("&typGrp=").append(typGrp);
        }
        return HttpUtil.restGetMap(url.toString(), HttpStatus.OK.value());
    }

    /**
     * 封装CRM28、(GET)查询客户的准入资格(APP+APPSERVER)
     *
     * @param params
     * @return
     */
    @RequestMapping(value = "/app/appserver/crm/cust/getCustIsPass", method = RequestMethod.GET)
    public Map<String, Object> getCustIsPass(@RequestParam Map<String, Object> params) {
        if (StringUtils.isEmpty(params.get("custName"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "姓名不能为空");
        }
        String idNo;
        if (StringUtils.isEmpty("idNo") && StringUtils.isEmpty("certNo")) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "身份证号不能为空");
        } else {
            idNo = (String) (StringUtils.isEmpty(params.get("idNo")) ? params.get("certNo") : params.get("idNo"));
        }
        params.put("certNo", idNo);

        Map<String, Object> result = crmService.getCustIsPass(params);
        return result;
    }

    /**
     * 封装CRM31、(GET)查询未实名认证客户是否存在有效邀请原因(APP)
     *
     * @param params
     * @return
     */
    @RequestMapping(value = "/app/appserver/crm/cust/getCustISExistsInvitedCauseTag", method = RequestMethod.GET)
    public Map<String, Object> getCustISExistsInvitedCauseTag(@RequestParam Map<String, Object> params) {
        String custName = "", certNo = "", phonenumber = "", cardNo = "";
        if (params.containsKey("custName")) {
            custName = String.valueOf(params.get("custName"));
        }
        if (params.containsKey("certNo")) {
            certNo = String.valueOf(params.get("certNo"));
        }
        if (params.containsKey("phonenumber")) {
            phonenumber = String.valueOf(params.get("phonenumber"));
        }
        if (params.containsKey("cardNo")) {
            cardNo = String.valueOf(params.get("cardNo"));
        }
        return crmService.getCustISExistsInvitedCauseTag(custName, certNo, phonenumber, cardNo);
    }

    /**
     * 封装CRM55、(GET)根据客户编号查询邀请原因(APP)
     *
     * @param custNo
     * @return
     */
    @RequestMapping(value = "/app/appserver/crm/cust/getInvitedCustByCustNo", method = RequestMethod.GET)
    public Map<String, Object> getInvitedCustByCustNo(String custNo) {
        if (StringUtils.isEmpty("custNo")) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "客户编号不能为空");
        }
        return HttpUtil.restGetMap(EurekaServer.CRM + "/app/crm/cust/getInvitedCustByCustNo?custNo=" + custNo, HttpStatus.OK.value());
    }

    /**
     * 封装CRM80、(POST)额度测算 (APP)
     *
     * @param params
     * @return
     */
    @RequestMapping(value = "/app/appserver/crm/cust/quotaCalculation", method = RequestMethod.POST)
    public Map<String, Object> quotaCalculation(@RequestBody Map<String, Object> params) {
        if (StringUtils.isEmpty(params.get("custNo"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "客户编号不能为空");
        }
        return HttpUtil.restPostMap(EurekaServer.CRM + "/app/crm/cust/quotaCalculation", params);
    }

    /**
     * 封装CRM17、(GET)查询客户实名认证信息（根据userid）(APP_person)
     *
     * @param userId
     * @return
     */
    @RequestMapping(value = "/app/appserver/crm/cust/queryPerCustInfo", method = RequestMethod.GET)
    public Map<String, Object> queryPerCustInfo(String userId) {
        if (StringUtils.isEmpty("userId")) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "用户登录用户名不能为空");
        }
        return HttpUtil.restGetMap(EurekaServer.CRM + "/app/crm/cust/queryPerCustInfo?userId=" + userId, HttpStatus.OK.value());
    }

    /**
     * 封装CRM33、(GET)查询门店详情(APP)
     *
     * @param storeNo
     * @return
     */
    @RequestMapping(value = "/app/appserver/crm/cust/getStore", method = RequestMethod.GET)
    public Map<String, Object> getStore(String storeNo) {

        StringBuffer url = new StringBuffer(EurekaServer.CRM).append("/app/crm/cust/getStore");
        if (!StringUtils.isEmpty(storeNo)) {
            url.append("?storeNo=").append(storeNo);
        } else {
            url.append("?storeNo=");
        }
        String json = HttpUtil.restGet(url.toString());
        return HttpUtil.json2DeepMap(json);
    }

    /**
     * 封装CRM34、(GET)查询门店列表详情（按城市代码）(APP)
     *
     * @param params
     * @return
     */
    @RequestMapping(value = "/app/appserver/crm/cust/getStoreList", method = RequestMethod.GET)
    public Map<String, Object> getStoreList(@RequestParam Map<String, Object> params) {
        if (StringUtils.isEmpty(params.get("city"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "城市代码不能为空");
        }
        if (StringUtils.isEmpty(params.get("pageNum"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "页码不能为空");
        }
        if (StringUtils.isEmpty(params.get("rownum"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "每页条数不能为空");
        }

        StringBuffer url = new StringBuffer(EurekaServer.CRM).append("/app/crm/cust/getStoreList")
                .append("?city=").append(params.get("city"))
                .append("&pageNum=").append(params.get("pageNum"))
                .append("&rownum=").append(params.get("rownum"));

        //CRM非必输字段，空则传空字符串
        if (!StringUtils.isEmpty(params.get("storeKind"))) {
            url.append("&storeKind=").append(params.get("storeKind"));
        } else {
            url.append("&storeKind=");
        }
        return HttpUtil.restGetMap(url.toString(), HttpStatus.OK.value());
    }

    /**
     * 封装CRM51、(GET)查询商户下销售代表对应门店列表(个人版APP)
     *
     * @param params
     * @return
     */
    @RequestMapping(value = "/app/appserver/crm/findMerchStoreForCustomer", method = RequestMethod.GET)
    public Map<String, Object> findMerchStoreForCustomer(@RequestParam Map<String, Object> params) {
        String merchNo = (String) params.get("merchNo");
        if (StringUtils.isEmpty(merchNo)) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "merchNo不能为空");
        }
        String salerId = (String) params.get("salerId");
        StringBuffer url = new StringBuffer(EurekaServer.CRM).append("/app/crm/findMerchStoreForCustomer?")
                .append("merchNo=").append(merchNo);
        if (!StringUtils.isEmpty(salerId)) {
            url.append("&salerId=").append(salerId);
        }
        return HttpUtil.restGetMap(url.toString(), HttpStatus.OK.value());
    }

    /**
     * 封装CRM27、(GET)查询客户已登记银行卡信息（包含支行信息）(APP+APPSERVER)
     *
     * @param cardNo
     * @return
     */
    @RequestMapping(value = "/app/appserver/crm/cust/getCustBankCardByCardNo", method = RequestMethod.GET)
    public Map<String, Object> getCustBankCardByCardNo(String cardNo) {
        if (StringUtils.isEmpty(cardNo)) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "银行卡号不能为空");
        }

        return crmService.getCustBankCardByCardNo(cardNo);
    }

    /**
     * 封装CRM43、(移除)(GET)查询已认证客户的所有有效标签（信贷）
     *
     * @param params
     * @return
     */
    @RequestMapping(value = "/app/appserver/crm/cust/getCustTag", method = RequestMethod.GET)
    public Map<String, Object> getCustTag(@RequestParam Map<String, Object> params) {
        if (StringUtils.isEmpty(params.get("custName"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "姓名不能为空");
        }
        if (StringUtils.isEmpty(params.get("idTyp"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "证件类型不能为空");
        }
        if (StringUtils.isEmpty(params.get("idNo"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "证件号码不能为空");
        }
        return HttpUtil.restGetMap(EurekaServer.CRM + "/app/crm/cust/getCustTag?"
                + "custName=" + params.get("custName")
                + "&idTyp=" + params.get("idTyp")
                + "&idNo=" + params.get("idNo"), HttpStatus.OK.value());
    }

    /**
     * 整合crm85接口  修改保存客户所有扩展信息
     *
     * @param requestMap
     * @return
     */
    @RequestMapping(value = "/app/appserver/crm/cust/saveAllCustExtInfo", method = RequestMethod.POST)
    public Map<String, Object> saveAllCustExtInfo(@RequestBody Map requestMap) {
        logger.info("crm_85 requestParm:" + requestMap);
        return crmService.saveAllCustExtInfo(requestMap);
    }

    /**
     * 整合crm1接口  保存/修改 单位(个人、房产)信息
     *
     * @param requestMap
     * @return
     */
    @RequestMapping(value = "/app/appserver/crm/cust/saveCustExtInfo", method = RequestMethod.POST)
    public Map<String, Object> saveCustExtInfo(@RequestBody Map requestMap) {
        logger.info("crm_1 requestParm:" + requestMap);
        return crmService.saveCustExtInfo(requestMap);
    }

    /**
     * 整合crm6接口  新增/修改 联系人
     *
     * @param requestMap
     * @return
     */
    @RequestMapping(value = "/app/appserver/crm/saveCustFCiCustContact", method = RequestMethod.POST)
    public Map<String, Object> saveCustFCiCustContact(@RequestBody Map requestMap) {
        logger.info("crm_6 requestParm:" + requestMap);
        return crmService.saveCustFCiCustContact(requestMap);
    }

    /**
     * 整合crm4接口  查询个人(单位、房产)信息
     *
     * @param custNo
     * @param pageName
     * @return
     */
    @RequestMapping(value = "/app/appserver/crm/cust/getCustExtInfo", method = RequestMethod.GET)
    public Map<String, Object> getCrm4CustExtInfo(String custNo, String pageName) {
        if (StringUtils.isEmpty(custNo)) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "客户编号不能为空");
        }
//        if (StringUtils.isEmpty(pageName)){
//            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "请求页面不能为空");
//        }
        logger.info("crm_4 requestParm:custNo=" + custNo + ";pageName=" + pageName);
        return crmService.getCrm4CustExtInfo(custNo, pageName);
    }

    /**
     * 整合crm64接口  查询指定银行卡的所有信息
     *
     * @param cardNo
     * @return
     */
    @RequestMapping(value = "/app/appserver/crm/cust/getBankInfo", method = RequestMethod.GET)
    public Map<String, Object> getBankInfo(String cardNo) {
        if (StringUtils.isEmpty(cardNo)) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "银行卡号不能为空");
        }
        logger.info("crm_64 requestParm:cardNo=" + cardNo);
        return crmService.getBankInfo(cardNo);
    }

    /**
     * 整合crm12接口  查询所有支持的银行列表
     *
     * @param
     * @return
     */
    @RequestMapping(value = "/app/appserver/crm/cust/getBankList", method = RequestMethod.GET)
    public Map<String, Object> getBankList() {
        return crmService.getBankList();
    }

    /**
     * 整合crm66接口  验证并新增实名认证信息
     *
     * @param requestMap
     * @return
     */
    @RequestMapping(value = "/app/appserver/crm/cust/fCiCustRealThreeInfo", method = RequestMethod.POST)
    public Map<String, Object> fCiCustRealThreeInfo(@RequestBody Map requestMap) {
        logger.info("crm_66 requestParm:" + requestMap);
        if (StringUtils.isEmpty(requestMap.get("custName"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "客户姓名不能为空");
        }
        if (StringUtils.isEmpty(requestMap.get("certNo"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "身份证号不能为空");
        }
        if (StringUtils.isEmpty(requestMap.get("cardNo"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "银行卡号不能为空");
        }
        if (StringUtils.isEmpty(requestMap.get("mobile"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "手机号不能为空");
        }
        if (StringUtils.isEmpty(requestMap.get("dataFrom"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "数据来源不能为空");
        }
        String channel = super.getChannel();
        String channelNo = super.getChannelNO();
        String dataFrom = "";
        if (StringUtils.isEmpty(channelNo)) {
            if (Objects.equals(channel, "13")) {
                //channelNo = "05";//chanelNo为空，并且chanel为13或者14的
                channelNo = "app_person";
            } else if (Objects.equals(channel, "14")) {
                channelNo = "app_merch";
            } else if (Objects.equals(channel, "16")) {
                //   channelNo = "31";//chanelNo为空并且chanel为16的
                channelNo = "app_xcd";
            } else if (StringUtils.isEmpty(channel)) {
                channelNo = "05";//chanelNo为空并且chanel为空的
            } else {
                channelNo = "A99";//其他的表示异常调用
            }
        }
        return crmService.fCiCustRealThreeInfo(requestMap, channelNo);
    }

    /**
     * 整合crm26接口  查询实名认证信息
     *
     * @param custNo
     * @return
     */
    @RequestMapping(value = "/app/appserver/crm/cust/queryCustRealInfoByCustNo", method = RequestMethod.GET)
    public Map<String, Object> queryCustRealInfoByCustNo(String custNo) {
        logger.info("crm_26 requestParm:custNo=" + custNo);
        if (StringUtils.isEmpty(custNo)) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "客户编号不能为空");
        }
        return crmService.queryCustRealInfoByCustNo(custNo);
    }

    /**
     * 整合crm13接口  查询实名认证客户信息
     *
     * @param custName
     * @param certNo
     * @return
     */

    @RequestMapping(value = "/app/appserver/crm/cust/queryMerchCustInfo", method = RequestMethod.GET)
    public Map<String, Object> queryMerchCustInfo(String custName, String certNo) {
        if (StringUtils.isEmpty(custName)) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "客户名称不能为空");
        }
        if (StringUtils.isEmpty(certNo)) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "证件号码不能为空");
        }
        logger.info("crm_13 requestParm:custName=" + custName + ";certNo=" + certNo);
        return crmService.queryMerchCustInfo(custName, certNo);
    }

    /**
     * 整合crm50接口  查询商户对应门店列表
     *
     * @param merchNo
     * @param userId
     * @return
     */

    @RequestMapping(value = "/app/appserver/crm/findMerchStore", method = RequestMethod.GET)
    @RequestCheck
    public Map<String, Object> findMerchStore(String merchNo, String userId) {
        logger.info("crm_50 requestParm:merchNo=" + merchNo + ";userId=" + userId);
        if (StringUtils.isEmpty(merchNo)) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "商户编号不能为空");
        }
        if (StringUtils.isEmpty(userId)) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "用户编号不能为空");
        }
        return crmService.findMerchStore(merchNo, userId);
    }

    /**
     * 整合crm24接口  查询商户列表
     *
     * @param userId
     * @return
     */
    @RequestMapping(value = "/app/appserver/crm/cust/getMerchs", method = RequestMethod.GET)
    @RequestCheck
    public Map<String, Object> getMerchs(String userId) {
        logger.info("crm_24 requestParm:userId=" + userId);
        if (StringUtils.isEmpty(userId)) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "用户编号不能为空");
        }
        return crmService.getMerchs(userId);
    }

    /***
     * 整合crm90接口  查询客户当前集团总积分
     *
     * @param custNo
     * @return
     */
    @RequestMapping(value = "/app/appserver/crm/cust/queryPointByCustNo", method = RequestMethod.GET)
    public Map<String, Object> queryPointByCustNo(String custNo) {
        logger.info("crm_90 requestParm:custNo=" + custNo);
        if (StringUtils.isEmpty(custNo)) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "客户编号不能为空");
        }
        return crmService.queryPointByCustNo(custNo);
    }

    /**
     * 根据appmanage中对appBannerTagSetting的配置，向CRM87接口获取贷款种类信息
     *
     * @return
     */
    @RequestMapping(value = "/app/appserver/getLoanCdeBySetting", method = RequestMethod.GET)
    public Map<String, Object> getLoanCdeBySetting() {
        String settingKey = "appBannerTagSetting";
        String settingValue = appManageService.getDictDetailByDictCde(settingKey);
        return crmService.getLoanCdeByTagId(settingValue);
    }

    /**
     * 社会化用户判断是否可以实名
     * 接口：
     * app/appserver/crm/cust/getCustISExistsInvitedCauseTag
     * 查询邀请原因
     * app/crm/cust/getCustTag
     * 无邀请原因用户查询有效标签
     * app/appmanage/citybean/checkCitySmrz
     * 无标签用户 查询城市是否允许
     * 传参：姓名，身份证号，手机号，身份证类型，省市编码（没有则传空）
     * 返回参数：是否允许实名认证。（bool）
     */
    @RequestMapping(value = "/app/appserver/getIfShhSmrz", method = RequestMethod.GET)
    public Map<String, Object> getIfShhSmrz(String custName, String idNo, String mobile, String idTyp, String cardNo, String provinceCode, String cityCode) {
        if (StringUtils.isEmpty(custName)) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "姓名不能为空");
        }
        if (StringUtils.isEmpty(idNo)) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "身份证号不能为空");
        }
        if (StringUtils.isEmpty(mobile)) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "手机号不能为空");
        }
        return crmService.getIfShhSmrz(custName, idNo, mobile, idTyp, cardNo, provinceCode, cityCode);
    }

    @RequestMapping(value = "/app/appserver/pub/crm/findAreaCodes", method = RequestMethod.GET)
    public Map<String, Object> findAreaCodes(@RequestParam String areaCode) {
        return crmService.findAreaCodes(areaCode);
    }

    /**
     * 根据门店编号查询销售代表id（CRM93）
     *
     * @param storeNo
     * @return
     */
    @RequestMapping(value = "/app/appserver/pub/crm/findUserIdByStoreNo", method = RequestMethod.GET)
    public Map<String, Object> findUserIdByStoreNo(@RequestParam String storeNo) {
        if (StringUtils.isEmpty(storeNo)) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "门店编号不能为空");
        }
        return crmService.findUserIdByStoreNo(storeNo);
    }

    /**
     * 查询行政编码级联信息（CRM94）
     *
     * @param areaCode
     * @param flag
     * @return
     */
    @RequestMapping(value = "/app/appserver/pub/crm/findDmAreaInfo", method = RequestMethod.GET)
    public Map<String, Object> findDmAreaInfo(String areaCode, @RequestParam String flag) {

        if (StringUtils.isEmpty(flag)) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "查询标志不能为空");
        }
        return crmService.findDmAreaInfo(areaCode, flag);
    }

    /**
     * 扫码获取信息接口
     *
     * @param userId   用户编号
     * @param scanCode 扫码编码
     * @return
     */
    @RequestMapping(value = "/app/appserver/getMsgByScan", method = RequestMethod.GET)
    @RequestCheck
    public Map<String, Object> getMsgByScan(String userId, String scanCode) {
        logger.info("收到扫码获取信息请求：userId=" + userId + ",scanCode=" + scanCode);
        if (StringUtils.isEmpty(userId)) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "用户编码不能为空");
        }
        if (StringUtils.isEmpty(scanCode)) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "扫码编码不能为空");
        }
        return crmService.getMsgByScan(userId, scanCode);
    }

    @RequestMapping(value = "/app/appserver/getCommonApplType")
    @RequestCheck
    public Map<String, Object> getCommonApplType(String userId, String goodsCode) {
        logger.info("getCommonApplType：userId = " + userId + ", goodsCode = " + goodsCode);
        if (StringUtils.isEmpty(userId)) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "用户编码不能为空");
        }
        if (StringUtils.isEmpty(goodsCode)) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "商品编码不能为空");
        }
        return crmService.getCommonApplType(userId, goodsCode);
    }

    /**
     * 根据合作机构查询所属商户(CRM83)
     *
     * @param deptId
     * @param page
     * @param pageNum
     * @return
     */
    @RequestMapping(value = "/app/appserver/crm/cust/coopMerchs")
    @RequestCheck
    public Map<String, Object> coopMerchs(@RequestParam String deptId, @RequestParam Integer page, @RequestParam Integer pageNum) {

        logger.info("coopMerchs：deptId=" + deptId + ",page=" + page + ",pageNum=" + pageNum);
        if (StringUtils.isEmpty(deptId)) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "合作机构编号不能为空");
        }
        if (StringUtils.isEmpty(page)) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "当前页不能为空");
        }
        if (StringUtils.isEmpty(pageNum)) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "每页显示的数目不能为空");
        }
        return crmService.coopMerchs(deptId, page, pageNum);
    }
}
