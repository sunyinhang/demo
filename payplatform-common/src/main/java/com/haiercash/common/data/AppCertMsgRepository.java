package com.haiercash.common.data;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Created by yinjun on 2017/2/20.
 */
public interface AppCertMsgRepository extends PagingAndSortingRepository<AppCertMsg, String> {
    @Query(value = "from AppCertMsg where certNo=?1 and deleteFlag='0'")
    AppCertMsg getCardMsgByCertNo(String certNo);
}
