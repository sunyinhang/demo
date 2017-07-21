package com.haiercash.common.data;


import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "APP_CONTRACT_FILE")
public class ContractPdfFile {

    @Id
    private String id;
    private String orderNo;
    private String applSeq;
    private String fileDesc;
    private String fileName;
    private String custNo;
    private String signType;
    private String commonFlag; //是否是共同还款人征信协议  1:是   2:否
    private String signCode;  //对应的签章任务表中的id
    private String appDate; //写入时间
    private String flag;    //合同、协议标识    1：合同   0：协议
    private String attachSeq; //文件唯一编号
    private String deleteFlag;//是否删除标识   1：已删除 0：未删除


    public String getAttachSeq() {
        return attachSeq;
    }

    public void setAttachSeq(String attachSeq) {
        this.attachSeq = attachSeq;
    }

    public String getDeleteFlag() {
        return deleteFlag;
    }

    public void setDeleteFlag(String deleteFlag) {
        this.deleteFlag = deleteFlag;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public String getAppDate() {
        return appDate;
    }

    public void setAppDate(String appDate) {
        this.appDate = appDate;
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

    public String getApplSeq() {
        return applSeq;
    }

    public void setApplSeq(String applSeq) {
        this.applSeq = applSeq;
    }

    public String getFileDesc() {
        return fileDesc;
    }

    public void setFileDesc(String fileDesc) {
        this.fileDesc = fileDesc;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getSignType() {
        return signType;
    }

    public void setSignType(String signType) {
        this.signType = signType;
    }

    public String getCommonFlag() {
        return commonFlag;
    }

    public void setCommonFlag(String commonFlag) {
        this.commonFlag = commonFlag;
    }

    public String getCustNo() {
        return custNo;
    }

    public void setCustNo(String custNo) {
        this.custNo = custNo;
    }

    public String getSignCode() {
        return signCode;
    }

    public void setSignCode(String signCode) {
        this.signCode = signCode;
    }

    @Override
    public String toString() {
        return "ContractPdfFile{" +
                "id='" + id + '\'' +
                ", orderNo='" + orderNo + '\'' +
                ", applSeq='" + applSeq + '\'' +
                ", fileDesc='" + fileDesc + '\'' +
                ", fileName='" + fileName + '\'' +
                ", custNo='" + custNo + '\'' +
                ", signType='" + signType + '\'' +
                ", commonFlag='" + commonFlag + '\'' +
                ", signCode='" + signCode + '\'' +
                ", appDate='" + appDate + '\'' +
                ", flag='" + flag + '\'' +
                ", attachSeq='" + attachSeq + '\'' +
                '}';
    }
}
