package com.haiercash.appserver.service;

import com.haiercash.common.apporder.utils.AcqTradeCode;
import com.haiercash.common.config.EurekaServer;
import com.haiercash.common.data.AppOrdernoTypgrpRelationRepository;
import com.haiercash.common.service.BaseService;
import com.haiercash.commons.cmis.CmisTradeCode;
import com.haiercash.commons.cmis.CmisUtil;
import com.haiercash.commons.util.AcqUtil;
import com.haiercash.commons.util.HttpUtil;
import com.haiercash.commons.util.RestUtil;
import com.haiercash.commons.util.ResultHead;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by yinjun on 2016/8/16.
 */
@Service
public class DhkService extends BaseService {
    private Log logger = LogFactory.getLog(this.getClass());

    public static String MODULE_NO = "59";

    public DhkService() {
        super(MODULE_NO);
    }

    @Autowired
    private CmisApplService cmisApplService;
    @Autowired
    private AppOrderService appOrderService;
    @Autowired
    private CrmService crmService;
    @Autowired
    private AppOrdernoTypgrpRelationRepository appOrdernoTypgrpRelationRepository;

    public Map<String, Object> queryAppLoanAndGoods(String applSeq) {
//        Map<String, Object> returnMap = new HashMap<>();
        //1.从收单查询贷款详情
        Map<String, Object> acqRetMap = selectApplInfoApp(applSeq, super.getChannelNo());
        logger.info("acqRetMap==" + acqRetMap);
        if (!isSuccess(acqRetMap)) {
            return fail("08", "查询失败");
        } else {
//            returnMap = HttpUtil.json2Map(json);
            // {superCoopr=zgdxyzf01, applSeq=957149, apprvAmt=638.0, loanNo=,
            // channelNo=05, idTyp=20, goods=[{goodsPrice=58,
            // goodsCode=2309070.0, goodsName=套餐勿删, goodsNum=11}],
            // applAcBankDesc=, applAcBchDesc=中国建设银行北京展览路支行,
            // idNo=372926199009295116, applCde=902016061800000957149,
            // indivMobile=18620391028, mailAddr=青岛, typGrp=01, mtdDesc=零利率还款,
            // mtdCde=LT001, payMtdDesc=每6期付费，每6期还本, custId=818738,
            // applAcBch=105100003040, applyTnrTyp=24, applyDt=2016-06-18,
            // custName=贺庆信, contNo=, loanTyp=1598a, applyAmt=638.0,
            // cooprCde=lydxfgs, feeAmt=0.0, applAcBank=105,

            // loanTypName=24期-翼支付C方案-电信手机（外转）, mthAmt=26.58, appOutAdvice=其他,
            // payMtd=10, fstPay=0.0, proPurAmt=638.0, mailOpt=A,
            // psNormIntAmt=0.0, repayApplCardNo=11001016700053002086,
            // applyTnr=24}
            Map<String, Object> acqRetBodyMap = (Map<String, Object>) acqRetMap.get("body");
            if (acqRetBodyMap == null || acqRetBodyMap.isEmpty()) {
                return fail("09", "查询不到贷款信息");
            }
            //设置贷款信息
            Map<String, Object> returnMap = setMapValue(applSeq, acqRetBodyMap);
            //去订单系统查询订单类型
            try {
                setOrderInf(applSeq, returnMap);
            } catch (Exception e) {
                e.printStackTrace();
                return fail(RestUtil.ERROR_INTERNAL_CODE, RestUtil.ERROR_INTERNAL_MSG);
            }
            //查询变更后的还款卡
            String hkcard = EurekaServer.CMISPROXY + "/api/appl/getChangeCardNo?applSeq=" + applSeq;
            String hkcardjson = HttpUtil.restGet(hkcard, super.getToken());
            if (StringUtils.isEmpty(hkcardjson)) {
                return fail("08", "还款卡查询失败");
            } else {
                Map<String, Object> cardMap = HttpUtil.json2Map(hkcardjson);
                if (cardMap.containsKey("repayApplAcNo")) {
                    String hkcardNo = String.valueOf(cardMap.get("repayApplAcNo"));
                    returnMap.put("repayApplCardNo", hkcardNo);
                }
            }
            String mailOpt = String.valueOf(returnMap.get("mailOpt"));
            if (null != mailOpt && mailOpt.toLowerCase().equals("null")) {
                returnMap.put("mailOpt", "");
            }
            // 总利息金额 psNormIntAmt
            // 费用总额 feeAmt
            double psNormIntAmt = StringUtils.isEmpty(returnMap.get("psNormIntAmt")) ? 0 : Double.parseDouble(String.valueOf(returnMap.get("psNormIntAmt")));
            double feeAmt = StringUtils.isEmpty(returnMap.get("feeAmt")) ? 0 : Double.parseDouble(String.valueOf(returnMap.get("feeAmt")));
            String cardNo = (String) returnMap.get("repayApplCardNo");
            if (StringUtils.isEmpty(cardNo)) {
                returnMap.put("repayAccBankName", "");
            } else {
                String queryBankNameUrl = EurekaServer.CRM + "/app/crm/cust/getCustBankCardByCardNo?cardNo=" + cardNo;
                logger.info("查询银行卡名称url：" + queryBankNameUrl);
                String queryBankNameJson = HttpUtil.restGet(queryBankNameUrl);
                logger.debug("查询银行卡名称返回:" + queryBankNameJson);
                if (StringUtils.isEmpty(queryBankNameJson)) {
                    logger.error("查询银行卡名称失败---》CRM 1.27,还款卡相关信息返回空");
                    //  return fail("50", RestUtil.ERROR_INTERNAL_MSG);
                    // 此处还款卡银行名称以信贷的为准
//                    returnMap.put("repayAccBankName", "");
//                    returnMap.put("repayAccBankCde","");
//                    returnMap.put("repayAccBchCde","");
//                    returnMap.put("repayAccBchName","");
//                    returnMap.put("repayAcProvince", "");
//                    returnMap.put("repayAcCity","");
                } else {
                    Map<String, Object> queryBankNameMap = HttpUtil.json2Map(queryBankNameJson);
                    Map<String, Object> queryBankNameHeadMap = HttpUtil.json2Map(queryBankNameMap.get("head").toString());
                    String bankRetFlag = String.valueOf(queryBankNameHeadMap.get("retFlag"));
                    String bankMsg = String.valueOf(queryBankNameHeadMap.get("retMsg"));
                    if (!Objects.equals("00000", bankRetFlag)) {
                        //  return fail("80",bankMsg);
                        logger.info("银行卡信息查询失败了，相关字段返回空");
                        // 此处还款卡银行名称以信贷的为准
//                        returnMap.put("repayAccBankName","");
//                        returnMap.put("repayAccBankCde","");
//                        returnMap.put("repayAccBchCde","");
//                        returnMap.put("repayAccBchName","");
//                        returnMap.put("repayAcProvince", "");
//                        returnMap.put("repayAcCity","");
                    } else {
                        Map<String, Object> queryBankNameBodyMap = HttpUtil.json2Map(queryBankNameMap.get("body").toString());
                        returnMap.put("repayAccBankName", queryBankNameBodyMap.get("bankName") == null ? "" : queryBankNameBodyMap.get("bankName"));
                        returnMap.put("repayAccBankCde", queryBankNameBodyMap.get("bankCode") == null ? "" : queryBankNameBodyMap.get("bankCode"));
                       //20170615更新，从收单获取
                        // returnMap.put("repayAccBchCde", queryBankNameBodyMap.get("accBchCde") == null ? "" : queryBankNameBodyMap.get("accBchCde"));
                        //20170615更新，从收单获取
                        //returnMap.put("repayAccBchName", queryBankNameBodyMap.get("accBchName") == null ? "" : queryBankNameBodyMap.get("accBchName"));
                        // returnMap.put("repayApplCardNo",  queryBankNameBodyMap.get("bankName") == null ? "" : appOrder.getRepayApplCardNo());
                        returnMap.put("repayAcProvince", queryBankNameBodyMap.get("acctProvince") == null ? "" : queryBankNameBodyMap.get("acctProvince"));
                        returnMap.put("repayAcCity", queryBankNameBodyMap.get("acctCity") == null ? "" : queryBankNameBodyMap.get("acctCity"));
                    }
                }
            }
            // 此处还款卡银行名称以信贷的为准
            returnMap.put("repayAccBankName", StringUtils.isEmpty(returnMap.get("repayAccBankName")) ? "" : (String) returnMap.get("repayAccBankName"));
            /** 如果利息 费用均为0，则调信贷3.60接口 **/
            if (psNormIntAmt == 0.0 && feeAmt == 0.0) {
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put("applSeq", applSeq);
                Map<String, Object> cmisMap = CmisUtil.getCmisResponse(CmisTradeCode.TRADECODE_HKSS_BYAPPLSEQ,
                        super.getToken(), map);
                logger.info("cmisMap==" + cmisMap);
                Map<String, Object> responseMap = (HashMap<String, Object>) cmisMap.get("response");
                Map<String, Object> headMap = (HashMap<String, Object>) responseMap.get("head");
                // retFlag": "00000",
                // "retMsg": "处理成功"
                String retFlag = String.valueOf(headMap.get("retFlag"));
                String retMsg = String.valueOf(headMap.get("retMsg"));
                if (!"00000".equals(retFlag)) {
                    // return fail(retFlag,retMsg);
                } else {
                    Map<String, Object> bodyMap = (HashMap<String, Object>) responseMap.get("body");
                    // 利息总额 normIntSum
                    // 费用总额 feeSum
                    String normIntSum = StringUtils.isEmpty(bodyMap.get("normIntSum")) ? "0.0"
                            : String.valueOf(bodyMap.get("normIntSum"));
                    String feeAmt_ = StringUtils.isEmpty(bodyMap.get("feeSum")) ? "0.0"
                            : String.valueOf(bodyMap.get("feeSum"));
                    returnMap.put("psNormIntAmt", new BigDecimal(normIntSum).doubleValue());
                    returnMap.put("feeAmt", new BigDecimal(feeAmt_).doubleValue());
                }
            }
            if (returnMap.containsKey("idNo") && returnMap.containsKey("custName")) {
                //返回客户编号
                Map<String, Object> custMap = cmisApplService.getSmrzInfoByCustNameAndIdNo(String.valueOf(returnMap.get("custName")), String.valueOf(returnMap.get("idNo")));
                JSONObject custHeadObject = (JSONObject) custMap.get("head");
                String retFlag = custHeadObject.getString("retFlag");
                if (!"00000".equals(retFlag)) {
                    logger.info("CRM13接口head返回:" + custHeadObject);
                    return fail("80", "客户实名认证信息查询失败！");
                }
                Map<String, Object> custBodyMap = HttpUtil.json2Map(custMap.get("body").toString());
                //客户编号
                String cuNo = String.valueOf(custBodyMap.get("custNo"));
                returnMap.put("custNo", cuNo);
            } else {
                returnMap.put("custNo", "");//身份证号和客户姓名查不到，返回空
            }

            ///返回贷款品种随借随还的日利息
            if (returnMap.containsKey("loanTyp")) {
//                //根据贷款品种详情查询日利率
//                String rll_url = super.getGateUrl() + "/app/cmisproxy/api/pLoanTyp/" + returnMap.get("loanTyp") + "/feeMsg?typCde=" + returnMap.get("loanTyp") + "&tnrOpt=" + returnMap.get("applyTnrTyp") + "&mtdTyp=" + returnMap.get("payMtd") + "&feeTnrTyp=03";
//                logger.info("日利率请求url" + rll_url);
//                String rll_json = HttpUtil.restGet(rll_url, super.getToken());
//                logger.info("日利率查询结果" + rll_json);
//                if (StringUtils.isEmpty(rll_json)) {
//                    return fail("08", "查询失败");
//                }
//                List<Map<String, Object>> resultlist = HttpUtil.json2List(rll_json);
//                logger.info("通过接口请求的合同费率为：" + resultlist);
//                if (resultlist.size() > 0) {
//                    Map<String, Object> resultMap = resultlist.get(0);
//                    String s = resultMap.get("feePct") == null ? "" : resultMap.get("feePct2").toString();
//                    String feerate = "";
//                    if (!StringUtils.isEmpty(s)) {
//                        //如果有日利率，则计算日息
//                        //获取借款总额
//                        String applyAmt = String.valueOf(StringUtils.isEmpty(returnMap.get("applyAmt")) ? 0 : returnMap.get("applyAmt"));
//                        BigDecimal rlx = new BigDecimal(applyAmt).multiply(new BigDecimal(s));
//                        returnMap.put("rlx", rlx);
//                    } else {
//                        returnMap.put("rlx", "");
//                    }
//                } else {
//                    returnMap.put("rlx", "");
//                }
                //获取借款期限类型
                String applyTnrTyp = String.valueOf(returnMap.get("applyTnrTyp"));
                if (Objects.equals(applyTnrTyp, "D")) {
                    //获取贷款品种代码
                    String typCde = String.valueOf(returnMap.get("loanTyp"));
                    //获取借款总额
                    String applyAmt = String.valueOf(StringUtils.isEmpty(returnMap.get("applyAmt")) ? 0 : returnMap.get("applyAmt"));
                    //获取审批总额
                    String apprvAmt = String.valueOf(StringUtils.isEmpty(returnMap.get("apprvAmt")) ? 0 : returnMap.get("apprvAmt"));
                    //按日进行还款试算
                    HashMap<String, Object> hm = new HashMap<>();
                    hm.put("typCde", typCde);
                    if (new BigDecimal(apprvAmt).compareTo(BigDecimal.ZERO) > 0) {
                        hm.put("apprvAmt", apprvAmt);
                    } else {
                        hm.put("apprvAmt", applyAmt);
                    }
                    hm.put("applyTnrTyp", "D");
                    hm.put("applyTnr", 1);
                    Map<String, Object> hkssMap = cmisApplService.getHkssReturnMap(hm, super.getGateUrl(), super.getToken());
                    logger.debug("还款试算结果：" + hkssMap);
                    Map<String, Object> hkssBodyMap = (HashMap<String, Object>) hkssMap.get("body");
                    String rlx = (String.valueOf(hkssBodyMap.get("totalNormInt")));// 总利息金额
                    returnMap.put("rlx", rlx);
                } else {
                    returnMap.put("rlx", "");
                }
            }
            logger.info("==channelNo:" + returnMap.get("channelNo"));
            if ("31".equals(returnMap.get("channelNo")) || "35".equals(returnMap.get("channelNo"))) {//星巢贷、美分期
                // 星巢贷订单，查询营销人员信息
                String promRequestUrl = EurekaServer.CMISPROXY + "/api/appl/getPromInfo?applseq=" + applSeq;
                logger.debug("去信贷系统查询营销人员信息：" + promRequestUrl);
                String promJson = HttpUtil.restGet(promRequestUrl, getToken());
                logger.debug("营销人员查询结果：" + promJson);
                if (StringUtils.isEmpty(promJson)) {
                    return fail("22", "星巢贷订单查询营销人员信息失败");
                }
                if (!"{}".equals(promJson)) {
                    Map<String, Object> promMap = HttpUtil.json2Map(promJson);
                    returnMap.put("promCde", promMap.containsKey("promCde") ? String.valueOf(promMap.get("promCde")) : "");
                    returnMap.put("promPhone", promMap.containsKey("promPhone") ? String.valueOf(promMap.get("promPhone")) : "");
                    returnMap.put("promDesc", promMap.containsKey("promDesc") ? String.valueOf(promMap.get("promDesc")) : "");
                }

                // 星巢贷和美分期订单，返回门店名称和商户名称
                if (!StringUtils.isEmpty(returnMap.get("cooprCde"))) {
                    String crmUrl = EurekaServer.CRM + "/pub/crm/cust/getStoreInfoByStoreNo?storeNo=" + returnMap.get("cooprCde");
                    logger.info("向CRM请求查询门店信息:" + crmUrl);
                    String crmJson = HttpUtil.restGet(crmUrl);
                    logger.info("CRM查询得到门店信息：" + crmJson);
                    String merchName = "";
                    String cooprName = "";
                    if (HttpUtil.isSuccess(crmJson)) {
                        Map<String, Object> crmMap = HttpUtil.json2Map(crmJson);
                        if (!StringUtils.isEmpty(crmMap.get("body"))) {
                            Map<String, Object> bodyMap = HttpUtil.json2Map(crmMap.get("body").toString());
                            merchName = StringUtils.isEmpty(bodyMap.get("merchChName")) ? "" : (String) bodyMap.get("merchChName");
                            cooprName = StringUtils.isEmpty(bodyMap.get("storeName")) ? "" : (String) bodyMap.get("storeName");
                        }
                    }
                    returnMap.put("merchName", merchName);
                    returnMap.put("cooprName", cooprName);
                }
            }
            //金额精度处理
            if (returnMap.containsKey("applyAmt")) {
                double applyAmt = Double.valueOf(String.valueOf(StringUtils.isEmpty(returnMap.get("applyAmt")) ? "0" : String.valueOf(returnMap.get("applyAmt"))));
                returnMap.put("applyAmt", new DecimalFormat("0.00").format(applyAmt));
            }
            if (returnMap.containsKey("apprvAmt")) {
                double apprvAmt = Double.valueOf(String.valueOf(StringUtils.isEmpty(returnMap.get("apprvAmt")) ? "0" : String.valueOf(returnMap.get("apprvAmt"))));
                returnMap.put("apprvAmt", new DecimalFormat("0.00").format(apprvAmt));
            }
            return success(returnMap);
        }
    }


