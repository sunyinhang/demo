package com.haiercash.payplatform.data;

/**
 * Created by zhaohan on 2017/2/14.
 */
public class DataPushLog {
    private String id;//id
    private String reqParam;//请求参数
    private String resParam;//响应参数
    private String reqTime;//请求时间
    private String state;//请求状态 Y:成功 N:失败
    private String remark;//备注

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getReqParam() {
        return reqParam;
    }

    public void setReqParam(String reqParam) {
        this.reqParam = reqParam;
    }

    public String getResParam() {
        return resParam;
    }

    public void setResParam(String resParam) {
        this.resParam = resParam;
    }

    public String getReqTime() {
        return reqTime;
    }

    public void setReqTime(String reqTime) {
        this.reqTime = reqTime;
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

    @Override
    public String toString() {
        return "DataPushLog{" +
                "id='" + id + '\'' +
                ", reqParam='" + reqParam + '\'' +
                ", resParam='" + resParam + '\'' +
                ", reqTime='" + reqTime + '\'' +
                ", state='" + state + '\'' +
                ", remark='" + remark + '\'' +
                '}';
    }
}
