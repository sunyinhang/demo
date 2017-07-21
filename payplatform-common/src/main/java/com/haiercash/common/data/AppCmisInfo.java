package com.haiercash.common.data;

import com.haiercash.commons.util.HttpUtil;
import org.json.JSONObject;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;
import java.util.Map;

/**
 * 存储待发送风险信息类
 *
 * Created by zhouwushuang on 2017.02.21.
 */
@Entity
@Table(name = "APP_CMIS_INFO")
public class AppCmisInfo {
    @Id
    private String id;// 主键
    private String tradeCode;// 交易码
    private String requestMap;// 请求体
    private Date insertTime;// 数据插入时间
    private Date sendTime;// 数据发送时间
    private String flag;// 发送状态：0 未发送；1 已发送
    private String returnMsg; // 信贷返回信息

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTradeCode() {
        return tradeCode;
    }

    public void setTradeCode(String tradeCode) {
        this.tradeCode = tradeCode;
    }

    public String getRequestMap() {
        return requestMap;
    }

    public void setRequestMap(String requestMap) {
        this.requestMap = requestMap;
    }

    public void setRequestMap(Map<String, Object> requestMap) {
        setRequestMap(new JSONObject(requestMap).toString());
    }

    public Date getInsertTime() {
        return insertTime;
    }

    public void setInsertTime(Date insertTime) {
        this.insertTime = insertTime;
    }

    public Date getSendTime() {
        return sendTime;
    }

    public void setSendTime(Date sendTime) {
        this.sendTime = sendTime;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public String getReturnMsg() {
        return returnMsg;
    }

    public void setReturnMsg(String returnMsg) {
        this.returnMsg = returnMsg;
    }

    @Override
    public String toString() {
        return "AppCmisInfo{" +
                "id='" + id + '\'' +
                ", tradeCode='" + tradeCode + '\'' +
                ", requestMap='" + requestMap + '\'' +
                ", insertTime=" + insertTime +
                ", sendTime=" + sendTime +
                ", flag='" + flag + '\'' +
                ", returnMsg='" + returnMsg + '\'' +
                '}';
    }
}
