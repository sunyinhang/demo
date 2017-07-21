package com.haiercash.common.data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "app_contract_association_info")
public class ContractAssInfo {
	@Id
	private String id;
	private String contractSeriNo; // 合同编号
	// private String applyType; //合同借款类型
	// private String applyDesc; //合同借款描述
	// private String contractApplyType;//所对应合同的借款条数
	private String applyRate; // 借款利率
	private String mtdCode; // 还款类型代码
	private String mtdDesc; // 还款类型描述
	private String contractMtdType; // 所对应合同的还款条数
	private String applyTnr; // 借款期限
	private String applyManagerRate; // 管理费费率
	private String feeRate; // 手续费费率
	private String hasComRepay; // 是否有共同还款人(目前暂时只针对HCFC-JZD-YZ-2016-1-20有住-个人借款合同)

	public String getHasComRepay() {
		return hasComRepay;
	}

	public void setHasComRepay(String hasComRepay) {
		this.hasComRepay = hasComRepay;
	}

	public String getFeeRate() {
		return feeRate;
	}

	public void setFeeRate(String feeRate) {
		this.feeRate = feeRate;
	}

	public String getApplyTnr() {
		return applyTnr;
	}

	public void setApplyTnr(String applyTnr) {
		this.applyTnr = applyTnr;
	}

	public String getApplyManagerRate() {
		return applyManagerRate;
	}

	public void setApplyManagerRate(String applyManagerRate) {
		this.applyManagerRate = applyManagerRate;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getContractSeriNo() {
		return contractSeriNo;
	}

	public void setContractSeriNo(String contractSeriNo) {
		this.contractSeriNo = contractSeriNo;
	}

	public String getApplyRate() {
		return applyRate;
	}

	public void setApplyRate(String applyRate) {
		this.applyRate = applyRate;
	}

	public String getMtdCode() {
		return mtdCode;
	}

	public void setMtdCode(String mtdCode) {
		this.mtdCode = mtdCode;
	}

	public String getMtdDesc() {
		return mtdDesc;
	}

	public void setMtdDesc(String mtdDesc) {
		this.mtdDesc = mtdDesc;
	}

	public String getContractMtdType() {
		return contractMtdType;
	}

	public void setContractMtdType(String contractMtdType) {
		this.contractMtdType = contractMtdType;
	}

}
