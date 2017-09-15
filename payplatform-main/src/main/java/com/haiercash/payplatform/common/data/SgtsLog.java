package com.haiercash.payplatform.common.data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by Administrator on 2017/9/3.
 */
@Entity
@Table(name = "Sgts_Log")
public class SgtsLog {
    @Id
    private String logId; // 日志id
    private String applSeq; // 申请流水号
    private String channelNo; // 渠道编码
    private String idNo;//身份证号
    private String edFlag;//额度标识
    private String dkFlag;//贷款标识
    private Integer tscount;//推送次数
    private String time;//推送时间
    private String outSts;//贷款状态
    private String msgTyp;//
    private String remark; //返回记录结果

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getLogId() {
        return logId;
    }

    public void setLogId(String logId) {
        this.logId = logId;
    }

    public String getApplSeq() {
        return applSeq;
    }

    public void setApplSeq(String applSeq) {
        this.applSeq = applSeq;
    }

    public String getChannelNo() {
        return channelNo;
    }

    public void setChannelNo(String channelNo) {
        this.channelNo = channelNo;
    }

    public String getIdNo() {
        return idNo;
    }

    public void setIdNo(String idNo) {
        this.idNo = idNo;
    }

    public String getEdFlag() {
        return edFlag;
    }

    public void setEdFlag(String edFlag) {
        this.edFlag = edFlag;
    }

    public String getDkFlag() {
        return dkFlag;
    }

    public void setDkFlag(String dkFlag) {
        this.dkFlag = dkFlag;
    }

    public Integer getTscount() {
        return tscount;
    }

    public void setTscount(Integer tscount) {
        this.tscount = tscount;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getOutSts() {
        return outSts;
    }

    public void setOutSts(String outSts) {
        this.outSts = outSts;
    }

    public String getMsgTyp() {
        return msgTyp;
    }

    public void setMsgTyp(String msgTyp) {
        this.msgTyp = msgTyp;
    }

    @Override
    public String toString() {
        return "SgtsLog{" +
                "logId='" + logId + '\'' +
                ", applSeq='" + applSeq + '\'' +
                ", channelNo='" + channelNo + '\'' +
                ", idNo='" + idNo + '\'' +
                ", edFlag='" + edFlag + '\'' +
                ", dkFlag='" + dkFlag + '\'' +
                ", tscount=" + tscount +
                ", time='" + time + '\'' +
                ", outSts='" + outSts + '\'' +
                ", msgTyp='" + msgTyp + '\'' +
                ", remark='" + remark + '\'' +
                '}';
    }
}
