package com.haiercash.payplatform.common.data;

import java.util.Date;

public class BaiChengCustomerPayInfo {
	private String customerId;	//客户ID	
	private String customerName;//客户名称
	private String firstNamePy;//客户姓名姓
	private String lastNamePy;//客户姓名名
	private String acntGender;//客户性别
	private String occupation;//职业
	private String cardNo;//身份证号
	private String signing;//签发机关
	private String validDateBn;//有效期开始日期
	private String validDateEd;//有效期终止日期
	private String addresseeName;//收件人姓名
	private String callPhone;//手机号码
	private String province;//省
	private String city;//市
	private String area;//区/（县）
	private String address;//详细地址
	private String delivAddress;//快递详细地址
	private String postalcode;//邮政编码
	private String realFlag;//实名认证标识   A：生效  I：无效
	private String cardJpgZm;//身份证(正面)
	private String cardJpgFm;//身份证(反面)
	private String businessNo;//业务流水号
	private String caJpg;//CA章
	private String createDate;//创建时间
	private String updataDate;//修改时间 
	private String mark;//备注
	private String bankcardNo;//银行卡号
	
	public String getCustomerId() {
		return customerId;
	}
	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}
	public String getCustomerName() {
		return customerName;
	}
	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}
	public String getOccupation() {
		return occupation;
	}
	public void setOccupation(String occupation) {
		this.occupation = occupation;
	}
	public String getCardNo() {
		return cardNo;
	}
	public void setCardNo(String cardNo) {
		this.cardNo = cardNo;
	}
	public String getSigning() {
		return signing;
	}
	public void setSigning(String signing) {
		this.signing = signing;
	}
	public String getAddresseeName() {
		return addresseeName;
	}
	public void setAddresseeName(String addresseeName) {
		this.addresseeName = addresseeName;
	}
	public String getCallPhone() {
		return callPhone;
	}
	public void setCallPhone(String callPhone) {
		this.callPhone = callPhone;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getPostalcode() {
		return postalcode;
	}
	public void setPostalcode(String postalcode) {
		this.postalcode = postalcode;
	}
	public String getRealFlag() {
		return realFlag;
	}
	public void setRealFlag(String realFlag) {
		this.realFlag = realFlag;
	}
	public String getCardJpgZm() {
		return cardJpgZm;
	}
	public void setCardJpgZm(String cardJpgZm) {
		this.cardJpgZm = cardJpgZm;
	}
	public String getCardJpgFm() {
		return cardJpgFm;
	}
	public void setCardJpgFm(String cardJpgFm) {
		this.cardJpgFm = cardJpgFm;
	}
	public String getBusinessNo() {
		return businessNo;
	}
	public void setBusinessNo(String businessNo) {
		this.businessNo = businessNo;
	}
	public String getCaJpg() {
		return caJpg;
	}
	public void setCaJpg(String caJpg) {
		this.caJpg = caJpg;
	}
	public String getCreateDate() {
		return createDate;
	}
	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}
	public String getUpdataDate() {
		return updataDate;
	}
	public void setUpdataDate(String updataDate) {
		this.updataDate = updataDate;
	}
	public String getMark() {
		return mark;
	}
	public void setMark(String mark) {
		this.mark = mark;
	}
	public String getValidDateBn() {
		return validDateBn;
	}
	public void setValidDateBn(String validDateBn) {
		this.validDateBn = validDateBn;
	}
	public String getValidDateEd() {
		return validDateEd;
	}
	public void setValidDateEd(String validDateEd) {
		this.validDateEd = validDateEd;
	}
	public String getDelivAddress() {
		return delivAddress;
	}
	public void setDelivAddress(String delivAddress) {
		this.delivAddress = delivAddress;
	}
	public String getProvince() {
		return province;
	}
	public void setProvince(String province) {
		this.province = province;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getArea() {
		return area;
	}
	public void setArea(String area) {
		this.area = area;
	}
	public String getAcntGender() {
		return acntGender;
	}
	public void setAcntGender(String acntGender) {
		this.acntGender = acntGender;
	}
	public String getFirstNamePy() {
		return firstNamePy;
	}
	public void setFirstNamePy(String firstNamePy) {
		this.firstNamePy = firstNamePy;
	}
	public String getLastNamePy() {
		return lastNamePy;
	}
	public void setLastNamePy(String lastNamePy) {
		this.lastNamePy = lastNamePy;
	}
	
	public String getBankcardNo() {
		return bankcardNo;
	}
	public void setBankcardNo(String bankcardNo) {
		this.bankcardNo = bankcardNo;
	}
	@Override
	public String toString() {
		return "BaiChengCustomerPayInfo [customerId=" + customerId + ", customerName=" + customerName + ", firstNamePy="
				+ firstNamePy + ", lastNamePy=" + lastNamePy + ", acntGender=" + acntGender + ", occupation="
				+ occupation + ", cardNo=" + cardNo + ", signing=" + signing 
				+ ", validDateBn=" + validDateBn + ", validDateEd=" + validDateEd + ", addresseeName=" + addresseeName
				+ ", callPhone=" + callPhone + ", province=" + province + ", city=" + city + ", area=" + area
				+ ", address=" + address + ", delivAddress=" + delivAddress + ", postalcode=" + postalcode
				+ ", realFlag=" + realFlag + ", cardJpgZm=" + cardJpgZm + ", cardJpgFm=" + cardJpgFm + ", businessNo="
				+ businessNo + ", caJpg=" + caJpg + ", createDate=" + createDate + ", updataDate=" + updataDate
				+ ", mark=" + mark + ", bankcardNo=" + bankcardNo + "]";
	}
	
}
