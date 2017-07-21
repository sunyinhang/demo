package com.haiercash.appserver.web;

import com.alibaba.fastjson.JSONObject;
import com.haiercash.appserver.service.AcquirerService;
import com.haiercash.appserver.service.AppOrderService;
import com.haiercash.appserver.service.ChService;
import com.haiercash.appserver.service.CmisApplService;
import com.haiercash.appserver.service.CommonRepaymentPersonService;
import com.haiercash.appserver.util.ConstUtil;
import com.haiercash.appserver.util.MoneyTool;
import com.haiercash.appserver.util.sign.FileSignUtil;
import com.haiercash.common.config.EurekaServer;
import com.haiercash.common.data.AppContract;
import com.haiercash.common.data.AppContractRepository;
import com.haiercash.common.data.AppOrder;
import com.haiercash.common.data.AppOrderRepository;
import com.haiercash.common.data.AppOrdernoTypgrpRelation;
import com.haiercash.common.data.AppOrdernoTypgrpRelationRepository;
import com.haiercash.common.data.ComRepayPersonAgreement;
import com.haiercash.common.data.ContractAssInfo;
import com.haiercash.common.data.ContractAssInfoRepository;
import com.haiercash.common.data.ContractInfo;
import com.haiercash.common.data.ContractPdfFile;
import com.haiercash.common.data.ContractPdfFileRepository;
import com.haiercash.common.data.Credit;
import com.haiercash.common.data.Grant;
import com.haiercash.commons.util.HttpUtil;
import com.haiercash.commons.util.ResultHead;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Controller
public class CHController extends BaseController {
    public static String MODULE_NO = "04";
    private Log logger = LogFactory.getLog(CHController.class);

    public CHController() {
        super(MODULE_NO);
    }

    @Autowired
    AppContractRepository appContractRepository;
    @Autowired
    AppOrderRepository appOrderRepository;
    @Autowired
    AppOrderService appOrderService;
    @Autowired
    ContractAssInfoRepository contractAssInfoRepository;
    @Autowired
    private CommonRepaymentPersonService commonRepaymentPersonService;
    @Autowired
    ContractPdfFileRepository contractPdfFileRepository;
    @Autowired
    CmisApplService cmisApplService;
    @Autowired
    private AcquirerService acquirerService;
    @Autowired
    private AppOrdernoTypgrpRelationRepository appOrdernoTypgrpRelationRepository;

    /**
     * ch service.
     */
    @Autowired
    private ChService chService;

    /**
     * 个人信用报告查询授权书跳转
     *
     * @param orderNo
     * @return
     */
    @RequestMapping(value = "/app/appserver/credit", method = RequestMethod.GET)
    public String creditForward(@RequestParam("orderNo") String orderNo,
            @RequestParam(value = "commonCustNo", required = false) String commonCustNo) {
        commonCustNo = StringUtils.isEmpty(commonCustNo) ? "" : commonCustNo;
        return "redirect:/static/agreement/credit.html?orderNo="+orderNo+"&commonCustNo="+commonCustNo;
    }

    /**
     * 提额征信授权书展示
     *
     * @param custName
     * @param certNo
     * @return
     */
    @RequestMapping(value = "/app/appserver/edCredit", method = RequestMethod.GET)
    public String riseEdCredit(@RequestParam("custName") String custName, @RequestParam("certNo") String certNo) {
        //return "redirect:/static/agreement/credit.html?custName="+custName+"&certNo="+certNo;
        return "/agreement/credit.html";
    }

    /**
     * 共同还款人协议
     *
     * @param orderNo      订单号
     * @param commonCustNo 共同还款人的编号
     * @return
     */
    @RequestMapping(value = "/app/appserver/comRepayPerson", method = RequestMethod.GET)
    public String comRepayPerson(@RequestParam("orderNo") String orderNo,
            @RequestParam("commonCustNo") String commonCustNo) {
        return "redirect:/static/agreement/commonRepayPerson.html?orderNo="+orderNo+"&commonCustNo="+commonCustNo;
    }

    /**
     * 获取共同还款人协议所需要的数据
     *
     * @param orderNo      订单号
     * @param commonCustNo  共同还款人的编号
     * @return
     */
    @RequestMapping(value = "/app/appserver/getComRepayPersonInfo", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> getComRepayPersonInfo(@RequestParam("orderNo") String orderNo,
            @RequestParam("commonCustNo") String commonCustNo) {
        // 通过流水号获取订单
        AppOrdernoTypgrpRelation relation = appOrdernoTypgrpRelationRepository.findOne(orderNo);
        if (null == relation) {
            return fail("01", "没有该订单!");
        }
        AppOrder appOrder = acquirerService.getAppOrderFromAcquirer(relation.getApplSeq(), super.getChannelNO());
        ComRepayPersonAgreement comRepayPersonAgreement = new ComRepayPersonAgreement();
        comRepayPersonAgreement.setCustName(appOrder.getCustName());// 借款人客户姓名
        comRepayPersonAgreement.setCustVerifyNo(appOrder.getIdNo());// 借款人身份证号
        comRepayPersonAgreement.setTypDesc(appOrder.getTypDesc());// 贷款品种名称
        comRepayPersonAgreement.setApplyAmt(FileSignUtil.amtConvert(appOrder.getApplyAmt()));// 贷款金额(小写)
        if (!StringUtils.isEmpty(appOrder.getApplyAmt())) {
            comRepayPersonAgreement.setApplyBigAmt(
                    MoneyTool.change(Double.parseDouble(FileSignUtil.amtConvert(appOrder.getApplyAmt()))));// 贷款金额(大写)
        }
        comRepayPersonAgreement.setApplyTnr(appOrder.getApplyTnr());// 贷款期限
        comRepayPersonAgreement.setApplCde(appOrder.getApplCde());// 贷款编号
        Calendar rightNow = Calendar.getInstance();
        comRepayPersonAgreement.setYear(String.valueOf(rightNow.get(Calendar.YEAR)));// 年
        comRepayPersonAgreement.setMonth(String.valueOf(rightNow.get(Calendar.MONTH) + 1));// 月
        comRepayPersonAgreement.setDay(String.valueOf(rightNow.get(Calendar.DAY_OF_MONTH)));// 日
        // 共同还款人客户编号获取共同还款人信息
        String crmUrl = EurekaServer.CRM + "/app/crm/cust/queryCustRealInfoByCustNo?custNo="
                + commonCustNo;
        String json = HttpUtil.restGet(crmUrl, super.getToken());
        if (StringUtils.isEmpty(json)) {
            return fail("02", "没用该共同还款人!");
        }
        json = json.replaceAll("null", "\"\"");
        Map<String, Object> map = HttpUtil.json2Map(json);
        if (HttpUtil.isSuccess(map)) {
            Map<String, Object> mapBody = HttpUtil.json2Map(map.get("body").toString());
            comRepayPersonAgreement.setCommonCustName(mapBody.get("custName").toString());// 共同还款人姓名
            comRepayPersonAgreement.setCommonCustVerifyNo(mapBody.get("certNo").toString());// 共同还款人身份证号
        }

        return success(comRepayPersonAgreement);
    }

    /**
     * 消费信贷服务协议（V1.0）页面跳转
     *
     * @param orderNo
     * @return
     */
    @RequestMapping(value = "/app/appserver/register", method = RequestMethod.GET)
    public String registForward(@RequestParam(value = "orderNo",required = false) String orderNo,@RequestParam(value = "custName",required = false) String custName) {
        return "redirect:/static/agreement/regist.html?orderNo="+orderNo+"&custName="+custName;
    }
    /**
     * 美凯龙额度使用须知页面跳转
     *
     * @param custName
     * @return
     */
    @RequestMapping(value = "/app/appserver/mkledsyxz", method = RequestMethod.GET)
    public String mkledsyxzForward(@RequestParam("custName") String custName) {
        return "redirect:/static/agreement/EDSYXZ.html?custName="+custName;
    }

    /**
     * 银行卡变更：APP授权书 展示
     *
     * @param custNo
     * @param cardNo
     * @return
     */
    @RequestMapping(value = "/app/appserver/grant", method = RequestMethod.GET)
    public String grantForward(@RequestParam("custNo") String custNo, @RequestParam("cardNo") String cardNo) {
        return "redirect:/static/agreement/grant.html?custNo="+custNo+"&cardNo="+cardNo;
    }

    /**
     * 获取协议信息
     *
     * @param orderNo 订单编号
     * @return 授权人姓名, 身份证号, 年, 月, 日
     */
    @RequestMapping(value = "/app/appserver/getAgreementInfo", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> getAgreementInfo(@RequestParam(value = "orderNo") String orderNo,
            @RequestParam(value = "commonCustNo", required = false) String commonCustNo) {
        // 获取 授权人姓名custName, 身份证号 ， 年 月 日
        if (StringUtils.isEmpty(orderNo)) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "订单号不能为空");
        }
        AppOrdernoTypgrpRelation relation = appOrdernoTypgrpRelationRepository.findOne(orderNo);
        if (relation == null) {
            return fail("09", "订单信息不存在");
        }
        AppOrder appOrder = acquirerService.getAppOrderFromAcquirer(relation.getApplSeq(), relation.getChannelNo());
        if (StringUtils.isEmpty(appOrder)) {
            return success();
        }
        Credit credit = new Credit();
        if (StringUtils.isEmpty(commonCustNo) || "null".equals(commonCustNo)) {
            credit.setCustNo(appOrder.getCustNo());
            credit.setCustName(appOrder.getCustName());
            credit.setIdNo(appOrder.getIdNo());
        } else {
            // 共同还款人客户编号获取共同还款人信息
            String crmUrl = EurekaServer.CRM + "/app/crm/cust/queryCustRealInfoByCustNo?custNo="
                    + commonCustNo;
            String json = HttpUtil.restGet(crmUrl);
            if (StringUtils.isEmpty(json)) {
                return fail("06", "查询共同还款人错误!");
            }
            json = json.replaceAll("null", "\"\"");
            Map<String, Object> comMap = HttpUtil.json2Map(json);
            if (HttpUtil.isSuccess(comMap)) {
                Map<String, Object> mapBody = HttpUtil.json2Map(comMap.get("body").toString());
                credit.setCustNo(commonCustNo);
                credit.setCustName(mapBody.get("custName").toString());// 共同还款人姓名
                credit.setIdNo(mapBody.get("certNo").toString());// 共同还款人身份证号
            }
        }

        Calendar rightNow = Calendar.getInstance();
        credit.setYear(String.valueOf(rightNow.get(Calendar.YEAR)));
        credit.setMonth(String.valueOf(rightNow.get(Calendar.MONTH) + 1));
        credit.setDay(String.valueOf(rightNow.get(Calendar.DAY_OF_MONTH)));
        return success(credit);
    }

