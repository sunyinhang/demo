package com.haiercash.common.data;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AppContractRepository extends PagingAndSortingRepository<AppContract, String> {

	@Query(" from AppContract where contType = ?1 ")
	List<AppContract> findByContType(@Param("contType") String contType);

	@Query(value = " select * from app_contract_info where cont_type = ?1  and apply_type like %?2% ", nativeQuery = true)
	AppContract findByContTypeAndApplyType(@Param("contType") String contType, @Param("applyType") String applyType);

	// @Query(value="select * from app_contract_info where cont_type =
	// ?1",nativeQuery=true)
	// AppContract findByTypCde(@Param("typCde") String typCde);

	// @Query(value="from AppContract where contType = ?1")
	// AppContract findByTypLevelTwo(@Param("typLevelTwo") String typLevelTwo);

}
