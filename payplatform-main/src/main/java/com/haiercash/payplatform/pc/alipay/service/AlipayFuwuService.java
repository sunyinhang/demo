package com.haiercash.payplatform.pc.alipay.service;

import com.alipay.api.AlipayApiException;
import com.alipay.api.response.AlipayUserInfoShareResponse;
import com.haiercash.core.collection.CollectionUtils;
import com.haiercash.core.collection.MapUtils;
import com.haiercash.core.lang.Convert;
import com.haiercash.core.lang.StringUtils;
import com.haiercash.core.serialization.JsonSerializer;
import com.haiercash.payplatform.config.AlipayConfig;
import com.haiercash.payplatform.config.OutreachConfig;
import com.haiercash.payplatform.pc.alipay.bean.AlipayToken;
import com.haiercash.payplatform.pc.alipay.util.AlipayUtils;
import com.haiercash.payplatform.service.AppServerService;
import com.haiercash.payplatform.service.OCRIdentityService;
import com.haiercash.payplatform.service.OutreachService;
import com.haiercash.payplatform.service.client.AcquirerClient;
import com.haiercash.payplatform.service.client.AppServerClient;
import com.haiercash.payplatform.service.client.OutreachClient;
import com.haiercash.payplatform.service.client.UauthClient;
import com.haiercash.payplatform.utils.EncryptUtil;
import com.haiercash.spring.context.TraceContext;
import com.haiercash.spring.redis.RedisUtils;
import com.haiercash.spring.rest.IResponse;
import com.haiercash.spring.rest.acq.AcqRequestBuilder;
import com.haiercash.spring.rest.acq.IAcqRequest;
import com.haiercash.spring.rest.common.CommonResponse;
import com.haiercash.spring.service.BaseService;
import com.haiercash.spring.util.BusinessException;
import com.haiercash.spring.util.ConstUtil;
import com.haiercash.spring.util.HttpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
    @Autowired
    private AcquirerClient acquirerClient;
    @Autowired
    private AppServerClient appServerClient;

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
        if (MapUtils.isEmpty(sessionMap))
            throw new BusinessException(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);

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
        IResponse<Map> verifyResponse = this.appServerClient.smsVerify(verifyNoMap);
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
        String thirdUserId = (String) sessionMap.get("uidHaier");
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
                //查询实名信息,判断四要素是否一致
                Map<String, Object> custMap = new HashMap<>();
                custMap.put("userId", userId);//内部userId
                custMap.put("channel", ConstUtil.CHANNEL);
                custMap.put("channelNo", this.getChannelNo());
                Map<String, Object> custresult = appServerService.queryPerCustInfo(token, custMap);
                String custRetFlag = HttpUtil.getRetFlag(custresult);
                if (HttpUtil.isSuccess(custresult)) {
                    Map<String, Object> custBody = HttpUtil.getBodyMap(custresult);
                    String custCertNo = Convert.toString(custBody.get("certNo"));
                    if (StringUtils.isEmpty(custCertNo))
                        throw new BusinessException(ConstUtil.ERROR_CODE, "实名身份证为空");
                    if (!custCertNo.equals(sessionMap.get("idNo")))
                        throw new BusinessException(ConstUtil.ERROR_CODE, "客户已被占用");
                } else if (!"C1220".equals(custRetFlag)) {
                    throw new BusinessException(HttpUtil.getRetFlag(custresult), HttpUtil.getRetMsg(custresult));
                }
                break;
            case "C"://被占用
                throw new BusinessException(ConstUtil.ERROR_CODE, "手机号被占用");
            default:
                throw new BusinessException(ConstUtil.ERROR_CODE, "查询注册状态失败");
        }
        //实名准备 session 数据
        sessionMap.put("userId", userId);
        sessionMap.put("phoneNo", phone);
        RedisUtils.setExpire(token, sessionMap);//实名的时候会用到 userId
        //实名
        IResponse<Map> realAuthResponse = this.ocrIdentityService.realAuthenticationForXjd(params);
        realAuthResponse.assertSuccess();
        //绑定
        Map<String, Object> editParams = new HashMap<>();
        editParams.put("mobile", EncryptUtil.simpleEncrypt(phone));
        editParams.put("externUid", EncryptUtil.simpleEncrypt(thirdUserId));
        editParams.put("externCompanyNo", EncryptUtil.simpleEncrypt(this.getChannelNo()));
        IResponse<Map> editResp = this.uauthClient.unvalidateAndBindUserByExternUid(editParams);
        editResp.assertSuccess();
        //返回
        Map<String, Object> body = new HashMap<>(1);
        body.put("legalPhone", "T");
        return CommonResponse.success(body);
    }

    //支付
    public String wapPay(Map<String, Object> params) throws AlipayApiException {
        String token = this.getToken();
        Map<String, Object> sessionMap = RedisUtils.getExpireMap(token);
        if (MapUtils.isEmpty(sessionMap))
            throw new BusinessException(ConstUtil.ERROR_CODE, "无效的操作, 请重新登陆");
        String custNo = Convert.toString(sessionMap.get("custNo"));
        if (StringUtils.isEmpty(custNo))
            throw new BusinessException(ConstUtil.ERROR_CODE, "custNo 不能为空");
        BigDecimal repayAmt = Convert.nullDecimal(params.get("repayAmt"));
        if (repayAmt == null)
            throw new BusinessException(ConstUtil.ERROR_CODE, "repayAmt 不能为空");

        //调用收单 还款申请
        Map<String, Object> acqParams = new HashMap<>();
        acqParams.put("applSeq", params.get("applSeq"));
        acqParams.put("setlTyp", "01");//01：信贷还款 02：充值还款
        acqParams.put("setlMode", "");//FS（全部还款）NM（归还欠款）ER（提前还款）信贷还款时必传
        acqParams.put("repayAmt", repayAmt);//还款总金额  repayAmt  NUMBER(16,2)  是
        acqParams.put("psPerdNo", "");//还款期  psPerdNo  VARCHAR2(200)  是  多个期号以“|”分隔。随借随还传“1”
        acqParams.put("acCardNo", "00000000");//还款卡号  acCardNo  VARCHAR2(30)  是
        acqParams.put("useCoup", "N");//是否使用优惠券  useCoup  VARCHAR2(10)  是  Y：使用 N：不使用
        acqParams.put("custNo", custNo);//客户编号  custNo  VARCHAR2(30)  是
        acqParams.put("actvPrcp", "");//提前还款本金  actvPrcp  Number  O:选填  提前还款本金模式时必输
        IAcqRequest request = AcqRequestBuilder.newBuilder("ACQ-2101")
                .body(Collections.singletonList(acqParams))
                .build();
        IResponse<List<Map>> response = this.acquirerClient.saveZdhkInfo(request);
        response.assertSuccessNeedBody();
        List<Map> bodyList = response.getBody();
        if (CollectionUtils.isEmpty(bodyList)) {
            this.logger.info("收单提交还款请求返回的 body 的 list 为空");
            throw new BusinessException(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        Map<String, Object> body = bodyList.get(0);
        String applSeq = Convert.toString(body.get("applSeq"));
        if (StringUtils.isEmpty(applSeq)) {
            this.logger.info("收单未返回 applSeq");
            throw new BusinessException(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }

        //发起网页支付
        return AlipayUtils.wapPay(applSeq, repayAmt, this.alipayConfig.getSubject());
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
