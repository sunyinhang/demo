package com.haiercash.payplatform.common.data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.nio.channels.Channel;

/**
 * Created by yuanli on 2017/10/15.
 */
@Entity
@Table(name = "SIGN_CONTRACT_INFO")
public class SignContractInfo {
    @Id
    private String id;
    private String channelno;
    private String typcde;
    private String signtype;
    private String remark;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getChannelno() {
        return channelno;
    }

    public void setChannelno(String channelno) {
        this.channelno = channelno;
    }

    public String getTypcde() {
        return typcde;
    }

    public void setTypcde(String typcde) {
        this.typcde = typcde;
    }

    public String getSigntype() {
        return signtype;
    }

    public void setSigntype(String signtype) {
        this.signtype = signtype;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
