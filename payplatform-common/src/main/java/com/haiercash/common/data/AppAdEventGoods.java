package com.haiercash.common.data;

/**
 * Created by Administrator on 2016/9/18.
 * APP活动商品明细表app_ad_event_goods
 */

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "app_ad_event_goods")
public class AppAdEventGoods implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    private String id; //唯一主键-- UUID
    private String adId; //广告ID  app_ad_info.id
    private String goodsCode;//商品编码
    private String salerCode;//销售代表编号
    private Date createTime; //创建时间
    private String remark; //备注
    private Integer tnrOpt;//期数
    private Double repayTnrAmt;//每期应还

    public Double getRepayTnrAmt() {
        return repayTnrAmt;
    }

    public void setRepayTnrAmt(Double repayTnrAmt) {
        this.repayTnrAmt = repayTnrAmt;
    }

    public Integer getTnrOpt() {
        return tnrOpt;
    }

    public void setTnrOpt(Integer tnrOpt) {
        this.tnrOpt = tnrOpt;
    }

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


    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getGoodsCode() {
        return goodsCode;
    }

    public void setGoodsCode(String goodsCode) {
        this.goodsCode = goodsCode;
    }

    public String getSalerCode() {
        return salerCode;
    }

    public void setSalerCode(String salerCode) {
        this.salerCode = salerCode;
    }
}
