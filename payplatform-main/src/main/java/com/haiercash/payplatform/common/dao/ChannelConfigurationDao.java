package com.haiercash.payplatform.common.dao;

import com.haiercash.payplatform.common.consts.BusinessConstance;
import com.haiercash.payplatform.common.data.ChannelConfiguration;
import org.mybatis.mapper.common.BaseMapper;
import org.springframework.stereotype.Repository;

/**
 * Created by 许崇雷 on 2018-01-04.
 */
@Repository
public interface ChannelConfigurationDao extends BaseMapper<ChannelConfiguration> {
    ChannelConfiguration selectConfig(String channelNo, String activeFlag);

    default ChannelConfiguration selectActiveConfig(String channelNo) {
        return this.selectConfig(channelNo, BusinessConstance.COOPERATIVEBUSINESS_ACTIVE);
    }
}
