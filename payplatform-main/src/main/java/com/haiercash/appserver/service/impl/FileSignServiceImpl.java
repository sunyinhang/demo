package com.haiercash.appserver.service.impl;

import com.amazonaws.util.json.JSONObject;
import com.haiercash.appserver.service.CommonRepaymentPersonService;
import com.haiercash.common.config.EurekaServer;
import com.haiercash.appserver.service.AppOrderService;
import com.haiercash.common.service.BaseService;
import com.haiercash.appserver.service.CmisApplService;
import com.haiercash.appserver.service.FileSignService;
import com.haiercash.appserver.util.HttpClient;
import com.haiercash.appserver.util.MoneyTool;
import com.haiercash.appserver.util.sign.FileSignAgreement;
import com.haiercash.appserver.util.sign.FileSignAgreementRepository;
import com.haiercash.appserver.util.sign.FileSignConfig;
import com.haiercash.appserver.util.sign.FileSignUtil;
import com.haiercash.appserver.util.sign.SignType;
import com.haiercash.appserver.util.sign.ca.CARequest;
import com.haiercash.appserver.util.sign.ca.CAService;
import com.haiercash.appserver.web.CmisController;
import com.haiercash.common.data.AppContract;
import com.haiercash.common.data.AppContractRepository;
import com.haiercash.common.data.AppOrder;
import com.haiercash.common.data.AppOrdernoTypgrpRelation;
import com.haiercash.common.data.AppOrdernoTypgrpRelationRepository;
import com.haiercash.common.data.ContractAssInfo;
import com.haiercash.common.data.ContractAssInfoRepository;
import com.haiercash.common.data.ContractPdfFile;
import com.haiercash.common.data.ContractPdfFileRepository;
import com.haiercash.common.data.FTPBean;
import com.haiercash.common.data.FTPBeanListInfo;
import com.haiercash.common.data.UAuthCASignRequest;
import com.haiercash.common.data.UAuthCASignRequestRepository;
import com.haiercash.commons.cmis.CmisUtil;
import com.haiercash.commons.util.CommonProperties;
import com.haiercash.commons.util.HttpUtil;
import com.haiercash.commons.util.ResultHead;
import com.haiercash.commons.util.SignProperties;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.haiercash.appserver.util.sign.FileSignUtil.*;

/**
 * Sign Service.
 *
 * @author Liu qingxiang
 * @see FileSignServiceImpl
 * @since v1.1.0
 */
@Service
public class FileSignServiceImpl extends BaseService implements FileSignService {

    private static Log logger = LogFactory.getLog(FileSignServiceImpl.class);

    @Autowired
    private FileSignAgreementRepository fileSignAgreementRepository;
    @Autowired
    private ContractAssInfoRepository contractAssInfoRepository;
    @Autowired
    private AppContractRepository appContractRepository;
    @Autowired
    private UAuthCASignRequestRepository uAuthCASignRequestRepository;
    @Autowired
    private ContractPdfFileRepository contractPdfFileRepository;
    @Autowired
    private CmisApplService cmisApplService;
    @Autowired
    private AppOrderService appOrderService;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private CommonRepaymentPersonService commonRepaymentPersonService;
    @Autowired
    private AppOrdernoTypgrpRelationRepository appOrdernoTypgrpRelationRepository;

    private Boolean isCAServiceEnabled() {
        Object value = CommonProperties.get("other.CAServiceEnabled");
        if (value == null) {
            return true;
        } else {
            Boolean result = Boolean.valueOf(value.toString());
            if (!result) {
                logger.warn("CA服务已被禁用，签章步骤自动跳过");
            }
            return result;
        }
    }

    @Override
    public String sign(UAuthCASignRequest signRequest) {
        String result;
        if (SignType.credit.toString().equals(signRequest.getSignType()) ||
                SignType.register.toString().equals(signRequest.getSignType())
                || SignType.bcGrant.toString().equals(signRequest.getSignType())
                || SignType.ZXXYUOKO.toString().equals(signRequest.getSignType())) { //征信协议、注册协议、百城授权承诺函
            result = this.signCreditAgreement(signRequest.getOrderJson(), signRequest.getSignType(),
                    signRequest.getCommonFlag() == null ? "" : signRequest.getCommonFlag(),
                    signRequest.getCommonCustName() == null ? "" : signRequest.getCommonCustName(),
                    signRequest.getCommonCustCertNo() == null ? "" : signRequest.getCommonCustCertNo(),
                    signRequest.getSignCode());
        } else if (SignType.common.toString().equals(signRequest.getSignType())) { //共同还款人协议
            result = this.signCommonAgreement(signRequest);
        } else if (SignType.grant.toString().equals(signRequest.getSignType())) { // 变更银行卡授权书
            result = this.signBankCardGrantAgreement(signRequest);
        } else if (SignType.risEdCredit.toString().equals(signRequest.getSignType())) { // 提额征信授权书
            result = this.signRiseEdAgreement(signRequest);
        } else if ("HCFC-JK-YK-V2.0".equals(signRequest.getSignType())
                || SignType.DOUZIBUSINESS.toString().equals(signRequest.getSignType())
                || SignType.DOUZIPERSONAL.toString().equals(signRequest.getSignType())
                || SignType.HDJR_JKHT.toString().equals(signRequest.getSignType())) {
//            result = this.signAgreementByOrderJson(signRequest.getOrderJson(), signRequest.getSignType(),
//                    signRequest.getFlag(), false, signRequest.getSignCode());
            result = this.signAgreementByCmis(signRequest.getApplseq(), signRequest.getSignType(),
                    signRequest.getFlag(), false, signRequest.getSignCode());
        } else { //合同
            result = this.signAgreement(signRequest.getOrderJson(), signRequest.getSignType(),
                    signRequest.getFlag(), false, signRequest.getSignCode());
        }
        return result;
    }


    /**
     * init file sign config.
     *
     * @param config
     */
    private void initFileConfig(FileSignConfig config) {
        config.setParam("$applyType", "");
        config.setParam("$mtdCde", "");
        config.setParam("$rate", "");
        config.setParam("$feeRate", "");
        config.setParam("$applyManagerRate", "");
    }

