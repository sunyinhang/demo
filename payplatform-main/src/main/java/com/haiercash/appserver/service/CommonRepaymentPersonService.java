package com.haiercash.appserver.service;

import com.haiercash.common.data.CommonRepaymentPerson;
import org.springframework.stereotype.Service;

import java.util.Map;


/**
 * Created by yinjun on 2016/7/28.
 */
@Service
public interface CommonRepaymentPersonService {

    /**
     * 新增共同还款人service.
     *
     * @param commonRepaymentPerson
     * @param tradeType 1:新增 2:修改
     * @return
     */
    Map<String, Object> addCommonRepaymentPerson(CommonRepaymentPerson commonRepaymentPerson, String tradeType);

    Map<String, Object> getCommonRepaymentPerson(String applSeq);

    /**
     * 删除共同还款人service.
     * @param applSeq
     * @return
     */
    Map<String, Object> deleteCommonRepaymentPerson(String applSeq);

    /**
     * 查询共同还款人数量service.
     * @return
     */
    Map<String, Object> countCommonRepaymentPerson(String applSeq);

    /**
     * 更新共同还款人service.
     * @param commonRepaymentPerson
     * @return
     */
    Map<String, Object> updateCommonRepaymentPerson(CommonRepaymentPerson commonRepaymentPerson);
}
