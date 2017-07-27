package com.haiercash.payplatform.data;

import java.util.Date;

public class BaiChengTimerOperation {
	private String id; //ID 主键
	private String status;//状态；Y：开启 ；  N：关闭
	private Date create_date;//创建时间
	private Date updata_date;//修改时间
	private String mark;//备注
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Date getCreate_date() {
		return create_date;
	}
	public void setCreate_date(Date create_date) {
		this.create_date = create_date;
	}
	public Date getUpdata_date() {
		return updata_date;
	}
	public void setUpdata_date(Date updata_date) {
		this.updata_date = updata_date;
	}
	public String getMark() {
		return mark;
	}
	public void setMark(String mark) {
		this.mark = mark;
	}
	
}
