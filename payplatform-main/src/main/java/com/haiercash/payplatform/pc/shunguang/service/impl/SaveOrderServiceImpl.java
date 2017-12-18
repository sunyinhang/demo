package com.haiercash.payplatform.pc.shunguang.service.impl;

import com.haiercash.core.lang.BeanUtils;
import com.haiercash.payplatform.common.data.AppOrder;
import com.haiercash.payplatform.config.ShunguangConfig;
import com.haiercash.payplatform.pc.shunguang.service.SaveOrderService;
import com.haiercash.payplatform.pc.shunguang.service.SgInnerService;
import com.haiercash.payplatform.service.AppServerService;
import com.haiercash.payplatform.service.CommonPageService;
import com.haiercash.payplatform.service.CrmManageService;
import com.haiercash.spring.redis.RedisUtils;
import com.haiercash.spring.service.BaseService;
import com.haiercash.spring.utils.ConstUtil;
import com.haiercash.spring.utils.HttpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by use on 2017/8/16.
 */
@Service
public class SaveOrderServiceImpl extends BaseService implements SaveOrderService {
    @Autowired
    private AppServerService appServerService;
    @Autowired
    private SgInnerService sgInnerService;
    @Autowired
    private CrmManageService crmManageService;
    @Autowired
    private CommonPageService commonPageService;
    @Autowired
    private ShunguangConfig shunguangConfig;