    @Override
    public String signAgreement(String orderJson, String signType, String contractFlag, boolean isManual,
                                String signCode) {
        FileSignConfig config = new FileSignConfig();
        //从数据库读取模板配置信息
        logger.debug("orderJson=" + orderJson + " ,signType=" + signType);
        FileSignAgreement fileSignAgreement = this.fileSignAgreementRepository.findBySignType(signType);
        logger.debug("fileSignAgreement is null =" + (fileSignAgreement == null));
        if (null == fileSignAgreement) {
            return "";
        }
        config.setTemplateFileName(fileSignAgreement.getTemplateFileName());
        config.setTemplateContractNo(fileSignAgreement.getTempSeriNum());
        config.setUserPage(fileSignAgreement.getUserPage());
        config.setUserX(fileSignAgreement.getUserX());
        config.setUserY(fileSignAgreement.getUserY());
        config.setCoPage(fileSignAgreement.getCoPage());
        config.setCoX(fileSignAgreement.getCoX());
        config.setCoY(fileSignAgreement.getCoY());
        config.setUseCoSign(fileSignAgreement.getUseCoSign());

        //获取appOrder对象
        Map<String, Object> orderMap = HttpUtil.json2Map(orderJson);
        Map<String, Object> appOrder = HttpUtil.json2Map(orderMap.get("order").toString());
        logger.debug("appOrder is null=" + (appOrder == null));
        if (null == appOrder) {
            return "";
        }

        String fileNameNoExt = String.format("%s_%s_%s", "app", appOrder.get("idNo"), signType);
        config.setFileNameNoExt(fileNameNoExt);
        //通过流水号获取影像上传路径
        if (null == appOrder.get("applseq") || "".equals(appOrder.get("applseq"))) {
            logger.debug("订单流水号为空!");
            return "";
        }

        String path = getPath((String) appOrder.get("applseq"));
        logger.debug("获取签章路径：path=" + path);
        config.setFtpPath(path);
        config.setParam("applseq", (String) appOrder.get("applseq"));
        config.setParam("custNo", (String) appOrder.get("custNo"));
        config.setParam("signCode", signCode);
        config.setParam("signType", signType);
        config.setParam("contractType", "1");
        config.setParam("$name", appOrder.get("custName") == null ? "" : (String) appOrder.get("custName"));
        config.setParam("$userIdentity", appOrder.get("idNo") == null ? "" : (String) appOrder.get("idNo"));
        Calendar rightNow = Calendar.getInstance();
        config.setParam("$year", String.valueOf(rightNow.get(Calendar.YEAR)));
        config.setParam("$month", String.valueOf(rightNow.get(Calendar.MONTH) + 1));
        config.setParam("$day", String.valueOf(rightNow.get(Calendar.DAY_OF_MONTH)));
        config.setUserName(appOrder.get("custName") == null ? "" : (String) appOrder.get("custName"));
        config.setUserIdentity(appOrder.get("idNo") == null ? "" : (String) appOrder.get("idNo"));
        config.setParam("$contractNo", "");//合同编号：暂时没有数据
        config.setParam("$custName",
                appOrder.get("custName") == null ? "" : (String) appOrder.get("custName"));          //借款人(甲方)姓名
        config.setParam("$identifyNo",
                appOrder.get("idNo") == null ? "" : (String) appOrder.get("idNo"));                //身份证号码
        if (appOrder.get("repay_acc_mobile") != null) {
            config.setParam("$custMobile", appOrder.get("repay_acc_mobile").toString());
        } else {
            config.setParam("$custMobile",
                    appOrder.get("indivMobile") == null ? "" : (String) appOrder.get("indivMobile"));  //联系电话
        }
        config.setParam("$cooprName",
                appOrder.get("cooprName") == null ? "" : (String) appOrder.get("cooprName"));       //门店名称
        config.setParam("$applyTnr",
                appOrder.get("applyTnr") == null ? "" : (String) appOrder.get("applyTnr"));          //借款期限
        config.setParam("$purpose",
                appOrder.get("purpose") == null ? "" : (String) appOrder.get("purpose"));             //贷款用途

        //获取门店所在市cityName   (crm:/app/crm/cust/getStore   门店代码:cooprCde)
        if (!StringUtils.isEmpty(appOrder.get("cooprCde"))) {
            String cooprInfoUrl =
                    EurekaServer.CRM + "/app/crm/cust/getStore?storeNo=" + (String) appOrder
                            .get("cooprCde");
            String cooprInfoJson = HttpUtil.restGet(cooprInfoUrl);
            logger.debug("获取门店信息cooprInfoJson=" + cooprInfoJson);
            if (!StringUtils.isEmpty(cooprInfoJson)) {
                cooprInfoJson = cooprInfoJson.replaceAll("null", "\"\"");
                Map<String, Object> cooprInfoMap = HttpUtil.json2Map(cooprInfoJson);
                if (HttpUtil.isSuccess(cooprInfoMap)) {
                    Map<String, Object> mapBody = HttpUtil.json2Map(cooprInfoMap.get("body").toString());
                    config.setParam("$cooprCityName",
                            (String) mapBody.get("cityName") == null ? "" : (String) mapBody.get("cityName"));
                } else {
                    config.setParam("$cooprCityName", "");
                }
            } else {
                config.setParam("$cooprCityName", "");
            }
        } else {
            config.setParam("$cooprCityName", "");
        }

        logger.info("调用crm接口获取客户扩展信息");
        //获取居住地址、邮箱、邮编       /app/crm/cust/getCustExtInfo
        String pageName = "";   //单位信息:dwInfo、居住信息:jzInfo、个人信息:grInfo、为空：所有信息
        String custInfoUrl = EurekaServer.CRM + "/app/crm/cust/getAllCustExtInfo?custNo=" + appOrder
                .get("custNo") ;
        String custInfoUrlJson = HttpUtil.restGet(custInfoUrl);
        logger.debug("客户拓展信息返回结果:" + custInfoUrlJson);
        if (StringUtils.isEmpty(custInfoUrlJson)) {
            logger.debug("查询个人信息为空!custInfoUrlJson=" + custInfoUrlJson);
            return "";
        }
        custInfoUrlJson = custInfoUrlJson.replaceAll("null", "\"\"");
        Map<String, Object> custInfoMap = HttpUtil.json2Map(custInfoUrlJson);
        if (HttpUtil.isSuccess(custInfoMap)) {
            Map<String, Object> mapBody = HttpUtil.json2Map(custInfoMap.get("body").toString());
            //获取省市区名称
            String address = "";
            if (mapBody.containsKey("liveProvinceName")) {
                address += (String) mapBody.get("liveProvinceName") == null ?
                        "" :
                        (String) mapBody.get("liveProvinceName");
            }
            if (mapBody.containsKey("liveCityName")) {
                if (!mapBody.get("liveProvinceName").equals(mapBody.get("liveCityName"))) {
                    address += (String) mapBody.get("liveCityName") == null ?
                            "" :
                            (String) mapBody.get("liveCityName");
                }
            }
            if (mapBody.containsKey("liveAreaName")) {
                address += (String) mapBody.get("liveAreaName") == null ? "" : (String) mapBody.get("liveAreaName");
            }
            config.setParam("$custAddress",
                    address + (mapBody.get("liveAddr") == null ? "" : (String) mapBody.get("liveAddr")));     //居住地址
            config.setParam("$email",
                    mapBody.get("email") == null ? "" : (String) mapBody.get("email"));                 //邮箱
            config.setParam("$liveZip",
                    mapBody.get("liveZip") == null ? "" : (String) mapBody.get("liveZip"));           //邮编
        } else {
            config.setParam("$custAddress", "");
            config.setParam("$email", "");
            config.setParam("$liveZip", "");
        }

        logger.info("处理借款金额和审批金额");
        //合同金额显示：
        String applyAmt = amtConvert((String) appOrder.get("applyAmt"));//借款金额
        String apprvAmt = amtConvert((String) appOrder.get("apprvAmt"));//审批金额
        if ("".equals(apprvAmt) || null == apprvAmt || "null".equals(apprvAmt)) {
            config.setParam("$applyAmtSmall", applyAmt == null ? "" : applyAmt);
            if (null != applyAmt && !"".equals(applyAmt)) {
                config.setParam("$applyAmtBig", MoneyTool.change(Double.parseDouble(applyAmt)));
            } else {
                config.setParam("$applyAmtBig", "");
            }
        } else {
            config.setParam("$applyAmtSmall", apprvAmt);
            config.setParam("$applyAmtBig", MoneyTool.change(Double.parseDouble(apprvAmt)));
        }
        config.setParam("$fstPaySmall", appOrder.get("fstPay") == null ?
                "" :
                amtConvert((String) appOrder.get("fstPay")));           //fstPay首付金额
        if (null == appOrder.get("fstPay") || "".equals(appOrder.get("fstPay"))) {
            config.setParam("$fstPayBig", "");
        } else {
            config.setParam("$fstPayBig",
                    MoneyTool.change(Double.parseDouble(amtConvert((String) appOrder.get("fstPay")))));
        }

        logger.info("获取放款账号信息");
        //贷款类型为耐用消费品贷款
        if ("01".equals(appOrder.get("typGrp"))) {
            String url = EurekaServer.CRM + "/pub/crm/cust/getMerchInfoByMerchNo" + "?merchNo="
                    + appOrder.get("merchNo");
            String json = HttpUtil.restGet(url);
            if (StringUtils.isEmpty(json)) {
                logger.debug("查询商户为空!json=" + json);
                return "";
            }
            json = json.replaceAll("null", "\"\"");
            Map<String, Object> map = HttpUtil.json2Map(json);
            //设置还款卡号
            if (HttpUtil.isSuccess(map)) {
                Map<String, Object> mapBody = HttpUtil.json2Map(map.get("body").toString());
                //获取贷款品种代码
                String typCde = String.valueOf(appOrder.get("typCde"));
                String typCdeUrl = EurekaServer.CMISPROXY + "/api/pLoanTyp/find?typCde=" + typCde;
                String typCdeJson = HttpUtil.restGet(typCdeUrl);
                String dnTyp = "";//固定放款途径，如果为9001则放款到商户内转账号
                if (StringUtils.isEmpty(json)) {
                    logger.info("CMIS==>贷款品种详情接口查询失败！");
                } else {
                    Map<String, Object> typCdemap = HttpUtil.json2Map(typCdeJson);
                    //获取固定放款途径	dnTyp
                    dnTyp = String.valueOf(typCdemap.get("dnTyp"));
                }
                if ("9001".equals(dnTyp)) {
                    //账户名称取默认的开户账户名（外转）；如果没有配置外转账号信息，则取商户名称
                    String accName = mapBody.get("acctName") == null ? "" : mapBody.get("acctName").toString();
                    if (StringUtils.isEmpty(accName)) {
                        accName = String.valueOf(mapBody.get("merchChName"));
                    }
                    config.setParam("$applAcNam", accName);   //放款账号户名
                    config.setParam("$accBankCde", "0000");    //放款开户银行代码
                    config.setParam("$accBankName", "海尔集团财务有限责任公司");  //放款开户银行名
                    config.setParam("$accAcBchCde", "0000");      //放款开户行支行代码
                    //若为9001，则放款至商户内转账号
                    config.setParam("$applCardNo", (String) mapBody.get("inFinaAcct") == null ?
                            "" :
                            (String) mapBody.get("inFinaAcct"));      //放款卡号
                } else {
                    config.setParam("$applAcNam",
                            mapBody.get("acctName") == null ? "" : (String) mapBody.get("acctName"));   //放款账号户名
                    config.setParam("$accBankCde",
                            mapBody.get("bankCde") == null ? "" : (String) mapBody.get("bankCde"));    //放款开户银行代码
                    config.setParam("$accBankName",
                            mapBody.get("bankName") == null ? "" : (String) mapBody.get("bankName"));  //放款开户银行名
                    config.setParam("$accAcBchCde",
                            mapBody.get("bchCde") == null ? "" : (String) mapBody.get("bchCde"));      //放款开户行支行代码
                    config.setParam("$applCardNo",
                            mapBody.get("acctNo") == null ? "" : (String) mapBody.get("acctNo"));      //放款卡号

                }
            }
        } else {//02：一般消费贷款
            if (null == appOrder.get("applAcNam") || "".equals(appOrder.get("applAcNam"))) {
                String url =
                        EurekaServer.CRM + "/app/crm/cust/getDefaultBankCard?custNo=" + appOrder
                                .get("custNo");
                String json = HttpUtil.restGet(url);
                if (StringUtils.isEmpty(json)) {
                    logger.debug("查询银行卡信息为空!json=" + json);
                    return "";
                }
                json = json.replaceAll("null", "\"\"");
                Map<String, Object> map = HttpUtil.json2Map(json);
                if (HttpUtil.isSuccess(map)) {
                    Map<String, Object> mapBody = HttpUtil.json2Map(map.get("body").toString());
                    config.setParam("$applAcNam",
                            mapBody.get("custName") == null ? "" : (String) mapBody.get("custName"));     //放款账号户名
                    config.setParam("$accBankCde",
                            mapBody.get("bankCode") == null ? "" : (String) mapBody.get("bankCode"));     //放款开户银行代码
                    config.setParam("$accBankName",
                            mapBody.get("bankName") == null ? "" : (String) mapBody.get("bankName"));    //放款开户银行名
                    config.setParam("$accAcBchCde",
                            mapBody.get("accBchCde") == null ? "" : (String) mapBody.get("accBchCde"));   //放款开户行支行代码
                    config.setParam("$applCardNo",
                            mapBody.get("cardNo") == null ? "" : (String) mapBody.get("cardNo"));       //放款卡号
                }
            } else {
                config.setParam("$applAcNam",
                        appOrder.get("applAcNam") == null ? "" : (String) appOrder.get("applAcNam"));       //放款账号户名
                config.setParam("$accBankCde",
                        appOrder.get("accBankCde") == null ? "" : (String) appOrder.get("accBankCde"));    //放款开户银行代码
                config.setParam("$accBankName",
                        appOrder.get("accBankName") == null ? "" : (String) appOrder.get("accBankName"));//放款开户银行名
                config.setParam("$accAcBchCde",
                        appOrder.get("accAcBchCde") == null ? "" : (String) appOrder.get("accAcBchCde"));//放款开户行支行代码
                config.setParam("$applCardNo",
                        appOrder.get("applCardNo") == null ? "" : (String) appOrder.get("applCardNo"));   //放款卡号
            }
        }

        logger.info("处理利率及各种费率");
        //通过还款方式查询合同中所对应的选择条数
        List<AppContract> contractList = this.appContractRepository
                .findByContType((String) appOrder.get("typLevelTwo"));
        List<ContractAssInfo> contractAssInfoList = new ArrayList<>();
        if (contractList.size() > 1) {
            //利率、手续费率、管理费率
            contractAssInfoList = this.contractAssInfoRepository
                    .findByPayMtdAndTypLevelTwoAndTypCde((String) appOrder.get("payMtd"),
                            (String) appOrder.get("typLevelTwo"), (String) appOrder.get("typCde"));
        } else if (contractList.size() == 1) {
            contractAssInfoList = this.contractAssInfoRepository
                    .findByPayMtdAndTypLevelTwo((String) appOrder.get("payMtd"), (String) appOrder.get("typLevelTwo"));
        }
        if (null == contractAssInfoList || contractAssInfoList.size() == 0) {
            this.initFileConfig(config);
        } else {
            if (contractAssInfoList.size() == 1) {
                ContractAssInfo contractAssInfo = contractAssInfoList.get(0);
                config.setParam("$applyType",
                        contractAssInfo.getContractMtdType() == null ? "" : contractAssInfo.getContractMtdType());
                config.setParam("$mtdCde",
                        contractAssInfo.getContractMtdType() == null ? "" : contractAssInfo.getContractMtdType());
                config.setParam("$rate", contractAssInfo.getApplyRate() == null ? "" : contractAssInfo.getApplyRate());
                config.setParam("$feeRate", contractAssInfo.getFeeRate() == null ? "" : contractAssInfo.getFeeRate());
                config.setParam("$applyManagerRate",
                        contractAssInfo.getApplyManagerRate() == null ? "" : contractAssInfo.getApplyManagerRate());
            } else if (contractAssInfoList.size() > 1) {
                List<ContractAssInfo> AssInfoList = new ArrayList<>();
                if (contractList.size() == 1) {
                    AssInfoList = this.contractAssInfoRepository
                            .findByPayMtdAndTypLevelTwoAndApplyTnr((String) appOrder.get("payMtd"),
                                    (String) appOrder.get("typLevelTwo"), (String) appOrder.get("applyTnr"));
                } else if (contractList.size() > 1) {
                    AssInfoList = this.contractAssInfoRepository
                            .findByPayMtdAndTypLevelTwoAndTypCdeAndApplyTnr((String) appOrder.get("payMtd"),
                                    (String) appOrder.get("typLevelTwo"), (String) appOrder.get("typCde"),
                                    (String) appOrder.get("applyTnr"));
                }
                if (null != AssInfoList) {
                    if (AssInfoList.size() == 1) {
                        ContractAssInfo contractAssInfo = AssInfoList.get(0);
                        config.setParam("$applyType", contractAssInfo.getContractMtdType() == null ?
                                "" :
                                contractAssInfo.getContractMtdType());
                        config.setParam("$mtdCde", contractAssInfo.getContractMtdType() == null ?
                                "" :
                                contractAssInfo.getContractMtdType());
                        config.setParam("$rate",
                                contractAssInfo.getApplyRate() == null ? "" : contractAssInfo.getApplyRate());
                        config.setParam("$feeRate",
                                contractAssInfo.getFeeRate() == null ? "" : contractAssInfo.getFeeRate());
                        config.setParam("$applyManagerRate", contractAssInfo.getApplyManagerRate() == null ?
                                "" :
                                contractAssInfo.getApplyManagerRate());
                    } else if (AssInfoList.size() > 1) {
                        //查询是否有共同还款人
                        AppOrdernoTypgrpRelation relation = appOrdernoTypgrpRelationRepository.findOne((String) appOrder.get("order_no"));
                        if (relation == null) {
                            return "";
                        }
                        Map<String, Object> countPerson = commonRepaymentPersonService.countCommonRepaymentPerson(relation.getApplSeq());
                        if (!HttpUtil.isSuccess(countPerson)) {
                            return "";
                        }
                        int count = (int) ((Map<String, Object>) countPerson.get("body")).get("count");
                        String hasComRepay;
                        if (count == 0) {//没有共同还款人
                            hasComRepay = "0";
                        } else {//有共同还款人
                            hasComRepay = "1";
                        }
                        ContractAssInfo contractAssInfo = null;
                        if (contractList.size() == 1) {
                            contractAssInfo = this.contractAssInfoRepository
                                    .findByHasComRepayOne((String) appOrder.get("payMtd"),
                                            (String) appOrder.get("typLevelTwo"), (String) appOrder.get("applyTnr"),
                                            hasComRepay);
                        } else if (contractList.size() > 1) {
                            contractAssInfo = this.contractAssInfoRepository
                                    .findByHasComRepayMore((String) appOrder.get("payMtd"),
                                            (String) appOrder.get("typLevelTwo"), (String) appOrder.get("typCde"),
                                            (String) appOrder.get("applyTnr"), hasComRepay);
                        }

                        if (null != contractAssInfo) {
                            config.setParam("$applyType", contractAssInfo.getContractMtdType() == null ?
                                    "" :
                                    contractAssInfo.getContractMtdType());
                            config.setParam("$mtdCde", contractAssInfo.getContractMtdType() == null ?
                                    "" :
                                    contractAssInfo.getContractMtdType());
                            config.setParam("$rate",
                                    contractAssInfo.getApplyRate() == null ? "" : contractAssInfo.getApplyRate());
                            config.setParam("$feeRate",
                                    contractAssInfo.getFeeRate() == null ? "" : contractAssInfo.getFeeRate());
                            config.setParam("$applyManagerRate", contractAssInfo.getApplyManagerRate() == null ?
                                    "" :
                                    contractAssInfo.getApplyManagerRate());
                        }
                    } else {
                        this.initFileConfig(config);
                    }
                } else {
                    this.initFileConfig(config);
                }
            }
        }

        logger.info("查询剩余可用额度");
        //获取剩余可用额度(目前只有 个人消费借款合同-自主支付版本-支用时使用.docx 合同用)
        String certType = "20";  //身份证：20
        String idNo = (String) appOrder.get("idNo");//证件号码
        // String EdCheckUrl = CommonProperties.get("address.gateUrl") + "/app/appserver/getEdCheck?idTyp=" + certType + "&idNo=" + idNo;
        // Map<String, Object> EdCheckMap = HttpUtil.restGetMap(EdCheckUrl);
        //if (HttpUtil.isSuccess(EdCheckMap)) {
        Map<String, Object> EdCheckMap = cmisApplService.getEdCheck(certType, idNo, super.getToken());
        logger.info(EdCheckMap);
        ResultHead head = (ResultHead) EdCheckMap.get("head");
        String flag = head.getRetFlag();
        String msg = head.getRetMsg();
        if (Objects.equals(flag, "00000")) {
            Map<String, Object> edCheckBody = (Map<String, Object>) EdCheckMap.get("body");
            String edAmt = String.valueOf(edCheckBody.get("crdNorAvailAmt")) == null ?
                    "" :
                    String.valueOf(edCheckBody.get("crdNorAvailAmt"));//自主支付可用额度金额
            String applAmt = config.getParams().get("$applyAmtSmall");
            //Double amt = Double.parseDouble(edAmt) - Double.parseDouble(applAmt);
            Double amt = new BigDecimal(edAmt).subtract(new BigDecimal(applAmt)).doubleValue();
            config.setParam("$crdNorAvailAmt", String.valueOf(amt) == null ? "" : amtConvert(String.valueOf(amt)));
            if (!StringUtils.isEmpty(String.valueOf(amt))) {
                config.setParam("$bigCrdNorAvailAmt",
                        MoneyTool.change(Double.parseDouble(amtConvert(String.valueOf(amt)))));
            }
        } else {
            config.setParam("$crdNorAvailAmt", "");
            config.setParam("$bigCrdNorAvailAmt", "");
        }

        logger.info("处理还款银行卡信息");
        //还款银行卡信息
        if (null == appOrder.get("repayApplCardNo") || "".equals(appOrder.get("repayApplCardNo"))) {
            //还款银行卡信息(查询CRM默认还款银行卡信息)
            String url = EurekaServer.CRM + "/app/crm/cust/getDefaultBankCard?custNo=" + appOrder
                    .get("custNo");
            String json = HttpUtil.restGet(url);
            if (StringUtils.isEmpty(json)) {
                logger.debug("查询默认还款卡为空!json=" + json);
                return "";
            }
            json = json.replaceAll("null", "\"\"");
            Map<String, Object> map = HttpUtil.json2Map(json);
            if (HttpUtil.isSuccess(map)) {
                Map<String, Object> mapBody = HttpUtil.json2Map(map.get("body").toString());
                config.setParam("$repayApplAcNam",
                        mapBody.get("custName") == null ? "" : (String) mapBody.get("custName"));//还款账号户名
                config.setParam("$repayAccBankName",
                        mapBody.get("bankName") == null ? "" : (String) mapBody.get("bankName"));//还款开户银行名
                config.setParam("$repayApplCardNo",
                        mapBody.get("cardNo") == null ? "" : (String) mapBody.get("cardNo"));//还款卡号
            }
        } else {
            config.setParam("$repayApplAcNam",
                    appOrder.get("repayApplAcNam") == null ? "" : (String) appOrder.get("repayApplAcNam"));//还款账号户名
            config.setParam("$repayAccBankName",
                    appOrder.get("repayAccBankName") == null ? "" : (String) appOrder.get("repayAccBankName"));//还款开户银行名
            config.setParam("$repayApplCardNo",
                    appOrder.get("repayApplCardNo") == null ? "" : (String) appOrder.get("repayApplCardNo"));//还款卡号
        }

        config.setParam("$accName", appOrder.get("custName") == null ? "" : (String) appOrder.get("custName"));//甲方签名
        config.setParam("$lenderName", "海尔消费金融有限公司");//乙方签名
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        config.setParam("$date", format.format(new Date()));//日期
        logger.info("合同参数抓取结束");

        //转换上传
        if (!makeAndUpload(config)) {
            return "";
        } else {
            //签名签章
            String str = caSign(config);
            if ("".equals(str)) {
                return "";
            }

            if (!isManual) {
                /**
                 * 2016年9月26日 10:49:26
                 * 取消渠道进件处理，统一放至控制层合约提交签约接口统一处理，此处只做签章处理。
                 */
                /**
                 Map<String, Object> parameterMap = new HashMap<>();
                 parameterMap.put("orderNo", (String) appOrder.get("orderNo"));
                 // Map<String, Object> resultMap = HttpUtil.restPostMap(url, parameterMap);
                 Map<String, Object> resultMap = appOrderService.subSignContractQdjj(parameterMap);
                 if (resultMap == null) {
                 logger.info("信贷系统保存贷款申请失败：返回结果为null，未知错误");
                 } else if (!CmisUtil.getIsSucceed(resultMap)) {

                 logger.info("信贷系统保存贷款申请失败：" + CmisUtil.getErrMsg(resultMap));
                 }
                 Map<String, Object> bodyMap = CmisUtil.getBody(resultMap);
                 */
                //                if (resultMap == null || (!CmisUtil.getIsSucceed(resultMap) && !RestUtil.isSuccess(resultMap))) {
                //                    logger.error("信贷系统保存贷款申请失败：" + CmisUtil.getErrMsg(resultMap));
                //                    //                    return "";
                //                }
                //提交合同
                /**
                 HashMap<String, Object> mapSubmit = new HashMap<>();
                 mapSubmit.put("applSeq", config.getParams().get("applseq"));
                 if ("".equals(contractFlag) || null == contractFlag) {
                 contractFlag = "1";
                 }
                 mapSubmit.put("flag", contractFlag); // 0：贷款取消 1:申请提交 2：合同提交

                 logger.debug("--------------contractFlag=" + contractFlag + ",status=" + appOrder.get("status") + ",mapSubmit=" + mapSubmit);
                 if (("1".equals(contractFlag) && "4".equals(appOrder.get("status"))) || "2".equals(contractFlag)) {
                 Map<String, Object> responseMap = CmisUtil.getCmisResponse(CmisTradeCode.TRADECODE_DK_CANCEL, null, mapSubmit);// 返回审批状态
                 logger.debug("-------合同提交返回：responseMap=" + new JSONObject(responseMap).toString());
                 }
                 */;
            }
            return str;
        }
    }

