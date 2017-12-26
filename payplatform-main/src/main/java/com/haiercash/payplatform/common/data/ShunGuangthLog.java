package com.haiercash.payplatform.common.data;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by Administrator on 2017/12/21.
 */
@Data
@Entity
@Table(name = "SG_RETURNGOODS_LOG")
public class ShunGuangthLog {
    @Id
    private String logId;//主键
    private String msgTyp;//推送类型
    private String channelNo;//渠道号
    private String applSeq;//申请流水号
    private String mallOrderNo;//商城订单号
    private String loanNo;//借据号
    private String idNo;//身份证号
    private String custName;//客户姓名
    private String businessId;//业务流水号
    private String businessType;//业务类型
    private String status;//状态
    private String content;//提示描述
    private String time;//时间
    private String flag;//标识  Y  成功   N 失败
    private String times;//推送次数
    private String remark;//备注
}
