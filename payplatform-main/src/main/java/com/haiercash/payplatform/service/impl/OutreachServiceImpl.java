package com.haiercash.payplatform.service.impl;

import com.haiercash.payplatform.service.OutreachService;
import com.haiercash.spring.eureka.EurekaServer;
import com.haiercash.spring.rest.IResponse;
import com.haiercash.spring.rest.common.CommonRestUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @Description: 外联接口
 */
@Service
public class OutreachServiceImpl implements OutreachService {
    /**
     * 授权
     *
     * @param params
     * @return
     */
    @Override
    public IResponse<Map> protocolauth(Map<String, Object> params) {
        String url = EurekaServer.OUTREACHPLATFORM + "/Outreachplatform/api/alibaba/protocolauth";
        return CommonRestUtils.postForMap(url, params);
    }

    /**
     * 获取芝麻分
     *
     * @param params
     * @return
     */
    @Override
    public IResponse<Map> score(Map<String, Object> params) {
        String url = EurekaServer.OUTREACHPLATFORM + "/Outreachplatform/api/alibaba/score";
        return CommonRestUtils.postForMap(url, params);
    }
}
