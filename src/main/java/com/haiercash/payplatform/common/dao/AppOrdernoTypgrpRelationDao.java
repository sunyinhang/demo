package com.haiercash.payplatform.common.dao;

import com.haiercash.payplatform.common.data.AppOrdernoTypgrpRelation;
import org.mybatis.mapper.common.BaseMapper;

/**
 * 订单收单关系维护表dao.
 * @author Liu qingxiang
 * @since v1.0.1
 */
public interface AppOrdernoTypgrpRelationDao extends BaseMapper<AppOrdernoTypgrpRelation>{

    /**
     * 根据orderNo获取关联关系.
     * @param noderNo  订单号
     * @return
     */
    AppOrdernoTypgrpRelation selectByOrderNo(String noderNo);
}
