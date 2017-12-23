package com.haiercash.payplatform.service.impl;

import com.haiercash.core.serialization.JsonSerializer;
import com.haiercash.payplatform.common.dao.AppOrdernoTypgrpRelationDao;
import com.haiercash.payplatform.common.data.AppOrder;
import com.haiercash.payplatform.common.data.AppOrderGoods;
import com.haiercash.payplatform.common.data.AppOrdernoTypgrpRelation;
import com.haiercash.payplatform.common.enums.OrderEnum;
import com.haiercash.payplatform.service.AppManageService;
import com.haiercash.payplatform.service.CmisService;
import com.haiercash.payplatform.service.CrmService;
import com.haiercash.payplatform.service.GmService;
import com.haiercash.payplatform.service.OrderService;
import com.haiercash.payplatform.utils.ChannelType;
import com.haiercash.payplatform.utils.FormatUtil;
import com.haiercash.spring.config.EurekaServer;
import com.haiercash.spring.service.BaseService;
import com.haiercash.spring.utils.ConstUtil;
import com.haiercash.spring.utils.HttpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * order service impl.
 * @author Liu qingxiang
 * @since v1.0.0
 */
@Service
public class OrderServiceImpl extends BaseService implements OrderService {

    @Autowired
    private GmService gmService;
    @Autowired
    private AppManageService appManageService;
    @Autowired
    private CrmService crmService;
    @Autowired
    private AppOrdernoTypgrpRelationDao appOrdernoTypgrpRelationDao;
    @Autowired
    private CmisService cmisService;

