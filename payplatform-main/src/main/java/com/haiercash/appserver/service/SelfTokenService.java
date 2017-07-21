package com.haiercash.appserver.service;

import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.stereotype.Service;

/**
 * self token service interface.
 * @author Liu qingxiang
 * @since v1.6.0
 */
@Service
public interface SelfTokenService {

    /**
     * 创建accessToken.
     * @param clientId      用户id或设备号
     * @return OAuth2AccessToken
     */
    OAuth2AccessToken createAccessToken(String clientId);

}
