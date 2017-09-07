/*
 * 功  能：简单说明该类的功能
 * 
 * 文件名：CooperativeBusiness.java
 * 
 * 描  述：
 * 
 * [变更履历]
 * Version   变更日         部署              作者           变更内容
 * -----------------------------------------------------------------------------
 * V1.00     2016年1月26日   Haiercash    xuchao      CREATE
 * -----------------------------------------------------------------------------
 *
 *
 * Copyright (c) 2016 Haiercash All Rights Reserved.
 *┌─────────────────────────────────────────────────—────┐
 *│ 版权声明                               Haiercash       │
 *└──────────────────────────────—————————————————————───┘
 */

package com.haiercash.payplatform.common.data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 *  DESCRIPTION:
 *
 * <p>
 * <a href="CooperativeBusiness.java"><i>View Source</i></a>
 * </p>
 * 
 * @author <a href="mailto:xuchao@haiercash.com">xuchao</a>
 *
 * @version Revision: 1.0  Date: 2016年1月26日 下午5:34:40 
 *
 */
@Entity
@Table(name = "COOPERATIVEBUSINESS")
public class CooperativeBusiness implements Serializable {
	private static final long serialVersionUID = 1L;
	@Id
	private int id;
	private String cooperationcoed;
	private String cooperationname;
	private String rsaprivate;
	private String rsapublic;
	private String activeflag;
	private String operatorcde;
	private String operatortel;
	private String resourceLanguage;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getCooperationcoed() {
		return cooperationcoed;
	}

	public void setCooperationcoed(String cooperationcoed) {
		this.cooperationcoed = cooperationcoed;
	}

	public String getCooperationname() {
		return cooperationname;
	}

	public void setCooperationname(String cooperationname) {
		this.cooperationname = cooperationname;
	}

	public String getRsaprivate() {
		return rsaprivate;
	}

	public void setRsaprivate(String rsaprivate) {
		this.rsaprivate = rsaprivate;
	}

	public String getRsapublic() {
		return rsapublic;
	}

	public void setRsapublic(String rsapublic) {
		this.rsapublic = rsapublic;
	}

	public String getActiveflag() {
		return activeflag;
	}

	public void setActiveflag(String activeflag) {
		this.activeflag = activeflag;
	}

	public String getOperatorcde() {
		return operatorcde;
	}

	public void setOperatorcde(String operatorcde) {
		this.operatorcde = operatorcde;
	}

	public String getOperatortel() {
		return operatortel;
	}

	public void setOperatortel(String operatortel) {
		this.operatortel = operatortel;
	}

	public String getResourceLanguage() {
		return resourceLanguage;
	}

	public void setResourceLanguage(String resourceLanguage) {
		this.resourceLanguage = resourceLanguage;
	}

	@Override
	public String toString() {
		return "CooperativeBusiness{" +
				"id=" + id +
				", cooperationcoed='" + cooperationcoed + '\'' +
				", cooperationname='" + cooperationname + '\'' +
				", rsaprivate='" + rsaprivate + '\'' +
				", rsapublic='" + rsapublic + '\'' +
				", activeflag='" + activeflag + '\'' +
				", operatorcde='" + operatorcde + '\'' +
				", operatortel='" + operatortel + '\'' +
				", resourceLanguage='" + resourceLanguage + '\'' +
				'}';
	}
}
