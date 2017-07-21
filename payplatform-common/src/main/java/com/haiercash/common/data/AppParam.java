package com.haiercash.common.data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "app_param")
public class AppParam implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private String paramId;//主键
    private String sysTyp;//系统标识
    private String paramCode;
    private String paramName;
    private String describe;
    private String paramValue;
    private String paramDict;
    private String state;
    private String remark;
    
    public String getParamCode() {
        return paramCode;
    }
    public void setParamCode(String paramCode) {
        this.paramCode = paramCode;
    }
    public String getParamName() {
        return paramName;
    }
    public void setParamName(String paramName) {
        this.paramName = paramName;
    }
    public String getDescribe() {
        return describe;
    }
    public void setDescribe(String describe) {
        this.describe = describe;
    }
    public String getParamValue() {
        return paramValue;
    }
    public void setParamValue(String paramValue) {
        this.paramValue = paramValue;
    }
    public String getParamDict() {
        return paramDict;
    }
    public void setParamDict(String paramDict) {
        this.paramDict = paramDict;
    }
    public String getState() {
        return state;
    }
    public void setState(String state) {
        this.state = state;
    }
    public String getRemark() {
        return remark;
    }
    public void setRemark(String remark) {
        this.remark = remark;
    }
    public String getSysTyp() {
        return sysTyp;
    }
    public void setSysTyp(String sysTyp) {
        this.sysTyp = sysTyp;
    }
    public String getParamId() {
        return paramId;
    }
    public void setParamId(String paramId) {
        this.paramId = paramId;
    }
}