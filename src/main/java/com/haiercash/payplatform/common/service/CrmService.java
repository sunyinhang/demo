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
}
