package com.haiercash.payplatform.common.dao;

import com.haiercash.payplatform.common.data.VipAbcAppOrderGoods;
import org.apache.ibatis.annotations.Param;
import org.mybatis.mapper.common.BaseMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by Administrator on 2017/12/25.
 */
@Repository
public interface VipAbcDao extends BaseMapper<VipAbcAppOrderGoods> {
    List<String> selectIdCard(@Param("ordersn") String ordersn);
}
