/*
 * 功  能：简单说明该类的功能
 * 
 * 文件名：CaTask.java
 * 
 * 描  述：
 * 
 * [变更履历]
 * Version   变更日         部署              作者           变更内容
 * -----------------------------------------------------------------------------
 * V1.00     2016年1月5日   Haiercash    xuchao      CREATE
 * -----------------------------------------------------------------------------
 *
 *
 * Copyright (c) 2016 Haiercash All Rights Reserved.
 *┌─────────────────────────────────────────────────—────┐
 *│ 版权声明                               Haiercash       │
 *└──────────────────────────────—————————————————————───┘
 */

package com.haiercash.payplatform.data;

import java.util.Map;

/**
 *  DESCRIPTION:CaTask表pojo类
 *
 * <p>
 * <a href="CaTask.java"><i>View Source</i></a>
 * </p>
 * 
 * @author <a href="mailto:xuchao@haiercash.com">xuchao</a>
 *
 * @version Revision: 1.0  Date: 2016年1月5日 下午6:15:21 
 *
 */

public class CaTask {
	private String ID;
	private String USERID;
	private String USERNAME;
	private String APPLSEQ;
	private String CONTTYPE;
	private String STATUS;
	private String MSG;
	private Integer OPLEVEL;
	private String TRANSTIME;
	private Map<String,Object> paramMap;//add by 'zsc'
	public String getID() {
		return ID;
	}
	public void setID(String iD) {
		ID = iD;
	}
	public String getUSERID() {
		return USERID;
	}
	public void setUSERID(String uSERID) {
		USERID = uSERID;
	}
	public String getUSERNAME() {
		return USERNAME;
	}
	public void setUSERNAME(String uSERNAME) {
		USERNAME = uSERNAME;
	}
	public String getAPPLSEQ() {
		return APPLSEQ;
	}
	public void setAPPLSEQ(String aPPLSEQ) {
		APPLSEQ = aPPLSEQ;
	}
	public String getCONTTYPE() {
		return CONTTYPE;
	}
	public void setCONTTYPE(String cONTTYPE) {
		CONTTYPE = cONTTYPE;
	}
	public String getSTATUS() {
		return STATUS;
	}
	public void setSTATUS(String sTATUS) {
		STATUS = sTATUS;
	}
	public String getMSG() {
		return MSG;
	}
	public void setMSG(String mSG) {
		MSG = mSG;
	}
	
	public Integer getOPLEVEL() {
		return OPLEVEL;
	}
	public void setOPLEVEL(Integer oPLEVEL) {
		OPLEVEL = oPLEVEL;
	}
	public String getTRANSTIME() {
		return TRANSTIME;
	}
	public void setTRANSTIME(String tRANSTIME) {
		TRANSTIME = tRANSTIME;
	}
	public Map<String, Object> getParamMap() {
		return paramMap;
	}
	public void setParamMap(Map<String, Object> paramMap) {
		this.paramMap = paramMap;
	}
	
	
}