    @Override
    public String signCreditAgreement(String orderJson, String type, String commonFlag, String commonCustName,
                                      String commonCustCertNo, String signCode) {
        String signType = type;
        if (SignType.credit.toString().equals(type)) {
            type = "ZXXY";
        }
        if (SignType.register.toString().equals(type)) {
            type = "ZCXY";
        }
        if (SignType.bcGrant.toString().equals(type)) {
            type = "BCGRANT";
        }
        if (SignType.ZXXYUOKO.toString().equals(type)) {
            type = "ZXXYUOKO";
        }
       /* if (SignType.mkledsyxz.toString().equals(type)) {
            type = "MKLEDSYXZ";
        }*/
        FileSignAgreement fileSignAgreement = this.fileSignAgreementRepository.findBySignType(type);
        if (null == fileSignAgreement) {
            return "";
        }
        FileSignConfig config = new FileSignConfig();
        config.setTemplateFileName(fileSignAgreement.getTemplateFileName());
        config.setTemplateContractNo(fileSignAgreement.getTempSeriNum());
        config.setUserPage(fileSignAgreement.getUserPage());
        config.setUserX(fileSignAgreement.getUserX());
        config.setUserY(fileSignAgreement.getUserY());
        config.setCoPage(fileSignAgreement.getCoPage());
        config.setCoX(fileSignAgreement.getCoX());
        config.setCoY(fileSignAgreement.getCoY());
        config.setUseCoSign(fileSignAgreement.getUseCoSign());

        //获取appOrder对象
        Map<String, Object> orderMap = HttpUtil.json2Map(orderJson);
        Map<String, Object> appOrder = HttpUtil.json2Map(orderMap.get("order").toString());
        if (null == appOrder) {
            return "";
        }
        //征信协议pdf文件名命名规则:
        String fileNameNoExt = "";
        if ("ZXXY".equals(type) || "ZXXYUOKO".equals(type)) {
            if ("1".equals(commonFlag)) {
                fileNameNoExt = String.format("%s_%s_%s", "app", commonCustCertNo, "credit");
            } else {
                fileNameNoExt = String.format("%s_%s_%s", "app", appOrder.get("idNo"), "credit");
            }
        } else if ("ZCXY".equals(type)) {
            fileNameNoExt = String.format("%s_%s_%s", "app", appOrder.get("idNo"), "register");
        } else if ("BCGRANT".equals(type)) {
            fileNameNoExt = String.format("%s_%s_%s", "app", appOrder.get("idNo"), "bcgrant");
        }
//        else if ("MKLEDSYXZ".equals(type)) {
//            fileNameNoExt = String.format("%s_%s_%s", "app", appOrder.get("idNo"), "mkledsyxz");
//        }
        config.setFileNameNoExt(fileNameNoExt);

        //通过流水号获取影像上传路径
        if (null == appOrder.get("applseq") || "".equals(appOrder.get("applseq"))) {
            logger.debug("订单流水号为空!");
            return "";
        }
        String path = getPath((String) appOrder.get("applseq"));
        logger.debug("获取签章路径：path=" + path);
        config.setFtpPath(path);
        config.setParam("applseq", (String) appOrder.get("applseq"));
        config.setParam("custNo", appOrder.get("custNo") == null ? "" : (String) appOrder.get("custNo"));
        config.setParam("signCode", signCode);
        config.setParam("signType", signType);
        config.setParam("contractType", "0");
        if ("ZXXY".equals(type) && "1".equals(commonFlag)) {  //共同还款人征信协议
            config.setUserName(commonCustName);
            config.setUserIdentity(commonCustCertNo);
            config.setParam("$custname", commonCustName);
            config.setParam("$custcode", commonCustCertNo);
        } else {
            if(StringUtils.isEmpty(appOrder.get("applseq"))){
                config.setUserName((String) appOrder.get("custName") == null ? "" : (String) appOrder.get("custName"));
                config.setUserIdentity((String) appOrder.get("idNo") == null ? "" : (String) appOrder.get("idNo"));
                config.setParam("$custname",
                        (String) appOrder.get("custName") == null ? "" : (String) appOrder.get("custName"));
                config.setParam("$custcode", (String) appOrder.get("idNo") == null ? "" : (String) appOrder.get("idNo"));
            }else{
                AppOrder order = appOrderService.getAppOrderFromCmis(appOrder.get("applseq").toString(), "0");
                config.setUserName((String) order.getCustName() == null ? "" : (String) order.getCustName());
                config.setUserIdentity((String) order.getIdNo() == null ? "" : (String) order.getIdNo());
                config.setParam("$custname",
                        (String) order.getCustName() == null ? "" : (String) order.getCustName());
                config.setParam("$custcode", (String) order.getIdNo() == null ? "" : (String) order.getIdNo());
            }
        }
        Calendar rightNow = Calendar.getInstance();
        config.setParam("$year", String.valueOf(rightNow.get(Calendar.YEAR)));
        config.setParam("$month", String.valueOf(rightNow.get(Calendar.MONTH) + 1));
        config.setParam("$day", String.valueOf(rightNow.get(Calendar.DAY_OF_MONTH)));

        //转换上传
        if (!makeAndUpload(config)) {
            return "";
        } else {
            //签名签章
            String str = caSign(config);
            return str;
        }
    }

