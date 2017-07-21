package com.haiercash.common.data;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

public interface AppPushRepository extends PagingAndSortingRepository<AppPush, String> {

	/* 推送成功或失败，修改推送状态 */
	@Modifying
	@Query(value = "update app_xinge_push u set u.push_time = ?2 , u.state = ?3  where id = ?1 ", nativeQuery = true)
	@Transactional
	public int updatePushInfo(@Param(value = "id") String id, @Param(value = "pushTime") String pushTime,
                              @Param(value = "state") String state);

	/* 更改推送次数 */
	@Modifying
	@Query(value = "update app_xinge_push  set times = (times + 1)  where id = ?1 ", nativeQuery = true)
	@Transactional
	public int updatePushTimes(@Param(value = "id") String id);
}
