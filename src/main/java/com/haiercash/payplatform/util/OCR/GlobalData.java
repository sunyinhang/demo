package com.haiercash.payplatform.util.OCR;


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
	
	public static int T_ONLY_CARD_NUM = 0x0001;//����ֻʶ�𿨺�
	public static int T_SET_HEADIMG   = 0x0002;//�����Ƿ�Ҫ��ȡ��ͷ����Ϣ
	public static int T_SET_LOGPATH   = 0x0004;// ���ñ���g�����ļ�λ��
	public static int T_SET_OPENORCLOSE_LOGPATH = 0x0005;// �򿪹رձ���log�ļ�����
	public static int T_SET_HEADIMGBUFMODE = 0x0006;// ������ͷ��ģʽ 0= ԭʼ��ʽ(����android iosֱ�Ӽ���)   1=BASE64������ʽ(����sdk���紫��)
	public static int T_SET_NDCORRECTION   = 0x0007; /* �����Ƿ���л��ν�������*/
	
	public static int TUNCERTAIN	  = 0x00; /*δ֪*/
	public static int TIDCARD2      = 0x11;   /*����֤*/
	public static int TIDCARDBACK   = 0x14;   /*����֤����*/
	public static int TIDBANK       = 0x15;   /*���п�*/
	public static int TIDLPR        = 0x16;   /*����*/
	public static int TIDJSZCARD    = 0x17;   /*����*/
	public static int TIDXSZCARD    = 0x18;   /*��ʻ֤*/
	public static int TIDTICKET	  = 0x19;     /*��ʻ֤*/
	public static int TIDBIZLIC	  = 0x23;	  /*Ӫҵִ��*/
	
}
