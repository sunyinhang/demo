/*
 * 功  能：简单说明该类的功能
 * 
 * 文件名：BchLoanTyp.java
 * 
 * 描  述：
 * 
 * [变更履历]
 * Version   变更日         部署              作者           变更内容
 * -----------------------------------------------------------------------------
 * V1.00     2016年2月14日   Haiercash    xuchao      CREATE
 * -----------------------------------------------------------------------------
 *
 *
 * Copyright (c) 2016 Haiercash All Rights Reserved.
 *┌─────────────────────────────────────────────────—────┐
 *│ 版权声明                               Haiercash       │
 *└──────────────────────────────—————————————————————───┘
 */

package com.haiercash.payplatform.data;

import java.io.Serializable;

/**
 * DESCRIPTION:贷款品种表
 *
 * <p>
 * <a href="BchLoanTyp.java"><i>View Source</i></a>
 * </p>
 * 
 * @author <a href="mailto:xuchao@haiercash.com">xuchao</a>
 *
 * @version Revision: 1.0 Date: 2016年2月14日 下午3:40:11
 *
 */

public class BchLoanTyp implements Serializable{
	private String TYP_CDE;
	private String TYP_DESC;
	private Integer TYP_SEQ;
	private String DOC_CHANNEL;
	private String TNR_OPT;
	private Integer TNR_MAX_DAYS;
	private String DUE_DAY_OPT;
	private String DUE_DAY;
	private String TYP_FREQ;
	private String TNR_OPT1;
	private String MTD_CDE;
	private String MTD_TYP;
	private String BEGIN_DATE;
	private String END_DATE;
	private Integer TYP_VER;
	private String COOPR_CDE;
	private String COOPR_CDE1;
	private String COOPR_NAME;
	private String COOPR_NAME1;
	public String getTYP_CDE() {
		return TYP_CDE;
	}
	public void setTYP_CDE(String tYP_CDE) {
		TYP_CDE = tYP_CDE;
	}
	public String getTYP_DESC() {
		return TYP_DESC;
	}
	public void setTYP_DESC(String tYP_DESC) {
		TYP_DESC = tYP_DESC;
	}
	public Integer getTYP_SEQ() {
		return TYP_SEQ;
	}
	public void setTYP_SEQ(Integer tYP_SEQ) {
		TYP_SEQ = tYP_SEQ;
	}
	public String getDOC_CHANNEL() {
		return DOC_CHANNEL;
	}
	public void setDOC_CHANNEL(String dOC_CHANNEL) {
		DOC_CHANNEL = dOC_CHANNEL;
	}
	public String getTNR_OPT() {
		return TNR_OPT;
	}
	public void setTNR_OPT(String tNR_OPT) {
		TNR_OPT = tNR_OPT;
	}
	public Integer getTNR_MAX_DAYS() {
		return TNR_MAX_DAYS;
	}
	public void setTNR_MAX_DAYS(Integer tNR_MAX_DAYS) {
		TNR_MAX_DAYS = tNR_MAX_DAYS;
	}
	public String getDUE_DAY_OPT() {
		return DUE_DAY_OPT;
	}
	public void setDUE_DAY_OPT(String dUE_DAY_OPT) {
		DUE_DAY_OPT = dUE_DAY_OPT;
	}
	public String getDUE_DAY() {
		return DUE_DAY;
	}
	public void setDUE_DAY(String dUE_DAY) {
		DUE_DAY = dUE_DAY;
	}
	public String getTYP_FREQ() {
		return TYP_FREQ;
	}
	public void setTYP_FREQ(String tYP_FREQ) {
		TYP_FREQ = tYP_FREQ;
	}
	public String getTNR_OPT1() {
		return TNR_OPT1;
	}
	public void setTNR_OPT1(String tNR_OPT1) {
		TNR_OPT1 = tNR_OPT1;
	}
	public String getMTD_CDE() {
		return MTD_CDE;
	}
	public void setMTD_CDE(String mTD_CDE) {
		MTD_CDE = mTD_CDE;
	}
	public String getMTD_TYP() {
		return MTD_TYP;
	}
	public void setMTD_TYP(String mTD_TYP) {
		MTD_TYP = mTD_TYP;
	}
	public String getBEGIN_DATE() {
		return BEGIN_DATE;
	}
	public void setBEGIN_DATE(String bEGIN_DATE) {
		BEGIN_DATE = bEGIN_DATE;
	}
	public String getEND_DATE() {
		return END_DATE;
	}
	public void setEND_DATE(String eND_DATE) {
		END_DATE = eND_DATE;
	}
	public Integer getTYP_VER() {
		return TYP_VER;
	}
	public void setTYP_VER(Integer tYP_VER) {
		TYP_VER = tYP_VER;
	}
	public String getCOOPR_CDE() {
		return COOPR_CDE;
	}
	public void setCOOPR_CDE(String cOOPR_CDE) {
		COOPR_CDE = cOOPR_CDE;
	}
	public String getCOOPR_CDE1() {
		return COOPR_CDE1;
	}
	public void setCOOPR_CDE1(String cOOPR_CDE1) {
		COOPR_CDE1 = cOOPR_CDE1;
	}
	public String getCOOPR_NAME() {
		return COOPR_NAME;
	}
	public void setCOOPR_NAME(String cOOPR_NAME) {
		COOPR_NAME = cOOPR_NAME;
	}
	public String getCOOPR_NAME1() {
		return COOPR_NAME1;
	}
	public void setCOOPR_NAME1(String cOOPR_NAME1) {
		COOPR_NAME1 = cOOPR_NAME1;
	}
}
