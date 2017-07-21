package com.haiercash.common.data;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CityRepository extends PagingAndSortingRepository<CityBean, String> {

	@Query("from CityBean where admit = ?1 and channel = ?2 and typLevelTwo <> 'smrz'")
	List<CityBean> findByAdmit(@Param("admit") String admit, @Param("channel") String channel);
	@Query(value = "SELECT ID,ADMIT,CITY_CODE,PROVINCE_CODE,TYP_LEVEL_TWO,CHANNEL,is_China FROM (SELECT DISTINCT ID,ADMIT,CITY_CODE,PROVINCE_CODE,TYP_LEVEL_TWO,CHANNEL,is_China, REGEXP_SUBSTR (TYP_LEVEL_TWO,'[^,]+',1,LEVEL) AS TOKEN FROM APP_CITY_RANGE  T CONNECT BY REGEXP_SUBSTR (TYP_LEVEL_TWO,'[^,]+',1,LEVEL) IS NOT NULL) WHERE   ADMIT = ?1 and TOKEN = ?2 and channel = ?3",nativeQuery = true)
	List<CityBean> findByAdmitByTypLevelTwo(@Param("admit") String admit, @Param("typLevelTwo") String typLevelTwo, @Param("channel") String channel);
	@Query("from CityBean where admit = ?1 and channel = ?2 and typLevelTwo <> 'smrz'")
	List<CityBean> findByAdmitAndChannel(@Param("admit") String admit, @Param("channel") String channel);
	@Query(value = "SELECT ID,CHANNEL,ADMIT,CITY_CODE,PROVINCE_CODE,TYP_LEVEL_TWO,is_China FROM (SELECT DISTINCT ID,CHANNEL,ADMIT,CITY_CODE,PROVINCE_CODE,TYP_LEVEL_TWO,is_China,  REGEXP_SUBSTR (TYP_LEVEL_TWO,'[^,]+',1,LEVEL) AS TOKEN FROM APP_CITY_RANGE  T CONNECT BY REGEXP_SUBSTR (TYP_LEVEL_TWO,'[^,]+',1,LEVEL) IS NOT NULL) WHERE   ADMIT = ?1 and TOKEN = ?2 and channel = ?3",nativeQuery = true)
	List<CityBean> findByAdmitByTypLevelTwoAndChannel(@Param("admit") String admit, @Param("typLevelTwo") String typLevelTwo, @Param("channel") String channel);

	@Query(value = "SELECT ID,ADMIT,CITY_CODE,PROVINCE_CODE,TYP_LEVEL_TWO,CHANNEL,is_China FROM (SELECT DISTINCT ID,ADMIT,CITY_CODE,PROVINCE_CODE,TYP_LEVEL_TWO,CHANNEL,is_China, REGEXP_SUBSTR (TYP_LEVEL_TWO,'[^,]+',1,LEVEL) AS TOKEN FROM APP_CITY_RANGE  T CONNECT BY REGEXP_SUBSTR (TYP_LEVEL_TWO,'[^,]+',1,LEVEL) IS NOT NULL) WHERE  PROVINCE_CODE= ?1 and  CITY_CODE =?2 and  TOKEN = ?3 and channel = ?4",nativeQuery = true)
	List<CityBean> findByProvinceAndCity(@Param("provinceCode") String provinceCode,@Param("cityCode") String cityCode,@Param("typLevelTwo") String typLevelTwo, @Param("channel") String channel);

	@Query(value = "SELECT ID,ADMIT,CITY_CODE,PROVINCE_CODE,TYP_LEVEL_TWO,CHANNEL,is_China FROM (SELECT DISTINCT ID,ADMIT,CITY_CODE,PROVINCE_CODE,TYP_LEVEL_TWO,CHANNEL,is_China, REGEXP_SUBSTR (TYP_LEVEL_TWO,'[^,]+',1,LEVEL) AS TOKEN FROM APP_CITY_RANGE  T CONNECT BY REGEXP_SUBSTR (TYP_LEVEL_TWO,'[^,]+',1,LEVEL) IS NOT NULL) WHERE  PROVINCE_CODE= ?1  and  TOKEN = ?2 and channel = ?3 and CITY_CODE is null",nativeQuery = true)
	List<CityBean> findByProvince(@Param("provinceCode") String provinceCode,@Param("typLevelTwo") String typLevelTwo, @Param("channel") String channel);

}
