package com.haiercash.payplatform.pc.alipay.service;

import com.alipay.api.AlipayApiException;
import com.alipay.api.response.AlipayUserInfoShareResponse;
import com.haiercash.core.collection.CollectionUtils;
import com.haiercash.core.collection.MapUtils;
import com.haiercash.core.lang.Convert;
import com.haiercash.core.lang.DateUtils;
import com.haiercash.core.lang.StringUtils;
import com.haiercash.core.serialization.JsonSerializer;
import com.haiercash.payplatform.config.AlipayConfig;
import com.haiercash.payplatform.config.OutreachConfig;
import com.haiercash.payplatform.pc.alipay.bean.AlipayOrder;
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
import java.util.Collections;
import java.util.Date;
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
        AlipayToken token;
        try {
            token = AlipayUtils.getOauthTokenByAuthCode(authCode);
        } catch (AlipayApiException e) {
            this.logger.info("获取支付宝 token 失败:" + e.getMessage());
            Map<String, Object> body = new HashMap<>();
            body.put("flag", "51");//h5 往后退一步
            return CommonResponse.success(body);
        }
        this.logger.info("支付宝 token: " + token);
        AlipayUserInfoShareResponse alipayUserInfo = AlipayUtils.getUserInfo(token.getToken());
        this.logger.info("支付宝用户信息: " + JsonSerializer.serialize(alipayUserInfo));
        if (!"2".equals(alipayUserInfo.getUserType())//非个人账号,不能准入
                || !"T".equals(alipayUserInfo.getUserStatus())//非认证用户,不能准入
                || !"T".equals(alipayUserInfo.getIsCertified())//非实名用户,不能准入
                || "T".equals(alipayUserInfo.getIsStudentCertified())) {//学生,不能准入
            this.logger.info("支付宝信息不符合准入条件");
            Map<String, Object> body = new HashMap<>();
            body.put("flag", "52");//支付宝个人信息不准入,h5 进入错误页
            return CommonResponse.success(body);
        }

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
        //覆盖支付宝预留手机号
        String authPhone = Convert.toString(params.get("authPhone"));
        if (StringUtils.isNotEmpty(authPhone)) {
            sessionMap.put("authPhone", authPhone);
            RedisUtils.setExpire(token, sessionMap);
        }

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
        authParams.put("mobileTwo", sessionMap.get("authPhone"));
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
                    throw new BusinessException(ConstUtil.ERROR_CODE, Convert.defaultString(identityResp.get("RET_MSG"), "实名认证失败"));
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
                    if (!custCertNo.equals(sessionMap.get("idCard")))
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

        //4.token绑定
        Map<String, Object> bindMap = new HashMap<>();
        bindMap.put("userId", userId);//内部userId
        bindMap.put("token", token);
        bindMap.put("channel", ConstUtil.CHANNEL);
        bindMap.put("channelNo", this.getChannelNo());
        Map<String, Object> bindresult = appServerService.saveThirdPartToken(bindMap);
        if (!HttpUtil.isSuccess(bindresult)) {//绑定失败
            throw new BusinessException(ConstUtil.ERROR_CODE, "绑定Token失败");
        }

        //实名
        IResponse<Map> realAuthResponse = this.ocrIdentityService.realAuthenticationForXjd(params);
        realAuthResponse.assertSuccess();
        //绑定
        Map<String, Object> editParams = new HashMap<>();
        editParams.put("mobile", EncryptUtil.simpleEncrypt(phone));
        editParams.put("externUid", EncryptUtil.simpleEncrypt(thirdUserId));
        editParams.put("externCompanyNo", EncryptUtil.simpleEncrypt(this.getChannelNo()));
        IResponse<Map> bindResp = this.uauthClient.unvalidateAndBindUserByExternUid(editParams);
        if (!bindResp.isSuccess() && !"U0181".equals(bindResp.getRetFlag()))
            bindResp.assertSuccess();
        Map<String, Object> bindBody = bindResp.getBody();
        if (MapUtils.isEmpty(bindBody)) {
            this.logger.info("unvalidateAndBindUserByExternUid 没有返回 body");
            throw new BusinessException(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        String linkMobile = Convert.toString(bindBody.get("linkMobile"));
        if (!linkMobile.equals(phone))
            throw new BusinessException(ConstUtil.ERROR_CODE, "该用户信息已绑定其他账号");
        //返回
        Map<String, Object> body = new HashMap<>(1);
        body.put("legalPhone", "T");
        return CommonResponse.success(body);
    }

    //支付
    public IResponse<Map> wapPay(Map<String, Object> params) throws AlipayApiException {
        //未在营业时间
        Date now = DateUtils.now();
        if (!AlipayConfig.ALLOW_PAY_SPAN.contains(now))
            throw new BusinessException(ConstUtil.ERROR_CODE, "抱歉，目前非营业时间，请稍后再试！");
        //渠道验证
        String token = this.getToken();
        if (StringUtils.isEmpty(token))
            throw new BusinessException(ConstUtil.ERROR_CODE, "无效的令牌");
        String channelNo = this.getChannelNo();
        if (!"60".equals(channelNo))
            throw new BusinessException(ConstUtil.ERROR_CODE, "只支持支付宝生活号");
        //会话验证
        Map<String, Object> sessionMap = RedisUtils.getExpireMap(token);
        if (MapUtils.isEmpty(sessionMap))
            throw new BusinessException(ConstUtil.ERROR_CODE, "无效的操作, 请重新登陆");
        String custNo = Convert.toString(sessionMap.get("custNo"));
        if (StringUtils.isEmpty(custNo))
            throw new BusinessException(ConstUtil.ERROR_CODE, "会话中[客户编号]不能为空");
        //参数验证
        String applSeq = Convert.toString(params.get("applSeq"));
        if (StringUtils.isEmpty(applSeq))
            throw new BusinessException(ConstUtil.ERROR_CODE, "[贷款申请流水号]不能为空");
        String isRetry = Convert.toString(params.get("isRetry"));//是否重试(处理中)
        String isAll = Convert.toString(params.get("isAll"));//是否全部还款

        //获取支付宝订单信息
        AlipayOrder order = "Y".equals(isRetry) ? this.getProcessingPay(applSeq) : this.applyRepay(isAll, applSeq, custNo, params);
        //支付
        String html = this.wapPayCore(order);
        this.logger.info("支付宝返回支付页面内容: " + html);
        Map<String, Object> body = new HashMap<>();
        body.put("html", html);
        return CommonResponse.success(body);
    }

    //请求支付宝发起支付
    private String wapPayCore(AlipayOrder order) throws AlipayApiException {
        String token = this.getToken();
        if (StringUtils.isEmpty(token))
            throw new BusinessException(ConstUtil.ERROR_CODE, "无效的令牌");
        String channelNo = this.getChannelNo();
        if (!"60".equals(channelNo))
            throw new BusinessException(ConstUtil.ERROR_CODE, "只支持支付宝生活号");
        order.valid();
        return AlipayUtils.wapPay(token, channelNo, order);
    }

    //申请还款
    private AlipayOrder applyRepay(String isAll, String applSeq, String custNo, Map<String, Object> params) {
        Date crtTime = DateUtils.now();//payNo 创建时间
        String repayAmt = Convert.toString(params.get("repayAmt"));
        if (StringUtils.isEmpty(repayAmt))
            throw new BusinessException(ConstUtil.ERROR_CODE, "[还款总金额]不能为空");
        String psPerdNo = Convert.toString(params.get("psPerdNo"));
        if (StringUtils.isEmpty(psPerdNo))
            throw new BusinessException(ConstUtil.ERROR_CODE, "[还款期]不能为空");
        //调用收单 还款申请
        Map<String, Object> acqParams = new HashMap<>();
        acqParams.put("applSeq", applSeq);
        if ("Y".equals(isAll)) {//全部还款
            acqParams.put("setlTyp", "01");//01：信贷还款 02：充值还款
            acqParams.put("setlMode", "FS");//FS（全部还款）NM（归还欠款）ER（提前还款）信贷还款时必传
        } else {//按期还款
            acqParams.put("setlTyp", "02");//01：信贷还款 02：充值还款
        }
        acqParams.put("repayAmt", repayAmt);//还款总金额  repayAmt  NUMBER(16,2)  是
        acqParams.put("psPerdNo", psPerdNo);//还款期  psPerdNo  VARCHAR2(200)  是  多个期号以“|”分隔。随借随还传“1”
        acqParams.put("acCardNo", AlipayConfig.REPAY_APPL_CARD_NO);//还款卡号  acCardNo  VARCHAR2(30)  是
        acqParams.put("useCoup", "N");//是否使用优惠券  useCoup  VARCHAR2(10)  是  Y：使用 N：不使用
        acqParams.put("custNo", custNo);//客户编号  custNo  VARCHAR2(30)  是
        acqParams.put("isNeedPayNo", "Y");//  是否需要支付流水号 isNeedPayNo	Varchar2 选填	Y--- N---否  默认为否仅支持信贷还款
        IAcqRequest request = AcqRequestBuilder.newBuilder("ACQ-2101")
                .sysFlag(ConstUtil.CHANNEL)
                .body(Collections.singletonMap("list", Collections.singletonList(acqParams)))
                .build();
        IResponse<Map> acqResponse = this.acquirerClient.saveZdhkInfo(request);
        acqResponse.assertSuccessNeedBody();
        Map<String, Object> acqRespBody = acqResponse.getBody();
        if (MapUtils.isEmpty(acqRespBody)) {
            this.logger.info("收单提交还款请求返回的 body 为空");
            throw new BusinessException(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        List<Map> list = (List<Map>) acqRespBody.get("list");
        if (CollectionUtils.isEmpty(list)) {
            this.logger.info("收单提交还款请求返回的 body 的 list 为空");
            throw new BusinessException(ConstUtil.ERROR_CODE, "申请还款失败");
        }
        Map<String, Object> one = list.get(0);
        if (MapUtils.isEmpty(one)) {
            this.logger.info("收单提交还款请求返回的 body 的 list 的第一个元素内容为空");
            throw new BusinessException(ConstUtil.ERROR_CODE, "申请还款失败");
        }
        String failReason = Convert.defaultString(one.get("failReason"), "申请还款失败");//失败原因
        String setlSts = Convert.toString(one.get("setlSts"));//01 处理中
        if (!"01".equals(setlSts)) {
            this.logger.info("收单返回 setlSts:" + setlSts);
            throw new BusinessException(ConstUtil.ERROR_CODE, failReason);
        }
        String payNo = Convert.toString(one.get("payNo"));
        this.logger.info("收单返回 payNo:" + payNo);
        if (StringUtils.isEmpty(payNo)) {
            this.logger.info("收单未返回 payNo");
            throw new BusinessException(ConstUtil.ERROR_CODE, failReason);
        }
        String repaySeq = Convert.toString(one.get("repaySeq"));
        this.logger.info("收单返回 repaySeq:" + repaySeq);
        if (StringUtils.isEmpty(repaySeq)) {
            this.logger.info("收单未返回 repaySeq");
            throw new BusinessException(ConstUtil.ERROR_CODE, failReason);
        }
        //repayAmt 为空说明 有处理中的
        if (StringUtils.isEmpty(repayAmt)) {
            repayAmt = Convert.toString(one.get("repayAmt"));
            if (StringUtils.isEmpty(repayAmt)) {
                this.logger.info("收单 ACQ-2202 未返回 repayAmt");
                throw new BusinessException(ConstUtil.ERROR_CODE, failReason);
            }
        }

        //返回
        AlipayOrder order = new AlipayOrder();
        order.setApplSeq(applSeq);
        order.setPayNo(payNo);
        order.setRepaySeq(repaySeq);
        order.setRepayAmt(repayAmt);
        order.setSubject(this.alipayConfig.getWapPaySubject());
        order.setTimeoutExpire(AlipayConfig.getLastPayTime(crtTime));
        return order;
    }

    //获取处理中还款
    private AlipayOrder getProcessingPay(String applSeq) {
        Map<String, Object> acqParams = new HashMap<>();
        acqParams.put("sortNo", "1");//使用 applSeq 参数
        acqParams.put("applSeq", applSeq);
        acqParams.put("setlSts", "01");//01：还款处理中 02：还款成功 03：还款失败
        acqParams.put("isNeedPayNo", "Y"); //需要 payNo
        IAcqRequest request = AcqRequestBuilder.newBuilder("ACQ-2202")
                .sysFlag(ConstUtil.CHANNEL)
                .body(Collections.singletonMap("list", Collections.singletonList(acqParams)))
                .build();
        IResponse<Map> acqResponse = this.acquirerClient.selectRepayRequestSetlSts(request);
        acqResponse.assertSuccessNeedBody();
        Map<String, Object> acqRespBody = acqResponse.getBody();
        if (MapUtils.isEmpty(acqRespBody)) {
            this.logger.info("收单提交还款请求返回的 body 为空");
            throw new BusinessException(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        List<Map> list = (List<Map>) acqRespBody.get("list");
        if (CollectionUtils.isEmpty(list)) {
            this.logger.info("收单提交还款请求返回的 body 的 list 为空");
            throw new BusinessException(ConstUtil.ERROR_CODE, "申请还款失败");
        }
        Map<String, Object> listOne = list.get(0);
        if (MapUtils.isEmpty(listOne)) {
            this.logger.info("收单提交还款请求返回的 body 的 list 的第一个元素内容为空");
            throw new BusinessException(ConstUtil.ERROR_CODE, "申请还款失败");
        }
        List<Object> rePayRecord = (List<Object>) listOne.get("rePayRecord");
        if (CollectionUtils.isEmpty(rePayRecord)) {
            this.logger.info("收单提交还款请求返回的 rePayRecord 为空");
            throw new BusinessException(ConstUtil.ERROR_CODE, "申请还款失败");
        }
        Map<String, Object> one = (Map<String, Object>) rePayRecord.get(0);
        if (MapUtils.isEmpty(one)) {
            this.logger.info("收单提交还款请求返回的 rePayRecord 的第一个元素内容为空");
            throw new BusinessException(ConstUtil.ERROR_CODE, "申请还款失败");
        }
        String failReason = Convert.defaultString(one.get("failReason"), "申请还款失败");//失败原因
        String setlSts = Convert.toString(one.get("setlSts"));//01 处理中
        if (!"01".equals(setlSts)) {
            this.logger.info("收单返回 setlSts:" + setlSts);
            throw new BusinessException(ConstUtil.ERROR_CODE, failReason);
        }
        String payNo = Convert.toString(one.get("payNo"));
        this.logger.info("收单返回 payNo:" + payNo);
        if (StringUtils.isEmpty(payNo)) {
            this.logger.info("收单未返回 payNo");
            throw new BusinessException(ConstUtil.ERROR_CODE, failReason);
        }
        String repaySeq = Convert.toString(one.get("repaySeq"));
        this.logger.info("收单返回 repaySeq:" + repaySeq);
        if (StringUtils.isEmpty(repaySeq)) {
            this.logger.info("收单未返回 repaySeq");
            throw new BusinessException(ConstUtil.ERROR_CODE, failReason);
        }
        //repayAmt 为空说明 有处理中的
        String repayAmt = Convert.toString(one.get("repayAmt"));
        if (StringUtils.isEmpty(repayAmt)) {
            this.logger.info("收单 ACQ-2202 未返回 repayAmt");
            throw new BusinessException(ConstUtil.ERROR_CODE, failReason);
        }

        String crtDt = Convert.toString(one.get("crtDt"));
        this.logger.info("收单 ACQ-2202 返回 crtDt:" + crtDt);
        Date crtTime = Convert.nullDate(crtDt);
        if (crtTime == null) {
            this.logger.info("收单 ACQ-2202 返回 crtDt 格式错误");
            throw new BusinessException(ConstUtil.ERROR_CODE, "操作失败,原订单创建时间有误");
        }

        //返回
        AlipayOrder order = new AlipayOrder();
        order.setApplSeq(applSeq);
        order.setPayNo(payNo);
        order.setRepaySeq(repaySeq);
        order.setRepayAmt(repayAmt);
        order.setSubject(this.alipayConfig.getWapPaySubject());
        order.setTimeoutExpire(AlipayConfig.getLastPayTime(crtTime));
        return order;
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
