package com.haiercash.common.data;

import java.util.ArrayList;
import java.util.List;

public class FTPBean {
	private String sysId;// 系统标识
	private String busId;// 业务标识
	private String applSeq;// 业务流号
	private String reserved1;// 预留字段1
	private String reserved2;// 预留字段2
	private String reserved3;// 预留字段3
	private String reserved4;// 预留字段4
	private String reserved5;// 预留字段5

	private List<FTPBeanListInfo> list = new ArrayList<FTPBeanListInfo>();

	public List<FTPBeanListInfo> getList() {
		return list;
	}

	public void setList(List<FTPBeanListInfo> list) {
		this.list = list;
	}

	public String getSysId() {
		return sysId;
	}

	public void setSysId(String sysId) {
		this.sysId = sysId;
	}

	public String getBusId() {
		return busId;
	}

	public void setBusId(String busId) {
		this.busId = busId;
	}

	public String getApplSeq() {
		return applSeq;
	}

	public void setApplSeq(String applSeq) {
		this.applSeq = applSeq;
	}

	public String getReserved1() {
		return reserved1;
	}

	public void setReserved1(String reserved1) {
		this.reserved1 = reserved1;
	}

	public String getReserved2() {
		return reserved2;
	}

	public void setReserved2(String reserved2) {
		this.reserved2 = reserved2;
	}

	public String getReserved3() {
		return reserved3;
	}

	public void setReserved3(String reserved3) {
		this.reserved3 = reserved3;
	}

	public String getReserved4() {
		return reserved4;
	}

	public void setReserved4(String reserved4) {
		this.reserved4 = reserved4;
	}

	public String getReserved5() {
		return reserved5;
	}

	public void setReserved5(String reserved5) {
		this.reserved5 = reserved5;
	}

}
