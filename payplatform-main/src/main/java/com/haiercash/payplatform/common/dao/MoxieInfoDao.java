package com.haiercash.payplatform.common.dao;

import com.haiercash.payplatform.common.data.MoxieInfo;
import org.apache.ibatis.annotations.Param;
import org.mybatis.mapper.common.BaseMapper;
import org.springframework.stereotype.Repository;

/**
 * Created by yuanli on 2017/10/9.
 */
@Repository
public interface MoxieInfoDao extends BaseMapper<MoxieInfo> {
    int saveMoxieInfoDao(MoxieInfo moxieInfo);

    MoxieInfo getMoxieInfo(@Param("applseq") String applseq, @Param("flag") String flag);
}
