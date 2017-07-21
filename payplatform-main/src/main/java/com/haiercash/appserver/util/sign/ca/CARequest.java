/*
 * 功  能：简单说明该类的功能
 * 
 * 文件名：CArequest.java
 * 
 * 描  述：
 * 
 * [变更履历]
 * Version   变更日         部署              作者           变更内容
 * -----------------------------------------------------------------------------
 * V1.00     2016年1月6日   Haiercash    xuchao      CREATE
 * -----------------------------------------------------------------------------
 *
 *
 * Copyright (c) 2016 Haiercash All Rights Reserved.
 *┌─────────────────────────────────────────────────—────┐
 *│ 版权声明                               Haiercash       │
 *└──────────────────────────────—————————————————————───┘
 */

package com.haiercash.appserver.util.sign.ca;

/**
 * DESCRIPTION:
 * <p>
 * <p>
 * <a href="CARequest.java"><i>View Source</i></a>
 * </p>
 *
 * @author <a href="mailto:xuchao@haiercash.com">xuchao</a>
 * @version Revision: 1.0 Date: 2016年1月6日 下午5:59:27
 */

public class CARequest {
	/**
	 * DESCRIPTION:CA签章服务
	 *
	 * @param apiId
	 *            固定写死haier
	 * @param path
	 *            上传文件成功地址
	 * @param filename
	 *            文件名
	 * @param docNum
	 *            uuid流水号
	 * @param applcde
	 *            贷款流水号
	 * @param usercode
	 *            身份证
	 * @param page1
	 *            客户签名的页码
	 * @param PositionX1
	 *            x坐标
	 * @param PositionY2
	 *            y坐标
	 * @param page2
	 *            公司章页码
	 * @param PositionX3
	 *            公司章x轴
	 * @param PositionY4
	 *            公司章y轴
	 * @param signCoorflag
	 *            如果不需要公司章则传false，现阶段只有征信授权书不需要公司章
	 * @return String
	 * @throws Exception
	 * @author xuchao
	 * @date 2016年1月8日 buildCASignRequest 方法
	 */
	public String buildCASignRequest(String apiId, String path, String filename, String docNum, String applcde,
			String usercode, String page1, String PositionX1, String PositionY2, String page2, String PositionX3,
			String PositionY4, String signCoorflag) throws Exception {
		StringBuffer sbf = new StringBuffer();
		sbf.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
		sbf.append("<apiId>" + apiId + "</apiId>");
		sbf.append("<contracts>");
		sbf.append("<doc>" + path + "</doc>");
		sbf.append("<docName>" + filename + "</docName>");
		sbf.append("<docNum>" + docNum + "</docNum>");
		sbf.append("<docType>pdf</docType>");
		sbf.append("<signatures>");
		sbf.append("<page>" + page1 + "</page>");
		sbf.append("<positionX>" + PositionX1 + "</positionX>");
		sbf.append("<positionY>" + PositionY2 + "</positionY>");
		sbf.append("<userCode>" + usercode + "</userCode>");
		sbf.append("<userType>0</userType>");
		sbf.append("</signatures>");
		if ("true".equals(signCoorflag)) {
			sbf.append("<signatures>");
			sbf.append("<page>" + page2 + "</page>");
			sbf.append("<positionX>" + PositionX3 + "</positionX>");
			sbf.append("<positionY>" + PositionY4 + "</positionY>");
			sbf.append("<userCode>32597203-5</userCode>");
			sbf.append("<userType>1</userType>");
			sbf.append("</signatures>");
		} else {
			sbf.append("<signatures>null</signatures>");
		}
		sbf.append("<title>AppServer</title>");
		sbf.append("</contracts>");
		sbf.append("<contracts>null</contracts>");
		sbf.append("<timestamp>" + System.currentTimeMillis() + "</timestamp>");
		System.out.println(sbf.toString());
		return sbf.toString();
	}
}
