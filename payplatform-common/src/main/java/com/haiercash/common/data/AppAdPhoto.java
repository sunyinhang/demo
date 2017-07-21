package com.haiercash.common.data;

/**
 * Created by Administrator on 2016/9/18.
 * 广告图片(app_ad_photo)
 */

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "app_ad_photo")
public class AppAdPhoto implements Serializable{
    private static final long serialVersionUID = 1L;
    @Id
    private String id; //唯一主键-- UUID
    private String adId; //广告ID  app_ad_info.id
    private String adImg; //图片文件 文件名，含相对路径
    private String imgType;//图片类型
    private String sizeType; //尺寸类型 取值范围：AND480, AND720, AND1080, IOS568, IOS667, IOS736, IOS480private String imgType;//图片类型
    private String  md5;//图片md5码
  //  private String size;//尺寸类型
    private Date createTime; //创建时间
    private String remark; //备注
    private Integer displayWidth; //display_width
    private Integer displayHeight;//display_height

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAdId() {
        return adId;
    }

    public void setAdId(String adId) {
        this.adId = adId;
    }

    public String getAdImg() {
        return adImg;
    }

    public void setAdImg(String adImg) {
        this.adImg = adImg;
    }

    public Integer getDisplayHeight() {
        return displayHeight;
    }

    public void setDisplayHeight(Integer displayHeight) {
        this.displayHeight = displayHeight;
    }

    public Integer getDisplayWidth() {
        return displayWidth;
    }

    public void setDisplayWidth(Integer displayWidth) {
        this.displayWidth = displayWidth;
    }
//    public String getSizeType() {
//        return sizeType;
//    }
//
//    public void setSizeType(String sizeType) {
//        this.sizeType = sizeType;
//    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

//    public String getImgType() {
//        return imgType;
//    }
//
//    public void setImgType(String imgType) {
//        this.imgType = imgType;
//    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

//    public String getSize() {
//        return size;
//    }
//
//    public void setSize(String size) {
//        this.size = size;
//    }

    public String getImgType() {
        return imgType;
    }

    public void setImgType(String imgType) {
        this.imgType = imgType;
    }

    public String getSizeType() {
        return sizeType;
    }

    public void setSizeType(String sizeType) {
        this.sizeType = sizeType;
    }
}
