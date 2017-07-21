package com.haiercash.appserver.service;

import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;


public interface PaymentService {

    /**
     * 付款码申请
     *
     * @param userId
     * @param useType
     * @param merPrivate
     * @param reserveData
     * @return
     */
    public Map<String, Object> payCodeAppl(@RequestParam String userId, @RequestParam String useType, String merPrivate, String reserveData);
}





