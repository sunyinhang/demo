package com.haiercash.payplatform.service;

import com.haiercash.payplatform.common.data.AppOrder;
import com.haiercash.payplatform.common.data.CommonRepaymentPerson;

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

     /**
     * 对从crm查询到的客户扩展信息中的地址进行预处理
     *
     * @param crmBodyMap
     */
    void dealAddress(Map<String, Object> crmBodyMap);

    /**
     * 渠道进件前，处理订单的送货地址
     *
     * @param order      要处理的订单
     * @param crmBodyMap crm查询出来的信息
     */
    void dealDeliverAddress(AppOrder order, Map<String, Object> crmBodyMap);


    /**
     * 电话号码处理 返回：Map<String,Object> zone tel
     * 处理逻辑：
     * 1、 将 横（-）全部替换
     * 2、如果不是以0开头，则不进行拆分，
     * 3、如果是以0开头，判断几个直辖市 如果为几个直辖市，则取前3位为区号，后面的为号码
     * 4、如果不是以0开头的，则说明不带区号，原封返回
     */
    Map<String, String> getPhoneNoAndZone(String phoneNo);

    /**
     * 封装共同还款人
     *
     * @param custNo
     * @return
     */
    Map<String, Object> getCommonPayPersonMap(String custNo, String source, String typGrp,
                                              CommonRepaymentPerson person, String version);



}
