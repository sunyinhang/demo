package com.haiercash.payplatform.common.dao;

import com.haiercash.payplatform.common.data.ShunGuangthLog;
import feign.Param;
import org.mybatis.mapper.common.BaseMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by Administrator on 2017/12/23.
 */
@Repository
public interface ShunGuangthLogDao extends BaseMapper<ShunGuangthLog> {

    List<ShunGuangthLog> selectDataByFlag(@Param("flag") String flag, @Param("channelNo") String channelNo);

    void updateFlagById(@Param("time") String time, @Param("flag") String flag, @Param("logId") String logId);

    void updateTimesById(@Param("time") String time, @Param("logId") String logId);
}