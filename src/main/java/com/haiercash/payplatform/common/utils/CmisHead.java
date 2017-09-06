package com.haiercash.payplatform.common.utils;

import java.io.Serializable;
import java.util.UUID;

public class CmisHead implements Serializable {
    private static final long serialVersionUID = 1L;
    private String retFlag;
    private String retMsg;
    private String serno;

    public CmisHead() {
    }

    public CmisHead(String retFlag, String retMsg) {
        this.retFlag = retFlag;
        this.retMsg = retMsg;
        this.serno = UUID.randomUUID().toString();
    }

    public CmisHead(String retFlag, String retMsg, String serno) {
        this.retFlag = retFlag;
        this.retMsg = retMsg;
        this.serno = serno;
    }

    public String getRetFlag() {
        return this.retFlag;
    }

    public void setRetFlag(String retFlag) {
        this.retFlag = retFlag;
    }

    public String getRetMsg() {
        return this.retMsg;
    }

    public void setRetMsg(String retMsg) {
        this.retMsg = retMsg;
    }

    public String getSerno() {
        return this.serno;
    }

    public void setSerno(String serno) {
        this.serno = serno;
    }
}
