package com.haiercash.common.data;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Created by use on 2017/3/14.
 */
public interface MoxieInfoRepository extends CrudRepository<MoxieInfo, String> {

    @Query(value = "from MoxieInfo where applseq = ?1 and flag = ?2")
    MoxieInfo getMoxieInfo(String applseq, String flag);
}
