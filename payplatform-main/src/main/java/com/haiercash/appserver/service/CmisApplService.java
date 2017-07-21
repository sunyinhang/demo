package com.haiercash.appserver.service;

import com.haiercash.appserver.apporder.DataVerificationUtil;
import com.haiercash.appserver.util.ConstUtil;
import com.haiercash.appserver.util.RandomCodeUtil;
import com.haiercash.appserver.util.ReflactUtils;
import com.haiercash.appserver.web.CmisController;
import com.haiercash.common.apporder.utils.AcqTradeCode;
import com.haiercash.common.config.EurekaServer;
import com.haiercash.common.data.AppCmisInfo;
import com.haiercash.common.data.AppCmisInfoRepository;
import com.haiercash.common.data.AppOrder;
import com.haiercash.common.data.AppOrdernoTypgrpRelation;
import com.haiercash.common.data.AppOrdernoTypgrpRelationRepository;
import com.haiercash.common.data.CityBean;
import com.haiercash.common.data.CityRepository;
import com.haiercash.common.data.CommonRepaymentPerson;
import com.haiercash.common.data.MsgRequest;
import com.haiercash.common.data.MsgRequestRepository;
import com.haiercash.common.service.BaseService;
import com.haiercash.commons.cmis.CmisTradeCode;
import com.haiercash.commons.cmis.CmisUtil;
import com.haiercash.commons.util.AcqUtil;
import com.haiercash.commons.util.EncryptUtil;
import com.haiercash.commons.util.HttpUtil;
import com.haiercash.commons.util.IdcardUtils;
import com.haiercash.commons.util.RestUtil;
import com.haiercash.commons.util.ResultHead;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * @author 尹君
 * @date 2016/7/5
 * @description:提供信贷系统进件相关的接口
 **/
@Service
public class CmisApplService extends BaseService {

    public CmisApplService() {
        super(MODULE_NO);
    }

    private Log logger = LogFactory.getLog(this.getClass());

    private static String MODULE_NO = "11";

    @Value("${common.address.outplatform}")
    private String outplatform;
    @Autowired
    private MsgRequestRepository msgRequestRepository;
    @Autowired
    private AppCmisInfoRepository appCmisInfoRepository;
    @Autowired
    private AppOrderService appOrderService;
    @Autowired
    private CityRepository cityRepository;
    @Autowired
    private AppManageService appManageService;
    @Autowired
    private AcquirerService acquirerService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private AppOrdernoTypgrpRelationRepository appOrdernoTypgrpRelationRepository;

    /**
     * <p>
     * 业务提交端口 描述：3.24 贷款取消/提交 贷款支用撤销 贷款支用接口提交
     * </p>
     *
     * @param applSeq 申请流水号
     * @return
     * @date 2016年4月11日
     * @author 尹君
     */
    public Map<String, Object> commitBussiness(String applSeq, AppOrder order) {

        // 先更新订单信息
        AppOrdernoTypgrpRelation relation = appOrdernoTypgrpRelationRepository.findOne(order.getOrderNo());
        if (relation == null) {
            return fail("23", "订单信息不存在");
        }
        if ("02".equals(relation.getTypGrp())) {
            Map<String, Object> resultMap = acquirerService
                    .cashLoan(order, appOrdernoTypgrpRelationRepository.findOne(order.getOrderNo()));
            if (!CmisUtil.getIsSucceed(resultMap)) {
                return (Map<String, Object>) resultMap.get("response");
            }
            logger.debug("收单系统接口" + AcqTradeCode.COMMIT_APPL + "开始");
            Map<String, Object> result = acquirerService.commitAppl(order, "1");
            logger.debug("收单系统接口" + AcqTradeCode.COMMIT_APPL + "结束");
            return result;
        } else {
            if (!"2".equals(order.getStatus())) {
                appOrderService.cleanGoodsInfo(order);
                // 防止个人版保存商户版提交时修改系统标识信息
                order.setSource(null);
                Map<String, Object> resultMap = orderService.saveOrUpdateAppOrder(order, null);
                if (!HttpUtil.isSuccess(resultMap)) {
                    return resultMap;
                }
            }
            logger.debug("订单系统接口提交订单开始");
            Map<String, Object> result = orderService.submitOrder(order.getOrderNo(), "0");
            return result;
        }
    }

    /**
     * 渠道进件接口
     *
     * @param apporder
     * @param orderNo
     * @param autoFlag
     * @return
     */
    public Map<String, Object> getQdjj(AppOrder apporder, String orderNo, String autoFlag) {
        //判断流水号是否存在，如果不存在，则正常执行渠道进件，若存在，则增加判断该流水号的单子是否是00和22状态，若不是，则直接返回成功
        String thisApplseq = apporder.getApplSeq();
        if (!StringUtils.isEmpty(thisApplseq)) {
            String url = EurekaServer.CMISPROXY + "/api/appl/getCustInfo?applSeq=" + thisApplseq;
            String s = HttpUtil.restGet(url, super.getToken());
            logger.info("查询订单客户信息请求url" + url);
            logger.info("返回：" + s);
            Map<String, Object> map;
            if (StringUtils.isEmpty(s)) {
                logger.debug("查询失败了，不做处理，无法判断单子状态，继续往下提，由信贷判断是否允许操作！");
                // return fail("01","查询失败");
            } else {
                map = HttpUtil.json2Map(s);
                String outSts = String.valueOf(map.get("OUT_STS"));
                if (!("00".equals(outSts) || "22".equals(outSts) || "SS".equals(outSts))) {
                    logger.info("订单状态不是22或者00和SS状态的不走渠道进件，直接返回成功！");
                    // HashMap<String,Object> responseMap=new HashMap<String,Object>();
                    HashMap<String, Object> parmMap = new HashMap<String, Object>();
                    parmMap.put("applCde", apporder.getApplCde());
                    parmMap.put("appl_seq", apporder.getApplSeq());
                    parmMap.put("wfSts", "");
                    parmMap.put("wfi_out_advice", "");
                    parmMap.put("cont_no", "");
                    parmMap.put("loan_no", "");
                    //responseMap.put("response",success(parmMap));
                    Map<String, Object> headMap = new HashMap<String, Object>();
                    headMap.put("retFlag", "00000");
                    headMap.put("retMsg", "处理成功");
                    HashMap<String, Object> bodyMap = new HashMap<String, Object>();
                    bodyMap.put("applSeq", thisApplseq);
                    bodyMap.put("applCde", apporder.getApplCde());
                    bodyMap.put("wfSts", "");
                    bodyMap.put("wfi_out_advice", "");
                    bodyMap.put("cont_no", "");
                    bodyMap.put("loan_no", "");
                    Map<String, Object> resultMap = new HashMap<String, Object>();
                    resultMap.put("head", headMap);
                    resultMap.put("body", bodyMap);
                    Map<String, Object> responseMap = new HashMap<String, Object>();
                    responseMap.put("response", resultMap);
                    return responseMap;
                }
            }

        }

        apporder.setOrderNo(orderNo);

        Map<String, Object> result;
        // 订单类型.
        AppOrdernoTypgrpRelation relation = appOrdernoTypgrpRelationRepository.findByApplSeq(apporder.getApplSeq());
        if (relation == null) {
            logger.info("relation关联关系查询失败, applSeq:" + apporder.getApplSeq() + ", orderNo:" + apporder.getOrderNo());
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "要更新的订单不存在！");
        }

        logger.info("============保存订单请求开始============");
        // 现金贷
        if ("02".equals(relation.getTypGrp())) {
            result = acquirerService.cashLoan(apporder, relation);
            if (result == null) {
                result = fail(RestUtil.ERROR_INTERNAL_CODE, "收单系统通信失败");
            } else {
                result = (Map<String, Object>) result.get("response");
            }
        } else {
            appOrderService.cleanGoodsInfo(apporder);
            // 商品贷
            //Map<String, Object> orderMap = orderService.order2OrderMap(apporder, null);
            //result = HttpUtil.restPostMap(EurekaServer.ORDER + "/api/order/save", orderMap);
            result = orderService.saveOrUpdateAppOrder(apporder, null);
        }

        logger.info("保存订单请求返回结果:\n" + new JSONObject(result));

