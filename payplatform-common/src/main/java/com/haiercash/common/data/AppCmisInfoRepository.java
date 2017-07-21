package com.haiercash.common.data;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Created by zhouwushuang on 2017.02.21.
 */
public interface AppCmisInfoRepository extends CrudRepository<AppCmisInfo, String> {

    List<AppCmisInfo> findByFlag(String flag);

    @Query(value = "select * from app_cmis_info where flag = ?1 and (request_map like '%\"dataTyp\":\"02\"%' or request_map like '%\"dataTyp\":\"03\"%' or request_map like '%\"dataTyp\":\"06\"%')", nativeQuery = true)
    List<AppCmisInfo> findByFlagAndType020306(String flag);

    @Query(value = "select * from app_cmis_info where flag = ?1 and (request_map not like '%\"dataTyp\":\"02\"%' and request_map not like '%\"dataTyp\":\"03\"%' and  request_map not like '%\"dataTyp\":\"06\"%')", nativeQuery = true)
    List<AppCmisInfo> findByFlagAndNoType020306(String flag);

    @Query(value = "select RISK_SEQUENCE.nextVal from dual", nativeQuery = true)
    Long getRiskSequence();
}
