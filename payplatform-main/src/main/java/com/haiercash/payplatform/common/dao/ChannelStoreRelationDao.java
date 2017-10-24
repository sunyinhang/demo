package com.haiercash.payplatform.common.dao;

import com.haiercash.payplatform.common.data.ChannelStoreRelation;
import org.mybatis.mapper.common.BaseMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by 许崇雷 on 2017-10-18.
 */
@Repository
public interface ChannelStoreRelationDao extends BaseMapper<ChannelStoreRelation> {
    List<ChannelStoreRelation> selectByChanelNo(String channelNo);
}
