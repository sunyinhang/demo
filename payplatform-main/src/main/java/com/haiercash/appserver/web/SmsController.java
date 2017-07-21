package com.haiercash.appserver.web;

import com.haiercash.common.config.EurekaServer;
import com.haiercash.appserver.service.CmisApplService;
import com.haiercash.appserver.service.UauthService;
import com.haiercash.common.apporder.utils.SendMsgType;
import com.haiercash.common.data.MsgRequest;
import com.haiercash.common.data.MsgRequestRepository;
import com.haiercash.commons.util.EncryptUtil;
import com.haiercash.commons.util.HttpUtil;
import com.haiercash.commons.util.SmsUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author liuhongbin
 * @date 2016/5/6
 * @description: 短信功能类，目前只支持验证码发送
 **/
@RestController
public class SmsController extends BaseController {
	private Log logger = LogFactory.getLog(this.getClass());

	private static String MODULE_NO = "82";

	public SmsController() {
		super(MODULE_NO);
	}


	@Autowired
    private CmisApplService cmisApplService;

	@Autowired
	private MsgRequestRepository msgRequestRepository;

	@Autowired
	private LoginController loginController;

	@Autowired
    private UauthService uauthService;

	@RequestMapping(value = "/app/appserver/smsSendVerify", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Object> smsSendVerify(String phone) {
		try {
			// 数据解密
			phone = EncryptUtil.simpleDecrypt(phone);
			String verifyNo = SmsUtil.sendVerifyNo(phone);
			if (verifyNo.isEmpty()) {
				return fail("10", "验证码发送失败");
			} else {
				return success();
			}
		} catch (Exception e) {
			return super.fail("11", "短信验证码发送过于频繁");
		}
	}

	@RequestMapping(value = "/app/appserver/smsSendRealVerify", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Object> smsSendRealVerify(String custName, String certNo, String cardNo) {
		String url = EurekaServer.CRM + "/app/crm/cust/queryMerchCustInfo" + "?custName="
				+ custName + "&certNo=" + certNo;
		String json = HttpUtil.restGet(url, super.getToken());
		if (StringUtils.isEmpty(json)) {
			return fail("01", "查询实名信息失败");
		}
		JSONObject jo = new JSONObject(json);
		if (!jo.has("body")) {
			return fail("02", "实名信息无效");
		}
		JSONObject data = jo.getJSONObject("body");
		if (!data.has("cardNo") || !cardNo.equals(data.getString("cardNo"))) {
			return fail("04", "实名信息错误");
		}
		if (!data.has("mobile")) {
			return fail("03", "读取实名认证手机号失败");
		}
		String phone = data.getString("mobile");

		try {
			String verifyNo = SmsUtil.sendVerifyNo(phone);
			if (verifyNo.isEmpty()) {
				return fail("10", "验证码发送失败");
			} else {
				return success();
			}
		} catch (Exception e) {
			return super.fail("11", "短信验证码发送过于频繁");
		}
	}

	@RequestMapping(value = "/app/appserver/smsVerify", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> smsVerify(@RequestBody Map<String, Object> paramMap) {
		if (paramMap != null) {
			String phone = (String) paramMap.get("phone");
			String verifyNo = (String) paramMap.get("verifyNo");

			int result = SmsUtil.checkVerifyNo(phone, verifyNo);
			if (result == 0) {
				return success();
			} else if (result == 1) {
				return fail("20", "验证码已经失效");
			} else if (result == 2) {
				return fail("21", "验证码错误");
			} else {
				return fail("99", "未知错误");
			}
		}
		return fail("99", "未取到参数");
	}

	/**
     * 短信请求接口.
     * @param msgRequest
     * @return
     */
    @RequestMapping(value = "/app/appserver/msgRequest", method = RequestMethod.POST)
	@Deprecated
    public Map<String, Object> msgRequest(@RequestBody MsgRequest msgRequest) {

		if (StringUtils.isEmpty(msgRequest.getUserId())) {
			return fail("51", "用户id不可为空");
		}
		if (StringUtils.isEmpty(msgRequest.getType())) {
			return fail("52", "短信类型不可为空");
		}
		if (StringUtils.isEmpty(msgRequest.getApplSeq())) {
			return fail("53", "贷款申请流水号不可为空");
		}

		return cmisApplService.msgRequest(msgRequest,"");
	}

	@RequestMapping(value = "/app/appserver/xcd/{userId}/userpwd", method = RequestMethod.GET)
	public Map<String, Object> sendUserPwd(@PathVariable String userId) {

		MsgRequest msgRequest = msgRequestRepository.findByUserIdAndTypeLimit1(userId, SendMsgType.payCode.toString());
		if (msgRequest == null) {
			return fail("54", "用户支付密码发送失败.");
		}

		if (msgRequest.getIsSend().equals("2")) {
            msgRequest.setIsSend("1");
            msgRequestRepository.save(msgRequest);
        }
        return success();

	}

}
