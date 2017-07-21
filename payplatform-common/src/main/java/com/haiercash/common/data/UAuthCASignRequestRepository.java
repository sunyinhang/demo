package com.haiercash.common.data;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UAuthCASignRequestRepository extends PagingAndSortingRepository<UAuthCASignRequest, String> {
    @Query("select o from UAuthCASignRequest o where orderNo = :orderNo and signType=:signType and state = :state and commonFlag = :commonFlag")
    UAuthCASignRequest findByOrderAndType(@Param("orderNo") String orderNo,
                                          @Param("signType") String signType, @Param("state") String state, @Param("commonFlag") String commonFlag);

    @Query("select o from UAuthCASignRequest o where orderNo = :orderNo and state = :state")
    UAuthCASignRequest findByOrderNo(@Param("orderNo") String orderNo, @Param("state") String state);

    @Query(value = "select * from uauth_ca_sign_request  where applseq = ?1 order by submit_date desc", nativeQuery = true)
    List<UAuthCASignRequest> findByApplseq(@Param("applseq") String applseq);

    @Query("select o from UAuthCASignRequest o where signCode = :signCode")
    UAuthCASignRequest findBySignCode(@Param("signCode") String signCode);

    @Query(value = "select * from uauth_ca_sign_request where applseq = ?1 and sign_type = ?2 and state <> '2' and common_flag='0'", nativeQuery = true)
    UAuthCASignRequest findByApplseqAndSignType(@Param("applseq") String applseq, @Param("signType") String signType);

    @Query(value = "select * from uauth_ca_sign_request  where applseq = ?1  and state = '0' ", nativeQuery = true)
    List<UAuthCASignRequest> findArgeementByApplseq(@Param("applseq") String applseq);

    @Query(value = "select * from uauth_ca_sign_request  where applseq = ?1  and common_cust_no = ?2	and sign_type='common' and state <> '2' and common_flag = '0' ", nativeQuery = true)
    UAuthCASignRequest findCommonInfo(@Param("applseq") String applseq, @Param("commonCustNo") String commonCustNo);

    @Query(value = "select * from uauth_ca_sign_request  where applseq = ?1  and common_cust_no = ?2	and sign_type='credit' and state <> '2' and common_flag = '1' ", nativeQuery = true)
    UAuthCASignRequest findCommonCreditInfo(@Param("applseq") String applseq,
                                            @Param("commonCustNo") String commonCustNo);

    // 查询未签章以及签章失败需要重新签章的数据.
    @Query(" from UAuthCASignRequest where state = '0' or state = '3'")
    List<UAuthCASignRequest> findUncompletedRequest();

    @Query(value = "SELECT * FROM UAUTH_CA_SIGN_REQUEST WHERE TO_NUMBER (SYSDATE - SIGN_DATE) * 24 <= ?1 and STATE='2' ", nativeQuery = true)
    List<UAuthCASignRequest> findToResignRequest(@Param("resignCheckHour") int resignCheckHour);
}
