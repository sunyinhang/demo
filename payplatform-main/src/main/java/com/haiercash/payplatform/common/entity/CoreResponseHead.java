/*
 * 功  能：简单说明该类的功能
 * 
 * 文件名：CoreResponseHead.java
 * 
 * 描  述：
 * 
 * [变更履历]
 * Version   变更日         部署              作者           变更内容
 * -----------------------------------------------------------------------------
 * V1.00     2016年1月11日   Haiercash    xuchao      CREATE
 * -----------------------------------------------------------------------------
 *
 *
 * Copyright (c) 2016 Haiercash All Rights Reserved.
 *┌─────────────────────────────────────────────────—────┐
 *│ 版权声明                               Haiercash       │
 *└──────────────────────────────—————————————————————───┘
 */

package com.haiercash.payplatform.common.entity;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.io.Serializable;

/**
 *  DESCRIPTION:
 *
 * <p>
 * <a href="CoreResponseHead.java"><i>View Source</i></a>
 * </p>
 * 
 * @author <a href="mailto:xuchao@haiercash.com">xuchao</a>
 *
 * @version Revision: 1.0  Date: 2016年1月11日 上午10:56:20 
 *
 */
@XStreamAlias("head")
public class CoreResponseHead implements Serializable{
	@XStreamAlias("retFlag")
	private String retFlag;
	@XStreamAlias("retMsg")
	private String retMsg;
	public String getRetFlag() {
		return retFlag;
	}
	public void setRetFlag(String retFlag) {
		this.retFlag = retFlag;
	}
	public String getRetMsg() {
		return retMsg;
	}
	public void setRetMsg(String retMsg) {
		this.retMsg = retMsg;
	}
}