    @Override
    public String signCommonAgreement(UAuthCASignRequest signRequest) {
        FileSignAgreement fileSignAgreement = this.fileSignAgreementRepository.findBySignType("GTHKR");//共同还款人协议协议
        if (null == fileSignAgreement) {
            return "";
        }
        FileSignConfig config = new FileSignConfig();
        config.setTemplateFileName(fileSignAgreement.getTemplateFileName());
        config.setTemplateContractNo(fileSignAgreement.getTempSeriNum());
        config.setUserPage(fileSignAgreement.getUserPage());
        config.setUserX(fileSignAgreement.getUserX());
        config.setUserY(fileSignAgreement.getUserY());
        config.setCoPage(fileSignAgreement.getCoPage());
        config.setCoX(fileSignAgreement.getCoX());
        config.setCoY(fileSignAgreement.getCoY());
        config.setUseCoSign(fileSignAgreement.getUseCoSign());

        //获取appOrder对象
        Map<String, Object> orderMap = (Map<String, Object>) HttpUtil.json2Map(signRequest.getOrderJson());
        Map<String, Object> appOrder = (Map<String, Object>) HttpUtil.json2Map(orderMap.get("order").toString());
        if (null == appOrder) {
            return "";
        }

        //共同还款人协议pdf文件名命名规则:
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
        String fileNameNoExt = String.format("%s_%s_%s", "app", (String) signRequest.getCommonCustCertNo(), "common");
        config.setFileNameNoExt(fileNameNoExt);
        //通过流水号获取影像上传路径
        if (null == appOrder.get("applseq") || "".equals(appOrder.get("applseq"))) {
            logger.debug("订单流水号为空!");
            return "";
        }
        String path = getPath((String) appOrder.get("applseq"));
        logger.debug("获取签章路径：path=" + path);
        config.setFtpPath(path);
        config.setParam("custNo", (String) appOrder.get("custNo"));
        config.setParam("signCode", signRequest.getSignCode());
        config.setParam("signType", signRequest.getSignType());
        config.setParam("contractType", "0");
        config.setUserName(signRequest.getCommonCustName() == null ? "" : signRequest.getCommonCustName());
        config.setUserIdentity(signRequest.getCommonCustCertNo() == null ? "" : signRequest.getCommonCustCertNo());
        config.setParam("applseq", (String) appOrder.get("applseq"));
        //模板替换参数
        config.setParam("$custName",
                (String) appOrder.get("custName") == null ? "" : (String) appOrder.get("custName"));//借款人客户姓名
        config.setParam("$custVerifyNo",
                (String) appOrder.get("idNo") == null ? "" : (String) appOrder.get("idNo"));//借款人身份证号
        config.setParam("$typDesc",
                (String) appOrder.get("typDesc") == null ? "" : (String) appOrder.get("typDesc"));//贷款品种名称
        if (null == appOrder.get("apprvAmt") || "".equals(appOrder.get("apprvAmt"))) {
            config.setParam("$applyAmt", (String) appOrder.get("applyAmt") == null ?
                    "" :
                    amtConvert((String) appOrder.get("applyAmt")));//贷款金额(小写)
            config.setParam("$applyBigAmt", (String) appOrder.get("applyAmt") == null ?
                    "" :
                    MoneyTool.change(Double.parseDouble(amtConvert((String) appOrder.get("applyAmt")))));//贷款金额(大写)
        } else {
            config.setParam("$applyAmt", (String) appOrder.get("apprvAmt") == null ?
                    "" :
                    amtConvert((String) appOrder.get("apprvAmt")));//贷款金额(小写)  贷款金额为审批金额
            config.setParam("$applyBigAmt", (String) appOrder.get("apprvAmt") == null ?
                    "" :
                    MoneyTool.change(Double.parseDouble(amtConvert((String) appOrder.get("apprvAmt")))));//贷款金额(大写)
        }
        config.setParam("$applyTnr",
                (String) appOrder.get("applyTnr") == null ? "" : (String) appOrder.get("applyTnr"));//贷款期限
        config.setParam("$applCde",
                (String) appOrder.get("applCde") == null ? "" : (String) appOrder.get("applCde"));//贷款编号
        config.setParam("$commonCustName", signRequest.getCommonCustName());//共同还款人姓名
        config.setParam("$commonCustVerifyNo", signRequest.getCommonCustCertNo());//共同还款人身份证号
        Calendar rightNow = Calendar.getInstance();
        config.setParam("$year", String.valueOf(rightNow.get(Calendar.YEAR)));
        config.setParam("$month", String.valueOf(rightNow.get(Calendar.MONTH) + 1));
        config.setParam("$day", String.valueOf(rightNow.get(Calendar.DAY_OF_MONTH)));

        //转换上传
        if (!makeAndUpload(config)) {
            return "";
        } else {
            //签名签章
            String str = caSign(config);
            return str;
        }
    }

    @Override
    public String signBankCardGrantAgreement(UAuthCASignRequest signRequest) {
        FileSignAgreement fileSignAgreement = this.fileSignAgreementRepository.findBySignType("GRANT");//APP授权书
        if (null == fileSignAgreement) {
            return "";
        }
        FileSignConfig config = new FileSignConfig();
        config.setTemplateFileName(fileSignAgreement.getTemplateFileName());
        config.setTemplateContractNo(fileSignAgreement.getTempSeriNum());
        config.setUserPage(fileSignAgreement.getUserPage());
        config.setUserX(fileSignAgreement.getUserX());
        config.setUserY(fileSignAgreement.getUserY());
        config.setCoPage(fileSignAgreement.getCoPage());
        config.setCoX(fileSignAgreement.getCoX());
        config.setCoY(fileSignAgreement.getCoY());
        config.setUseCoSign(fileSignAgreement.getUseCoSign());

        //获取银行卡信息
        Map<String, Object> cardMap = (Map<String, Object>) HttpUtil.json2Map(signRequest.getOrderJson());
        Map<String, Object> cardInfo = (Map<String, Object>) HttpUtil.json2Map(cardMap.get("cardInfo").toString());
        if (null == cardInfo) {
            return "";
        }
        //APP授权书pdf文件名命名规则:
        String fileNameNoExt = String.format("%s_%s_%s", "app", (String) cardInfo.get("cardNo"), "grant");
        config.setFileNameNoExt(fileNameNoExt);

        String path = getGrantPath((String) cardInfo.get("certNo"));
        logger.debug("获取签章路径：path=" + path);
        config.setFtpPath(path);
        config.setParam("applseq", "");
        config.setParam("custNo", (String) cardInfo.get("custNo"));
        config.setParam("signCode", signRequest.getSignCode());
        config.setParam("signType", signRequest.getSignType());
        config.setParam("contractType", "0");

        config.setUserName((String) cardInfo.get("custName") == null ? "" : (String) cardInfo.get("custName"));
        config.setUserIdentity((String) cardInfo.get("certNo") == null ? "" : (String) cardInfo.get("certNo"));
        config.setParam("$custname",
                (String) cardInfo.get("custName") == null ? "" : (String) cardInfo.get("custName"));
        config.setParam("$cardno", (String) cardInfo.get("cardNo") == null ? "" : (String) cardInfo.get("cardNo"));
        config.setParam("$bankname",
                (String) cardInfo.get("bankName") == null ? "" : (String) cardInfo.get("bankName"));
        Calendar rightNow = Calendar.getInstance();
        config.setParam("$year", String.valueOf(rightNow.get(Calendar.YEAR)));
        config.setParam("$month", String.valueOf(rightNow.get(Calendar.MONTH) + 1));
        config.setParam("$day", String.valueOf(rightNow.get(Calendar.DAY_OF_MONTH)));

        //转换上传
        if (!makeAndUpload(config)) {
            return "";
        } else {
            //签名签章
            String str = caSign(config);
            return str;
        }
    }

