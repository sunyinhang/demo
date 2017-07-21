package com.haiercash.common.data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "APP_ATTACH_FILE")
public class AttachFile {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private Long id;
	private String orderNo;
	private String applSeq;
	private String attachType;
	private String attachName;
	private String fileDesc;
	private String fileName;
	private String attachSeq;
	private String fileMd5;
	private String custNo;
	private String commonCustNo; //共同还款人客户编号
	private String busiCode;
	private String primaryFile;

	public String getBusiCode() {
		return busiCode;
	}

	public void setBusiCode(String busiCode) {
		this.busiCode = busiCode;
	}

	public String getPrimaryFile() {
		return primaryFile;
	}

	public void setPrimaryFile(String primaryFile) {
		this.primaryFile = primaryFile;
	}

	public String getCommonCustNo() {
		return commonCustNo;
	}

	public void setCommonCustNo(String commonCustNo) {
		this.commonCustNo = commonCustNo;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}

	public String getApplSeq() {
		return applSeq;
	}

	public void setApplSeq(String applSeq) {
		this.applSeq = applSeq;
	}

	public String getAttachType() {
		return attachType;
	}

	public void setAttachType(String attachType) {
		this.attachType = attachType;
	}

	public String getAttachName() {
		return attachName;
	}

	public void setAttachName(String attachName) {
		this.attachName = attachName;
	}

	public String getFileDesc() {
		return fileDesc;
	}

	public void setFileDesc(String fileDesc) {
		this.fileDesc = fileDesc;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getAttachSeq() {
		return attachSeq;
	}

	public void setAttachSeq(String attachSeq) {
		this.attachSeq = attachSeq;
	}

	public String getFileMd5() {
		return fileMd5;
	}

	public void setFileMd5(String fileMd5) {
		this.fileMd5 = fileMd5;
	}

	public String getCustNo() {
		return custNo;
	}

	public void setCustNo(String custNo) {
		this.custNo = custNo;
	}

	@Override
	public String toString() {
		return "AttachFile{" +
				"id=" + id +
				", orderNo='" + orderNo + '\'' +
				", applSeq='" + applSeq + '\'' +
				", attachType='" + attachType + '\'' +
				", attachName='" + attachName + '\'' +
				", fileDesc='" + fileDesc + '\'' +
				", fileName='" + fileName + '\'' +
				", attachSeq='" + attachSeq + '\'' +
				", fileMd5='" + fileMd5 + '\'' +
				", custNo='" + custNo + '\'' +
				", commonCustNo='" + commonCustNo + '\'' +
				", busiCode='" + busiCode + '\'' +
				", primaryFile='" + primaryFile + '\'' +
				'}';
	}
}
