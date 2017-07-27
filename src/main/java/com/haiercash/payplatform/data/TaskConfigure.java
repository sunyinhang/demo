/*
 * 功  能：简单说明该类的功能
 * 
 * 文件名：TaskConfigure.java
 * 
 * 描  述：
 * 
 * [变更履历]
 * Version   变更日         部署              作者           变更内容
 * -----------------------------------------------------------------------------
 * V1.00     2016年1月26日   Haiercash    xuchao      CREATE
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
 *  DESCRIPTION:任务配置类
 *
 * <p>
 * <a href="TaskConfigure.java"><i>View Source</i></a>
 * </p>
 * 
 * @author <a href="mailto:xuchao@haiercash.com">xuchao</a>
 *
 * @version Revision: 1.0  Date: 2016年1月26日 下午10:57:23 
 *
 */

public class TaskConfigure{
	private int id;
	private String channelno;
	private String operationtype;
	private String is_catask;
	private String is_imagetask;
	private String is_thridtask;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getChannelno() {
		return channelno;
	}
	public void setChannelno(String channelno) {
		this.channelno = channelno;
	}
	public String getOperationtype() {
		return operationtype;
	}
	public void setOperationtype(String operationtype) {
		this.operationtype = operationtype;
	}
	public String getIs_catask() {
		return is_catask;
	}
	public void setIs_catask(String is_catask) {
		this.is_catask = is_catask;
	}
	public String getIs_imagetask() {
		return is_imagetask;
	}
	public void setIs_imagetask(String is_imagetask) {
		this.is_imagetask = is_imagetask;
	}
	public String getIs_thridtask() {
		return is_thridtask;
	}
	public void setIs_thridtask(String is_thridtask) {
		this.is_thridtask = is_thridtask;
	}
}
