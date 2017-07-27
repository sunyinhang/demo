package com.haiercash.payplatform.data;

import java.math.BigDecimal;
import java.util.Date;

public class BaiChengBusinessInfo {
	private String businessId;// 业务ID
	private String customerid;// 客户ID
	private String customerName;// 客户名称
	private String sum;// 存款金额
	private String poundage;//应付金额
	private String loanType;//贷款品种
	private String term;// 存款期限
	private String contractNo;//合同号
	private String annualRate;// 年利率
	private String openerDateBn;// 开具日期
	private String openerDateEd;// 开具截止日期
	private String proveNo;// 证明份数
	private String flag;// 有效标识
	private String payStatus;// 支付状态
	private String bankNo;// 银行流水号
	private String openbankNo;// 开户账号(监管账号)
	private String flagQdjj;// 渠道进件状态
	private String createDate;// 创建时间
	private String updateDate;// 修改时间
	private String mark;// 备注
	private String flagBank;//调用银行接口状态
	private String failNum;//调用接口失败次数
	private String failReason;//调用接口失败原因
	private String docId;//影像唯一标识
	private String applCde;//申请编号
	private String prompt;//是否发送短信提醒；Y：已提醒N：未提醒
	private String applSeq;//申请流水号
	private String loanNo;//借据号
	private String restAmt;//光大银行利息
	private String pounAmt;//银联手续费差值
	
	public String getBusinessId() {
		return businessId;
	}
	public void setBusinessId(String businessId) {
		this.businessId = businessId;
	}
	public String getCustomerid() {
		return customerid;
	}
	public void setCustomerid(String customerid) {
		this.customerid = customerid;
	}
	public String getCustomerName() {
		return customerName;
	}
	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}
	public String getSum() {
		return sum;
	}
	public void setSum(String sum) {
		this.sum = sum;
	}
	public String getTerm() {
		return term;
	}
	public void setTerm(String term) {
		this.term = term;
	}
	public String getAnnualRate() {
		return annualRate;
	}
	public void setAnnualRate(String annualRate) {
		this.annualRate = annualRate;
	}
	public String getOpenerDateBn() {
		return openerDateBn;
	}
	public void setOpenerDateBn(String openerDateBn) {
		this.openerDateBn = openerDateBn;
	}
	public String getOpenerDateEd() {
		return openerDateEd;
	}
	public void setOpenerDateEd(String openerDateEd) {
		this.openerDateEd = openerDateEd;
	}
	public String getProveNo() {
		return proveNo;
	}
	public void setProveNo(String proveNo) {
		this.proveNo = proveNo;
	}
	public String getFlag() {
		return flag;
	}
	public void setFlag(String flag) {
		this.flag = flag;
	}
	public String getPayStatus() {
		return payStatus;
	}
	public void setPayStatus(String payStatus) {
		this.payStatus = payStatus;
	}
	public String getBankNo() {
		return bankNo;
	}
	public void setBankNo(String bankNo) {
		this.bankNo = bankNo;
	}
	public String getOpenbankNo() {
		return openbankNo;
	}
	public void setOpenbankNo(String openbankNo) {
		this.openbankNo = openbankNo;
	}
	public String getCreateDate() {
		return createDate;
	}
	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}
	public String getUpdateDate() {
		return updateDate;
	}
	public void setUpdateDate(String updateDate) {
		this.updateDate = updateDate;
	}
	public String getMark() {
		return mark;
	}
	public void setMark(String mark) {
		this.mark = mark;
	}
	public String getLoanType() {
		return loanType;
	}
	public void setLoanType(String loanType) {
		this.loanType = loanType;
	}
	public String getFlagQdjj() {
		return flagQdjj;
	}
	public void setFlagQdjj(String flagQdjj) {
		this.flagQdjj = flagQdjj;
	}
	public String getPoundage() {
		return poundage;
	}
	public void setPoundage(String poundage) {
		this.poundage = poundage;
	}
	public String getContractNo() {
		return contractNo;
	}
	public void setContractNo(String contractNo) {
		this.contractNo = contractNo;
	}
	public String getFlagBank() {
		return flagBank;
	}
	public void setFlagBank(String flagBank) {
		this.flagBank = flagBank;
	}
	public String getFailNum() {
		return failNum;
	}
	public void setFailNum(String failNum) {
		this.failNum = failNum;
	}
	public String getFailReason() {
		return failReason;
	}
	public void setFailReason(String failReason) {
		this.failReason = failReason;
	}
	public String getDocId() {
		return docId;
	}
	public void setDocId(String docId) {
		this.docId = docId;
	}
	public String getApplCde() {
		return applCde;
	}
	public void setApplCde(String applCde) {
		this.applCde = applCde;
	}
	public String getPrompt() {
		return prompt;
	}
	public void setPrompt(String prompt) {
		this.prompt = prompt;
	}
	public String getApplSeq() {
		return applSeq;
	}
	public void setApplSeq(String applSeq) {
		this.applSeq = applSeq;
	}
	public String getLoanNo() {
		return loanNo;
	}
	public void setLoanNo(String loanNo) {
		this.loanNo = loanNo;
	}
	public String getRestAmt() {
		return restAmt;
	}
	public void setRestAmt(String restAmt) {
		this.restAmt = restAmt;
	}
	public String getPounAmt() {
		return pounAmt;
	}
	public void setPounAmt(String pounAmt) {
		this.pounAmt = pounAmt;
	}
	@Override
	public String toString() {
		return "BaiChengBusinessInfo [businessId=" + businessId + ", customerid=" + customerid + ", customerName="
				+ customerName + ", sum=" + sum + ", poundage=" + poundage + ", loanType=" + loanType + ", term=" + term
				+ ", contractNo=" + contractNo + ", annualRate=" + annualRate + ", openerDateBn=" + openerDateBn
				+ ", openerDateEd=" + openerDateEd + ", proveNo=" + proveNo + ", flag=" + flag + ", payStatus="
				+ payStatus + ", bankNo=" + bankNo + ", openbankNo=" + openbankNo + ", flagQdjj=" + flagQdjj
				+ ", createDate=" + createDate + ", updateDate=" + updateDate + ", mark=" + mark + ", flagBank="
				+ flagBank + ", failNum=" + failNum + ", failReason=" + failReason + ", docId=" + docId + ", applCde="
				+ applCde + ", prompt=" + prompt + ", applSeq=" + applSeq + ", loanNo=" + loanNo + ", restAmt="
				+ restAmt + ", pounAmt=" + pounAmt + "]";
	}
	
}
