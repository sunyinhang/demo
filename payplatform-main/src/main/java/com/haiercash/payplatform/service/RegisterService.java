package com.haiercash.payplatform.service;

import java.util.Map;

/**
 * 注册登陆相关接口
 * Created by ljy on 2017/8/17.
 */
public interface RegisterService {
    //判断用户是否注册
    public Map<String, Object> isRegister(String token, String channel, String channelNo, Map<String, Object> params) throws Exception;

    //用户注册
    public Map<String, Object> saveUauthUsers(String token, Map<String, Object> params) throws Exception;

    //用户登陆
    public Map<String, Object> validateUsers(String token, String channel, String channelNo, Map<String, Object> params) throws Exception;

    //客户登录密码设置、修改
    public Map<String, Object> custUpdatePwd(String token, Map<String, Object> params) throws Exception;
}
