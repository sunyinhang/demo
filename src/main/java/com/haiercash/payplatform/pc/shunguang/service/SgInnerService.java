package com.haiercash.payplatform.pc.shunguang.service;

import java.util.Map;

/**
 * shunguang service interface.
 * @author yuan li
 * @since v1.0.1
 */
public interface SgInnerService {

    /**
     * 登录（同于集团用户绑定）
     * @param map
     * @return
     */
    Map<String, Object> userlogin(Map<String, Object> map);

    /**
     * 白条分期页面加载
     * @param map
     * @return
     */
    Map<String, Object> initPayApply(Map<String, Object> map);

    /**
     * 白条分期页面获取应还款总额
     * @param map
     * @return
     */
    Map<String, Object> gettotalAmt(Map<String, Object> map);

    /**
     * 根据用户中心token获取统一认证Id
     * @param token
     * @return
     */
    String getuserId(String token);

    /**
     * 额度回调url
     * @return
     */
    Map<String, Object> getedbackurl();

    /**
     * 贷款回调url
     * @return
     */
    Map<String, Object> getpaybackurl();
}