    @Override
    public String signAgreementByOrderJson(String orderJson, String signType, String contractFlag, boolean isManual,
                                           String signCode) {
        FileSignConfig config = new FileSignConfig();
        //从数据库读取模板配置信息
        logger.debug("orderJson=" + orderJson.toString() + " ,signType=" + signType);
        FileSignAgreement fileSignAgreement = this.fileSignAgreementRepository.findBySignType(signType);
        if (null == fileSignAgreement) {
            logger.debug("fileSignAgreement is null ");
            return "";
        }
        config.setTemplateFileName(fileSignAgreement.getTemplateFileName());
        config.setTemplateContractNo(fileSignAgreement.getTempSeriNum());
        config.setUserPage(fileSignAgreement.getUserPage());
        config.setUserX(fileSignAgreement.getUserX());
        config.setUserY(fileSignAgreement.getUserY());
        config.setCoPage(fileSignAgreement.getCoPage());
        config.setCoX(fileSignAgreement.getCoX());
        config.setCoY(fileSignAgreement.getCoY());
        config.setUseCoSign(fileSignAgreement.getUseCoSign());

        //获取appOrder对象
        Map<String, Object> orderMap = HttpUtil.json2Map(orderJson);
        Map<String, Object> appOrder = HttpUtil.json2Map(orderMap.get("order").toString());
        logger.debug("appOrder is null=" + (appOrder == null));
        if (null == appOrder) {
            return "";
        }

        String fileNameNoExt = String.format("%s_%s_%s", "app", appOrder.get("idNo"), signType);
        config.setFileNameNoExt(fileNameNoExt);
        //通过流水号获取影像上传路径
        if (null == appOrder.get("applseq") || "".equals(appOrder.get("applseq"))) {
            logger.debug("订单流水号为空!");
            return "";
        }

        String path = getPath((String) appOrder.get("applseq"));
        logger.debug("获取签章路径：path=" + path);
        config.setFtpPath(path);
        config.setParam("applseq", (String) appOrder.get("applseq"));
        config.setParam("custNo", appOrder.get("custNo") == null ? "" : (String) appOrder.get("custNo"));
        config.setParam("signCode", signCode);
        config.setParam("signType", signType);
        config.setParam("contractType", "1");
        config.setParam("$name", appOrder.get("custName") == null ? "" : (String) appOrder.get("custName"));
        config.setParam("$userIdentity", appOrder.get("idNo") == null ? "" : (String) appOrder.get("idNo"));
        Calendar rightNow = Calendar.getInstance();
        config.setParam("$year", String.valueOf(rightNow.get(Calendar.YEAR)));
        config.setParam("$month", String.valueOf(rightNow.get(Calendar.MONTH) + 1));
        config.setParam("$day", String.valueOf(rightNow.get(Calendar.DAY_OF_MONTH)));
        config.setUserName(appOrder.get("custName") == null ? "" : (String) appOrder.get("custName"));
        config.setUserIdentity(appOrder.get("idNo") == null ? "" : (String) appOrder.get("idNo"));
        config.setParam("$contractNo", "");//合同编号：暂时没有数据
        config.setParam("$custName",
                appOrder.get("custName") == null ? "" : (String) appOrder.get("custName"));          //借款人(甲方)姓名
        config.setParam("$identifyNo",
                appOrder.get("idNo") == null ? "" : (String) appOrder.get("idNo"));                //身份证号码
        if (appOrder.get("repay_acc_mobile") != null) {
            config.setParam("$custMobile", appOrder.get("repay_acc_mobile").toString());
        } else {
            config.setParam("$custMobile",
                    appOrder.get("indivMobile") == null ? "" : (String) appOrder.get("indivMobile"));  //联系电话
        }
        config.setParam("$cooprName",
                appOrder.get("cooprName") == null ? "" : (String) appOrder.get("cooprName"));       //门店名称
        config.setParam("$applyTnr",
                appOrder.get("applyTnr") == null ? "" : (String) appOrder.get("applyTnr"));          //借款期限
        config.setParam("$purpose",
                appOrder.get("purpose") == null ? "" : (String) appOrder.get("purpose"));             //贷款用途

        //获取门店所在市cityName   (crm:/app/crm/cust/getStore   门店代码:cooprCde)
        config.setParam("$cooprCityName",
                (String) appOrder.get("cityName") == null ? "" : (String) appOrder.get("cityName"));

        // 提前还款费率
        config.setParam("$preRate", (String) appOrder.get("preRate") == null ? "" : (String) appOrder.get("preRate"));

        //获取居住地址、邮箱、邮编       /app/crm/cust/getCustExtInfo
        String pageName = "";   //单位信息:dwInfo、居住信息:jzInfo、个人信息:grInfo、为空：所有信息
        String address = "";
        if (appOrder.containsKey("pptyProvinceName")) {
            address += appOrder.get("pptyProvinceName") == null ? "" : (String) appOrder.get("pptyProvinceName");
        }
        if (appOrder.containsKey("pptyCityName")) {
            if (!appOrder.get("pptyProvinceName").equals(appOrder.get("pptyCityName"))) {
                address += appOrder.get("pptyCityName") == null ? "" : (String) appOrder.get("pptyCityName");
            }
        }
        if (appOrder.containsKey("pptyAreaName")) {
            address += appOrder.get("pptyAreaName") == null ? "" : (String) appOrder.get("pptyAreaName");
        }
        config.setParam("$custAddress",
                address + (appOrder.get("deliverAddr") == null ? "" : (String) appOrder.get("deliverAddr")));     //居住地址
        config.setParam("$email",
                appOrder.get("email") == null ? "" : (String) appOrder.get("email"));                 //邮箱
        config.setParam("$liveZip",
                appOrder.get("liveZip") == null ? "" : (String) appOrder.get("liveZip"));           //邮编

        //合同金额显示：
        String applyAmt = amtConvert((String) appOrder.get("applyAmt"));//借款金额
        String apprvAmt = amtConvert((String) appOrder.get("apprvAmt"));//审批金额
        if ("".equals(apprvAmt) || null == apprvAmt || "null".equals(apprvAmt)) {
            config.setParam("$applyAmtSmall", applyAmt == null ? "" : applyAmt);
            if (null != applyAmt && !"".equals(applyAmt)) {
                config.setParam("$applyAmtBig", MoneyTool.change(Double.parseDouble(applyAmt)));
            } else {
                config.setParam("$applyAmtBig", "");
            }
        } else {
            config.setParam("$applyAmtSmall", apprvAmt);
            config.setParam("$applyAmtBig", MoneyTool.change(Double.parseDouble(apprvAmt)));
        }
        config.setParam("$fstPaySmall", appOrder.get("fstPay") == null ?
                "" :
                amtConvert((String) appOrder.get("fstPay")));           //fstPay首付金额
        if (null == appOrder.get("fstPay") || "".equals(appOrder.get("fstPay"))) {
            config.setParam("$fstPayBig", "");
        } else {
            config.setParam("$fstPayBig",
                    MoneyTool.change(Double.parseDouble(amtConvert((String) appOrder.get("fstPay")))));
        }
        logger.info("获取放款账号信息");
        config.setParam("$applAcNam",
                appOrder.get("applAcNam") == null ? "" : (String) appOrder.get("applAcNam"));       //放款账号户名
        config.setParam("$accBankCde",
                appOrder.get("accBankCde") == null ? "" : (String) appOrder.get("accBankCde"));    //放款开户银行代码
        config.setParam("$accBankName",
                appOrder.get("accBankName") == null ? "" : (String) appOrder.get("accBankName"));//放款开户银行名
        config.setParam("$accAcBchCde",
                appOrder.get("accAcBchCde") == null ? "" : (String) appOrder.get("accAcBchCde"));//放款开户行支行代码
        config.setParam("$applCardNo",
                appOrder.get("applCardNo") == null ? "" : (String) appOrder.get("applCardNo"));   //放款卡号

        logger.info("处理利率及各种费率");
        //通过还款方式查询合同中所对应的选择条数
        List<AppContract> contractList = this.appContractRepository
                .findByContType((String) appOrder.get("typLevelTwo"));
        List<ContractAssInfo> contractAssInfoList = new ArrayList<>();
        if (contractList.size() > 1) {
            //利率、手续费率、管理费率
            contractAssInfoList = this.contractAssInfoRepository
                    .findByPayMtdAndTypLevelTwoAndTypCde((String) appOrder.get("payMtd"),
                            (String) appOrder.get("typLevelTwo"), (String) appOrder.get("typCde"));
        } else if (contractList.size() == 1) {
            contractAssInfoList = this.contractAssInfoRepository
                    .findByPayMtdAndTypLevelTwo((String) appOrder.get("payMtd"), (String) appOrder.get("typLevelTwo"));
        }
        if (null == contractAssInfoList || contractAssInfoList.size() == 0) {
            this.initFileConfig(config);
        } else {
            if (contractAssInfoList.size() == 1) {
                ContractAssInfo contractAssInfo = contractAssInfoList.get(0);
                config.setParam("$applyType",
                        contractAssInfo.getContractMtdType() == null ? "" : contractAssInfo.getContractMtdType());
                config.setParam("$mtdCde",
                        contractAssInfo.getContractMtdType() == null ? "" : contractAssInfo.getContractMtdType());
                config.setParam("$rate", contractAssInfo.getApplyRate() == null ? "" : contractAssInfo.getApplyRate());
                config.setParam("$feeRate", contractAssInfo.getFeeRate() == null ? "" : contractAssInfo.getFeeRate());
                config.setParam("$applyManagerRate",
                        contractAssInfo.getApplyManagerRate() == null ? "" : contractAssInfo.getApplyManagerRate());
            } else if (contractAssInfoList.size() > 1) {
                List<ContractAssInfo> AssInfoList = new ArrayList<>();
                if (contractList.size() == 1) {
                    AssInfoList = this.contractAssInfoRepository
                            .findByPayMtdAndTypLevelTwoAndApplyTnr((String) appOrder.get("payMtd"),
                                    (String) appOrder.get("typLevelTwo"), (String) appOrder.get("applyTnr"));
                } else if (contractList.size() > 1) {
                    AssInfoList = this.contractAssInfoRepository
                            .findByPayMtdAndTypLevelTwoAndTypCdeAndApplyTnr((String) appOrder.get("payMtd"),
                                    (String) appOrder.get("typLevelTwo"), (String) appOrder.get("typCde"),
                                    (String) appOrder.get("applyTnr"));
                }
                if (null != AssInfoList) {
                    if (AssInfoList.size() == 1) {
                        ContractAssInfo contractAssInfo = AssInfoList.get(0);
                        config.setParam("$applyType",
                                contractAssInfo.getContractMtdType() == null ?
                                        "" :
                                        contractAssInfo.getContractMtdType());
                        config.setParam("$mtdCde",
                                contractAssInfo.getContractMtdType() == null ?
                                        "" :
                                        contractAssInfo.getContractMtdType());
                        config.setParam("$rate",
                                contractAssInfo.getApplyRate() == null ? "" : contractAssInfo.getApplyRate());
                        config.setParam("$feeRate",
                                contractAssInfo.getFeeRate() == null ? "" : contractAssInfo.getFeeRate());
                        config.setParam("$applyManagerRate",
                                contractAssInfo.getApplyManagerRate() == null ?
                                        "" :
                                        contractAssInfo.getApplyManagerRate());
                    } else if (AssInfoList.size() > 1) {
                        //查询是否有共同还款人
                        AppOrdernoTypgrpRelation relation = appOrdernoTypgrpRelationRepository.findOne((String) appOrder.get("order_no"));
                        if (relation == null) {
                            return "";
                        }
                        Map<String, Object> countPerson = commonRepaymentPersonService.countCommonRepaymentPerson(relation.getApplSeq());
                        if (!HttpUtil.isSuccess(countPerson)) {
                            return "";
                        }
                        int count = (int) ((Map<String, Object>) countPerson.get("body")).get("count");
                        String hasComRepay;
                        if (count == 0) {//没有共同还款人
                            hasComRepay = "0";
                        } else {//有共同还款人
                            hasComRepay = "1";
                        }
                        ContractAssInfo contractAssInfo = null;
                        if (contractList.size() == 1) {
                            contractAssInfo = this.contractAssInfoRepository
                                    .findByHasComRepayOne((String) appOrder.get("payMtd"),
                                            (String) appOrder.get("typLevelTwo"), (String) appOrder.get("applyTnr"),
                                            hasComRepay);
                        } else if (contractList.size() > 1) {
                            contractAssInfo = this.contractAssInfoRepository
                                    .findByHasComRepayMore((String) appOrder.get("payMtd"),
                                            (String) appOrder.get("typLevelTwo"), (String) appOrder.get("typCde"),
                                            (String) appOrder.get("applyTnr"), hasComRepay);
                        }

                        if (null != contractAssInfo) {
                            config.setParam("$applyType", contractAssInfo.getContractMtdType() == null ?
                                    "" :
                                    contractAssInfo.getContractMtdType());
                            config.setParam("$mtdCde", contractAssInfo.getContractMtdType() == null ?
                                    "" :
                                    contractAssInfo.getContractMtdType());
                            config.setParam("$rate",
                                    contractAssInfo.getApplyRate() == null ? "" : contractAssInfo.getApplyRate());
                            config.setParam("$feeRate",
                                    contractAssInfo.getFeeRate() == null ? "" : contractAssInfo.getFeeRate());
                            config.setParam("$applyManagerRate", contractAssInfo.getApplyManagerRate() == null ?
                                    "" :
                                    contractAssInfo.getApplyManagerRate());
                        }
                    } else {
                        this.initFileConfig(config);
                    }
                } else {
                    this.initFileConfig(config);
                }
            }
        }

        logger.info("查询剩余可用额度");
        //获取剩余可用额度(目前只有 个人消费借款合同-自主支付版本-支用时使用.docx 合同用)
        String certType = "20";  //身份证：20
        String idNo = (String) appOrder.get("idNo");//证件号码
        Map<String, Object> EdCheckMap = cmisApplService.getEdCheck(certType, idNo, super.getToken());
        logger.info(EdCheckMap);
        ResultHead head = (ResultHead) EdCheckMap.get("head");
        String flag = head.getRetFlag();
        String msg = head.getRetMsg();
        if (Objects.equals(flag, "00000")) {
            Map<String, Object> edCheckBody = (Map<String, Object>) EdCheckMap.get("body");
            String edAmt = String.valueOf(edCheckBody.get("crdNorAvailAmt")) == null ?
                    "" :
                    String.valueOf(edCheckBody.get("crdNorAvailAmt"));//自主支付可用额度金额
            String applAmt = config.getParams().get("$applyAmtSmall");
            //Double amt = Double.parseDouble(edAmt) - Double.parseDouble(applAmt);
            Double amt = new BigDecimal(edAmt).subtract(new BigDecimal(applAmt)).doubleValue();
            config.setParam("$crdNorAvailAmt", String.valueOf(amt) == null ? "" : amtConvert(String.valueOf(amt)));
            if (!StringUtils.isEmpty(String.valueOf(amt))) {
                config.setParam("$bigCrdNorAvailAmt",
                        MoneyTool.change(Double.parseDouble(amtConvert(String.valueOf(amt)))));
            }
        } else {
            config.setParam("$crdNorAvailAmt", "");
            config.setParam("$bigCrdNorAvailAmt", "");
        }

        logger.info("处理还款银行卡信息");
        //还款银行卡信息
        if (null == appOrder.get("repayApplCardNo") || "".equals(appOrder.get("repayApplCardNo"))) {
            //还款银行卡信息(查询CRM默认还款银行卡信息)
            String url = EurekaServer.CRM + "/app/crm/cust/getDefaultBankCard?custNo=" + appOrder
                    .get("custNo");
            String json = HttpUtil.restGet(url);
            if (StringUtils.isEmpty(json)) {
                logger.debug("查询默认还款卡为空!json=" + json);
                return "";
            }
            json = json.replaceAll("null", "\"\"");
            Map<String, Object> map = HttpUtil.json2Map(json);
            if (HttpUtil.isSuccess(map)) {
                Map<String, Object> mapBody = HttpUtil.json2Map(map.get("body").toString());
                config.setParam("$repayApplAcNam",
                        mapBody.get("custName") == null ? "" : (String) mapBody.get("custName"));//还款账号户名
                config.setParam("$repayAccBankName",
                        mapBody.get("bankName") == null ? "" : (String) mapBody.get("bankName"));//还款开户银行名
                config.setParam("$repayApplCardNo",
                        mapBody.get("cardNo") == null ? "" : (String) mapBody.get("cardNo"));//还款卡号
            }
        } else {
            config.setParam("$repayApplAcNam",
                    appOrder.get("repayApplAcNam") == null ? "" : (String) appOrder.get("repayApplAcNam"));//还款账号户名
            config.setParam("$repayAccBankName",
                    appOrder.get("repayAccBankName") == null ? "" : (String) appOrder.get("repayAccBankName"));//还款开户银行名
            config.setParam("$repayApplCardNo",
                    appOrder.get("repayApplCardNo") == null ? "" : (String) appOrder.get("repayApplCardNo"));//还款卡号
        }

        config.setParam("$accName", appOrder.get("custName") == null ? "" : (String) appOrder.get("custName"));//甲方签名
        config.setParam("$lenderName", "海尔消费金融有限公司");//乙方签名
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        config.setParam("$date", format.format(new Date()));//日期

        logger.info("合同参数抓取结束");

        //转换上传
        if (!makeAndUpload(config)) {
            return "";
        } else {
            //签名签章
            String str = caSign(config);
            if ("".equals(str)) {
                return "";
            }
            return str;
        }

    }

