package com.haiercash.payplatform.data;

public class CooperativeMsg {
	private String channelno; //渠道编号
	private String activeflag;//状态标识
	private String cooperationurl; //消息推送URL
	private String remark; //备注
	private String castateurl;//签章状态消息推送URL
	private String preapprovalNoticeUrl;//额度前置审批通知url
	private String loanApprovalNoticeUrl;//贷款审批通知url
	
	
	public String getChannelno() {
		return channelno;
	}
	public void setChannelno(String channelno) {
		this.channelno = channelno;
	}
	public String getActiveflag() {
		return activeflag;
	}
	public void setActiveflag(String activeflag) {
		this.activeflag = activeflag;
	}
	public String getCooperationurl() {
		return cooperationurl;
	}
	public void setCooperationurl(String cooperationurl) {
		this.cooperationurl = cooperationurl;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public String getCastateurl() {
		return castateurl;
	}
	public void setCastateurl(String castateurl) {
		this.castateurl = castateurl;
	}
	public String getPreapprovalNoticeUrl() {
		return preapprovalNoticeUrl;
	}
	public void setPreapprovalNoticeUrl(String preapprovalNoticeUrl) {
		this.preapprovalNoticeUrl = preapprovalNoticeUrl;
	}
	public String getLoanApprovalNoticeUrl() {
		return loanApprovalNoticeUrl;
	}
	public void setLoanApprovalNoticeUrl(String loanApprovalNoticeUrl) {
		this.loanApprovalNoticeUrl = loanApprovalNoticeUrl;
	}
	
	
}
