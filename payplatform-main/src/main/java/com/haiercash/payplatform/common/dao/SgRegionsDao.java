package com.haiercash.payplatform.common.dao;

import com.haiercash.payplatform.common.data.SgRegions;
import org.mybatis.mapper.common.BaseMapper;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/4/30.
 */
public interface SgRegionsDao extends BaseMapper<SgRegions> {
    SgRegions selectByGbCode(String gbCode);

    SgRegions selectByRegionId(String regionId);
}
