package com.haiercash.common.data;

/**
 * 
 * <p>
 * 标题： 客户家庭成员关系类，为CustomerInfoBean的List属性
 * </p>
 * <p>
 * 功能描述：
 * </p>
 * <p>
 * 创建日期：2016年4月12日 下午3:09:08
 * </p>
 * <p>
 * 作者：尹君
 * </p>
 * <p>
 * 公司：济南百思为科
 * </p>
 * 
 */

public class CustomerFamilyBean {

	/**
	 * 联系人姓名 relName
	 */
	private String relName;
	/**
	 * 与申请人关系 relRelation
	 */
	private String relRelation;
	/**
	 * 联系人电话 relMobile
	 */
	private String relMobile;
	/**
	 * 所在单位 relEmpName
	 */
	private String relEmpName;
	/**
	 * 联系人居住地址 relAddr
	 */
	private String relAddr;

	public String getRelName() {
		return relName;
	}

	public void setRelName(String relName) {
		this.relName = relName;
	}

	public String getRelRelation() {
		return relRelation;
	}

	public void setRelRelation(String relRelation) {
		this.relRelation = relRelation;
	}

	public String getRelMobile() {
		return relMobile;
	}

	public void setRelMobile(String relMobile) {
		this.relMobile = relMobile;
	}

	public String getRelEmpName() {
		return relEmpName;
	}

	public void setRelEmpName(String relEmpName) {
		this.relEmpName = relEmpName;
	}

	public String getRelAddr() {
		return relAddr;
	}

	public void setRelAddr(String relAddr) {
		this.relAddr = relAddr;
	}

}
