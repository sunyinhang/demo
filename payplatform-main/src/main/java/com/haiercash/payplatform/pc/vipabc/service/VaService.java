package com.haiercash.payplatform.pc.vipabc.service;

import org.apache.catalina.servlet4preview.http.HttpServletRequest;

import java.util.Map;

/**
 * Created by Administrator on 2017/12/25.
 */
public interface VaService {

    Map<String, Object> queryAppLoanAndGood(String token, String channel, String channelNo, Map<String, Object> map) throws Exception;

    Map<String, Object> vipAbcDeleteOrderInfo(HttpServletRequest request, String token, String channel, String channelNo, Map<String, Object> map) throws Exception;

}
