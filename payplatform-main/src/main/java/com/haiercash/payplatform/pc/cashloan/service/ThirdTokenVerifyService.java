package com.haiercash.payplatform.pc.cashloan.service;

import com.haiercash.payplatform.common.data.ActivityPageSetting;
import com.haiercash.payplatform.common.entity.ThirdTokenVerifyResult;

import java.util.Map;

/**
 * Created by 许崇雷 on 2017-10-11.
 */
public interface ThirdTokenVerifyService {
    ThirdTokenVerifyResult verify(ActivityPageSetting setting, String token);

    /**
     * 从后台查询第三方用户的信息
     *
     * @param outUserId 明文,第三方用户 id
     * @return
     */
    Map<String, Object> queryUserInfoFromAppServer(String outUserId);

    /**
     * 向后台注册第三方用户
     *
     * @param outUserId 明文,第三方用户 id
     * @param phoneNo   明文,电话
     * @return
     */
    Map<String, Object> registerUserToAppServer(String outUserId, String phoneNo);
}
