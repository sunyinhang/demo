package com.haiercash.payplatform.pc.vipabc.service;

import java.util.Map;

/**
 * Created by Administrator on 2017/12/25.
 */
public interface VipAbcService {
    /**
     * 根据第三方订单号查询身份证号
     *
     * @param map
     * @return
     */

    Map<String, Object> getIdCardInfo(Map<String, Object> map);

    /**
     * 判断用户是否注册
     *
     * @param token
     * @param channel
     * @param channelNo
     * @param params
     * @return
     */
    Map<String, Object> isRegister(String token, String channel, String channelNo, Map<String, Object> params);

    /**
     * 刷新海尔会员验证码
     *
     * @param channel
     * @param channelNo
     * @return
     */
    Map<String, Object> haierCaptcha(String channel, String channelNo);

    /**
     * 登录
     *
     * @param channel
     * @param channelNo
     * @param params
     * @return
     */
    Map<String, Object> vipAbcLogin(String channel, String channelNo, Map<String, Object> params);

    /**
     * 分期申请，订单保存
     *
     * @param token
     * @param channel
     * @param channelNo
     * @param params
     * @return
     * @throws Exception
     */
    Map<String, Object> vipAbcsaveOrderServlet(String token, String channel, String channelNo, Map<String, Object> params) throws Exception;

    /**
     * 分期详情
     *
     * @param token
     * @param channel
     * @param channelNo
     * @param params
     * @return
     */
    Map<String, Object> vipAbcpayApplyInfo(String token, String channel, String channelNo, Map<String, Object> params);

    /**
     * 第三方数据入口
     *
     * @param token
     * @param channel
     * @param channelNo
     * @param params
     * @return
     */
    Map<String, Object> vipAbcThirdPartyData(String token, String channel, String channelNo, Map<String, Object> params) throws Exception;


    /**
     * 二维码生成
     *
     * @param token
     * @param channel
     * @param channelNo
     * @param params
     * @return
     */
    Map<String, Object> vipAbcPcStore(String token, String channel, String channelNo, Map<String, Object> params);

    /**
     * 完善单位信息、个人信息、紧急联系人
     *
     * @param token
     * @param channel
     * @param channelNo
     * @param params
     * @return
     */
    Map<String, Object> saveCustExtInfo(String token, String channel, String channelNo, Map<String, Object> params);

    /**
     * 订单保存
     *
     * @param token
     * @param channel
     * @param channelNo
     * @param params
     * @return
     */
    Map<String, Object> treatyShowServlet(String token, String channel, String channelNo, Map<String, Object> params) throws Exception;


}
