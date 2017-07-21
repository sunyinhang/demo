package com.haiercash.appserver.service.impl;

import com.haiercash.common.service.BaseService;
import com.haiercash.appserver.service.SelfTokenService;
import com.haiercash.appserver.service.UauthService;
import com.haiercash.appserver.web.SmsController;
import com.haiercash.common.apporder.utils.FormatUtil;
import com.haiercash.common.config.EurekaServer;
import com.haiercash.commons.util.EncryptUtil;
import com.haiercash.commons.util.HttpUtil;
import com.haiercash.commons.util.RestUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * uauth service impl.
 *
 * @author admin
 * @since v1.0.0
 */
@Service
public class UauthServiceImpl extends BaseService implements UauthService {

    private Log logger = LogFactory.getLog(UauthServiceImpl.class);
    @Autowired
    private SmsController smsController;

    @Autowired
    private SelfTokenService selfTokenService;

    /**
     * 用户支付密码手势密码验证是否设置
     *
     * @param userId
     * @return
     */
    @Override
    public Map<String, Object> validateUserFlag(String userId) {
        String url = EurekaServer.UAUTH + "/app/uauth/validateUserFlag?userId=" + userId;
        logger.info("url:" + url);
        Map<String, Object> result = HttpUtil.restGetMap(url);
        logger.info("==result:" + result);
        return result;
    }

    /**
     * 获取图片
     *
     * @param userId
     * @return
     */
    @Override
    public Map<String, Object> getUserPic(String userId) {
        String url = EurekaServer.UAUTH + "/app/uauth/getUserPic?userId=" + userId;
        logger.info("url:" + url);
        Map<String, Object> result = HttpUtil.restGetMap(url);
        logger.info("==result:" + result);
        return result;
    }

    /**
     * 支付密码验证
     *
     * @param params
     * @return
     */
    @Override
    public Map<String, Object> validatePayPasswd(Map<String, Object> params) {
        StringBuffer url = new StringBuffer(EurekaServer.UAUTH + "/app/uauth/validatePayPasswd?")
                .append("userId=" + params.get("userId") + "&")
                .append("payPasswd=" + params.get("payPasswd"));
        logger.info("url:" + url.toString());
        Map<String, Object> result = HttpUtil.restGetMap(url.toString());
        logger.info("==result:" + result);
        return result;
    }

    /**
     * 手势密码设置
     *
     * @param params
     * @return
     */
    @Override
    public Map<String, Object> gesture(Map<String, Object> params, String token) {
        String url = EurekaServer.UAUTH + "/app/uauth/gesture";
        logger.info("url:" + url + "  params:" + params);
        Map<String, Object> result = HttpUtil.restPutMap(url, token, params);
        logger.info("==result:" + result);
        return result;
    }

    /**
     * 手势密码验证
     *
     * @param params
     * @return
     */
    @Override
    public Map<String, Object> validateGesture(Map<String, Object> params) {
        StringBuffer url = new StringBuffer(EurekaServer.UAUTH + "/app/uauth/validateGesture?")
                .append("userId=" + params.get("userId") + "&")
                .append("gesture=" + params.get("gesture"));
        logger.info("url:" + url.toString());
        Map<String, Object> result = HttpUtil.restGetMap(url.toString());
        logger.info("==result:" + result);
        return result;
    }

    /**
     * 冻结用户.
     *
     * @param params
     * @return
     */
    @Override
    public Map<String, Object> frozenUsers(Map<String, Object> params) {
        String url = EurekaServer.UAUTH + "/app/uauth/frozenUsers";
        logger.info("url:" + url + "  params:" + params);
        Map<String, Object> result = HttpUtil.restPutMap(url, params);
        logger.info("==result:" + result);
        return result;
    }

    /**
     * 支付密码设置.
     *
     * @param params
     * @return
     */
    @Override
    public Map<String, Object> payPasswd(Map<String, Object> params, String token) {
        String url = EurekaServer.UAUTH + "/app/uauth/payPasswd";
        logger.info("url:" + url + "  params:" + params);
        Map<String, Object> result = HttpUtil.restPutMap(url, token, params);
        logger.info("==result:" + result);
        return result;
    }

