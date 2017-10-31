package com.haiercash.payplatform.pc.cashloan.service.impl;

import com.bestvike.io.CharsetNames;
import com.bestvike.lang.Base64Utils;
import com.bestvike.lang.Convert;
import com.bestvike.lang.StringUtils;
import com.bestvike.lang.ThrowableUtils;
import com.bestvike.serialization.JsonSerializer;
import com.haiercash.payplatform.common.dao.CooperativeBusinessDao;
import com.haiercash.payplatform.common.data.CooperativeBusiness;
import com.haiercash.payplatform.common.data.EntrySetting;
import com.haiercash.payplatform.common.entity.ThirdTokenVerifyResult;
import com.haiercash.payplatform.pc.cashloan.service.ThirdTokenVerifyService;
import com.haiercash.payplatform.rest.client.JsonClientUtils;
import com.haiercash.payplatform.service.AppServerService;
import com.haiercash.payplatform.service.BaseService;
import com.haiercash.payplatform.utils.BusinessException;
import com.haiercash.payplatform.utils.ConstUtil;
import com.haiercash.payplatform.utils.RSAUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

/**
 * Created by 许崇雷 on 2017-10-23.
 */
@Service
public class CommonThirdTokenVerifyService extends BaseService implements ThirdTokenVerifyService {
    @Autowired
    private AppServerService appServerService;
    @Autowired
    private CooperativeBusinessDao cooperativeBusinessDao;

    @Override
    public ThirdTokenVerifyResult verify(EntrySetting setting, String token) {
        //查询渠道配置
        CooperativeBusiness channelConfig = cooperativeBusinessDao.selectBycooperationcoed(this.getChannelNo());
        if (channelConfig == null)
            throw new BusinessException(ConstUtil.ERROR_CODE, "未配置该渠道的加密信息");
        //token 加密
        String tokenEncripted;
        try {
            byte[] encryptData = RSAUtils.encryptByPublicKey(token.getBytes(CharsetNames.UTF_8), channelConfig.getRsapublic());
            tokenEncripted = Base64Utils.encode(encryptData);
        } catch (Exception e) {
            logger.error(ThrowableUtils.getString(e));
            throw new BusinessException(ConstUtil.ERROR_CODE, "加密token 失败:" + ThrowableUtils.getMessage(e));
        }

        //验证
        String verifyUrl = setting.getVerifyUrlThird();
        logger.info("验证token url:" + verifyUrl);
        HttpHeaders headers = new HttpHeaders();
        headers.set("token", tokenEncripted);
        String response = JsonClientUtils.postForString(verifyUrl, null, headers);
        if (StringUtils.isEmpty(response))
            throw new BusinessException(ConstUtil.ERROR_CODE, "验证 token 未返回任何数据");

        Map<String, Object> map = JsonSerializer.deserializeMap(response);
        String code = Convert.toString(map.get("code"));
        if (!Objects.equals(code, "0000")) {
            String msg = Convert.toString(map.get("msg"));
            if (StringUtils.isEmpty(msg))
                msg = "没有任何错误信息";
            throw new BusinessException(ConstUtil.ERROR_CODE, msg);
        }
        //验证客户信息
        String userId = Convert.toString(map.get("user_id"));
        String phoneNumber = Convert.toString(map.get("phone_number"));
        if (StringUtils.isEmpty(userId))
            throw new BusinessException(ConstUtil.ERROR_CODE, "验证用户信息时, UID 为空");
        if (StringUtils.isEmpty(phoneNumber))
            throw new BusinessException(ConstUtil.ERROR_CODE, "验证用户信息时, 客户未进行手机号绑定");
        //返回
        ThirdTokenVerifyResult result = new ThirdTokenVerifyResult();
        result.setUserId(userId);
        result.setPhoneNo(phoneNumber);
        return result;
    }
}
