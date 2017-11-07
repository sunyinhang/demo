package com.haiercash.payplatform.common.dao;

import com.haiercash.payplatform.common.data.SgRegions;
import org.mybatis.mapper.common.BaseMapper;
import org.springframework.stereotype.Repository;

/**
 * Created by Administrator on 2017/4/30.
 */
@Repository
public interface SgRegionsDao extends BaseMapper<SgRegions> {
    SgRegions selectByGbCode(String gbCode);

    SgRegions selectByRegionId(String regionId);
}
