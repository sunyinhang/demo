package com.haiercash.appserver.service;

import com.haiercash.common.data.AppOrder;
import com.haiercash.common.data.AppOrderGoods;
import com.haiercash.common.data.AppOrdernoTypgrpRelation;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * acquirer service interface.
 * @author Liu qingxiang
 * @since v2.0.0.
 */
@Service
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

    /**
     * 将订单商品信息放入收单系统请求map中.
     * @param order         订单信息
     * @param acquirer      收单请求map
     * @return
     */
    Map<String, Object> putGoodIntoMap(AppOrder order, Map<String, Object> acquirer);


    /**
     * 将申请人信息放入收单系统请求map中.
     * @param order         订单信息
     * @param acquirer      收单请求map
     * @return
     */
    Map<String, Object> putApplyListIntoMap(AppOrder order, Map<String, Object> acquirer);

    /**
     * 现金贷校验默认值.
     * @param map
     * @return
     */
    Map<String, Object> cashLoanCheckDefaultValue(AppOrder appOrder, Map<String, Object> map);

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
    AppOrder getAppOrderFromAcquirer(String applSeq, String channelNo);

    /**
     * 从收单查询贷款详情
     * @param applSeq       流水号
     * @param channelNo     渠道号
     * @return Map
     */
    Map<String, Object> getApplInfFromAcquirer(String applSeq, String channelNo);

    /**
     * 贷款申请提交.
     * @param order     订单详情
     * @param flag      0:贷款取消  1:贷款提交
     * @return Map
     */
    Map<String, Object> commitAppl(AppOrder order, String flag);

    /**
     * 贷款申请取消.
     * @param order 订单详情
     * @return Map
     */
    Map<String, Object> cancelAppl(AppOrder order);

    /**
     * 退回订单给商户.
     * @param applSeq 流水号
     * @param reason  原因
     * @return
     */
    Map<String, Object> backOrderToCust(String applSeq, String reason);

    /**
     * 贷款审批进度查询
     * @param applSeq   流水号
     * @return Map
     */
    Map<String, Object> getApprovalProcess(String applSeq);


}
