package com.haiercash.payplatform.common.service.impl;

import com.haiercash.commons.redis.Session;
import com.haiercash.payplatform.common.service.AppServerService;
import com.haiercash.payplatform.common.service.InstallmentAccountService;
import com.haiercash.payplatform.common.utils.ConstUtil;
import com.haiercash.payplatform.service.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ljy on 2017/8/17.
 */
@Service
public class InstallmentAccountServiceImpl extends BaseService implements InstallmentAccountService {
    @Autowired
    private Session session;
    @Autowired
    private AppServerService appServerService;
    //模块编码  02
    private static String MODULE_NO = "05";
    public InstallmentAccountServiceImpl() {
        super(MODULE_NO);
    }

    @Override
    public Map<String, Object> queryAllLoanInfo(String token, String channelNo, String channel, Map<String, Object> map) {
        logger.info("*********查询全部贷款信息列表**************开始");
        int page = 0;
        int size = 0;
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
        Map<String, Object> cacheMap = session.get(token, Map.class);
        if(cacheMap.isEmpty()){
            logger.info("Redis数据获取失败");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        //总入口需查询客户信息数据
        String crtUsr = (String)cacheMap.get("userId");
        String idNo = (String) cacheMap.get("idCard");//身份证号
//        String crtUsr = "15264826872";
//        String idNo = "37040319910722561X";
        if(crtUsr == null || "".equals(crtUsr)){
            logger.info("crtUsr为空");
            return fail(ConstUtil.ERROR_CODE, "UserID为空!");
        }
        if(idNo == null || "".equals(idNo)){
            logger.info("idNo为空");
            return fail(ConstUtil.ERROR_CODE, "idNo为空!");
        }
        page = (Integer) map.get("page");
        size = (Integer) map.get("size");
        Map req = new HashMap<String,Object>();
        req.put("channelNo", channelNo);
        req.put("channel", channel);
        req.put("crtUsr", crtUsr);
        req.put("idNo", idNo);
        req.put("page", String.valueOf(page));
        req.put("size", String.valueOf(size));
        logger.info("查询全部贷款信息列表接口，请求数据："+req.toString());
        Map<String, Object> dateAppOrderPerson = appServerService.getDateAppOrderPerson(token, req);
        if(dateAppOrderPerson == null){
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        }
        Map resultmapjsonMap = (HashMap<String, Object>) dateAppOrderPerson.get("head");
        String resultmapFlag = (String) resultmapjsonMap.get("retFlag");
        if(!"00000".equals(resultmapFlag)){
            String retMsg = (String) resultmapjsonMap.get("retMsg");
            return fail(ConstUtil.ERROR_CODE, retMsg);
        }
//        Map resultmapbodyMap = (HashMap<String, Object>) dateAppOrderPerson.get("body");
//        List<Map> ordersList = (List) resultmapbodyMap.get("orders");
        return dateAppOrderPerson;
    }

    @Override
    public Map<String, Object> QueryPendingLoanInfo(String token, String channelNo, String channel, Map<String, Object> map) {
        logger.info("*********查询待提交订单列表**************开始");
        int page = 0;
        int size = 0;
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
        Map<String, Object> cacheMap = session.get(token,Map.class);
        if(cacheMap.isEmpty()){
            logger.info("Redis数据获取失败");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        //总入口需查询客户信息数据
        String custNo = (String)cacheMap.get("custNo");
//        String custNo = "C201708090109301402340";
//        custNo = (String) map.get("custNo");
        if(custNo == null || "".equals(custNo)){
            logger.info("custNo为空");
            return fail(ConstUtil.ERROR_CODE, "custNo为空!");
        }
        page = (Integer) map.get("page");
        size = (Integer) map.get("size");
        Map req = new HashMap<String,Object>();
        req.put("channelNo", channelNo);
        req.put("channel", channel);
        req.put("custNo", custNo);
        req.put("page", String.valueOf(page));
        req.put("size", String.valueOf(size));
        logger.info("查询待提交订单列表接口，请求数据："+req.toString());
        Map<String, Object> dateAppOrderPerson = appServerService.getWtjAppOrderCust(token, req);
        if(dateAppOrderPerson == null){
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        }
        Map resultmapjsonMap = (HashMap<String, Object>) dateAppOrderPerson.get("head");
        String resultmapFlag = (String) resultmapjsonMap.get("retFlag");
        if(!"00000".equals(resultmapFlag)){
            String retMsg = (String) resultmapjsonMap.get("retMsg");
            return fail(ConstUtil.ERROR_CODE, retMsg);
        }
        return dateAppOrderPerson;
    }

    //待还款信息查询(全部)
    @Override
    public Map<String, Object> queryPendingRepaymentInfo(String token, String channelNo, String channel, Map<String, Object> map) {
        logger.info("*********待还款信息查询(全部)**************开始");
        int page = 0;
        int size = 0;
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
        Map<String, Object> cacheMap = session.get(token,Map.class);
        if(cacheMap.isEmpty()){
            logger.info("Redis数据获取失败");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        //总入口需查询客户信息数据
        String idNo = (String)cacheMap.get("idNo");
//        String idNo = "37040319910722561X";
//        idNo = (String) map.get("idNo");
        if(idNo == null || "".equals(idNo)){
            logger.info("idNo为空");
            return fail(ConstUtil.ERROR_CODE, "idNo为空!");
        }
        page = (Integer) map.get("page");
        size = (Integer) map.get("size");
        Map req = new HashMap<String,Object>();
        req.put("channelNo", channelNo);
        req.put("channel", channel);
        req.put("idNo", idNo);
        req.put("page", String.valueOf(page));
        req.put("size", String.valueOf(size));
        req.put("flag", "A");
        logger.info("待还款信息查询(全部)接口，请求数据："+req.toString());
        Map<String, Object> dateAppOrderPerson = appServerService.queryApplAllByIdNo(token, req);
        if(dateAppOrderPerson == null){
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        }
        Map resultmapjsonMap = (HashMap<String, Object>) dateAppOrderPerson.get("head");
        String resultmapFlag = (String) resultmapjsonMap.get("retFlag");
        if(!"00000".equals(resultmapFlag)){
            String retMsg = (String) resultmapjsonMap.get("retMsg");
            return fail(ConstUtil.ERROR_CODE, retMsg);
        }
        return dateAppOrderPerson;
    }

    //查询已提交贷款申请列表
    @Override
    public Map<String, Object> queryApplLoanInfo(String token, String channelNo, String channel, Map<String, Object> map) {
        logger.info("*********查询已提交贷款申请列表**************开始");
        int page = 0;
        int size = 0;
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
        Map<String, Object> cacheMap = session.get(token,Map.class);
        if(cacheMap.isEmpty()){
            logger.info("Redis数据获取失败");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        //总入口需查询客户信息数据
        String idNo = (String)cacheMap.get("idNo");
//        String idNo = "37040319910722561X";
//        idNo = (String) map.get("idNo");
        if(idNo == null || "".equals(idNo)){
            logger.info("idNo为空");
            return fail(ConstUtil.ERROR_CODE, "idNo为空!");
        }
        String outSts = (String) map.get("outSts");
        page = (Integer) map.get("page");
        size = (Integer) map.get("size");
        Map req = new HashMap<String,Object>();
        req.put("channelNo", channelNo);
        req.put("channel", channel);
        req.put("idNo", idNo);
        req.put("page", String.valueOf(page));
        req.put("pageSize", String.valueOf(size));
        req.put("outSts", outSts);
        req.put("applyDate", "");
        logger.info("查询已提交贷款申请列表接口，请求数据："+req.toString());
        Map<String, Object> dateAppOrderPerson = appServerService.queryApplListPerson(token, req);
        if(dateAppOrderPerson == null){
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        }
        Map resultmapjsonMap = (HashMap<String, Object>) dateAppOrderPerson.get("head");
        String resultmapFlag = (String) resultmapjsonMap.get("retFlag");
        if(!"00000".equals(resultmapFlag)){
            String retMsg = (String) resultmapjsonMap.get("retMsg");
            return fail(ConstUtil.ERROR_CODE, retMsg);
        }
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
        if(!map.containsKey("orderNo")){
            logger.info("订单号orderNo为空");
            return fail(ConstUtil.ERROR_CODE, "参数orderNo不存在!");
        }
        String orderNo = (String) map.get("orderNo");
        if(StringUtils.isEmpty(orderNo)){
            logger.info("订单号orderNo为空");
            return fail(ConstUtil.ERROR_CODE, "参数orderNo为空!");
        }
        Map req = new HashMap<String,Object>();
        req.put("channelNo", channelNo);
        req.put("channel", channel);
        req.put("orderNo", orderNo);
        logger.info("查询订单详情接口，请求数据："+req.toString());
        Map<String, Object> queryOrderInfo = appServerService.queryOrderInfo(token, req);
        if(queryOrderInfo == null){
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        }
        Map<String, Object> resultHeadMap = (Map<String, Object>) queryOrderInfo.get("head");
        String retFlag = (String) resultHeadMap.get("retFlag");
        if(!"00000".equals(retFlag)){
            String retMsg = (String) resultHeadMap.get("retMsg");
            return fail(ConstUtil.ERROR_CODE, retMsg);
        }
        BigDecimal xfze = new BigDecimal(0) ;
        BigDecimal applyAmt = new BigDecimal(0) ;
        String ordertotal = "";
        Map<String, Object> resMap = (Map<String, Object>) queryOrderInfo.get("body");
        String applyTnrTyp = (String) resMap.get("applyTnrTyp");
        if(!applyTnrTyp.equals("D") && !applyTnrTyp.equals("d") &&( applyTnrTyp != null && !"".equals(applyTnrTyp))){
            String xfzeStr = String.valueOf(resMap.get("xfze"));//息费总额
            if (xfzeStr.equals("null")){
                xfze = new BigDecimal(0);
            }else{
                xfze = new BigDecimal(xfzeStr);
            }
            String applyAmtStr = String.valueOf(resMap.get("applyAmt"));//借款总额
            if (applyAmtStr.equals("null")){
                applyAmt = new BigDecimal(0);
            }else{
                applyAmt = new BigDecimal(applyAmtStr);
            }
            BigDecimal total = new BigDecimal(0);
            total = xfze.add(applyAmt);
            ordertotal =  total.divide(new BigDecimal(1) , 2,BigDecimal.ROUND_HALF_UP) + "";
            resMap.put("ordertotal", ordertotal);
        }
        return success(resMap);
    }


    //删除订单
    @Override
    public Map<String, Object> deleteOrderInfo(String token, String channelNo, String channel, Map<String, Object> map) {
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
        if (map.get("orderNo") == null || "".equals(map.get("orderNo"))) {
            logger.info("orderNo为空");
            return fail(ConstUtil.ERROR_CODE, "参数orderNo为空!");
        }
        //缓存数据获取
        Map<String, Object> cacheMap = session.get(token, Map.class);
        if(cacheMap.isEmpty()){
            logger.info("Redis数据获取失败");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        Map req = new HashMap<String,Object>();
        req.put("channelNo", channelNo);
        req.put("channel", channel);
        req.put("orderNo", map.get("orderNo"));
        logger.info("删除订单接口，请求数据："+req.toString());
        Map<String, Object> dateAppOrderPerson = appServerService.deleteAppOrder(token, req);
        return dateAppOrderPerson;
    }
}