    @Override
    public Map<String, Object> order2OrderMap(AppOrder order, Map<String, Object> map) {
        if (order == null) {
            return null;
        }
        if (map == null) {
            map = new HashMap<>();
        }
        try {
            Class clazz = order.getClass();
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                PropertyDescriptor pd = new PropertyDescriptor(field.getName(), clazz);
                Method method = pd.getReadMethod();
                Object value = method.invoke(order);
                List<String> key = OrderEnum.getOrderAttrs(field.getName());
                if (!CollectionUtils.isEmpty(key)) {
                    if (value != null) {
                        Map<String, Object> finalMap = map;
                        key.forEach(singleKey -> finalMap.put(singleKey, value));
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

        Map<String, Object> childMap = new HashMap<>();
        Stream<String> applInfStream = Stream
                .of("applSeq", "typGrp", "purpose", "typCde", "fstPay", "applyTnr", "applyTnrTyp", "totalNormInt",
                        "totalFeeAmt", "applAcTyp",
                        "applCardNo", "repayApplCardNo", "appInAdvice");
        FormatUtil.moveEntryBetweenMap(applInfStream, map, childMap);
        Map<String, Object> oldMap = (Map<String, Object>) map.get("appl_inf");
        if (oldMap == null) {
            oldMap = new HashMap<>();
        }
        oldMap.putAll(childMap);
        oldMap.put("creditType", "02");
        map.put("appl_inf", oldMap);

        childMap.clear();
        Stream<String> goodsInfStream = Stream
                .of("goodsCode", "goodsNum", "goodsPrice", "goodsName", "goodsModel", "brandName", "kindName");
        FormatUtil.moveEntryBetweenMap(goodsInfStream, map, childMap);
        oldMap = (Map<String, Object>) map.get("goods_inf");
        if (oldMap == null) {
            oldMap = new HashMap<>();
        }
        oldMap.putAll(childMap);
        map.put("goods_inf", oldMap);

        childMap.clear();
        Stream<String> addrInfStream = Stream
                .of("deliverTyp", "adProvince", "adCity", "adArea", "adAddr", "adName", "adPhone", "adType");
        FormatUtil.moveEntryBetweenMap(addrInfStream, map, childMap);
        oldMap = (Map<String, Object>) map.get("addr_inf");
        if (oldMap == null) {
            oldMap = new HashMap<>();
        }
        oldMap.putAll(childMap);
        map.put("addr_inf", childMap);

        // version固定传1
        map.put("version", "1");

        return map;
    }

    @Override
    public Map<String, Object> saveOrUpdateAppOrder(AppOrder appOrder, Map<String, Object> src) {
        // 商品贷，走订单系统
        if (StringUtils.isEmpty(appOrder.getFormType()) && StringUtils.isEmpty(appOrder.getOrderNo())) {
            // 新订单处理默认值
            appOrder.setFormType("10"); //默认线下
        }
        Map<String, Object> orderMap = this.order2OrderMap(appOrder, src);
        String channel = null;
        if ("2".equals(appOrder.getSource())) {
            channel = "14";
        } else if ("1".equals(appOrder.getSource())) {
            channel = "13";
        } else if (StringUtils.isEmpty(appOrder.getSource())) {
            if (StringUtils.isEmpty(appOrder.getOrderNo())) {
                // 新订单
                channel = super.getChannel();
            } else {
                AppOrdernoTypgrpRelation relation = appOrdernoTypgrpRelationDao.selectByOrderNo(appOrder.getOrderNo());
                if (relation != null) {
                    channel = relation.getChannel();
                }
            }
        }
        orderMap.put("sysNo", channel);
        if (StringUtils.isEmpty(appOrder.getChannelNo())) {
            if (StringUtils.isEmpty(super.getChannelNo())) {
                Map<String, Object> channelNoMap = this.getSysFlagAndChannelNo(appOrder);
                orderMap.put("channelNo", channelNoMap.get("channelNo"));
            } else {
                orderMap.put("channelNo", super.getChannelNo());
            }
        }

        // 多商品处理
        String goodCode = null;
        List<AppOrderGoods> appOrderGoodsList = appOrder.getAppOrderGoodsList();
        boolean isGoodsList = (appOrderGoodsList != null && appOrderGoodsList.size() > 0) ? true : false;
        logger.info("是否是多个商品：" + isGoodsList);
        if (!StringUtils.isEmpty(appOrder.getGoodsCode()) || isGoodsList) {
            goodCode = appOrder.getGoodsCode();
            //多商品 同属于同一门店，发货状态相同
            if (isGoodsList) {//多商品
                goodCode = appOrderGoodsList.get(0).getGoodsCode();
                orderMap.remove("goods_inf");
            }
        } else if (!StringUtils.isEmpty(appOrder.getOrderNo())) {
            // 获取商品详情，校验是否需要商户校验
            Map<String, Object> goodList = this.getGoodsList(appOrder.getOrderNo());
            if (!HttpUtil.isSuccess(goodList)) {
                logger.info("订单系统获取商品(formId:" + appOrder.getOrderNo() + ")信息失败, 返回结果:" + goodList);
                return fail("05", "获取商品信息失败");
            }
            Map<String, Object> body = (Map<String, Object>) goodList.get("body");
            List<Map<String, Object>> orderGoodsMapList = (List<Map<String, Object>>) body.get("orderGoodsMapList");
            if (orderGoodsMapList.size() > 0) {
                goodCode = (String) orderGoodsMapList.get(0).get("goodsCode");
            } else {
                return fail("05", "获取商品信息失败");
            }
        }

        if (!StringUtils.isEmpty(goodCode)) {
            Map<String, Object> needAndConfirm = gmService.getIsNeedSendAndIsConfirm(goodCode);
            if (!HttpUtil.isSuccess(needAndConfirm)) {
                return needAndConfirm;
            }
            // 是否需要发货处理.
            Map<String, Object> needAndConfirmBody = (Map<String, Object>) needAndConfirm.get("body");
            String isNeedSend = needAndConfirmBody.get("isNeedSend").toString();
            if ("Y".equals(isNeedSend)) {
                orderMap.put("isNeedSend", "01");
            } else {
                orderMap.put("isNeedSend", "00");
            }
        } else {
            // 默认需要发货确认
            orderMap.put("isNeedSend", "01");
        }

        if ("46".equals(super.getChannelNo())) {
            orderMap.put("isNeedSend", "00");
        }
        orderMap.put("sysNo", "11");
        String url;
        if ("46".equals(getChannelNo())) {
            if (isGoodsList) {//多商品
                url = EurekaServer.ORDER + "/api/order/saveOrderEK";
            } else {
                url = EurekaServer.ORDER + "/api/order/saveEK";
            }
        } else {
            if (isGoodsList) {//多商品
                url = EurekaServer.ORDER + "/api/order/saveOrder";
            } else {
                url = EurekaServer.ORDER + "/api/order/save";
            }
        }
        orderMap.entrySet().removeIf(entry -> entry.getValue() == null);
        logger.info("前appOrder：" + JsonSerializer.serialize(appOrder));
        this.checkOrderDefaultValue(appOrder, orderMap);
        logger.info("后orderMap：" + JsonSerializer.serialize(orderMap));
        logger.info("==> ORDER save :" + FormatUtil.toJson(orderMap));
        Map<String, Object> returnMap = HttpUtil.restPostMap(url, orderMap);
        logger.info("<== ORDER save :" + FormatUtil.toJson(returnMap));
        if (returnMap == null || returnMap.isEmpty()) {
            return fail(ConstUtil.ERROR_CODE, "订单系统通信失败");
        }
        if (HttpUtil.isSuccess(returnMap)) {
            String orderNo = (String) ((Map<String, Object>) returnMap.get("body")).get("formId");
            String applSeq = (String) ((Map<String, Object>) returnMap.get("body")).get("applSeq");

            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("orderNo", orderNo);
            responseMap.put("applSeq", applSeq);
            return success(responseMap);
        } else {
            return returnMap;
        }
    }


    @Override
    public Map<String, Object> submitOrder(String formId, String type) {
        Map<String, Object> params = new HashMap<>();
        params.put("formId", formId);

        // 获取商品详情，校验是否需要商户校验
        Map<String, Object> goodList = this.getGoodsList(formId);
        if (!HttpUtil.isSuccess(goodList)) {
            logger.info("订单系统获取商品(formId:" + formId + ")信息失败, 返回结果:" + goodList);
            return fail("05", "获取商品信息失败");
        }
        Map<String, Object> body = (Map<String, Object>) goodList.get("body");
        String goodCode;
        List<Map<String, Object>> orderGoodsMapList = (List<Map<String, Object>>) body.get("orderGoodsMapList");
        if (orderGoodsMapList.size() > 0) {
            goodCode = (String) orderGoodsMapList.get(0).get("goodsCode");
        } else {
            return fail("05", "获取商品信息失败");
        }
        // 商品编号为空时，默认提交给商户
        if (goodCode == null) {
            type = "1";
        }
        String isConfirm = "Y";
        if (goodCode != null) {
            Map<String, Object> needAndConfirm = gmService.getIsNeedSendAndIsConfirm(goodCode);
            if (!HttpUtil.isSuccess(needAndConfirm)) {
                return needAndConfirm;
            }
            Map<String, Object> needAndConfirmBody = (Map<String, Object>) needAndConfirm.get("body");
            isConfirm = needAndConfirmBody.get("isConfirm").toString();
        }
        // 默认提交商户.
        if ("46".equals(super.getChannelNo())){
            // 顺逛白条不需要商户确认
            params.put("type", "0");
        } else if (StringUtils.isEmpty(type)) {
            params.put("type", "1");
        } else if ("N".equals(isConfirm)) {
            params.put("type", "0");
        } else {
            params.put("type", type);
        }
        Map<String, Object> result = HttpUtil.restPostMap(EurekaServer.ORDER + "/api/order/submit", params);
        return result;
    }


    @Override
    public Map<String, Object> cancelOrder(String formId) {
        Map<String, Object> params = new HashMap<>();
        params.put("formId", formId);
        Map<String, Object> result = HttpUtil.restGetMap(EurekaServer.ORDER + "/api/order/cancel?formId="+formId);
        return result;
    }

    @Override
    public Map<String, Object> getGoodsList(String formId) {
        String url = EurekaServer.ORDER + "/api/order/goods/list?formId=" + formId;
        logger.info("==> ORDER 获取商品列表：" + url);
        Map<String, Object> goodsMap = HttpUtil
                .restGetMap(url);
        logger.info("<== ORDER 获取商品列表：" + goodsMap);
        if (StringUtils.isEmpty(goodsMap)) {
            return fail(ConstUtil.ERROR_CODE, "订单系统系统通信失败");
        }
        if (!HttpUtil.isSuccess(goodsMap)) {
            logger.info("订单系统获取商品列表失败, formId:" + formId);
        }
        return goodsMap;
    }

    @Override
    public void checkOrderDefaultValue(AppOrder appOrder, Map<String, Object> orderMap) {
        if (appOrder == null || orderMap == null || orderMap.isEmpty()) {
            return;
        }
        Map<String, Object> applInfMap = (Map<String, Object>) orderMap.get("appl_inf");
        // 如果为随借随换，借款期限上传为最大值.
        if ("D".equalsIgnoreCase(appOrder.getApplyTnrTyp())) {
            Map<String, Object> typCdeMap = cmisService.findPLoanTyp(appOrder.getTypCde());
            if (HttpUtil.isSuccess(typCdeMap)) {
                Map<String, Object> typResultMap = (Map<String, Object>) typCdeMap.get("body");
                applInfMap.put("applyTnr", typResultMap.get("tnrMaxDays").toString());
            }
        }
        ChannelType channelType = ChannelType.forName(null, appOrder.getChannelNo(), appOrder.getSource());
        // 个人版、美凯龙、大数据、美分期固定为：SALE、消费
        if ( channelType == ChannelType.BigData
                || channelType == ChannelType.LoveByStage
                || channelType == ChannelType.Shunguang) {
            applInfMap.put("purpose", "SALE");
        }
        // 用户选择其他用途时，自动修改为：SALE、消费
        if ("OTH".equals(appOrder.getPurpose())) {
            applInfMap.put("purpose", "SALE");
        }
        // 受托支付不传放款账号信息
        if ("01".equals(appOrder.getTypGrp())) {
            applInfMap.put("applAcTyp", "");
            applInfMap.put("applCardNo", "");
        }
        /** 如果贷款类型（typGre）为01（耐用消费品），传从数据库取的值， 如果为02（现金贷）传固定值 **/
        /** 部分特殊类型的现金贷，APP会上传门店等信息，不传固定值 **/
        if ("02".equals(appOrder.getTypGrp()) && StringUtils.isEmpty(appOrder.getCooprCde()) && channelType != ChannelType.OtherPayPlat) {
            Map<String, Object> saleMsg = appManageService.getSaleMsg(appOrder.getTypCde());
            if (saleMsg == null) {
                saleMsg = appManageService.getSaleMsg("default");
            }
            orderMap.put("salerCde", saleMsg.get("saler_cde"));
            orderMap.put("cooprCde", saleMsg.get("coopr_cde"));
        } else {
            // 星巢贷订单，saler_name为crm查询得到的门店名称
            if (channelType == ChannelType.Micron) {
                Map<String, Object> saleMsg = appManageService.getSaleMsg("xcd");
                if (saleMsg == null) {
                    saleMsg = appManageService.getSaleMsg("default");
                }
                orderMap.put("salerCde", saleMsg.get("saler_cde"));
                orderMap.put("cooprCde", saleMsg.get("coopr_cde"));
            }
            orderMap.put("salerCde", appOrder.getCrtUsr());
        }

        // 登记人员编码
        if (channelType == ChannelType.Personal || channelType == ChannelType.Micron) {
            // 个人版同销售代表
            orderMap.put("crtUsr", orderMap.get("salerCde"));
        }

        // 证件类型
        if (!StringUtils.isEmpty(appOrder.getIdNo()) && StringUtils.isEmpty(appOrder.getIdTyp())) {
            appOrder.setIdTyp("20");
        }

        // 顺逛白条月均收入默认5000
        if (channelType == ChannelType.Shunguang) {
            orderMap.put("IndivMthInc", "5000");
        }
    }

    public Map<String, Object> getSysFlagAndChannelNo(AppOrder appOrder) {
        Map<String, Object> crmParam = new HashMap<>();
        crmParam.put("custName", appOrder.getCustName());
        crmParam.put("idNo", appOrder.getIdNo());
        Map<String, Object> custIsPass = crmService.getCustIsPass(crmParam);
        if (custIsPass == null || custIsPass.isEmpty()) {
            return fail(ConstUtil.ERROR_CODE, "CRM 通信失败");
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
        //够花
        if ("42".equals(appOrder.getChannelNo())) {
            result.put("sysFlag", "18");
            result.put("channelNo", "42");
        }
        //顺逛Ｈ５
        if ("46".equals(appOrder.getChannelNo())) {
            result.put("sysFlag", "11");
            result.put("channelNo", "46");
        }
        // 如果为空，设置默认值》
        if (StringUtils.isEmpty(result.get("sysFlag"))) {
            result.put("sysFlag", "04");
            result.put("channelNo", "05");
        }

        return result;
    }


}

