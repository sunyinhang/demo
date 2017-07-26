package com.haiercash.payplatform.service;

import com.haiercash.payplatform.data.BcBankInfo;

/**
 * bc bank info service.
 * @author Liu qingxiang
 * @since v1.0.0
 */
public interface BcBankInfoService {

    BcBankInfo selectById(String id);

}
