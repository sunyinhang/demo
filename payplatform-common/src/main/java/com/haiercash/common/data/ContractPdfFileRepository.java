package com.haiercash.common.data;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Created by Administrator on 2016/7/19.
 */
public interface ContractPdfFileRepository  extends PagingAndSortingRepository<ContractPdfFile, String>{

    @Query("from ContractPdfFile where applSeq = :applseq and flag = :flag and deleteFlag = '0' and rownum = 1")
    ContractPdfFile findByApplseqAndFlag(@Param("applseq") String applseq, @Param("flag") String flag);

    @Query("from ContractPdfFile where id = :id")
    ContractPdfFile findById(@Param("id") String id);

    @Query(value="select * from APP_CONTRACT_FILE where appl_seq = ?1  and flag = ?2 and common_flag = ?3  and delete_flag ='0' order by app_date desc" ,nativeQuery = true)
    List<ContractPdfFile> findAlreadyExistsContract(@Param("applseq") String applseq, @Param("flag") String flag, @Param("commonFlag") String commonFlag);

    @Query(value="select * from APP_CONTRACT_FILE where appl_seq = ?1  and flag = ?2 and common_flag = ?3  and sign_type = ?4 and delete_flag ='0' order by app_date desc" ,nativeQuery = true)
    List<ContractPdfFile> findAlreadyExistsAgreement(@Param("applseq") String applseq, @Param("flag") String flag, @Param("commonFlag") String commonFlag, @Param("signType") String signType);


}
