package com.haiercash.payplatform.service;

import java.util.Map;

/**
 * Created by yuanli on 2017/9/20.
 */
public interface CommonPageService {
    /*
    合同展示
     */
    Map<String, Object> showcontract(Map<String, Object> map) throws Exception;

    /*
    订单提交
     */
    Map<String, Object> commitAppOrder(String orderNo, String applSeq, String opType, String msgCode, String expectCredit, String typGrp);

    /**
     * 合同签订
     * @param custName
     * @param custIdCode
     * @param applseq
     * @param phone
     * @param typCde
     * @param channelNo
     * @param token
     * @return
     */
    Map<String, Object> signContract(String custName, String custIdCode, String applseq, String phone, String typCde,
                                            String channelNo, String token);
}
