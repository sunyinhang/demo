package com.haiercash.appserver.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;

/**
 * OAuth2服务配置类 Created by Liuhongbin on 2016/4/7.
 */
@Configuration
public class OAuth2ServerConfiguration {

    @Configuration
    @EnableResourceServer
    protected static class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {
        private String HC_RESOURCE_ID = "haiercash";

        @Value("${common.app.checkAuth}")
        private Boolean checkAuth;

        @Override
        public void configure(ResourceServerSecurityConfigurer resources) {
            resources.resourceId(HC_RESOURCE_ID);
        }

        @Override
        public void configure(HttpSecurity http) throws Exception {
            if (checkAuth == null || !checkAuth) {
                http.authorizeRequests().antMatchers("/**").permitAll().anyRequest().authenticated();
            } else {
                http.authorizeRequests()
                        .antMatchers("/", "/appjs/**", "/app/portal/mUser/**", "/app/uauth/**",
                                "/app/appmanage/version/**",
                                /*"/app/portal/mUser/login",
                                "/app/portal/mUser/login",
                                "/app/portal/mUser/frozen", // 商户用户冻结
                                "/app/portal/mUser/info",
                                "/pub/portal/mUser/queryByAlias",
                                "/app/uauth/frozenUsers", // 个人用户冻结
								"/app/uauth/login",
                                "/app/uauth/saveUauthUsers",
                                "/app/uauth/validateUsers",
                                "/app/uauth/validateUserFlag",*/
                                "/app/uauth/isRegister",//查询是否已经注册
                                "/app/appserver/uauth/isRegister",//查询是否已经注册
                                "/app/appserver/customerLogin",
                                "/app/appserver/verifyAndBindDeviceId",//新登录流程，确认验证码并绑定设备号
                                "/app/appserver/uauth/custUpdatePwd",//重新设置密码
                                "/app/appserver/uauth/validateUserFlag",//查询用户类型
                                "/app/appserver/uauth/saveUauthUsers",//用户注册接口
                                "/app/appserver/uauth/identify",//实名认证修改密码
                                "/app/appserver/uauth/haierCaptcha",//刷新海尔会员验证码
                                "/app/appserver/salerLogin",
                                "/app/appserver/getFaceThreshold",
                                "/app/appserver/getMessagePull",
                                "/app/appserver/smsSendVerify",//发送短信验证码
                                "/app/appserver/smsVerify",//校验短信验证码
                                "/app/appserver/appmanage/param/selectByParams",
                                "/app/appserver/cmis/getDict",
                                "/app/crm/cust/getBankList",
                                "/app/appserver/caRequest", // 签章请求
                                "/app/appserver/validate/checkIdNo",
                                "/app/crm/cust/getCustBankCardByCardNo",
                                "/app/crm/cust/getAllBankInfo",
                                "/app/appserver/ad/**",//广告及图片的浏览下载接口
                                "/pub/gm/getGoodsByCode",
                                "/pub/gm/getGoodsPic",
                                "/app/crm/cust/getBankInfo",

                                /************ cmis数据获取 **********************/
                                "/app/appserver/cmis/getBlanceByIdNo",//根据用户身份证号获取贷款余额

                                /************ App后台封装接口 **********************/
                                "/app/appserver/crm/cust/getBankList",
                                "/app/appserver/crm/cust/getCustBankCardByCardNo",
                                "/app/appserver/crm/cust/getAllBankInfo",
                                "/pub/appserver/gm/getGoodsByCode",
                                "/app/appserver/crm/cust/getBankInfo",
                                "/app/appserver/pub/gm/getGoodsByCode",
                                "/app/appserver/uauth/custVerifyUpdatePwd",

                                /*********** 以下是合同、协议展示及数据获取 start **********/
                                "/app/appserver/credit",//个人信用报告查询授权书展示
                                "/app/appserver/edCredit",//提额征信授权书展示
                                "/app/appserver/comRepayPerson",//共同还款人协议展示
                                "/app/appserver/getComRepayPersonInfo",
                                "/app/appserver/register",//注册协议展示
                                "/app/appserver/mkledsyxz",//美凯龙额度使用须知展示
                                "/app/appserver/getAgreementInfo",
                                "/app/appserver/getGrantInfo",
                                "/app/appserver/grant",//银行卡变更授权书展示
                                "/app/appserver/contract",//合同展示
                                "/app/appserver/getContractInfo",
                                "/app/appserver/contractForACQ",//合同展示(收单)
                                "/app/appserver/getContractInfoFromACQ",
                                /******************     end      *************************/

                                /******************     乔融豆子  start     *************************/
                                "/app/appserver/moxieFund",
                                "/app/appserver/moxieBank",
                                "/app/appserver/moxieCarrier",
                                "/app/appserver/getMoxieByApplseq",
                                "/app/appserver/isHaveMoxieByCode",
                                "/app/appserver/getMoxieUserId",
                                "/app/appserver/fileUpload",
                                "/app/appserver/isNeedFaceCheck",
                                "/app/appserver/getCustInfo",
                                "/app/appserver/getFaceIDResult",
                                "/app/appserver/faceReturnUrl",
                                "/app/appserver/faceNotifyUrl",
                                "/app/appserver/qrdzCheckFourKeys",
                                "/app/appserver/getLoanByApplseq",
                                "/app/appserver/encryptParam",
                                "/app/appserver/ca/loanconfirm",
                                /******************     乔融豆子  end     *************************/

                                "/app/appserver/customer/getBatchPaySs",
                                "/app/appmanage/**",//appmanager项目的接口，目前图片展示需要
                                "/app/appserver/apporderrequest/**",//支付平台的请求
                                "/app/appserver/ca/hdjr",//互动金融签章请求
                                "/app/appserver/contractData",//互动金融短信合同确认
                                "/app/appserver/ca/hdjrforward",//互动金融合同数据请求接口
                                "/app/appserver/ca/{code}/confirm",//互动金融签章确认
                                "/app/appserver/getContractInfoFromCmis",//互动金融合同页面获取信贷信息
                                "/app/appserver/caCheckFourKeys",//互动金融签章检查四要素信息

                                "/app/appserver/appInfo/queryCityRange",//查询城市范围
                                "/app/appserver/ad/getAdInfoCheck",//广告页信息

                                "/app/appserver/pub/gm/getGoodsAddImgByCode", //商品管理
                                "/app/appserver/pub/gm/getGoodsInventory",

                                "/app/appserver/appmanage/sc/SCimages", //查询商城广告页信息
                                "/pub/gm/getImgByFilepath",
                                "/app/appserver/pub/gm/queryInventoryBySkucode"
                        )
                        .permitAll().anyRequest().authenticated().and().headers().frameOptions().sameOrigin();
            }
        }
    }

    @Configuration
    @EnableAuthorizationServer
    protected static class AuthorizationServerConfiguration extends AuthorizationServerConfigurerAdapter {

        @Autowired
        private AuthenticationManager authenticationManager;

        @Autowired
        private HcClientDetailsService clientDetailsService;

        @Override
        public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
            endpoints.tokenStore(tokenStore())
                    .reuseRefreshTokens(true) //刷新token不失效
                    .authenticationManager(this.authenticationManager)
                    .pathMapping("/oauth/token", "/app/appserver/token")
                    .allowedTokenEndpointRequestMethods(HttpMethod.GET, HttpMethod.POST);
        }

        @Override
        public void configure(AuthorizationServerSecurityConfigurer oauthServer) throws Exception {
            oauthServer.allowFormAuthenticationForClients();
        }

        @Override
        public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
            clients.withClientDetails(clientDetailsService);
        }

        @Bean
        public TokenStore tokenStore() {
            return new RedisTokenStore();
        }
    }

}
