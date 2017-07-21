/*
 * 功  能：简单说明该类的功能
 * 
 * 文件名：CAResponse.java
 * 
 * 描  述：
 * 
 * [变更履历]
 * Version   变更日         部署              作者           变更内容
 * -----------------------------------------------------------------------------
 * V1.00     2016年2月21日   Haiercash    xuchao      CREATE
 * -----------------------------------------------------------------------------
 *
 *
 * Copyright (c) 2016 Haiercash All Rights Reserved.
 *┌─────────────────────────────────────────────────—────┐
 *│ 版权声明                               Haiercash       │
 *└──────────────────────────────—————————————————————───┘
 */

package com.haiercash.appserver.util.sign.ca;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * DESCRIPTION:
 * <p>
 * <p>
 * <a href="CAResponse.java"><i>View Source</i></a>
 * </p>
 *
 * @author <a href="mailto:xuchao@haiercash.com">xuchao</a>
 * @version Revision: 1.0 Date: 2016年2月21日 下午9:44:05
 */
@XmlRootElement
public class CAResponse {
	private String code;
	private String isOK;
	private String message;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getIsOK() {
		return isOK;
	}

	public void setIsOK(String isOK) {
		this.isOK = isOK;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
