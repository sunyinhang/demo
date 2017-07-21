package com.haiercash.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * eureka server list.
 * @author Liu qingxiang
 * @since v1.5.1
 */

@ConfigurationProperties(
        prefix = "EUREKASERVER"
)
@Component
public class EurekaServer {

    public static String APPCA;
    public static String APPMSG;
    public static String APPMANAGE;
    public static String CMISPROXY;
    public static String CMISFRONTSERVER;
    public static String CRM;
    public static String GM;
    public static String UAUTH;
    public static String HCPORTAL;
    public static String ACQUIRER;
    public static String ORDER;
    public static String APPSERVER;
    public static String APPSERVERNOAUTH;


    public static String getAPPCA() {
        return APPCA;
    }

    public static void setAPPCA(String APPCA) {
        EurekaServer.APPCA = APPCA;
    }

    public static String getAPPMSG() {
        return APPMSG;
    }

    public static void setAPPMSG(String APPMSG) {
        EurekaServer.APPMSG = APPMSG;
    }

    public static String getAPPMANAGE() {
        return APPMANAGE;
    }

    public static void setAPPMANAGE(String APPMANAGE) {
        EurekaServer.APPMANAGE = APPMANAGE;
    }

    public static String getCMISPROXY() {
        return CMISPROXY;
    }

    public static void setCMISPROXY(String CMISPROXY) {
        EurekaServer.CMISPROXY = CMISPROXY;
    }

    public static String getCMISFRONTSERVER() {
        return CMISFRONTSERVER;
    }

    public static void setCMISFRONTSERVER(String CMISFRONTSERVER) {
        EurekaServer.CMISFRONTSERVER = CMISFRONTSERVER;
    }

    public static String getCRM() {
        return CRM;
    }

    public static void setCRM(String CRM) {
        EurekaServer.CRM = CRM;
    }

    public static String getGM() {
        return GM;
    }

    public static void setGM(String GM) {
        EurekaServer.GM = GM;
    }

    public static String getUAUTH() {
        return UAUTH;
    }

    public static void setUAUTH(String UAUTH) {
        EurekaServer.UAUTH = UAUTH;
    }

    public static String getHCPORTAL() {
        return HCPORTAL;
    }

    public static void setHCPORTAL(String HCPORTAL) {
        EurekaServer.HCPORTAL = HCPORTAL;
    }

    public static String getACQUIRER() {
        return ACQUIRER;
    }

    public static void setACQUIRER(String ACQUIRER) {
        EurekaServer.ACQUIRER = ACQUIRER;
    }

    public static String getORDER() {
        return ORDER;
    }

    public static void setORDER(String ORDER) {
        EurekaServer.ORDER = ORDER;
    }

    public static String getAPPSERVER() {
        return APPSERVER;
    }

    public static void setAPPSERVER(String APPSERVER) {
        EurekaServer.APPSERVER = APPSERVER;
    }

    public static String getAPPSERVERNOAUTH() {
        return APPSERVERNOAUTH;
    }

    public static void setAPPSERVERNOAUTH(String APPSERVERNOAUTH) {
        EurekaServer.APPSERVERNOAUTH = APPSERVERNOAUTH;
    }
}

