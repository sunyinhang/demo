package com.haiercash.common.data;

/**
 * 描述：贷款申请中的商品bean
 * 
 * @author 尹君
 *
 */
@Deprecated
public class GoodsBean {
	private String goods_brand;// 产品/服务品牌
	private String goods_kind;// 产品/服务类型
	private String goods_name;// 商品名称
	private String goods_model;// 商品型号
	private Integer goods_num;// 商品数量
	private Double goods_price;// 商品单价（元）

	public String getGoods_brand() {
		return goods_brand;
	}

	public void setGoods_brand(String goods_brand) {
		this.goods_brand = goods_brand;
	}

	public String getGoods_kind() {
		return goods_kind;
	}

	public void setGoods_kind(String goods_kind) {
		this.goods_kind = goods_kind;
	}

	public String getGoods_name() {
		return goods_name;
	}

	public void setGoods_name(String goods_name) {
		this.goods_name = goods_name;
	}

	public String getGoods_model() {
		return goods_model;
	}

	public void setGoods_model(String goods_model) {
		this.goods_model = goods_model;
	}

	public Integer getGoods_num() {
		return goods_num;
	}

	public void setGoods_num(Integer goods_num) {
		this.goods_num = goods_num;
	}

	public Double getGoods_price() {
		return goods_price;
	}

	public void setGoods_price(Double goods_price) {
		this.goods_price = goods_price;
	}

}
