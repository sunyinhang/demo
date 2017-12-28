package com.haiercash.payplatform.pc.shunguang.service;

import com.haiercash.payplatform.common.data.AppOrder;

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
    Map<String, Object> payApply(Map<String, Object> map);

    /**
     * 白条额度申请接口
     *
     * @param map
     * @return Map
     * @throws Exception
     */
    Map<String, Object> edApply(Map<String, Object> map);

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
    Map<String, Object> queryAppLoanAndGoods(Map<String, Object> map);

    /**
     * 10.  白条额度进行贷款支付结果主动查询接口    Sg-10009
     *
     * @param map
     * @return
     * @throws Exception
     */

    Map<String, Object> queryAppLoanAndGoodsOne(Map<String, Object> map);


    /**
     * 11.  白条额度进行主动查询接口    Sg-10010
     *
     * @param map
     * @return
     * @throws Exception
     */
    Map<String, Object> edcheck(Map<String, Object> map);

    /**
     * 额度测试入口
     * @param map
     * @return
     * @throws Exception
     */
    Map<String, Object> edApplytest(Map<String, Object> map);

    /**
     * 贷款测试入口
     * @param appOrder
     * @return
     * @throws Exception
     */
    Map<String, Object> payApplytest(AppOrder appOrder);
    /**
     * @Title returnGoods
     * @Description: 退货接口
     * @author yu jianwei
     * @date 2017/11/6 17:41
     */
    Map<String, Object> returnGoods(Map<String, Object> map);

    /**
     * @Title getReturnGoodsInfo
     * @Description: 查询退货详情
     * @author yu jianwei
     * @date 2017/12/15 11:13
     */
    Map<String, Object> getReturnGoodsInfo(Map<String, Object> map);


    /**
     * 顺逛退货消息推送
     * @param map
     * @return
     */
    Map<String, Object> shunguangth(Map<String, Object> map);

    /**
     * @Title pushMessage
     * @Description:
     * @author yu jianwei
     * @date 2017/12/25 18:12
     */
    Map<String, Object> pushMessage();


}
