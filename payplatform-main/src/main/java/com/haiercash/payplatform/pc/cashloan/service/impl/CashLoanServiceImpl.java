package com.haiercash.payplatform.pc.cashloan.service.impl;

import com.bestvike.collection.CollectionUtils;
import com.bestvike.lang.Convert;
import com.bestvike.lang.StringUtils;
import com.bestvike.linq.IEnumerable;
import com.bestvike.linq.Linq;
import com.bestvike.reflect.GenericType;
import com.bestvike.serialization.JsonSerializer;
import com.haiercash.commons.redis.Session;
import com.haiercash.payplatform.common.dao.AppOrdernoTypgrpRelationDao;
import com.haiercash.payplatform.common.dao.ChannelStoreRelationDao;
import com.haiercash.payplatform.common.dao.EntrySettingDao;
import com.haiercash.payplatform.common.data.AppOrder;
import com.haiercash.payplatform.common.data.AppOrdernoTypgrpRelation;
import com.haiercash.payplatform.common.data.ChannelStoreRelation;
import com.haiercash.payplatform.common.data.EntrySetting;
import com.haiercash.payplatform.common.entity.LoanType;
import com.haiercash.payplatform.common.entity.LoanTypes;
import com.haiercash.payplatform.common.entity.ThirdTokenVerifyResult;
import com.haiercash.payplatform.config.CashLoanConfig;
import com.haiercash.payplatform.config.EurekaServer;
import com.haiercash.payplatform.pc.cashloan.service.CashLoanService;
import com.haiercash.payplatform.pc.cashloan.service.ThirdTokenVerifyService;
import com.haiercash.payplatform.rest.IResponse;
import com.haiercash.payplatform.rest.common.CommonResponse;
import com.haiercash.payplatform.rest.common.CommonRestUtils;
import com.haiercash.payplatform.service.AppServerService;
import com.haiercash.payplatform.service.BaseService;
import com.haiercash.payplatform.service.CommonPageService;
import com.haiercash.payplatform.utils.*;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by 许崇雷 on 2017-10-10.
 */
@Service
public class CashLoanServiceImpl extends BaseService implements CashLoanService {
    @Autowired
    private Session redisSession;
    @Autowired
    private AppServerService appServerService;
    @Autowired
    private EntrySettingDao entrySettingDao;
    @Autowired
    private ChannelStoreRelationDao channelStoreRelationDao;
    @Autowired
    private AppOrdernoTypgrpRelationDao appOrdernoTypgrpRelationDao;
    @Autowired
    private CommonPageService commonPageService;
    @Autowired
    private CashLoanConfig cashLoanConfig;

