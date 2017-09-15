package com.haiercash.payplatform.common.data;

import javax.persistence.Transient;
import java.util.Arrays;

public class AppOrderGoods {
	private String seqNo;// 商品流水号
	private String orderNo;// 订单号
	private String goodsCode;// 商品代码
	private String goodsBrand;// 商品品牌
	private String goodsKind;// 商品类型
	private String goodsName;// 商品名称
	private String goodsModel;// 商品型号
	private String goodsNum;// 数量
	private String goodsPrice;// 单价
	private String cOrderSn;//网单号

	private String brandName;//商品品牌
	private String skuCode;//sku码

	/**====以下字段为商品管理系统需要的字段====**/
	@Transient
	private String brandCode;//商品品牌代码
	@Transient
	private String kindCode;//商品类型代码
	@Transient
	private String goodsDesc;//商品描述
	@Transient
	private String merchantCode;//商户代码
	@Transient
	private String[] storeCode;//门店代码
	@Transient
	private String[]	storeName;//门店名称
	@Transient
	private String state;//状态
	@Transient
	private String goodsLine;//线上线下
	@Transient
	private String haveMenu;//有无套餐
	@Transient
	private String[] loanCode;//贷款品种
	@Transient
	private String  lastChgUser;//最后修改用户

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	@Transient
	private String  version;//版本号



	public String getSeqNo() {
		return seqNo;
	}

	public void setSeqNo(String seqNo) {
		this.seqNo = seqNo;
	}

	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}

	public String getGoodsCode() {
		return goodsCode;
	}

	public void setGoodsCode(String goodsCode) {
		this.goodsCode = goodsCode;
	}

	public String getGoodsBrand() {
		return goodsBrand;
	}

	public void setGoodsBrand(String goodsBrand) {
		this.goodsBrand = goodsBrand;
	}

	public String getGoodsKind() {
		return goodsKind;
	}

	public void setGoodsKind(String goodsKind) {
		this.goodsKind = goodsKind;
	}

	public String getGoodsName() {
		return goodsName;
	}

	public void setGoodsName(String goodsName) {
		this.goodsName = goodsName;
	}

	public String getGoodsModel() {
		return goodsModel;
	}

	public void setGoodsModel(String goodsModel) {
		this.goodsModel = goodsModel;
	}

	public String getGoodsNum() {
		return goodsNum;
	}

	public void setGoodsNum(String goodsNum) {
		this.goodsNum = goodsNum;
	}

	public String getGoodsPrice() {
		return goodsPrice;
	}

	public void setGoodsPrice(String goodsPrice) {
		this.goodsPrice = goodsPrice;
	}

	public String getBrandCode() {
		return brandCode;
	}

	public void setBrandCode(String brandCode) {
		this.brandCode = brandCode;
	}

	public String getGoodsDesc() {
		return goodsDesc;
	}

	public void setGoodsDesc(String goodsDesc) {
		this.goodsDesc = goodsDesc;
	}

	public String getGoodsLine() {
		return goodsLine;
	}

	public void setGoodsLine(String goodsLine) {
		this.goodsLine = goodsLine;
	}

	public String getHaveMenu() {
		return haveMenu;
	}

	public void setHaveMenu(String haveMenu) {
		this.haveMenu = haveMenu;
	}

	public String getKindCode() {
		return kindCode;
	}

	public void setKindCode(String kindCode) {
		this.kindCode = kindCode;
	}

	public String getLastChgUser() {
		return lastChgUser;
	}

	public void setLastChgUser(String lastChgUser) {
		this.lastChgUser = lastChgUser;
	}



	public String getMerchantCode() {
		return merchantCode;
	}

	public void setMerchantCode(String merchantCode) {
		this.merchantCode = merchantCode;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String[] getLoanCode() {
		return loanCode;
	}

	public void setLoanCode(String[] loanCode) {
		this.loanCode = loanCode;
	}

	public String[] getStoreCode() {
		return storeCode;
	}

	public void setStoreCode(String[] storeCode) {
		this.storeCode = storeCode;
	}

	public String[] getStoreName() {
		return storeName;
	}

	public void setStoreName(String[] storeName) {
		this.storeName = storeName;
	}

	public String getcOrderSn() {
		return cOrderSn;
	}

	public void setcOrderSn(String cOrderSn) {
		this.cOrderSn = cOrderSn;
	}

	public String getBrandName() {
		return brandName;
	}

	public void setBrandName(String brandName) {
		this.brandName = brandName;
	}

	public String getSkuCode() {
		return skuCode;
	}

	public void setSkuCode(String skuCode) {
		this.skuCode = skuCode;
	}

	@Override
	public String toString() {
		return "AppOrderGoods{" +
				"brandCode='" + brandCode + '\'' +
				", seqNo='" + seqNo + '\'' +
				", orderNo='" + orderNo + '\'' +
				", goodsCode='" + goodsCode + '\'' +
				", goodsBrand='" + goodsBrand + '\'' +
				", goodsKind='" + goodsKind + '\'' +
				", goodsName='" + goodsName + '\'' +
				", goodsModel='" + goodsModel + '\'' +
				", goodsNum='" + goodsNum + '\'' +
				", goodsPrice='" + goodsPrice + '\'' +
				", kindCode='" + kindCode + '\'' +
				", goodsDesc='" + goodsDesc + '\'' +
				", merchantCode='" + merchantCode + '\'' +
				", storeCode=" + Arrays.toString(storeCode) +
				", storeName=" + Arrays.toString(storeName) +
				", state='" + state + '\'' +
				", goodsLine='" + goodsLine + '\'' +
				", haveMenu='" + haveMenu + '\'' +
				", loanCode=" + Arrays.toString(loanCode) +
				", lastChgUser='" + lastChgUser + '\'' +
				", version='" + version + '\'' +
				'}';
	}
}
