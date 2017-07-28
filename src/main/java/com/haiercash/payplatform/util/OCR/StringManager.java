package com.haiercash.payplatform.util.OCR;

//import net.sf.json.JSONException;
//import net.sf.json.JSONObject;

import org.json.JSONObject;

import java.io.*;

public class StringManager {
	public  String headimg = "";
	public  String NAME                = "";/* ����*/
	public  String SEX                 = "";/* �Ա�*/
	public  String FOLK                = "";/* ����*/
	public  String BIRTHDAY            = "";/* ��������*/
	public  String ADDRESS             = "";/* ��ַ*/
	public  String NUM                 = "";/* ����*/
	public  String ISSUE               = "";/* ǩ������*/
	public  String PERIOD              = "";/* ��Ч����*/
	public  String LPR_NUM			   = "";/* ���ƺ���*/
	public  String LPR_PLATECOLOR	   = "";/* ������ɫ*/
	//��ʻ֤
	public  String DP_PLATENO		   = "";/* ���ƺ���*/
	public  String DP_TYPE			   = "";/* ��������*/
	public  String DP_OWNER				= "";/* ������*/
	public  String DP_ADDRESS			= "";/* סַ*/
	public  String DP_USECHARACTER		= "";/* ʹ������*/
	public  String DP_MODEL				= "";/* Ʒ�ƺ���*/
	public  String DP_VIN				= "";/* ����ʶ�����*/
	public  String DP_ENGINENO			= "";/* ����������*/
	public  String DP_REGISTER_DATE		= "";/* ע������*/
	public  String DP_ISSUE_DATE		= "";/* ��֤����*/

	//��ʻ֤
	public  String DL_NUM				= "";/* ����*/
	public  String DL_NAME				= "";/* ����*/
	public  String DL_SEX				= "";/* �Ա�*/
	public  String DL_COUNTRY			= "";/* ����*/
	public  String DL_ADDRESS			= "";/* ��ַ*/
	public  String DL_BIRTHDAY			= "";/* ��������*/
	public  String DL_ISSUE_DATE		= "";/*������֤����*/
	public  String DL_CLASS				= "";/*׼�ݳ���*/
	public  String DL_VALIDFROM			= "";/*��Ч��ʼ����*/
	public  String DL_VALIDFOR			= "";/* ��Ч����*/

	//��Ʊ
	public  String TIC_START			= "";/* ��ʼվ*/
	public  String TIC_NUM				= "";/* ����*/
	public  String TIC_END				= "";/* �յ�վ*/
	public  String TIC_TIME				= "";/* ����ʱ��*/
	public  String TIC_SEAT				= "";/* ��λ��*/
	public  String TIC_NAME				= "";/* ����*/

