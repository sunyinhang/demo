package com.haiercash.payplatform.data;

import java.math.BigDecimal;

import com.thoughtworks.xstream.annotations.XStreamAlias;
@XStreamAlias("FileTransLog")
public class FileTransLog {
	private String log_id; // 日志id
	private String tradecode; // 交易码
	private String channelno; // 渠道编码
	private String applseq; // 申请流水号
	private String serno; // 报文流水号
	private String tradetime; // 交易时间
	private String sysid; // 系统标识
	private String busid; // 业务标识
	private String thirdreqdata; // 第三方请求数据：第三方->支付平台
	private String thirdresdata; // 支付响应第三方数据
	private String cmisreqdata; // 信贷方请求数据:支付平台->信贷
	private String cmisresdata; // 信贷方响应支付平台数据
	private int totalfile; // 应下载文件总数
	private int downfilenum; // 成功下载文件数
	private String fileflag; // 下载标志:成功:success,失败:failure
	private String filemsg; // 文件下载失败信息汇总
	private String retflag; // 最终处理结果状态,成功:00,失败:11
	private String retmsg; // 最终处理失败错误信息
	private String remark; // 备注
	public String getLog_id() {
		return log_id;
	}
	public void setLog_id(String log_id) {
		this.log_id = log_id;
	}
	public String getTradecode() {
		return tradecode;
	}
	public void setTradecode(String tradecode) {
		this.tradecode = tradecode;
	}
	public String getChannelno() {
		return channelno;
	}
	public void setChannelno(String channelno) {
		this.channelno = channelno;
	}
	public String getApplseq() {
		return applseq;
	}
	public void setApplseq(String applseq) {
		this.applseq = applseq;
	}
	public String getSerno() {
		return serno;
	}
	public void setSerno(String serno) {
		this.serno = serno;
	}
	public String getTradetime() {
		return tradetime;
	}
	public void setTradetime(String tradetime) {
		this.tradetime = tradetime;
	}
	public String getSysid() {
		return sysid;
	}
	public void setSysid(String sysid) {
		this.sysid = sysid;
	}
	public String getBusid() {
		return busid;
	}
	public void setBusid(String busid) {
		this.busid = busid;
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
	public String getCmisreqdata() {
		return cmisreqdata;
	}
	public void setCmisreqdata(String cmisreqdata) {
		this.cmisreqdata = cmisreqdata;
	}
	public String getCmisresdata() {
		return cmisresdata;
	}
	public void setCmisresdata(String cmisresdata) {
		this.cmisresdata = cmisresdata;
	}
	public int getTotalfile() {
		return totalfile;
	}
	public void setTotalfile(int totalfile) {
		this.totalfile = totalfile;
	}
	public int getDownfilenum() {
		return downfilenum;
	}
	public void setDownfilenum(int downfilenum) {
		this.downfilenum = downfilenum;
	}
	public String getFileflag() {
		return fileflag;
	}
	public void setFileflag(String fileflag) {
		this.fileflag = fileflag;
	}
	public String getFilemsg() {
		return filemsg;
	}
	public void setFilemsg(String filemsg) {
		this.filemsg = filemsg;
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
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	

	

}
