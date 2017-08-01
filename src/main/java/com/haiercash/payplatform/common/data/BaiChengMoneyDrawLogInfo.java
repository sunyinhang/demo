package com.haiercash.payplatform.common.data;

public class BaiChengMoneyDrawLogInfo {
	 private String tranSerNum;
     private String iousNum;
     private String deductDate;
     private String deductAmount;
     private String accountName;
     private String state;
     private String createDate;
     private String updateDate;
     private String busDate;
     private String batchNo;
     private String total;
     private String flag;
     private String mark;
     private String retFlag;
     private String retMsg;
	public String getTranSerNum() {
		return tranSerNum;
	}
	public void setTranSerNum(String tranSerNum) {
		this.tranSerNum = tranSerNum;
	}
	public String getIousNum() {
		return iousNum;
	}
	public void setIousNum(String iousNum) {
		this.iousNum = iousNum;
	}
	public String getDeductDate() {
		return deductDate;
	}
	public void setDeductDate(String deductDate) {
		this.deductDate = deductDate;
	}
	public String getDeductAmount() {
		return deductAmount;
	}
	public void setDeductAmount(String deductAmount) {
		this.deductAmount = deductAmount;
	}
	public String getAccountName() {
		return accountName;
	}
	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
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
	public String getBusDate() {
		return busDate;
	}
	public void setBusDate(String busDate) {
		this.busDate = busDate;
	}
	public String getBatchNo() {
		return batchNo;
	}
	public void setBatchNo(String batchNo) {
		this.batchNo = batchNo;
	}
	public String getTotal() {
		return total;
	}
	public void setTotal(String total) {
		this.total = total;
	}
	public String getFlag() {
		return flag;
	}
	public void setFlag(String flag) {
		this.flag = flag;
	}
	public String getMark() {
		return mark;
	}
	public void setMark(String mark) {
		this.mark = mark;
	}
	public String getRetFlag() {
		return retFlag;
	}
	public void setRetFlag(String retFlag) {
		this.retFlag = retFlag;
	}
	public String getRetMsg() {
		return retMsg;
	}
	public void setRetMsg(String retMsg) {
		this.retMsg = retMsg;
	}
	@Override
	public String toString() {
		return "BaiChengMoneyDrawLogInfo [tranSerNum=" + tranSerNum + ", iousNum=" + iousNum + ", deductDate="
				+ deductDate + ", deductAmount=" + deductAmount + ", accountName=" + accountName + ", state=" + state
				+ ", createDate=" + createDate + ", updateDate=" + updateDate + ", busDate=" + busDate + ", batchNo="
				+ batchNo + ", total=" + total + ", flag=" + flag + ", mark=" + mark + ", retFlag=" + retFlag
				+ ", retMsg=" + retMsg + "]";
	}
     
}
