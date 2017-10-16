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
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by 许崇雷 on 2017-10-10.
 */
@Service
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

    @Value("${app.other.haiercashpay_web_url}")
    protected String haiercashpay_web_url;

    @Override
    public ModelAndView getActivityUrl() {
        String channelNo = this.getChannelNo();
        this.logger.info("开始活动跳转 channelNo:" + channelNo);
        EntrySetting setting = this.entrySettingDao.selectBychanelNo(channelNo);
        if (setting == null) {
            return new ModelAndView("forward:/error");
        }
        String url = haiercashpay_web_url + setting.getActivityUrl();
        return new ModelAndView("forward:" + url);
    }

    @Override
    public Map<String, Object> joinActivity() {
        String channelNo = this.getChannelNo();
        EntrySetting setting = this.entrySettingDao.selectBychanelNo(channelNo);
        if (setting == null) {
            return fail(ConstUtil.ERROR_CODE, "没有配置相应渠道数据！");
        }
        String loginType = setting.getLoginType();
        switch (loginType) {
            case "01":
//                Map cachemap = new HashMap<String, Object>();
//                String token = UUID.randomUUID().toString();
//                cachemap.put("token", token);
//                this.redisSession.set(token, cachemap);
                Map<String, Object> map = new HashMap<>();
                map.put("flag", "1");
//                map.put("token", token);
                return success(map);//跳转到登陆页

            case "02":
                String thirdToken = this.getToken();
                if (StringUtils.isEmpty(thirdToken))
                    return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "token 登陆,但是未传递 token");
                return this.joinActivityRedirect(setting);

            default:
                String msg = "错误的登陆类型:" + loginType;
                logger.warn(msg);
                return fail(ConstUtil.ERROR_CODE, msg);
        }
    }

    private Map<String, Object> joinActivityRedirect(EntrySetting setting) {
        logger.info("申请接口*******************开始");
        Map cachemap = new HashMap<String, Object>();
        Map returnmap = new HashMap<String, Object>();//返回的map
        String channelNo = this.getChannelNo();
        String thirdToken = this.getToken();
        String verifyUrl = setting.getVerifyUrlThird() + thirdToken;
        String uidLocal = "";//统一认证userid
        String phoneNo = "";//统一认证绑定手机号
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
        if (Objects.equals(retFlag, "00000")) {
            //集团uid已在统一认证做过绑定
            String body = userInfo.get("body").toString();
            //Map<String, Object> bodyMap = HttpUtil.json2Map(body);
            JSONObject bodyMap = new JSONObject(body);
            uidLocal = bodyMap.get("userId").toString();//统一认证内userId
            phoneNo = bodyMap.get("mobile").toString();//统一认绑定手机号
        } else if (Objects.equals(retFlag, "U0157")) {//U0157：未查到该集团用户的信息
            //向后台注册用户信息
            Map<String, Object> registerResult = thirdTokenVerifyService.registerUserToAppServer(thirdInfo.getUserId(), thirdInfo.getPhoneNo());
            String registerResultFlag = HttpUtil.getReturnCode(registerResult);
            if ("00000".equals(registerResultFlag)) {
                uidLocal = registerResult.get("body").toString();//统一认证内userId
                phoneNo = thirdInfo.getPhoneNo();//统一认绑定手机号
            } else if ("U0160".equals(registerResultFlag)) {//U0160:该用户已注册，无法注册
                //跳转登录页面进行登录
//        this.redisSession.hset(thirdToken, cachemap);
                this.redisSession.set(thirdToken, cachemap);
//        String backurl = haiercashpay_web_url + "sgbt/#!/login/login.html?token=" + token;
                returnmap.put("flag", "2");//跳转登陆绑定页
                returnmap.put("token", thirdToken);
                return success(returnmap);
            } else {
                //注册失败
                String userretmsg = HttpUtil.getRetMsg(registerResult);
                return fail(ConstUtil.ERROR_CODE, userretmsg);
            }
        } else {
            throw new BusinessException(HttpUtil.getReturnCode(userInfo), HttpUtil.getRetMsg(userInfo));
        }

        cachemap.put("userId", uidLocal);//统一认证userId
        cachemap.put("phoneNo", phoneNo);//绑定手机号
//        redisSession.set(thirdToken, cachemap);

        logger.info("进行token绑定");
        //4.token绑定
        Map<String, Object> bindMap = new HashMap<String, Object>();
        bindMap.put("userId", uidLocal);//内部userId
        bindMap.put("token", thirdToken);
        bindMap.put("channel", "11");
        bindMap.put("channelNo", channelNo);
        Map bindresult = appServerService.saveThirdPartToken(bindMap);
        if (!HttpUtil.isSuccess(bindresult)) {//绑定失败
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        }

        //5.查询实名信息
        Map<String, Object> custMap = new HashMap<String, Object>();
        custMap.put("userId", uidLocal);//内部userId
        custMap.put("channel", "11");
        custMap.put("channelNo", channelNo);
        Map custresult = appServerService.queryPerCustInfo(thirdToken, custMap);
        String custretflag = ((Map<String, Object>) (custresult.get("head"))).get("retFlag").toString();
        if (!"00000".equals(custretflag) && !"C1220".equals(custretflag)) {//查询实名信息失败
            String custretMsg = ((Map<String, Object>) (custresult.get("head"))).get("retMsg").toString();
            return fail(ConstUtil.ERROR_CODE, custretMsg);
        }
        if ("C1220".equals(custretflag)) {//C1120  客户信息不存在  跳转无额度页面
            logger.info("token:" + thirdToken);
            logger.info("跳转额度激活，cachemap：" + cachemap.toString());
            redisSession.set(thirdToken, cachemap);

            returnmap.put("flag", "3");//跳转无额度
            returnmap.put("token", thirdToken);
//
//            String backurl = haiercashpay_web_url + "sgbt/#!/applyQuota/amountNot.html?token=" + thirdToken;
//            returnmap.put("backurl", backurl);
//            logger.info("页面跳转到：" + backurl);
            return success(returnmap);
        }
        String certType = ((Map<String, Object>) (custresult.get("body"))).get("certType").toString();//证件类型
        String certNo = ((Map<String, Object>) (custresult.get("body"))).get("certNo").toString();//身份证号
        String custNo = ((Map<String, Object>) (custresult.get("body"))).get("custNo").toString();//客户编号
        String custName = ((Map<String, Object>) (custresult.get("body"))).get("custName").toString();//客户名称
        String cardNo = ((Map<String, Object>) (custresult.get("body"))).get("cardNo").toString();//银行卡号
        String bankNo = ((Map<String, Object>) (custresult.get("body"))).get("acctBankNo").toString();//银行代码
        String bankName = ((Map<String, Object>) (custresult.get("body"))).get("acctBankName").toString();//银行名称

        cachemap.put("custNo", custNo);//客户编号
        cachemap.put("name", custName);//客户姓名
        cachemap.put("cardNo", cardNo);//银行卡号
        cachemap.put("bankCode", bankNo);//银行代码
        cachemap.put("bankName", bankName);//银行名称
        cachemap.put("idNo", certNo);//身份证号
        cachemap.put("idCard", certNo);//身份证号
        cachemap.put("idType", certType);
        redisSession.set(thirdToken, cachemap);
        returnmap.put("flag", "4");//额度激活

        returnmap.put("token", thirdToken);
        return success(returnmap);
    }
}
