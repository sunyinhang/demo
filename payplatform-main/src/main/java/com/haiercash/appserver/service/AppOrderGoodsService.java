package com.haiercash.appserver.service;

import com.haiercash.common.data.AppOrderGoods;

import java.util.Map;

/**
 * 订单商品service接口
 * Created by zhouwushuang on 2017.04.18.
 */
public interface AppOrderGoodsService {
    /**
     * 新增订单商品
     * @param appOrderGoods
     * @return
     */
    Map<String, Object> saveAppOrderGoods(AppOrderGoods appOrderGoods);

    /**
     * 更新订单商品
     * @param appOrderGoods
     * @return
     */
    Map<String, Object> updateAppOrderGoods(AppOrderGoods appOrderGoods);

    /**
     * 查询订单商品
     * @param orderNo
     * @return
     */
    Map<String, Object> getAppOrderGoodsByOrderNo(String orderNo);

    /**
     * 删除订单商品
     * @param orderNo   订单号
     * @param seqNo     订单商品编号
     * @param goodsCode 商品编号
     * @return
     */
    Map<String, Object> deleteAppOrderGoodsByKeys(String orderNo, String seqNo, String goodsCode);
}
