package com.haiercash.payplatform.common.data;

/**
 * @Describtion 外部接口交互日志
 * Created by zhaohan on 2017/2/24.
 */
public class DataInterfaceLog {
    private String id; //id
    private String reqParam; //请求参数
    private String reqUrl; //请求url
    private String resParam; //返回参数
    private String resType; //返回参数类型
    private String reqTime; //请求时间 2017-02-14 16:02:00
    private String state; //请求结果
    private String failRes; //失败原因
    private String remark; //备注

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

    public String getReqUrl() {
        return reqUrl;
    }

    public void setReqUrl(String reqUrl) {
        this.reqUrl = reqUrl;
    }

    public String getResParam() {
        return resParam;
    }

    public void setResParam(String resParam) {
        this.resParam = resParam;
    }

    public String getResType() {
        return resType;
    }

    public void setResType(String resType) {
        this.resType = resType;
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

    public String getFailRes() {
        return failRes;
    }

    public void setFailRes(String failRes) {
        this.failRes = failRes;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public String toString() {
        return "DataInterfaceLog{" +
                "id='" + id + '\'' +
                ", reqParam='" + reqParam + '\'' +
                ", reqUrl='" + reqUrl + '\'' +
                ", resParam='" + resParam + '\'' +
                ", resType='" + resType + '\'' +
                ", reqTime='" + reqTime + '\'' +
                ", state='" + state + '\'' +
                ", failRes='" + failRes + '\'' +
                ", remark='" + remark + '\'' +
                '}';
    }
}