    public Map<String, Object> selectApplInfoApp(String applSeq, String channelNo) {
        String url = EurekaServer.ACQUIRER + "/api/appl/selectApplInfoApp";
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("channelNo", channelNo);
        paramMap.put("applSeq", applSeq);
        logger.info("==>ACQ  获取贷款详情请求参数url:" + url + ", 请求参数:" + paramMap);
        Map<String, Object> acqResponse = AcqUtil.getAcqResponse(url, AcqTradeCode.SELECT_APP_APPL_INFO, super.getChannel(), channelNo, "", "",
                paramMap);
        logger.info("<==ACQ  返回结果:" + acqResponse);
        if (acqResponse == null || acqResponse.isEmpty()) {
            return fail("08", "查询失败");
        }
        if (!CmisUtil.getIsSucceed(acqResponse)) {
            return fail("08", "查询失败");
        }
        Map<String, Object> acqResponseMap = (Map<String, Object>) acqResponse.get("response");
        if (isSuccess(acqResponseMap)) {
            Map<String, Object> acqBody = (Map<String, Object>) acqResponseMap.get("body");
            return success(acqBody);
        } else {
            Map<String, Object> responseHeadMap = (Map<String, Object>) acqResponseMap.get("head");
            return fail("99", (String) responseHeadMap.get("retMsg"));
        }
    }

