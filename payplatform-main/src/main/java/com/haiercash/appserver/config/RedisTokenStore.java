package com.haiercash.appserver.config;

import com.haiercash.common.data.UAuthUserTokenRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.AuthenticationKeyGenerator;
import org.springframework.security.oauth2.provider.token.DefaultAuthenticationKeyGenerator;
import org.springframework.security.oauth2.provider.token.TokenStore;

import com.haiercash.commons.util.EncryptUtil;
import com.haiercash.commons.util.RedisUtil;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * OAuth2服务实现redis存储token，支持集群能力 参考
 * @see org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore
 */
@Configurable(autowire = Autowire.BY_TYPE)
public class RedisTokenStore implements TokenStore {
	private static Log logger = LogFactory.getLog(RedisUtil.class);

	@Autowired
	UAuthUserTokenRepository uAuthUserTokenRepository;

	private static final byte[] userStore = RedisUtil.serialize("__token_app_userStore");
	private final byte[] accessTokenStore = RedisUtil.serialize("__token_app_accessTokenStore");
	private final byte[] authenticationToAccessTokenStore = RedisUtil
			.serialize("__token_app_authenticationToAccessTokenStore");
	private final byte[] userNameToAccessTokenStore = RedisUtil.serialize("__token_app_userNameToAccessTokenStore");
	private final byte[] clientIdToAccessTokenStore = RedisUtil.serialize("__token_app_clientIdToAccessTokenStore");
	private final byte[] refreshTokenStore = RedisUtil.serialize("__token_app_refreshTokenStore");
	private final byte[] accessTokenToRefreshTokenStore = RedisUtil
			.serialize("__token_app_accessTokenToRefreshTokenStore");
	private final byte[] authenticationStore = RedisUtil.serialize("__token_app_authenticationStore");
	private final byte[] refreshTokenAuthenticationStore = RedisUtil
			.serialize("__token_app_refreshTokenAuthenticationStore");
	private final byte[] refreshTokenToAccessTokenStore = RedisUtil
			.serialize("__token_app_refreshTokenToAccessTokenStore");
	private final String expiryQueue = "__token_app_expiryQueue";
	private long redisLockTimeOut = 60000;

	private static final int DEFAULT_FLUSH_INTERVAL = 1000;

	private AuthenticationKeyGenerator authenticationKeyGenerator = new DefaultAuthenticationKeyGenerator();
	private int flushInterval = DEFAULT_FLUSH_INTERVAL;
	private AtomicInteger flushCounter = new AtomicInteger(0);

	public RedisTokenStore() {

	}

	@Override
	public OAuth2Authentication readAuthentication(OAuth2AccessToken oAuth2AccessToken) {
		__debug("=====readAuthentication: %s", oAuth2AccessToken.getValue());
		return readAuthentication(oAuth2AccessToken.getValue());
	}

	@Override
	public OAuth2Authentication readAuthentication(String token) {
		__debug("=====readAuthentication: %s", token);
		return (OAuth2Authentication) RedisUtil
				.unserialize(RedisUtil.hget(authenticationStore, RedisUtil.serialize(token)));
	}

	@Override
	public void storeAccessToken(OAuth2AccessToken oAuth2AccessToken, OAuth2Authentication oAuth2Authentication) {
		__debug("=====storeAccessToken: %s", oAuth2AccessToken.getValue());
		if (this.flushCounter.incrementAndGet() >= this.flushInterval) {
			flush();
			this.flushCounter.set(0);
		}

		byte[] key = RedisUtil.serialize(oAuth2AccessToken.getValue());

		String clientId = oAuth2Authentication.getOAuth2Request().getClientId();
		clientId = EncryptUtil.simpleDecrypt(clientId);
		RedisUtil.hset(userStore, key, RedisUtil.serialize(getUserIdByClientId(clientId)));
		RedisUtil.hset(accessTokenStore, key, RedisUtil.serialize(oAuth2AccessToken));
		RedisUtil.hset(authenticationStore, key, RedisUtil.serialize(oAuth2Authentication));
		RedisUtil.hset(authenticationToAccessTokenStore,
				RedisUtil.serialize(authenticationKeyGenerator.extractKey(oAuth2Authentication)),
				RedisUtil.serialize(oAuth2AccessToken));
		if (!oAuth2Authentication.isClientOnly()) {
			addToCollection(userNameToAccessTokenStore, RedisUtil.serialize(getApprovalKey(oAuth2Authentication)),
					oAuth2AccessToken);
		}
		addToCollection(clientIdToAccessTokenStore, RedisUtil.serialize(clientId), oAuth2AccessToken);

		if (oAuth2AccessToken.getExpiration() != null) {
			TokenExpiry expiry = new TokenExpiry(oAuth2AccessToken.getValue(), oAuth2AccessToken.getExpiration());
			putExpiryQueue(expiry);
		}
		if (oAuth2AccessToken.getRefreshToken() != null && oAuth2AccessToken.getRefreshToken().getValue() != null) {
			RedisUtil.hset(refreshTokenToAccessTokenStore,
					RedisUtil.serialize(oAuth2AccessToken.getRefreshToken().getValue()), key);
			RedisUtil.hset(accessTokenToRefreshTokenStore, key,
					RedisUtil.serialize(oAuth2AccessToken.getRefreshToken().getValue()));
		}

/*		// 把用户账号和access_token关系写入缓存
		String userId = getUserIdByClientId(clientId);
		if (oAuth2AccessToken.getExpiration() != null) {
			RedisUtil.setExpireAt("_ACCESS_TOKEN_" + oAuth2AccessToken.getValue(), userId,
					oAuth2AccessToken.getExpiration().getTime());
		} else {
			RedisUtil.set("_ACCESS_TOKEN_" + oAuth2AccessToken.getValue(), userId);
		}*/
	}

