package com.haiercash.payplatform.service;

import com.haiercash.spring.rest.IResponse;

import java.util.Map;

/**
 * Created by yu jianwei on 2017/11/22
 *
 * @Description:
 */
public interface OutreachService {
    /**
     * @Title protocolauth
     * @Description: 外联平台芝麻授权接口
     * @author yu jianwei
     * @date 2017/11/22 10:42
     */
    IResponse<Map> protocolauth(Map<String, Object> params);

    /**
     * 获取芝麻分
     *
     * @param params
     * @return
     */
    IResponse<Map> score(Map<String, Object> params);

    /**
     * 芝麻签章
     * @param params
     * @return
     */
    IResponse<Map> signature(Map<String, Object> params);
}
