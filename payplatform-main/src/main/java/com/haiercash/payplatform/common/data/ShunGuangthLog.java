package com.haiercash.payplatform.common.data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by Administrator on 2017/12/21.
 */
@Entity
@Table(name = "ShunGuangth_LOG")
public class ShunGuangthLog {
    @Id
    private String logid;//主键
    private String msgtyp;//推送类型
    private String applseq;//申请流水号
    private String mallorderno;//商城订单号
    private String loanno;//借据号
    private String idno;//身份证号
    private String custname;//客户姓名
    private String businessid;//业务流水号
    private String businesstype;//业务类型
    private String status;//状态
    private String content;//提示描述
    private String time;//时间
    private String flag;//标识  Y  成功   N 失败
    private String times;//推送次数
    private String remark;//备注

    public String getLogid() {
        return logid;
    }

    public void setLogid(String logid) {
        this.logid = logid;
    }

    public String getMsgtyp() {
        return msgtyp;
    }

    public void setMsgtyp(String msgtyp) {
        this.msgtyp = msgtyp;
    }

    public String getApplseq() {
        return applseq;
    }

    public void setApplseq(String applseq) {
        this.applseq = applseq;
    }

    public String getMallorderno() {
        return mallorderno;
    }

    public void setMallorderno(String mallorderno) {
        this.mallorderno = mallorderno;
    }

    public String getLoanno() {
        return loanno;
    }

    public void setLoanno(String loanno) {
        this.loanno = loanno;
    }

    public String getIdno() {
        return idno;
    }

    public void setIdno(String idno) {
        this.idno = idno;
    }

    public String getCustname() {
        return custname;
    }

    public void setCustname(String custname) {
        this.custname = custname;
    }

    public String getBusinessid() {
        return businessid;
    }

    public void setBusinessid(String businessid) {
        this.businessid = businessid;
    }

    public String getBusinesstype() {
        return businesstype;
    }

    public void setBusinesstype(String businesstype) {
        this.businesstype = businesstype;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public String getTimes() {
        return times;
    }

    public void setTimes(String times) {
        this.times = times;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public String toString() {
        return "ShunGuangthLog{" +
                "logid='" + logid + '\'' +
                ", msgtyp='" + msgtyp + '\'' +
                ", applseq='" + applseq + '\'' +
                ", mallorderno='" + mallorderno + '\'' +
                ", loanno='" + loanno + '\'' +
                ", idno='" + idno + '\'' +
                ", custname='" + custname + '\'' +
                ", businessid='" + businessid + '\'' +
                ", businesstype='" + businesstype + '\'' +
                ", status='" + status + '\'' +
                ", content='" + content + '\'' +
                ", time='" + time + '\'' +
                ", flag='" + flag + '\'' +
                ", times='" + times + '\'' +
                ", remark='" + remark + '\'' +
                '}';
    }
}
