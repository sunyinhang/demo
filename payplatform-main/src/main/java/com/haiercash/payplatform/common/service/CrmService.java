package com.haiercash.payplatform.common.service;

import java.util.Map;

/**
 * crm service interface.
 * @author  Liu qingxiang
 * @since v1.0.1
 */
public interface CrmService {

    /**
     * 根据用户id获取实名信息.
     * @param userId 用户id
     * @return Map
     */
    Map<String, Object>  queryPerCustInfoByUserId(String userId);

    /**
     * 查询用户准入资格
     *
     * @param params custName certNo
     * @return
     */
    Map<String, Object> getCustIsPass(Map<String, Object> params);

    /**
     * 获取销售代表
     *
     * @param userId
     * @return
     */
    Map<String, Object> getStoreSaleByUserId(String userId);

    /**
     * 整合crm13接口  查询实名认证客户信息
     *
     * @param custName
     * @param certNo
     * @return
     */
    Map<String, Object> queryMerchCustInfo(String custName, String certNo);
}
