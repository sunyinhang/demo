package com.haiercash.payplatform.service.impl;

import com.haiercash.core.collection.MapUtils;
import com.haiercash.core.lang.Convert;
import com.haiercash.core.lang.StringUtils;
import com.haiercash.payplatform.common.dao.AppOrdernoTypgrpRelationDao;
import com.haiercash.payplatform.common.data.AppOrdernoTypgrpRelation;
import com.haiercash.payplatform.service.AcquirerService;
import com.haiercash.payplatform.service.AppServerService;
import com.haiercash.payplatform.service.CustExtInfoService;
import com.haiercash.payplatform.service.InstallmentAccountService;
import com.haiercash.payplatform.service.OrderService;
import com.haiercash.spring.redis.RedisUtils;
import com.haiercash.spring.rest.IResponse;
import com.haiercash.spring.service.BaseService;
import com.haiercash.spring.util.ConstUtil;
import com.haiercash.spring.util.HttpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ljy on 2017/8/17.
 */
@Service
public class InstallmentAccountServiceImpl extends BaseService implements InstallmentAccountService {
    @Autowired
    private AppServerService appServerService;
    @Autowired
    private AcquirerService acquirerService;
    @Autowired
    private AppOrdernoTypgrpRelationDao appOrdernoTypgrpRelationDao;
    @Autowired
    private OrderService orderService;
    @Autowired
    private CustExtInfoService custExtInfoService;


