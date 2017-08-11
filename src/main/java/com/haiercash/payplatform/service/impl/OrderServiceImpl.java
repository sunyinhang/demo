package com.haiercash.payplatform.service.impl;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.haiercash.payplatform.common.config.EurekaServer;
import com.haiercash.payplatform.common.service.GmService;
import com.haiercash.payplatform.common.utils.HttpUtil;
import com.haiercash.payplatform.common.utils.RestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.haiercash.payplatform.common.data.AppOrder;
import com.haiercash.payplatform.common.enums.OrderEnum;
import com.haiercash.payplatform.common.utils.FormatUtil;
import com.haiercash.payplatform.service.BaseService;
import com.haiercash.payplatform.service.OrderService;

/**
 * order service impl.
 * @author Liu qingxiang
 * @since v1.0.0
 */
@Service
public class OrderServiceImpl extends BaseService implements OrderService {

    @Autowired
    private GmService gmService;

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
                .of("applSeq", "typGrp", "purpose", "typCde", "fstPay", "applyTnr", "applyTnrTyp", "totalNormInt",
                        "totalFeeAmt", "applAcTyp",
                        "applCardNo", "repayApplCardNo", "appInAdvice");
        FormatUtil.moveEntryBetweenMap(applInfStream, map, childMap);
        Map<String, Object> oldMap = (Map<String, Object>) map.get("appl_inf");
        if (oldMap == null) {
            oldMap = new HashMap<>();
        }
        oldMap.putAll(childMap);
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
        if (StringUtils.isEmpty(type)) {
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

}

