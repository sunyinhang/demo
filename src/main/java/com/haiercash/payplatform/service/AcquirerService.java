package com.haiercash.payplatform.service;

import java.util.Map;

import com.haiercash.payplatform.common.data.AppOrder;
import com.haiercash.payplatform.common.data.AppOrderGoods;

/**
 * acquirer service.
 * @author Liu qingxiang
 * @since v1.0.0
 */
public interface AcquirerService {

        /**
     * 映射商品map为商品对象.	
     * @param goodMap           商品属性map
     * @param appOrderGoods     商品对象
     * @return AppOrderGoods
     */
    AppOrderGoods acquirerGoodsMap2OrderGood(Map<String, Object> goodMap, AppOrderGoods appOrderGoods);

    /**
     * 将收单系统返回的Map数据转换为AppOrder对象.
     * @param acquirer      收单数据
     * @param order         订单信息
     * @return
     */
    AppOrder acquirerMap2OrderObject(Map<String, Object> acquirer, AppOrder order);


}
