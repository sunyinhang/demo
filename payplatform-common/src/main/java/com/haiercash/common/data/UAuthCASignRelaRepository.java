package com.haiercash.common.data;

import org.springframework.data.repository.PagingAndSortingRepository;

public interface UAuthCASignRelaRepository extends PagingAndSortingRepository<UAuthCASignRela, String> {

	// @Query(value="select * from v_uauth_ca_sign_relation where sign_type =
	// ?1",nativeQuery=true)
	// UAuthCASignRela findBySignType(String signType);
}
