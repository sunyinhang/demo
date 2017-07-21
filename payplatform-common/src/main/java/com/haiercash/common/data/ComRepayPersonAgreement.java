package com.haiercash.common.data;

public class ComRepayPersonAgreement {

	private String custName;// 借款人客户姓名
	private String custVerifyNo;// 借款人身份证号
	private String typDesc;// 贷款品种名称
	private String applyAmt;// 贷款金额(小写)
	private String applyBigAmt;// 贷款金额(大写)
	private String applyTnr;// 贷款期限
	private String applCde;// 贷款编号
	private String commonCustName;// 共同还款人姓名
	private String commonCustVerifyNo;// 共同还款人身份证号
	private String year;
	private String month;
	private String day;

	public String getCustName() {
		return custName;
	}

	public void setCustName(String custName) {
		this.custName = custName;
	}

	public String getCustVerifyNo() {
		return custVerifyNo;
	}

	public void setCustVerifyNo(String custVerifyNo) {
		this.custVerifyNo = custVerifyNo;
	}

	public String getTypDesc() {
		return typDesc;
	}

	public void setTypDesc(String typDesc) {
		this.typDesc = typDesc;
	}

	public String getApplyAmt() {
		return applyAmt;
	}

	public void setApplyAmt(String applyAmt) {
		this.applyAmt = applyAmt;
	}

	public String getApplyBigAmt() {
		return applyBigAmt;
	}

	public void setApplyBigAmt(String applyBigAmt) {
		this.applyBigAmt = applyBigAmt;
	}

	public String getApplyTnr() {
		return applyTnr;
	}

	public void setApplyTnr(String applyTnr) {
		this.applyTnr = applyTnr;
	}

	public String getApplCde() {
		return applCde;
	}

	public void setApplCde(String applCde) {
		this.applCde = applCde;
	}

	public String getCommonCustName() {
		return commonCustName;
	}

	public void setCommonCustName(String commonCustName) {
		this.commonCustName = commonCustName;
	}

	public String getCommonCustVerifyNo() {
		return commonCustVerifyNo;
	}

	public void setCommonCustVerifyNo(String commonCustVerifyNo) {
		this.commonCustVerifyNo = commonCustVerifyNo;
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public String getMonth() {
		return month;
	}

	public void setMonth(String month) {
		this.month = month;
	}

	public String getDay() {
		return day;
	}

	public void setDay(String day) {
		this.day = day;
	}

	@Override
	public String toString() {
		return "comRepayPersonAgreement [custName=" + custName + ", custVerifyNo=" + custVerifyNo + ", typDesc="
				+ typDesc + ", applyAmt=" + applyAmt + ", applyBigAmt=" + applyBigAmt + ", applyTnr=" + applyTnr
				+ ", applCde=" + applCde + ", commonCustName=" + commonCustName + ", commonCustVerifyNo="
				+ commonCustVerifyNo + ", year=" + year + ", month=" + month + ", day=" + day + "]";
	}
}
