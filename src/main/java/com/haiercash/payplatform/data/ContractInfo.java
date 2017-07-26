package com.haiercash.payplatform.data;

public class ContractInfo {
	private String merchName; // 商户名称
	private String contractNo; // 合同编号
	private String custName; // 借款人(甲方)姓名
	private String identifyNo; // 身份证号码
	private String custAddress; // 居住地址
	private String custMobile; // 联系电话
	private String email; // 邮箱
	private String applyAmtSmall; // 借款金额小写
	private String applyAmtBig; // 借款金额大写
	private String applyTnr; // 借款期限
	private String applyTnrTyp; // 借款方式类型
	private String rate; // 利率标准
	private String fstPayBig; // 首付金额大写
	private String fstPaySmall; // 首付金额小写
	private String purpose; // 贷款用途
	private String applAcNam; // 放款账户的户名
	private String accBankCde; // 放款行行号
	private String accBankName; // 放款账户开户行
	private String accAcBchCde; // 放款行支行代码
	private String applCardNo; // 放款银行账号
	private String mtdCde; // 还款方式类型
	private String repayApplAcNam; // 还款账号户名
	private String repayAccBankName;// 还款开户银行名
	private String repayApplCardNo; // 还款卡号
	private String accName; // 甲方签名
	private String lenderName; // 乙方签名 (海尔消费金融有限公司)
	private String date; // 日期
	private String cooprName; // 门店名称
	private String cooprCityName; //门店所在市名称
	private String applyManagerRate;// 管理费率
	private String liveZip; // 邮编
	private String feeRate; // 手续费率
	private String crdNorAvailAmt; // 自主支付可用额度金额 小写
	private String bigCrdNorAvailAmt; // 自主支付可用额度金额 大写
	private String year;
	private String month;
	private String day;

	private String applCde;// 贷款编号

	public String getMerchName() {
		return merchName;
	}

	public void setMerchName(String merchName) {
		this.merchName = merchName;
	}
	public String getCooprCityName() {
		return cooprCityName;
	}

