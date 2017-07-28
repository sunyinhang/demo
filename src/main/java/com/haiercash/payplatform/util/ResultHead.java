package com.haiercash.payplatform.util;

import java.io.Serializable;

/**
 * Created by use on 2017/7/25.
 */
public class ResultHead implements Serializable {
    private static final long serialVersionUID = 1L;
    private String retFlag;
    private String retMsg;

    public ResultHead() {
    }

    public ResultHead(String retFlag, String retMsg) {
        this.retFlag = retFlag;
        this.retMsg = retMsg;
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
}