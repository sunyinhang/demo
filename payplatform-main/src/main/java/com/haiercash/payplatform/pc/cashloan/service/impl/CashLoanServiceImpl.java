package com.haiercash.payplatform.pc.cashloan.service.impl;

import com.bestvike.lang.StringUtils;
import com.haiercash.commons.redis.Session;
import com.haiercash.payplatform.common.dao.EntrySettingDao;
import com.haiercash.payplatform.common.data.EntrySetting;
import com.haiercash.payplatform.common.entity.ThirdTokenVerifyResult;
import com.haiercash.payplatform.pc.cashloan.service.CashLoanService;
import com.haiercash.payplatform.pc.cashloan.service.ThirdTokenVerifyService;
import com.haiercash.payplatform.service.AppServerService;
import com.haiercash.payplatform.service.BaseService;
import com.haiercash.payplatform.utils.ApplicationContextUtil;
import com.haiercash.payplatform.utils.BusinessException;
import com.haiercash.payplatform.utils.ConstUtil;
import com.haiercash.payplatform.utils.HttpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Created by 许崇雷 on 2017-10-10.
 */
public class CashLoanServiceImpl extends BaseService implements CashLoanService {
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private Session redisSession;

    @Autowired
    private AppServerService appServerService;

    @Autowired
    private ThirdTokenVerifyService thirdTokenVerifyService;

    @Autowired
    private EntrySettingDao entrySettingDao;

    @Override
    public Map<String, Object> getActivityUrl() {
        String channelNo = this.getChannelNo();
        this.logger.info("开始活动跳转 channelNo:" + channelNo);
        EntrySetting setting = this.entrySettingDao.selectByPrimaryKey(channelNo);
        String url = setting.getActivityUrl();
        return success(url);
    }

    @Override
    public Map<String, Object> joinActivity(Map<String, Object> params) {
        String channelNo = this.getChannelNo();
        EntrySetting setting = this.entrySettingDao.selectByPrimaryKey(channelNo);
        String loginType = setting.getLoginType();
        switch (loginType) {
            case "01":
                String token = UUID.randomUUID().toString();
                this.redisSession.hset("__MAP_CHANEL_", token, channelNo);
                Map<String, Object> map = new HashMap<>();
                map.put("url", "/login");
                return success(map);//跳转到登录页

            case "02":
                String thirdToken = this.getToken();
                if (StringUtils.isEmpty(thirdToken))
                    return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "token 登陆,但是未传递 token");
                return this.joinActivityRedirect(params, setting);

            default:
                String msg = "错误的登陆类型:" + loginType;
                logger.warn(msg);
                return fail(ConstUtil.ERROR_CODE, msg);
        }
    }

    private Map<String, Object> joinActivityRedirect(Map<String, Object> params, EntrySetting setting) {
        logger.info("白条额度申请接口*******************开始");
        String channelNo = this.getChannelNo();
        String thirdToken = this.getToken();
        String verifyUrl = setting.getVerifyUrlThird() + thirdToken;
        logger.info("验证第三方 token:" + verifyUrl);
        //验证客户信息
        ThirdTokenVerifyService thirdTokenVerifyService;
        try {
            thirdTokenVerifyService = ApplicationContextUtil.getBean(setting.getVerifyUrlService(), ThirdTokenVerifyService.class);
        } catch (Exception e) {
            throw new BusinessException(ConstUtil.ERROR_CODE, "错误的 thirdTokenVerifyService 名称:'" + setting.getVerifyUrlService() + "'");
        }
        ThirdTokenVerifyResult thirdInfo = thirdTokenVerifyService.verify(setting, thirdToken);

        //从后台查询用户信息
        Map<String, Object> userInfo = thirdTokenVerifyService.queryUserInfoFromAppServer(thirdInfo.getUserId());
        String retFlag = HttpUtil.getReturnCode(userInfo);
        if (Objects.equals(retFlag, "U0157")) {//U0157：未查到该集团用户的信息
            //向后台注册用户信息
            Map<String, Object> registerResult = thirdTokenVerifyService.registerUserToAppServer(thirdInfo.getUserId(), thirdInfo.getPhoneNo());
        }
        String retUrl = setting.getLoginUrl() + thirdToken;
        return success(retUrl);
    }
}
