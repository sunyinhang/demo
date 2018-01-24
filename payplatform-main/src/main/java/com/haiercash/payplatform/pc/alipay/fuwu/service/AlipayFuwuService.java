package com.haiercash.payplatform.pc.alipay.fuwu.service;

import com.alipay.api.AlipayApiException;
import com.alipay.api.response.AlipayUserInfoShareResponse;
import com.haiercash.payplatform.pc.alipay.bean.AlipayToken;
import com.haiercash.payplatform.pc.alipay.util.AlipayUtils;
import com.haiercash.payplatform.utils.AppServerUtils;
import com.haiercash.payplatform.utils.EncryptUtil;
import com.haiercash.spring.config.EurekaServer;
import com.haiercash.spring.redis.RedisUtils;
import com.haiercash.spring.rest.IResponse;
import com.haiercash.spring.rest.common.CommonResponse;
import com.haiercash.spring.rest.common.CommonRestUtils;
import com.haiercash.spring.service.BaseService;
import com.haiercash.spring.util.ConstUtil;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by 许崇雷 on 2018-01-18.
 */
@Service
public class AlipayFuwuService extends BaseService {
    //联合登陆 auth_base 模式
    public IResponse<Map> login(String authCode) throws AlipayApiException {
        String channelNo = getChannelNo();
        AlipayToken token = AlipayUtils.getOauthTokenByAuthCode(authCode);
        String thirdUserId = token.getUserId();
        String makeToken = channelNo + "-" + thirdUserId;//自定义 token:渠道号+第三方 userId

        //查询第三方账号信息
        IResponse<Map> externUidResp = this.queryUserByExternUid(thirdUserId);
        if (!externUidResp.isSuccessNeedBody()) {
            Map<String, Object> body = new HashMap<>();
            body.put("flag", "01");//失败 -> loading 页
            body.put("token", makeToken);
            return CommonResponse.success(body);
        }
        Map<String, String> externUidBody = externUidResp.getBody();
        String userId = externUidBody.get("userId");
        String phone = externUidBody.get("mobile");

        //查询实名信息
        IResponse<Map> custInfoResp = this.queryPerCustInfo(userId);
        if (!custInfoResp.isSuccessNeedBody()) {
            Map<String, Object> body = new HashMap<>();
            body.put("flag", "02");//资料录入
            body.put("token", makeToken);
            return CommonResponse.success(body);
        }
        Map<String, String> custInfoBody = custInfoResp.getBody();

        //将个人资料存入 redis
        Map<String, Object> cachemap = new HashMap<>();
        cachemap.put("userId", userId);
        cachemap.put("phoneNo", phone);
        cachemap.put("custNo", custInfoBody.get("custNo"));//客户编号
        cachemap.put("name", custInfoBody.get("custName"));//客户姓名
        cachemap.put("cardNo", custInfoBody.get("cardNo"));//银行卡号
        cachemap.put("bankCode", custInfoBody.get("acctBankNo"));//银行代码
        cachemap.put("bankName", custInfoBody.get("acctBankName"));//银行名称
        cachemap.put("idNo", custInfoBody.get("certNo"));//身份证号
        cachemap.put("idCard", custInfoBody.get("certNo"));//身份证号
        cachemap.put("idType", custInfoBody.get("certType"));
        RedisUtils.setExpire(makeToken, cachemap);

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
        switch (checkEdResp.getRetFlag()) {
            case "01": {//审批中=已提交
                Map<String, Object> bodyRet = new HashMap<>();
                bodyRet.put("flag", "10");
                bodyRet.put("token", makeToken);
                return CommonResponse.success(bodyRet);
            }
            case "22": {//退回
                Map<String, Object> bodyRet = new HashMap<>();
                bodyRet.put("flag", "11");
                bodyRet.put("token", makeToken);
                return CommonResponse.success(bodyRet);
            }
            case "25": {//被拒绝
                Map<String, Object> bodyRet = new HashMap<>();
                bodyRet.put("flag", "12");
                bodyRet.put("token", makeToken);
                return CommonResponse.success(bodyRet);
            }
            case "27": {//有额度
                Map<String, Object> bodyRet = new HashMap<>();
                bodyRet.put("flag", "13");
                bodyRet.put("token", makeToken);
                return CommonResponse.success(bodyRet);
            }
            default:
                return CommonResponse.fail(ConstUtil.ERROR_CODE, "额度状态错误");
        }
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
