package com.haiercash.payplatform.pc.cashloan.service;

import com.haiercash.payplatform.common.data.EntrySetting;
import com.haiercash.payplatform.common.entity.LoanType;
import com.haiercash.spring.rest.IResponse;

import java.util.List;
import java.util.Map;

/**
 * Created by 许崇雷 on 2017-10-10.
 */
public interface CashLoanService {
    String getActivityUrl();

    Map<String, Object> joinActivity();

    /**
     * 根据channelNo 获取贷款种类,不受配置影响
     *
     * @return 贷款种类列表
     */
    IResponse<List<LoanType>> getLoanTypeByChannelNo();

    /**
     * 根据 姓名,证件 获取贷款种类,不受配置影响
     *
     * @param custName 姓名
     * @param idType   证件类型 20 身份证,00 手机号
     * @param idNo     身份证或手机号
     * @return 贷款种类列表
     */
    IResponse<List<LoanType>> getLoanTypeByCustInfo(String custName, String idType, String idNo);

    /**
     * 根据配置和参数查询贷款种类
     *
     * @param setting  渠道配置可以为 null
     * @param custName 姓名
     * @param idType   证件类型 20 身份证,00 手机号
     * @param idNo     身份证或手机号
     * @return 贷款种类列表
     */
    IResponse<List<LoanType>> getLoanType(EntrySetting setting, String custName, String idType, String idNo);

    /**
     * 现金贷贷款提交
     *
     * @param map
     * @return
     * @throws Exception
     */
    public Map<String, Object> commitOrder(Map<String, Object> map) throws Exception;

    /**
     * 现金贷订单保存
     *
     * @param map
     * @return
     */
    Map<String, Object> saveOrder(Map<String, Object> map);


    /**
     * 验证并绑定第三方（非海尔集团）用户
     *
     * @param map
     * @return
     */
    Map<String, Object> validateAndBindUserByExternUid(Map<String, Object> map);


}
