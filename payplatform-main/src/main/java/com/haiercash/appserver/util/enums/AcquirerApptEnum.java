package com.haiercash.appserver.util.enums;

/**
 * 收单系统申请人map映射关系
 * Created by zhouwushuang on 2017.04.19.
 */
public enum AcquirerApptEnum {

    INDIV_MARITAL       ("maritalStatus",       "indiv_marital"),       //婚姻状况
    LIVE_INFO           ("liveInfo",            "live_info"),           //现住房情况
    INDIV_EMAIL         ("email",               "indiv_email"),         //电子邮箱
    INDIV_MTH_INC       ("mthInc",              "indiv_mth_inc"),       //月均收入
    MAIL_OPT            ("deliverAddrTyp",      "mail_opt"),            //送货地址选项
    MAIL_PROVINCE       ("deliverProvince",     "mail_province"),       //送货地址省
    MAIL_CITY           ("deliverCity",         "mail_city"),           //送货地址市
    MAIL_AREA           ("deliverArea",         "mail_area"),           //送货地址区
    MAIL_ADDR           ("deliverAddr",         "mail_addr"),           //送货地址
    INDIV_EMP_NAME      ("officeName",          "indiv_emp_name"),      //现单位名称
    INDIV_EMP_TEL       ("officeTel",           "indiv_emp_tel");       //办公电话

    private String orderAttr;
    private String acquirerAttr;

    AcquirerApptEnum(String orderName, String acquirerName) {
        this.orderAttr = orderName;
        this.acquirerAttr = acquirerName;
    }

    public static String getAcquirerAttr(String orderAttr){

        for (AcquirerApptEnum acquirerApptEnum : AcquirerApptEnum.values()) {
            if (acquirerApptEnum.getOrderAttr().equals(orderAttr)) {
                return acquirerApptEnum.getAcquirerAttr();
            }
        }
        return null;
    }

    public static String getOrderAttr(String acquirerAttr) {
        for (AcquirerApptEnum acquirerApptEnum: AcquirerApptEnum.values()) {
            if (acquirerApptEnum.getAcquirerAttr().equals(acquirerAttr)) {
                return acquirerApptEnum.getOrderAttr();
            }
        }
        return null;
    }

    public String getOrderAttr() {
        return orderAttr;
    }

    public String getAcquirerAttr() {
        return acquirerAttr;
    }


}
