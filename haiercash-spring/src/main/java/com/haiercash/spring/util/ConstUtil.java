package com.haiercash.spring.util;

/**
 * Created by use on 2017/7/25.
 */
public final class ConstUtil {
    public static final String SUCCESS_CODE = "00000";
    public static final String SUCCESS_CODE2 = "0000";
    public static final String SUCCESS_MSG = "处理成功";
    public static final String ERROR_CODE = "99";
    public static final String ERROR_MSG = "网络通讯异常";//页面数据判空
    public static final String TIME_OUT = "登录超时";//token，redis失效
    public static final String THREE_PARAM_VAL_Y = "1";//需要三要素验证
    public static final String THREE_PARAM_VAL_N = "-1";//不需要三要素验证
    public static final String CERT_FILE_TYPE_A = "DOC53";//身份证正面type
    public static final String CERT_FILE_NAME_A = "身份证正面";//身份证正面name
    public static final String CERT_FILE_TYPE_B = "DOC54";//身份证反面type
    public static final String CERT_FILE_NAME_B = "身份证反面";//身份证反面name
    public static final String ATTACHTYPE_DOC065 = "DOC065";//影像类型
    public static final String ATTACHTYPE_DOC065_DESC = "人脸照片";
    public static final String ATTACHTYPE_APP01 = "App01";//影像类型
    public static final String ATTACHTYPE_APP01_DESC = "本人手持身份证与本人面部合照照片";//影像类型名称
    public static final String ATTACHTYPE_DOC099 = "DOC012";//影像类型 其他
    public static final String CHANNEL = "11";
    public static final String SOURCE = "11";
    public static final String ERROR_PARAM_INVALID_CODE = "91";
    public static final String ERROR_PARAM_INVALID_MSG = "参数无效";
}