    public Map<String, Object> setMapValue(String applSeq, Map<String, Object> map) {
        Map<String, Object> returnMap = new HashMap<>();
        returnMap.put("applSeq", applSeq);

        //贷款申请人主表信息
        Map<String, Object> apptListMap = (Map<String, Object>) map.get("apptList");
        if (apptListMap.size() > 0) {
            List<Map<String, Object>> apptList = (List<Map<String, Object>>) apptListMap.get("appt");
            for (Map mapTem : apptList) {
                String apptTyp = StringUtils.isEmpty(mapTem.get("appt_typ")) ? "" : (String) mapTem.get("appt_typ");// 申请人类型 01 - 主申请人
                if ("01".equals(apptTyp)) {
                    returnMap.put("indivMobile", StringUtils.isEmpty(mapTem.get("indiv_mobile")) ? "" : mapTem.get("indiv_mobile"));//客户手机号
                    returnMap.put("mailOpt", StringUtils.isEmpty(mapTem.get("mail_opt")) ? "" : mapTem.get("mail_opt"));//送货地址选项
                    returnMap.put("mailAddr", StringUtils.isEmpty(mapTem.get("mail_addr")) ? "" : mapTem.get("mail_addr"));//送货地址
                    returnMap.put("mailProvince", StringUtils.isEmpty(mapTem.get("mail_province")) ? "" : mapTem.get("mail_province"));//送货地址省
                    returnMap.put("mailCity", StringUtils.isEmpty(mapTem.get("mail_city")) ? "" : mapTem.get("mail_city"));//送货地址市
                    returnMap.put("mailArea", StringUtils.isEmpty(mapTem.get("mail_area")) ? "" : mapTem.get("mail_area"));//送货地址区
                }
            }
        }

        returnMap.put("applCde", StringUtils.isEmpty(map.get("appl_cde")) ? "" : map.get("appl_cde"));//贷款编号
        returnMap.put("applyDt", StringUtils.isEmpty(map.get("apply_dt")) ? "" : map.get("apply_dt"));//申请日期
        returnMap.put("custId", StringUtils.isEmpty(map.get("cust_id")) ? "" : map.get("cust_id"));//客户编号（信贷）
        returnMap.put("custName", StringUtils.isEmpty(map.get("cust_name")) ? "" : map.get("cust_name"));//客户姓名

        returnMap.put("loanTyp", StringUtils.isEmpty(map.get("typ_cde")) ? "" : map.get("typ_cde"));//贷款品种代码
        returnMap.put("loanTypName", StringUtils.isEmpty(map.get("typ_desc")) ? "" : map.get("typ_desc"));// 贷款品种名称
        returnMap.put("applyTnrTyp", StringUtils.isEmpty(map.get("apply_tnr_typ")) ? "" : map.get("apply_tnr_typ"));//还款期限类型
        returnMap.put("applyTnr", StringUtils.isEmpty(map.get("apply_tnr")) ? "" : map.get("apply_tnr"));//还款期限
        returnMap.put("proPurAmt", StringUtils.isEmpty(map.get("pro_pur_amt")) ? "" : map.get("pro_pur_amt"));//商品总额
        returnMap.put("fstPay", StringUtils.isEmpty(map.get("fst_pay")) ? "" : map.get("fst_pay"));//首付金额
        returnMap.put("applyAmt", StringUtils.isEmpty(map.get("apply_amt")) ? "" : map.get("apply_amt"));//借款总额
        returnMap.put("superCoopr", StringUtils.isEmpty(map.get("super_coopr")) ? "" : map.get("super_coopr"));//商户编号
        returnMap.put("cooprCde", StringUtils.isEmpty(map.get("coopr_cde")) ? "" : map.get("coopr_cde"));//门店编号
        returnMap.put("typGrp", StringUtils.isEmpty(map.get("typ_grp")) ? "" : map.get("typ_grp"));//贷款类型
        returnMap.put("mtdCde", StringUtils.isEmpty(map.get("mtd_cde")) ? "" : map.get("mtd_cde"));//还款方式代码

        returnMap.put("mtdDesc", StringUtils.isEmpty(map.get("mtd_desc")) ? "" : map.get("mtd_desc"));// 还款方式名称
        returnMap.put("applAcBank", StringUtils.isEmpty(map.get("acc_bank_cde")) ? "" : map.get("acc_bank_cde"));//放款开户银行代码
        returnMap.put("applAcBankDesc", StringUtils.isEmpty(map.get("acc_bank_name")) ? "" : map.get("acc_bank_name"));//放款开户银行名
        returnMap.put("applAcBch", StringUtils.isEmpty(map.get("appl_ac_bch")) ? "" : map.get("appl_ac_bch"));//放款开户银行分支行代码
        logger.info("acq返回的 放款开户银行分支行代码:" + map.get("appl_ac_bch") + ",放款开户银行分支行名:" + map.get("appl_ac_bch_desc"));
        //放款开户银行分支行名采用收单返回得值，返回空也显示空。不再单独查询CRM，由收单维护此值。
        returnMap.put("applAcBchDesc", StringUtils.isEmpty(map.get("appl_ac_bch_desc")) ? "" : map.get("appl_ac_bch_desc"));//放款开户银行分支行名

        ////20170615更新，从收单获取
        // 还款卡账号开户机构名称，还款账号开户机构代码，放款卡支行名称
        returnMap.put("repayAccBchName", StringUtils.isEmpty(map.get("repay_appl_ac_bch_name")) ? "" : map.get("repay_appl_ac_bch_name"));//放款开户银行分支行代码
        returnMap.put("repayAccBchCde", StringUtils.isEmpty(map.get("repay_appl_ac_bch")) ? "" : map.get("repay_appl_ac_bch"));//放款开户银行分支行代码
        returnMap.put("accAcBchName", StringUtils.isEmpty(map.get("acc_ac_bch_name")) ? "" : map.get("acc_ac_bch_name"));//放款开户银行分支行代码

        returnMap.put("psNormIntAmt", 0);//总利息金额
        returnMap.put("feeAmt", 0);//费用总额
        returnMap.put("apprvAmt", StringUtils.isEmpty(map.get("apprv_amt")) ? "" : map.get("apprv_amt"));//审批金额
        returnMap.put("appOutAdvice", StringUtils.isEmpty(map.get("app_out_advice")) ? "" : map.get("app_out_advice"));//退回原因
        returnMap.put("contNo", StringUtils.isEmpty(map.get("cont_no")) ? "" : map.get("cont_no"));//合同号
        returnMap.put("payMtd", StringUtils.isEmpty(map.get("pay_mtd")) ? "" : map.get("pay_mtd"));// 还款方式种类代码
        returnMap.put("payMtdDesc", StringUtils.isEmpty(map.get("pay_mtd_desc")) ? "" : map.get("pay_mtd_desc"));// 还款方式种类名称
        returnMap.put("loanNo", StringUtils.isEmpty(map.get("loan_no")) ? "" : map.get("loan_no"));//借据号
        returnMap.put("applCardNo", StringUtils.isEmpty(map.get("appl_card_no")) ? "" : map.get("appl_card_no"));//2016.10.24 尹君将此此字段改为放款卡号（applCardNo）
        returnMap.put("mthAmt", StringUtils.isEmpty(map.get("mth_amt")) ? "" : map.get("mth_amt"));//期供金额
        returnMap.put("channelNo", StringUtils.isEmpty(map.get("channel_no")) ? "" : map.get("channel_no"));//渠道编码
        returnMap.put("idTyp", StringUtils.isEmpty(map.get("id_typ")) ? "" : map.get("id_typ"));//证件类型
        returnMap.put("idNo", StringUtils.isEmpty(map.get("id_no")) ? "" : map.get("id_no"));//证件号码

        String outSts = StringUtils.isEmpty(map.get("outSts")) ? "" : (String) map.get("outSts");
        returnMap.put("outSts", outSts);

        returnMap.put("appInAdvice", StringUtils.isEmpty(map.get("app_in_advice")) ? "" : map.get("app_in_advice"));//备注
        returnMap.put("expectCredit", StringUtils.isEmpty(map.get("expectCredit")) ? "0" : map.get("expectCredit"));//期望额度
        returnMap.put("repayApplCardNo", StringUtils.isEmpty(map.get("repay_appl_card_no")) ? "" : map.get("repay_appl_card_no"));//还款卡号
        returnMap.put("repayAccBankName", StringUtils.isEmpty(map.get("repay_acc_bank_name")) ? "" : map.get("repay_acc_bank_name"));//还款卡银行名称

        BigDecimal repayAmt = StringUtils.isEmpty(map.get("repayAmt")) ? BigDecimal.ZERO : new BigDecimal(map.get("repayAmt").toString());//还款总额
        BigDecimal psPrcpAmt = StringUtils.isEmpty(map.get("psPrcpAmt")) ? BigDecimal.ZERO : new BigDecimal(map.get("psPrcpAmt").toString());//应还本金
        BigDecimal setlPrcpAmt = StringUtils.isEmpty(map.get("setlPrcpAmt")) ? BigDecimal.ZERO : new BigDecimal(map.get("setlPrcpAmt").toString());//已还本金
        BigDecimal psIncAmt = StringUtils.isEmpty(map.get("psIncAmt")) ? BigDecimal.ZERO : new BigDecimal(map.get("psIncAmt").toString());//应还利息
        BigDecimal setlIncAmt = StringUtils.isEmpty(map.get("setlIncAmt")) ? BigDecimal.ZERO : new BigDecimal(map.get("setlIncAmt").toString());//已还利息
        BigDecimal psFeeAmt = StringUtils.isEmpty(map.get("psFeeAmt")) ? BigDecimal.ZERO : new BigDecimal(map.get("psFeeAmt").toString());//应还费用
        BigDecimal setlFeeAmt = StringUtils.isEmpty(map.get("setlFeeAmt")) ? BigDecimal.ZERO : new BigDecimal(map.get("setlFeeAmt").toString());//已还费用

        returnMap.put("repayAmt", repayAmt);//还款总额
        returnMap.put("psPrcpAmt", psPrcpAmt);//应还本金
        returnMap.put("setlPrcpAmt", setlPrcpAmt);//已还本金
        returnMap.put("repayPrcpAmt", psPrcpAmt.subtract(setlPrcpAmt));//剩余本金
        returnMap.put("psNormIntAmt", psIncAmt);//应还利息
        returnMap.put("setlIntAmt", setlIncAmt);//已还利息
        returnMap.put("repayIntAmt", psIncAmt.subtract(setlIncAmt));//剩余利息
        returnMap.put("feeAmt", psFeeAmt);//应还费用
        returnMap.put("setlFeeAmt", setlFeeAmt);//已还费用
        returnMap.put("repayFeeAmt", psFeeAmt.subtract(setlFeeAmt));//剩余费用

        // 去订单系统查询订单状态等信息
        String batchQueryOrderStateUrl = EurekaServer.ORDER + "/api/order/batchQueryOrderState";
        List<Map<String, Object>> requestList = new ArrayList<>();
        Map<String, Object> applSeqMap = new HashMap<>();
        applSeqMap.put("applSeq", applSeq);
        requestList.add(applSeqMap);
        String requestOrderJson = JSONObject.valueToString(requestList);
        Map<String, Object> orderResponseMap = appOrderService.batchQueryOrderState(batchQueryOrderStateUrl, requestOrderJson);
        Map<String, Object> orderBodyMap = (Map<String, Object>) orderResponseMap.get("body");
        List<Map<String, Object>> orderStateList = (List<Map<String, Object>>) orderBodyMap.get("list");
        if (orderStateList.size() != requestList.size()) {
            return fail("99", "查询失败");
        }
        for (int i = 0; i < orderStateList.size(); i++) {
            Map<String, Object> orderStateMap = orderStateList.get(i);

            String formTyp = StringUtils.isEmpty(orderStateMap.get("formTyp")) ? "" : (String) orderStateMap.get("formTyp");//订单类型
            returnMap.put("formTyp", formTyp);//订单类型 10-线下订单 20-线上订单 21-商户扫码录单 11-个人扫码录单
            //获取订单状态
            String sysSts = StringUtils.isEmpty(orderStateMap.get("sysSts")) ? "" : (String) orderStateMap.get("sysSts");
            //处理返回状态
            if ("06".equals(outSts)) {//已放款 已放款的，除了逾期，优先返回发货状态
                //hkSts还款状态：  还款中（00）：逾期（01）：已结清（02）    其余状态返空。
                String hkSts = StringUtils.isEmpty(map.get("hkSts")) ? "" : (String) map.get("hkSts");
                if ("01".equals(hkSts)) {//逾期
                    returnMap.put("outSts", "OD");
                } else {//非逾期 优先返回发货状态
                    if ("02".equals(hkSts)) {//已结清
                        //SE为结清，NS为未结清
                        returnMap.put("ifSettled", "SE");
                    } else {//还款中（00）
                        returnMap.put("ifSettled", "NS");
                    }

                    if ("92".equals(sysSts) || "93".equals(sysSts) || "30".equals(sysSts) || "31".equals(sysSts)) {//92-退货中 93-已退货
                        returnMap.put("outSts", sysSts);
                    }
                }
            } else if ("04".equals(outSts)) {//04-合同签订中
                if ("10".equals(formTyp) && "30".equals(sysSts)) {//10-线下订单
                    returnMap.put("outSts", sysSts);//30-已付款待发货
                }
            }
            if ("05".equals(outSts)) {//05-审批通过，等待放款
                if ("10".equals(formTyp) && "31".equals(sysSts)) {//10-线下订单
                    returnMap.put("outSts", sysSts);//31-已发货
                }
            }

            //                        待发货先按这个逻辑处理。收单不管发货状态。
            //                        outSts='04'合同签订中，线下订单，订单状态为“30-待发货”；
            //                        outSts='06'已放款，线上订单，订单状态为“30-待发货”。
            //                        outSts='05'线下订单，订单状态为“31-已发货”；
            //                        outSts='06'线下订单，订单状态为“80-还款中”。
            //                        线上订单的已发货状态根据外部系统接口判断。
        }
        //商品列表
        Map<String, Object> goodsListMap = (Map<String, Object>) map.get("goodsList");
        if (goodsListMap.size() > 0) {
            List<Map<String, Object>> goodsList = (List<Map<String, Object>>) goodsListMap.get("good");
            List<Map<String, Object>> goodsListNew = new ArrayList<>();
            for (Map goodsMap : goodsList) {
                Map<String, Object> goodsMapNew = new HashMap<>();
                if (goodsMap.containsKey("goods_name")) {//商品名称
                    goodsMapNew.put("goodsName", goodsMap.get("goods_name"));
                }
                if (goodsMap.containsKey("goods_model")) {//商品编号
                    goodsMapNew.put("goodsCode", goodsMap.get("goods_model"));
                }
                if (goodsMap.containsKey("goods_price")) {//商品价格
                    goodsMapNew.put("goodsPrice", goodsMap.get("goods_price"));
                }
                if (goodsMap.containsKey("goods_num")) {//数量
                    goodsMapNew.put("goodsNum", goodsMap.get("goods_num"));
                }
                if (goodsMap.containsKey("goods_kind")) {//商品类型
                    goodsMapNew.put("goodsKind", goodsMap.get("goods_kind"));
                }
                if (goodsMap.containsKey("goods_brand")) {//商品品牌
                    goodsMapNew.put("goodsBrand", goodsMap.get("goods_brand"));
                }
                if (goodsMap.containsKey("goods_code")) {//商品编码
                    goodsMapNew.put("goodsCode", goodsMap.get("goods_code"));
                }
                goodsListNew.add(goodsMapNew);
            }
            returnMap.put("goods", goodsListNew);

        }
        return returnMap;
    }

