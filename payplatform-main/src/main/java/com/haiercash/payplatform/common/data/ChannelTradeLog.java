package com.haiercash.payplatform.common.data;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by 许崇雷 on 2017-10-07.
 */
@Data
@Entity
@Table(name = "channel_trade_log")
public class ChannelTradeLog {
    @Id
    private String applyno;
    private String channelno;
    private String tradecode;
    private String retflag;
    private String retmsg;
    private String tradetime;
}
