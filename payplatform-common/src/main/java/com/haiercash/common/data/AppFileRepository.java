package com.haiercash.common.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

public interface AppFileRepository extends JpaRepository<AppFile, Long> {
	/** 获取所有的app版本（根据大版本、中版本、小版本排序） **/
	@Query(" from AppFile order by version_big desc,version_middle desc,version_small desc")
	List<AppFile> getAllAppFileOrderByVersion();

	/** 每下载一次app，下载次数加1 **/
	@Modifying
	@Query(value = "update APP_FILE_VERSION o set o.download_count=nvl(download_count,0)+1  where o.id =?1", nativeQuery = true)
	@Transactional
	void updateCount(long id);

	/** 查询下载总数 **/
	@Query("select count(download_count)  from AppFile")
	int getAllDownloadCount();

	/** 查询比当前版本新的版本列表 **/
	@Query(" from AppFile where lpad(version_big, 3,'0')||lpad(version_middle, 3,'0')||lpad(version_small, 3,'0')>?1 order by version_big desc,version_middle desc,version_small desc")
	ArrayList<AppFile> getAppFilesByVersion(String version);

}
