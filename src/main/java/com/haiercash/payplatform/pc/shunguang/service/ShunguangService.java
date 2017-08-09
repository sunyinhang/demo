package com.haiercash.payplatform.pc.shunguang.service;

import java.util.Map;

/**
 * shunguang service interface.
 * @author yuan li
 * @since v1.0.1
 */
public interface ShunguangService {

    /**
     * 保存微店主信息.
     * @param storeInfo
     * @return Map
     */
    Map<String, Object> saveStoreInfo (Map<String, Object> storeInfo);

    // todo 对接口功能以及参数进行注解
    /**
     * 额度申请.
     * @param map
     * @return Map
     * @throws Exception
     */
    Map<String, Object> edApply(Map<String, Object> map) throws Exception;

}