    @Override
    public Map<String, Object> queryAllLoanInfo(String token, String channelNo, String channel, Map<String, Object> map) {
        logger.info("*********查询全部贷款信息列表**************开始");
        int page;
        int size;
        //参数非空判断
        if (token.isEmpty()) {
            logger.info("token为空");
            return fail(ConstUtil.ERROR_CODE, "参数token为空!");
        }
        if (channel.isEmpty()) {
            logger.info("channel为空");
            return fail(ConstUtil.ERROR_CODE, "参数channel为空!");
        }
        if (channelNo.isEmpty()) {
            logger.info("channelNo为空");
            return fail(ConstUtil.ERROR_CODE, "参数channelNo为空!");
        }
        if (map.get("page") == null || "".equals(map.get("page"))) {
            logger.info("pwd为空");
            return fail(ConstUtil.ERROR_CODE, "参数pwd为空!");
        }
        if (map.get("size") == null || "".equals(map.get("size"))) {
            logger.info("size为空");
            return fail(ConstUtil.ERROR_CODE, "参数size为空!");
        }
        //缓存数据获取
        Map<String, Object> cacheMap = RedisUtils.getExpireMap(token);
        if (MapUtils.isEmpty(cacheMap)) {
            logger.info("Redis数据获取失败");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        //总入口需查询客户信息数据
        String crtUsr = (String) cacheMap.get("userId");
        String idNo = (String) cacheMap.get("idCard");//身份证号
//        String crtUsr = "15264826872";
//        String idNo = "37040319910722561X";
        if (crtUsr == null || "".equals(crtUsr)) {
            logger.info("crtUsr为空");
            return fail(ConstUtil.ERROR_CODE, "UserID为空!");
        }
        if (idNo == null || "".equals(idNo)) {
            logger.info("idCard为空");
            return fail(ConstUtil.ERROR_CODE, "idCard为空!");
        }
        page = (Integer) map.get("page");
        size = (Integer) map.get("size");
        Map req = new HashMap<String, Object>();
        req.put("channelNo", channelNo);
        req.put("channel", channel);
        req.put("crtUsr", crtUsr);
        req.put("idNo", idNo);
        req.put("page", String.valueOf(page));
        req.put("size", String.valueOf(size));
        logger.info("查询全部贷款信息列表接口，请求数据：" + req.toString());
        Map<String, Object> dateAppOrderPerson = appServerService.getDateAppOrderPerson(token, req);
        if (dateAppOrderPerson == null) {
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        }
        Map resultmapjsonMap = (Map<String, Object>) dateAppOrderPerson.get("head");
        String resultmapFlag = (String) resultmapjsonMap.get("retFlag");
        if (!"00000".equals(resultmapFlag)) {
            String retMsg = (String) resultmapjsonMap.get("retMsg");
            return fail(ConstUtil.ERROR_CODE, retMsg);
        }
        Map resultmapbodyMap = (Map<String, Object>) dateAppOrderPerson.get("body");
        List<Map> ordersList = (List) resultmapbodyMap.get("orders");
        Map<String, Object> orders = new HashMap<>();
        orders.put("orders", ordersList);
        dateAppOrderPerson.put("body", orders);
        return dateAppOrderPerson;
    }

    @Override
    public Map<String, Object> queryPendingLoanInfo(String token, String channelNo, String channel, Map<String, Object> map) {
        logger.info("*********查询待提交订单列表**************开始");
        int page;
        int size;
        //参数非空判断
        if (token.isEmpty()) {
            logger.info("token为空");
            return fail(ConstUtil.ERROR_CODE, "参数token为空!");
        }
        if (channel.isEmpty()) {
            logger.info("channel为空");
            return fail(ConstUtil.ERROR_CODE, "参数channel为空!");
        }
        if (channelNo.isEmpty()) {
            logger.info("channelNo为空");
            return fail(ConstUtil.ERROR_CODE, "参数channelNo为空!");
        }
        if (map.get("page") == null || "".equals(map.get("page"))) {
            logger.info("pwd为空");
            return fail(ConstUtil.ERROR_CODE, "参数pwd为空!");
        }
        if (map.get("size") == null || "".equals(map.get("size"))) {
            logger.info("size为空");
            return fail(ConstUtil.ERROR_CODE, "参数size为空!");
        }
        //缓存数据获取
        Map<String, Object> cacheMap = RedisUtils.getExpireMap(token);
        if (MapUtils.isEmpty(cacheMap)) {
            logger.info("Redis数据获取失败");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        //总入口需查询客户信息数据
        String custNo = (String) cacheMap.get("custNo");
//        String custNo = "C201708090109301402340";
//        custNo = (String) map.get("custNo");
        if (custNo == null || "".equals(custNo)) {
            logger.info("custNo为空");
            return fail(ConstUtil.ERROR_CODE, "custNo为空!");
        }
        page = (Integer) map.get("page");
        size = (Integer) map.get("size");
        Map req = new HashMap<String, Object>();
        req.put("channelNo", channelNo);
        req.put("channel", channel);
        req.put("custNo", custNo);
        req.put("page", String.valueOf(page));
        req.put("size", String.valueOf(size));
        logger.info("查询待提交订单列表接口，请求数据：" + req.toString());
        Map<String, Object> dateAppOrderPerson = appServerService.getWtjAppOrderCust(token, req);
        if (dateAppOrderPerson == null) {
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        }
        Map resultmapjsonMap = (Map<String, Object>) dateAppOrderPerson.get("head");
        String resultmapFlag = (String) resultmapjsonMap.get("retFlag");
        if (!"00000".equals(resultmapFlag)) {
            String retMsg = (String) resultmapjsonMap.get("retMsg");
            return fail(ConstUtil.ERROR_CODE, retMsg);
        }
        return dateAppOrderPerson;
    }

    //待还款信息查询(全部)
    @Override
    public Map<String, Object> queryPendingRepaymentInfo(String token, String channelNo, String channel, Map<String, Object> map) {
        logger.info("*********待还款信息查询(全部)**************开始");
        int page;
        int size;
        //参数非空判断
        if (token.isEmpty()) {
            logger.info("token为空");
            return fail(ConstUtil.ERROR_CODE, "参数token为空!");
        }
        if (channel.isEmpty()) {
            logger.info("channel为空");
            return fail(ConstUtil.ERROR_CODE, "参数channel为空!");
        }
        if (channelNo.isEmpty()) {
            logger.info("channelNo为空");
            return fail(ConstUtil.ERROR_CODE, "参数channelNo为空!");
        }
        if (map.get("page") == null || "".equals(map.get("page"))) {
            logger.info("pwd为空");
            return fail(ConstUtil.ERROR_CODE, "参数pwd为空!");
        }
        if (map.get("size") == null || "".equals(map.get("size"))) {
            logger.info("size为空");
            return fail(ConstUtil.ERROR_CODE, "参数size为空!");
        }
        //缓存数据获取
        Map<String, Object> cacheMap = RedisUtils.getExpireMap(token);
        if (MapUtils.isEmpty(cacheMap)) {
            logger.info("Redis数据获取失败");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        //总入口需查询客户信息数据
        String idNo = (String) cacheMap.get("idCard");
        if (idNo == null || "".equals(idNo)) {
            logger.info("idCard为空");
            return fail(ConstUtil.ERROR_CODE, "idCard为空!");
        }
        page = (Integer) map.get("page");
        size = (Integer) map.get("size");
        Map req = new HashMap<String, Object>();
        req.put("channelNo", channelNo);
        req.put("channel", channel);
        req.put("idNo", idNo);
        req.put("page", String.valueOf(page));
        req.put("size", String.valueOf(size));
//        req.put("flag", "A");
        logger.info("待还款信息查询(全部)接口，请求数据：" + req.toString());
        Map<String, Object> dateAppOrderPerson = appServerService.queryApplAllByIdNo(token, req);
        if (dateAppOrderPerson == null) {
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        }
        Map resultmapjsonMap = (Map<String, Object>) dateAppOrderPerson.get("head");
        String resultmapFlag = (String) resultmapjsonMap.get("retFlag");
        if (!"00000".equals(resultmapFlag)) {
            String retMsg = (String) resultmapjsonMap.get("retMsg");
            return fail(ConstUtil.ERROR_CODE, retMsg);
        }
        Map<String, Object> body = (Map<String, Object>) dateAppOrderPerson.get("body");
        List<Map<String, Object>> order = (List<Map<String, Object>>) body.get("orders");
        List<Map<String, Object>> order_rep = new ArrayList<>();
        for (Map<String, Object> ordermap : order) {
            BigDecimal sybj = Convert.toDecimal(ordermap.get("sybj"));
            Integer remainDays = Convert.toInteger(ordermap.get("remainDays"));
//            String psDueDt = Convert.toString(ordermap.get("psDueDt"));
            if (remainDays >= 0) {
                ordermap.put("outSts", "DH");//待还款
            } else {
                ordermap.put("outSts", "OD");//逾期
            }
            ordermap.put("apprvAmt", sybj);
            order_rep.add(ordermap);
        }
        body.put("orders", order_rep);
        dateAppOrderPerson.put("body", body);
        return dateAppOrderPerson;
    }

    //查询已提交贷款申请列表
    @Override
    public Map<String, Object> queryApplLoanInfo(String token, String channelNo, String channel, Map<String, Object> map) {
        logger.info("*********查询已提交贷款申请列表**************开始");
        int page;
        int size;
        //参数非空判断
        if (token.isEmpty()) {
            logger.info("token为空");
            return fail(ConstUtil.ERROR_CODE, "参数token为空!");
        }
        if (channel.isEmpty()) {
            logger.info("channel为空");
            return fail(ConstUtil.ERROR_CODE, "参数channel为空!");
        }
        if (channelNo.isEmpty()) {
            logger.info("channelNo为空");
            return fail(ConstUtil.ERROR_CODE, "参数channelNo为空!");
        }
        if (map.get("page") == null || "".equals(map.get("page"))) {
            logger.info("pwd为空");
            return fail(ConstUtil.ERROR_CODE, "参数pwd为空!");
        }
        if (map.get("size") == null || "".equals(map.get("size"))) {
            logger.info("size为空");
            return fail(ConstUtil.ERROR_CODE, "参数size为空!");
        }
        if (map.get("outSts") == null || "".equals(map.get("outSts"))) {
            logger.info("outSts为空");
            return fail(ConstUtil.ERROR_CODE, "参数outSts为空!");
        }
        //缓存数据获取
        Map<String, Object> cacheMap = RedisUtils.getExpireMap(token);
        if (MapUtils.isEmpty(cacheMap)) {
            logger.info("Redis数据获取失败");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        //总入口需查询客户信息数据
        String idNo = (String) cacheMap.get("idCard");
//        String idNo = "37040319910722561X";
//        idNo = (String) map.get("idNo");
        if (idNo == null || "".equals(idNo)) {
            logger.info("idCard为空");
            return fail(ConstUtil.ERROR_CODE, "idCard为空!");
        }
        String outSts = (String) map.get("outSts");
        page = (Integer) map.get("page");
        size = (Integer) map.get("size");
        Map req = new HashMap<String, Object>();
        req.put("channelNo", channelNo);
        req.put("channel", channel);
        req.put("idNo", idNo);
        req.put("page", String.valueOf(page));
        req.put("pageSize", String.valueOf(size));
        req.put("outSts", outSts);
        req.put("applyDate", "");
        logger.info("查询已提交贷款申请列表接口，请求数据：" + req.toString());
        Map<String, Object> dateAppOrderPerson = appServerService.queryApplListPerson(token, req);
        if (dateAppOrderPerson == null) {
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        }
        Map resultmapjsonMap = (Map<String, Object>) dateAppOrderPerson.get("head");
        String resultmapFlag = (String) resultmapjsonMap.get("retFlag");
        if (!"00000".equals(resultmapFlag)) {
            String retMsg = (String) resultmapjsonMap.get("retMsg");
            return fail(ConstUtil.ERROR_CODE, retMsg);
        }
        List<Map<String, Object>> body = (List<Map<String, Object>>) dateAppOrderPerson.get("body");
        Map<String, Object> orders = new HashMap<>();
        orders.put("orders", body);
        dateAppOrderPerson.put("body", orders);
        return dateAppOrderPerson;
    }

    //查询订单详情
    @Override
    public Map<String, Object> queryOrderInfo(String token, String channelNo, String channel, Map<String, Object> map) {
        //参数非空判断
        if (token.isEmpty()) {
            logger.info("token为空");
            return fail(ConstUtil.ERROR_CODE, "参数token为空!");
        }
        if (channel.isEmpty()) {
            logger.info("channel为空");
            return fail(ConstUtil.ERROR_CODE, "参数channel为空!");
        }
        if (channelNo.isEmpty()) {
            logger.info("channelNo为空");
            return fail(ConstUtil.ERROR_CODE, "参数channelNo为空!");
        }
        if (!map.containsKey("orderNo")) {
            logger.info("订单号orderNo为空");
            return fail(ConstUtil.ERROR_CODE, "参数orderNo不存在!");
        }
        String orderNo = (String) map.get("orderNo");
        if (StringUtils.isEmpty(orderNo)) {
            logger.info("订单号orderNo为空");
            return fail(ConstUtil.ERROR_CODE, "参数orderNo为空!");
        }
        Map req = new HashMap<String, Object>();
        req.put("channelNo", channelNo);
        req.put("channel", channel);
        req.put("orderNo", orderNo);
        logger.info("查询订单详情接口，请求数据：" + req.toString());
        AppOrdernoTypgrpRelation AppOrdernoTypgrpRelation = appOrdernoTypgrpRelationDao.selectByOrderNo(orderNo);
        if (AppOrdernoTypgrpRelation == null) {
            logger.info("没有获取到订单信息");
            return fail(ConstUtil.ERROR_CODE, "没有获取到订单信息");
        }
        //获取申请流水号
        String applseq = AppOrdernoTypgrpRelation.getApplSeq();
        Map<String, Object> queryOrderInfo = acquirerService.getOrderFromAcquirer(applseq, channel, channelNo, null, null, "2");
        if (queryOrderInfo == null) {
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        }
        Map<String, Object> resultHeadMap = (Map<String, Object>) queryOrderInfo.get("head");
        String retFlag = (String) resultHeadMap.get("retFlag");
        if (!"00000".equals(retFlag)) {
            String retMsg = (String) resultHeadMap.get("retMsg");
            return fail(ConstUtil.ERROR_CODE, retMsg);
        }
        BigDecimal xfze;
        String ordertotal;
        Map<String, Object> resMap = (Map<String, Object>) queryOrderInfo.get("body");
        String applyTnrTyp = (String) resMap.get("apply_tnr_typ");
        logger.info("--日志输出：");
        BigDecimal totalnormint = new BigDecimal(resMap.get("totalnormint").toString());
        BigDecimal totalfeeamt = new BigDecimal(resMap.get("totalfeeamt").toString());
        BigDecimal Totalnormint;
        BigDecimal Totalfeeamt;
        Totalnormint = totalnormint;
        Totalfeeamt = totalfeeamt;
        BigDecimal xfzeBig;
        xfzeBig = Totalnormint.add(Totalfeeamt);

        BigDecimal applyAmt;
        if (StringUtils.isNotEmpty(applyTnrTyp) && !applyTnrTyp.equals("D") && !applyTnrTyp.equals("d")) {
            String xfzeStr = String.valueOf(xfzeBig);//息费总额
            if (xfzeStr.equals("null")) {
                xfze = new BigDecimal(0);
            } else {
                xfze = new BigDecimal(xfzeStr);
            }
            applyAmt = new BigDecimal(resMap.get("apply_amt").toString());
            BigDecimal total;
            total = xfze.add(applyAmt);
            ordertotal = total.divide(new BigDecimal(1), 2, BigDecimal.ROUND_HALF_UP) + "";
            resMap.put("ordertotal", ordertotal);
            resMap.put("xfze", xfzeStr);
        } else if (StringUtils.isNotEmpty(applyTnrTyp) && ("D".equals(applyTnrTyp) || "d".equals(applyTnrTyp))) {
            String xfzeStr = String.valueOf(xfzeBig);//息费总额
            if (xfzeStr.equals("null")) {
                xfze = new BigDecimal(0);
            } else {
                xfze = new BigDecimal(xfzeStr);
            }
            applyAmt = new BigDecimal(resMap.get("apply_amt").toString());
            BigDecimal total;
            total = xfze.add(applyAmt);
            ordertotal = total + "";
            resMap.put("ordertotal", ordertotal);
            resMap.put("xfze", xfzeStr);
        }
        return success(resMap);
    }


    @Override
    public Map<String, Object> orderQueryXjd(String token, String channelNo, String channel, Map<String, Object> params) {
        Map<String, Object> resultMap = new HashMap<>();
        Map<String, Object> stringObjectMap = queryOrderInfo(token, channelNo, channel, params);
        logger.info("stringObjectMap:" + stringObjectMap);
        if (!HttpUtil.isSuccess(stringObjectMap)) {
            return stringObjectMap;
        }
        Map<String, Object> resMap = (Map<String, Object>) stringObjectMap.get("body");
        Double apply_amt = Convert.toDouble(resMap.get("apply_amt"));//借款金额
        String apply_tnr = Convert.toString(resMap.get("apply_tnr"));//申请期限
        String apply_tnr_typ = Convert.toString(resMap.get("apply_tnr_typ"));//期限类型
        String typ_cde = Convert.toString(resMap.get("typ_cde"));//贷款品种
        String mtd_cde = Convert.toString(resMap.get("mtd_cde"));//还款方式
        Map<String, Object> map = new HashMap<>();
        map.put("typCde", typ_cde);
        map.put("apprvAmt", apply_amt);
        map.put("applyTnrTyp", apply_tnr_typ);
        map.put("applyTnr", apply_tnr);
//        //TODO   后期改动
//        if ("01".equals(mtd_cde)) {
//            mtd_cde = "M0002";
//        } else if ("13".equals(mtd_cde)) {
//            mtd_cde = "M0001";
//        }
        map.put("mtdCde", mtd_cde);
        Map<String, Object> paySs = custExtInfoService.getPaySs(token, channel, channelNo, map);
        Map<String, Object> head = (Map) paySs.get("head");
        if (!"00000".equals(head.get("retFlag"))) {
            logger.info("还款试算,错误信息：" + head.get("retMsg"));
            return fail(ConstUtil.ERROR_CODE, (String) head.get("retMsg"));
        }
        Map<String, Object> bodyMap = (Map) paySs.get("body");
        IResponse<Map> loanTypeAndBankInfo = custExtInfoService.getLoanTypeAndBankInfo(token, channel, channelNo);
        Map loanTypeAndBankInfoMap = loanTypeAndBankInfo.getBody();
        resultMap.put("orderInfoMap", resMap);
        resultMap.put("paySsMap", bodyMap);
        resultMap.put("typCdeMap", loanTypeAndBankInfoMap);
        logger.info("================resMap:" + resMap);
        return success(resultMap);
    }

}
