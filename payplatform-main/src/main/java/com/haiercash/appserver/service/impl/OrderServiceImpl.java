package com.haiercash.appserver.service.impl;

import com.haiercash.appserver.gm.service.GmService;
import com.haiercash.appserver.service.AppManageService;
import com.haiercash.appserver.service.AppOrderService;
import com.haiercash.appserver.service.OrderService;
import com.haiercash.appserver.util.enums.OrderEnum;
import com.haiercash.common.apporder.utils.FormatUtil;
import com.haiercash.common.config.EurekaServer;
import com.haiercash.common.data.AppOrder;
import com.haiercash.common.data.AppOrdernoTypgrpRelation;
import com.haiercash.common.data.AppOrdernoTypgrpRelationRepository;
import com.haiercash.common.service.BaseService;
import com.haiercash.commons.util.HttpUtil;
import com.haiercash.commons.util.RestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * order service impl.
 *
 * @author Liu qingxiang
 * @since v2.0.0
 */
@Service
public class OrderServiceImpl extends BaseService implements OrderService {

    @Autowired
    private AppManageService appManageService;

    @Autowired
    private GmService gmService;
    @Autowired
    private AppOrderService appOrderService;
    @Autowired
    private AppOrdernoTypgrpRelationRepository appOrdernoTypgrpRelationRepository;

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
                if (!StringUtils.isEmpty(key)) {
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
                .of("applSeq", "typGrp", "purpose", "typCde", "fstPay", "applyTnr", "applyTnrTyp", "totalNormInt", "totalFeeAmt", "applAcTyp",
                        "applCardNo", "repayApplCardNo", "appInAdvice");
        FormatUtil.moveEntryBetweenMap(applInfStream, map, childMap);
        Map<String, Object> oldMap = (Map<String, Object>) map.get("appl_inf");
        if (oldMap == null) {
            oldMap = new HashMap<>();
        }
        oldMap.putAll(childMap);
        map.put("appl_inf", oldMap);

        childMap.clear();
        Stream<String> goodsInfStream = Stream.of("goodsCode", "goodsNum", "goodsPrice", "goodsName", "goodsModel", "brandName", "kindName");
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
                AppOrdernoTypgrpRelation relation = appOrdernoTypgrpRelationRepository.findOne(appOrder.getOrderNo());
                if (relation != null) {
                    channel = relation.getChannel();
                }
            }
        }
        orderMap.put("sysNo", channel);
        if (StringUtils.isEmpty(appOrder.getChannelNo())) {
            if (StringUtils.isEmpty(super.getChannelNo())) {
                Map<String, Object> channelNoMap = appOrderService.getSysFlagAndChannelNo(appOrder);
                orderMap.put("channelNo", channelNoMap.get("channelNo"));
            } else {
                orderMap.put("channelNo", super.getChannelNo());
            }
        }

        String goodCode = null;
        if (!StringUtils.isEmpty(appOrder.getGoodsCode())) {
            goodCode = appOrder.getGoodsCode();
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
        }else {
            // 默认需要发货确认
            orderMap.put("isNeedSend", "01");
        }
        String url = EurekaServer.ORDER + "/api/order/save";
        orderMap.entrySet().removeIf(entry -> entry.getValue() == null);
        this.checkOrderDefaultValue(appOrder, orderMap);
        logger.info("==> ORDER save :" + FormatUtil.toJson(orderMap));
        Map<String, Object> returnMap = HttpUtil.restPostMap(url, orderMap);
        logger.info("<== ORDER save :" + FormatUtil.toJson(returnMap));
        if (returnMap == null || returnMap.isEmpty()) {
            return fail(RestUtil.ERROR_INTERNAL_CODE, "订单系统通信失败");
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
        logger.info("formId:" + formId + ",type:" + type + ",goodCode:" + goodCode);
        if ("1".equals(type) && goodCode == null) {
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
        if (StringUtils.isEmpty(type)) {
            params.put("type", "1");
        }else if ("N".equals(isConfirm)) {
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
        Map<String, Object> result = HttpUtil.restPostMap(EurekaServer.ORDER + "/api/order/cancel", params);
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
            return fail(RestUtil.ERROR_INTERNAL_CODE, "订单系统系统通信失败");
        }
        if (!HttpUtil.isSuccess(goodsMap)) {
            logger.info("订单系统获取商品列表失败, formId:" + formId);
        }
        return goodsMap;
    }

    @Override
    public Map<String, Object> findSgLogisticsInfoByFormId(String formId) {
        Map<String, Object> result = HttpUtil
                .restGetMap(EurekaServer.ORDER + "/api/order/sg/findSgLogisticsInfoByFormId?formId=" + formId);
        if (!HttpUtil.isSuccess(result)) {
            logger.info("获取订单物流信息失败, formId:" + formId);
        }
        return result;
    }

    @Override
    public Map<String, Object> backOrderToCust(String applSeq, String reason) {
        Map<String, Object> params = new HashMap<>();
        params.put("applSeq", applSeq);
        params.put("failReason", reason);
        Map<String, Object> result = HttpUtil
                .restPostMap(EurekaServer.ORDER + "/api/order/returnApplByMerchant", params);
        logger.info("订单退回给商户结果：" + result);
        return result;
    }

    @Override
    public void checkOrderDefaultValue(AppOrder appOrder, Map<String, Object> orderMap) {
        if (appOrder == null || orderMap == null || orderMap.isEmpty()) {
            return;
        }
        Map<String, Object> applInfMap = (Map<String, Object>) orderMap.get("appl_inf");
        // 如果为随借随换，借款期限上传为最大值.
        if ("D".equalsIgnoreCase(appOrder.getApplyTnrTyp())) {
            String url = EurekaServer.CMISPROXY + "/api/pLoanTyp/find?typCde="
                    + appOrder.getTypCde();
            String json = HttpUtil.restGet(url, null);
            logger.info("获取还款详情:" + json);
            if (StringUtils.isEmpty(json)) {
                logger.info("CMIS==》贷款品种详情接口查询结果异常！随借随还借款期限上传原值！ ");
            } else {
                Map<String, Object> typResultMap = HttpUtil.json2DeepMap(json);// 贷款品种resultMap,利用此map封装贷款相关的一些数据
                applInfMap.put("applyTnr", typResultMap.get("tnrMaxDays").toString());
            }
        }
        // 个人版、美凯龙、大数据、美分期固定为：SALE、消费
        if ("2".equals(appOrder.getSource()) || "16".equals(appOrder.getSource())
                || "34".equals(appOrder.getChannelNo()) || "35".equals(appOrder.getChannelNo())) {
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
        if ("02".equals(appOrder.getTypGrp()) && StringUtils.isEmpty(appOrder.getCooprCde()) && (!Objects
                .equals("3", appOrder.getSource()))) {
            Map<String, Object> saleMsg = appManageService.getSaleMsg(appOrder.getTypCde());
            if (saleMsg == null) {
                saleMsg = appManageService.getSaleMsg("default");
            }
            orderMap.put("salerCde", saleMsg.get("saler_cde"));
            orderMap.put("cooprCde", saleMsg.get("coopr_cde"));
        } else {
            // 星巢贷订单，saler_name为crm查询得到的门店名称
            if ("16".equals(appOrder.getSource())) {
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
        if ("2".equals(appOrder.getSource()) || "16".equals(appOrder.getSource())) {
            // 个人版同销售代表
            orderMap.put("crtUsr", orderMap.get("salerCde"));
        }

        // 证件类型
        if (!StringUtils.isEmpty(appOrder.getIdNo()) && StringUtils.isEmpty(appOrder.getIdTyp())) {
            orderMap.put("idTyp", "20");
        }
    }

}
