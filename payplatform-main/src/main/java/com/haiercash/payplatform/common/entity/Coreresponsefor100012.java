/*
 * 功  能：简单说明该类的功能
 * 
 * 文件名：Coreresponse.java
 * 
 * 描  述：
 * 
 * [变更履历]
 * Version   变更日         部署              作者           变更内容
 * -----------------------------------------------------------------------------
 * V1.00     2015年12月23日   Haiercash    xuchao      CREATE
 * -----------------------------------------------------------------------------
 *
 *
 * Copyright (c) 2015 Haiercash All Rights Reserved.
 *┌─────────────────────────────────────────────────—────┐
 *│ 版权声明                               Haiercash       │
 *└──────────────────────────────—————————————————————───┘
 */

package com.haiercash.payplatform.common.entity;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * DESCRIPTION:核心返回信息
 *
 * <p>
 * <a href="Coreresponse.java"><i>View Source</i></a>
 * </p>
 * 
 * @author <a href="mailto:xuchao@haiercash.com">xuchao</a>
 *
 * @version Revision: 1.0 Date: 2015年12月23日 下午10:38:25
 *
 */
@XStreamAlias("response")
public class Coreresponsefor100012 {
	// 报文头
	@XStreamAlias("head")
	private CoreResponseHead head;
	@XStreamAlias("body")
	private CoreResponseBody body;
	public CoreResponseHead getHead() {
		return head;
	}
	public void setHead(CoreResponseHead head) {
		this.head = head;
	}
	public CoreResponseBody getBody() {
		return body;
	}
	public void setBody(CoreResponseBody body) {
		this.body = body;
	}
}
