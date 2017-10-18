package com.haiercash.payplatform.pc.cashloan.service;

import com.haiercash.payplatform.common.data.EntrySetting;
import com.haiercash.payplatform.rest.IResponse;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;

/**
 * Created by 许崇雷 on 2017-10-10.
 */
public interface CashLoanService {
    ModelAndView getActivityUrl();

    Map<String, Object> joinActivity();

    /**
     * 根据channelNo 获取贷款种类,不受配置影响
     *
     * @param channelNo
     * @return 贷款种类列表
     */
    IResponse<List<String>> getLoanTypeByChannelNo(String channelNo);

    /**
     * 根据 姓名,证件 获取贷款种类,不受配置影响
     *
     * @param custName 姓名
     * @param idType   证件类型 20 身份证,00 手机号
     * @param idNo     身份证或手机号
     * @return 贷款种类列表
     */
    IResponse<List<String>> getLoanTypeByCustInfo(String custName, String idType, String idNo);

    /**
     * 根据配置和参数查询贷款种类
     *
     * @param setting   渠道配置可以为 null
     * @param channelNo 渠道号
     * @param custName  姓名
     * @param idType    证件类型 20 身份证,00 手机号
     * @param idNo      身份证或手机号
     * @return 贷款种类列表
     */
    IResponse<List<String>> getLoanType(EntrySetting setting, String channelNo, String custName, String idType, String idNo);
}
