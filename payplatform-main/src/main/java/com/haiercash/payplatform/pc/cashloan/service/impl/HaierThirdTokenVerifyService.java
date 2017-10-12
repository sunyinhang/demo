package com.haiercash.payplatform.pc.cashloan.service.impl;

import com.bestvike.lang.Convert;
import com.bestvike.lang.StringUtils;
import com.bestvike.serialization.JsonSerializer;
import com.haiercash.payplatform.common.data.EntrySetting;
import com.haiercash.payplatform.common.entity.ThirdTokenVerifyResult;
import com.haiercash.payplatform.pc.cashloan.service.ThirdTokenVerifyService;
import com.haiercash.payplatform.service.AppServerService;
import com.haiercash.payplatform.service.BaseService;
import com.haiercash.payplatform.utils.BusinessException;
import com.haiercash.payplatform.utils.ConstUtil;
import com.haiercash.payplatform.utils.EncryptUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by 许崇雷 on 2017-10-11.
 */
@Service
public class HaierThirdTokenVerifyService extends BaseService implements ThirdTokenVerifyService {
    @Autowired
    private AppServerService appServerService;

    @Override
    public ThirdTokenVerifyResult verify(EntrySetting setting, String token) {
        String verifyUrl = setting.getVerifyUrlThird();
        logger.info("验证海尔 token:" + verifyUrl);
        //验证客户信息
        ResponseEntity<String> responseEntity = this.restTemplate.getForEntity(verifyUrl, String.class);
        if (responseEntity.getStatusCode() != HttpStatus.OK)
            throw new BusinessException(ConstUtil.ERROR_CODE, "登陆已过期");
        //读取 user_id
        Map map = JsonSerializer.deserialize(responseEntity.getBody(), Map.class);
        String userId = Convert.toString(map.get("user_id"));
        String phoneNumber = Convert.toString(map.get("phone_number"));
        String error = Convert.toString(map.get("error"));
        if (StringUtils.isEmpty(userId))
            throw new BusinessException(ConstUtil.ERROR_CODE, StringUtils.defaultString(error, "验证客户信息失败"));
        if (StringUtils.isEmpty(phoneNumber))
            throw new BusinessException(ConstUtil.ERROR_CODE, "客户未进行手机号绑定");
        //返回
        ThirdTokenVerifyResult result = new ThirdTokenVerifyResult();
        result.setUserId(userId);
        result.setPhoneNo(phoneNumber);
        return result;
    }

    /**
     * 从后台查询第三方用户的信息
     *
     * @param outUserId 明文,第三方用户 id
     * @return
     */
    @Override
    public Map<String, Object> queryUserInfoFromAppServer(String outUserId) {
        String response = appServerService.queryHaierUserInfo(EncryptUtil.simpleEncrypt(outUserId));
        if (StringUtils.isEmpty(response))
            throw new BusinessException(ConstUtil.ERROR_CODE, "根据集团用户ID查询用户信息失败");
        Map<String, Object> map = JsonSerializer.deserializeMap(response);
//        if (!HttpUtil.isSuccess(map))
//            throw new BusinessException(HttpUtil.getReturnCode(map), HttpUtil.getRetMsg(map));
        return map;
    }

    /**
     * 向后台注册第三方用户
     *
     * @param outUserId 明文,第三方用户 id
     * @param phoneNo   明文,电话
     * @return
     */
    @Override
    public Map<String, Object> registerUserToAppServer(String outUserId, String phoneNo) {
        Map<String, Object> param = new HashMap<>(2);
        param.put("externUid", EncryptUtil.simpleEncrypt(outUserId));
        param.put("mobile", EncryptUtil.simpleEncrypt(phoneNo));
        Map<String, Object> response = appServerService.saveUauthUsersByHaier(param);
//        if (!HttpUtil.isSuccess(response))
//            throw new BusinessException(HttpUtil.getReturnCode(response), HttpUtil.getRetMsg(response));
        return response;
    }
}
