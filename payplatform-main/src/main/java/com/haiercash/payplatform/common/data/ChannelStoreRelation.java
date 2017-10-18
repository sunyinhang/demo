package com.haiercash.payplatform.common.data;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by 许崇雷 on 2017-10-18.
 */
@Data
@Entity
@Table(name = "CHANNEL_STORE_RELATION")
public class ChannelStoreRelation {
    @Id
    private String csrId;
    private String channelNo;
    private String merchantCode;
    private String storeCode;
}