    /**
     * 是否注册.
     *
     * @param mobile
     * @return
     */
    @Override
    public Map<String, Object> isRegister(String mobile) {
        String url = EurekaServer.UAUTH + "/app/uauth/isRegister?mobile=" + mobile;
        logger.info("url:" + url);
        Map<String, Object> result = HttpUtil.restGetMap(url);
        logger.info("==result:" + result);
        return result;
        /*if (StringUtils.isEmpty(result)) {
            return fail("03", "信息获取失败");
        }
        Map<String, Object>  body = (Map<String, Object>) result.get("body");
        String isRegister = String.valueOf(body.get("isRegister"));
        if ("Y".equals(isRegister)) {
            return fail("01", "此帐号已注册", body);
        } else if ("C".equals(isRegister)) {
            return fail("02", "此账号已被占用为绑定手机号", body);
        }

        return success("此帐号未注册", body);*/
    }


     /**
     * 是否注册.
     *
     * @param mobile
     * @return
     */
    @Override
    public Map<String, Object> isRegisterHaier(String mobile) {
        String url = EurekaServer.UAUTH + "/app/uauth/isRegisterHaier?mobile=" + mobile;
        logger.info("isRegisterHaier url:" + url);
        Map<String, Object> result = HttpUtil.restGetMap(url);
        logger.info("isRegisterHaier result:" + result);
        return result;

    }


    /**
     * 登录验证(Portal使用).
     *
     * @param params
     * @return
     */
    @Override
    public Map<String, Object> validateUsers(Map<String, Object> params) {
        StringBuffer url = new StringBuffer(EurekaServer.UAUTH + "/app/uauth/validateUsers?")
                .append("userId=" + params.get("userId") + "&")
                .append("password=" + params.get("password"));
        logger.info("url:" + url.toString());
        Map<String, Object> result = HttpUtil.restGetMap(url.toString());
        logger.info("==result:" + result);
        return result;
    }

    /**
     * 头像设置.
     *
     * @param params
     * @return
     */
    @Override
    public Map<String, Object> avatarUrl(Map<String, Object> params) {
        String url = EurekaServer.UAUTH + "/app/uauth/avatarUrl";
        logger.info("url:" + url + "  params:" + params);
        Map<String, Object> result = HttpUtil.restPutMap(url, params);
        logger.info("==result:" + result);
        return result;
    }

    /**
     * 实名认证.
     *
     * @param params
     * @return
     */
    @Override
    public Map<String, Object> identify(Map<String, Object> params) {
        StringBuffer url = new StringBuffer(EurekaServer.UAUTH + "/app/uauth/identify?")
                .append("userId=" + params.get("userId") + "&")
                .append("custName=" + params.get("custName") + "&")
                .append("certNo=" + params.get("certNo") + "&")
                .append("cardNo=" + params.get("cardNo") + "&")
                .append("mobile=" + params.get("mobile") + "&")
                .append("bankCode=" + params.get("bankCode"));

        logger.info("url:" + url.toString());
        Map<String, Object> result = HttpUtil.restGetMap(url.toString());
        logger.info("==result:" + result);

        return result;
    }

    /**
     * 修改绑定手机号.
     *
     * @param params
     * @return
     */
    @Override
    public Map<String, Object> updateMobile(Map<String, Object> params) {
        String url = EurekaServer.UAUTH + "/app/uauth/updateMobile";
        logger.info("url:" + url + "  params:" + params);
        Map<String, Object> result = HttpUtil.restPutMap(url, params);
        logger.info("==result:" + result);
        return result;
    }

