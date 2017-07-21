package com.haiercash.common.data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "APP_CITY_RANGE")
public class CityBean {
	@Id
	private String id; // 主键
	private String provinceCode; // 省代码
	private String cityCode; // 市代码
	private String admit; // 是否准入 0-不准入 1-准入
	private String typLevelTwo;//二级分类字段
	private String channel;//二级分类字段
	private String isChina;//是否全国

	public String getIsChina() {
		return isChina;
	}

	public void setIsChina(String isChina) {
		this.isChina = isChina;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getTypLevelTwo() {
		return typLevelTwo;
	}

	public void setTypLevelTwo(String typLevelTwo) {
		this.typLevelTwo = typLevelTwo;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getProvinceCode() {
		return provinceCode;
	}

	public void setProvinceCode(String provinceCode) {
		this.provinceCode = provinceCode;
	}

	public String getCityCode() {
		return cityCode;
	}

	public void setCityCode(String cityCode) {
		this.cityCode = cityCode;
	}

	public String getAdmit() {
		return admit;
	}

	public void setAdmit(String admit) {
		this.admit = admit;
	}

}