        logger.info("============保存订单请求结束============");
        return result;
    }

    /**
     * 封装共同还款人
     *
     * @param custNo
     * @return
     */

    public Map<String, Object> getCommonPayPersonMap(String custNo, String source, String typGrp,
                                                     CommonRepaymentPerson person, String version) {
        HashMap<String, Object> apptmap = new HashMap<String, Object>();
        // attpmap封装从crm中调取的个人信息的数据
        String url = EurekaServer.CRM + "/app/crm/cust/getCustExtInfo?pageName=1&custNo=" + custNo;
        logger.debug("CRM getCustExtInfo...");
        String json = HttpUtil.restGet(url, getToken());
        logger.debug("CRM getCustExtInfo DONE");
        logger.debug("CRM个人扩展信息接口返回json==" + json);
        if (StringUtils.isEmpty(json)) {
            logger.error("客户扩展信息接口返回异常!——》 CRM 1.4");
            return fail("51", RestUtil.ERROR_INTERNAL_MSG);
        }
        Map<String, Object> custExtInfoMap = HttpUtil.json2Map(json);
        logger.debug("custExtInfoMap==" + custExtInfoMap);
        Map<String, Object> custExtInfoBodyMap = new HashMap<>();
        if (!StringUtils.isEmpty(custExtInfoMap.get("body"))) {
            // 客户扩展信息查询
            custExtInfoBodyMap = HttpUtil.json2Map(custExtInfoMap.get("body").toString());
            logger.info("custExtInfoBodyMap==" + custExtInfoBodyMap);
        }
        this.dealAddress(custExtInfoBodyMap);//对地址进行处理
        // 客户基本信息查询
        //从版本（1）开始，共同还款人四要素信息全部从订单读取
        if (!StringUtils.isEmpty(version) && Integer.parseInt(version) >= 1) {
            apptmap.put("appt_id_no", person.getIdNo());
            apptmap.put("appt_cust_name", person.getName());
            apptmap.put("indiv_mobile", person.getMobile());// 联系电话
            apptmap.put("appt_id_typ_oth", person.getCardNo());// 银行卡号
        } else {//兼容旧版本
            String smrzUrl = EurekaServer.CRM + "/app/crm/cust/queryCustRealInfoByCustNo?custNo=" + custNo;
            String smrzJson = HttpUtil.restGet(smrzUrl, getToken());
            logger.info("CRM 1.26 smrzResult==" + smrzJson);
            if (StringUtils.isEmpty(smrzJson)) {
                logger.error("实名认证信息查询失败！——》CRM 1.26");
                return fail("54", RestUtil.ERROR_INTERNAL_MSG);
            }
            Map<String, Object> smrzMap = HttpUtil.json2Map(smrzJson);
            if (!StringUtils.isEmpty(smrzMap.get("body"))) {
                JSONObject smrz = (JSONObject) smrzMap.get("body");
                apptmap.put("appt_id_no", smrz.get("certNo"));
                apptmap.put("appt_cust_name", smrz.get("custName"));
                apptmap.put("indiv_mobile", smrz.get("mobile"));// 联系电话
            }
        }
        String idNo = String.valueOf(apptmap.get("appt_id_no"));
        if (!StringUtils.isEmpty(idNo)) {
            apptmap.put("appt_indiv_sex", IdcardUtils.getGenderByIdCard(idNo));
            apptmap.put("appt_start_date", IdcardUtils.getYearByIdCard(idNo) + "-"
                    + IdcardUtils.getMonthByIdCard(idNo) + "-" + IdcardUtils.getDateByIdCard(idNo));
        }

        // 获取联系人
        String lxrUrl = EurekaServer.CRM + "/app/crm/cust/findCustFCiCustContactByCustNo?custNo=" + custNo;
        logger.debug("CRM findCustFCiCustContactByCustNo...");
        String lxrJson = HttpUtil.restGet(lxrUrl, getToken());
        logger.debug("CRM findCustFCiCustContactByCustNo DONE");
        if (StringUtils.isEmpty(lxrJson)) {
            logger.error("联系人列表查询失败!——》CRM 1.8");
            return fail("52", RestUtil.ERROR_INTERNAL_MSG);
        }

        Map<String, Object> lxrMap = HttpUtil.json2Map(lxrJson);
        List<Map<String, Object>> lxrlist = new ArrayList<Map<String, Object>>();
        if (!StringUtils.isEmpty(lxrMap.get("body"))) {
            lxrlist = (ArrayList) lxrMap.get("body");

        }
        //////////////////
        apptmap.put("appt_typ", "02");// 申请人类型 01 - 共同申请人
        apptmap.put("appt_relation", person.getRelation());// 与主申请人关系
        apptmap.put("appt_id_typ", "20");
        if (!apptmap.containsKey("appt_id_typ_oth")) {//防止前面写入的银行卡号丢失
            apptmap.put("appt_id_typ_oth", "");
        }

        apptmap.put("indiv_marital", person.getMaritalStatus());// 婚姻状况直接调共同还款人录入的信息
        apptmap.put("indiv_edu",
                StringUtils.isEmpty(custExtInfoBodyMap.get("education")) ? "10" : custExtInfoBodyMap.get("education"));
        apptmap.put("appt_reg_province", custExtInfoBodyMap.get("regProvince"));
        apptmap.put("appt_reg_city", custExtInfoBodyMap.get("regCity"));
        if ("16".equals(source) && StringUtils.isEmpty(custExtInfoBodyMap.get("regProvince"))) {
            apptmap.put("appt_reg_province", custExtInfoBodyMap.get("liveProvince"));
            apptmap.put("appt_reg_city", custExtInfoBodyMap.get("liveCity"));
        }

        apptmap.put("live_info", custExtInfoBodyMap.get("liveInfo"));
        /**live_info字段若为星巢贷，则统一传99（其他）**/
        if (Objects.equals("16", source)) {
            apptmap.put("live_info", "99");
        }
        apptmap.put("live_province", custExtInfoBodyMap.get("liveProvince"));
        apptmap.put("live_city", custExtInfoBodyMap.get("liveCity"));
        apptmap.put("live_area", custExtInfoBodyMap.get("liveArea"));
        apptmap.put("live_addr", custExtInfoBodyMap.get("liveAddr"));
        apptmap.put("live_zip", "");// custExtInfoBodyMap.get("liveZip")
        apptmap.put("live_mj", custExtInfoBodyMap.get("liveSize"));
        apptmap.put("ppty_live", StringUtils.isEmpty(custExtInfoBodyMap.get("pptyLiveInd")) ?
                "Y" :
                custExtInfoBodyMap.get("pptyLiveInd"));// 自有房产地址
        apptmap.put("ppty_province", custExtInfoBodyMap.get("liveProvince"));// pptyProvince
        apptmap.put("ppty_city", custExtInfoBodyMap.get("liveCity"));// pptyCity
        apptmap.put("ppty_area", custExtInfoBodyMap.get("liveArea"));// pptyArea
        apptmap.put("ppty_addr", custExtInfoBodyMap.get("liveAddr"));// pptyAddr
        apptmap.put("ppty_zip", "");
        apptmap.put("ppty_mj", custExtInfoBodyMap.get("liveSize"));// pptySize
        apptmap.put("indiv_fmly_zone", custExtInfoBodyMap.get("fmlyZone"));
        apptmap.put("indiv_fmly_tel", custExtInfoBodyMap.get("fmlyTel"));
        apptmap.put("local_resid", custExtInfoBodyMap.get("localResid"));
        apptmap.put("indiv_email", custExtInfoBodyMap.get("email"));
        if (StringUtils.isEmpty(custExtInfoBodyMap.get("mthInc"))) {
            apptmap.put("annual_earn", "0");
        } else {
            apptmap.put("annual_earn", person.getMthInc().multiply(new BigDecimal(12)));
        }
        apptmap.put("household_own_rel", "");
        apptmap.put("max_crdt_card", custExtInfoBodyMap.get("maxAmount"));
        apptmap.put("iss_bank", custExtInfoBodyMap.get("creditCount"));
        apptmap.put("indiv_mth_inc", person.getMthInc());// 月收入取共同还款人录入的
        apptmap.put("position_opt", StringUtils.isEmpty(custExtInfoBodyMap.get("positionType")) ? "10"
                : custExtInfoBodyMap.get("positionType"));//工作性质position_opt固定传"10"，
        // 个人版现金贷，以下赋默认值
        if ("2".equals(source) && "02".equals(typGrp)) {
            apptmap.put("live_year",
                    StringUtils.isEmpty(custExtInfoBodyMap.get("liveYear")) ? "0" : custExtInfoBodyMap.get("liveYear"));
            apptmap.put("indiv_dep_no", StringUtils.isEmpty(custExtInfoBodyMap.get("providerNum")) ? "0"
                    : custExtInfoBodyMap.get("providerNum"));
            apptmap.put("mail_opt", "");
            apptmap.put("mail_province", "");
            apptmap.put("mail_city", "");
            apptmap.put("mail_area", "");
            apptmap.put("mail_addr", "");
        } else {
            apptmap.put("live_year",
                    StringUtils.isEmpty(custExtInfoBodyMap.get("liveYear")) ? "0" : custExtInfoBodyMap.get("liveYear"));
            apptmap.put("indiv_dep_no", StringUtils.isEmpty(custExtInfoBodyMap.get("providerNum")) ? "0"
                    : custExtInfoBodyMap.get("providerNum"));
            apptmap.put("mail_opt", custExtInfoBodyMap.get("postQtInd"));
            apptmap.put("mail_province", custExtInfoBodyMap.get("postProvince"));
            apptmap.put("mail_city", custExtInfoBodyMap.get("postCity"));
            apptmap.put("mail_area", custExtInfoBodyMap.get("postArea"));
            apptmap.put("mail_addr", custExtInfoBodyMap.get("postAddr"));
        }
        // 学习方式
        apptmap.put("study_mth", custExtInfoBodyMap.get("studyType"));

        apptmap.put("studying_deg", custExtInfoBodyMap.get("studyDegree"));
        apptmap.put("school_name", custExtInfoBodyMap.get("schoolName"));
        apptmap.put("study_major", custExtInfoBodyMap.get("studyMajor"));
        apptmap.put("school_kind", custExtInfoBodyMap.get("schoolKind"));
        apptmap.put("school_leng", custExtInfoBodyMap.get("schoolLeng"));
        apptmap.put("geade", custExtInfoBodyMap.get("schoolGrade"));
        apptmap.put("emp_reg_name", custExtInfoBodyMap.get("officeName"));//
        apptmap.put("emp_reg_dt", "");
        apptmap.put("manage_addr", "");
        apptmap.put("manage_province", "");
        apptmap.put("manage_city", "");
        apptmap.put("manage_area", "");
        apptmap.put("emp_reg_rel_tel", person.getOfficeTel());// 共同还款人中录入的办公室电话
        apptmap.put("emp_reg_add_zip", "");
        apptmap.put("emp_reg_num", "");
        apptmap.put("inc_cde", "");
        apptmap.put("indiv_opt", "");
        apptmap.put("share_hold_pct", "");
        apptmap.put("manage_typ", "04");
        apptmap.put("manage_main_biz", "");
        apptmap.put("mth_turnover", "");
        apptmap.put("manage_no", "");
        apptmap.put("pur_sale_cont_no", "");
        apptmap.put("indiv_emp_name", person.getOfficeName());// 工作单位直接取录入的共同还款人的
        apptmap.put("indiv_branch", StringUtils.isEmpty(custExtInfoBodyMap.get("officeDept")) ?
                "默认" :
                custExtInfoBodyMap.get("officeDept"));
        apptmap.put("indiv_emp_typ",
                StringUtils.isEmpty(custExtInfoBodyMap.get("officeTyp")) ? "Z" : custExtInfoBodyMap.get("officeTyp"));
        apptmap.put("indiv_emp_yrs", StringUtils.isEmpty(custExtInfoBodyMap.get("serviceYears")) ?
                0 :
                custExtInfoBodyMap.get("serviceYears"));// custExtInfoBodyMap.get("serviceYears")
        apptmap.put("indiv_emp_province", custExtInfoBodyMap.get("officeProvince"));
        apptmap.put("indiv_emp_city", custExtInfoBodyMap.get("officeCity"));
        apptmap.put("indiv_emp_area", custExtInfoBodyMap.get("officeArea"));
        apptmap.put("empaddr", custExtInfoBodyMap.get("officeAddr"));
        apptmap.put("indiv_emp_zip", "266000");

        //        if (StringUtils.isEmpty(custExtInfoBodyMap.get("officeTel"))) {
        //            apptmap.put("indiv_emp_zone", "0532");
        //            apptmap.put("indiv_emp_tel", "58869762");
        //        } else {
        String tel = String.valueOf(custExtInfoBodyMap.get("officeTel"));
        //兼容旧版本办公电话取自
        if ((!StringUtils.isEmpty(version)) && (Integer.parseInt(version) >= 1) && person.getOfficeTel() != null) {
            tel = person.getOfficeTel();
        }
        Map<String, String> phoneMap = this.getPhoneNoAndZone(tel);
        apptmap.put("indiv_emp_zone", phoneMap.get("zone"));
        apptmap.put("indiv_emp_tel", phoneMap.get("tel"));// custExtInfoBodyMap.get("officeTel")
        if (tel.equals("null") || StringUtils.isEmpty(tel)) {
            apptmap.put("indiv_emp_zone", "0532");
            apptmap.put("indiv_emp_tel", "58869762");
        }

        //   }
        apptmap.put("indiv_emp_tel_sub", "");
        apptmap.put("indiv_emp_hr_zone", "");
        apptmap.put("indiv_emp_hr_phone", "");
        apptmap.put("indiv_emp_hr_sub", "");

        apptmap.put("indiv_position", custExtInfoBodyMap.get("position"));
        apptmap.put("indiv_work_yrs", StringUtils.isEmpty(custExtInfoBodyMap.get("workYrs")) ?
                0 :
                custExtInfoBodyMap.get("workYrs"));// custExtInfoBodyMap.get("workYrs")
        apptmap.put("spouse_name", custExtInfoBodyMap.get("spouseName"));// 如果有夫妻关系联系人，会覆盖此参数
        apptmap.put("spouse_id_typ", custExtInfoBodyMap.get("spouseCertType"));
        apptmap.put("spouse_id_no", custExtInfoBodyMap.get("spouseCertNo"));
        apptmap.put("spouse_emp", custExtInfoBodyMap.get("spouseOffice"));
        apptmap.put("spouse_mobile", custExtInfoBodyMap.get("spouseMobile"));// 如果有夫妻关系联系人，会覆盖此参数
        apptmap.put("spouse_branch", "");
        apptmap.put("spouse_emp_zone", "");
        apptmap.put("spouse_emp_tel", "");
        apptmap.put("spouse_emp_tel_sub", "");
        apptmap.put("spouse_emp_province", "");
        apptmap.put("spouse_emp_city", "");
        apptmap.put("spouse_emp_area", "");
        apptmap.put("spouse_emp_addr", "");
        apptmap.put("sp_mth_inc", "");
        apptmap.put("spouse_pay_ind", "");
        // 房产地址：APP不录入房产信息，必填项填固定值
        //        apptmap.put("ppty_live_opt", StringUtils.isEmpty(custExtInfoBodyMap.get("pptyLiveInd")) ?
        //                "10" :
        //                custExtInfoBodyMap.get("pptyLiveInd").toString());//自有房产地址
        apptmap.put("ppty_live_opt", "10");
        apptmap.put("ppty_loan_ind", StringUtils.isEmpty(custExtInfoBodyMap.get("pptyLoanInd")) ?
                "N" :
                custExtInfoBodyMap.get("pptyLoanInd").toString());//是否按揭
        apptmap.put("ppty_righ_name", StringUtils.isEmpty(custExtInfoBodyMap.get("pptyRighName")) ?
                "无" :
                custExtInfoBodyMap.get("pptyRighName").toString());//房屋产权人
        apptmap.put("ppty_amt", StringUtils.isEmpty(custExtInfoBodyMap.get("pptyAmt")) ?
                0 :
                Double.parseDouble(custExtInfoBodyMap.get("pptyAmt").toString()));//购买价格
        apptmap.put("ppty_live_province", custExtInfoBodyMap.get("pptyProvince"));
        apptmap.put("ppty_live_city", custExtInfoBodyMap.get("pptyCity"));
        apptmap.put("ppty_live_area", custExtInfoBodyMap.get("pptyArea"));
        apptmap.put("ppty_live_addr", custExtInfoBodyMap.get("pptyAddr"));
        apptmap.put("ppty_loan_amt", StringUtils.isEmpty(custExtInfoBodyMap.get("pptyLoanAmt")) ?
                "0" :
                custExtInfoBodyMap.get("pptyLoanAmt"));
        apptmap.put("ppty_loan_year", custExtInfoBodyMap.get("pptyLoanYear"));
        apptmap.put("ppty_loan_bank", custExtInfoBodyMap.get("pptyLoanBank"));
        //////////////////////
        HashMap<String, Object> relMap = new HashMap<String, Object>();

        List rellist = new ArrayList();
        for (Map<String, Object> obj : lxrlist) {
            HashMap<String, Object> rel = new HashMap<String, Object>();
            rel.put("rel_name", obj.get("contactName"));
            rel.put("rel_id_typ", obj.get("certType"));
            rel.put("rel_id_no", obj.get("certNo"));
            rel.put("rel_relation", obj.get("relationType"));
            rel.put("rel_addr", obj.get("contactAddr"));
            rel.put("rel_mobile", obj.get("contactMobile"));
            rel.put("rel_emp_name", obj.get("officeName"));
            if (rel.get("rel_relation").equals("06") // 06,夫妻
                    && apptmap.get("indiv_marital").equals("20")) {
                // 配偶姓名 spouse_name
                apptmap.put("spouse_name", rel.get("rel_name"));
                // 配偶电话 spouse_mobile
                apptmap.put("spouse_mobile", rel.get("rel_mobile"));
                continue;
            }
            rellist.add(rel);

            //			// 如果申请人已婚，必输配偶姓名和配偶手机号
            //			if (rel.get("rel_relation").equals("06") // 06,夫妻
            //					&& apptmap.get("indiv_marital").equals("20")) {// 20、已婚
            //				// 配偶姓名 spouse_name
            //				apptmap.put("spouse_name", rel.get("rel_name"));
            //				// 配偶电话 spouse_mobile
            //				apptmap.put("spouse_mobile", rel.get("rel_mobile"));
            //			}
        }
        relMap.put("rel", rellist);
        apptmap.put("relList", relMap);
        return apptmap;
    }

    /**
     * 额度申请service
     */
    public Map<String, Object> getEdApplInfo(String custNo, String flag, String applSeq, String expectCredit,
                                             String channel, String channelNo) {
        HashMap<String, Object> parmMap = new HashMap<String, Object>();
        logger.debug("==expectCredit:" + expectCredit);
        if (!StringUtils.isEmpty(expectCredit)) {
            if (!DataVerificationUtil.isNumber(expectCredit)) {
                logger.debug(ConstUtil.ERROR_PARAM_INVALID_CODE + "期望额度只能填写数字！");
                return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "期望额度只能填写数字！");
            }
            Double expectCreditMax = Double.valueOf("200000");
            if (Double.valueOf(expectCredit) > expectCreditMax) {
                logger.debug(ConstUtil.ERROR_PARAM_INVALID_CODE + " 期望额度不能超过" + expectCreditMax + "！");
                return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, " 期望额度不能超过" + expectCreditMax + "！");
            }
            parmMap.put("expectCredit", expectCredit);//期望额度
        }
        parmMap.put("autoFlag", "N");// 是否自动提交
        //if(!"1".equals(flag)){
        parmMap.put("userId", "HAIERONLINE");// 用户Id 1必输
        //}
        parmMap.put("applSeq", applSeq);//额度申请流水号 2必输
        parmMap.put("flag", StringUtils.isEmpty(flag) ? "0" : flag);// 标示 0.额度申请 1.用户信息维护 2.额度信息修改
        String grInfoUrl = EurekaServer.CRM + "/app/crm/cust/queryCustRealInfoByCustNo?custNo=" + custNo;
        String grInfoJson = HttpUtil.restGet(grInfoUrl, getToken());
        logger.debug("CRM 1.26 grInfoJson==" + grInfoJson);
        if (StringUtils.isEmpty(grInfoJson)) {
            logger.error("实名认证信息查询失败！——》CRM 1.26");
            return fail("54", RestUtil.ERROR_INTERNAL_MSG);
        }
        // {head={"retMsg":"客户实名信息不存在!","retFlag":"C1219"}}
        /**
         * {"head":{"retFlag":"00000","retMsg":"处理成功"},"body":{"custNo":
         * "C201511130922759X66750","custName":"闫玉看","certType":"20","certNo":
         * "51102719810922759X","cardNo":"6221885939000813258","mobile":
         * "13278376675","acctName":null,"acctBankNo":"403","acctBankName":
         * "中国邮政储蓄银行","acctProvince":"440000","acctCity":"441200","acctArea":
         * null,"accBchCde":null,"accBchName":null,"faceValue":null,"dataFrom":
         * "old_crm"}}
         */
        Map<String, Object> grInfoMap = HttpUtil.json2Map(grInfoJson);
        logger.info("grInfoMap==" + grInfoMap);
        JSONObject headJson = (JSONObject) grInfoMap.get("head");
        logger.info("headJson==" + headJson);
        String retFlag = headJson.getString("retFlag");
        String retMsg = headJson.getString("retMsg");
        if (!"00000".equals(retFlag)) {
            return fail(RestUtil.ERROR_INTERNAL_CODE, retMsg);
        }
        JSONObject bodyJson = (JSONObject) grInfoMap.get("body");
        logger.info("headJson==" + bodyJson);
        parmMap.put("apptCustName", bodyJson.getString("custName"));// 姓名
        parmMap.put("apptIndivSex", IdcardUtils.getGenderByIdCard(bodyJson.getString("certNo")));// 性别
        parmMap.put("apptIdNo", bodyJson.getString("certNo"));// 身份证号
        parmMap.put("indivMobile", bodyJson.getString("mobile"));// 手机号
        String url = EurekaServer.CRM + "/app/crm/cust/getCustExtInfo?pageName=1&custNo=" + custNo;

        String json = HttpUtil.restGet(url, getToken());
        logger.debug("CRM 1.4 json==" + json);
        if (StringUtils.isEmpty(json)) {
            logger.error("客户扩展信息接口返回异常!——》 CRM 1.4");
            return fail("51", RestUtil.ERROR_INTERNAL_MSG);
        }
        Map<String, Object> custExtInfoMap = HttpUtil.json2Map(json);
        logger.debug("custExtInfoMap==" + custExtInfoMap);
        Map<String, Object> custExtInfoBodyMap = new HashMap<>();
        if (!StringUtils.isEmpty(custExtInfoMap.get("body"))) {
            // 客户扩展信息查询
            custExtInfoBodyMap = HttpUtil.json2Map(custExtInfoMap.get("body").toString());
            logger.info("custExtInfoBodyMap==" + custExtInfoBodyMap);
        }
        this.dealAddress(custExtInfoBodyMap);//对地址进行处理
        parmMap.put("indivEdu", custExtInfoBodyMap.get("education"));// 最高学历

        if ("11".equals(channel) || "14".equals(channel)) {
            parmMap.put("indivEdu", StringUtils.isEmpty(custExtInfoBodyMap.get("education")) ?
                    "20" :
                    custExtInfoBodyMap.get("education"));// 最高学历
        }
        parmMap.put("indivMarital", custExtInfoBodyMap.get("maritalStatus"));// 婚姻状态
        parmMap.put("indivDepNo", StringUtils.isEmpty(custExtInfoBodyMap.get("providerNum")) ? "0"
                : custExtInfoBodyMap.get("providerNum"));// 供养子女数
        if ("16".equals(channel)) {
            parmMap.put("indivDepNo", StringUtils.isEmpty(custExtInfoBodyMap.get("providerNum")) ? "1"
                    : custExtInfoBodyMap.get("providerNum"));
        }
        // 如果户籍省市为空，则传居住省市.星巢贷修改
        parmMap.put("apptRegProvince",
                StringUtils.isEmpty(custExtInfoBodyMap.get("regProvince")) ?
                        custExtInfoBodyMap.get("liveProvince") : custExtInfoBodyMap.get("regProvince"));// 户籍所在省
        parmMap.put("apptRegCity", StringUtils.isEmpty(custExtInfoBodyMap.get("regCity")) ?
                custExtInfoBodyMap.get("liveCity") : custExtInfoBodyMap.get("regCity"));// 户籍所在市
        parmMap.put("liveInfo", "10");// 现居住房屋所有权 写死
        parmMap.put("liveProvince", custExtInfoBodyMap.get("liveProvince"));// 现居住省
        parmMap.put("liveCity", custExtInfoBodyMap.get("liveCity"));// 现居住市
        parmMap.put("liveArea", custExtInfoBodyMap.get("liveArea"));// 现居地址区
        parmMap.put("liveAddr", custExtInfoBodyMap.get("liveAddr"));// 现居地址
        parmMap.put("localResid", custExtInfoBodyMap.get("localResid"));// 户口性质
        if (("11".equals(channel) || "14".equals(channel) || "16".equals(channel)) && StringUtils.isEmpty(custExtInfoBodyMap.get("localResid"))) {
            parmMap.put("localResid", "10");// 户口性质
        }

        parmMap.put("liveYear", StringUtils.isEmpty(custExtInfoBodyMap.get("liveYear")) ?
                "0" :
                custExtInfoBodyMap.get("liveYear").toString());// 本地居住年限 为0
        parmMap.put("indivEmpName", custExtInfoBodyMap.get("officeName"));// 单位)名称
        parmMap.put("indivEmpTyp", StringUtils.isEmpty(custExtInfoBodyMap.get("officeTyp")) ?
                "Z" :
                custExtInfoBodyMap.get("officeTyp"));// 行业性质
        parmMap.put("indivBranch", StringUtils.isEmpty(custExtInfoBodyMap.get("officeDept")) ?
                "默认" :
                custExtInfoBodyMap.get("officeDept"));// 所在部门
        if("16".equals(channel)) {
            parmMap.put("indivBranch", StringUtils.isEmpty(custExtInfoBodyMap.get("officeDept")) ?
                    "其他" :
                    custExtInfoBodyMap.get("officeDept"));// 所在部门
        }
        // 如果职务为空，默认传02中层，为了星巢贷处理
        parmMap.put("indivPosition", StringUtils.isEmpty(custExtInfoBodyMap.get("position")) ?
                "02" :
                custExtInfoBodyMap.get("position"));// 职务
        parmMap.put("positionOpt", StringUtils.isEmpty(custExtInfoBodyMap.get("positionType")) ? "10"
                : custExtInfoBodyMap.get("positionType"));// 从业性质
        parmMap.put("indivEmpProvince", custExtInfoBodyMap.get("officeProvince"));// 单位地址省
        parmMap.put("indivEmpCity", custExtInfoBodyMap.get("officeCity"));// 单位地址市
        parmMap.put("indivEmpArea", custExtInfoBodyMap.get("officeArea"));// 现单位地址区
        parmMap.put("empaddr", custExtInfoBodyMap.get("officeAddr"));// 现单位地址
        parmMap.put("IndivMthInc", custExtInfoBodyMap.get("mthInc"));// 月均收入
        if (StringUtils.isEmpty(custExtInfoBodyMap.get("mthInc"))) {
            parmMap.put("annualEarn", "0");
            if ("16".equals(channel)) {
                parmMap.put("IndivMthInc", "00");
            }
        } else {
            parmMap.put("annualEarn",
                    new BigDecimal(custExtInfoBodyMap.get("mthInc").toString()).multiply(new BigDecimal(12))
                            .toString());// 税后年收入
        }
        //parmMap.put("indivEmpTel", "0532-58869762");// 电话
        //
        if (StringUtils.isEmpty(custExtInfoBodyMap.get("officeTel"))) {
            parmMap.put("indivEmpTel", "0532-58869762");// 电话
        } else {
            String tel = String.valueOf(custExtInfoBodyMap.get("officeTel"));
            Map<String, String> phoneMap = this.getPhoneNoAndZone(tel);
            parmMap.put("indivEmpTel", phoneMap.get("zone") + "-" + phoneMap.get("tel"));// 电话
        }
        //
        parmMap.put("indivWorkYrs", StringUtils.isEmpty(custExtInfoBodyMap.get("workYrs")) ?
                0 :
                custExtInfoBodyMap.get("workYrs"));// 总工龄
        if ("16".equals(channel)) {
            parmMap.put("indivWorkYrs", StringUtils.isEmpty(custExtInfoBodyMap.get("workYrs")) ?
                    1 :
                    custExtInfoBodyMap.get("workYrs"));// 总工龄
        }
        parmMap.put("applyAmt", "");// 申请金额 可以为空
        parmMap.put("repayApplCardNo", String.valueOf(bodyJson.get("cardNo")));// 还款卡号
        parmMap.put("repayApplAcNam", String.valueOf(bodyJson.get("acctName")));// 账户名
        parmMap.put("repayAccBankCde", bodyJson.get("acctBankNo"));// 银行号
        parmMap.put("repayAccBankName", bodyJson.get("acctBankName"));// 开户银行名称
        parmMap.put("repayAcProvince", bodyJson.get("acctProvince"));// 开户省
        parmMap.put("repayAcCity", bodyJson.get("acctCity"));// 开户市
        //        parmMap.put("pptyLiveOpt", StringUtils.isEmpty(custExtInfoBodyMap.get("pptyLiveInd")) ?
        //                "10" :
        //                custExtInfoBodyMap.get("pptyLiveInd").toString());// 房产地址
        parmMap.put("pptyLiveOpt", "10");
        // custExtInfoBodyMap.get("pptyLiveInd")
        parmMap.put("pptyLiveProvince", custExtInfoBodyMap.get("liveProvince"));// 房产地址所在省
        // pptyProvince
        parmMap.put("pptyLiveCity", custExtInfoBodyMap.get("liveCity"));// 房产地址所在市
        // pptyCity
        parmMap.put("pptyLiveArea", custExtInfoBodyMap.get("liveArea"));// 房产地址所在区
        // pptyArea

        parmMap.put("liveZip", custExtInfoBodyMap.get("liveZip"));// 邮政编码
        parmMap.put("pptyRighName", StringUtils.isEmpty(custExtInfoBodyMap.get("pptyRighName")) ?
                "无" :
                custExtInfoBodyMap.get("pptyRighName").toString());// 房屋产权人
        parmMap.put("pptyAmt", StringUtils.isEmpty(custExtInfoBodyMap.get("pptyAmt")) ?
                0 :
                Double.parseDouble(custExtInfoBodyMap.get("pptyAmt").toString()));// 购买价格
        parmMap.put("pptyLoanInd", StringUtils.isEmpty(custExtInfoBodyMap.get("pptyLoanInd")) ?
                "N" :
                custExtInfoBodyMap.get("pptyLoanInd"));// 是否按揭
        parmMap.put("mortgageRatio", custExtInfoBodyMap.get("mortgageRatio"));// 按揭比例
        parmMap.put("pptyLoanYear", custExtInfoBodyMap.get("pptyLoanYear"));// 按揭周期（年）
        parmMap.put("mortgagePartner", custExtInfoBodyMap.get("mortgagePartner"));// 按揭参与人
        parmMap.put("pptyLoanBank", custExtInfoBodyMap.get("pptyLoanBank"));// 按揭银行
        appManageService.putCooprSettingToMap(parmMap, channel); //门店信息
        HashMap<String, Object> relMap = new HashMap<>();
        String lxrUrl = EurekaServer.CRM + "/app/crm/cust/findCustFCiCustContactByCustNo?custNo=" + custNo;
        String lxrJson = HttpUtil.restGet(lxrUrl, getToken());
        if (StringUtils.isEmpty(lxrJson)) {
            logger.error("联系人列表查询失败!——》CRM 1.8");
            return fail("52", RestUtil.ERROR_INTERNAL_MSG);
        }
        Map<String, Object> lxrMap = HttpUtil.json2Map(lxrJson);
        List<Map<String, Object>> lxrlist = new ArrayList<>();
        if (!StringUtils.isEmpty(lxrMap.get("body"))) {
            lxrlist = (ArrayList) lxrMap.get("body");
        }

        List rellist = new ArrayList();
        for (Map<String, Object> obj : lxrlist) {
            HashMap<String, Object> rel = new HashMap<>();
            rel.put("relName", obj.get("contactName") == null ? "" : obj.get("contactName"));
            rel.put("relRelation", obj.get("relationType") == null ? "" : obj.get("relationType"));
            rel.put("relAddr", obj.get("contactAddr") == null ? "" : obj.get("contactAddr"));
            rel.put("relMobile", obj.get("contactMobile") == null ? "" : obj.get("contactMobile"));
            rel.put("relEmpName", obj.get("officeName") == null ? "" : obj.get("officeName"));
            rellist.add(rel);
        }
        relMap.put("rel", rellist);
        parmMap.put("list", relMap);
        // 把null全部替换成空字符串，避免信贷接口报错
        for (String key : parmMap.keySet()) {
            if (parmMap.get(key) == null || "null".equals(parmMap.get(key).toString())) {
                parmMap.put(key, "");
            }
        }

        // 红星美凯龙处理
        if (!StringUtils.isEmpty(channel) && "16".equals(channel)) {
            parmMap.put("sysFlag", channel);
            parmMap.put("channelNo", "31");
        }
        if (!StringUtils.isEmpty(channelNo) && !StringUtils.isEmpty(channel)) {
            parmMap.put("sysFlag", channel);
            parmMap.put("channelNo", channelNo);
        }

        if (StringUtils.isEmpty(channel)) {
            parmMap.put("sysFlag", "14");
            parmMap.put("channelNo", "05");
        }

        logger.info("信贷100010请求参数：" + parmMap);
        Map<String, Object> applMapResult = CmisUtil.getCmisResponse(CmisTradeCode.TRADECODE_ED_APPLAY, getToken(),
                parmMap);
        return applMapResult;
    }

    /**
     * 电话号码处理 返回：Map<String,Object> zone tel
     * 处理逻辑：
     * 1、 将 横（-）全部替换
     * 2、如果不是以0开头，则不进行拆分，
     * 3、如果是以0开头，判断几个直辖市 如果为几个直辖市，则取前3位为区号，后面的为号码
     * 4、如果不是以0开头的，则说明不带区号，原封返回
     */
    public Map<String, String> getPhoneNoAndZone(String phoneNo) {
        HashMap resultMap = new HashMap<String, String>();
        // 1、将号码中的-替换掉
        String no = phoneNo.replaceAll("-", "");
        // 判断长度，防止数组越界
        if (no.length() < 5) {
            resultMap.put("zone", "");
            resultMap.put("tel", no);
            return resultMap;
        }

        // 0开头说明有区号
        if (no.startsWith("0")) {
            // 判断是否以直辖市开头（ 北京市 010 上海市 021 天津市 022 重庆市 023 香港 852 澳门 853）
            if (no.startsWith("010") || no.startsWith("021") || no.startsWith("022") || no.startsWith("023")
                    || no.startsWith("852") || no.startsWith("853")) {
                resultMap.put("zone", no.substring(0, 3));
                resultMap.put("tel", no.substring(3, no.length()));
            } else {
                resultMap.put("zone", no.substring(0, 4));
                resultMap.put("tel", no.substring(4, no.length()));
            }
            //如果是手機號（即為1開頭的）,將其拆成4+7 格式
        } else if (no.startsWith("1")) {
            resultMap.put("zone", no.substring(0, 4));
            resultMap.put("tel", no.substring(4, no.length()));
        } else {
            resultMap.put("zone", "");
            resultMap.put("tel", no);
        }
        return resultMap;
    }

    /**
     * 还款试算的service
     *
     * @param map
     * @param gateUrl
     * @param token
     * @return
     */
    public Map<String, Object> getHkssReturnMap(HashMap map, String gateUrl, String token) {
        /**
         * 查询贷款品种详情
         */
        Map<String, Object> dataMap;
        //
        String url = EurekaServer.CMISPROXY + "/api/pLoanTyp/find?typCde=" + map.get("typCde");
        logger.debug("查询贷款品种详情url" + url);
        String json = HttpUtil.restGet(url, token);
        logger.info("贷款品种详情返回：" + json);
        if (StringUtils.isEmpty(json)) {
            return fail("01", "查询失败");
        } else {
            dataMap = HttpUtil.json2Map(json);
        }
        logger.info("贷款品种详情dataMap：" + dataMap);
        // JSONObject dkpzmap = (JSONObject ) dataMap.get("body");
        // logger.info("贷款dkpzmap查询"+dkpzmap);
        /////////////////
        if (!map.containsKey("typSeq")) {
            // 贷款品种序号
            map.put("typSeq", dataMap.get("typSeq"));
        }
        // 还款间隔
        if (!map.containsKey("loanFreq")) {
            map.put("loanFreq", dataMap.get("typFreq"));
        }
        if (!map.containsKey("mtdCde") || StringUtils.isEmpty(map.get("mtdCde"))) {
            List<Map<String, Object>> hkfsMap;
            //
            String url2 = EurekaServer.CMISPROXY + "/api/pLoanTyp/typMtd?typCde=" + map.get("typCde");
            logger.info("url2:" + url2);
            String json2 = HttpUtil.restGet(url2, token);
            logger.debug("===" + json2);
            if (StringUtils.isEmpty(json2)) {
                return fail("01", "查询失败");
            } else {
                hkfsMap = HttpUtil.json2List(json2);
            }
            logger.debug("hkfsMap==" + hkfsMap);
            if (hkfsMap.size() > 0) {
                Map<String, Object> hkmap = hkfsMap.get(0);
                // 还款方式
                map.put("mtdCde", hkmap.get("mtdCde"));
            }
        }

        if (!map.containsKey("proPurAmt")) {
            map.put("proPurAmt", "0");
        }
        // 如果flag为空，则默认为0,反之，接收flag的实际值
        if (!map.containsKey("flag")) {
            map.put("flag", "0");
        }
        Map<String, Object> result = new HashMap<String, Object>();
        logger.debug("map==" + map);
        try {
            result = CmisUtil.getCmisResponse(CmisTradeCode.TRADECODE_HKSS, token, map);
        } catch (Exception e) {
            logger.debug("发现异常被执行：==" + e.getMessage());
        }
        logger.debug("还款试算cmis查询结果result.get('response')==" + result.get("response"));
        HashMap<String, Object> responseMap = (HashMap<String, Object>) result.get("response");
        HashMap<String, Object> headMap = (HashMap<String, Object>) responseMap.get("head");
        String retFlag = String.valueOf(headMap.get("retFlag"));
        String retMsg = String.valueOf(headMap.get("retMsg"));
        if (!"00000".equals(retFlag)) {
            return fail("99", "还款试算接口异常：" + retMsg);
        }
        HashMap<String, Object> bodyMap = (HashMap<String, Object>) responseMap.get("body");
        logger.debug("=======" + result);
        HashMap<String, Object> returnmap = new HashMap<String, Object>();
        System.out.println(result);
        List relist = new ArrayList();
        HashMap<String, Object> payList = (HashMap<String, Object>) bodyMap.get("payList");
        List<HashMap<String, Object>> mx = (List<HashMap<String, Object>>) payList.get("mx");
        if (mx != null) {
            // 初始化第0期的费用为0
            BigDecimal fee_0 = BigDecimal.ZERO;
            BigDecimal deductAmt = BigDecimal.ZERO;//斩头息费
            BigDecimal actualArriveAmt = BigDecimal.ZERO;//实际到账金额
            logger.debug("===还款试算的还款计划列表:" + mx);
            for (HashMap<String, Object> hm : mx) {
                HashMap<String, Object> newhm = new HashMap<String, Object>();
                if ((Integer) hm.get("psPerdNo") == 0) {
                    logger.debug("===第0期:" + hm);
                    // 获取第0期的费用并转String
                    String fee_0_String = String.valueOf(hm.get("psFeeAmt"));
                    if (!StringUtils.isEmpty(fee_0_String) && (!Objects.equals(fee_0_String, "null"))) {
                        fee_0 = new BigDecimal(fee_0_String);
                    }
                    BigDecimal normInt_0 = BigDecimal.ZERO;//第0期的应归还利息
                    String normInt_0_String = String.valueOf(hm.get("normInt"));
                    if (!StringUtils.isEmpty(normInt_0_String) && (!Objects.equals(normInt_0_String, "null"))) {
                        normInt_0 = new BigDecimal(normInt_0_String);
                    }
                    deductAmt = normInt_0.add(fee_0).setScale(2, BigDecimal.ROUND_UP);
                    logger.debug("本金：" + hm.get("psRemPrcp") + ",fee_0:" + fee_0);
                    actualArriveAmt = new BigDecimal(String.valueOf(hm.get("psRemPrcp"))).subtract(fee_0);
                    continue;
                }
                newhm.put("psPerdNo", hm.get("psPerdNo"));
                BigDecimal defee = null;
                String feeString = String.valueOf(StringUtils.isEmpty(hm.get("psFeeAmt")) ? "0" : hm.get("psFeeAmt"));
                if (!StringUtils.isEmpty(feeString) && (!Objects.equals(feeString, "null"))) {
                    defee = new BigDecimal(feeString);
                }
                // 每期应归还的期供等于期供+费用
                newhm.put("instmAmt", String.valueOf(new BigDecimal(String.valueOf(hm.get("instmAmt"))).add(defee)
                        .setScale(2, BigDecimal.ROUND_HALF_UP)));
                relist.add(newhm);
            }
            returnmap.put("totalAmt", bodyMap.get("totalAmt"));
            returnmap.put("totalFeeAmt",
                    new BigDecimal(String.valueOf(bodyMap.get("totalFeeAmt"))).subtract(fee_0).doubleValue());
            returnmap.put("totalNormInt", bodyMap.get("totalNormInt"));
            returnmap.put("deductAmt", deductAmt.doubleValue());
            returnmap.put("actualArriveAmt", actualArriveAmt.doubleValue());
            BigDecimal totalFeeAmtTemp = new BigDecimal(String.valueOf(bodyMap.get("totalFeeAmt"))).subtract(fee_0);
            BigDecimal totalNormIntTemp = new BigDecimal(bodyMap.get("totalNormInt").toString());
            BigDecimal totalFees = totalFeeAmtTemp.add(totalNormIntTemp).add(deductAmt);//总息费金额
            returnmap.put("totalFees", totalFees.doubleValue());

            BigDecimal repaymentTotalAmt = new BigDecimal(String.valueOf(bodyMap.get("totalAmt")))
                    .add(totalFeeAmtTemp);//还款总额
            returnmap.put("repaymentTotalAmt", repaymentTotalAmt.doubleValue());
        }
        returnmap.put("mx", relist);
        logger.debug("还款试算返回：" + returnmap);
        return success(returnmap);
    }

    /**
     * 批量还款试算的service
     *
     * @param typCde
     * @param apprvAmt
     * @param gateUrl
     * @param token
     * @return
     */
    public Map<String, Object> getBatchHkssReturnMap(String typCde, String apprvAmt, String gateUrl, String token) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        List<Map<String, Object>> resultList = new ArrayList<>();
        HashMap<String, Object> map = new HashMap<>();
        /**
         * 查询贷款品种详情
         */
        Map<String, Object> dataMap;
        //
        String url = EurekaServer.CMISPROXY + "/api/pLoanTyp/find?typCde=" + typCde;
        logger.debug("查询贷款品种详情url:" + url);
        String json = HttpUtil.restGet(url, token);
        if (StringUtils.isEmpty(json)) {
            return fail("01", "查询失败");
        } else {
            dataMap = HttpUtil.json2Map(json);
        }
        logger.info("贷款品种详情dataMap：" + dataMap);
        // JSONObject dkpzmap = (JSONObject ) dataMap.get("body");
        // logger.info("贷款dkpzmap查询"+dkpzmap);
        /////////////////
        map.put("typCde", typCde);
        map.put("apprvAmt", apprvAmt);
        if (!map.containsKey("typSeq")) {
            // 贷款品种序号
            map.put("typSeq", dataMap.get("typSeq"));
        }
        // 还款间隔
        if (!map.containsKey("loanFreq")) {
            map.put("loanFreq", dataMap.get("typFreq"));
        }
        if (!map.containsKey("mtdCde") || StringUtils.isEmpty(map.get("mtdCde"))) {
            List<Map<String, Object>> hkfsMap;
            //
            String url2 = EurekaServer.CMISPROXY + "/api/pLoanTyp/typMtd?typCde=" + map.get("typCde");
            logger.info("url2:" + url2);
            String json2 = HttpUtil.restGet(url2, token);
            logger.debug("===" + json2);
            if (StringUtils.isEmpty(json2)) {
                return fail("01", "查询失败");
            } else {
                hkfsMap = HttpUtil.json2List(json2);
            }
            logger.debug("hkfsMap==" + hkfsMap);
            if (hkfsMap.size() > 0) {
                Map<String, Object> hkmap = hkfsMap.get(0);
                // 还款方式
                map.put("mtdCde", hkmap.get("mtdCde"));
            }
        }

        if (!map.containsKey("proPurAmt")) {
            map.put("proPurAmt", "0");
        }
        // 如果flag为空，则默认为0,反之，接收flag的实际值
        if (!map.containsKey("flag")) {
            map.put("flag", "0");
        }

        String tnrOptData = String.valueOf(dataMap.get("tnrOpt"));
        if ("D".equals(tnrOptData)) { //D- 按天
            logger.debug("还款期限类型为D-按天");
            return fail("01", "还款期限类型为按天");
        } else {//有多种还款期限
            String[] tnrOpts = String.valueOf(dataMap.get("tnrOpt")).split(",");
            String psPerdNo;//期数
            for (int i = 0; i < tnrOpts.length; i++) {
                psPerdNo = tnrOpts[i];
                logger.debug("---还款期限为：" + psPerdNo + " 开始---");
                map.put("applyTnrTyp", psPerdNo);
                map.put("applyTnr", psPerdNo);
                Map<String, Object> result = new HashMap<String, Object>();
                logger.debug("map==" + map);
                try {
                    result = CmisUtil.getCmisResponse(CmisTradeCode.TRADECODE_HKSS, token, map);
                } catch (Exception e) {
                    logger.debug("发现异常被执行：==" + e.getMessage());
                }
                HashMap<String, Object> responseMap = (HashMap<String, Object>) result.get("response");
                HashMap<String, Object> headMap = (HashMap<String, Object>) responseMap.get("head");
                String retFlag = String.valueOf(headMap.get("retFlag"));
                String retMsg = String.valueOf(headMap.get("retMsg"));
                if (!"00000".equals(retFlag)) {
                    return fail("99", "还款试算接口异常：" + retMsg);
                }
                HashMap<String, Object> bodyMap = (HashMap<String, Object>) responseMap.get("body");
                logger.debug("=======" + result);
                HashMap<String, Object> returnmap = new HashMap<String, Object>();
                System.out.println(result);
                //                List relist = new ArrayList();
                HashMap<String, Object> payList = (HashMap<String, Object>) bodyMap.get("payList");
                List<HashMap<String, Object>> mx = (List<HashMap<String, Object>>) payList.get("mx");
                if (mx != null) {
                    BigDecimal instmAmt = null;
                    String feeString = "0";
                    // 初始化第0期的费用为0
                    BigDecimal fee_0 = BigDecimal.ZERO;
                    //                    HashMap<String, Object> newhm = new HashMap<String, Object>();
                    for (HashMap<String, Object> hm : mx) {
                        if ((Integer) hm.get("psPerdNo") == 1) {
                            // 获取第1期的费用并转String
                            String fee_0_String = String.valueOf(hm.get("psFeeAmt"));
                            if (!StringUtils.isEmpty(fee_0_String) && fee_0_String != "null") {
                                fee_0 = new BigDecimal(fee_0_String);
                            }
                            instmAmt = new BigDecimal(String.valueOf(hm.get("instmAmt")));
                            feeString = String
                                    .valueOf(StringUtils.isEmpty(hm.get("psFeeAmt")) ? "0" : hm.get("psFeeAmt"));
                            break;
                        }
                    }
                    Map<String, Object> MapInfo = new HashMap<String, Object>();
                    MapInfo.put("psPerdNo", psPerdNo);
                    BigDecimal defee = null;
                    if (!StringUtils.isEmpty(feeString) && feeString != "null") {
                        defee = new BigDecimal(feeString);
                    }
                    // 每期应归还的期供等于期供+费用
                    MapInfo.put("instmAmt", instmAmt.add(defee).setScale(2, BigDecimal.ROUND_HALF_UP));
                    logger.debug("---还款期限为" + psPerdNo + "查询结束,返回结果:" + MapInfo);
                    resultList.add(MapInfo);
                }
            }
            resultMap.put("info", resultList);
            return success(resultMap);
        }
    }

    public Map<String, Object> getZdhhFee(String loanNo, String paymMoney, String paymMode, String token,
                                          String gateIp) {
        double zdhhFee;
        if ("FS".equals(paymMode)) {
            HashMap<String, Object> fsMap = new HashMap<>();
            // 借据号 loanNo
            fsMap.put("loanNo", loanNo);
            // 主动还款金额 actvPayAmt
            fsMap.put("actvPayAmt", paymMoney);
            // 还款类型 paymMode
            fsMap.put("paymMode", "FS");
            fsMap.put("setlTyp", "NM");
            fsMap.put("relPerdCnt", 0);
            // 格式{response={head={retMsg=交易成功！, retFlag=00000}, body={odInt=0,
            // prcpAmt=1333.33, actvPrcp=14666.67, normInt=0,
            // loanNo=HCF-HAPA0120160320795362001, commInt=0, actvNormInt=0,
            // relPerdCnt=0, feeAmt=362.53}}}
            Map<String, Object> xdHkssMap = CmisUtil.getCmisResponse(CmisTradeCode.TRADECODE_ZDHKSS, token, fsMap);
            ;
            Map<String, Object> xdHkssResponseMap = (HashMap<String, Object>) xdHkssMap.get("response");
            Map<String, Object> xdHkssHeadMap = (HashMap<String, Object>) xdHkssResponseMap.get("head");
            String retFlag = String.valueOf(xdHkssHeadMap.get("retFlag"));
            String retMsg = String.valueOf(xdHkssHeadMap.get("retMsg"));
            if (!"00000".equals(retFlag)) {
                return fail(RestUtil.ERROR_INTERNAL_CODE, retMsg);
            }
            Map<String, Object> xdHkssBodyMap = (HashMap<String, Object>) xdHkssResponseMap.get("body");
            logger.info("xdHkssMap==" + xdHkssMap);
            logger.info("xDHkssBodyMap==" + xdHkssBodyMap);
            // 应归还本金 PRCP_AMT
            String prcpAmt = String.valueOf(xdHkssBodyMap.get("prcpAmt"));
            // 应归还正常利息 Norm_Int
            String normInt = String.valueOf(xdHkssBodyMap.get("normInt"));
            // 应归还逾期利息 OD_INT
            String odInt = String.valueOf(xdHkssBodyMap.get("odInt"));
            // 应归还复利 COMM_INT
            String commInt = String.valueOf(xdHkssBodyMap.get("commInt"));
            // 应归还费用 FEE_AMT
            String feeAmt = String.valueOf(xdHkssBodyMap.get("feeAmt"));
            // 主动还款金额
            String actvPrcp = String.valueOf(xdHkssBodyMap.get("actvPrcp"));
            // 主动还款利息
            String actvNormInt = String.valueOf(xdHkssBodyMap.get("actvNormInt"));
            logger.info(prcpAmt + "==" + normInt + "==" + odInt + "==" + commInt + "==" + feeAmt);
            zdhhFee = new BigDecimal(prcpAmt).add(new BigDecimal(normInt)).add(new BigDecimal(odInt))
                    .add(new BigDecimal(commInt)).add(new BigDecimal(feeAmt)).add(new BigDecimal(actvPrcp))
                    .add(new BigDecimal(actvNormInt)).doubleValue();
            logger.info("FS  zdhhFee==" + zdhhFee);

			/*
             * NM（4.11）: x
			 * 1.调欠款查询接口，获取欠款的所有金额的和（应归还本金+应归还正常利息+应归还逾期利息+应归还复利+应归还费用）
			 * 如果主动金额不等于欠款查询的所有金额的和返回还款金额不正确
			 * 2再调还款试算（核算系统4.11），欠款查询接口返回来的值加起来就是客户欠的钱假设为A，然后调试算接口，
			 * 还款金额填欠款查询接口里得到的值，还款类型选择NM,将试算出来的结果应归还的钱（除了费用假设为C）
			 * 加起来假设为B和欠款接口查询的总额A比较，如果B＞ A调主动还款接口时金额就传A，其他情况调用主动还款接口的时候金额传B+C
			 */
        } else if ("NM".equals(paymMode)) {
            /**
             * 1.调欠款查询接口，获取欠款的所有金额的和（应归还本金+应归还正常利息+应归还逾期利息+应归还复利+应归还费用）
             * 如果主动金额不等于欠款查询的所有金额的和返回还款金额不正确
             */
            HashMap<String, Object> qkMap = new HashMap<>();
            qkMap.put("LOAN_NO", loanNo);
            // 格式：{msgall={LOAN_NO=HCF-HAPA0120160320795362001, NORM_INT=0,
            // FEE_AMT=202.53, COMM_INT=0, errorCode=00000, OD_INT=0,
            // PRCP_AMT=1333.33, errorMsg=success}}
            Map<String, Object> qkResultMap = this.getQkCheck(qkMap, gateIp, token);
            logger.info("欠款查询qkResultMap==" + qkResultMap);
            Map<String, Object> msgAllMap = (HashMap<String, Object>) qkResultMap.get("msgall");
            // 应归还本金 PRCP_AMT
            String prcpAmt = String.valueOf(msgAllMap.get("PRCP_AMT"));
            // 应归还正常利息 Norm_Int
            String normInt = String.valueOf(msgAllMap.get("NORM_INT"));
            // 应归还逾期利息 OD_INT
            String odInt = String.valueOf(msgAllMap.get("OD_INT"));
            // 应归还复利 COMM_INT
            String commInt = String.valueOf(msgAllMap.get("COMM_INT"));
            // 应归还费用 FEE_AMT
            String feeAmt = String.valueOf(msgAllMap.get("FEE_AMT"));
            // 获取错误码
            String errorCode = String.valueOf(msgAllMap.get("errorCode"));
            // 获取错误信息
            String errorMsg = String.valueOf(msgAllMap.get("errorMsg"));
            logger.info(prcpAmt + "==" + normInt + "==" + odInt + "==" + commInt + "==" + feeAmt);
            if (!"00000".equals(errorCode)) {
                return fail(RestUtil.ERROR_INTERNAL_CODE, errorMsg);
            }
            // double a = Double.parseDouble(prcpAmt) +
            // Double.parseDouble(normInt) + Double.parseDouble(odInt)
            // + Double.parseDouble(commInt) + Double.parseDouble(feeAmt);

            double a = new BigDecimal(prcpAmt).add(new BigDecimal(normInt)).add(new BigDecimal(odInt))
                    .add(new BigDecimal(commInt)).add(new BigDecimal(feeAmt)).doubleValue();
            logger.info("a==" + a);
            // double money = Double.parseDouble(paymMoney);
            double money = new BigDecimal(paymMoney).doubleValue();
            if (a != money) {
                return fail(RestUtil.ERROR_INTERNAL_CODE, "还款金额不正确");
            }
            /**
             * 2再调还款试算（核算系统4.11），
             */

            HashMap<String, Object> hsHkssParmMap = new HashMap<String, Object>();
            // 借据号 LOAN_NO
            hsHkssParmMap.put("LOAN_NO", loanNo);
            // 主动还款金额 ACTV_PAY_AMT
            hsHkssParmMap.put("ACTV_PAY_AMT", paymMoney);
            // 还款类型 PAYM_MODE
            hsHkssParmMap.put("PAYM_MODE", "NM");
            /**
             * 格式：{msgall={FEE_AMT=202.53, errorCode=00000, errorMsg=,
             * ActvPayShdTryList={MX={PS_REM_PRCP=, NORM_INT=, INT_RATE=,
             * DUE_DT=, INSTM_AMT=, OD_INT_RATE=, PERD_NO=, PRCP_AMT=}},
             * PaymentShdList={MX={DUE_DT=2016-04-12, SETL_COMM_OD_INT=0,
             * PROD_INT_AMT=0, PROD_COMM_INT_AMT=0, PS_INT_RATE=0, PERD_NO=1,
             * PS_REM_PRCP=14666.67, NORM_INT=0, INSTM_AMT=1333.33, COMM_INT=0,
             * PROD_PRCP_AMT=0, SETL_NORM_INT=0, SETL_PRCP=1333.33,
             * PS_OD_INT_RATE=0, SETL_OD_INT_AMT=0, OD_INT=0,
             * PRCP_AMT=1333.33}}, LOAN_NO=HCF-HAPA0120160320795362001,
             * ACTV_PRCP=0, NORM_INT=0, COMM_INT=0, REL_PERD_CNT=0,
             * ACTV_NORM_INT=0, OD_INT=0, PRCP_AMT=1333.33}}
             **/
            Map<String, Object> hsHkssMap = this.getZdhkSs(hsHkssParmMap, gateIp, token);
            logger.info("hsHkssMap==" + hsHkssMap);
            Map<String, Object> hsMsgAllMap = (HashMap<String, Object>) hsHkssMap.get("msgall");
            // 应归还本金 PRCP_AMT
            String hs_prcpAmt = String.valueOf(hsMsgAllMap.get("PRCP_AMT"));
            // 应归还正常利息 Norm_Int
            String hs_normInt = String.valueOf(hsMsgAllMap.get("NORM_INT"));
            // 应归还逾期利息 OD_INT
            String hs_odInt = String.valueOf(hsMsgAllMap.get("OD_INT"));
            // 应归还复利 COMM_INT
            String hs_commInt = String.valueOf(hsMsgAllMap.get("COMM_INT"));
            // 应归还费用 FEE_AMT
            String hs_feeAmt = String.valueOf(hsMsgAllMap.get("FEE_AMT"));
            /**
             * 欠款查询接口返回来的值加起来就是客户欠的钱假设为A，然后调试算接口，
             * 还款金额填欠款查询接口里得到的值，还款类型选择NM,将试算出来的结果应归还的钱（除了费用假设为C）
             * 加起来假设为B和欠款接口查询的总额A比较，如果B＞ A调主动还款接口时金额就传A，其他情况调用主动还款接口的时候金额传B+C
             */
            // double b = Double.parseDouble(hs_prcpAmt) +
            // Double.parseDouble(hs_normInt) + Double.parseDouble(hs_odInt)
            // + Double.parseDouble(hs_commInt);
            double b = new BigDecimal(hs_prcpAmt).add(new BigDecimal(hs_normInt)).add(new BigDecimal(hs_odInt))
                    .add(new BigDecimal(hs_commInt)).doubleValue();
            // double c = Double.parseDouble(hs_feeAmt);
            double c = new BigDecimal(hs_feeAmt).doubleValue();
            if (b > a) {
                zdhhFee = a;
            } else {
                zdhhFee = new BigDecimal(b).add(new BigDecimal(c)).doubleValue();
            }
            logger.info("NM  zdhhFee==" + zdhhFee);

        } else if ("ER".equals(paymMode)) {
            /**
             * 1、先调欠款查询接口，如果有欠款，则不允许提前还款 2、调核算还款试算接口4.11， 3、调主动还款接口
             */
            HashMap<String, Object> qkMap = new HashMap<String, Object>();
            qkMap.put("LOAN_NO", loanNo);
            // 格式：{msgall={LOAN_NO=HCF-HAPA0120160320795362001, NORM_INT=0,
            // FEE_AMT=202.53, COMM_INT=0, errorCode=00000, OD_INT=0,
            // PRCP_AMT=1333.33, errorMsg=success}}
            Map<String, Object> qkResultMap = this.getQkCheck(qkMap, gateIp, token);
            logger.info("qkResultMap==" + qkResultMap);
            Map<String, Object> msgAllMap = (HashMap<String, Object>) qkResultMap.get("msgall");
            // 应归还本金 PRCP_AMT
            String prcpAmt = String.valueOf(msgAllMap.get("PRCP_AMT"));
            // 应归还正常利息 Norm_Int
            String normInt = String.valueOf(msgAllMap.get("NORM_INT"));
            // 应归还逾期利息 OD_INT
            String odInt = String.valueOf(msgAllMap.get("OD_INT"));
            // 应归还复利 COMM_INT
            String commInt = String.valueOf(msgAllMap.get("COMM_INT"));
            // 应归还费用 FEE_AMT
            String feeAmt = String.valueOf(msgAllMap.get("FEE_AMT"));
            // 获取错误码
            String errorCode = String.valueOf(msgAllMap.get("errorCode"));
            // 获取错误信息
            String errorMsg = String.valueOf(msgAllMap.get("errorMsg"));
            logger.info(prcpAmt + "==" + normInt + "==" + odInt + "==" + commInt + "==" + feeAmt);
            if (!"00000".equals(errorCode)) {
                return fail(RestUtil.ERROR_INTERNAL_CODE, errorMsg);
            }
            // double a = Double.parseDouble(prcpAmt) +
            // Double.parseDouble(normInt) + Double.parseDouble(odInt)
            // + Double.parseDouble(commInt) + Double.parseDouble(feeAmt);
            double a = new BigDecimal(prcpAmt).add(new BigDecimal(normInt)).add(new BigDecimal(odInt))
                    .add(new BigDecimal(commInt)).add(new BigDecimal(feeAmt)).doubleValue();
            logger.info("a==" + a);
            // double money = Double.parseDouble(paymMoney);
            double money = new BigDecimal(paymMoney).doubleValue();
            if (a != 0) {
                return fail(RestUtil.ERROR_INTERNAL_CODE, "您好，您有欠款不允许做提前还款,如有疑问请联系400-018-7777详询。");
            }
            /** 调核算4.11接口进行试算 **/
            HashMap<String, Object> hsHkssParmMap = new HashMap<String, Object>();
            // 借据号 LOAN_NO
            hsHkssParmMap.put("LOAN_NO", loanNo);
            // 主动还款金额 ACTV_PAY_AMT
            hsHkssParmMap.put("ACTV_PAY_AMT", paymMoney);
            // 还款类型 PAYM_MODE
            hsHkssParmMap.put("PAYM_MODE", "ER");
            /**
             * 格式：{msgall={FEE_AMT=202.53, errorCode=00000, errorMsg=,
             * ActvPayShdTryList={MX={PS_REM_PRCP=, NORM_INT=, INT_RATE=,
             * DUE_DT=, INSTM_AMT=, OD_INT_RATE=, PERD_NO=, PRCP_AMT=}},
             * PaymentShdList={MX={DUE_DT=2016-04-12, SETL_COMM_OD_INT=0,
             * PROD_INT_AMT=0, PROD_COMM_INT_AMT=0, PS_INT_RATE=0, PERD_NO=1,
             * PS_REM_PRCP=14666.67, NORM_INT=0, INSTM_AMT=1333.33, COMM_INT=0,
             * PROD_PRCP_AMT=0, SETL_NORM_INT=0, SETL_PRCP=1333.33,
             * PS_OD_INT_RATE=0, SETL_OD_INT_AMT=0, OD_INT=0,
             * PRCP_AMT=1333.33}}, LOAN_NO=HCF-HAPA0120160320795362001,
             * ACTV_PRCP=0, NORM_INT=0, COMM_INT=0, REL_PERD_CNT=0,
             * ACTV_NORM_INT=0, OD_INT=0, PRCP_AMT=1333.33}}
             **/
            Map<String, Object> hsHkssMap = this.getZdhkSs(hsHkssParmMap, gateIp, token);
            logger.info("hsHkssMap==" + hsHkssMap);
            Map<String, Object> hsMsgAllMap = (HashMap<String, Object>) hsHkssMap.get("msgall");
            // 错误码 PRCP_AMT
            String code = String.valueOf(hsMsgAllMap.get("errorCode"));
            if (!"00000".equals(code)) {
                return hsHkssMap;
            }
            // 应归还本金 PRCP_AMT
            String hs_prcpAmt = String.valueOf(hsMsgAllMap.get("PRCP_AMT"));
            // 应归还正常利息 Norm_Int
            String hs_normInt = String.valueOf(hsMsgAllMap.get("NORM_INT"));
            // 应归还逾期利息 OD_INT
            String hs_odInt = String.valueOf(hsMsgAllMap.get("OD_INT"));
            // 应归还复利 COMM_INT
            String hs_commInt = String.valueOf(hsMsgAllMap.get("COMM_INT"));
            // 应归还费用 FEE_AMT
            String hs_feeAmt = String.valueOf(hsMsgAllMap.get("FEE_AMT"));
            // 主动还款利息 ACTV_NORM_INT
            String hs_actvNormInt = String.valueOf(hsMsgAllMap.get("ACTV_NORM_INT"));
            // 主动还款本金 ACTV_PRCP
            String hs_actvPrcp = String.valueOf(hsMsgAllMap.get("ACTV_PRCP"));

            /**

             */
            // double b = Double.parseDouble(hs_prcpAmt) +
            // Double.parseDouble(hs_normInt) + Double.parseDouble(hs_odInt)
            // + Double.parseDouble(hs_commInt) +
            // Double.parseDouble(hs_actvNormInt)
            // + Double.parseDouble(hs_actvPrcp);
            // double c = Double.parseDouble(hs_feeAmt);
            // zdhhFee = b + c;
            zdhhFee = new BigDecimal(hs_prcpAmt).add(new BigDecimal(hs_normInt)).add(new BigDecimal(hs_odInt))
                    .add(new BigDecimal(hs_commInt)).add(new BigDecimal(hs_feeAmt)).add(new BigDecimal(hs_actvPrcp))
                    .add(new BigDecimal(hs_actvNormInt)).doubleValue();
            logger.info("ER 原始zdhhFee==" + zdhhFee);
            // 调主动还款接口，得出最终的还款金额
            HashMap<String, Object> zzhkParmMap = new HashMap<String, Object>();
            // loanNo 借据号
            zzhkParmMap.put("loanNo", loanNo);
            // usr_cde 登录用户名
            zzhkParmMap.put("usr_cde", "admin");
            // setlMode 还款模式
            zzhkParmMap.put("setlMode", paymMode);
            // payMoney 主动还款金额
            //  zzhkParmMap.put("payMoney", zdhhFee);
            zzhkParmMap.put("payMoney", "0");
            zzhkParmMap.put("reserved4", paymMoney);
            Map<String, Object> zzhmMap = this.getZdhk(zzhkParmMap, token);
            logger.info("zzhmMap==" + zzhmMap);
            Map<String, Object> zzhmResponseMap = (HashMap<String, Object>) zzhmMap.get("response");
            Map<String, Object> zzhmHeadMap = (HashMap<String, Object>) zzhmResponseMap.get("head");
            String retMsg = String.valueOf(zzhmHeadMap.get("retMsg"));
            String retFlag = String.valueOf(zzhmHeadMap.get("retFlag"));
            logger.info("retFlag==" + retFlag + ";retMsg==" + retMsg);
            if ("20009".equals(retFlag)) {
                JSONObject json = new JSONObject(retMsg);
                logger.info("20009模式下的json:" + json);
                double payMoney = json.getDouble("payMoney");
                logger.info("20009模式下的还款金额:" + payMoney);
                zdhhFee = payMoney;
            } else {
                return fail(retFlag, retMsg);
            }

        } else {
            return fail("99", "还款模式参数有误！");
        }
        HashMap<String, Object> hm = new HashMap<String, Object>();
        hm.put("zdhkFee", zdhhFee);//实际主动还款总金额
        hm.put("zdhkBj", new BigDecimal(paymMoney).setScale(2, BigDecimal.ROUND_HALF_UP));//主动还款本金=用户输入的金额
        hm.put("zdhkXf", new BigDecimal(zdhhFee).subtract(new BigDecimal(paymMoney))
                .setScale(2, BigDecimal.ROUND_HALF_DOWN));//主动还款息费
        return success(hm);
    }

    /**
     * 欠款查询
     *
     * @param map
     * @param gateIp
     * @param token
     * @return
     */
    public Map<String, Object> getQkCheck(Map<String, Object> map, String gateIp, String token) {
        map.put("serviceId", CmisTradeCode.TRADECODE_HSSYS_QKCHECK);
        //封装成核算接口的报文格式
        HashMap sendMap = new HashMap();
        sendMap.put("msgbody", map);

        Map<String, Object> resultMap = HttpUtil
                .restPostMap(EurekaServer.CMISFRONTSERVER + "/pub/cmiscore", token, sendMap); //直接调
        //Map<String, Object> resultMap = CmisUtil.getHxxdResponse(gateIp + "/pub/cmiscore", token, map); //走路由
        logger.info("核算欠款查询返回resultMap==" + resultMap);
        Map<String, Object> msgallMap = (HashMap<String, Object>) resultMap.get("msgall");
        String errorCode = String.valueOf(msgallMap.get("errorCode"));
        if ("00000".equals(errorCode)) {
            // OD_AMT=OD_INT+ COMM_INT
            String odInt = String.valueOf(msgallMap.get("OD_INT"));
            String commInt = String.valueOf(msgallMap.get("COMM_INT"));
            //double odAmt = Double.parseDouble(odInt) + Double.parseDouble(commInt);
            double odAmt = new BigDecimal(odInt).add(new BigDecimal(commInt)).doubleValue();
            msgallMap.put("OD_AMT", odAmt);
            HashMap<String, Object> returnMap = new HashMap<String, Object>();
            returnMap.put("msgall", msgallMap);
            return returnMap;
        } else {
            msgallMap.put("NORM_INT", 0);
            msgallMap.put("FEE_AMT", 0);
            msgallMap.put("COMM_INT", 0);
            msgallMap.put("errorCode", "00000");
            msgallMap.put("OD_INT", 0);
            msgallMap.put("PRCP_AMT", 0);
            msgallMap.put("errorMsg", "success");
            msgallMap.put("OD_AMT", 0);
            HashMap<String, Object> returnMap = new HashMap<String, Object>();
            returnMap.put("msgall", msgallMap);
            return returnMap;
        }
    }

    /**
     * * 描述： 主动还款模式 (3.58 主动还款 )
     * </p>
     *
     * @param map
     * @param map
     * @param token
     * @return
     * @date 2016年4月13日
     * @author 尹君
     */
    public Map<String, Object> getZdhk(HashMap<String, Object> map, String token) {

        map.put("paymInd", "N");// --paymInd 传N 表示未到账
        if (StringUtils.isEmpty(map.get("payMoney"))) {
            return fail(RestUtil.ERROR_INTERNAL_CODE, "还款金额不可为空！");
        }
        map.put("reserved2", map.get("payMoney"));
        return CmisUtil.getCmisResponse(CmisTradeCode.TRADECODE_ZZHK, token, map);
    }

    /**
     * 4.11.主动还款试算(信贷管理与核算系统接口)
     *
     * @param map
     * @param gateIp
     * @param token
     * @return
     */
    private Map<String, Object> getZdhkSs(@RequestBody Map<String, Object> map, String gateIp, String token) {
        map.put("serviceId", CmisTradeCode.TRADECODE_HSSYS_ZDHKSS);
        //封装成核算接口的报文格式
        HashMap sendMap = new HashMap();
        sendMap.put("msgbody", map);

        return HttpUtil.restPostMap(EurekaServer.CMISFRONTSERVER + "/pub/cmiscore", token, sendMap);
    }

    /**
     * 对从crm查询到的客户扩展信息中的地址进行预处理
     *
     * @param crmBodyMap
     */
    public void dealAddress(Map<String, Object> crmBodyMap) {//1 渠道进件  2共同还款人 3额度申请 用
        //户籍地址
        // crm: regLiveInd(Y  N) --户籍地址是否同现住房
        // crm 户籍：regProvince   regCity  regArea  regAddr  现住房：liveProvince  regCity
        //获取户籍地址的标志位
        String regLiveInd = String.valueOf(crmBodyMap.get("regLiveInd"));
        if (StringUtils.isEmpty(regLiveInd)) {
            regLiveInd = "Y";
            crmBodyMap.put("regLiveInd", regLiveInd);
        }
        //若同现住房，则将户籍地址的省市代码赋值到户籍地址上
        if ("Y".equals(regLiveInd)) {
            crmBodyMap.put("regProvince", crmBodyMap.get("liveProvince"));
            crmBodyMap.put("regCity", crmBodyMap.get("liveCity"));
            crmBodyMap.put("regArea", crmBodyMap.get("liveArea"));
            crmBodyMap.put("regAddr", crmBodyMap.get("liveAddr"));
            logger.info("处理后的户籍地址省市区及详细地址为：" + crmBodyMap.get("regProvince") + "  " + crmBodyMap.get("regCity") + "  "
                    + crmBodyMap.get("regArea") + "  " + crmBodyMap.get("regAddr"));
        }

        //获取房产地址标识位： 10、同现住房地址 20、同自有房产地址  30、其他（30不做处理，接口中就有数据）
        //pptyProvince pptyCity pptyArea  pptyAddr
        //获取房产地址标志位
        String pptyLiveInd = String.valueOf(crmBodyMap.get("pptyLiveInd"));
        if (StringUtils.isEmpty(pptyLiveInd)) {
            // pptyLiveInd = "10";
            pptyLiveInd = "Y";
            crmBodyMap.put("pptyLiveInd", pptyLiveInd);
        }
        if ("Y".equals(pptyLiveInd)) {
            crmBodyMap.put("pptyProvince", crmBodyMap.get("liveProvince"));
            crmBodyMap.put("pptyCity", crmBodyMap.get("liveCity"));
            crmBodyMap.put("pptyArea", crmBodyMap.get("liveArea"));
            crmBodyMap.put("pptyAddr", crmBodyMap.get("liveAddr"));
            logger.info("处理后的房产地址省市区及详细地址为：" + crmBodyMap.get("pptyProvince") + "  " + crmBodyMap.get("pptyCity") + "  "
                    + crmBodyMap.get("pptyArea") + "  " + crmBodyMap.get("pptyAddr"));
        }/*else if("20".equals(pptyLiveInd)){
                crmBodyMap.put("pptyProvince",crmBodyMap.get(""));
				crmBodyMap.put("pptyCity",crmBodyMap.get(""));
				crmBodyMap.put("pptyArea",crmBodyMap.get(""));
				crmBodyMap.put("pptyAddr",crmBodyMap.get(""));
		}*/

        //邮寄地址（送货地址）mail_opt mail_province  mail_city  mail_area   mail_addr
        //邮寄地址 A：现住房地址   B：现单位住址   O：其他地址(O不做处理，接口中就有值)

        String postQtInd = String.valueOf(crmBodyMap.get("postQtInd"));
        if ("A".equals(postQtInd)) {
            crmBodyMap.put("postProvince", crmBodyMap.get("liveProvince"));
            crmBodyMap.put("postCity", crmBodyMap.get("liveCity"));
            crmBodyMap.put("postArea", crmBodyMap.get("liveArea"));
            crmBodyMap.put("postAddr", crmBodyMap.get("liveAddr"));
            logger.info("处理后的邮寄地址省市区及详细地址为：" + crmBodyMap.get("postProvince") + "  " + crmBodyMap.get("postCity") + "  "
                    + crmBodyMap.get("postArea") + "  " + crmBodyMap.get("postAddr"));
        } else if ("B".equals(postQtInd)) {
            crmBodyMap.put("postProvince", crmBodyMap.get("officeProvince"));
            crmBodyMap.put("postCity", crmBodyMap.get("officeCity"));
            crmBodyMap.put("postArea", crmBodyMap.get("officeArea"));
            crmBodyMap.put("postAddr", crmBodyMap.get("officeAddr"));
            logger.info("处理后的邮寄地址省市区及详细地址为：" + crmBodyMap.get("postProvince") + "  " + crmBodyMap.get("postCity") + "  "
                    + crmBodyMap.get("postArea") + "  " + crmBodyMap.get("postAddr"));
        }

    }

    /**
     * 渠道进件前，处理订单的送货地址
     *
     * @param order      要处理的订单
     * @param crmBodyMap crm查询出来的信息
     */
    public void dealDeliverAddress(AppOrder order, Map<String, Object> crmBodyMap) {
        //获取送货地址选项
        String deliverAddrTyp = order.getDeliverAddrTyp();
        if (Objects.equals("A", deliverAddrTyp)) {
            order.setDeliverProvince(StringUtils.isEmpty(crmBodyMap.get("liveProvince")) ?
                    null :
                    String.valueOf(crmBodyMap.get("liveProvince")));
            order.setDeliverCity(StringUtils.isEmpty(crmBodyMap.get("liveCity")) ?
                    null :
                    String.valueOf(crmBodyMap.get("liveCity")));
            order.setDeliverArea(StringUtils.isEmpty(crmBodyMap.get("liveArea")) ?
                    null :
                    String.valueOf(crmBodyMap.get("liveArea")));
            order.setDeliverAddr(StringUtils.isEmpty(crmBodyMap.get("liveAddr")) ?
                    null :
                    String.valueOf(crmBodyMap.get("liveAddr")));
        } else if (Objects.equals("B", deliverAddrTyp)) {
            order.setDeliverProvince(StringUtils.isEmpty(crmBodyMap.get("officeProvince")) ?
                    null :
                    String.valueOf(crmBodyMap.get("officeProvince")));
            order.setDeliverCity(StringUtils.isEmpty(crmBodyMap.get("officeCity")) ?
                    null :
                    String.valueOf(crmBodyMap.get("officeCity")));
            order.setDeliverArea(StringUtils.isEmpty(crmBodyMap.get("officeArea")) ?
                    null :
                    String.valueOf(crmBodyMap.get("officeArea")));
            order.setDeliverAddr(StringUtils.isEmpty(crmBodyMap.get("officeAddr")) ?
                    null :
                    String.valueOf(crmBodyMap.get("officeAddr")));
        }
  /*      //如果送货地址为空，则处理成O其他
        if(StringUtils.isEmpty(deliverAddrTyp)){
            order.setDeliverAddrTyp("O");
        }*/

    }

    /**
     * 額度查詢service
     *
     * @param idTyp
     * @param idNo
     * @param token
     * @return
     */
    public Map<String, Object> getEdCheck(String idTyp, String idNo, String token) {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("idTyp", idTyp);
        map.put("idNo", idNo);
        Map<String, Object> result = CmisUtil.getCmisResponse(CmisTradeCode.TRADECODE_ED_CHECK, token, map);
        /**
         * { "response": { "head": { "retMsg": "交易成功！", "retFlag": "00000" },
         * "body": { "crdComUsedAmt": 6001, "crdComAvailAmt": 0, "crdComAmt":
         * 10000, "crdNorUsedAmt": 6001, "crdAmt": 10000, "crdNorAvailAmt": 0,
         * "crdSts": 30, "crdNorAmt": 10000, "contDt": "2018-03-19" } } }
         */
        logger.info("信贷100016返回" + result);
        if (result == null) {
            return fail(RestUtil.ERROR_INTERNAL_CODE, "信贷系统额度查询失败！");
        }
        HashMap<String, Object> responseMap = (HashMap<String, Object>) result.get("response");
        if (!CmisUtil.getIsSucceed(result)) {
            return fail("99", CmisUtil.getErrMsg(result));
        }
        HashMap<String, Object> bodyMap = (HashMap<String, Object>) responseMap.get("body");
        Double crdComAvailAmt = Double.valueOf(bodyMap.get("crdComAvailAmt").toString());
        Double crdNorAvailAmt = Double.valueOf(bodyMap.get("crdNorAvailAmt").toString());
        Double crdAmt = new BigDecimal(crdNorAvailAmt).add(new BigDecimal(crdComAvailAmt)).doubleValue();
        // 额度状态
        Integer crdSts = (Integer) bodyMap.get("crdSts");
        HashMap remap = new HashMap<String, Object>();
        // 剩余额度
        remap.put("surplusAmt", crdAmt);
        remap.put("crdSts", crdSts);
        ////////////////////////
        remap.put("crdAmt", bodyMap.get("crdAmt"));
        remap.put("crdComAmt", bodyMap.get("crdComAmt"));
        remap.put("crdComUsedAmt", bodyMap.get("crdComUsedAmt"));
        remap.put("crdComAvailAnt", bodyMap.get("crdComAvailAmt"));
        remap.put("crdNorAmt", bodyMap.get("crdNorAmt"));
        remap.put("crdNorUsedAmt", bodyMap.get("crdNorUsedAmt"));
        remap.put("crdNorAvailAmt", bodyMap.get("crdNorAvailAmt"));
        remap.put("contDt", bodyMap.get("contDt"));

        /**
         * 增加预授信额度字段
         */
        remap.put("haierCredit", "0");//统一返回0
        /**预授信额度10.30版本暂时不查了
         //crm48、(GET)根据身份证号查询客户基本信息的userId(APP)
         String userUrl = getGateUrl() + "/app/crm/cust/getCustInfoByCertNo?certNo=" + idNo;
         logger.info("CRM48 请求url" + userUrl);
         Map<String, Object> userMap = HttpUtil.restGetMap(userUrl);
         logger.info("CRM48 返回" + userMap);
         if (RestUtil.isSuccess(userMap)) {
         Map<String, Object> userBody = (Map<String, Object>) userMap.get("body");
         String userId = String.valueOf(userBody.get("userId"));
         //根据userId查询客户实名信息
         Map<String, Object> userInfoMap = this.getSmrzInfoByUserId(userId);
         logger.info("通过userId查询的实名认证信息返回：" + userInfoMap);
         JSONObject custHeadObject = (JSONObject) userInfoMap.get("head");
         String retFlag = custHeadObject.getString("retFlag");
         if (!"00000".equals(retFlag)) {
         logger.error("实名认证信息查询失败！未知的实名信息！授信额度为0，返回的错误信息为：" + userInfoMap);
         // return fail("83", "客户实名信息查询失败！");
         remap.put("haierCredit", "0");
         } else {
         Map<String, Object> custBodyMap = HttpUtil.json2Map(userInfoMap.get("body").toString());
         //客户编号
         String cuNo = String.valueOf(custBodyMap.get("custNo"));
         //姓名
         String name = String.valueOf(custBodyMap.get("custName"));
         //身份证号
         String id = String.valueOf(custBodyMap.get("certNo"));
         String mobile = String.valueOf(custBodyMap.get("mobile"));
         Map<String, Object> isPassMap = appOrderService.getCustIsPassFromCrm(name, idNo, mobile);
         logger.info("CRM准入资格方法返回：" + isPassMap);
         JSONObject isPassHead = (JSONObject) isPassMap.get("head");
         String isPassRetFlag = isPassHead.getString("retFlag");
         String isPassRetMsg = isPassHead.getString("retMsg");
         if (!"00000".equals(isPassRetFlag)) {
         logger.error("CRM28接口调用返回异常信息，原因： " + isPassMap);
         return fail(isPassRetFlag, isPassRetMsg);//返回crm的错误码
         }
         JSONObject isPassBodyMap = (JSONObject) isPassMap.get("body");
         //  级别对应的授信额度
         String credit = String.valueOf(isPassBodyMap.get("haierCredit"));
         String isPass=String.valueOf(isPassBodyMap.get("isPass"));
         String level=String.valueOf(isPassBodyMap.get("level"));
         remap.put("isPass", isPass);
         remap.put("level", level);
         logger.debug("credit==" + credit);
         if ("null".equals(credit) || StringUtils.isEmpty(credit)) {
         logger.info("CRM28查询到该用户级别对应的授信额度为空，返回0");
         remap.put("haierCredit", "0");
         } else {
         logger.info("该用户级别对应的授信额度为" + credit);
         remap.put("haierCredit", credit);
         }
         }
         } else {
         logger.error("CRM48 根据身份证号查询客户基本信息的userId查询失败 返回的错误信息为：" + userMap);
         remap.put("haierCredit", "0");
         // return fail(RestUtil.ERROR_INTERNAL_CODE, RestUtil.ERROR_INTERNAL_MSG);
         }
         **/
        return success(remap);
    }

    /**
     * 风险信息合法性校验.
     */
    public Map<String, Object> riskInfoCheck(Map<String, Object> map) {
        String idNo = (String) map.get("idNo");
        if (StringUtils.isEmpty(idNo)) {
            return super.fail(ConstUtil.ERROR_MES_CODE_EMPTY_CODE, "身份证号" + ConstUtil.ERROR_MES_CODE_EMPTY_MSG);
        }
        String name = (String) map.get("name");
        if (StringUtils.isEmpty(name)) {
            return super.fail(ConstUtil.ERROR_MES_CODE_EMPTY_CODE, "姓名" + ConstUtil.ERROR_MES_CODE_EMPTY_MSG);
        }
        String mobile = (String) map.get("mobile");
        if (StringUtils.isEmpty(mobile)) {
            return super.fail(ConstUtil.ERROR_MES_CODE_EMPTY_CODE, "手机号" + ConstUtil.ERROR_MES_CODE_EMPTY_MSG);
        }
        String dataTyp = (String) map.get("dataTyp");
        if (StringUtils.isEmpty(dataTyp)) {
            return super.fail(ConstUtil.ERROR_MES_CODE_EMPTY_CODE, "数据类型" + ConstUtil.ERROR_MES_CODE_EMPTY_MSG);
        }
        String source = (String) map.get("source");
        if (StringUtils.isEmpty(source)) {
            return super.fail(ConstUtil.ERROR_MES_CODE_EMPTY_CODE, "数据来源" + ConstUtil.ERROR_MES_CODE_EMPTY_MSG);
        }
        List<Object> content = (List) map.get("content");
        if (content == null || content.size() == 0) {
            return super.fail(ConstUtil.ERROR_MES_CODE_EMPTY_CODE, "信息列表(内容)" + ConstUtil.ERROR_MES_CODE_EMPTY_MSG);
        }

        // 由实时上送风险信息改为异步上送。
        AppCmisInfo appCmisInfo = new AppCmisInfo();
        appCmisInfo.setFlag("0");
        appCmisInfo.setInsertTime(new Date());
        appCmisInfo.setRequestMap(map);
        appCmisInfo.setTradeCode(CmisTradeCode.TRADECODE_WWRISK);
        appCmisInfo.setId(UUID.randomUUID().toString().replace("-", ""));
        appCmisInfoRepository.save(appCmisInfo);

        return super.success("插入成功", Collections.EMPTY_MAP);

    }

    /**
     * 外围风险信息收集
     *
     * @param map
     * @return
     */
    public Map<String, Object> updateRiskInfo(Map<String, Object> map) {
        String idNo = (String) map.get("idNo");
        if (StringUtils.isEmpty(idNo)) {
            return super.fail(ConstUtil.ERROR_MES_CODE_EMPTY_CODE, "身份证号" + ConstUtil.ERROR_MES_CODE_EMPTY_MSG);
        }
        idNo = EncryptUtil.simpleDecrypt(idNo);
        String name = (String) map.get("name");
        if (StringUtils.isEmpty(name)) {
            return super.fail(ConstUtil.ERROR_MES_CODE_EMPTY_CODE, "姓名" + ConstUtil.ERROR_MES_CODE_EMPTY_MSG);
        }
        name = EncryptUtil.simpleDecrypt(name);
        String mobile = (String) map.get("mobile");
        if (StringUtils.isEmpty(mobile)) {
            return super.fail(ConstUtil.ERROR_MES_CODE_EMPTY_CODE, "手机号" + ConstUtil.ERROR_MES_CODE_EMPTY_MSG);
        }
        mobile = EncryptUtil.simpleDecrypt(mobile);
        String dataTyp = (String) map.get("dataTyp");
        if (StringUtils.isEmpty(dataTyp)) {
            return super.fail(ConstUtil.ERROR_MES_CODE_EMPTY_CODE, "数据类型" + ConstUtil.ERROR_MES_CODE_EMPTY_MSG);
        }
        String source = (String) map.get("source");
        if (StringUtils.isEmpty(source)) {
            return super.fail(ConstUtil.ERROR_MES_CODE_EMPTY_CODE, "数据来源" + ConstUtil.ERROR_MES_CODE_EMPTY_MSG);
        }
        List<Object> content = (List) map.get("content");
        if (content == null || content.size() == 0) {
            return super.fail(ConstUtil.ERROR_MES_CODE_EMPTY_CODE, "信息列表(内容)" + ConstUtil.ERROR_MES_CODE_EMPTY_MSG);
        }

        HashMap<String, Object> cmisMap = new HashMap<String, Object>();
        cmisMap.put("custName", name);
        cmisMap.put("mobileNo", mobile);
        cmisMap.put("idTyp", "20");
        cmisMap.put("idNo", idNo);
        cmisMap.put("dataTyp", dataTyp);
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        cmisMap.put("dataDt", format.format(new Date()));
        cmisMap.put("sysId", "1".equals(source) ? "13" : "04");

        Map<String, Object> listMap = new HashMap<>();

        List<Map<String, Object>> dataList = new ArrayList<>();
        for (Object data : content) {
            // 如果为美凯龙借款事件或摩羯事件，则上传为List<Map<String, Object>>
            if (!StringUtils.isEmpty(map.get("channel")) && (("antifraud_lend".equals(map.get("reserved7"))
                    || "A601".equals(dataTyp) || "A602".equals(dataTyp)))) {
                List<Map<String, Object>> info = (List<Map<String, Object>>) data;
                for (Map<String, Object> infoMap : info) {
                    Map<String, Object> dataMap = new HashMap<>();
                    dataMap.put("dataTyp", dataTyp);
                    dataMap.put("content", EncryptUtil.simpleDecrypt((String) infoMap.get("content")));// 数据解密
                    dataMap.put("reserved6", StringUtils.isEmpty(map.get("applSeq")) ? "" : map.get("applSeq"));
                    if (!StringUtils.isEmpty(infoMap.get("reserved7"))) {
                        dataMap.put("reserved7",
                                StringUtils.isEmpty(infoMap.get("reserved7")) ? "" : infoMap.get("reserved7"));
                    }
                    dataMap.put("remark3", map.get("remark3"));
                    dataList.add(dataMap);
                }
                break;
            } else {
                Map<String, Object> dataMap = new HashMap<>();
                dataMap.put("dataTyp", dataTyp);
                dataMap.put("content", EncryptUtil.simpleDecrypt(String.valueOf(data)));// 数据解密
                dataMap.put("reserved6", StringUtils.isEmpty(map.get("applSeq")) ? "" : map.get("applSeq"));
                if (!StringUtils.isEmpty(map.get("reserved7"))) {
                    dataMap.put("reserved7", StringUtils.isEmpty(map.get("reserved7")) ? "" : map.get("reserved7"));
                }
                dataList.add(dataMap);
            }
        }

        listMap.put("info", dataList);
        cmisMap.put("list", listMap);

        if (!StringUtils.isEmpty(map.get("channel"))) {
            cmisMap.put("sysFlag", map.get("channel"));
        }
        if (!StringUtils.isEmpty(map.get("channelNo"))) {
            cmisMap.put("channelNo", map.get("channelNo"));
        }

        logger.info("cmisMap=" + cmisMap);
        Map<String, Object> riskInfoResult = new CmisController().updateRiskInfo(cmisMap);
        logger.info("riskInfoResult=" + riskInfoResult);
        return riskInfoResult;

    }

    /**
     * 红星美凯龙营销人员信息通知到信贷
     *
     * @param order 必需属性：idNo,promCde,promPhone
     * @return
     */
    public Map<String, Object> updateRedStarRiskInfo(AppOrder order) {
        if (order == null) {
            return fail("73", "传入订单为空");
        }
        logger.info("传入order：" + order);
        String idNo = order.getIdNo();
        if (StringUtils.isEmpty(idNo)) {
            return fail("74", "订单证件号码不能为空");
        }

        if (StringUtils.isEmpty(order.getPromCde()) && StringUtils.isEmpty(order.getPromPhone()) && StringUtils
                .isEmpty(order.getPromDesc())) {
            return fail("75", "营销人员信息均为空");
        }

        String idTyp = order.getIdTyp();
        idTyp = StringUtils.isEmpty(idTyp) ? "20" : idTyp;

        // A04 红星美凯龙APP
        String dataTyp = "A04";
        String dataDt = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        // sysId 即 sysFlag,星巢贷 sysFlag 固定为16
        String sysId = "16";

        HashMap<String, Object> sendMap = new HashMap<>();
        sendMap.put("idTyp", idTyp);
        sendMap.put("idNo", idNo);
        sendMap.put("dataTyp", dataTyp);
        sendMap.put("dataDt", dataDt);
        sendMap.put("sysId", sysId);
        // 非必须字段
        if (!StringUtils.isEmpty(order.getCustName())) {
            sendMap.put("custName", order.getCustName());
        }
        if (!StringUtils.isEmpty(order.getIndivMobile())) {
            sendMap.put("mobileNo", order.getIndivMobile());
        }
        // 营销人员信息
        List<Map<String, Object>> info = new ArrayList<>();

        if (!StringUtils.isEmpty(order.getPromCde())) {
            Map<String, Object> promCdeMap = new HashMap<>();
            promCdeMap.put("dataTyp", "A041");
            promCdeMap.put("content", order.getPromCde());
            promCdeMap.put("reserved6", order.getApplSeq());
            info.add(promCdeMap);
        }

        if (!StringUtils.isEmpty(order.getPromDesc())) {
            Map<String, Object> promDescMap = new HashMap<>();
            promDescMap.put("dataTyp", "A042");
            promDescMap.put("content", StringUtils.isEmpty(order.getPromDesc()) ? "" : order.getPromDesc());
            promDescMap.put("reserved6", order.getApplSeq());
            info.add(promDescMap);
        }

        if (!StringUtils.isEmpty(order.getPromPhone())) {
            Map<String, Object> promPhoneMap = new HashMap<>();
            promPhoneMap.put("dataTyp", "A043");
            promPhoneMap.put("content", order.getPromPhone());
            promPhoneMap.put("reserved6", order.getApplSeq());
            info.add(promPhoneMap);
        }

        Map<String, Object> listMap = new HashMap<>();
        listMap.put("info", info);
        sendMap.put("list", listMap);
        logger.info("sendMap:" + sendMap);
        //Map<String, Object> resultMap = new CmisController().updateRiskInfo(sendMap);
        // 重新设置请求头
        Map<String, Object> map = new HashedMap();
        if ("16".equals(order.getSource())) {
            map.put("sysFlag", order.getSource());
        }
        HashMap<String, Object> headMap = CmisUtil.makeHeadMap(CmisTradeCode.TRADECODE_WWRISK, "", map);
        headMap.put("sysFlag", "16");
        headMap.put("channelNo", "31");
        // body
        HashMap<String, Object> bodyMap = CmisUtil.makeBodyMap(sendMap);

        // 封装完整请求：
        HashMap<String, Object> requestMap = new HashMap<String, Object>();
        requestMap.put("head", headMap);
        requestMap.put("body", bodyMap);
        HashMap<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("request", requestMap);

        // 把null全部替换成空字符串，避免信贷接口报错
        for (String key : bodyMap.keySet()) {
            if (bodyMap.get(key) == null) {
                bodyMap.put(key, "");
            }
        }
        logger.info("营销人员通知请求参数：\n" + new JSONObject(paramMap));

        Map<String, Object> result = HttpUtil
                .restPostMap(EurekaServer.CMISFRONTSERVER + "/pub/cmisfront", getToken(), paramMap);
        logger.info("营销人员通知返回结果:\n" + new JSONObject(result));
        return result;
    }

    /**
     * 录单校验之通过客户编号查询实名信息系
     *
     * @param custNo
     * @return
     */
    private Map<String, Object> getSmrzInfoByCustNo(String custNo) {
        //扫码录单
        //1、根据客户编号，查询客户信息
        String smrzUrl = EurekaServer.CRM + "/app/crm/cust/queryCustRealInfoByCustNo?custNo="
                + custNo;
        String smrzJson = HttpUtil.restGet(smrzUrl);
        logger.info("根据客户编号查询实名认证信息返回：smrzResult==" + smrzJson);
        Map<String, Object> smrzMap = HttpUtil
                .json2Map(smrzJson.replace("\"null\"", "\"\"").replaceAll("null", "\"\""));
        return smrzMap;
    }

    /**
     * 录单校验之通过userId查询实名信息系
     *
     * @param userId
     * @return
     */
    public Map<String, Object> getSmrzInfoByUserId(String userId) {
        //扫码录单
        //1、根据客户编号，查询客户信息
        String smrzUrl = EurekaServer.CRM + "/app/crm/cust/queryPerCustInfo?userId="
                + userId;
        String smrzJson = HttpUtil.restGet(smrzUrl);
        logger.info("根据用户Id查询实名认证信息返回：smrzResult==" + smrzJson);
        Map<String, Object> smrzMap = HttpUtil.json2Map(smrzJson);
        return smrzMap;
    }

    /**
     * 录单校验之通过客户姓名及身份证号查询实名信息
     *
     * @param custName
     * @param idNo
     * @return
     */
    public Map<String, Object> getSmrzInfoByCustNameAndIdNo(String custName, String idNo) {
        String cust_url = EurekaServer.CRM + "/app/crm/cust/queryMerchCustInfo?certNo=" + idNo + "&custName="
                + custName;
        logger.info("CRM 实名认证接口请求地址：" + cust_url);
        String cust_json = HttpUtil.restGet(cust_url, super.getToken());
        logger.info("CRM 实名认证接口请求地址返回：" + cust_json);
        if (StringUtils.isEmpty(cust_json)) {
            logger.info("CRM  该订单的实名认证信息接口查询失败，返回异常！");
        }
        Map<String, Object> custMap = HttpUtil.json2Map(cust_json);
        return custMap;
    }

    /**
     * 查询未实名认证客户是否存在有效邀请原因 CRM 31
     *
     * @param custName 姓名
     * @param idNo     身份证号
     * @param mobile   手机号
     * @return
     */
    private Map<String, Object> getCustISExistsInvitedCauseTag(String custName, String idNo, String mobile) {
        String url = EurekaServer.CRM + "/app/crm/cust/getCustISExistsInvitedCauseTag?custName=" + custName + "&certNo="
                + idNo + "&phonenumber=" + mobile;
        logger.info("CRM 31【查询未实名认证客户是否存在有效邀请原因】接口请求地址：" + url);
        String json = HttpUtil.restGet(url, super.getToken());
        if (StringUtils.isEmpty(json)) {
            logger.info("CRM  31【查询未实名认证客户是否存在有效邀请原因】接口返回空，返回异常！");
        }
        Map<String, Object> custMap = HttpUtil.json2Map(json);
        return custMap;
    }

    /**
     * 根据客户编号查询邀请原因 CRM 55
     *
     * @param custNo 姓名编号
     * @return
     */
    private Map<String, Object> getCustISExistsInvitedCauseTag(String custNo) {
        String url = EurekaServer.CRM + "/app/crm/cust/getInvitedCustByCustNo?custNo=" + custNo;
        logger.info("CRM 58【根据客户编号查询邀请原因】接口请求地址：" + url);
        String json = HttpUtil.restGet(url, super.getToken());
        if (StringUtils.isEmpty(json)) {
            logger.info("CRM  58【根据客户编号查询邀请原因】接口返回空，返回异常！");
        }
        Map<String, Object> custMap = HttpUtil.json2Map(json);
        return custMap;
    }

    /**
     * 录单校验接口service
     *
     * @param custNo
     * @param custName
     * @param idNo
     * @return
     */
    public Map<String, Object> getCustInfoAndEdInfo(String custNo, String custName, String idNo, String paramMobile) {
        if (StringUtils.isEmpty(custNo) && (StringUtils.isEmpty(custName) || StringUtils.isEmpty(idNo))) {
            return fail("80", "客户编号为空或者客户姓名及身份证号有为空！");
        }
        //返回结果map
        HashMap<String, Object> resultMap = new HashMap<String, Object>();

        //扫码录单录单 (客户编号不为空时，姓名、身份证号参数自动忽略)
        if (!StringUtils.isEmpty(custNo)) {
            Map<String, Object> smrzMap = this.getSmrzInfoByCustNo(custNo);

            //若存在客户实名信息，封装客户信息
            if (HttpUtil.isSuccess(smrzMap)) {
                if (smrzMap.get("body") != null) {
                    JSONObject smrz = (JSONObject) smrzMap.get("body");
                    String cuNo = smrz.getString("custNo");
                    String name = smrz.getString("custName");
                    String id = smrz.getString("certNo");
                    String cardNo = smrz.getString("cardNo");
                    String bdmobile = appOrderService.getBindMobileByCustNameAndIdNo(name, id, super.getToken());
                    if (StringUtils.isEmpty(bdmobile)) {
                        bdmobile = smrz.getString("mobile");
                    }
                    String bankCode = smrz.getString("acctBankNo");
                    String bankName = smrz.getString("acctBankName");
                    String acctProvince = smrz.getString("acctProvince");//开户省
                    String acctCity = smrz.getString("acctCity");//开户市
                    String acctArea = smrz.getString("acctArea");//开户区
                    resultMap.put("custNo", cuNo);
                    resultMap.put("custName", name);
                    resultMap.put("idNo", id);
                    resultMap.put("bankCode", bankCode);
                    resultMap.put("bankName", bankName);
                    resultMap.put("bdMobile", bdmobile);
                    resultMap.put("cardNo", cardNo);
                    resultMap.put("acctProvince", acctProvince);
                    resultMap.put("acctCity", acctCity);
                    resultMap.put("acctArea", acctArea);
                    //10月13日，此处加入判断是否准入的校验---此处校验准入的手机号参数使用的是绑定的手机号
                    //扫码录单 无论是否传手机号，均校验准入资格，传入手机号为空，则用实名认证手机号
                    String checkMobile = paramMobile;
                    if (StringUtils.isEmpty(paramMobile)) {
                        checkMobile = smrz.getString("mobile");
                    }
                    Map<String, Object> isPassMap = appOrderService.getCustIsPassFromCrm(name, id, checkMobile);
                    logger.info("准入资格方法返回：" + isPassMap);
                    JSONObject isPasshead = (JSONObject) isPassMap.get("head");
                    String retFlag = isPasshead.getString("retFlag");
                    String retMsg = isPasshead.getString("retMsg");
                    if (!"00000".equals(retFlag)) {
                        return fail(retFlag, retMsg);//返回crm的错误码
                    }
                    JSONObject bodyMap = (JSONObject) isPassMap.get("body");
                    String isPass = String.valueOf(bodyMap.get("isPass"));
                    if ("-1".equals(isPass)) {
                        logger.info("准入资格校验失败，失败原因：" + isPassMap);
                        return fail("86", "准入资格校验失败！");
                    }

                    //2、查询额度
                    Map<String, Object> edCheckMap = this.getEdCheck("20", id, super.getToken());
                    logger.info("额度查询结果返回：" + edCheckMap);
                    ResultHead head = (ResultHead) edCheckMap.get("head");
                    String flag = head.getRetFlag();
                    String msg = head.getRetMsg();
                    //3、如果客户有额度，检查额度是否已冻结！
                    if (Objects.equals(flag, "00000")) {
                        Map<String, Object> edCheckBody = (Map<String, Object>) edCheckMap.get("body");
                        //受托支付可用额度金额
                        String crdComAvailAnt = String.valueOf(edCheckBody.get("crdComAvailAnt"));
                        //自主支付可用额度金额
                        String crdNorAvailAmt = String.valueOf(edCheckBody.get("crdNorAvailAmt"));
                        //额度状态
                        String crdSts = String.valueOf(edCheckBody.get("crdSts"));
                        resultMap.put("crdComAvailAnt", Objects.equals("null", crdComAvailAnt) ? "0" : crdComAvailAnt);
                        resultMap.put("crdNorAvailAmt", Objects.equals("null", crdNorAvailAmt) ? "0" : crdNorAvailAmt);
                        resultMap.put("crdSts", crdSts);
                        //3、判断是否有额度
                        if ("20".equals(crdSts)) {
                            return fail("82", "您有额度，但额度已经被冻结，不允许贷款！");
                        }
                        return success(resultMap);
                    } else {
                        //没有额度，查询是否可发起申请
                        //4如果没有额度，检查是否允许发起额度申请
                        String url = EurekaServer.CMISPROXY + "/api/appl/ifEdAppl?idNo=" + idNo;
                        logger.info("查询是否允许发起额度申请：CMIS==>url:" + url);
                        String json = HttpUtil.restGet(url, super.getToken());
                        logger.info("查询是否允许发起额度申请：CMIS==>返回结果:" + json);
                        if (StringUtils.isEmpty(json)) {
                            logger.error("否允许发起额度申请查询失败！");
                            return fail("84", RestUtil.ERROR_INTERNAL_MSG);
                        } else {
                            Map<String, Object> map = HttpUtil.json2Map(json);
                            String ifEdFlag = String.valueOf(map.get("flag"));
                            if ("N".equals(ifEdFlag)) {
                                logger.info("您有在途的额度申请加支用信息,不允许贷款！");
                                return fail("85", "您有在途的额度申请加支用信息,不允许贷款！");
                            } else if ("Y".equals(ifEdFlag)) {
                                logger.info("该客户没有在途的额度申请加支用，允许其贷款！");
                                //没有额度，都为0,额度状态为0
                                resultMap.put("crdComAvailAnt", "0");
                                resultMap.put("crdNorAvailAmt", "0");
                                resultMap.put("crdSts", "00");
                                return success(resultMap);
                            } else {
                                logger.error("是否允许发起额度申请接口返回异常！==》未知错误！");
                                return fail("84", RestUtil.ERROR_INTERNAL_MSG);
                            }
                        }
                    }
                } else {
                    logger.error("客户实名信息返回异常，body体为null");
                    return fail("83", "客户实名信息查询失败！");
                }

            } else {
                return fail("83", "客户实名信息查询失败！");
            }
        } else if ((!StringUtils.isEmpty(idNo)) && (!(StringUtils.isEmpty(custName)))) {
            //正常录单（根据姓名、身份证号进行录单）
            //1、身份证号校验  b为true则校验通过，反之，则校验失败
            boolean b = IdcardUtils.validateCard(idNo);
            if (!b) {
                return fail("81", "身份证号格式不正确，校验失败！");
            }
            //10月13日，此处加入判断是否准入的校验---此处校验准入的手机号参数使用的是绑定的手机号
            //正常录单--如果手机号不传，则不判断准入资格
            //if (!StringUtils.isEmpty(paramMobile)) {
            Map<String, Object> isPassMap = appOrderService.getCustIsPassFromCrm(custName, idNo, paramMobile);
            logger.info("准入资格方法返回：" + isPassMap);
            JSONObject isPasshead = (JSONObject) isPassMap.get("head");
            String zrRetFlag = isPasshead.getString("retFlag");
            String zrRetMsg = isPasshead.getString("retMsg");
            if (!"00000".equals(zrRetFlag)) {
                return fail(zrRetFlag, zrRetMsg);//返回crm的错误码
            }
            JSONObject bodyMap = (JSONObject) isPassMap.get("body");
            String isPass = String.valueOf(bodyMap.get("isPass"));
            if ("-1".equals(isPass)) {
                logger.info("准入资格校验失败，失败原因：" + isPassMap);
                return fail("86", "准入资格校验失败！");
            }
            // }
            //2、查询额度
            Map<String, Object> edCheckMap = this.getEdCheck("20", idNo, super.getToken());
            logger.info("额度查询结果返回：" + edCheckMap);
            ResultHead head = (ResultHead) edCheckMap.get("head");
            String flag = head.getRetFlag();
            String msg = head.getRetMsg();
            //3、如果客户有额度，检查额度是否已冻结！
            if (Objects.equals(flag, "00000")) {
                Map<String, Object> edCheckBody = (Map<String, Object>) edCheckMap.get("body");
                //受托支付可用额度金额
                String crdComAvailAnt = String.valueOf(edCheckBody.get("crdComAvailAnt"));
                //自主支付可用额度金额
                String crdNorAvailAmt = String.valueOf(edCheckBody.get("crdNorAvailAmt"));
                //额度状态
                String crdSts = String.valueOf(edCheckBody.get("crdSts"));
                resultMap.put("crdComAvailAnt", Objects.equals("null", crdComAvailAnt) ? "0" : crdComAvailAnt);
                resultMap.put("crdNorAvailAmt", Objects.equals("null", crdNorAvailAmt) ? "0" : crdNorAvailAmt);
                resultMap.put("crdSts", crdSts);
                //3、判断是否有额度
                if ("20".equals(crdSts)) {
                    return fail("82", "您有额度，但额度已经被冻结，不允许贷款！");
                }
            } else {
                //没有额度，查询是否可发起申请
                //4如果没有额度，检查是否允许发起额度申请
                String url = EurekaServer.CMISPROXY + "/api/appl/ifEdAppl?idNo=" + idNo;
                logger.info("查询是否允许发起额度申请：CMIS==>url:" + url);
                String json = HttpUtil.restGet(url, super.getToken());
                logger.info("查询是否允许发起额度申请：CMIS==>返回结果:" + json);
                if (StringUtils.isEmpty(json)) {
                    return fail("10", "查询失败");
                } else {
                    Map<String, Object> map = HttpUtil.json2Map(json);
                    String ifEdFlag = String.valueOf(map.get("flag"));
                    if ("N".equals(ifEdFlag)) {
                        logger.info("您有在途的额度申请加支用信息,不允许贷款！");
                        return fail("85", "您有在途的额度申请加支用信息,不允许贷款！");
                    } else if ("Y".equals(ifEdFlag)) {
                        logger.info("该客户没有在途的额度申请加支用，将查询客户的信息！");
                        //额度状态
                        //没有额度，都为0,额度状态为0
                        resultMap.put("crdComAvailAnt", "0");
                        resultMap.put("crdNorAvailAmt", "0");
                        resultMap.put("crdSts", "00");
                    }
                }
            }
            //查询客户信息
            //若客户姓名和身份证号不为空，
            Map<String, Object> custMap = getSmrzInfoByCustNameAndIdNo(custName, idNo);
            logger.info("CRM 实名认证（17或13）接口返回custMap==" + custMap);
            JSONObject custHeadObject = (JSONObject) custMap.get("head");
            String retFlag = custHeadObject.getString("retFlag");
            if (!"00000".equals(retFlag)) {
                logger.info("实名认证信息查询失败！未知的实名信息如手机号、银行代码等将返回空！");
                resultMap.put("custNo", "");
                resultMap.put("custName", custName);
                resultMap.put("idNo", idNo);
                resultMap.put("bankCode", "");
                resultMap.put("bankName", "");
                resultMap.put("bdMobile", "");
                resultMap.put("cardNo", "");
                resultMap.put("acctProvince", "");
                resultMap.put("acctCity", "");
                resultMap.put("acctArea", "");
                return success(resultMap);
            } else {
                Map<String, Object> custBodyMap = HttpUtil.json2Map(custMap.get("body").toString());
                //客户编号
                String cuNo = String.valueOf(custBodyMap.get("custNo"));
                //姓名
                String name = String.valueOf(custBodyMap.get("custName"));
                //身份证号
                String id = String.valueOf(custBodyMap.get("certNo"));
                //银行代码
                String bankCode = String.valueOf(custBodyMap.get("acctBankNo"));
                //银行名称
                String bankName = String.valueOf(custBodyMap.get("acctBankName"));
                String cardNo = String.valueOf(custBodyMap.get("cardNo"));
                //绑定手机号
                String bdMobile = appOrderService.getBindMobileByCustNameAndIdNo(name, id, super.getToken());
                if (StringUtils.isEmpty(bdMobile)) {
                    //绑定手机号为空，则使用实名认证手机号
                    bdMobile = String.valueOf(custBodyMap.get("mobile"));
                }
                //开户省
                String acctProvince = String.valueOf(custBodyMap.get("acctProvince"));
                //开户市
                String acctCity = String.valueOf(custBodyMap.get("acctCity"));
                //开户区
                String acctArea = String.valueOf(custBodyMap.get("acctArea"));
                resultMap.put("custNo", cuNo);
                resultMap.put("custName", name);
                resultMap.put("idNo", id);
                resultMap.put("bankCode", bankCode);
                resultMap.put("bankName", bankName);
                resultMap.put("bdMobile", bdMobile);
                resultMap.put("cardNo", cardNo);
                resultMap.put("acctProvince", acctProvince);
                resultMap.put("acctCity", acctCity);
                resultMap.put("acctArea", acctArea);
                return success(resultMap);
            }
        } else {
            return fail("80", "参数异常，客户编号为空且客户姓名和身份证号有为空！");
        }
    }

    public Map<String, Object> getCustInfoAndEdInfoPerson(String userId, String provinceCode, String cityCode,
                                                          String typLevelTwo, String channel) {
        if (StringUtils.isEmpty(userId)) {
            return fail("80", "用户id为空值");
        }
        HashMap<String, Object> resultMap = new HashMap<String, Object>();//返回的resultMap
        Map<String, Object> smrzMap = this.getSmrzInfoByUserId(userId);
        logger.info("通过userId查询的实名认证信息返回：" + smrzMap);
        if (smrzMap == null) {
            logger.error("实名认证信息查询失败！");
            return fail("83", "客户实名信息查询失败！");
        }
        JSONObject custHeadObject = (JSONObject) smrzMap.get("head");
        String retFlag = custHeadObject.getString("retFlag");
        if (!"00000".equals(retFlag)) {
            logger.info("实名认证信息查询失败！未知的实名信息如手机号、银行代码等将返回空！");
            return fail("83", "客户实名信息查询失败！");
        } else {
            Map<String, Object> custBodyMap = HttpUtil.json2Map(smrzMap.get("body").toString());
            //客户编号
            String cuNo = String.valueOf(custBodyMap.get("custNo"));
            //姓名
            String name = String.valueOf(custBodyMap.get("custName"));
            //身份证号
            String id = String.valueOf(custBodyMap.get("certNo"));
            //银行代码
            String bankCode = String.valueOf(custBodyMap.get("acctBankNo"));
            //银行名称
            String bankName = String.valueOf(custBodyMap.get("acctBankName"));
            //绑定手机号
            String bdMobile = appOrderService.getBindMobileByCustNameAndIdNo(name, id, super.getToken());
            if (StringUtils.isEmpty(bdMobile)) {
                //绑定手机号为空，则使用实名认证手机号
                bdMobile = String.valueOf(custBodyMap.get("mobile"));
            }
            /**
             证件类型	certType
             银行卡号	cardNo
             账户名	acctName
             账号	acctNo
             开户省	acctProvince
             开户市	acctCity
             开户区	acctArea
             开户机构代码	accBchCde
             开户机构名称	accBchName
             人脸识别结果	faceValue
             人脸识别次数	faceCount
             */
            String certType = String.valueOf(custBodyMap.get("certType"));
            String cardNo = String.valueOf(custBodyMap.get("cardNo"));
            String acctName = String.valueOf(custBodyMap.get("acctName"));
            String acctProvince = String.valueOf(custBodyMap.get("acctProvince"));
            String acctCity = String.valueOf(custBodyMap.get("acctCity"));
            String acctArea = String.valueOf(custBodyMap.get("acctArea"));
            String accBchCde = String.valueOf(custBodyMap.get("accBchCde"));
            String accBchName = String.valueOf(custBodyMap.get("accBchName"));
            //todo 人脸识别结果需自行比较(人脸阈值获取personFaceService.getFaceThreshold)
            String faceValue = String.valueOf(custBodyMap.get("faceValue"));
            String faceCount = String.valueOf(custBodyMap.get("faceCount"));
            String dataFrom = String.valueOf(custBodyMap.get("dataFrom"));
            String createDt = String.valueOf(custBodyMap.get("createDt"));
            String passStandard = String.valueOf(custBodyMap.get("passStandard"));
            resultMap.put("custNo", cuNo);
            resultMap.put("custName", name);
            resultMap.put("idNo", id);
            resultMap.put("bankCode", bankCode);
            resultMap.put("bankName", bankName);
            resultMap.put("bdMobile", bdMobile);
            resultMap.put("certType", certType);
            resultMap.put("cardNo", cardNo);
            resultMap.put("acctName", acctName);
            resultMap.put("acctProvince", acctProvince);
            resultMap.put("acctCity", acctCity);
            resultMap.put("acctArea", acctArea);
            resultMap.put("accBchCde", accBchCde);
            resultMap.put("accBchName", accBchName);
            resultMap.put("faceValue", faceValue);
            resultMap.put("faceCount", faceCount);
            resultMap.put("dataFrom", dataFrom);
            resultMap.put("createDt", createDt);
            resultMap.put("passStandard", passStandard);

            //2、查询额度
            Map<String, Object> edCheckMap = this.getEdCheck("20", id, super.getToken());
            logger.info("额度查询结果返回：" + edCheckMap);
            ResultHead head = (ResultHead) edCheckMap.get("head");
            String flag = head.getRetFlag();
            String msg = head.getRetMsg();

            //3、如果客户有额度，检查额度是否已冻结！
            if (Objects.equals(flag, "00000")) {
                if ("16".equals(channel) || "34".equals(channel)) {//星巢贷 集团大数据
                    //edHistory是否进行过额度申请字段 Y:申请过 N：未申请过
                    resultMap.put("edHistory", "Y");
                }
                Map<String, Object> edCheckBody = (Map<String, Object>) edCheckMap.get("body");
                //受托支付可用额度金额
                String crdComAvailAnt = String.valueOf(edCheckBody.get("crdComAvailAnt"));
                //自主支付可用额度金额
                String crdNorAvailAmt = String.valueOf(edCheckBody.get("crdNorAvailAmt"));
                //额度状态
                String crdSts = String.valueOf(edCheckBody.get("crdSts"));
                resultMap.put("crdComAvailAnt", Objects.equals("null", crdComAvailAnt) ? "0" : crdComAvailAnt);
                resultMap.put("crdNorAvailAmt", Objects.equals("null", crdNorAvailAmt) ? "0" : crdNorAvailAmt);
                resultMap.put("crdSts", crdSts);
                //3、判断是否有额度
                if ("20".equals(crdSts)) {
                    return fail("82", "您有额度，但额度已经被冻结，不允许贷款！");
                }
                //return success(resultMap);
            } else {
                //没有额度，查询是否可发起申请
                if ("16".equals(channel) || "34".equals(channel)) {//星巢贷 集团大数据
                    //edHistory是否进行过额度申请字段 Y:申请过 N：未申请过
                    resultMap.put("edHistory", "N");
                }
                //4如果没有额度，检查是否允许发起额度申请
                String url = EurekaServer.CMISPROXY + "/api/appl/ifEdAppl?idNo=" + id;
                logger.info("查询是否允许发起额度申请：CMIS==>url:" + url);
                String json = HttpUtil.restGet(url, super.getToken());
                logger.info("查询是否允许发起额度申请：CMIS==>返回结果:" + json);
                if (StringUtils.isEmpty(json)) {
                    logger.error("否允许发起额度申请查询失败！");
                    return fail("84", RestUtil.ERROR_INTERNAL_MSG);
                } else {
                    Map<String, Object> map = HttpUtil.json2Map(json);
                    String ifEdFlag = String.valueOf(map.get("flag"));
                    if ("N".equals(ifEdFlag)) {
                        logger.info("您有在途的额度申请加支用信息,不允许贷款！");
                        Map<String, Object> edMap = new HashMap<>();
                        edMap.put("outSts", map.get("outSts"));
                        edMap.put("crdSeq", map.get("crdSeq"));
                        return fail("85", "您有在途的额度申请加支用信息,不允许贷款！", edMap);
                    } else if ("Y".equals(ifEdFlag)) {
                        logger.info("该客户没有在途的额度申请加支用，允许其贷款！");
                        //没有额度，都为0,额度状态为0
                        resultMap.put("crdComAvailAnt", "0");
                        resultMap.put("crdNorAvailAmt", "0");
                        resultMap.put("crdSts", "00");
                    } else {
                        logger.error("是否允许发起额度申请接口返回异常！==》未知错误！");
                        return fail("84", RestUtil.ERROR_INTERNAL_MSG);
                    }
                }
            }
            //星巢贷客户全部为社会化人员，校验crm准入资格限制,同时走社会化人员处理。
            logger.info("录单校验channel:" + channel);
            boolean isXCD = "16".equals(channel) ? true : false;//是否是星巢贷客户
            boolean isBigData = "34".equals(channel) ? true : false;//是否是集团大数据
            //查询准入资格
            //获取准入资格
            /**
             * 准入资格接口查询较慢，故在此处进行如下处理：
             * 由于额度查询service已经调过是否准入接口，故通过额度查询接口中取出准入资格相关信息，若额度查询service返回失败报文，则再调准入资格查询service方法
             */
            String isPassRetFlag = "";
            String isPassRetMsg = "";
            String isPass = "";
            String level = "";
            //额度查询若返回失败报文，即无额度，则通过getCustIsPassFromCrm方法获取准入资格
            // if(!Objects.equals(flag, "00000")) {
            Map<String, Object> isPassMap = appOrderService.getCustIsPassFromCrm(name, id, bdMobile);
            logger.info("准入资格方法返回：" + isPassMap);
            JSONObject isPassHead = (JSONObject) isPassMap.get("head");
            isPassRetFlag = isPassHead.getString("retFlag");
            isPassRetMsg = isPassHead.getString("retMsg");
            JSONObject isPassBodyMap = (JSONObject) isPassMap.get("body");
            //  1  -1  shh
            isPass = String.valueOf(isPassBodyMap.get("isPass"));
            //A B  ssh（空）
            level = String.valueOf(isPassBodyMap.get("level"));
            if (!"00000".equals(isPassRetFlag)) {
                return fail(isPassRetFlag, isPassRetMsg);//返回crm的错误码
            }
            /**  }else{
             //额度查询若返回5个0报文，即有额度，则通过额度查询返回的map获取准入资格
             Map<String, Object> edCheckBody = (Map<String, Object>) edCheckMap.get("body");
             isPass = String.valueOf(edCheckBody.get("isPass"));
             level = String.valueOf(edCheckBody.get("level"));
             }
             **/
            //不准入
            if ("-1".equals(isPass)) {
                logger.info("用户准入资格受限，受限原因：" + isPassRetMsg);
                return fail("86", "准入资格校验失败，贷款失败！");
            } else {
                //shh
                if ("shh".equals(isPass) || isXCD) {
                    return shhDeal(typLevelTwo, provinceCode, cityCode, cuNo, resultMap, channel);
                } else { //1  查询白名单级别，如果A则直接准入，如果B查询是否在中国
                    if ("A".equals(level)) {
                        return success(resultMap);
                    } else if ("B".equals(level)) {
                        if (isBigData) {//集团大数据开放全国
                            return success(resultMap);
                        } else {
                            if (StringUtils.isEmpty(provinceCode) || StringUtils.isEmpty(cityCode)) {
                                return fail("88", "只允许在国内贷款！");
                            } else {
                                return success(resultMap);
                            }
                        }
                    } else if (Objects.equals("C", level)) {//c返社会化一样处理
                        logger.info("准入资格为C，返回结果同SHH");
                        return shhDeal(typLevelTwo, provinceCode, cityCode, cuNo, resultMap, channel);
                    } else {
                        logger.info("异常的白名单准入级别==》" + level);
                        return fail("89", "白名单级别查询异常");
                    }
                }
            }

        }
    }

    /**
     * 社会化人员处理
     *
     * @param typLevelTwo
     * @param provinceCode
     * @param cityCode
     * @param custNo
     * @param resultMap
     * @return
     */
    public Map<String, Object> shhDeal(String typLevelTwo, String provinceCode, String cityCode, String custNo,
                                       Map resultMap, String channel) {

        logger.info("===社会化人员处理 channel:" + channel);
        //查询邀请原因
        //  Map<String, Object> reasonMap = this.getCustISExistsInvitedCauseTag(name, id, String.valueOf(custBodyMap.get("mobile")));
        Map<String, Object> reasonMap = this.getCustISExistsInvitedCauseTag(custNo);
        logger.info("CRM 58【查询未实名认证客户是否存在有效邀请原因】返回：" + reasonMap);
        JSONObject reasonHeadObject = (JSONObject) reasonMap.get("head");
        String reasonRetFlag = reasonHeadObject.getString("retFlag");
        String reasonRetMsg = reasonHeadObject.getString("retMsg");
        if ("00000".equals(reasonRetFlag)) {
            ArrayList reasonBodyObject = (ArrayList) reasonMap.get("body");
            int length = reasonBodyObject.size();
            //  1  -1  shh
            //  String isExits = String.valueOf(reasonBodyObject.get("isExits"));
            //   if ("Y".equals(isExits)) {
            if (length > 0) {
                return success(resultMap);
            } else {
                if (!StringUtils.isEmpty(typLevelTwo)) {
                    if (!"16".equals(channel)) {
                        channel = "0";
                    }
                    /*List<CityBean> list = cityRepository.findByAdmitByTypLevelTwo("1", typLevelTwo, channel);
                    for (CityBean cityBean : list) {
                        if ("Y".equals(cityBean.getIsChina())) {
                            logger.info("==允许全国==");
                            return success(resultMap);
                        } else {
                            String myProvinceCode = cityBean.getProvinceCode();
                            String myCityCode = cityBean.getCityCode();
                            if (StringUtils.isEmpty(myCityCode)) {//开通全省
                                if (Objects.equals(myProvinceCode, provinceCode)) {
                                    logger.info("==允许全省==" + myProvinceCode);
                                    return success(resultMap);
                                }
                            } else {//开通具体某个城市
                                if (Objects.equals(myProvinceCode, provinceCode) && Objects
                                        .equals(myCityCode, cityCode)) {
                                    logger.info("==允许某个城市==" + myCityCode);
                                    return success(resultMap);
                                }
                            }

                        }
                    }*/
                    if(StringUtils.isEmpty(provinceCode) || StringUtils.isEmpty(cityCode)){
                        return fail("87", "省市编码不能为空");
                    }
                    List<CityBean> list = cityRepository.findByProvinceAndCity(provinceCode,cityCode,typLevelTwo, channel);
                    if(list.size()>0){
                        CityBean cityBean = list.get(0);
                        String admit = cityBean.getAdmit();
                        if ("1".equals(admit)) {
                            logger.info("==允许某个城市==" + cityBean.getCityCode());
                            return success(resultMap);
                        }else{
                            return fail("87", "所在城市未开通服务");
                        }
                    }
                    List<CityBean> provincelist = cityRepository.findByProvince(provinceCode,typLevelTwo, channel);
                    if(provincelist.size()>0){
                        CityBean cityBean = provincelist.get(0);
                        String admit = cityBean.getAdmit();
                        if ("1".equals(admit)) {
                            logger.info("==允许全省==" + cityBean.getProvinceCode());
                            return success(resultMap);
                        }else{
                            return fail("87", "所在城市未开通服务");
                        }
                    }
                    List<CityBean> admitlist = cityRepository.findByAdmitByTypLevelTwo("1", typLevelTwo, channel);
                    for (CityBean cityBean : admitlist) {
                        if ("Y".equals(cityBean.getIsChina())) {
                            logger.info("==允许全国==");
                            return success(resultMap);
                        }
                    }
                    return fail("87", "所在城市未开通服务");
                } else {
                    //查询城市范围
                    if (!"16".equals(channel)) {
                        channel = "0";
                    }
                    List<CityBean> list = cityRepository.findByAdmit("1", channel);
                    for (CityBean cityBean : list) {
                        if ("Y".equals(cityBean.getIsChina())) {
                            logger.info("==允许全国==");
                            return success(resultMap);
                        } else {
                            String myProvinceCode = cityBean.getProvinceCode();
                            String myCityCode = cityBean.getCityCode();
                            if (StringUtils.isEmpty(myCityCode)) {//开通全省
                                if (Objects.equals(myProvinceCode, provinceCode)) {
                                    logger.info("==允许全省==" + myProvinceCode);
                                    return success(resultMap);
                                }
                            } else {//开通具体某个城市
                                if (Objects.equals(myProvinceCode, provinceCode) && Objects
                                        .equals(myCityCode, cityCode)) {
                                    logger.info("==允许某个城市==" + myCityCode);
                                    return success(resultMap);
                                }
                            }
                        }
                    }
                    return fail("87", "所在城市未开通服务");
                }
            }
        } else {
            return fail(reasonRetFlag, reasonRetMsg);
        }
    }

    /**
     * 额度申请校验接口整合service
     *
     * @param idNo
     * @param idTyp
     * @param userId
     * @return
     */
    public Map<String, Object> checkEdAppl(String idNo, String idTyp, String userId) {
        //返回结果map
        HashMap<String, Object> resMap = new HashMap<String, Object>();
        //（1）实名认证查询app/crm/cust/queryPerCustInfo
        Map<String, Object> smrzMap = this.getSmrzInfoByUserId(userId);
        logger.info("通过userId查询的实名认证信息返回：" + smrzMap);
        JSONObject custHeadObject = (JSONObject) smrzMap.get("head");
        String retFlag = custHeadObject.getString("retFlag");
        if (!"00000".equals(retFlag)) {
            logger.error("实名认证信息查询失败！未知的实名信息！返回的错误信息为：" + smrzMap);
            return fail("83", "客户实名信息查询失败！");
        } else {
            Map<String, Object> custBodyMap = HttpUtil.json2Map(smrzMap.get("body").toString());
            //客户编号
            String custNo = String.valueOf(custBodyMap.get("custNo"));
            //姓名
            String custname = String.valueOf(custBodyMap.get("custName"));
            //身份证号
            String certNo = String.valueOf(custBodyMap.get("certNo"));
            String mobile = String.valueOf(custBodyMap.get("mobile"));
            String acctBankName = String.valueOf(custBodyMap.get("acctBankName"));
            String cardNo = String.valueOf(custBodyMap.get("cardNo"));
            String acctBankNo = String.valueOf(custBodyMap.get("acctBankNo"));
            String acctCity = String.valueOf(custBodyMap.get("acctCity"));
            String acctProvince = String.valueOf(custBodyMap.get("acctProvince"));
            String certType = String.valueOf(custBodyMap.get("certType"));
            resMap.put("custname", custname);
            resMap.put("certNo", certNo);
            resMap.put("mobile", mobile);
            resMap.put("acctBankName", acctBankName);
            resMap.put("cardNo", cardNo);
            resMap.put("acctBankNo", acctBankNo);
            resMap.put("acctCity", acctCity);
            resMap.put("acctProvince", acctProvince);
            resMap.put("custNo", custNo);
            resMap.put("certType", certType);
        }

        // 获取额度期望值
        String urlCredit = EurekaServer.CMISPROXY + "/api/appl/getExpectCredit?idNo=" + idNo;
        String jsonCredit = HttpUtil.restGet(urlCredit, super.getToken());
        if (StringUtils.isEmpty(jsonCredit)) {
            logger.error("查询用户期望额度值失败,失败原因：" + jsonCredit);
            return fail("84", RestUtil.ERROR_INTERNAL_MSG);
        } else {
            Map<String, Object> map = HttpUtil.json2Map(jsonCredit);
            logger.info("用户额度查新结果：" + idNo + "--" + map.get("expectCredit").toString());
            resMap.put("expectCredit", map.get("expectCredit").toString());
        }
        /**
         //（2）CRM64 查询银行卡信息/app/crm/cust/getBankInfo (cardType) 银行卡类型
         String cardInfoUrl = getGateUrl() + "/app/crm/cust/getBankInfo?cardNo="
         + resMap.get("cardNo");
         String cardInfoJson = HttpUtil.restGet(cardInfoUrl);
         logger.info("CRM64 根据银行卡号查询银行卡信息请求url:"+cardInfoUrl);
         logger.info("CRM64 根据银行卡号查询银行卡信息返回：cardInfoJson==" + cardInfoJson);
         Map<String, Object> cardInfoMap = HttpUtil.json2Map(cardInfoJson);
         JSONObject cardInfoHeadObject = (JSONObject) cardInfoMap.get("head");
         String cardRetFlag = cardInfoHeadObject.getString("retFlag");
         if (!"00000".equals(cardRetFlag)) {
         logger.error("CRM64 根据银行卡号查询银行卡信息接口返回异常信息！原因：" + cardInfoMap);
         return RestUtil.fail(cardRetFlag, cardInfoHeadObject.getString("retMsg"));
         } else {
         Map<String, Object> cardInfoBodyMap = HttpUtil.json2Map(cardInfoMap.get("body").toString());
         //银行卡类型
         String cardType = String.valueOf(cardInfoBodyMap.get("cardType"));
         resMap.put("cardType", cardType);
         }
         **/
        // （3） 额度查询app/appserver/getEdCheck
        //如果身份证号未传，使用实名认证中的身份证号
        if (StringUtils.isEmpty(idNo)) {
            idNo = String.valueOf(resMap.get("certNo"));
        }
        //如果证件类型未传，使用实名认证查询出来的证件类型，如果实名认证查询的证件类型为空，则默认传20（身份证）
        if (StringUtils.isEmpty(idTyp)) {
            idTyp = String.valueOf(resMap.get("certType"));
            if (StringUtils.isEmpty(idTyp) || "null".equals(idTyp)) {
                idTyp = "20";
            }
        }
        Map<String, Object> edCheckMap = this.getEdCheck(idTyp, idNo, super.getToken());
        logger.info("额度查询结果返回：" + edCheckMap);
        ResultHead head = (ResultHead) edCheckMap.get("head");
        String flag = head.getRetFlag();
        String msg = head.getRetMsg();
        if (Objects.equals(flag, "00000")) {
            //（crdAmt、crdNorAmt、crdComAvailAnt、crdNorAvailAmt、crdComAmt、crdSts、 ）
            Map<String, Object> edCheckBody = (Map<String, Object>) edCheckMap.get("body");
            //受托支付可用额度金额
            String crdComAvailAnt = String.valueOf(edCheckBody.get("crdComAvailAnt"));
            //自主支付可用额度金额
            String crdNorAvailAmt = String.valueOf(edCheckBody.get("crdNorAvailAmt"));
            //额度状态
            String crdSts = String.valueOf(edCheckBody.get("crdSts"));
            String crdComAmt = String.valueOf(edCheckBody.get("crdComAmt"));
            String crdAmt = String.valueOf(edCheckBody.get("crdAmt"));
            String crdNorAmt = String.valueOf(edCheckBody.get("crdNorAmt"));
            resMap.put("crdComAvailAnt", crdComAvailAnt);
            resMap.put("crdNorAvailAmt", crdNorAvailAmt);
            resMap.put("crdSts", crdSts);
            resMap.put("crdComAmt", crdComAmt);
            resMap.put("crdAmt", crdAmt);
            resMap.put("crdNorAmt", crdNorAmt);
            resMap.put("outSts", "");//状态为空
            resMap.put("flag", "");
            resMap.put("crdSeq", "");
            resMap.put("applType", "");
            resMap.put("outStsName", "");
            HashMap<String, Object> parmMap = new HashMap<String, Object>();
            parmMap.put("idNo", idNo);
            parmMap.put("idTyp", "20");
            resMap.put("rejectRsn", "");
            //            Map<String, Object> progressMap = this.getEdApplProgress(parmMap);
            //            logger.info("额度审批进度查询结果：" + progressMap);
            //            ResultHead progressHead = (ResultHead) progressMap.get("head");
            //            String progressFlag = progressHead.getRetFlag();
            //            String progressMsg = progressHead.getRetMsg();
            //            if (!"00000".equals(progressFlag)) {
            //                logger.error("额度审批进度查询信息异常,相关状态不做处理，异常原因：" + progressMsg);
            //            } else {
            //                Map<String, Object> bodyMap = (HashMap<String, Object>) progressMap.get("body");
            //                String applOutSts = String.valueOf(bodyMap.get("outSts"));
            //                String applApplseq = String.valueOf(bodyMap.get("applSeq"));
            //                resMap.put("outSts", applOutSts);//
            //                resMap.put("crdSeq", applApplseq);
            //                resMap.put("flag", "Y");
            //            }
            return success(resMap);
        } else {
            logger.error("额度查询结果返回异常，异常原因为：" + edCheckMap);
            resMap.put("crdComAvailAnt", "");
            resMap.put("crdNorAvailAmt", "");
            resMap.put("crdSts", "");
            resMap.put("crdComAmt", "");
            resMap.put("crdAmt", "");
            resMap.put("crdNorAmt", "");
            // return RestUtil.fail(flag, msg);
            //  （5）查询是否可以额度申请app/appserver/cmis/ifEdAppl
            String url = EurekaServer.CMISPROXY + "/api/appl/ifEdAppl?idNo=" + idNo;
            logger.info("查询是否允许发起额度申请：CMIS==>url:" + url);
            String json = HttpUtil.restGet(url, super.getToken());
            logger.info("查询是否允许发起额度申请：CMIS==>返回结果:" + json);
            if (StringUtils.isEmpty(json)) {
                logger.error("否允许发起额度申请查询失败！失败原因：" + json);
                return fail("84", RestUtil.ERROR_INTERNAL_MSG);
            } else {
                Map<String, Object> map = HttpUtil.json2Map(json);
                String ifEdFlag = String.valueOf(map.get("flag"));
                String outSts = String.valueOf(map.get("outSts"));
                String crdSeq = String.valueOf(map.get("crdSeq"));
                String applType = String.valueOf(map.get("applType"));
                String outStsName = String.valueOf(map.get("outStsName"));
                resMap.put("flag", ifEdFlag);
                resMap.put("outSts", outSts);//状态为空
                resMap.put("crdSeq", crdSeq);
                resMap.put("applType", applType);
                resMap.put("outStsName", outStsName);
                resMap.put("rejectRsn", "");
                // if (Objects.equals("Y", ifEdFlag)) {
                HashMap<String, Object> parmMap = new HashMap<String, Object>();
                parmMap.put("idNo", idNo);
                parmMap.put("idTyp", "20");
                Map<String, Object> progressMap = this.getEdApplProgress(parmMap);
                logger.info("额度审批进度查询结果：" + progressMap);
                ResultHead progressHead = (ResultHead) progressMap.get("head");
                String progressFlag = progressHead.getRetFlag();
                String progressMsg = progressHead.getRetMsg();
                if (!"00000".equals(progressFlag)) {
                    logger.error("额度审批进度查询信息异常,相关状态不做处理，异常原因：" + progressMsg);
                } else {
                    Map<String, Object> bodyMap = (HashMap<String, Object>) progressMap.get("body");
                    String applOutSts = String.valueOf(bodyMap.get("outSts"));
                    String applApplseq = String.valueOf(bodyMap.get("applSeq"));
                    resMap.put("outSts", applOutSts);//
                    resMap.put("crdSeq", applApplseq);
                    //  resMap.put("flag", "Y");
                    String rejectRsn = "";
                    //判断是否含rejectRsn字段
                    if (bodyMap.containsKey("rejectRsn")) {
                        rejectRsn = String.valueOf(bodyMap.get("rejectRsn")).replace(" ", "");
                        String[] rejectCodes = rejectRsn.split(",");
                        if (rejectCodes.length > 0) {
                            for (String str : rejectCodes) {
                                if (Objects.equals("REVIEW100", str)) {//// TODO: 2017/2/25 补充资料的意见码，待核心提供，先以00代替
                                    resMap.put("rejectRsn", str);
                                }
                                break;
                            }
                            if (!resMap.containsKey("rejectRsn")) {
                                resMap.put("rejectRsn", "");
                            }
                        } else {
                            resMap.put("rejectRsn", "");
                        }

                    }
                }
                //   }
                //若outSts为空则查询一下最新审批进度
                //                if(StringUtils.isEmpty(outSts)){
                //                    HashMap<String,Object> parmMap=new HashMap<String,Object>();
                //                    parmMap.put("idNo",idNo);
                //                    parmMap.put("idTyp","20");
                //                    Map<String,Object> progressMap=this.getEdApplProgress(parmMap);
                //                    logger.info("额度审批进度查询结果："+progressMap);
                //                    ResultHead progressHead=(ResultHead)progressMap.get("head");
                //                    String progressFlag=progressHead.getRetFlag();
                //                    String progressMsg=progressHead.getRetMsg();
                //                    if(!"00000".equals(progressFlag)){
                //                        logger.error("额度审批进度查询信息异常,相关状态不做处理，异常原因："+progressMsg);
                //                    }else{
                //                        Map<String,Object> bodyMap=(HashMap<String,Object>)progressMap.get("body");
                //                        String applOutSts=String.valueOf(bodyMap.get("outSts"));
                //                        resMap.put("outSts", applOutSts);//
                //                    }
                //                }
                return success(resMap);
            }
        }
    }

    /**
     * 额度申请审批进度查询service
     *
     * @param map
     * @return
     */
    public Map<String, Object> getEdApplProgress(HashMap<String, Object> map) {
        Map<String, Object> resMap = CmisUtil
                .getCmisResponse(CmisTradeCode.TRADECODE_EDAPPLAY_PROGRESS, super.getToken(), map);
        logger.info("信贷100017额度申请返回resMap==" + resMap);
        Map<String, Object> responseMap = (HashMap<String, Object>) resMap.get("response");
        Map<String, Object> headMap = (HashMap<String, Object>) responseMap.get("head");
        String retFlag = String.valueOf(headMap.get("retFlag"));
        String retMsg = String.valueOf(headMap.get("retMsg"));
        if (!"00000".equals(retFlag)) {
            logger.info("信贷100017额度申请返回异常！");
            return RestUtil.fail(retFlag, retMsg);//返回信贷的错误码
        }
        Map<String, Object> bodyMap = (HashMap<String, Object>) responseMap.get("body");
        Map<String, Object> listMap = (HashMap<String, Object>) bodyMap.get("list");
        Object obj = listMap.get("info");
        if (obj instanceof List) {
            List<Map<String, Object>> infoList = (ArrayList<Map<String, Object>>) listMap.get("info");
            Comparator<Map<String, Object>> comparator = new Comparator<Map<String, Object>>() {
                public int compare(Map<String, Object> s1, Map<String, Object> s2) {
                    //                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    //                    String s1Time = String.valueOf(s1.get("operateTime"));
                    //                    String s2Time = String.valueOf(s2.get("operateTime"));
                    //                    try {
                    //                        return (sdf.parse(s2Time).compareTo(sdf.parse(s1Time)));
                    //                    } catch (ParseException e) {
                    //                        // TODO Auto-generated catch block
                    //                        e.printStackTrace();
                    //                        return 0;
                    //                    }
                    //改为比较申请流水号
                    String applSeq1 = String.valueOf(s1.get("applSeq"));
                    String applSeq2 = String.valueOf(s2.get("applSeq"));
                    try {
                        return (new BigDecimal(applSeq2).compareTo(new BigDecimal(applSeq1)));
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        return 1;
                    }

                    // return 1;
                }
            };
            Collections.sort(infoList, comparator);
            logger.info("额度申请进度最终节点数量infoList.size()==" + infoList.size());
            return success(infoList.get(0));
        } else if (obj instanceof Map) {
            return success(obj);
        } else {
            return null;
        }
    }

    /**
     * 四要素验证service
     *
     * @param custName
     * @param idNo
     * @param mobile
     * @param cardNo
     * @return
     */
    public Map<String, Object> checkFourKeys(String custName, String idNo, String mobile, String cardNo) {
        logger.info("Parm:{custName=" + custName + ",idNo=" + idNo + ",mobile=" + mobile + ",cardNo=" + cardNo + "}");
        HashMap<String, Object> resMap = new HashMap<String, Object>();
        String flag;
        //1、校验银行卡是否通过
        Map<String, Object> cardInfoMap = this.getBankInfoFromCrm(cardNo);
        JSONObject cardInfoHeadObject = (JSONObject) cardInfoMap.get("head");
        String cardRetFlag = cardInfoHeadObject.getString("retFlag");
        if (!"00000".equals(cardRetFlag)) {
            logger.error("CRM64 根据银行卡号查询银行卡信息接口返回失败，可能是银行卡所在行不支持！具体原因：" + cardInfoMap);
            return RestUtil.fail(cardRetFlag, cardInfoHeadObject.getString("retMsg"));//返回CRM的错误码和错误信息
        } else {
            //获取银行卡信息
            Map<String, Object> mapBody = HttpUtil.json2Map(cardInfoMap.get("body").toString());
            String bankCode = mapBody.get("bankNo").toString();
            HashMap<String, Object> requestMap = new HashMap<String, Object>();
            requestMap.put("accountName", custName);
            requestMap.put("accountNo", cardNo);
            requestMap.put("bankCode", bankCode);
            requestMap.put("id", idNo);
            requestMap.put("cardPhone", mobile);
            requestMap.put("channelNo", "appServer");
            requestMap.put("flag", "1");

            //根据客户、身份证号查询实名信息
            Map<String, Object> smrzMap = this.getSmrzInfoByCustNameAndIdNo(custName, idNo);
            logger.info("CRM 实名认证（13）接口返回custMap==" + smrzMap);
            JSONObject custHeadObject = (JSONObject) smrzMap.get("head");
            String retFlag = custHeadObject.getString("retFlag");
            //若不存在，则取外联平台进行比对
            if (!"00000".equals(retFlag)) {
                logger.info("实名认证信息查询失败！去外联平台进行四要素验证！");
                Map<String, Object> outPlateMap = this.outPlateFormFourKeys(requestMap);
                logger.info("外联平台返回结果" + outPlateMap);
                String outPlateRetFlag = (String) (outPlateMap).get("RET_CODE");
                String outPlateRetMsg = (String) (outPlateMap).get("RET_MSG");
                if ("00000".equals(outPlateRetFlag)) {
                    flag = "Y";
                    resMap.put("flag", flag);
                    return success(resMap);
                } else {
                    flag = "N";
                    resMap.put("flag", flag);
                    return fail("10", "四要素验证失败", resMap);
                }

            } else {
                //若存在，则比对身份证号和手机号是否一致
                Map<String, Object> custBodyMap = HttpUtil.json2Map(smrzMap.get("body").toString());
                //实名认证身份证号
                String smIdNo = String.valueOf(custBodyMap.get("certNo"));
                //实名手机号
                String smMobile = String.valueOf(custBodyMap.get("mobile"));
                if (Objects.equals(smIdNo, idNo) && Objects.equals(smMobile, mobile)) {
                    flag = "Y";
                    resMap.put("flag", flag);
                    return success(resMap);//验证通过

                } else {
                    //比对失败时，去外联平台进行校验
                    logger.info("实名认证存在，但身份证号手机号比对失败，将去外联平台进行四要素验证！");
                    Map<String, Object> outPlateMap = this.outPlateFormFourKeys(requestMap);
                    logger.info("外联平台返回结果" + outPlateMap);
                    String outPlateRetFlag = (String) (outPlateMap).get("RET_CODE");
                    String outPlateRetMsg = (String) (outPlateMap).get("RET_MSG");
                    if ("00000".equals(outPlateRetFlag)) {
                        flag = "Y";
                        resMap.put("flag", flag);
                        return success(resMap);
                    } else {
                        flag = "N";
                        resMap.put("flag", flag);
                        return fail("10", "四要素验证失败", resMap);
                    }
                }
            }
        }
    }

    /**
     * CRM64接口 查询银行卡信息
     *
     * @param cardNo
     * @return
     */
    public Map<String, Object> getBankInfoFromCrm(String cardNo) {
        String cardInfoUrl = EurekaServer.CRM + "/app/crm/cust/getBankInfo?cardNo="
                + cardNo;
        String cardInfoJson = HttpUtil.restGet(cardInfoUrl);
        logger.info("CRM64 根据银行卡号查询银行卡信息请求url:" + cardInfoUrl);
        logger.info("CRM64 根据银行卡号查询银行卡信息返回：cardInfoJson==" + cardInfoJson);
        Map<String, Object> cardInfoMap = HttpUtil.json2Map(cardInfoJson);
        return cardInfoMap;
    }

    public Map<String, Object> outPlateFormFourKeys(HashMap<String, Object> requestMap) {
        HttpHeaders headers = new HttpHeaders();
        MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
        headers.setContentType(type);
        ResponseEntity<Map> result = null;
        RestTemplate template = new RestTemplate();
        String flag = "";
        // 四要素接口
        //  map.put("passStandard", "1");
        requestMap.put("flag", "1");
        JSONObject json = new JSONObject(requestMap);
        HttpEntity<String> reqE = new HttpEntity<String>(json.toString(), headers);
        logger.info("request Param:" + reqE);
        logger.info(outplatform);
        result = template.exchange(outplatform + "/Outreachplatform/api/chinaPay/identifyByFlag", HttpMethod.POST, reqE,
                Map.class);
        logger.info("四要素接口返回结果:" + result);

        return (Map) result.getBody();
    }

    /**
     * 星巢贷支付密码短信发送请求处理.
     *
     * @param msgRequest 请求信息.
     * @return Map
     */
    public Map<String, Object> msgRequest(MsgRequest msgRequest, String custName) {

        // 测试 暂时写死
        Map<String, Object> userMsg = this.getSmrzInfoByCustNameAndIdNo(custName, msgRequest.getUserId());
        // Map<String, Object> userMsg = this.getSmrzInfoByUserId("18602959696");
        if (!HttpUtil.isSuccess(userMsg)) {
            return fail("53", "用户拓展信息获取失败");
        }

        JSONObject body = (JSONObject) userMsg.get("body");
        if (StringUtils.isEmpty(body.get("mobile"))) {
            return fail("54", "用户手机号码查询失败");
        }

        //绑定手机号
        String bdMobile = appOrderService
                .getBindMobileByCustNameAndIdNo(body.get("custName").toString(), body.get("certNo").toString(), null);
        if (StringUtils.isEmpty(bdMobile)) {
            msgRequest.setPhone(body.get("mobile").toString());
        } else {
            msgRequest.setPhone(bdMobile);
        }
        // 测试暂时写死手机号码
        // msgRequest.setPhone("13188973791");

        msgRequest.setId(UUID.randomUUID().toString().replace("-", ""));
        msgRequest.setRequestTime(new Date());
        msgRequest.setIsSend("0");

        // TODO 话术待定
        msgRequest.setMsg("您的支付密码为：" + msgRequest.getPayCode());
        logger.debug("执行待发送短信保存");
        msgRequestRepository.save(msgRequest);
        return success();
    }

    /**
     * 星巢贷放款通知保存至数据库(已移至appserver-msg)
     *
     * @param idNo    客户证件号码
     * @param applseq 贷款流水号
     * @return
     */
    @Deprecated
    public Map<String, Object> xcdLoan(String idNo, String applseq) {
        // 验证该申请流水号是否已发送过请求
        List<MsgRequest> hasStore = msgRequestRepository.findByApplSeq(applseq);
        if (hasStore != null && !hasStore.isEmpty()) {
            return success();
        }

        MsgRequest msgRequest = new MsgRequest();

        msgRequest.setApplSeq(applseq);
        msgRequest.setType("payCode");
        msgRequest.setPayCode(RandomCodeUtil.getRandomNumber(6));

        Map<String, Object> cmisMap = appOrderService.getAppOrderMapFromCmis(msgRequest.getApplSeq());
        if (cmisMap == null || cmisMap.isEmpty()) {
            return fail("52", "CMIS获取用户订单信息失败");
        }
        if (!"16".equals(cmisMap.get("CRE_APP"))) {
            // 渠道非星巢贷订单，直接返回成功
            return success();
        }
        String indivMobile = (String) cmisMap.get("INDIV_MOBILE");
        if (StringUtils.isEmpty(indivMobile)) {
            return fail("53", "CMIS不存在用户手机号码");
        }
        if (StringUtils.isEmpty(idNo)) {
            idNo = (String) cmisMap.get("ID_NO");
        }
        msgRequest.setUserId(idNo);

        //绑定手机号
        //String bdMobile = appOrderService
        //        .getBindMobileByCustNameAndIdNo(cmisMap.get("CUST_NAME").toString(), cmisMap.get("ID_NO").toString(), null);
        String userId = appOrderService.getUserIdByCustNameAndIdNo(cmisMap.get("CUST_NAME").toString(), idNo);
        if (StringUtils.isEmpty(userId)) {
            logger.info("crm中不存在该用户信息，手机号码保存为订单中的手机号码。userId=" + userId);
            msgRequest.setPhone(indivMobile);
        } else {
            String bdMobile = appOrderService.getBindMobileByUserId(userId, null);
            if (StringUtils.isEmpty(bdMobile)) {
                logger.info("crm中不存在该用户手机号码，手机号码保存为订单中的手机号码。mobile=" + bdMobile);
                msgRequest.setPhone(indivMobile);
            } else {
                msgRequest.setPhone(bdMobile);
            }
        }
        msgRequest.setId(UUID.randomUUID().toString().replace("-", ""));
        msgRequest.setRequestTime(new Date());
        msgRequest.setIsSend("0");

        msgRequest.setMsg("【海尔消费金融】您的支付密码为：" + msgRequest.getPayCode() + "。如有疑问，请致电海尔消费金融客服电话：4000187777");
        logger.debug("执行待发送短信保存");
        msgRequestRepository.save(msgRequest);
        return success();
    }

    /**
     * 定时任务星巢贷放款通过通知外联平台(已移至appserver-msg)
     *
     * @param msgRequest
     * @return
     */
    @Deprecated
    public Map<String, Object> timerXcdLoan(MsgRequest msgRequest) {
        Map<String, Object> sendParam = new HashMap<>();

        // 调用信贷接口查询详细贷款信息
        Map<String, Object> cmisMap = appOrderService.getAppOrderMapFromCmis(msgRequest.getApplSeq());
        if (StringUtils.isEmpty(cmisMap) || cmisMap.isEmpty()) {
            return null;
        }
        sendParam.put("userId", msgRequest.getUserId());// 用户ID
        sendParam.put("applySeq", msgRequest.getApplSeq());// 贷款流水号
        sendParam.put("userMobile", cmisMap.get("INDIV_MOBILE"));// 用户手机
        sendParam.put("userCertId", msgRequest.getUserId());// 用户证件号
        sendParam.put("userPwd", msgRequest.getPayCode());// 支付口令
        sendParam.put("userName", cmisMap.get("CUST_NAME"));// 用户姓名
        String applyAmt = cmisMap.get("APPLY_AMT") == null ? "" : String.valueOf(cmisMap.get("APPLY_AMT"));
        sendParam.put("applyAmt",
                String.valueOf(new BigDecimal(applyAmt).multiply(BigDecimal.valueOf(100)).setScale(0)));// 分期金额，以分为单位
        sendParam.put("applyType", "00");// 分期模式 暂定死为00
        // sendParam.put("merId", cmisMap.get("COOPR_CDE"));// 商户ID即门店ID

        // 去信贷系统查询营销人员代码
        String promRequestUrl = EurekaServer.CMISPROXY + "/api/appl/getPromInfo?applseq=" + msgRequest.getApplSeq();
        logger.debug("去信贷系统查询营销人员信息：" + promRequestUrl);
        String promJson = HttpUtil.restGet(promRequestUrl);
        logger.debug("营销人员查询结果：" + promJson);
        if (StringUtils.isEmpty(promJson)) {
            return fail("22", "星巢贷订单查询营销人员信息失败");
        }
        Map<String, Object> promMap = HttpUtil.json2Map(promJson);
        sendParam.put("marketCode", promMap.containsKey("promCde") ? String.valueOf(promMap.get("promCde")) : "");

        // 去crm系统查询商户ID
        String crmRequestUrl = EurekaServer.CRM + "/app/crm/cust/getStore?storeNo=" + cmisMap.get("COOPR_CDE");
        logger.info("向crm发起查询商户ID请求：" + crmRequestUrl);
        String crmResultJson = HttpUtil.restGet(crmRequestUrl);
        logger.info("crm获得商户信息：" + crmResultJson);
        if (StringUtils.isEmpty(crmResultJson)) {
            return fail("24", "crm获取商户信息失败");
        }
        if (!HttpUtil.isSuccess(crmResultJson)) {
            return fail("24", "crm获取商户信息失败");
        }
        Map<String, Object> crmResultMap = HttpUtil.json2Map(crmResultJson);
        sendParam.put("marketId", String.valueOf(((JSONObject) crmResultMap.get("body")).get("merchNo")));// 商场ID即商户ID
        /*List<Map<String, Object>> goodsList = (ArrayList<Map<String, Object>>) cmisMap.get("goods");
        if (!goodsList.isEmpty()) {
            Map<String, Object> good = goodsList.get(0);
            sendParam.put("goodsId", good.get("goods_model"));// 商品ID
            sendParam.put("goodsName", good.get("GOODS_NAME"));// 商品名称
        }*/

        // 调用外联平台接口上传信息
        JSONObject json = new JSONObject(sendParam);
        HttpHeaders headers = new HttpHeaders();
        MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
        headers.setContentType(type);
        HttpEntity<String> reqE = new HttpEntity<String>(json.toString(), headers);
        RestTemplate restTemplate = new RestTemplate();
        logger.info("向外联平台发起请求:" + json.toString());
        ResponseEntity<Map> result = restTemplate
                .exchange(outplatform + "/Outreachplatform/api/xcd/loan", HttpMethod.POST, reqE,
                        Map.class);

        logger.info("外联平台返回结果：" + result);
        if (result != null) {
            if ("00000".equals((String) result.getBody().get("retFlag"))) {
                return success();
            } else {
                return fail((String) result.getBody().get("retFlag"), (String) result.getBody().get("retMsg"));
            }
        }
        return fail("25", "外联平台返回结果为空");
    }

    /**
     * 根据用户身份证号去信贷查询用户贷款余额
     *
     * @param idNo
     * @return key:"blance"
     */
    public Map<String, Object> getBlanceByIdNo(String idNo) {
        String url = EurekaServer.CMISPROXY + "/api/appl/getIdBlanceByIdNo?idNo=" + idNo;
        Map<String, Object> resultMap = HttpUtil.restGetMap(url);
        return resultMap;
    }

    public Map<String, Object> queryApplCountNew(Map<String, Object> map) {
        List<Map<String, Object>> returnList = new ArrayList<>();

        String channel = (String) map.get("channel");
        String channelNo = (String) map.get("channelNo");
        Map<String, Object> paramsBody = new HashMap<>();
        String sourceForm = (String) map.get("sourceForm");
        paramsBody.put("sourceForm", sourceForm);
        String outSts = (String) map.get("outSts");
        logger.info("===outSts:" + outSts);
        if (!StringUtils.isEmpty(outSts)) {
            paramsBody.put("outSts", outSts);
        }
        if ("01".equals(sourceForm)) {//商户版
            paramsBody.put("crtUsr", map.get("crtUsr"));
        } else {//个人版
            paramsBody.put("idNo", map.get("idNo"));
        }
        String url = EurekaServer.ACQUIRER + "/api/appl/queryApplCount";
        Map<String, Object> acqResponse = AcqUtil
                .getAcqResponse(url, AcqTradeCode.SELECT_APPL_COUNT, channel, channelNo, null, null, paramsBody);
        logger.info("返回结果result：" + acqResponse);
        Map<String, Object> responseMap = (Map<String, Object>) acqResponse.get("response");
        Map<String, Object> returnMap;
        if (isSuccess(responseMap)) {
            returnMap = (Map<String, Object>) responseMap.get("body");
        } else {
            Map<String, Object> responseHeadMap = (Map<String, Object>) responseMap.get("head");
            return fail("99", (String) responseHeadMap.get("retMsg"));
        }
        //查询已提交的订单的数量，取列表的大小
        if (StringUtils.isEmpty(outSts)) {
            //待发货数量 从订单查询
            //WS先从订单系统查订单状态为30的流水号，再调用收单接口批量查询贷款信息
            try {
                List waitSendOrderList = appOrderService.queryWaitSendOrder(map);
                returnMap.put("sendCount", waitSendOrderList.size());
            } catch (Exception e) {
                e.printStackTrace();
                return fail("99", "查询失败");
            }
            //合同签订中数量
            // 去掉04-合同签订中，线下订单 需要发货处理 的订单(即04合同签订中里的30已付款待发货的订单)
            try {
                Integer contCount = queryContCount(map);
                returnMap.put("contCount", contCount);
            } catch (Exception e) {
                e.printStackTrace();
                return fail("99", "查询失败");
            }
        }
        return success(returnMap);
    }

    //订单系统 查询订单数量
    public Map<String, Object> queryOrderCount(String url) {
        Map<String, Object> returnMap = new HashMap<>();

        logger.info("==>ORDER  url:" + url);
        Map<String, Object> orderRetMap = HttpUtil.restGetMap(url, HttpStatus.OK.value());
        logger.info("<==ORDER  返回结果:" + orderRetMap);
        if (StringUtils.isEmpty(orderRetMap)) {
            return fail(RestUtil.ERROR_INTERNAL_CODE, RestUtil.ERROR_INTERNAL_MSG);
        }
        if (isSuccess(orderRetMap)) {
            Map<String, Object> orderRetBodyMap = (Map<String, Object>) orderRetMap.get("body");
            List<Map<String, Object>> orderStateList = (List<Map<String, Object>>) orderRetBodyMap.get("list");
            returnMap.put("list", orderStateList);
            return success(returnMap);
        } else {
            Map<String, Object> responseHeadMap = (Map<String, Object>) orderRetMap.get("head");
            return fail("99", (String) responseHeadMap.get("retMsg"));
        }
    }

    //查询合同签订中数量
    public Integer queryContCount(Map map) throws Exception {
        logger.info("查询合同签订中数量 参数:" + map);
        String queryOutSts = "04";//04-合同签订中
        String channel = (String) map.get("channel");
        String channelNo = (String) map.get("channelNo");
        String sourceForm = (String) map.get("sourceForm");

        String channelNoStr = ReflactUtils.getChannelNoByChannelAndChannelNo(channel, channelNo);
        logger.info("==channelNo:" + channelNoStr);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("channel", channel);
        paramMap.put("channelNo", channelNoStr);
        //设置默认值
        Integer page = 1;
        Integer pageSize = 1000;
        paramMap.put("sourceForm", sourceForm);
        paramMap.put("outSts", queryOutSts);
        if ("01".equals(sourceForm)) {//商户版
            paramMap.put("crtUsr", map.get("crtUsr"));
        } else {//个人版
            paramMap.put("idNo", map.get("idNo"));
        }
        paramMap.put("page", page);
        paramMap.put("pageSize", pageSize);
        Map<String, Object> response = appOrderService.getDateAppOrderNew(paramMap);
        logger.info("查询订单列表返回:" + response);
        if (RestUtil.isSuccess(response)) {
            List orderStateList = (List) ((Map<String, Object>) response.get("body")).get("orders");
            return orderStateList.size();
        } else {
            throw new Exception("查询失败");
        }
    }

}