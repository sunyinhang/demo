package com.haiercash.common.data;

/**
 * Created by Administrator on 2016/9/18.
 * 广告信息(app_ad_info)
 */

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "app_ad_info")
public class AppAd  implements Serializable{
    private static final long serialVersionUID = 1L;
//    @Transient
//    private String adId; //广告ID  app_ad_info.id
//    @Transient
//    private String sizeType;
//    @Transient
//    private String adphotoId;
    @Id
    private String id; //唯一主键-- UUID
 //   private String adType; //广告类型 1-开屏广告 2-首页焦点广告
    @Deprecated
 //   private String adImg; //图片文件
//    private Integer goodsCode; //商品编码
    private String isActive; //是否启用 Y-启用 N-未启用

    private String isSplash;//是否开屏广告 Y-是  N-否
    private String remark; //备注
    private Date createTime; //创建时间
    private Date activateTime; //启用时间
    private String showTime; //显示时间
  //  private String salerCode; //销售代表编号

    public String getShowTime() {
        return showTime;
    }

    public void setShowTime(String showTime) {
        this.showTime = showTime;
    }

    public String getIsSplash() {
        return isSplash;
    }

    public void setIsSplash(String isSplash) {
        this.isSplash = isSplash;
    }
//    public String getSalerCode() {
//        return salerCode;
//    }
//
//    public void setSalerCode(String salerCode) {
//        this.salerCode = salerCode;
//    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

//    public String getAdType() {
//        return adType;
//    }
//
//    public void setAdType(String adType) {
//        this.adType = adType;
//    }
//
//    public String getAdImg() {
//        return adImg;
//    }
//
//    public void setAdImg(String adImg) {
//        this.adImg = adImg;
//    }

//    public Integer getGoodsCode() {
//        return goodsCode;
//    }
//
//    public void setGoodsCode(Integer goodsCode) {
//        this.goodsCode = goodsCode;
//    }

    public String getIsActive() {
        return isActive;
    }

    public void setIsActive(String isActive) {
        this.isActive = isActive;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getActivateTime() {
        return activateTime;
    }

    public void setActivateTime(Date activateTime) {
        this.activateTime = activateTime;
    }

//    public Integer getGoodsCode() {
//        return goodsCode;
//    }
//
//    public void setGoodsCode(Integer goodsCode) {
//        this.goodsCode = goodsCode;
//    }
//    public String getAdId() {
//        return adId;
//    }
//
//    public void setAdId(String adId) {
//        this.adId = adId;
//    }
//
//    public String getSizeType() {
//        return sizeType;
//    }
//
//    public void setSizeType(String sizeType) {
//        this.sizeType = sizeType;
//    }
//
//
//    public String getAdphotoId() {
//        return adphotoId;
//    }
//
//    public void setAdphotoId(String adphotoId) {
//        this.adphotoId = adphotoId;
//    }
}






//package com.haiercash.appserver.data;
//
///**
// * Created by Administrator on 2016/9/18.
// */
//import javax.persistence.*;
//import java.io.Serializable;
//
//@Entity
//@Table(name = "app_ad_info")
//public class AppAd  implements Serializable{
//    private static final long serialVersionUID = 1L;
//    @Id
//    private String id; //唯一主键-- UUID
//    private String adType; //广告类型 1-开屏广告 2-首页焦点广告
//    private String adImg; //图片文件
//    private Integer goodsCode; //商品编码
//    private String isActive; //是否启用 Y-启用 N-未启用
//    private String remark; //备注
//
//
//    public String getId() {
//        return id;
//    }
//
//    public void setId(String id) {
//        this.id = id;
//    }
//
//    public String getAdType() {
//        return adType;
//    }
//
//    public void setAdType(String adType) {
//        this.adType = adType;
//    }
//
//    public String getAdImg() {
//        return adImg;
//    }
//
//    public void setAdImg(String adImg) {
//        this.adImg = adImg;
//    }
//
//    public Integer getGoodsCode() {
//        return goodsCode;
//    }
//
//    public void setGoodsCode(Integer goodsCode) {
//        this.goodsCode = goodsCode;
//    }
//
//    public String getIsActive() {
//        return isActive;
//    }
//
//    public void setIsActive(String isActive) {
//        this.isActive = isActive;
//    }
//
//    public String getRemark() {
//        return remark;
//    }
//
//    public void setRemark(String remark) {
//        this.remark = remark;
//    }
//
//}
