package com.haiercash.payplatform.common.service;

import java.util.Map;

/**
 * 客户扩展信息总接口
 * @author ljy
 *
 */
public interface CustExtInfoService {
    //获取客户个人扩展信息及影像
    public Map<String, Object> getAllCustExtInfoAndDocCde(String token,String channel,String channelNo) throws Exception;
    //获取客户个人扩展信息
    public Map<String, Object> getAllCustExtInfo(String token,String channel,String channelNo) throws Exception;
}
