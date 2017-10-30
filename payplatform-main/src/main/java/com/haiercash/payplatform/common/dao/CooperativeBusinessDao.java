package com.haiercash.payplatform.common.dao;

import com.haiercash.payplatform.common.data.CooperativeBusiness;
import org.mybatis.mapper.common.BaseMapper;
import org.springframework.stereotype.Repository;

/**
 * Created by use on 2017/8/8.
 */
@Repository
public interface CooperativeBusinessDao extends BaseMapper<CooperativeBusiness> {
    CooperativeBusiness selectBycooperationcoed(String cooperationcoed);
}
