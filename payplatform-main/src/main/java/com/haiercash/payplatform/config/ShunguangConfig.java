package com.haiercash.payplatform.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Created by 许崇雷 on 2017-11-06.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.shunguang")
public class ShunguangConfig {
    private String merchNo;// EHAIER
    private String storeNo;// SHUNGUANG
    private String userId;// SAQDGM01
    private String typCde;// 17098a
    private String shopKeeper;// 882f7f5c91fc4ed09a754e28841bc7ad
    private String consumer;// dc04a273474a404bafdad48ddb63cce2
    private String tsUrl;
}