    public String signAgreementByCmis(String Applseq, String signType, String contractFlag, boolean isManual,
                                      String signCode) {
        FileSignConfig config = new FileSignConfig();
        //从数据库读取模板配置信息
        logger.debug("Applseq=" + Applseq.toString() + " ,signType=" + signType);
        FileSignAgreement fileSignAgreement = this.fileSignAgreementRepository.findBySignType(signType);
        if (null == fileSignAgreement) {
            logger.debug("fileSignAgreement is null ");
            return "";
        }
        config.setTemplateFileName(fileSignAgreement.getTemplateFileName());
        config.setTemplateContractNo(fileSignAgreement.getTempSeriNum());
        config.setUserPage(fileSignAgreement.getUserPage());
        config.setUserX(fileSignAgreement.getUserX());
        config.setUserY(fileSignAgreement.getUserY());
        config.setCoPage(fileSignAgreement.getCoPage());
        config.setCoX(fileSignAgreement.getCoX());
        config.setCoY(fileSignAgreement.getCoY());
        config.setUseCoSign(fileSignAgreement.getUseCoSign());

        //获取appOrder对象
        //Map<String, Object> orderMap = HttpUtil.json2Map(orderJson);
        AppOrder order = appOrderService.getAppOrderFromCmis(Applseq, "0");
        try {
// Map<String, Object> orderMap=transBean2Map(order);
            JSONObject jsonOrder = new JSONObject(order);
            Map<String, Object> appOrder = HttpUtil.json2Map(jsonOrder.toString());
            logger.debug("appOrder is null=" + (appOrder == null));
            if (null == appOrder) {
                return "";
            }

            String fileNameNoExt = String.format("%s_%s_%s", "app", appOrder.get("idNo"), signType);
            config.setFileNameNoExt(fileNameNoExt);
            //通过流水号获取影像上传路径
            if (null == appOrder.get("applseq") || "".equals(appOrder.get("applseq"))) {
                logger.debug("订单流水号为空!");
                return "";
            }

            String path = getPath((String) appOrder.get("applseq"));
            logger.debug("获取签章路径：path=" + path);
            config.setFtpPath(path);
            config.setParam("applseq", (String) appOrder.get("applseq"));
            config.setParam("custNo",  StringUtils.isEmpty(appOrder.get("custNo")) ? "" : (String) appOrder.get("custNo"));
            config.setParam("signCode", signCode);
            config.setParam("signType", signType);
            config.setParam("contractType", "1");
            config.setParam("$name", StringUtils.isEmpty(appOrder.get("custName")) ? "" : (String) appOrder.get("custName"));
            config.setParam("$userIdentity", StringUtils.isEmpty(appOrder.get("idNo")) ? "" : (String) appOrder.get("idNo"));
            Calendar rightNow = Calendar.getInstance();
            config.setParam("$year", String.valueOf(rightNow.get(Calendar.YEAR)));
            config.setParam("$month", String.valueOf(rightNow.get(Calendar.MONTH) + 1));
            config.setParam("$day", String.valueOf(rightNow.get(Calendar.DAY_OF_MONTH)));
            config.setUserName(StringUtils.isEmpty(appOrder.get("custName")) ? "" : (String) appOrder.get("custName"));
            config.setUserIdentity(StringUtils.isEmpty(appOrder.get("idNo")) ? "" : (String) appOrder.get("idNo"));
            config.setParam("$contractNo", "");//合同编号：暂时没有数据
            config.setParam("$custName",
                    StringUtils.isEmpty(appOrder.get("custName")) ? "" : (String) appOrder.get("custName"));          //借款人(甲方)姓名
            config.setParam("$identifyNo",
                    StringUtils.isEmpty(appOrder.get("idNo")) ? "" : (String) appOrder.get("idNo"));                //身份证号码
            if (!StringUtils.isEmpty(appOrder.get("repay_acc_mobile"))) {
                config.setParam("$custMobile", appOrder.get("repay_acc_mobile").toString());
            } else {
                config.setParam("$custMobile",
                        StringUtils.isEmpty(appOrder.get("indivMobile")) ? "" : (String) appOrder.get("indivMobile"));  //联系电话
            }
            config.setParam("$cooprName",
                    StringUtils.isEmpty(appOrder.get("cooprName")) ? "" : (String) appOrder.get("cooprName"));       //门店名称
            config.setParam("$applyTnr",
                    StringUtils.isEmpty(appOrder.get("applyTnr")) ? "" : (String) appOrder.get("applyTnr"));          //借款期限
            config.setParam("$purpose",
                    StringUtils.isEmpty(appOrder.get("purpose")) ? "" : (String) appOrder.get("purpose"));             //贷款用途

            //获取门店所在市cityName   (crm:/app/crm/cust/getStore   门店代码:cooprCde)
            config.setParam("$cooprCityName",
                    StringUtils.isEmpty(appOrder.get("cityName")) ? "" : (String) appOrder.get("cityName"));
            //根据商户编码获取商户名称
            String merchName="";
            if (!StringUtils.isEmpty(appOrder.get("merchNo"))) {
                String url = EurekaServer.CRM + "/pub/crm/cust/getMerchInfoByMerchNo?merchNo=" + appOrder.get("merchNo").toString();
                String json = HttpUtil.restGet(url, super.getToken());
                logger.info("通过crm请求的根据商户编号查询商户的信息的url" + url);
                Map<String, Object> resultMap = HttpUtil.json2Map(json);
                logger.info("通过crm请求的根据商户编号查询商户的信息为：" + resultMap);
                if(!StringUtils.isEmpty(resultMap.get("body"))){
                    Map<String, Object> bodyMap = HttpUtil.json2Map(resultMap.get("body").toString());
                    merchName = StringUtils.isEmpty(bodyMap.get("merchChName")) ? "" : (String) bodyMap.get("merchChName");
                }
            }
            config.setParam("$merchName", merchName );
            //获取居住地址、邮箱、邮编       /app/crm/cust/getCustExtInfo
            String pageName = "";   //单位信息:dwInfo、居住信息:jzInfo、个人信息:grInfo、为空：所有信息
            String address = "";
            if (appOrder.containsKey("pptyProvinceName")) {
                address += appOrder.get("pptyProvinceName") == null ? "" : (String) appOrder.get("pptyProvinceName");
            }
            if (appOrder.containsKey("pptyCityName")) {
                if (!appOrder.get("pptyProvinceName").equals(appOrder.get("pptyCityName"))) {
                    address += appOrder.get("pptyCityName") == null ? "" : (String) appOrder.get("pptyCityName");
                }
            }
            if (appOrder.containsKey("pptyAreaName")) {
                address += appOrder.get("pptyAreaName") == null ? "" : (String) appOrder.get("pptyAreaName");
            }
            config.setParam("$custAddress",
                    address + (appOrder.get("liveInfo") == null ? "" : (String) appOrder.get("liveInfo")));     //居住地址
            config.setParam("$email",
                    StringUtils.isEmpty(appOrder.get("email")) ? "" : (String) appOrder.get("email"));                 //邮箱
            config.setParam("$liveZip",
                    StringUtils.isEmpty(appOrder.get("liveZip")) ? "" : (String) appOrder.get("liveZip"));           //邮编

            //合同金额显示：
            String applyAmt = amtConvert((String) appOrder.get("applyAmt"));//借款金额
            String apprvAmt = amtConvert((String) appOrder.get("apprvAmt"));//审批金额
            if ("".equals(apprvAmt) || null == apprvAmt || "null".equals(apprvAmt)) {
                config.setParam("$applyAmtSmall", applyAmt == null ? "" : applyAmt);
                if (null != applyAmt && !"".equals(applyAmt)) {
                    config.setParam("$applyAmtBig", MoneyTool.change(Double.parseDouble(applyAmt)));
                } else {
                    config.setParam("$applyAmtBig", "");
                }
            } else {
                config.setParam("$applyAmtSmall", apprvAmt);
                config.setParam("$applyAmtBig", MoneyTool.change(Double.parseDouble(apprvAmt)));
            }
            config.setParam("$fstPaySmall", StringUtils.isEmpty(appOrder.get("fstPay")) ?
                    "" :
                    amtConvert((String) appOrder.get("fstPay")));           //fstPay首付金额
            if (null == appOrder.get("fstPay") || "".equals(appOrder.get("fstPay"))) {
                config.setParam("$fstPayBig", "");
            } else {
                config.setParam("$fstPayBig",
                        MoneyTool.change(Double.parseDouble(amtConvert((String) appOrder.get("fstPay")))));
            }
            logger.info("获取放款账号信息");
            config.setParam("$applAcNam",
                    StringUtils.isEmpty(appOrder.get("applAcNam")) ? "" : (String) appOrder.get("applAcNam"));       //放款账号户名
            config.setParam("$accBankCde",
                    StringUtils.isEmpty(appOrder.get("accBankCde")) ? "" : (String) appOrder.get("accBankCde"));    //放款开户银行代码
            if(StringUtils.isEmpty(appOrder.get("accBankName"))){//放款开户银行名
                if(appOrder.get("accAcBchName") != null){
                    config.setParam("$accBankName",appOrder.get("accAcBchName").toString());
                }else{
                    config.setParam("$accBankName","");
                }
            }else{
                config.setParam("$accBankName",appOrder.get("accBankName").toString());
            }
//            config.setParam("$accBankName",
//                    appOrder.get("accBankName") == null ? (appOrder.get("accAcBchName") == null ? "" : (String) appOrder.get("accAcBchName")) : (String) appOrder.get("accBankName"));
            config.setParam("$accAcBchCde",
                    StringUtils.isEmpty(appOrder.get("accAcBchCde")) ? "" : (String) appOrder.get("accAcBchCde"));//放款开户行支行代码
            config.setParam("$applCardNo",
                    StringUtils.isEmpty(appOrder.get("applCardNo")) ? "" : (String) appOrder.get("applCardNo"));   //放款卡号

            logger.info("处理利率及各种费率");
            //通过还款方式查询合同中所对应的选择条数
            List<AppContract> contractList = this.appContractRepository
                    .findByContType((String) appOrder.get("typLevelTwo"));
            List<ContractAssInfo> contractAssInfoList = new ArrayList<>();
            if (contractList.size() > 1) {
                //利率、手续费率、管理费率
                contractAssInfoList = this.contractAssInfoRepository
                        .findByPayMtdAndTypLevelTwoAndTypCde((String) appOrder.get("payMtd"),
                                (String) appOrder.get("typLevelTwo"), (String) appOrder.get("typCde"));
            } else if (contractList.size() == 1) {
                contractAssInfoList = this.contractAssInfoRepository
                        .findByPayMtdAndTypLevelTwo((String) appOrder.get("payMtd"), (String) appOrder.get("typLevelTwo"));
            }
            if (null == contractAssInfoList || contractAssInfoList.size() == 0) {
                this.initFileConfig(config);
               /* String tnrOpt = StringUtils.isEmpty(appOrder.get("applyTnr")) ? "" : appOrder.get("applyTnr").toString(); //还款期数
                String mtdTyp = StringUtils.isEmpty(appOrder.get("payMtd"))   ? "" : appOrder.get("payMtd").toString(); //还款方式类型
                String typCde = StringUtils.isEmpty(appOrder.get("typCde")) ? "" : appOrder.get("typCde").toString(); //贷款品种
                String url = super.getGateUrl() + "/app/cmisproxy/api/pLoanTyp/" + typCde + "/feeMsg?tnrOpt=" + tnrOpt + "&mtdTyp=" + mtdTyp + "&feeTnrTyp=03";
                String json = HttpUtil.restGet(url, super.getToken());
                logger.info("通过接口请求的合同费率的url" + url);
                List<Map<String, Object>> resultlist = HttpUtil.json2List(json);
                logger.info("通过接口请求的合同费率为：" + resultlist);
                if (resultlist.size() > 0) {
                    Map<String, Object> resultMap = resultlist.get(0);
                    String s = StringUtils.isEmpty(resultMap.get("feePct2")) ? "" : resultMap.get("feePct2").toString();
                    String feerate = "";
                    if (!StringUtils.isEmpty(s)) {
                        BigDecimal a = new BigDecimal(0.1);
                        BigDecimal b = new BigDecimal(s);
                        BigDecimal d = new BigDecimal(100);
                        BigDecimal c = a.subtract(b);
                        BigDecimal bd = c.multiply(d);
                        DecimalFormat df = new DecimalFormat("0.00");
                        feerate = df.format(bd) + "%";
                    }
                    config.setParam("$feeRate", feerate);
                    config.setParam("$applyType", "");
                    config.setParam("$mtdCde", "");
                    config.setParam("$rate", "");
                    config.setParam("$applyManagerRate", "");
                } else {
                    this.initFileConfig(config);
                }*/
            } else {
                if (contractAssInfoList.size() == 1) {
                    ContractAssInfo contractAssInfo = contractAssInfoList.get(0);
                    config.setParam("$applyType",
                            StringUtils.isEmpty(contractAssInfo.getContractMtdType()) ? "" : contractAssInfo.getContractMtdType());
                    config.setParam("$mtdCde",
                            StringUtils.isEmpty(contractAssInfo.getContractMtdType()) ? "" : contractAssInfo.getContractMtdType());
                    config.setParam("$rate",StringUtils.isEmpty(contractAssInfo.getApplyRate()) ? "" : contractAssInfo.getApplyRate());
                    config.setParam("$feeRate", StringUtils.isEmpty(contractAssInfo.getFeeRate())  ? "" : contractAssInfo.getFeeRate());
                    config.setParam("$applyManagerRate",
                            StringUtils.isEmpty(contractAssInfo.getApplyManagerRate()) ? "" : contractAssInfo.getApplyManagerRate());
                } else if (contractAssInfoList.size() > 1) {
                    List<ContractAssInfo> AssInfoList = new ArrayList<>();
                    if (contractList.size() == 1) {
                        AssInfoList = this.contractAssInfoRepository
                                .findByPayMtdAndTypLevelTwoAndApplyTnr((String) appOrder.get("payMtd"),
                                        (String) appOrder.get("typLevelTwo"), (String) appOrder.get("applyTnr"));
                    } else if (contractList.size() > 1) {
                        AssInfoList = this.contractAssInfoRepository
                                .findByPayMtdAndTypLevelTwoAndTypCdeAndApplyTnr((String) appOrder.get("payMtd"),
                                        (String) appOrder.get("typLevelTwo"), (String) appOrder.get("typCde"),
                                        (String) appOrder.get("applyTnr"));
                    }
                    if (null != AssInfoList) {
                        if (AssInfoList.size() == 1) {
                            ContractAssInfo contractAssInfo = AssInfoList.get(0);
                            config.setParam("$applyType",
                                    StringUtils.isEmpty(contractAssInfo.getContractMtdType()) ?
                                            "" :
                                            contractAssInfo.getContractMtdType());
                            config.setParam("$mtdCde",
                                    StringUtils.isEmpty(contractAssInfo.getContractMtdType()) ?
                                            "" :
                                            contractAssInfo.getContractMtdType());
                            config.setParam("$rate",
                                    StringUtils.isEmpty(contractAssInfo.getApplyRate()) ? "" : contractAssInfo.getApplyRate());
                            config.setParam("$feeRate",
                                    StringUtils.isEmpty(contractAssInfo.getFeeRate()) ? "" : contractAssInfo.getFeeRate());
                            config.setParam("$applyManagerRate",
                                    StringUtils.isEmpty(contractAssInfo.getApplyManagerRate()) ?
                                            "" :
                                            contractAssInfo.getApplyManagerRate());
                        } else if (AssInfoList.size() > 1) {
                            //查询是否有共同还款人
                            AppOrdernoTypgrpRelation relation = appOrdernoTypgrpRelationRepository.findOne((String) appOrder.get("order_no"));
                            if (relation == null) {
                                return "";
                            }
                            Map<String, Object> countPerson = commonRepaymentPersonService.countCommonRepaymentPerson(relation.getApplSeq());
                            if (!HttpUtil.isSuccess(countPerson)) {
                                return "";
                            }
                            int count = (int) ((Map<String, Object>) countPerson.get("body")).get("count");
                            String hasComRepay;
                            if (count == 0) {//没有共同还款人
                                hasComRepay = "0";
                            } else {//有共同还款人
                                hasComRepay = "1";
                            }
                            ContractAssInfo contractAssInfo = null;
                            if (contractList.size() == 1) {
                                contractAssInfo = this.contractAssInfoRepository
                                        .findByHasComRepayOne((String) appOrder.get("payMtd"),
                                                (String) appOrder.get("typLevelTwo"), (String) appOrder.get("applyTnr"),
                                                hasComRepay);
                            } else if (contractList.size() > 1) {
                                contractAssInfo = this.contractAssInfoRepository
                                        .findByHasComRepayMore((String) appOrder.get("payMtd"),
                                                (String) appOrder.get("typLevelTwo"), (String) appOrder.get("typCde"),
                                                (String) appOrder.get("applyTnr"), hasComRepay);
                            }

                            if (null != contractAssInfo) {
                                config.setParam("$applyType", StringUtils.isEmpty(contractAssInfo.getContractMtdType()) ?
                                        "" :
                                        contractAssInfo.getContractMtdType());
                                config.setParam("$mtdCde", StringUtils.isEmpty(contractAssInfo.getContractMtdType()) ?
                                        "" :
                                        contractAssInfo.getContractMtdType());
                                config.setParam("$rate",
                                        StringUtils.isEmpty(contractAssInfo.getApplyRate()) ? "" : contractAssInfo.getApplyRate());
                                config.setParam("$feeRate",
                                        StringUtils.isEmpty(contractAssInfo.getFeeRate()) ? "" : contractAssInfo.getFeeRate());
                                config.setParam("$applyManagerRate", StringUtils.isEmpty(contractAssInfo.getApplyManagerRate()) ?
                                        "" :
                                        contractAssInfo.getApplyManagerRate());
                            }
                        } else {
                            this.initFileConfig(config);
                        }
                    } else {
                        this.initFileConfig(config);
                    }
                }
            }

            logger.info("查询剩余可用额度");
            //获取剩余可用额度(目前只有 个人消费借款合同-自主支付版本-支用时使用.docx 合同用)
            String certType = "20";  //身份证：20
            String idNo = (String) appOrder.get("idNo");//证件号码
            Map<String, Object> EdCheckMap = cmisApplService.getEdCheck(certType, idNo, super.getToken());
            logger.info(EdCheckMap);
            ResultHead head = (ResultHead) EdCheckMap.get("head");
            String flag = head.getRetFlag();
            String msg = head.getRetMsg();
            if (Objects.equals(flag, "00000")) {
                Map<String, Object> edCheckBody = (Map<String, Object>) EdCheckMap.get("body");
                String edAmt = String.valueOf(StringUtils.isEmpty(edCheckBody.get("crdNorAvailAmt")) ?
                        "" :
                        String.valueOf(edCheckBody.get("crdNorAvailAmt")));//自主支付可用额度金额
                String applAmt = config.getParams().get("$applyAmtSmall");
                //Double amt = Double.parseDouble(edAmt) - Double.parseDouble(applAmt);
                Double amt = new BigDecimal(edAmt).subtract(new BigDecimal(applAmt)).doubleValue();
                config.setParam("$crdNorAvailAmt", String.valueOf(amt) == null ? "" : amtConvert(String.valueOf(amt)));
                if (!StringUtils.isEmpty(String.valueOf(amt))) {
                    config.setParam("$bigCrdNorAvailAmt",
                            MoneyTool.change(Double.parseDouble(amtConvert(String.valueOf(amt)))));
                }
            } else {
                config.setParam("$crdNorAvailAmt", "");
                config.setParam("$bigCrdNorAvailAmt", "");
            }

            logger.info("处理还款银行卡信息");
            //还款银行卡信息
            if (null == appOrder.get("repayApplCardNo") || "".equals(appOrder.get("repayApplCardNo"))) {
                //还款银行卡信息(查询CRM默认还款银行卡信息)
                String url = EurekaServer.CRM + "/app/crm/cust/getDefaultBankCard?custNo=" + appOrder.get("custNo");
                String json = HttpUtil.restGet(url);
                if (StringUtils.isEmpty(json)) {
                    logger.debug("查询默认还款卡为空!json=" + json);
                    return "";
                }
                json = json.replaceAll("null", "\"\"");
                Map<String, Object> map = HttpUtil.json2Map(json);
                if (HttpUtil.isSuccess(map)) {
                    Map<String, Object> mapBody = HttpUtil.json2Map(map.get("body").toString());
                    config.setParam("$repayApplAcNam",
                            StringUtils.isEmpty(mapBody.get("custName")) ? "" : (String) mapBody.get("custName"));//还款账号户名
                    config.setParam("$repayAccBankName",
                            StringUtils.isEmpty(mapBody.get("bankName")) ? "" : (String) mapBody.get("bankName"));//还款开户银行名
                    config.setParam("$repayApplCardNo",
                            StringUtils.isEmpty(mapBody.get("cardNo")) ? "" : (String) mapBody.get("cardNo"));//还款卡号
                }
            } else {
                config.setParam("$repayApplAcNam",
                        StringUtils.isEmpty(appOrder.get("repayApplAcNam")) ? "" : (String) appOrder.get("repayApplAcNam"));//还款账号户名
                config.setParam("$repayAccBankName",
                        StringUtils.isEmpty(appOrder.get("repayAccBankName")) ? "" : (String) appOrder.get("repayAccBankName"));//还款开户银行名
                config.setParam("$repayApplCardNo",
                        StringUtils.isEmpty(appOrder.get("repayApplCardNo")) ? "" : (String) appOrder.get("repayApplCardNo"));//还款卡号
            }

            config.setParam("$accName", StringUtils.isEmpty(appOrder.get("custName")) ? "" : (String) appOrder.get("custName"));//甲方签名
            config.setParam("$lenderName", "海尔消费金融有限公司");//乙方签名
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            config.setParam("$date", format.format(new Date()));//日期

            logger.info("合同参数抓取结束");

            //转换上传
            if (!makeAndUpload(config)) {
                return "";
            } else {
                //签名签章
                String str = caSign(config);
                if ("".equals(str)) {
                    return "";
                }
                return str;
            }

        } catch (Exception e) {
            logger.error(e.getMessage() + e);
        }
        return "";
    }

