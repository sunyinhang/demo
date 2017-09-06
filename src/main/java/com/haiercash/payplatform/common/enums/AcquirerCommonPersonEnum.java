package com.haiercash.payplatform.common.enums;

import java.util.Map;

import com.haiercash.payplatform.common.data.CommonRepaymentPerson;
import com.haiercash.payplatform.common.utils.FormatUtil;
import com.haiercash.payplatform.common.utils.ReflactUtils;

/**
 * enum for common person.
 * @author Liu qingxiang
 * @since v1.0
 */
public enum AcquirerCommonPersonEnum {
    RELATION        ("relation",        "appt_relation"),   // 与申请人之间的关系
    MARITAL_STATUS  ("maritalStatus",   "indiv_marital"),   // 婚姻状况
    OFFICE_NAME     ("officeName",      "emp_reg_name"),    // 单位名称
    MTH_INC         ("mthInc",          "indiv_mth_inc"),   // 月收入
    OFFICE_TEL      ("officeTel",       "indiv_emp_tel"),   // 单位电话
    NAME            ("name",            "appt_cust_name"),  // 客户姓名
    ID_NO           ("idNo",            "appt_id_no"),      // 身份证号
    MOBILE          ("mobile",          "indiv_mobile");    // 电话号码


    private String commonPersonAttr;
    private String acquirerAttr;

    AcquirerCommonPersonEnum(String commonPersonAttr, String acquirerName) {
        this.commonPersonAttr = commonPersonAttr;
        this.acquirerAttr = acquirerName;
    }

    public static String getAcquirerAttr(String commonPersonAttr){

        for (AcquirerCommonPersonEnum acquirerCommonPersonEnum : AcquirerCommonPersonEnum.values()) {
            if (acquirerCommonPersonEnum.getCommonPersonAttr().equals(commonPersonAttr)) {
                return acquirerCommonPersonEnum.getAcquirerAttr();
            }
        }
        return null;
    }

    public static String getCommonPersonAttr(String acquirerAttr){
        for (AcquirerCommonPersonEnum acquirerCommonPersonEnum : AcquirerCommonPersonEnum.values()) {
            if (acquirerCommonPersonEnum.getAcquirerAttr().equals(acquirerAttr)) {
                return acquirerCommonPersonEnum.getCommonPersonAttr();
            }
        }
        return null;
    }

    public String getCommonPersonAttr() {
        return commonPersonAttr;
    }

    public String getAcquirerAttr() {
        return acquirerAttr;
    }

    public static CommonRepaymentPerson acquirerMap2CommonPersonObject(Map<String, Object> acquirer, CommonRepaymentPerson commonRepaymentPerson) {
        acquirer.keySet().removeIf((key) -> AcquirerCommonPersonEnum.getCommonPersonAttr(key) == null);
        acquirer.forEach((key, value) -> ReflactUtils.setProperty(commonRepaymentPerson, AcquirerCommonPersonEnum.getCommonPersonAttr(key),
                FormatUtil
                        .checkValueType(AcquirerCommonPersonEnum.getCommonPersonAttr(key), value, CommonRepaymentPerson.class)));
        return commonRepaymentPerson;
    }
}
