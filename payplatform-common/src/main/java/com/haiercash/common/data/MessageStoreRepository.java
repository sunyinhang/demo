package com.haiercash.common.data;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface MessageStoreRepository extends CrudRepository<MessageStore, String> {

	@Query(value = " select * from (select b.*,rownum rownumber from (select * from message_store where user_id=?1  order by pull_date desc) b  where rownum <= ?2*?3 ) c where c.rownumber>(?2-1)*?3", nativeQuery = true)
	List<MessageStore> getMessageListCust(String userId, int page, int pageNum);

	@Query(value = " select * from (select b.*,rownum rownumber from (select * from message_store where usr_cde=?1  order by pull_date desc) b  where rownum <= ?2*?3 ) c where c.rownumber>(?2-1)*?3", nativeQuery = true)
	List<MessageStore> getMessageList(String userId, int page, int pageNum);

	@Modifying
	@Query(value = " update message_store  set flag = 'Y' where id = ?1 ", nativeQuery = true)
	@Transactional
	int updateMsgIsRead(String msgId);

	@Query(value = "select * from message_store where msg_typ = '42' and channel = '16' and is_send = ?1", nativeQuery = true)
	List<MessageStore> findByIsSend(String isSend);

	@Query(value = "from MessageStore where outplat = ?1 and channelNo is not null and channelNo!='0'")
	List<MessageStore> getMsgByOutplat(String outplat);
}
