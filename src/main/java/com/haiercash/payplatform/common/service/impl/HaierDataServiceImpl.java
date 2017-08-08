package com.haiercash.payplatform.common.service.impl;

import com.haiercash.commons.redis.Cache;
import com.haiercash.payplatform.common.service.HaierDataService;
import com.haiercash.payplatform.common.utils.HttpClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Created by yuanli on 2017/8/8.
 */
@Service
public class HaierDataServiceImpl implements HaierDataService{
    public Log logger = LogFactory.getLog(getClass());

    @Value("${app.other.haierData_url}")
    protected String haierData_url;

    @Override
    public JSONObject userinfo(String token) {
        String url = haierData_url + "/userinfo?access_token=" + token;
        logger.info("根据token验证用户信息，请求url:" + url);
        JSONObject jsonObj = HttpClient.sendGetUrl(url);
        logger.info("根据token验证用户信息，返回数据：" + jsonObj);
        return jsonObj;
    }
}
