package com.idcard;
//import net.sf.json.JSONException;
//import net.sf.json.JSONObject;

import org.json.JSONObject;

import java.io.*;

public class StringManager {
	public  String headimg = "";
	public  String NAME                = "";/* 姓名*/
	public  String SEX                 = "";/* 性别*/
	public  String FOLK                = "";/* 民族*/
	public  String BIRTHDAY            = "";/* 出生日期*/
	public  String ADDRESS             = "";/* 地址*/
	public  String NUM                 = "";/* 号码*/
	public  String ISSUE               = "";/* 签发机关*/
	public  String PERIOD              = "";/* 有效期限*/
	public  String LPR_NUM			   = "";/* 车牌号码*/
	public  String LPR_PLATECOLOR	   = "";/* 车牌颜色*/
	//行驶证
	public  String DP_PLATENO		   = "";/* 号牌号码*/
	public  String DP_TYPE			   = "";/* 车辆类型*/
	public  String DP_OWNER				= "";/* 所有人*/
	public  String DP_ADDRESS			= "";/* 住址*/
	public  String DP_USECHARACTER		= "";/* 使用性质*/
	public  String DP_MODEL				= "";/* 品牌号码*/
	public  String DP_VIN				= "";/* 车辆识别代号*/
	public  String DP_ENGINENO			= "";/* 发动机号码*/
	public  String DP_REGISTER_DATE		= "";/* 注册日期*/
	public  String DP_ISSUE_DATE		= "";/* 发证日期*/

	//驾驶证
	public  String DL_NUM				= "";/* 号码*/
	public  String DL_NAME				= "";/* 姓名*/
	public  String DL_SEX				= "";/* 性别*/
	public  String DL_COUNTRY			= "";/* 国籍*/
	public  String DL_ADDRESS			= "";/* 地址*/
	public  String DL_BIRTHDAY			= "";/* 出生日期*/
	public  String DL_ISSUE_DATE		= "";/*初次领证日期*/
	public  String DL_CLASS				= "";/*准驾车型*/
	public  String DL_VALIDFROM			= "";/*有效起始日期*/
	public  String DL_VALIDFOR			= "";/* 有效期限*/

	//火车票
	public  String TIC_START			= "";/* 起始站*/
	public  String TIC_NUM				= "";/* 车次*/
	public  String TIC_END				= "";/* 终点站*/
	public  String TIC_TIME				= "";/* 发车时间*/
	public  String TIC_SEAT				= "";/* 座位号*/
	public  String TIC_NAME				= "";/* 姓名*/

