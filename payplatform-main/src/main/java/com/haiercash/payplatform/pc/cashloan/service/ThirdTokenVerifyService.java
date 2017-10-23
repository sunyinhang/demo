package com.haiercash.payplatform.pc.cashloan.service;

import com.haiercash.payplatform.common.data.EntrySetting;
import com.haiercash.payplatform.common.entity.ThirdTokenVerifyResult;

/**
 * Created by 许崇雷 on 2017-10-11.
 */
public interface ThirdTokenVerifyService {
    ThirdTokenVerifyResult verify(EntrySetting setting, String token);
}
