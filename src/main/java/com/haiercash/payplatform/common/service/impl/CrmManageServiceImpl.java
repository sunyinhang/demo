package com.haiercash.payplatform.common.service.impl;

import com.haiercash.commons.redis.Session;
import com.haiercash.payplatform.common.config.EurekaServer;
import com.haiercash.payplatform.common.service.CrmManageService;
import com.haiercash.payplatform.common.utils.HttpUtil;
import com.haiercash.payplatform.service.BaseService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Created by use on 2017/8/29.
 */
@Service
public class CrmManageServiceImpl  extends BaseService implements CrmManageService {
    public Log logger = LogFactory.getLog(getClass());
    @Autowired
    private Session session;

    @Override
    public Map<String, Object> getCustTag(String token, Map<String, Object> paramMap) {
        String url = EurekaServer.CRM + "/app/crm/cust/getCustTag";
        logger.info("查询客户标签列表,请求地址："+url);
        logger.info("查询客户标签列表, 请求数据：" + paramMap);
        Map<String, Object> map = HttpUtil.restGetMap(url, token, paramMap);
        return map;
    }

    @Override
    public Map<String, Object> setCustTag(String token, Map<String, Object> paramMap) {
        String url = EurekaServer.CRM + "/app/crm/cust/setCustTag";
        logger.info("查询客户标签列表,请求地址："+url);
        logger.info("查询客户标签列表, 请求数据：" + paramMap);
        Map<String, Object> map = HttpUtil.restGetMap(url, token, paramMap);
        return map;
    }
}