    //设置订单信息
    public void setOrderInf(String applSeq, Map<String, Object> returnMap) throws Exception {
        String batchQueryOrderStateUrl = EurekaServer.ORDER + "/api/order/batchQueryOrderState";
        List<Map<String, Object>> requestList = new ArrayList<>();
        Map<String, Object> applSeqMap = new HashMap<>();
        applSeqMap.put("applSeq", applSeq);
        requestList.add(applSeqMap);
        String requestOrderJson = JSONObject.valueToString(requestList);
        Map<String, Object> orderResponseMap = appOrderService.batchQueryOrderState(batchQueryOrderStateUrl, requestOrderJson);

        if (orderResponseMap == null || orderResponseMap.isEmpty()) {
            logger.info("订单系统通信失败");
            throw new Exception("系统通信失败");
        }
        if (!HttpUtil.isSuccess(orderResponseMap)) {
            logger.info("OM批量查询订单状态" + orderResponseMap);
            ResultHead repHead = (ResultHead) orderResponseMap.get("head");
            logger.info(repHead.getRetMsg());
            throw new Exception((String) repHead.getRetFlag());
        }
        Map<String, Object> orderBodyMap = (Map<String, Object>) orderResponseMap.get("body");
        List<Map<String, Object>> orderStateList = (List<Map<String, Object>>) orderBodyMap.get("list");
        if (orderStateList.size() != requestList.size()) {
            logger.info("查询失败");
            throw new Exception("查询失败");
        }
        for (Map<String, Object> orderMap : orderStateList) {
            String formTyp = StringUtils.isEmpty(orderMap.get("formTyp")) ? "" : (String) orderMap.get("formTyp");//订单类型
            String isAllowReturn = StringUtils.isEmpty(orderMap.get("isAllowReturn")) ? "" : (String) orderMap.get("isAllowReturn");
            returnMap.put("formTyp", formTyp);//订单类型 10-线下订单 20-线上订单 21-商户扫码录单 11-个人扫码录单
            returnMap.put("isAllowReturn", isAllowReturn);
            break;
        }
    }
}
