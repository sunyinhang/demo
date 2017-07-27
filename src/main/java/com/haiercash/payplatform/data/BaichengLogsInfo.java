package com.haiercash.payplatform.data;

public class BaichengLogsInfo {
	private String logid; //日志ID
	private String bankname; //银行接口
	private String transcode; //交易码
	private String masterid; //企业客户号
	private String packetid; //报文号
	private String timestamp; //交易时间戳
	private String channelno; //渠道编码
	private String thirdreqdata; //第三方请求数据
	private String thirdresdata; //响应第三方数据
	private String bankreqdata; //交易请求数据：平台发送给银行的数据
	private String bankresdata; //交易响应数据：银行返回给平台数据
	private String signreqdata; //签名请求数据
	private String tradetime; //交易时间
	private String retflag; //返回标识
	private String retmsg; //返回信息：失败错误信息
	private String bankflag; //银行返回标识
	private String bankmsg; //银行返回信息：失败错误信息
	private String remark; //描述
	private String contractNo;//合同编号
	public String getLogid() {
		return logid;
	}
	public void setLogid(String logid) {
		this.logid = logid;
	}
	public String getBankname() {
		return bankname;
	}
	public void setBankname(String bankname) {
		this.bankname = bankname;
	}
	public String getTranscode() {
		return transcode;
	}
	public void setTranscode(String transcode) {
		this.transcode = transcode;
	}
	public String getMasterid() {
		return masterid;
	}
	public void setMasterid(String masterid) {
		this.masterid = masterid;
	}
	public String getPacketid() {
		return packetid;
	}
	public void setPacketid(String packetid) {
		this.packetid = packetid;
	}
	public String getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	public String getChannelno() {
		return channelno;
	}
	public void setChannelno(String channelno) {
		this.channelno = channelno;
	}
	public String getThirdreqdata() {
		return thirdreqdata;
	}
	public void setThirdreqdata(String thirdreqdata) {
		this.thirdreqdata = thirdreqdata;
	}
	public String getThirdresdata() {
		return thirdresdata;
	}
	public void setThirdresdata(String thirdresdata) {
		this.thirdresdata = thirdresdata;
	}
	public String getBankreqdata() {
		return bankreqdata;
	}
	public void setBankreqdata(String bankreqdata) {
		this.bankreqdata = bankreqdata;
	}
	public String getBankresdata() {
		return bankresdata;
	}
	public void setBankresdata(String bankresdata) {
		this.bankresdata = bankresdata;
	}
	public String getSignreqdata() {
		return signreqdata;
	}
	public void setSignreqdata(String signreqdata) {
		this.signreqdata = signreqdata;
	}
	public String getTradetime() {
		return tradetime;
	}
	public void setTradetime(String tradetime) {
		this.tradetime = tradetime;
	}
	public String getRetflag() {
		return retflag;
	}
	public void setRetflag(String retflag) {
		this.retflag = retflag;
	}
	public String getRetmsg() {
		return retmsg;
	}
	public void setRetmsg(String retmsg) {
		this.retmsg = retmsg;
	}
	public String getBankflag() {
		return bankflag;
	}
	public void setBankflag(String bankflag) {
		this.bankflag = bankflag;
	}
	public String getBankmsg() {
		return bankmsg;
	}
	public void setBankmsg(String bankmsg) {
		this.bankmsg = bankmsg;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public String getContractNo() {
		return contractNo;
	}
	public void setContractNo(String contractNo) {
		this.contractNo = contractNo;
	}
	@Override
	public String toString() {
		return "BaichengLogsInfo [logid=" + logid + ", bankname=" + bankname + ", transcode=" + transcode
				+ ", masterid=" + masterid + ", packetid=" + packetid + ", timestamp=" + timestamp + ", channelno="
				+ channelno + ", thirdreqdata=" + thirdreqdata + ", thirdresdata=" + thirdresdata + ", bankreqdata="
				+ bankreqdata + ", bankresdata=" + bankresdata + ", signreqdata=" + signreqdata + ", tradetime="
				+ tradetime + ", retflag=" + retflag + ", retmsg=" + retmsg + ", bankflag=" + bankflag + ", bankmsg="
				+ bankmsg + ", remark=" + remark + ", contractNo=" + contractNo + "]";
	}
	
}
