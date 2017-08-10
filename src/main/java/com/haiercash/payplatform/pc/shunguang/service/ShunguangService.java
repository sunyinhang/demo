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

    /**
     * 保存微店主信息.
     * @param  ordinaryInfo
     * @return Map
     */
    Map<String, Object> saveOrdinaryUserInfo (Map<String, Object> ordinaryInfo);


    /**
     * 白条额度申请接口
     * @param map
     * @return Map
     * @throws Exception
     */
    Map<String, Object> edApply(Map<String, Object> map) throws Exception;

}
