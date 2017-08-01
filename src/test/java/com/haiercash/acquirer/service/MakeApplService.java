package com.haiercash.acquirer.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.haiercash.commons.rest.Rest;
import com.haiercash.commons.support.ServiceException;
import com.haiercash.payplatform.service.BaseService;

/**
 * Created by  on 2017/3/20.
 */
@Service
public class MakeApplService extends BaseService {
    @Autowired
    private LcApplDao lcApplDao;//贷款申请主表dao
    @Autowired
    private LcApplExtDao lcApplExtDao;//贷款申请扩展表dao
    @Autowired
    private LcApplGoodsDao lcApplGoodsDao;//商品dao
    @Autowired
    private LcApplAccInfoDao lcApplAccInfoDao;//账号信息表dao
    @Autowired
    private LcApptRelDao lcApptRelDao;//联系人dao
    @Autowired
    private LcApptAssetDao lcApptAssetDao;//申请人资产负债信息表dao
    @Autowired
    private LcApplApptDao lcApplApptDao;
    @Autowired
    private LcApptIndivDao lcApptIndivDao;//申请人信息表dao
    @Autowired
    private LcApptExtDao lcApptExtDao;//贷款申请人扩展表dao
    @Autowired
    private LcApplApptService lcApplApptService;//贷款申请人主表daoLC_APPT_REL
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    private Rest rest;
    @Autowired
    private LcApplService lcApplService;

    /**
     * 生成新的贷款申请，复制现有数据。贷款基本信息和申请人信息从同一笔贷款复制
     * @param applSeq 要复制的贷款申请流水号
     * @return
     */
    public HashMap<String, Object> makeAppl(Long applSeq) {
        return makeAppl(applSeq, applSeq);
    }

    /**
     * 生成新的贷款申请，复制现有数据。贷款基本信息和申请人信息从不同的贷款复制
     * @param applSeq 要复制的贷款基本信息申请流水号
     * @param apptApplSeq 要复制的申请人申请流水号
     * @return
     */
    public HashMap<String, Object> makeAppl(Long applSeq, Long apptApplSeq) {
        HashMap<String, Object> request = makeRequest(applSeq, apptApplSeq);
        String url = "http://10.164.197.232:8090/api/appl/saveLcAppl";// 这里使用的是ribbon配置，eureka无法使用
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
        headers.setContentType(type);
        headers.add("Accept", MediaType.APPLICATION_JSON.toString());
       // JSONObject jsonObj = new JSONObject();
      //  HttpEntity<String> formEntity = new HttpEntity<String>(request.toString(), headers);
       // String result = restTemplate.postForObject(url, formEntity, String.class);
        ResponseEntity<HashMap> response = rest.exchange(restTemplate, url, HttpMethod.POST, request, HashMap.class);
        return response.getBody();
    }

    /**
     * 提交贷款申请
     * @param applSeq 要提交的贷款申请流水号
     * @return
     */
//    public HashMap<String, Object> commitAppl(Long applSeq) {
//        CmisResponse cmisResponse = lcApplService.commitAppl(applSeq, "");
//        HashMap<String, Object> response = new HashMap<>();
//        response.put("response", cmisResponse.getResponse());
//        response.put("result", cmisResponse.formatErrMsg());
//        return response;
//    }

    /**
     * 根据已有贷款申请生成进件请求报文
     * @param applSeq 要复制的贷款基本信息申请流水号
     * @param apptApplSeq 要复制的申请人申请流水号
     * @return
     */
    private HashMap<String, Object> makeRequest(Long applSeq, Long apptApplSeq) {

        // 封装完整请求：
        HashMap<String, Object> headMap = makeHeadMap(applSeq);
        HashMap<String, Object> requestMap = new HashMap<>();
        requestMap.put("head", headMap);
        requestMap.put("body", makeBodyMap(applSeq,apptApplSeq));
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("request", requestMap);

        logger.debug("渠道进件请求:\n" + new JSONObject(paramMap));
        return paramMap;
    }