    /**
     * APP授权书信息获取
     *
     * @param custNo
     * @param cardNo
     * @return
     */
    @RequestMapping(value = "/app/appserver/getGrantInfo", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> getGrantInfo(@RequestParam("custNo") String custNo,
            @RequestParam("cardNo") String cardNo) {
        String custName = "", certNo = "", bankName = "";
        //通过custNo获取custName和certNo
        String crmCustInfoUrl =
                EurekaServer.CRM + "/app/crm/cust/queryCustRealInfoByCustNo?custNo=" + custNo;
        String custInfoJson = HttpUtil.restGet(crmCustInfoUrl);
        if (StringUtils.isEmpty(custInfoJson)) {
            return fail("01", "查询客户信息错误!");
        }
        custInfoJson = custInfoJson.replaceAll("null", "\"\"");
        Map<String, Object> comMap = HttpUtil.json2Map(custInfoJson);
        if (HttpUtil.isSuccess(comMap)) {
            Map<String, Object> mapBody = HttpUtil.json2Map(comMap.get("body").toString());
            custName = mapBody.get("custName").toString();
            certNo = mapBody.get("certNo").toString();
        }
        //通过cardNo获取bankName
        //	String crmBankInfoUrl = CommonProperties.get("address.gateUrl") + "/app/crm/cust/getCustBankCardByCardNo?cardNo=" + cardNo;
        //获取银行卡名称不在从上面的接口查询，而是从getBankInfo接口查询
        String crmBankInfoUrl = EurekaServer.CRM + "/app/crm/cust/getBankInfo?cardNo=" + cardNo;
        logger.info("CRM 通过getBankInfo接口查询银行卡名称：Url" + crmBankInfoUrl);
        String bankInfoJson = HttpUtil.restGet(crmBankInfoUrl);
        logger.info("CRM  getBankInfo接口查询银行卡名称返回：" + bankInfoJson);
        if (StringUtils.isEmpty(bankInfoJson)) {
            return fail("02", "查询银行卡信息错误!");
        }
        bankInfoJson = bankInfoJson.replaceAll("null", "\"\"");
        Map<String, Object> bankMap = HttpUtil.json2Map(bankInfoJson);
        if (HttpUtil.isSuccess(bankMap)) {
            Map<String, Object> mapBody = HttpUtil.json2Map(bankMap.get("body").toString());
            bankName = mapBody.get("bankName").toString();
        }

        Grant grant = new Grant();
        grant.setCustName(custName);
        grant.setCardNo(cardNo);
        grant.setBankName(bankName);
        Calendar rightNow = Calendar.getInstance();
        grant.setYear(String.valueOf(rightNow.get(Calendar.YEAR)));
        grant.setMonth(String.valueOf(rightNow.get(Calendar.MONTH) + 1));
        grant.setDay(String.valueOf(rightNow.get(Calendar.DAY_OF_MONTH)));
        logger.debug("APP授权信息grant=" + grant.toString());
        return success(grant);
    }

    /**
     * 合同展示页面跳转
     *
     * @param applseq 申请流水号
     * @return
     */
    @RequestMapping(value = "/app/appserver/contract", method = RequestMethod.GET)
    public String contractForward(@RequestParam("applseq") String applseq) {
        logger.debug("===applseq:" + applseq);
        AppOrder appOrder = appOrderService.getAppOrderFromACQ(applseq,"1");
        if (null == appOrder) {
            appOrder = appOrderService.getAppOrderFromCmis(applseq, "1");
            if (appOrder == null) {
                logger.debug("未从cmis查得该订单");
                return "";
            }
        }

        logger.debug("appOrder:" + appOrder.toString());
        logger.debug("getTypLevelTwo:" + appOrder.getTypLevelTwo());
        if (null == appOrder.getTypLevelTwo() || "".equals(appOrder.getTypLevelTwo())) {
            logger.error("贷款品种类别为空!");
            return "";
        }
        // logger.debug("typLevelTwo="+appOrder.getTypLevelTwo());
        // 通过贷款品种类别查询所对应的合同:
        // AppContract appContract =
        // appContractRepository.findByContType(appOrder.getTypLevelTwo());
        List<AppContract> appContractList = appContractRepository.findByContType(appOrder.getTypLevelTwo());
        logger.debug("appContractList:" + appContractList);
        if (appContractList == null || appContractList.size() == 0) {
            return "";
        }
        AppContract appContract;
        if (appContractList.size() > 1) {
            appContract = appContractRepository
                    .findByContTypeAndApplyType(appOrder.getTypLevelTwo(), appOrder.getTypCde());
            logger.debug("appContract:" + appContract);
            if (appContract == null) {
                logger.debug("贷款品种错误!");
                return "";
            }
        } else {
            appContract = appContractList.get(0);
        }
        logger.debug("/static/contract/" + appContract.getContCode() + ".html?applseq="+applseq);
        return "redirect:/static/contract/" + appContract.getContCode() + ".html?applseq="+applseq;
    }


    /**
     * 合同展示页面跳转(收单系统查询订单)
     *
     * @param applseq 申请流水号
     * @return
     */
    @RequestMapping(value = "/app/appserver/contractForACQ", method = RequestMethod.GET)
    public String contractForACQ(@RequestParam("applseq") String applseq) {
        AppOrder appOrder = appOrderService.getAppOrderFromACQ(applseq, "1");
        if (appOrder == null) {
            logger.debug("未从收单系统查得该订单");
            return "";
        }
        logger.debug("appOrder:" + appOrder.toString());
        logger.debug("getTypLevelTwo:" + appOrder.getTypLevelTwo());
        if (null == appOrder.getTypLevelTwo() || "".equals(appOrder.getTypLevelTwo())) {
            logger.error("贷款品种类别为空!");
            return "";
        }
        // logger.debug("typLevelTwo="+appOrder.getTypLevelTwo());
        // 通过贷款品种类别查询所对应的合同:
        // AppContract appContract =
        // appContractRepository.findByContType(appOrder.getTypLevelTwo());
        List<AppContract> appContractList = appContractRepository.findByContType(appOrder.getTypLevelTwo());
        logger.debug("appContractList:" + appContractList);
        if (appContractList == null || appContractList.size() == 0) {
            return "";
        }
        AppContract appContract;
        if (appContractList.size() > 1) {
            appContract = appContractRepository
                    .findByContTypeAndApplyType(appOrder.getTypLevelTwo(), appOrder.getTypCde());
            logger.debug("appContract:" + appContract);
            if (appContract == null) {
                logger.debug("贷款品种错误!");
                return "";
            }
        } else {
            appContract = appContractList.get(0);
        }
        logger.debug("/static/contract/" + appContract.getContCode() + ".html?applseq="+applseq);
        return "redirect:/static/contract/" + appContract.getContCode() + ".html?applseq="+applseq;
    }


    /**
     * 返回合同所需要的数据，合同预览页面使用（js）
     *从收单系统获取数据
     * @param applseq 流水号
     * @return
     */
    @RequestMapping(value = "/app/appserver/getContractInfo", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> getContractInfo(@RequestParam(value = "applseq") String applseq) {
        // 查询合同所需要的数据:
        logger.info("------查询html展示数据开始------");
        AppOrder appOrder = appOrderService.getAppOrderFromACQ(applseq,"1");
        if (null == appOrder) {
            return fail("01", "没有该订单!");
        }
        ContractInfo contractInfo = new ContractInfo();
        contractInfo.setContractNo(""); // 合同编号：暂时没有数据
        contractInfo.setCustName(appOrder.getCustName()); // 借款人(甲方)姓名
        contractInfo.setIdentifyNo(appOrder.getIdNo()); // 身份证号码
        contractInfo.setCustMobile(appOrder.getIndivMobile()); // 联系电话
        contractInfo.setPurpose(appOrder.getPurpose()); // 贷款用途
        contractInfo.setCooprName(appOrder.getCooprName()); // 门店名称

        //获取门店所在市cityName   (crm:/app/crm/cust/getStore   门店代码:cooprCde)
        if (!StringUtils.isEmpty(appOrder.getCooprCde())) {
            String cooprInfoUrl = EurekaServer.CRM + "/app/crm/cust/getStore?storeNo=" + appOrder
                    .getCooprCde();
            String cooprInfoJson = HttpUtil.restGet(cooprInfoUrl);
            logger.debug("获取门店信息cooprInfoJson=" + cooprInfoJson);
            if (!StringUtils.isEmpty(cooprInfoJson)) {
                cooprInfoJson = cooprInfoJson.replaceAll("null", "\"\"");
                Map<String, Object> cooprInfoMap = HttpUtil.json2Map(cooprInfoJson);
                if (HttpUtil.isSuccess(cooprInfoMap)) {
                    Map<String, Object> mapBody = HttpUtil.json2Map(cooprInfoMap.get("body").toString());
                    contractInfo.setCooprCityName(
                            (String) mapBody.get("cityName") == null ? "" : (String) mapBody.get("cityName"));
                }
            }
        }
        //根据商户编码获取商户名称
        String merchName = "";
        if (!StringUtils.isEmpty(appOrder.getMerchNo())) {
            String url = EurekaServer.CRM + "/pub/crm/cust/getMerchInfoByMerchNo?merchNo=" + appOrder.getMerchNo()
                    .toString();
            String json = HttpUtil.restGet(url, super.getToken());
            logger.info("通过crm请求的根据商户编号查询商户的信息的url" + url);
            Map<String, Object> resultMap = HttpUtil.json2Map(json);
            logger.info("通过crm请求的根据商户编号查询商户的信息为：" + resultMap);
            if (!StringUtils.isEmpty(resultMap.get("body"))) {
                Map<String, Object> bodyMap = HttpUtil.json2Map(resultMap.get("body").toString());
                merchName = StringUtils.isEmpty(bodyMap.get("merchChName")) ? "" : (String) bodyMap.get("merchChName");
            }
        }
        contractInfo.setMerchName(merchName);
        // 获取居住地址、邮箱、邮编 /app/crm/cust/getCustExtInfo
        // 单位信息:dwInfo、居住信息:jzInfo、个人信息:grInfo、为空：所有信息
        String custInfoUrl =
                EurekaServer.CRM + "/app/crm/cust/getAllCustExtInfo?custNo=" + appOrder.getCustNo();
        String custInfoUrlJson = HttpUtil.restGet(custInfoUrl);
        if (StringUtils.isEmpty(custInfoUrlJson)) {
            logger.debug("该客户不存在!");
            contractInfo.setCustAddress(appOrder.getLiveInfo()); // 居住地址
            contractInfo.setEmail(appOrder.getEmail()); // 邮箱
            contractInfo.setLiveZip(""); // 邮编
        } else {
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
                contractInfo.setCustAddress(address + ((String) mapBody.get("liveAddr") == null ?
                        "" :
                        (String) mapBody.get("liveAddr")));// 居住地址
                contractInfo.setEmail((String) mapBody.get("email") == null ? "" : (String) mapBody.get("email")); // 邮箱
                contractInfo.setLiveZip(
                        (String) mapBody.get("liveZip") == null ? "" : (String) mapBody.get("liveZip")); // 邮编
            } else {
                contractInfo.setCustAddress(appOrder.getLiveInfo()); // 居住地址
                contractInfo.setEmail(appOrder.getEmail()); // 邮箱
                contractInfo.setLiveZip(""); // 邮编
            }
        }

        // 合同展示借款金额：
        String applyAmt = FileSignUtil.amtConvert((String) appOrder.getApplyAmt());// 借款金额
        String apprvAmt = FileSignUtil.amtConvert((String) appOrder.getApprvAmt());// 审批金额
        if (StringUtils.isEmpty(apprvAmt) || "null".equals(apprvAmt)) {
            contractInfo.setApplyAmtSmall(applyAmt);
            if (null != applyAmt && !"".equals(applyAmt)) {
                contractInfo.setApplyAmtBig(MoneyTool.change(Double.parseDouble(applyAmt)));
            } else {
                contractInfo.setApplyAmtBig("");
            }
        } else {
            contractInfo.setApplyAmtSmall(apprvAmt);
            contractInfo.setApplyAmtBig(MoneyTool.change(Double.parseDouble(apprvAmt)));
        }

        contractInfo.setApplyTnr(appOrder.getApplyTnr()); // 借款期限
        contractInfo.setFstPaySmall(FileSignUtil.amtConvert(appOrder.getFstPay())); // 首付金额小写
        if (StringUtils.isEmpty(appOrder.getFstPay())) { // 首付金额大写
            contractInfo.setFstPayBig("");
        } else {
            contractInfo
                    .setFstPayBig(MoneyTool.change(Double.parseDouble(FileSignUtil.amtConvert(appOrder.getFstPay()))));
        }
        logger.info("获取放款账号信息");
        // 贷款类型为耐用消费品贷款
       /* if ("01".equals(appOrder.getTypGrp())) {
            String url = EurekaServer.CRM + "/pub/crm/cust/getMerchInfoByMerchNo" + "?merchNo="
                    + appOrder.getMerchNo();
            String json = HttpUtil.restGet(url);
            if (StringUtils.isEmpty(json)) {
                return fail("02", "商户编号为空!");
            }
            json = json.replaceAll("null", "\"\"");
            Map<String, Object> map = HttpUtil.json2Map(json);
            //放款卡号设置
            if (HttpUtil.isSuccess(map)) {
                //获取贷款品种代码
                String typCde = String.valueOf(appOrder.getTypCde());
                String typCdeUrl =
                        EurekaServer.CMISPROXY + "/api/pLoanTyp/find?typCde=" + typCde;
                String typCdeJson = HttpUtil.restGet(typCdeUrl);
                String dnTyp = "";//固定放款途径，如果为9001则放款到商户内转账号
                if (StringUtils.isEmpty(json)) {
                    logger.info("CMIS==>贷款品种详情接口查询失败！");
                } else {
                    Map<String, Object> typCdemap = HttpUtil.json2Map(typCdeJson);
                    //获取固定放款途径	dnTyp
                    dnTyp = String.valueOf(typCdemap.get("dnTyp"));
                }
                Map<String, Object> mapBody = HttpUtil.json2Map(map.get("body").toString());
                if ("9001".equals(dnTyp)) {
                    //账户名称取默认的开户账户名（外转）；如果没有配置外转账号信息，则取商户名称
                    String accName = mapBody.get("acctName") == null ? "" : mapBody.get("acctName").toString();
                    if (StringUtils.isEmpty(accName)) {
                        accName = String.valueOf(mapBody.get("merchChName"));
                    }
                    contractInfo.setApplAcNam(accName); // 放款账号户名
                    contractInfo.setAccBankCde("0000"); // 放款开户银行代码
                    contractInfo.setAccBankName("海尔集团财务有限责任公司"); // 放款开户银行名
                    contractInfo.setApplCardNo((String) mapBody.get("inFinaAcct") == null ?
                            "" :
                            (String) mapBody.get("inFinaAcct")); // 放款卡号
                    contractInfo.setAccAcBchCde("0000"); // 放款开户行支行代码
                } else {
                    contractInfo.setApplAcNam(
                            (String) mapBody.get("acctName") == null ? "" : (String) mapBody.get("acctName")); // 放款账号户名
                    contractInfo.setAccBankCde(
                            (String) mapBody.get("bankCde") == null ? "" : (String) mapBody.get("bankCde")); // 放款开户银行代码
                    contractInfo.setAccBankName((String) mapBody.get("bankName") == null ?
                            "" :
                            (String) mapBody.get("bankName")); // 放款开户银行名
                    contractInfo.setApplCardNo(
                            (String) mapBody.get("acctNo") == null ? "" : (String) mapBody.get("acctNo")); // 放款卡号
                    contractInfo.setAccAcBchCde(
                            (String) mapBody.get("bchCde") == null ? "" : (String) mapBody.get("bchCde")); // 放款开户行支行代码
                }

            }
        } else {// 02：一般消费贷款
            if (StringUtils.isEmpty(appOrder.getApplAcNam())) {
                String url =
                        EurekaServer.CRM + "/app/crm/cust/getDefaultBankCard?custNo=" + appOrder
                                .getCustNo();
                String json = HttpUtil.restGet(url);
                if (StringUtils.isEmpty(json)) {
                    return fail("03", "不存在该客户默认放款银行卡信息!");
                }
                json = json.replaceAll("null", "\"\"");
                Map<String, Object> map = HttpUtil.json2Map(json);
                if (HttpUtil.isSuccess(map)) {
                    Map<String, Object> mapBody = HttpUtil.json2Map(map.get("body").toString());
                    contractInfo.setApplAcNam((String) mapBody.get("custName")); // 放款账号户名
                    contractInfo.setAccBankCde((String) mapBody.get("bankCode")); // 放款开户银行代码
                    contractInfo.setAccBankName((String) mapBody.get("bankName")); // 放款开户银行名
                    contractInfo.setApplCardNo((String) mapBody.get("cardNo")); // 放款卡号
                    contractInfo.setAccAcBchCde((String) mapBody.get("accBchCde")); // 放款开户行支行代码
                }
            } else {
                contractInfo.setApplAcNam(appOrder.getApplAcNam()); // 放款账号户名
                contractInfo.setAccBankCde(appOrder.getAccBankCde()); // 放款开户银行代码
                contractInfo.setAccBankName(appOrder.getAccBankName()); // 放款开户银行名
                contractInfo.setApplCardNo(appOrder.getApplCardNo()); // 放款卡号
                contractInfo.setAccAcBchCde(appOrder.getAccAcBchCde()); // 放款开户行支行代码
            }
        }*/
        contractInfo.setApplAcNam(appOrder.getApplAcNam()); // 放款账号户名
        contractInfo.setAccBankCde(appOrder.getAccBankCde()); // 放款开户银行代码
        if(StringUtils.isEmpty(appOrder.getAccBankName())){//放款开户银行名
            if(appOrder.getAccAcBchName() != null){
                contractInfo.setAccBankName(appOrder.getAccAcBchName());
            }else{
                contractInfo.setAccBankName("");
            }
        }else{
            contractInfo.setAccBankName(appOrder.getAccBankName()); // 放款开户银行名
        }
        contractInfo.setApplCardNo(appOrder.getApplCardNo()); // 放款卡号
        contractInfo.setAccAcBchCde(appOrder.getAccAcBchCde()); // 放款开户行支行代码
        // 通过还款方式查询合同中所对应的选择条数
        List<AppContract> contractList = appContractRepository.findByContType((String) appOrder.getTypLevelTwo());
        List<ContractAssInfo> contractAssInfoList = new ArrayList<>();
        if (contractList.size() > 1) {
            // 利率、手续费率、管理费率
            //			if (null != appOrder.getPayMtd() && !"".equals(appOrder.getPayMtd())) {
            contractAssInfoList = contractAssInfoRepository
                    .findByPayMtdAndTypLevelTwoAndTypCde((String) appOrder.getPayMtd(),
                            (String) appOrder.getTypLevelTwo(), (String) appOrder.getTypCde());
            //			}
        } else if (contractList.size() == 1) {
            //			if (null != appOrder.getPayMtd() && !"".equals(appOrder.getPayMtd())) {
            contractAssInfoList = contractAssInfoRepository
                    .findByPayMtdAndTypLevelTwo((String) appOrder.getPayMtd(), (String) appOrder.getTypLevelTwo());
            //			}
        }
        if (null != contractAssInfoList) {
            if (contractAssInfoList.size() == 1) {
                ContractAssInfo contractAssInfo = contractAssInfoList.get(0);
                contractInfo.setApplyTnrTyp(
                        contractAssInfo.getContractMtdType() == null ? "" : contractAssInfo.getContractMtdType());
                contractInfo.setMtdCde(
                        contractAssInfo.getContractMtdType() == null ? "" : contractAssInfo.getContractMtdType());
                contractInfo.setRate(contractAssInfo.getApplyRate() == null ? "" : contractAssInfo.getApplyRate());
                contractInfo.setApplyManagerRate(
                        contractAssInfo.getApplyManagerRate() == null ? "" : contractAssInfo.getApplyManagerRate());
                contractInfo.setFeeRate(contractAssInfo.getFeeRate() == null ? "" : contractAssInfo.getFeeRate());
            } else if (contractAssInfoList.size() > 1) {
                List<ContractAssInfo> AssInfoList = new ArrayList<>();
                if (contractList.size() == 1) {
                    AssInfoList = contractAssInfoRepository
                            .findByPayMtdAndTypLevelTwoAndApplyTnr((String) appOrder.getPayMtd(),
                                    (String) appOrder.getTypLevelTwo(), (String) appOrder.getApplyTnr());
                } else if (contractList.size() > 1) {
                    AssInfoList = contractAssInfoRepository
                            .findByPayMtdAndTypLevelTwoAndTypCdeAndApplyTnr((String) appOrder.getPayMtd(),
                                    (String) appOrder.getTypLevelTwo(), (String) appOrder.getTypCde(),
                                    (String) appOrder.getApplyTnr());
                }
                if (null != AssInfoList) {
                    if (AssInfoList.size() == 1) {
                        ContractAssInfo contractAssInfo = AssInfoList.get(0);
                        contractInfo.setApplyTnrTyp(contractAssInfo.getContractMtdType() == null ?
                                "" :
                                contractAssInfo.getContractMtdType());
                        contractInfo.setMtdCde(contractAssInfo.getContractMtdType() == null ?
                                "" :
                                contractAssInfo.getContractMtdType());
                        contractInfo
                                .setRate(contractAssInfo.getApplyRate() == null ? "" : contractAssInfo.getApplyRate());
                        contractInfo.setApplyManagerRate(contractAssInfo.getApplyManagerRate() == null ?
                                "" :
                                contractAssInfo.getApplyManagerRate());
                        contractInfo
                                .setFeeRate(contractAssInfo.getFeeRate() == null ? "" : contractAssInfo.getFeeRate());
                    } else if (AssInfoList.size() > 1) {
                        // 查询是否有共同还款人
                        Map<String, Object> countPersonMap = commonRepaymentPersonService.countCommonRepaymentPerson(applseq);
                        if (!HttpUtil.isSuccess(countPersonMap)) {
                            return countPersonMap;
                        }
                        int count = (int) ((Map<String, Object>) countPersonMap.get("body")).get("count");
                        String hasComRepay;
                        if (count == 0) {// 没有共同还款人
                            hasComRepay = "0";
                        } else {// 有共同还款人
                            hasComRepay = "1";
                        }
                        ContractAssInfo contractAssInfo = null;
                        if (contractList.size() == 1) {
                            contractAssInfo = contractAssInfoRepository
                                    .findByHasComRepayOne((String) appOrder.getPayMtd(),
                                            (String) appOrder.getTypLevelTwo(), (String) appOrder.getApplyTnr(),
                                            hasComRepay);
                        } else if (contractList.size() > 1) {
                            contractAssInfo = contractAssInfoRepository
                                    .findByHasComRepayMore((String) appOrder.getPayMtd(),
                                            (String) appOrder.getTypLevelTwo(), (String) appOrder.getTypCde(),
                                            (String) appOrder.getApplyTnr(), hasComRepay);
                        }
                        if (null != contractAssInfo) {
                            contractInfo.setApplyTnrTyp(contractAssInfo.getContractMtdType() == null ?
                                    "" :
                                    contractAssInfo.getContractMtdType());
                            contractInfo.setMtdCde(contractAssInfo.getContractMtdType() == null ?
                                    "" :
                                    contractAssInfo.getContractMtdType());
                            contractInfo.setRate(
                                    contractAssInfo.getApplyRate() == null ? "" : contractAssInfo.getApplyRate());
                            contractInfo.setApplyManagerRate(contractAssInfo.getApplyManagerRate() == null ?
                                    "" :
                                    contractAssInfo.getApplyManagerRate());
                            contractInfo.setFeeRate(
                                    contractAssInfo.getFeeRate() == null ? "" : contractAssInfo.getFeeRate());
                        }
                    }
                }
            }
        }

        // 获取剩余可用额度(目前只有 个人消费借款合同-自主支付版本-支用时使用.docx 合同用)
        String certType = "20"; // 身份证：20
        String idNo = (String) appOrder.getIdNo();// 证件号码
        //String EdCheckUrl = CommonProperties.get("address.gateUrl") + "/app/appserver/getEdCheck?idTyp=" + certType + "&idNo=" + idNo;
        //Map<String, Object> EdCheckMap = HttpUtil.restGetMap(EdCheckUrl);
        Map<String, Object> EdCheckMap = cmisApplService.getEdCheck(certType, idNo, super.getToken());
        logger.info(EdCheckMap);
        ResultHead head = (ResultHead) EdCheckMap.get("head");
        String flag = head.getRetFlag();
        String msg = head.getRetMsg();
        if (Objects.equals(flag, "00000")) {
            //if (HttpUtil.isSuccess(EdCheckMap)) {
            Map<String, Object> edCheckBody = (Map<String, Object>) EdCheckMap.get("body");
            String edAmt = String.valueOf(edCheckBody.get("crdNorAvailAmt")) == null ?
                    "" :
                    FileSignUtil.amtConvert(String.valueOf(edCheckBody.get("crdNorAvailAmt")));// 自主支付可用额度金额
            String applAmt = contractInfo.getApplyAmtSmall();
            Double amt = new BigDecimal(edAmt).subtract(new BigDecimal(applAmt)).doubleValue();
            contractInfo
                    .setCrdNorAvailAmt(String.valueOf(amt) == null ? "" : FileSignUtil.amtConvert(String.valueOf(amt)));
            if (!StringUtils.isEmpty(String.valueOf(amt))) {
                contractInfo.setBigCrdNorAvailAmt(
                        MoneyTool.change(Double.parseDouble(FileSignUtil.amtConvert(String.valueOf(amt)))));
            }
        }

        // 还款银行卡信息
        if (StringUtils.isEmpty(appOrder.getRepayApplCardNo())) {
            // 还款银行卡信息(查询CRM默认还款银行卡信息)
            String url = EurekaServer.CRM + "/app/crm/cust/getDefaultBankCard?custNo=" + appOrder
                    .getCustNo();
            String json = HttpUtil.restGet(url);
            if (StringUtils.isEmpty(json)) {
                return fail("04", "不存在该客户默认还款银行卡信息！");
            }
            json = json.replaceAll("null", "\"\"");
            Map<String, Object> map = HttpUtil.json2Map(json);
            if (HttpUtil.isSuccess(map)) {
                Map<String, Object> mapBody = HttpUtil.json2Map(map.get("body").toString());
                contractInfo.setRepayApplAcNam(
                        (String) mapBody.get("custName") == null ? "" : (String) mapBody.get("custName")); // 还款账号户名
                contractInfo.setRepayAccBankName(
                        (String) mapBody.get("bankName") == null ? "" : (String) mapBody.get("bankName")); // 还款开户银行名
                contractInfo.setRepayApplCardNo(
                        (String) mapBody.get("cardNo") == null ? "" : (String) mapBody.get("cardNo")); // 还款卡号
            }
        } else {
            contractInfo.setRepayApplAcNam(appOrder.getRepayApplAcNam()); // 还款账号户名
            contractInfo.setRepayAccBankName(appOrder.getRepayAccBankName()); // 还款开户银行名
            contractInfo.setRepayApplCardNo(appOrder.getRepayApplCardNo()); // 还款卡号
        }

        contractInfo.setAccName(appOrder.getCustName()); // 甲方签名
        contractInfo.setLenderName(""); // 乙方签名
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        contractInfo.setDate(format.format(new Date())); // 日期
        Calendar rightNow = Calendar.getInstance();
        contractInfo.setYear(String.valueOf(rightNow.get(Calendar.YEAR)));
        contractInfo.setMonth(String.valueOf(rightNow.get(Calendar.MONTH) + 1));
        contractInfo.setDay(String.valueOf(rightNow.get(Calendar.DAY_OF_MONTH)));
        logger.info("查询合同展示页面参数：" + JSONObject.toJSON(contractInfo).toString());
        logger.info("------查询html展示数据结束------");
        return success(contractInfo);
    }

    /**
     * 返回合同所需要的数据，合同预览页面使用（js）
     *从收单系统获取数据
     * @param applseq 流水号
     * @return
     */
    @RequestMapping(value = "/app/appserver/getContractInfoFromACQ", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> getContractInfoFromACQ(@RequestParam(value = "applseq") String applseq) {
        AppOrder appOrder = appOrderService.getAppOrderFromACQ(applseq, "1");

        if (appOrder == null) {
            logger.debug("未从收单系统查得该订单");
            return fail("01", "没有该订单！");
        }
        ContractInfo contractInfo = new ContractInfo();
        contractInfo.setContractNo(""); // 合同编号：暂时没有数据
        contractInfo.setCustName(appOrder.getCustName()); // 借款人(甲方)姓名
        contractInfo.setIdentifyNo(appOrder.getIdNo()); // 身份证号码
        contractInfo.setCustMobile(appOrder.getIndivMobile()); // 联系电话
        contractInfo.setPurpose(appOrder.getPurpose()); // 贷款用途
        contractInfo.setCooprName(appOrder.getCooprName()); // 门店名称

        //获取门店所在市cityName   (crm:/app/crm/cust/getStore   门店代码:cooprCde)
        if (!StringUtils.isEmpty(appOrder.getCooprCde())) {
            String cooprInfoUrl = EurekaServer.CRM + "/app/crm/cust/getStore?storeNo=" + appOrder
                    .getCooprCde();
            String cooprInfoJson = HttpUtil.restGet(cooprInfoUrl);
            logger.debug("获取门店信息cooprInfoJson=" + cooprInfoJson);
            if (!StringUtils.isEmpty(cooprInfoJson)) {
                cooprInfoJson = cooprInfoJson.replaceAll("null", "\"\"");
                Map<String, Object> cooprInfoMap = HttpUtil.json2Map(cooprInfoJson);
                if (HttpUtil.isSuccess(cooprInfoMap)) {
                    Map<String, Object> mapBody = HttpUtil.json2Map(cooprInfoMap.get("body").toString());
                    contractInfo.setCooprCityName(
                            (String) mapBody.get("cityName") == null ? "" : (String) mapBody.get("cityName"));
                }
            }
        }

        contractInfo.setCustAddress(appOrder.getLiveInfo()); // 居住地址
        contractInfo.setEmail(appOrder.getEmail()); // 邮箱
        contractInfo.setLiveZip(""); // 邮编

        // 合同展示借款金额：
        String applyAmt = FileSignUtil.amtConvert((String) appOrder.getApplyAmt());// 借款金额
        String apprvAmt = FileSignUtil.amtConvert((String) appOrder.getApprvAmt());// 审批金额
        if (StringUtils.isEmpty(apprvAmt) || "null".equals(apprvAmt)) {
            contractInfo.setApplyAmtSmall(applyAmt);
            if (null != applyAmt && !"".equals(applyAmt)) {
                contractInfo.setApplyAmtBig(MoneyTool.change(Double.parseDouble(applyAmt)));
            } else {
                contractInfo.setApplyAmtBig("");
            }
        } else {
            contractInfo.setApplyAmtSmall(apprvAmt);
            contractInfo.setApplyAmtBig(MoneyTool.change(Double.parseDouble(apprvAmt)));
        }

        contractInfo.setApplyTnr(appOrder.getApplyTnr()); // 借款期限
        contractInfo.setFstPaySmall(FileSignUtil.amtConvert(appOrder.getFstPay())); // 首付金额小写
        if (StringUtils.isEmpty(appOrder.getFstPay())) { // 首付金额大写
            contractInfo.setFstPayBig("");
        } else {
            contractInfo
                    .setFstPayBig(MoneyTool.change(Double.parseDouble(FileSignUtil.amtConvert(appOrder.getFstPay()))));
        }
        logger.info("获取放款账号信息");
        // 贷款类型为耐用消费品贷款
        /*if ("01".equals(appOrder.getTypGrp())) {
            String url = EurekaServer.CRM + "/pub/crm/cust/getMerchInfoByMerchNo" + "?merchNo="
                    + appOrder.getMerchNo();
            String json = HttpUtil.restGet(url);
            if (StringUtils.isEmpty(json)) {
                return fail("02", "商户编号为空!");
            }
            json = json.replaceAll("null", "\"\"");
            Map<String, Object> map = HttpUtil.json2Map(json);
            //放款卡号设置
            if (HttpUtil.isSuccess(map)) {
                //获取贷款品种代码
                String typCde = String.valueOf(appOrder.getTypCde());
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
                Map<String, Object> mapBody = HttpUtil.json2Map(map.get("body").toString());
                if ("9001".equals(dnTyp)) {
                    //账户名称取默认的开户账户名（外转）；如果没有配置外转账号信息，则取商户名称
                    String accName = mapBody.get("acctName") == null ? "" : mapBody.get("acctName").toString();
                    if (StringUtils.isEmpty(accName)) {
                        accName = String.valueOf(mapBody.get("merchChName"));
                    }
                    contractInfo.setApplAcNam(accName); // 放款账号户名
                    contractInfo.setAccBankCde("0000"); // 放款开户银行代码
                    contractInfo.setAccBankName("海尔集团财务有限责任公司"); // 放款开户银行名
                    contractInfo.setApplCardNo((String) mapBody.get("inFinaAcct") == null ?
                            "" :
                            (String) mapBody.get("inFinaAcct")); // 放款卡号
                    contractInfo.setAccAcBchCde("0000"); // 放款开户行支行代码
                } else {
                    contractInfo.setApplAcNam(
                            (String) mapBody.get("acctName") == null ? "" : (String) mapBody.get("acctName")); // 放款账号户名
                    contractInfo.setAccBankCde(
                            (String) mapBody.get("bankCde") == null ? "" : (String) mapBody.get("bankCde")); // 放款开户银行代码
                    contractInfo.setAccBankName((String) mapBody.get("bankName") == null ?
                            "" :
                            (String) mapBody.get("bankName")); // 放款开户银行名
                    contractInfo.setApplCardNo(
                            (String) mapBody.get("acctNo") == null ? "" : (String) mapBody.get("acctNo")); // 放款卡号
                    contractInfo.setAccAcBchCde(
                            (String) mapBody.get("bchCde") == null ? "" : (String) mapBody.get("bchCde")); // 放款开户行支行代码
                }

            }
        } else {// 02：一般消费贷款
            if (StringUtils.isEmpty(appOrder.getApplAcNam())) {
                String url =
                        EurekaServer.CRM + "/app/crm/cust/getDefaultBankCard?custNo=" + appOrder
                                .getCustNo();
                String json = HttpUtil.restGet(url);
                if (StringUtils.isEmpty(json)) {
                    return fail("03", "不存在该客户默认放款银行卡信息!");
                }
                json = json.replaceAll("null", "\"\"");
                Map<String, Object> map = HttpUtil.json2Map(json);
                if (HttpUtil.isSuccess(map)) {
                    Map<String, Object> mapBody = HttpUtil.json2Map(map.get("body").toString());
                    contractInfo.setApplAcNam((String) mapBody.get("custName")); // 放款账号户名
                    contractInfo.setAccBankCde((String) mapBody.get("bankCode")); // 放款开户银行代码
                    contractInfo.setAccBankName((String) mapBody.get("bankName")); // 放款开户银行名
                    contractInfo.setApplCardNo((String) mapBody.get("cardNo")); // 放款卡号
                    contractInfo.setAccAcBchCde((String) mapBody.get("accBchCde")); // 放款开户行支行代码
                }
            } else {
                contractInfo.setApplAcNam(appOrder.getApplAcNam()); // 放款账号户名
                contractInfo.setAccBankCde(appOrder.getAccBankCde()); // 放款开户银行代码
                contractInfo.setAccBankName(appOrder.getAccBankName()); // 放款开户银行名
                contractInfo.setApplCardNo(appOrder.getApplCardNo()); // 放款卡号
                contractInfo.setAccAcBchCde(appOrder.getAccAcBchCde()); // 放款开户行支行代码
            }
        }*/
        contractInfo.setApplAcNam(appOrder.getApplAcNam()); // 放款账号户名
        contractInfo.setAccBankCde(appOrder.getAccBankCde()); // 放款开户银行代码
        if(StringUtils.isEmpty(appOrder.getAccBankName())){//放款开户银行名
            if(appOrder.getAccAcBchName() != null){
                contractInfo.setAccBankName(appOrder.getAccAcBchName());
            }else{
                contractInfo.setAccBankName("");
            }
        }else{
            contractInfo.setAccBankName(appOrder.getAccBankName()); // 放款开户银行名
        }
        contractInfo.setApplCardNo(appOrder.getApplCardNo()); // 放款卡号
        contractInfo.setAccAcBchCde(appOrder.getAccAcBchCde()); // 放款开户行支行代码
        // 通过还款方式查询合同中所对应的选择条数
        List<AppContract> contractList = appContractRepository.findByContType((String) appOrder.getTypLevelTwo());
        List<ContractAssInfo> contractAssInfoList = new ArrayList<>();
        if (contractList.size() > 1) {
            // 利率、手续费率、管理费率
            //			if (null != appOrder.getPayMtd() && !"".equals(appOrder.getPayMtd())) {
            contractAssInfoList = contractAssInfoRepository
                    .findByPayMtdAndTypLevelTwoAndTypCde((String) appOrder.getPayMtd(),
                            (String) appOrder.getTypLevelTwo(), (String) appOrder.getTypCde());
            //			}
        } else if (contractList.size() == 1) {
            //			if (null != appOrder.getPayMtd() && !"".equals(appOrder.getPayMtd())) {
            contractAssInfoList = contractAssInfoRepository
                    .findByPayMtdAndTypLevelTwo((String) appOrder.getPayMtd(), (String) appOrder.getTypLevelTwo());
            //			}
        }
        if (null != contractAssInfoList) {
            if (contractAssInfoList.size() == 1) {
                ContractAssInfo contractAssInfo = contractAssInfoList.get(0);
                contractInfo.setApplyTnrTyp(
                        contractAssInfo.getContractMtdType() == null ? "" : contractAssInfo.getContractMtdType());
                contractInfo.setMtdCde(
                        contractAssInfo.getContractMtdType() == null ? "" : contractAssInfo.getContractMtdType());
                contractInfo.setRate(contractAssInfo.getApplyRate() == null ? "" : contractAssInfo.getApplyRate());
                contractInfo.setApplyManagerRate(
                        contractAssInfo.getApplyManagerRate() == null ? "" : contractAssInfo.getApplyManagerRate());
                contractInfo.setFeeRate(contractAssInfo.getFeeRate() == null ? "" : contractAssInfo.getFeeRate());
            } else if (contractAssInfoList.size() > 1) {
                List<ContractAssInfo> AssInfoList = new ArrayList<>();
                if (contractList.size() == 1) {
                    AssInfoList = contractAssInfoRepository
                            .findByPayMtdAndTypLevelTwoAndApplyTnr((String) appOrder.getPayMtd(),
                                    (String) appOrder.getTypLevelTwo(), (String) appOrder.getApplyTnr());
                } else if (contractList.size() > 1) {
                    AssInfoList = contractAssInfoRepository
                            .findByPayMtdAndTypLevelTwoAndTypCdeAndApplyTnr((String) appOrder.getPayMtd(),
                                    (String) appOrder.getTypLevelTwo(), (String) appOrder.getTypCde(),
                                    (String) appOrder.getApplyTnr());
                }
                if (null != AssInfoList) {
                    if (AssInfoList.size() == 1) {
                        ContractAssInfo contractAssInfo = AssInfoList.get(0);
                        contractInfo.setApplyTnrTyp(contractAssInfo.getContractMtdType() == null ?
                                "" :
                                contractAssInfo.getContractMtdType());
                        contractInfo.setMtdCde(contractAssInfo.getContractMtdType() == null ?
                                "" :
                                contractAssInfo.getContractMtdType());
                        contractInfo
                                .setRate(contractAssInfo.getApplyRate() == null ? "" : contractAssInfo.getApplyRate());
                        contractInfo.setApplyManagerRate(contractAssInfo.getApplyManagerRate() == null ?
                                "" :
                                contractAssInfo.getApplyManagerRate());
                        contractInfo
                                .setFeeRate(contractAssInfo.getFeeRate() == null ? "" : contractAssInfo.getFeeRate());
                    } else if (AssInfoList.size() > 1) {
                        // 查询是否有共同还款人
                        Map<String, Object> countPersonMap = commonRepaymentPersonService.countCommonRepaymentPerson(applseq);
                        if (!HttpUtil.isSuccess(countPersonMap)) {
                            return countPersonMap;
                        }
                        int count = (int) ((Map<String, Object>) countPersonMap.get("body")).get("count");
                        String hasComRepay;
                        if (count == 0) {// 没有共同还款人
                            hasComRepay = "0";
                        } else {// 有共同还款人
                            hasComRepay = "1";
                        }
                        ContractAssInfo contractAssInfo = null;
                        if (contractList.size() == 1) {
                            contractAssInfo = contractAssInfoRepository
                                    .findByHasComRepayOne((String) appOrder.getPayMtd(),
                                            (String) appOrder.getTypLevelTwo(), (String) appOrder.getApplyTnr(),
                                            hasComRepay);
                        } else if (contractList.size() > 1) {
                            contractAssInfo = contractAssInfoRepository
                                    .findByHasComRepayMore((String) appOrder.getPayMtd(),
                                            (String) appOrder.getTypLevelTwo(), (String) appOrder.getTypCde(),
                                            (String) appOrder.getApplyTnr(), hasComRepay);
                        }
                        if (null != contractAssInfo) {
                            contractInfo.setApplyTnrTyp(contractAssInfo.getContractMtdType() == null ?
                                    "" :
                                    contractAssInfo.getContractMtdType());
                            contractInfo.setMtdCde(contractAssInfo.getContractMtdType() == null ?
                                    "" :
                                    contractAssInfo.getContractMtdType());
                            contractInfo.setRate(
                                    contractAssInfo.getApplyRate() == null ? "" : contractAssInfo.getApplyRate());
                            contractInfo.setApplyManagerRate(contractAssInfo.getApplyManagerRate() == null ?
                                    "" :
                                    contractAssInfo.getApplyManagerRate());
                            contractInfo.setFeeRate(
                                    contractAssInfo.getFeeRate() == null ? "" : contractAssInfo.getFeeRate());
                        }
                    }
                }
            }
        }

        // 获取剩余可用额度(目前只有 个人消费借款合同-自主支付版本-支用时使用.docx 合同用)
        String certType = "20"; // 身份证：20
        String idNo = (String) appOrder.getIdNo();// 证件号码
        //String EdCheckUrl = CommonProperties.get("address.gateUrl") + "/app/appserver/getEdCheck?idTyp=" + certType + "&idNo=" + idNo;
        //Map<String, Object> EdCheckMap = HttpUtil.restGetMap(EdCheckUrl);
        Map<String, Object> EdCheckMap = cmisApplService.getEdCheck(certType, idNo, super.getToken());
        logger.info(EdCheckMap);
        ResultHead head = (ResultHead) EdCheckMap.get("head");
        String flag = head.getRetFlag();
        String msg = head.getRetMsg();
        if (Objects.equals(flag, "00000")) {
            //if (HttpUtil.isSuccess(EdCheckMap)) {
            Map<String, Object> edCheckBody = (Map<String, Object>) EdCheckMap.get("body");
            String edAmt = String.valueOf(edCheckBody.get("crdNorAvailAmt")) == null ?
                    "" :
                    FileSignUtil.amtConvert(String.valueOf(edCheckBody.get("crdNorAvailAmt")));// 自主支付可用额度金额
            String applAmt = contractInfo.getApplyAmtSmall();
            Double amt = new BigDecimal(edAmt).subtract(new BigDecimal(applAmt)).doubleValue();
            contractInfo
                    .setCrdNorAvailAmt(String.valueOf(amt) == null ? "" : FileSignUtil.amtConvert(String.valueOf(amt)));
            if (!StringUtils.isEmpty(String.valueOf(amt))) {
                contractInfo.setBigCrdNorAvailAmt(
                        MoneyTool.change(Double.parseDouble(FileSignUtil.amtConvert(String.valueOf(amt)))));
            }
        }

        // 还款银行卡信息
        if (StringUtils.isEmpty(appOrder.getRepayApplCardNo())) {
            // 还款银行卡信息(查询CRM默认还款银行卡信息)
            String url = EurekaServer.CRM + "/app/crm/cust/getDefaultBankCard?custNo=" + appOrder
                    .getCustNo();
            String json = HttpUtil.restGet(url);
            if (StringUtils.isEmpty(json)) {
                return fail("04", "不存在该客户默认还款银行卡信息！");
            }
            json = json.replaceAll("null", "\"\"");
            Map<String, Object> map = HttpUtil.json2Map(json);
            if (HttpUtil.isSuccess(map)) {
                Map<String, Object> mapBody = HttpUtil.json2Map(map.get("body").toString());
                contractInfo.setRepayApplAcNam(
                        (String) mapBody.get("custName") == null ? "" : (String) mapBody.get("custName")); // 还款账号户名
                contractInfo.setRepayAccBankName(
                        (String) mapBody.get("bankName") == null ? "" : (String) mapBody.get("bankName")); // 还款开户银行名
                contractInfo.setRepayApplCardNo(
                        (String) mapBody.get("cardNo") == null ? "" : (String) mapBody.get("cardNo")); // 还款卡号
            }
        } else {
            contractInfo.setRepayApplAcNam(appOrder.getRepayApplAcNam()); // 还款账号户名
            contractInfo.setRepayAccBankName(appOrder.getRepayAccBankName()); // 还款开户银行名
            contractInfo.setRepayApplCardNo(appOrder.getRepayApplCardNo()); // 还款卡号
        }

        contractInfo.setAccName(appOrder.getCustName()); // 甲方签名
        contractInfo.setLenderName(""); // 乙方签名
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        contractInfo.setDate(format.format(new Date())); // 日期
        Calendar rightNow = Calendar.getInstance();
        contractInfo.setYear(String.valueOf(rightNow.get(Calendar.YEAR)));
        contractInfo.setMonth(String.valueOf(rightNow.get(Calendar.MONTH) + 1));
        contractInfo.setDay(String.valueOf(rightNow.get(Calendar.DAY_OF_MONTH)));

        //获取每期手续费费率
        String tnrOpt = appOrder.getApplyTnr() == null ? "" : appOrder.getApplyTnr().toString(); //还款期数
        String mtdTyp = appOrder.getPayMtd() == null ? "" : appOrder.getPayMtd().toString(); //还款方式类型
        String typCde = appOrder.getTypCde() == null ? "" : appOrder.getTypCde().toString(); //贷款品种
        String typLevelTwo = appOrder.getTypLevelTwo() == null ? "" : appOrder.getTypLevelTwo().toString(); //贷款品种

        List<ContractAssInfo> resultList = contractAssInfoRepository
                .findByPayMtdAndTypLevelTwoAndTypCdeAndApplyTnr(mtdTyp, typLevelTwo, typCde,
                        tnrOpt);
        logger.debug("查询每期手续费费率：tnrOpt=" + tnrOpt + ",mtdTyp=" + mtdTyp + ",typCde=" + typCde + ",typLevelTwo="
                + typLevelTwo);
        logger.debug("查询结果：" + resultList);
        if (resultList != null && resultList.size() == 1) {
            ContractAssInfo contractAssInfo = resultList.get(0);
            contractInfo.setApplyManagerRate(contractAssInfo.getApplyManagerRate());
        }

/*
        String url = super.getGateUrl() + "/app/cmisproxy/api/pLoanTyp/" + typCde + "/feeMsg?tnrOpt=" + tnrOpt + "&mtdTyp=" + mtdTyp + "&feeTnrTyp=03";
        String json = HttpUtil.restGet(url, super.getToken());
        logger.info("通过接口请求的合同费率的url" + url);
        List<Map<String, Object>> resultlist = HttpUtil.json2List(json);
        logger.info("通过接口请求的合同费率为：" + resultlist);
        if (resultlist.size() > 0) {
            Map<String, Object> resultMap = resultlist.get(0);
            String s = resultMap.get("feePct2") == null ? "" : resultMap.get("feePct2").toString();
            String feerate = "";
            if (!StringUtils.isEmpty(s)) {
                BigDecimal a = new BigDecimal(0.1);
                BigDecimal b = new BigDecimal(s);
                BigDecimal d = new BigDecimal(100);
                BigDecimal c = a.subtract(b);
                BigDecimal bd = c.multiply(d);
                DecimalFormat df = new DecimalFormat("0.00");
                feerate = df.format(bd);
            }
            contractInfo.setApplyManagerRate(feerate);
        }*/

        return success(contractInfo);
    }


    /**
     * 返回合同所需要的数据，合同预览页面使用（js）
     *
     * @param applseq 流水号
     * @return
     */
    @RequestMapping(value = "/app/appserver/getContractInfoFromCmis", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> getContractInfoFromCmis(@RequestParam(value = "applseq") String applseq) {
        // 查询合同所需要的数据:
        AppOrder appOrder = appOrderService.getAppOrderFromCmis(applseq, "1");
        //        if (null == appOrderList || appOrderList.size() == 0) {
        //            return fail("01", "没有该订单!");
        //        }
        if (appOrder == null) {
            logger.debug("未从cmis查得该订单");
            return fail("01", "没有该订单！");
        }
        //        AppOrder appOrder = appOrderList.get(0);
        ContractInfo contractInfo = new ContractInfo();
        contractInfo.setContractNo(""); // 合同编号：暂时没有数据
        contractInfo.setCustName(appOrder.getCustName()); // 借款人(甲方)姓名
        contractInfo.setIdentifyNo(appOrder.getIdNo()); // 身份证号码
        contractInfo.setCustMobile(appOrder.getIndivMobile()); // 联系电话
        contractInfo.setPurpose(appOrder.getPurpose()); // 贷款用途
        contractInfo.setCooprName(appOrder.getCooprName()); // 门店名称

        //获取门店所在市cityName   (crm:/app/crm/cust/getStore   门店代码:cooprCde)
        if (!StringUtils.isEmpty(appOrder.getCooprCde())) {
            String cooprInfoUrl = EurekaServer.CRM + "/app/crm/cust/getStore?storeNo=" + appOrder
                    .getCooprCde();
            String cooprInfoJson = HttpUtil.restGet(cooprInfoUrl);
            logger.debug("获取门店信息cooprInfoJson=" + cooprInfoJson);
            if (!StringUtils.isEmpty(cooprInfoJson)) {
                cooprInfoJson = cooprInfoJson.replaceAll("null", "\"\"");
                Map<String, Object> cooprInfoMap = HttpUtil.json2Map(cooprInfoJson);
                if (HttpUtil.isSuccess(cooprInfoMap)) {
                    Map<String, Object> mapBody = HttpUtil.json2Map(cooprInfoMap.get("body").toString());
                    contractInfo.setCooprCityName(
                            (String) mapBody.get("cityName") == null ? "" : (String) mapBody.get("cityName"));
                }
            }
        }

        contractInfo.setCustAddress(appOrder.getLiveInfo()); // 居住地址
        contractInfo.setEmail(appOrder.getEmail()); // 邮箱
        contractInfo.setLiveZip(""); // 邮编

        // 合同展示借款金额：
        String applyAmt = FileSignUtil.amtConvert((String) appOrder.getApplyAmt());// 借款金额
        String apprvAmt = FileSignUtil.amtConvert((String) appOrder.getApprvAmt());// 审批金额
        if (StringUtils.isEmpty(apprvAmt) || "null".equals(apprvAmt)) {
            contractInfo.setApplyAmtSmall(applyAmt);
            if (null != applyAmt && !"".equals(applyAmt)) {
                contractInfo.setApplyAmtBig(MoneyTool.change(Double.parseDouble(applyAmt)));
            } else {
                contractInfo.setApplyAmtBig("");
            }
        } else {
            contractInfo.setApplyAmtSmall(apprvAmt);
            contractInfo.setApplyAmtBig(MoneyTool.change(Double.parseDouble(apprvAmt)));
        }

        contractInfo.setApplyTnr(appOrder.getApplyTnr()); // 借款期限
        contractInfo.setFstPaySmall(FileSignUtil.amtConvert(appOrder.getFstPay())); // 首付金额小写
        if (StringUtils.isEmpty(appOrder.getFstPay())) { // 首付金额大写
            contractInfo.setFstPayBig("");
        } else {
            contractInfo
                    .setFstPayBig(MoneyTool.change(Double.parseDouble(FileSignUtil.amtConvert(appOrder.getFstPay()))));
        }
        logger.info("获取放款账号信息");
        // 贷款类型为耐用消费品贷款
        if ("01".equals(appOrder.getTypGrp())) {
            String url = EurekaServer.CRM + "/pub/crm/cust/getMerchInfoByMerchNo" + "?merchNo="
                    + appOrder.getMerchNo();
            String json = HttpUtil.restGet(url);
            if (StringUtils.isEmpty(json)) {
                return fail("02", "商户编号为空!");
            }
            json = json.replaceAll("null", "\"\"");
            Map<String, Object> map = HttpUtil.json2Map(json);
            //放款卡号设置
            if (HttpUtil.isSuccess(map)) {
                //获取贷款品种代码
                String typCde = String.valueOf(appOrder.getTypCde());
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
                Map<String, Object> mapBody = HttpUtil.json2Map(map.get("body").toString());
                if ("9001".equals(dnTyp)) {
                    //账户名称取默认的开户账户名（外转）；如果没有配置外转账号信息，则取商户名称
                    String accName = mapBody.get("acctName") == null ? "" : mapBody.get("acctName").toString();
                    if (StringUtils.isEmpty(accName)) {
                        accName = String.valueOf(mapBody.get("merchChName"));
                    }
                    contractInfo.setApplAcNam(accName); // 放款账号户名
                    contractInfo.setAccBankCde("0000"); // 放款开户银行代码
                    contractInfo.setAccBankName("海尔集团财务有限责任公司"); // 放款开户银行名
                    contractInfo.setApplCardNo((String) mapBody.get("inFinaAcct") == null ?
                            "" :
                            (String) mapBody.get("inFinaAcct")); // 放款卡号
                    contractInfo.setAccAcBchCde("0000"); // 放款开户行支行代码
                } else {
                    contractInfo.setApplAcNam(
                            (String) mapBody.get("acctName") == null ? "" : (String) mapBody.get("acctName")); // 放款账号户名
                    contractInfo.setAccBankCde(
                            (String) mapBody.get("bankCde") == null ? "" : (String) mapBody.get("bankCde")); // 放款开户银行代码
                    contractInfo.setAccBankName((String) mapBody.get("bankName") == null ?
                            "" :
                            (String) mapBody.get("bankName")); // 放款开户银行名
                    contractInfo.setApplCardNo(
                            (String) mapBody.get("acctNo") == null ? "" : (String) mapBody.get("acctNo")); // 放款卡号
                    contractInfo.setAccAcBchCde(
                            (String) mapBody.get("bchCde") == null ? "" : (String) mapBody.get("bchCde")); // 放款开户行支行代码
                }

            }
        } else {// 02：一般消费贷款
            if (StringUtils.isEmpty(appOrder.getApplAcNam())) {
                String url =
                        EurekaServer.CRM + "/app/crm/cust/getDefaultBankCard?custNo=" + appOrder
                                .getCustNo();
                String json = HttpUtil.restGet(url);
                if (StringUtils.isEmpty(json)) {
                    return fail("03", "不存在该客户默认放款银行卡信息!");
                }
                json = json.replaceAll("null", "\"\"");
                Map<String, Object> map = HttpUtil.json2Map(json);
                if (HttpUtil.isSuccess(map)) {
                    Map<String, Object> mapBody = HttpUtil.json2Map(map.get("body").toString());
                    contractInfo.setApplAcNam((String) mapBody.get("custName")); // 放款账号户名
                    contractInfo.setAccBankCde((String) mapBody.get("bankCode")); // 放款开户银行代码
                    contractInfo.setAccBankName((String) mapBody.get("bankName")); // 放款开户银行名
                    contractInfo.setApplCardNo((String) mapBody.get("cardNo")); // 放款卡号
                    contractInfo.setAccAcBchCde((String) mapBody.get("accBchCde")); // 放款开户行支行代码
                }
            } else {
                contractInfo.setApplAcNam(appOrder.getApplAcNam()); // 放款账号户名
                contractInfo.setAccBankCde(appOrder.getAccBankCde()); // 放款开户银行代码
                contractInfo.setAccBankName(appOrder.getAccBankName()); // 放款开户银行名
                contractInfo.setApplCardNo(appOrder.getApplCardNo()); // 放款卡号
                contractInfo.setAccAcBchCde(appOrder.getAccAcBchCde()); // 放款开户行支行代码
            }
        }

        // 通过还款方式查询合同中所对应的选择条数
        List<AppContract> contractList = appContractRepository.findByContType((String) appOrder.getTypLevelTwo());
        List<ContractAssInfo> contractAssInfoList = new ArrayList<>();
        if (contractList.size() > 1) {
            // 利率、手续费率、管理费率
            //			if (null != appOrder.getPayMtd() && !"".equals(appOrder.getPayMtd())) {
            contractAssInfoList = contractAssInfoRepository
                    .findByPayMtdAndTypLevelTwoAndTypCde((String) appOrder.getPayMtd(),
                            (String) appOrder.getTypLevelTwo(), (String) appOrder.getTypCde());
            //			}
        } else if (contractList.size() == 1) {
            //			if (null != appOrder.getPayMtd() && !"".equals(appOrder.getPayMtd())) {
            contractAssInfoList = contractAssInfoRepository
                    .findByPayMtdAndTypLevelTwo((String) appOrder.getPayMtd(), (String) appOrder.getTypLevelTwo());
            //			}
        }
        if (null != contractAssInfoList) {
            if (contractAssInfoList.size() == 1) {
                ContractAssInfo contractAssInfo = contractAssInfoList.get(0);
                contractInfo.setApplyTnrTyp(
                        contractAssInfo.getContractMtdType() == null ? "" : contractAssInfo.getContractMtdType());
                contractInfo.setMtdCde(
                        contractAssInfo.getContractMtdType() == null ? "" : contractAssInfo.getContractMtdType());
                contractInfo.setRate(contractAssInfo.getApplyRate() == null ? "" : contractAssInfo.getApplyRate());
                contractInfo.setApplyManagerRate(
                        contractAssInfo.getApplyManagerRate() == null ? "" : contractAssInfo.getApplyManagerRate());
                contractInfo.setFeeRate(contractAssInfo.getFeeRate() == null ? "" : contractAssInfo.getFeeRate());
            } else if (contractAssInfoList.size() > 1) {
                List<ContractAssInfo> AssInfoList = new ArrayList<>();
                if (contractList.size() == 1) {
                    AssInfoList = contractAssInfoRepository
                            .findByPayMtdAndTypLevelTwoAndApplyTnr((String) appOrder.getPayMtd(),
                                    (String) appOrder.getTypLevelTwo(), (String) appOrder.getApplyTnr());
                } else if (contractList.size() > 1) {
                    AssInfoList = contractAssInfoRepository
                            .findByPayMtdAndTypLevelTwoAndTypCdeAndApplyTnr((String) appOrder.getPayMtd(),
                                    (String) appOrder.getTypLevelTwo(), (String) appOrder.getTypCde(),
                                    (String) appOrder.getApplyTnr());
                }
                if (null != AssInfoList) {
                    if (AssInfoList.size() == 1) {
                        ContractAssInfo contractAssInfo = AssInfoList.get(0);
                        contractInfo.setApplyTnrTyp(contractAssInfo.getContractMtdType() == null ?
                                "" :
                                contractAssInfo.getContractMtdType());
                        contractInfo.setMtdCde(contractAssInfo.getContractMtdType() == null ?
                                "" :
                                contractAssInfo.getContractMtdType());
                        contractInfo
                                .setRate(contractAssInfo.getApplyRate() == null ? "" : contractAssInfo.getApplyRate());
                        contractInfo.setApplyManagerRate(contractAssInfo.getApplyManagerRate() == null ?
                                "" :
                                contractAssInfo.getApplyManagerRate());
                        contractInfo
                                .setFeeRate(contractAssInfo.getFeeRate() == null ? "" : contractAssInfo.getFeeRate());
                    } else if (AssInfoList.size() > 1) {
                        // 查询是否有共同还款人
                        AppOrdernoTypgrpRelation relation = appOrdernoTypgrpRelationRepository.findOne((String) appOrder.getOrderNo());
                        if (relation == null) {
                            return fail("37", "该订单信息不存在");
                        }
                        Map<String, Object> countPerson = commonRepaymentPersonService.countCommonRepaymentPerson(relation.getApplSeq());
                        if (!HttpUtil.isSuccess(countPerson)) {
                            return countPerson;
                        }
                        int count = (int) ((Map<String, Object>) countPerson.get("body")).get("count");
                        String hasComRepay;
                        if (count == 0) {// 没有共同还款人
                            hasComRepay = "0";
                        } else {// 有共同还款人
                            hasComRepay = "1";
                        }
                        ContractAssInfo contractAssInfo = null;
                        if (contractList.size() == 1) {
                            contractAssInfo = contractAssInfoRepository
                                    .findByHasComRepayOne((String) appOrder.getPayMtd(),
                                            (String) appOrder.getTypLevelTwo(), (String) appOrder.getApplyTnr(),
                                            hasComRepay);
                        } else if (contractList.size() > 1) {
                            contractAssInfo = contractAssInfoRepository
                                    .findByHasComRepayMore((String) appOrder.getPayMtd(),
                                            (String) appOrder.getTypLevelTwo(), (String) appOrder.getTypCde(),
                                            (String) appOrder.getApplyTnr(), hasComRepay);
                        }
                        if (null != contractAssInfo) {
                            contractInfo.setApplyTnrTyp(contractAssInfo.getContractMtdType() == null ?
                                    "" :
                                    contractAssInfo.getContractMtdType());
                            contractInfo.setMtdCde(contractAssInfo.getContractMtdType() == null ?
                                    "" :
                                    contractAssInfo.getContractMtdType());
                            contractInfo.setRate(
                                    contractAssInfo.getApplyRate() == null ? "" : contractAssInfo.getApplyRate());
                            contractInfo.setApplyManagerRate(contractAssInfo.getApplyManagerRate() == null ?
                                    "" :
                                    contractAssInfo.getApplyManagerRate());
                            contractInfo.setFeeRate(
                                    contractAssInfo.getFeeRate() == null ? "" : contractAssInfo.getFeeRate());
                        }
                    }
                }
            }
        }

        // 获取剩余可用额度(目前只有 个人消费借款合同-自主支付版本-支用时使用.docx 合同用)
        String certType = "20"; // 身份证：20
        String idNo = (String) appOrder.getIdNo();// 证件号码
        //String EdCheckUrl = CommonProperties.get("address.gateUrl") + "/app/appserver/getEdCheck?idTyp=" + certType + "&idNo=" + idNo;
        //Map<String, Object> EdCheckMap = HttpUtil.restGetMap(EdCheckUrl);
        Map<String, Object> EdCheckMap = cmisApplService.getEdCheck(certType, idNo, super.getToken());
        logger.info(EdCheckMap);
        ResultHead head = (ResultHead) EdCheckMap.get("head");
        String flag = head.getRetFlag();
        String msg = head.getRetMsg();
        if (Objects.equals(flag, "00000")) {
            //if (HttpUtil.isSuccess(EdCheckMap)) {
            Map<String, Object> edCheckBody = (Map<String, Object>) EdCheckMap.get("body");
            String edAmt = String.valueOf(edCheckBody.get("crdNorAvailAmt")) == null ?
                    "" :
                    FileSignUtil.amtConvert(String.valueOf(edCheckBody.get("crdNorAvailAmt")));// 自主支付可用额度金额
            String applAmt = contractInfo.getApplyAmtSmall();
            Double amt = new BigDecimal(edAmt).subtract(new BigDecimal(applAmt)).doubleValue();
            contractInfo
                    .setCrdNorAvailAmt(String.valueOf(amt) == null ? "" : FileSignUtil.amtConvert(String.valueOf(amt)));
            if (!StringUtils.isEmpty(String.valueOf(amt))) {
                contractInfo.setBigCrdNorAvailAmt(
                        MoneyTool.change(Double.parseDouble(FileSignUtil.amtConvert(String.valueOf(amt)))));
            }
        }

        // 还款银行卡信息
        if (StringUtils.isEmpty(appOrder.getRepayApplCardNo())) {
            // 还款银行卡信息(查询CRM默认还款银行卡信息)
            String url = EurekaServer.CRM + "/app/crm/cust/getDefaultBankCard?custNo=" + appOrder
                    .getCustNo();
            String json = HttpUtil.restGet(url);
            if (StringUtils.isEmpty(json)) {
                return fail("04", "不存在该客户默认还款银行卡信息！");
            }
            json = json.replaceAll("null", "\"\"");
            Map<String, Object> map = HttpUtil.json2Map(json);
            if (HttpUtil.isSuccess(map)) {
                Map<String, Object> mapBody = HttpUtil.json2Map(map.get("body").toString());
                contractInfo.setRepayApplAcNam(
                        (String) mapBody.get("custName") == null ? "" : (String) mapBody.get("custName")); // 还款账号户名
                contractInfo.setRepayAccBankName(
                        (String) mapBody.get("bankName") == null ? "" : (String) mapBody.get("bankName")); // 还款开户银行名
                contractInfo.setRepayApplCardNo(
                        (String) mapBody.get("cardNo") == null ? "" : (String) mapBody.get("cardNo")); // 还款卡号
            }
        } else {
            contractInfo.setRepayApplAcNam(appOrder.getRepayApplAcNam()); // 还款账号户名
            contractInfo.setRepayAccBankName(appOrder.getRepayAccBankName()); // 还款开户银行名
            contractInfo.setRepayApplCardNo(appOrder.getRepayApplCardNo()); // 还款卡号
        }

        contractInfo.setAccName(appOrder.getCustName()); // 甲方签名
        contractInfo.setLenderName(""); // 乙方签名
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        contractInfo.setDate(format.format(new Date())); // 日期
        Calendar rightNow = Calendar.getInstance();
        contractInfo.setYear(String.valueOf(rightNow.get(Calendar.YEAR)));
        contractInfo.setMonth(String.valueOf(rightNow.get(Calendar.MONTH) + 1));
        contractInfo.setDay(String.valueOf(rightNow.get(Calendar.DAY_OF_MONTH)));

        //获取每期手续费费率
        String tnrOpt = appOrder.getApplyTnr() == null ? "" : appOrder.getApplyTnr().toString(); //还款期数
        String mtdTyp = appOrder.getPayMtd() == null ? "" : appOrder.getPayMtd().toString(); //还款方式类型
        String typCde = appOrder.getTypCde() == null ? "" : appOrder.getTypCde().toString(); //贷款品种
        String typLevelTwo = appOrder.getTypLevelTwo() == null ? "" : appOrder.getTypLevelTwo().toString(); //贷款品种

        List<ContractAssInfo> resultList = contractAssInfoRepository
                .findByPayMtdAndTypLevelTwoAndTypCdeAndApplyTnr(mtdTyp, typLevelTwo, typCde,
                        tnrOpt);
        logger.debug("查询每期手续费费率：tnrOpt=" + tnrOpt + ",mtdTyp=" + mtdTyp + ",typCde=" + typCde + ",typLevelTwo="
                + typLevelTwo);
        logger.debug("查询结果：" + resultList);
        if (resultList != null && resultList.size() == 1) {
            ContractAssInfo contractAssInfo = resultList.get(0);
            contractInfo.setApplyManagerRate(contractAssInfo.getApplyManagerRate());
        }

/*
        String url = super.getGateUrl() + "/app/cmisproxy/api/pLoanTyp/" + typCde + "/feeMsg?tnrOpt=" + tnrOpt + "&mtdTyp=" + mtdTyp + "&feeTnrTyp=03";
        String json = HttpUtil.restGet(url, super.getToken());
        logger.info("通过接口请求的合同费率的url" + url);
        List<Map<String, Object>> resultlist = HttpUtil.json2List(json);
        logger.info("通过接口请求的合同费率为：" + resultlist);
        if (resultlist.size() > 0) {
            Map<String, Object> resultMap = resultlist.get(0);
            String s = resultMap.get("feePct2") == null ? "" : resultMap.get("feePct2").toString();
            String feerate = "";
            if (!StringUtils.isEmpty(s)) {
                BigDecimal a = new BigDecimal(0.1);
                BigDecimal b = new BigDecimal(s);
                BigDecimal d = new BigDecimal(100);
                BigDecimal c = a.subtract(b);
                BigDecimal bd = c.multiply(d);
                DecimalFormat df = new DecimalFormat("0.00");
                feerate = df.format(bd);
            }
            contractInfo.setApplyManagerRate(feerate);
        }*/

        return success(contractInfo);
    }

    /**
     * 合同签约提交.
     *
     * @param custNo     客户编号
     * @param applSeq    流水号
     * @param verifiCode 短信验证码
     * @return
     */
    @RequestMapping(value = "/app/appserver/subSignContract", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> subSignContract(@RequestParam("custNo") String custNo,
            @RequestParam("applSeq") String applSeq,
            @RequestParam(value = "verifiCode", required = false) String verifiCode,
            @RequestParam("flag") String flag, String expectCredit) { // 1.补签合同 2.申请放款

        if (!"1".equals(flag) && !"2".equals(flag)) {
            return fail("07", "合同签约类型错误");
        }

        if (!StringUtils.isEmpty(super.getChannel()) && super.getChannel().equals("16")) {
            // 如果是星巢贷的订单，应通知信贷更新营销人员信息
            // 判断渠道进件成功
            // todo 营销人员信息上送收单系统.
            AppOrder appOrder = appOrderRepository.findByApplseq(applSeq);
            if (appOrder == null) {
                return fail("55", "订单不存在");
            }
            logger.debug(
                    "星巢贷业务，通知信贷更新营销人员信息：promCde=" + appOrder.getPromCde() + "/promDesc=" + appOrder
                            .getPromDesc()
                            + "/promPhone="
                            + appOrder.getPromPhone());
            Map<String, Object> redStarRiskInfo = cmisApplService.updateRedStarRiskInfo(appOrder);
            logger.debug("星巢贷业务通知信贷更新营销人员信息结果：" + redStarRiskInfo);
        }

        return chService.subSignContract(custNo, applSeq, verifiCode, flag, getToken(), expectCredit);

    }

    /**
     * 商品贷申请放款.
     *
     * @param applSeq   流水号
     * @return  Map
     */
    @RequestMapping(value = "/app/appserver/subSignCont", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> subSignCont(@RequestParam("applSeq") String applSeq, @RequestParam(required = false) String flag) {
        // 提交合同
        HashMap<String, Object> map = new HashMap<>();
        map.put("applSeq", applSeq);
        map.put("flag", StringUtils.isEmpty(flag) ? "2" : flag); // 0：贷款取消 1:申请提交 2：合同提交
        if ("16".equals(super.getChannel())) {
            map.put("sysFlag", super.getChannel());
            map.put("channel", "31");
        }

        if ("11".equals(super.getChannel())) {
            map.put("sysFlag", super.getChannel());
            map.put("channel", StringUtils.isEmpty(super.getChannelNO()) ? "34" : super.getChannelNO());
        }

        // 商品贷向收单系统发起申请放款
        AppOrder appOrder = new AppOrder();
        appOrder.setApplSeq(applSeq);

        Map<String, Object> responseMap = acquirerService.commitAppl(appOrder, flag);
        return responseMap;
    }

    /**
     * 下载合同pdf
     *
     * @param applseq
     * @param response
     * @return
     */
    @RequestMapping(value = "/app/appserver/downContractPdf", method = RequestMethod.GET)
    public String downContractPdfFile(@RequestParam("applseq") String applseq, HttpServletResponse response) {
        String flag = "1";   //1:合同   0:协议
        ContractPdfFile contractPdfFile = contractPdfFileRepository.findByApplseqAndFlag(applseq, flag);
        if (null == contractPdfFile) {
            return "404";
        }
        File file = new File(contractPdfFile.getFileName());
        if (!file.exists()) {
            return "404";
        }
        // 设置响应的数据类型;下载的文件名;打开方式
        response.setContentType("application/file");
        response.setHeader("content-Disposition", "attachment;filename=" + contractPdfFile.getFileDesc());
        try {
            // 从下载文件中获取输入流
            InputStream is = new BufferedInputStream(new FileInputStream(file));
            // 从响应中获取一个输出流
            OutputStream os = new BufferedOutputStream(response.getOutputStream());
            byte[] b = new byte[1024];
            int len;
            while ((len = is.read(b)) != -1)
                os.write(b, 0, len);
            is.close();
            os.flush();
            os.close();
        } catch (IOException e) {
            logger.error(e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return "";
        }
        return "";
    }

    /**
     * 测试签章word模板
     *
     * @return
     */
    @RequestMapping(value = "/app/appserver/testFor", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> testFor(HttpServletRequest request, String id) {
        String clientId = getParam("client_id");
        clientId = "A0000055B0FB82";// 临时写死
        //		FileSignUtil.sendGrantToCrm("C201607170228121071860","6228480128349152974", "D:\\grantTest\\","AppServer新需求v1.1.0.pdf");
        String path = "D:\\grantTest\\AppServer新需求v1.1.0.pdf";
        try {
            File file = new File(path);
            FileInputStream in = new FileInputStream(file);
            MappedByteBuffer byteBuffer = in.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, file.length());
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(byteBuffer);
            BigInteger bi = new BigInteger(1, md5.digest());
            String md5Num = bi.toString(16);
            logger.debug("first:***MD5=" + md5Num);
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.debug("- - - - - - - - - - - - - - - - - - -");
        try {
            FileInputStream fis = new FileInputStream(path);
            String md5 = DigestUtils.md5Hex(IOUtils.toByteArray(fis));
            IOUtils.closeQuietly(fis);
            logger.debug("second:***MD5=" + md5);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return success();
    }

    /**
     * 海尔会员有额度查询
     *
     * @return
     */
    @RequestMapping(value = "/app/appserver/getInfoForVip", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> getInfoForVip(@RequestParam String crtUsr,@RequestParam String idNo) {
        String channelNo =super.getChannelNO();
        Map<String, Object>  map = chService.getInfoForVip(crtUsr,idNo,channelNo);
        logger.info("返回结果："+map);
        return success(map);
    }
}
