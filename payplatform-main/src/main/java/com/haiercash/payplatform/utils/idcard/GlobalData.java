package com.haiercash.payplatform.utils.idcard;


public class GlobalData {
	public static int NAME  = 0;
	public static int SEX   = 1;
	public static int FOLK  = 2;
	public static int BIRTHDAY = 3;
	public static int ADDRESS = 4;
	public static int NUM   = 5;
	public static int issue = 6;
	public static int period = 7;

	public static int isfirst = 0;
	public static int x =0;
	public static int y =0;
	public static int m =0;
	public static int n =0;

	public static int T_ONLY_CARD_NUM = 0x0001;//设置只识别卡号
	public static int T_SET_HEADIMG   = 0x0002;//设置是否要截取人头像信息
	public static int T_SET_LOGPATH   = 0x0004;// 设置保存g保存文件位置
	public static int T_SET_OPENORCLOSE_LOGPATH = 0x0005;// 打开关闭保存log文件功能
	public static int T_SET_HEADIMGBUFMODE = 0x0006;// 设置人头像模式 0= 原始形式(便于android ios直接加载)   1=BASE64加密形式(便于sdk网络传输)
	public static int T_SET_NDCORRECTION   = 0x0007; 		/* 设置是否进行畸形矫正功能*/
	public static int T_SET_PER_CALL_ACCOUNT	  = 0x0017; /* 设置授权用户名*/
	public static int T_SET_PER_CALL_PASSWORD	  = 0x0018; /* 设置授权密码*/
	public static int T_SET_PER_CALL_SERVERURL  = 0x0019;	/* 设置服务接口*/
	public static int T_SET_PER_CALL_TIMEOUT	  = 0x0020; /* 设置超时时长*/

	public static int TUNCERTAIN	  = 0x00; /*未知*/
	public static int TIDCARD2      = 0x11;   /*二代证*/
	public static int TIDCARDBACK   = 0x14;   /*二代证背面*/
	public static int TIDBANK       = 0x15;   /*银行卡*/
	public static int TIDLPR        = 0x16;   /*车牌*/
	public static int TIDJSZCARD    = 0x17;   /*驾照*/
	public static int TIDXSZCARD    = 0x18;   /*行驶证*/
	public static int TIDTICKET	  	= 0x19;   /*行驶证*/
	public static int TIDSSCCARD	= 0x20;   /*社保卡*/
	public static int TIDPASSPORT	= 0x21;	  /*护照*/
	public static int TIDBIZLIC	  	= 0x23;	  /*营业执照*/
	public static int TIDRMB		= 0x26;	  /*人民币冠字号*/
}
