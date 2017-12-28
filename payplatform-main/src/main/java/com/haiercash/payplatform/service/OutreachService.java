package com.haiercash.payplatform.service;

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
    Map<String, Object> protocolauth(Map<String, Object> params);
}
