package com.haiercash.payplatform.data;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("BankInfo")
public class BaiChengBankInfo {
	private String cardSign;
	private String cardSignlen;
	private String cardNoLen;
	private String cardType;
	private String cardName;
	private String bankNo;
	private String bankName;
	
	public String getCardSign() {
		return cardSign;
	}
	public void setCardSign(String cardSign) {
		this.cardSign = cardSign;
	}
	public String getCardSignlen() {
		return cardSignlen;
	}
	public void setCardSignlen(String cardSignlen) {
		this.cardSignlen = cardSignlen;
	}
	public String getCardNoLen() {
		return cardNoLen;
	}
	public void setCardNoLen(String cardNoLen) {
		this.cardNoLen = cardNoLen;
	}
	public String getCardType() {
		return cardType;
	}
	public void setCardType(String cardType) {
		this.cardType = cardType;
	}
	public String getCardName() {
		return cardName;
	}
	public void setCardName(String cardName) {
		this.cardName = cardName;
	}
	public String getBankNo() {
		return bankNo;
	}
	public void setBankNo(String bankNo) {
		this.bankNo = bankNo;
	}
	public String getBankName() {
		return bankName;
	}
	public void setBankName(String bankName) {
		this.bankName = bankName;
	}
}
