package com.haiercash.payplatform.common.utils.ocr;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.UnsupportedEncodingException;

public class Demo {
	static
    {
		Logger xmllog = LogManager.getLogger("xmllog");
		try{

			System.out.println("java.library.path:"+System.getProperty("java.library.path"));
			xmllog.info("天诚OCR：java.library.path:"+System.getProperty("java.library.path"));

			System.loadLibrary("IDCARD_THR");

		}catch (Throwable e){
			xmllog.info("加载OCRDLL_THR出现异常："+e.getMessage());
		}

    } 
	public static int isBootOK = 0;
	/**  ?????????*/
    public native static byte [] GetCopyrightInfo();
    
    /** ????????*/
    public native static byte [] GetVersion();
    
    /** ????????????*/
    public native static byte [] GetUseTimeString();
    
    /** ???????????TIMEKEY*/
    public native static byte [] GetEngineTimeKey();
    
    /** ????????? ?????1??????   -1???????  100?????????    0??????????????*/
	public native static int RECOCRBoot(String TimeKey);
	
	/**????????????*/
	public native static int SetParam(int param,int val);
	
	/**??????? ?????????*/
	public native static byte[] RECOCROFPATH(int typeid, String path);
	
	/**??????? ????????*/
	public native static byte[] RECOCROFMEM(int typeid, byte [] pImagebuf, int len);
    
	/** ??????????*/
	public native static int TerminateOCRHandle();
    
	public String Byte2String(byte[] info)
	{
		String str = null;
		if (info != null) {
			try {
				str = new String(info,"GBK");
			} catch (UnsupportedEncodingException e) {
				// TODO ???????? catch ??
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
