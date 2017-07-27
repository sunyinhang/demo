package com.haiercash.payplatform.data;

public class CebTradeKeyInfo {
	private String id; //ID
	private String trmseqnum; //终端流水号
	private String tradedate; //交易日期
	private String pinkeyname; //PIN密钥名称
	private String pinkeyvalue; //主密钥串
	private String pinverifyvalue; //校验值
	private String mackeyname; //MAC密钥名称
	private String mackeyvalue; //主密钥串1
	private String macverifyvalue; //校验值1
	private String reqdata; //请求数据
	private String resdata; //响应数据
	private String succflag; //成功标识   Y:成功，N:失败
	private String errorcode; //错误码
	private String errorinfo; //错误信息
	private String remark; //备注
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getTrmseqnum() {
		return trmseqnum;
	}
	public void setTrmseqnum(String trmseqnum) {
		this.trmseqnum = trmseqnum;
	}
	public String getTradedate() {
		return tradedate;
	}
	public void setTradedate(String tradedate) {
		this.tradedate = tradedate;
	}
	public String getPinkeyname() {
		return pinkeyname;
	}
	public void setPinkeyname(String pinkeyname) {
		this.pinkeyname = pinkeyname;
	}
	public String getPinkeyvalue() {
		return pinkeyvalue;
	}
	public void setPinkeyvalue(String pinkeyvalue) {
		this.pinkeyvalue = pinkeyvalue;
	}
	public String getPinverifyvalue() {
		return pinverifyvalue;
	}
	public void setPinverifyvalue(String pinverifyvalue) {
		this.pinverifyvalue = pinverifyvalue;
	}
	public String getMackeyname() {
		return mackeyname;
	}
	public void setMackeyname(String mackeyname) {
		this.mackeyname = mackeyname;
	}
	public String getMackeyvalue() {
		return mackeyvalue;
	}
	public void setMackeyvalue(String mackeyvalue) {
		this.mackeyvalue = mackeyvalue;
	}
	public String getMacverifyvalue() {
		return macverifyvalue;
	}
	public void setMacverifyvalue(String macverifyvalue) {
		this.macverifyvalue = macverifyvalue;
	}
	public String getReqdata() {
		return reqdata;
	}
	public void setReqdata(String reqdata) {
		this.reqdata = reqdata;
	}
	public String getResdata() {
		return resdata;
	}
	public void setResdata(String resdata) {
		this.resdata = resdata;
	}
	public String getSuccflag() {
		return succflag;
	}
	public void setSuccflag(String succflag) {
		this.succflag = succflag;
	}
	public String getErrorcode() {
		return errorcode;
	}
	public void setErrorcode(String errorcode) {
		this.errorcode = errorcode;
	}
	public String getErrorinfo() {
		return errorinfo;
	}
	public void setErrorinfo(String errorinfo) {
		this.errorinfo = errorinfo;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	
}
