package com.haiercash.payplatform.common.dao;

import com.haiercash.payplatform.common.data.BcBankInfo;
import org.mybatis.mapper.common.BaseMapper;
import org.springframework.stereotype.Repository;

@Repository
public interface BcBankInfoDao extends BaseMapper<BcBankInfo> {

    BcBankInfo selectById(String id);
}
