package com.haiercash.payplatform.service.impl;

import com.haiercash.payplatform.config.AppOtherConfig;
import com.haiercash.payplatform.service.OutreachService;
import com.haiercash.spring.rest.client.JsonClientUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @Description: 外联接口
 */
@Service
public class OutreachServiceImpl implements OutreachService {
    @Autowired
    private AppOtherConfig appOtherConfig;

    @Override
    public Map<String, Object> protocolauth(Map<String, Object> params) {
        String url = appOtherConfig.getOutplatform_url() + "/Outreachplatform/api/alibaba/protocolauth";
        return JsonClientUtils.postForMap(url, params);
    }
}
