package com.haiercash.appserver.service.impl;

import com.haiercash.appserver.service.AttachService;
import com.haiercash.appserver.util.sign.FileSignUtil;
import com.haiercash.common.service.BaseService;
import com.haiercash.appserver.service.CASignService;
import com.haiercash.appserver.service.CmisApplService;
import com.haiercash.appserver.service.CommonRepaymentPersonService;
import com.haiercash.appserver.service.CrmService;
import com.haiercash.appserver.util.enums.AcquirerCommonPersonEnum;
import com.haiercash.common.apporder.utils.AcqTradeCode;
import com.haiercash.common.config.EurekaServer;
import com.haiercash.common.data.AppOrder;
import com.haiercash.common.data.AppOrdernoTypgrpRelation;
import com.haiercash.common.data.AppOrdernoTypgrpRelationRepository;
import com.haiercash.common.data.CommonRepaymentPerson;
import com.haiercash.commons.cmis.CmisUtil;
import com.haiercash.commons.util.AcqUtil;
import com.haiercash.commons.util.HttpUtil;
import com.haiercash.commons.util.IdcardUtils;
import com.haiercash.commons.util.RestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Commont repayment person service impl.
 *
 * @author Liu qingxiang
 * @see CommonRepaymentPersonService
 */
@Service
public class CommonRepaymentPersonServiceImpl extends BaseService implements CommonRepaymentPersonService {
    private static String MODULE_NO = "11";

    /**
     * CommonRepaymentPersonServiceImpl类构造方法
     */
    public CommonRepaymentPersonServiceImpl() {
        super(MODULE_NO);
    }

    @Autowired
    private CASignService cASignService;
    @Autowired
    private AppOrdernoTypgrpRelationRepository appOrdernoTypgrpRelationRepository;
    @Autowired
    private CmisApplService cmisApplService;
    @Autowired
    private CrmService crmService;
    @Autowired
    private AttachService attachService;

