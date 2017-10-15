package com.haiercash.payplatform.common.dao;

import com.haiercash.payplatform.common.data.SignContractInfo;
import org.apache.ibatis.annotations.Param;
import org.mybatis.mapper.common.BaseMapper;

/**
 * Created by yuanli on 2017/10/15.
 */
public interface SignContractInfoDao extends BaseMapper<SignContractInfo> {
    SignContractInfo getSignContractInfo(@Param("typcde") String typcde);
}
