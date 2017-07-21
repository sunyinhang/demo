package com.haiercash.appserver.util.sign;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * @author liuhongbin
 * @date 2016/4/9
 * @description: 文件签名签章配置信息
 **/
public class FileSignConfig {
	/**
	 * 模板文件名
	 */
	private String templateFileName = "";

	/**
	 * 模板合同编号
	 */
	private String templateContractNo = "";
	/**
	 * 文件名，不含文件扩展名
	 */
	private String fileNameNoExt = "";
	/**
	 * 参数Map，key必须和模板文件内的占位符一致 $year, $month,
	 * $day等可自动处理(presetParams方法)的参数，value无效
	 */
	private Map<String, String> params = new HashMap<>();
	/**
	 * 文件上传到ftp的目录，默认为"CA/"
	 */
	private String ftpPath = "CA/";

	/**
	 * 签名参数：客户名称
	 */
	private String userName = "";
	/**
	 * 签名参数：客户身份证号码，用于提取客户签名文件.
	 */
	private String userIdentity = "";
	/**
	 * 签名参数：客户签名页码.
	 */
	private String userPage = "";
	/**
	 * 签名参数：客户签名X坐标（中心点坐标，页面左下角为坐标原点）.
	 */
	private String userX = "";
	/**
	 * 签名参数：客户签名Y坐标（中心点坐标，页面左下角为坐标原点）.
	 */
	private String userY = "";
	/**
	 * 签名参数：单位签章页码.
	 */
	private String coPage = "";
	/**
	 * 签名参数：单位签章X坐标（中心点坐标，页面左下角为坐标原点）.
	 */
	private String coX = "";
	/**
	 * 签名参数：单位签章Y坐标（中心点坐标，页面左下角为坐标原点）.
	 */
	private String coY = "";
	/**
	 * 签名参数：是否进行单位签章，默认为"true"； 如果设置为"false"，则忽略单位签章参数.
	 */
	private String useCoSign = "true";

	/**
	 * 设置参数值，存放于params内
	 *
	 * @param k
	 *            键
	 * @param v
	 *            值
	 */
	public void setParam(String k, String v) {
		params.put(k, v);
	}

	/**
	 * 更新参数值，如果params内不存在k这个键，不做任何操作.
	 *
	 * @param k
	 * @param v
	 */
	public void updateParam(String k, String v) {
		if (params.containsKey(k)) {
			params.put(k, v);
		}
	}

	/**
	 * 参数值预处理，给$year, $month, $day，$name等参数赋值 一般在签名方法内调用.
	 */
	public void presetParams() {
		Calendar calendar = Calendar.getInstance();
		updateParam("$year", calendar.get(calendar.YEAR) + "");
		updateParam("$month", (calendar.get(calendar.MONTH) + 1) + "");
		updateParam("$day", calendar.get(calendar.DATE) + "");
		updateParam("$name", this.userName);
		updateParam("$userIdentity", this.userIdentity);
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUserIdentity() {
		return userIdentity;
	}

	public void setUserIdentity(String userIdentity) {
		this.userIdentity = userIdentity;
	}

	public String getUserPage() {
		return userPage;
	}

	public void setUserPage(String userPage) {
		this.userPage = userPage;
	}

	public String getUserX() {
		return userX;
	}

	public void setUserX(String userX) {
		this.userX = userX;
	}

	public String getUserY() {
		return userY;
	}

	public void setUserY(String userY) {
		this.userY = userY;
	}

	public String getCoPage() {
		return coPage;
	}

	public void setCoPage(String coPage) {
		this.coPage = coPage;
	}

	public String getCoX() {
		return coX;
	}

	public void setCoX(String coX) {
		this.coX = coX;
	}

	public String getCoY() {
		return coY;
	}

	public void setCoY(String coY) {
		this.coY = coY;
	}

	public String getUseCoSign() {
		return useCoSign;
	}

	public void setUseCoSign(String useCoSign) {
		this.useCoSign = useCoSign;
	}

	public Map<String, String> getParams() {
		return params;
	}

	public String getTemplateFileName() {
		return templateFileName;
	}

	public void setTemplateFileName(String templateFileName) {
		this.templateFileName = templateFileName;
	}

	public String getFileNameNoExt() {
		return fileNameNoExt;
	}

	public void setFileNameNoExt(String fileNameNoExt) {
		this.fileNameNoExt = fileNameNoExt;
	}

	public String getFtpPath() {
		return ftpPath;
	}

	public void setFtpPath(String ftpPath) {
		this.ftpPath = ftpPath;
	}

	public String getTemplateContractNo() {
		return templateContractNo;
	}

	public void setTemplateContractNo(String templateContractNo) {
		this.templateContractNo = templateContractNo;
	}

}