    /**
     * 根据已有贷款申请生成请求报文头
     * @param applSeq 要复制的申请流水号
     * @return
     */
    private HashMap<String, Object> makeHeadMap(Long applSeq) {
        HashMap<String, Object> headMap = new HashMap<>();
        LcAppl lcAppl = lcApplDao.selectByPrimaryKey(applSeq);
        headMap.put("cooprCode", lcAppl.getCooprCde());//"22"
        headMap.put("tradeTime", new SimpleDateFormat("HH:mm:ss").format(new Date()));
        headMap.put("autoFlag", "Y");
        headMap.put("sysFlag", lcAppl.getCreApp());//"13"
        headMap.put("channelNo", lcAppl.getChannelNo());
        headMap.put("serno", (new Date()).getTime() + "" + (int)(Math.random() * 100.0D));
        headMap.put("tradeCode", "100001");
        headMap.put("tradeDate", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        headMap.put("tradeType", "1");
        headMap.put("applCde", "");
        return headMap;
    }
    private HashMap<String,Object>makeBodyMap(Long applSeq, Long apptApplSeq){
        HashMap<String, Object> map = new HashMap<>();
        //贷款主信息
        map.putAll(makeApplMap(applSeq));
        //商品列表
        map.put("goodsList", makeGoodsMap(applSeq));

        //主申请人信息
        map.putAll(makeMainApptMap(apptApplSeq));
        //账号信息
        map.putAll(makeAccMap(apptApplSeq));
        // apptList开始
        map.put("apptList", makeApptMap(apptApplSeq));
        return  map;
    }

    /**
     * 根据已有贷款申请生成贷款申请主信息
     * @param applSeq 要复制的申请流水号
     * @return
     */
    private HashMap<String, Object> makeApplMap(Long applSeq) {
        HashMap<String, Object> applMap = new HashMap<>();
        LcAppl lcAppl = lcApplDao.selectByPrimaryKey(applSeq);//贷款申请主表
        if (lcAppl == null) {
            throw new ServiceException("99", "贷款申请不存在：applSeq=" + applSeq);
        }
        LcApplExt lcApplExt = lcApplExtDao.selectByPrimaryKey(applSeq);//贷款申请扩展表

        applMap.put("cont_zone", lcAppl.getCooprZone());
        applMap.put("cont_tel", lcAppl.getCooprTel());
        applMap.put("cont_sub", lcAppl.getCooprSub());
        applMap.put("typ_grp", lcAppl.getTypGrp());
        applMap.put("prom_cde", lcAppl.getLoanProm());
        //applMap.put("prom_desc", "");//todo 营销专案名称
        applMap.put("typ_seq", lcAppl.getTypSeq());
        applMap.put("typ_cde", lcAppl.getLoanTyp());
        applMap.put("typ_desc", lcAppl.getLoanTyp());// 贷款品种名称
        applMap.put("apply_dt", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));//lcAppl.getApplyDt()
        applMap.put("crt_dt", lcAppl.getCrtDt());// 登记日期
        applMap.put("pro_pur_amt", lcAppl.getProPurAmt());
        applMap.put("fst_pct", lcAppl.getFstPct());
        applMap.put("fst_pay", lcAppl.getFstPay());// 首付金额
        applMap.put("apply_amt", lcAppl.getApplyAmt());// 申请金额
        applMap.put("apply_tnr", lcAppl.getApplyTnr());
        applMap.put("apply_tnr_typ", lcAppl.getApplyTnrTyp());
        applMap.put("apply_tnr", lcAppl.getApplyTnr());
        applMap.put("purpose", "OTH");
        applMap.put("other_purpose", lcAppl.getOtherPurpose()+new Date());
        applMap.put("month_repay", lcApplExt.getPayMthAmt());
        applMap.put("oper_goods_typ", lcAppl.getOperGoodsTyp());
        applMap.put("mtd_cde", lcAppl.getMtdCde());
        applMap.put("mtd_mode", "");//todo 利率模式
        applMap.put("repc_opt", "");//todo 利率模式
        applMap.put("loan_freq", lcAppl.getLoanFreq());
        applMap.put("due_day_opt", lcAppl.getDueDayOpt());
        applMap.put("due_day", lcAppl.getDueDay());
        applMap.put("doc_channel", lcAppl.getDocChannel());
        //销售代表相关信息
        applMap.put("saler_name", lcAppl.getSalerName());
        applMap.put("saler_mobile", lcAppl.getSalerMobile());
        applMap.put("coopr_cde", lcAppl.getCooprCde());
        applMap.put("coopr_name", lcAppl.getCooprName());
        applMap.put("saler_cde", lcAppl.getCrtUsr());
        // 客户经理
        applMap.put("operator_name", lcAppl.getOperatorName());
        applMap.put("operator_cde", lcAppl.getOperatorCde());
        applMap.put("operator_tel", lcAppl.getOperatorTel());
        applMap.put("crt_usr", lcAppl.getCrtUsr());
        applMap.put("app_in_advice", lcAppl.getAppInAdvice());
        //applMap.put("crd_flag", "Y");// 是否需要额度申请加支用
        //applMap.put("grt_type", "");// todo 担保方式
        applMap.put("grt_coopr_cde", "");// todo 商户/合同机构代码
        applMap.put("grt_coopr_name", lcAppl.getCooprName());// 商户/合作机构名称
        //applMap.put("grt_corporate_name", "");// todo 法人
        //applMap.put("grt_corporate_tel", "");// todo 法人电话
        //applMap.put("grt_coopr_contact_email", "");// todo 法人邮箱
        //applMap.put("grt_ind_officer", "");// todo 工贸客户经理
        //applMap.put("grt_coopr_tnr", "");// todo 与担保人合作年限
        applMap.put("expectCredit", lcAppl.getExpectCredit());// 期望额度
        return applMap;
    }