	public  String BLIC_CODE			= "";/* ͳһ������ô���*/ 
	public  String BLIC_NAME			= ""; /*����*/
	public  String BLIC_TYPE			= ""; /*����*/
	public  String BLIC_ADDR			= ""; /*ס��*/
	public  String BLIC_PERSON			= ""; /*����������*/
	public  String BLIC_CAPTIAL			= ""; /*ע���ʱ�*/
	public  String BLIC_DATE			= ""; /*��������*/
	public  String BLIC_PERIOD			= ""; /*Ӫҵ����*/
    public  String BLIC_ISSUE			= ""; /*��֤����*/
	/*���п��ֶ�*/
	public  String TBANK_NUM			= "";//��ȡ���п���
	public  String TBANK_NAME			= "";//��ȡ���п�������
	public  String TBANK_ORGCODE		= "";//��ȡ���л�������
	public  String TBANK_CLASS			= "";//��ȡ����
	public  String TBANK_CARD_NAME		= "";//��ȡ����
	public  int initString()
	{
		headimg 			= "";
		NAME                = "";/* ����*/
		SEX                 = "";/* �Ա�*/
		FOLK                = "";/* ����*/
		BIRTHDAY            = "";/* ��������*/
		ADDRESS             = "";/* ��ַ*/
		NUM                 = "";/* ����*/
		ISSUE               = "";/* ǩ������*/
		PERIOD              = "";/* ��Ч����*/
		LPR_NUM				= "";/* ���ƺ���*/
		LPR_PLATECOLOR		= "";/* ������ɫ*/
		//��ʻ֤
		DP_PLATENO		   = "";/* ���ƺ���*/
		DP_TYPE			   = "";/* ��������*/
		DP_OWNER				= "";/* ������*/
		DP_ADDRESS			= "";/* סַ*/
		DP_USECHARACTER		= "";/* ʹ������*/
		DP_MODEL				= "";/* Ʒ�ƺ���*/
		DP_VIN				= "";/* ����ʶ�����*/
		DP_ENGINENO			= "";/* ����������*/
		DP_REGISTER_DATE		= "";/* ע������*/
		DP_ISSUE_DATE		= "";/* ��֤����*/

		//��ʻ֤
		DL_NUM				= "";/* ����*/
		DL_NAME				= "";/* ����*/
		DL_SEX				= "";/* �Ա�*/
		DL_COUNTRY			= "";/* ����*/
		DL_ADDRESS			= "";/* ��ַ*/
		DL_BIRTHDAY			= "";/* ��������*/
		DL_ISSUE_DATE		= "";/*������֤����*/
		DL_CLASS				= "";/*׼�ݳ���*/
		DL_VALIDFROM			= "";/*��Ч��ʼ����*/
		DL_VALIDFOR			= "";/* ��Ч����*/

		//��Ʊ
		TIC_START			= "";/* ��ʼվ*/
		TIC_NUM				= "";/* ����*/
		TIC_END				= "";/* �յ�վ*/
		TIC_TIME				= "";/* ����ʱ��*/
		TIC_SEAT				= "";/* ��λ��*/
		TIC_NAME				= "";/* ����*/

		BLIC_CODE			= "";/* ͳһ������ô���*/ 
		BLIC_NAME			= ""; /*����*/
		BLIC_TYPE			= ""; /*����*/
		BLIC_ADDR			= ""; /*ס��*/
		BLIC_PERSON			= ""; /*����������*/
		BLIC_CAPTIAL			= ""; /*ע���ʱ�*/
		BLIC_DATE			= ""; /*��������*/
		BLIC_PERIOD			= ""; /*Ӫҵ����*/
	    BLIC_ISSUE			= ""; /*��֤����*/
		/*���п��ֶ�*/
		TBANK_NUM			= "";//��ȡ���п���
		TBANK_NAME			= "";//��ȡ���п�������
		TBANK_ORGCODE		= "";//��ȡ���л�������
		TBANK_CLASS			= "";//��ȡ����
		TBANK_CARD_NAME		= "";//��ȡ����
		return 1;
	}
	public int FomatJsonString(String jsonString)
	{
		initString();
		try {
			JSONObject jsonObject01 = new JSONObject(jsonString);
		    //JSONObject jsonObject01 = JSONObject.fromObject(jsonString);
			headimg = jsonObject01.getString("headimg");
			NAME                = jsonObject01.getString("NAME");/* ����*/
			SEX                 = jsonObject01.getString("SEX");/* �Ա�*/
			FOLK                = jsonObject01.getString("FOLK");/* ����*/
			BIRTHDAY            = jsonObject01.getString("BIRTHDAY");/* ��������*/
			ADDRESS             = jsonObject01.getString("ADDRESS");/* ��ַ*/
			NUM                 = jsonObject01.getString("NUM");/* ����*/
			ISSUE               = jsonObject01.getString("ISSUE");/* ǩ������*/
			PERIOD              = jsonObject01.getString("PERIOD");/* ��Ч����*/
			LPR_NUM				= jsonObject01.getString("LPR_NUM");/* ���ƺ���*/
			LPR_PLATECOLOR		= jsonObject01.getString("LPR_PLATECOLOR");/* ������ɫ*/
			//��ʻ֤
			DP_PLATENO		   = jsonObject01.getString("DP_PLATENO");/* ���ƺ���*/
			DP_TYPE			   = jsonObject01.getString("DP_TYPE");/* ��������*/
			DP_OWNER			= jsonObject01.getString("DP_OWNER");/* ������*/
			DP_ADDRESS			= jsonObject01.getString("DP_ADDRESS");/* סַ*/
			DP_USECHARACTER		= jsonObject01.getString("DP_USECHARACTER");/* ʹ������*/
			DP_MODEL				= jsonObject01.getString("DP_MODEL");/* Ʒ�ƺ���*/
			DP_VIN				= jsonObject01.getString("DP_VIN");/* ����ʶ�����*/
			DP_ENGINENO			= jsonObject01.getString("DP_ENGINENO");/* ����������*/
			DP_REGISTER_DATE		= jsonObject01.getString("DP_REGISTER_DATE");/* ע������*/
			DP_ISSUE_DATE		= jsonObject01.getString("DP_ISSUE_DATE");/* ��֤����*/

			//��ʻ֤
			DL_NUM				= jsonObject01.getString("DL_NUM");/* ����*/
			DL_NAME				= jsonObject01.getString("DL_NAME");/* ����*/
			DL_SEX				= jsonObject01.getString("DL_SEX");/* �Ա�*/
			DL_COUNTRY			= jsonObject01.getString("DL_COUNTRY");/* ����*/
			DL_ADDRESS			= jsonObject01.getString("DL_ADDRESS");/* ��ַ*/
			DL_BIRTHDAY			= jsonObject01.getString("DL_BIRTHDAY");/* ��������*/
			DL_ISSUE_DATE		= jsonObject01.getString("DL_ISSUE_DATE");/*������֤����*/
			DL_CLASS				= jsonObject01.getString("DL_CLASS");/*׼�ݳ���*/
			DL_VALIDFROM			= jsonObject01.getString("DL_VALIDFROM");/*��Ч��ʼ����*/
			DL_VALIDFOR			= jsonObject01.getString("DL_VALIDFOR");/* ��Ч����*/

			//��Ʊ
			TIC_START			= jsonObject01.getString("TIC_START");/* ��ʼվ*/
			TIC_NUM				= jsonObject01.getString("TIC_NUM");/* ����*/
			TIC_END				= jsonObject01.getString("TIC_END");/* �յ�վ*/
			TIC_TIME				= jsonObject01.getString("TIC_TIME");/* ����ʱ��*/
			TIC_SEAT				= jsonObject01.getString("TIC_SEAT");/* ��λ��*/
			TIC_NAME				= jsonObject01.getString("TIC_NAME");/* ����*/

			BLIC_CODE			= jsonObject01.getString("BLIC_CODE");/* ͳһ������ô���*/ 
			BLIC_NAME			= jsonObject01.getString("BLIC_NAME"); /*����*/
			BLIC_TYPE			= jsonObject01.getString("BLIC_TYPE"); /*����*/
			BLIC_ADDR			= jsonObject01.getString("BLIC_ADDR"); /*ס��*/
			BLIC_PERSON			= jsonObject01.getString("BLIC_PERSON"); /*����������*/
			BLIC_CAPTIAL		= jsonObject01.getString("BLIC_CAPTIAL"); /*ע���ʱ�*/
			BLIC_DATE			= jsonObject01.getString("BLIC_DATE"); /*��������*/
			BLIC_PERIOD			= jsonObject01.getString("BLIC_PERIOD"); /*Ӫҵ����*/
		    BLIC_ISSUE			= jsonObject01.getString("BLIC_ISSUE"); /*��֤����*/
			/*���п��ֶ�*/
			TBANK_NUM			= jsonObject01.getString("TBANK_NUM");//��ȡ���п���
			TBANK_NAME			= jsonObject01.getString("TBANK_NAME");//��ȡ���п�������
			TBANK_ORGCODE		= jsonObject01.getString("TBANK_ORGCODE");//��ȡ���л�������
			TBANK_CLASS			= jsonObject01.getString("TBANK_CLASS");//��ȡ����
			TBANK_CARD_NAME		= jsonObject01.getString("TBANK_CARD_NAME");//��ȡ����
			} catch (Exception e1) {

			// TODO Auto-generated catch block

			e1.printStackTrace();

			}
		return 1;
	}
	public static byte [] Decode(byte[] Data,int DataByte,int []len)
	{
		//�����
		byte [] strDecode = new byte[DataByte];
		byte []DecodeTable =
		{
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			62, // '+'
			0, 0, 0,
			63, // '/'
			52, 53, 54, 55, 56, 57, 58, 59, 60, 61, // '0'-'9'
			0, 0, 0, 0, 0, 0, 0,
			0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12,
			13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, // 'A'-'Z'
			0, 0, 0, 0, 0, 0,
			26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38,
			39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, // 'a'-'z'
		};
		int nValue;
		int i= 0;
		int j =0;
		int k =0;
		len[0]=0;
		while (i < DataByte)
		{
			if (Data[j] != '\r' && Data[j]!='\n')
			{
				nValue = DecodeTable[Data[j++]] << 18;
				nValue += DecodeTable[Data[j++]] << 12;
				strDecode[k++]=(byte) ((nValue & 0x00FF0000) >> 16);
				//OutByte++;
				if (Data[j] != '=')
				{
					nValue += DecodeTable[Data[j++]] << 6;
					strDecode[k++]=(byte) ((nValue & 0x0000FF00) >> 8);
					//OutByte++;
					if (Data[j] != '=')
					{
						nValue += DecodeTable[Data[j++]];
					
						strDecode[k++]=(byte) (nValue & 0x000000FF);
						//OutByte++;
					}
				}
				i += 4;
			}
			else// �س�����,����
			{
				j++;
				i++;
			}
		}
		len[0] = k;
		return strDecode;
	}
	public static int SaveJPGFile(String path,byte [] buffer)
	{
		if (buffer == null || buffer.length <= 0) {
			return 0;
		}
		 File img = new File(path);   
         try {
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(img));
			int [] len = new int[1];
			int lennnn = buffer.length;
			byte [] jpg = StringManager.Decode(buffer, buffer.length,len);
			//int len = jpg.length;
			//out.write(jpg);
			out.write(jpg, 0, len[0]);
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}   
         return 1;
	}
}
