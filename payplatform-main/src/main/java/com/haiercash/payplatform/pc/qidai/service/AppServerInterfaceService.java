package com.haiercash.payplatform.pc.qidai.service;

import com.haiercash.spring.rest.IResponse;

import java.util.Map;

/**
 * @author 'zn'
 * @Description:APP后台接口
 * @date 2017年2月15日 上午10:46:44
 */
public interface AppServerInterfaceService {
    //10.1.(GET) PDF签章
    IResponse<Map> signPDFResult(Map<String, Object> paramMap, String channel, String channelNo, String token) throws Exception;
}