    /**
     * 根据已有贷款申请生成商品信息
     * @param applSeq 要复制的申请流水号
     * @return
     */
    private HashMap<String, Object> makeGoodsMap(Long applSeq) {
        HashMap<String, Object> goodsMap = new HashMap<>();
        LcApplGoods lcApplGoods = new LcApplGoods();
        lcApplGoods.setApplSeq(applSeq);
        List<LcApplGoods> list = lcApplGoodsDao.select(lcApplGoods);
        if (list.size() > 0) {
            List goodslist = new ArrayList();
            for (LcApplGoods goods : list) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("goods_brand", goods.getGoodsBrand());
                map.put("goods_kind", goods.getGoodsKind());
                map.put("goods_name", goods.getGoodsName());
                map.put("goods_model", goods.getGoodsModel());
                map.put("goods_num", goods.getGoodsNum());
                map.put("goods_price", goods.getGoodsPrice());
                goodslist.add(map);
            }
            goodsMap.put("good", goodslist);
        }
        return goodsMap;
    }

    /**
     * 根据已有贷款申请生成账户信息
     * @param applSeq 要复制的申请流水号
     * @return
     */
    private HashMap<String, Object> makeAccMap(Long applSeq) {
        HashMap<String, Object> accMap = new HashMap<>();
        List<LcApplAccInfo> accList = lcApplAccInfoDao.selectByApplSeq(applSeq);
        for (LcApplAccInfo acc : accList) {
            //银行卡信息 01.放款账号 02.还款账号
            if (Objects.equals("02", acc.getApplAcKind())) {
                logger.info("还款卡信息==》"+acc);
                accMap.put("repay_appl_ac_nam", acc.getApplAcNam());
                accMap.put("repay_appl_card_no", acc.getApplAcNo());
                accMap.put("repay_acc_bank_cde", acc.getApplAcBank());
                accMap.put("repay_acc_bank_name", acc.getApplAcBankDesc());
                accMap.put("repay_ac_province", acc.getAcProvince());
                accMap.put("repay_ac_city", acc.getAcCity());
            } else if (Objects.equals("01", acc.getApplAcKind())) {
                logger.info("放款卡信息==》"+acc);
                accMap.put("appl_ac_typ", acc.getApplAcTyp());
                accMap.put("appl_ac_nam", acc.getApplAcNam());
                accMap.put("appl_card_no", acc.getApplCardNo());
                accMap.put("appl_ac_no", acc.getApplAcNo());
                accMap.put("acc_bank_cde", acc.getApplAcBank());
                accMap.put("acc_bank_name", acc.getApplAcBankDesc());
                accMap.put("ac_province", acc.getAcProvince());
                accMap.put("ac_city", acc.getAcCity());
                accMap.put("appl_ac_bch", acc.getApplAcBch());
            }
        }
        return accMap;
    }

    /**
     * 根据已有贷款申请生成主申请人信息，用于填充贷款基本信息
     * @param applSeq 要复制的申请流水号
     * @return
     */
    private HashMap<String, Object> makeMainApptMap(Long applSeq) {
        HashMap<String, Object> mainApptMap = new HashMap<>();
        List<LcApplAppt> listAppt = lcApplApptService.seletctApptByApplSeq(String.valueOf(applSeq),"01");//获取主申请人
        LcApplAppt zAppl_Appt = listAppt.get(0);//主申请人主表信息
        mainApptMap.put("id_typ", zAppl_Appt.getIdTyp());
        mainApptMap.put("id_no", zAppl_Appt.getIdNo());
        mainApptMap.put("cust_name", zAppl_Appt.getCustName());
        LcApptIndiv zAppl_Indiv = lcApptIndivDao.selectByPrimaryKey(zAppl_Appt.getApptSeq());
        mainApptMap.put("indiv_mobile", zAppl_Indiv.getIndivMobile());
        mainApptMap.putAll(makeAssetMap(zAppl_Indiv.getApptSeq()));
        return mainApptMap;
    }

    /**
     * 根据已有贷款申请生成申请人信息
     * @param applSeq 要复制的申请流水号
     * @return
     */
    private HashMap<String, Object> makeApptMap(Long applSeq) {
        HashMap<String, Object> apptMap = new HashMap<>();
        List<Map<String, Object>> apptList = new ArrayList<>();
        List<LcApplAppt> allAppts = lcApplApptDao.selectByApplSeq(String.valueOf(applSeq));
        for (LcApplAppt appAppt : allAppts) {
            LcApptIndiv lcApptIndivOne = lcApptIndivDao.selectByPrimaryKey(appAppt.getApptSeq());
            LcApptExt lcApptExtOne = lcApptExtDao.selectByPrimaryKey(appAppt.getApptSeq());
            HashMap<String, Object> map = new HashMap<>();
            map.put("appt_typ", appAppt.getApptTyp());// 申请人类型 01 - 主申请人
            map.put("appt_relation", appAppt.getApptRelation());//与主申请人的关系
            map.put("appt_id_typ", appAppt.getIdTyp());//证件类型
            map.put("appt_id_typ_oth", appAppt.getIdTypOth());//证件类型其他备注
            map.put("appt_id_no", appAppt.getIdNo());//证件号码
            map.put("appt_cust_name", appAppt.getCustName());//姓名
            map.put("appt_indiv_sex", lcApptIndivOne.getIndivSex());//性别
            map.put("appt_start_date", appAppt.getApptStartDate());//出生日期
            map.put("indiv_marital", lcApptIndivOne.getIndivMarital());//婚姻状况
            map.put("indiv_edu", lcApptIndivOne.getIndivEdu());//最高学历
            //户籍地址
            map.put("appt_reg_province", lcApptIndivOne.getRegProvince());//户籍地址省
            map.put("appt_reg_city", lcApptIndivOne.getRegCity());//户籍地址市
            //apptmap.put("appt_reg_province", lcApptIndivOne.getRegProvince());
            //apptmap.put("appt_reg_city", lcApptIndivOne.getRegCity());
            map.put("live_info", lcApptIndivOne.getLiveInfo());//现住房情况
            map.put("live_province", lcApptIndivOne.getLiveProvince());//申请人信息表中 现住房省
            map.put("live_city", lcApptIndivOne.getLiveCity());//申请人信息表中 现住房省
            map.put("live_area", lcApptIndivOne.getLiveArea());//申请人信息表中 现住房区
            map.put("live_addr", lcApptIndivOne.getLiveAddr());//申请人信息表中 现住房地址
            //apptmap.put("live_zip", "");// custExtInfoBodyMap.get("liveZip")  申请人信息表中 现住房邮编
            map.put("live_zip", lcApptIndivOne.getLiveZip());
            map.put("live_mj", lcApptIndivOne.getLiveMj());//申请人信息表中 现住房面积
            map.put("ppty_live", lcApptIndivOne.getPptyLive());// 自有房产地址
            map.put("ppty_province", lcApptIndivOne.getPptyProvince());// pptyProvince
            map.put("ppty_city", lcApptIndivOne.getPptyCity());// pptyCity
            map.put("ppty_area", lcApptIndivOne.getPptyArea());// pptyArea
            map.put("ppty_addr", lcApptIndivOne.getPptyAddr());// pptyAddr
            map.put("ppty_zip", lcApptIndivOne.getPptyZip());//pptyZip
            map.put("ppty_mj", lcApptIndivOne.getPptyMj());//自有房面积
            map.put("indiv_mobile", lcApptIndivOne.getIndivMobile());
            map.put("indiv_fmly_zone", lcApptIndivOne.getIndivFmlyZone());//indivFmlyZone
            map.put("indiv_fmly_tel", lcApptIndivOne.getIndivFmlyTel());//indivFmlyTel
            map.put("local_resid", lcApptIndivOne.getLocalResid());//localResid
            //apptmap.put("local_resid", lcApptIndivOne.getLocalResid());
            map.put("indiv_email", lcApptIndivOne.getIndivEmail());//indivEmail
            map.put("annual_earn", lcApptIndivOne.getIndivYInc());//年收入（税后） indivYInc
            map.put("household_own_rel", lcApptExtOne.getHolderRelation());// 户主与申请人的关系 holderRelation
            map.put("max_crdt_card", lcApptExtOne.getCredCardAmt());//credCardAmt
            map.put("iss_bank", lcApptExtOne.getCardBankNum());//待确认cardBankNum
            map.put("indiv_mth_inc", lcApptIndivOne.getIndivMthInc());//月均收入 indivMthInc
            map.put("position_opt", lcApptIndivOne.getPositionOpt());//待确认
            map.put("live_year", lcApptExtOne.getLiveYear());//本地已居住时间（年） liveYear
            map.put("indiv_dep_no", lcApptIndivOne.getIndivDepNo());//供养人数（子女） indivDepNo
            map.put("mail_opt", lcApptIndivOne.getMailOpt());//送货地址选项
            map.put("mail_province", lcApptIndivOne.getMailProvince());//邮寄地址省
            map.put("mail_city", lcApptIndivOne.getMailCity());//邮寄地址市
            map.put("mail_area", lcApptIndivOne.getMailArea());//邮寄地址市
            map.put("mail_addr", lcApptIndivOne.getMailAddr());//邮寄地址区
            map.put("study_mth", lcApptExtOne.getStudyMth());//学习方式
            map.put("studying_deg", lcApptExtOne.getStudyingDeg());//在读学历
            map.put("school_name", lcApptExtOne.getSchoolName());//学校名称
            map.put("study_major", lcApptExtOne.getStudyMajor());//专业
            map.put("school_kind", lcApptExtOne.getSchoolKind());//学校性质
            map.put("school_leng", lcApptExtOne.getSchoolLeng());//学制
            map.put("geade", lcApptExtOne.getGeade());//年级
            map.put("emp_reg_name", lcApptExtOne.getEmpRegName());//企业名称
            map.put("emp_reg_dt", lcApptExtOne.getEmpRegDt());//成立日期
            map.put("manage_addr", lcApptExtOne.getManageAddr());//经营地址
            map.put("manage_province", lcApptExtOne.getManageProvince());//经营地址省
            map.put("manage_city", lcApptExtOne.getManageCity());//经营地址市
            map.put("manage_area", lcApptExtOne.getManageArea());//经营地址区
            map.put("emp_reg_rel_tel", lcApptExtOne.getEmpRegRelTel());//企业联系电话
            map.put("emp_reg_add_zip", lcApptExtOne.getEmpRegAddZip());//企业地址邮编
            map.put("emp_reg_num", lcApptExtOne.getEmpRegNum());//企业注册号
            map.put("inc_cde", lcApptExtOne.getIncCde());//所属工贸
            map.put("indiv_opt", lcApptExtOne.getIndivOpt());//申请人身份
            map.put("share_hold_pct", lcApptExtOne.getShareHoldPct());//持股比例
            map.put("manage_typ", lcApptExtOne.getManageTyp());//经营实体类型
            map.put("manage_main_biz", lcApptExtOne.getManageMainBiz());//主营业务
            map.put("mth_turnover", lcApptExtOne.getMthTurnover());//月均营业额（万元）
            map.put("manage_no", lcApptExtOne.getManageNo());//营业执照号
            map.put("pur_sale_cont_no", lcApptExtOne.getPurSaleContNo());//购销合同编号
            map.put("indiv_emp_name", lcApptIndivOne.getIndivEmpName());//现单位名称
            map.put("indiv_branch", lcApptIndivOne.getIndivBranch());//所在部门 indivBranch
            map.put("indiv_emp_typ", lcApptIndivOne.getIndivEmpTyp());//现单位性质  indivEmpTyp
            map.put("indiv_emp_yrs", lcApptIndivOne.getIndivEmpYrs());// custExtInfoBodyMap.get("serviceYears") 现单位工龄 indivEmpYrs
            map.put("indiv_mobile", lcApptIndivOne.getIndivMobile());
            map.put("indiv_emp_province", lcApptIndivOne.getIndivEmpProvince());//现单位地址省
            map.put("indiv_emp_city", lcApptIndivOne.getIndivEmpCity());//现单位地址市
            map.put("indiv_emp_area", lcApptIndivOne.getIndivEmpArea());//现单位地址区
            map.put("empaddr", lcApptIndivOne.getIndivEmpAddr());//现单位地址
            map.put("indiv_emp_zip", lcApptIndivOne.getIndivEmpZip());//现单位邮编
            map.put("indiv_emp_zone", lcApptIndivOne.getIndivEmpZone());
            map.put("indiv_emp_tel", lcApptIndivOne.getIndivEmpTel());
            map.put("indiv_emp_tel_sub", lcApptIndivOne.getIndivEmpTelSub());//办公电话分机
            map.put("indiv_emp_hr_zone", lcApptIndivOne.getIndivEmpHrZone());//办公电话2区号
            map.put("indiv_emp_hr_phone", lcApptIndivOne.getIndivEmpHrPhone());//办公电话2
            map.put("indiv_emp_hr_sub", lcApptIndivOne.getIndivEmpHrSub());//办公电话2分机
            map.put("indiv_position", lcApptIndivOne.getIndivPosition());//职务
            map.put("indiv_work_yrs", lcApptIndivOne.getIndivWorkYrs());// custExtInfoBodyMap.get("workYrs") 总工龄
            map.put("spouse_name", lcApptExtOne.getSpouseName());// 如果有夫妻关系联系人，会覆盖此参数  配偶姓名 如果申请人已婚，必输
            map.put("spouse_id_typ", lcApptExtOne.getSpouseIdTyp());//配偶姓名
            map.put("spouse_id_no", lcApptExtOne.getSpouseIdNo());//配偶证件号码
            map.put("spouse_emp", lcApptExtOne.getSpouseEmp());//配偶工作单位名称
            map.put("spouse_mobile", lcApptExtOne.getSpouseMobile());// 如果有夫妻关系联系人，会覆盖此参数 配偶手机号 如果申请人已婚，必输
            map.put("spouse_branch", lcApptExtOne.getSpouseBranch());//配偶工作单位所在部门
            map.put("spouse_emp_zone", lcApptExtOne.getSpouseEmpZone());//配偶办公电话区号
            map.put("spouse_emp_tel", lcApptExtOne.getSpouseEmpTel());//配偶办公电话
            map.put("spouse_emp_tel_sub", lcApptExtOne.getSpouseEmpTelSub());//配偶办公电话分机
            map.put("spouse_emp_province", lcApptExtOne.getSpouseEmpProvince());//配偶单位地址省
            map.put("spouse_emp_city", lcApptExtOne.getSpouseEmpCity());//配偶单位地址市
            map.put("spouse_emp_area", lcApptExtOne.getSpouseEmpArea());//配偶单位地址区
            map.put("spouse_emp_addr", lcApptExtOne.getSpouseEmpAddr());//配偶单位地址
            map.put("sp_mth_inc", lcApptIndivOne.getSpMthInc());//月均收入
            map.put("spouse_pay_ind", lcApptExtOne.getSpousePayInd());//是否为共同还款人
            map.put("ppty_live_opt", lcApptExtOne.getPptyLiveOpt());//自有房产地址
            map.put("ppty_loan_ind", lcApptExtOne.getPptyLoanInd());//是否按揭
            map.put("ppty_righ_name", lcApptExtOne.getPptyRighName());//房屋产权人
            map.put("ppty_amt", lcApptExtOne.getPptyAmt());//购买价格
            map.put("ppty_live_province", lcApptExtOne.getPptyLiveProvince());//省 如果房产地址选项为"其他"，此字段必输
            map.put("ppty_live_city", lcApptExtOne.getPptyLiveCity());//市 如果房产地址选项为"其他"，此字段必输
            map.put("ppty_live_area", lcApptExtOne.getPptyLiveArea());//区 如果房产地址选项为"其他"，此字段必输
            map.put("ppty_live_addr", lcApptExtOne.getPptyLiveAddr());//详细地址 如果房产地址选项为"其他"，此字段必输
            map.put("ppty_loan_amt", lcApptExtOne.getPptyLoanAmt());//按揭金额 如果是否按揭选择“是”，此字段必输
            map.put("ppty_loan_year", lcApptExtOne.getPptyLoanYear());//按揭周期（年）
            map.put("ppty_loan_bank", lcApptExtOne.getPptyLoanBank());//按揭银行
            map.put("relList", makeRelMap(appAppt.getApptSeq()));
            apptList.add(map);
        }
        apptMap.put("appt", apptList);
        return apptMap;
    }

    /**
     * 根据已有贷款申请生成联系人信息
     * @param apptSeq 要复制的申请人流水号
     * @return
     */
    private HashMap<String, Object> makeRelMap(Long apptSeq) {
        HashMap<String, Object> relMap = new HashMap<>();
        List<LcApptRel> lcApptRelList = lcApptRelDao.selectByApptSeq(apptSeq);
        List rellist = new ArrayList();
        for (LcApptRel obj : lcApptRelList) {
            HashMap<String, Object> rel = new HashMap<>();
            rel.put("rel_name", obj.getRelName());//联系人姓名
            rel.put("rel_id_typ", obj.getIdTyp());//证件类型
            rel.put("rel_id_no", obj.getIdNo());//证件号码
            rel.put("rel_relation", obj.getRelRelation());//联系人与申请人关系
            rel.put("rel_addr", obj.getRelAddr());//联系地址
            rel.put("rel_mobile", obj.getRelMobile());//联系电话
            rel.put("rel_emp_name", obj.getRelEmpName());//单位名称
            // 配偶姓名 spouse_name  婚姻状况 indivMarital
            //apptmap.put("spouse_name", rel.get("rel_name"));//配偶姓名 如果申请人已婚，必输
            // 配偶电话 spouse_mobile
            //apptmap.put("spouse_mobile", rel.get("rel_mobile"));//配偶手机号 如果申请人已婚，必输
            rellist.add(rel);
        }
        relMap.put("rel", rellist);
        return relMap;
    }

    /**
     * 根据已有贷款申请生成主申请人资产信息
     * @param apptSeq 要复制的主申请人流水号
     * @return
     */
    private HashMap<String, Object> makeAssetMap(Long apptSeq) {
        HashMap<String, Object> assetMap = new HashMap<>();
        LcApptAsset lcApptAsset = new LcApptAsset();
        lcApptAsset.setApptSeq(apptSeq);
        List<LcApptAsset> lcApptAssetList = lcApptAssetDao.select(lcApptAsset);
        if (lcApptAssetList.size() > 0) {
            //取最后一条记录  TODO 这里应按流水号排序，可能存在多条记录
            LcApptAsset lcApptAssetOne = lcApptAssetList.get(lcApptAssetList.size() - 1);
            assetMap.put("asset_kind", lcApptAssetOne.getAssetKind());// 资产性质
            assetMap.put("asset_typ", lcApptAssetOne.getAssetTyp());// 资产类型
            assetMap.put("asset_amt", lcApptAssetOne.getAssetAmt());// 金额
            assetMap.put("asset_mth_amt", lcApptAssetOne.getAssetMthAmt());// 月还款额
            assetMap.put("asset_re_amt", lcApptAssetOne.getAssetReAmt());// 贷款余额
        }
        return assetMap;
    }
}