    @Override
    public Map<String, Object> saveOrder(Map<String, Object> map) {
        logger.info("订单保存****************开始");
        //前端传入参数获取(放开)
        String token = (String) map.get("token");
        String channel = (String) map.get("channel");
        String channelNo = (String) map.get("channelNo");
        String applyTnr = (String) map.get("applyTnr");//借款期限
        String applyTnrTyp = (String) map.get("applyTnrTyp");//期限类型（若天则传D）
        String updflag = (String) map.get("flag");//1.待提交返显
        String orderNo = (String) map.get("orderNo");//待提交时必传
//        String areaCode = (String) map.get("areaCode");//区编码
        String typCde = (String) map.get("typCde");//贷款品种编码
        String province = (String) map.get("province");//省名称
        String city = (String) map.get("city");//市名称
        String district = (String) map.get("district");//区名称
        //非空判断
        if (StringUtils.isEmpty(token) || StringUtils.isEmpty(channel) || StringUtils.isEmpty(channelNo)
                || StringUtils.isEmpty(applyTnr) || StringUtils.isEmpty(applyTnrTyp) || StringUtils.isEmpty(typCde)) {
            logger.info("token:" + token + "  channel:" + channel + "   channelNo:" + channelNo
                    + "   applyTnr:" + applyTnr + "   applyTnrTyp" + applyTnrTyp
                    + "   updflag:" + updflag + "  orderNo:" + orderNo + "   typCde:" + typCde);
            logger.info("前台获取数据有误");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        }

        //appOrder缓存获取（放开）
        Map<String, Object> cacheMap = RedisUtils.getExpireMap(token);
        if (cacheMap == null || "".equals(cacheMap)) {
            logger.info("Jedis数据获取失败");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }


        Map<String, Object> appOrderMap = (Map<String, Object>) cacheMap.get("apporder");
        if (appOrderMap == null) {
            logger.info("登录超时");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        AppOrder appOrder = BeanUtils.mapToBean(appOrderMap, AppOrder.class);
        appOrder.setTypCde(typCde);//贷款品种编码
        logger.info("appOrder1" + appOrder);

        //根据token获取统一认证userid
        String userId = sgInnerService.getuserId(token);
        if (StringUtils.isEmpty(userId)) {
            logger.info("根据用户中心token获取统一认证userId失败");
            return fail(ConstUtil.ERROR_CODE, "获取内部注册信息失败");
        }

        //获取客户信息
        logger.info("订单保存，根据userId获取客户信息");
        Map<String, Object> custMap = new HashMap<String, Object>();
        custMap.put("userId", userId);
        custMap.put("channel", channel);
        custMap.put("channelNo", channelNo);
        Map<String, Object> custInforesult = appServerService.queryPerCustInfo(token, custMap);
        if (!HttpUtil.isSuccess(custInforesult)) {
            return fail(ConstUtil.ERROR_CODE, "获取实名信息失败");
        }
        String payresultstr = com.alibaba.fastjson.JSONObject.toJSONString(custInforesult);
        com.alibaba.fastjson.JSONObject custresult = com.alibaba.fastjson.JSONObject.parseObject(payresultstr).getJSONObject("body");
        String custName = (String) custresult.get("custName");
        String custNo = (String) custresult.get("custNo");
        logger.info("客户编号：" + custNo + "   客户姓名：" + custName);
        String certNo = (String) custresult.get("certNo");
        String mobile = (String) custresult.get("mobile");

        //获取客户标签
        logger.info("获取标签");
        Map tagmap = new HashMap<>();
        tagmap.put("custName", custName);//姓名
        tagmap.put("idTyp", "20");//证件类型
        tagmap.put("idNo", certNo);//证件号码
        Map tagmapresult = crmManageService.getCustTag("", tagmap);
        if (!HttpUtil.isSuccess(tagmapresult)) {
            return tagmapresult;
        }
        String userType = (String) cacheMap.get("userType");
        //String userType = "01";
        String tagId = "";
        if ("01".equals(userType)) {//微店主
            tagId = shunguangConfig.getShopKeeper();
        }
        if ("02".equals(userType)) {//消费者
            tagId = shunguangConfig.getConsumer();
        }
        //
        Boolean b = false;
        List<Map<String, Object>> body = (List<Map<String, Object>>) tagmapresult.get("body");
        for (int i = 0; i < body.size(); i++) {
            Map<String, Object> m = body.get(i);
            String tagid = m.get("tagId").toString();
            if (tagid.equals(tagId)) {
                b = true;
            }
        }
        //若不存在进行添加  /app/crm/cust/setCustTag
        if (!b) {
            logger.info("打标签");
            Map addtagmap = new HashMap<>();
            addtagmap.put("certNo", certNo);//身份证号
            addtagmap.put("tagId", tagId);//自定义标签ID
            Map addtagmapresult = crmManageService.setCustTag("", addtagmap);
            if (!HttpUtil.isSuccess(addtagmapresult)) {
                return addtagmapresult;
            }
        }


        //获取订单金额  总利息 金额
        logger.info("订单保存，获取订单金额，总利息金额");
        //applyTnrTyp = applyTnr;
        Map<String, Object> payMap = new HashMap<String, Object>();
        payMap.put("typCde", appOrder.getTypCde());
        payMap.put("apprvAmt", appOrder.getApplyAmt());
        payMap.put("applyTnrTyp", applyTnrTyp);
        payMap.put("applyTnr", applyTnr);
        payMap.put("channel", channel);
        payMap.put("channelNo", channelNo);
        Map<String, Object> payresultMap = appServerService.getPaySs(token, payMap);
        if (!HttpUtil.isSuccess(payresultMap)) {//还款试算
            String retmsg = (String) ((Map<String, Object>) (payresultMap.get("head"))).get("retMsg");
            return fail(ConstUtil.ERROR_CODE, retmsg);
        }
        String payresult = com.alibaba.fastjson.JSONObject.toJSONString(payresultMap);
        com.alibaba.fastjson.JSONObject payBody = com.alibaba.fastjson.JSONObject.parseObject(payresult).getJSONObject("body");
        logger.info("payBody:" + payBody);
        String totalAmt = payBody.get("totalAmt").toString();
        String totalNormInt = payBody.get("totalNormInt").toString();//订单保存（totalNormInt）
        String totalFeeAmt = payBody.get("totalFeeAmt").toString();//订单保存总利息金额（totalAmt）

        //获取订单信息
        logger.info("订单保存，获取订单信息");
        appOrder.setVersion("1");//接口版本号  固定传’1’
        appOrder.setSource("11");//订单来源
        appOrder.setChannelNo((String) map.get("channelNo"));//渠道编号
        appOrder.setApplyTnr(applyTnr);//借款期限
        appOrder.setApplyTnrTyp(applyTnrTyp);//借款期限类型
        appOrder.setTotalnormint(totalNormInt);//总利息金额
        appOrder.setTotalfeeamt(totalFeeAmt);//费用总额
        appOrder.setMerchNo(shunguangConfig.getMerchNo());//商户编号
        appOrder.setCooprCde(shunguangConfig.getStoreNo());//门店编号
        appOrder.setCrtUsr(shunguangConfig.getUserId());//销售代表用户ID（）
        appOrder.setTypGrp("01");//贷款类型  01:商品贷  02  现金贷
        appOrder.setSource(ConstUtil.SOURCE);//订单来源
        appOrder.setWhiteType("SHH");//白名单类型
        appOrder.setFormType("10");//10:线下订单   20:线上订单
        appOrder.setCustNo(custNo);//客户编号
        appOrder.setCustName(custName);//客户姓名
        appOrder.setIdTyp("20");//证件类型
        appOrder.setIdNo(certNo);//客户证件号码
        appOrder.setUserId(userId);//录单用户ID
        appOrder.setChannelNo(channelNo);
        //appOrder.setFstPay("0");//首付金额
        String updatemallflag = (String) cacheMap.get("updatemallflag");
        if ("1".equals(updflag) || "1".equals(updatemallflag)) {//待提交订单
            if ("1".equals(updflag)) {
                if (StringUtils.isEmpty(orderNo)) {
                    logger.info("前台传入参数有误");
                    return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
                }
                appOrder.setOrderNo(orderNo);
            } else {
                logger.info("退回及待提交进行订单保存");
                orderNo = (String) cacheMap.get("updatemalloderNo");
                appOrder.setOrderNo(orderNo);
            }
        }
        logger.info("订单信息：" + appOrder);

        //0.准入资格校验
        logger.info("进行准入资格校验");
        Map<String, Object> ispassmap = new HashMap<String, Object>();
        ispassmap.put("custName", custName);//姓名
        ispassmap.put("certNo", certNo);//身份证
        ispassmap.put("phonenumber", mobile);//手机号
        ispassmap.put("userId", userId);//登录用户名
        ispassmap.put("channel", channel);
        ispassmap.put("channelNo", channelNo);
        Map<String, Object> ispassresult = appServerService.getCustIsPass(token, ispassmap);
        if (!HttpUtil.isSuccess(ispassresult)) {//准入资格校验失败
            String retmsg = (String) ((Map<String, Object>) (ispassresult.get("head"))).get("retMsg");
            return fail(ConstUtil.ERROR_CODE, retmsg);
        }
        String isPass = (String) ((Map<String, Object>) (ispassresult.get("body"))).get("isPass");
        if ("-1".equals(isPass)) {
            return fail(ConstUtil.ERROR_CODE, "没有准入资格");
        }

        //1.录单校验（所在城市开通服务）
        //获取市代码
        String cityCode = "";
        String provinceCode = "";
        String areaType = "";
        if (StringUtils.isEmpty(province) && StringUtils.isEmpty(city)) {
            cityCode = "990100";
            provinceCode = "990000";
        } else {
            logger.info("获取业务发生地省市");
            Map<String, Object> areaCode = commonPageService.getAreaCode(province, city, district);
            if (!HttpUtil.isSuccess(areaCode)) {
                return areaCode;
            }
            logger.info("获取业务发生地省市areaCode------" + areaCode);
            Map<String, Object> areaCodeBody = (Map<String, Object>) areaCode.get("body");
            provinceCode = (String) areaCodeBody.get("province");
            cityCode = (String) areaCodeBody.get("city");
        }
        //录单校验
        logger.info("进行录单校验");
        Map<String, Object> ordercheakmap = new HashMap<String, Object>();
        ordercheakmap.put("userId", userId);
        ordercheakmap.put("provinceCode", provinceCode);
        ordercheakmap.put("cityCode", cityCode);
        ordercheakmap.put("channel", channel);
        ordercheakmap.put("channelNo", channelNo);
        Map<String, Object> ordercheakresult = appServerService.getCustInfoAndEdInfoPerson(token, ordercheakmap);
        if (!HttpUtil.isSuccess(ordercheakresult)) {//录单校验失败
            String retmsg = (String) ((Map<String, Object>) (ordercheakresult.get("head"))).get("retMsg");
            return fail(ConstUtil.ERROR_CODE, retmsg);
        }

        //2.是否允许申请贷款
        logger.info("查看是否允许申请贷款");
        //String typCde = appOrder.getTypCde();
        SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd");
        String date = dateFormater.format(new Date());
        Map<String, Object> queryordermap = new HashMap<String, Object>();
        queryordermap.put("typCde", typCde);
        queryordermap.put("date", date);
        queryordermap.put("channel", channel);
        queryordermap.put("channelNo", channelNo);
        Map<String, Object> queryorderresult = appServerService.queryBeyondContral(token, queryordermap);
        if (!HttpUtil.isSuccess(queryorderresult)) {//是否允许申请贷款失败
            String retmsg = (String) ((Map<String, Object>) (queryorderresult.get("head"))).get("retMsg");
            return fail(ConstUtil.ERROR_CODE, retmsg);
        }
        String flag = (String) ((Map<String, Object>) (queryorderresult.get("body"))).get("flag");
        if (!"Y".equals(flag)) {
            return fail(ConstUtil.ERROR_CODE, "不允许申请贷款");
        }

        //3.订单保存
        logger.info("appOrder2:" + appOrder);
        Map<String, Object> ordermap = commonPageService.saveAppOrderInfo(appOrder);
        cacheMap.put("ordermap", ordermap);
        cacheMap.put("custName", custName);
        cacheMap.put("custNo", custNo);
        cacheMap.put("certNo", certNo);
        cacheMap.put("apporder", appOrder);
        logger.info("贷款品种编码为：" + appOrder.getTypCde());
        RedisUtils.setExpire(token, cacheMap);
        logger.info("订单保存结果：" + ordermap.toString());
        if (!HttpUtil.isSuccess(ordermap)) {//订单保存失败
            return ordermap;
        }

        return ordermap;
    }

}
