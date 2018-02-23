package com.haiercash.payplatform.pc.alipay.service;

import com.alipay.api.AlipayApiException;
import com.alipay.api.response.AlipayUserInfoShareResponse;
import com.haiercash.core.collection.MapUtils;
import com.haiercash.core.lang.BeanUtils;
import com.haiercash.core.lang.Convert;
import com.haiercash.core.lang.StringUtils;
import com.haiercash.core.reflect.GenericType;
import com.haiercash.core.serialization.JsonSerializer;
import com.haiercash.payplatform.config.AlipayConfig;
import com.haiercash.payplatform.config.OutreachConfig;
import com.haiercash.payplatform.pc.alipay.bean.AlipayToken;
import com.haiercash.payplatform.pc.alipay.util.AlipayUtils;
import com.haiercash.payplatform.service.AppServerService;
import com.haiercash.payplatform.service.OCRIdentityService;
import com.haiercash.payplatform.service.OutreachService;
import com.haiercash.payplatform.service.client.OutreachClient;
import com.haiercash.payplatform.service.client.UauthClient;
import com.haiercash.payplatform.utils.EncryptUtil;
import com.haiercash.spring.context.TraceContext;
import com.haiercash.spring.redis.RedisUtils;
import com.haiercash.spring.rest.IResponse;
import com.haiercash.spring.rest.common.CommonResponse;
import com.haiercash.spring.service.BaseService;
import com.haiercash.spring.util.BusinessException;
import com.haiercash.spring.util.ConstUtil;
import com.haiercash.spring.util.HttpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Created by 许崇雷 on 2018-01-18.
 */
@Service
public class AlipayFuwuService extends BaseService {
    @Autowired
    private AppServerService appServerService;
    @Autowired
    private OCRIdentityService ocrIdentityService;
    @Autowired
    private OutreachConfig outreachConfig;
    @Autowired
    private AlipayConfig alipayConfig;
    @Autowired
    private OutreachService outreachService;
    @Autowired
    private UauthClient uauthClient;
    @Autowired
    private OutreachClient outreachClient;

    //联合登陆 auth_base 模式
    public IResponse<Map> login(String authCode) throws AlipayApiException {
        AlipayToken alipayToken = AlipayUtils.getOauthTokenByAuthCode(authCode);
        String thirdUserId = alipayToken.getUserId();//支付宝 userId
        String token = UUID.randomUUID().toString();//支付平台的 token
        //返回(临时代码)
        Map<String, Object> body = new HashMap<>();
        body.put("flag", "13");//转额度
        body.put("token", token);
        //保存 session
        Map<String, Object> sessionMap = new HashMap<>();
        sessionMap.put("externalUserId", "2088122166360986");
        sessionMap.put("userId", "17685797923");
        sessionMap.put("phoneNo", "17685797923");
        sessionMap.put("custNo", "A201801030203522321930");//客户编号
        sessionMap.put("name", "朱晓雪");//客户姓名
        sessionMap.put("cardNo", "6217000010068654378");//银行卡号
        sessionMap.put("bankCode", "105");//银行代码
        sessionMap.put("bankName", "中国建设银行");//银行名称
        sessionMap.put("idNo", "370782199302035223");//身份证号
        sessionMap.put("idCard", "370782199302035223");//身份证号
        sessionMap.put("idType", "20");
        RedisUtils.setExpire(token, sessionMap);

        return CommonResponse.success(body);
    }

    //授权后验证用户
    public IResponse<Map> validUser(String authCode) throws AlipayApiException {
        AlipayToken token = AlipayUtils.getOauthTokenByAuthCode(authCode);
        this.logger.info("支付宝 token: " + token);
        AlipayUserInfoShareResponse alipayUserInfo = AlipayUtils.getUserInfo(token.getToken());
        this.logger.info("支付宝用户信息: " + JsonSerializer.serialize(alipayUserInfo));
        if (!"2".equals(alipayUserInfo.getUserType()))
            return CommonResponse.fail(ConstUtil.ERROR_CODE, "非个人账号,不能准入");
        if (!"T".equals(alipayUserInfo.getUserStatus()))
            return CommonResponse.fail(ConstUtil.ERROR_CODE, "非认证用户,不能准入");
        if (!"T".equals(alipayUserInfo.getIsCertified()))
            return CommonResponse.fail(ConstUtil.ERROR_CODE, "非实名用户,不能准入");
        if ("T".equals(alipayUserInfo.getIsStudentCertified()))
            return CommonResponse.fail(ConstUtil.ERROR_CODE, "学生,不能准入");

        Map<String, Object> sessionMap = RedisUtils.getExpireMap(this.getToken());
        String flag = Convert.toString(sessionMap.get("redirect_flag"));
        if (StringUtils.isEmpty(flag))
            return CommonResponse.fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        Map<String, Object> result = new HashMap<>(1);
        result.put("flag", flag);
        return CommonResponse.success(result);
    }

