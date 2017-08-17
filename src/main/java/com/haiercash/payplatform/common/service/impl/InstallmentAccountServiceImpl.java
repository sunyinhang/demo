package com.haiercash.payplatform.common.service.impl;

import com.haiercash.commons.redis.Cache;
import com.haiercash.payplatform.common.service.AppServerService;
import com.haiercash.payplatform.common.service.InstallmentAccountService;
import com.haiercash.payplatform.common.utils.ConstUtil;
import com.haiercash.payplatform.service.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ljy on 2017/8/17.
 */
@Service
public class InstallmentAccountServiceImpl extends BaseService implements InstallmentAccountService {
    @Autowired
    private Cache cache;
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
        Map<String, Object> cacheMap = cache.get(token);
//        if(cacheMap.isEmpty()){
//            logger.info("Redis数据获取失败");
//            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
//        }
        //TODO 总入口需查询客户信息数据
//        String crtUsr = (String)cacheMap.get("userId");
//        String idNo = (String) cacheMap.get("idNo");//身份证号
        String crtUsr = "15264826872";
        String idNo = "37040319910722561X";
        if(crtUsr == null || "".equals(crtUsr)){
            logger.info("crtUsr为空");
            return fail(ConstUtil.ERROR_CODE, "UserID为空!");
        }
        if(idNo == null || "".equals(idNo)){
            logger.info("idNo为空");
            return fail(ConstUtil.ERROR_CODE, "idNo为空!");
        }
        page = Integer.parseInt((String) map.get("page"));
        size = Integer.parseInt((String) map.get("size"));
        Map req = new HashMap<String,Object>();
        req.put("channelNo", channelNo);
        req.put("channel", channel);
        req.put("crtUsr", crtUsr);
        req.put("idNo", idNo);
        req.put("page", page);
        req.put("size", size);
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
        Map<String, Object> cacheMap = cache.get(token);
//        if(cacheMap.isEmpty()){
//            logger.info("Redis数据获取失败");
//            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
//        }
        //TODO 总入口需查询客户信息数据
        //        String custNo = (String)cacheMap.get("custNo");
        String custNo = "C201708090109301402340";
        custNo = (String) map.get("custNo");
        if(custNo == null || "".equals(custNo)){
            logger.info("custNo为空");
            return fail(ConstUtil.ERROR_CODE, "custNo为空!");
        }
        page = Integer.parseInt((String) map.get("page"));
        size = Integer.parseInt((String) map.get("size"));
        Map req = new HashMap<String,Object>();
        req.put("channelNo", channelNo);
        req.put("channel", channel);
        req.put("custNo", custNo);
        req.put("page", page);
        req.put("size", size);
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
        Map<String, Object> cacheMap = cache.get(token);
//        if(cacheMap.isEmpty()){
//            logger.info("Redis数据获取失败");
//            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
//        }
        //TODO 总入口需查询客户信息数据
        //        String idNo = (String)cacheMap.get("idNo");
        String idNo = "37040319910722561X";
        idNo = (String) map.get("idNo");
        if(idNo == null || "".equals(idNo)){
            logger.info("idNo为空");
            return fail(ConstUtil.ERROR_CODE, "idNo为空!");
        }
        page = Integer.parseInt((String) map.get("page"));
        size = Integer.parseInt((String) map.get("size"));
        Map req = new HashMap<String,Object>();
        req.put("channelNo", channelNo);
        req.put("channel", channel);
        req.put("idNo", idNo);
        req.put("page", page);
        req.put("size", size);
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
        Map<String, Object> cacheMap = cache.get(token);
//        if(cacheMap.isEmpty()){
//            logger.info("Redis数据获取失败");
//            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
//        }
        //TODO 总入口需查询客户信息数据
        //        String idNo = (String)cacheMap.get("idNo");
        String idNo = "37040319910722561X";
        idNo = (String) map.get("idNo");
        if(idNo == null || "".equals(idNo)){
            logger.info("idNo为空");
            return fail(ConstUtil.ERROR_CODE, "idNo为空!");
        }
        String outSts = (String) map.get("outSts");
        page = Integer.parseInt((String) map.get("page"));
        size = Integer.parseInt((String) map.get("size"));
        Map req = new HashMap<String,Object>();
        req.put("channelNo", channelNo);
        req.put("channel", channel);
        req.put("idNo", idNo);
        req.put("page", page);
        req.put("size", size);
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
        Map<String, Object> cacheMap = cache.get(token);
//        if(cacheMap.isEmpty()){
//            logger.info("Redis数据获取失败");
//            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
//        }
        Map req = new HashMap<String,Object>();
        req.put("channelNo", channelNo);
        req.put("channel", channel);
        req.put("orderNo", map.get("orderNo"));
        logger.info("删除订单接口，请求数据："+req.toString());
        Map<String, Object> dateAppOrderPerson = appServerService.deleteAppOrder(token, req);
        return dateAppOrderPerson;
    }
}
