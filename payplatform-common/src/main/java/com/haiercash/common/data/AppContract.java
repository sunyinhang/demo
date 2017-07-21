package com.haiercash.common.data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "app_contract_info")
public class AppContract {
	@Id
	private String contNo; // 合同编号
	private String contCode; // 合同对应的代码
	private String contDesc; // 合同描述
	private String contType; // 合同类型
	private String contBigClass; // 所属大类
	private String contSmaClass; // 所属小类
	private String applyType; // 贷款品种
	private String signMode; // 签章方式 1-旧签章服务 2-新签章服务

	public String getSignMode() {
		return signMode;
	}

	public void setSignMode(String signMode) {
		this.signMode = signMode;
	}

	public String getApplyType() {
		return applyType;
	}

	public void setApplyType(String applyType) {
		this.applyType = applyType;
	}

	public String getContNo() {
		return contNo;
	}

	public void setContNo(String contNo) {
		this.contNo = contNo;
	}

	public String getContCode() {
		return contCode;
	}

	public void setContCode(String contCode) {
		this.contCode = contCode;
	}

	public String getContDesc() {
		return contDesc;
	}

	public void setContDesc(String contDesc) {
		this.contDesc = contDesc;
	}

	public String getContType() {
		return contType;
	}

	public void setContType(String contType) {
		this.contType = contType;
	}

	public String getContBigClass() {
		return contBigClass;
	}

	public void setContBigClass(String contBigClass) {
		this.contBigClass = contBigClass;
	}

	public String getContSmaClass() {
		return contSmaClass;
	}

	public void setContSmaClass(String contSmaClass) {
		this.contSmaClass = contSmaClass;
	}

	@Override
	public String toString() {
		return "AppContract [contNo=" + contNo + ", contCode=" + contCode + ", contDesc=" + contDesc + ", contType="
				+ contType + ", contBigClass=" + contBigClass + ", contSmaClass=" + contSmaClass + ", applyType="
				+ applyType + ",signMode="+ signMode +"]";
	}

}
