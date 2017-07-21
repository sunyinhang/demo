package com.haiercash.common.data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * Created by Administrator on 2016/10/21.
 */
@Entity
@Table(name = "IMPORT_APP_ORDER_REQUEST")
public class AppOrderRequest {
    @Id
    private String id; //唯一主键-- UUID
    private Integer applSeq;// 申请流水号
    private String custName;// 客户姓名
    private String idNo;// 身份证号
    private Date createTime;//创建时间
    private Date finishTime;//完成时间
    private String state;//任务状态
    private String errorMsg;// 失败原因
    private Integer tryCount;// 执行次数

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getApplSeq() {
        return applSeq;
    }

    public void setApplSeq(Integer applSeq) {
        this.applSeq = applSeq;
    }

    public String getCustName() {
        return custName;
    }

    public void setCustName(String custName) {
        this.custName = custName;
    }

    public String getIdNo() {
        return idNo;
    }

    public void setIdNo(String idNo) {
        this.idNo = idNo;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public void setFinishTime(Date finishTime) {
        this.finishTime = finishTime;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public void setTryCount(Integer tryCount) {
        this.tryCount = tryCount;
    }

    public Date getFinishTime() {
        return finishTime;
    }

    public String getState() {
        return state;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public Integer getTryCount() {
        return tryCount;
    }
}
