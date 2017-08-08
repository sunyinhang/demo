package com.haiercash.payplatform.common.dao;

import com.haiercash.payplatform.common.data.BcBankInfo;
import com.haiercash.payplatform.common.data.CooperativeBusiness;
import org.mybatis.mapper.common.BaseMapper;

/**
 * Created by use on 2017/8/8.
 */
public interface CooperativeBusinessDao extends BaseMapper<CooperativeBusiness> {
    CooperativeBusiness selectBycooperationcoed(String cooperationcoed);
}
