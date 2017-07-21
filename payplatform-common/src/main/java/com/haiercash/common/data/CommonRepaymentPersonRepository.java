package com.haiercash.common.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CommonRepaymentPersonRepository extends JpaRepository<CommonRepaymentPerson, String> {

	@Query(value = "select count(1) from common_repayment_person where appl_seq = (select applSeq from app_order where order_no = ?1) ", nativeQuery = true)
	int countCommonRepaymentPerson(@Param("orderNo") String orderNo);

	@Query("from CommonRepaymentPerson where orderNo = ?1  and commonCustNo = ?2")
	CommonRepaymentPerson getCommonRepaymentPersonInfo(@Param("orderNo") String orderNo,
                                                       @Param("commonCustNo") String commonCustNo);

	CommonRepaymentPerson findByOrderNo(String orderNo);//一个订单只有一个共同还款人

	List<CommonRepaymentPerson> findByApplSeq(String applSeq);

	/**
	 * 根据订单号删除共同还款人
	 * 
	 * @param orderNo
	 */
	@Modifying
	@Query(value = "delete from Common_Repayment_Person t where t.order_no=?1 ", nativeQuery = true)
	@Transactional
	void deleteByOrderNo(String orderNo);

	@Modifying
	@Query(value = "delete from CommonRepaymentPerson where applSeq = :applSeq")
	@Transactional
	void deleteByApplSeq(@Param("applSeq") String applSeq);
}
