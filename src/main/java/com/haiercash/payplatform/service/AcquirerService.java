package com.haiercash.payplatform.service;

import java.util.Map;

import com.haiercash.payplatform.common.data.AppOrder;
import com.haiercash.payplatform.common.data.AppOrderGoods;
import com.haiercash.payplatform.common.data.AppOrdernoTypgrpRelation;

/**
 * acquirer service.
 *
 * @author Liu qingxiang
 * @since v1.0.0
 */
public interface AcquirerService {

    /**
     * 根据订单信息从收单系统获取贷款详情.
     *
     * @param applSeq   流水号
     * @param channel   系统标识
     * @param channelNo 渠道号
     * @param cooprCde  合作方编码
     * @param tradeType 交易类型
     * @param flag      1:普通格式  2:渠道进件核心格式
     * @return Map<String, Object>
     */
    Map<String, Object> getOrderFromAcquirer(String applSeq, String channel, String channelNo, String cooprCde,
            String tradeType, String flag);

    /**
     * 映射商品map为商品对象.
     *
     * @param goodMap       商品属性map
     * @param appOrderGoods 商品对象
     * @return AppOrderGoods
     */
    AppOrderGoods acquirerGoodsMap2OrderGood(Map<String, Object> goodMap, AppOrderGoods appOrderGoods);

    /**
     * 将收单系统返回的Map数据转换为AppOrder对象.
     *
     * @param acquirer 收单数据
     * @param order    订单信息
     * @return
     */
    AppOrder acquirerMap2OrderObject(Map<String, Object> acquirer, AppOrder order);

    /**
     * 将订单中数据影射为收单系统渠道进件格式map.
     * 不包括list(联系人、申请人信息、商品信息)相关数据.
     *
     * @param order 订单信息
     * @param map   映射结果
     * @return Map
     */
    Map<String, Object> order2AcquirerMap(AppOrder order, Map<String, Object> map);

    /**
     * 如果为现金贷，则根据appOrder中的applSeql获取贷款详情，并向收单系统发起渠道进件.
     *
     * @param order 订单信息
     * @return Map
     */
    Map<String, Object> cashLoan(AppOrder order, AppOrdernoTypgrpRelation relation);

    /**
     * 去收单查apporder
     * @param applSeq       流水号
     * @param channelNo     渠道号
     * @return  Map
     */
    public AppOrder getAppOrderFromAcquirer(String applSeq, String channelNo);

    /**
     * 贷款申请取消.
     * @param applSeq 贷款申请流水号
     * @return Map
     */
    Map<String, Object> cancelAppl(String applSeq);
}
