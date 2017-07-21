package com.haiercash.common.data;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;

public interface UAuthUserTokenRepository extends PagingAndSortingRepository<UAuthUserToken, String> {
	UAuthUserToken findByClientId(@Param("clientId") String clientId);

	@Modifying
	@Query("update UAuthUserToken set userId = null where clientId = :clientId")
	@Transactional
	void clearUserIdByClientId(@Param("clientId") String clientId);

	List<UAuthUserToken> findByUserId(@Param("userId") String userId);
}