    /**
     * 新增共同还款人service.
     *
     * @param commonRepaymentPerson
     * @param tradeType 1:新增；2:修改
     * @return
     */
    public Map<String, Object> addCommonRepaymentPerson(CommonRepaymentPerson commonRepaymentPerson, String tradeType) {
        String smsCode = commonRepaymentPerson.getSmsCode();
        if (StringUtils.isEmpty(smsCode)) {
            return fail("01", "短信验证码不能为空!");
        }

        String orderNo = commonRepaymentPerson.getOrderNo();
        AppOrdernoTypgrpRelation relation = appOrdernoTypgrpRelationRepository.findOne(orderNo);
        if (relation == null) {
            return fail("05", "共同还款人对应的订单不存在：" + orderNo);
        }

        String checkVerifyNoResult = FileSignUtil.checkVerifyNo(commonRepaymentPerson.getMobile(), smsCode);
        if (!"00000".equals(checkVerifyNoResult)) {
            return fail("02", "添加共同还款人失败：" + checkVerifyNoResult);
        }

        // 客户基本信息查询
        String smrzUrl = EurekaServer.CRM + "/app/crm/cust/queryCustRealInfoByCustNo?custNo="
                + commonRepaymentPerson.getCommonCustNo();
        String smrzJson = HttpUtil.restGet(smrzUrl);
        logger.info("smrzResult==" + smrzJson);
        Map<String, Object> smrzMap = HttpUtil.json2DeepMap(smrzJson);
        if (StringUtils.isEmpty(smrzMap) || smrzMap.isEmpty()) {
            return fail("03", "共同还款人实名信息不存在!");
        }
        if (!HttpUtil.isSuccess(smrzMap)) {
            return smrzMap;
        }
        Map<String, Object> smrzBodyMap = (Map<String, Object>) smrzMap.get("body");
        String custName = (String) smrzBodyMap.get("custName");
        String idNo = (String) smrzBodyMap.get("certNo");
        String mobile = (String) smrzBodyMap.get("mobile");
        String cardNo = (String) smrzBodyMap.get("cardNo");
        String repayAcProvince = "";
        String repayAcCity = "";
        if (smrzBodyMap.containsKey("repayAcProvince")) {
            repayAcProvince = StringUtils.isEmpty(smrzBodyMap.get("repayAcProvince")) ? "" : (String) smrzBodyMap.get("repayAcProvince");
        }
        if (smrzBodyMap.containsKey("repayAcCity")) {
            repayAcCity = StringUtils.isEmpty(smrzBodyMap.get("repayAcCity")) ? "" : (String) smrzBodyMap.get("repayAcCity");
        }
        HashMap<String, Object> apptmap = new HashMap<>();
        apptmap.put("applSeq", relation.getApplSeq());
        apptmap.put("appt_typ", "02"); // 共同申请人
        apptmap.put("appt_relation", commonRepaymentPerson.getRelation());
        apptmap.put("appt_id_typ", smrzBodyMap.get("certType"));
        apptmap.put("appt_id_typ_oth", "");
        apptmap.put("appt_id_no", idNo);
        apptmap.put("appt_cust_name", custName);

        List<Map<String, Object>> apptMapList = new ArrayList<Map<String, Object>>();
        Map<String, Object> custExtInfoBodyMap = new HashMap<String, Object>();
        String url = EurekaServer.CRM + "/app/crm/cust/getCustExtInfo?pageName=1&custNo=" + commonRepaymentPerson.getCommonCustNo();
        logger.debug("CRM getCustExtInfo...");
        String json = HttpUtil.restGet(url, getToken());
        if (StringUtils.isEmpty(json)) {
            logger.error("客户扩展信息接口返回异常！-->CRM 1.4");
            return fail("51", RestUtil.ERROR_INTERNAL_MSG);
        }
        logger.debug("CRM 获取客户拓展信息返回json:" + json);
        Map<String, Object> custExtInfoMap = HttpUtil.json2Map(json);
        logger.debug("客户拓展信息转换值:" + custExtInfoMap);

        if (!StringUtils.isEmpty(custExtInfoMap.get("body"))) {
            // 客户扩展信息查询
            custExtInfoBodyMap = HttpUtil.json2Map(custExtInfoMap.get("body").toString());
        }
        cmisApplService.dealAddress(custExtInfoBodyMap); //对地址进行处理
        //对订单的收货地址进行处理
        AppOrder deliverOrder = new AppOrder();
        cmisApplService.dealDeliverAddress(deliverOrder, custExtInfoBodyMap);
        // 获取联系人
        // 客户基本信息查询
        List<Map<String, Object>> lxrlist = new ArrayList<Map<String, Object>>();

        String lxrUrl =
                EurekaServer.CRM + "/app/crm/cust/findCustFCiCustContactByCustNo?custNo=" + commonRepaymentPerson.getCommonCustNo();
        String lxrJson = HttpUtil.restGet(lxrUrl, getToken());
        logger.debug("CRM 获取客户基本信息:" + lxrJson);
        if (StringUtils.isEmpty(lxrJson)) {
            logger.error("联系人列表查询失败！——>CRM 1.8");
            return fail("52", RestUtil.ERROR_INTERNAL_MSG);
        }
        Map<String, Object> lxrMap = HttpUtil.json2Map(lxrJson);
        if (!StringUtils.isEmpty(lxrMap.get("body"))) {
            lxrlist = (ArrayList) lxrMap.get("body");
        }
        //////////////////

        apptmap.put("appt_indiv_sex", StringUtils.isEmpty(idNo) ? "" : IdcardUtils.getGenderByIdCard(idNo));
        apptmap.put("appt_start_date", StringUtils.isEmpty(idNo) ? "" : IdcardUtils.getYearByIdCard(idNo) + "-" + IdcardUtils
                .getMonthByIdCard(idNo)
                + "-" + IdcardUtils.getDateByIdCard(idNo));
        apptmap.put("indiv_marital", commonRepaymentPerson.getMaritalStatus());

        apptmap.put("indiv_edu", custExtInfoBodyMap.get("education"));
        // 美分期 集团大数据 默认值 大专
        if ("14".equals(super.getChannel()) || "34".equals(super.getChannelNo()) || "35".equals(super.getChannelNo())) {
            apptmap.put("indiv_edu", StringUtils.isEmpty(custExtInfoBodyMap.get("education")) ? "20" : custExtInfoBodyMap.get("education"));
        }

        //户籍地址
        apptmap.put("appt_reg_province", custExtInfoBodyMap.get("regProvince"));
        apptmap.put("appt_reg_city", custExtInfoBodyMap.get("regCity"));
        //星巢贷户籍地址处理==》若户籍地址为空，则使用现住房地址的省市
        if ("16".equals(super.getChannel()) && StringUtils.isEmpty(custExtInfoBodyMap.get("regProvince"))) {
            apptmap.put("appt_reg_province", custExtInfoBodyMap.get("liveProvince"));
            apptmap.put("appt_reg_city", custExtInfoBodyMap.get("liveCity"));
            // 增加默认值：山东青岛
            if (StringUtils.isEmpty(apptmap.get("appt_reg_province"))) {
                apptmap.put("appt_reg_province", "370000");
                apptmap.put("appt_reg_city", "370200");
            }
        }

        // 房产自有性质默认无
        apptmap.put("live_info", StringUtils.isEmpty(custExtInfoBodyMap.get("liveInfo")) ? "10" : custExtInfoBodyMap.get("liveInfo"));
        /**现住房信息 星巢贷、大数据、美分期统一传99（其他）**/
        if (Objects.equals("16", super.getChannel()) || "34".equals(super.getChannelNo()) || "35".equals(super.getChannelNo())) {
            apptmap.put("live_info", "99");
        }
        apptmap.put("live_province", custExtInfoBodyMap.get("liveProvince"));
        apptmap.put("live_city", custExtInfoBodyMap.get("liveCity"));
        apptmap.put("live_area", custExtInfoBodyMap.get("liveArea"));
        apptmap.put("live_addr", custExtInfoBodyMap.get("liveAddr"));
        apptmap.put("live_zip", "");// custExtInfoBodyMap.get("liveZip")
        apptmap.put("live_mj", custExtInfoBodyMap.get("liveSize"));
        apptmap.put("ppty_live", StringUtils.isEmpty(custExtInfoBodyMap.get("pptyLiveInd")) ? "Y" : custExtInfoBodyMap.get("pptyLiveInd"));// 自有房产地址
        apptmap.put("ppty_province", custExtInfoBodyMap.get("liveProvince"));// pptyProvince
        apptmap.put("ppty_city", custExtInfoBodyMap.get("liveCity"));// pptyCity
        apptmap.put("ppty_area", custExtInfoBodyMap.get("liveArea"));// pptyArea
        apptmap.put("ppty_addr", custExtInfoBodyMap.get("liveAddr"));// pptyAddr
        apptmap.put("ppty_zip", "");
        apptmap.put("ppty_mj", custExtInfoBodyMap.get("liveSize"));// pptySize
        apptmap.put("indiv_mobile", mobile);
        apptmap.put("indiv_fmly_zone", custExtInfoBodyMap.get("fmlyZone"));
        apptmap.put("indiv_fmly_tel", custExtInfoBodyMap.get("fmlyTel"));
        apptmap.put("local_resid", custExtInfoBodyMap.get("localResid"));
        if ("14".equals(super.getChannel()) && StringUtils.isEmpty(custExtInfoBodyMap.get("localResid"))) {
            apptmap.put("local_resid", "10");
        }
        // 美分期 集团大数据 默认本地
        if ("34".equals(super.getChannelNo()) || "35".equals(super.getChannelNo())) {
            apptmap.put("local_resid", "10");
        }
        apptmap.put("indiv_email", custExtInfoBodyMap.get("email"));
        apptmap.put("live_year",
                StringUtils.isEmpty(custExtInfoBodyMap.get("liveYear")) ?
                        "0" : custExtInfoBodyMap.get("liveYear"));
        apptmap.put("indiv_dep_no", StringUtils.isEmpty(custExtInfoBodyMap.get("providerNum")) ? "0"
                : custExtInfoBodyMap.get("providerNum"));

        if (StringUtils.isEmpty(custExtInfoBodyMap.get("mthInc"))) {
            apptmap.put("annual_earn", "0");
        } else {
            apptmap.put("annual_earn",
                    new BigDecimal(custExtInfoBodyMap.get("mthInc").toString()).multiply(new BigDecimal(12)));
        }
        apptmap.put("household_own_rel", "");
        apptmap.put("max_crdt_card", custExtInfoBodyMap.get("maxAmount"));
        apptmap.put("iss_bank", custExtInfoBodyMap.get("creditCount"));
        apptmap.put("position_opt", StringUtils.isEmpty(custExtInfoBodyMap.get("positionType")) ? "10"
                : custExtInfoBodyMap.get("positionType"));
        apptmap.put("indiv_mth_inc", commonRepaymentPerson.getMthInc());
        // 个人版现金贷，以下赋默认值
        if (("14".equals(super.getChannel()) || "34".equals(super.getChannelNo()))) {
            apptmap.put("mail_opt", "");
            apptmap.put("mail_province", "");
            apptmap.put("mail_city", "");
            apptmap.put("mail_area", "");
            apptmap.put("mail_addr", "");
        } else {
            String mailOpt = deliverOrder.getDeliverAddrTyp();
            apptmap.put("mail_opt", mailOpt);
            //2016.11.03送货地址只从订单中取
            apptmap.put("mail_province",
                    deliverOrder.getDeliverProvince() == null ? "" : deliverOrder.getDeliverProvince());
            apptmap.put("mail_city", deliverOrder.getDeliverCity() == null ? "" : deliverOrder.getDeliverCity());
            apptmap.put("mail_area", deliverOrder.getDeliverArea() == null ? "" : deliverOrder.getDeliverArea());
            apptmap.put("mail_addr", deliverOrder.getDeliverAddr() == null ? "" : deliverOrder.getDeliverAddr());
        }


        // 学习方式
        apptmap.put("study_mth", custExtInfoBodyMap.get("studyType"));

        apptmap.put("studying_deg", custExtInfoBodyMap.get("studyDegree"));
        // 集团大数据 在读学历 默认值处理
        if (StringUtils.isEmpty(custExtInfoBodyMap.get("studyDegree")) && "34".equals(super.getChannelNo())) {
            apptmap.put("studying_deg", "01"); //大专
        }
        apptmap.put("school_name", custExtInfoBodyMap.get("schoolName"));
        apptmap.put("study_major", custExtInfoBodyMap.get("studyMajor"));
        apptmap.put("school_kind", custExtInfoBodyMap.get("schoolKind"));
        apptmap.put("school_leng", custExtInfoBodyMap.get("schoolLeng"));
        apptmap.put("geade", custExtInfoBodyMap.get("schoolGrade"));
        apptmap.put("emp_reg_name", commonRepaymentPerson.getOfficeName());//
        apptmap.put("emp_reg_dt", "");
        apptmap.put("manage_addr", "");
        apptmap.put("manage_province", "");
        apptmap.put("manage_city", "");
        apptmap.put("manage_area", "");
        apptmap.put("emp_reg_rel_tel", "");//
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
        apptmap.put("indiv_emp_name", custExtInfoBodyMap.get("officeName"));
        apptmap.put("indiv_branch", StringUtils.isEmpty(custExtInfoBodyMap.get("officeDept")) ?
                "默认" :
                custExtInfoBodyMap.get("officeDept"));
        apptmap.put("indiv_emp_typ",
                StringUtils.isEmpty(custExtInfoBodyMap.get("officeTyp")) ?
                        "Z" :
                        custExtInfoBodyMap.get("officeTyp"));
        apptmap.put("indiv_emp_yrs", StringUtils.isEmpty(custExtInfoBodyMap.get("serviceYears")) ?
                0 :
                custExtInfoBodyMap.get("serviceYears"));// custExtInfoBodyMap.get("serviceYears")
        apptmap.put("indiv_emp_province", custExtInfoBodyMap.get("officeProvince"));
        apptmap.put("indiv_emp_city", custExtInfoBodyMap.get("officeCity"));
        apptmap.put("indiv_emp_area", custExtInfoBodyMap.get("officeArea"));
        apptmap.put("empaddr", custExtInfoBodyMap.get("officeAddr"));
        apptmap.put("indiv_emp_zip", "266000");
        if (StringUtils.isEmpty(custExtInfoBodyMap.get("officeTel"))) {
            apptmap.put("indiv_emp_zone", "0532");
        } else {
            String tel = String.valueOf(custExtInfoBodyMap.get("officeTel"));
            Map<String, String> phoneMap = cmisApplService.getPhoneNoAndZone(tel);
            apptmap.put("indiv_emp_zone", phoneMap.get("zone"));
        }
        apptmap.put("indiv_emp_tel", commonRepaymentPerson.getOfficeTel());

        apptmap.put("indiv_emp_tel_sub", "");
        apptmap.put("indiv_emp_hr_zone", "");
        apptmap.put("indiv_emp_hr_phone", "");
        apptmap.put("indiv_emp_hr_sub", "");
        // 美凯龙职务默认传中层
        if ("16".equals(super.getChannel())) {
            apptmap.put("indiv_position", "02");
        } else {
            apptmap.put("indiv_position", custExtInfoBodyMap.get("position"));
        }
        // 美分期 职务 默认基层
        if ("35".equals(super.getChannel()) && StringUtils.isEmpty(custExtInfoBodyMap.get("position"))) {
            apptmap.put("indiv_position", "03");
        }

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
        // 美分期月收入默认值处理：3000
        if ("35".equals(super.getChannelNo()) && StringUtils.isEmpty(apptmap.get("sp_mth_inc"))) {
            apptmap.put("sp_mth_inc", "3000");
        }
        apptmap.put("spouse_pay_ind", "");
        apptmap.put("ppty_live_opt", "10");//自有房产地址
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
        if (lxrlist.isEmpty()) {
            HashMap<String, Object> rel = new HashMap<String, Object>();
            rel.put("rel_name", "");
            rel.put("rel_id_typ", "");
            rel.put("rel_id_no", "");
            rel.put("rel_relation", "");
            rel.put("rel_addr", "");
            rel.put("rel_mobile", "");
            rel.put("rel_emp_name", "");
            rellist.add(rel);
        } else {
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
            }
        }
        relMap.put("rel", rellist);
        apptmap.put("relList", relMap);

