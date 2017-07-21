package com.haiercash.appserver.util.sign;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

public interface FileSignAgreementRepository extends PagingAndSortingRepository<FileSignAgreement, String> {

	// @Query(value="select * from uauth_ca_sign_agreement where temp_seri_num
	// =(select temp_seri_num from v_uauth_ca_sign_relation where sign_type =
	// ?1)", nativeQuery=true)
	@Query(value = "select * from uauth_ca_sign_agreement where temp_seri_num = ?1 ", nativeQuery = true)
	FileSignAgreement findBySignType(@Param("signType") String signType);

}
