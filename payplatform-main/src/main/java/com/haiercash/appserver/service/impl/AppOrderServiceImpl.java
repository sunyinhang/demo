package com.haiercash.appserver.service.impl;

import com.alibaba.fastjson.JSON;
import com.haiercash.appserver.apporder.DataVerificationUtil;
import com.haiercash.appserver.gm.service.GmService;
import com.haiercash.appserver.service.AcquirerService;
import com.haiercash.appserver.service.AppManageService;
import com.haiercash.appserver.service.AppOrderService;
import com.haiercash.appserver.service.AttachService;
import com.haiercash.appserver.service.CASignService;
import com.haiercash.appserver.service.CmisApplService;
import com.haiercash.appserver.service.CommonRepaymentPersonService;
import com.haiercash.appserver.service.CrmService;
import com.haiercash.appserver.service.MerchFaceService;
import com.haiercash.appserver.service.OrderService;
import com.haiercash.appserver.service.PersonFaceService;
import com.haiercash.appserver.util.ConstUtil;
import com.haiercash.appserver.util.enums.AcquirerEnum;
import com.haiercash.appserver.util.sign.FileSignUtil;
import com.haiercash.common.apporder.utils.AcqTradeCode;
import com.haiercash.common.apporder.utils.FormatUtil;
import com.haiercash.common.config.EurekaServer;
import com.haiercash.common.data.AppOrder;
import com.haiercash.common.data.AppOrderRepositoryImpl;
import com.haiercash.common.data.AppOrdernoTypgrpRelation;
import com.haiercash.common.data.AppOrdernoTypgrpRelationRepository;
import com.haiercash.common.data.AttachFile;
import com.haiercash.common.data.AttachFileRepository;
import com.haiercash.common.data.BusinessType;
import com.haiercash.common.data.CommonRepaymentPerson;
import com.haiercash.common.data.MsgType;
import com.haiercash.common.data.UAuthCASignRequest;
import com.haiercash.common.data.UAuthCASignRequestRepository;
import com.haiercash.common.data.UserTag;
import com.haiercash.common.service.BaseService;
import com.haiercash.commons.cmis.CmisUtil;
import com.haiercash.commons.util.AcqUtil;
import com.haiercash.commons.util.EncryptUtil;
import com.haiercash.commons.util.HttpUtil;
import com.haiercash.commons.util.RestUtil;
import com.haiercash.commons.util.ResultHead;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * @author liuhongbin
 * @date 2016/5/20
 * @description:
 **/
@Service
public class AppOrderServiceImpl extends BaseService implements AppOrderService {
    private Log logger = LogFactory.getLog(this.getClass());
    private static String MODULE_NO = "11";

    public AppOrderServiceImpl() {
        super(MODULE_NO);
    }

    @Autowired
    UAuthCASignRequestRepository uAuthCASignRequestRepository;
    @Autowired
    AttachFileRepository attachFileRepository;
    @Autowired
    AttachService attachService;
    @Autowired
    CmisApplService cmisApplService;
    @Autowired
    private CASignService caSignService;
    @Autowired
    private MerchFaceService merchFaceService;

    @Autowired
    private CrmService crmService;

    @Autowired
    AppOrderRepositoryImpl appOrderREpositoryImpl;
    @Autowired
    private PersonFaceService personFaceService;

    @Autowired
    private OrderService orderService;
    @Autowired
    private AppManageService appManageService;

    @Autowired
    private AppOrdernoTypgrpRelationRepository appOrdernoTypgrpRelationRepository;
    @Autowired
    private AcquirerService acquirerService;
    @Autowired
    private CommonRepaymentPersonService commonRepaymentPersonService;
    @Autowired
    private GmService gmService;
    //人脸识别最大次数
    @Value("${common.other.faceCountLimit}")
    protected Integer faceCountLimit;

    //人脸识别阈值
    @Value("${common.other.faceThreshold}")
    protected Integer faceThresHold;

    @Value("${common.xcd.typCde}")
    protected String xcdTypCde;

    /**
     * 把门店信息写入订单
     *
     * @param order 订单对象
     */
    public void updateStoreInfo(AppOrder order, String token) {
        String storeNo = order.getCooprCde();
        if (StringUtils.isEmpty(storeNo)) {
            logger.info("本订单的门店编号（storeNo）为空，请求处理被迫停止！");
            return;
        }

		/*
         * 接口返回值参考： {"head":{"retFlag":"00000","retMsg":"处理成功"},
		 * "body":{"storeNo":"DZHW01","merchNo":"8800125101","storeName":
		 * "银川市大展宏伟工贸有限公司",
		 * "storePhoneZone":"","storePhone":"","storePhoneSub":"","cumNo":
		 * "01376256"}}
		 */
        String url = EurekaServer.CRM + "/app/crm/cust/getStoreInfo" + "?storeNo=" + storeNo;
        String json = HttpUtil.restGet(url, token);
        if (StringUtils.isEmpty(json)) {
            logger.info("CRM==>门店信息（getStoreInfo）接口查询失败！");
            return;
        }
        json = json.replaceAll("null", "\"\"");
        Map<String, Object> map = HttpUtil.json2Map(json);
        if (HttpUtil.isSuccess(map)) {
            Map<String, Object> mapBody = HttpUtil.json2Map(map.get("body").toString());
            order.setCooprName(mapBody.get("storeName").toString());
            order.setContZone(mapBody.get("storePhoneZone").toString());
            order.setContTel(mapBody.get("storePhone").toString());
            order.setContSub(mapBody.get("storePhoneSub").toString());
            order.setOperatorCde(mapBody.get("cumNo").toString());
        }
    }

    /**
     * 把销售代表信息写入订单
     *
     * @param order 订单对象
     */
    public void updateSalesInfo(AppOrder order, String token) {
        String salesNo = order.getCrtUsr();
        if (StringUtils.isEmpty(salesNo)) {
            logger.info("销售代表编号（salesNo）为空，销售代表信息写入失败！");
            return;
        }

		/*
         * 接口返回值参考： {"head":{"retFlag":"00000","retMsg":"处理成功"},
		 * "body":{"mobileNum":"","userName":"陈琳琳","userId":"chenlinlin"}}
		 */
        String url = EurekaServer.CRM + "/app/crm/cust/getSalesInfo" + "?userid=" + salesNo;
        String json = HttpUtil.restGet(url, token);
        if (StringUtils.isEmpty(json)) {
            logger.info("CRM==>销售代表信息查询（getSalesInfo）接口返回异常！请求被迫停止处理！");
            return;
        }
        json = json.replaceAll("null", "\"\"");
        Map<String, Object> map = HttpUtil.json2Map(json);
        if (HttpUtil.isSuccess(map)) {
            Map<String, Object> mapBody = HttpUtil.json2Map(map.get("body").toString());
            order.setSalerName(mapBody.get("userName").toString());
            order.setSalerMobile(mapBody.get("mobileNum").toString());
        }
    }