    /**
     * 客户登录密码设置、修改（验证码）.
     *
     * @param params
     * @return
     */
    @Override
    public Map<String, Object> custUpdatePwd(Map<String, Object> params) {
        String url = EurekaServer.UAUTH + "/app/uauth/custUpdatePwd";
        logger.info("url:" + url + "  params:" + params);
        Map<String, Object> result = HttpUtil.restPutMap(url, params);
        logger.info("==result:" + result);
        return result;
    }

    /**
     * 客户登录密码设置、修改（实名认证+验证码）.
     *
     * @param params
     * @return
     */
    @Override
    public Map<String, Object> custVerifyUpdatePwd(Map<String, Object> params) {
        String url = EurekaServer.UAUTH + "/app/uauth/custVerifyUpdatePwd";
        logger.info("url:" + url + "  params:" + params);
        Map<String, Object> result = HttpUtil.restPutMap(url, params);
        logger.info("==result:" + result);
        return result;
    }

    /**
     * 支付密码修改.
     *
     * @param params
     * @return
     */
    @Override
    public Map<String, Object> updatePayPasswd(Map<String, Object> params, String token) {
        String url = EurekaServer.UAUTH + "/app/uauth/updatePayPasswd";
        logger.info("url:" + url + "  params:" + params);
        Map<String, Object> result = HttpUtil.restPutMap(url, token, params);
        logger.info("==result:" + result);
        return result;
    }

    /**
     * 用户注册.
     *
     * @param params
     * @return
     */
    @Override
    public Map<String, Object> saveUauthUsers(Map<String, Object> params, String channel) {
        // 验证码验证
        Map<String, Object> verifyMap = new HashMap<>();
        verifyMap.put("phone", EncryptUtil.simpleDecrypt((String) params.get("mobile")));
        verifyMap.put("verifyNo", params.get("verifyNo"));
        Map<String, Object> verifyResult = smsController.smsVerify(verifyMap);
        if (HttpUtil.isSuccess(verifyResult)) {
            // 验证码验证成功，调用统一认证用户注册接口
            String url = "";
            if ("16".equals(channel)) {
                url = EurekaServer.UAUTH + "/app/uauth/saveUauthUsers";
            } else {
                url = EurekaServer.UAUTH + "/app/uauth/saveUauthUsersHaier";
            }
            logger.info("url:" + url + "  params:" + params);
            Map<String, Object> result = HttpUtil.restPostMap(url, params);
            logger.info("==result:" + result);
            return result;
        } else {
            return verifyResult;
        }
    }

    /**
     * 客户支付密码设置、修改（实名认证+验证码）.
     *
     * @param params
     * @return
     */
    @Override
    public Map<String, Object> custVerifyUpdatePayPwd(Map<String, Object> params) {
        String url = EurekaServer.UAUTH + "/app/uauth/custVerifyUpdatePayPwd";
        logger.info("url:" + url + "  params:" + params);
        Map<String, Object> result = HttpUtil.restPutMap(url, params);
        logger.info("==result:" + result);
        return result;
    }

    @Override
    public Map<String, Object> validateUsersCount(Map<String, Object> params) {
        String url = EurekaServer.UAUTH + "/app/uauth/validateUsersCount";
        url = FormatUtil.putParam2Url(url, params);
        logger.info("登录验证 url:" + url);
        String json = HttpUtil.restGet(url);
        logger.info("登录返回：" + json);
        return HttpUtil.json2DeepMap(json);
    }

