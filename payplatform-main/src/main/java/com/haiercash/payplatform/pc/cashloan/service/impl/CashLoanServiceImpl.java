package com.haiercash.payplatform.pc.cashloan.service.impl;

import com.bestvike.lang.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.haiercash.commons.redis.Session;
import com.haiercash.payplatform.common.dao.AppOrdernoTypgrpRelationDao;
import com.haiercash.payplatform.common.dao.ChannelStoreRelationDao;
import com.haiercash.payplatform.common.dao.CooperativeBusinessDao;
import com.haiercash.payplatform.common.dao.EntrySettingDao;
import com.haiercash.payplatform.common.data.AppOrder;
import com.haiercash.payplatform.common.data.AppOrdernoTypgrpRelation;
import com.haiercash.payplatform.common.data.EntrySetting;
import com.haiercash.payplatform.common.entity.ThirdTokenVerifyResult;
import com.haiercash.payplatform.pc.cashloan.service.CashLoanService;
import com.haiercash.payplatform.pc.cashloan.service.ThirdTokenVerifyService;
import com.haiercash.payplatform.pc.shunguang.service.SgInnerService;
import com.haiercash.payplatform.rest.IResponse;
import com.haiercash.payplatform.service.*;
import com.haiercash.payplatform.utils.ApplicationContextUtil;
import com.haiercash.payplatform.utils.BusinessException;
import com.haiercash.payplatform.utils.ConstUtil;
import com.haiercash.payplatform.utils.EncryptUtil;
import com.haiercash.payplatform.utils.HttpUtil;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

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

    @Autowired
    private ChannelStoreRelationDao channelStoreRelationDao;

    @Autowired
    private AppOrdernoTypgrpRelationDao appOrdernoTypgrpRelationDao;
    @Autowired
    private SgInnerService sgInnerService;
    @Autowired
    private CommonPageService commonPageService;

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
                Map<String, Object> map = new HashMap<>();
                map.put("flag", "1");
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

    /**
     * 根据channelNo 获取贷款种类,不受配置影响
     *
     * @param channelNo
     * @return 贷款种类列表
     */
    @Override
    public IResponse<List<String>> getLoanTypeByChannelNo(String channelNo) {
//        Assert.notNull(channelNo,"channelNo can not be null");
//        List<ChannelStoreRelation> relations= this.channelStoreRelationDao.selectByChanelNo(channelNo);
//        if(CollectionUtils.isEmpty(relations))
//            return CommonResponse.create(ConstUtil.ERROR_CODE,"该渠道没有配置任何门店商户");
//
//
//
//
//        for (ChannelStoreRelation relation : relations) {
//            Map params=new HashMap();
//            params.put("merchantCode",relation.getMerchantCode());
//            params.put("storeCode",relation.getStoreCode());
//
//        }
//
        return null;
    }

    /**
     * 根据 姓名,证件 获取贷款种类,不受配置影响
     *
     * @param custName 姓名
     * @param idType   证件类型 20 身份证,00 手机号
     * @param idNo     身份证或手机号
     * @return 贷款种类列表
     */
    @Override
    public IResponse<List<String>> getLoanTypeByCustInfo(String custName, String idType, String idNo) {
        return null;
    }

    /**
     * 根据配置和参数查询贷款种类
     *
     * @param setting   渠道配置可以为 null
     * @param channelNo 渠道号
     * @param custName  姓名
     * @param idType    证件类型 20 身份证,00 手机号
     * @param idNo      身份证或手机号
     * @return 贷款种类列表
     */
    @Override
    public IResponse<List<String>> getLoanType(EntrySetting setting, String channelNo, String custName, String idType, String idNo) {
        return null;
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
        Map<String, Object> ifNeedFaceChkByTypCdeMap = new HashMap<String, Object>();
        Map<String, Object> validateUserFlagMap = new HashMap<String, Object>();
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

            returnmap.put("flag", "3");//跳转OCR
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
        String tag = "SHH";
        String typCde = "";//贷款品种
        Map<String, Object> cacheedmap = new HashMap<>();
        cacheedmap.put("channel", "11");
        cacheedmap.put("channelNo", channelNo);
        cacheedmap.put("userId", uidLocal);
        Map<String, Object> mapcache = appServerService.checkEdAppl(thirdToken, cacheedmap);
        logger.info("额度申请校验接口返回数据：" + mapcache);
        if (!HttpUtil.isSuccess(mapcache)) {
            Map<String, Object> head = (Map) mapcache.get("head");
            String _retFlag_ = (String) head.get("retFlag");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        }
        Object head2 = mapcache.get("head");
        Map<String, Object> retinfo = (Map) head2;
        String retFlag_ = (String) retinfo.get("retFlag");
        String retMsg_ = (String) retinfo.get("retMsg");
        if ("00000".equals(retFlag_)) {
            Map<String, Object> headinfo = (Map) (mapcache.get("body"));
            String applType = (String) headinfo.get("applType");
            String flag = (String) headinfo.get("flag");
            //applType="2";
            String retmsg = "01";//未申请
            if ("1".equals(applType) || ("".equals(applType) && "Y".equals(flag))) {
                logger.info("没有额度申请");
                Map<String, Object> paramMap = new HashMap<String, Object>();
                paramMap.put("channelNo", channelNo);
                paramMap.put("tag", tag);//标签
                paramMap.put("businessType", "EDJH");//业务类型 现金贷：XJD   商品分期：SPFQ      额度激活：EDJH    提额：TE   额度申请：EDSQ   个人信息维护：GRXX
                paramMap.put("channel", this.getChannel());//渠道号
                paramMap.put("isOrder", "N");//是否为订单
                paramMap.put("orderNo", "");//订单编号
                paramMap.put("applSeq", "");//订单流水号
                paramMap.put("userId", uidLocal);//用户id
                paramMap.put("custNo", custNo);//用户编号
                paramMap.put("typCde", typCde);//贷款品种代码
                paramMap.put("custName", custName);//用户名称
                paramMap.put("noEduLocal", "NO");//是否校验最高学历与户口性质
                paramMap.put("idNo", certNo);//身份证号
                Map<String, Object> stringObjectMap = appServerService.checkIfMsgComplete(thirdToken, paramMap);
                if (stringObjectMap == null) {
                    return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
                }
                Map resultmapjsonMap = (Map<String, Object>) stringObjectMap.get("head");
                String resultmapFlag = (String) resultmapjsonMap.get("retFlag");
                if (!"00000".equals(resultmapFlag)) {
                    String retMsg = (String) resultmapjsonMap.get("retMsg");
                    return fail(ConstUtil.ERROR_CODE, retMsg);
                }
                Map resultmapbodyMap = (Map<String, Object>) stringObjectMap.get("body");
                String SMRZ = (String) resultmapbodyMap.get("SMRZ");//实名认证信息
                String GRJBXX = (String) resultmapbodyMap.get("GRJBXX");//个人基本信息
                String DWXX = (String) resultmapbodyMap.get("DWXX");//单位信息
                String JZXX = (String) resultmapbodyMap.get("JZXX");//居住信息
                String LXRXX = (String) resultmapbodyMap.get("LXRXX");//联系人信息
                Map BCYXMap = (Map<String, Object>) resultmapbodyMap.get("BCYX");//必传影像信息
                String BCYX = (String) BCYXMap.get("BCYX");
                if ("N".equals(SMRZ)) {
                    //没有做过实名认证，跳转实名认证页面
                    returnmap.put("flag", "3");//转实名认证页面
                } else {
                    if ("Y".equals(GRJBXX) && "Y".equals(DWXX) && "Y".equals(JZXX) && "Y".equals(LXRXX) && "Y".equals(BCYX)) {//
                        //如个人信息完整，则判断是否做过人脸识别
                        ifNeedFaceChkByTypCdeMap.put("typCde", typCde);
                        ifNeedFaceChkByTypCdeMap.put("source", getChannel());
                        ifNeedFaceChkByTypCdeMap.put("custNo", custNo);
                        ifNeedFaceChkByTypCdeMap.put("name", custName);
                        ifNeedFaceChkByTypCdeMap.put("idNumber", certNo);
                        ifNeedFaceChkByTypCdeMap.put("isEdAppl", "Y");
                        Map<String, Object> saveCustFCiCustContactMap = appServerService.ifNeedFaceChkByTypCde(thirdToken, ifNeedFaceChkByTypCdeMap);
                        if (saveCustFCiCustContactMap == null) {
                            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
                        }
                        Map saveCustFCiCustContactMapHeadMap = (Map<String, Object>) saveCustFCiCustContactMap.get("head");
                        String saveCustFCiCustContactMapHeadFlag = (String) saveCustFCiCustContactMapHeadMap.get("retFlag");
                        if (!"00000".equals(saveCustFCiCustContactMapHeadFlag)) {
                            String retMsg = (String) saveCustFCiCustContactMapHeadMap.get("retMsg");
                            return fail(ConstUtil.ERROR_CODE, retMsg);
                        }
                        Map saveCustFCiCustContactMapBodyMap = (Map<String, Object>) saveCustFCiCustContactMap.get("body");
                        String code = (String) saveCustFCiCustContactMapBodyMap.get("code");
                        if (code != null && !"".equals(code)) {
                            logger.info("*********人脸识别标识码：" + code);
                            if ("00".equals(code)) {// 00：已经通过了人脸识别（得分合格），不需要再做人脸识别
                                validateUserFlagMap.put("channelNo", channelNo);// 渠道
                                validateUserFlagMap.put("channel", getChannel());
                                validateUserFlagMap.put("userId", EncryptUtil.simpleEncrypt(uidLocal));//客户编号18254561920
                                Map<String, Object> alidateUserMap = appServerService.validateUserFlag(thirdToken, validateUserFlagMap);
                                if (alidateUserMap == null) {
                                    return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
                                }
                                Map alidateUserHeadMap = (Map<String, Object>) alidateUserMap.get("head");
                                String alidateUserHeadMapFlag = (String) alidateUserHeadMap.get("retFlag");
                                if (!"00000".equals(alidateUserHeadMapFlag)) {
                                    String retMsg = (String) alidateUserHeadMap.get("retMsg");
                                    return fail(ConstUtil.ERROR_CODE, retMsg);
                                }
                                Map alidateUserBodyMap = (Map<String, Object>) alidateUserMap.get("body");
                                String payPasswdFlag = (String) alidateUserBodyMap.get("payPasswdFlag");
                                if (payPasswdFlag == null || "".equals(payPasswdFlag)) {
                                    return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
                                }
                                if ("1".equals(payPasswdFlag)) {//1.已设置支付密码
                                    returnmap.put("flag", "4");
                                } else {//没有设置支付密码
                                    returnmap.put("flag", "5");
                                }
                            } else if ("01".equals(code)) {// 01：未通过人脸识别，剩余次数为0，不能再做人脸识别，录单终止
                                returnmap.put("flag", "6");
                            } else if ("02".equals(code)) {// 02：未通过人脸识别，剩余次数为0，不能再做人脸识别，但可以上传替代影像
                                returnmap.put("flag", "7");
                            } else {//跳转人脸识别
                                returnmap.put("flag", "8");
                            }

                        }
                    } else {
                        //个人信息不完成，跳转完善个人扩展信息页面
                        returnmap.put("flag", "9");//跳转完善个人扩展信息页面
                    }
                }
            } else if ("2".equals(applType)) {
                HashMap<String, Object> edCheckmap = new HashMap<>();
                edCheckmap.put("idNo", certNo);
                edCheckmap.put("channel", "11");
                edCheckmap.put("channelNo", channelNo);
                edCheckmap.put("idTyp", certType);
                Map<String, Object> edApplProgress = appServerService.getEdApplProgress(null, edCheckmap);//(POST)额度申请进度查询（最新的进度 根据idNo查询）
                Map<String, Object> head = (Map) edApplProgress.get("head");
                if (!"00000".equals(head.get("retFlag"))) {
                    logger.info("额度申请进度查询（最新的进度 根据idNo查询）,错误信息：" + head.get("retMsg"));
                    return fail(ConstUtil.ERROR_CODE, (String) head.get("retMsg"));
                }
                Map<String, Object> body = (Map) edApplProgress.get("body");
                Integer crdSeqInt = (Integer) body.get("applSeq");
                String crdSeq = Integer.toString(crdSeqInt);
                cachemap.put("crdSeq", crdSeq);
                this.redisSession.set(thirdToken, cachemap);
                String outSts = body.get("outSts").toString();
                if ("27".equals(outSts)) {
                    returnmap.put("flag", "12");//通过  我的额度
                } else if ("25".equals(outSts)) {
                    returnmap.put("flag", "10");// 拒绝
                } else if ("22".equals(outSts)) {
                    returnmap.put("flag", "11");// 退回
                } else {//审批中
                    returnmap.put("flag", "13");// 审批中
                }
            } else if ("".equals(flag)) {
                returnmap.put("flag", "12");//通过  我的额度
            }

        }
        returnmap.put("token", thirdToken);
        return success(returnmap);
    }

    @Override
    public Map<String, Object> commitOrder(Map<String, Object> map) throws Exception {
        logger.info("订单提交****************开始");
        String channel = (String) map.get("channel");
        String channelNo = (String) map.get("channelNo");
        String token = (String) map.get("token");
        String orderNo = (String) map.get("orderNo");
        String applSeq = (String) map.get("applSeq");
        String paypwd = (String) map.get("paypwd");
        BigDecimal longitude = new BigDecimal(0);
        BigDecimal latitude = new BigDecimal(0);
        if(!org.springframework.util.StringUtils.isEmpty(map.get("longitude"))){
            longitude = (BigDecimal)map.get("longitude");//经度
        }
        if(!org.springframework.util.StringUtils.isEmpty(map.get("latitude"))){
            latitude = (BigDecimal)map.get("latitude");//维度
        }
        String area = (String) map.get("area");//区域
        //缓存获取（放开）
        Map<String, Object> cacheMap = redisSession.get(token, Map.class);
        if (cacheMap == null || "".equals(cacheMap)) {
            logger.info("Jedis数据获取失败");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        ObjectMapper objectMapper = new ObjectMapper();
        AppOrder appOrder = null;
        String typCde = "";
        try {
            logger.info("缓存数据获取");
            appOrder = objectMapper.readValue(cacheMap.get("apporder").toString(), AppOrder.class);
            logger.info("提交订单信息appOrder:" + appOrder);
            if(appOrder == null){
                return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
            }
            typCde = appOrder.getTypCde();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //参数非空校验
        if(org.springframework.util.StringUtils.isEmpty(channel) || org.springframework.util.StringUtils.isEmpty(channelNo) || org.springframework.util.StringUtils.isEmpty(token)
                || org.springframework.util.StringUtils.isEmpty(orderNo) || org.springframework.util.StringUtils.isEmpty(applSeq)){
            logger.info("channel:" + channel + "  channelNo:" + channelNo + "   token:" + token
                    + "  orderNo:" + orderNo + "  applSeq:" + applSeq /*+ "  longitude:" + longitude + "  latitude:" + latitude + "  area:" + area*/);
            logger.info("前台获取数据有误");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        }

        String userId = cacheMap.get("userId").toString();

        //根据userId获取客户编号
        logger.info("获取客户实名信息");
        Map<String, Object> custMap = new HashMap<String, Object>();
        custMap.put("userId", userId);
        custMap.put("channel", channel);
        custMap.put("channelNo", channelNo);
        Map<String, Object> custInforesult = appServerService.queryPerCustInfo(token, custMap);
        if (!HttpUtil.isSuccess(custInforesult) ) {
            logger.info("订单提交，获取实名信息失败");
            return fail(ConstUtil.ERROR_CODE, "获取实名信息失败");
        }
        String payresultstr = com.alibaba.fastjson.JSONObject.toJSONString(custInforesult);
        com.alibaba.fastjson.JSONObject custresult = com.alibaba.fastjson.JSONObject.parseObject(payresultstr).getJSONObject("body");
        String custNo = (String) custresult.get("custNo");
        String custName = (String) custresult.get("custName");
        String certNo = (String) custresult.get("certNo");
        String mobile = (String) custresult.get("mobile");
        logger.info("订单提交，获取客户实名信息成功");

        //1.支付密码验证
        HashMap<String, Object> pwdmap = new HashMap<>();
        String userIdEncrypt = EncryptUtil.simpleEncrypt(userId);
        String payPasswdEncrypt = EncryptUtil.simpleEncrypt(paypwd);
        pwdmap.put("userId", userIdEncrypt);
        pwdmap.put("payPasswd", payPasswdEncrypt);
        pwdmap.put("channel", channel);
        pwdmap.put("channelNo", channelNo);
        Map<String, Object> resmap = appServerService.validatePayPasswd(token, pwdmap);
        if(!HttpUtil.isSuccess(resmap)){
            logger.info("订单提交，支付密码验证失败");
            return fail("error", "支付密码校验失败");
        }
        logger.info("订单提交，支付密码验证成功");

        //2.合同签订
        Map<String, Object> contractmap =  commonPageService.signContract(custName, certNo, applSeq, mobile, typCde, channelNo, token);
        if(!HttpUtil.isSuccess(contractmap)){
            logger.info("订单提交，合同签订失败");
            return contractmap;
        }
        logger.info("订单提交，合同签订成功");

        //3.影像上传
        Map<String, Object> uploadimgmap = new HashMap<String, Object>();
        uploadimgmap.put("custNo", custNo);//客户编号
        uploadimgmap.put("applSeq", applSeq);//订单号
        uploadimgmap.put("channel", channel);
        uploadimgmap.put("channelNo", channelNo);
        Map<String,Object> uploadimgresultmap = appServerService.uploadImg2CreditDep(token, uploadimgmap);
        if(!HttpUtil.isSuccess(uploadimgresultmap)){
            logger.info("订单提交，影像上传失败失败");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        }
        logger.info("订单提交，影像上传成功");


        //5.订单提交
        // 获取订单对象
        AppOrdernoTypgrpRelation relation = appOrdernoTypgrpRelationDao.selectByOrderNo(orderNo);
        if (relation == null) {
            logger.debug("订单编号为" + orderNo + "的订单不存在！");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        }
//        applSeq = relation.getApplSeq();

        //风险信息上送
        ArrayList<Map<String, Object>> arrayList = new ArrayList<>();
        ArrayList<String> listOne = new ArrayList<>();
        ArrayList<String> listTwo = new ArrayList<>();
        HashMap<String, Object> hashMap = new HashMap<String, Object>();
        HashMap<String, Object> hashMapOne = new HashMap<String, Object>();
        HashMap<String, Object> hashMapTwo = new HashMap<String, Object>();
        String longLatitude = "经度" + longitude + "维度" + latitude;
        logger.info("经维度解析前:" + longLatitude);
        String longLatitudeEncrypt = com.haiercash.commons.util.EncryptUtil.simpleEncrypt(longLatitude);
        logger.info("经维度解析后:" + longLatitudeEncrypt);
        listOne.add(longLatitudeEncrypt);
        hashMapOne.put("idNo", com.haiercash.commons.util.EncryptUtil.simpleEncrypt(certNo));
        hashMapOne.put("name", com.haiercash.commons.util.EncryptUtil.simpleEncrypt(custName));
        hashMapOne.put("mobile", com.haiercash.commons.util.EncryptUtil.simpleEncrypt(mobile));
        hashMapOne.put("dataTyp", "04");
        hashMapOne.put("source", "2");
        hashMapOne.put("applSeq", applSeq);
        hashMapOne.put("Reserved6", applSeq);
        hashMapOne.put("content", listOne);
        listTwo.add(com.haiercash.commons.util.EncryptUtil.simpleEncrypt(area));
        hashMapTwo.put("idNo", com.haiercash.commons.util.EncryptUtil.simpleEncrypt(certNo));
        hashMapTwo.put("name", com.haiercash.commons.util.EncryptUtil.simpleEncrypt(custName));
        hashMapTwo.put("mobile", com.haiercash.commons.util.EncryptUtil.simpleEncrypt(mobile));
        hashMapTwo.put("dataTyp", "A504");
        hashMapTwo.put("source", "2");
        hashMapTwo.put("applSeq", applSeq);
        hashMapTwo.put("Reserved6", applSeq);
        hashMapTwo.put("content", listTwo);
        arrayList.add(hashMapOne);
        arrayList.add(hashMapTwo);
        hashMap.put("list", arrayList);
//        hashMap.put("channel", channel);
//        hashMap.put("channelNo", channelNo);
        Map<String, Object> stringObjectMap = appServerService.updateListRiskInfo(token, hashMap);
        if (stringObjectMap == null) {
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        }
//        Map setcustTagHeadMap = (Map<String, Object>) stringObjectMap.get("head");
        Map<String, Object> setcustTagMapFlag = (Map<String, Object>) stringObjectMap.get("response");
        Map<String, Object> setcustTagHeadMap = (Map<String, Object>) setcustTagMapFlag.get("head");
        String setcustTagHeadMapFlag = (String) setcustTagHeadMap.get("retFlag");
        if (!"00000".equals(setcustTagHeadMapFlag)) {
            String retMsg = (String) setcustTagHeadMap.get("retMsg");
            return fail(ConstUtil.ERROR_CODE, retMsg);
        }

        Map<String, Object> result = commonPageService.commitAppOrder(orderNo, applSeq, "1", null, null, relation.getTypGrp());
        logger.info("订单提交,客户姓名：" + custName);
        logger.info("订单提交，返回数据：" + result);

        return result;
    }
}
