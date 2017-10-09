package com.haiercash.payplatform.service;

import java.util.Map;

/**
 * 注册相关接口
 * Created by ljy on 2017/8/17.
 */
public interface RegisterService {
    //判断用户是否注册
    public Map<String, Object> isRegister(String token, String channel, String channelNo,Map<String, Object> params) throws Exception;
    public Map<String, Object> saveUauthUsers(String token,Map<String, Object> params) throws Exception;
}