    @Override
    public String getActivityUrl() {
        String channelNo = this.getChannelNo();
        this.logger.info("开始活动跳转 channelNo:" + channelNo);
        EntrySetting setting = this.entrySettingDao.selectBychanelNo(channelNo);
        if (setting == null) {
            return StringUtils.EMPTY;
        }
        return this.cashLoanConfig.getActiveUrlPrefix() + setting.getActivityUrl();
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
    public IResponse<List<LoanType>> getLoanTypeByChannelNo(String channelNo) {
        Assert.notNull(channelNo, "channelNo can not be null");
        List<ChannelStoreRelation> relations = this.channelStoreRelationDao.selectByChanelNo(channelNo);
        if (CollectionUtils.isEmpty(relations))
            return CommonResponse.create(ConstUtil.ERROR_CODE, "该渠道没有配置任何门店商户");

        List<LoanType> loanTypeList = new ArrayList<>();
        String url = AppServerUtils.getAppServerUrl() + "/app/appserver/pub/gm/getLoanDic";
        for (ChannelStoreRelation relation : relations) {
            Map<String, String> params = new HashMap<>();
            params.put("merchantCode", relation.getMerchantCode());
            params.put("storeCode", relation.getStoreCode());
            IResponse<List<Map>> loanTypes = CommonRestUtils.getForObject(url, new GenericType<List<Map>>() {
            }, params);
            loanTypes.assertSuccessNeedBody();
            Linq.asEnumerable(loanTypes.getBody()).select(map -> {
                LoanType loanType = new LoanType();
                loanType.setTypCde(Convert.toString(map.get("loanCode")));
                return loanType;
            }).forEach(loanTypeList::add);
        }
        //贷款品种去重
        List<LoanType> distinctLoanTypes = Linq.asEnumerable(loanTypeList).distinct().toList();//去重
        CommonResponse<List<LoanType>> response = CommonResponse.success();
        response.setBody(distinctLoanTypes);
        return response;
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
    public IResponse<List<LoanType>> getLoanTypeByCustInfo(String custName, String idType, String idNo) {
        //查询标签
        String tagUrl = EurekaServer.CRM + "/app/crm/cust/getCustTag";
        Map<String, Object> params = new HashMap<>();
        params.put("custName", custName);
        params.put("idTyp", idType);
        params.put("idNo", idNo);
        IResponse<List<Map>> tagsResponse = CommonRestUtils.getForObject(tagUrl, new GenericType<List<Map>>() {
        }, params);
        tagsResponse.assertSuccessNeedBody();
        List<Map> tags = tagsResponse.getBody();
        if (CollectionUtils.isEmpty(tags))
            return CommonResponse.create(ConstUtil.ERROR_CODE, "没有任何标签");
        IEnumerable<String> userTags = Linq.asEnumerable(tags).select(tagMap -> Convert.toString(tagMap.get("tagId")));
        //标签跟配置的标签取交集
        List<String> allowTags = cashLoanConfig.getTagIds();
        if (CollectionUtils.isEmpty(allowTags))
            return CommonResponse.create(ConstUtil.ERROR_CODE, "支付平台未配置允许的标签");
        List<String> intersectTags = userTags.intersect(Linq.asEnumerable(allowTags)).toList();//取交集
        //根据标签查询贷款品种
        String loanUrl = EurekaServer.CRM + "/app/crm/cust/getLoanCodeByTagId";
        List<LoanType> loanTypeList = new ArrayList<>();
        for (String tagId : intersectTags) {
            Map<String, Object> args = new HashMap<>();
            args.put("tagId", tagId);
            IResponse<LoanTypes> loanResponse = CommonRestUtils.getForObject(loanUrl, LoanTypes.class, args);
            loanResponse.assertSuccessNeedBody();
            loanTypeList.addAll(loanResponse.getBody().getInfo());
        }
        //贷款品种去重
        List<LoanType> distinctLoanTypes = Linq.asEnumerable(loanTypeList).distinct().toList();//去重
        CommonResponse<List<LoanType>> response = CommonResponse.success();
        response.setBody(distinctLoanTypes);
        return response;
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
    public IResponse<List<LoanType>> getLoanType(EntrySetting setting, String channelNo, String custName, String idType, String idNo) {
        if (setting == null)
            setting = this.entrySettingDao.selectBychanelNo(channelNo);
        if (setting == null)
            return CommonResponse.create(ConstUtil.ERROR_CODE, "该渠道未配置贷款品种的获取方式");
        switch (setting.getLoanTypeFrom()) {
            case "01"://根据 channelNo 取
                return this.getLoanTypeByChannelNo(channelNo);
            case "02"://根据客户信息从 crm 取
                return this.getLoanTypeByCustInfo(custName, idType, idNo);
            default:
                return CommonResponse.create(ConstUtil.ERROR_CODE, "该渠道未配置贷款品种的获取方式");
        }
    }

    private Map<String, Object> joinActivityRedirect(EntrySetting setting) {
        logger.info("申请接口*******************开始");
        Map<String, Object> cachemap = new HashMap<>();
        Map<String, Object> returnmap = new HashMap<>();//返回的map
        String channelNo = this.getChannelNo();
        String thirdToken = this.getToken();
        String verifyUrl = setting.getVerifyUrlThird() + thirdToken;
        String uidLocal;//统一认证userid
        String phoneNo;//统一认证绑定手机号
        Map<String, Object> ifNeedFaceChkByTypCdeMap = new HashMap<>();
        Map<String, Object> validateUserFlagMap = new HashMap<>();
        logger.info("验证第三方 token:" + verifyUrl);
        //验证客户信息
        ThirdTokenVerifyService thirdTokenVerifyService;
        try {
            thirdTokenVerifyService = ApplicationContextUtils.getBean(setting.getVerifyUrlService(), ThirdTokenVerifyService.class);
        } catch (Exception e) {
            throw new BusinessException(ConstUtil.ERROR_CODE, "错误的 thirdTokenVerifyService 名称:'" + setting.getVerifyUrlService() + "'");
        }
        ThirdTokenVerifyResult thirdInfo = thirdTokenVerifyService.verify(setting, thirdToken);
        String userId__ = thirdInfo.getUserId();
        String phoneNo_ = thirdInfo.getPhoneNo();
        cachemap.put("uidHaier", userId__);
        cachemap.put("haieruserId", phoneNo_);
        //从后台查询用户信息
        Map<String, Object> userInfo = this.queryUserByExternUid(channelNo, userId__);
        String retFlag = HttpUtil.getReturnCode(userInfo);
        if (Objects.equals(retFlag, "00000")) {
            //集团uid已在统一认证做过绑定
            String body = userInfo.get("body").toString();
            //Map<String, Object> bodyMap = HttpUtil.json2Map(body);
            JSONObject bodyMap = new JSONObject(body);
            uidLocal = bodyMap.get("userId").toString();//统一认证内userId
            phoneNo = bodyMap.get("mobile").toString();//统一认绑定手机号
        } else if (Objects.equals(retFlag, "U0178")) {//U0157：未查到该用户的信息
            //向后台注册用户信息
            Map<String, Object> registerResult = this.saveUserByExternUid(thirdInfo.getUserId(), thirdInfo.getPhoneNo(), phoneNo_);
            String registerResultFlag = HttpUtil.getReturnCode(registerResult);
            if ("00000".equals(registerResultFlag)) {
                uidLocal = registerResult.get("body").toString();//统一认证内userId
                phoneNo = thirdInfo.getPhoneNo();//统一认绑定手机号
            } else if ("U0160".equals(registerResultFlag)) {//U0160:该用户已注册，无法注册
                this.redisSession.set(thirdToken, cachemap);
                returnmap.put("flag", "2");//跳转登陆绑定页
//                returnmap.put("token", thirdToken);
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
        Map<String, Object> bindMap = new HashMap<>();
        bindMap.put("userId", uidLocal);//内部userId
        bindMap.put("token", thirdToken);
        bindMap.put("channel", "11");
        bindMap.put("channelNo", channelNo);
        Map<String, Object> bindresult = appServerService.saveThirdPartToken(bindMap);
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
            Map<String, Object> head = (Map<String, Object>) mapcache.get("head");
            String _retFlag_ = (String) head.get("retFlag");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        }
        Object head2 = mapcache.get("head");
        Map<String, Object> retinfo = (Map<String, Object>) head2;
        String retFlag_ = (String) retinfo.get("retFlag");
        String retMsg_ = (String) retinfo.get("retMsg");
        if ("00000".equals(retFlag_)) {
            Map<String, Object> headinfo = (Map<String, Object>) (mapcache.get("body"));
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
                Map<String, Object> head = (Map<String, Object>) edApplProgress.get("head");
                if (!"00000".equals(head.get("retFlag"))) {
                    logger.info("额度申请进度查询（最新的进度 根据idNo查询）,错误信息：" + head.get("retMsg"));
                    return fail(ConstUtil.ERROR_CODE, (String) head.get("retMsg"));
                }
                Map<String, Object> body = (Map<String, Object>) edApplProgress.get("body");
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


    /**
     * 从后台查询第三方用户的信息
     *
     * @param outUserId 明文,第三方用户 id
     * @return
     */
    private Map<String, Object> queryUserInfoFromAppServer(String outUserId) {
        String response = appServerService.queryHaierUserInfo(EncryptUtil.simpleEncrypt(outUserId));
        if (StringUtils.isEmpty(response))
            throw new BusinessException(ConstUtil.ERROR_CODE, "根据集团用户ID查询用户信息失败");
        return JsonSerializer.deserializeMap(response);
    }

    /**
     * 向后台注册第三方用户
     *
     * @param outUserId 明文,第三方用户 id
     * @param phoneNo   明文,电话
     * @return
     */
    private Map<String, Object> registerUserToAppServer(String outUserId, String phoneNo) {
        Map<String, Object> param = new HashMap<>(2);
        param.put("externUid", EncryptUtil.simpleEncrypt(outUserId));
        param.put("mobile", EncryptUtil.simpleEncrypt(phoneNo));
        return appServerService.saveUauthUsersByHaier(param);
    }

    /**
     * 现金贷订单提交
     *
     * @param map
     * @return
     * @throws Exception
     */
    @Override
    public Map<String, Object> commitOrder(Map<String, Object> map) throws Exception {
        logger.info("订单提交****************开始");
        String channel = (String) map.get("channel");
        String channelNo = (String) map.get("channelNo");
        String token = (String) map.get("token");
        String orderNo = (String) map.get("orderNo");
        String applSeq = (String) map.get("applSeq");
        String paypwd = (String) map.get("paypwd");
        String typCde = (String) map.get("typCde");
        BigDecimal longitude = new BigDecimal(0);
        BigDecimal latitude = new BigDecimal(0);
        if (!org.springframework.util.StringUtils.isEmpty(map.get("longitude"))) {
            longitude = (BigDecimal) map.get("longitude");//经度
        }
        if (!org.springframework.util.StringUtils.isEmpty(map.get("latitude"))) {
            latitude = (BigDecimal) map.get("latitude");//维度
        }
        String area = (String) map.get("area");//区域

        //非空判断
        if (StringUtils.isEmpty(token) || StringUtils.isEmpty(channel) || StringUtils.isEmpty(channelNo)
                || StringUtils.isEmpty(orderNo) || StringUtils.isEmpty(applSeq) || StringUtils.isEmpty(paypwd)
                || StringUtils.isEmpty(typCde)) {
            logger.info("token:" + token + "  channel:" + channel + "   channelNo:" + channelNo
                    + "   applSeq:" + applSeq + "   paypwd" + paypwd + "   typCde:" + typCde
                    + "  orderNo:" + orderNo);
            logger.info("前台获取数据有误");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        }

        //缓存获取（放开）
        Map<String, Object> cacheMap = redisSession.get(token, Map.class);
        if (cacheMap == null || "".equals(cacheMap)) {
            logger.info("Jedis数据获取失败");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }


        //参数非空校验
        if (StringUtils.isEmpty(channel) || StringUtils.isEmpty(channelNo) || StringUtils.isEmpty(token)
                || StringUtils.isEmpty(applSeq) || StringUtils.isEmpty(orderNo)) {
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
        if (!HttpUtil.isSuccess(custInforesult)) {
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
        if (!HttpUtil.isSuccess(resmap)) {
            logger.info("订单提交，支付密码验证失败");
            return fail("error", "支付密码校验失败");
        }
        logger.info("订单提交，支付密码验证成功");

        //2.合同签订  （TODO!!!!）
//        Map<String, Object> contractmap = commonPageService.signContract(custName, certNo, applSeq, mobile, typCde, channelNo, token);
//        if (!HttpUtil.isSuccess(contractmap)) {
//            logger.info("订单提交，合同签订失败");
//            return contractmap;
//        }
//        logger.info("订单提交，合同签订成功");

        //3.影像上传
        Map<String, Object> uploadimgmap = new HashMap<String, Object>();
        uploadimgmap.put("custNo", custNo);//客户编号
        uploadimgmap.put("applSeq", applSeq);//订单号
        uploadimgmap.put("channel", channel);
        uploadimgmap.put("channelNo", channelNo);
        Map<String, Object> uploadimgresultmap = appServerService.uploadImg2CreditDep(token, uploadimgmap);
        if (!HttpUtil.isSuccess(uploadimgresultmap)) {
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

    /**
     * 现金贷订单保存
     *
     * @param map
     * @return
     */
    @Override
    public Map<String, Object> saveOrder(Map<String, Object> map) {
        logger.info("现金贷订单保存****************开始");
        //前端传入参数获取(放开)
        String token = (String) map.get("token");
        String channel = (String) map.get("channel");
        String channelNo = (String) map.get("channelNo");
        String applyTnr = (String) map.get("applyTnr");//借款期限
        String applyTnrTyp = (String) map.get("applyTnrTyp");//期限类型（若天则传D）
        String updflag = (String) map.get("flag");//1.待提交返显
        String orderNo = (String) map.get("orderNo");//待提交时必传
        String areaCode = (String) map.get("areaCode");//区编码
        String applyAmt = Convert.toString(map.get("applyAmt"));//申请金额
        String typCde = (String) map.get("typCde");//贷款品种
        String purpose = (String) map.get("purpose");//贷款用途
        String applCardNo = (String) map.get("applCardNo");//放款卡号
        String repayApplCardNo = (String) map.get("repayApplCardNo");//还款卡号

        //非空判断
        if (StringUtils.isEmpty(token) || StringUtils.isEmpty(channel) || StringUtils.isEmpty(channelNo)
                || StringUtils.isEmpty(applyTnr) || StringUtils.isEmpty(applyTnrTyp) || StringUtils.isEmpty(applyAmt)
                || StringUtils.isEmpty(typCde) || StringUtils.isEmpty(purpose) || StringUtils.isEmpty(applCardNo)
                || StringUtils.isEmpty(repayApplCardNo)) {
            logger.info("token:" + token + "  channel:" + channel + "   channelNo:" + channelNo
                    + "   applyTnr:" + applyTnr + "   applyTnrTyp" + applyTnrTyp + "   updflag:" + updflag
                    + "  orderNo:" + orderNo + "   applyAmt:" + applyAmt + "   typcde:" + typCde
                    + "  purpose:" + purpose + "  applCardNo:" + applCardNo + "   repayApplCardNo:" + repayApplCardNo);
            logger.info("前台获取数据有误");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        }

        //appOrder缓存获取（放开）
        Map<String, Object> cacheMap = redisSession.get(token, Map.class);
        if (cacheMap == null || "".equals(cacheMap)) {
            logger.info("Jedis数据获取失败");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        String userId = (String) cacheMap.get("userId");
        //获取客户信息
        logger.info("订单保存，根据userId获取客户信息");
        Map<String, Object> custMap = new HashMap<String, Object>();
        custMap.put("userId", userId);
        custMap.put("channel", channel);
        custMap.put("channelNo", channelNo);
        Map<String, Object> custInforesult = appServerService.queryPerCustInfo(token, custMap);
        if (!HttpUtil.isSuccess(custInforesult)) {
            return fail(ConstUtil.ERROR_CODE, "获取实名信息失败");
        }
        String payresultstr = com.alibaba.fastjson.JSONObject.toJSONString(custInforesult);
        com.alibaba.fastjson.JSONObject custresult = com.alibaba.fastjson.JSONObject.parseObject(payresultstr).getJSONObject("body");
        String custName = (String) custresult.get("custName");
        String custNo = (String) custresult.get("custNo");
        logger.info("客户编号：" + custNo + "   客户姓名：" + custName);
        String certNo = (String) custresult.get("certNo");
        String mobile = (String) custresult.get("mobile");

        //获取订单金额  总利息 金额
        logger.info("订单保存，获取订单金额，总利息金额");
        //IResponse<List<LoanType>> IResponse= this.getLoanType(null, channelNo, custName, "20", certNo);
        Map<String, Object> payMap = new HashMap<String, Object>();
        payMap.put("typCde", typCde);
        payMap.put("apprvAmt", applyAmt);
        payMap.put("applyTnrTyp", applyTnrTyp);
        payMap.put("applyTnr", applyTnr);
        payMap.put("channel", channel);
        payMap.put("channelNo", channelNo);
        Map<String, Object> payresultMap = appServerService.getPaySs(token, payMap);
        if (!HttpUtil.isSuccess(payresultMap)) {//额度校验失败
            String retmsg = (String) ((Map<String, Object>) (payresultMap.get("head"))).get("retMsg");
            return fail(ConstUtil.ERROR_CODE, retmsg);
        }
        String payresult = com.alibaba.fastjson.JSONObject.toJSONString(payresultMap);
        com.alibaba.fastjson.JSONObject payBody = com.alibaba.fastjson.JSONObject.parseObject(payresult).getJSONObject("body");
        logger.info("payBody:" + payBody);
        String totalAmt = payBody.get("totalAmt").toString();
        String totalNormInt = payBody.get("totalNormInt").toString();//订单保存（totalNormInt）
        String totalFeeAmt = payBody.get("totalFeeAmt").toString();//订单保存总利息金额（totalAmt）

        //获取订单信息
        logger.info("订单保存，获取订单信息");

        List<ChannelStoreRelation> relations = this.channelStoreRelationDao.selectByChanelNo(channelNo);
        if (CollectionUtils.isEmpty(relations))
            return fail(ConstUtil.ERROR_CODE, "该渠道没有配置任何门店商户");

        String merchNo = "";
        String cooprCde = "";
        String crtUsr = "";
        for (ChannelStoreRelation relation : relations) {
            merchNo = relation.getMerchantCode();
            cooprCde = relation.getStoreCode();
            crtUsr = relation.getSalerName();
        }

        if (StringUtils.isEmpty(merchNo) || StringUtils.isEmpty(cooprCde) || StringUtils.isEmpty(crtUsr)) {
            logger.info("merchNo:" + merchNo + "  cooprCde:" + cooprCde + "   crtUsr:" + crtUsr);
            return fail(ConstUtil.ERROR_CODE, "请配置商户门店销售代表信息");
        }

        AppOrder appOrder = new AppOrder();
        appOrder.setVersion("1");//接口版本号  固定传’1’
        appOrder.setSource("11");//订单来源
        appOrder.setChannelNo(channelNo);//渠道编号
        appOrder.setApplyTnr(applyTnr);//借款期限
        appOrder.setApplyTnrTyp(applyTnrTyp);//借款期限类型
        appOrder.setTotalnormint(totalNormInt);//总利息金额
        appOrder.setTotalfeeamt(totalFeeAmt);//费用总额
        appOrder.setMerchNo(merchNo);//商户编号
        appOrder.setCooprCde(cooprCde);//门店编号
        appOrder.setCrtUsr(crtUsr);//销售代表用户ID
        appOrder.setTypGrp("02");//贷款类型  01:商品贷  02  现金贷
        appOrder.setSource(ConstUtil.SOURCE);//订单来源
        //appOrder.setFormType("10");// 商品贷独有 10:线下订单   20:线上订单
        appOrder.setCustNo(custNo);//客户编号
        appOrder.setCustName(custName);//客户姓名
        appOrder.setIdTyp("20");//证件类型
        appOrder.setIdNo(certNo);//客户证件号码
        appOrder.setUserId(userId);//录单用户ID
        appOrder.setChannelNo(channelNo);
        appOrder.setPurpose(purpose);//贷款用途
        appOrder.setApplCardNo(applCardNo);// 放款卡号
        appOrder.setRepayApplCardNo(repayApplCardNo);// 还款卡号
        appOrder.setTypCde(typCde);//贷款品种
        appOrder.setApplyAmt(applyAmt);//借款总额);//借款总额
//        appOrder.setFstPct("0.00");//首付比例
//        appOrder.setFstPay("0");//首付金额

        //
        if ("1".equals(updflag)) {//待提交
            appOrder.setOrderNo(orderNo);
        } else {//新增
            appOrder.setOrderNo("");
        }

        //0.准入资格校验
        logger.info("进行准入资格校验");
        Map<String, Object> ispassmap = new HashMap<String, Object>();
        ispassmap.put("custName", custName);//姓名
        ispassmap.put("certNo", certNo);//身份证
        ispassmap.put("phonenumber", mobile);//手机号
        ispassmap.put("userId", userId);//登录用户名
        ispassmap.put("channel", channel);
        ispassmap.put("channelNo", channelNo);
        Map<String, Object> ispassresult = appServerService.getCustIsPass(token, ispassmap);
        if (!HttpUtil.isSuccess(ispassresult)) {//准入资格校验失败
            String retmsg = (String) ((Map<String, Object>) (ispassresult.get("head"))).get("retMsg");
            return fail(ConstUtil.ERROR_CODE, retmsg);
        }
        String isPass = (String) ((Map<String, Object>) (ispassresult.get("body"))).get("isPass");
        if ("-1".equals(isPass)) {
            return fail(ConstUtil.ERROR_CODE, "没有准入资格");
        }

        if ("SHH".equals(isPass.toUpperCase())) {
            appOrder.setWhiteType("SHH");//白名单类型
        } else {
            String level = (String) ((Map<String, Object>) (ispassresult.get("body"))).get("level");
            appOrder.setWhiteType(level);
        }

        //1.录单校验（所在城市开通服务）
        //获取市代码
        String cityCode = "";
        String provinceCode = "";
        if (org.springframework.util.StringUtils.isEmpty(areaCode)) {
            cityCode = "370000";
            provinceCode = "370200";
        } else {
            logger.info("获取业务发生地省市区");
            Map<String, Object> citymap = new HashMap<String, Object>();
            citymap.put("areaCode", areaCode);
            citymap.put("flag", "parent");
            citymap.put("channel", channel);
            citymap.put("channelNo", channelNo);
            cityCode = commonPageService.getCode(token, citymap);
            if (org.springframework.util.StringUtils.isEmpty(cityCode)) {
                logger.info("获取市编码失败");
                return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
            }
            //获取省代码
            Map<String, Object> provincemap = new HashMap<String, Object>();
            provincemap.put("areaCode", cityCode);
            provincemap.put("flag", "parent");
            provincemap.put("channel", channel);
            provincemap.put("channelNo", channelNo);
            provinceCode = commonPageService.getCode(token, provincemap);
            if (org.springframework.util.StringUtils.isEmpty(provinceCode)) {
                logger.info("获取省编码失败");
                return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
            }
        }
        //录单校验
        logger.info("进行录单校验");
        Map<String, Object> ordercheakmap = new HashMap<String, Object>();
        ordercheakmap.put("userId", userId);
        ordercheakmap.put("provinceCode", provinceCode);
        ordercheakmap.put("cityCode", cityCode);
        ordercheakmap.put("channel", channel);
        ordercheakmap.put("channelNo", channelNo);
        Map<String, Object> ordercheakresult = appServerService.getCustInfoAndEdInfoPerson(token, ordercheakmap);
        if (!HttpUtil.isSuccess(ordercheakresult)) {//录单校验失败
            String retmsg = (String) ((Map<String, Object>) (ordercheakresult.get("head"))).get("retMsg");
            return fail(ConstUtil.ERROR_CODE, retmsg);
        }

        //2.是否允许申请贷款
        logger.info("查看是否允许申请贷款");
//        String typCde = appOrder.getTypCde();
        SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd");
        String date = dateFormater.format(new Date());
        Map<String, Object> queryordermap = new HashMap<String, Object>();
        queryordermap.put("typCde", typCde);
        queryordermap.put("date", date);
        queryordermap.put("channel", channel);
        queryordermap.put("channelNo", channelNo);
        Map<String, Object> queryorderresult = appServerService.queryBeyondContral(token, queryordermap);
        if (!HttpUtil.isSuccess(queryorderresult)) {//是否允许申请贷款失败
            String retmsg = (String) ((Map<String, Object>) (queryorderresult.get("head"))).get("retMsg");
            return fail(ConstUtil.ERROR_CODE, retmsg);
        }
        String flag = (String) ((Map<String, Object>) (queryorderresult.get("body"))).get("flag");
        if (!"Y".equals(flag)) {
            return fail(ConstUtil.ERROR_CODE, "不允许申请贷款");
        }

        //3.订单保存
        Map<String, Object> ordermap = commonPageService.saveAppOrderInfo(appOrder);
        cacheMap.put("ordermap", ordermap);
        cacheMap.put("custName", custName);
        cacheMap.put("custNo", custNo);
        cacheMap.put("certNo", certNo);
        redisSession.set(token, cacheMap);
        logger.info("订单保存结果：" + ordermap.toString());
        if (!HttpUtil.isSuccess(ordermap)) {//订单保存失败
            logger.info("订单保存失败");
            Map resultHead = (Map<String, Object>) (ordermap.get("head"));
            String retmsg = resultHead.get("retMsg").toString();
            //String retmsg = resultHead.getRetMsg();
            //String retmsg = (String) ((Map<String, Object>)(ordermap.get("head"))).get("retMsg");
            return fail(ConstUtil.ERROR_CODE, retmsg);
        }

        return ordermap;
    }

    private Map<String, Object> queryUserByExternUid(String externCompanyNo, String externUid) {
        Map<String, Object> map = new HashMap<>();
        String externCompanyNo_ = EncryptUtil.simpleEncrypt(externCompanyNo);
        String externUid_ = EncryptUtil.simpleEncrypt(externUid);
        map.put("externCompanyNo", externCompanyNo_);
        map.put("externUid", externUid_);
        Map<String, Object> stringObjectMap = appServerService.queryUserByExternUid(this.getToken(), map);
//        String response = appServerService.queryHaierUserInfo(EncryptUtil.simpleEncrypt(outUserId));
        if (stringObjectMap == null)
            throw new BusinessException(ConstUtil.ERROR_CODE, "根据第三方（非海尔集团）id查询用户信息失败");
        return stringObjectMap;
    }


    private Map<String, Object> saveUserByExternUid(String externCompanyNo, String externUid, String linkMobile) {
        Map<String, Object> map = new HashMap<>();
        String externCompanyNo_ = EncryptUtil.simpleEncrypt(externCompanyNo);
        String externUid_ = EncryptUtil.simpleEncrypt(externUid);
        String linkMobile_ = EncryptUtil.simpleEncrypt(linkMobile);
        map.put("externCompanyNo", externCompanyNo_);
        map.put("externUid", externUid_);
        map.put("linkMobile", linkMobile_);
        Map<String, Object> stringObjectMap = appServerService.saveUserByExternUid(this.getToken(), map);
        if (stringObjectMap == null)
            throw new BusinessException(ConstUtil.ERROR_CODE, "根据第三方（非海尔集团）id查询用户信息失败");
        return stringObjectMap;
    }

    @Override
    public Map<String, Object> validateAndBindUserByExternUid(Map<String, Object> map) {
        Map<String, Object> stringObjectMap = appServerService.validateAndBindUserByExternUid(this.getToken(), map);
        if (stringObjectMap == null)
            throw new BusinessException(ConstUtil.ERROR_CODE, "验证并绑定第三方（非海尔集团）用户失败");
        return stringObjectMap;
    }

}
