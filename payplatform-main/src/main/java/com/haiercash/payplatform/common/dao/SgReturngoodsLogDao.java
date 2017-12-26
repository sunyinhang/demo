package com.haiercash.payplatform.common.dao;

import com.haiercash.payplatform.common.data.SgReturngoodsLog;
import feign.Param;
import org.mybatis.mapper.common.BaseMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by Administrator on 2017/12/23.
 */
@Repository
public interface SgReturngoodsLogDao extends BaseMapper<SgReturngoodsLog> {
    //根据商城订单号查询退货推送信息
    SgReturngoodsLog getByMallOrderNo(@Param("mallorderno") String mallorderno);
    //根据商城订单号修改退货推送信息
    void updateByMallOrderNo(SgReturngoodsLog SgReturngoodsLog);

    List<SgReturngoodsLog> selectDataByFlag(@Param("flag") String flag, @Param("channelNo") String channelNo);

    void updateFlagById(@Param("time") String time, @Param("flag") String flag, @Param("logId") String logId);

    void updateTimesById(@Param("time") String time, @Param("logId") String logId);
}