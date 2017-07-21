package com.haiercash.appserver.web;

import java.util.Map;

import com.haiercash.appserver.util.ConstUtil;
import com.haiercash.common.util.ThreadLocalFactory;
import com.haiercash.commons.controller.AbstractController;
import com.haiercash.commons.util.RestUtil;
import org.springframework.util.StringUtils;

public class BaseController extends AbstractController {

	public BaseController(String moduleNo) {
		super(moduleNo);
	}

	protected Map<String, Object> fail(String retFlag, String retMsg) {
		return RestUtil.fail(ConstUtil.APP_CODE + this.getModuleNo() + retFlag, retMsg);
	}

	protected Map<String, Object> fail(String retFlag, String retMsg, Object result) {
		Map<String, Object> resultMap = RestUtil.fail(ConstUtil.APP_CODE + this.getModuleNo() + retFlag, retMsg);
		resultMap.put("body", result);
		return resultMap;
	}

	@Override
	protected String getChannel() {
		ThreadLocal threadLocal = ThreadLocalFactory.getThreadLocal();
		Map<String, Object> threadMap = (Map<String, Object>) threadLocal.get();
		Object channel = threadMap.get("channel");
		return StringUtils.isEmpty(channel) ? "" : (String)channel;
	}

	@Override
	protected String getChannelNO() {
		ThreadLocal threadLocal = ThreadLocalFactory.getThreadLocal();
		Map<String, Object> threadMap = (Map<String, Object>) threadLocal.get();
		Object channelNo = threadMap.get("channelNo");
		return StringUtils.isEmpty(channelNo) ? "" : (String)channelNo;
	}
}