	/**
	 * 通过access_token获取用户ID
	 * 
	 * @param token
	 * @return
	 */
	public static String getUserIdByToken(String token) {
		return (String) RedisUtil.unserialize(RedisUtil.hget(userStore, RedisUtil.serialize(token)));
	}

	private String getUserIdByClientId(String clientId) {
		try {
			return uAuthUserTokenRepository.findByClientId(clientId).getUserId();
		} catch (Exception e) {
			logger.error("通过ClientID查询用户ID失败：" + clientId + " ==> " + e.getMessage());
			return "";
		}
	}

	@Override
	public OAuth2AccessToken readAccessToken(String tokenValue) {
		__debug("=====readAccessToken: %s", tokenValue);
		return (OAuth2AccessToken) RedisUtil
				.unserialize(RedisUtil.hget(accessTokenStore, RedisUtil.serialize(tokenValue)));
	}

	@Override
	public void removeAccessToken(OAuth2AccessToken oAuth2AccessToken) {
		__debug("=====removeAccessToken: %s", oAuth2AccessToken.getValue());
		removeAccessToken(oAuth2AccessToken.getValue());
	}

	public void removeAccessToken(String tokenValue) {
		__debug("=====removeAccessToken: %s", tokenValue);
		byte[] key = RedisUtil.serialize(tokenValue);
		OAuth2AccessToken removed = (OAuth2AccessToken) RedisUtil.unserialize(RedisUtil.hget(accessTokenStore, key));
		RedisUtil.hmdel(accessTokenStore, key, new byte[0]);
		RedisUtil.hmdel(accessTokenToRefreshTokenStore, key, new byte[0]);

		OAuth2Authentication authentication = (OAuth2Authentication) RedisUtil
				.unserialize(RedisUtil.hget(authenticationStore, key));
		RedisUtil.hmdel(authenticationStore, key, new byte[0]);
		if (authentication != null) {
			RedisUtil.hmdel(authenticationToAccessTokenStore,
					RedisUtil.serialize(authenticationKeyGenerator.extractKey(authentication)), new byte[0]);
			Collection<OAuth2AccessToken> tokens;
			Jedis jedis = null;
			try {
				jedis = RedisUtil.getJedis();

				long start = System.nanoTime();
				byte[] authKey = RedisUtil.serialize(authentication.getName());
				while (redisLockTimeOut > 0 && (System.nanoTime() - start) / 1000000 < redisLockTimeOut) {
					jedis.watch(userNameToAccessTokenStore, new byte[0]);
					tokens = (Collection<OAuth2AccessToken>) RedisUtil
							.unserialize(jedis.hget(userNameToAccessTokenStore, authKey));
					if (tokens != null) {
						tokens.remove(removed);
					} else {
						break;
					}
					Transaction transaction = jedis.multi();
					transaction.hset(userNameToAccessTokenStore, authKey, RedisUtil.serialize(tokens));
					if (transaction.exec() != null) {
						break;
					}
					jedis.unwatch();
					Thread.sleep(10);
				}

				start = System.nanoTime();
				byte[] clientId = RedisUtil.serialize(authentication.getOAuth2Request().getClientId());
				while (redisLockTimeOut > 0 && (System.nanoTime() - start) / 1000000 < redisLockTimeOut) {
					jedis.watch(clientIdToAccessTokenStore, new byte[0]);
					tokens = (Collection<OAuth2AccessToken>) RedisUtil
							.unserialize(jedis.hget(clientIdToAccessTokenStore, clientId));
					if (tokens != null) {
						tokens.remove(removed);
					} else {
						break;
					}
					Transaction transaction = jedis.multi();
					transaction.hset(clientIdToAccessTokenStore, clientId, RedisUtil.serialize(tokens));
					if (transaction.exec() != null) {
						break;
					}
					jedis.unwatch();
					Thread.sleep(10);
				}

			} catch (Exception e) {
				logger.error("RedisTokenStore.removeAccessToken - " + e.getMessage());
			} finally {
				RedisUtil.returnResource(jedis);
			}
			RedisUtil.hmdel(authenticationToAccessTokenStore,
					RedisUtil.serialize(authenticationKeyGenerator.extractKey(authentication)), new byte[0]);
		}
	}

