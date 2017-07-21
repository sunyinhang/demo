package com.haiercash.appserver.service;

import java.util.Map;


public interface UauthService {

    /**
     * 用户支付密码手势密码验证是否设置.
     *
     * @param userId
     * @return
     */
    Map<String, Object> validateUserFlag(String userId);

    /**
     * 获取图片.
     *
     * @param userId
     * @return
     */
    Map<String, Object> getUserPic(String userId);

    /**
     * 支付密码验证.
     *
     * @param params
     * @return
     */
    Map<String, Object> validatePayPasswd(Map<String, Object> params);


    /**
     * 手势密码设置.
     *
     * @param params
     * @return
     */
    Map<String, Object> gesture(Map<String, Object> params, String token);

    /**
     * 手势密码验证.
     *
     * @param params
     * @return
     */
    Map<String, Object> validateGesture(Map<String, Object> params);

    /**
     * 冻结用户.
     *
     * @param params
     * @return
     */
    Map<String, Object> frozenUsers(Map<String, Object> params);


    /**
     * 支付密码设置.
     *
     * @param params
     * @return
     */
    Map<String, Object> payPasswd(Map<String, Object> params, String token);


    /**
     * 美凯龙、美分期等使用,是否注册.
     *
     * @param mobile
     * @return
     */
    Map<String, Object> isRegister(String mobile);

    /**
     * 嗨付专用.
     * @param mobile
     * @return
     */
    Map<String, Object> isRegisterHaier(String mobile);

    /**
     * 登录验证(Portal使用).
     *
     * @param params
     * @return
     */
    Map<String, Object> validateUsers(Map<String, Object> params);

    /**
     * 头像设置.
     *
     * @param params
     * @return
     */
    Map<String, Object> avatarUrl(Map<String, Object> params);

    /**
     * 实名认证.
     *
     * @param params
     * @return
     */
    Map<String, Object> identify(Map<String, Object> params);


    /**
     * 修改绑定手机号.
     *
     * @param params
     * @return
     */
    Map<String, Object> updateMobile(Map<String, Object> params);

    /**
     * 客户登录密码设置、修改（验证码）.
     *
     * @param params
     * @return
     */
    Map<String, Object> custUpdatePwd(Map<String, Object> params);

    /**
     * 客户登录密码设置、修改（实名认证+验证码）.
     *
     * @param params
     * @return
     */
    Map<String, Object> custVerifyUpdatePwd(Map<String, Object> params);

    /**
     * 支付密码修改.
     *
     * @param params
     * @return
     */
    Map<String, Object> updatePayPasswd(Map<String, Object> params, String token);

    /**
     * 用户注册.
     *
     * @param params
     * @param channel
     * @return
     */
    Map<String, Object> saveUauthUsers(Map<String, Object> params, String channel);

    /**
     * 客户支付密码设置、修改（实名认证+验证码）.
     *
     * @param params
     * @return
     */
    Map<String, Object> custVerifyUpdatePayPwd(Map<String, Object> params);

    /**
     * 登陆验证.
     *
     * @param params
     * @return
     */
    Map<String, Object> validateUsersCount(Map<String, Object> params);

    /**
     * 生成token并获取用户是否已实名认证.
     * @param map validateUser*返回的map.
     * @param clientId 设备id.
     * @return map
     */
    Map<String, Object> createTokenAndgetRealInfo(Map<String, Object> map, String clientId);

    /**
     * 手势密码验证.
     *
     * @param params
     * @return
     */
    Map<String, Object> validateGestureCount(Map<String, Object> params);

    /**
     * 根据集团用户id查询用户信息.
     *
     * @param params
     * @return
     */
    Map<String, Object> queryHaierUserInfo(Map<String, Object> params);

    /**
     * 集团用户注册统一认证账户.
     *
     * @param params
     * @return
     */
    Map<String, Object> saveUauthUsersByHaier(Map<String,Object> params);

    /**
     *1.29(POST)验证并绑定集团用户（已绑定的不可用）
     */
    Map<String, Object> validateAndBindHaierUser(Map<String,Object> params);

    /**
     * 登录验证(支持海尔集团会员用户登录认证、不记录登录失败次数) 统一认证30接口
     * @param params
     * @return
     */
    Map<String, Object> validateUsersHaier(Map<String, Object> params);

    Map<String, Object> getMobile(String userId);

    /**
     * 1.34(GET)刷新海尔会员验证码
     * @return
     */
    Map<String, Object> haierCaptcha();
}
