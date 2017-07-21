package com.haiercash.appserver.web;

import com.haiercash.appserver.service.AppPushService;
import com.haiercash.appserver.util.push.PushUtil;
import com.haiercash.appserver.util.push.TimeInterval;
import com.haiercash.commons.util.CommonProperties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * Title: AppPushController
 * </p>
 * <p>
 * Description: app推送接口
 * </p>
 */
@SuppressWarnings("unchecked")
@RestController
public class AppPushController extends BaseController {
	private static String MODULE_NO = "70";
	public Log logger = LogFactory.getLog(this.getClass());
	private static String REDIS_KEY_XINGEPUSH;

	public static String getRedisXingeKey() {
		if (REDIS_KEY_XINGEPUSH == null) {
			REDIS_KEY_XINGEPUSH = CommonProperties.get("other.redisKeyXingePush").toString();
		}
		return REDIS_KEY_XINGEPUSH;
	}

	public AppPushController() {
		super(MODULE_NO);
	}

	@Autowired
	private AppPushService appPushService;

	/**
	 * Description: 单个通知token推送接口
	 */
	@RequestMapping(value = "/app/appserver/message/singleTokenPush", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> singleTokenPush(@RequestBody Map<String, Object> params) {
		try {
			params.put("type", "singleToken");
			Map<String, Object> valiMap = appPushService.validateParams(params);
			if (!"00".equals((String) valiMap.get("code"))) {
				return fail("01", (String) valiMap.get("msg"));
			}
			String userId = (String) params.get("userId");
			String phoneType = (String) params.get("phoneType");
			String token = (String) params.get("token");
			String startTime = (String) params.get("startTime");
			String endTime = (String) params.get("endTime");
			String title = (String) params.get("title");
			String message = (String) params.get("message");
			Map<String, Object> custom;
			if (params.containsKey("custom")) {
				custom = (Map<String, Object>) params.get("custom");
			} else {
				custom = new HashMap<>();
			}
			PushUtil pushUtil = new PushUtil();
			Map<String, Object> map;
			map = pushUtil.timeInterval(startTime, endTime);
			TimeInterval t = (TimeInterval) map.get("acceptTime");
			String result = appPushService.singleTokenPush( userId, phoneType,map, title, message, token,custom);
			if("00000".equals(result)){
				return success();
			}else{
				return fail("01",result);
			}
		}catch (Exception e){
			e.printStackTrace();
			return fail("99", "推送异常");
		}
	}

	/**
	 * Description: 单个token消息推送接口
	 */
	@RequestMapping(value = "/app/appserver/message/singleTokenMsgPush", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> singleTokenMsgPush(@RequestBody Map<String, Object> params) {
		try{
			Map<String, Object> valiMap = appPushService.validateParams(params);
			if (!"00".equals((String) valiMap.get("code"))) {
				return fail("04", (String) valiMap.get("msg"));
			}
			String userId = (String) params.get("userId");
			String phoneType = (String) params.get("phoneType");
			String token = (String) params.get("token");
			String startTime = (String) params.get("startTime");
			String endTime = (String) params.get("endTime");
			String title = (String) params.get("title");
			String message = (String) params.get("message");
			Map<String, Object> custom;
			if (params.containsKey("custom")) {
				custom = (Map<String, Object>) params.get("custom");
			} else {
				custom = new HashMap<>();
			}
			PushUtil pushUtil = new PushUtil();
			Map<String, Object> map;
			map = pushUtil.timeInterval(startTime, endTime);
			TimeInterval t = (TimeInterval) map.get("acceptTime");
			String result = appPushService.singleTokenMsgPush( userId, phoneType,map, title, message, token,custom);
			if("00000".equals(result)){
				return success();
			}else{
				return fail("01",result);
			}
		}catch (Exception e){
			e.printStackTrace();
			return fail("99", "推送异常");
		}
	}

	/**
	 * Description: 批量token通知推送接口
	 */
	@RequestMapping(value = "/app/appserver/message/multiNoticeTokensPush", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> multiNoticeTokensPush(@RequestBody Map<String, Object> params) {
		try{
			params.put("type", "multiToken");
			Map<String, Object> valiMap = appPushService.validateParams(params);
			if (!"00".equals((String) valiMap.get("code"))) {
				return fail("07", (String) valiMap.get("msg"));
			}
			String userId = (String) params.get("userId");
			String phoneType = (String) params.get("phoneType");
			String tokens = (String) params.get("tokens");
			String startTime = (String) params.get("startTime");
			String endTime = (String) params.get("endTime");
			String title = (String) params.get("title");
			String message = (String) params.get("message");
			Map<String, Object> custom;
			if (params.containsKey("custom")) {
				custom = (Map<String, Object>) params.get("custom");
			} else {
				custom = new HashMap<>();
			}
			PushUtil pushUtil = new PushUtil();
			Map<String, Object> map;
			map = pushUtil.timeInterval(startTime, endTime);
			TimeInterval t = (TimeInterval) map.get("acceptTime");
			String result = appPushService.multiNoticeTokensPush( userId, phoneType,map, title, message, tokens,custom);
			if("00000".equals(result)){
				return success();
			}else{
				return fail("01",result);
			}
		}catch (Exception e){
			e.printStackTrace();
			return fail("99", "推送异常");
		}
	}

	/**
	 * Description: 批量tokens消息推送接口
	 */
	@RequestMapping(value = "/app/appserver/message/multiTokensMsgPush", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> multiTokensMsgPush(@RequestBody Map<String, Object> params) {
		try{
			params.put("type", "multiToken");
			Map<String, Object> valiMap = appPushService.validateParams(params);
			if (!"00".equals((String) valiMap.get("code"))) {
				return fail("10", (String) valiMap.get("msg"));
			}
			String userId = (String) params.get("userId");
			String phoneType = (String) params.get("phoneType");
			String tokens = (String) params.get("tokens");
			String startTime = (String) params.get("startTime");
			String endTime = (String) params.get("endTime");
			String title = (String) params.get("title");
			String message = (String) params.get("message");
			Map<String, Object> custom;
			if (params.containsKey("custom")) {
				custom = (Map<String, Object>) params.get("custom");
			} else {
				custom = new HashMap<>();
			}
			PushUtil pushUtil = new PushUtil();
			Map<String, Object> map;
			map = pushUtil.timeInterval(startTime, endTime);
			TimeInterval t = (TimeInterval) map.get("acceptTime");
			String result = appPushService.multiTokensMsgPush( userId, phoneType,map, title, message, tokens,custom);
			if("00000".equals(result)){
				return success();
			}else{
				return fail("01",result);
			}
		}catch (Exception e){
			e.printStackTrace();
			return fail("99", "推送异常");
		}
	}

	/**
	 * Description: 单个账号通知推送接口
	 */
	@RequestMapping(value = "/app/appserver/message/singleNoticePush", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> singleNoticePush(@RequestBody Map<String, Object> params) {
		try{
			params.put("type", "singleAccount");
			Map<String, Object> valiMap = appPushService.validateParams(params);
			if (!"00".equals((String) valiMap.get("code"))) {
				return fail("13", (String) valiMap.get("msg"));
			}
			String userId = (String) params.get("userId");
			String phoneType = (String) params.get("phoneType");
			String account = (String) params.get("account");
			String startTime = (String) params.get("startTime");
			String endTime = (String) params.get("endTime");
			String title = (String) params.get("title");
			String message = (String) params.get("message");
			Map<String, Object> custom;
			if (params.containsKey("custom")) {
				custom = (Map<String, Object>) params.get("custom");
			} else {
				custom = new HashMap<>();
			}
			PushUtil pushUtil = new PushUtil();
			Map<String, Object> map;
			map = pushUtil.timeInterval(startTime, endTime);
			TimeInterval t = (TimeInterval) map.get("acceptTime");
			String result = appPushService.singleNoticePush( userId, phoneType,map, title, message, account,custom);
			if("00000".equals(result)){
				return success();
			}else{
				return fail("01",result);
			}
		}catch (Exception e){
			e.printStackTrace();
			return fail("99", "推送异常");
		}
	}

	/**
	 * Description: 单个账号消息推送接口
	 */
	@RequestMapping(value = "/app/appserver/message/singleMsgPush", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> singleMsgPush(@RequestBody Map<String, Object> params) {
		try{
			params.put("type", "singleAccount");
			Map<String, Object> valiMap = appPushService.validateParams(params);
			if (!"00".equals((String) valiMap.get("code"))) {
				return fail("16", (String) valiMap.get("msg"));
			}
			String userId = (String) params.get("userId");
			String phoneType = (String) params.get("phoneType");
			String account = (String) params.get("account");
			String startTime = (String) params.get("startTime");
			String endTime = (String) params.get("endTime");
			String title = (String) params.get("title");
			String message = (String) params.get("message");
			Map<String, Object> custom;
			if (params.containsKey("custom")) {
				custom = (Map<String, Object>) params.get("custom");
			} else {
				custom = new HashMap<>();
			}
			PushUtil pushUtil = new PushUtil();
			Map<String, Object> map;
			map = pushUtil.timeInterval(startTime, endTime);
			TimeInterval t = (TimeInterval) map.get("acceptTime");
			String result = appPushService.singleMsgPush( userId, phoneType,map, title, message, account,custom);
			if("00000".equals(result)){
				return success();
			}else{
				return fail("01",result);
			}
		}catch (Exception e){
			e.printStackTrace();
			return fail("99", "推送异常");
		}
	}

	/**
	 * Description: 批量账号通知推送接口
	 */
	@RequestMapping(value = "/app/appserver/message/multiNoticePush", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> multiNoticePush(@RequestBody Map<String, Object> params) {
		try{
			params.put("type", "multiAccount");
			Map<String, Object> valiMap = appPushService.validateParams(params);
			if (!"00".equals((String) valiMap.get("code"))) {
				return fail("19", (String) valiMap.get("msg"));
			}
			String userId = (String) params.get("userId");
			String phoneType = (String) params.get("phoneType");
			String accounts = (String) params.get("accounts");
			String startTime = (String) params.get("startTime");
			String endTime = (String) params.get("endTime");
			String title = (String) params.get("title");
			String message = (String) params.get("message");
			Map<String, Object> custom;
			if (params.containsKey("custom")) {
				custom = (Map<String, Object>) params.get("custom");
			} else {
				custom = new HashMap<>();
			}
			PushUtil pushUtil = new PushUtil();
			Map<String, Object> map;
			map = pushUtil.timeInterval(startTime, endTime);
			TimeInterval t = (TimeInterval) map.get("acceptTime");
			String result = appPushService.multiNoticePush( userId, phoneType,map, title, message, accounts,custom);
			if("00000".equals(result)){
				return success();
			}else{
				return fail("01",result);
			}
		}catch (Exception e){
			e.printStackTrace();
			return fail("99", "推送异常");
		}
	}

	/**
	 * Description: 批量账号消息推送接口
	 */
	@RequestMapping(value = "/app/appserver/message/multiMsgPush", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> multiMsgPush(@RequestBody Map<String, Object> params) {
		try{
			params.put("type", "multiAccount");
			Map<String, Object> valiMap = appPushService.validateParams(params);
			if (!"00".equals((String) valiMap.get("code"))) {
				return fail("22", (String) valiMap.get("msg"));
			}
			String userId = (String) params.get("userId");
			String phoneType = (String) params.get("phoneType");
			String accounts = (String) params.get("accounts");
			String startTime = (String) params.get("startTime");
			String endTime = (String) params.get("endTime");
			String title = (String) params.get("title");
			String message = (String) params.get("message");
			Map<String, Object> custom;
			if (params.containsKey("custom")) {
				custom = (Map<String, Object>) params.get("custom");
			} else {
				custom = new HashMap<>();
			}
			PushUtil pushUtil = new PushUtil();
			Map<String, Object> map;
			map = pushUtil.timeInterval(startTime, endTime);
			TimeInterval t = (TimeInterval) map.get("acceptTime");
			String result = appPushService.multiMsgPush( userId, phoneType,map, title, message, accounts,custom);
			if("00000".equals(result)){
				return success();
			}else{
				return fail("01",result);
			}
		}catch (Exception e){
			e.printStackTrace();
			return fail("99", "推送异常");
		}
	}
}
