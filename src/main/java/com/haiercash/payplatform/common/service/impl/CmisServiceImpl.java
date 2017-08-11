package com.haiercash.payplatform.common.service.impl;

import com.haiercash.payplatform.common.config.EurekaServer;
import com.haiercash.payplatform.common.service.CmisService;
import com.haiercash.payplatform.common.utils.ConstUtil;
import com.haiercash.payplatform.common.utils.HttpUtil;
import com.haiercash.payplatform.service.BaseService;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * cmis service impl.
 * @author Liu qingxiang
 * @since v1.0.1
 */
public class CmisServiceImpl extends BaseService implements CmisService{


    public Map<String, Object> findPLoanTyp(String typCde) {
        if (StringUtils.isEmpty(typCde)) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "贷款品种不能未空");
        }
        String typCdeUrl = EurekaServer.CMISPROXY + "/api/pLoanTyp/find?typCde=" + typCde;
        logger.info("==> CMISPROXY:" + typCdeUrl);
        String typCdeJson = HttpUtil.restGet(typCdeUrl);
        logger.info("<== CMISPROXY:" + typCdeJson);
        if (StringUtils.isEmpty(typCdeJson)) {
            return fail("75", "无效的贷款品种");
        }
        return success(HttpUtil.json2DeepMap(typCdeJson));
    }
}
