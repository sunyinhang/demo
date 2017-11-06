package com.haiercash.payplatform.service.impl;

import com.haiercash.payplatform.common.data.AppOrder;
import com.haiercash.payplatform.common.data.CommonRepaymentPerson;
import com.haiercash.payplatform.config.EurekaServer;
import com.haiercash.payplatform.service.BaseService;
import com.haiercash.payplatform.service.CmisService;
import com.haiercash.payplatform.utils.ConstUtil;
import com.haiercash.payplatform.utils.HttpUtil;
import com.haiercash.payplatform.utils.IdCardUtils;
import com.haiercash.payplatform.utils.RestUtil;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * cmis service impl.
 * @author Liu qingxiang
 * @since v1.0.1
 */
@Service
public class CmisServiceImpl extends BaseService implements CmisService{


    @Override
    public Map<String, Object> findPLoanTyp(String typCde) {
        if (StringUtils.isEmpty(typCde)) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "贷款品种不能未空");
        }
        String typCdeUrl = EurekaServer.CMISPROXY + "/api/pLoanTyp/find?typCde=" + typCde;
        logger.info("==> CMISPROXY:" + typCdeUrl);
        String typCdeJson = HttpUtil.restGet(typCdeUrl);
        logger.info("<== CMISPROXY:" + typCdeJson);
        if (StringUtils.isEmpty(typCdeJson)) {
            return fail("75", "无效的贷款品种");
        }
        return success(HttpUtil.json2DeepMap(typCdeJson));
    }


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
            apptmap.put("appt_indiv_sex", IdCardUtils.getGenderByIdCard(idNo));
            apptmap.put("appt_start_date", IdCardUtils.getYearByIdCard(idNo) + "-"
                    + IdCardUtils.getMonthByIdCard(idNo) + "-" + IdCardUtils.getDateByIdCard(idNo));
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
            lxrlist = (List) lxrMap.get("body");

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

}
