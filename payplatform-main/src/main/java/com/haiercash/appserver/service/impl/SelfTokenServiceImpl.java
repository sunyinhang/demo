package com.haiercash.appserver.service.impl;

import com.haiercash.appserver.service.SelfTokenService;
import com.haiercash.commons.util.EncryptUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * self token service impl.
 * @author Liu qingxiang
 * @since v1.6.0
 */
@Service
public class SelfTokenServiceImpl implements SelfTokenService{

    @Autowired
    private AuthorizationServerTokenServices tokenServices;

    @Override
    public OAuth2AccessToken createAccessToken(String clientId) {
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("role_client");
        Set<String> scope = new HashSet<>();
        scope.add("read");
        scope.add("write");
        Set<GrantedAuthority>  authorities = new HashSet<>();
        authorities.add(authority);
        OAuth2Request request = new OAuth2Request(null, EncryptUtil.simpleEncrypt(clientId), authorities, true, scope, null, null, null, null);
        OAuth2Authentication oAuth2Authentication = new OAuth2Authentication(request, null);
        OAuth2AccessToken oAuth2AccessToken = tokenServices.createAccessToken(oAuth2Authentication);
        return oAuth2AccessToken;
    }
}
