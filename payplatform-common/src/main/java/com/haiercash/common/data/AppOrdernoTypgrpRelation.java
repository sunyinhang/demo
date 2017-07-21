package com.haiercash.common.data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * 保存订单号和贷款种类关系
 * Created by zhouwushuang on 2017.04.15.
 * since 1.6.3
 */
@Entity
@Table(name = "APP_ORDERNO_TYPGRP_RELATION")
public class AppOrdernoTypgrpRelation {

    /**
     * 主键：订单系统的formId, 收单系统的applSeq
     */
    @Id
    private String orderNo;

    /**
     * 贷款类型，01商品贷，02现金贷，03伞下店
     */
    private String typGrp;

    /**
     * 订单流水号.
     */
    private String applSeq;

    private String faceTypCde;          // 人脸识别贷款品种
    private String faceValue;           // 贷款品种人脸分值
    private String applyFaceSucc;       // 申请人人脸识别是否成功
    private String applyFaceCount;      // 申请人人脸识别次数
    private String applyFaceValue;      // 申请人人脸识别分值
    private String comApplyFaceSucc;    // 共同申请人人脸识别是否成功
    private String comApplyFaceCount;   // 共同申请人人脸识别次数
    private String comApplyFaceValue;   // 共同申请人人脸识别分值
    private String isConfirmAgreement;  // 是否已订单协议确认
    private String isConfirmContract;   // 是否已合同确认
    private String isCustInfoComplete;  // 客户信息是否完善
    private String commonCustNo;        // 共同还款人客户编号
    private Date   insertTime;          // 保存时间
    private String custNo;              // 用户编号
    private String channel;             // 系统标识
    private String channelNo;           // 渠道号
    private String state;               // 状态

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getTypGrp() {
        return typGrp;
    }

    public void setTypGrp(String typGrp) {
        this.typGrp = typGrp;
    }

    public String getApplSeq() {
        return applSeq;
    }

    public void setApplSeq(String applSeq) {
        this.applSeq = applSeq;
    }

    public String getFaceTypCde() {
        return faceTypCde;
    }

    public void setFaceTypCde(String faceTypCde) {
        this.faceTypCde = faceTypCde;
    }

    public String getFaceValue() {
        return faceValue;
    }

    public void setFaceValue(String faceValue) {
        this.faceValue = faceValue;
    }

    public String getApplyFaceSucc() {
        return applyFaceSucc;
    }

    public void setApplyFaceSucc(String applyFaceSucc) {
        this.applyFaceSucc = applyFaceSucc;
    }

    public String getApplyFaceCount() {
        return applyFaceCount;
    }

    public void setApplyFaceCount(String applyFaceCount) {
        this.applyFaceCount = applyFaceCount;
    }

    public String getApplyFaceValue() {
        return applyFaceValue;
    }

    public void setApplyFaceValue(String applyFaceValue) {
        this.applyFaceValue = applyFaceValue;
    }

    public String getComApplyFaceSucc() {
        return comApplyFaceSucc;
    }

    public void setComApplyFaceSucc(String comApplyFaceSucc) {
        this.comApplyFaceSucc = comApplyFaceSucc;
    }

    public String getComApplyFaceCount() {
        return comApplyFaceCount;
    }

    public void setComApplyFaceCount(String comApplyFaceCount) {
        this.comApplyFaceCount = comApplyFaceCount;
    }

    public String getComApplyFaceValue() {
        return comApplyFaceValue;
    }

    public void setComApplyFaceValue(String comApplyFaceValue) {
        this.comApplyFaceValue = comApplyFaceValue;
    }

    public String getIsConfirmAgreement() {
        return isConfirmAgreement;
    }

    public void setIsConfirmAgreement(String isConfirmAgreement) {
        this.isConfirmAgreement = isConfirmAgreement;
    }

    public String getIsConfirmContract() {
        return isConfirmContract;
    }

    public void setIsConfirmContract(String isConfirmContract) {
        this.isConfirmContract = isConfirmContract;
    }

    public String getIsCustInfoComplete() {
        return isCustInfoComplete;
    }

    public void setIsCustInfoComplete(String isCustInfoComplete) {
        this.isCustInfoComplete = isCustInfoComplete;
    }

    public String getCommonCustNo() {
        return commonCustNo;
    }

    public void setCommonCustNo(String commonCustNo) {
        this.commonCustNo = commonCustNo;
    }

    public Date getInsertTime() {
        return insertTime;
    }

    public void setInsertTime(Date insertTime) {
        this.insertTime = insertTime;
    }


    /**
     * 判断是否是现金贷
     *
     * @return true or false
     */
    public boolean isCashTyp() {
        boolean flag = false;
        if ("02".equals(this.typGrp)) {
            flag = true;
        }
        return flag;
    }

    public String getCustNo() {
        return custNo;
    }

    public void setCustNo(String custNo) {
        this.custNo = custNo;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getChannelNo() {
        return channelNo;
    }

    public void setChannelNo(String channelNo) {
        this.channelNo = channelNo;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "AppOrdernoTypgrpRelation{" +
                "orderNo='" + orderNo + '\'' +
                ", typGrp='" + typGrp + '\'' +
                ", applSeq='" + applSeq + '\'' +
                ", faceTypCde='" + faceTypCde + '\'' +
                ", faceValue='" + faceValue + '\'' +
                ", applyFaceSucc='" + applyFaceSucc + '\'' +
                ", applyFaceCount='" + applyFaceCount + '\'' +
                ", applyFaceValue='" + applyFaceValue + '\'' +
                ", comApplyFaceSucc='" + comApplyFaceSucc + '\'' +
                ", comApplyFaceCount='" + comApplyFaceCount + '\'' +
                ", comApplyFaceValue='" + comApplyFaceValue + '\'' +
                ", isConfirmAgreement='" + isConfirmAgreement + '\'' +
                ", isConfirmContract='" + isConfirmContract + '\'' +
                ", isCustInfoComplete='" + isCustInfoComplete + '\'' +
                ", commonCustNo='" + commonCustNo + '\'' +
                ", custNo='" + custNo + '\'' +
                ", insertTime=" + insertTime +
                ", channel=" + channel +
                ", channelNo=" + channelNo +
                ", state =" + state +
                '}';
    }
}
