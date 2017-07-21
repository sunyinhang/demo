package com.haiercash.common.data;

import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.SelectBeforeUpdate;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

@Entity
@Table(name = "uauth_user_token")
@DynamicUpdate
@SelectBeforeUpdate
public class UAuthUserToken implements Serializable {
	private static final long serialVersionUID = 2738830833739591493L;

	@Id
	private String clientId;

	private String userId;
	private String clientSecret;
	private String registerDate;
	private String deviceType;

	public String getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	public String getRegisterDate() {
		return registerDate;
	}

	public void setRegisterDate(Date registerDate) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		this.registerDate = sdf.format(registerDate);
	}

	public void setRegisterDate(String registerDate) {
		this.registerDate = registerDate;
	}
}