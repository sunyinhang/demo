package com.haiercash.payplatform.controller;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Created by 许崇雷 on 2017-10-12.
 */
@Data
@NoArgsConstructor
public class DemoBean {
    private String value;
    private Date nowTime;
    private Date nowDate;

    public DemoBean(Date nowTime, Date nowDate) {
        this.nowTime = nowTime;
        this.nowDate = nowDate;
    }
}
