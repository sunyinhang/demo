package com.haiercash.appserver.service;

import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Ch Service impl.
 * @author Liu qingxiang
 * @since v1.2.0
 */
@Service
public interface ChService {

    /**
     * 合同提交.
     * @param custNo
     * @param applSeq 流水号
     * @param verifiCode 验证码
     * @param flag 订单flag
     * @param token
     * @param expectCredit
     * @return
     */
    Map<String, Object> subSignContract(String custNo, String applSeq, String verifiCode,
            String flag, String token,String expectCredit);

    //6.2.9.	-(GET)查询全部贷款信息列表-个人版
    Map<String, Object> getInfoForVip(String crtUsr,String idNo,String channelNo);

}
