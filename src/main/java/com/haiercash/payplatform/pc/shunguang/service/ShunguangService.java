package com.haiercash.payplatform.pc.shunguang.service;

import java.util.Map;

/**
 * shunguang service interface.
 *
 * @author yuan li
 * @since v1.0.1
 */
public interface ShunguangService {

    /**
     * 保存微店主信息.
     *
     * @param storeInfo
     * @return Map
     */
    Map<String, Object> saveStoreInfo(Map<String, Object> storeInfo);

    /**
     * 保存微店主信息.
     *
     * @param ordinaryInfo
     * @return Map
     */
    Map<String, Object> saveOrdinaryUserInfo(Map<String, Object> ordinaryInfo);

    /**
     * 白条支付申请接口
     *
     * @param map
     * @return
     * @throws Exception
     */
    Map<String, Object> payApply(Map<String, Object> map) throws Exception;

    /**
     * 白条额度申请接口
     *
     * @param map
     * @return Map
     * @throws Exception
     */
    Map<String, Object> edApply(Map<String, Object> map) throws Exception;//checkEdAppl


    /**
     * 7.白条额度申请状态查询    Sg-10006    checkEdAppl
     *
     * @param map
     * @return
     */
    Map<String, Object> checkEdAppl(Map<String, Object> map);


    /**
     * 9. 白条额度进行贷款支付结果主动查询接口
     *
     * @param map
     * @return
     * @throws Exception
     */
    Map<String, Object> queryAppLoanAndGoods(Map<String, Object> map) throws Exception;

    /**
     * 10.  白条额度进行贷款支付结果主动查询接口    Sg-10009
     *
     * @param map
     * @return
     * @throws Exception
     */

    Map<String, Object> queryAppLoanAndGoodsOne(Map<String, Object> map) throws Exception;


    /**
     * 11.  白条额度进行主动查询接口    Sg-10010
     *
     * @param map
     * @return
     * @throws Exception
     */
    Map<String, Object> edcheck(Map<String, Object> map) throws Exception;

}
