package com.haiercash.payplatform.common.service.impl;

import com.haiercash.payplatform.common.config.EurekaServer;
import com.haiercash.payplatform.common.service.CrmService;
import com.haiercash.payplatform.common.utils.ConstUtil;
import com.haiercash.payplatform.common.utils.FormatUtil;
import com.haiercash.payplatform.common.utils.HttpUtil;
import com.haiercash.payplatform.common.utils.RestUtil;
import com.haiercash.payplatform.service.BaseService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * crm service impl.
 * @author Liu qingxiang
 * @since v1.0.1
 */
@Service
public class CrmServiceImpl extends BaseService implements CrmService{

    @Override
    public Map<String, Object> queryPerCustInfoByUserId(String userId) {
        if (StringUtils.isEmpty("userId")) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "用户登录用户名不能为空");
        }

        return HttpUtil.restGetMap(EurekaServer.CRM + "/app/crm/cust/queryPerCustInfo?userId=" + userId, HttpStatus.OK.value());
    }


    @Override
    public Map<String, Object> getCustIsPass(Map<String, Object> params) {
        String url = EurekaServer.CRM + "/app/crm/cust/getCustIsPass";
        String paramUrl = FormatUtil.putParam2Url(url, params);
        logger.debug("CRM28 getCustIsPass ==> " + paramUrl);
        String resultJson = HttpUtil.restGet(paramUrl);
        logger.debug("CRM28 getCustIsPass <== " + resultJson);
        return HttpUtil.json2DeepMap(resultJson);
    }

    /**
     * 获取销售代表
     *
     * @param userId
     * @return
     */
    @Override
    public Map<String, Object> getStoreSaleByUserId(String userId) {
        if (StringUtils.isEmpty(userId))
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "用户编码不能为空");
        String checkSalerUrl = EurekaServer.CRM + "/app/crm/cust/getStoreSaleByUserId?userId=" + userId;
        logger.debug("CRM getStoreSaleByUserId  userId:" + userId);
        return HttpUtil.restGetMap(checkSalerUrl);
    }


    @Override
    public Map<String, Object> queryMerchCustInfo(String custName, String certNo) {
        String url = EurekaServer.CRM + "/app/crm/cust/queryMerchCustInfo?custName=" + custName + "&certNo=" + certNo;
        String jsonStr = HttpUtil.restGet(url);
        logger.debug("CRM13 queryMerchCustInfo:" + jsonStr);
        if (StringUtils.isEmpty(jsonStr)) {
            logger.error("CRM13 查询实名认证客户信息失败！");
            return fail("52", RestUtil.ERROR_INTERNAL_MSG);
        }
        return HttpUtil.json2DeepMap(jsonStr);
    }

}
