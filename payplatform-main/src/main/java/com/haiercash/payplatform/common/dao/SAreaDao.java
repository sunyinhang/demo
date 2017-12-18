package com.haiercash.payplatform.common.dao;

import com.haiercash.payplatform.common.data.SArea;
import org.apache.ibatis.annotations.Param;
import org.mybatis.mapper.common.BaseMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by yuanli on 2017/12/13.
 */
@Repository
public interface SAreaDao extends BaseMapper<SArea> {
    //根据名称及类型查询
    SArea getByCodeAndType(@Param("areaName") String areaName, @Param("areaType") String areaType);
    //根据名称查询
    List<SArea> selectByName(@Param("areaName") String areaName);
    //根据编码查询
    List<SArea> selectByCode(@Param("areaCode") String areaCode);

    //根据地区名称及上级编码查询
    SArea selectByCodeAndAreaParentCode(@Param("areaName") String areaName, @Param("areaParentCode") String areaParentCode, @Param("areaType") String areaType);

    //根据省编码、区名称查询
    SArea searchCityCode(@Param("areaParentCode") String areaParentCode, @Param("areaName") String areaName);

    //根据编码及类型查询
    SArea selectByCodeAndAreaType(@Param("areaCode") String areaCode, @Param("areaType") String areaType);

}
