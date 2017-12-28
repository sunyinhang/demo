package com.haiercash.payplatform.service;

import java.util.Map;

/**
 * 额度相关总接口
 * @author ljy
 *
 */
public interface LimitService {
    //额度激活（判断跳转哪个页面）
    Map<String, Object> creditLineApply(String token, String channel, String channelNo);
}
