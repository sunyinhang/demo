package com.haiercash.common.data;

public class Grant {
	private String custName; // 客户姓名
	private String cardNo;//银行卡号
	private String bankName; //开户行
	private String year; // 年
	private String month; // 月
	private String day; // 日

	public String getCustName() {
		return custName;
	}

	public void setCustName(String custName) {
		this.custName = custName;
	}

	public String getCardNo() {
		return cardNo;
	}

	public void setCardNo(String cardNo) {
		this.cardNo = cardNo;
	}

	public String getBankName() {
		return bankName;
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
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
		return "Grant{" +
				"custName='" + custName + '\'' +
				", cardNo='" + cardNo + '\'' +
				", bankName='" + bankName + '\'' +
				", year='" + year + '\'' +
				", month='" + month + '\'' +
				", day='" + day + '\'' +
				'}';
	}
}
