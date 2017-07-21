package com.haiercash.common.data;

import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.SelectBeforeUpdate;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "uauth_ca_sign_request")
@DynamicUpdate
@SelectBeforeUpdate
public class UAuthCASignRequest implements Serializable {
    private static final long serialVersionUID = 3739591427388308393L;

    @Id
    private String signCode;
    private String custName;
    private String custIdCode;
    private String clientId;
    private String signType;
    private String userId;
    private Date submitDate;
    private String state;  // 0:签章进行中  1:签章成功  2：签章失败  3: 签章失败再次尝试 4: 签章失败超时
    private Date signDate;
    private String orderNo;
    @Deprecated
    private String commonCustNo;// 1.1.0版本开始，共同还款人不会生成客户编号
    private String commonCustName;
    private String commonCustCertNo;
    private String orderJson;
    private int times;
    private String flag; //1.补签合同 2.申请放款
    private String applseq; //订单流水号
    private String commonFlag; //是否为共同还款人征信协议   0：不是     1：是
    private String sysFlag; // 签章来源  11:支付平台 13:个人版 14:商户版
    private String channelNo; // 签章来源  11:支付平台 13:个人版 14:商户版

    public String getCommonFlag() {
        return commonFlag;
    }

    public void setCommonFlag(String commonFlag) {
        this.commonFlag = commonFlag;
    }

    public String getApplseq() {
        return applseq;
    }

    public void setApplseq(String applseq) {
        this.applseq = applseq;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public String getCommonCustName() {
        return commonCustName;
    }

    public void setCommonCustName(String commonCustName) {
        this.commonCustName = commonCustName;
    }

    public String getCommonCustCertNo() {
        return commonCustCertNo;
    }

    public void setCommonCustCertNo(String commonCustCertNo) {
        this.commonCustCertNo = commonCustCertNo;
    }

    @Deprecated
    public String getCommonCustNo() {
        return commonCustNo;
    }

    @Deprecated
    public void setCommonCustNo(String commonCustNo) {
        this.commonCustNo = commonCustNo;
    }

    public int getTimes() {
        return times;
    }

    public void setTimes(int times) {
        this.times = times;
    }

    public String getOrderJson() {
        return orderJson;
    }

    public void setOrderJson(String orderJson) {
        this.orderJson = orderJson;
    }

    public String getSignCode() {
        return signCode;
    }

    public void setSignCode(String signCode) {
        this.signCode = signCode;
    }

    public String getCustName() {
        return custName;
    }

    public void setCustName(String custName) {
        this.custName = custName;
    }

    public String getCustIdCode() {
        return custIdCode;
    }

    public void setCustIdCode(String custIdCode) {
        this.custIdCode = custIdCode;
    }

    public String getSignType() {
        return signType;
    }

    public void setSignType(String signType) {
        this.signType = signType;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getSubmitDate() {
        return submitDate;
    }

    public void setSubmitDate(Date submitDate) {
        this.submitDate = submitDate;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Date getSignDate() {
        return signDate;
    }

    public void setSignDate(Date signDate) {
        this.signDate = signDate;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getChannelNo() {
        return channelNo;
    }

    public void setChannelNo(String channelNo) {
        this.channelNo = channelNo;
    }

    @Override
    public String toString() {
        return "UAuthCASignRequest{" +
                "signCode='" + signCode + '\'' +
                ", custName='" + custName + '\'' +
                ", custIdCode='" + custIdCode + '\'' +
                ", clientId='" + clientId + '\'' +
                ", signType='" + signType + '\'' +
                ", userId='" + userId + '\'' +
                ", submitDate=" + submitDate +
                ", state='" + state + '\'' +
                ", signDate=" + signDate +
                ", orderNo='" + orderNo + '\'' +
                ", commonCustNo='" + commonCustNo + '\'' +
                ", commonCustName='" + commonCustName + '\'' +
                ", commonCustCertNo='" + commonCustCertNo + '\'' +
                ", orderJson='" + orderJson + '\'' +
                ", times=" + times +
                ", flag='" + flag + '\'' +
                ", applseq='" + applseq + '\'' +
                ", commonFlag='" + commonFlag + '\'' +
                ", channelNo='" + channelNo + '\'' +
                '}';
    }

    public String getSysFlag() {
        return sysFlag;
    }

    public void setSysFlag(String sysFlag) {
        this.sysFlag = sysFlag;
    }
}



