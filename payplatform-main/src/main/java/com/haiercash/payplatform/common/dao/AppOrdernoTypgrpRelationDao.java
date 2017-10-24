package com.haiercash.payplatform.common.dao;

import com.haiercash.payplatform.common.data.AppOrdernoTypgrpRelation;
import org.mybatis.mapper.common.BaseMapper;
import org.springframework.stereotype.Repository;

/**
 * 订单收单关系维护表dao.
 * @author Liu qingxiang
 * @since v1.0.1
 */
@Repository
public interface AppOrdernoTypgrpRelationDao extends BaseMapper<AppOrdernoTypgrpRelation>{

    /**
     * 根据orderNo获取关联关系.
     * @param orderNo  订单号
     * @return
     */
    AppOrdernoTypgrpRelation selectByOrderNo(String orderNo);

    /**
     * 根据applSeq获取关联关系
     * @param applSeq  申请流水号
     * @return
     */
    AppOrdernoTypgrpRelation selectByApplSeq(String applSeq);

    /**
     * 根据订单号删除关联关系
     * @param orderNo  订单号
     */
    void deleteByOrderNo(String orderNo);

    /**
     * 保存关联关系
     * @param appOrdernoTypgrpRelation
     * @return
     */
    int saveAppOrdernoTypgrpRelation(AppOrdernoTypgrpRelation appOrdernoTypgrpRelation);
}
