package com.haiercash.core.lang;

import lombok.Data;

/**
 * Created by 许崇雷 on 2018-02-08.
 */
@Data
public class DemoBean<T> {
    private DemoBeanHead head;
    private T body;

    public String getRetFlag() {
        return this.head == null ? null : this.head.getRetFlag();
    }

    public String getRetMsg() {
        return this.head == null ? null : this.head.getRetMsg();
    }
}
