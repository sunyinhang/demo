package com.haiercash.common.data;

public class Credit {
	private String custNo; // 客户编号
	private String idNo; // 身份证号
	private String custName; // 客户姓名
	private String year; // 年
	private String month; // 月
	private String day; // 日

	public String getCustNo() {
		return custNo;
	}

	public void setCustNo(String custNo) {
		this.custNo = custNo;
	}

	public String getIdNo() {
		return idNo;
	}

	public void setIdNo(String idNo) {
		this.idNo = idNo;
	}

	public String getCustName() {
		return custName;
	}

	public void setCustName(String custName) {
		this.custName = custName;
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
		return "Credit [custNo=" + custNo + ", idNo=" + idNo + ", custName=" + custName + ", year=" + year + ", month="
				+ month + ", day=" + day + "]";
	}

}
