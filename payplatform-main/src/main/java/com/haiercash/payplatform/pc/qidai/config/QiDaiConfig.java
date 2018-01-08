package com.haiercash.payplatform.pc.qidai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Created by 许崇雷 on 2018-01-04.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.qidai")
public class QiDaiConfig {
    private String attachPath;
    private String cmisFtpHost;
    private Integer cmisFtpPort;
    private String cmisFtpUsername;
    private String cmisFtpPassword;
    private String pgwFtpHost;
    private Integer pgwFtpPort;
    private String pgwFtpUsername;
    private String pgwFtpPassword;
    private List<String> noThreeParamChannelNos;
    private String cmisYcLoanURL;
}
