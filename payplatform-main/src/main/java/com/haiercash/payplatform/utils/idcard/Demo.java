package com.haiercash.payplatform.utils.idcard;

import com.alibaba.druid.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.util.Locale;

public class Demo {
	static
    {

		//System.loadLibrary("IDCARD_THR");
		String osName = System.getProperty("os.name").toLowerCase(Locale.US);
		if (!StringUtils.isEmpty(osName) && osName.startsWith("win")) { //window
			System.loadLibrary("OCRDLL_THR");
		} else { //linux
			System.loadLibrary("IDCARD_THR");
		}
//			System.out.println("java.library.path:"+System.getProperty("java.library.path"));
//
//			System.loadLibrary("IDCARD_THR");
//			System.out.println("天诚OCR：java.library.path:"+System.getProperty("java.library.path"));

    }
	public static int isBootOK = 0;
	/**  获取版权信息*/
	public native static byte [] GetCopyrightInfo();

	/** 获取版本信息*/
	public native static byte [] GetVersion();

	/** 获取版本试用时间*/
	public native static byte [] GetUseTimeString();

	/** 获取引擎内置TIMEKEY*/
	public native static byte [] GetEngineTimeKey();

	/** 引擎初始化 返回值1：正常   -1：未绑定设备  100：时间过期    0：初始化引擎失败*/
	public native static int RECOCRBoot(String TimeKey);

	/**设置引擎参数*/
	public native static int SetParam(int param,int val);
	/**设置引擎带String参数*/
	public native static int SetParamString(int param,String val);
	/**识别入口 路径形式识别*/
	public native static byte[] RECOCROFPATH(int typeid, String path);

	/**识别入口 流形式识别*/
	public native static byte[] RECOCROFMEM(int typeid, byte [] pImagebuf, int len);

	/** 释放引擎内存*/
	public native static int TerminateOCRHandle();

	public String Byte2String(byte[] info)
	{
		String str = null;
		if (info != null) {
			try {
				str = new String(info,"GBK");
			} catch (UnsupportedEncodingException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
		}
		return str;
	}
	public static int Start(String TimeKey)
	{
		if (isBootOK == 0) {
			isBootOK = RECOCRBoot(TimeKey);
		}
		return isBootOK;
	}
}
