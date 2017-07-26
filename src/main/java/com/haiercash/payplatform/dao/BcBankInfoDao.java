package com.haiercash.payplatform.dao;

import com.haiercash.payplatform.data.BcBankInfo;
import org.mybatis.mapper.common.BaseMapper;

public interface BcBankInfoDao extends BaseMapper<BcBankInfo> {

    BcBankInfo selectById(String id);
}