	public  String BLIC_CODE			= "";/* 统一社会信用代码*/
	public  String BLIC_NAME			= ""; /*名称*/
	public  String BLIC_TYPE			= ""; /*类型*/
	public  String BLIC_ADDR			= ""; /*住所*/
	public  String BLIC_PERSON			= ""; /*法定代表人*/
	public  String BLIC_CAPTIAL			= ""; /*注册资本*/
	public  String BLIC_DATE			= ""; /*成立日期*/
	public  String BLIC_PERIOD			= ""; /*营业期限*/
	public  String BLIC_ISSUE			= ""; /*发证日期*/
	/*银行卡字段*/
	public  String TBANK_NUM			= "";//获取银行卡号
	public  String TBANK_NAME			= "";//获取银行卡开户行
	public  String TBANK_ORGCODE		= "";//获取银行机构代码
	public  String TBANK_CLASS			= "";//获取卡种
	public  String TBANK_CARD_NAME		= "";//获取卡名
	public  int initString()
	{
		headimg 			= "";
		NAME                = "";/* 姓名*/
		SEX                 = "";/* 性别*/
		FOLK                = "";/* 民族*/
		BIRTHDAY            = "";/* 出生日期*/
		ADDRESS             = "";/* 地址*/
		NUM                 = "";/* 号码*/
		ISSUE               = "";/* 签发机关*/
		PERIOD              = "";/* 有效期限*/
		LPR_NUM				= "";/* 车牌号码*/
		LPR_PLATECOLOR		= "";/* 车牌颜色*/
		//行驶证
		DP_PLATENO		   = "";/* 号牌号码*/
		DP_TYPE			   = "";/* 车辆类型*/
		DP_OWNER				= "";/* 所有人*/
		DP_ADDRESS			= "";/* 住址*/
		DP_USECHARACTER		= "";/* 使用性质*/
		DP_MODEL				= "";/* 品牌号码*/
		DP_VIN				= "";/* 车辆识别代号*/
		DP_ENGINENO			= "";/* 发动机号码*/
		DP_REGISTER_DATE		= "";/* 注册日期*/
		DP_ISSUE_DATE		= "";/* 发证日期*/

		//驾驶证
		DL_NUM				= "";/* 号码*/
		DL_NAME				= "";/* 姓名*/
		DL_SEX				= "";/* 性别*/
		DL_COUNTRY			= "";/* 国籍*/
		DL_ADDRESS			= "";/* 地址*/
		DL_BIRTHDAY			= "";/* 出生日期*/
		DL_ISSUE_DATE		= "";/*初次领证日期*/
		DL_CLASS				= "";/*准驾车型*/
		DL_VALIDFROM			= "";/*有效起始日期*/
		DL_VALIDFOR			= "";/* 有效期限*/

		//火车票
		TIC_START			= "";/* 起始站*/
		TIC_NUM				= "";/* 车次*/
		TIC_END				= "";/* 终点站*/
		TIC_TIME				= "";/* 发车时间*/
		TIC_SEAT				= "";/* 座位号*/
		TIC_NAME				= "";/* 姓名*/

		BLIC_CODE			= "";/* 统一社会信用代码*/
		BLIC_NAME			= ""; /*名称*/
		BLIC_TYPE			= ""; /*类型*/
		BLIC_ADDR			= ""; /*住所*/
		BLIC_PERSON			= ""; /*法定代表人*/
		BLIC_CAPTIAL			= ""; /*注册资本*/
		BLIC_DATE			= ""; /*成立日期*/
		BLIC_PERIOD			= ""; /*营业期限*/
		BLIC_ISSUE			= ""; /*发证日期*/
		/*银行卡字段*/
		TBANK_NUM			= "";//获取银行卡号
		TBANK_NAME			= "";//获取银行卡开户行
		TBANK_ORGCODE		= "";//获取银行机构代码
		TBANK_CLASS			= "";//获取卡种
		TBANK_CARD_NAME		= "";//获取卡名
		return 1;
	}
	public int FomatJsonString(String jsonString)
	{
		initString();
		try {
			JSONObject jsonObject01 = new JSONObject(jsonString);
			headimg = jsonObject01.getString("headimg");
			NAME                = jsonObject01.getString("NAME");/* 姓名*/
			SEX                 = jsonObject01.getString("SEX");/* 性别*/
			FOLK                = jsonObject01.getString("FOLK");/* 民族*/
			BIRTHDAY            = jsonObject01.getString("BIRTHDAY");/* 出生日期*/
			ADDRESS             = jsonObject01.getString("ADDRESS");/* 地址*/
			NUM                 = jsonObject01.getString("NUM");/* 号码*/
			ISSUE               = jsonObject01.getString("ISSUE");/* 签发机关*/
			PERIOD              = jsonObject01.getString("PERIOD");/* 有效期限*/
			LPR_NUM				= jsonObject01.getString("LPR_NUM");/* 车牌号码*/
			LPR_PLATECOLOR		= jsonObject01.getString("LPR_PLATECOLOR");/* 车牌颜色*/
			//行驶证
			DP_PLATENO		   = jsonObject01.getString("DP_PLATENO");/* 号牌号码*/
			DP_TYPE			   = jsonObject01.getString("DP_TYPE");/* 车辆类型*/
			DP_OWNER			= jsonObject01.getString("DP_OWNER");/* 所有人*/
			DP_ADDRESS			= jsonObject01.getString("DP_ADDRESS");/* 住址*/
			DP_USECHARACTER		= jsonObject01.getString("DP_USECHARACTER");/* 使用性质*/
			DP_MODEL				= jsonObject01.getString("DP_MODEL");/* 品牌号码*/
			DP_VIN				= jsonObject01.getString("DP_VIN");/* 车辆识别代号*/
			DP_ENGINENO			= jsonObject01.getString("DP_ENGINENO");/* 发动机号码*/
			DP_REGISTER_DATE		= jsonObject01.getString("DP_REGISTER_DATE");/* 注册日期*/
			DP_ISSUE_DATE		= jsonObject01.getString("DP_ISSUE_DATE");/* 发证日期*/

			//驾驶证
			DL_NUM				= jsonObject01.getString("DL_NUM");/* 号码*/
			DL_NAME				= jsonObject01.getString("DL_NAME");/* 姓名*/
			DL_SEX				= jsonObject01.getString("DL_SEX");/* 性别*/
			DL_COUNTRY			= jsonObject01.getString("DL_COUNTRY");/* 国籍*/
			DL_ADDRESS			= jsonObject01.getString("DL_ADDRESS");/* 地址*/
			DL_BIRTHDAY			= jsonObject01.getString("DL_BIRTHDAY");/* 出生日期*/
			DL_ISSUE_DATE		= jsonObject01.getString("DL_ISSUE_DATE");/*初次领证日期*/
			DL_CLASS				= jsonObject01.getString("DL_CLASS");/*准驾车型*/
			DL_VALIDFROM			= jsonObject01.getString("DL_VALIDFROM");/*有效起始日期*/
			DL_VALIDFOR			= jsonObject01.getString("DL_VALIDFOR");/* 有效期限*/

			//火车票
			TIC_START			= jsonObject01.getString("TIC_START");/* 起始站*/
			TIC_NUM				= jsonObject01.getString("TIC_NUM");/* 车次*/
			TIC_END				= jsonObject01.getString("TIC_END");/* 终点站*/
			TIC_TIME				= jsonObject01.getString("TIC_TIME");/* 发车时间*/
			TIC_SEAT				= jsonObject01.getString("TIC_SEAT");/* 座位号*/
			TIC_NAME				= jsonObject01.getString("TIC_NAME");/* 姓名*/

			BLIC_CODE			= jsonObject01.getString("BLIC_CODE");/* 统一社会信用代码*/
			BLIC_NAME			= jsonObject01.getString("BLIC_NAME"); /*名称*/
			BLIC_TYPE			= jsonObject01.getString("BLIC_TYPE"); /*类型*/
			BLIC_ADDR			= jsonObject01.getString("BLIC_ADDR"); /*住所*/
			BLIC_PERSON			= jsonObject01.getString("BLIC_PERSON"); /*法定代表人*/
			BLIC_CAPTIAL		= jsonObject01.getString("BLIC_CAPTIAL"); /*注册资本*/
			BLIC_DATE			= jsonObject01.getString("BLIC_DATE"); /*成立日期*/
			BLIC_PERIOD			= jsonObject01.getString("BLIC_PERIOD"); /*营业期限*/
			BLIC_ISSUE			= jsonObject01.getString("BLIC_ISSUE"); /*发证日期*/
			/*银行卡字段*/
			TBANK_NUM			= jsonObject01.getString("TBANK_NUM");//获取银行卡号
			TBANK_NAME			= jsonObject01.getString("TBANK_NAME");//获取银行卡开户行
			TBANK_ORGCODE		= jsonObject01.getString("TBANK_ORGCODE");//获取银行机构代码
			TBANK_CLASS			= jsonObject01.getString("TBANK_CLASS");//获取卡种
			TBANK_CARD_NAME		= jsonObject01.getString("TBANK_CARD_NAME");//获取卡名
		} catch (Exception e1) {

			// TODO Auto-generated catch block

			e1.printStackTrace();

		}
		return 1;
	}
	public static byte [] Decode(byte[] Data,int DataByte,int []len)
	{
		//解码表
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
			else// 回车换行,跳过
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
