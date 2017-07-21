package com.haiercash.common.data;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface LoanTypeModelRepository extends PagingAndSortingRepository<LoanTypeModel, String> {
    @Query(value ="SELECT MODEL_NO FROM (SELECT DISTINCT MODEL_NO, REGEXP_SUBSTR (TYP_LEVEL_TWO,'[^,]+',1,LEVEL) AS TOKEN FROM LOAN_TYPE_MODEL  T CONNECT BY REGEXP_SUBSTR (TYP_LEVEL_TWO,'[^,]+',1,LEVEL) IS NOT NULL) T  WHERE  T.TOKEN =?1 and rownum=1 ",nativeQuery = true)
     String findByLoanType(String loanType);
}
