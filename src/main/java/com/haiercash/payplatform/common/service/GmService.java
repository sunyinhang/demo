package com.haiercash.payplatform.common.service;

import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * gm service.
 * @author Liu qingxiang
 * @since v2.0.0
 */
public interface GmService {

    /**
     *  根据商品编号获取商品详情.
     * @param goodsCode 商品编号
     * @return Map
     */
    Map<String, Object> getGoodsByCode(String goodsCode);

    /**
     * 获取是否需要发货处理以及是否需要商户确认.
     * @param goodsCode 商品编码
     * @return
     */
    Map<String, Object> getIsNeedSendAndIsConfirm(String goodsCode);
}
