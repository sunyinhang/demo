package com.haiercash.payplatform.service.impl;

import com.haiercash.payplatform.config.EurekaServer;
import com.haiercash.payplatform.rest.IResponse;
import com.haiercash.payplatform.rest.common.CommonResponse;
import com.haiercash.payplatform.rest.common.CommonRestUtil;
import com.haiercash.payplatform.service.BaseService;
import com.haiercash.payplatform.service.CrmService;
import com.haiercash.payplatform.utils.AppServerUtils;
import com.haiercash.payplatform.utils.ConstUtil;
import com.haiercash.payplatform.utils.FormatUtil;
import com.haiercash.payplatform.utils.HttpUtil;
import com.haiercash.payplatform.utils.RestUtil;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * crm service impl.
 *
 * @author Liu qingxiang
 * @since v1.0.1
 */
@Service
public class CrmServiceImpl extends BaseService implements CrmService {

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

    @Override
    public IResponse<Map> validateUsers(String userId, String password) {
        if (StringUtils.isEmpty(userId))
            return CommonResponse.create(ConstUtil.ERROR_PARAM_INVALID_CODE, "用户账号不能为空");
        if (StringUtils.isEmpty(password))
            return CommonResponse.create(ConstUtil.ERROR_PARAM_INVALID_CODE, "用户密码不能为空");
//        String url = EurekaServer.UAUTH + "/app/uauth/validateUsers?userId=" + userId + "&password=" + password;
        String url = AppServerUtils.getAppServerUrl() + "/app/appserver/customerLogin";
        Map paramMap = new HashMap<String, Object>();
        paramMap.put("channel", getChannel());
        paramMap.put("channelNo", getChannelNo());
        paramMap.put("userId", userId);
        paramMap.put("password", password);
        paramMap.put("deviceType", "H5");
        paramMap.put("type", "login");
        IResponse<Map> response = CommonRestUtil.putForMap(url, paramMap);
        logger.debug("App validateUsers :" + response);
        if (StringUtils.isEmpty(response)) {
            logger.error("登录验证信息失败！");
            return CommonResponse.create(RestUtil.ERROR_INTERNAL_CODE, RestUtil.ERROR_INTERNAL_MSG);
        }
        return response;
    }

    @Override
    public  Map<String, Object>  getBankCard(String custNo) {
        if (StringUtils.isEmpty(custNo))
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "客户编号为空!");
        String url = EurekaServer.CRM + "/app/crm/cust/getBankCard?custNo="+custNo;
        Map<String, Object> resultmap = HttpUtil.restGetMap(url);
        logger.info("获取客户银行卡接口，返回数据" + resultmap);
        return resultmap;
    }
}
