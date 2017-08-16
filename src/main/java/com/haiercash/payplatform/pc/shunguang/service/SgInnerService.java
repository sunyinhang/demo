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
     * 订单保存
     * @param map
     * @return
     */
    Map<String, Object> saveOrder(Map<String, Object> map);
}
