package com.haiercash.appserver.web;

import com.haiercash.appserver.service.UauthService;
import com.haiercash.appserver.util.ConstUtil;
import com.haiercash.commons.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 整合统一认证接口的控制器
 */
@RestController
public class UauthController extends BaseController {
    private static String MODULE_NO = "17";

    public UauthController() {
        super(MODULE_NO);
    }

    @Autowired
    private UauthService uauthService;

    /**
     * 用户支付密码手势密码验证是否设置
     *
     * @param userId
     * @return
     */
    @RequestMapping(value = "/app/appserver/uauth/validateUserFlag", method = RequestMethod.GET)
    public Map<String, Object> validateUserFlag(@RequestParam String userId) {
        return uauthService.validateUserFlag(userId);
    }

    /**
     * 获取图片
     *
     * @param userId
     * @return
     */
    @RequestMapping(value = "/app/appserver/uauth/getUserPic", method = RequestMethod.GET)
    public Map<String, Object> getUserPic(@RequestParam String userId) {
        return uauthService.getUserPic(userId);
    }

    /**
     * 支付密码验证
     *
     * @param params
     * @return
     */
    @RequestMapping(value = "/app/appserver/uauth/validatePayPasswd", method = RequestMethod.GET)
    public Map<String, Object> validatePayPasswd(@RequestParam Map<String, Object> params) {
        if (StringUtils.isEmpty(params.get("userId")) || StringUtils.isEmpty(params.get("payPasswd"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "用户账号或支付密码不能为空");
        }
        return uauthService.validatePayPasswd(params);
    }

    /**
     * 手势密码设置
     *
     * @param params
     * @return
     */
    @RequestMapping(value = "/app/appserver/uauth/gesture", method = RequestMethod.PUT)
    public Map<String, Object> gesture(@RequestBody Map<String, Object> params) {
        if (StringUtils.isEmpty(params.get("userId")) || StringUtils.isEmpty(params.get("gesture"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "用户账号或手势密码不能为空");
        }
        String token = super.getToken();
        return uauthService.gesture(params, token);
    }

    /**
     * 手势密码验证
     *
     * @param params
     * @return
     */
    @RequestMapping(value = "/app/appserver/uauth/validateGesture", method = RequestMethod.GET)
    public Map<String, Object> validateGesture(@RequestParam Map<String, Object> params) {
        if (StringUtils.isEmpty(params.get("userId")) || StringUtils.isEmpty(params.get("gesture"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "用户账号或手势密码不能为空");
        }
        String channel = super.getChannel();
        if (StringUtils.isEmpty(channel)) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "系统标识不能为空");
        }
        if ("16".equals(channel)) {
            return uauthService.validateGesture(params);
        }
        if ("14".equals(channel)) {
            return uauthService.validateGestureCount(params);
        }
        return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "不支持的系统标识");
    }

