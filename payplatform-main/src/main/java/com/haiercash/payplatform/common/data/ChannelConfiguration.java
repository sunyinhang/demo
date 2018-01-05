package com.haiercash.payplatform.common.data;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "CHANNEL_CONFIGURATION")
public class ChannelConfiguration {
    @Id
    private String id;
    private String channelNo;
    private String channelName;
    private String sysCode;
    private String sysName;
    private String activeflag;
}
