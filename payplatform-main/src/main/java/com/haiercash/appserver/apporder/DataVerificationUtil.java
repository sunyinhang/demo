package com.haiercash.appserver.apporder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 数据校验类
 * 
 * @author 尹君
 *
 */
public class DataVerificationUtil {

	// public static void main(String...arg){
	// System.out.println(isDate("2016-8-29"));
	// }
	/**
	 * 判断当前输入的字符串是否是数字，如果是数字(包括小数)返回true，否则，返回false
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isNumber(String str) {
		Pattern pattern = Pattern.compile("^[0-9]+(.[0-9]*)?$");
		Matcher match = pattern.matcher(str);
		return match.matches();
	}

	/**
	 * 日期格式判断
	 * 
	 * @param date
	 * @return
	 */
	public static boolean isDate(String date) {
		/**
		 * 判断日期格式和范围
		 */
		String rexp = "^((\\d{2}(([02468][048])|([13579][26]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])))))|(\\d{2}(([02468][1235679])|([13579][01345789]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|(1[0-9])|(2[0-8]))))))";

		Pattern pat = Pattern.compile(rexp);

		Matcher mat = pat.matcher(date);

		boolean dateType = mat.matches();

		return dateType;
	}
}