	@Override
	public void storeRefreshToken(OAuth2RefreshToken oAuth2RefreshToken, OAuth2Authentication oAuth2Authentication) {
		__debug("=====storeAccessToken: %s", oAuth2RefreshToken.getValue());
		byte[] key = RedisUtil.serialize(oAuth2RefreshToken.getValue());
		RedisUtil.hset(refreshTokenStore, key, RedisUtil.serialize(oAuth2RefreshToken));
		RedisUtil.hset(refreshTokenAuthenticationStore, key, RedisUtil.serialize(oAuth2Authentication));
	}

	@Override
	public OAuth2RefreshToken readRefreshToken(String tokenValue) {
		__debug("=====readRefreshToken: %s", tokenValue);
		return (OAuth2RefreshToken) RedisUtil
				.unserialize(RedisUtil.hget(refreshTokenStore, RedisUtil.serialize(tokenValue)));
	}

	@Override
	public OAuth2Authentication readAuthenticationForRefreshToken(OAuth2RefreshToken oAuth2RefreshToken) {
		__debug("=====readAuthenticationForRefreshToken: %s", oAuth2RefreshToken.getValue());
		return (OAuth2Authentication) RedisUtil.unserialize(
				RedisUtil.hget(refreshTokenAuthenticationStore, RedisUtil.serialize(oAuth2RefreshToken.getValue())));
	}

	@Override
	public void removeRefreshToken(OAuth2RefreshToken oAuth2RefreshToken) {
		__debug("=====removeRefreshToken: %s", oAuth2RefreshToken.getValue());
		byte[] key = RedisUtil.serialize(oAuth2RefreshToken.getValue());
		RedisUtil.hmdel(refreshTokenStore, key, new byte[0]);
		RedisUtil.hmdel(refreshTokenAuthenticationStore, key, new byte[0]);
		RedisUtil.hmdel(refreshTokenToAccessTokenStore, key, new byte[0]);
	}

	@Override
	public void removeAccessTokenUsingRefreshToken(OAuth2RefreshToken oAuth2RefreshToken) {
		__debug("=====removeAccessTokenUsingRefreshToken: %s", oAuth2RefreshToken.getValue());
		byte[] key = RedisUtil.serialize(oAuth2RefreshToken.getValue());
		String accessToken = (String) RedisUtil.unserialize(RedisUtil.hget(refreshTokenToAccessTokenStore, key));
		RedisUtil.hmdel(refreshTokenToAccessTokenStore, key, new byte[0]);
		if (accessToken != null) {
			removeAccessToken(accessToken);
		}
	}

	@Override
	public OAuth2AccessToken getAccessToken(OAuth2Authentication oAuth2Authentication) {
		__debug("=====getAccessToken: %s", oAuth2Authentication.toString());
		String key = authenticationKeyGenerator.extractKey(oAuth2Authentication);
		OAuth2AccessToken accessToken = (OAuth2AccessToken) RedisUtil
				.unserialize(RedisUtil.hget(authenticationToAccessTokenStore, RedisUtil.serialize(key)));
		if (accessToken != null
				&& !key.equals(authenticationKeyGenerator.extractKey(readAuthentication(accessToken.getValue())))) {
			storeAccessToken(accessToken, oAuth2Authentication);
		}
		return accessToken;
	}

	@Override
	public Collection<OAuth2AccessToken> findTokensByClientIdAndUserName(String clientId, String userName) {
		__debug("=====findTokensByClientIdAndUserName: %s - %s", clientId, userName);
		Collection<OAuth2AccessToken> result = (Collection<OAuth2AccessToken>) RedisUtil.unserialize(
				RedisUtil.hget(userNameToAccessTokenStore, RedisUtil.serialize(getApprovalKey(clientId, userName))));
		return result != null ? Collections.<OAuth2AccessToken> unmodifiableCollection(result)
				: Collections.<OAuth2AccessToken> emptySet();
	}

	@Override
	public Collection<OAuth2AccessToken> findTokensByClientId(String clientId) {
		__debug("=====findTokensByClientId: %s", clientId);
		Collection<OAuth2AccessToken> result = (Collection<OAuth2AccessToken>) RedisUtil
				.unserialize(RedisUtil.hget(clientIdToAccessTokenStore, RedisUtil.serialize(clientId)));
		return result != null ? Collections.<OAuth2AccessToken> unmodifiableCollection(result)
				: Collections.<OAuth2AccessToken> emptySet();
	}

