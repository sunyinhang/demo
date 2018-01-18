package com.haiercash.payplatform.common.data;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "PAY_APPLY_LOG")
public class PayApplyLog {
    @Id
    private String id;
    private String channelno;
    private String applseq;
    private String flag;
    private String tradetime;
    private String tradecode;
    private String pushnum;
    private String applcde;
}
