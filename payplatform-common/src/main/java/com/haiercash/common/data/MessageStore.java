package com.haiercash.common.data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "message_store")
public class MessageStore {
	@Id
	private String id;
	private String msgTyp;// 消息类型
	private String msgTitil;// 消息标题
	private String message;// 消息主体
	private String applSeq;// 申请号
	private String typGrp;// 贷款类型
	private String idNo;// 身份证
	private String usrCde;// 录单人员（销售代表user_id）
	private String usrName;// 录单人员（销售代表user_name）
	private String cooprCde;// 商户号
	private String scooprCde;// 门店号
	private String reserved1;// 预留字段1
	private String reserved2;// 预留字段2
	private String reserved3;// 预留字段3
	private String reserved4;// 预留字段4
	private String reserved5;// 预留字段5
	private String userId;// 客户的user_id，可能为空
	private String flag;// 已读标识
	private String pullDate;// 推送时间
	private String status;// 状态
	private String ip;// 来源ip
	private String port;// 端口号
	private String updateDate;// 更新时间
	private String indivMobile; // 用户绑定手机号
	private String isSend; // 短信发送标识
	private String channel; // sysFlag
	private String outplat; // 是否已通知外联平台标识 0未发送 1已发送 2暂不处理
	private String channelNo; // 渠道号
	private String outSts; // 审批状态

	public String getUsrName() {
		return usrName;
	}

	public void setUsrName(String usrName) {
		this.usrName = usrName;
	}

	public String getTypGrp() {
		return typGrp;
	}

	public void setTypGrp(String typGrp) {
		this.typGrp = typGrp;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(String updateDate) {
		this.updateDate = updateDate;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getFlag() {
		return flag;
	}

	public void setFlag(String flag) {
		this.flag = flag;
	}

	public String getPullDate() {
		return pullDate;
	}

	public void setPullDate(String pullDate) {
		this.pullDate = pullDate;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getMsgTyp() {
		return msgTyp;
	}

	public void setMsgTyp(String msgTyp) {
		this.msgTyp = msgTyp;
	}

	public String getMsgTitil() {
		return msgTitil;
	}

	public void setMsgTitil(String msgTitil) {
		this.msgTitil = msgTitil;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getApplSeq() {
		return applSeq;
	}

	public void setApplSeq(String applSeq) {
		this.applSeq = applSeq;
	}

	public String getIdNo() {
		return idNo;
	}

	public void setIdNo(String idNo) {
		this.idNo = idNo;
	}

	public String getUsrCde() {
		return usrCde;
	}

	public void setUsrCde(String usrCde) {
		this.usrCde = usrCde;
	}

	public String getCooprCde() {
		return cooprCde;
	}

	public void setCooprCde(String cooprCde) {
		this.cooprCde = cooprCde;
	}

	public String getScooprCde() {
		return scooprCde;
	}

	public void setScooprCde(String scooprCde) {
		this.scooprCde = scooprCde;
	}

	public String getReserved1() {
		return reserved1;
	}

	public void setReserved1(String reserved1) {
		this.reserved1 = reserved1;
	}

	public String getReserved2() {
		return reserved2;
	}

	public void setReserved2(String reserved2) {
		this.reserved2 = reserved2;
	}

	public String getReserved3() {
		return reserved3;
	}

	public void setReserved3(String reserved3) {
		this.reserved3 = reserved3;
	}

	public String getReserved4() {
		return reserved4;
	}

	public void setReserved4(String reserved4) {
		this.reserved4 = reserved4;
	}

	public String getReserved5() {
		return reserved5;
	}

	public void setReserved5(String reserved5) {
		this.reserved5 = reserved5;
	}


    public String getIsSend() {
		return isSend;
	}

	public void setIsSend(String isSend) {
		this.isSend = isSend;
	}

	public String getIndivMobile() {
		return indivMobile;
	}

	public void setIndivMobile(String indivMobile) {
		this.indivMobile = indivMobile;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}


	public String getOutplat() {
		return outplat;
	}

	public void setOutplat(String outplat) {
		this.outplat = outplat;
	}

	public String getChannelNo() {
		return channelNo;
	}

	public void setChannelNo(String channelNo) {
		this.channelNo = channelNo;
	}

	public String getOutSts() {
		return outSts;
	}

	public void setOutSts(String outSts) {
		this.outSts = outSts;
	}

	@Override
	public String toString() {
		return "MessageStore{" +
				"id='" + id + '\'' +
				", msgTyp='" + msgTyp + '\'' +
				", msgTitil='" + msgTitil + '\'' +
				", message='" + message + '\'' +
				", applSeq='" + applSeq + '\'' +
				", typGrp='" + typGrp + '\'' +
				", idNo='" + idNo + '\'' +
				", usrCde='" + usrCde + '\'' +
				", usrName='" + usrName + '\'' +
				", cooprCde='" + cooprCde + '\'' +
				", scooprCde='" + scooprCde + '\'' +
				", reserved1='" + reserved1 + '\'' +
				", reserved2='" + reserved2 + '\'' +
				", reserved3='" + reserved3 + '\'' +
				", reserved4='" + reserved4 + '\'' +
				", reserved5='" + reserved5 + '\'' +
				", userId='" + userId + '\'' +
				", flag='" + flag + '\'' +
				", pullDate='" + pullDate + '\'' +
				", status='" + status + '\'' +
				", ip='" + ip + '\'' +
				", port='" + port + '\'' +
				", updateDate='" + updateDate + '\'' +
				", indivMobile='" + indivMobile + '\'' +
				", isSend='" + isSend + '\'' +
				", channel='" + channel + '\'' +
				", outplat='" + outplat + '\'' +
				", channelNo='" + channelNo + '\'' +
				", outSts='" + outSts + '\'' +
				'}';
	}
}