	private void addToCollection(byte[] collectionName, byte[] key, OAuth2AccessToken token) {
		Jedis jedis = null;
		try {
			jedis = RedisUtil.getJedis();
			long start = System.nanoTime();
			while (redisLockTimeOut > 0l && (System.nanoTime() - start) / 1000000 < redisLockTimeOut) {
				jedis.watch(collectionName, new byte[0]);
				Collection<OAuth2AccessToken> collection = (Collection<OAuth2AccessToken>) RedisUtil
						.unserialize(jedis.hget(collectionName, key));
				if (collection == null) {
					collection = new HashSet<>();
				}
				collection.add(token);
				Transaction transaction = jedis.multi();
				transaction.hset(collectionName, key, RedisUtil.serialize(collection));
				if (transaction.exec() != null) {
					break;
				}
				jedis.unwatch();
				Thread.sleep(10);
			}
		} catch (Exception e) {
			logger.error("RedisTokenStore.addToCollection - " + e.getMessage());
		} finally {
			RedisUtil.returnResource(jedis);
		}
	}

	private void putExpiryQueue(TokenExpiry expiry) {
		Jedis jedis = null;
		try {
			jedis = RedisUtil.getJedis();
			long start = System.nanoTime();
			byte[] key = RedisUtil.serialize(expiryQueue);
			while (redisLockTimeOut > 0 && ((System.nanoTime() - start) / 1000000 < redisLockTimeOut)) {
				jedis.watch(key, new byte[0]);
				RedisDelayQueue<TokenExpiry> delayQueue = (RedisDelayQueue<TokenExpiry>) RedisUtil
						.unserialize(jedis.get(key));
				if (delayQueue == null) {
					delayQueue = new RedisDelayQueue<>();
				}
				delayQueue.remove(expiry);
				delayQueue.put(expiry);
				Transaction transaction = jedis.multi();
				transaction.set(key, RedisUtil.serialize(delayQueue));
				if (transaction.exec() != null) {
					break;
				}
				jedis.unwatch();
				Thread.sleep(10);
			}
		} catch (Exception e) {
			logger.error("RedisTokenStore.putExpiryQueue - " + e.getMessage());
		} finally {
			RedisUtil.returnResource(jedis);
		}
	}

	private String getApprovalKey(OAuth2Authentication authentication) {
		String userName = authentication.getUserAuthentication() == null ? ""
				: authentication.getUserAuthentication().getName();
		return getApprovalKey(authentication.getOAuth2Request().getClientId(), userName);
	}

	private String getApprovalKey(String clientId, String userName) {
		return clientId + (userName == null ? "" : ":" + userName);
	}

	private void flush() {
		Jedis jedis = null;
		try {
			jedis = RedisUtil.getJedis();
			long start = System.nanoTime();
			byte[] key = RedisUtil.serialize(expiryQueue);
			while (redisLockTimeOut > 0 && (System.nanoTime() - start) / 1000000 < redisLockTimeOut) {
				jedis.watch(key, new byte[0]);
				RedisDelayQueue<TokenExpiry> delayQueue = (RedisDelayQueue<TokenExpiry>) RedisUtil
						.unserialize(jedis.get(key));
				if (delayQueue == null) {
					delayQueue = new RedisDelayQueue<>();
				}
				TokenExpiry expiry = (TokenExpiry) delayQueue.poll();
				while (expiry != null) {
					removeAccessToken(expiry.getValue());
					expiry = (TokenExpiry) delayQueue.poll();
				}
				Transaction transaction = jedis.multi();
				transaction.set(key, RedisUtil.serialize(delayQueue));
				if (transaction.exec() != null) {
					break;
				}
				jedis.unwatch();
				Thread.sleep(10);
			}
		} catch (Exception e) {
			logger.error("RedisTokenStore.flush - " + e.getMessage());
		} finally {
			RedisUtil.returnResource(jedis);
		}
	}

	private void __debug(String msg, Object... args) {
		logger.debug(String.format(msg, args));
	}

	private static class RedisDelayQueue<E extends Delayed> extends DelayQueue implements Serializable {

	}

	private static class TokenExpiry implements Delayed {

		private final long expiry;

		private final String value;

		public TokenExpiry(String value, Date date) {
			this.value = value;
			this.expiry = date.getTime();
		}

		public int compareTo(Delayed other) {
			if (this == other) {
				return 0;
			}
			long diff = getDelay(TimeUnit.MILLISECONDS) - other.getDelay(TimeUnit.MILLISECONDS);
			return (diff == 0l ? 0 : ((diff < 0) ? -1 : 1));
		}

		public long getDelay(TimeUnit unit) {
			return expiry - System.currentTimeMillis();
		}

		public String getValue() {
			return value;
		}

	}
}
