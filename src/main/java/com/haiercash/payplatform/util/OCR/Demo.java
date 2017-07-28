package com.haiercash.payplatform.util.OCR;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.UnsupportedEncodingException;

public class Demo {
	static
    {
		//public Log logger = LogFactory.getLog(getClass());
		//Logger xmllog = LogManager.getLogger("xmllog");
		try{

		    System.out.println("java.library.path:"+System.getProperty("java.library.path"));
			//xmllog.info("���OCR��java.library.path:"+System.getProperty("java.library.path"));

			System.loadLibrary("IDCARD_THR");

		}catch (Throwable e){
			//xmllog.info("����OCRDLL_THR�����쳣��"+e.getMessage());
		}

    } 
	public static int isBootOK = 0;
	/**  ��ȡ��Ȩ��Ϣ*/
    public native static byte [] GetCopyrightInfo();
    
    /** ��ȡ�汾��Ϣ*/
    public native static byte [] GetVersion();
    
    /** ��ȡ�汾����ʱ��*/
    public native static byte [] GetUseTimeString();
    
    /** ��ȡ��������TIMEKEY*/
    public native static byte [] GetEngineTimeKey();
    
    /** �����ʼ��? ����ֵ1������   -1��δ���豸  100��ʱ�����?    0����ʼ������ʧ��*/
	public native static int RECOCRBoot(String TimeKey);
	
	/**�����������?*/
	public native static int SetParam(int param,int val);
	
	/**ʶ�����? ·����ʽʶ��*/
	public native static byte[] RECOCROFPATH(int typeid, String path);
	
	/**ʶ�����? ����ʽʶ��*/
	public native static byte[] RECOCROFMEM(int typeid, byte [] pImagebuf, int len);
    
	/** �ͷ������ڴ�*/
	public native static int TerminateOCRHandle();
    
	public String Byte2String(byte[] info)
	{
		String str = null;
		if (info != null) {
			try {
				str = new String(info,"GBK");
			} catch (UnsupportedEncodingException e) {
				// TODO �Զ����ɵ� catch ��
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
