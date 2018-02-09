package com.haiercash.payplatform.service.impl;

import com.haiercash.core.lang.DateUtils;
import com.haiercash.core.util.IDCard;
import com.haiercash.payplatform.common.dao.AppOrdernoTypgrpRelationDao;
import com.haiercash.payplatform.common.data.AppOrder;
import com.haiercash.payplatform.common.data.AppOrderGoods;
import com.haiercash.payplatform.common.data.AppOrdernoTypgrpRelation;
import com.haiercash.payplatform.common.data.CommonRepaymentPerson;
import com.haiercash.payplatform.common.enums.AcquirerApptEnum;
import com.haiercash.payplatform.common.enums.AcquirerEnum;
import com.haiercash.payplatform.common.enums.AcquirerGoodsEnum;
import com.haiercash.payplatform.service.AcquirerService;
import com.haiercash.payplatform.service.AppManageService;
import com.haiercash.payplatform.service.CmisService;
import com.haiercash.payplatform.service.CommonRepaymentPersonService;
import com.haiercash.payplatform.service.CrmService;
import com.haiercash.payplatform.service.OrderService;
import com.haiercash.payplatform.utils.AcqTradeCode;
import com.haiercash.payplatform.utils.AcqUtil;
import com.haiercash.payplatform.utils.ChannelType;
import com.haiercash.payplatform.utils.CmisUtil;
import com.haiercash.payplatform.utils.FormatUtil;
import com.haiercash.payplatform.utils.ReflactUtils;
import com.haiercash.spring.config.EurekaServer;
import com.haiercash.spring.service.BaseService;
import com.haiercash.spring.util.BusinessException;
import com.haiercash.spring.util.ConstUtil;
import com.haiercash.spring.util.HttpUtil;
import org.apache.commons.lang.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * acquirer service impl.
 *
 * @author Liu qingxiang
 * @since v1.0.1
 */
@Service
public class AcquirerServiceImpl extends BaseService implements AcquirerService {

    @Autowired
    private OrderService orderService;
    @Autowired
    private AppManageService appManageService;
    @Autowired
    private CrmService crmService;
    @Autowired
    private CmisService cmisService;
    @Autowired
    private CommonRepaymentPersonService commonRepaymentPersonService;
    @Autowired
    private AppOrdernoTypgrpRelationDao appOrdernoTypgrpRelationDao;

    @Override
    public Map<String, Object> getOrderFromAcquirer(String applSeq, String channel, String channelNo, String cooprCde,
                                                    String tradeType, String flag) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("applSeq", applSeq);
        paramMap.put("channelNo", channelNo);

        if (StringUtils.isEmpty(applSeq)) {
            logger.info("当前订单未进行渠道进件，返回空map");
            return new HashMap<>();
        }

        Map<String, Object> acquierOrder;
        if ("1".equals(flag)) {
            acquierOrder = AcqUtil.getAcqResponse(EurekaServer.ACQUIRER + "/api/appl/selectApplInfo",
                    AcqTradeCode.SELECT_APPL_INFO, channel, channelNo, cooprCde, tradeType, paramMap);
        } else {
            acquierOrder = AcqUtil.getAcqResponse(EurekaServer.ACQUIRER + "/api/appl/selectApplInfoApp",
                    AcqTradeCode.SELECT_APP_APPL_INFO, channel, channelNo, cooprCde, tradeType, paramMap);
        }
        if (StringUtils.isEmpty(acquierOrder)) {
            logger.info("收单系统获取贷款详情失败, applSeq=" + applSeq);
            return fail("51", "收单系统获取贷款详情失败");
        }

