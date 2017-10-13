/*
 * 功  能：简单说明该类的功能
 * 
 * 文件名：CoreResponseBody.java
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
 * <a href="CoreResponseBody.java"><i>View Source</i></a>
 * </p>
 * 
 * @author <a href="mailto:xuchao@haiercash.com">xuchao</a>
 *
 * @version Revision: 1.0  Date: 2016年1月11日 上午10:57:45 
 *
 */
@XStreamAlias("body")
public class CoreResponseBody implements Serializable{
	private Object obj;

	public Object getObj() {
		return obj;
	}

	public void setObj(Object obj) {
		this.obj = obj;
	}
}
