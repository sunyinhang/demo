/*
 * 功  能：简单说明该类的功能
 * 
 * 文件名：CooperationTask.java
 * 
 * 描  述：
 * 
 * [变更履历]
 * Version   变更日         部署              作者           变更内容
 * -----------------------------------------------------------------------------
 * V1.00     2016年1月22日   Haiercash    xuchao      CREATE
 * -----------------------------------------------------------------------------
 *
 *
 * Copyright (c) 2016 Haiercash All Rights Reserved.
 *┌─────────────────────────────────────────────────—────┐
 *│ 版权声明                               Haiercash       │
 *└──────────────────────────────—————————————————————───┘
 */

package com.haiercash.payplatform.data;

/**
 *  DESCRIPTION:
 *
 * <p>
 * <a href="CooperationTask.java"><i>View Source</i></a>
 * </p>
 * 
 * @author <a href="mailto:xuchao@haiercash.com">xuchao</a>
 *
 * @version Revision: 1.0  Date: 2016年1月22日 上午10:29:56 
 *
 */

public class CooperationTask {
	private String  ID;
	private String  applSeq;
	private String  cooperationNo;
	private String  custno;
	private String  status;
	private String  optime;
	private String  msg;
	
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public String getID() {
		return ID;
	}
	public void setID(String iD) {
		ID = iD;
	}
	public String getApplSeq() {
		return applSeq;
	}
	public void setApplSeq(String applSeq) {
		this.applSeq = applSeq;
	}
	public String getCooperationNo() {
		return cooperationNo;
	}
	public void setCooperationNo(String cooperationNo) {
		this.cooperationNo = cooperationNo;
	}
	public String getCustno() {
		return custno;
	}
	public void setCustno(String custno) {
		this.custno = custno;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getOptime() {
		return optime;
	}
	public void setOptime(String optime) {
		this.optime = optime;
	}
}
