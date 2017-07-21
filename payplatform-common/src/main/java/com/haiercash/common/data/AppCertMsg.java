package com.haiercash.common.data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * Created by yinjun on 2017/2/20.
 */

@Entity
@Table(name = "app_cert_msg")
public class AppCertMsg {
    @Id
    private String id;//主键
    private String custName;//姓名
    private String birthDt;//生日，格式：yyyy-MM-dd
    private String gender;//性别，格式：10：男，20：女
    private String regAddr;//户籍地址
    private String certStrDt;//身份证有效期限开始日期，格式：yyyy-MM-dd'
    private String certEndDt;//身份证有效期限终止日期，格式：yyyy-MM-dd'，如为“长期”，需填写：“9999-99-99”
    private String certOrga;//身份证签发机关
    private String ethnic;//民族
    private Date insertTime;//插入时间
    private String deleteFlag;//删除标识（0:未删除  1：删除）
    private String certNo;//身份证号

    /**
     * 添加修改后的值
     *
     * @return
     */
    private String afterCustName;//手动修改后的姓名
    private String afterBirthDt;//手动修改后的生日，格式：yyyy-MM-dd
    private String afterGender;//手动修改后的性别，格式：10：男，20：女
    private String afterRegAddr;//手动修改后的户籍地址
    private String afterCertStrDt;//手动修改后的身份证有效期限开始日期，格式：yyyy-MM-dd'
    private String afterCertEndDt;//手动修改后的身份证有效期限终止日期，格式：yyyy-MM-dd'，如为“长期”，需填写：“9999-99-99”
    private String afterCertOrga;//手动修改后的身份证签发机关
    private String afterEthnic;//手动修改后的民族
    private String afterCertNo;//手动修改后的身份证号

    public String getAfterCustName() {
        return afterCustName;
    }

    public void setAfterCustName(String afterCustName) {
        this.afterCustName = afterCustName;
    }

    public String getAfterBirthDt() {
        return afterBirthDt;
    }

    public void setAfterBirthDt(String afterBirthDt) {
        this.afterBirthDt = afterBirthDt;
    }

    public String getAfterGender() {
        return afterGender;
    }

    public void setAfterGender(String afterGender) {
        this.afterGender = afterGender;
    }

    public String getAfterRegAddr() {
        return afterRegAddr;
    }

    public void setAfterRegAddr(String afterRegAddr) {
        this.afterRegAddr = afterRegAddr;
    }

    public String getAfterCertStrDt() {
        return afterCertStrDt;
    }

    public void setAfterCertStrDt(String afterCertStrDt) {
        this.afterCertStrDt = afterCertStrDt;
    }

    public String getAfterCertEndDt() {
        return afterCertEndDt;
    }

    public void setAfterCertEndDt(String afterCertEndDt) {
        this.afterCertEndDt = afterCertEndDt;
    }

    public String getAfterCertOrga() {
        return afterCertOrga;
    }

    public void setAfterCertOrga(String afterCertOrga) {
        this.afterCertOrga = afterCertOrga;
    }

    public String getAfterEthnic() {
        return afterEthnic;
    }

    public void setAfterEthnic(String afterEthnic) {
        this.afterEthnic = afterEthnic;
    }

    public String getAfterCertNo() {
        return afterCertNo;
    }

    public void setAfterCertNo(String afterCertNo) {
        this.afterCertNo = afterCertNo;
    }

    public String getCertNo() {
        return certNo;
    }

    public void setCertNo(String certNo) {
        this.certNo = certNo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCustName() {
        return custName;
    }

    public void setCustName(String custName) {
        this.custName = custName;
    }

    public String getBirthDt() {
        return birthDt;
    }

    public void setBirthDt(String birthDt) {
        this.birthDt = birthDt;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getRegAddr() {
        return regAddr;
    }

    public void setRegAddr(String regAddr) {
        this.regAddr = regAddr;
    }

    public String getCertStrDt() {
        return certStrDt;
    }

    public void setCertStrDt(String certStrDt) {
        this.certStrDt = certStrDt;
    }

    public String getCertEndDt() {
        return certEndDt;
    }

    public void setCertEndDt(String certEndDt) {
        this.certEndDt = certEndDt;
    }

    public String getCertOrga() {
        return certOrga;
    }

    public void setCertOrga(String certOrga) {
        this.certOrga = certOrga;
    }

    public String getEthnic() {
        return ethnic;
    }

    public void setEthnic(String ethnic) {
        this.ethnic = ethnic;
    }

    public Date getInsertTime() {
        return insertTime;
    }

    public void setInsertTime(Date insertTime) {
        this.insertTime = insertTime;
    }

    public String getDeleteFlag() {
        return deleteFlag;
    }

    public void setDeleteFlag(String deleteFlag) {
        this.deleteFlag = deleteFlag;
    }

    @Override
    public String toString() {
        return "AppCertMsg{" +
                "id='" + id + '\'' +
                ", custName='" + custName + '\'' +
                ", birthDt='" + birthDt + '\'' +
                ", gender='" + gender + '\'' +
                ", regAddr='" + regAddr + '\'' +
                ", certStrDt='" + certStrDt + '\'' +
                ", certEndDt='" + certEndDt + '\'' +
                ", certOrga='" + certOrga + '\'' +
                ", ethnic='" + ethnic + '\'' +
                ", insertTime=" + insertTime +
                ", deleteFlag='" + deleteFlag + '\'' +
                ", certNo='" + certNo + '\'' +
                ", afterCustName='" + afterCustName + '\'' +
                ", afterBirthDt='" + afterBirthDt + '\'' +
                ", afterGender='" + afterGender + '\'' +
                ", afterRegAddr='" + afterRegAddr + '\'' +
                ", afterCertStrDt='" + afterCertStrDt + '\'' +
                ", afterCertEndDt='" + afterCertEndDt + '\'' +
                ", afterCertOrga='" + afterCertOrga + '\'' +
                ", afterEthnic='" + afterEthnic + '\'' +
                ", afterCertNo='" + afterCertNo + '\'' +
                '}';
    }
}
