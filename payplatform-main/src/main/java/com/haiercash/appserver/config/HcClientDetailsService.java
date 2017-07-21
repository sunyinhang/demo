package com.haiercash.appserver.config;

import com.haiercash.common.data.UAuthUserToken;
import com.haiercash.common.data.UAuthUserTokenRepository;
import com.haiercash.commons.util.EncryptUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
import org.springframework.stereotype.Service;


/**
 * 客户端信息验证功能类 Created by Liuhongbin on 2016/4/7.
 */
@Service
class HcClientDetailsService implements ClientDetailsService {
	private Log logger = LogFactory.getLog(this.getClass());

	private String HC_RESOURCE_ID = "haiercash";
	private Integer ACCESS_TOKEN_VALIDITY_SECONDS = 60 * 60 * 24;// 60 * 10

	@Autowired
	UAuthUserTokenRepository UAuthUserTokenRepository;

	@Override
	public ClientDetails loadClientByClientId(String s) throws ClientRegistrationException {
		// 根据传入的client_id查找用户信息
		logger.debug("loadClientByClientId: " + s);
		// 数据解密
		String clientId = EncryptUtil.simpleDecrypt(s);
		logger.debug("loadClientByClientId: " + clientId);
		UAuthUserToken userToken = UAuthUserTokenRepository.findByClientId(clientId);
		if (userToken == null) {
			logger.debug("未找到对应的Client信息: " + s);
			return null;
		}
		logger.debug("ClientSecret: " + userToken.getClientSecret());

		BaseClientDetails clientDetails;
		String scopes = "read,write";// ,trust
		String grandTypes = "client_credentials,refresh_token";// ,authorization_code,implicit,password
		String authorities = "ROLE_CLIENT";// ,ROLE_USER
		clientDetails = new BaseClientDetails(s, HC_RESOURCE_ID, scopes, grandTypes, authorities);
		clientDetails.setClientSecret(userToken.getClientSecret());
		clientDetails.setAccessTokenValiditySeconds(ACCESS_TOKEN_VALIDITY_SECONDS);
		return clientDetails;
	}
}
