package com.haiercash.payplatform.common.data;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "FILETRANSLOG")
@XStreamAlias("FileTransLog")
public class FileTransLog {
    @Id
    private String log_id; // 日志id
    private String tradecode; // 交易码
    private String channelno; // 渠道编码
    private String applseq; // 申请流水号
    private String serno; // 报文流水号
    private String tradetime; // 交易时间
    private String sysid; // 系统标识
    private String busid; // 业务标识
    private String thirdreqdata; // 第三方请求数据：第三方->支付平台
    private String thirdresdata; // 支付响应第三方数据
    private String cmisreqdata; // 信贷方请求数据:支付平台->信贷
    private String cmisresdata; // 信贷方响应支付平台数据
    private int totalfile; // 应下载文件总数
    private int downfilenum; // 成功下载文件数
    private String fileflag; // 下载标志:成功:success,失败:failure
    private String filemsg; // 文件下载失败信息汇总
    private String retflag; // 最终处理结果状态,成功:00,失败:11
    private String retmsg; // 最终处理失败错误信息
    private String remark; // 备注
}