    /**
     * 冻结用户
     *
     * @param params
     * @return
     */
    @RequestMapping(value = "/app/appserver/uauth/frozenUsers", method = RequestMethod.PUT)
    public Map<String, Object> frozenUsers(@RequestBody Map<String, Object> params) {
        if (StringUtils.isEmpty(params.get("userId"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "用户账号不能为空");
        }
        return uauthService.frozenUsers(params);
    }

    /**
     * 设置支付密码
     *
     * @param params
     * @return
     */
    @RequestMapping(value = "/app/appserver/uauth/payPasswd", method = RequestMethod.PUT)
    public Map<String, Object> payPasswd(@RequestBody Map<String, Object> params) {
        if (StringUtils.isEmpty(params.get("userId")) || StringUtils.isEmpty(params.get("payPasswd"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "用户账号或支付密码不能为空");
        }
        String token = super.getToken();
        return uauthService.payPasswd(params, token);
    }

    /**
     * 是否注册
     *
     * @param mobile
     * @return
     */
    @RequestMapping(value = "/app/appserver/uauth/isRegister", method = RequestMethod.GET)
    public Map<String, Object> isRegister(@RequestParam String mobile) {
        if ("13".equals(super.getChannel()) || "14".equals(super.getChannel()) || "34".equals(super.getChannelNO())) {
            return uauthService.isRegisterHaier(mobile);
        } else {
            return uauthService.isRegister(mobile);
        }
    }

    /**
     * 登录验证
     *
     * @param params
     * @return
     */
    @RequestMapping(value = "/app/appserver/uauth/validateUsers", method = RequestMethod.PUT)
    @Deprecated
    public Map<String, Object> validateUsers(@RequestBody Map<String, Object> params) {
        if (StringUtils.isEmpty(params.get("userId")) || StringUtils.isEmpty(params.get("password"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "用户账号或登录密码不能为空");
        }
        return uauthService.validateUsers(params);
    }

    /**
     * 头像设置
     *
     * @param params
     * @return
     */
    @RequestMapping(value = "/app/appserver/uauth/avatarUrl", method = RequestMethod.PUT)
    public Map<String, Object> avatarUrl(@RequestBody Map<String, Object> params) {
        if (StringUtils.isEmpty(params.get("userId")) || StringUtils.isEmpty(params.get("avatarUrl"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "用户账号或头像不能为空");
        }
        return uauthService.avatarUrl(params);
    }

    /**
     * 实名认证
     *
     * @param params
     * @return
     */
    @RequestMapping(value = "/app/appserver/uauth/identify", method = RequestMethod.GET)
    public Map<String, Object> identify(@RequestParam Map<String, Object> params) {
        if (StringUtils.isEmpty(params.get("userId"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "用户账号不能为空");
        }
        if (StringUtils.isEmpty(params.get("custName"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "姓名不能为空");
        }
        if (StringUtils.isEmpty(params.get("certNo"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "身份证不能为空");
        }
        if (StringUtils.isEmpty(params.get("cardNo"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "银行卡号不能为空");
        }
        if (StringUtils.isEmpty(params.get("mobile"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "预留手机号不能为空");
        }
        if (StringUtils.isEmpty(params.get("bankCode"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "银行号不能为空");
        }
        return uauthService.identify(params);
    }

    /**
     * 修改绑定手机号
     *
     * @param params
     * @return
     */
    @RequestMapping(value = "/app/appserver/uauth/updateMobile", method = RequestMethod.PUT)
    public Map<String, Object> updateMobile(@RequestBody Map<String, Object> params) {
        if (StringUtils.isEmpty(params.get("userId"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "用户账号不能为空");
        }
        if (StringUtils.isEmpty(params.get("oldMobile"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "原手机号不能为空");
        }
        if (StringUtils.isEmpty(params.get("newMobile"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "新手机号不能为空");
        }
        if (StringUtils.isEmpty(params.get("verifyNo"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "验证码不能为空");
        }

        return uauthService.updateMobile(params);
    }

    /**
     * 客户登录密码设置、修改（验证码）
     *
     * @param params
     * @return
     */
    @RequestMapping(value = "/app/appserver/uauth/custUpdatePwd", method = RequestMethod.PUT)
    public Map<String, Object> custUpdatePwd(@RequestBody Map<String, Object> params) {
        if (StringUtils.isEmpty(params.get("userId"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "用户账号不能为空");
        }
        if (StringUtils.isEmpty(params.get("newPassword"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "新登录密码不能为空");
        }
        if (StringUtils.isEmpty(params.get("verifyNo"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "验证码不能为空");
        }
        return uauthService.custUpdatePwd(params);
    }

    /**
     * 客户登录密码设置、修改（实名认证+验证码）
     *
     * @param params
     * @return
     */
    @RequestMapping(value = "/app/appserver/uauth/custVerifyUpdatePwd", method = RequestMethod.PUT)
    public Map<String, Object> custVerifyUpdatePwd(@RequestBody Map<String, Object> params) {
        if (StringUtils.isEmpty(params.get("userId"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "用户账号不能为空");
        }
        if (StringUtils.isEmpty(params.get("newPassword"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "新登录密码不能为空");
        }

        if (StringUtils.isEmpty(params.get("certNo"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "身份证号不能为空");
        }
        if (StringUtils.isEmpty(params.get("cardNo"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "银行卡号不能为空");
        }
        if (StringUtils.isEmpty(params.get("custName"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "客户姓名不能为空");
        }
        if (StringUtils.isEmpty(params.get("mobile"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "预留手机号不能为空");
        }
        if (StringUtils.isEmpty(params.get("bankCode"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "银行号不能为空");
        }
        if (StringUtils.isEmpty(params.get("verifyNo"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "验证码不能为空");
        }
        return uauthService.custVerifyUpdatePwd(params);
    }

    /**
     * 支付密码修改
     *
     * @param params
     * @return
     */
    @RequestMapping(value = "/app/appserver/uauth/updatePayPasswd", method = RequestMethod.PUT)
    public Map<String, Object> updatePayPasswd(@RequestBody Map<String, Object> params) {
        if (StringUtils.isEmpty(params.get("userId"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "用户账号不能为空");
        }
        if (StringUtils.isEmpty(params.get("payPasswd"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "原支付密码不能为空");
        }
        if (StringUtils.isEmpty(params.get("newPayPasswd"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "新支付密码不能为空");
        }
        String token = super.getToken();
        return uauthService.updatePayPasswd(params, token);
    }

    /**
     * 用户注册
     *
     * @param params
     * @return
     */
    @RequestMapping(value = "/app/appserver/uauth/saveUauthUsers", method = RequestMethod.POST)
    public Map<String, Object> saveUauthUsers(@RequestBody Map<String, Object> params) {
        if (StringUtils.isEmpty(params.get("mobile"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "手机号不能为空");
        }
        if (StringUtils.isEmpty(params.get("password"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "登录密码不能为空");
        }
        if (StringUtils.isEmpty(params.get("verifyNo"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "验证码不能为空");
        }
        return uauthService.saveUauthUsers(params, super.getChannel());
    }


    /**
     * 客户支付密码设置、修改（实名认证+验证码）
     *
     * @param params
     * @return
     */
    @RequestMapping(value = "/app/appserver/uauth/custVerifyUpdatePayPwd", method = RequestMethod.PUT)
    public Map<String, Object> custVerifyUpdatePayPwd(@RequestBody Map<String, Object> params) {
        if (StringUtils.isEmpty(params.get("userId"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "用户账号不能为空");
        }
        if (StringUtils.isEmpty(params.get("newPayPasswd"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "新支付密码不能为空");
        }

        if (StringUtils.isEmpty(params.get("certNo"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "身份证号不能为空");
        }
        if (StringUtils.isEmpty(params.get("cardNo"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "银行卡号不能为空");
        }
        if (StringUtils.isEmpty(params.get("custName"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "客户姓名不能为空");
        }
        if (StringUtils.isEmpty(params.get("mobile"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "预留手机号不能为空");
        }
        if (StringUtils.isEmpty(params.get("bankCode"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "银行号不能为空");
        }
        if (StringUtils.isEmpty(params.get("verifyNo"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "验证码不能为空");
        }
        return uauthService.custVerifyUpdatePayPwd(params);
    }

    /**
     * 登陆验证
     *
     * @param params
     * @return
     */
    @Deprecated
    @RequestMapping(value = "/app/appserver/uauth/validateUsersCount", method = RequestMethod.GET)
    public Map<String, Object> validateUsersCount(@RequestParam Map<String, Object> params) {
        if (StringUtils.isEmpty(params.get("userId"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "用户账号不能为空");
        }
        if (StringUtils.isEmpty(params.get("password"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "支付密码不能为空");
        }
       if(!StringUtils.isEmpty(params.get("captchaAnswer"))){
            if(StringUtils.isEmpty(params.get("captchaToken"))){
                return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "验证码token为空");
            }
        }
        return uauthService.validateUsersCount(params);
    }

    /**
     * 手势密码验证
     *
     * @param params
     * @return
     */
    @Deprecated
    @RequestMapping(value = "/app/appserver/uauth/validateGestureCount", method = RequestMethod.GET)
    public Map<String, Object> validateGestureCount(@RequestParam Map<String, Object> params) {
        if (StringUtils.isEmpty(params.get("userId"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "用户账号不能为空");
        }
        if (StringUtils.isEmpty(params.get("gesture"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "手势密码不能为空");
        }
        return uauthService.validateGestureCount(params);
    }

    /**
     * 根据集团用户id查询用户信息
     *
     * @param params
     * @return
     */
    @RequestMapping(value = "/app/appserver/uauth/queryHaierUserInfo", method = RequestMethod.GET)
    public Map<String, Object> queryHaierUserInfo(@RequestParam Map<String, Object> params) {
        if (StringUtils.isEmpty(params.get("externUid"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "集团用户Id不能为空");
        }
        return uauthService.queryHaierUserInfo(params);
    }

    /**
     * 集团用户注册统一认证账户
     *
     * @param params
     * @return
     */
    @RequestMapping(value = "/app/appserver/uauth/saveUauthUsersByHaier", method = RequestMethod.POST)
    public Map<String, Object> saveUauthUsersByHaier(@RequestBody Map<String, Object> params) {
        if (StringUtils.isEmpty(params.get("externUid"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "集团用户Id不能为空");
        }
        if (StringUtils.isEmpty(params.get("mobile"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "集团手机号不能为空");
        }
        return uauthService.saveUauthUsersByHaier(params);
    }

    /**
     * 封装统一认证 1.29，验证并绑定集团用户（已绑定的不可用）
     *
     * @param params
     * @return
     */
    @Deprecated
    @RequestMapping(value = "/app/appserver/uauth/validateAndBindHaierUser", method = RequestMethod.POST)
    public Map<String, Object> validateAndBindHaierUser(@RequestBody Map<String, Object> params) {
        if (StringUtils.isEmpty(params.get("userId"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "用户Id不能为空");
        }
        if (StringUtils.isEmpty(params.get("password"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "登录密码不能为空");
        }
        if (StringUtils.isEmpty(params.get("externUid"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "集团用户Id不能为空");
        }

        return uauthService.validateAndBindHaierUser(params);
    }


    /**
     * 第三方系统token保存至redis
     *
     * @param params
     * @return
     */
    @RequestMapping(value = "/app/appserver/saveThirdPartToken", method = RequestMethod.POST)
    public Map<String, Object> saveThirdPartToken(@RequestBody Map<String, Object> params) {
        logger.info("第三方系统token保存至redis: " + params);
        if (StringUtils.isEmpty(params.get("userId"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "用户id不能为空");
        }
        if (StringUtils.isEmpty(params.get("token"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "用户口令token不能为空");
        }

        String userId = (String) params.get("userId");
        String token = (String) params.get("token");
        Long hset = RedisUtil.hset(RedisUtil.serialize("__token_app_userStore"), RedisUtil.serialize(token), RedisUtil.serialize(userId));
        logger.debug("第三方系统保存token结果: " + hset);
        return hset == 0L || hset == 1L ? success() : fail("23", "保存失败！");
    }

    /**
     * 得到绑定手机号
     * @param userId
     * @return
     */
    @RequestMapping(value = "/app/appserver/uauth/getMobile", method = RequestMethod.GET)
    public Map<String, Object> getMobile(@RequestParam String userId) {
        if (StringUtils.isEmpty(userId)) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "用户账号不能为空");
        }
        return uauthService.getMobile(userId);
    }

    /**
     * 刷新海尔会员验证码
     */
    @RequestMapping(value = "/app/appserver/uauth/haierCaptcha", method = RequestMethod.GET)
    public Map<String, Object> haierCaptcha() {
        return uauthService.haierCaptcha();
    }
}