package com.haiercash.payplatform.service;

import com.haiercash.payplatform.common.data.AppOrder;
import com.haiercash.spring.rest.IResponse;

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
    Map<String, Object> getCode(String token, Map<String, Object> citymap);

    /*
    查询贷款用途
     */
    Map<String, Object> getPurpose(Map<String, Object> params);

    /*
    实名认证（外联实名验证）
     */
    Map<String, Object> identity(Map<String, Object> params) throws Exception;

    /*
    解密
     */
    String decryptData(String data, String channelNo) throws Exception;

    /**
     * @Title queryApplReraidPlanByloanNo
     * @Description: 还款计划查询
     * @author yu jianwei
     * @date 2017/11/6 13:49
     */
    Map<String, Object> queryApplReraidPlanByloanNo(Map<String, Object> params) throws Exception;

    /**
     * 预约
     *
     * @param phone
     * @param name
     * @param education
     * @return
     */
    IResponse appointment(String phone, String name, String education);
}
