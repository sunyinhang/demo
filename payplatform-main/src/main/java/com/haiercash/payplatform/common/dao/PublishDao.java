package com.haiercash.payplatform.common.dao;

import com.haiercash.payplatform.common.data.CooperativeMsg;
import org.mybatis.mapper.common.BaseMapper;
import org.springframework.stereotype.Repository;

@Repository
public interface PublishDao extends BaseMapper<CooperativeMsg> {
    //贷款申请URL
    String selectChannelNoUrl(String channelNo);

    //额度申请URL
    String selectChannelNoUrlOne(String channelNo);

}
