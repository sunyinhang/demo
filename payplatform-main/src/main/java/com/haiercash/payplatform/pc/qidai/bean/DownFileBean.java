package com.haiercash.payplatform.pc.qidai.bean;

import lombok.Data;

@Data
public class DownFileBean {
    private String channelno;//渠道编码
    private String appno;//流水号
    private String apptime;//申请时间
    private String appl;//申请号
    private String fileType;// 文件类型 UP
}