	public void setCooprCityName(String cooprCityName) {
		this.cooprCityName = cooprCityName;
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

	public String getBigCrdNorAvailAmt() {
		return bigCrdNorAvailAmt;
	}

	public void setBigCrdNorAvailAmt(String bigCrdNorAvailAmt) {
		this.bigCrdNorAvailAmt = bigCrdNorAvailAmt;
	}

	public String getCrdNorAvailAmt() {
		return crdNorAvailAmt;
	}

	public void setCrdNorAvailAmt(String crdNorAvailAmt) {
		this.crdNorAvailAmt = crdNorAvailAmt;
	}

	public String getFeeRate() {
		return feeRate;
	}

	public void setFeeRate(String feeRate) {
		this.feeRate = feeRate;
	}

	public String getLiveZip() {
		return liveZip;
	}

	public void setLiveZip(String liveZip) {
		this.liveZip = liveZip;
	}

	public String getAccAcBchCde() {
		return accAcBchCde;
	}

	public void setAccAcBchCde(String accAcBchCde) {
		this.accAcBchCde = accAcBchCde;
	}

	public String getApplyManagerRate() {
		return applyManagerRate;
	}

	public void setApplyManagerRate(String applyManagerRate) {
		this.applyManagerRate = applyManagerRate;
	}

	public String getCooprName() {
		return cooprName;
	}

	public void setCooprName(String cooprName) {
		this.cooprName = cooprName;
	}

	public String getContractNo() {
		return contractNo;
	}

	public void setContractNo(String contractNo) {
		this.contractNo = contractNo;
	}

	public String getCustName() {
		return custName;
	}

	public void setCustName(String custName) {
		this.custName = custName;
	}

	public String getIdentifyNo() {
		return identifyNo;
	}

	public void setIdentifyNo(String identifyNo) {
		this.identifyNo = identifyNo;
	}

	public String getCustAddress() {
		return custAddress;
	}

	public void setCustAddress(String custAddress) {
		this.custAddress = custAddress;
	}

	public String getCustMobile() {
		return custMobile;
	}

	public void setCustMobile(String custMobile) {
		this.custMobile = custMobile;
	}

	public String getApplyAmtSmall() {
		return applyAmtSmall;
	}

	public void setApplyAmtSmall(String applyAmtSmall) {
		this.applyAmtSmall = applyAmtSmall;
	}

	public String getApplyAmtBig() {
		return applyAmtBig;
	}

	public void setApplyAmtBig(String applyAmtBig) {
		this.applyAmtBig = applyAmtBig;
	}

	public String getApplyTnr() {
		return applyTnr;
	}

	public void setApplyTnr(String applyTnr) {
		this.applyTnr = applyTnr;
	}

	public String getApplyTnrTyp() {
		return applyTnrTyp;
	}

	public void setApplyTnrTyp(String applyTnrTyp) {
		this.applyTnrTyp = applyTnrTyp;
	}

	public String getApplAcNam() {
		return applAcNam;
	}

	public void setApplAcNam(String applAcNam) {
		this.applAcNam = applAcNam;
	}

	public String getAccBankName() {
		return accBankName;
	}

	public void setAccBankName(String accBankName) {
		this.accBankName = accBankName;
	}

	public String getApplCardNo() {
		return applCardNo;
	}

	public void setApplCardNo(String applCardNo) {
		this.applCardNo = applCardNo;
	}

	public String getMtdCde() {
		return mtdCde;
	}

	public void setMtdCde(String mtdCde) {
		this.mtdCde = mtdCde;
	}

	public String getLenderName() {
		return lenderName;
	}

	public void setLenderName(String lenderName) {
		if (null == lenderName || "".equals(lenderName)) {
			this.lenderName = "海尔消费金融有限公司";
		} else {
			this.lenderName = lenderName;
		}
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getAccName() {
		return accName;
	}

	public void setAccName(String accName) {
		this.accName = accName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getRepayApplAcNam() {
		return repayApplAcNam;
	}

	public void setRepayApplAcNam(String repayApplAcNam) {
		this.repayApplAcNam = repayApplAcNam;
	}

	public String getRepayAccBankName() {
		return repayAccBankName;
	}

	public void setRepayAccBankName(String repayAccBankName) {
		this.repayAccBankName = repayAccBankName;
	}

	public String getRepayApplCardNo() {
		return repayApplCardNo;
	}

	public void setRepayApplCardNo(String repayApplCardNo) {
		this.repayApplCardNo = repayApplCardNo;
	}

	public String getPurpose() {
		return purpose;
	}

	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}

	public String getAccBankCde() {
		return accBankCde;
	}

	public void setAccBankCde(String accBankCde) {
		this.accBankCde = accBankCde;
	}

	public String getFstPayBig() {
		return fstPayBig;
	}

	public void setFstPayBig(String fstPayBig) {
		this.fstPayBig = fstPayBig;
	}

	public String getFstPaySmall() {
		return fstPaySmall;
	}

	public void setFstPaySmall(String fstPaySmall) {
		this.fstPaySmall = fstPaySmall;
	}

	public String getRate() {
		return rate;
	}

	public void setRate(String rate) {
		this.rate = rate;
	}

	public String getApplCde() {
		return applCde;
	}

	public void setApplCde(String applCde) {
		this.applCde = applCde;
	}

	@Override
	public String toString() {
		return "ContractInfo [contractNo=" + contractNo + ", custName=" + custName + ", merchName=" + merchName +", identifyNo=" + identifyNo + ", custAddress=" + custAddress + ", custMobile=" + custMobile + ", email=" + email + ", applyAmtSmall=" + applyAmtSmall + ", applyAmtBig=" + applyAmtBig + ", applyTnr=" + applyTnr + ", applyTnrTyp=" + applyTnrTyp + ", rate=" + rate + ", fstPayBig=" + fstPayBig + ", fstPaySmall=" + fstPaySmall + ", purpose=" + purpose + ", applAcNam=" + applAcNam + ", accBankCde=" + accBankCde + ", accBankName=" + accBankName + ", accAcBchCde=" + accAcBchCde + ", applCardNo=" + applCardNo + ", mtdCde=" + mtdCde + ", repayApplAcNam=" + repayApplAcNam + ", repayAccBankName=" + repayAccBankName + ", repayApplCardNo=" + repayApplCardNo + ", accName=" + accName + ", lenderName=" + lenderName + ", date=" + date + ", cooprName=" + cooprName + ", applyManagerRate=" + applyManagerRate + ", liveZip=" + liveZip + ", feeRate=" + feeRate + ", crdNorAvailAmt=" + crdNorAvailAmt + ", bigCrdNorAvailAmt=" + bigCrdNorAvailAmt + ", year=" + year + ", month=" + month + ", day=" + day + "]";
	}

	

}
