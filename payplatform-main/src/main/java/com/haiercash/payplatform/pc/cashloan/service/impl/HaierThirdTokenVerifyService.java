package com.haiercash.payplatform.pc.cashloan.service.impl;

import com.bestvike.lang.Convert;
import com.bestvike.lang.StringUtils;
import com.bestvike.serialization.JsonSerializer;
import com.haiercash.payplatform.common.data.EntrySetting;
import com.haiercash.payplatform.common.entity.ThirdTokenVerifyResult;
import com.haiercash.payplatform.pc.cashloan.service.ThirdTokenVerifyService;
import com.haiercash.payplatform.rest.RestTemplateUtil;
import com.haiercash.payplatform.service.BaseService;
import com.haiercash.payplatform.utils.BusinessException;
import com.haiercash.payplatform.utils.ConstUtil;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by 许崇雷 on 2017-10-11.
 */
@Service
public class HaierThirdTokenVerifyService extends BaseService implements ThirdTokenVerifyService {
    @Override
    public ThirdTokenVerifyResult verify(EntrySetting setting, String token) {
        String verifyUrl = setting.getVerifyUrlThird();
        Map<String, Object> param = new HashMap<>();
        param.put("access_token", token);
        //验证客户信息
        String response = RestTemplateUtil.getForString(verifyUrl, param);
        if (StringUtils.isEmpty(response))
            throw new BusinessException(ConstUtil.ERROR_CODE, "验证 token 未返回任何数据");

        Map<String, Object> map = JsonSerializer.deserializeMap(response);
        //读取 user_id
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
}
