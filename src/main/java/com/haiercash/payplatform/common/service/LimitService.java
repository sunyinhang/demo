package com.haiercash.payplatform.common.service;

import java.util.Map;

/**
 * 额度相关总接口
 * @author ljy
 *
 */
public interface LimitService {
    //额度激活（判断跳转哪个页面）
    public Map<String, Object> CreditLineApply(String token, String channel, String channelNo,Map<String, Object> params) throws Exception;
}
