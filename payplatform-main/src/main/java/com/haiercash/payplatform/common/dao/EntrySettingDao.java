package com.haiercash.payplatform.common.dao;

import com.haiercash.payplatform.common.data.EntrySetting;
import org.mybatis.mapper.common.BaseMapper;
import org.springframework.stereotype.Repository;

/**
 * Created by 许崇雷 on 2017-10-10.
 */
@Repository
public interface EntrySettingDao extends BaseMapper<EntrySetting> {
    EntrySetting selectBychanelNo(String channelNo);
}
