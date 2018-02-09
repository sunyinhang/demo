package com.haiercash.payplatform.pc.alipay.service;

import com.haiercash.payplatform.common.data.EntrySetting;
import com.haiercash.payplatform.common.entity.ThirdTokenVerifyResult;
import com.haiercash.payplatform.pc.alipay.bean.AlipayToken;
import com.haiercash.payplatform.pc.alipay.util.AlipayUtils;
import com.haiercash.payplatform.pc.cashloan.service.ThirdTokenVerifyService;
import com.haiercash.spring.service.BaseService;
import org.springframework.stereotype.Service;

/**
 * Created by 许崇雷 on 2018-02-09.
 */
@Service
public class AlipayAuthCodeVerifyService extends BaseService implements ThirdTokenVerifyService {
    @Override
    public ThirdTokenVerifyResult verify(EntrySetting setting, String thirdToken) throws Exception {
        AlipayToken alipayToken = AlipayUtils.getOauthTokenByAuthCode(thirdToken);
        String aliUid = alipayToken.getUserId();//支付宝 userId
        ThirdTokenVerifyResult result = new ThirdTokenVerifyResult();
        result.setUserId(aliUid);
        return result;
    }
}
