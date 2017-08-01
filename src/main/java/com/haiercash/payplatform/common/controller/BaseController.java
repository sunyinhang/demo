package com.haiercash.payplatform.common.controller;

import com.haiercash.commons.controller.AbstractController;
import com.haiercash.payplatform.common.utils.ConstUtil;
import com.haiercash.payplatform.common.utils.RestUtil;

import java.util.Map;
//import com.haiercash.commons.util.RestUtil;


public class BaseController extends AbstractController {

	private String moduleNo;
	public BaseController(String moduleNo) {
		this.moduleNo = moduleNo;
	}
//	//public BaseController(String moduleNo) {
//		super(moduleNo);
//	}

	protected Map<String, Object> fail(String retFlag, String retMsg) {
		return RestUtil.fail(ConstUtil.APP_CODE + this.moduleNo + retFlag, retMsg);
	}

	protected Map<String, Object> fail(String retFlag, String retMsg, Object result) {
		Map<String, Object> resultMap = RestUtil.fail(ConstUtil.APP_CODE + this.moduleNo + retFlag, retMsg);
		resultMap.put("body", result);
		return resultMap;
	}
}
