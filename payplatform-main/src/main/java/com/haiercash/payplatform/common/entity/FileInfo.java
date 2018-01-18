package com.haiercash.payplatform.common.entity;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

@Data
@XStreamAlias("info1")
public class FileInfo {
    private String sequenceId;//文件上传序列号
    private String attachPath;//文件路径
    private String attachName;//原文件名称
    private String attachNameNew;//文件名称
    private String state;//状态
    private String crtUsr;//上传人员
    private String crtDt;//上传时间
    private String loseEffectUsr;//失效人员
    private String attachTyp;//上传类型
}
