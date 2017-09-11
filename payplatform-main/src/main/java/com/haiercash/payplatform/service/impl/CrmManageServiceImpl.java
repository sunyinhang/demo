package com.haiercash.payplatform.service.impl;

import com.haiercash.commons.redis.Session;
import com.haiercash.payplatform.config.EurekaServer;
import com.haiercash.payplatform.service.CrmManageService;
import com.haiercash.payplatform.utils.HttpUtil;
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
        logger.info("查询客户标签列表, 返回数据：" + map);
        return map;
    }

    @Override
    public Map<String, Object> setCustTag(String token, Map<String, Object> paramMap) {
        String url = EurekaServer.CRM + "/app/crm/cust/setCustTag";
        logger.info("为指定客户增加指定的自定义标签,请求地址："+url);
        logger.info("为指定客户增加指定的自定义标签, 请求数据：" + paramMap);
        Map<String, Object> map = HttpUtil.restGetMap(url, token, paramMap);
        logger.info("为指定客户增加指定的自定义标签, 返回数据：" + paramMap);
        return map;
    }
}
