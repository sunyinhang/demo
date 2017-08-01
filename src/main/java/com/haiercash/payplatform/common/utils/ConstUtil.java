package com.haiercash.payplatform.common.utils;

/**
 * Created by use on 2017/7/25.
 */
public class ConstUtil {
    public static String APP_CODE = "P";

    public static final String ERROR_CODE = "99";
    public static final String FAILED_INFO = "网络通讯异常";//页面数据判空
    public static final String ERROR_INFO = "网络通讯异常";//响应数据为空
    public static final String TIME_OUT = "登录超时";//token，redis失效

    public static final String THREE_PARAM_VAL_Y = "1";//需要三要素验证
    public static final String THREE_PARAM_VAL_N = "-1";//不需要三要素验证

    public static final String CERT_FILE_TYPE_A = "DOC53";//身份证正面type
    public static final String CERT_FILE_NAME_A = "身份证正面";//身份证正面name
    public static final String CERT_FILE_TYPE_B = "DOC54";//身份证反面type
    public static final String CERT_FILE_NAME_B = "身份证反面";//身份证反面name

}