        return (Map<String, Object>) acquierOrder.get("response");
    }

    // 映射贷款信息商品信息至apporder对象
    private AppOrder acquirerGoodMap2OrderObject(Map<String, Object> acquirer, AppOrder order) {
        if (StringUtils.isEmpty(acquirer.get("goodsList"))) {
            logger.info("收单系统贷款详情无商品信息：" + acquirer.get("applSeq"));
            return order;
        } else {
            Map<String, Object> goodsList = (Map<String, Object>) acquirer.get("goodsList");
            if (goodsList == null || goodsList.get("good") == null) {
                return order;
            }
            List<AppOrderGoods> appOrderGoodsList = new ArrayList<>();
            List<Map<String, Object>> good = (List<Map<String, Object>>) goodsList.get("good");
            if (good.size() > 0) {
                for (Map<String, Object> map : good) {
                    map.keySet().removeIf((key) -> AcquirerGoodsEnum.getOrderAttr(key) == null);
                    AppOrderGoods appOrderGoods = new AppOrderGoods();
                    map.forEach(
                            (key, value) -> ReflactUtils.setProperty(appOrderGoods, AcquirerGoodsEnum.getOrderAttr(key),
                                    FormatUtil.checkValueType(AcquirerGoodsEnum.getOrderAttr(key), value,
                                            AppOrderGoods.class)));
                    appOrderGoodsList.add(appOrderGoods);
                }
            }
            order.setAppOrderGoodsList(appOrderGoodsList);
        }

        return order;
    }

    // 映射贷款信息申请人信息至apporder对象
    private AppOrder acquirerApptMap2OrderObject(Map<String, Object> acquirer, AppOrder order) {
        if (StringUtils.isEmpty(acquirer.get("apptList"))) {
            logger.info("收单系统贷款详情无申请人信息:" + acquirer.get("applSeq"));
            return order;
        } else {
            Map<String, Object> apptList = (Map<String, Object>) acquirer.get("apptList");
            if (apptList == null || apptList.get("appt") == null) {
                return order;
            }
            List<Map<String, Object>> appt = (List<Map<String, Object>>) apptList.get("appt");
            if (apptList.size() > 0) {
                for (Map<String, Object> map : appt) {
                    if ("01".equals(map.get("appt_typ"))) {
                        map.keySet().removeIf((key) -> AcquirerApptEnum.getOrderAttr(key) == null);
                        map.forEach(
                                (key, value) -> ReflactUtils.setProperty(order, AcquirerApptEnum.getOrderAttr(key),
                                        FormatUtil.checkValueType(AcquirerApptEnum.getOrderAttr(key), value,
                                                AppOrder.class)));
                        break;
                    }
                }
            }

            return order;
        }
    }

    public AppOrderGoods acquirerGoodsMap2OrderGood(Map<String, Object> goodMap, AppOrderGoods appOrderGoods) {
        goodMap.keySet().removeIf((key) -> AcquirerGoodsEnum.getOrderAttr(key) == null);
        goodMap.forEach((key, value) -> ReflactUtils.setProperty(appOrderGoods, AcquirerGoodsEnum.getOrderAttr(key),
                FormatUtil.checkValueType(AcquirerGoodsEnum.getOrderAttr(key), value, AppOrderGoods.class)));
        return appOrderGoods;
    }

    @Override
    public AppOrder acquirerMap2OrderObject(Map<String, Object> acquirer, AppOrder order) {
        // 映射主申请人收获地址等信息
        this.acquirerApptMap2OrderObject(acquirer, order);
        // 映射商品信息
        this.acquirerGoodMap2OrderObject(acquirer, order);
        acquirer.keySet().removeIf((key) -> AcquirerEnum.getOrderAttr(key) == null);
        acquirer.forEach((key, value) -> ReflactUtils.setProperty(order, AcquirerEnum.getOrderAttr(key),
                FormatUtil.checkValueType(AcquirerEnum.getOrderAttr(key), value, AppOrder.class)));
        logger.info("收单映射appOrder结果：" + order);
        return order;
    }


    @Override
    public Map<String, Object> order2AcquirerMap(AppOrder order, Map<String, Object> map) {
        if (order == null) {
            return null;
        }

        if (map == null) {
            map = new HashMap();
        }

        try {
            Class clazz = order.getClass();
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                PropertyDescriptor pd = new PropertyDescriptor(field.getName(), clazz);
                Method method = pd.getReadMethod();
                Object value = method.invoke(order);
                String key = AcquirerEnum.getAcquirerAttr(field.getName());
                if (!StringUtils.isEmpty(key)) {
                    // 当order的值不为null获取map中不存在该字段时，替换map中的值.
                    if (value != null || map.get(key) == null) {
                        map.put(key, value);
                    }
                }
            }
        } catch (IntrospectionException | InvocationTargetException | IllegalAccessException e) {
            logger.error(e);
            throw new RuntimeException(e);
        }

        return map;
    }

    public Map<String, Object> putApplyListIntoMap(AppOrder apporder, Map<String, Object> acquirer) {
        // apptList开始
        HashMap<String, Object> apptList = new HashMap<>();
        HashMap<String, Object> apptmap = new HashMap<>();
        List<Map<String, Object>> apptMapList = new ArrayList<>();

        ChannelType channelType = ChannelType.forName(super.getChannel(), apporder.getChannelNo(), apporder.getSource());
        String url = EurekaServer.CRM + "/app/crm/cust/getCustExtInfo?pageName=1&custNo=" + apporder.getCustNo();
        logger.debug("CRM getCustExtInfo...");
        String json = HttpUtil.restGet(url, getToken());
        if (StringUtils.isEmpty(json)) {
            logger.error("客户扩展信息接口返回异常！-->CRM 1.4");
            return fail("51", ConstUtil.ERROR_INFO);
        }
        logger.debug("CRM 获取客户拓展信息返回json:" + json);
        Map<String, Object> custExtInfoMap = HttpUtil.json2Map(json);
        logger.debug("客户拓展信息转换值:" + custExtInfoMap);
        Map<String, Object> custExtInfoBodyMap = new HashMap<>();
        if (!StringUtils.isEmpty(custExtInfoMap.get("body"))) {
            // 客户扩展信息查询
            custExtInfoBodyMap = HttpUtil.json2Map(custExtInfoMap.get("body").toString());
        }
        cmisService.dealAddress(custExtInfoBodyMap);//对地址进行处理
        //对订单的收货地址进行处理
        cmisService.dealDeliverAddress(apporder, custExtInfoBodyMap);
        // 获取联系人
        // 客户基本信息查询
        String lxrUrl = EurekaServer.CRM + "/app/crm/cust/findCustFCiCustContactByCustNo?custNo=" + apporder.getCustNo();
        String lxrJson = HttpUtil.restGet(lxrUrl, getToken());
        logger.debug("CRM 获取客户基本信息:" + lxrJson);
        if (StringUtils.isEmpty(lxrJson)) {
            logger.error("联系人列表查询失败！——>CRM 1.8");
            return fail("52", ConstUtil.ERROR_INFO);
        }
        Map<String, Object> lxrMap = HttpUtil.json2Map(lxrJson);
        List<Map<String, Object>> lxrlist = new ArrayList<>();
        if (!StringUtils.isEmpty(lxrMap.get("body"))) {
            lxrlist = (List) lxrMap.get("body");
        }
        apptmap.put("appt_typ", "01");// 申请人类型 01 - 主申请人
        apptmap.put("appt_relation", "");
        apptmap.put("appt_id_typ", "20");
        apptmap.put("appt_id_typ_oth", "");
        apptmap.put("appt_id_no", apporder.getIdNo());
        apptmap.put("appt_cust_name", apporder.getCustName());
        IDCard idCard = new IDCard(apporder.getIdNo());
        apptmap.put("appt_indiv_sex", idCard.getGenderCode());
        apptmap.put("appt_start_date", DateUtils.toDateString(idCard.getBirthday()));
        apptmap.put("indiv_marital", custExtInfoBodyMap.get("maritalStatus"));
        apptmap.put("indiv_edu", custExtInfoBodyMap.get("education"));
        // 个人/够花/美分期/大数据/星巢贷 教育为空,大专
        if (channelType == ChannelType.Personal
                || channelType == ChannelType.EnoughSpend
                || channelType == ChannelType.LoveByStage
                || channelType == ChannelType.BigData
                || channelType == ChannelType.Micron) {
            apptmap.put("indiv_edu", ObjectUtils.toString(custExtInfoBodyMap.get("education"), "20"));
        }
        //户籍地址
        apptmap.put("appt_reg_province", custExtInfoBodyMap.get("regProvince"));
        apptmap.put("appt_reg_city", custExtInfoBodyMap.get("regCity"));
        //星巢贷户籍地址处理==》若户籍地址为空，则使用现住房地址的省市
        if (channelType == ChannelType.Micron && StringUtils.isEmpty(custExtInfoBodyMap.get("regProvince"))) {
            apptmap.put("appt_reg_province", custExtInfoBodyMap.get("liveProvince"));
            apptmap.put("appt_reg_city", custExtInfoBodyMap.get("liveCity"));
            // 增加默认值：山东青岛
            if (StringUtils.isEmpty(apptmap.get("appt_reg_province"))) {
                apptmap.put("appt_reg_province", "370000");
                apptmap.put("appt_reg_city", "370200");
            }
        }

        apptmap.put("live_info", custExtInfoBodyMap.get("liveInfo"));
        /**现住房信息 星巢贷、大数据、美分期统一传99（其他）**/
        if (channelType == ChannelType.Micron
                || channelType == ChannelType.BigData
                || channelType == ChannelType.LoveByStage
                || channelType == ChannelType.Shunguang) {
            apptmap.put("live_info", "99");
        }
        // 现住房性质缺省值10 (无)
        if (StringUtils.isEmpty(apptmap.get("live_info"))) {
            apptmap.put("live_info", "10");
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
        apptmap.put("indiv_mobile", StringUtils.isEmpty(apporder.getRepayAccMobile()) ?
                apporder.getIndivMobile() :
                apporder.getRepayAccMobile());// 联系电话 1030版本先传还款卡绑定手机号，若为空则传绑定手机号
        apptmap.put("indiv_fmly_zone", custExtInfoBodyMap.get("fmlyZone"));
        apptmap.put("indiv_fmly_tel", custExtInfoBodyMap.get("fmlyTel"));
        apptmap.put("local_resid", custExtInfoBodyMap.get("localResid"));
        if ((channelType == ChannelType.Personal || channelType == ChannelType.EnoughSpend) && StringUtils
                .isEmpty(custExtInfoBodyMap.get("localResid"))) {
            apptmap.put("local_resid", "10");
        }
        // 美分期 集团大数据 默认本地
        if (channelType == ChannelType.BigData || channelType == ChannelType.LoveByStage) {
            apptmap.put("local_resid", "10");
        }
        apptmap.put("indiv_email", custExtInfoBodyMap.get("email"));
        if (StringUtils.isEmpty(custExtInfoBodyMap.get("mthInc"))) {
            apptmap.put("annual_earn", "0");
        } else {
            apptmap.put("annual_earn",
                    new BigDecimal(custExtInfoBodyMap.get("mthInc").toString()).multiply(new BigDecimal(12)));
        }
        apptmap.put("household_own_rel", "");
        apptmap.put("max_crdt_card", custExtInfoBodyMap.get("maxAmount"));
        apptmap.put("iss_bank", custExtInfoBodyMap.get("creditCount"));
        apptmap.put("indiv_mth_inc", custExtInfoBodyMap.get("mthInc"));
        // 个人版现金贷，以下赋默认值
        if ((channelType == ChannelType.Personal
                || channelType == ChannelType.EnoughSpend
                || channelType == ChannelType.BigData
                || channelType == ChannelType.CashLoan)
                && "02".equals(apporder.getTypGrp())) {
            apptmap.put("position_opt", ObjectUtils.toString(custExtInfoBodyMap.get("positionType"), "10"));
            apptmap.put("live_year", ObjectUtils.toString(custExtInfoBodyMap.get("liveYear"), "0"));
            apptmap.put("indiv_dep_no", ObjectUtils.toString(custExtInfoBodyMap.get("providerNum"), "0"));
            apptmap.put("mail_opt", "");
            apptmap.put("mail_province", "");
            apptmap.put("mail_city", "");
            apptmap.put("mail_area", "");
            apptmap.put("mail_addr", "");
        } else {
            apptmap.put("position_opt", ObjectUtils.toString(custExtInfoBodyMap.get("positionType"), "10"));
            apptmap.put("live_year", ObjectUtils.toString(custExtInfoBodyMap.get("liveYear"), "0"));
            apptmap.put("indiv_dep_no", ObjectUtils.toString(custExtInfoBodyMap.get("providerNum"), "0"));
            apptmap.put("mail_opt", apporder.getDeliverAddrTyp());
            apptmap.put("mail_province", ObjectUtils.toString(apporder.getDeliverProvince()));
            apptmap.put("mail_city", ObjectUtils.toString(apporder.getDeliverCity()));
            apptmap.put("mail_area", ObjectUtils.toString(apporder.getDeliverArea()));
            apptmap.put("mail_addr", ObjectUtils.toString(apporder.getDeliverAddr()));
        }
        // 学习方式
        apptmap.put("study_mth", custExtInfoBodyMap.get("studyType"));
        apptmap.put("studying_deg", custExtInfoBodyMap.get("studyDegree"));
        // 集团大数据 在读学历 默认值处理
        if (StringUtils.isEmpty(custExtInfoBodyMap.get("studyDegree")) && channelType == ChannelType.BigData) {
            apptmap.put("studying_deg", "01"); //大专
        }
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
        apptmap.put("emp_reg_rel_tel", custExtInfoBodyMap.get("officeTel"));//
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
        apptmap.put("indiv_branch", StringUtils.isEmpty(custExtInfoBodyMap.get("officeDept")) ? "默认" : custExtInfoBodyMap.get("officeDept"));
        String indiv_branch = (String) apptmap.get("indiv_branch");
        logger.info("所在部门：" + indiv_branch);
        apptmap.put("indiv_emp_typ", StringUtils.isEmpty(custExtInfoBodyMap.get("officeTyp")) ? "Z" : custExtInfoBodyMap.get("officeTyp"));
        apptmap.put("indiv_emp_yrs", StringUtils.isEmpty(custExtInfoBodyMap.get("serviceYears")) ? 0 : custExtInfoBodyMap.get("serviceYears"));// custExtInfoBodyMap.get("serviceYears")
        apptmap.put("indiv_emp_province", custExtInfoBodyMap.get("officeProvince"));
        apptmap.put("indiv_emp_city", custExtInfoBodyMap.get("officeCity"));
        apptmap.put("indiv_emp_area", custExtInfoBodyMap.get("officeArea"));
        apptmap.put("empaddr", custExtInfoBodyMap.get("officeAddr"));
        apptmap.put("indiv_emp_zip", "266000");
        if (StringUtils.isEmpty(custExtInfoBodyMap.get("officeTel"))) {
            apptmap.put("indiv_emp_zone", "0532");
            apptmap.put("indiv_emp_tel", "58869762");
        } else {
            String tel = String.valueOf(custExtInfoBodyMap.get("officeTel"));
            Map<String, String> phoneMap = cmisService.getPhoneNoAndZone(tel);
            apptmap.put("indiv_emp_zone", phoneMap.get("zone"));
            apptmap.put("indiv_emp_tel", phoneMap.get("tel"));// custExtInfoBodyMap.get("officeTel")
        }
        apptmap.put("indiv_emp_tel_sub", "");
        apptmap.put("indiv_emp_hr_zone", "");
        apptmap.put("indiv_emp_hr_phone", "");
        apptmap.put("indiv_emp_hr_sub", "");
        // 美凯龙职务默认传中层
        if (channelType == ChannelType.Micron) {
            apptmap.put("indiv_position", "02");
        } else {
            apptmap.put("indiv_position", custExtInfoBodyMap.get("position"));
        }
        // 美分期 职务 默认基层
        if (channelType == ChannelType.LoveByStage && StringUtils.isEmpty(custExtInfoBodyMap.get("position"))) {
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
        if (channelType == ChannelType.LoveByStage && StringUtils.isEmpty(apptmap.get("sp_mth_inc"))) {
            apptmap.put("sp_mth_inc", "3000");
        }
        apptmap.put("spouse_pay_ind", "");
        apptmap.put("ppty_live_opt", "10");//自有房产地址
        apptmap.put("ppty_loan_ind", StringUtils.isEmpty(custExtInfoBodyMap.get("pptyLoanInd")) ? "N" : custExtInfoBodyMap.get("pptyLoanInd"));//是否按揭
        apptmap.put("ppty_righ_name", StringUtils.isEmpty(custExtInfoBodyMap.get("pptyRighName")) ? "无" : custExtInfoBodyMap.get("pptyRighName")); //房屋产权人
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
        HashMap<String, Object> relMap = new HashMap<>();

        List rellist = new ArrayList();
        for (Map<String, Object> obj : lxrlist) {
            HashMap<String, Object> rel = new HashMap<>();
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
        relMap.put("rel", rellist);
        apptmap.put("relList", relMap);
        apptMapList.add(apptmap);

        Map<String, Object> commonPersonMap = commonRepaymentPersonService
                .getCommonRepaymentPerson(apporder.getApplSeq());
        logger.debug("共同还款人map列表  commonPersonMap==" + commonPersonMap);
        if (HttpUtil.isSuccess(commonPersonMap)) {
            List<CommonRepaymentPerson> personlist = (List<CommonRepaymentPerson>) commonPersonMap.get("body");
            logger.info("共同还款人列表：" + personlist);
            for (CommonRepaymentPerson person : personlist) {
                // 获取共同还款人的客户编号
                Map<String, Object> commMap = cmisService
                        .getCommonPayPersonMap(person.getCommonCustNo(), apporder.getSource(),
                                apporder.getTypGrp(), person, apporder.getVersion());
                apptMapList.add(commMap);
            }
        }
        apptList.put("appt", apptMapList);
        acquirer.put("apptList", apptList);
        return acquirer;
    }


    /**
     * 如果为现金贷，则根据appOrder中的applSeq获取贷款详情，并向收单系统发起渠道进件.
     *
     * @param order 订单信息
     * @return Map
     */
    public Map<String, Object> cashLoan(AppOrder order, AppOrdernoTypgrpRelation relation) {
        Map<String, Object> acquirer;
        if (relation == null || StringUtils.isEmpty(relation.getApplSeq())) {
            acquirer = new HashMap<>();
            acquirer.put("tradeType", "1");// 新增
        } else {
            order.setApplSeq(relation.getApplSeq());
            Map<String, Object> responseMap = this.getOrderFromAcquirer(order.getApplSeq(), super.getChannel(), super.getChannelNo(), order.getCooprCde(), null, "2");
            if (!HttpUtil.isSuccess(responseMap)) {
                logger.info("从收到系统获取贷款详情失败, map=" + responseMap);
                return fail("53", "从收单系统获取贷款详情失败");
            }
            acquirer = (Map<String, Object>) responseMap.get("body");
            acquirer.put("tradeType", "2");// 修改
            acquirer.put("applSeq", relation.getApplSeq());
        }
        acquirer.put("applCde", "");

        // 获取贷款详情成功,根据订单信息渠道进件.
        if (relation != null && StringUtils.isEmpty(relation.getApplSeq())) {
            logger.info("获取订单流水关联关系失败,orderNo :" + order.getOrderNo());
            return fail("52", "获取订单流水关联关系失败");
        }

        // 反射order中不为空的值, 放入acquirer中
        this.order2AcquirerMap(order, acquirer);

        // 申请人信息
        this.putApplyListIntoMap(order, acquirer);

        // 默认值处理
        this.cashLoanCheckDefaultValue(order, acquirer);

        // 获取系统标识和渠道号
        Map<String, Object> sysFlagAndChannelNo = orderService.getSysFlagAndChannelNo(order);
        // 收单系统保存贷款详情
        String tradeType = "1";
        if (relation != null) {
            tradeType = "2";
        }
        Map<String, Object> headMap = AcqUtil
                .getAcqHead(AcqTradeCode.SAVE_APPL, sysFlagAndChannelNo.get("sysFlag").toString(),
                        sysFlagAndChannelNo.get("channelNo").toString(), order.getCooprCde(), tradeType);
        headMap.put("autoFlag", "N");
        if (relation != null && !StringUtils.isEmpty(relation.getApplSeq())) {
            headMap.put("applSeq", relation.getApplSeq());
        }
        acquirer.put("coopr_cde",headMap.get("cooprCode"));
        logger.info("向收单系统发起贷款申请, 参数:" + acquirer);
        Map<String, Object> result;
        result = AcqUtil
                .getAcqResponse(EurekaServer.ACQUIRER + "/api/appl/saveLcApplEK", headMap, acquirer);
//        result = AcqUtil
//                .getAcqResponse(EurekaServer.ACQUIRER + "/api/appl/saveLcAppl", headMap, acquirer);
        if (AcqUtil.isSuccess(result)) {
            logger.info("更新订单，收单系统保存贷款详情成功, applSeq:" + order.getApplSeq()
                    + ",返回结果:" + result);
        } else {
            logger.info("更新订单，收单系统保存贷款详情失败, applSeq:" + order.getApplSeq()
                    + ",返回结果:" + result);
        }
        return result;
    }

    public Map<String, Object> cashLoanCheckDefaultValue(AppOrder appOrder, Map<String, Object> acquirer) {
        acquirer.put("crt_dt", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));// 登记日期
        // 如果为随借随换，借款期限上传为最大值.
        if ("D".equalsIgnoreCase((String) acquirer.get("apply_tnr_typ"))) {
            Map<String, Object> typCdeMap = cmisService.findPLoanTyp(FormatUtil.getStrDealNull(acquirer.get("typ_cde")));
            if (HttpUtil.isSuccess(typCdeMap)) {
                Map<String, Object> typResultMap = (Map<String, Object>) typCdeMap.get("body");
                acquirer.put("apply_tnr", typResultMap.get("tnrMaxDays").toString());
            }
        }
        //渠道类型
        ChannelType channelType = ChannelType.forName(super.getChannel(), appOrder.getChannelNo(), appOrder.getSource());
        // 个人版/美凯龙/大数据/美分期/够花  固定为：SALE、消费
        if (channelType == ChannelType.Personal
                || channelType == ChannelType.Merchant
                || channelType == ChannelType.BigData
                || channelType == ChannelType.LoveByStage
                || channelType == ChannelType.EnoughSpend
                || channelType == ChannelType.Shunguang) {
            acquirer.put("purpose", "SALE");
        }
        // 用户选择其他用途时，自动修改为：SALE、消费
        if ("OTH".equals(acquirer.get("purpose"))) {
            acquirer.put("purpose", "SALE");
        }

        if ("01".equals(acquirer.get("typ_grp"))) { // 受托支付不传放款账号信息
            acquirer.put("appl_ac_typ", "");
            acquirer.put("appl_ac_nam", "");
            acquirer.put("appl_card_no", "");
            acquirer.put("acc_bank_cde", "");
            acquirer.put("acc_bank_name", "");
            acquirer.put("appl_ac_bch", "");
            acquirer.put("ac_province", "");
            acquirer.put("ac_city", "");
        } else {
            if (StringUtils.isEmpty(acquirer.get("appl_ac_nam")))
                acquirer.put("appl_ac_nam", "海尔集团财务有限责任公司");
        }
        // 嗨付个人版简版/够花 设置放款账户开户机构名称   大数据走0000  海尔集团财务有限责任公司
        if (channelType == ChannelType.Personal
                || channelType == ChannelType.BigData
                || channelType == ChannelType.EnoughSpend) {
            if (StringUtils.isEmpty(acquirer.get("acc_ac_bch_name"))) {
                acquirer.put("acc_ac_bch_name", "海尔集团财务有限责任公司");
            }
            if (StringUtils.isEmpty(acquirer.get("acc_ac_bch_cde"))) {
                acquirer.put("acc_ac_bch_cde", "0000");
            }
            if (StringUtils.isEmpty(acquirer.get("acc_ac_bch_cde"))) {
                acquirer.put("appl_ac_bch", "0000");
            }
        }

        /** 如果贷款类型（typGre）为01（耐用消费品），传从数据库取的值， 如果为02（现金贷）传固定值 **/
        /** 部分特殊类型的现金贷，APP会上传门店等信息，不传固定值 **/
        /** 2017.05.10 与app端确认，无“特殊类型现金贷”**/
        //  (!Objects.equals("3", appOrder.getSource()))) {
        if ("02".equals(acquirer.get("typ_grp")) && channelType != ChannelType.OtherPayPlat) {
            if (!appManageService.putSaleMsgIntoMap(String.valueOf(acquirer.get("typ_cde")), acquirer)) {
                if (!appManageService.putSaleMsgIntoMap("default", acquirer))
                    return fail("69", "获取销售信息失败");
            }
            logger.debug("添加销售信息完毕：" + acquirer);
        } else {
            String userId = String.valueOf(acquirer.get("crt_usr"));
            Map<String, Object> saleRes = crmService.getStoreSaleByUserId(userId);
            if (!HttpUtil.isSuccess(saleRes)) {
                logger.error("销售代表信息查询失败---》CRM 1.29 userId:" + userId);
                return fail(HttpUtil.getRetFlag(saleRes), HttpUtil.getRetMsg(saleRes));
            }
            Map salerBodyMap = (Map) saleRes.get("body");
            acquirer.put("saler_name", salerBodyMap.get("userName"));
            acquirer.put("saler_mobile", salerBodyMap.get("mobileNum"));
            // 客户经理
            acquirer.put("operator_name", acquirer.get("saler_name"));
            acquirer.put("operator_cde", acquirer.get("saler_cde"));
            acquirer.put("operator_tel", acquirer.get("saler_mobile"));
            // 星巢贷订单，saler_name为crm查询得到的门店名称
            if (channelType == ChannelType.Micron) {
                boolean isPutXcdSalerMsg = appManageService.putSaleMsgIntoMap("xcd", acquirer);
                if (!isPutXcdSalerMsg) {
                    logger.error("查询星巢贷销售人员信息失败，渠道进件停止");
                    return fail("70", "查询星巢贷销售人员信息失败");
                }
                String crmUrl = String.format("%s/app/crm/findStoreByMerchNo?merchNo=%s&isDefaultStore=Y", EurekaServer.CRM, appOrder.getMerchNo());
                logger.info("星巢贷订单向crm发起请求查询门店名称：" + crmUrl);
                String crmJson = HttpUtil.restGet(crmUrl);
                logger.info("星巢贷订单crm返回门店信息：" + crmJson);
                if (StringUtils.isEmpty(crmJson) || !HttpUtil.isSuccess(crmJson)) {
                    return fail("56", "crm查询门店信息失败");
                }
                Map<String, Object> crmMap = HttpUtil.json2Map(crmJson);
                List<Map<String, Object>> crmList = (List<Map<String, Object>>) crmMap.get("body");
                if (crmList != null && crmList.size() > 0) {
                    Map<String, Object> o = crmList.get(0);
                    String storeName = o.get("storeName").toString();
                    acquirer.put("saler_name", StringUtils.isEmpty(storeName) ? acquirer.get("saler_name") : storeName);
                }
            }
        }

        // 登记人员编码
        if (channelType == ChannelType.Personal
                || channelType == ChannelType.Micron
                || channelType == ChannelType.EnoughSpend) {
            // 个人版同销售代表
            acquirer.put("crt_usr", acquirer.get("saler_cde"));
        }

        // 顺逛默认值处理
        if ("49".equals(this.getChannelNo())) {
            acquirer.put("crd_flag", "N");//是否额度加支用，默认是
//            acquirer.put("creditType", "02");//支用类型     00：单纯贷款    01：额度申请加支用  02：支用
        } else {
            acquirer.put("crd_flag", "Y");
        }
        return acquirer;
    }


    @Override
    public AppOrder getAppOrderFromAcquirer(String applSeq, String channelNo) {
        if (StringUtils.isEmpty(channelNo)) {
            channelNo = super.getChannelNo();
        }
        String url = EurekaServer.ACQUIRER + "/api/appl/selectApplInfoApp";
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("channelNo", channelNo);
        paramMap.put("applSeq", applSeq);
        logger.info("获取贷款详情请求参数:applSeq:" + applSeq + ",channelNo:" + channelNo);
        Map<String, Object> acqResponse = AcqUtil
                .getAcqResponse(url, AcqTradeCode.SELECT_APP_APPL_INFO, super.getChannel(), channelNo, "", "",
                        paramMap);
        if (acqResponse == null || acqResponse.isEmpty()) {
            return null;
        }
        if (!CmisUtil.isSuccess(acqResponse)) {
            return null;
        }
        Map<String, Object> acqBody = (Map<String, Object>) ((Map<String, Object>) acqResponse.get("response"))
                .get("body");
        AppOrder appOrder = this.acquirerMap2OrderObject(acqBody, new AppOrder());
        AppOrdernoTypgrpRelation relation = appOrdernoTypgrpRelationDao.selectByApplSeq(applSeq);
        if (relation == null) {
            throw new BusinessException("22", "订单不存在");
        }
        appOrder.setCustNo(relation.getCustNo());

        return appOrder;
    }

    @Override
    public Map<String, Object> cancelAppl(String applSeq) {
        Map<String, Object> params = new HashMap<>();
        params.put("applSeq", applSeq);
        Map<String, Object> result = AcqUtil
                .getAcqResponse(EurekaServer.ACQUIRER + "/api/appl/cancelAppl", AcqTradeCode.COMMIT_APPL,
                        super.getChannel(), super.getChannelNo(), null, null, params);
        if (result == null || result.isEmpty()) {
            return fail(ConstUtil.ERROR_CODE, "收单系统通信失败！");
        }
        if (!CmisUtil.isSuccess(result)) {
            logger.info("收单系统取消贷款申请失败, applSeq:" + applSeq);
        } else {
            logger.info("收单系统取消贷款申请成功, applSeq:" + applSeq);
        }

        return (Map<String, Object>) result.get("response");
    }

    @Override
    public Map<String, Object> saveRiskInfo(Map<String, Object> map) {
        String url = EurekaServer.ACQUIRER + "/api/appl/saveRiskInfo";
        logger.info("风险信息采集接口定义请求地址：" + url);
        logger.info("风险信息采集接口定义请求数据：" + map);
        Map<String, Object> result = HttpUtil.restPostMap(url, "", map);
        logger.info("订单合同确认接口返回数据：" + result);
        return result;
    }

    @Override
    public Map<String, Object> commitAppl(AppOrder order, String flag, String riskJson) {
        // 默认申请放款
        if (StringUtils.isEmpty(flag)) {
            flag = "2";
        }
        Map<String, Object> param = new HashMap<>();
        param.put("applSeq", order.getApplSeq());
        param.put("flag", flag);
        if (!StringUtils.isEmpty(riskJson)) {
            param.put("riskJson", riskJson);
        }
        Map<String, Object> result = AcqUtil
                .getAcqResponse(EurekaServer.ACQUIRER + "/api/appl/commitAppl", AcqTradeCode.COMMIT_APPL,
                        super.getChannel(), super.getChannelNo(), order.getCooprCde(), null, param);
        if (!CmisUtil.isSuccess(result)) {
            logger.info("收单系统提交贷款申请失败, applSeq:" + order.getApplSeq());
        } else {
            logger.info("收单系统提交贷款申请成功, applSeq:" + order.getApplSeq());
        }

        return (Map<String, Object>) result.get("response");
    }

    @Override
    public Map<String, Object> returnGoods(String tradeCode, String sysFlag, String channelNo, String cooprCode, String tradeType, Map<String, Object> map) {
        String url = EurekaServer.ACQUIRER + "/api/appl/returnGoods";
        Map headMap = AcqUtil.getAcqHead(tradeCode, sysFlag, channelNo, cooprCode, tradeType);
        return (Map<String, Object>) AcqUtil.getAcqResponse(url, headMap, map);
    }

    @Override
    public Map<String, Object> getReturnGoodsInfo(String tradeCode, String sysFlag, String channelNo, String cooprCode, String tradeType, Map<String, Object> map) {
        String url = EurekaServer.ACQUIRER + "/api/appl/getReturnGoodsInfo";
        Map headMap = AcqUtil.getAcqHead(tradeCode, sysFlag, channelNo, cooprCode, tradeType);
        return (Map<String, Object>) AcqUtil.getAcqResponse(url, headMap, map);
    }
}
