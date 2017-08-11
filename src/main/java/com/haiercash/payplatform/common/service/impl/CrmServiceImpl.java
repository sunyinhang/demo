package com.haiercash.payplatform.common.service.impl;

import com.haiercash.payplatform.common.config.EurekaServer;
import com.haiercash.payplatform.common.service.CrmService;
import com.haiercash.payplatform.common.utils.ConstUtil;
import com.haiercash.payplatform.common.utils.FormatUtil;
import com.haiercash.payplatform.common.utils.HttpUtil;
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



}