        String acqUrl = EurekaServer.ACQUIRER + "/api/appt/saveAppt";
        Map<String, Object> acqResponse = AcqUtil.getAcqResponse(acqUrl, AcqTradeCode.SAVE_APPT, super.getChannel(), super.getChannelNo(), "", tradeType, apptmap);
        if (acqResponse == null || acqResponse.isEmpty()) {
            return fail(RestUtil.ERROR_INTERNAL_CODE, "收单系统通信失败");
        }
        if (!CmisUtil.getIsSucceed(acqResponse)) {
            return fail(HttpUtil.getRetFlag(acqResponse), HttpUtil.getRetMsg(acqResponse));
        }

        // 调用共同还款人签章接口
        Map<String, Object> commonMap = cASignService.commonRepayPersonCaSignRequest(commonRepaymentPerson);
        if (!"00000".equals(commonMap.get("resultCode"))) {
            return fail("07", (String) commonMap.get("resultMsg"));
        }
        HashMap<String, Object> hm = new HashMap<String, Object>();
        hm.put("id", orderNo);
        return success(hm);
    }

    public Map<String, Object> getCommonRepaymentPerson(String applSeq) {
        String acqUrl = EurekaServer.ACQUIRER + "/api/appl/seletctApptInfo";
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("channelNo", super.getChannelNo());
        paramMap.put("applSeq", applSeq);
        Map<String, Object> acqResponse = AcqUtil.getAcqResponse(acqUrl, AcqTradeCode.SELECT_COMMON_PERSON, super.getChannel(), super.getChannelNo(), "", "", paramMap);
        if (acqResponse == null || acqResponse.isEmpty()) {
            return fail(RestUtil.ERROR_INTERNAL_CODE, "收单系统通信失败");
        }
        if (!CmisUtil.getIsSucceed(acqResponse)) {
            Map<String, Object> responseMap = (Map<String, Object>) acqResponse.get("response");
            Map<String, Object> headMap = (Map<String, Object>) responseMap.get("head");
            return fail((String) headMap.get("retFlag"), (String) headMap.get("retMsg"));
        }
        Map<String, Object> bodyMap = (Map<String, Object>) ((Map<String, Object>) acqResponse.get("response")).get("body");
        List<Map<String, Object>> apptList = (List<Map<String, Object>>) ((Map<String, Object>) bodyMap.get("apptList")).get("appt");
        List<CommonRepaymentPerson> personList = new ArrayList<>();
        apptList.forEach(apptMap -> personList.add(AcquirerCommonPersonEnum.acquirerMap2CommonPersonObject(apptMap, new CommonRepaymentPerson())));
        for (CommonRepaymentPerson person : personList) {
            person.setApplSeq(applSeq);
            person.setId(applSeq);

            // 客户基本信息查询
            Map<String, Object> smrzMap = crmService.queryMerchCustInfo(person.getName(), person.getIdNo());
            if (!HttpUtil.isSuccess(smrzMap) || StringUtils.isEmpty(smrzMap.get("body"))) {
                return fail("96", "实名认证失败！");
            }
            Map<String, Object> smrz = (Map<String, Object>) smrzMap.get("body");
            String mobile = StringUtils.isEmpty(smrz.get("mobile")) ? "" : (String) smrz.get("mobile");
            String name = StringUtils.isEmpty(smrz.get("custName")) ? "" : (String) smrz.get("custName");
            String idNo = StringUtils.isEmpty(smrz.get("certNo")) ? "" : (String) smrz.get("certNo");
            String cardNo = StringUtils.isEmpty(smrz.get("cardNo")) ? "" : (String) smrz.get("cardNo");
            String custNo = StringUtils.isEmpty(smrz.get("custNo")) ? "" : (String) smrz.get("custNo");
            String repayAcProvince = "";
            String repayAcCity = "";
            if (smrz.containsKey("acctProvince")) {
                repayAcProvince = StringUtils.isEmpty(smrz.get("acctProvince")) ? "" : (String) smrz.get("acctProvince");
            }
            if (smrz.containsKey("acctCity")) {
                repayAcCity = StringUtils.isEmpty(smrz.get("acctCity")) ? "" : (String) smrz.get("acctCity");
            }

            person.setName(name);
            person.setIdNo(idNo);
            person.setMobile(mobile);
            person.setCardNo(cardNo);
            //以查询的为准，没值的用实名认证的覆盖掉
            person.setRepayAcProvince(StringUtils.isEmpty(person.getRepayAcProvince()) ? repayAcProvince : person.getRepayAcProvince());
            person.setRepayAcCity(StringUtils.isEmpty(person.getRepayAcCity()) ? repayAcCity : person.getRepayAcCity());
            person.setCommonCustNo(custNo);
        }
        return success(personList);
    }

    @Override
    @Transactional
    public Map<String, Object> deleteCommonRepaymentPerson(String applSeq) {
        Map<String, Object> getCommonPerson = this.getCommonRepaymentPerson(applSeq);
        if (!HttpUtil.isSuccess(getCommonPerson)) {
            return getCommonPerson;
        }
        List<CommonRepaymentPerson> personList = (List<CommonRepaymentPerson>) getCommonPerson.get("body");
        if (personList == null || personList.isEmpty()) {
            return fail("10", "要删除的共同还款人不存在！");
        }
        CommonRepaymentPerson person = personList.get(0);
        //同步删除共同还款人影像
        if (!StringUtils.isEmpty(person.getCommonCustNo())) {
            attachService.deleteCommonImages(applSeq, person.getCommonCustNo());
        }
        //删除共同还款人信息
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("applSeq", applSeq);
        String acqUrl = EurekaServer.ACQUIRER + "/api/appt/deleteAppt";
        Map<String, Object> acqResponse = AcqUtil.getAcqResponse(acqUrl, AcqTradeCode.DELETE_APPT, super.getChannel(), super.getChannelNo(), "", "", paramMap);
        if (acqResponse == null || acqResponse.isEmpty()) {
            throw new RuntimeException("收单系统通信失败！");
        }
        if (!CmisUtil.getIsSucceed(acqResponse)) {
            throw new RuntimeException("删除共同还款人失败!");
        }
        return success();
    }

    @Override
    public Map<String, Object> countCommonRepaymentPerson(String applSeq) {
        String acqUrl = EurekaServer.ACQUIRER + "/api/appl/seletctApptInfo";
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("channelNo", super.getChannelNo());
        paramMap.put("applSeq", applSeq);
        Map<String, Object> acqResponse = AcqUtil.getAcqResponse(acqUrl, AcqTradeCode.SELECT_COMMON_PERSON, super.getChannel(), super.getChannelNo(), "", "", paramMap);
        if (acqResponse == null || acqResponse.isEmpty()) {
            return fail(RestUtil.ERROR_INTERNAL_CODE, "收单系统通信失败");
        }
        if (!CmisUtil.getIsSucceed(acqResponse)) {
            return (Map<String, Object>) acqResponse.get("response");
        }
        Map<String, Object> bodyMap = (Map<String, Object>) ((Map<String, Object>) acqResponse.get("response")).get("body");
        List<Map<String, Object>> apptList = (List<Map<String, Object>>) ((Map<String, Object>) bodyMap.get("apptList")).get("appt");
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("count", apptList.size());
        return success(resultMap);
    }

    @Override
    public Map<String, Object> updateCommonRepaymentPerson(CommonRepaymentPerson commonRepaymentPerson) {
        //身份证号将小写变成大写
        String idNo = commonRepaymentPerson.getIdNo();
        if (!StringUtils.isEmpty(idNo)) {
            commonRepaymentPerson.setIdNo(idNo.toUpperCase());
        }
        AppOrdernoTypgrpRelation relation = appOrdernoTypgrpRelationRepository.findOne(commonRepaymentPerson.getOrderNo());
        if (relation == null) {
            return fail("63", "该订单号不存在！");
        }
        commonRepaymentPerson.setApplSeq(relation.getApplSeq());
        try {
            Map<String, Object> getPersonResult = this.getCommonRepaymentPerson(relation.getApplSeq());
            if (!HttpUtil.isSuccess(getPersonResult)) {
                return getPersonResult;
            }
            List personList = (List) getPersonResult.get("body");
            if (personList.isEmpty()) {
                return fail("64", "原共同还款人信息异常，修改失败！");
            }
            CommonRepaymentPerson oldPerson = (CommonRepaymentPerson) personList.get(0);
            if (!Objects.equals(oldPerson.getCommonCustNo(), commonRepaymentPerson.getCommonCustNo())) {
                // 删除原共同还款人影像
                attachService.deleteCommonImages(relation.getApplSeq(), oldPerson.getCommonCustNo());
            }
            Map<String, Object> addPersonResult = this.addCommonRepaymentPerson(commonRepaymentPerson, "2");
            if (!HttpUtil.isSuccess(addPersonResult)) {
                return addPersonResult;
            }
            return success();
        } catch (Exception e) {
            logger.error("修改共同还款人发生异常：" + e.getMessage());
            e.printStackTrace();
            return fail(RestUtil.ERROR_INTERNAL_CODE, RestUtil.ERROR_INTERNAL_MSG);
        }
    }


}
