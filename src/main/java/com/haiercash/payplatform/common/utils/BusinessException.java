package com.haiercash.payplatform.common.utils;

/**
 * Created by use on 2017/8/15.
 */
public class BusinessException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private String retFlag;
    private String retMsg;

    public BusinessException(String retFlag, String retMsg) {
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

    public String toString() {
        return "BusinessException{retFlag=\'" + this.retFlag + '\'' + ", retMsg=\'" + this.retMsg + '\'' + '}';
    }
}