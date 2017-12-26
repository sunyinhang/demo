package com.haiercash.payplatform.common.dao;

import com.haiercash.payplatform.common.data.VipAbcAppOrderGoods;
import org.mybatis.mapper.common.BaseMapper;
import org.springframework.stereotype.Repository;

/**
 * Created by Administrator on 2017/12/25.
 */
@Repository
public interface VipAbcDao extends BaseMapper<VipAbcAppOrderGoods> {
    String selectIdCard(String ordersn);
}
