package com.haiercash.common.data;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface AttachFileRepository extends CrudRepository<AttachFile, Long> {
    List<AttachFile> findByOrderNo(@Param("orderNo") String orderNo);

    @Query(" from AttachFile where applSeq=?1 order by attachType,id")
    List<AttachFile> findByApplSeq(@Param("applSeq") String applSeq);

    @Query(" from AttachFile where applSeq=?1 and commonCustNo is null order by attachType,id")
    List<AttachFile> findByApplSeqExcludeCommon(@Param("applSeq") String applSeq);

    @Query(" from AttachFile where applSeq=?1 and attachType=?2 and commonCustNo is null order by id")
    List<AttachFile> findByApplSeqAndAttachType(@Param("applSeq") String applSeq,
                                                @Param("attachType") String attachType);

    @Query((" from AttachFile where applSeq=?1 and attachType=?2 and commonCustNo=?3 order by id"))
    List<AttachFile> comonFindByApplSeqAndAttachType(@Param("applSeq") String applSeq,
                                                     @Param("attachType") String attachType,
                                                     @Param("commonCustNo") String commonCustNo);

    @Query(" from AttachFile where custNo=?1 and attachType is not null order by attachType,id")
    List<AttachFile> findByCustNo(@Param("custNo") String custNo);

    @Query(" from AttachFile where (custNo=?1 or  applSeq=?1) and attachType is not null order by attachType,id")
    List<AttachFile> findByCustNo2(@Param("custNo") String custNo);

    @Query(" from AttachFile where custNo=?1 and attachType=?2 order by id")
    List<AttachFile> findByCustNoAndAttachType(@Param("custNo") String custNo, @Param("attachType") String attachType);

    @Query(" from AttachFile where (applSeq = :applSeq or (applSeq is null and custNo = :custNo)) and attachType = :attachType order by id")
    List<AttachFile> findByApplSeqOrCustNoAndAttachType(@Param("applSeq") String applSeq,
                                                        @Param("custNo") String custNo, @Param("attachType") String attachType);

    @Modifying
    @Query(value = "update APP_ATTACH_FILE set appl_seq = ?1 WHERE  appl_seq='0' and cust_no =?2  ", nativeQuery = true)
    @Transactional
    int updateAttachByApplseqAndCustno(String applSeq, String custNo);

    @Query(value = " select * from APP_ATTACH_FILE where appl_seq = ?1 and common_cust_no = ?2 order by attach_type,id ", nativeQuery = true)
    List<AttachFile> findByApplSeqAndCommonCustNo(@Param("applSeq") String applSeq, @Param("commonCustNo") String commonCustNo);

    @Query(value = " select * from APP_ATTACH_FILE  where cust_no= ?1 and (appl_seq= ?2 or appl_seq='0') order by attach_type,id", nativeQuery = true)
    List<AttachFile> findByCustNoAndApplSeq(String custNo, String applSeq);

    /**
     * 查询人脸照片
     * 为了和其他影像区分，cust_no字段存的是影像代码，客户编号保存在appl_seq字段中
     *
     * @param custNo
     * @param docType
     * @return
     */
    @Query(value = "select * from app_attach_file where cust_no = ?2 and appl_seq = ?1 order by id desc", nativeQuery = true)
    List<AttachFile> findFacePhoto(String custNo, String docType);

    /**
     * 提额影像列表按类型查询(个人版)
     *
     * @param custNo
     * @param attachType
     * @return
     */
    @Query(" from AttachFile where custNo=?1 and attachType=?2 and (applSeq= ?3 or applSeq='0') order by id")
    List<AttachFile> findByCustNoAndAttachType(String custNo, String attachType, String applSeq);

    @Query(" from AttachFile where custNo=?1 and attachType=?2 and applSeq= ?3  order by id")
    List<AttachFile> findByCustNoAndAttachTypeAndApplSeq(String custNo, String attachType, String applSeq);

    @Query(value = "SELECT DISTINCT ATTACH_TYPE,ATTACH_NAME FROM APP_ATTACH_FILE WHERE CUST_NO= ?1", nativeQuery = true)
    List<Object[]> findAllByCustNo(String custNo);

    @Query(" from AttachFile where commonCustNo=?1 and attachType=?2 and applSeq=?3 order by id")
    List<AttachFile> findByCommonAndApplseqAndAttachType(@Param("custNo") String custNo, @Param("attachType") String attachType, @Param("applSeq") String applSeq);

    @Query(" from AttachFile where custNo=?1 and attachType is not null and attachType in('DOC53','DOC54') order by attachType,id")
    List<AttachFile> findByCustNoAndIdType(@Param("custNo") String custNo);

    @Query(value = "select count(*) from (select distinct attach_type from app_attach_file where cust_no=?1 and attach_type is not null and attach_type in('DOC53','DOC54'))", nativeQuery = true)
    int findByCustNoAndDOC5354(@Param("custNo") String custNo);
}
