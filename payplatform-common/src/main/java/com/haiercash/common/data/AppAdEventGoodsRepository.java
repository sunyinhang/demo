package com.haiercash.common.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AppAdEventGoodsRepository extends JpaRepository<AppAdEventGoods, String> {

    @Query(value="select * from  APP_AD_EVENT_GOODS t where  AD_ID = ?1 order by create_time" ,nativeQuery = true)
    List<AppAdEventGoods> findByAdId(String adId);
}
