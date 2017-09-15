package com.haiercash.payplatform.common.data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 顺逛 - 区域代码表
 * Created by Administrator on 2017/4/30.
 */
@Entity
@Table(name = "sg_regions")
public class SgRegions {
    @Id
    private String regionId;//区域ID
    private String province;//省名称
    private String city;//市名称
    private String county;//区县名称
    private String gbCode;//国标区县代码
    private String status;//数据状态：00-不可用01-可用
    private String memo;//备注
    private String zipCode;// 邮政编码

    public String getRegionId() {
        return regionId;
    }

    public void setRegionId(String regionId) {
        this.regionId = regionId;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    public String getGbCode() {
        return gbCode;
    }

    public void setGbCode(String gbCode) {
        this.gbCode = gbCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    @Override
    public String toString() {
        return "SgRegions{" +
                "regionId='" + regionId + '\'' +
                ", province='" + province + '\'' +
                ", city='" + city + '\'' +
                ", county='" + county + '\'' +
                ", gbCode='" + gbCode + '\'' +
                ", status='" + status + '\'' +
                ", memo='" + memo + '\'' +
                ", zipCode='" + zipCode + '\'' +
                '}';
    }
}
