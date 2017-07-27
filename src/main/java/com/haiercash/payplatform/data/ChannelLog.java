/*
 * 功  能：简单说明该类的功能
 *
 * 文件名：ChannelLog.java
 *
 * 描  述：
 *
 * [变更履历]
 * Version   变更日         部署              作者           变更内容
 * -----------------------------------------------------------------------------
 * V1.00     2015年5月18日   haiercash    suyang      CREATE
 * -----------------------------------------------------------------------------
 *
 *
 * Copyright (c) 2015 haiercash All Rights Reserved.
 *┌─────────────────────────────────────────────────—────┐
 *│ 版权声明                               haiercash       │
 *└──────────────────────────────—————————————————————───┘
 */
package com.haiercash.payplatform.data;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 *  DESCRIPTION:渠道日志
 *
 * <p>
 * <a href="BusinessLog.java"><i>View Source</i></a>
 * </p>
 *
 * @author <a href="mailto:suyang@haiercash.com">suyang</a>
 *
 * @version Revision: 1.0  Date: 2015年5月18日 下午3:39:36
 *
 */
@XStreamAlias("ChannelLog")
public class ChannelLog {
	private String applCde;
	private String channelNo;
	private String custno;
	private String outer_custno;//客户ID
	private String orderNo;//订单编号
	private String approve_status;
	private String appno;
	private String operationType;
	private String optime;
	private String applyType;
	private String loopback1;
	private String loopback2;
	private String appl_seq;
	private String EXT1;
	private String EXT2;
	private String EXT3;
	private String flag;
	private int pushnum;
	
	public String getOperationType() {
		return operationType;
	}
	public void setOperationType(String operationType) {
		this.operationType = operationType;
	}
	public String getOptime() {
		return optime;
	}
	public void setOptime(String optime) {
		this.optime = optime;
	}
	public String getApplyType() {
		return applyType;
	}
	public void setApplyType(String applyType) {
		this.applyType = applyType;
	}
	public String getOuter_custno() {
		return outer_custno;
	}
	public void setOuter_custno(String outer_custno) {
		this.outer_custno = outer_custno;
	}
	public String getCustno() {
		return custno;
	}
	public void setCustno(String custno) {
		this.custno = custno;
	}
	public String getAppl_seq() {
		return appl_seq;
	}
	public void setAppl_seq(String appl_seq) {
		this.appl_seq = appl_seq;
	}
	public String getEXT1() {
		return EXT1;
	}
	public void setEXT1(String eXT1) {
		EXT1 = eXT1;
	}
	public String getEXT2() {
		return EXT2;
	}
	public void setEXT2(String eXT2) {
		EXT2 = eXT2;
	}
	public String getEXT3() {
		return EXT3;
	}
	public void setEXT3(String eXT3) {
		EXT3 = eXT3;
	}
	public String getLoopback1() {
		return loopback1;
	}
	public void setLoopback1(String loopback1) {
		this.loopback1 = loopback1;
	}
	public String getLoopback2() {
		return loopback2;
	}
	public void setLoopback2(String loopback2) {
		this.loopback2 = loopback2;
	}
	public String getApprove_status() {
		return approve_status;
	}
	public void setApprove_status(String approve_status) {
		this.approve_status = approve_status;
	}
	public String getAppno() {
		return appno;
	}
	public void setAppno(String appno) {
		this.appno = appno;
	}
	public String getOrderNo() {
		return orderNo;
	}
	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}
	public String getApplCde() {
		return applCde;
	}
	public void setApplCde(String applCde) {
		this.applCde = applCde;
	}
	public String getChannelNo() {
		return channelNo;
	}
	public void setChannelNo(String channelNo) {
		this.channelNo = channelNo;
	}
	public String getFlag() {
		return flag;
	}
	public void setFlag(String flag) {
		this.flag = flag;
	}
	public int getPushnum() {
		return pushnum;
	}
	public void setPushnum(int pushnum) {
		this.pushnum = pushnum;
	}
	
	
	
	
}
