package com.haiercash.payplatform.common.data;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Data
@Entity
@Table(name = "VIPABC_ORDER_INFO")
public class VipAbcAppOrderGoods implements Serializable {
    @Id
    private String orderid;
    private String orderSn;
    private String loanType;
    private String payAmt;
    private String province;
    private String city;
    private String country;
    private String detailAddress;
    private String orderDate;
    private String applyTnr;
    private String cOrderSn;
    private String topLevel;
    private String model;
    private String sku;
    private String price;
    private String num;
    private String cOrderAmt;
    private String cOrderPayAmt;
    private String applseq;
    private String orderNo;
    private String vipuuid;
    private String remark;
    private String idCard;
    private String inputtime;
    private String saveordertime;

}
