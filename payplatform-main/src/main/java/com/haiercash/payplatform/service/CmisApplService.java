package com.haiercash.payplatform.service;

import com.haiercash.core.lang.Convert;
import com.haiercash.payplatform.common.dao.AppOrdernoTypgrpRelationDao;
import com.haiercash.payplatform.common.data.AppOrder;
import com.haiercash.payplatform.common.data.AppOrdernoTypgrpRelation;
import com.haiercash.payplatform.utils.AcqTradeCode;
import com.haiercash.payplatform.utils.CmisTradeCode;
import com.haiercash.payplatform.utils.CmisUtil;
import com.haiercash.spring.config.EurekaServer;
import com.haiercash.spring.service.BaseService;
import com.haiercash.spring.util.ConstUtil;
import com.haiercash.spring.util.HttpUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author 尹君
 * @date 2016/7/5
 * @description:提供信贷系统进件相关的接口
 **/
@Service
public class CmisApplService extends BaseService {
    private final Log logger = LogFactory.getLog(this.getClass());

    @Autowired
    private AcquirerService acquirerService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private AppOrdernoTypgrpRelationDao appOrdernoTypgrpRelationDao;

    /**
     * 查询贷款品种详情
     *
     * @param typCde
     * @return
     */
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

    /**
     * 还款试算的service
     *
     * @param map     必输：typCde, apprvAmt, applyTnrTyp, applyTnr
     * @param gateUrl
     * @param token
     * @return
     */
    public Map<String, Object> getHkssReturnMap(Map map, String gateUrl, String token) {
        /**
         * 查询贷款品种详情
         */
        Map<String, Object> typCdeMap = this.findPLoanTyp((String) map.get("typCde"));
        if (!HttpUtil.isSuccess(typCdeMap)) {
            return typCdeMap;
        }
        Map<String, Object> dataMap = (Map<String, Object>) typCdeMap.get("body");
        logger.info("贷款品种详情dataMap：" + dataMap);
        if (!map.containsKey("typSeq")) {
            // 贷款品种序号
            map.put("typSeq", dataMap.get("typSeq"));
        }
        // 还款间隔
        if (!map.containsKey("loanFreq")) {
            map.put("loanFreq", dataMap.get("typFreq"));
        }
        String mtdCde = Convert.toString(map.get("mtdCde"));
        if (StringUtils.isEmpty(mtdCde)) {
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
                mtdCde = Convert.toString(hkmap.get("mtdCde"));
                if (StringUtils.isEmpty(mtdCde)) {
                    return fail("08", "还款方式编码为空");
                }
                map.put("mtdCde", mtdCde);
            }
        }

        if (!map.containsKey("proPurAmt")) {
            map.put("proPurAmt", "0");
        }
        // 如果flag为空，则默认为0,反之，接收flag的实际值
        if (!map.containsKey("flag")) {
            map.put("flag", "0");
        }
        Map<String, Object> result = new HashMap<>();
        logger.debug("map==" + map);
        try {
            map.put("sysFlag", super.getChannel());
            map.put("channelNo", super.getChannelNo());
            result = CmisUtil.getCmisResponse(CmisTradeCode.TRADECODE_HKSS, token, map);
        } catch (Exception e) {
            logger.debug("发现异常被执行：==" + e.getMessage());
        }
        logger.debug("还款试算cmis查询结果result.get('response')==" + result.get("response"));
        Map<String, Object> responseMap = (Map<String, Object>) result.get("response");
        Map<String, Object> headMap = (Map<String, Object>) responseMap.get("head");
        String retFlag = String.valueOf(headMap.get("retFlag"));
        String retMsg = String.valueOf(headMap.get("retMsg"));
        if (!"00000".equals(retFlag)) {
            return fail("99", "还款试算接口异常：" + retMsg);
        }
        Map<String, Object> bodyMap = (Map<String, Object>) responseMap.get("body");
        logger.debug("=======" + result);
        HashMap<String, Object> returnmap = new HashMap<>();
        System.out.println(result);
        List relist = new ArrayList();
        Map<String, Object> payList = (Map<String, Object>) bodyMap.get("payList");
        List<HashMap<String, Object>> mx = (List<HashMap<String, Object>>) payList.get("mx");
        if (mx != null) {
            // 初始化第0期的费用为0
            BigDecimal fee_0 = BigDecimal.ZERO;
            BigDecimal deductAmt = BigDecimal.ZERO;//斩头息费
            BigDecimal actualArriveAmt = BigDecimal.ZERO;//实际到账金额
            logger.debug("===还款试算的还款计划列表:" + mx);
            for (Map<String, Object> hm : mx) {
                HashMap<String, Object> newhm = new HashMap<>();
                if (Convert.toInteger(hm.get("psPerdNo")) == 0) {
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
                BigDecimal instmAmt = new BigDecimal(String.valueOf(hm.get("instmAmt")));
                String feeString = String.valueOf(StringUtils.isEmpty(hm.get("psFeeAmt")) ? "0" : hm.get("psFeeAmt"));
                if (!StringUtils.isEmpty(feeString) && (!Objects.equals(feeString, "null"))) {
                    BigDecimal defee = new BigDecimal(feeString);
                    instmAmt = instmAmt.add(defee);
                }
                instmAmt = instmAmt.setScale(2, BigDecimal.ROUND_HALF_UP);
                // 每期应归还的期供等于期供+费用
                newhm.put("instmAmt", instmAmt.toString());
                //每期的到期日
                newhm.put("dueDt", hm.get("dueDt"));
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
            returnmap.put("mtdCde", mtdCde);
        }
        returnmap.put("mx", relist);
        logger.debug("还款试算返回：" + returnmap);
        return success(returnmap);
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
        return HttpUtil.json2Map(cust_json);
    }

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
        AppOrdernoTypgrpRelation relation = appOrdernoTypgrpRelationDao.selectByOrderNo(order.getOrderNo());
        if (relation == null) {
            return fail("23", "订单信息不存在");
        }
        if ("02".equals(relation.getTypGrp())) {//现金贷
            Map<String, Object> resultMap = acquirerService
                    .cashLoan(order, appOrdernoTypgrpRelationDao.selectByOrderNo(order.getOrderNo()));
            if (!CmisUtil.isSuccess(resultMap)) {
                return (Map<String, Object>) resultMap.get("response");
            }
            logger.debug("收单系统接口" + AcqTradeCode.COMMIT_APPL + "开始");
            Map<String, Object> result = acquirerService.commitAppl(order, "1", null);//
            logger.debug("收单系统接口" + AcqTradeCode.COMMIT_APPL + "结束");
            return result;
        } else {
//            if (!"2".equals(order.getStatus())) {
//                this.cleanGoodsInfo(order);
//                // 防止个人版保存商户版提交时修改系统标识信息
//                order.setSource(null);
//                Map<String, Object> resultMap = orderService.saveOrUpdateAppOrder(order, null);
//                if (!HttpUtil.isSuccess(resultMap)) {
//                    return resultMap;
//                }
//            }
            logger.debug("订单系统接口提交订单开始");
            return orderService.submitOrder(order.getOrderNo(), "0");
        }
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