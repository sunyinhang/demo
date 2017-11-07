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
public class AppShunguangConfig {
    private String sg_merch_no;// EHAIER
    private String sg_store_no;// SHUNGUANG
    private String sg_user_id;// SAQDGM01
    private String sg_typCde;// 17098a
    private String sg_shopkeeper;// 882f7f5c91fc4ed09a754e28841bc7ad
    private String sg_consumer;// dc04a273474a404bafdad48ddb63cce2
}
