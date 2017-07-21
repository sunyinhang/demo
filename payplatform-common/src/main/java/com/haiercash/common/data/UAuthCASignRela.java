package com.haiercash.common.data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "uauth_ca_sign_relation")
public class UAuthCASignRela {
	@Id
	private String signType;
	private String tempSeriNum;

	public String getSignType() {
		return signType;
	}

	public void setSignType(String signType) {
		this.signType = signType;
	}

	public String getTempSeriNum() {
		return tempSeriNum;
	}

	public void setTempSeriNum(String tempSeriNum) {
		this.tempSeriNum = tempSeriNum;
	}

}
