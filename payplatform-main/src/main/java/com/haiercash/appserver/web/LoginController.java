package com.haiercash.appserver.web;

import com.haiercash.common.config.EurekaServer;
import com.haiercash.appserver.service.UauthService;
import com.haiercash.appserver.util.ConstUtil;
import com.haiercash.common.data.UAuthUserToken;
import com.haiercash.common.data.UAuthUserTokenRepository;
import com.haiercash.commons.util.EncryptUtil;
import com.haiercash.commons.util.HttpUtil;
import com.haiercash.commons.util.RestUtil;
import com.haiercash.commons.util.ResultHead;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 客户端注册接口控制器 Created by Liuhongbin on 2016/4/7.
 */
@RestController
@EnableRedisHttpSession
public class LoginController extends BaseController {
    @Autowired
    UAuthUserTokenRepository uAuthUserTokenRepository;
    @Autowired
    UauthService uauthService;
    @Autowired
    private SmsController smsController;

    public static String MODULE_NO = "01";

    public LoginController() {
        super(MODULE_NO);
    }

    @RequestMapping(value = "/app/appserver/customerLogin", method = RequestMethod.PUT)
    @ResponseBody
    public Map<String, Object> customerLogin(@RequestBody Map<String, Object> userMap) {
        if (StringUtils.isEmpty(userMap.get("type"))) {
            //type为空：老版本
            String userId = (String) userMap.get("userAlias");
            String userPass = (String) userMap.get("userPass");
            String deviceId = (String) userMap.get("deviceId");
            String deviceType = (String) userMap.get("deviceType");
            String captchaAnswer = StringUtils.isEmpty(userMap.get("captchaAnswer")) ? "" : (String) userMap.get("captchaAnswer");
            String captchaToken = StringUtils.isEmpty(userMap.get("captchaToken")) ? "" : (String) userMap.get("captchaToken");
            if (!"IOS".equals(deviceType) && !"AND".equals(deviceType)) {
                return fail("10", "设备类型错误");
            }
            if(!StringUtils.isEmpty(captchaAnswer)){
               if(StringUtils.isEmpty(captchaToken)){
                   return fail("10", "验证码token为空");
               }
            }
            Map<String, Object> resultMap = new HashedMap();
            logger.debug("customerLogin - 统一认证登录接口：userId=" + userId + "&password=" + userPass);
            if (!StringUtils.isEmpty(super.getChannel()) && !"16".equals(super.getChannel())) {
                String splitDeviceId = "";
                if (!StringUtils.isEmpty(deviceId)) {
                    String decDeviceId = EncryptUtil.simpleDecrypt(deviceId);
                    splitDeviceId = decDeviceId.substring(decDeviceId.indexOf("-") + 1, decDeviceId.lastIndexOf("-"));
                    splitDeviceId = EncryptUtil.simpleEncrypt(splitDeviceId);
                }
                Map<String, Object> requestMap = new HashedMap();
                requestMap.put("userId", userId);
                requestMap.put("password", userPass);
                requestMap.put("deviceId", splitDeviceId);
                requestMap.put("captchaAnswer", captchaAnswer);
                requestMap.put("captchaToken", captchaToken);
                resultMap = uauthService.validateUsersCount(requestMap);
                     /*   HttpUtil
                        .restGetMap(EurekaServer.UAUTH + "/app/uauth/validateUsersCount?userId=" + userId + "&password="
                                + userPass);*/
            } else {
                resultMap = HttpUtil
                        .restGetMap(EurekaServer.UAUTH + "/app/uauth/validateUsers?userId=" + userId + "&password="
                                + userPass);
            }
            logger.debug("customerLogin - 统一认证登录接口返回：" + resultMap);
            if (resultMap == null) {
                return fail(RestUtil.ERROR_INTERNAL_CODE, RestUtil.ERROR_INTERNAL_MSG);
            } else {
                if (!super.isSuccess(resultMap)) {
                    return resultMap;
                }
                userId = (String) ((HashMap<String, Object>) resultMap.get("body")).get("userId");
                // 数据解密
                // userPass = EncryptUtil.simpleDecrypt(userPass);
                deviceId = EncryptUtil.simpleDecrypt(deviceId);

                // 生成clientSecret，并将deviceId和clientSecret保存到关系表
                PasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();
                String clientSecret = PASSWORD_ENCODER.encode(deviceId + userId);

                storeToken((String) ((Map<String, Object>) resultMap.get("body")).get("userId"), deviceId, clientSecret,
                        deviceType);
                ((Map<String, Object>) resultMap.get("body")).put("clientSecret", clientSecret);
                uauthService.createTokenAndgetRealInfo(resultMap, deviceId);
                return resultMap;
            }

        }
        // 新版本
        if (StringUtils.isEmpty(userMap.get("userId")) && StringUtils.isEmpty(userMap.get("userAlias"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "用户账号不能为空");
        }
        if (StringUtils.isEmpty(userMap.get("password")) && StringUtils.isEmpty(userMap.get("userPass"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "密码不能为空");
        }
        String captchaAnswer = StringUtils.isEmpty(userMap.get("captchaAnswer")) ? "" : (String) userMap.get("captchaAnswer");
        String captchaToken = StringUtils.isEmpty(userMap.get("captchaToken")) ? "" : (String) userMap.get("captchaToken");
        if(!StringUtils.isEmpty(captchaAnswer)){
            if(StringUtils.isEmpty(captchaToken)){
                return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "验证码token为空");
            }
        }
        String userId = StringUtils.isEmpty(userMap.get("userId")) ?
                String.valueOf(userMap.get("userAlias")) :
                String.valueOf(userMap.get("userId"));
        String userPass = StringUtils.isEmpty(userMap.get("password")) ?
                String.valueOf(userMap.get("userPass")) :
                String.valueOf(userMap.get("password"));
        String type = (String) userMap.get("type");
        String deviceId = StringUtils.isEmpty(userMap.get("deviceId")) ? "" : (String) userMap.get("deviceId");
        String deviceType = StringUtils.isEmpty(userMap.get("deviceType")) ? "" : (String) userMap.get("deviceType");
        String splitDeviceId = "";

        if (!StringUtils.isEmpty(deviceId)) {
            String decDeviceId = EncryptUtil.simpleDecrypt(deviceId);
            splitDeviceId = decDeviceId.substring(decDeviceId.indexOf("-") + 1, decDeviceId.lastIndexOf("-"));
            logger.debug("截取后deviceId:" + splitDeviceId);
            splitDeviceId = EncryptUtil.simpleEncrypt(splitDeviceId);
            logger.debug("加密后deviceId:" + splitDeviceId);
        }

        Map<String, Object> requestMap = new HashMap<>();

        requestMap.put("userId", userId);
        requestMap.put("password", userPass);
        //requestMap.put("deviceId", deviceId);
        //requestMap.put("deviceType", deviceType);
        if ("login".equals(type)) {
            if (StringUtils.isEmpty(deviceType)) {
                return fail("10", "设备类型错误");
            }
        }
        Map<String, Object> resultMap = new HashedMap();
        logger.debug("customerLogin - 统一认证登录接口：userId=" + userId + "&password=" + userPass);
        String channel = super.getChannel();
        String channelNO = super.getChannelNO();
        if (StringUtils.isEmpty(channel)) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "channel不能为空");
        }
        if (channel.equals("16")) {
            // 星巢贷订单走原接口 统一认证3接口
            if ("login".equals(type)) {
                requestMap.put("deviceId", splitDeviceId);
            }
            resultMap = uauthService.validateUsers(requestMap);
        } else if (channel.equals("14")) {
            // 个人版
            // 首次登录，调用统一认证25接口
            if (type.equals("login")) {
                // 个人版登录对设备号做判断
                requestMap.put("deviceId", splitDeviceId);
                //验证码
                requestMap.put("captchaAnswer", captchaAnswer);
                requestMap.put("captchaToken", captchaToken);
                resultMap = uauthService.validateUsersCount(requestMap);
            } else if (type.equals("set")) {
                // 登录后设置，调用统一认证30接口
                requestMap.put("captchaAnswer", captchaAnswer);
                requestMap.put("captchaToken", captchaToken);
                resultMap = uauthService.validateUsersHaier(requestMap);
            } else {
                return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "不支持的类型");
            }
        } else if ("34".equals(channelNO)) {
            // 集团大数据，调用统一认证29接口
            if (StringUtils.isEmpty(userMap.get("externUid"))) {
                return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "集团用户Id不能为空");
            }
            requestMap.put("externUid", userMap.get("externUid"));
            resultMap = uauthService.validateAndBindHaierUser(requestMap);
            if (StringUtils.isEmpty(userMap.get("deviceId"))) {
                deviceId = userId;
            }
        } else if ("35".equals(channelNO)) {
            // 美分期
            resultMap = uauthService.validateUsers(requestMap);
            if (StringUtils.isEmpty(userMap.get("deviceId"))) {
                deviceId = userId;
            }
        } else {
            // 其他渠道登录
            resultMap = uauthService.validateUsers(requestMap);
        }

        if (resultMap == null || resultMap.isEmpty()) {
            return fail(ConstUtil.ERROR_AUTH_FAIL_CODE, "统一认证系统通信失败");
        }

        // 需要设备号做验证的情况，要返回mobile
        /*
        if ("U0169".equals(((HashMap<String, Object>) resultMap.get("head")).get("retFlag"))) {
            String getUserInfoUrl =
                    EurekaServer.UAUTH + "/app/uauth/findUserByUserid?userId=" + EncryptUtil.simpleDecrypt(userId);
            logger.info("UAUTH 32 ==> " + getUserInfoUrl);
            String getUserInfoStr = HttpUtil.restGet(getUserInfoUrl);
            logger.info("UAUTH 32 <== " + getUserInfoStr);
            if (StringUtils.isEmpty(getUserInfoStr)) {
                return fail("34", "统一认证32接口通信失败");
            }
            HashMap<String, Object> userInfoResult = HttpUtil.json2DeepMap(getUserInfoStr);
            if (!HttpUtil.isSuccess(getUserInfoStr)) {
                return userInfoResult;
            }
            HashMap<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("mobile", ((Map<String, Object>) userInfoResult.get("body")).get("mobile"));
            resultMap.put("body", bodyMap);
        }
        */

        if (!HttpUtil.isSuccess(resultMap) || !type.equals("login")) {
            // 登录失败或非登录，无需存储登录信息，直接返回
            return resultMap;
        } else {
            // 存储登录信息
            userId = (String) ((HashMap<String, Object>) resultMap.get("body")).get("userId");
            // 数据解密
            deviceId = EncryptUtil.simpleDecrypt(deviceId);

            // 生成clientSecret，并将deviceId和clientSecret保存到关系表
            PasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();
            String clientSecret = PASSWORD_ENCODER.encode(deviceId + userId);

            storeToken((String) ((Map<String, Object>) resultMap.get("body")).get("userId"), deviceId, clientSecret,
                    deviceType);
            ((Map<String, Object>) resultMap.get("body")).put("clientSecret", clientSecret);

            uauthService.createTokenAndgetRealInfo(resultMap, deviceId);
            return resultMap;
        }
    }

    @RequestMapping(value = "/app/appserver/salerLogin", method = RequestMethod.PUT)
    @ResponseBody
    public Map<String, Object> salerLogin(@RequestBody Map<String, Object> userMap) {
        String userId = (String) userMap.get("userAlias");
        String userPass = (String) userMap.get("userPass");
        String deviceId = (String) userMap.get("deviceId");
        String deviceType = (String) userMap.get("deviceType");
        if (!"IOS".equals(deviceType) && !"AND".equals(deviceType)) {
            return fail("20", "设备类型错误");
        }

        userMap.put("userAlias", userId);
        userMap.put("userPass", userPass);
        userMap.put("deviceId", deviceId);
        logger.debug("salerLogin - PORTAL登录接口：" + userMap);
        Map<String, Object> resultMap = HttpUtil.restPutMap(EurekaServer.HCPORTAL + "/app/portal/mUser/login", userMap,
                HttpStatus.OK.value());
        logger.debug("salerLogin - PORTAL登录接口返回：" + resultMap);
        if (resultMap == null) {
            return fail(ConstUtil.ERROR_LOGIN_FAIL_CODE, ConstUtil.ERROR_LOGIN_FAIL_MSG);
        } else {
            if (!super.isSuccess(resultMap)) {
                return resultMap;
            }

            // 数据解密
            userId = EncryptUtil.simpleDecrypt(userId);
            // userPass = EncryptUtil.simpleDecrypt(userPass);
            deviceId = EncryptUtil.simpleDecrypt(deviceId);

            // 生成clientSecret，并将deviceId和clientSecret保存到关系表
            PasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();
            String clientSecret = PASSWORD_ENCODER.encode(deviceId + userId);

            storeToken(userId, deviceId, clientSecret, deviceType);
            ((Map<String, Object>) resultMap.get("body")).put("clientSecret", clientSecret);
            return resultMap;
        }
    }

    /**
     * 校验验证码并绑定设备号
     *
     * @param paramMap
     * @return
     */
    @RequestMapping(value = "/app/appserver/verifyAndBindDeviceId", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> verifyAndBindDeviceId(@RequestBody Map<String, Object> paramMap) {
        if (StringUtils.isEmpty(paramMap.get("userId"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "userId不能为空");
        }
        if (StringUtils.isEmpty(paramMap.get("deviceId"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "设备号不能为空");
        }
        if (StringUtils.isEmpty(paramMap.get("deviceType"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "deviceType不能为空");
        }

        Map<String, Object> varifyResult = smsController.smsVerify(paramMap);
        if (!((ResultHead) varifyResult.get("head")).getRetFlag().equals("00000")) {
            return varifyResult;
        }

        // 验证成功，绑定设备号
        String url = EurekaServer.UAUTH + "/app/uauth/bindDeviceId?userId=" + String.valueOf(paramMap.get("userId"))
                + "&deviceId=" + paramMap.get("deviceId");
        logger.info("向统一认证发起绑定设备号请求：" + url);
        String json = HttpUtil.restGet(url);
        logger.info("统一认证绑定设备号结果：" + json);
        if (StringUtils.isEmpty(json)) {
            return fail("33", "统一认证系统通信失败");
        }
        HashMap<String, Object> resultMap = HttpUtil.json2DeepMap(json);
        if (resultMap.isEmpty()) {
            return fail("33", "统一认证系统通信失败");
        }
        if (HttpUtil.isSuccess(json)) {
            if (resultMap.get("body") == null) {
                Map<String, Object> bodyMap = new HashMap<>();
                resultMap.put("body", bodyMap);
            }
            // 绑定成功，请求用户登录需要的数据
            String userId = EncryptUtil.simpleDecrypt((String) paramMap.get("userId"));
            String getUserInfoUrl = EurekaServer.UAUTH + "/app/uauth/findUserByUserid?userId=" + userId;
            logger.info("UAUTH 32 ==> " + getUserInfoUrl);
            String getUserInfoStr = HttpUtil.restGet(getUserInfoUrl);
            logger.info("UAUTH 32 <== " + getUserInfoStr);
            if (StringUtils.isEmpty(getUserInfoStr)) {
                return fail("34", "统一认证32接口通信失败");
            }
            HashMap<String, Object> userInfoResult = HttpUtil.json2DeepMap(getUserInfoStr);
            if (!HttpUtil.isSuccess(getUserInfoStr)) {
                return userInfoResult;
            }
            // 把所有返回数据都放入返回map
            ((Map<String, Object>) resultMap.get("body")).putAll((Map<String, Object>) userInfoResult.get("body"));

            // 将登录数据存入数据库
            // 数据解密
            // userPass = EncryptUtil.simpleDecrypt(userPass);
            String deviceId = EncryptUtil.simpleDecrypt((String) paramMap.get("deviceId"));
            // 使用统一认证返回的userId
            userId = (String)((Map<String, Object>)userInfoResult.get("body")).get("userId");
            // 生成clientSecret，并将deviceId和clientSecret保存到关系表
            PasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();
            String clientSecret = PASSWORD_ENCODER.encode(deviceId + userId);
            storeToken(userId, deviceId, clientSecret, (String) paramMap.get("deviceType"));
            ((Map<String, Object>) resultMap.get("body")).put("clientSecret", clientSecret);
            uauthService.createTokenAndgetRealInfo(resultMap, deviceId);
        }
        return resultMap;
    }

    public void storeToken(String userId, String deviceId, String clientSecret, String deviceType) {
        UAuthUserToken userToken = new UAuthUserToken();
        userToken.setUserId(userId);
        userToken.setClientId(deviceId);
        userToken.setClientSecret(clientSecret);
        userToken.setRegisterDate(new Date());
        userToken.setDeviceType(deviceType);
        uAuthUserTokenRepository.save(userToken);
    }

}
