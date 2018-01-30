package com.haiercash.payplatform.pc.alipay.fuwu.service;

import com.alipay.api.AlipayApiException;
import com.alipay.api.response.AlipayUserInfoShareResponse;
import com.haiercash.core.lang.BeanUtils;
import com.haiercash.core.lang.Convert;
import com.haiercash.core.lang.StringUtils;
import com.haiercash.core.reflect.GenericType;
import com.haiercash.payplatform.pc.alipay.bean.AlipayToken;
import com.haiercash.payplatform.pc.alipay.util.AlipayUtils;
import com.haiercash.payplatform.service.AppServerService;
import com.haiercash.payplatform.utils.AppServerUtils;
import com.haiercash.payplatform.utils.EncryptUtil;
import com.haiercash.spring.config.EurekaServer;
import com.haiercash.spring.redis.RedisUtils;
import com.haiercash.spring.rest.IResponse;
import com.haiercash.spring.rest.common.CommonResponse;
import com.haiercash.spring.rest.common.CommonRestUtils;
import com.haiercash.spring.service.BaseService;
import com.haiercash.spring.util.BusinessException;
import com.haiercash.spring.util.ConstUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    //联合登陆 auth_base 模式
    public IResponse<Map> login(String authCode) throws AlipayApiException {
        AlipayToken token = AlipayUtils.getOauthTokenByAuthCode(authCode);
        String thirdUserId = token.getUserId();//支付宝 userId
        String sysToken = UUID.randomUUID().toString();//支付平台的 token

        //根据第三方 uid 查询用户信息
        IResponse<Map> externUidResp = this.queryUserByExternUid(thirdUserId);
        if (!externUidResp.isSuccessNeedBody()) {//该用户不存在
            //保存 session
            Map<String, Object> sessionMap = new HashMap<>();
            sessionMap.put("externalUserId", thirdUserId);
            RedisUtils.setExpire(sysToken, sessionMap);
            //返回
            Map<String, Object> body = new HashMap<>();
            body.put("userState", "01");//失败 -> loading 页
            body.put("token", sysToken);
            return CommonResponse.success(body);
        }//其他
        Map<String, String> externUidBody = externUidResp.getBody();
        String userId = externUidBody.get("userId");
        String phone = externUidBody.get("mobile");

        //查询实名信息
        IResponse<Map> custInfoResp = this.queryPerCustInfo(userId);
        if (!custInfoResp.isSuccessNeedBody()) {
            //保存 session
            Map<String, Object> sessionMap = new HashMap<>();
            sessionMap.put("externalUserId", thirdUserId);
            RedisUtils.setExpire(sysToken, sessionMap);
            //返回
            Map<String, Object> body = new HashMap<>();
            body.put("userState", "02");//实名认证
            body.put("token", sysToken);
            return CommonResponse.success(body);
        }
        Map<String, String> custInfoBody = custInfoResp.getBody();

        //保存 session
        Map<String, Object> sessionMap = new HashMap<>();
        sessionMap.put("externalUserId", thirdUserId);
        sessionMap.put("userId", userId);
        sessionMap.put("phoneNo", phone);
        sessionMap.put("custNo", custInfoBody.get("custNo"));//客户编号
        sessionMap.put("name", custInfoBody.get("custName"));//客户姓名
        sessionMap.put("cardNo", custInfoBody.get("cardNo"));//银行卡号
        sessionMap.put("bankCode", custInfoBody.get("acctBankNo"));//银行代码
        sessionMap.put("bankName", custInfoBody.get("acctBankName"));//银行名称
        sessionMap.put("idNo", custInfoBody.get("certNo"));//身份证号
        sessionMap.put("idCard", custInfoBody.get("certNo"));//身份证号
        sessionMap.put("idType", custInfoBody.get("certType"));
        RedisUtils.setExpire(sysToken, sessionMap);

        //查询是否做过人脸
        Map<String, Object> ifNeedFaceParams = new HashMap<>();
        ifNeedFaceParams.put("typCde", StringUtils.EMPTY);
        ifNeedFaceParams.put("source", getChannel());
        ifNeedFaceParams.put("custNo", custInfoBody.get("custNo"));
        ifNeedFaceParams.put("name", custInfoBody.get("custName"));
        ifNeedFaceParams.put("idNumber", custInfoBody.get("certNo"));
        ifNeedFaceParams.put("isEdAppl", "Y");
        Map<String, Object> ifNeedFaceResult = appServerService.ifNeedFaceChkByTypCde(sysToken, ifNeedFaceParams);
        IResponse<Map> ifNeedFaceResp = BeanUtils.mapToBean(ifNeedFaceResult, new GenericType<CommonResponse<Map>>() {
        });
        ifNeedFaceResp.assertSuccessNeedBody();
        String faceCode = Convert.toString(ifNeedFaceResp.getBody().get("code"));
        if (!Objects.equals(faceCode, "00")) {//未做人脸
            Map<String, Object> body = new HashMap<>();
            body.put("userState", "03");//人脸
            body.put("token", sysToken);
            return CommonResponse.success(body);
        }

        //支付密码判断
        Map<String, Object> validateUserFlagMap = new HashMap<>();
        validateUserFlagMap.put("channelNo", this.getChannelNo());// 渠道
        validateUserFlagMap.put("channel", this.getChannel());
        validateUserFlagMap.put("userId", EncryptUtil.simpleEncrypt(userId));//客户编号18254561920
        Map<String, Object> alidateUserMap = appServerService.validateUserFlag(sysToken, validateUserFlagMap);
        IResponse<Map> alidateUserResp = BeanUtils.mapToBean(alidateUserMap, new GenericType<CommonResponse<Map>>() {
        });
        alidateUserResp.assertSuccessNeedBody();
        String payPasswdFlag = Convert.toString(alidateUserResp.getBody().get("payPasswdFlag"));//1:已设置支付密码
        Map<String, Object> body = new HashMap<>();
        body.put("userState", Objects.equals(payPasswdFlag, "1") ? "04" : "05");//04:转到确认支付密码,05转到设置支付密码
        body.put("edState", this.getEdState(userId));//额度状态
        body.put("token", sysToken);
        return CommonResponse.success(body);
    }

    //额度入口,点击loading 页后 支付宝 重定向到该接口
    public IResponse<Map> creditEntry(String authCode) throws AlipayApiException {
        AlipayToken token = AlipayUtils.getOauthTokenByAuthCode(authCode);
        AlipayUserInfoShareResponse alipayUserInfo = AlipayUtils.getUserInfo(token.getToken());
        if (Objects.equals(alipayUserInfo.getUserType(), "2")
                && Objects.equals(alipayUserInfo.getUserStatus(), "T")
                && Objects.equals(alipayUserInfo.getIsCertified(), "T")
                && Objects.equals(alipayUserInfo.getIsStudentCertified(), "T"))
            return CommonResponse.success();
        return CommonResponse.fail(ConstUtil.ERROR_CODE, "用户不符合准入规则");
    }

    //根据userId 查询用户的额度状态
    private String getEdState(String userId) {
        //查询额度信息
        IResponse<Map> checkEdResp = this.checkEdAppl(userId);
        checkEdResp.assertSuccessNeedBody();
        //00 待提交
        //01 审批中
        //03 已取消
        //22 审批退回
        //25 额度申请被拒
        //26 额度申请已取消
        //27 已通过
        String edFlag = checkEdResp.getRetFlag();
        switch (edFlag) {
            case "01"://审批中=已提交
            case "22"://退回
            case "25"://被拒绝
            case "27"://有额度
                return edFlag;
            default:
                throw new BusinessException(ConstUtil.ERROR_CODE, "额度状态错误");
        }
    }

    //查询第三方账号
    private IResponse<Map> queryUserByExternUid(String thirdUserId) {
        Map<String, Object> map = new HashMap<>();
        map.put("externCompanyNo", EncryptUtil.simpleEncrypt(this.getChannelNo()));
        map.put("externUid", EncryptUtil.simpleEncrypt(thirdUserId));
        String url = EurekaServer.UAUTH + "/app/uauth/queryUserByExternUid";
        return CommonRestUtils.getForMap(url, map);
    }

    //6.1.102.	(GET)额度申请校验
    private IResponse<Map> checkEdAppl(String userId) {
        Map<String, Object> params = new HashMap<>();
        params.put("channel", ConstUtil.CHANNEL);
        params.put("channelNo", this.getChannelNo());
        params.put("userId", userId);
        String url = AppServerUtils.getAppServerUrl() + "/app/appserver/validate/checkEdAppl";
        return CommonRestUtils.getForMap(url, params);
    }

    //查询实名信息
    private IResponse<Map> queryPerCustInfo(String userId) {
        Map<String, Object> params = new HashMap<>();
        params.put("channel", ConstUtil.CHANNEL);
        params.put("channelNo", this.getChannelNo());
        params.put("userId", userId);
        String url = AppServerUtils.getAppServerUrl() + "/app/appserver/crm/cust/queryPerCustInfo";
        return CommonRestUtils.getForMap(url, params);
    }

}
