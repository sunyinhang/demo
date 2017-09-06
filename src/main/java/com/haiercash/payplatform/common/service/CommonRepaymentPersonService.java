package com.haiercash.payplatform.common.service;

import com.haiercash.payplatform.common.data.CommonRepaymentPerson;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Created by yinjun on 2016/7/28.
 */
@Service
public interface CommonRepaymentPersonService {

    Map<String, Object> getCommonRepaymentPerson(String applSeq);

    /**
     * 删除共同还款人service.
     * @param applSeq
     * @return
     */
    Map<String, Object> deleteCommonRepaymentPerson(String applSeq);

}