    //实名认证
    public IResponse<Map> realAuthentication(Map<String, Object> params) throws IOException {
        String token = this.getToken();
        String verifyNo = Convert.toString(params.get("verifyNo"));
        String phone = Convert.toString(params.get("mobile"));

        //验证短信验证码 实名的时候会验证
        Map<String, Object> verifyNoMap = new HashMap<>();
        verifyNoMap.put("phone", phone);
        verifyNoMap.put("verifyNo", verifyNo);
        verifyNoMap.put("token", this.getToken());
        verifyNoMap.put("channel", this.getChannel());
        verifyNoMap.put("channelNo", this.getChannelNo());
        IResponse<Map> verifyResponse = BeanUtils.mapToBean(appServerService.smsVerify(this.getToken(), verifyNoMap), new GenericType<CommonResponse<Map>>() {
        });
        verifyResponse.assertSuccess();

        //获取 session 信息
        Map<String, Object> sessionMap = RedisUtils.getExpireMap(this.getToken());
        if (MapUtils.isEmpty(sessionMap))
            throw new BusinessException(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);

        //芝麻授权
        String name = Convert.toString(sessionMap.get("name"));
        String certNo = Convert.toString(sessionMap.get("idCard"));
        if (StringUtils.isEmpty(name) || StringUtils.isEmpty(certNo))
            return CommonResponse.fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        Map<String, Object> authParams = new HashMap<>();
        authParams.put("channelNo", this.outreachConfig.getChannelNo());
        authParams.put("businessChannelNo", this.getChannelNo());
        authParams.put("appid", this.outreachConfig.getAppid());
        authParams.put("name", name);
        authParams.put("certNo", certNo);
        authParams.put("mobileOne", sessionMap.get("authPhone"));
        authParams.put("mobileTwo", phone);
        authParams.put("applseq", TraceContext.getTraceSpanId());
        IResponse<Map> authResponse = outreachService.protocolauth(authParams);
        if (Objects.equals(authResponse.getRetFlag(), "00005") || Objects.equals(authResponse.getRetFlag(), "00091")) {//空或不一致
            Map<String, Object> body = new HashMap<>(1);
            body.put("legalPhone", "F");
            return CommonResponse.success(body);
        }
        authResponse.assertSuccess();

        //芝麻分判断
        Map<String, Object> scoreParams = new HashMap<>();
        scoreParams.put("channelNo", this.outreachConfig.getChannelNo());
        scoreParams.put("businessChannelNo", this.getChannelNo());
        scoreParams.put("appid", this.outreachConfig.getAppid());
        scoreParams.put("certNo", certNo);
        scoreParams.put("applseq", TraceContext.getTraceSpanId());
        scoreParams.put("interfaceCode", "00801");
        scoreParams.put("status", "00");
        IResponse<Map> scoreResponse = outreachService.score(scoreParams);
        scoreResponse.assertSuccessNeedBody();
        Integer score = Convert.nullInteger(scoreResponse.getBody().get("zm_score"));
        if (score == null || score < alipayConfig.getFuwuMinScore())
            return CommonResponse.fail(ConstUtil.ERROR_CODE, "芝麻分数不符合条件");

        //根据token 查找 三方 uid
        String thirdUserId = (String) sessionMap.get("externalUserId");
        if (StringUtils.isEmpty(thirdUserId))
            throw new BusinessException(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);

        //根据第三方 uid 查询用户信息
        IResponse<Map> isRegisterResp = this.uauthClient.isRegister(EncryptUtil.simpleEncrypt(phone));
        String isRegisterFlag = Convert.toString(isRegisterResp.getBody().get("isRegister"));//N：未注册,Y：已注册, C: 手机号已被占用
        String userId;
        switch (isRegisterFlag) {
            case "N"://未注册
                //调用外联实名认证
                Map<String, Object> identityParams = new HashMap<>();
                identityParams.put("accountName", name);//姓名
                identityParams.put("accountNo", params.get("cardnumber"));//银行卡号
                identityParams.put("bankCode", params.get("bankNo"));//银行号
                identityParams.put("id", certNo);
                identityParams.put("cardPhone", params.get("mobile"));
                identityParams.put("flag", "1");//四要素
                identityParams.put("channelNo", this.getChannelNo());
                identityParams.put("days", "0");
                Map<String, Object> identityResp = this.outreachClient.identifyByFlag(identityParams);
                if (!"00000".equals(identityResp.get("RET_CODE")))
                    throw new BusinessException(ConstUtil.ERROR_CODE, "实名认证失败");
                //注册并绑定
                IResponse<String> saveResult = this.saveUserByExternUid(thirdUserId, phone);
                saveResult.assertSuccessNeedBody();
                userId = saveResult.getBody();
                logger.info("注册后返回 userId: " + userId);
                break;
            case "Y"://已注册
                IResponse<Map> userInfo = this.uauthClient.getUserId(EncryptUtil.simpleEncrypt(phone));//根据手机获取 userId
                userId = Convert.toString(userInfo.getBody().get("userId"));
                //5.查询实名信息
                Map<String, Object> custMap = new HashMap<>();
                custMap.put("userId", userId);//内部userId
                custMap.put("channel", ConstUtil.CHANNEL);
                custMap.put("channelNo", this.getChannelNo());
                Map<String, Object> custresult = appServerService.queryPerCustInfo(token, custMap);
                if (!HttpUtil.isSuccess(custresult))
                    throw new BusinessException(HttpUtil.getRetFlag(custresult), HttpUtil.getRetMsg(custresult));
                Map<String, Object> custBody = HttpUtil.getBodyMap(custresult);
                String custCertNo = Convert.toString(custBody.get("certNo"));
                if (StringUtils.isEmpty(custCertNo))
                    throw new BusinessException(ConstUtil.ERROR_CODE, "实名身份证为空");
                if (!custCertNo.equals(sessionMap.get("certNo")))
                    throw new BusinessException(ConstUtil.ERROR_CODE, "客户已被占用");
                //绑定


                break;
            case "C"://被占用
                throw new BusinessException(ConstUtil.ERROR_CODE, "手机号被占用");
            default:
                throw new BusinessException(ConstUtil.ERROR_CODE, "查询注册状态失败");
        }

        //再次查询
        IResponse<Map> externUidResp = this.queryUserByExternUid(thirdUserId);
        externUidResp.assertSuccessNeedBody();
        Map<String, String> custInfoBody = externUidResp.getBody();

        //保存 session
        sessionMap.put("userId", custInfoBody.get("userId"));
        sessionMap.put("phoneNo", custInfoBody.get("mobile"));
        sessionMap.put("custNo", custInfoBody.get("custNo"));//客户编号
        sessionMap.put("name", custInfoBody.get("custName"));//客户姓名
        sessionMap.put("cardNo", custInfoBody.get("cardNo"));//银行卡号
        sessionMap.put("bankCode", custInfoBody.get("acctBankNo"));//银行代码
        sessionMap.put("bankName", custInfoBody.get("acctBankName"));//银行名称
        sessionMap.put("idNo", custInfoBody.get("certNo"));//身份证号
        sessionMap.put("idCard", custInfoBody.get("certNo"));//身份证号
        sessionMap.put("idType", custInfoBody.get("certType"));
        RedisUtils.setExpire(token, sessionMap);

        IResponse<Map> realAuthResponse = this.ocrIdentityService.realAuthentication(params);
        realAuthResponse.assertSuccess();
        Map<String, Object> body = new HashMap<>(1);
        body.put("legalPhone", "T");
        return CommonResponse.success(body);
    }

    //查询第三方账号
    private IResponse<Map> queryUserByExternUid(String thirdUserId) {
        return this.uauthClient.queryUserByExternUid(EncryptUtil.simpleEncrypt(this.getChannelNo()), EncryptUtil.simpleEncrypt(thirdUserId));
    }

    //注册第三方用户
    private IResponse<String> saveUserByExternUid(String externUid, String linkMobile) {
        Map<String, Object> map = new HashMap<>();
        String externCompanyNo_ = EncryptUtil.simpleEncrypt(this.getChannelNo());
        String externUid_ = EncryptUtil.simpleEncrypt(externUid);
        String linkMobile_ = EncryptUtil.simpleEncrypt(linkMobile);
        map.put("externCompanyNo", externCompanyNo_);
        map.put("externUid", externUid_);
        map.put("linkMobile", linkMobile_);
        return this.uauthClient.saveUserByExternUid(map);
    }
}
