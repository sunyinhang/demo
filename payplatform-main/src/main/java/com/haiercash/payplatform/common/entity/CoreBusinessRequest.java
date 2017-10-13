package com.haiercash.payplatform.common.entity;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * 
 *  DESCRIPTION:构建核心接口请求xml
 * <p>
 * <a href="CoreBusinessRequest.java"><i>View Source</i></a>
 * </p>
 * 
 * @author <a href="mailto:xuchao@haiercash.com">xuchao</a>
 *
 * @version Revision: 1.0  Date: 2015年12月23日 下午4:50:14 
 *
 */
@XStreamAlias("request")
public class CoreBusinessRequest {
	// 报文头
	@XStreamAlias("head")
	private Object head;
	@XStreamAlias("body")
	private Object body;

	public Object getHead() {
		return head;
	}

	public void setHead(Object head) {
		this.head = head;
	}

	public Object getBody() {
		return body;
	}

	public void setBody(Object body) {
		this.body = body;
	}
}
