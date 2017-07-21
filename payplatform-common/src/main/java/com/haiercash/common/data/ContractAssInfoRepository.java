package com.haiercash.common.data;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ContractAssInfoRepository extends PagingAndSortingRepository<ContractAssInfo, String> {

	@Query(value = " select * from app_contract_association_info where (mtd_code= ?1 or mtd_code is null) and contract_seri_no =(select cont_code from app_contract_info where cont_type = ?2 and apply_type like %?3%) ", nativeQuery = true)
	List<ContractAssInfo> findByPayMtdAndTypLevelTwoAndTypCde(@Param("payMtd") String payMtd,
                                                              @Param("typLevelTwo") String typLevelTwo, @Param("typCde") String typCde);

	@Query(value = " select * from app_contract_association_info where (mtd_code= ?1 or mtd_code is null )and contract_seri_no =(select cont_code from app_contract_info where cont_type = ?2 and apply_type like %?3%) and apply_tnr = ?4", nativeQuery = true)
	List<ContractAssInfo> findByPayMtdAndTypLevelTwoAndTypCdeAndApplyTnr(@Param("payMtd") String payMtd,
                                                                         @Param("typLevelTwo") String typLevelTwo, @Param("typCde") String typCde,
                                                                         @Param("applyTnr") String applyTnr);

	@Query(value = " select * from app_contract_association_info where (mtd_code= ?1 or mtd_code is null) and contract_seri_no =(select cont_code from app_contract_info where cont_type = ?2 ) and apply_tnr = ?3", nativeQuery = true)
	List<ContractAssInfo> findByPayMtdAndTypLevelTwoAndApplyTnr(@Param("payMtd") String payMtd,
                                                                @Param("typLevelTwo") String typLevelTwo, @Param("applyTnr") String applyTnr);

	@Query(value = " select * from app_contract_association_info where (mtd_code= ?1 or mtd_code is null) and contract_seri_no =(select cont_code from app_contract_info where cont_type = ?2 and apply_type like %?3%) and apply_tnr = ?4 and has_com_repay=?5", nativeQuery = true)
	ContractAssInfo findByHasComRepayMore(@Param("payMtd") String payMtd, @Param("typLevelTwo") String typLevelTwo,
                                          @Param("typCde") String typCde, @Param("applyTnr") String applyTnr,
                                          @Param("hasComRepay") String hasComRepay);

	@Query(value = " select * from app_contract_association_info where (mtd_code= ?1 or mtd_code is null) and contract_seri_no =(select cont_code from app_contract_info where cont_type = ?2 ) and apply_tnr = ?3 and has_com_repay=?4 ", nativeQuery = true)
	ContractAssInfo findByHasComRepayOne(@Param("payMtd") String payMtd, @Param("typLevelTwo") String typLevelTwo,
                                         @Param("applyTnr") String applyTnr, @Param("hasComRepay") String hasComRepay);

	@Query(value = " select * from app_contract_association_info where (mtd_code= ?1 or mtd_code is null) and contract_seri_no =(select cont_code from app_contract_info where cont_type = ?2) ", nativeQuery = true)
	List<ContractAssInfo> findByPayMtdAndTypLevelTwo(@Param("payMtd") String payMtd,
                                                     @Param("typLevelTwo") String typLevelTwo);

	// @Query(value="select * from app_contract_association_info where apply_tnr
	// = ?2 and contract_seri_no =(select cont_code from app_contract_info where
	// cont_type = ?1 and apply_type like %?3%)",nativeQuery=true)
	// ContractAssInfo findByPayMtdAndApplyTnrAndTypCde(@Param("payMtd") String
	// payMtd,@Param("applyTnr") String applyTnr,@Param("typCde") String
	// typCde);
	//
	//
	// @Query(value="select * from app_contract_association_info where apply_tnr
	// = ?2 and contract_seri_no =(select cont_code from app_contract_info where
	// cont_type = ?1)",nativeQuery=true)
	// ContractAssInfo findByPayMtdAndApplyTnr(@Param("payMtd") String
	// payMtd,@Param("applyTnr") String applyTnr);

}
