package com.haiercash.payplatform.common.entity;

import lombok.Data;

import java.util.List;

@Data
public class ReturnMessage {
    private String code;
    private String message;
    private List data;
    private Object retObj;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List getData() {
        return data;
    }

    public void setData(List data) {
        this.data = data;
    }

    public Object getRetObj() {
        return retObj;
    }

    public void setRetObj(Object retObj) {
        this.retObj = retObj;
    }

}
