package com.haiercash.payplatform.pc.qiaorong.service.impl;

import com.haiercash.commons.redis.Session;
import com.haiercash.payplatform.config.EurekaServer;
import com.haiercash.payplatform.pc.qiaorong.service.QiaorongService;
import com.haiercash.payplatform.service.BaseService;
import com.haiercash.payplatform.service.CmisApplService;
import com.haiercash.payplatform.utils.ConstUtil;
import com.haiercash.payplatform.utils.HttpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;

/**
 * Created by yuanli on 2017/9/25.
 */
public class QiaorongServiceImpl extends BaseService implements QiaorongService {

    @Autowired
    private Session session;
    @Autowired
    private CmisApplService cmisApplService;

    /*
    合同初始化
     */
    @Override
    public Map<String, Object> contractInit(Map<String, Object> map) {
        logger.info("合同展示页面初始化*******开始");
        String applseq = (String) map.get("applseq");
        Map<String, Object> cachemap = new HashMap<String, Object>();
        logger.info("申请流水号：" + applseq);
        if(StringUtils.isEmpty(applseq)){
            logger.info("申请流水号为空");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        }

        // 去cmis查询订单信息
        Map<String, Object> appOrderMapCmis = this.getAppOrderMapFromCmis(applseq);
        if (appOrderMapCmis == null || appOrderMapCmis.isEmpty()) {
            logger.info("未查询到贷款信息");
            return fail(ConstUtil.ERROR_CODE, "未查询到贷款信息");
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("totalamount", appOrderMapCmis.get("APPLY_AMT"));//应还总额
        resultMap.put("loannumber", appOrderMapCmis.get("APPLY_TNR"));//剩余期数
        List<String> goodsNameList = new ArrayList<>();
        List<Map<String, Object>> goods = (List<Map<String, Object>>) appOrderMapCmis.get("goods");
        if (goods != null && !goods.isEmpty()) {
            for (Map<String, Object> good : goods) {
                goodsNameList.add((String) good.get("GOODS_NAME"));
            }
        }
        resultMap.put("goods", goodsNameList);//分期产品
        resultMap.put("name", appOrderMapCmis.get("CUST_NAME"));//姓名
        resultMap.put("phone", appOrderMapCmis.get("INDIV_MOBILE"));//手机号
        resultMap.put("idnumber", appOrderMapCmis.get("ID_NO"));//身份证号
        //还款帐号
        List<Map<String, Object>> cardList = (ArrayList<Map<String, Object>>) appOrderMapCmis.get("accInfo");
        for (Map<String, Object> card : cardList) {
            // 获取卡类型
            String type = String.valueOf(card.get("APPL_AC_KIND"));
            if ("02".equals(type) || "2".equals(type)) {
                resultMap.put("cardnumber", card.get("APPL_AC_NO"));//还款卡号
                cachemap.put("cardnumber", card.get("APPL_AC_NO"));
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
                resultMap.put("loanpayment", perMxString);//每期应还金额
            }
        }

        String uuid = UUID.randomUUID().toString().replace("-", "");
        resultMap.put("token", uuid);


        cachemap.put("name", appOrderMapCmis.get("CUST_NAME"));
        cachemap.put("phone", appOrderMapCmis.get("INDIV_MOBILE"));
        cachemap.put("idNo", appOrderMapCmis.get("ID_NO"));

        cachemap.put("totalamount", appOrderMapCmis.get("APPLY_AMT"));
        session.set(uuid, cachemap);


        logger.info(resultMap);
        logger.info("合同展示页面初始化*******结束");
        return success(resultMap);
    }

    @Override
    public Map<String, Object> checkFourKeys(Map<String, Object> map) {
        logger.info("实名四要素验证****************开始");
        String token = (String) map.get("token");
        if(StringUtils.isEmpty(token)){
            logger.info("token为空");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        }


//        String code,
//        String name,
//        String phone,
//        String idNo,
//        String cardnumber,
//        String totalamount

        return null;
    }

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
}
