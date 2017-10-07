package com.haiercash.payplatform.utils;

import com.haiercash.payplatform.common.entity.CoreBusinessRequest;
import com.thoughtworks.xstream.XStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.util.Map;

/**
 * 
 * DESCRIPTION:构建核心请求工具
 * <p>
 * <a href="XstreamBuild.java"><i>View Source</i></a>
 * </p>
 * 
 * @author <a href="mailto:xuchao@haiercash.com">xuchao</a>
 *
 * @version Revision: 1.0 Date: 2015年12月23日 下午4:50:39
 *
 */
public class XstreamBuild {
	Log logger= LogFactory.getLog(XstreamBuild.class);
	/**
	 * 
	 * DESCRIPTION:核心请求报文组装工具
	 * 
	 * @author xuchao
	 * @date 2015年12月23日 bulidCorerequestXml 方法
	 * @param body
	 * @param head
	 * @return
	 * @param @param body
	 * @param @param head
	 * @param @return
	 * @return String
	 */
	public String bulidCorerequestXml(Object body, Object head) {
		CoreBusinessRequest request = new CoreBusinessRequest();
		request.setBody(body);
		request.setHead(head);
		XStream stream = new XStream();
		stream.autodetectAnnotations(true);// 打开注解
		stream.aliasSystemAttribute(null, "class");// 去掉class属性
		StringBuffer sbf = new StringBuffer();
		sbf.append("<?xml version=\"1.0\" encoding=\"utf-8\" ?>");
		String xml = stream.toXML(request).replaceAll("__","_");//xstream转换双下划线BUG解决
		sbf.append(xml);
		// 打印请求报文
		StringBuffer loggerBugffer = new StringBuffer();
		loggerBugffer.append("\n*************客户登录请求报文************ \n");
		loggerBugffer.append(sbf + "\n");
		loggerBugffer.append("*************客户登录请求报文************");
		logger.info(loggerBugffer);
		return sbf.toString();
	}
	
	/**
	 * 
	 * DESCRIPTION:转换回复报文
	 * @author xuchao
	 * @date 2015年12月23日
	 * bulidCoreresponseBean 方法
	 * @param response
	 * @return
	 * @param @param response
	 * @param @return
	 * @return Object
	 */
	public Object bulidCoreresponseBean(String response) {
		Map map = null; 
		try {
			Document doc = DocumentHelper.parseText(response);
			Element root = doc.getRootElement();
			map = XmlUtilTool.iterateElement(root);
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		return map;
	}

 
	/**
	 * 
	 * DESCRIPTION:构建CA请求报文
	 * @author xuchao
	 * @date 2016年1月6日
	 * bulidCArequestBody 方法
	 * @return
	 * @param @return
	 * @return Object
	 */
	public String bulidCArequestBody(Object request){
		XStream stream = new XStream();
		stream.autodetectAnnotations(true);// 打开注解
		stream.aliasSystemAttribute(null, "class");// 去掉class属性
		
		StringBuffer sbf = new StringBuffer();
		sbf.append(stream.toXML(request));
		// 打印请求报文
		StringBuffer loggerBugffer = new StringBuffer();
		loggerBugffer.append("\n*************客户登录请求报文************ \n");
		loggerBugffer.append(sbf + "\n");
		loggerBugffer.append("*************客户登录请求报文************");
		logger.info(loggerBugffer);
		return sbf.toString();
	}
}
