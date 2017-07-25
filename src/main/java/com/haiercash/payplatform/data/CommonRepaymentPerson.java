package com.haiercash.payplatform.data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

/**
 * 共同还款人实体类
 *
 * @author 尹君
 */
@Entity
@Table(name = "common_repayment_person")
public class CommonRepaymentPerson {
    /**
     * 订单号、客户编号、关系、婚姻状况、工作单位、月收入、单位电话、共同还款人客户编号、短信验证码
     **/
    @Id
    private String id;// 主键
    private String orderNo;// 订单编号
    @Deprecated
    private String custNo;// 客户编号；1.1.0版本开始，共同还款人不会生成客户编号
    private String relation;// 关系
    private String maritalStatus;// 婚姻状况
    private String officeName;// 工作单位
    private BigDecimal mthInc;// 月收入
    private String officeTel;// 单位电话
    private String commonCustNo;// 共同还款人客户编号
    private String smsCode;// 短信验证码
    private String applSeq; // 信贷流水号
    private String name;//姓名
    private String idNo;//身份证号
    private String mobile;//手机号
    private String cardNo;//银行卡号
    private String repayAcProvince;// 还款账户所在省
    private String repayAcCity;// 还款账户所在市

    public String getRepayAcProvince() {
        return repayAcProvince;
    }

    public void setRepayAcProvince(String repayAcProvince) {
        this.repayAcProvince = repayAcProvince;
    }

    public String getRepayAcCity() {
        return repayAcCity;
    }

    public void setRepayAcCity(String repayAcCity) {
        this.repayAcCity = repayAcCity;
    }

    public String getCardNo() {
        return cardNo;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }

    public String getIdNo() {
        return idNo;
    }

    public void setIdNo(String idNo) {
        this.idNo = idNo;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    @Deprecated
    public String getCustNo() {
        return custNo;
    }

    @Deprecated
    public void setCustNo(String custNo) {
        this.custNo = custNo;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    public String getMaritalStatus() {
        return maritalStatus;
    }

    public void setMaritalStatus(String maritalStatus) {
        this.maritalStatus = maritalStatus;
    }

    public String getOfficeName() {
        return officeName;
    }

    public void setOfficeName(String officeName) {
        this.officeName = officeName;
    }

    public BigDecimal getMthInc() {
        return mthInc;
    }

    public void setMthInc(BigDecimal mthInc) {
        this.mthInc = mthInc;
    }

    public String getOfficeTel() {
        return officeTel;
    }

    public void setOfficeTel(String officeTel) {
        this.officeTel = officeTel;
    }

    public String getCommonCustNo() {
        return commonCustNo;
    }

    public void setCommonCustNo(String commonCustNo) {
        this.commonCustNo = commonCustNo;
    }

    public String getSmsCode() {
        return smsCode;
    }

    public void setSmsCode(String smsCode) {
        this.smsCode = smsCode;
    }

    public String getApplSeq() {
        return applSeq;
    }

    public void setApplSeq(String applSeq) {
        this.applSeq = applSeq;
    }

    @Override
    public String toString() {
        return "CommonRepaymentPerson{" +
                "applSeq='" + applSeq + '\'' +
                ", id='" + id + '\'' +
                ", orderNo='" + orderNo + '\'' +
                ", custNo='" + custNo + '\'' +
                ", relation='" + relation + '\'' +
                ", maritalStatus='" + maritalStatus + '\'' +
                ", officeName='" + officeName + '\'' +
                ", mthInc=" + mthInc +
                ", officeTel='" + officeTel + '\'' +
                ", commonCustNo='" + commonCustNo + '\'' +
                ", smsCode='" + smsCode + '\'' +
                ", name='" + name + '\'' +
                ", idNo='" + idNo + '\'' +
                ", mobile='" + mobile + '\'' +
                ", cardNo='" + cardNo + '\'' +
                ", repayAcProvince='" + repayAcProvince + '\'' +
                ", repayAcCity='" + repayAcCity + '\'' +
                '}';
    }
}
