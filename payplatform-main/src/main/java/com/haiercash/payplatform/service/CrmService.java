package com.haiercash.payplatform.service;

import com.haiercash.spring.rest.IResponse;

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

    /**
     * (GET)登录验证
     *
     * @param userId
     * @param password
     * @return
     */
    IResponse<Map> validateUsers(String userId, String password);

    /**
     * (GET)查询指定客户的所有银行卡（根据客户编号）列表
     *
     * @param custNo
     * @return
     */
    Map<String, Object>  getBankCard(String custNo);

    /**
     * 查询白名单列表
     *
     * @param params custName certNo
     * @return
     */
    Map<String, Object> getCustWhiteListCmis(Map<String, Object> params);
    /**
     * @Title queryApplReraidPlanByloanNo
     * @Description: 108、(GET)还款计划查询 (crm)
     * @author yu jianwei
     * @date 2017/11/6 13:41
     */
    Map<String, Object> queryApplReraidPlanByloanNo(Map<String, Object> params);


}
