package com.haiercash.common.data;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CaSignPushRepository extends PagingAndSortingRepository<CaSignPush, String> {
    @Query(value = "from CaSignPush where flag = ?1 ")
    List<CaSignPush> getMsgByflag(String flag);
}
