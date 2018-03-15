package com.haiercash.payplatform.pc.alipay.util;

/**
 * Created by 许崇雷 on 2018-03-15.
 */
public enum RepayMode {
    /**
     * 处理中(可能多期)
     */
    PROCESSING,
    /**
     * 逾期(可能多期)
     */
    OVERDUE,
    /**
     * 全部(可能多期)
     */
    ALL,
    /**
     * 当前期(只能一期)
     */
    CURRENT;

    //通过名字转换,失败返回 null
    public static RepayMode forName(String name) {
        try {
            return Enum.valueOf(RepayMode.class, name);
        } catch (Exception e) {
            return null;
        }
    }
}
