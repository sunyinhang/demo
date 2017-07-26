package com.haiercash.payplatform.service.impl;

import com.haiercash.payplatform.common.enums.AcquirerApptEnum;
import com.haiercash.payplatform.common.enums.AcquirerEnum;
import com.haiercash.payplatform.common.enums.AcquirerGoodsEnum;
import com.haiercash.payplatform.common.utils.FormatUtil;
import com.haiercash.payplatform.common.utils.ReflactUtils;
import com.haiercash.payplatform.data.AppOrder;
import com.haiercash.payplatform.data.AppOrderGoods;
import com.haiercash.payplatform.service.AcquirerService;
import com.haiercash.payplatform.service.BaseService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * acquirer service impl.
 * @author Liu qingxiang
 * @since v1.0.0
 */
@Service
public class AcquirerServiceImpl extends BaseService implements AcquirerService {


    // 映射贷款信息商品信息至apporder对象
    private AppOrder acquirerGoodMap2OrderObject(Map<String, Object> acquirer, AppOrder order) {
        if (StringUtils.isEmpty(acquirer.get("goodsList"))) {
            logger.info("收单系统贷款详情无商品信息：" + acquirer.get("applSeq"));
            return order;
        } else {
            Map<String, Object> goodsList = (Map<String, Object>) acquirer.get("goodsList");
            if (goodsList == null || goodsList.get("good") == null) {
                return order;
            }
            List<AppOrderGoods> appOrderGoodsList = new ArrayList<>();
            List<Map<String, Object>> good = (List<Map<String, Object>>) goodsList.get("good");
            if (good.size() > 0) {
                for (Map<String, Object> map : good) {
                    map.keySet().removeIf((key) -> AcquirerGoodsEnum.getOrderAttr(key) == null);
                    AppOrderGoods appOrderGoods = new AppOrderGoods();
                    map.forEach(
                            (key, value) -> ReflactUtils.setProperty(appOrderGoods, AcquirerGoodsEnum.getOrderAttr(key),
                                    FormatUtil.checkValueType(AcquirerGoodsEnum.getOrderAttr(key), value,
                                            AppOrderGoods.class)));
                    appOrderGoodsList.add(appOrderGoods);
                }
            }
            order.setAppOrderGoodsList(appOrderGoodsList);
        }

        return order;
    }

    // 映射贷款信息申请人信息至apporder对象
    private AppOrder acquirerApptMap2OrderObject(Map<String, Object> acquirer, AppOrder order) {
        if (StringUtils.isEmpty(acquirer.get("apptList"))) {
            logger.info("收单系统贷款详情无申请人信息:" + acquirer.get("applSeq"));
            return order;
        } else {
            Map<String, Object> apptList = (Map<String, Object>) acquirer.get("apptList");
            if (apptList == null || apptList.get("appt") == null) {
                return order;
            }
            List<Map<String, Object>> appt = (List<Map<String, Object>>) apptList.get("appt");
            if (apptList.size() > 0) {
                for (Map<String, Object> map : appt) {
                    if ("01".equals(map.get("appt_typ"))) {
                        map.keySet().removeIf((key) -> AcquirerApptEnum.getOrderAttr(key) == null);
                        map.forEach(
                                (key, value) -> ReflactUtils.setProperty(order, AcquirerApptEnum.getOrderAttr(key),
                                        FormatUtil.checkValueType(AcquirerApptEnum.getOrderAttr(key), value,
                                                AppOrder.class)));
                        break;
                    }
                }
            }

            return order;
        }
    }

    public AppOrderGoods acquirerGoodsMap2OrderGood(Map<String, Object> goodMap, AppOrderGoods appOrderGoods) {
        goodMap.keySet().removeIf((key) -> AcquirerGoodsEnum.getOrderAttr(key) == null);
        goodMap.forEach((key, value) -> ReflactUtils.setProperty(appOrderGoods, AcquirerGoodsEnum.getOrderAttr(key),
                FormatUtil.checkValueType(AcquirerGoodsEnum.getOrderAttr(key), value, AppOrderGoods.class)));
        return appOrderGoods;
    }

    @Override
    public AppOrder acquirerMap2OrderObject(Map<String, Object> acquirer, AppOrder order) {
        // 映射主申请人收获地址等信息
        this.acquirerApptMap2OrderObject(acquirer, order);
        // 映射商品信息
        this.acquirerGoodMap2OrderObject(acquirer, order);
        acquirer.keySet().removeIf((key) -> AcquirerEnum.getOrderAttr(key) == null);
        acquirer.forEach((key, value) -> ReflactUtils.setProperty(order, AcquirerEnum.getOrderAttr(key),
                FormatUtil.checkValueType(AcquirerEnum.getOrderAttr(key), value, AppOrder.class)));
        logger.info("收单映射appOrder结果：" + order);
        return order;
    }

}
