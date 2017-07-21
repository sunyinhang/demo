package com.haiercash.common.data;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MsgRequestRepository extends PagingAndSortingRepository<MsgRequest, String> {

    /**
     * 读取状态为isSend的短信记录.
     * @param isSend
     * @return
     */
    @Query(value = "select * from msg_send_request where is_send = ?1", nativeQuery = true)
    List<MsgRequest> findByIsSend(String isSend);

    @Query(value = "select * from (select * from msg_send_request where user_id = ?1 and is_send = '2' order by " +
            "request_time desc ) where rownum = 1",
            nativeQuery = true)
    MsgRequest findByUserIdLimit1(String userId);

    @Query(value = "select * from (select * from msg_send_request where user_id = ?1 and is_send = '2' and type=?2 order by " +
            "request_time desc ) where rownum = 1",
            nativeQuery = true)
    MsgRequest findByUserIdAndTypeLimit1(String userId, String type);

    @Query(value = "select * from msg_send_request where is_send = ?1 and type = ?2 order by request_time", nativeQuery = true)
    List<MsgRequest> findByIsSendAndTypeOrderByRequestTime(String isSend, String type);

    List<MsgRequest> findByApplSeq(String applSeq);

    @Query(value = "select * from MSG_SEND_REQUEST where to_char(REQUEST_TIME,'yyyy-mm-dd') = to_char(SYSDATE," +
            "'yyyy-mm-dd') and type=?1", nativeQuery = true)
    List<MsgRequest> findByDayAndType(String type);
}
