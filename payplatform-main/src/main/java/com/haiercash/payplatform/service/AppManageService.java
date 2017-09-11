package com.haiercash.payplatform.service;

import java.util.Map;

/**
 * @author zhouwushuang
 * @date 2017.01.06
 */
public interface AppManageService {

    /**
     * 获取销售信息
     *
     * @param typCde
     * @return saler_cde/saler_name/saler_mobile/coopr_name/coopr_cde/operator_name/operator_cde/operator_tel
     * 如果查询发生错误，返回null
     */
    Map<String, Object> getSaleMsg(String typCde);

    /**
     * 直接把销售信息放入map中
     *
     * @param typCde,map
     */
    boolean putSaleMsgIntoMap(String typCde, Map<String, Object> map);

    /**
     * 获取appmanage字典信息
     *
     * @param dictCode
     * @return
     */
    String getDictDetailByDictCde(String dictCode);

    /**
     * 把配置门店信息放入map
     *
     * @param map
     * @param channel 渠道号
     */
    void putCooprSettingToMap(Map<String, Object> map, String channel);

    /**
     * 把默认门店信息放入map
     *
     * @param map
     */
    void putDefaultCooprSettingToMap(Map<String, Object> map);
}
