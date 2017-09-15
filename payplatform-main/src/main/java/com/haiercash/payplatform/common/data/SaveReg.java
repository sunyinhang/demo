package com.haiercash.payplatform.common.data;

public class SaveReg {
	private String saveId;//注册信息表主键
	private String userId;//用户账号
	private String channel;//系统标识
	private String channelNo;//渠道编码
	public String getSaveId() {
		return saveId;
	}
	public void setSaveId(String saveId) {
		this.saveId = saveId;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getChannel() {
		return channel;
	}
	public void setChannel(String channel) {
		this.channel = channel;
	}
	public String getChannelNo() {
		return channelNo;
	}
	public void setChannelNo(String channelNo) {
		this.channelNo = channelNo;
	}
	@Override
	public String toString() {
		return "SaveReg [saveId=" + saveId + ", userId=" + userId + ", channel=" + channel + ", channelNo=" + channelNo
				+ "]";
	}
	
}
