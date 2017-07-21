package com.haiercash.appserver.util.push;

import java.util.HashMap;
import java.util.Map;

public class PushUtil {
	public static String APPPUSH_ERROR = "71";
	// ANDROID:
	public static int ACCESSID_ANDROID_MERCH = 2100196084;
	public static int ACCESSID_ANDROID_USER = 2100196085;
	public static String SECRETKEY_ANDROID_MERCH = "88b83a09997b26f1ce38433a5894b08b";
	public static String SECRETKEY_ANDROID_USER = "cb13b89ca36f73c268c7e0c4192f3ba4";

	// IOS:
	public static Long ACCESSID_IOS_MERCH = 2200203660L;
	public static Long ACCESSID_IOS_USER = 2200202606L;
	public static String SECRETKEY_IOS_MERCH = "623b85693fc49d08131698153dcefe6a";
	public static String SECRETKEY_IOS_USER = "579728766aac76ccefec1ea7563ff6f3";
	Map<String, String> map = new HashMap<String, String>();

	// 获取推送时间区间
	public Map<String, Object> timeInterval(String startTime, String endTime) {
		Map<String, Object> map = new HashMap<String, Object>();
		int startHour, startMin, endHour, endMin;
		boolean flag = false;
		if (null != startTime && !"".equals(startTime)) {
			String[] start_times = startTime.split(":");
			startHour = Integer.parseInt(start_times[0]);
			startMin = Integer.parseInt(start_times[1]);
		} else {
			startHour = 0;
			startMin = 0;
		}
		if (null != endTime && !"".equals(endTime)) {
			String[] end_times = endTime.split(":");
			endHour = Integer.parseInt(end_times[0]);
			endMin = Integer.parseInt(end_times[1]);
		} else {
			endHour = 0;
			endMin = 0;
		}
		TimeInterval acceptTime = new TimeInterval(startHour, startMin, endHour, endMin);
		flag = acceptTime.isValid();
		if (flag) {
			map.put("result", "0");
			map.put("acceptTime", acceptTime);
			return map;
		}

		map.put("result", "-1");
		map.put("err_msg", "推送时间处理失败!");
		return map;
	}

}
