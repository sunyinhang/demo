package com.haiercash.payplatform.common.data;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by 赵先鲁 on 2018/2/7.
 */
@Data
@Entity
@Table(name = "VIPABC_INFO")
public class Vipmessage {
    @Id
    private String uuid;
    private String starts;
    private String end;
    private String nowtime;
    private String flag;
    private String ordersn;
    private String remark;
}

