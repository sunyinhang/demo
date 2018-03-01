package com.haiercash.payplatform.common.dao;

import com.haiercash.payplatform.common.data.VipAbcAppOrderGoods;
import org.mybatis.mapper.common.BaseMapper;
import org.springframework.stereotype.Repository;

/**
 * Created by 程慧梅 on 2018/2/27.
 */
@Repository
public interface VipAbcOrderDao extends BaseMapper<VipAbcAppOrderGoods> {
    String queryvipabcapplSeq(String orderSn);

    String queryviporderno(String orderSn);
}
