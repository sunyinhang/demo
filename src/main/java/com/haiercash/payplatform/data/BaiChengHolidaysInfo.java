package com.haiercash.payplatform.data;

import java.util.Date;

public class BaiChengHolidaysInfo {
	private String id;//主键
	private Date horwdate;//日期
	private String falg;//标志
	private String year;//年份
	private String mark;//备注
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Date getHorwdate() {
		return horwdate;
	}
	public void setHorwdate(Date horwdate) {
		this.horwdate = horwdate;
	}
	public String getFalg() {
		return falg;
	}
	public void setFalg(String falg) {
		this.falg = falg;
	}
	public String getYear() {
		return year;
	}
	public void setYear(String year) {
		this.year = year;
	}
	public String getMark() {
		return mark;
	}
	public void setMark(String mark) {
		this.mark = mark;
	}
	
}
