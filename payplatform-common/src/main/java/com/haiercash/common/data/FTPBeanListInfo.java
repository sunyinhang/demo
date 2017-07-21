package com.haiercash.common.data;

import org.springframework.web.multipart.MultipartFile;

public class FTPBeanListInfo {
	private String sequenceId;// 文件上传序列号
	private String attachPath;// 文件路径
	private String attachName;// 原文件名称
	private String attachNameNew;// 文件名称
	private String state;// 状态
	private String crtUsr;// 上传人员
	private String crtDt;// 上传时间
	private String loseEffectUsr;// 失效人员
	private String attachTyp;// 上传类型
	private String reserved6;// 预留字段6
	private String reserved7;// 预留字段7
	private String reserved8;// 预留字段8
	private String attachSeq;// 文件唯一编号
	private MultipartFile myfile;// 上传的文件

	public MultipartFile getMyfile() {
		return myfile;
	}

	public void setMyfile(MultipartFile myfile) {
		this.myfile = myfile;
	}

	public String getAttachSeq() {
		return attachSeq;
	}

	public void setAttachSeq(String attachSeq) {
		this.attachSeq = attachSeq;
	}

	public String getSequenceId() {
		return sequenceId;
	}

	public void setSequenceId(String sequenceId) {
		this.sequenceId = sequenceId;
	}

	public String getAttachPath() {
		return attachPath;
	}

	public void setAttachPath(String attachPath) {
		this.attachPath = attachPath;
	}

	public String getAttachName() {
		return attachName;
	}

	public void setAttachName(String attachName) {
		this.attachName = attachName;
	}

	public String getAttachNameNew() {
		return attachNameNew;
	}

	public void setAttachNameNew(String attachNameNew) {
		this.attachNameNew = attachNameNew;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getCrtUsr() {
		return crtUsr;
	}

	public void setCrtUsr(String crtUsr) {
		this.crtUsr = crtUsr;
	}

	public String getCrtDt() {
		return crtDt;
	}

	public void setCrtDt(String crtDt) {
		this.crtDt = crtDt;
	}

	public String getLoseEffectUsr() {
		return loseEffectUsr;
	}

	public void setLoseEffectUsr(String loseEffectUsr) {
		this.loseEffectUsr = loseEffectUsr;
	}

	public String getAttachTyp() {
		return attachTyp;
	}

	public void setAttachTyp(String attachTyp) {
		this.attachTyp = attachTyp;
	}

	public String getReserved6() {
		return reserved6;
	}

	public void setReserved6(String reserved6) {
		this.reserved6 = reserved6;
	}

	public String getReserved7() {
		return reserved7;
	}

	public void setReserved7(String reserved7) {
		this.reserved7 = reserved7;
	}

	public String getReserved8() {
		return reserved8;
	}

	public void setReserved8(String reserved8) {
		this.reserved8 = reserved8;
	}

}
