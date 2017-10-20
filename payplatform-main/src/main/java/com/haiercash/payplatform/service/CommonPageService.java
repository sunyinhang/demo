package com.haiercash.payplatform.service;

import com.haiercash.payplatform.common.data.AppOrder;

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

    /*
    合同签订
     */
    Map<String, Object> signContract(String custName, String custIdCode, String applseq, String phone, String typCde,
                                            String channelNo, String token);

    /*
    订单保存
     */
    Map<String, Object> saveAppOrderInfo(AppOrder appOrder);

    /*
    获取省市编码
     */
    String getCode(String token, Map<String, Object> citymap);
}
