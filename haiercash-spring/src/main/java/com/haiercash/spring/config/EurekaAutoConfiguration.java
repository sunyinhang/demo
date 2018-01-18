package com.haiercash.spring.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Created by 许崇雷 on 2018-01-09.
 */
@Configuration
@EnableConfigurationProperties(EurekaProperties.class)
public class EurekaAutoConfiguration {
    public EurekaAutoConfiguration(EurekaProperties eurekaProperties) {
        EurekaServer.ACQUIRER = eurekaProperties.getACQUIRER();
        EurekaServer.APPCA = eurekaProperties.getAPPCA();
        EurekaServer.APPMANAGE = eurekaProperties.getAPPMANAGE();
        EurekaServer.APPMSG = eurekaProperties.getAPPMSG();
        EurekaServer.APPSERVER = eurekaProperties.getAPPSERVER();
        EurekaServer.APPSERVERNOAUTHNEW = eurekaProperties.getAPPSERVERNOAUTHNEW();
        EurekaServer.CMISFRONTSERVER = eurekaProperties.getCMISFRONTSERVER();
        EurekaServer.CMISINTERFACESERVER = eurekaProperties.getCMISINTERFACESERVER();
        EurekaServer.CMISPROXY = eurekaProperties.getCMISPROXY();
        EurekaServer.CRM = eurekaProperties.getCRM();
        EurekaServer.GM = eurekaProperties.getGM();
        EurekaServer.HCPORTAL = eurekaProperties.getHCPORTAL();
        EurekaServer.ORDER = eurekaProperties.getORDER();
        EurekaServer.OUTREACHPLATFORM = eurekaProperties.getOUTREACHPLATFORM();
        EurekaServer.UAUTH = eurekaProperties.getUAUTH();
    }
}
