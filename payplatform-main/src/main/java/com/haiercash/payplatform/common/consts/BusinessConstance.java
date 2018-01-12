/*
 * 功  能：简单说明该类的功能
 *
 * 文件名：BusinessConstance.java
 *
 * 描  述：
 *
 * [变更履历]
 * Version   变更日         部署              作者           变更内容
 * -----------------------------------------------------------------------------
 * V1.00     2015年12月23日   Haiercash    xuchao      CREATE
 * -----------------------------------------------------------------------------
 *
 *
 * Copyright (c) 2015 Haiercash All Rights Reserved.
 *┌─────────────────────────────────────────────────—────┐
 *│ 版权声明                               Haiercash       │
 *└──────────────────────────────—————————————————————───┘
 */

package com.haiercash.payplatform.common.consts;

/**
 * DESCRIPTION:常量类
 * <p>
 * <p>
 * <a href="BusinessConstance.java"><i>View Source</i></a>
 * </p>
 *
 * @author <a href="mailto:xuchao@haiercash.com">xuchao</a>
 * @version Revision: 1.0  Date: 2015年12月23日 下午9:10:29
 */

public class BusinessConstance {
    public static final String sysFlag = "11";//海尔消费金融支付平台
    public static final String channelNo = "15";//海尔消费金融支付平台


//	public static final String sucess_code="0000";
//	public static Map DataConfigMap;
//	public static Map ChannelConfigurationMap;
//	public static Map CooperativeBusinessMap;
//	public static Map CooperativeRSAMap;
//	public static Map CADataConfigMap;
//	public static Map CooperativeMsgMap;
//	public static Map BaiChengParamsMap;
//
//	//渠道编号
//	public static final String CHANNELNO_WUNIU = "CD9998";
//
//	//CA签署文档类型01，实名认证；02，征信查询授权；03，循环额度合同；04，贷款支用合同,05,美创贷款申请合同
//	public static final String DOC_TYPE_USERVALIDATE = "01";
//	public static final String DOC_TYPE_CREDITREPORTING = "02";
//	public static final String DOC_TYPE_CREDITINFO = "03";
//	public static final String DOC_TYPE_LOANAPPLY = "04";
//	public static final String DOC_TYPE_MEICHUANGLOANAPPLY = "05";
//	public static final String DOC_TYPE_WUNIULOANDOC = "06";//屋牛贷款申请文档
//
//
//	//屋牛公钥
//	public static final String WU_NIU_RSA_PUBLICKEY = "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAI4A6cESFwMUTAL+XWB1ZAbYPivHpr+Q1ps0foCE9IqEkNKzC3sg5YWTo5Br06vwSxYWIVaMVMFj39+EdIx/hFkCAwEAAQ==";
//	public static final String WU_NIU_RSA_PRIVATEKEY = "MIIBUwIBADANBgkqhkiG9w0BAQEFAASCAT0wggE5AgEAAkEAjgDpwRIXAxRMAv5dYHVkBtg+K8emv5DWmzR+gIT0ioSQ0rMLeyDlhZOjkGvTq/BLFhYhVoxUwWPf34R0jH+EWQIDAQABAkB7ajO+RviVBojheSyybpQWls3Bwu0bRMgfb8oZ7HOUBj2lftzLbxM0w4aEAg55yJywuenRTzIOhhIZNtpbAAOZAiEA5aL6a24GFV51JfrWZBJN1MVeYGnHP2O6F+xNgF4YwRcCIQCeTmYM2oqwNNL8S+XOHK54LdQVsgWMCC97WSxrqi3sDwIgCb2sGbqFD4r+NSo6xevw2UG2CyjxhsLYNgOUFgnnyukCIAZxNE/zmM9A5xEjWY/AtQzo+IeFfhpLvvrrDxa2jzXbAiBdakH3kwgTktNyTTFs8trhFcZViEfxQraCOhNlfr3M2g==";
//
//	//徙木公钥
//	public static final String XI_MU_RSA_PUBLICKEY = "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBALtsgltcfuTxnRzDoOWbk4+Ja7WRq6SRmTbaPsJ8Njh269iBldFRv4tGZ6xgAg+d87QbRKSXVox/yFUfVGR2Cc0CAwEAAQ==";
//	//美创公钥
//	public static final String MEICHUANG_RSA_PUBLICKEY =  "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAMsAEWQepQLdo3mhgw3mLU00n/ldDd4knbq01bH89XnE5RITn+qZBAkHow5jgwH2T5ve2KJzkrHD6JbDDQtRO0UCAwEAAQ==";
//	public static final String  XIMU_SUCCESS_CODE = "200";//成功返回值
//	public static final String  SUCCESS_CODE = "0000";//成功
//	//错误码
//	public static final String ERROEKEY_NULL = "0001";//传入参数为空
//	public static final String ERROEKEY_DECRYPTERROR = "0002";//公钥解密失败
//	public static final String ERROEKEY_SYSERROR = "0003";//内部错误失败
//	public static final String ERROEKEY_IMAGEERROR = "0004";//文件上传失败
//	public static final String ERROEKEY_IMAGEERROR01 = "0008";//文件列表不存在
//
//	public static final String ERROEKEY_APPLSEQERROR = "0005";//applseq不存在
//	public static final String ERROEKEY_APPLSEQRepeatERROR = "0006";//applseq重复通知
//	public static final String ERROEKEY_PARAMETERERROR = "0007";//参数违法-格式不对
//	public static final String ERROEKEY_MESSAGEERROR = "1111";//信息错误
//	public static final String ERROEKEY_CHANNELNOERROR = "0009";//渠道号不能为空
//	public static final String ERROEKEY_APPNOERROR = "0010";//流水号不允许为空
//	public static final String ERROEKEY_FILEERROR = "0011";//文件类型不能为空
//
//	public static final String ERROEKEY_UNTREATED = "0012";//未处理
//
//	/**
//	 * 宇信状态
//	 */
//	public  static final String CMIS_LOAN_UNLOAN = "00";//申请录入
//	public  static final String CMIS_LOAN_SUCCESS = "04";//合同签订中
//	public  static final String CMIS_LOAN_REFUSE = "02";//贷款被拒绝
//	public  static final String CMIS_LOAN_DOING = "01";//审批中
//	public  static final String CMIS_LOAN_CANCLE = "03";//取消
//	public  static final String CMIS_LOAN_CARRYBACK = "05";//审批通过，等待放款
//	public  static final String CMIS_LOAN_CALLBACK = "06";//已放款
//	public  static final String CMIS_LOAN_RETREAT = "20";//待放款
//	public  static final String CMIS_LOAN_CANCELED = "22";//审批退回
//	public  static final String CMIS_LOAN_ADDITIONAL = "23";//合同签章中
//	public  static final String CMIS_LOAN_LOAN_APPROVE = "24";//放款审核中
//	public  static final String CMIS_CREDIT_REFUSE = "25";//额度申请被拒
//	public  static final String CMIS_CREDIT_CANCLE = "26";//额度申请已取消
//	public  static final String CMIS_CREDIT_PASS = "27";//通过
//	public  static final String CMIS_LOAN_CACLE = "AA";//取消放款
//	public  static final String CMIS_LOAN_OUTER_APPLY = "WW";//外界系统审批中
//
//
//	/**
//	 * 线程处理状态
//	 */
//	public  static final String THREAD_DOING = "01";//处理中
//	public  static final String THREAD_UNDO = "00";//待处理
//	public  static final String THREAD_SUCCESS= "02";//处理成功
//	public  static final String THREAD_OUT_SUCCESS= "03";//外部系统处理成功
//	public  static final String THREAD_OUT_LOAN_SUCCESS= "05";//外部系统贷款处理成功
//	public  static final String THREAD_COMFIRM_UNDO= "99";//等待确认处理
//
//	/**
//	 * 任务处理
//	 */
//	public  static final String  TASK_CA= "A";//CA签署
//	public  static final String  TASK_FILEUPLOAD= "B";//附件上传异步任务
//	public  static final String  TASK_OUTERTASK= "C";//第三方合作审批异步任务
//
    /**
     * 商户初始化表状态
     */
    public static final String COOPERATIVEBUSINESS_INACTIVE = "00";//未激活
    public static final String COOPERATIVEBUSINESS_ACTIVE = "01";//激活
//
//	/**
//	 * 进件类型
//	 */
//	public  static final String  APPLY_TYPE_CREDIT= "01";//额度申请
//	public  static final String  APPLY_TYPE_LOAN= "02";//额度支用
//	public  static final String  APPLY_TYPE_CREDITANDLOAN= "03";//额度支用加申请
//
//	/**
//	 * 进件渠道解密类型
//	 */
//	public  static final String  APPLY_RSA_METHOD_01="01";//解密方式，私钥加密，公钥解密
//	public  static final String  APPLY_RSA_METHOD_02="02";//解密方式，公钥加密，私钥解密
//
//	/**
//	 * 银联验证签名状态
//	 */
//	public  static final String CHINAPAY_VALIDATE_OK = "0000";//验证成功
//	public  static final String CHINAPAY_VALIDATE_FALSE = "0001";//验证失败
//
//	/**
//	 * 商户初始化表状态
//	 */
//	public  static final String  COOPERATIVEMSG_INACTIVE= "00";//未激活
//	public  static final String  COOPERATIVEMSG_ACTIVE= "01";//激活

}
