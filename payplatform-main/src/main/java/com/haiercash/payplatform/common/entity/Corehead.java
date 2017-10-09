/*
 * 功  能：简单说明该类的功能
 * 
 * 文件名：Corehead.java
 * 
 * 描  述：
 * 
 * [变更履历]
 * Version   变更日         部署              作者           变更内容
 * -----------------------------------------------------------------------------
 * V1.00     2015年12月23日   Haiercash    xuchao      CREATE
 * -----------------------------------------------------------------------------
 *
 *
 * Copyright (c) 2015 Haiercash All Rights Reserved.
 *┌─────────────────────────────────────────────────—────┐
 *│ 版权声明                               Haiercash       │
 *└──────────────────────────────—————————————————————───┘
 */

package com.haiercash.payplatform.common.entity;

import com.haiercash.payplatform.common.consts.BusinessConstance;
import lombok.Data;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * DESCRIPTION:核心请求head
 * <p>
 * <p>
 * <a href="Corehead.java"><i>View Source</i></a>
 * </p>
 *
 * @author <a href="mailto:xuchao@haiercash.com">xuchao</a>
 * @version Revision: 1.0  Date: 2015年12月23日 下午4:59:25
 */
@Data
public class Corehead {
    private String tradeCode;
    private String serno;
    private String sysFlag;
    private String tradeType;
    private String tradeDate;
    private String tradeTime;
    private String channelNo;
    private String cooprCode;

    public Corehead() {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("hh:mm:ss");
        String date_ = simpleDateFormat.format(date);
        String time_ = simpleTimeFormat.format(date);
        this.setTradeDate(date_);
        this.setTradeTime(time_);
        this.setSysFlag(BusinessConstance.sysFlag);
        this.setChannelNo(BusinessConstance.channelNo);
        this.setCooprCode("");
        this.setSerno(uuid);
        this.setTradeType("");
    }
}
