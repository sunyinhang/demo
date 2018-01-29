package com.haiercash.spring.config;

import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

/**
 * Created by 许崇雷 on 2018-01-29.
 */
@Component
@DependsOn(EurekaProvider.BEAN_NAME)
public class EurekaServer {
    public static final String ACQUIRER;
    public static final String APPCA;
    public static final String APPMANAGE;
    public static final String APPMSG;
    public static final String APPSERVER;
    public static final String APPSERVERNOAUTHNEW;
    public static final String CMISFRONTSERVER;
    public static final String CMISINTERFACESERVER;
    public static final String CMISPROXY;
    public static final String CRM;
    public static final String GM;
    public static final String HCPORTAL;
    public static final String ORDER;
    public static final String OUTREACHPLATFORM;
    public static final String UAUTH;

    static {
        EurekaProperties eurekaProperties = EurekaProvider.getEurekaProperties();
        ACQUIRER = eurekaProperties.getACQUIRER();
        APPCA = eurekaProperties.getAPPCA();
        APPMANAGE = eurekaProperties.getAPPMANAGE();
        APPMSG = eurekaProperties.getAPPMSG();
        APPSERVER = eurekaProperties.getAPPSERVER();
        APPSERVERNOAUTHNEW = eurekaProperties.getAPPSERVERNOAUTHNEW();
        CMISFRONTSERVER = eurekaProperties.getCMISFRONTSERVER();
        CMISINTERFACESERVER = eurekaProperties.getCMISINTERFACESERVER();
        CMISPROXY = eurekaProperties.getCMISPROXY();
        CRM = eurekaProperties.getCRM();
        GM = eurekaProperties.getGM();
        HCPORTAL = eurekaProperties.getHCPORTAL();
        ORDER = eurekaProperties.getORDER();
        OUTREACHPLATFORM = eurekaProperties.getOUTREACHPLATFORM();
        UAUTH = eurekaProperties.getUAUTH();
    }
}