    @Override
    public Map<String, Object> createTokenAndgetRealInfo(Map<String, Object> map, String clientId) {
        // 登录成功后，实时生成token，返回给用户。
        OAuth2AccessToken token = selfTokenService.createAccessToken(clientId);
        ((Map<String, Object>) map.get("body")).put("token", token);
        // 查询用户是否已实名认证
        Map<String, Object> mapBody = (Map<String, Object>) map.get("body");
        String url = EurekaServer.CRM + "/app/crm/cust/queryPerCustInfo" + "?userId=" + mapBody.get("userId");
        logger.info("CRM 实名信息接口请求url==" + url);
        String json = HttpUtil.restGet(url, null);
        logger.info("CRM 实名信息接口返回：" + json);
        if (StringUtils.isEmpty(json)) {
            logger.info("CRM实名认证信息（getCustRealInfo）接口返回异常！请求处理被迫停止！");
            ((Map<String, Object>) map.get("body")).put("isRealInfo", "N");
            ((Map<String, Object>) map.get("body")).put("realInfo", new HashMap<>());
            return map;
        }

        if (!StringUtils.isEmpty(HttpUtil.json2Map(json).get("body"))) {
            mapBody = HttpUtil.json2Map(String.valueOf(HttpUtil.json2Map(json).get("body")));
        }
        if (HttpUtil.isSuccess(json) && !StringUtils.isEmpty(mapBody.get("custNo"))) {
            ((Map<String, Object>) map.get("body")).put("isRealInfo", "Y");
            ((Map<String, Object>) map.get("body")).put("realInfo", mapBody);
        } else {
            ((Map<String, Object>) map.get("body")).put("isRealInfo", "N");
            ((Map<String, Object>) map.get("body")).put("realInfo", new HashMap<>());
        }

        return map;
    }

    @Override
    public Map<String, Object> validateGestureCount(Map<String, Object> params) {
        String url = EurekaServer.UAUTH + "/app/uauth/validateGestureCount";
        url = FormatUtil.putParam2Url(url, params);
        logger.info("手势密码验证 url:" + url);
        return HttpUtil.json2DeepMap(HttpUtil.restGet(url));
    }

    @Override
    public Map<String, Object> queryHaierUserInfo(Map<String, Object> params) {
        String url = EurekaServer.UAUTH + "/app/uauth/queryHaierUserInfo";
        url = FormatUtil.putParam2Url(url, params);
        logger.info("根据集团用户id查询用户信息 url:" + url);
        return HttpUtil.json2DeepMap(HttpUtil.restGet(url));
    }

    @Override
    public Map<String, Object> saveUauthUsersByHaier(Map<String, Object> params) {
        String url = EurekaServer.UAUTH + "/app/uauth/saveUauthUsersByHaier";
        logger.info("集团用户注册统一认证账户 url:" + url + "  params:" + params);
        Map<String, Object> result = HttpUtil.restPostMap(url, params);
        logger.info("==result:" + result);
        return result;
    }

    @Override
    public Map<String, Object> validateAndBindHaierUser(Map<String, Object> params) {

        String url = EurekaServer.UAUTH + "/app/uauth/validateAndBindHaierUser";
        logger.info("验证并绑定集团用户（已绑定的不可用） url:" + url + "  params:" + params);
        Map<String, Object> result = HttpUtil.restPostMap(url, params);
        logger.info("==result:" + result);
        return result;
    }

    @Override
    public Map<String, Object> validateUsersHaier(Map<String, Object> params) {
        String url = EurekaServer.UAUTH + "/app/uauth/validateUsersHaier";
        url = FormatUtil.putParam2Url(url, params);
        logger.info("根据集团用户id查询用户信息 url:" + url);
        return HttpUtil.json2DeepMap(HttpUtil.restGet(url));
    }


    @Override
    public Map<String, Object> getMobile(String userId) {
        String url = EurekaServer.UAUTH + "/app/uauth/getMobile?userId=" + userId;
        logger.info("url:" + url);
        Map<String, Object> result = HttpUtil.restGetMap(url);
        logger.info("==result:" + result);
        return result;
    }

    @Override
    public Map<String, Object> haierCaptcha() {
        String url = EurekaServer.UAUTH + "/app/uauth/haierCaptcha";
        logger.info("==> UAUTH 1.34 url:" + url);
        String returnJson = HttpUtil.restGet(url);
        logger.info("<== UAUTH 1.34 return:" + returnJson);
        if (StringUtils.isEmpty(returnJson)) {
            return fail(RestUtil.ERROR_INTERNAL_CODE, "1.34 统一认证系统通信失败");
        }
        return HttpUtil.json2DeepMap(returnJson);
    }
}