    /**
     * 把实名认证信息写入订单
     *
     * @param order 订单对象
     */
    public Map<String, Object> updateCustRealInfo(AppOrder order, String token) {
        String custNo = order.getCustNo();
        if (StringUtils.isEmpty(custNo)) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "custNo为空，上传实名认证信息失败");
        }

        String url = EurekaServer.CRM + "/app/crm/cust/getCustRealInfo" + "?custNo=" + custNo;
        logger.info("CRM 实名信息接口请求url==" + url);
        String json = HttpUtil.restGet(url, token);
        logger.info("CRM 实名信息接口返回：" + json);
        if (StringUtils.isEmpty(json)) {
            logger.info("CRM实名认证信息（getCustRealInfo）接口返回异常！请求处理被迫停止！");
            return fail(RestUtil.ERROR_INTERNAL_CODE, "CRM系统通信失败");
        }
        if (!HttpUtil.isSuccess(json)) {
            return HttpUtil.json2DeepMap(json);
        }
        json = json.replaceAll("null", "\"\"");
        Map<String, Object> map = HttpUtil.json2DeepMap(json);
        if (HttpUtil.isSuccess(map)) {
            Map<String, Object> mapBody = (Map<String, Object>) map.get("body");
            order.setIdTyp(StringUtils.isEmpty(mapBody.get("certType")) ? "" : (String) mapBody.get("certType"));
            order.setIdNo(StringUtils.isEmpty(mapBody.get("certNo")) ?
                    "" :
                    mapBody.get("certNo").toString().toUpperCase());//身份证号
            order.setCustName(
                    StringUtils.isEmpty(mapBody.get("custName")) ? "" : mapBody.get("custName").toString());//客户姓名
            //order.setIndivMobile(mapBody.get("mobile").toString());
            //客户手机号处理逻辑
            //（1）如果版本号未传，则默认为0，订单手机号为实名认证手机号，忽略手机号参数；
            //（2）如果版本号大于等于1，订单手机号为传入的手机号参数；==》(程序代码中此处不需要做处理)
            //（3）如果版本号大于等于1且传入的手机号参数为空，则根据客户姓名和身份证号查询绑定手机号
            if (StringUtils.isEmpty(order.getVersion()) || "0".equals(order.getVersion())) {
                logger.info("旧版本==》查询实名认证手机号为：" + String.valueOf(mapBody.get("mobile")));
                order.setVersion("0");
                order.setIndivMobile(String.valueOf(mapBody.get("mobile")));
            } else if (Integer.parseInt(order.getVersion()) >= 1 && StringUtils.isEmpty(order.getIndivMobile())) {
                String mobile = this.getBindMobileByCustNameAndIdNo(order.getCustName(), order.getIdNo(), token);
                //如果查询的绑定手机号不为空，则用绑定手机号，若为空，则用实名认证手机号
                if (!StringUtils.isEmpty(mobile)) {
                    order.setIndivMobile(mobile);
                } else {
                    order.setIndivMobile(String.valueOf(mapBody.get("mobile")));
                }
            }
            // 放款账号信息
            order.setApplAcTyp("01");// 01、个人账户
            this.setFkNo(order, token);
            order.setApplAcNam(mapBody.get("custName").toString());
            order.setAccAcProvince(mapBody.get("acctProvince").toString());
            order.setAccAcCity(mapBody.get("acctCity").toString());
            order.setRepayApplAcNam(mapBody.get("custName").toString());
            this.setHkNo(order, token);
            if (StringUtils.isEmpty(order.getApplCardNo())) {
                logger.info("放款卡号为空！可能会导致数据异常！！");
            }
            if (String.valueOf(order.getApplCardNo()).equals(order.getRepayApplCardNo())) {
                // 目前还款卡信息没有开户省市，如果还款卡号与放款卡号一致，开户省市也一致
                order.setRepayAcProvince(order.getAccAcProvince());
                order.setRepayAcCity(order.getAccAcCity());
            }
        }
        // 如果从实名认证中依然获取不到开户行的省市信息，则将其写死
        if (StringUtils.isEmpty(order.getRepayAcProvince()) || StringUtils.isEmpty(order.getRepayAcCity())) {
            order.setRepayAcProvince("370000");
            order.setRepayAcCity("370200");
        }
        if (StringUtils.isEmpty(order.getAccAcProvince()) || StringUtils.isEmpty(order.getAccAcCity())) {
            order.setAccAcProvince("370000");
            order.setAccAcCity("370200");
        }
        return success();
    }

    /**
     * 把贷款品种信息写入订单
     *
     * @param order 订单对象
     */
    public void updateTypInfo(AppOrder order, String token) {
        if (order.getTypCde() != null) {
            // 从贷款品种详情接口中查询贷款品种版本号（typVer） 贷款品种流水号（typSeq）
            String url = EurekaServer.CMISPROXY + "/api/pLoanTyp/find?typCde="
                    + order.getTypCde();
            String json = HttpUtil.restGet(url, token);
            if (StringUtils.isEmpty(json)) {
                logger.info("CMIS==》贷款品种详情接口查询结果异常！请求处理被停止！ ");
                return;
            }
            Map<String, Object> typResultMap = HttpUtil.json2Map(json);// 贷款品种resultMap,利用此map封装贷款相关的一些数据

            String typVer = (Integer) typResultMap.get("typVer") + "";
            String typSeq = (Integer) typResultMap.get("typSeq") + "";
            order.setTypVer(typVer);
            order.setTypSeq(typSeq);
            // 还款间隔（loanFreq） 每期还款日（dueDayOpt） 还款日(dueDay) 进件通路(docChannel)
            order.setLoanFreq((String) typResultMap.get("typFreq"));
            order.setDueDayOpt((String) typResultMap.get("dueDayOpt"));
            order.setDueDay(typResultMap.get("dueDay").toString());
            order.setDocChannel((String) typResultMap.get("docChannel"));
            // 贷款品种名称
            order.setTypDesc((String) typResultMap.get("typDesc"));
            // 贷款品种类别
            order.setTypLevelTwo((String) typResultMap.get("levelTwo"));
            // 还款方式种类
            order.setPayMtd((String) typResultMap.get("payMtd"));
            order.setPayMtdDesc((String) typResultMap.get("payMtdDesc"));
            /**
             pLoanTypFstPct;//贷款品种详情的最低首付比例(fstPct)
             pLoanTypMinAmt; //单笔最小贷款金额(minAmt)
             pLoanTypMaxAmt;//单笔最大贷款金额(maxAmt)
             pLoanTypGoodMaxNum;//同笔贷款同型号商品数量上限(goodMaxNum)
             pLoanTypTnrOpt;//借款期限(tnrOpt)
             */
            if (!StringUtils.isEmpty(order.getVersion()) && Integer.parseInt(order.getVersion()) >= 2) {
                order.setpLoanTypFstPct(StringUtils.isEmpty(typResultMap.get("fstPct")) ?
                        null :
                        new BigDecimal(String.valueOf(typResultMap.get("fstPct"))).doubleValue());
                order.setpLoanTypMinAmt(StringUtils.isEmpty(typResultMap.get("minAmt")) ?
                        null :
                        new BigDecimal(String.valueOf(typResultMap.get("minAmt"))).doubleValue());
                order.setpLoanTypMaxAmt(StringUtils.isEmpty(typResultMap.get("maxAmt")) ?
                        null :
                        new BigDecimal(String.valueOf(typResultMap.get("maxAmt"))).doubleValue());
                order.setpLoanTypGoodMaxNum(StringUtils.isEmpty(typResultMap.get("goodMaxNum")) ?
                        0 :
                        (Integer) typResultMap.get("goodMaxNum"));
                order.setpLoanTypTnrOpt((String) typResultMap.get("tnrOpt"));
            }
        }
    }

    /**
     * 计算首付比例
     *
     * @param order
     */
    public void calcFstPct(AppOrder order) {
        if (StringUtils.isEmpty(order.getFstPay())) {
            order.setFstPay("0.0");
            order.setFstPct("0.0");
        } else if (!StringUtils.isEmpty(order.getProPurAmt())) {
            // 首付
            BigDecimal fstPay_big = new BigDecimal(order.getFstPay());
            BigDecimal propurAmt_big = new BigDecimal(order.getProPurAmt());
            BigDecimal fstPct_big = BigDecimal.ZERO;
            if (propurAmt_big.compareTo(BigDecimal.ZERO) != 0) {
                fstPct_big = fstPay_big.divide(propurAmt_big, 4, BigDecimal.ROUND_HALF_UP);
            }
            order.setFstPct(fstPct_big.toString());
        }
        logger.info("首付金额：" + order.getFstPay() + "首付比例：" + order.getFstPct());
    }

    /**
     * 设置还款卡号
     *
     * @return
     */
    public void setHkNo(AppOrder order, String token) {
        // 后去还款卡号
        String hkNo = order.getRepayApplCardNo();
        // 判断还款卡号是否已传，如果为空，则从银行卡列表中查询
        if (StringUtils.isEmpty(hkNo)) {
            String custNo = order.getCustNo();
            String url = EurekaServer.CRM + "/app/crm/cust/getBankCard?custNo=" + custNo;
            logger.info("CRM  getBankCard 请求url==" + url);
            String json = HttpUtil.restGet(url, token);
            if (StringUtils.isEmpty(json)) {
                logger.info("CRM==>还款银行卡列表查询失败！请求处理已停止！");
                return;
            }
            Map<String, Object> bankmap = HttpUtil.json2DeepMap(json);
            if (!HttpUtil.isSuccess(json)) {
                logger.info("CRM==>还款银行卡列表查询失败！" + bankmap.get("head"));
                return;
            }
            Map<String, Object> infoMap = (Map<String, Object>) bankmap.get("body");
            List<Map<String, Object>> bankList = (List<Map<String, Object>>) infoMap.get("info");
            // boolean flag = false;
            for (Map<String, Object> bank : bankList) {
                // 银行卡号
                if (((String) bank.get("isDefaultCard")).equals("Y")) {
                    String bankName = (String) bank.get("bankName");
                    String bankCode = (String) bank.get("bankCode");
                    String accBchCde = (String) bank.get("accBchCde");
                    String accBchName = (String) bank.get("accBchName");
                    String cardNo = (String) bank.get("cardNo");
                    String acctProvince = (String) bank.get("acctProvince");//开户省
                    String acctCity = (String) bank.get("acctCity");//开户市
                    String repayAccMobile = String.valueOf(bank.get("mobile"));
                    order.setRepayApplCardNo(cardNo);
                    order.setRepayAccBankCde(bankCode);
                    order.setRepayAccBankName(bankName);
                    order.setRepayAccBchCde(accBchCde);
                    order.setRepayAccBchName(accBchName);
                    order.setRepayAcProvince(acctProvince);
                    order.setRepayAcCity(acctCity);
                    order.setRepayAccMobile(repayAccMobile);//还款卡手机号

                    // 还款卡省、还款卡市、还款卡开户名不做处理了
                    // order.setRepayAcProvince(mapBody.get("acctProvince").toString());
                    // order.setRepayAcCity(mapBody.get("acctCity").toString());
                    //                     order.setRepayApplAcNam(mapBody.get("custName").toString());
                    break;
                }
            }
        } else {
            // 根据卡号，从银行卡列表中查询
            String url = EurekaServer.CRM + "/app/crm/cust/getCustBankCardByCardNo?cardNo="
                    + hkNo;
            logger.info("CRM==》getCustBankCardByCardNo接口请求url==" + url);
            String json = HttpUtil.restGet(url, token);
            if (StringUtils.isEmpty(json)) {
                logger.info("CRM==>getCustBankCardByCardNo接口返回结果异常！请求处理停止！");
                return;
            }
            Map<String, Object> bankmap = HttpUtil.json2Map(json);
            logger.info("bankmap==" + bankmap);
            if (HttpUtil.isSuccess(bankmap)) {
                Map<String, Object> bank = HttpUtil.json2Map(bankmap.get("body").toString());
                String bankName = (String) bank.get("bankName");
                String bankCode = (String) bank.get("bankCode");
                String accBchCde = (String) bank.get("accBchCde");
                String accBchName = (String) bank.get("accBchName");
                String acctProvince = (String) bank.get("acctProvince");//开户省
                String acctCity = (String) bank.get("acctCity");//开户市
                order.setRepayAccBankCde(bankCode);
                order.setRepayAccBankName(bankName);
                order.setRepayAccBchCde(accBchCde);
                order.setRepayAccBchName(accBchName);
                order.setRepayAcProvince(acctProvince);
                order.setRepayAcCity(acctCity);
                order.setRepayAccMobile((String) bank.get("mobile"));//还款卡手机号

            }
        }
        logger.debug("还款卡手机号：" + order.getRepayAccMobile());
    }

    /**
     * 设置放款卡号
     *
     * @return
     */
    private void setFkNo(AppOrder order, String token) {
        // 获取放款卡号
        String fkNo = order.getApplCardNo();
        if (StringUtils.isEmpty(fkNo)) {
            String custNo = order.getCustNo();
            String url = EurekaServer.CRM + "/app/crm/cust/getBankCard?custNo=" + custNo;
            String json = HttpUtil.restGet(url, token);
            logger.info("CRM getBankCard接口请求URL：" + url);
            logger.info("CRM getBankCard接口返回" + json);
            if (StringUtils.isEmpty(json)) {
                logger.info("银行卡信息获取失败！请求处理停止！");
                return;
            }
            Map<String, Object> bankmap = HttpUtil.json2DeepMap(json);
            if (!HttpUtil.isSuccess(bankmap)) {
                logger.info("银行卡信息获取失败！请求处理停止！" + bankmap);
                return;
            }
            Map<String, Object> infoMap = (Map<String, Object>) bankmap.get("body");
            List<Map<String, Object>> bankList = (List<Map<String, Object>>) infoMap.get("info");
            if (bankList == null || bankList.size() == 0) {
                logger.info("银行卡列表获取失败！！");
            }
            // 是否更新的标识位
            // boolean flag = false;
            for (Map<String, Object> bank : bankList) {
                // 银行卡号
                if (((String) bank.get("isRealnameCard")).equals("Y")) {
                    String bankName = (String) bank.get("bankName");
                    String bankCode = (String) bank.get("bankCode");
                    String accBchCde = (String) bank.get("accBchCde");
                    String accBchName = (String) bank.get("accBchName");
                    String cardNo = (String) bank.get("cardNo");
                    String acctProvince = (String) bank.get("acctProvince");//开户省
                    String acctCity = (String) bank.get("acctCity");//开户市
                    // order.setRepayApplCardNo(cardNo);
                    // order.setRepayAccBankCde(bankCode);
                    // order.setRepayAccBankName(bankName);
                    // order.setRepayAccBchCde(accBchCde);
                    // order.setRepayAccBchName(accBchName);
                    order.setApplCardNo(cardNo);
                    order.setAccBankCde(bankCode);
                    order.setAccBankName(bankName);
                    order.setAccAcBchCde(accBchCde);
                    order.setAccAcBchName(accBchName);
                    order.setRepayAcProvince(acctProvince);
                    order.setRepayAcCity(acctCity);
                    // 放款卡省、放款卡市、放款卡开户名从实名认证中获取
                    // order.setRepayApplAcNam(mapBody.get("custName").toString());
                    // order.setRepayAcProvince(mapBody.get("acctProvince").toString());
                    // order.setRepayAcCity(mapBody.get("acctCity").toString());
                    break;
                }
            }
        } else {
            // 根据卡号，从银行卡列表中查询
            String url = EurekaServer.CRM + "/app/crm/cust/getCustBankCardByCardNo?cardNo="
                    + fkNo;
            String json = HttpUtil.restGet(url, token);
            logger.info("CRM getCustBankCardByCardNo接口请求url==" + url);
            logger.info("CRM ==>getCustBankCardByCardNo接口返回" + json);
            if (StringUtils.isEmpty(json)) {
                logger.info("CRM getCustBankCardByCardNo接口查询银行卡列表查询失败！");
                return;
            }
            Map<String, Object> bankmap = HttpUtil.json2Map(json);
            logger.info("bankmap==" + bankmap);
            if (HttpUtil.isSuccess(bankmap)) {
                Map<String, Object> bank = HttpUtil.json2Map(bankmap.get("body").toString());
                String bankName = (String) bank.get("bankName");
                String bankCode = (String) bank.get("bankCode");
                String accBchCde = (String) bank.get("accBchCde");
                String accBchName = (String) bank.get("accBchName");
                String acctProvince = (String) bank.get("acctProvince");//开户省
                String acctCity = (String) bank.get("acctCity");//开户市
                order.setAccBankCde(bankCode);
                order.setAccBankName(bankName);
                order.setAccAcBchCde(accBchCde);
                order.setAccAcBchName(accBchName);
                order.setRepayAcProvince(acctProvince);
                order.setRepayAcCity(acctCity);
            }
        }
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
        } catch (IntrospectionException e) {
            logger.error(e);
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            logger.error(e);
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            logger.error(e);
            throw new RuntimeException(e);
        }

        return map;
    }

    @Override
    public Map<String, Object> getSysFlagAndChannelNo(AppOrder appOrder) {
        Map<String, Object> crmParam = new HashMap<>();
        crmParam.put("custName", appOrder.getCustName());
        crmParam.put("idNo", appOrder.getIdNo());
        Map<String, Object> custIsPass = crmService.getCustIsPass(crmParam);
        if (custIsPass == null || custIsPass.isEmpty()) {
            return fail(RestUtil.ERROR_INTERNAL_CODE, "CRM 通信失败");
        }
        if (!HttpUtil.isSuccess(custIsPass)) {
            return custIsPass;
        }
        Map<String, Object> bodyMap = (Map<String, Object>) custIsPass.get("body");
        String whiteType = "";
        if (StringUtils.isEmpty(bodyMap.get("isPass")) || "shh".equalsIgnoreCase((String) bodyMap.get("isPass"))) {
            whiteType = "SHH";
        } else {
            whiteType = (String) bodyMap.get("level");
        }
        Map<String, Object> result = new HashMap<>();
        // 来源，1商户（13） 2 个人（14）
        if ("1".equals(appOrder.getSource()) || "13".equals(appOrder.getSource())) {
            result.put("sysFlag", "13");
        } else if ("2".equals(appOrder.getSource()) || "14".equals(appOrder.getSource())) {
            result.put("sysFlag", "14");
        } else if ("3".equals(appOrder.getSource()) || "11".equals(appOrder.getSource())) {
            result.put("sysFlag", "11");
        }
        if ("1".equals(appOrder.getSource())) {
            result.put("channelNo", "05");
        } else {
            // 白名单类型设置channelNo
            if ("A".equals(whiteType)) {
                result.put("channelNo", "17");
            } else if ("B".equals(whiteType)) {
                result.put("channelNo", "18");
            } else if ("SHH".equals(whiteType)) {
                result.put("channelNo", "19");
            } else if ("C".equals(whiteType)) {
                if ("34".equals(appOrder.getChannelNo())) {
                    // H5集团大数据存量用户
                    result.put("channelNo", "34");
                } else if ("2".equals(appOrder.getSource())) {
                    // App集团大数据存量用户
                    result.put("channelNo", "41");
                }
            } else {
                result.put("channelNo", "19");
            }
        }
        if (!StringUtils.isEmpty(super.getChannel())) {
            result.put("sysFlag", super.getChannel());
        }
        if (!StringUtils.isEmpty(super.getChannelNo())) {
            result.put("channelNo", super.getChannelNo());
        }
        if ("3".equals(appOrder.getSource())) {
            result.put("channelNo", "27");//27为天行财富
        }
        // 渠道来源为星巢贷
        if ("16".equals(appOrder.getSource())) {
            result.put("sysFlag", "16");
            result.put("channelNo", "31");
        }
        if ("34".equals(appOrder.getChannelNo())) {
            result.put("sysFlag", "11");
            result.put("channelNo", "34");
        }
        //美分期
        if ("35".equals(appOrder.getChannelNo())) {
            result.put("sysFlag", "11");
            result.put("channelNo", "35");
        }

        // 如果为空，设置默认值》
        if (StringUtils.isEmpty(result.get("sysFlag"))) {
            result.put("sysFlag", "04");
            result.put("channelNo", "05");
        }

        return result;
    }

    @Override
    public Map<String, Object> commitAppOrder(String orderNo, String applSeq, String opType, String msgCode,
                                              String expectCredit, String typGrp) {

        // 去收单查该订单的信息
        AppOrder apporder = acquirerService.getAppOrderFromAcquirer(applSeq, super.getChannelNo());
        if (apporder == null) {
            return fail("06", "贷款详情获取失败");
        }

        logger.info("订单提交业务类型:" + applSeq + ", typgrp:" + apporder.getTypGrp());
        // 商户版直接提交核心
        if ("13".equals(super.getChannel())) {
            opType = "1";
            // 现金贷直接提交核心
        } else if ("02".equals(apporder.getTypGrp())) {
            opType = "1";
        } else {
            // 个人版商品贷，提交方式后台控制.
            if (!"13".equals(super.getChannel()) && !StringUtils.isEmpty(apporder.getGoodsCode())) {
                // 校验当前商品提交给商户还是直接提交核心
                Map<String, Object> needAndConfirm = gmService.getIsNeedSendAndIsConfirm(apporder.getGoodsCode());
                if (!HttpUtil.isSuccess(needAndConfirm)) {
                    return needAndConfirm;
                }
                Map<String, Object> needAndConfirmBody = (Map<String, Object>) needAndConfirm.get("body");
                String isConfirm = needAndConfirmBody.get("isConfirm").toString();
                // 如果是被退回订单，不提交给商户
                boolean isReturnOrder = "22".equals(apporder.getStatus());
                if ("Y".equals(isConfirm) && !isReturnOrder) {
                    opType = "2";
                } else {
                    opType = "1";
                }
            }
            // 个人版自定义商品需商户确认.
            if (!"13".equals(super.getChannel()) && StringUtils.isEmpty(apporder.getGoodsCode())) {
                opType = "2";
            }
        }
        logger.info("提交订单方式：" + applSeq + ", type:" + opType);
        apporder.setOrderNo(orderNo);
        apporder.setTypGrp(typGrp);
        apporder.setSource(super.getChannel());
        apporder.setChannelNo(super.getChannelNo());
        // 获取客户实名认证信息
        Map<String, Object> custInfo = cmisApplService
                .getSmrzInfoByCustNameAndIdNo(apporder.getCustName(), apporder.getIdNo());
        if (custInfo == null || !HttpUtil.isSuccess(custInfo)) {
            logger.info("获取用户实名信息失败, custName:" + apporder.getCustName() + ",idNo:" + apporder.getIdNo());
            return fail("03", "获取实名信息失败");
        }
        custInfo = HttpUtil.json2DeepMap(custInfo.get("body").toString());
        // 用户编号
        apporder.setCustNo(custInfo.get("custNo").toString());

        // 校验人脸分值是否满足要求.
        logger.info("订单提交人脸资格校验:" + orderNo + ", idNo:" + apporder.getIdNo());
        try {
            Map<String, Object> ifNeedFace;
            logger.info("订单提交人脸校验输出:channel:" + super.getChannel() + ",channelNo:" + super.getChannelNo());
            //人脸区分 个人版商户版等
            AppOrdernoTypgrpRelation appOrdernoTypgrpRelation = appOrdernoTypgrpRelationRepository
                    .findByApplSeq(applSeq);
            logger.info(
                    "订单信息: channel:" + appOrdernoTypgrpRelation.getChannel() + ",channelNo" + appOrdernoTypgrpRelation
                            .getChannelNo());
            String sourceStr = "2";// 1-APP商户版 2-APP个人版（默认） 16-星巢贷
            if ("13".equals(appOrdernoTypgrpRelation.getChannel())) {//商户版
                sourceStr = "1";
            } else if ("16".equals(appOrdernoTypgrpRelation.getChannel())) {
                sourceStr = appOrdernoTypgrpRelation.getChannel();
            } else if ("11".equals(appOrdernoTypgrpRelation.getChannel())) {
                sourceStr = appOrdernoTypgrpRelation.getChannelNo();
            }

            if ("13".equals(appOrdernoTypgrpRelation.getChannel())) {
                ifNeedFace = merchFaceService.ifNeedFaceCheckByTypCde(orderNo, sourceStr, null);
            } else {
                if ("11".equals(appOrdernoTypgrpRelation.getChannel())) {
                    if ("34".equals(appOrdernoTypgrpRelation.getChannelNo()) || "35"
                            .equals(appOrdernoTypgrpRelation.getChannelNo()) || "33"
                            .equals(appOrdernoTypgrpRelation.getChannelNo())) {
                        ifNeedFace = personFaceService
                                .ifNeedFaceChkByTypCde(apporder.getTypCde(), sourceStr, apporder.getCustNo(),
                                        apporder.getCustName(), apporder.getIdNo());
                    } else {
                        ifNeedFace = personFaceService
                                .ifNeedFaceCheckByTypCde(orderNo, sourceStr, apporder.getCustNo(),
                                        apporder.getCustName(), apporder.getIdNo());
                    }
                } else {
                    ifNeedFace = personFaceService
                            .ifNeedFaceCheckByTypCde(orderNo, sourceStr, apporder.getCustNo(),
                                    apporder.getCustName(), apporder.getIdNo());
                }
            }
            if (StringUtils.isEmpty(ifNeedFace)) {
                logger.info("订单提交人脸分值校验返回空," + apporder.getIdNo() + "," + apporder.getCustName());
                return fail("61", "人脸分值校验失败");
            }
            if (!super.isSuccess(ifNeedFace)) {
                return fail("61", "人脸分值校验失败");
            } else {
                String isPass = (String) ((Map<String, Object>) ifNeedFace.get("body")).get("code");
                if (!"00".equals(isPass)) {
                    return fail("60", "人脸分值不足");
                }
            }
            logger.info("订单提交人脸校验结果：" + ifNeedFace);
        } catch (Exception e) {
            logger.error(e);
            logger.info("订单提交人脸校验发生错误:" + apporder.getIdNo());
            return fail("61", "人脸分值校验失败");
        }

        /**
         * 处理白名单类型及准入资格
         * 1、调crm 28接口，查询未实名认证客户的准入资格，如果返回不准入，则返回订单提交失败信息，提示不准入。
         * 2、调crm61接口查询白名单最高级别，如果跟订单的白名单级别不符，则将订单的白名单级别更新为查询出的最高级别
         * 3、将订单对象的白名单更新成查询出的最高级别并实例化到数据库
         * **/
        //获取准入资格
        Map<String, Object> isPassMap = this
                .getCustIsPassFromCrm(apporder.getCustName(), apporder.getIdNo(), apporder.getIndivMobile());
        logger.info("准入资格方法返回：" + isPassMap);
        JSONObject head = (JSONObject) isPassMap.get("head");
        String retFlag = head.getString("retFlag");
        String retMsg = head.getString("retMsg");
        if (!"00000".equals(retFlag)) {
            return fail(retFlag, retMsg);//返回crm的错误码
        }
        JSONObject bodyJson = (JSONObject) isPassMap.get("body");
        String isPass = String.valueOf(bodyJson.get("isPass"));
        if ("-1".equals(isPass)) {
            logger.info("准入资格校验失败，失败原因：" + isPassMap);
            return fail("86", "准入资格校验失败！");
        }

        // 订单号、短信验证码（来源为个人、贷款类型为一般消费品时，必填） 3为支付平台，处理方式同个人版
        // 星巢贷用户不校验短信验证码
        boolean isPerson = "14".equals(super.getChannel()) || StringUtils.isEmpty(super.getChannel());
        boolean isBigData = "34".equals(super.getChannelNo());
        boolean isChannelNeedCode = isPerson || isBigData;
        if (isChannelNeedCode && "02".equals(typGrp)) {
            //        if ((StringUtils.isEmpty(super.getChannel()) || (!super.getChannel().equals("11") && !super.getChannel().equals("16")))
            //                && ("2".equals(apporder.getSource()) || "3".equals(apporder.getSource()) || "34".equals(apporder.getChannelNo()))
            //                && "02".equals(apporder.getTypGrp())) {// 02一般消费品
            // 现金贷有部分品种需要商户确认的，商户确认提交不需要短信验证码
            String url = EurekaServer.CMISPROXY + "/api/appl/queryIsWsLoanTyp?typCde=" + apporder.getTypCde();
            String json = HttpUtil.restGet(url, super.getToken());
            Map<String, Object> typWsMap = HttpUtil.json2Map(json);
            if ("0".equals(typWsMap.get("result"))) {
                if (null == msgCode || "".equals(msgCode)) {
                    return fail("05", "短信验证码不能为空!");
                }
                //直接取订单中的手机号
                //个人版的手机号校验规则：验证的时候取绑定的手机号，并把绑定的手机号更新至订单。
                this.updateAppOrderMobile(apporder, getToken());
                String checkVerifyNoResult = FileSignUtil.checkVerifyNo(apporder.getIndivMobile(), msgCode);
                if (!"00000".equals(checkVerifyNoResult)) {
                    return fail("06", checkVerifyNoResult);
                }
            }
        }

        // 查询当前用户信息
        String clientId = "A0000055B0FB82";// TODO 这里暂时使用固定值，不会影响业务数据

        if (opType.equals("1")) {//3为支付平台，处理方式同个人版
            try {
                AppOrdernoTypgrpRelation relation = appOrdernoTypgrpRelationRepository.findByApplSeq(applSeq);
                if (relation == null) {
                    logger.info("订单不存在， applSeq：" + applSeq);
                    return fail("19", "订单不存在");
                }

                if ("14".equals(relation.getChannel()) || "27".equals(super.getChannelNo())
                        || "16".equals(super.getChannel()) || "34".equals(super.getChannelNo())
                        || "35".equals(super.getChannelNo()) || "33".equals(super.getChannelNo())) {
                    logger.debug("复制个人影像开始");
                    boolean success;
                    success = attachService
                            .ftpFiles(apporder.getCustNo(), apporder.getApplSeq(), false, super.getChannelNo());
                    if (success) {
                        logger.info("custNo:" + apporder.getCustNo() + "个人影像上传成功");
                    } else {
                        logger.info("custNo:" + apporder.getCustNo() + "个人影像上传失败");
                        return fail("56", "个人影像上传信贷失败.");
                    }
                    logger.debug("复制个人影像结束");
                }

            } catch (Exception e) {
                logger.error("个人影像全部提交失败：" + e.getMessage());
            }
            // 上传人脸照片
            attachService.uploadFacePhoto(apporder.getCustNo(), apporder.getApplSeq());

            logger.debug("贷款申请提交, " + apporder.getApplSeq());
            apporder.setExpectCredit(expectCredit);//期望额度
            Map<String, Object> result = cmisApplService.commitBussiness(apporder.getApplSeq(), apporder);
            logger.debug("订单提交commitBussiness方法返回：" + result);
            HashMap<String, Object> hm = new HashMap<>();
            if (HttpUtil.isSuccess(result)) {
                try {
                    if (!StringUtils.isEmpty(super.getChannel()) && super.getChannel().equals("16")) {
                        // 如果是星巢贷的订单，应通知信贷更新营销人员信息
                        // 判断渠道进件成功
                        logger.debug(
                                "星巢贷业务，营销人员信息：promCde=" + apporder.getPromCde() + "/promDesc=" + apporder
                                        .getPromDesc()
                                        + "/promPhone="
                                        + apporder.getPromPhone());
                        Map<String, Object> redStarRiskInfo = cmisApplService.updateRedStarRiskInfo(apporder);
                        logger.debug("星巢贷业务通知信贷更新营销人员信息结果：" + redStarRiskInfo);
                    }

                    // 提交合同签章
                    logger.debug("提交合同签章请求开始:" + orderNo);
                    Map<String, Object> resultMap = caSignService.caSignRequest(orderNo, clientId, "1");
                    logger.debug("提交合同签章请求结束:" + orderNo);
                    logger.debug("合同签章结束后返回：" + resultMap);
                    if ("err".equals(resultMap.get("resultCode"))) {
                        logger.error("提交合同签章失败:" + resultMap.get("resultMsg"));
                    }

                    hm.put("orderNo", orderNo);
                    hm.put("isDeleteAppl", "0");// 避免调用贷款取消接口
                } catch (Exception e) {
                    logger.error("提交合同签章并删除订单发生未知异常:" + e.getMessage());
                }
                // 返回申请流水号
                result.clear();
                /* 提交订单成功年后删除共同还款人替代影像信息
                if (apporder.getSource().equals("1")) {
                    attachService.deleteCommonReplaceImage(apporder.getApplSeq(), apporder.getCommonCustNo(), apporder.getTypCde());
                }*/
                result.put("applSeq", apporder.getApplSeq());
                result.put("applCde", apporder.getApplCde());
                logger.debug("提交订单成功:" + orderNo);
                return success(result);
            } else {
                Map<String, Object> mapHead = (Map) result.get("head");
                return fail("15", "提交订单失败，" + mapHead.get("retMsg"));
            }
        } else if (opType.equals("2")) {
            Map<String, Object> submitResult = orderService.submitOrder(orderNo, "1");
            if (!HttpUtil.isSuccess(submitResult)) {
                logger.info("订单提交到商户失败,applSeq:" + applSeq + ",result:" + submitResult);
            }
            return submitResult;
        } else {
            return fail("05", "订单提交类型不正确");
        }
    }

    @Override
    public Map<String, Object> cancelAppOrder(String orderNo) {
        AppOrdernoTypgrpRelation relation = appOrdernoTypgrpRelationRepository.findOne(orderNo);
        if (relation == null) {
            logger.info("订单关系表查询失败, orderNo:" + orderNo);
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "要更新的订单不存在！");
        }

        Map<String, Object> result;
        if ("01".equals(relation.getTypGrp())) {
            AppOrder order = new AppOrder();
            order.setApplSeq(relation.getApplSeq());
            order.setOrderNo(orderNo);
            result = acquirerService.cancelAppl(order);
            if (!CmisUtil.getIsSucceed(result)) {
                logger.info("收单系统取消贷款申请失败, applSeq:" + orderNo);
                return fail("16", "取消贷款申请失败");
            }
        } else if ("02".equals(relation.getTypGrp())) {
            result = orderService.cancelOrder(orderNo);
            if (!HttpUtil.isSuccess(result)) {
                logger.info("订单系统取消订单失败, orderNo:" + orderNo);
                return fail("17", "订单系统取消贷款申请失败");
            }
        }
        // 订单提交时，清空修改状态字段
        relation.setState("");
        appOrdernoTypgrpRelationRepository.save(relation);
        return success();
    }

    @Override
    public Map<String, Object> getAppOrderAndGoods(String orderNo) {
        AppOrdernoTypgrpRelation relation = appOrdernoTypgrpRelationRepository.findOne(orderNo);
        if (relation == null) {
            logger.info("查询relation失败，订单不存在, orderNo:" + orderNo);
            return fail("02", "订单不存在");
        }
        Map<String, Object> applInfMap = acquirerService
                .getApplInfFromAcquirer(relation.getApplSeq(), super.getChannelNo());
        Map<String, Object> applInfMapTem = new HashMap<>();
        applInfMapTem.putAll(applInfMap);
        AppOrder appOrder = acquirerService.acquirerMap2OrderObject(applInfMapTem, new AppOrder());
        if (appOrder == null) {
            return fail(RestUtil.ERROR_INTERNAL_CODE, "所查询的订单不存在！");
        }
        appOrder.setCustNo(relation.getCustNo());
        List<Map<String, Object>> goodList = new ArrayList<>();
        if (!"02".equals(relation.getTypGrp())) {
            Map<String, Object> goodMap = orderService.getGoodsList(orderNo);
            if (!HttpUtil.isSuccess(goodMap)) {
                return fail("15", "商品列表查询失败");
            }
            Map<String, Object> goodBodyMap = (Map<String, Object>) goodMap.get("body");
            goodList = (List<Map<String, Object>>) goodBodyMap.get("orderGoodsMapList");

            if (goodList == null) {
                logger.info("订单不存在任何商品！");
                // return fail(RestUtil.ERROR_INTERNAL_CODE, "订单不存在任何商品！");
            } else {
                goodList.forEach(goodsMap -> FormatUtil
                        .changeKeyName(Arrays.asList("goodsInfId"), Arrays.asList("seqNo"), goodsMap));
            }
        }
        this.setFkNo(appOrder, super.getToken());
        this.setHkNo(appOrder, super.getToken());

        HashMap<String, Object> hm = new HashMap<>();
        hm.put("mthAmt", StringUtils.isEmpty(applInfMap.get("mth_amt")) ? null : applInfMap.get("mth_amt"));// 每期还款额
        // 订单号
        hm.put("orderNo", orderNo);
        //期望额度expectCredit
        hm.put("expectCredit",
                StringUtils.isEmpty(appOrder.getExpectCredit()) || Objects.equals("null", appOrder.getExpectCredit()) ?
                        "" :
                        appOrder.getExpectCredit());
        // 申请流水号
        hm.put("applseq", appOrder.getApplSeq());
        // 客户姓名
        hm.put("custName", appOrder.getCustName());
        // 增加证件类型 证件号码字段
        hm.put("idTyp", appOrder.getIdTyp() == null ? "" : appOrder.getIdTyp());
        hm.put("idNo", appOrder.getIdNo() == null ? "" : appOrder.getIdNo());
        // 客户手机号
        String bdMobile = this.getBindMobileByCustNameAndIdNo(appOrder.getCustName(), appOrder.getIdNo(), null);
        logger.debug("绑定手机号：" + bdMobile);
        if (Objects.equals(appOrder.getIndivMobile(), bdMobile)) {
            hm.put("indivMobile", appOrder.getIndivMobile());
        } else {
            if (!StringUtils.isEmpty(bdMobile)) {//若绑定手机号查询不为空，说明统一认证能查出来将绑定手机号写入订单
                hm.put("indivMobile", bdMobile);
                //将绑定手机号写入订单
            } else {
                hm.put("indivMobile", appOrder.getIndivMobile());
            }
            //若查询为空，则此处不做处理了！！
        }

        // 贷款品种、借款期限类型、借款期限、商品总额、首付金额、送货地址类型、送货地址、借款总额、息费总额
        hm.put("typCde", appOrder.getTypCde() == null ? "" : appOrder.getTypCde());
        // 借款期限类型
        hm.put("applyTnrTyp", appOrder.getApplyTnrTyp() == null ? "" : appOrder.getApplyTnrTyp());
        // 借款期限
        hm.put("applyTnr", appOrder.getApplyTnr() == null ? "" : appOrder.getApplyTnr());
        // 贷款类型
        hm.put("typGrp", appOrder.getTypGrp() == null ? "" : appOrder.getTypGrp());
        // 商品总额
        hm.put("proPurAmt", appOrder.getProPurAmt() == null ? "" : appOrder.getProPurAmt());
        // 首付金额
        hm.put("fstPay", appOrder.getFstPay() == null ? "" : appOrder.getFstPay());
        // 送货地址类型
        hm.put("deliverAddrTyp", appOrder.getDeliverAddrTyp() == null ? "" : appOrder.getDeliverAddrTyp());
        // 送货地址
        hm.put("deliverAddr", appOrder.getDeliverAddr() == null ? "" : appOrder.getDeliverAddr());
        // 借款总额
        hm.put("applyAmt", appOrder.getApplyAmt() == null ? "" : appOrder.getApplyAmt());
        // 总利息金额
        hm.put("totalnormint", appOrder.getTotalnormint() == null ? "" : appOrder.getTotalnormint());
        // 费用总额
        hm.put("totalfeeamt", appOrder.getTotalfeeamt() == null ? "" : appOrder.getTotalfeeamt());
        // 商户编号（merchNo） 门店编码（cooprCde） 贷款类型（typGrp）
        String merchNo = appOrder.getMerchNo() == null ? "" : appOrder.getMerchNo();
        hm.put("merchNo", merchNo);
        // 返回商家名称
        String merchName = "";
        if (appOrder.getMerchNo() != null) {
            String crmUrl = EurekaServer.CRM + "/pub/crm/cust/getMerchInfoByMerchNo?merchNo=" + merchNo;
            logger.info("向CRM请求查询商户名称:" + crmUrl);
            String crmJson = HttpUtil.restGet(crmUrl);
            logger.info("CRM查询得到商户信息：" + crmJson);
            if (HttpUtil.isSuccess(crmJson)) {
                Map<String, Object> crmMap = HttpUtil.json2Map(crmJson);
                if (!StringUtils.isEmpty(crmMap.get("body"))) {
                    Map<String, Object> bodyMap = HttpUtil.json2Map(crmMap.get("body").toString());
                    merchName = StringUtils.isEmpty(bodyMap.get("merchChName")) ?
                            "" :
                            (String) bodyMap.get("merchChName");
                }
            }
        }
        hm.put("merchName", merchName);

        hm.put("cooprCde", appOrder.getCooprCde() == null ? "" : appOrder.getCooprCde());
        hm.put("cooprName", appOrder.getCooprName() == null ? "" : appOrder.getCooprName());
        hm.put("typGrp", appOrder.getTypGrp() == null ? "" : appOrder.getTypGrp());
        hm.put("custNo", relation.getCustNo() == null ? "" : relation.getCustNo());
        // 还款方式: pay_mtd用于显示
        hm.put("payMtd", appOrder.getPayMtd() == null ? "" : appOrder.getPayMtd());
        hm.put("payMtdDesc", appOrder.getPayMtdDesc() == null ? "" : appOrder.getPayMtdDesc());
        // 放款银行，支行代码
        hm.put("accBankCde", appOrder.getAccBankCde() == null ? "" : appOrder.getAccBankCde());
        hm.put("accBankName", appOrder.getAccBankName() == null ? "" : appOrder.getAccBankName());
        hm.put("accAcBchCde", appOrder.getAccAcBchCde() == null ? "" : appOrder.getAccAcBchCde());
        hm.put("applCardNo", appOrder.getApplCardNo() == null ? "" : appOrder.getApplCardNo());
        if (!StringUtils.isEmpty(appOrder.getApplCardNo()) && "02".equals(relation.getTypGrp())) {
            // 从crm查放款卡支行信息
            Map<String, Object> cardInfoMap = crmService.getCustBankCardByCardNo(appOrder.getApplCardNo());
            if (!HttpUtil.isSuccess(cardInfoMap)) {
                logger.error("CRM 查询放款卡信息失败");
                return cardInfoMap;
            }
            Map<String, Object> cardInfoBody = (Map<String, Object>) cardInfoMap.get("body");
            hm.put("accAcBchName", cardInfoBody.get("accBchName") == null ? "" : cardInfoBody.get("accBchName"));
        } else {
            hm.put("accAcBchName", appOrder.getAccAcBchName() == null ? "" : appOrder.getAccAcBchName());
        }
        // 还款银行，支行代码

        hm.put("repayAccBankCde", appOrder.getRepayAccBankCde() == null ? "" : appOrder.getRepayAccBankCde());
        hm.put("repayAccBankName", appOrder.getRepayAccBankName() == null ? "" : appOrder.getRepayAccBankName());
        hm.put("repayAccBchCde", appOrder.getRepayAccBchCde() == null ? "" : appOrder.getRepayAccBchCde());
        hm.put("repayAccBchName", appOrder.getRepayAccBchName() == null ? "" : appOrder.getRepayAccBchName());
        hm.put("repayApplCardNo", appOrder.getRepayApplCardNo() == null ? "" : appOrder.getRepayApplCardNo());
        hm.put("repayAcProvince", appOrder.getRepayAcProvince() == null ? "" : appOrder.getRepayAcProvince());
        hm.put("repayAcCity", appOrder.getRepayAcCity() == null ? "" : appOrder.getRepayAcCity());

        // 贷款用途
        hm.put("purpose", appOrder.getPurpose() == null ? "" : appOrder.getPurpose());
        // 送货地址
        hm.put("deliverAddr", appOrder.getDeliverAddr() == null ? "" : appOrder.getDeliverAddr());
        // 送货地址省
        hm.put("deliverProvince", appOrder.getDeliverProvince() == null ? "" : appOrder.getDeliverProvince());
        // 送货地址市
        hm.put("deliverCity", appOrder.getDeliverCity() == null ? "" : appOrder.getDeliverCity());
        // 送货地址区
        hm.put("deliverArea", appOrder.getDeliverArea() == null ? "" : appOrder.getDeliverArea());
        // 贷款品种名称
        hm.put("typDesc", appOrder.getTypDesc() == null ? "" : appOrder.getTypDesc());
        // 息费总额
        String zfy = StringUtils.isEmpty(appOrder.getTotalfeeamt()) ? "0" : appOrder.getTotalfeeamt();
        String zlx = StringUtils.isEmpty(appOrder.getTotalnormint()) ? "0" : appOrder.getTotalnormint();
        //息费总额的计算改为bigDecimal
        String xfze = new BigDecimal(zfy).add(new BigDecimal(zlx)).toString();
        hm.put("xfze", xfze);
        // 是否已确认协议
        hm.put("isConfirmAgreement", relation.getIsConfirmAgreement() == null ? "0" : relation.getIsConfirmAgreement());
        // 是否已确认合同
        hm.put("isConfirmContract", relation.getIsConfirmContract() == null ? "0" : relation.getIsConfirmContract());
        // 个人信息是否已经完整
        hm.put("isCustInfoCompleted",
                relation.getIsCustInfoComplete() == null ? "" : relation.getIsCustInfoComplete());
        // 退回原因
        hm.put("backReason", appOrder.getBackReason() == null ? "" : appOrder.getBackReason());
        // 商品列表
        hm.put("goods", goodList);
        // 录单备注
        hm.put("appInAdvice", appOrder.getAppInAdvice());
        // 申请日期
        hm.put("applyDt", appOrder.getApplyDt() == null ? "" : appOrder.getApplyDt());
        // 返回销售代表
        hm.put("crtUsr", appOrder.getCrtUsr() == null ? "" : appOrder.getCrtUsr());
        //订单状态
        hm.put("state", appOrder.getState() == null ? "" : appOrder.getState());
        //共同还款人数量
        Map<String, Object> countMap = commonRepaymentPersonService.countCommonRepaymentPerson(appOrder.getApplSeq());
        logger.info("共同还款人数量查询返回:" + countMap);
        if (!HttpUtil.isSuccess(countMap)) {
            logger.info("获取共同还款人数量失败, orderNo:" + orderNo);
            return fail("16", "获取共同还款人数量失败");
        } else {
            hm.put("countCommonRepaymentPerson", ((Map<String, Object>) countMap.get("body")).get("count"));
        }
        /**
         * pLoanTypFstPct;//贷款品种详情的最低首付比例(fstPct)
         pLoanTypMinAmt; //单笔最小贷款金额(minAmt)
         pLoanTypMaxAmt; //单笔最大贷款金额(maxAmt)
         pLoanTypGoodMaxNum;//同笔贷款同型号商品数量上限(goodMaxNum)
         pLoanTypTnrOpt;	//借款期限(tnrOpt)
         */
        hm.put("pLoanTypFstPct", appOrder.getPLoanTypFstPct() == null ? "" : appOrder.getPLoanTypFstPct() + "");
        hm.put("pLoanTypMinAmt", appOrder.getPLoanTypMinAmt() == null ? "" : appOrder.getPLoanTypMinAmt() + "");
        hm.put("pLoanTypMaxAmt", appOrder.getPLoanTypMaxAmt() == null ? "" : appOrder.getPLoanTypMaxAmt() + "");
        hm.put("pLoanTypGoodMaxNum",
                appOrder.getPLoanTypGoodMaxNum() == null ? "" : appOrder.getPLoanTypGoodMaxNum() + "");
        hm.put("pLoanTypTnrOpt", appOrder.getPLoanTypTnrOpt() == null ? "" : appOrder.getPLoanTypTnrOpt() + "");
        /**
         * 查询共同还款人
         */
        String applSeq = appOrder.getApplSeq();
        Map<String, Object> commonRepayList = commonRepaymentPersonService.getCommonRepaymentPerson(applSeq);
        logger.info("共同还款人列表：" + commonRepayList);
        ResultHead head = (ResultHead) commonRepayList.get("head");
        String retFlag = head.getRetFlag();
        String retMsg = head.getRetMsg();
        if ("00000".equals(retFlag)) {
            List bodyMap = (ArrayList) commonRepayList.get("body");
            logger.info("共同还款人body：" + bodyMap);
            hm.put("commonInfo", bodyMap);

        } else {
            hm.put("commonInfo", new ArrayList());
        }
        /**添加日利息的返回
         * **/
        ///返回贷款品种随借随还的日利息
        if (hm.containsKey("typCde")) {
            //获取借款期限类型
            String applyTnrTyp = String.valueOf(hm.get("applyTnrTyp"));
            if (Objects.equals(applyTnrTyp, "D")) {
                //获取贷款品种代码
                String typCde = String.valueOf(hm.get("typCde"));
                //获取借款总额
                //获取审批金额
                String apprvAmt = String
                        .valueOf(StringUtils.isEmpty(appOrder.getApprvAmt()) ? 0 : appOrder.getApprvAmt());
                String applyAmt = String.valueOf(StringUtils.isEmpty(hm.get("applyAmt")) ? 0 : hm.get("applyAmt"));
                //按日进行还款试算
                HashMap<String, Object> parmHm = new HashMap<>();
                parmHm.put("typCde", typCde);
                //若审批金额大于0，则使用审批金额，否则使用申请金额
                if (new BigDecimal(apprvAmt).compareTo(BigDecimal.ZERO) > 0) {
                    parmHm.put("apprvAmt", apprvAmt);
                } else {
                    parmHm.put("apprvAmt", applyAmt);
                }
                parmHm.put("applyTnrTyp", "D");
                parmHm.put("applyTnr", 1);
                Map<String, Object> hkssMap = cmisApplService
                        .getHkssReturnMap(parmHm, super.getGateUrl(), super.getToken());
                logger.debug("还款试算结果：" + hkssMap);
                Map<String, Object> hkssBodyMap = (HashMap<String, Object>) hkssMap.get("body");
                String rlx = (String.valueOf(hkssBodyMap.get("totalNormInt")));// 总利息金额
                hm.put("rlx", rlx);
            } else {
                hm.put("rlx", "");
            }
        } else {
            hm.put("rlx", "");
        }

        // 营销人员信息
        hm.put("promCde", appOrder.getPromCde() == null ? "" : appOrder.getPromCde());
        hm.put("promPhone", appOrder.getPromPhone() == null ? "" : appOrder.getPromPhone());
        hm.put("promDesc", appOrder.getPromDesc() == null ? "" : appOrder.getPromDesc());
        hm.put("addGoodsSwitch", appManageService.getDictDetailByDictCde("addGoodsSwitch"));

        // 订单编辑状态
        hm.put("state", relation.getState());
        return success(hm);
    }

    @Override
    public Map<String, Object> updateOrder(AppOrder src) {

        Map<String, Object> cResult = this.checkLevelC(src);
        if (!HttpUtil.isSuccess(cResult)) {
            return cResult;
        }

        // 现金贷
        AppOrdernoTypgrpRelation relation = appOrdernoTypgrpRelationRepository.findOne(src.getOrderNo());
        if (relation == null) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "要更新的订单不存在！");
        }

        // state更新到relation表
        relation.setState(src.getState());

        // 收单系统获取订单详情
        AppOrder appOrder = acquirerService.getAppOrderFromAcquirer(relation.getApplSeq(), super.getChannelNo());
        if (appOrder == null) {
            logger.info("从收单系统查询订单详情失败, applSeq:" + relation.getApplSeq());
            return fail("05", "订单详情查询失败");
        }
        this.cleanGoodsInfo(appOrder);
        // 如果未传custNo，则通过身份证号去CRM查得，然后保存至relation
        if (StringUtils.isEmpty(appOrder.getCustNo()) && !StringUtils.isEmpty(appOrder.getIdNo()) && !StringUtils
                .isEmpty(appOrder.getCustName())) {
            Map<String, Object> custNoMap = crmService.queryMerchCustInfo(appOrder.getCustName(), appOrder.getIdNo());
            if (!HttpUtil.isSuccess(custNoMap)) {
                return custNoMap;
            }
            Map<String, Object> custNoMapBody = (Map<String, Object>) custNoMap.get("body");
            String custNo = StringUtils.isEmpty(custNoMapBody.get("custNo")) ?
                    "" :
                    (String) custNoMapBody.get("custNo");
            relation.setCustNo(custNo);
        }
        appOrdernoTypgrpRelationRepository.save(relation);

        // 把客户实名信息写入订单
        this.updateCustRealInfo(appOrder, super.getToken());

        if (StringUtils.isEmpty(appOrder.getCustNo()) && !StringUtils.isEmpty(relation.getCustNo())) {
            appOrder.setCustNo(relation.getCustNo());
        }

        // 6商户编号  美凯龙允许修改商户
        if (!StringUtils.isEmpty(src.getSource()) && src.getSource().equals("16")) {
            if (!StringUtils.isEmpty(src.getMerchNo())) {
                appOrder.setMerchNo(src.getMerchNo());
            }
        }
        // 7门店代码
        if (!StringUtils.isEmpty(src.getCooprCde())) {
            appOrder.setCooprCde(src.getCooprCde());
            // 把门店信息写入订单
            this.updateStoreInfo(appOrder, super.getToken());
        }
        // 7期望额度
        if (!StringUtils.isEmpty(src.getExpectCredit())) {
            if (!DataVerificationUtil.isNumber(src.getExpectCredit())) {
                return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "期望额度只能填写数字！");
            }
            appOrder.setExpectCredit(src.getExpectCredit());
        }
        // 14 promCde
        appOrder.setPromCde(src.getPromCde());
        // 15 promDesc
        appOrder.setPromDesc(src.getPromDesc());
        // promPhone
        appOrder.setPromPhone(src.getPromPhone());

        // 16 贷款品种代码
        if (!StringUtils.isEmpty(src.getTypCde())) {
            appOrder.setTypCde(src.getTypCde());
            this.updateTypInfo(appOrder, super.getToken());
            // 还款方式
            String url2 = EurekaServer.CMISPROXY + "/api/pLoanTyp/typMtd?typCde=" + src.getTypCde();
            String json2 = HttpUtil.restGet(url2, super.getToken());
            logger.info(url2 + "==" + json2);
            if (StringUtils.isEmpty(json2)) {
                return fail("01", "CMIS==》查询还款方式失败");
            } else {
                List<Map<String, Object>> typResultList = HttpUtil.json2List(json2);
                appOrder.setMtdCde(typResultList.get(0).get("mtdCde").toString());
            }
        }
        // 20 商品总额
        if (!StringUtils.isEmpty(src.getProPurAmt())) {
            appOrder.setProPurAmt(src.getProPurAmt());
        }
        // 22 首付金额
        if (!StringUtils.isEmpty(src.getFstPay())) {
            appOrder.setFstPay(src.getFstPay());
            // 计算首付比例
            this.calcFstPct(appOrder);
        } else {
            appOrder.setFstPay("0");
            appOrder.setFstPct("0");
        }
        // 23借款总额
        if (!StringUtils.isEmpty(src.getApplyAmt())) {
            if (!DataVerificationUtil.isNumber(src.getApplyAmt())) {
                return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "借款总额只能填写数字！");
            }
            appOrder.setApplyAmt(src.getApplyAmt());
        }
        // 24借款期限
        if (!StringUtils.isEmpty(src.getApplyTnr())) {
            appOrder.setApplyTnr(src.getApplyTnr());
        }
        // 25借款期限类型
        if (!StringUtils.isEmpty(src.getApplyTnrTyp())) {
            appOrder.setApplyTnrTyp(src.getApplyTnrTyp());
        }
        // 26总利息金额
        if (!StringUtils.isEmpty(src.getTotalnormint())) {
            if (!DataVerificationUtil.isNumber(src.getTotalnormint())) {
                return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "总利息金额只能填写数字！");
            }
            appOrder.setTotalnormint(src.getTotalnormint());
        }
        // 27费用总额
        if (!StringUtils.isEmpty(src.getTotalfeeamt())) {
            if (!DataVerificationUtil.isNumber(src.getTotalfeeamt())) {
                return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "费用总额只能填写数字！");
            }
            appOrder.setTotalfeeamt(src.getTotalfeeamt());
        }
        // 28送货地址类型
        if (!StringUtils.isEmpty(src.getDeliverAddrTyp())) {
            appOrder.setDeliverAddrTyp(src.getDeliverAddrTyp());
        } else {
            appOrder.setDeliverAddrTyp("");
        }
        // 29送货地址
        if (!StringUtils.isEmpty(src.getDeliverAddr())) {
            appOrder.setDeliverAddr(src.getDeliverAddr());
        } else {
            appOrder.setDeliverAddr("");
        }
        // 34monthRepay
        if (!StringUtils.isEmpty(src.getMonthRepay())) {
            if (!DataVerificationUtil.isNumber(src.getMonthRepay())) {
                return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "期供只能填写数字！");
            }
            appOrder.setMonthRepay(src.getMonthRepay());
        }
        // // 36 mtdName
        if (!StringUtils.isEmpty(src.getMtdName())) {
            appOrder.setMtdName(src.getMtdName());
        }
        // 43放款卡号
        if (!StringUtils.isEmpty(src.getApplCardNo())) {
            appOrder.setApplCardNo(src.getApplCardNo());
            this.setFkNo(appOrder, getToken());
        }
        // 46放款开户银行分支行代码
        if (!StringUtils.isEmpty(src.getAccAcBchCde())) {
            appOrder.setAccAcBchCde(src.getAccAcBchCde());
        }
        // 47 放款开户银行分支行名
        if (!StringUtils.isEmpty(src.getAccAcBchName())) {
            appOrder.setAccAcBchName(src.getAccAcBchName());
        }

        // 商品信息
        if (!StringUtils.isEmpty(src.getGoodsNum())) {
            appOrder.setGoodsCode(src.getGoodsCode());
            appOrder.setGoodsName(src.getGoodsName());
            appOrder.setGoodsNum(src.getGoodsNum());
            appOrder.setGoodsPrice(src.getGoodsPrice());
            appOrder.setGoodsBrand(src.getGoodsBrand());
            appOrder.setGoodsKind(src.getGoodsKind());
            appOrder.setGoodsModel(src.getGoodsModel());
        }
        // 把还款银行卡支行信息保存到crm
        String accBchCde = src.getAccAcBchCde();
        String accBchName = src.getAccAcBchName();
        if (!StringUtils.isEmpty(accBchCde) && !StringUtils.isEmpty(accBchName)) {
            String url = EurekaServer.CRM + "/app/crm/cust/updateAccBch";
            Map<String, String> params = new HashMap<>();
            params.put("custNo", appOrder.getCustNo());
            params.put("cardNo", appOrder.getApplCardNo());
            params.put("accBchCde", accBchCde);
            params.put("accBchName", accBchName);
            logger.debug("把还款银行卡支行信息保存到crm: " + params);
            String json = HttpUtil.restPut(url, super.getToken(), JSONObject.valueToString(params), 200);
            logger.debug("把还款银行卡支行信息保存到crm, 返回参数json==" + json);
            Map<String, Object> custExtInfoMap = HttpUtil.json2Map(json);
            if (custExtInfoMap == null) {
                return fail("99", "把还款银行卡支行信息保存到crm：未知错误");
            }/* else if (!RestUtil.isSuccess(custExtInfoMap)) {
                return fail("99", "crm系统设置还款银行卡支行信息：" + CmisUtil.getErrMsg(custExtInfoMap));
            }*/
            // 把支行信息写回订单
            appOrder.setAccAcBchCde(accBchCde);
            appOrder.setAccAcBchName(accBchName);
        }

        // 51 还款卡号
        if (!StringUtils.isEmpty(src.getRepayApplCardNo())) {
            this.setHkNo(src, getToken());
            appOrder.setRepayApplCardNo(src.getRepayApplCardNo());
            // 从CRM取还款卡信息，如果还款卡信息不为空，则替换。
            if (!StringUtils.isEmpty(src.getRepayAccBankName())) {
                appOrder.setRepayAccBankName(src.getRepayAccBankName());
                appOrder.setRepayAccBankCde(src.getRepayAccBankCde());
                appOrder.setRepayAccBchCde(src.getRepayAccBchCde());
                appOrder.setRepayAccBchName(src.getRepayAccBchName());
                appOrder.setRepayAcProvince(src.getRepayAcProvince());
                appOrder.setRepayAcCity(src.getRepayAcCity());
                appOrder.setRepayAccMobile(src.getRepayAccMobile());
            }
        }
        // 52 个人信息是否已经完整
        if (!StringUtils.isEmpty(relation.getIsCustInfoComplete())) {
            appOrder.setIsCustInfoCompleted(relation.getIsCustInfoComplete());
        }
        // 62appInAdvice;// 录单备注
        if (src.getAppInAdvice() != null) {//空字符串值有效
            appOrder.setAppInAdvice(src.getAppInAdvice());
        }
        //送货地址省
        if (!StringUtils.isEmpty(src.getDeliverProvince())) {
            appOrder.setDeliverProvince(src.getDeliverProvince());
        } else {
            appOrder.setDeliverProvince("");
        }
        //送货地址市
        if (!StringUtils.isEmpty(src.getDeliverCity())) {
            appOrder.setDeliverCity(src.getDeliverCity());
        } else {
            appOrder.setDeliverCity("");
        }
        //送货地址区
        if (!StringUtils.isEmpty(src.getDeliverArea())) {
            appOrder.setDeliverArea(src.getDeliverArea());
        } else {
            appOrder.setDeliverArea("");
        }

        // 修改标志
        appOrder.setApprvAmt("");
        appOrder.setState(src.getState());
        if (StringUtils.isEmpty(appOrder.getCustNo())) {
            appOrder.setCustNo(relation.getCustNo());
        }
        boolean ifAccessEd = false;
        try {
            logger.info("订单更新校验银行卡限额策略, custNo:" + appOrder.getCustNo());
            ifAccessEd = this.ifAccessEd(appOrder);
        } catch (Exception e) {
            return fail("47", "用户卡信息中不存在该银行卡");
        }
        if (!ifAccessEd) {
            return fail("46", "每期还款额超过银行卡的单笔代收限额！");
        }
        //订单保存后，调贷款详情保存，更新订单信息
        logger.debug("走渠道进件的订单：" + appOrder);
        Map<String, Object> result; //  = cmisApplService.getQdjj(appOrder, appOrder.getOrderNo(), "N");

        // 现金贷
        if ("02".equals(relation.getTypGrp())) {
            result = acquirerService.cashLoan(appOrder, relation);
            if (!CmisUtil.getIsSucceed(result)) {
                logger.info("订单系统保存订单失败：" + CmisUtil.getErrMsg(result));
                return fail("99", "保存订单信息失败：" + CmisUtil.getErrMsg(result));
            }
        } else {
            // 商品贷
            Map<String, Object> orderMap = orderService.order2OrderMap(appOrder, null);
            result = orderService.saveOrUpdateAppOrder(appOrder, orderMap);
            //result = HttpUtil.restPostMap(EurekaServer.ORDER + "api/order/save", orderMap);
            if (!HttpUtil.isSuccess(result)) {
                logger.info("收单系统保存贷款详情失败：" + result);
                return result;
            }
        }
        logger.info("渠道进件后返回结果：" + result);
        Map<String, Object> bodyMap;
        if ("02".equals(relation.getTypGrp())) {
            bodyMap = (Map<String, Object>) ((Map<String, Object>) result.get("response")).get("body");
        } else {
            bodyMap = (Map<String, Object>) result.get("body");
        }
        // 申请流水号
        String applSeq;
        if (!StringUtils.isEmpty(bodyMap.get("appl_seq"))) {
            applSeq = bodyMap.get("appl_seq") + "";
            appOrder.setApplSeq(applSeq);
            // 更新relation表
            relation.setApplSeq(applSeq);
            if (StringUtils.isEmpty(relation.getCustNo())) {
                relation.setCustNo(appOrder.getCustNo());
            }
            appOrdernoTypgrpRelationRepository.save(relation);
        }

        //如果是耐用消费品（01）调用商品管理的接口保存商品和贷款品种关系
        if ("01".equals(appOrder.getTypGrp())) {
            String url = EurekaServer.GM + "/pub/gm/updateLoanByGoodsCode";
            logger.info("GM1.5 商品管理===》多商品修改贷款品种:url" + url);
            /**请求参数封装**/
            Map<String, Object> parmMap = new HashMap<String, Object>();
            //封装订单下所有的商品的goodsCode
            List<String> parmList = new ArrayList();
            parmList.add(appOrder.getGoodsCode());
            parmMap.put("goodsCode", parmList);
            parmMap.put("loanCode", appOrder.getTypCde());
            logger.info("调用商品管理请求参数：parmMap==" + parmMap);
            Map<String, Object> jsonMap = HttpUtil.restPutMap(url, super.getToken(), parmMap);
            if (StringUtils.isEmpty(jsonMap)) {
                logger.info("GM1.5  商品管理==》【多商品修改贷款品种】返回为空，商品管理接口处理失败！");
                //  return fail("01", "新增商品失败！返回为空！");
            } else {
                logger.info("GM1.5 商品管理==》【多商品修改贷款品种】接收到的返回结果为：" + jsonMap);

            }
        }
        HashMap<String, Object> resultMap = new HashMap<>();
        resultMap.put("applSeq", appOrder.getApplSeq() == null ? "" : appOrder.getApplSeq());
        return success(resultMap);
    }

    //银行卡限额策略
    public boolean ifAccessEd(AppOrder order) {

        logger.info("银行卡限额策略,custNo：" + order.getCustNo());
        String custNo = order.getCustNo();
        String hkNo = order.getRepayApplCardNo();
        String payMtd = order.getPayMtd();//还款方式  09表示随借随还，其余的为分期
        logger.info("此用户还款方式为：patyMtd=" + payMtd);
        BigDecimal hkssFirstEd;//比对的金额
        //随借随还的比对金额为申请金额 即applyAmt
        if (Objects.equals("09", payMtd)) {
            hkssFirstEd = new BigDecimal(order.getApplyAmt());
        } else {
            //还款试算
            HashMap<String, Object> hm = new HashMap<>();
            hm.put("typCde", order.getTypCde());
            hm.put("apprvAmt", order.getApplyAmt());
            hm.put("applyTnrTyp", order.getApplyTnrTyp());
            hm.put("applyTnr", order.getApplyTnr());
            hm.put("fstPay", order.getFstPay());
            hm.put("mtdCde", order.getMtdCde());
            Map<String, Object> hkss_json = cmisApplService.getHkssReturnMap(hm, super.getGateUrl(), super.getToken());
            logger.info("还款试算service返回hkss:" + hkss_json);
            Map<String, Object> hkssBodyMap = (HashMap<String, Object>) hkss_json.get("body");
            Map<String, Object> first = (Map) ((List) hkssBodyMap.get("mx")).get(0);//获取第0期的费用
            hkssFirstEd = new BigDecimal(String.valueOf(first.get("instmAmt")));
        }
        //////////////////////
        //从crm查询指定客户的所有银行卡
        Map<String, Object> bankCard = HttpUtil
                .restGetMap(EurekaServer.CRM + "/app/crm/cust/getBankCard?custNo=" + custNo);
        logger.info("客户编号为" + custNo + "的用户的银行卡列表为：" + bankCard);
        Map bodyMap = (Map) bankCard.get("body");
        List<Map<String, Object>> bankList = (List<Map<String, Object>>) bodyMap.get("info");
        boolean flag = false;
        if (bankList.size() > 0) {
            for (Map<String, Object> bank : bankList) {
                // 银行卡号
                if (((String) bank.get("cardNo")).equals(hkNo)) {
                    String singleCollLimited = String.valueOf(bank.get("singleCollLimited"));
                    if (Objects.equals("-1", singleCollLimited)) {
                        logger.info("该银行卡不限额，直接返回成功：orderNo=" + order.getOrderNo() + ";hkNo=" + hkNo);
                        flag = true;
                        return flag;

                    } else {
                        //单笔限额
                        BigDecimal maxEd = new BigDecimal(singleCollLimited);//使用单笔代收金额
                        logger.info("银行卡限额：maxEd=" + maxEd + ",比对的金额：hkssFirstEd=" + hkssFirstEd);
                        if (maxEd.compareTo(hkssFirstEd) >= 0) {
                            return true;
                        } else {
                            logger.info("超过银行卡最大限额:最大额度=" + maxEd + ";测算的每期额度：" + hkssFirstEd);
                            return false;
                        }
                    }
                    //      break;
                }
            }
            return flag;
        } else {
            logger.info("该用户的银行卡列表信息与订单银行卡信息不符,抛出非法用户的异常");
            throw new RuntimeException("非法用户！");

        }
    }

    /**
     * 保存订单号与贷款类型关系
     *
     * @param orderNo
     * @param typGrp
     */
    private void saveRelation(String orderNo, String typGrp, String applSeq, String custNo) {
        AppOrdernoTypgrpRelation relation = appOrdernoTypgrpRelationRepository.findOne(orderNo);
        if (relation == null) {
            if (!StringUtils.isEmpty(applSeq)) {
                relation = appOrdernoTypgrpRelationRepository.findByApplSeq(applSeq);
            }
            if (relation == null) {
                // 新订单
                relation = new AppOrdernoTypgrpRelation();
            } else {
                // 重复创建的情况
                appOrdernoTypgrpRelationRepository.delete(relation);
            }
            relation.setOrderNo(orderNo);
            relation.setTypGrp(typGrp);
            relation.setApplSeq(applSeq);
            relation.setCustNo(custNo);
            relation.setIsConfirmAgreement("0"); // 未确认
            relation.setIsConfirmContract("0");  // 未确认
            relation.setIsCustInfoComplete("N"); // 未确认
            relation.setInsertTime(new Date());
        } else {
            relation.setTypGrp(typGrp);
            relation.setApplSeq(applSeq);
        }
        relation.setChannel(super.getChannel());
        relation.setChannelNo(super.getChannelNo());
        appOrdernoTypgrpRelationRepository.save(relation);
    }

    // 根据客户编号获取白名单类型
    public Map<String, Object> getChannelNoAndWhiteType(String custNo, AppOrder appOrder) {
        if (StringUtils.isEmpty(custNo)) {
            return fail("22", "用户编号不可为空");
        }
        Map<String, Object> levelMap = this.getCustLevel(custNo);
        JSONObject levelhead = (JSONObject) levelMap.get("head");
        String levelRetFlag = levelhead.getString("retFlag");
        String levelRetMsg = levelhead.getString("retMsg");
        if (!"00000".equals(levelRetFlag)) {
            logger.debug("获取准入级别异常：" + levelRetFlag + ":" + levelRetMsg);
            return fail(levelRetFlag, levelRetMsg);//返回crm的错误码
        }
        JSONObject levelBodyMap = (JSONObject) levelMap.get("body");
        String level = String.valueOf(levelBodyMap.get("level"));
        if (!Objects.equals(level, appOrder.getWhiteType())) {
            if ("".equals(level.trim())) {
                appOrder.setWhiteType("shh");
            } else {
                appOrder.setWhiteType(level);
            }

            if ("A".equals(level)) {
                appOrder.setChannelNo("17");
            } else if ("B".equals(level)) {
                appOrder.setChannelNo("18");
            } else {
                appOrder.setChannelNo("19");
            }
        }
        return success();
    }

    @Override
    public Map<String, Object> saveMerchAppOrder(AppOrder appOrder) {
        // 初始化订单参数
        appOrder.setApplyDt(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        appOrder.setIsConfirmAgreement("0");// 0-未确认
        appOrder.setIsConfirmContract("0");// 0-未确认
        appOrder.setProPurAmt("0");// 商品总额，默认为0
        appOrder.setIsCustInfoCompleted("N");// 个人信息是否完整 默认为N 否
        // order.setDeliverAddrTyp("O");//送货地址都传O
        appOrder.setChannelNo(StringUtils.isEmpty(super.getChannelNo()) ? "" : super.getChannelNo());
        // 如果渠道号为空，到crm获取白名单
        if (StringUtils.isEmpty(appOrder.getChannelNo()) && !StringUtils.isEmpty(appOrder.getCustNo())) {
            Map<String, Object> result = this.getChannelNoAndWhiteType(appOrder.getCustNo(), appOrder);
            if (!HttpUtil.isSuccess(result)) {
                return result;
            }
        }
        // 把门店信息写入订单
        this.updateStoreInfo(appOrder, super.getToken());
        // 把客户经理信息写入订单
        this.updateSalesInfo(appOrder, super.getToken());
        // 把客户实名信息写入订单
        this.updateCustRealInfo(appOrder, super.getToken());
        //        if (!StringUtils.isEmpty(order.getApplseq())) {
        //            appOrderRepository.deleteByApplseq(order.getApplseq());
        //        }
        // 保存订单时，不保存用户还款卡信息
        appOrder.setRepayAccBankName("");
        appOrder.setRepayAccBankCde("");
        appOrder.setRepayAcProvince("");
        appOrder.setRepayAcCity("");
        appOrder.setRepayApplCardNo("");
        appOrder.setRepayApplAcNam(appOrder.getCustName());

        String orderJson = JSON.toJSONString(appOrder);
        logger.info("待保存order：" + orderJson);
        if ("02".equals(appOrder.getTypGrp())) {
            Map<String, Object> resultResponseMap = acquirerService.cashLoan(appOrder, null);
            if (CmisUtil.getIsSucceed(resultResponseMap)) {
                Map<String, Object> bodyMap = (Map<String, Object>) ((Map<String, Object>) resultResponseMap
                        .get("response")).get("body");
                String applSeq = (String) bodyMap.get("applSeq");
                this.saveRelation(applSeq, appOrder.getTypGrp(), applSeq, appOrder.getCustNo());
                bodyMap.put("orderNo", applSeq);
                return success(bodyMap);
            }
            return (Map<String, Object>) resultResponseMap.get("response");
        } else {
            Map<String, Object> resultMap = orderService.saveOrUpdateAppOrder(appOrder, null);
            if (HttpUtil.isSuccess(resultMap)) {
                Map<String, Object> bodyMap = (Map<String, Object>) resultMap.get("body");
                this.saveRelation((String) bodyMap.get("orderNo"), appOrder.getTypGrp(),
                        bodyMap.get("applSeq") == null ? null : bodyMap.get("applSeq").toString(),
                        appOrder.getCustNo());
            }
            return resultMap;
        }
    }

    // C类用户录单校验是否合法
    public Map<String, Object> checkLevelC(AppOrder appOrder) {
        // 校验当前用户是否为C类用户，C类用户必须现有额度，再支用。
        String typGrp = appOrder.getTypGrp();
        //17006a，17007a App存量用户现金贷特殊处理
        if("17006a".equals(appOrder.getTypCde()) || "17007a".equals(appOrder.getTypCde())){
//        if ("02".equals(typGrp)){//现金贷需要特殊处理   商品贷不校验是否是会员，是否有额度
            ///身份证号和客户姓名 从实名认证信息中获取
            Map<String, Object> custRealInfoMap = crmService.queryCustRealInfoByCustNo(appOrder.getCustNo());
            if (!HttpUtil.isSuccess(custRealInfoMap)) {
                logger.info("用户未实名，不进行C类用户校验, 客户编号:" + appOrder.getCustNo());
                return success();
            }
            Map<String, Object> custRealInfoBodyMap = (Map<String, Object>) custRealInfoMap.get("body");
            String custName = (String) custRealInfoBodyMap.get("custName");
            String certNo = (String) custRealInfoBodyMap.get("certNo");
            Map<String, Object> custPassInfo = this.getCustIsPassFromCrm(custName, certNo, appOrder.getIndivMobile());
            if (!HttpUtil.isSuccess(custPassInfo)) {
                logger.info("用户未实名，不进行C类用户校验, 身份证号:" + certNo);
            } else {
                String level = (String) HttpUtil.json2Map(custPassInfo.get("body") + "").get("level");
                String isPass = (String) HttpUtil.json2Map(custPassInfo.get("body") + "").get("isPass");
                if ("-1".equals(isPass)) {
                    return fail("97", "当前用户不准入");
                }
                if (!StringUtils.isEmpty(level) && "C".equals(level.toUpperCase())) {
                    Map<String, Object> edInfo = cmisApplService.getEdCheck("20", certNo, null);
                    if (!HttpUtil.isSuccess(edInfo)) {
                        return fail("98", "您暂无额度，请先到个人中心，点击”立即获取“，进行额度激活");
                    } else {
                        Map<String, Object> body = (Map<String, Object>) edInfo.get("body");
                        String crtSts = String.valueOf(body.get("crdSts"));
                        BigDecimal surplusAmt = new BigDecimal(body.get("surplusAmt").toString());
                        int isBigger = surplusAmt.compareTo(new BigDecimal(0.00));
                        // 冻结用户或剩余额度为0， 提示进行额度申请
                        if (crtSts.equals("20") || isBigger <= 0) {
                            return fail("98", "您暂无额度，请先到个人中心，点击”立即获取“，进行额度激活");
                        }
                    }
                }
            }
        }
        return success();
    }

    public Map<String, Object> saveAppOrderInfo(AppOrder appOrder) {

        Map<String, Object> cResult = this.checkLevelC(appOrder);
        if (!HttpUtil.isSuccess(cResult)) {
            return cResult;
        }

        if (StringUtils.isEmpty(appOrder.getWhiteType()) && !StringUtils.isEmpty(appOrder.getWhiteType1())) {
            logger.debug("白名单类型：" + appOrder.getWhiteType1());
            appOrder.setWhiteType(appOrder.getWhiteType1());
        }

        // 初始化订单参数
        appOrder.setApplyDt(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        appOrder.setIsConfirmAgreement("0");// 0-未确认
        appOrder.setIsConfirmContract("0");// 0-未确认
        appOrder.setProPurAmt("0");// 商品总额，默认为0
        appOrder.setIsCustInfoCompleted("N");// 个人信息是否完整 默认为N 否
        appOrder.setChannelNo(StringUtils.isEmpty(super.getChannelNo()) ? "" : super.getChannelNo());
        // 如果渠道号为空，到crm获取白名单
        if (StringUtils.isEmpty(appOrder.getChannelNo()) && !StringUtils.isEmpty(appOrder.getCustNo())) {
            Map<String, Object> result = this.getChannelNoAndWhiteType(appOrder.getCustNo(), appOrder);
            if (!HttpUtil.isSuccess(result)) {
                return result;
            }
        }
        // order.setDeliverAddrTyp("O");//送货地址都传O

        // 把门店信息写入订单
        this.updateStoreInfo(appOrder, super.getToken());
        // 把销售代表信息写入订单
        this.updateSalesInfo(appOrder, super.getToken());

        // 把客户实名信息写入订单。注意：订单可能修改放款支行信息
        String accBchCde = appOrder.getAccAcBchCde();
        String accBchName = appOrder.getAccAcBchName();
        this.updateCustRealInfo(appOrder, super.getToken());
        //        //实名认证手机号先写入，如果userid不为空，则取绑定的手机号
        //        if(!StringUtils.isEmpty(order.getUserId())){
        //            appOrderService.updateBindMobile(order,super.getToken());
        //        }
        if (!StringUtils.isEmpty(accBchCde) && !StringUtils.isEmpty(accBchName)) {
            // 把还款银行卡支行信息保存到crm
            String url = EurekaServer.CRM + "/app/crm/cust/updateAccBch";
            Map<String, String> params = new HashMap<>();
            params.put("custNo", appOrder.getCustNo());
            params.put("cardNo", appOrder.getApplCardNo());
            params.put("accBchCde", accBchCde);
            params.put("accBchName", accBchName);
            logger.debug("把还款银行卡支行信息保存到crm: " + params);
            String json = HttpUtil.restPut(url, super.getToken(), JSONObject.valueToString(params), 200);
            logger.debug("把还款银行卡支行信息保存到crm, 返回参数json==" + json);
            Map<String, Object> custExtInfoMap = HttpUtil.json2Map(json);
            if (custExtInfoMap == null) {
                return fail("99", "把还款银行卡支行信息保存到crm：未知错误");
            }/* else if (!RestUtil.isSuccess(custExtInfoMap)) {
                return fail("99", "crm系统设置还款银行卡支行信息：" + CmisUtil.getErrMsg(custExtInfoMap));
            }*/
            // 把支行信息写回订单
            appOrder.setAccAcBchCde(accBchCde);
            appOrder.setAccAcBchName(accBchName);
        }

        // 把贷款品种信息写入订单
        this.updateTypInfo(appOrder, super.getToken());
        // 还款方式
        String url2 = EurekaServer.CMISPROXY + "/api/pLoanTyp/typMtd?typCde=" + appOrder.getTypCde();
        String json2 = HttpUtil.restGet(url2, super.getToken());
        if (StringUtils.isEmpty(json2)) {
            return fail("01", "查询还款方式失败");
        } else {
            List<Map<String, Object>> typResultList = HttpUtil.json2List(json2);
            appOrder.setMtdCde(typResultList.get(0).get("mtdCde").toString());
        }

        // 增加商品总额、商品列表字段
        //        if (!StringUtils.isEmpty(order.getApplseq())) {
        //            appOrderRepository.deleteByApplseq(order.getApplseq());
        //        }
        boolean ifAccessEd = false;
        try {
            logger.info("个人版保存订单校验银行卡限额策略, custNo:" + appOrder.getCustNo());
            ifAccessEd = this.ifAccessEd(appOrder);
        } catch (Exception e) {
            return fail("47", "用户卡信息中不存在该银行卡");
        }
        if (!ifAccessEd) {
            return fail("46", "每期还款额超过银行卡的单笔代收限额！");
        }
        // 个人版：扫码分期提交给商户(S)，现金贷提交给信贷系统(N)
        String autoFlag = appOrder.getTypGrp().equals("02") ? "N" : "S";

        String orderNo = "";
        String applSeq = "";
        if ("02".equals(appOrder.getTypGrp())) {
            Map<String, Object> resultResponseMap = acquirerService.cashLoan(appOrder, null);
            if (CmisUtil.getIsSucceed(resultResponseMap)) {
                Map<String, Object> bodyMap = (Map<String, Object>) ((Map<String, Object>) resultResponseMap
                        .get("response")).get("body");
                applSeq = (String) bodyMap.get("applSeq");
                orderNo = (String) bodyMap.get("applSeq");
                this.saveRelation(applSeq, appOrder.getTypGrp(), applSeq, appOrder.getCustNo());
            } else {
                return (Map<String, Object>) resultResponseMap.get("response");
            }
        } else {
            Map<String, Object> resultMap = orderService.saveOrUpdateAppOrder(appOrder, null);
            if (HttpUtil.isSuccess(resultMap)) {
                Map<String, Object> bodyMap = (Map<String, Object>) resultMap.get("body");
                orderNo = (String) bodyMap.get("orderNo");
                applSeq = (String) bodyMap.get("applSeq");
                this.saveRelation(orderNo, appOrder.getTypGrp(), applSeq, appOrder.getCustNo());
            } else {
                return resultMap;
            }
        }

        appOrder.setApplSeq(applSeq);

        //如果含有共同还款人信息，则添加共同还款人
        //relation( 关系) maritalStatus( 婚姻状况) officeName( 工作单位) mthInc( 月收入） officeTel( 单位电话） commonCustNo( 共同还款人客户编号）smsCode( 短信验证码）applSeq ( 信贷流水号）
        if (!StringUtils.isEmpty(appOrder.getSmsCode()) && !StringUtils.isEmpty(appOrder.getCommonCustNo())
                && !StringUtils
                .isEmpty(appOrder.getMthInc()) && !StringUtils.isEmpty(appOrder.getRelation()) && !StringUtils
                .isEmpty(appOrder.getMaritalStatus()) && !StringUtils.isEmpty(appOrder.getOfficeName()) && !StringUtils
                .isEmpty(appOrder.getOfficeTel())) {
            String relation = appOrder.getRelation();
            String maritalStatus = appOrder.getMaritalStatus();
            String officeName = appOrder.getOfficeName();
            BigDecimal mthInc = appOrder.getMthInc();
            String officeTel = appOrder.getOfficeTel();
            String commonCustNo = appOrder.getCommonCustNo();
            String smsCode = appOrder.getSmsCode();
            String seq = appOrder.getApplSeq();
            //封装共同还款人信息
            CommonRepaymentPerson commonPerson = new CommonRepaymentPerson();
            commonPerson.setOrderNo(StringUtils.isEmpty(orderNo) ? applSeq : orderNo);
            commonPerson.setCustNo(appOrder.getCustNo());
            commonPerson.setRelation(relation);
            commonPerson.setMaritalStatus(maritalStatus);
            commonPerson.setOfficeName(officeName);
            commonPerson.setOfficeTel(officeTel);
            commonPerson.setMthInc(mthInc);
            commonPerson.setCommonCustNo(commonCustNo);
            commonPerson.setSmsCode(smsCode);
            commonPerson.setApplSeq(seq);
            Map<String, Object> map = commonRepaymentPersonService.addCommonRepaymentPerson(commonPerson, "1");
            logger.info("新增共同还款人service返回：" + map);
            ResultHead head = (ResultHead) map.get("head");
            String retFlag = head.getRetFlag();
            String retMsg = head.getRetMsg();
            if (!"00000".equals(retFlag)) {
                return fail(retFlag, retMsg);
            }
        }

        HashMap<String, Object> hm = new HashMap<>();
        hm.put("orderNo", orderNo);
        hm.put("applSeq", applSeq);
        hm.put("applCde", "");
        return success(hm);
    }

    /**
     * 使用绑定手机号（个人版订单保存使用 6.43）
     *
     * @param order
     * @param token
     */
    public void updateBindMobile(AppOrder order, String token) {
        String userId = order.getUserId();
        if (StringUtils.isEmpty(userId)) {
            return;
        }
        //加密后的str传入接口
        String str = EncryptUtil.simpleEncrypt(userId);
        String url = EurekaServer.UAUTH + "/app/uauth/getMobile" + "?userId=" + str;
        logger.info("统一认证1.21==》请求url==" + url);
        String json = HttpUtil.restGet(url, token);
        logger.info("统一认证1.21==》返回：" + json);
        if (StringUtils.isEmpty(json)) {
            logger.info("统一认证1.21==》(getMobile)接口返回异常！请求处理被迫停止！");
            return;
        }
        json = json.replaceAll("null", "\"\"");
        Map<String, Object> map = HttpUtil.json2Map(json);
        if (HttpUtil.isSuccess(map)) {
            Map<String, Object> mapBody = HttpUtil.json2Map(map.get("body").toString());
            if (!StringUtils.isEmpty(mapBody.get("mobile"))) {
                order.setIndivMobile(mapBody.get("mobile").toString());
            }
            logger.info("用户绑定手机号为：" + order.getIndivMobile());
        }
    }

    /**
     * 通过用户Id查询统一认证手机号
     *
     * @param userId
     * @param token
     * @return
     */
    public String getBindMobileByUserId(String userId, String token) {
        if (StringUtils.isEmpty(userId)) {
            return null;
        }
        //加密后的str传入接口
        String str = EncryptUtil.simpleEncrypt(userId);
        String url = EurekaServer.UAUTH + "/app/uauth/getMobile" + "?userId=" + str;
        logger.info("统一认证1.21==》请求url==" + url);
        String json = HttpUtil.restGet(url, token);
        logger.info("统一认证1.21==》返回：" + json);
        if (StringUtils.isEmpty(json)) {
            logger.info("统一认证1.21==》(getMobile)接口返回异常！请求处理被迫停止！");
            return null;
        }
        json = json.replaceAll("null", "\"\"");
        Map<String, Object> map = HttpUtil.json2Map(json);
        if (HttpUtil.isSuccess(map)) {
            Map<String, Object> mapBody = HttpUtil.json2Map(map.get("body").toString());
            if (!StringUtils.isEmpty(mapBody.get("mobile"))) {
                logger.info("用户绑定手机号为：" + mapBody.get("mobile").toString());
                return mapBody.get("mobile").toString();
            }
        }
        return null;
    }

    /**
     * 根据客户姓名及身份证号查询统一认证手机号
     *
     * @param custName
     * @param idNo
     * @param token
     * @return
     */
    public String getBindMobileByCustNameAndIdNo(String custName, String idNo, String token) {
        //若身份证号或客户姓名为空，则返回null
        if (StringUtils.isEmpty(custName) || StringUtils.isEmpty(idNo)) {
            return null;
        }
        String url =
                EurekaServer.CRM + "/app/crm/cust/getUserIdByCustNameAndCertNo" + "?custName=" + custName + "&certNo="
                        + idNo;
        logger.info("CRM(74)==》请求url==" + url);
        String json = HttpUtil.restGet(url, token);
        logger.info("CRM(74)==》返回：" + json);
        if (StringUtils.isEmpty(json)) {
            logger.info("CRM(74)==》(getUserIdByCustNameAndCertNo)接口返回异常！请求处理被迫停止！");
            return null;
        }
        json = json.replaceAll("null", "\"\"");
        Map<String, Object> map = HttpUtil.json2Map(json);
        if (HttpUtil.isSuccess(map)) {
            Map<String, Object> mapBody = HttpUtil.json2Map(map.get("body").toString());
            logger.info("CRM(74)==》body体：" + mapBody);
            String userId = StringUtils.isEmpty(mapBody.get("userId")) ? null : mapBody.get("userId").toString();
            if (StringUtils.isEmpty(userId)) {
                logger.info("CRM(74)接口查询失败！userId查询为空！");
                return null;
            }
            return this.getBindMobileByUserId(userId, token);
        }
        return null;
    }

    public String getUserIdByCustNameAndIdNo(String custName, String idNo) {
        //若身份证号或客户姓名为空，则返回null
        if (StringUtils.isEmpty(custName) || StringUtils.isEmpty(idNo)) {
            return null;
        }
        String url =
                EurekaServer.CRM + "/app/crm/cust/getUserIdByCustNameAndCertNo" + "?custName=" + custName + "&certNo="
                        + idNo;
        logger.info("CRM(74)==》请求url==" + url);
        String json = HttpUtil.restGet(url, super.getToken());
        logger.info("CRM(74)==》返回：" + json);
        if (StringUtils.isEmpty(json)) {
            logger.info("CRM(74)==》(getUserIdByCustNameAndCertNo)接口返回异常！请求处理被迫停止！");
            return null;
        }
        json = json.replaceAll("null", "\"\"");
        Map<String, Object> map = HttpUtil.json2Map(json);
        if (HttpUtil.isSuccess(map)) {
            Map<String, Object> mapBody = HttpUtil.json2Map(map.get("body").toString());
            logger.info("CRM(74)==》body体：" + mapBody);
            String userId = StringUtils.isEmpty(mapBody.get("userId")) ? null : mapBody.get("userId").toString();
            if (StringUtils.isEmpty(userId)) {
                logger.info("CRM(74)接口查询失败！userId查询为空！");
                return null;
            }
            return userId;
        }
        return null;
    }

    /**
     * 获取实名认证的手机号
     *
     * @param userId
     * @param custName
     * @param idNo
     * @param token
     * @return
     */
    public String getMobileBySmrz(String userId, String custName, String idNo, String token) {
        String mobile = "";//要返回的手机号
        String cust_url = "";
        /** 调用实名认证接口5.13，查询手机号和客户号 **/
        //若用户id
        if (!StringUtils.isEmpty(userId)) {
            cust_url = EurekaServer.CRM + "/app/crm/cust/queryPerCustInfo" + "?userId=" + userId;
        } else if ((!StringUtils.isEmpty(custName)) && (!StringUtils.isEmpty(idNo))) {
            //若客户姓名和身份证号不为空，
            cust_url = EurekaServer.CRM + "/app/crm/cust/queryMerchCustInfo?certNo=" + idNo + "&custName="
                    + custName;
        }
        logger.info("CRM 实名认证接口请求地址：" + cust_url);
        String cust_json = HttpUtil.restGet(cust_url, super.getToken());
        if (StringUtils.isEmpty(cust_json)) {
            logger.info("CRM  该订单的实名认证信息接口查询失败，返回异常！");
            mobile = "";
        }
        Map<String, Object> custMap = HttpUtil.json2Map(cust_json);
        logger.info("CRM 实名认证（17或13）接口返回custMap==" + custMap);
        JSONObject custHeadObject = (JSONObject) custMap.get("head");
        String retFlag = custHeadObject.getString("retFlag");
        if (!"00000".equals(retFlag)) {
            logger.info("实名认证手机号查询失败，返回空！");
            return "";
        }
        Map<String, Object> custBodyMap = HttpUtil.json2Map(custMap.get("body").toString());
        mobile = StringUtils.isEmpty(custBodyMap.get("mobile")) ? null : custBodyMap.get("mobile").toString();
        if (StringUtils.isEmpty(mobile)) {
            logger.info("实名认证手机号查询失败！！");
            logger.info("统一认证绑定手机号及实名认证手机号查询全都失败！返回空");
        }
        logger.info("实名认证：mobile:" + mobile);
        return mobile;
    }

    /**
     * 合同签约提交调用渠道进件接口.
     */
    //    @Transactional(propagation = Propagation.REQUIRED)
    public Map<String, Object> subSignContractQdjj(AppOrder appOrder) {
        logger.info("合同签约提交调用渠道进件接口请求参数：parameterMap=" + appOrder);
        String orderNo = appOrder.getOrderNo();

        if (appOrder == null) {
            logger.error("订单不存在:" + orderNo);
            return null;
        }
        logger.info("合同签约提交订单状态：" + appOrder.getStatus());
        if ("22".equals(appOrder.getStatus())) { //22-被退回
            // 重传影像
            if ("14".equals(appOrder.getSource()) || "16".equals(appOrder.getSource()) || "11"
                    .equals(appOrder.getSource())) {
                try {
                    logger.debug("重传个人影像...");
                    attachService.deleteFtpInterface(appOrder.getApplSeq(), super.getToken());
                    attachService.ftpFiles(appOrder.getCustNo(), appOrder.getApplSeq(), false, appOrder.getChannelNo());
                } catch (Exception e) {
                    logger.error("个人影像全部提交失败：" + e.getMessage());
                }
            }

            Map<String, Object> resultMap = cmisApplService.getQdjj(appOrder, orderNo, "N");
            logger.info("getQdjj返回结果:" + resultMap);
            if (resultMap == null) {
                return fail("99", "保存贷款申请失败：未知错误");
            } else if (!HttpUtil.isSuccess(resultMap)) {
                return fail("99", "保存贷款申请失败：" + resultMap);
            }
            return resultMap;
        }
        return null;

    }

    //根据原始订单的客户信息，更新订单手机号为绑定手机号，并保存数据库
    public void updateAppOrderMobile(AppOrder order, String token) {
        logger.info("订单信息order=" + order);
        String custName = order.getCustName();
        String idNo = order.getIdNo();
        String mobile = getBindMobileByCustNameAndIdNo(custName, idNo, token);
        if (StringUtils.isEmpty(mobile)) {
            logger.info("客户绑定手机号查询失败，现读取实名认证手机号！");
            mobile = getMobileBySmrz(null, custName, idNo, token);
            if (StringUtils.isEmpty(mobile)) {
                logger.info("实名认证手机号查询也失败！！不再处理订单手机号！");

            } else {
                logger.info("订单最终处理结果为实名认证手机号，实名认证手机号查询为：" + mobile);
            }
        }
        logger.info("个人版订单最终更新的手机号为：" + mobile);

        //若手机号不为空，则将该手机号重新保存至数据库
        if (!StringUtils.isEmpty(mobile)) {
            order.setIndivMobile(mobile);
        }
    }

    /**
     * CRM28 查询客户的准入资格
     *
     * @param custName
     * @param idNo
     * @param phone
     * @return
     */
    @Override
    public Map<String, Object> getCustIsPassFromCrm(String custName, String idNo, String phone) {
        // 现金贷有部分品种需要商户确认的，商户确认提交不需要短信验证码
        String url = EurekaServer.CRM + "/app/crm/cust/getCustIsPass?custName=" + custName + "&certNo=" + idNo
                + "&phonenumber=" + phone;
        logger.info("CRM 28接口请求地址：" + url);
        String json = HttpUtil.restGet(url, super.getToken());
        logger.info("CRM 28接口返回：" + json);
        if (StringUtils.isEmpty(json)) {
            logger.info("CRM28接口【查询未实名认证客户的准入资格】请求获得结果为空！");
            return fail("10", "CRM28返回为空！");
        }
        Map<String, Object> isPassMap = HttpUtil.json2Map(json);
        return isPassMap;
    }

    /**
     * CRM28 查询客户白名单最高级别
     *
     * @param custNo
     * @return
     */
    @Override
    public Map<String, Object> getCustLevel(String custNo) {
        // 现金贷有部分品种需要商户确认的，商户确认提交不需要短信验证码
        String url = EurekaServer.CRM + "/app/crm/cust/getCustLevel?custNo=" + custNo;
        logger.info("CRM 61接口请求地址：" + url);
        String json = HttpUtil.restGet(url, super.getToken());
        logger.info("CRM 61接口返回：" + json);
        if (StringUtils.isEmpty(json)) {
            logger.info("CRM61【查询已实名认证的客户白名单最高级别（根据客户编号）】接口请求获得结果为空！");
            return fail("10", "CRM61返回为空！");
        }
        Map<String, Object> levelMap = HttpUtil.json2Map(json);
        return levelMap;
    }

    @Override
    public Map<String, Object> getAppOrderMapFromCmis(String applSeq) {
        /** 从核心数据库查询贷款详情 **/
        String url = EurekaServer.CMISPROXY + "/api/appl/queryApplFull?applSeq=" + applSeq;
        String json = HttpUtil.restGet(url, super.getToken());
        logger.info("通过cmis请求的订单信息的url" + url);
        logger.info("通过cmis获得的订单信息：" + json);
        if (StringUtils.isEmpty(json)) {
            return null;
        }
        Map<String, Object> resultMap = HttpUtil.json2Map(json);
        return resultMap;
    }

    @Override
    public AppOrder getAppOrderFromCmis(String applSeq, String version) {
        /** 从核心数据库查询贷款详情 **/
        String url = EurekaServer.CMISPROXY + "/api/appl/queryApplFull?applSeq=" + applSeq;
        String json = HttpUtil.restGet(url, super.getToken());
        logger.info("通过cmis请求的订单信息的url" + url);
        if (StringUtils.isEmpty(json)) {
            return null;
        }
        Map<String, Object> resultMap = HttpUtil.json2Map(json);
        logger.info("通过cmis请求的订单信息为：" + resultMap);
        if (!HttpUtil.isSuccess(resultMap)) {
            logger.error("通过cmis请求的订单信息失败");
            return null;
        }
        // 商品列表
        // List<Map<String, Object>> goodList = (ArrayList<Map<String, Object>>) resultMap.get("goods");
        // 银行卡信息
        List<Map<String, Object>> cardList = (ArrayList<Map<String, Object>>) resultMap.get("accInfo");
        // 共同还款人
        List<Map<String, Object>> commomPersonList = (ArrayList<Map<String, Object>>) resultMap.get("apptInfo");

        // 封装订单及商品信息
        AppOrder order = new AppOrder();
        // 主键重新生成
        order.setOrderNo(UUID.randomUUID().toString());// 主键订单号
        order.setApplSeq(String.valueOf(resultMap.get("APPL_SEQ")));// 申请流水号
        order.setIdTyp((String) resultMap.get("ID_TYP"));// 客户证件类型
        order.setIdNo((String) resultMap.get("ID_NO"));// 客户证件号码
        order.setCustName((String) resultMap.get("CUST_NAME"));// 客户姓名
        order.setMerchNo((String) resultMap.get("SUPER_COOPR"));// 商户编号
        order.setCooprCde((String) resultMap.get("COOPR_CDE"));// 门店代码
        order.setCooprName((String) resultMap.get("COOPR_NAME"));// 门店名称
        order.setContZone((String) resultMap.get("COOPR_ZONE"));// 门店联系电话区号
        order.setContTel((String) resultMap.get("COOPR_TEL"));// 门店联系电话
        order.setContSub((String) resultMap.get("COOPR_SUB"));// 门店联系电话分机
        order.setTypGrp((String) resultMap.get("TYP_GRP"));// 贷款类型
        order.setPurpose((String) resultMap.get("PURPOSE"));// 贷款用途
        order.setTypCde((String) resultMap.get("LOAN_TYP"));// 贷款品种代码
        order.setTypVer(String.valueOf(resultMap.get("TYP_VER")));// 贷款品种版本号
        order.setTypSeq(String.valueOf(resultMap.get("TYP_SEQ")));// 贷款品种流水号
        order.setApplyDt(String.valueOf(resultMap.get("APPLY_DT")));// 申请日期
        order.setProPurAmt(String.valueOf(resultMap.get("PRO_PUR_AMT")));// 商品总额
        order.setFstPct(String.valueOf(resultMap.get("FST_PCT")));//
        // 首付金额
        if (!StringUtils.isEmpty(resultMap.get("FST_PAY"))) {
            order.setFstPay(String.valueOf(resultMap.get("FST_PAY")));
        } else {
            order.setFstPay("0");
        }
        // 重新计算首付比例，没有首付的异常数据，退回后可以重新计算首付
        //        this.calcFstPct(order);
        order.setApplyAmt(String.valueOf(resultMap.get("APPLY_AMT")));// 借款总额
        order.setApplyTnr(String.valueOf(resultMap.get("APPLY_TNR")));// 借款期限
        order.setApplyTnrTyp((String) resultMap.get("APPLY_TNR_TYP"));// 借款期限类型
        order.setOtherPurpose((String) resultMap.get("OTHER_PURPOSE"));//
        order.setMtdCde((String) resultMap.get("MTD_CDE"));// 还款方式代码
        order.setLoanFreq(String.valueOf(resultMap.get("LOAN_FREQ")));// 还款间隔
        order.setDueDayOpt(String.valueOf(resultMap.get("DUE_DAY_OPT")));//
        order.setDueDay(String.valueOf(resultMap.get("DUE_DAY")));//
        order.setDocChannel(String.valueOf(resultMap.get("DOC_CHANNEL")));// 进件通路
        order.setDeliverAddrTyp(resultMap.get("MAIL_OPT") == null ? null : (String) resultMap.get("MAIL_OPT"));// 送货地址类型
        order.setDeliverAddr(resultMap.get("MAIL_ADDR") == null ? null : (String) resultMap.get("MAIL_ADDR"));// 送货地址
        /**送货地址省市区**/
        order.setDeliverProvince(
                resultMap.get("MAIL_PROVINCE") == null ? null : (String) resultMap.get("MAIL_PROVINCE"));// 送货地址省
        order.setDeliverCity(resultMap.get("MAIL_CITY") == null ? null : (String) resultMap.get("MAIL_CITY"));// 送货地址市
        order.setDeliverArea(resultMap.get("MAIL_AREA") == null ? null : (String) resultMap.get("MAIL_AREA"));// 送货地址区
        // 放款卡号与还款卡号设置
        for (Map<String, Object> card : cardList) {
            // 获取卡类型
            String type = String.valueOf(card.get("APPL_AC_KIND"));
            // 1、放款账号
            if ("01".equals(type) || "1".equals(type)) {
                order.setApplAcTyp((String) card.get("APPL_AC_TYP"));// 放款账号类型
                order.setApplAcNam((String) card.get("APPL_AC_NAM"));// 放款账号户名
                order.setApplCardNo((String) card.get("APPL_AC_NO"));// 放款卡号
                order.setAccBankCde((String) card.get("APPL_AC_BANK"));// 放款开户银行代码
                order.setAccBankName((String) card.get("APPL_AC_BANK_DESC"));// 放款开户银行名
                order.setAccAcBchCde((String) card.get("APPL_AC_BCH"));// 放款开户银行分支行代码
                order.setAccAcBchName((String) card.get("APPL_AC_BCH_DESC"));// 放款开户银行分支行名
                order.setAccAcProvince((String) card.get("AC_PROVINCE"));// 放款开户行所在省
                order.setAccAcCity((String) card.get("AC_CITY"));// 放款开户行所在市
                // 2、还款账号
            } else if ("02".equals(type) || "2".equals(type)) {
                order.setRepayApplAcNam((String) card.get("APPL_AC_NAM"));// 还款账号户名
                order.setRepayApplCardNo((String) card.get("APPL_AC_NO"));// 还款卡号
                order.setRepayAccBankCde((String) card.get("APPL_AC_BANK"));// 还款开户银行代码
                order.setRepayAccBankName((String) card.get("APPL_AC_BANK_DESC"));// 还款开户银行名
                order.setRepayAccBchCde((String) card.get("APPL_AC_BCH"));// 还款开户银行分支行代码
                order.setRepayAccBchName((String) card.get("APPL_AC_BCH_DESC"));// 还款开户银行分支行名
                order.setRepayAcProvince((String) card.get("AC_PROVINCE"));// 还款账户所在省
                order.setRepayAcCity((String) card.get("AC_CITY"));// 还款账户所在市
            }
        }
        order.setCrtTyp("");// 销售代表类型
        order.setCrtUsr((String) resultMap.get("CRT_USR"));// 销售代表代码
        order.setSalerName((String) resultMap.get("SALER_NAME"));// 销售代表姓名
        order.setSalerMobile((String) resultMap.get("SALER_MOBILE"));// 销售代表电话
        order.setAppInAdvice((String) resultMap.get("APP_IN_ADVICE"));// 录单备注
        order.setOperatorName((String) resultMap.get("OPERATOR_NAME"));// 客户经理名称
        order.setOperatorCde((String) resultMap.get("OPERATOR_CDE"));// 客户经理代码
        order.setOperatorTel((String) resultMap.get("OPERATOR_TEL"));// 客户经理联系电话
        order.setIsConfirmAgreement("1");// 是否已确认协议
        order.setIsConfirmContract("1");// 是否已确认合同
        // order.setSource(source);// 订单来源
        order.setStatus("4");// 订单状态
        order.setApplCde(String.valueOf(resultMap.get("APPL_CDE")));// 申请流水号
        order.setIsCustInfoCompleted("N");// 个人信息是否已完整
        order.setIndivMobile((String) resultMap.get("INDIV_MOBILE"));//取自接口的手机号

        /** 调用实名认证接口5.13，查询手机号和客户号 **/
        //  String hkss_url = getGateUrl() + "/app/appserver/customer/getPaySs";
        //  logger.info(hkss_url);
        HashMap<String, Object> hm = new HashMap<>();
        hm.put("typCde", order.getTypCde());
        hm.put("apprvAmt", order.getApplyAmt());
        hm.put("applyTnrTyp", order.getApplyTnrTyp());
        hm.put("applyTnr", order.getApplyTnr());
        hm.put("fstPay", order.getFstPay());
        hm.put("mtdCde", order.getMtdCde());
        // Map<String, Object> hkss_json = HttpUtil.restPostMap(hkss_url, super.getToken(), hm);
        Map<String, Object> hkss_json = cmisApplService.getHkssReturnMap(hm, super.getGateUrl(), super.getToken());
        logger.info("还款试算service返回hkss:" + hkss_json);
        // Map<String, Object> hkssResponseMap = (HashMap<String, Object>) hkss_json.get("response");
        Map<String, Object> hkssBodyMap = (HashMap<String, Object>) hkss_json.get("body");

        order.setTotalnormint(String.valueOf(hkssBodyMap.get("totalNormInt")));// 总利息金额
        order.setTotalfeeamt(String.valueOf(hkssBodyMap.get("totalFeeAmt")));// 费用总额

        /** 根据接口6.21，查看贷款品种详情 **/
        // String dkxq_url = getGateUrl() + "/app/appserver/cmis/pLoanTyp?typCde=" + order.getTypCde();
        String dkxq_url = EurekaServer.CMISPROXY + "/api/pLoanTyp/find?typCde=" + order.getTypCde();
        String dkxq_json = HttpUtil.restGet(dkxq_url, super.getToken());
        Map<String, Object> dkxqMap = HttpUtil.json2Map(dkxq_json);
        Map<String, Object> dkxqBodyMap = dkxqMap;
        // Map<String, Object> dkxqBodyMap = HttpUtil.json2Map(dkxqMap.get("body").toString());
        order.setPayMtd(String.valueOf(dkxqBodyMap.get("payMtd")));// 还款方式种类代码
        order.setPayMtdDesc(String.valueOf(dkxqBodyMap.get("payMtdDesc")));// 还款方式种类名称
        order.setTypDesc(String.valueOf(dkxqBodyMap.get("typDesc")));// 贷款品种名称
        order.setTypLevelTwo(String.valueOf(dkxqBodyMap.get("levelTwo")));// 贷款品种类别
        /**
         * pLoanTypFstPct;//贷款品种详情的最低首付比例(fstPct)
         pLoanTypMinAmt; //单笔最小贷款金额(minAmt)
         pLoanTypMaxAmt;       //单笔最大贷款金额(maxAmt)
         pLoanTypGoodMaxNum;//同笔贷款同型号商品数量上限(goodMaxNum)
         pLoanTypTnrOpt;	//借款期限(tnrOpt)
         */
        //旧版本的实体类中没有下列属性，故需要通过版本号加以控制
        logger.info("贷款品种详情a:");
        if (Integer.parseInt(version) >= 2) {
            order.setpLoanTypFstPct(StringUtils.isEmpty(dkxqBodyMap.get("fstPct")) ?
                    null :
                    new BigDecimal(String.valueOf(dkxqBodyMap.get("fstPct"))).doubleValue());
            order.setpLoanTypMinAmt(StringUtils.isEmpty(dkxqBodyMap.get("minAmt")) ?
                    null :
                    new BigDecimal(String.valueOf(dkxqBodyMap.get("minAmt"))).doubleValue());
            order.setpLoanTypMaxAmt(StringUtils.isEmpty(dkxqBodyMap.get("maxAmt")) ?
                    null :
                    new BigDecimal(String.valueOf(dkxqBodyMap.get("maxAmt"))).doubleValue());
            order.setpLoanTypGoodMaxNum(
                    StringUtils.isEmpty(dkxqBodyMap.get("goodMaxNum")) ? 0 : (Integer) dkxqBodyMap.get("goodMaxNum"));
            order.setpLoanTypTnrOpt((String) dkxqBodyMap.get("tnrOpt"));
        }

        /** 调6.23接口，查询还款方式名称 **/
        // String hkfs_url = getGateUrl() + "/app/appserver/cmis/pLoanTypMtd?typCde=" + order.getTypCde();
        String hkfs_url = EurekaServer.CMISPROXY + "/api/pLoanTyp/typMtd?typCde=" + order.getTypCde();
        String hkfs_json = HttpUtil.restGet(hkfs_url, super.getToken());
        //        Map<String, Object> hkfsMap = HttpUtil.json2Map(hkfs_json);
        //        logger.info("hkfsMap==" + hkfsMap);
        //        List<Map<String, Object>> hkfslist = (ArrayList<Map<String, Object>>) hkfsMap.get("body");
        List<Map<String, Object>> hkfslist = HttpUtil.json2List(hkfs_json);
        for (Map<String, Object> hkfsmap : hkfslist) {
            String hkfsname = String.valueOf(hkfsmap.get("mtdCde"));
            if (hkfsname.equals(order.getMtdCde())) {
                order.setMtdName((String) hkfsmap.get("mtdDesc"));// 还款方式名称
                break;
            }
        }
        /** 批准金额 调信贷详情接口6.61 ***/
        // String xdxq_url = getGateUrl() + "/app/appserver/apporder/queryAppLoanAndGoods?applSeq=" + order.getApplseq();

        //  logger.info(xdxq_url);
        // String xdxq_json = HttpUtil.restGet(xdxq_url, super.getToken());
        // logger.info("xdxq_json==" + xdxq_json);
        //  Map<String, Object> xdxqMap = HttpUtil.json2Map(xdxq_json);
 /*       Map<String, Object> xdxqMap = dhkService.queryAppLoanAndGoods(order.getApplseq());
        if (!"00000".equals(((ResultHead) xdxqMap.get("head")).getRetFlag())) {
            logger.info("贷款详情service返回:" + xdxqMap);
            return null;
        }
        //  JSONObject xdxqBodyJson = (JSONObject) xdxqMap.get("body");
        HashMap<String, Object> xdxqBodyJson = (HashMap<String, Object>) xdxqMap.get("body");
        order.setApprvAmt(String.valueOf(xdxqBodyJson.get("apprvAmt")));// 批准金额
        String channelNo = String.valueOf(resultMap.get("CHANNEL_NO"));
        if ("17".equals(channelNo)) {
            order.setWhiteType("A");// 白名单类型
        } else if ("18".equals(channelNo)) {
            order.setWhiteType("B");// 白名单类型
        } else if ("19".equals(channelNo)) {
            order.setWhiteType("SHH");// 白名单类型
        }
        String sysFlag = String.valueOf(resultMap.get("CRE_APP"));
        if ("13".equals(sysFlag)) {
            order.setSource("1");// 商户
        } else if ("14".equals(sysFlag)) {
            order.setSource("2");// 个人
        } else if ("11".equals(sysFlag)) {
            order.setSource("3");// 来自支付平台
        }*/
        order.setOperGoodsTyp("");//
        order.setApprSts("");// 审批状态
        order.setDeliverSts("");// 发货状态
        order.setSetlSts("");// 还款状态
        order.setMonthRepay("");//
        order.setPromCde("");//
        order.setPromDesc("");//
        String liveProvince = resultMap.get("LIVE_PROVINCE") == null ? "" : resultMap.get("LIVE_PROVINCE").toString();
        String lProvince = "";
        if (resultMap.get("LIVE_PROVINCE") != null) {
            String url1 = EurekaServer.CRM + "/pub/crm/findByAreaCode?areaCode=" + liveProvince;
            String json1 = HttpUtil.restGet(url1, super.getToken());
            Map<String, Object> resultMap1 = HttpUtil.json2Map(json1);
            Map<String, Object> body = HttpUtil.json2Map(resultMap1.get("body").toString());
            lProvince = body.get("areaName") == null ? "" : body.get("areaName").toString();
        }

        String liveCity = resultMap.get("LIVE_CITY") == null ? "" : resultMap.get("LIVE_CITY").toString();
        String lCity = "";
        if (resultMap.get("LIVE_CITY") != null) {
            String url1 = EurekaServer.CRM + "/pub/crm/findByAreaCode?areaCode=" + liveCity;
            String json1 = HttpUtil.restGet(url1, super.getToken());
            Map<String, Object> resultMap1 = HttpUtil.json2Map(json1);
            Map<String, Object> body = HttpUtil.json2Map(resultMap1.get("body").toString());
            lCity = body.get("areaName") == null ? "" : body.get("areaName").toString();
        }
        String liveArea = resultMap.get("LIVE_AREA") == null ? "" : resultMap.get("LIVE_AREA").toString();
        String lArea = "";
        if (resultMap.get("LIVE_AREA") != null) {
            String url1 = EurekaServer.CRM + "/pub/crm/findByAreaCode?areaCode=" + liveArea;
            String json1 = HttpUtil.restGet(url1, super.getToken());
            Map<String, Object> resultMap1 = HttpUtil.json2Map(json1);
            Map<String, Object> body = HttpUtil.json2Map(resultMap1.get("body").toString());
            lArea = body.get("areaName") == null ? "" : body.get("areaName").toString();
        }

        String liveAddr = resultMap.get("LIVE_ADDR") == null ? "" : resultMap.get("LIVE_ADDR").toString();
        logger.info("居住地址:" + liveAddr);
        String custAddress = lProvince + lCity + lArea + liveAddr;
        order.setLiveInfo(custAddress);// 客户居住地址
        order.setEmail("");// 邮箱
        order.setBackReason(String.valueOf(resultMap.get("APP_OUT_ADVICE")));// 退回原因
        return order;
    }

    @Override
    public AppOrder getAppOrderFromACQ(String applSeq, String version) {
        // 封装订单及商品信息
        AppOrder order = new AppOrder();
        Map<String, Object> headMap = AcqUtil
                .getAcqHead("ACQ-1145", "CA", "CA", "", "");
        Map<String, Object> acquirer = new HashedMap();
        acquirer.put("channelNo", "CA");
        acquirer.put("applSeq", applSeq);
        String url = EurekaServer.ACQUIRER + "/api/appl/selectApplInfoApp";
        Map<String, Object> reMap = AcqUtil
                .getAcqResponse(url, headMap, acquirer);
        Map<String, Object> responseMap = (Map) reMap.get("response");
        if (!HttpUtil.isSuccess(responseMap)) {
            logger.info("从收单系统查询订单信息失败，流水号为" + applSeq);
            return order;
        }
        Map<String, Object> resultMap = (Map) responseMap.get("body");
        // 商品列表
        // List<Map<String, Object>> goodList = (List<Map<String, Object>>) resultMap.get("goodsList");

        // apptList信息
        Map<String, Object> apptMapList = (Map) resultMap.get("apptList");
        List<Map<String, Object>> apptList = (ArrayList<Map<String, Object>>) apptMapList.get("appt");

        // 主键重新生成
        order.setOrderNo(resultMap.get("formId").toString());// 主键订单号
        order.setApplseq(String.valueOf(applSeq));// 申请流水号
        order.setApplSeq(String.valueOf(applSeq));
        order.setIdTyp((String) resultMap.get("id_typ"));// 客户证件类型
        order.setIdNo((String) resultMap.get("id_no"));// 客户证件号码
        order.setCustName((String) resultMap.get("cust_name"));// 客户姓名
        order.setMerchNo((String) resultMap.get("grt_coopr_cde"));// 商户编号
        order.setCooprCde((String) resultMap.get("coopr_cde"));// 门店代码
        order.setCooprName((String) resultMap.get("coopr_name"));// 门店名称
        order.setContZone((String) resultMap.get("cont_zone"));// 门店联系电话区号
        order.setContTel((String) resultMap.get("cont_tel"));// 门店联系电话
        order.setContSub((String) resultMap.get("cont_sub"));// 门店联系电话分机
        order.setTypGrp((String) resultMap.get("typ_grp"));// 贷款类型
        order.setPurpose((String) resultMap.get("purpose"));// 贷款用途
        order.setTypCde((String) resultMap.get("typ_cde"));// 贷款品种代码
        // order.setTypVer(String.valueOf(resultMap.get("TYP_VER")));// 贷款品种版本号
        order.setTypSeq(String.valueOf(resultMap.get("typ_seq")));// 贷款品种流水号
        order.setApplyDt(String.valueOf(resultMap.get("apply_dt")));// 申请日期
        order.setProPurAmt(String.valueOf(resultMap.get("pro_pur_amt")));// 商品总额
        order.setFstPct(String.valueOf(resultMap.get("fst_pct")));//
        // 首付金额
        if (!StringUtils.isEmpty(resultMap.get("fst_pay"))) {
            order.setFstPay(String.valueOf(resultMap.get("fst_pay")));
        } else {
            order.setFstPay("0");
        }
        // 重新计算首付比例，没有首付的异常数据，退回后可以重新计算首付
        //        this.calcFstPct(order);
        order.setApplyAmt(String.valueOf(resultMap.get("apply_amt")));// 借款总额
        order.setApplyTnr(String.valueOf(resultMap.get("apply_tnr")));// 借款期限
        order.setApplyTnrTyp((String) resultMap.get("apply_tnr_typ"));// 借款期限类型
        order.setOtherPurpose((String) resultMap.get("other_purpose"));//
        order.setMtdCde((String) resultMap.get("mtd_cde"));// 还款方式代码
        order.setLoanFreq(String.valueOf(resultMap.get("loan_freq")));// 还款间隔
        order.setDueDayOpt(String.valueOf(resultMap.get("due_day_opt")));//
        order.setDueDay(String.valueOf(resultMap.get("due_day")));//
        order.setDocChannel(String.valueOf(resultMap.get("doc_channel")));// 进件通路

        for (Map<String, Object> appt : apptList) {
            //获取申请人类型 01、主申请人02、共同申请人 03、保证人 04、委托人
            String appt_typ = String.valueOf(appt.get("appt_typ"));
            if ("01".equals(appt_typ)) {
                order.setDeliverAddrTyp(appt.get("mail_opt") == null ? null : (String) appt.get("mail_opt"));// 送货地址类型
                order.setDeliverAddr(appt.get("mail_addr") == null ? null : (String) appt.get("mail_addr"));// 送货地址
                /**送货地址省市区**/
                order.setDeliverProvince(
                        appt.get("mail_province") == null ? null : (String) appt.get("mail_province"));// 送货地址省
                order.setDeliverCity(appt.get("mail_city") == null ? null : (String) appt.get("mail_city"));// 送货地址市
                order.setDeliverArea(appt.get("mail_area") == null ? null : (String) appt.get("mail_area"));// 送货地址区

                String liveProvince = appt.get("live_province") == null ? "" : appt.get("live_province").toString();
                String lProvince = "";
                if (appt.get("live_province") != null) {
                    String url1 = EurekaServer.CRM + "/pub/crm/findByAreaCode?areaCode=" + liveProvince;
                    String json1 = HttpUtil.restGet(url1, super.getToken());
                    Map<String, Object> resultMap1 = HttpUtil.json2Map(json1);
                    Map<String, Object> body = HttpUtil.json2Map(resultMap1.get("body").toString());
                    lProvince = body.get("areaName") == null ? "" : body.get("areaName").toString();
                }

                String liveCity = appt.get("live_city") == null ? "" : appt.get("live_city").toString();
                String lCity = "";
                if (appt.get("live_city") != null) {
                    String url1 = EurekaServer.CRM + "/pub/crm/findByAreaCode?areaCode=" + liveCity;
                    String json1 = HttpUtil.restGet(url1, super.getToken());
                    Map<String, Object> resultMap1 = HttpUtil.json2Map(json1);
                    Map<String, Object> body = HttpUtil.json2Map(resultMap1.get("body").toString());
                    lCity = body.get("areaName") == null ? "" : body.get("areaName").toString();
                }
                String liveArea = appt.get("live_area") == null ? "" : appt.get("live_area").toString();
                String lArea = "";
                if (appt.get("live_area") != null) {
                    String url1 = EurekaServer.CRM + "/pub/crm/findByAreaCode?areaCode=" + liveArea;
                    String json1 = HttpUtil.restGet(url1, super.getToken());
                    Map<String, Object> resultMap1 = HttpUtil.json2Map(json1);
                    Map<String, Object> body = HttpUtil.json2Map(resultMap1.get("body").toString());
                    lArea = body.get("areaName") == null ? "" : body.get("areaName").toString();
                }
                String liveAddr = appt.get("live_addr") == null ? "" : appt.get("live_addr").toString();
                String custAddress = lProvince + lCity + lArea + liveAddr;
                order.setLiveInfo(custAddress);// 客户居住地址
            }
        }

        // 放款卡号与还款卡号设置
        order.setApplAcTyp((String) resultMap.get("appl_ac_typ"));// 放款账号类型
        order.setApplAcNam((String) resultMap.get("appl_ac_nam"));// 放款账号户名
        order.setApplCardNo((String) resultMap.get("appl_card_no"));// 放款卡号
        order.setAccBankCde((String) resultMap.get("acc_bank_cde"));// 放款开户银行代码
        order.setAccBankName((String) resultMap.get("acc_bank_name"));// 放款开户银行名
        order.setAccAcBchCde((String) resultMap.get("appl_ac_bch"));// 放款开户银行分支行代码
        order.setAccAcBchName((String) resultMap.get("acc_ac_bch_name"));// 放款开户银行分支行名
        order.setAccAcProvince((String) resultMap.get("ac_province"));// 放款开户行所在省
        order.setAccAcCity((String) resultMap.get("ac_city"));// 放款开户行所在市

        order.setRepayApplAcNam((String) resultMap.get("repay_appl_ac_nam"));// 还款账号户名
        order.setRepayApplCardNo((String) resultMap.get("repay_appl_card_no"));// 还款卡号
        order.setRepayAccBankCde((String) resultMap.get("repay_acc_bank_cde"));// 还款开户银行代码
        order.setRepayAccBankName((String) resultMap.get("repay_acc_bank_name"));// 还款开户银行名

        order.setRepayAcProvince((String) resultMap.get("repay_ac_province"));// 还款账户所在省
        order.setRepayAcCity((String) resultMap.get("repay_ac_city"));// 还款账户所在市
        //从CRM获取还款开户银行分支行代码和名称
        String url1 = EurekaServer.CRM + "/app/crm/cust/getBankCardByCardNo" + order.getRepayApplCardNo();
        String json1 = HttpUtil.restGet(url1, super.getToken());
        Map<String, Object> resultMap1 = HttpUtil.json2Map(json1);
        if (HttpUtil.isSuccess(resultMap1)) {
            Map<String, Object> body = HttpUtil.json2Map(resultMap1.get("body").toString());
            List<Map<String, Object>> Info = (ArrayList) body.get("info");
            Map<String, Object> InfoMap = Info.get(0);
            order.setRepayAccBchCde((String) InfoMap.get("accBchCde"));// 还款开户银行分支行代码
            order.setRepayAccBchName((String) InfoMap.get("accBchName"));// 还款开户银行分支行名
        } else {
            order.setRepayAccBchCde("");// 还款开户银行分支行代码
            order.setRepayAccBchName("");// 还款开户银行分支行名
        }
        order.setCrtTyp("");// 销售代表类型
        order.setCrtUsr((String) resultMap.get("saler_cde"));// 销售代表代码
        order.setSalerName((String) resultMap.get("saler_name"));// 销售代表姓名
        order.setSalerMobile((String) resultMap.get("saler_mobile"));// 销售代表电话
        order.setAppInAdvice((String) resultMap.get("app_in_advice"));// 录单备注
        order.setOperatorName((String) resultMap.get("operator_name"));// 客户经理名称
        order.setOperatorCde((String) resultMap.get("operator_cde"));// 客户经理代码
        order.setOperatorTel((String) resultMap.get("operator_tel"));// 客户经理联系电话
        order.setIsConfirmAgreement("1");// 是否已确认协议
        order.setIsConfirmContract("1");// 是否已确认合同
        // order.setSource(source);// 订单来源
        order.setStatus("4");// 订单状态
        order.setApplCde(String.valueOf(resultMap.get("appl_cde")));// 申请流水号
        order.setIsCustInfoCompleted("N");// 个人信息是否已完整
        order.setIndivMobile((String) resultMap.get("indiv_mobile"));//取自接口的手机号

        HashMap<String, Object> hm = new HashMap<>();
        hm.put("typCde", order.getTypCde());
        hm.put("apprvAmt", order.getApplyAmt());
        hm.put("applyTnrTyp", order.getApplyTnrTyp());
        hm.put("applyTnr", order.getApplyTnr());
        hm.put("fstPay", order.getFstPay());
        hm.put("mtdCde", order.getMtdCde());
        // Map<String, Object> hkss_json = HttpUtil.restPostMap(hkss_url, super.getToken(), hm);
        Map<String, Object> hkss_json = cmisApplService.getHkssReturnMap(hm, super.getGateUrl(), super.getToken());
        logger.info("还款试算service返回hkss:" + hkss_json);
        // Map<String, Object> hkssResponseMap = (HashMap<String, Object>) hkss_json.get("response");
        Map<String, Object> hkssBodyMap = (HashMap<String, Object>) hkss_json.get("body");

        order.setTotalnormint(String.valueOf(hkssBodyMap.get("totalNormInt")));// 总利息金额
        order.setTotalfeeamt(String.valueOf(hkssBodyMap.get("totalFeeAmt")));// 费用总额

        /** 根据接口6.21，查看贷款品种详情 **/
        // String dkxq_url = getGateUrl() + "/app/appserver/cmis/pLoanTyp?typCde=" + order.getTypCde();
        String dkxq_url = EurekaServer.CMISPROXY + "/api/pLoanTyp/find?typCde=" + order.getTypCde();
        String dkxq_json = HttpUtil.restGet(dkxq_url, super.getToken());
        Map<String, Object> dkxqMap = HttpUtil.json2Map(dkxq_json);
        Map<String, Object> dkxqBodyMap = dkxqMap;
        // Map<String, Object> dkxqBodyMap = HttpUtil.json2Map(dkxqMap.get("body").toString());
        order.setPayMtd(String.valueOf(dkxqBodyMap.get("payMtd")));// 还款方式种类代码
        order.setPayMtdDesc(String.valueOf(dkxqBodyMap.get("payMtdDesc")));// 还款方式种类名称
        order.setTypDesc(String.valueOf(dkxqBodyMap.get("typDesc")));// 贷款品种名称
        order.setTypLevelTwo(String.valueOf(dkxqBodyMap.get("levelTwo")));// 贷款品种类别
        /**
         * pLoanTypFstPct;//贷款品种详情的最低首付比例(fstPct)
         pLoanTypMinAmt; //单笔最小贷款金额(minAmt)
         pLoanTypMaxAmt;       //单笔最大贷款金额(maxAmt)
         pLoanTypGoodMaxNum;//同笔贷款同型号商品数量上限(goodMaxNum)
         pLoanTypTnrOpt;	//借款期限(tnrOpt)
         */
        //旧版本的实体类中没有下列属性，故需要通过版本号加以控制
        logger.info("贷款品种详情a:");
        if (Integer.parseInt(version) >= 2) {
            order.setpLoanTypFstPct(StringUtils.isEmpty(dkxqBodyMap.get("fstPct")) ?
                    null :
                    new BigDecimal(String.valueOf(dkxqBodyMap.get("fstPct"))).doubleValue());
            order.setpLoanTypMinAmt(StringUtils.isEmpty(dkxqBodyMap.get("minAmt")) ?
                    null :
                    new BigDecimal(String.valueOf(dkxqBodyMap.get("minAmt"))).doubleValue());
            order.setpLoanTypMaxAmt(StringUtils.isEmpty(dkxqBodyMap.get("maxAmt")) ?
                    null :
                    new BigDecimal(String.valueOf(dkxqBodyMap.get("maxAmt"))).doubleValue());
            order.setpLoanTypGoodMaxNum(
                    StringUtils.isEmpty(dkxqBodyMap.get("goodMaxNum")) ? 0 : (Integer) dkxqBodyMap.get("goodMaxNum"));
            order.setpLoanTypTnrOpt((String) dkxqBodyMap.get("tnrOpt"));
        }

        /** 调6.23接口，查询还款方式名称 **/
        String hkfs_url = EurekaServer.CMISPROXY + "/api/pLoanTyp/typMtd?typCde=" + order.getTypCde();
        String hkfs_json = HttpUtil.restGet(hkfs_url, super.getToken());
        List<Map<String, Object>> hkfslist = HttpUtil.json2List(hkfs_json);
        for (Map<String, Object> hkfsmap : hkfslist) {
            String hkfsname = String.valueOf(hkfsmap.get("mtdCde"));
            if (hkfsname.equals(order.getMtdCde())) {
                order.setMtdName((String) hkfsmap.get("mtdDesc"));// 还款方式名称
                break;
            }
        }
        order.setOperGoodsTyp("");//
        order.setApprSts("");// 审批状态
        order.setDeliverSts("");// 发货状态
        order.setSetlSts("");// 还款状态
        order.setMonthRepay("");//
        order.setPromCde("");//
        order.setPromDesc("");//
        order.setEmail("");// 邮箱
        order.setBackReason("");// 退回原因
        return order;
    }

    @Override
    public AppOrder getAppOrderAllFromCmis(String applSeq, String version) {
        /** 从核心数据库查询贷款详情 **/
        String url = EurekaServer.CMISPROXY + "/api/appl/queryApplFull?applSeq=" + applSeq;
        String json = HttpUtil.restGet(url, super.getToken());
        logger.info("通过cmis请求的订单信息的url" + url);
        Map<String, Object> resultMap = HttpUtil.json2Map(json);
        logger.info("通过cmis请求的订单信息为：" + resultMap);
        // 商品列表
        // List<Map<String, Object>> goodList = (ArrayList<Map<String, Object>>) resultMap.get("goods");
        // 银行卡信息
        List<Map<String, Object>> cardList = (ArrayList<Map<String, Object>>) resultMap.get("accInfo");
        // 共同还款人
        // List<Map<String, Object>> commomPersonList = (ArrayList<Map<String, Object>>) resultMap.get("apptInfo");

        // 封装订单及商品信息
        AppOrder order = new AppOrder();
        // 主键重新生成
        order.setOrderNo(UUID.randomUUID().toString());// 主键订单号
        order.setApplSeq(String.valueOf(resultMap.get("APPL_SEQ")));// 申请流水号
        order.setIdTyp((String) resultMap.get("ID_TYP"));// 客户证件类型
        order.setIdNo((String) resultMap.get("ID_NO"));// 客户证件号码
        order.setCustName((String) resultMap.get("CUST_NAME"));// 客户姓名
        order.setMerchNo((String) resultMap.get("SUPER_COOPR"));// 商户编号
        order.setCooprCde((String) resultMap.get("COOPR_CDE"));// 门店代码
        order.setCooprName((String) resultMap.get("COOPR_NAME"));// 门店名称
        order.setContZone((String) resultMap.get("COOPR_ZONE"));// 门店联系电话区号
        order.setContTel((String) resultMap.get("COOPR_TEL"));// 门店联系电话
        order.setContSub((String) resultMap.get("COOPR_SUB"));// 门店联系电话分机
        order.setTypGrp((String) resultMap.get("TYP_GRP"));// 贷款类型
        order.setPurpose((String) resultMap.get("PURPOSE"));// 贷款用途
        order.setTypCde((String) resultMap.get("LOAN_TYP"));// 贷款品种代码
        order.setTypVer(String.valueOf(resultMap.get("TYP_VER")));// 贷款品种版本号
        order.setTypSeq(String.valueOf(resultMap.get("TYP_SEQ")));// 贷款品种流水号
        order.setApplyDt(String.valueOf(resultMap.get("APPLY_DT")));// 申请日期
        order.setProPurAmt(String.valueOf(resultMap.get("PRO_PUR_AMT")));// 商品总额
        order.setFstPct(String.valueOf(resultMap.get("FST_PCT")));//
        // 首付金额
        if (!StringUtils.isEmpty(resultMap.get("FST_PAY"))) {
            order.setFstPay(String.valueOf(resultMap.get("FST_PAY")));
        } else {
            order.setFstPay("0");
        }
        // 重新计算首付比例，没有首付的异常数据，退回后可以重新计算首付
        //        this.calcFstPct(order);
        order.setApplyAmt(String.valueOf(resultMap.get("APPLY_AMT")));// 借款总额
        order.setApplyTnr(String.valueOf(resultMap.get("APPLY_TNR")));// 借款期限
        order.setApplyTnrTyp((String) resultMap.get("APPLY_TNR_TYP"));// 借款期限类型
        order.setOtherPurpose((String) resultMap.get("OTHER_PURPOSE"));//
        order.setMtdCde((String) resultMap.get("MTD_CDE"));// 还款方式代码
        order.setLoanFreq(String.valueOf(resultMap.get("LOAN_FREQ")));// 还款间隔
        order.setDueDayOpt(String.valueOf(resultMap.get("DUE_DAY_OPT")));//
        order.setDueDay(String.valueOf(resultMap.get("DUE_DAY")));//
        order.setDocChannel(String.valueOf(resultMap.get("DOC_CHANNEL")));// 进件通路
        order.setDeliverAddrTyp(resultMap.get("MAIL_OPT") == null ? null : (String) resultMap.get("MAIL_OPT"));// 送货地址类型
        order.setDeliverAddr(resultMap.get("MAIL_ADDR") == null ? null : (String) resultMap.get("MAIL_ADDR"));// 送货地址
        /**送货地址省市区**/
        order.setDeliverProvince(
                resultMap.get("MAIL_PROVINCE") == null ? null : (String) resultMap.get("MAIL_PROVINCE"));// 送货地址省
        order.setDeliverCity(resultMap.get("MAIL_CITY") == null ? null : (String) resultMap.get("MAIL_CITY"));// 送货地址市
        order.setDeliverArea(resultMap.get("MAIL_AREA") == null ? null : (String) resultMap.get("MAIL_AREA"));// 送货地址区
        // 放款卡号与还款卡号设置
        for (Map<String, Object> card : cardList) {
            // 获取卡类型
            String type = String.valueOf(card.get("APPL_AC_KIND"));
            // 1、放款账号
            if ("01".equals(type) || "1".equals(type)) {
                order.setApplAcTyp((String) card.get("APPL_AC_TYP"));// 放款账号类型
                order.setApplAcNam((String) card.get("APPL_AC_NAM"));// 放款账号户名
                order.setApplCardNo((String) card.get("APPL_AC_NO"));// 放款卡号
                order.setAccBankCde((String) card.get("APPL_AC_BANK"));// 放款开户银行代码
                order.setAccBankName((String) card.get("APPL_AC_BANK_DESC"));// 放款开户银行名
                order.setAccAcBchCde((String) card.get("APPL_AC_BCH"));// 放款开户银行分支行代码
                order.setAccAcBchName((String) card.get("APPL_AC_BCH_DESC"));// 放款开户银行分支行名
                order.setAccAcProvince((String) card.get("AC_PROVINCE"));// 放款开户行所在省
                order.setAccAcCity((String) card.get("AC_CITY"));// 放款开户行所在市
                // 2、还款账号
            } else if ("02".equals(type) || "2".equals(type)) {
                order.setRepayApplAcNam((String) card.get("APPL_AC_NAM"));// 还款账号户名
                order.setRepayApplCardNo((String) card.get("APPL_AC_NO"));// 还款卡号
                order.setRepayAccBankCde((String) card.get("APPL_AC_BANK"));// 还款开户银行代码
                order.setRepayAccBankName((String) card.get("APPL_AC_BANK_DESC"));// 还款开户银行名
                order.setRepayAccBchCde((String) card.get("APPL_AC_BCH"));// 还款开户银行分支行代码
                order.setRepayAccBchName((String) card.get("APPL_AC_BCH_DESC"));// 还款开户银行分支行名
                order.setRepayAcProvince((String) card.get("AC_PROVINCE"));// 还款账户所在省
                order.setRepayAcCity((String) card.get("AC_CITY"));// 还款账户所在市
            }
        }
        order.setCrtTyp("");// 销售代表类型
        order.setCrtUsr((String) resultMap.get("CRT_USR"));// 销售代表代码
        order.setSalerName((String) resultMap.get("SALER_NAME"));// 销售代表姓名
        order.setSalerMobile((String) resultMap.get("SALER_MOBILE"));// 销售代表电话
        order.setAppInAdvice((String) resultMap.get("APP_IN_ADVICE"));// 录单备注
        order.setOperatorName((String) resultMap.get("OPERATOR_NAME"));// 客户经理名称
        order.setOperatorCde((String) resultMap.get("OPERATOR_CDE"));// 客户经理代码
        order.setOperatorTel((String) resultMap.get("OPERATOR_TEL"));// 客户经理联系电话
        order.setIsConfirmAgreement("1");// 是否已确认协议
        order.setIsConfirmContract("1");// 是否已确认合同
        // order.setSource(source);// 订单来源
        order.setStatus("4");// 订单状态
        order.setApplCde(String.valueOf(resultMap.get("APPL_CDE")));// 申请流水号
        order.setIsCustInfoCompleted("N");// 个人信息是否已完整
        order.setIndivMobile((String) resultMap.get("INDIV_MOBILE"));//取自接口的手机号

        /** 调用实名认证接口5.13，查询手机号和客户号 **/
        //  String hkss_url = getGateUrl() + "/app/appserver/customer/getPaySs";
        //  logger.info(hkss_url);
        HashMap<String, Object> hm = new HashMap<>();
        hm.put("typCde", order.getTypCde());
        hm.put("apprvAmt", order.getApplyAmt());
        hm.put("applyTnrTyp", order.getApplyTnrTyp());
        hm.put("applyTnr", order.getApplyTnr());
        hm.put("fstPay", order.getFstPay());
        hm.put("mtdCde", order.getMtdCde());
        // Map<String, Object> hkss_json = HttpUtil.restPostMap(hkss_url, super.getToken(), hm);
        Map<String, Object> hkss_json = cmisApplService.getHkssReturnMap(hm, super.getGateUrl(), super.getToken());
        logger.info("还款试算service返回hkss:" + hkss_json);
        // Map<String, Object> hkssResponseMap = (HashMap<String, Object>) hkss_json.get("response");
        Map<String, Object> hkssBodyMap = (HashMap<String, Object>) hkss_json.get("body");

        order.setTotalnormint(String.valueOf(hkssBodyMap.get("totalNormInt")));// 总利息金额
        order.setTotalfeeamt(String.valueOf(hkssBodyMap.get("totalFeeAmt")));// 费用总额

        /** 根据接口6.21，查看贷款品种详情 **/
        // String dkxq_url = getGateUrl() + "/app/appserver/cmis/pLoanTyp?typCde=" + order.getTypCde();
        String dkxq_url = EurekaServer.CMISPROXY + "/api/pLoanTyp/find?typCde=" + order.getTypCde();
        String dkxq_json = HttpUtil.restGet(dkxq_url, super.getToken());
        Map<String, Object> dkxqMap = HttpUtil.json2Map(dkxq_json);
        Map<String, Object> dkxqBodyMap = dkxqMap;
        // Map<String, Object> dkxqBodyMap = HttpUtil.json2Map(dkxqMap.get("body").toString());
        order.setPayMtd(String.valueOf(dkxqBodyMap.get("payMtd")));// 还款方式种类代码
        order.setPayMtdDesc(String.valueOf(dkxqBodyMap.get("payMtdDesc")));// 还款方式种类名称
        order.setTypDesc(String.valueOf(dkxqBodyMap.get("typDesc")));// 贷款品种名称
        order.setTypLevelTwo(String.valueOf(dkxqBodyMap.get("levelTwo")));// 贷款品种类别
        /**
         * pLoanTypFstPct;//贷款品种详情的最低首付比例(fstPct)
         pLoanTypMinAmt; //单笔最小贷款金额(minAmt)
         pLoanTypMaxAmt;       //单笔最大贷款金额(maxAmt)
         pLoanTypGoodMaxNum;//同笔贷款同型号商品数量上限(goodMaxNum)
         pLoanTypTnrOpt;	//借款期限(tnrOpt)
         */
        //旧版本的实体类中没有下列属性，故需要通过版本号加以控制
        logger.info("贷款品种详情a:");
        if (Integer.parseInt(version) >= 2) {
            order.setpLoanTypFstPct(StringUtils.isEmpty(dkxqBodyMap.get("fstPct")) ?
                    null :
                    new BigDecimal(String.valueOf(dkxqBodyMap.get("fstPct"))).doubleValue());
            order.setpLoanTypMinAmt(StringUtils.isEmpty(dkxqBodyMap.get("minAmt")) ?
                    null :
                    new BigDecimal(String.valueOf(dkxqBodyMap.get("minAmt"))).doubleValue());
            order.setpLoanTypMaxAmt(StringUtils.isEmpty(dkxqBodyMap.get("maxAmt")) ?
                    null :
                    new BigDecimal(String.valueOf(dkxqBodyMap.get("maxAmt"))).doubleValue());
            order.setpLoanTypGoodMaxNum(
                    StringUtils.isEmpty(dkxqBodyMap.get("goodMaxNum")) ? 0 : (Integer) dkxqBodyMap.get("goodMaxNum"));
            order.setpLoanTypTnrOpt((String) dkxqBodyMap.get("tnrOpt"));
        }

        /** 调6.23接口，查询还款方式名称 **/
        // String hkfs_url = getGateUrl() + "/app/appserver/cmis/pLoanTypMtd?typCde=" + order.getTypCde();
        String hkfs_url = EurekaServer.CMISPROXY + "/api/pLoanTyp/typMtd?typCde=" + order.getTypCde();
        String hkfs_json = HttpUtil.restGet(hkfs_url, super.getToken());
        //        Map<String, Object> hkfsMap = HttpUtil.json2Map(hkfs_json);
        //        logger.info("hkfsMap==" + hkfsMap);
        //        List<Map<String, Object>> hkfslist = (ArrayList<Map<String, Object>>) hkfsMap.get("body");
        List<Map<String, Object>> hkfslist = HttpUtil.json2List(hkfs_json);
        for (Map<String, Object> hkfsmap : hkfslist) {
            String hkfsname = String.valueOf(hkfsmap.get("mtdCde"));
            if (hkfsname.equals(order.getMtdCde())) {
                order.setMtdName((String) hkfsmap.get("mtdDesc"));// 还款方式名称
                break;
            }
        }
        /** 批准金额 调信贷详情接口6.61 ***/
        // String xdxq_url = getGateUrl() + "/app/appserver/apporder/queryAppLoanAndGoods?applSeq=" + order.getApplseq();

        //  logger.info(xdxq_url);
        // String xdxq_json = HttpUtil.restGet(xdxq_url, super.getToken());
        // logger.info("xdxq_json==" + xdxq_json);
        //  Map<String, Object> xdxqMap = HttpUtil.json2Map(xdxq_json);
 /*       Map<String, Object> xdxqMap = dhkService.queryAppLoanAndGoods(order.getApplseq());
        if (!"00000".equals(((ResultHead) xdxqMap.get("head")).getRetFlag())) {
            logger.info("贷款详情service返回:" + xdxqMap);
            return null;
        }
        //  JSONObject xdxqBodyJson = (JSONObject) xdxqMap.get("body");
        HashMap<String, Object> xdxqBodyJson = (HashMap<String, Object>) xdxqMap.get("body");
        order.setApprvAmt(String.valueOf(xdxqBodyJson.get("apprvAmt")));// 批准金额
        String channelNo = String.valueOf(resultMap.get("CHANNEL_NO"));
        if ("17".equals(channelNo)) {
            order.setWhiteType("A");// 白名单类型
        } else if ("18".equals(channelNo)) {
            order.setWhiteType("B");// 白名单类型
        } else if ("19".equals(channelNo)) {
            order.setWhiteType("SHH");// 白名单类型
        }
        String sysFlag = String.valueOf(resultMap.get("CRE_APP"));
        if ("13".equals(sysFlag)) {
            order.setSource("1");// 商户
        } else if ("14".equals(sysFlag)) {
            order.setSource("2");// 个人
        } else if ("11".equals(sysFlag)) {
            order.setSource("3");// 来自支付平台
        }*/
        order.setOperGoodsTyp("");//
        order.setApprSts("");// 审批状态
        order.setDeliverSts("");// 发货状态
        order.setSetlSts("");// 还款状态
        order.setMonthRepay("");//
        order.setPromCde("");//
        order.setPromDesc("");//

        String liveProvince = resultMap.get("LIVE_PROVINCE") == null ? "" : resultMap.get("LIVE_PROVINCE").toString();
        String lProvince = "";
        if (resultMap.get("LIVE_PROVINCE") != null) {
            String url1 = EurekaServer.CRM + "/pub/crm/findByAreaCode?areaCode=" + liveProvince;
            String json1 = HttpUtil.restGet(url1, super.getToken());
            Map<String, Object> resultMap1 = HttpUtil.json2Map(json1);
            lProvince = resultMap1.get("areaName") == null ? "" : resultMap1.get("areaName").toString();
        }

        String liveCity = resultMap.get("LIVE_CITY") == null ? "" : resultMap.get("LIVE_CITY").toString();
        String lCity = "";
        if (resultMap.get("LIVE_CITY") != null) {
            String url1 = EurekaServer.CRM + "/pub/crm/findByAreaCode?areaCode=" + liveCity;
            String json1 = HttpUtil.restGet(url1, super.getToken());
            Map<String, Object> resultMap1 = HttpUtil.json2Map(json1);
            lCity = resultMap1.get("areaName") == null ? "" : resultMap1.get("areaName").toString();
        }
        String liveArea = resultMap.get("LIVE_AREA") == null ? "" : resultMap.get("LIVE_AREA").toString();
        String lArea = "";
        if (resultMap.get("LIVE_AREA") != null) {
            String url1 = EurekaServer.CRM + "/pub/crm/findByAreaCode?areaCode=" + liveArea;
            String json1 = HttpUtil.restGet(url1, super.getToken());
            Map<String, Object> resultMap1 = HttpUtil.json2Map(json1);
            lArea = resultMap1.get("areaName") == null ? "" : resultMap1.get("areaName").toString();
        }

        String liveAddr = resultMap.get("LIVE_ADDR") == null ? "" : resultMap.get("LIVE_ADDR").toString();
        logger.info("居住地址:" + liveAddr);
        String custAddress = lProvince + lCity + lArea + liveAddr;
        order.setLiveInfo(custAddress);// 客户居住地址
        order.setEmail("");// 邮箱
        order.setBackReason(String.valueOf(resultMap.get("APP_OUT_ADVICE")));// 退回原因
        return order;
    }

    @Override
    public Map<String, Object> getContractConfirmData(String signCode) {
        // 根据签章编码查询签约流水号
        UAuthCASignRequest request = uAuthCASignRequestRepository.findBySignCode(signCode);
        if (request == null) {
            return null;
        }
        // 去cmis查询订单信息
        Map<String, Object> appOrderMapCmis = this.getAppOrderMapFromCmis(request.getApplseq());
        if (appOrderMapCmis == null || appOrderMapCmis.isEmpty()) {
            return null;
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("totalamount", appOrderMapCmis.get("APPLY_AMT"));
        resultMap.put("loannumber", appOrderMapCmis.get("APPLY_TNR"));
        List<String> goodsNameList = new ArrayList<>();
        List<Map<String, Object>> goods = (List<Map<String, Object>>) appOrderMapCmis.get("goods");
        if (goods != null && !goods.isEmpty()) {
            for (Map<String, Object> good : goods) {
                goodsNameList.add((String) good.get("GOODS_NAME"));
            }
        }
        resultMap.put("goods", goodsNameList);
        resultMap.put("name", appOrderMapCmis.get("CUST_NAME"));
        resultMap.put("phone", appOrderMapCmis.get("INDIV_MOBILE"));
        resultMap.put("idnumber", appOrderMapCmis.get("ID_NO"));
        //还款帐号
        List<Map<String, Object>> cardList = (ArrayList<Map<String, Object>>) appOrderMapCmis.get("accInfo");
        for (Map<String, Object> card : cardList) {
            // 获取卡类型
            String type = String.valueOf(card.get("APPL_AC_KIND"));
            if ("02".equals(type) || "2".equals(type)) {
                resultMap.put("cardnumber", card.get("APPL_AC_NO"));
            }
        }

        //resultMap.put("cardnumber",appOrderMapCmis.get(""));
        //调用6.17接口 查询每期应还款
        HashMap<String, Object> loanRequestMap = new HashMap<>();
        loanRequestMap.put("typCde", appOrderMapCmis.get("LOAN_TYP"));
        loanRequestMap.put("apprvAmt", appOrderMapCmis.get("APPLY_AMT"));
        loanRequestMap.put("applyTnrTyp", appOrderMapCmis.get("APPLY_TNR_TYP"));
        loanRequestMap.put("applyTnr", appOrderMapCmis.get("APPLY_TNR"));
        loanRequestMap.put("fstPay", appOrderMapCmis.get("FST_PAY"));
        logger.debug("loanRequestMap==" + loanRequestMap);
        Map<String, Object> paySsResultMap = cmisApplService.getHkssReturnMap(loanRequestMap, getGateUrl(), getToken());

        if (paySsResultMap != null) {
            Map<String, Object> body = (Map<String, Object>) paySsResultMap.get("body");
            List<Map<String, Object>> mx = (List<Map<String, Object>>) body.get("mx");
            if (mx != null) {

                Map<String, Object> perMx = mx.size() > 1 ? mx.get(1) : mx.get(0);
                String perMxString = String.valueOf(perMx.get("instmAmt"));
                resultMap.put("loanpayment", perMxString);
            }
        }
        // 添加signType
        resultMap.put("signType", request.getSignType());
        return resultMap;
    }

    @Override
    public Map checkIfMsgComplete(String tag, String businessType, Map<String, Object> params) throws Exception {

        Map<String, Object> result = new HashedMap();

        Map<String, Object> smrzInfo;
        result.put(MsgType.SMRZ.toString(), "Y");
        if (params.get("isOrder").toString().equals("Y")) {
            // 实名认证
            smrzInfo = cmisApplService
                    .getSmrzInfoByCustNameAndIdNo(params.get("custName").toString(), params.get("idNo").toString());
            if (null == smrzInfo) {
                result.put(MsgType.SMRZ.toString(), "N");
                return fail("21", "获取实名认证信息失败");
            }
        } else {
            smrzInfo = cmisApplService.getSmrzInfoByUserId(params.get("userId").toString());
            if (null == smrzInfo || !HttpUtil.isSuccess(smrzInfo)) {
                result.put(MsgType.SMRZ.toString(), "N");
            }
        }

        List<Map<String, Object>> bcyx = new ArrayList<>();
        Map<String, Object> child = new HashedMap();
        child.put("docCde", "DOC53");
        child.put("docDesc", "身份证正面");
        child.put("docRevInd", "01");
        bcyx.add(child);
        child = new HashedMap();
        child.put("docCde", "DOC54");
        child.put("docDesc", "身份证反面");
        child.put("docRevInd", "01");
        bcyx.add(child);

        List<Map<String, Object>> teBcyx = new ArrayList<>();

        // 获取提额必传影像
        String teUrl = EurekaServer.CRM + "/app/crm/cust/getCustLoanPhotos?custNo=" + params.get("custNo").toString();
        String teJson = HttpUtil.restGet(teUrl, null);
        logger.info("crm获取用户提额影像信息:" + teJson);
        if (StringUtils.isEmpty(teJson)) {
            logger.error("从CRM获取提额必传影像失败!custNo=" + params.get("custNo").toString());
            // 如果提额必传影像获取失败，默认身份证正反面.
            teBcyx = bcyx;
        } else {
            Map<String, Object> yxResult = HttpUtil.json2Map(teJson);
            if (HttpUtil.isSuccess(yxResult)) {
                List<Map<String, Object>> bodyList = (List<Map<String, Object>>) yxResult.get("body");
                for (Map<String, Object> body : bodyList) {
                    if ("01".equals(body.get("docRevInd").toString())) {
                        teBcyx.add(body);
                        logger.info("提额必传影像列表：" + teBcyx);
                    }
                }
            }
        }

        String channel = "";
        // 渠道为星巢贷则走全版校验
        if (!StringUtils.isEmpty(params.get("channel"))) {
            logger.info("channel:" + params.get("channel") + ",channelNO:" + params.get("channelNo"));
            channel = params.get("channel").toString();
            if ("34".equals(params.get("channelNo"))) {//集团大数据走简版
                channel = (String) params.get("channelNo");
            }
        }

        // 人脸识别
        if (params.get("isOrder").toString().equals("Y")
                || !businessType.equals(BusinessType.GRXX.toString())) {
            // 调个人版人脸接口
            String source = "2";
            if ("16".equals(channel)) {
                source = "16";
            }
            Map<String, Object> faceResult = personFaceService
                    .ifNeedFaceCheckByTypCde(params.get("orderNo") == null ? null : params.get("orderNo").toString(),
                            source,
                            params.get("custNo").toString(),
                            params.get("custName").toString(), params.get("idNo").toString());
            logger.debug("个人版人脸接口ifNeedFaceCheckByTypCde返回:" + faceResult);
            if (faceResult == null) {
                logger.error("是否需要人脸识别接口返回null, orderNo" + params.get("orderNo").toString());
            }
            result.put(MsgType.RLSB.toString(), faceResult.get("body"));
        } else {
            Map<String, Object> custInfo;
            custInfo = cmisApplService.getSmrzInfoByUserId(params.get("userId").toString());
            Map<String, Object> rlsb = new HashedMap();
            logger.info("rlsb：" + custInfo);
            Map<String, Object> custInfoBodyMap = null;
            if (HttpUtil.isSuccess(custInfo)) {
                custInfoBodyMap = HttpUtil.json2Map(custInfo.get("body").toString());
            }
            logger.info("custInfoBodyMap:" + custInfoBodyMap);
            if (custInfoBodyMap == null) {
                logger.error("custInfoBodyMap为空");
            }

            // 默认失败
            rlsb.put("isPass", "N");
            rlsb.put("flag", "N");
            rlsb.put("code", "01");
            rlsb.put("remainCount", 0);
            if (!StringUtils.isEmpty(custInfoBodyMap)
                    && !StringUtils.isEmpty(custInfoBodyMap.get("faceValue"))) {
                if ("1".equals(custInfoBodyMap.get("faceValue"))) {
                    rlsb.put(MsgType.RLSB.toString(), "Y");
                    rlsb.put("isPass", "Y");
                    rlsb.put("flag", "N");
                    rlsb.put("code", "00");
                } else {
                    //有分就过
                    boolean passFlag = false;//是否通过
                    if ("2".equals(custInfoBodyMap.get("faceValue"))) {//2 - 未通过
                        Double faceVal = StringUtils.isEmpty(custInfoBodyMap.get("faceVal")) ?
                                Double.valueOf("0") :
                                Double.valueOf(custInfoBodyMap.get("faceVal").toString());
                        logger.debug("人脸分值faceVal:" + faceVal);
                        if (faceVal > 0) {
                            passFlag = true;
                            rlsb.put(MsgType.RLSB.toString(), "Y");
                            rlsb.put("isPass", "Y");
                            rlsb.put("flag", "N");
                            rlsb.put("code", "00");
                        }
                    }
                    if (!passFlag) {
                        rlsb.put("isPass", "N");
                        rlsb.put(MsgType.RLSB.toString(), "N");
                        int remainCount = 0;
                        if (!StringUtils.isEmpty(custInfoBodyMap.get("faceCount"))) {
                            Integer faceCountTemp = Integer.parseInt(custInfoBodyMap.get("faceCount").toString());
                            remainCount = faceCountTemp < faceCountLimit ? faceCountLimit - faceCountTemp : 0;
                        }
                        rlsb.put("remainCount", remainCount);
                        if (remainCount > 0) {
                            rlsb.put("flag", "Y");
                            rlsb.put("code", "10");
                        } else {
                            rlsb.put("flag", "N");
                            rlsb.put("code", "01");
                        }
                    }
                }
            }
            result.put(MsgType.RLSB.toString(), rlsb);
        }

        // 提额申请，不校验用户标签
        if ((
                (businessType.equals(BusinessType.TE.toString())
                        || businessType.equals(BusinessType.GRXX.toString()))
        )
                &&
                (!channel.toUpperCase().equals("16") && !channel.toUpperCase().equals("34"))
                ) {

            // 个人基本信息完整版
            Map<String, Object> custExtInfoMap = crmService.getCustExtInfo(params.get("custNo").toString(), "Y");
            logger.info("请求的个人基本信息为：" + custExtInfoMap);
            if (null == custExtInfoMap) {
                result.put(MsgType.GRJBXX.toString(), "N");
                //                result.put(MsgType.CERTFLAG.toString(), "N");
            }
            HashMap<String, Object> custExtBodyMap;
            if (!((ResultHead) custExtInfoMap.get("head")).getRetFlag().equals("00000")) {
                custExtBodyMap = new HashMap<>();
                //                result.put(MsgType.CERTFLAG.toString(), "N");
            } else {
                custExtBodyMap = (HashMap<String, Object>) custExtInfoMap.get("body");

                // 校验当前用户的身份证是否有效  1、判断影像表里有没有身份证正反面  2、判断身份证有效期
                List<AttachFile> attachAlreadyList;
                attachAlreadyList = attachFileRepository.findByCustNoAndIdType(params.get("custNo").toString());
                logger.info("查询的身份证信息为：" + attachAlreadyList);
                /*if (attachAlreadyList == null || attachAlreadyList.size() != 2) {
                    result.put(MsgType.CERTFLAG.toString(), "N");
                } else {
                    if (DateFormatUtils.format(new Date(), "yyyy-mm-dd").compareTo(String.valueOf(custExtBodyMap.get("certEndDt"))) < 0
                            && DateFormatUtils.format(new Date(), "yyyy-mm-dd").compareTo(String.valueOf(custExtBodyMap.get("certStrDt"))) > 0) {
                        result.put(MsgType.CERTFLAG.toString(), "Y");
                    } else {
                        result.put(MsgType.CERTFLAG.toString(), "N");
                    }
                }*/
            }
            // 取消ocr日期校验, 只校验用户是否有身份证正反面
            if (attachFileRepository.findByCustNoAndDOC5354(params.get("custNo").toString()) == 2) {
                result.put(MsgType.CERTFLAG.toString(), "Y");
            } else {
                result.put(MsgType.CERTFLAG.toString(), "N");
            }

            //            JSONObject custExtInfo = new JSONObject(custExtBodyMap);
            if (StringUtils.isEmpty(custExtBodyMap.get("education"))
                    || StringUtils.isEmpty(custExtBodyMap.get("localResid"))
                    || StringUtils.isEmpty(custExtBodyMap.get("liveProvince"))
                    || StringUtils.isEmpty(custExtBodyMap.get("liveCity"))
                    //                    || StringUtils.isEmpty(custExtBodyMap.get("liveArea"))
                    || StringUtils.isEmpty(custExtBodyMap.get("liveAddr"))
                    || StringUtils.isEmpty(custExtBodyMap.get("fmlyTel"))
                    || StringUtils.isEmpty(custExtBodyMap.get("maritalStatus"))
                    || StringUtils.isEmpty(custExtBodyMap.get("providerNum"))
                    ||
                    // 同住宅地址 regLiveInd:Y 否则regLiveInd:N
                    (!"Y".equals(custExtBodyMap.get("regLiveInd").toString())
                            &&
                            (StringUtils.isEmpty(custExtBodyMap.get("regProvince"))
                                    || StringUtils.isEmpty(custExtBodyMap.get("regCity"))
                                    //                                    || StringUtils.isEmpty(custExtBodyMap.get("regArea"))
                            )
                    )
                    ||
                    // 现住房地址 postQtInd:A 现单位地址 postQtInd:B 其他地址:postQtInd:O
                    ((!"A".equals(custExtBodyMap.get("postQtInd").toString())
                            && !"B".equals(custExtBodyMap.get("postQtInd").toString()))
                            && (
                            StringUtils.isEmpty(custExtBodyMap.get("postProvince"))
                                    || StringUtils.isEmpty(custExtBodyMap.get("postCity"))
                            //                                    || StringUtils.isEmpty(custExtBodyMap.get("postArea"))
                    )
                    )
                    ||
                    (StringUtils.isEmpty(custExtBodyMap.get("creditCount"))
                            ||
                            (!"0".equals(custExtBodyMap.get("creditCount").toString())
                                    && StringUtils.isEmpty(custExtBodyMap.get("maxAmount"))))) {
                result.put(MsgType.GRJBXX.toString(), "N");
            } else {
                result.put(MsgType.GRJBXX.toString(), "Y");
            }

            // 单位信息完整版
            if (StringUtils.isEmpty(custExtBodyMap.get("officeName"))
                    || StringUtils.isEmpty(custExtBodyMap.get("officeProvince"))
                    || StringUtils.isEmpty(custExtBodyMap.get("officeCity"))
                    //                    || StringUtils.isEmpty(custExtBodyMap.get("officeArea"))
                    || StringUtils.isEmpty(custExtBodyMap.get("officeAddr"))
                    || StringUtils.isEmpty(custExtBodyMap.get("position"))
                    || StringUtils.isEmpty(custExtBodyMap.get("custIndtry"))
                    || StringUtils.isEmpty(custExtBodyMap.get("officeDept"))
                    || StringUtils.isEmpty(custExtBodyMap.get("positionType"))
                    || StringUtils.isEmpty(custExtBodyMap.get("mthInc"))
                    || StringUtils.isEmpty(custExtBodyMap.get("officeTel"))) {
                result.put(MsgType.DWXX.toString(), "N");
            } else {
                result.put(MsgType.DWXX.toString(), "Y");
            }

            result.put(MsgType.JZXX.toString(), "Y");
            // 居住信息完整版
            // 自有房有贷款
            if (!StringUtils.isEmpty(custExtBodyMap.get("liveInfo"))
                    && "20".equals(custExtBodyMap.get("liveInfo").toString())) {
                if ((StringUtils.isEmpty(custExtBodyMap.get("pptyProvince"))
                        || StringUtils.isEmpty(custExtBodyMap.get("pptyCity"))
                        //                        || StringUtils.isEmpty(custExtBodyMap.get("pptyArea"))
                        || StringUtils.isEmpty(custExtBodyMap.get("pptyAddr"))
                        || StringUtils.isEmpty(custExtBodyMap.get("pptyRighName"))
                        || StringUtils.isEmpty(custExtBodyMap.get("pptyAmt"))
                        || StringUtils.isEmpty(custExtBodyMap.get("mortgageRatio"))
                        || StringUtils.isEmpty(custExtBodyMap.get("pptyLoanYear"))
                        || StringUtils.isEmpty(custExtBodyMap.get("mortgagePartner"))
                        || StringUtils.isEmpty(custExtBodyMap.get("pptyLoanBank"))
                        || StringUtils.isEmpty(custExtBodyMap.get("liveYear")))
                        && (!"10".equals(custExtBodyMap.get("pptyLiveInd").toString())
                        && !"Y".equals(custExtBodyMap.get("pptyLiveInd").toString()))) {
                    result.put(MsgType.JZXX.toString(), "N");
                }
            }
            // 自有房无贷款
            else if (!StringUtils.isEmpty(custExtBodyMap.get("liveInfo"))
                    && "10".equals(custExtBodyMap.get("liveInfo").toString())) {
                if ((StringUtils.isEmpty(custExtBodyMap.get("pptyProvince"))
                        || StringUtils.isEmpty(custExtBodyMap.get("pptyCity"))
                        //                        || StringUtils.isEmpty(custExtBodyMap.get("pptyArea"))
                        || StringUtils.isEmpty(custExtBodyMap.get("pptyAddr"))
                        || StringUtils.isEmpty(custExtBodyMap.get("pptyRighName"))
                        || StringUtils.isEmpty(custExtBodyMap.get("pptyAmt"))
                        || StringUtils.isEmpty(custExtBodyMap.get("liveYear")))
                        && (!"10".equals(custExtBodyMap.get("pptyLiveInd").toString())
                        && !"Y".equals(custExtBodyMap.get("pptyLiveInd").toString()))) {
                    result.put(MsgType.JZXX.toString(), "N");
                }
            }
            // 无自有房
            else if (StringUtils.isEmpty(custExtBodyMap.get("liveYear"))) {
                result.put(MsgType.JZXX.toString(), "N");
            }

            // 联系人信息三个
            List<Map<String, Object>> listLxr = (List<Map<String, Object>>) custExtBodyMap.get("lxrList");
            logger.debug("联系人信息：" + listLxr);
            if (listLxr == null || listLxr.size() == 0) {
                result.put(MsgType.LXRXX.toString(), "N");
            } else {
                if (((businessType.equals(BusinessType.GRXX.toString()) || channel.equals("16")) && listLxr.size() >= 1)
                        ||
                        ((businessType.equals(BusinessType.TE.toString())) && listLxr.size() >= 3)) {
                    result.put(MsgType.LXRXX.toString(), "Y");
                } else {
                    result.put(MsgType.LXRXX.toString(), "N");
                }
            }

            // 提额必传影像是否完整
            List<String> idRequestList = new ArrayList<>();//身份证必传影像标准
            idRequestList.add("DOC53");//身份证正面
            idRequestList.add("DOC54");//身份证反面
            if (BusinessType.TE.toString().equals(businessType)
                    || (businessType.equals(BusinessType.EDJH.toString()) && !channel.equals("16"))) {
                for (Map<String, Object> map : teBcyx) {
                    idRequestList.add(map.get("docCde").toString());
                }
            }
            //按客户编号查询影像列表
            List<Map<String, Object>> attachAlreadyList;
            if (params.get("isOrder").equals("Y") && !channel.equals("16")) {
                attachAlreadyList = attachService
                        .attachSearchPerson(params.get("custNo").toString(), params.get("applSeq").toString());
            } else {
                attachAlreadyList = attachService.attachSearchPersonAndApplSeq(params.get("custNo").toString(), null);
            }
            List<String> attachAlreadyCdeList = new ArrayList<>();
            for (Map<String, Object> m : attachAlreadyList) {
                attachAlreadyCdeList.add(m.get("attachType").toString());
            }
            logger.debug("客户编号查询影像列表=" + attachAlreadyCdeList);

            //判断是否一致
            Map<String, Object> bcResult = new HashedMap();
            if (businessType.equals(BusinessType.TE.toString())) {
                bcResult.put("list", teBcyx);
            } else {
                bcResult.put("list", bcyx);
            }
            logger.debug("必传影像结果:" + attachAlreadyCdeList + ", 必填列表:" + idRequestList);
            if (attachAlreadyCdeList.containsAll(idRequestList)) {
                bcResult.put(MsgType.BCYX.toString(), "Y");
            } else {
                bcResult.put(MsgType.BCYX.toString(), "N");
            }
            result.put(MsgType.BCYX.toString(), bcResult);

            // 返回更多影像列表 只返回类型,无校验结果
            String url = EurekaServer.CMISPROXY + "/api/pLoanTyp/images?isAll=" + 0;
            String s = HttpUtil.restGet(url, super.getToken());
            List<Map<String, Object>> syyx = HttpUtil.json2List(s);
            List<Map<String, Object>> qtyx = new ArrayList<>();
            if (syyx == null) {
                logger.info("获取所有影像类型数据失败");
                throw new RuntimeException("获取影像信息失败");
            }
            for (Map<String, Object> yxxx : syyx) {
                // 过滤掉人脸照片
                if (!idRequestList.contains(yxxx.get("docCde"))
                        && !"DOC065".equals(yxxx.get("docCde"))) {
                    yxxx.put("docReInd", "02");
                    qtyx.add(yxxx);
                }
            }
            result.put(MsgType.QTYX.toString(), qtyx);

            // 银行流水
            idRequestList.clear();
            idRequestList.add("DOC023");
            Map<String, Object> yhls = new HashedMap();
            yhls.put("docCde", "DOC023");
            yhls.put("docDesc", "银行流水");
            if (attachAlreadyCdeList.containsAll(idRequestList)) {
                yhls.put(MsgType.YHLS.toString(), "Y");
            } else {
                yhls.put(MsgType.YHLS.toString(), "N");
            }
            result.put(MsgType.YHLS.toString(), yhls);

            // 工作证明
            idRequestList.clear();
            idRequestList.add("DOC007");
            if (attachAlreadyCdeList.containsAll(idRequestList)) {
                result.put(MsgType.GZZM.toString(), "Y");
            } else {
                result.put(MsgType.GZZM.toString(), "N");
            }

            // 公积金
            idRequestList.clear();
            idRequestList.add("DOC035");
            Map<String, Object> gjj = new HashedMap();
            gjj.put("docCde", "DOC035");
            gjj.put("docDesc", "公积金");
            if (attachAlreadyCdeList.containsAll(idRequestList)) {
                gjj.put(MsgType.GJJ.toString(), "Y");
            } else {
                gjj.put(MsgType.GJJ.toString(), "N");
            }
            result.put(MsgType.GJJ.toString(), gjj);

            // 房产信息
            idRequestList.clear();
            idRequestList.add("DOC027");
            Map<String, Object> fcxx = new HashedMap();
            fcxx.put("docCde", "DOC027");
            fcxx.put("docDesc", "房产信息");
            if (attachAlreadyCdeList.containsAll(idRequestList)) {
                fcxx.put(MsgType.FCXX.toString(), "Y");
            } else {
                fcxx.put(MsgType.FCXX.toString(), "N");
            }
            result.put(MsgType.FCXX.toString(), fcxx);

            // 车辆信息
            idRequestList.clear();
            idRequestList.add("DOC028");
            Map<String, Object> clxx = new HashedMap();
            clxx.put("docCde", "DOC028");
            clxx.put("docDesc", "车辆信息");
            if (attachAlreadyCdeList.containsAll(idRequestList)) {
                clxx.put(MsgType.CLXX.toString(), "Y");
            } else {
                clxx.put(MsgType.CLXX.toString(), "N");
            }
            result.put(MsgType.CLXX.toString(), clxx);

            // 更多影像信息
            return success(result);
        }

        // TODO 实名认证

        // 个人基本资料简版
        Map<String, Object> custExtInfoMap = crmService.getCustExtInfo(params.get("custNo").toString(), "Y");
        if (null == custExtInfoMap) {
            result.put(MsgType.GRJBXX.toString(), "N");
            result.put(MsgType.CERTFLAG.toString(), "N");
        }
        JSONObject custExtInfoJson = new JSONObject(custExtInfoMap);
        if (!((JSONObject) custExtInfoJson.get("head")).get("retFlag").equals("00000")) {
            result.put(MsgType.GRJBXX.toString(), "N");
            result.put(MsgType.LXRXX.toString(), "N");
            result.put(MsgType.DWXX.toString(), "N");
            result.put(MsgType.CERTFLAG.toString(), "N");
        } else {
            Map<String, Object> custExtInfo = (Map<String, Object>) custExtInfoMap.get("body");
            logger.info("custExtInfo输出:" + custExtInfo);

            // 校验当前用户的身份证有效期
            List<AttachFile> attachAlreadyList;
            attachAlreadyList = attachFileRepository.findByCustNoAndIdType(params.get("custNo").toString());
            logger.info("查询的身份证信息为：" + attachAlreadyList);
            // noEduLocal 校验最高学历、户口性质等信息.
            if (StringUtils.isEmpty(params.get("noEduLocal")) || "YES".equals(params.get("noEduLocal").toString())) {
                if (StringUtils.isEmpty(custExtInfo.get("education"))
                        || StringUtils.isEmpty(custExtInfo.get("localResid"))) {
                    result.put(MsgType.GRJBXX.toString(), "N");
                }

                // 美凯龙不校验
                if (!"16".equals(channel)) {
                    if (StringUtils.isEmpty(custExtInfo.get("liveInfo"))
                            || (!"Y".equals(custExtInfo.get("regLiveInd").toString())
                            &&
                            (StringUtils.isEmpty(custExtInfo.get("regProvince"))
                                    || StringUtils.isEmpty(custExtInfo.get("regCity"))
                                    //                              || StringUtils.isEmpty(custExtInfo.get("regArea"))
                            ))) {
                        result.put(MsgType.GRJBXX.toString(), "N");
                    }
                    if (StringUtils.isEmpty(custExtInfo.get("liveInfo"))) {
                        result.put(MsgType.GRJBXX.toString(), "N");
                    }
                }
            }

            if (StringUtils.isEmpty(custExtInfo.get("liveProvince"))
                    || StringUtils.isEmpty(custExtInfo.get("liveCity"))
                    //                    || StringUtils.isEmpty(custExtInfo.get("liveArea"))
                    || StringUtils.isEmpty(custExtInfo.get("liveAddr"))
                    || StringUtils.isEmpty(custExtInfo.get("maritalStatus"))
                    ) {
                result.put(MsgType.GRJBXX.toString(), "N");
            } else {
                result.put(MsgType.GRJBXX.toString(), "Y");
            }

            // 联系人信息一个
            List<Map<String, Object>> listLxr = (List<Map<String, Object>>) custExtInfo.get("lxrList");
            if (listLxr.size() >= 1) {
                result.put(MsgType.LXRXX.toString(), "Y");
            } else {
                result.put(MsgType.LXRXX.toString(), "N");
            }

            // 单位信息简版
            if (StringUtils.isEmpty(custExtInfo.get("officeName"))
                    || StringUtils.isEmpty(custExtInfo.get("officeProvince"))
                    || StringUtils.isEmpty(custExtInfo.get("officeCity"))
                    || StringUtils.isEmpty(custExtInfo.get("officeAddr"))
                    || StringUtils.isEmpty(custExtInfo.get("officeTel"))) {
                result.put(MsgType.DWXX.toString(), "N");
            } else {
                result.put(MsgType.DWXX.toString(), "Y");
            }

            // 美凯龙不校验
            if (!"16".equals(channel)) {
                if (StringUtils.isEmpty(custExtInfo.get("position")) || StringUtils.isEmpty(custExtInfo.get("mthInc"))) {
                    result.put(MsgType.DWXX.toString(), "N");
                }
            }

        }

        // 当前取消日志校验，直接返回Y
        if (attachFileRepository.findByCustNoAndDOC5354(params.get("custNo").toString()) == 2) {
            result.put(MsgType.CERTFLAG.toString(), "Y");
        } else {
            result.put(MsgType.CERTFLAG.toString(), "N");
        }
        /**
         * 影像信息.
         * 录单： 贷款品种关联必选项校验
         * 额度激活：身份证正反面
         */
        if ((businessType.equals(BusinessType.SPFQ.toString())
                || businessType.equals(BusinessType.XJD.toString()))) {
            // 贷款品种关联影像
            Map<String, Object> isComplete = attachService
                    .attachIsComplete(params.get("custNo").toString(), params.get("typCde").toString(),
                            params.get("applSeq").toString());
            Map<String, Object> ploanMap = attachService
                    .getPLoanTypImages(this.getGateUrl(), params.get("typCde").toString(), null, "0");
            if (ploanMap == null) {
                logger.info("获取贷款品种所需影像列表失败");
                throw new RuntimeException("获取贷款品种所需影像列表失败");
            }
            Map<String, Object> yxResult = new HashedMap();
            yxResult.put(MsgType.BCYX.toString(), ((Map) isComplete.get("body")).get("msg"));
            bcyx.clear();
            if (!"00000".equals(ploanMap.get("retCode"))) {
                return fail("01", ploanMap.get("retMsg").toString());
            } else {
                List<Map<String, Object>> loanResult = (List<Map<String, Object>>) ploanMap.get("retList");
                for (Map<String, Object> map : loanResult) {
                    if ("01".equals(map.get("docRevInd"))) {//必传的影像   01：必传
                        bcyx.add(map);
                    }
                }
            }
            yxResult.put("list", bcyx);
            result.put(MsgType.BCYX.toString(), yxResult);
        } else {
            // 身份证正反面
            // 提额必传影像是否完整
            List<String> idRequestList = new ArrayList<>();//身份证必传影像标准
            idRequestList.add("DOC53");//身份证正面
            idRequestList.add("DOC54");//身份证反面
            if (businessType.equals(BusinessType.EDJH.toString())
                    && !"16".equals(channel)) {
                for (Map<String, Object> map : teBcyx) {
                    idRequestList.add(map.get("docCde").toString());
                }
            }
            //按客户编号查询影像列表
            List<Map<String, Object>> attachAlreadyList = attachService
                    .attachSearchPersonAndApplSeq(params.get("custNo").toString(), null);
            List<String> attachAlreadyCdeList = new ArrayList<>();
            for (Map<String, Object> m : attachAlreadyList) {
                attachAlreadyCdeList.add(m.get("attachType").toString());
            }
            logger.debug("客户编号查询影像列表=" + attachAlreadyCdeList);

            //判断是否一致
            Map<String, Object> bcResult = new HashedMap();
            if (businessType.equals(BusinessType.EDJH.toString())
                    && !"16".equals(channel)) {
                bcResult.put("list", teBcyx);
            } else {
                bcResult.put("list", bcyx);
            }
            if (attachAlreadyCdeList.containsAll(idRequestList)) {
                bcResult.put(MsgType.BCYX.toString(), "Y");
                result.put(MsgType.BCYX.toString(), bcResult);
            } else {
                bcResult.put(MsgType.BCYX.toString(), "N");
                result.put(MsgType.BCYX.toString(), bcResult);
            }
        }

        // 返回更多影像列表 只返回类型,无校验结果
        String typCde = String.valueOf(params.get("typCde"));

        // 星巢贷默认获取
        if ("16".equals(channel) && !StringUtils.isEmpty(this.xcdTypCde)) {
            typCde = this.xcdTypCde;
            params.put("typCde", typCde);
        }

        String url;
        if (StringUtils.isEmpty(params.get("typCde"))) {
            url = EurekaServer.CMISPROXY + "/api/pLoanTyp/images?isAll=" + 0;
        } else {
            url = EurekaServer.CMISPROXY + "/api/pLoanTyp/typImages?typCde=" + typCde;
        }
        logger.info("影像获取url:" + url);
        String s = HttpUtil.restGet(url, super.getToken());
        List<Map<String, Object>> syyx = HttpUtil.json2List(s);
        List<Map<String, Object>> qtyx = new ArrayList<>();
        if (syyx == null) {
            logger.info("获取所有影像类型数据为空");
            syyx = new ArrayList<>();
            //            throw new RuntimeException("获取影像信息失败");
        }

        List<String> cdeRequest = new ArrayList<>();
        for (Map<String, Object> bc : bcyx) {
            cdeRequest.add(bc.get("docCde").toString());
        }
        for (Map<String, Object> yxxx : syyx) {
            // 过滤掉人脸照片
            if (!cdeRequest.contains(yxxx.get("docCde").toString())
                    && !"DOC065".equals(yxxx.get("docCde"))) {
                yxxx.put("docReInd", "02");
                qtyx.add(yxxx);
            }
        }
        result.put(MsgType.QTYX.toString(), qtyx);

        // 简版个人信息包括个人信息、联系人信息、单位信息
        if (!result.get(MsgType.GRJBXX.toString()).equals("Y")
                || !result.get(MsgType.LXRXX.toString()).equals("Y")
                || !result.get(MsgType.DWXX.toString()).equals("Y")) {
            result.put(MsgType.GRJBXX.toString(), "N");
        }

        if (tag.equals(UserTag.SHH.toString())) {
            // TODO 资产证明 公积金  银联

        }
        logger.info("信息完整性结果输出：" + success(result));
        return success(result);
    }

    @Override
    public void updateDeleteCardToEmpty(String cardNo) {
        Map<String, Object> params = new HashMap<>();
        params.put("cardNo", cardNo);
        params.put("channelNo", super.getChannelNo());
        params.put("sysFlag", super.getChannel());
        // 删除订单中待提交银行卡信息.
        Map<String, Object> result = HttpUtil
                .restPostMap(EurekaServer.ORDER + "api/order/deleteUnSubmitCardInfo", params);
        if (!HttpUtil.isSuccess(result)) {
            logger.info("调用订单接口删除银行卡失败,结果:" + result + ",卡号:" + cardNo);
        } else {
            logger.info("调用订单接口删除银行卡成功,结果:" + result + ",卡号:" + cardNo);
        }

        // 删除收单中待提交银行卡信息.
        result = AcqUtil.getAcqResponse(EurekaServer.ACQUIRER + "/api/appl/deleteUnSubmitCardInfo",
                AcqTradeCode.DELETE_UNSUBMIT_CARD_INFO
                , super.getChannel(), super.getChannelNo(), null, null, params);
        if (!HttpUtil.isSuccess(result)) {
            logger.info("调用收单接口删除银行卡失败,结果:" + result + ",卡号:" + cardNo);
        } else {
            logger.info("调用收单接口删除银行卡成功,结果:" + result + ",卡号:" + cardNo);
        }
    }

    @Override
    public Map<String, Object> getDateAppOrderPerson(String crtUsr, String idNo, Integer page, Integer size,
                                                     String source) {

        HashMap<String, Object> map = new HashMap<>();
        List<Map<String, Object>> returnList = new ArrayList<>();
        // 按日期取appServer的订单列表，个人版取待提交订单，此订单列表不参与分页运算，始终置顶。
        logger.info("Service:getDateAppOrderPerson source:" + source);
        if (page == 1) {
            List<Object[]> list = appOrderREpositoryImpl.queryAppOrder(crtUsr, null, idNo, source);
            if (list != null) {
                for (Object[] appOrder : list) {
                    if (!"3".equals(appOrder[7])) {
                        continue; // 只显示3-商户退回的待提交订单
                    }
                    HashMap<String, Object> hm = new HashMap<>();
                    DecimalFormat df = new DecimalFormat("#0.00"); // 订单编号
                    hm.put("orderNo", appOrder[0]);
                    // applyAmt 贷款总数（借款总数）
                    BigDecimal amount1 = appOrder[1] == null ? BigDecimal.ZERO : new BigDecimal(appOrder[1].toString());
                    BigDecimal amount2 = appOrder[2] == null ? BigDecimal.ZERO : new BigDecimal(appOrder[2].toString());
                    BigDecimal amount3 = appOrder[3] == null ? BigDecimal.ZERO : new BigDecimal(appOrder[3].toString());
                    BigDecimal amount = amount1.add(amount2).add(amount3);
                    hm.put("applyAmt", df.format(amount1));
                    hm.put("fee", df.format(amount2.add(amount3)));
                    // applyTnr(期数)
                    Integer applyTnr = appOrder[4] == null ? 0 : new Integer((String) appOrder[4]);
                    hm.put("applyTnr", applyTnr);
                    // applyTnrTyp(期数类型)
                    String applyTnrTypStr = appOrder[11] == null ? "" : (String) appOrder[11];
                    hm.put("applyTnrTyp", applyTnrTypStr);
                    if ("D".equals(applyTnrTypStr)) {
                        applyTnr = 1;
                    }
                    if (applyTnr > 0 && amount.compareTo(BigDecimal.ZERO) == 1) {
                        hm.put("mthAmt", df.format(
                                amount.divide(new BigDecimal(applyTnr), 2, BigDecimal.ROUND_HALF_UP).doubleValue()));
                    } else {
                        hm.put("mthAmt", new BigDecimal(0));
                    }

                    // 申请日期 贷款类型
                    hm.put("typGrp", appOrder[5] == null ? "" : appOrder[5]);
                    hm.put("applyDt", appOrder[6] == null ? "" : appOrder[6]);
                    hm.put("outSts", appOrder[7] == null ? "" : appOrder[7]);
                    hm.put("goodsName", appOrder[8] == null ? "" : appOrder[8]);
                    hm.put("goodsCount", appOrder[9] == null ? "" : appOrder[9]);
                    hm.put("custName", appOrder[10] == null ? "" : appOrder[10]);

                    returnList.add(hm);
                }
            }
        }

        // 按日期取cmisServer的订单列表
        // String channel = StringUtils.isEmpty(super.getChannel()) ? "" : super.getChannel();
        String sourceTem = "16".equals(source) ? "31" : source;//星巢贷转译
        String url = EurekaServer.CMISPROXY + "/api/appl/queryApplListByDatePerson?idNo=" + idNo + "&page="
                + page
                + "&pageSize=" + size + "&source=" + sourceTem;

        logger.info("queryApplListByDatePerson url:" + url);
        String json = HttpUtil.restGet(url, super.getToken());
        logger.info("从cmisServer索取的订单列表为：" + json);
        if (StringUtils.isEmpty(json)) {
            return fail("08", "查询失败");
        }
        List<Map<String, Object>> cmisList = HttpUtil.json2List(json);
        returnList.addAll(cmisList);
        map.put("orders", returnList);
        return map;
    }

    @Override
    public Map<String, Object> getDateAppOrderNew(Map<String, Object> map) {
        List<Map<String, Object>> returnList = new ArrayList<>();

        Map<String, Object> params = new HashMap<>();
        Map<String, Object> paramsHead = new HashMap<>();
        Map<String, Object> paramsBody = new HashMap<>();
        String sourceForm = (String) map.get("sourceForm");
        //        paramsBody.put("sourceForm", sourceForm);
        Integer page = (Integer) map.get("page");
        Integer pageSize = (Integer) map.get("pageSize");
        // TODO: 2017/5/22 暂时全部展示订单
        map.put("pageSize",1000);
        String outStatus = (String) map.get("outSts");
        logger.info("==outSts:" + outStatus);
        String channel = (String) map.get("channel");
        String channelNo = (String) map.get("channelNo");
        paramsHead.put("channel", map.get("channel"));
        paramsHead.put("channelNo", map.get("channelNo"));
        params.put("head", paramsHead);
        Map<String, Object> requestMap = new HashMap<>();

        if ("WS".equals(outStatus) || "30".equals(outStatus)) {//待发货 查询订单
            //WS先从订单系统查订单状态为30的流水号，再调用收单接口批量查询贷款信息
            try {
                returnList = queryWaitSendOrder(map);
            } catch (Exception e) {
                e.printStackTrace();
                return fail(RestUtil.ERROR_INTERNAL_CODE, RestUtil.ERROR_INTERNAL_MSG);
            }
        } else {//其他状态 查询收单
            logger.info("需要查询的订单状态(空则查询全部):" + outStatus);
            if (StringUtils.isEmpty(outStatus)) {
                outStatus = "APP";//App专用 不包含 待提交 待确认的订单
                map.put("outSts", outStatus);
            }

            try {
                logger.info("查询状态:" + outStatus + ",sourceForm:" + sourceForm);
                returnList = selectApplList(map);
                if ("01".equals(outStatus) && "02".equals(sourceForm)) {//审批中
                    //个人提交的待确认订单也在审批中显示,商户版的不显示
                    map.put("outSts", "2");//2-待确认
                    List<Map<String, Object>> dqrList = selectApplList(map);
                    for (Map<String, Object> mapTem : dqrList) {
                        mapTem.put("status", "2");//订单状态 1-待提交 2-待确认 4-被退回
                        returnList.add(mapTem);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                return fail(RestUtil.ERROR_INTERNAL_CODE, RestUtil.ERROR_INTERNAL_MSG);
            }
        }
        Map<String, Object> returnMap = new HashMap<>();
        sortLoanList(returnList);//排序
        returnMap.put("orders", returnList);
        return success(returnMap);
    }

    public List<Map<String, Object>> selectApplList(Map<String, Object> map) throws Exception {
        List<Map<String, Object>> returnList = new ArrayList<>();

        String sourceForm = (String) map.get("sourceForm");
        Integer page = (Integer) map.get("page");
        Integer pageSize = (Integer) map.get("pageSize");
        String outStatus = (String) map.get("outSts");
        logger.info("==outSts:" + outStatus);
        String channel = (String) map.get("channel");
        String channelNo = (String) map.get("channelNo");

        Map<String, Object> requestMap = new HashMap<>();
        if ("01".equals(sourceForm)) {//商户版
            requestMap.put("crtUsr", map.get("crtUsr"));
            requestMap.put("custName", map.get("custName"));
        } else {//个人版
            requestMap.put("idNo", map.get("idNo"));
        }

        requestMap.put("applyDate", map.get("applyDate"));
        requestMap.put("page", page);
        requestMap.put("pageSize", pageSize);
        requestMap.put("outSts", outStatus);
        //        params.put("body", paramsBody);
        //        requestMap.put("request", params);
        String url = EurekaServer.ACQUIRER + "/api/appl/selectApplList";
        logger.info("==>ACQ 贷款列表查询 url:" + url + ", 请求参数:" + requestMap);
        Map<String, Object> acqResponse = AcqUtil
                .getAcqResponse(url, AcqTradeCode.SELECT_APPL_LIST, channel, channelNo, null, null, requestMap);
        logger.info("<==ACQ  返回结果:" + acqResponse);
        if (acqResponse == null || acqResponse.isEmpty()) {
            logger.info("收单系统通信失败");
            throw new Exception("收单系统通信失败");
        }
        if (!CmisUtil.getIsSucceed(acqResponse)) {
            logger.info(RestUtil.ERROR_INTERNAL_MSG);
            throw new Exception(RestUtil.ERROR_INTERNAL_MSG);
        }
        Map<String, Object> responseMap = (Map<String, Object>) acqResponse.get("response");
        Map<String, Object> responseHeadMap = (Map<String, Object>) responseMap.get("head");
        Map<String, Object> responseBodyMap = (Map<String, Object>) responseMap.get("body");
        List<Map<String, Object>> cmisList = (List<Map<String, Object>>) responseBodyMap.get("list");
        // 去订单系统查询订单状态等信息
        if (cmisList != null && cmisList.size() > 0) {
            String batchQueryOrderStateUrl = EurekaServer.ORDER + "/api/order/batchQueryOrderState";
            List<Map<String, Object>> requestList = new ArrayList<>();
            cmisList.forEach(cmisMap -> {
                Map<String, Object> applSeqMap = new HashMap<>();
                applSeqMap.put("applSeq", cmisMap.get("applSeq"));
                requestList.add(applSeqMap);
            });
            String requestOrderJson = JSONObject.valueToString(requestList);
            Map<String, Object> orderResponseMap = batchQueryOrderState(batchQueryOrderStateUrl, requestOrderJson);
            if (orderResponseMap == null || orderResponseMap.isEmpty()) {
                logger.info("订单系统通信失败");
                throw new Exception("系统通信失败");
            }
            if (!HttpUtil.isSuccess(orderResponseMap)) {
                logger.info("OM批量查询订单状态" + orderResponseMap);
                ResultHead repHead = (ResultHead) orderResponseMap.get("head");
                throw new Exception(repHead.getRetMsg());
            }
            Map<String, Object> orderBodyMap = (Map<String, Object>) orderResponseMap.get("body");
            List<Map<String, Object>> orderStateList = (List<Map<String, Object>>) orderBodyMap.get("list");
            if (orderStateList.size() != cmisList.size()) {
                throw new Exception("查询失败");
            }
            for (int i = 0; i < orderStateList.size(); i++) {
                Map<String, Object> orderStateMap = orderStateList.get(i);
                Map<String, Object> cmisMap = cmisList.get(i);
                //现金贷applSeq替代orderNo
                String applSeq = String.valueOf(cmisMap.get("applSeq"));
                cmisMap.put("applSeq", applSeq);//统一格式为String
                String typGrp = StringUtils.isEmpty(cmisMap.get("typGrp")) ?
                        "" :
                        (String) cmisMap.get("typGrp");//01、耐用消费品贷款 02、一般消费贷款 03、伞下店
                logger.info("===applSeq:" + applSeq + ",typGrp:" + typGrp);
                String outSts = StringUtils.isEmpty(cmisMap.get("outSts")) ? "" : (String) cmisMap.get("outSts");

                String formTyp = StringUtils.isEmpty(orderStateMap.get("formTyp")) ?
                        "" :
                        (String) orderStateMap.get("formTyp");//订单类型
                cmisMap.put("formTyp", formTyp);//订单类型 10-线下订单 20-线上订单 21-商户扫码录单 11-个人扫码录单
                //获取订单状态
                String sysSts = StringUtils.isEmpty(orderStateMap.get("sysSts")) ?
                        "" :
                        (String) orderStateMap.get("sysSts");
                //处理返回状态
                /**
                 * 30-已放款待发货 31-已发货 92-退货中 93-已退货
                 （1）05-审批通过等待放款： 线下订单:除了30直接返回审批状态; 线上订单:30.31.92.93返回订单状态，其他返回审批状态。
                 （2）04-合同签订中，线下订单，修改订单状态为“30-待发货”；线上订单不做判断
                 （3）06-已放款：除了逾期，优先返回发货状态
                 线下订单:除了30直接返回审批状态; 线上订单:30.31.92.93返回订单状态，其他返回审批状态
                 另外：判断待发货状态的时候，需要判断isNeedSend是否需要发货处理字段，如果不需要发货处理，就不进入待发货状态
                 */
                String isNeedSend = (String) orderStateMap.get("isNeedSend"); //是否需要发货处理 00--不需要  01--需要

                logger.info("申请流水号:" + applSeq + ",审批状态:" + outSts + ",hkSts还款状态:" + cmisMap.get("hkSts") + ",订单状态:" + sysSts);
                if ("06".equals(outSts)) {//已放款 已放款的，除了逾期，优先返回发货状态
                    //hkSts还款状态：  还款中（00）：逾期（01）：已结清（02）    其余状态返空。
                    String hkSts = StringUtils.isEmpty(cmisMap.get("hkSts")) ? "" : (String) cmisMap.get("hkSts");
                    if ("01".equals(hkSts)) {//逾期
                        cmisMap.put("outSts", "OD");
                    } else {//非逾期 优先返回发货状态
                        if ("02".equals(hkSts)) {//已结清
                            //SE为结清，NS为未结清
                            cmisMap.put("ifSettled", "SE");
                        } else {//还款中（00）
                            cmisMap.put("ifSettled", "NS");
                        }
                        if ("20".equals(formTyp)) {//线上订单
                            // TODO: 2017/6/9 临时方案： 线上订单（顺逛）未提供退货的相关接口，收单系统无法更新订单状态
                            // 若线上订单（顺逛）已经结清，app后台临时显示 结清状态，不显示订单状态。待收单系统上线后，恢复版本
                            if ("02".equals(hkSts)) {//已结清
                                cmisMap.put("outSts", outSts);
                            }else {
                                if ("92".equals(sysSts) || "93".equals(sysSts) || "30".equals(sysSts) || "31".equals(sysSts)) {//92-退货中 93-已退货
                                    cmisMap.put("outSts", sysSts);
                                }
                            }
                        } else {//线下订单 除了30直接返回审批状态
                            if ("30".equals(sysSts)) {
                                cmisMap.put("outSts", sysSts);
                            }
                        }
                    }
                } else if ("04".equals(outSts)) {//04-合同签订中
                    if ("10".equals(formTyp) && "01".equals(isNeedSend)) {//10-线下订单 //需要发货处理
                        cmisMap.put("outSts", "30");//30-已付款待发货
                        if ("04".equals(outStatus)) {//单独查询04状态的订单
                            cmisMap.put("isWaitSendFlag", "Y");
                        }
                    }
                } else if ("05".equals(outSts)) {//05-审批通过，等待放款
                    if ("20".equals(formTyp)) {//线上订单
                        if ("92".equals(sysSts) || "93".equals(sysSts) || "30".equals(sysSts) || "31".equals(sysSts)) {//92-退货中 93-已退货
                            cmisMap.put("outSts", sysSts);
                        }
                    } else {//线下订单 除了30直接返回审批状态
                        if ("30".equals(sysSts)) {
                            cmisMap.put("outSts", sysSts);
                        }
                    }
                }

                if ("01".equals(sourceForm)) {//商户版
                    cmisMap.put("crtUsr", map.get("crtUsr"));
                }

                if (!"02".equals(typGrp)) {
                    String formId = StringUtils.isEmpty(cmisMap.get("formId")) ?
                            "" :
                            (String) cmisMap.get("formId");//订单ID
                    cmisMap.put("orderNo", formId);
                } else {// 02、一般消费贷款
                    cmisMap.put("orderNo", applSeq);
                }
                String merchantConfirm = StringUtils.isEmpty(cmisMap.get("merchantConfirm")) ?
                        "" :
                        (String) cmisMap.get("merchantConfirm");
                // 个人版返回订单状态status 1-待提交 2-待确认 4-被退回
                if (!"01".equals(sourceForm)) {//个人版
                    if ("00".equals(outSts)) {
                        //商户确认 01-待确认  02-商户退回
                        if (StringUtils.isEmpty(merchantConfirm)) {//待提交
                            cmisMap.put("outSts", "1");
                        }
                        cmisMap.put("status", "1");//待提交
                        if ("01".equals(merchantConfirm)) {//01-待确认
                            cmisMap.put("outSts", "2");
                            cmisMap.put("status", "2");
                        } else if ("02".equals(merchantConfirm)) {//02-商户退回
                            cmisMap.put("outSts", "3");
                            cmisMap.put("status", "4");
                        }
                    } else if ("22".equals(outSts)) {//区分审批退回 商户退回
                        if ("02".equals(merchantConfirm)) {//02-商户退回
                            cmisMap.put("outSts", "3");
                            cmisMap.put("status", "4");
                        }
                    }
                } else {//商户版
                    if ("00".equals(outSts)) {
                        //商户确认 01-待确认  02-商户退回
                        if (StringUtils.isEmpty(merchantConfirm)) {//待提交
                            cmisMap.put("outSts", "1");
                        }
                        if ("01".equals(merchantConfirm)) {//01-待确认
                            cmisMap.put("outSts", "2");
                        } else if ("02".equals(merchantConfirm)) {//02-商户退回
                            cmisMap.put("outSts", "3");
                        }
                    } else if ("22".equals(outSts)) {//区分审批退回 商户退回
                        if ("02".equals(merchantConfirm)) {//02-商户退回
                            cmisMap.put("outSts", "3");
                        }
                    }
                }
                DecimalFormat df = new DecimalFormat("#0.00");
                BigDecimal totalnormint = StringUtils.isEmpty(cmisMap.get("totalnormint")) ?
                        BigDecimal.ZERO :
                        new BigDecimal(String.valueOf(cmisMap.get("totalnormint")));//总利息
                BigDecimal totalfeeamt = StringUtils.isEmpty(cmisMap.get("totalfeeamt")) ?
                        BigDecimal.ZERO :
                        new BigDecimal(String.valueOf(cmisMap.get("totalfeeamt")));//总费用
                cmisMap.put("fee", df.format(totalnormint.add(totalfeeamt)));//息费
            }
            returnList.addAll(cmisList);
        }
        if ("04".equals(outStatus)) {//单独查询04状态的订单
            returnList.removeIf(retMap -> "Y".equals(retMap.get("isWaitSendFlag")));
        }
        return returnList;
    }

    //订单系统批量查询订单状态
    public Map<String, Object> batchQueryOrderState(String url, String requestJson) {
        Map<String, Object> returnMap = new HashMap<>();

        logger.info("==>ORDER  url:" + url + ", 请求参数:" + requestJson);
        String returnOrderJson = HttpUtil.restPost(url, null, requestJson, 200);
        logger.info("<==ORDER  返回结果:" + returnOrderJson);
        if (StringUtils.isEmpty(returnOrderJson)) {
            return fail(RestUtil.ERROR_INTERNAL_CODE, RestUtil.ERROR_INTERNAL_MSG);
        }
        Map<String, Object> orderResponse = HttpUtil.json2DeepMap(returnOrderJson);
        if (isSuccess(orderResponse)) {
            List<Map<String, Object>> orderStateList = (List<Map<String, Object>>) orderResponse.get("body");
            returnMap.put("list", orderStateList);
            return success(returnMap);
        } else {
            Map<String, Object> responseHeadMap = (Map<String, Object>) orderResponse.get("head");
            return fail("99", (String) responseHeadMap.get("retMsg"));
        }
    }

    @Override
    public Map<String, Object> queryApplAmountByIdNo(String idNo, String flag) {
        Map<String, Object> returnMap = new HashMap<>();

        String url =
                EurekaServer.CMISPROXY + "/api/appl/queryApplAmountByIdNo?idNo=" + idNo + "&flag=" + flag;
        logger.info("从cmisServer中查询的待还款信息url为：" + url);
        String json = HttpUtil.restGet(url, super.getToken());
        logger.info("从cmisServer中查询的待还款信息为：" + json);
        if (StringUtils.isEmpty(json)) {
            return fail("08", "查询失败");
        } else {
            returnMap = HttpUtil.json2Map(json);
        }
        return returnMap;
    }

    /**
     * 校验身份证是否有效
     *
     * @param params
     * @return
     */
    @Override
    public Map checkIfCertValid(Map<String, Object> params) {

        Map<String, Object> result = new HashedMap();

        Map<String, Object> smrzInfo;
        //result.put(MsgType.SMRZ.toString(), "Y");
        // 个人基本信息完整版
        Map<String, Object> custExtInfoMap = crmService.getCustExtInfo(params.get("custNo").toString(), "Y");
        logger.info("请求的个人基本信息为：" + custExtInfoMap);
        if (null == custExtInfoMap) {
            result.put(MsgType.CERTFLAG.toString(), "N");
        }
        HashMap<String, Object> custExtBodyMap;
        if (!((ResultHead) custExtInfoMap.get("head")).getRetFlag().equals("00000")) {
            custExtBodyMap = new HashMap<>();
            result.put(MsgType.CERTFLAG.toString(), "N");
        } else {
            custExtBodyMap = (HashMap<String, Object>) custExtInfoMap.get("body");

            // 校验当前用户的身份证有效期
            if (DateFormatUtils.format(new Date(), "yyyy-mm-dd")
                    .compareTo(String.valueOf(custExtBodyMap.get("certEndDt"))) < 0
                    && DateFormatUtils.format(new Date(), "yyyy-mm-dd")
                    .compareTo(String.valueOf(custExtBodyMap.get("certStrDt"))) > 0) {
                result.put(MsgType.CERTFLAG.toString(), "Y");
            } else {
                result.put(MsgType.CERTFLAG.toString(), "N");
            }
        }
        return success(result);
    }

    @Override
    public Map<String, Object> returnGoods(Map<String, Object> params) {
        String applSeq = (String) params.get("applSeq");
        AppOrdernoTypgrpRelation relation = appOrdernoTypgrpRelationRepository.findByApplSeq(applSeq);
        if (relation == null) {
            return fail("43", "该订单不存在");
        }
        String formId = relation.getOrderNo();
        params.put("formId", formId);
        String url = EurekaServer.ORDER + "/api/order/returnCargo";
        logger.info("==> ORDER 退回订单申请请求:" + params);
        Map<String, Object> resultMap = HttpUtil.restPostMap(url, params);
        logger.info("<== ORDER 退回订单申请返回:" + resultMap);
        if (resultMap == null || resultMap.isEmpty()) {
            return fail(RestUtil.ERROR_INTERNAL_CODE, "订单系统通信失败");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> backOrderToCust(String orderNo, String reason) {
        AppOrdernoTypgrpRelation relation = appOrdernoTypgrpRelationRepository.findOne(orderNo);
        if (relation == null) {
            return fail("9004", "订单不存在");
        }

        if ("01".equals(relation.getTypGrp())) {
            return orderService.backOrderToCust(relation.getApplSeq(), reason);
        } else {
            return acquirerService.backOrderToCust(relation.getApplSeq(), reason);
        }
    }

    /**
     * 按照状态排列
     **/
    public void sortLoanList(List list) {
        HashMap<String, Integer> hm = new HashMap<String, Integer>();

        hm.put("1", 24);//1-待提交
        hm.put("2", 23);//2-待确认
        hm.put("3", 22);//3-被退回
        hm.put("22", 21);//22审批退回
        hm.put("04", 20);//04合同签订中
        hm.put("23", 19);//23合同签章中
        hm.put("01", 18);//01审批中
        hm.put("24", 17);//24放款审核中
        hm.put("05", 16);//05审批通过，等待放款
        hm.put("20", 15);//20待放款
        hm.put("WS", 14);//WS 待发货/待取货
        hm.put("06", 13);//06已放款

        hm.put("92", 12);//92-退货中
        hm.put("93", 11);//93-已退货
        hm.put("30", 10);//30-已付款待发货
        hm.put("31", 9);//31-已发货

        hm.put("27", 8);//27已通过
        hm.put("OD", 7);//OD 逾期
        hm.put("02", 6);//02	贷款被拒绝
        hm.put("25", 5);//25	额度申请被拒
        hm.put("03", 4);//03	贷款已取消
        hm.put("26", 3);//26额度申请已取消
        hm.put("AA", 2);//AA	取消放款
        if (list != null) {
            Collections.sort(list, new Comparator<Map<String, Object>>() {
                @Override
                public int compare(Map<String, Object> s1, Map<String, Object> s2) {
                    String outSts1 = String.valueOf(s1.get("outSts"));
                    String outSts2 = String.valueOf(s2.get("outSts"));
                    if (hm.containsKey(outSts1) && hm.containsKey(outSts2)) {
                        return hm.get(outSts2) - hm.get(outSts1);
                    } else {
                        return 1;
                    }
                }
            });
        }
    }

    /**
     * 待发货列表查询(只显示04合同签订中的订单)
     *
     * @param map
     * @return
     * @throws Exception
     */
    public List<Map<String, Object>> queryWaitSendOrder(Map<String, Object> map) throws Exception {
        List<Map<String, Object>> returnList = new ArrayList<>();

        String sysSts = "30";//30-已付款待发货
        //page pageSize未传，则默认查找全部
        Integer page = StringUtils.isEmpty(map.get("page")) ? 1 : (Integer) map.get("page");
        Integer pageSize = StringUtils.isEmpty(map.get("pageSize")) ? 1000 : (Integer) map.get("pageSize");
        String sourceForm = (String) map.get("sourceForm");
        String channel = (String) map.get("channel");
        String channelNo = (String) map.get("channelNo");

        StringBuffer urlStr = new StringBuffer(EurekaServer.ORDER + "/api/order/list?");
        urlStr.append("page=" + page).append("&size=" + pageSize).append("&sysSts=" + sysSts);
        if (!StringUtils.isEmpty(map.get("crtUsr"))) {
            urlStr.append("&salerCde=" + map.get("crtUsr"));
        }
        if (!StringUtils.isEmpty(map.get("idNo"))) {
            urlStr.append("&idNo=" + map.get("idNo"));
        }
        if (!StringUtils.isEmpty(map.get("applyDate"))) {
            urlStr.append("&crtDt=" + map.get("applyDate"));
        }

        logger.info("==>ORDER  url:" + urlStr);
        Map<String, Object> orderRetMap = HttpUtil.restGetMap(urlStr.toString(), HttpStatus.OK.value());
        logger.info("<==ORDER  返回结果:" + orderRetMap);
        if (RestUtil.isSuccess(orderRetMap)) {
            Map<String, Object> orderRetBodyMap = (Map<String, Object>) orderRetMap.get("body");
            List<Map<String, Object>> orderRetList = (List<Map<String, Object>>) orderRetBodyMap.get("orderMapList");
            List<Map<String, Object>> applseqList = new ArrayList<>();
            for (Map<String, Object> mapTem : orderRetList) {
                String applseqTem = (String) mapTem.get("applseq");
                if (!StringUtils.isEmpty(applseqTem)) {
                    Map<String, Object> applseqMap = new HashMap<>();
                    applseqMap.put("applSeq", applseqTem);
                    applseqList.add(applseqMap);
                }
            }
            if (applseqList.size() > 0) {
                //调用收单接口批量查询贷款信息
                Map<String, Object> requestMap = new HashMap<>();
                requestMap.put("list", applseqList);
                String url = EurekaServer.ACQUIRER + "/api/appl/batchQueryApplState";
                logger.info("==>ACQ 批量查询贷款状态url:" + url + ", 请求参数:" + requestMap);
                Map<String, Object> acqResponse = AcqUtil
                        .getAcqResponse(url, AcqTradeCode.BATCH_QUERY_APPL_STATE, channel, channelNo, null, null,
                                requestMap);
                logger.info("<==ACQ  返回结果:" + acqResponse);
                if (acqResponse == null || acqResponse.isEmpty()) {
                    throw new Exception("收单系统通信失败");
                }
                if (!CmisUtil.getIsSucceed(acqResponse)) {
                    throw new Exception(RestUtil.ERROR_INTERNAL_MSG);
                }
                Map<String, Object> responseMap = (Map<String, Object>) acqResponse.get("response");
                Map<String, Object> cmisBodyMap = (Map<String, Object>) responseMap.get("body");
                List<Map<String, Object>> cmisList = (List<Map<String, Object>>) cmisBodyMap.get("info");
                if (cmisList != null && cmisList.size() > 0) {
                    //批量查询订单状态
                    String batchQueryOrderStateUrl = EurekaServer.ORDER + "/api/order/batchQueryOrderState";
                    List<Map<String, Object>> requestList = new ArrayList<>();
                    cmisList.forEach(cmisMap -> {
                        Map<String, Object> applSeqMap = new HashMap<>();
                        applSeqMap.put("applSeq", cmisMap.get("applSeq"));
                        requestList.add(applSeqMap);
                    });
                    String requestOrderJson = JSONObject.valueToString(requestList);
                    Map<String, Object> orderResponseMap = batchQueryOrderState(batchQueryOrderStateUrl,
                            requestOrderJson);
                    if (isSuccess(orderResponseMap)) {

                        Map<String, Object> orderBodyMap = (Map<String, Object>) orderResponseMap.get("body");
                        List<Map<String, Object>> orderStateList = (List<Map<String, Object>>) orderBodyMap.get("list");
                        if (orderStateList.size() != cmisList.size()) {
                            throw new Exception("查询失败");
                        }
                        for (int i = 0; i < orderStateList.size(); i++) {
                            Map<String, Object> orderStateMap = orderStateList.get(i);
                            Map<String, Object> cmisMap = cmisList.get(i);
                            String outSts = StringUtils.isEmpty(cmisMap.get("outSts")) ?
                                    "" :
                                    (String) cmisMap.get("outSts");
                            String formTyp = StringUtils.isEmpty(orderStateMap.get("formTyp")) ?
                                    "" :
                                    (String) orderStateMap.get("formTyp");//订单类型
                            cmisMap.put("formTyp", formTyp);//订单类型 10-线下订单 20-线上订单 21-商户扫码录单 11-个人扫码录单
                            String formId = StringUtils.isEmpty(orderStateMap.get("formId")) ?
                                    "" :
                                    (String) orderStateMap.get("formId");//订单ID
                            cmisMap.put("orderNo", formId);
                            if ("01".equals(sourceForm)) {//商户版
                                cmisMap.put("crtUsr", map.get("crtUsr"));
                            }
                            DecimalFormat df = new DecimalFormat("#0.00");
                            BigDecimal totalnormint = StringUtils.isEmpty(cmisMap.get("totalnormint")) ?
                                    BigDecimal.ZERO :
                                    new BigDecimal(String.valueOf(cmisMap.get("totalnormint")));//总利息
                            BigDecimal totalfeeamt = StringUtils.isEmpty(cmisMap.get("totalfeeamt")) ?
                                    BigDecimal.ZERO :
                                    new BigDecimal(String.valueOf(cmisMap.get("totalfeeamt")));//总费用
                            cmisMap.put("fee", df.format(totalnormint.add(totalfeeamt)));//息费
                        }
                    } else {
                        throw new Exception("查询失败");
                    }
                    //只显示04合同签订中的订单,去掉审批状态为06已放款 24放款审核中 等状态的订单，
                    cmisList.removeIf(mapTem -> !"04".equals(mapTem.get("outSts")));
                    //处理返回状态
                    cmisList.forEach(mapTem -> mapTem.put("outSts",sysSts));
                    returnList.addAll(cmisList);
                }
            }
        } else {
            throw new Exception("查询失败");
        }
        logger.info("待发货列表查询 返回:" + returnList);
        return returnList;
    }

    public void cleanGoodsInfo(AppOrder appOrder) {
        appOrder.setGoodsBrand(null);
        appOrder.setGoodsCode(null);
        appOrder.setGoodsKind(null);
        appOrder.setGoodsModel(null);
        appOrder.setGoodsName(null);
        appOrder.setGoodsNum(null);
        appOrder.setGoodsPrice(null);
    }

}
