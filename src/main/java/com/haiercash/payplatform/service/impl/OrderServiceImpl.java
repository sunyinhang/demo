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

}

