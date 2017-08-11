package com.haiercash.payplatform.common.service;

import java.util.Map;

/**
 * cmisproxy service.
 * @author Liu qingxiang
 * @since v1.0.1
 */
public interface CmisService {

    /**
     * 查询贷款品种详情
     *
     * @param typCde
     * @return
     */
    Map<String, Object> findPLoanTyp(String typCde);

}
