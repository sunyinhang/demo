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

public class CooperativeBusiness {
	private int id;
	private String cooperationCoed;
	private String cooperationName;
	private String rsaprivate;
	private String rsapublic;
	private String activeflag;
	private String operatorcde;
	private String operatortel;
	private String resourceLanguage;
	
	public String getResourceLanguage() {
		return resourceLanguage;
	}
	public void setResourceLanguage(String resourceLanguage) {
		this.resourceLanguage = resourceLanguage;
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
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getCooperationCoed() {
		return cooperationCoed;
	}
	public void setCooperationCoed(String cooperationCoed) {
		this.cooperationCoed = cooperationCoed;
	}
	public String getCooperationName() {
		return cooperationName;
	}
	public void setCooperationName(String cooperationName) {
		this.cooperationName = cooperationName;
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

	@Override
	public String toString() {
		return "CooperativeBusiness [id=" + id + ", cooperationCoed=" + cooperationCoed + ", cooperationName="
				+ cooperationName + ", rsaprivate=" + rsaprivate + ", rsapublic=" + rsapublic + ", activeflag="
				+ activeflag + ", operatorcde=" + operatorcde + ", operatortel=" + operatortel + ", resourceLanguage="
				+ resourceLanguage + "]";
	}
}