    public static Map<String, Object> transBean2Map(Object obj) {

        if (obj == null) {
            return null;
        }
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor property : propertyDescriptors) {
                String key = property.getName();

                // 过滤class属性
                if (!key.equals("class")) {
                    // 得到property对应的getter方法
                    Method getter = property.getReadMethod();
                    Object value = getter.invoke(obj);

                    map.put(key, value);
                }

            }
        } catch (Exception e) {
            System.out.println("transBean2Map Error " + e);
        }

        return map;

    }

    /**
     * 提额征信授权书签章
     *
     * @param signRequest
     * @return
     */
    public String signRiseEdAgreement(UAuthCASignRequest signRequest) {
        FileSignAgreement fileSignAgreement = this.fileSignAgreementRepository.findBySignType("ZXXY");//提额征信授权书
        if (null == fileSignAgreement) {
            return "";
        }
        FileSignConfig config = new FileSignConfig();
        config.setTemplateFileName(fileSignAgreement.getTemplateFileName());
        config.setTemplateContractNo(fileSignAgreement.getTempSeriNum());
        config.setUserPage(fileSignAgreement.getUserPage());
        config.setUserX(fileSignAgreement.getUserX());
        config.setUserY(fileSignAgreement.getUserY());
        config.setCoPage(fileSignAgreement.getCoPage());
        config.setCoX(fileSignAgreement.getCoX());
        config.setCoY(fileSignAgreement.getCoY());
        config.setUseCoSign(fileSignAgreement.getUseCoSign());

        //获取用户名和身份证号
        Map<String, Object> map = (Map<String, Object>) HttpUtil.json2Map(signRequest.getOrderJson());
        Map<String, Object> info = (Map<String, Object>) HttpUtil.json2Map(map.get("info").toString());
        if (null == info) {
            return "";
        }

        //征信授权书pdf文件名命名规则:
        String fileNameNoExt = String.format("%s_%s_%s", "app", (String) info.get("certNo"), "credit");
        config.setFileNameNoExt(fileNameNoExt);

        String path = getPath(signRequest.getApplseq());
        logger.debug("获取签章路径：path=" + path);
        config.setFtpPath(path);
        config.setParam("applseq", signRequest.getApplseq());
        config.setParam("custNo", info.get("custNo").toString());
        config.setParam("signCode", signRequest.getSignCode());
        config.setParam("signType", signRequest.getSignType());
        config.setParam("contractType", "0");
        config.setUserName(signRequest.getCustName());
        config.setUserIdentity(signRequest.getCustIdCode());
        config.setParam("$custname", signRequest.getCustName() == null ? "" : signRequest.getCustName());
        config.setParam("$custcode", signRequest.getCustIdCode() == null ? "" : signRequest.getCustIdCode());
        Calendar rightNow = Calendar.getInstance();
        config.setParam("$year", String.valueOf(rightNow.get(Calendar.YEAR)));
        config.setParam("$month", String.valueOf(rightNow.get(Calendar.MONTH) + 1));
        config.setParam("$day", String.valueOf(rightNow.get(Calendar.DAY_OF_MONTH)));
        //转换上传
        if (!makeAndUpload(config)) {
            return "";
        } else {
            //签名签章
            String str = caSign(config);
            return str;
        }
    }

    /**
     * CA签名签章.
     *
     * @param config
     * @return
     */
    private String caSign(FileSignConfig config) {
        String caPath = FileSignUtil.getCAFolder() + File.separator + config.getFtpPath();
        String fileNameNoEx = config.getFileNameNoExt();
        String uploadFileName = fileNameNoEx + ".pdf";
        logger.debug("caSign:uploadFileName=" + uploadFileName);
        String key = SignProperties.CA_APP_SECRET;
        String appId = SignProperties.CA_APP_ID;
        String docNum = FileSignUtil.getUUID();
        String applcde = "";// applcde贷款流水号 不需要传值
        CARequest caRequest = new CARequest();
        try {
            String responseCode = "";
            String response = "";
            if (isCAServiceEnabled()) {
                //注册用户
                String name = config.getUserName();
                String idCode = config.getUserIdentity();
                String mobile = "18766395858";//58869762
                CAService.registerUser(name, idCode, "it@haiercash.com", mobile);

                //参数：默认值haier，目录，文件名，随机数，贷款流水号，身份证号，
                //     签名页页码，x轴坐标，y轴坐标，签证页页码，x轴坐标，y轴坐标,
                //     是否需要公司章true需要false不需要

                //        	logger.debug("**************************** 要签章的docName="+uploadFileName);
                String xml = caRequest.buildCASignRequest(appId, caPath, uploadFileName, docNum, applcde,
                        config.getUserIdentity(),
                        config.getUserPage(), config.getUserX(), config.getUserY(),
                        config.getCoPage(), config.getCoX(), config.getCoY(),
                        config.getUseCoSign());
                String signature = HttpClient.hmacSha1(key.getBytes("utf-8"), xml.getBytes("utf-8"));
                String signUrl = SignProperties.CA_SERVICE_URL + "/signAPI/psignForFTP.do";
                //          logger.debug("signUrl="+signUrl);
                //          logger.debug("xml="+xml);
                //          logger.debug("signature="+signature);
                response = HttpClient.sendPostDoCA(signUrl, xml, "utf-8", signature);
                logger.debug("response=" + response);
                //解析返回结果
            /*XStream stream = new XStream();
            stream.autodetectAnnotations(true);// 打开注解
            stream.alias("body", CAResponse.class);
            //TODO 此行报错，暂不使用xstream解析，直接使用字符串查找获取返回值
            CAResponse caResponse = (CAResponse) stream.fromXML(response);*/
                Pattern pattern = Pattern.compile("<code>(.*)</code>");
                Matcher m = pattern.matcher(response);
                if (m.find()) {
                    responseCode = m.group(1);
                }
            } else {
                responseCode = "0";
            }
            String uploadFilepath = SignProperties.UPLOAD_PATH + uploadFileName;
            File temPdfFile = new File(uploadFilepath);
            if ("0".equals(responseCode)) {
                //签章成功
                logger.debug("------------签章成功--------------");
                FileSignUtil.deleteFile(temPdfFile);

                String signCode = config.getParams().get("signCode");
                logger.debug("signCode=" + signCode);
                UAuthCASignRequest uAuthCASignRequest = uAuthCASignRequestRepository
                        .findBySignCode(signCode);
                logger.debug("uAuthCASignRequest=" + uAuthCASignRequest);
                String applseq = config.getParams().get("applseq");    // 流水号
                String signType = config.getParams().get("signType");
                String flag = config.getParams().get("contractType"); //合同、协议标识  1：合同  0：协议
                String commonFlag = uAuthCASignRequest.getCommonFlag();

                logger.debug("signType=" + config.getParams().get("signType"));
                if (!config.getParams().get("signType").equals(SignType.grant.toString())) {
                    List<ContractPdfFile> contractPdfFileList = null;
                    if ("1".equals(flag)) {//合同
                        contractPdfFileList = contractPdfFileRepository
                                .findAlreadyExistsContract(applseq, flag, commonFlag);
                    } else if ("0".equals(flag)) {//协议
                        contractPdfFileList = contractPdfFileRepository
                                .findAlreadyExistsAgreement(applseq, flag, commonFlag, signType);
                    }
                    logger.debug("contractPdfFileList=" + contractPdfFileList);
                    // 删除因审核打回等情况产生的已作废的合同信息
                    if (contractPdfFileList != null && contractPdfFileList.size() > 0) {
                        for (ContractPdfFile oldContractPdfFile : contractPdfFileList) {
                            //上传文件前先删除已提交的合同
                            if (oldContractPdfFile != null) {
                                if (!StringUtils.isEmpty(oldContractPdfFile.getAttachSeq())) {
                                    FTPBean ftpUpdateBean = new FTPBean();
                                    ftpUpdateBean.setSysId(FileSignUtil.getSystemFlag());
                                    ftpUpdateBean.setBusId(getBusinessFlag());
                                    ftpUpdateBean.setApplSeq(config.getParams().get("applseq"));
                                    FTPBeanListInfo updateFileInfo = new FTPBeanListInfo();
                                    updateFileInfo.setAttachSeq(oldContractPdfFile.getAttachSeq());
                                    updateFileInfo.setState("0");//状态设置为失效
                                    List<FTPBeanListInfo> updateFileList = new ArrayList<>();
                                    updateFileList.add(updateFileInfo);
                                    ftpUpdateBean.setList(updateFileList);
                                    logger.debug("删除已提交、作废的合同: " + ftpUpdateBean);
                                    Map<String, Object> updateResult = new CmisController()
                                            .updateFTPInterface(ftpUpdateBean);
                                    logger.debug("Ftp删除已提交、作废合同返回结果: " + updateResult);
                                    if (!CmisUtil.getIsSucceed(updateResult)) {
                                        logger.debug("删除已提交、作废合同失败: " + CmisUtil.getErrMsg(updateResult));
                                    } else {
                                        //修改状态为：已删除
                                        oldContractPdfFile.setDeleteFlag("1");// 是否已删除标识   1：已删除   0：未删除
                                        contractPdfFileRepository.save(oldContractPdfFile);
                                    }
                                }
                            }
                        }
                        logger.debug("---------删除已提交合同成功------------");
                    }
                }

                //把签章文件信息写入签章记录表
                ContractPdfFile contractPdfFile = new ContractPdfFile();
                String contractPdfFileId = FileSignUtil.getUUID();
                contractPdfFile.setId(contractPdfFileId);
                contractPdfFile.setSignCode(signCode);  //签章任务的id
                contractPdfFile.setSignType(config.getParams().get("signType"));  //签章类型
                contractPdfFile.setApplSeq(config.getParams().get("applseq"));    //流水号
                contractPdfFile.setOrderNo(uAuthCASignRequest.getOrderNo());       //订单号
                contractPdfFile.setCustNo(config.getParams().get("custNo"));      //客户编号
                contractPdfFile.setCommonFlag(uAuthCASignRequest.getCommonFlag()); //是否是共同还款人征信协议
                contractPdfFile.setFileName(getImageFolder() + File.separator + config.getFtpPath() +
                        File.separator + fileNameNoEx + ".pdf"); //pdf路径
                contractPdfFile.setFileDesc(uploadFileName); //文件名称
                contractPdfFile.setAppDate(new SimpleDateFormat("yyyy-MM-dd 24hh:mm:ss").format(new Date())); // 日期
                contractPdfFile.setFlag(config.getParams().get("contractType"));//合同、协议标识  1：合同  0：协议
                contractPdfFile.setDeleteFlag("0");   // 是否已删除标识   1：已删除   0：未删除
                logger.debug("contractPdfFile=" + contractPdfFile);
                contractPdfFileRepository.save(contractPdfFile);
                logger.debug("记录签章文件信息contractPdfFileId=" + contractPdfFileId);

                // 非银行卡变更授权书，需上传到信贷系统
                if (!config.getParams().get("signType").equals(SignType.grant.toString())) {
                    // 调信贷接口上传文件
                    FTPBean ftpBean = new FTPBean();
                    ftpBean.setSysId(FileSignUtil.getSystemFlag());
                    ftpBean.setBusId(getBusinessFlag());
                    ftpBean.setApplSeq(config.getParams().get("applseq"));
                    FTPBeanListInfo fileInfo = new FTPBeanListInfo();
                    String uuid = FileSignUtil.getUUID();
                    fileInfo.setSequenceId(uuid);
                    fileInfo.setAttachPath(getImageFolder() + File.separator + config.getFtpPath()
                            + File.separator + fileNameNoEx + ".pdf");// 完整路径
                    fileInfo.setAttachName(fileNameNoEx + ".pdf");
                    fileInfo.setAttachNameNew(uuid + ".pdf");
                    fileInfo.setState("1");// 1:有效
                    fileInfo.setCrtUsr("admin");
                    fileInfo.setCrtDt(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date()));
                    fileInfo.setAttachTyp(FileSignUtil.getDocType());
                    List<FTPBeanListInfo> fileList = new ArrayList<>();
                    fileList.add(fileInfo);
                    ftpBean.setList(fileList);
                    logger.debug("pdf上传attachPath=" + fileInfo.getAttachPath() + ",attachName=" + fileInfo
                            .getAttachNameNew());
                    Map<String, Object> cmisResult = new CmisController().addFTPInterface(ftpBean);
                    logger.debug("ftp新增接口(100055)返回cmisResult=" + cmisResult);
                    if (!CmisUtil.getIsSucceed(cmisResult)) {
                        logger.debug("pdf影像上传失败：" + CmisUtil.getErrMsg(cmisResult));
                    } else {
                        logger.debug("-------------pdf上传成功-----------");
                    }

                    //把attachSeq写入签章记录表
                    Map<String, Object> bodyMap = CmisUtil.getBody(cmisResult);
                    logger.debug("bodyMap=" + bodyMap.toString());
                    Map<String, Object> ListMap = (Map<String, Object>) bodyMap.get("list");
                    logger.debug("ListMap=" + ListMap.toString());
                    Map<String, Object> infoMap = (Map<String, Object>) ListMap.get("info");
                    logger.debug("infoMap=" + infoMap);
                    if (infoMap != null) {
                        String getAttachSeq = infoMap.get("attachSeq").toString();
                        logger.debug("attachSeq=" + getAttachSeq + ",contractPdfFileId=" + contractPdfFileId);
                        ContractPdfFile contractPdfFileInfo = contractPdfFileRepository
                                .findById(contractPdfFileId);
                        contractPdfFileInfo.setAttachSeq(getAttachSeq);
                        contractPdfFileRepository.save(contractPdfFileInfo);
                        logger.debug("记录文件唯一标识成功");
                    }
                } else {//signType = "grant"  银行卡变更授权书
                    Map<String, Object> resultMap = sendGrantToCrm(
                            config.getParams().get("custNo"),
                            config.getParams().get("$cardno"),
                            getImageFolder() + File.separator + config.getFtpPath() + File.separator,
                            fileNameNoEx + ".pdf", "1");
                    if (!"00000".equals(resultMap.get("retCod"))) {
                        logger.debug("银行卡变更授权书上传CRM失败! retMsg=" + resultMap.get("retMsg"));
                        return "";
                    }
                }

                return getImageFolder() + File.separator + config.getFtpPath() + File.separator + uploadFileName;
            } else {
                //签章失败
                FileSignUtil.deleteFile(temPdfFile);
                Pattern pattern = Pattern.compile("<message>(.*)</message>");
                Matcher m = pattern.matcher(response);
                if (m.find()) {
                    responseCode = m.group(1);
                }
                logger.info("文件签名签章失败：" + responseCode);
                return "";
            }
        } catch (Exception e) {
            logger.error("文件签名签章发生异常：" + e.getMessage());
            return "";
        }
    }

    /**
     * 把授权书发送CRM
     *
     * @param custNo
     * @param cardNo
     * @param path
     * @param fileName
     * @return
     */
    public Map<String, Object> sendGrantToCrm(String custNo, String cardNo, String path, String fileName, String archType) {
        FileSystemResource fileSystemResource = new FileSystemResource(path + fileName);
        MultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<String, Object>();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            InputStream is = new BufferedInputStream(new FileInputStream(path + fileName));
            String md5 = DigestUtils.md5Hex(IOUtils.toByteArray(is));
            IOUtils.closeQuietly(is);

            String crmUrl = EurekaServer.CRM + "/app/crm/cust/arch/upload?custNo=" + custNo
                    + "&cardNo=" + cardNo + "&fileName=" + fileName + "&md5=" + md5 + "&archType=" + archType;
            HttpHeaders headers = new HttpHeaders();
            //          headers.add("Content-Type", "application/octet-stream");
            paramMap.add("file", fileSystemResource);
            HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<MultiValueMap<String, Object>>(
                    paramMap, headers);
            //          ResponseEntity entity = new RestTemplate().exchange(crmUrl, HttpMethod.POST, httpEntity, Map.class);
            ResponseEntity entity = restTemplate.exchange(crmUrl, HttpMethod.POST, httpEntity, Map.class);
            Map<String, Object> crmRetMap = (Map<String, Object>) entity.getBody();
            logger.debug("授权书发送CRM:entity=" + crmRetMap);

            if (!"00000".equals(((Map<String, Object>) crmRetMap.get("head")).get("retFlag"))) {
                resultMap.put("retCod", "00001");
                resultMap.put("retMsg", ((Map<String, Object>) entity.getBody()).get("retMsg"));
                return resultMap;
            }
        } catch (Exception e) {
            logger.error("文件上传CRM异常!" + e.getMessage());
            resultMap.put("retCod", "00002");
            resultMap.put("retMsg", "文件上传CRM异常!");
            return resultMap;
        }
        resultMap.put("retCod", "00000");
        return resultMap;
    }
}
