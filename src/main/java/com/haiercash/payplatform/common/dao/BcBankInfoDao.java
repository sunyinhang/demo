package com.haiercash.payplatform.common.dao;

import org.mybatis.mapper.common.BaseMapper;

import com.haiercash.payplatform.common.data.BcBankInfo;

public interface BcBankInfoDao extends BaseMapper<BcBankInfo> {

    BcBankInfo selectById(String id);
}
