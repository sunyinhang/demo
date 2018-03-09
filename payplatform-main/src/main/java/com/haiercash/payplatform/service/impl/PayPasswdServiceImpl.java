package com.haiercash.payplatform.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.haiercash.core.collection.MapUtils;
import com.haiercash.core.lang.Convert;
import com.haiercash.payplatform.config.CashloanConfig;
import com.haiercash.payplatform.config.OutreachConfig;
import com.haiercash.payplatform.service.AcquirerService;
import com.haiercash.payplatform.service.AppServerService;
import com.haiercash.payplatform.service.CrmManageService;
import com.haiercash.payplatform.service.CrmService;
import com.haiercash.payplatform.service.OutreachService;
import com.haiercash.payplatform.service.PayPasswdService;
import com.haiercash.payplatform.utils.AcqUtil;
import com.haiercash.payplatform.utils.CmisUtil;
import com.haiercash.payplatform.utils.EncryptUtil;
import com.haiercash.spring.eureka.EurekaServer;
import com.haiercash.spring.redis.RedisUtils;
import com.haiercash.spring.rest.common.CommonRestUtils;
import com.haiercash.spring.service.BaseService;
import com.haiercash.spring.util.ConstUtil;
import com.haiercash.spring.util.HttpUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by Administrator on 2017/8/7.
 */
@Service
public class PayPasswdServiceImpl extends BaseService implements PayPasswdService {
    public final Log logger = LogFactory.getLog(getClass());
    @Autowired
    private AppServerService appServerService;
    @Autowired
    private AcquirerService acquirerService;
    @Autowired
    private OutreachService outreachService;
    @Autowired
    private OutreachConfig outreachConfig;
    @Autowired
    private CrmManageService crmManageService;
    @Autowired
    private CashloanConfig cashloanConfig;
    @Autowired
    private CrmService crmService;

    public Map<String, Object> resetPayPasswd(String token, String channelNo, String channel, Map<String, Object> param) {
        logger.info("查询******额度提交接口******开始");
        String retflag;
        String retmsg;
        String payPasswd = (String) param.get("payPasswd");//密码
        String verifyNo = (String) param.get("verifyNo");//验证码
        String edxgflag = (String) param.get("edxgflag");//修改申请提交标识
        BigDecimal longitude;
        BigDecimal latitude;
        String area;
        if (!StringUtils.isEmpty(param.get("longitude")) && !StringUtils.isEmpty(param.get("latitude")) && !StringUtils.isEmpty(param.get("area"))) {
            longitude = (BigDecimal) param.get("longitude");//经度
            latitude = (BigDecimal) param.get("latitude");//维度
            area = (String) param.get("area");//区域
        } else {
            return fail(ConstUtil.ERROR_CODE, "获取地理位置失败!");
        }
        if (StringUtils.isEmpty(token)) {
            logger.info("token:" + token);
            logger.info("从前端获取的的token为空");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        if ("46".equals(channelNo)) {
            if (StringUtils.isEmpty(payPasswd) || StringUtils.isEmpty(verifyNo)) {
                logger.info("payPasswd:" + payPasswd + "verifyNo" + verifyNo);
                logger.info("从前端获取的参数为空");
                return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
            }
        } else {
            if (StringUtils.isEmpty(payPasswd)) {
                logger.info("payPasswd:" + payPasswd);
                logger.info("从前端获取的参数为空");
                return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
            }
        }

        Map<String, Object> cacheMap = RedisUtils.getExpireMap(token);
        if (MapUtils.isEmpty(cacheMap)) {
            logger.info("Jedis获取失败");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        String certNo = (String) cacheMap.get("idNo");//身份证号
        String name = (String) cacheMap.get("name");//姓名
        String userId = (String) cacheMap.get("userId");
        logger.info("获取的userId为：" + userId);
        //String userId = "18325423979";
        String phoneNo = (String) cacheMap.get("phoneNo");//手机号
        String custNo = (String) cacheMap.get("custNo");// 客户号
        logger.info("获取的客户号：" + custNo);
        String crdSeq = (String) cacheMap.get("crdSeq");//在途的申请流水号

        Map<String, Object> validateUserFlagMap = new HashMap<>();
        validateUserFlagMap.put("channelNo", channelNo);// 渠道
        validateUserFlagMap.put("channel", channel);
        validateUserFlagMap.put("userId", EncryptUtil.simpleEncrypt(userId));//客户编号
        Map<String, Object> alidateUserMap = appServerService.validateUserFlag(token, validateUserFlagMap);
        if (alidateUserMap == null) {
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        Map alidateUserHeadMap = (Map<String, Object>) alidateUserMap.get("head");
        String alidateUserHeadMapFlag = (String) alidateUserHeadMap.get("retFlag");
        if (!"00000".equals(alidateUserHeadMapFlag)) {
            String retMsg = (String) alidateUserHeadMap.get("retMsg");
            return fail(ConstUtil.ERROR_CODE, retMsg);
        }
        Map alidateUserBodyMap = (Map<String, Object>) alidateUserMap.get("body");
        String flag = (String) alidateUserBodyMap.get("payPasswdFlag");
        logger.info("密码设置标识：flag" + flag);
        switch (flag) {
            case "0": {//0  密码未设置
                logger.info("支付密码未设置，进行密码的设置");
                Map<String, Object> paramsMap = new HashMap<>();
                paramsMap.put("userId", EncryptUtil.simpleEncrypt(userId));
                paramsMap.put("payPasswd", EncryptUtil.simpleEncrypt(payPasswd));
                paramsMap.put("channel", channel);
                paramsMap.put("channelNo", channelNo);
                //paramsMap.put("access_token", token);
                Map<String, Object> map = appServerService.resetPayPasswd(token, paramsMap);
                if (StringUtils.isEmpty(map)) {
                    logger.info("设置支付密码失败，app后台返回数据为空");
                    String resultMsg = "设置支付密码失败，app后台返回数据为空";
                    return fail(ConstUtil.ERROR_CODE, resultMsg);
                }
                String result = JSONObject.toJSONString(map);
                JSONObject jsonObject = JSONObject.parseObject(result);
                JSONObject resultHead = jsonObject.getJSONObject("head");
                String retFlag = resultHead.getString("retFlag");
                String retMsg = resultHead.getString("retMsg");
                if (!"00000".equals(retFlag)) {
                    logger.info("设置支付密码失败" + retMsg);
                    return fail(retFlag, retMsg);
                }
                break;
            }
            case "1": {// 支付密码验证
                HashMap<String, Object> map = new HashMap<>();
                String userIdEncrypt = EncryptUtil.simpleEncrypt(userId);
                String payPasswdEncrypt = EncryptUtil.simpleEncrypt(payPasswd);
                map.put("userId", userIdEncrypt);
                map.put("payPasswd", payPasswdEncrypt);
                map.put("channel", channel);
                map.put("channelNo", channelNo);
                Map<String, Object> validatamap = appServerService.validatePayPasswd(token, map);// 验证支付密码

                if (StringUtils.isEmpty(validatamap)) {
                    logger.info("支付密码验证失败,app后台返回为空result" + validatamap);
                    return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
                }
                String result = JSONObject.toJSONString(validatamap);
                JSONObject jsonObject = JSONObject.parseObject(result);
                JSONObject resultHead = jsonObject.getJSONObject("head");
                retflag = resultHead.getString("retFlag");
                retmsg = resultHead.getString("retMsg");
                if (!"00000".equals(retflag)) {
                    logger.info("支付密码验证失败！" + retmsg);
                    return fail(ConstUtil.ERROR_CODE, "支付密码验证失败");
                }
                break;
            }
            default:
                logger.info("是否设置过支付密码标志无效");
                return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_PARAM_INVALID_MSG);
        }
        Map<String, Object> mapEd = new HashMap();
        mapEd.put("token", token);
        mapEd.put("channel", channel);
        mapEd.put("channelNo", channelNo);
        mapEd.put("custNo", custNo);
        mapEd.put("verifyMobile", phoneNo);//手机号
        mapEd.put("verifyNo", verifyNo);//验证码
        if ("1".equals(edxgflag)) {//有在途的流水号(修改)
            mapEd.put("flag", "2");
            mapEd.put("applSeq", crdSeq);
        } else {//新增
            mapEd.put("flag", "0");//额度申请
        }
        if ("59".equals(channelNo)) {
            //2.调用crm   getCustTag
            Map<String, Object> gettigIDMap = new HashMap<String, Object>();
            gettigIDMap.put("custName", name);
            gettigIDMap.put("idTyp", "20");
            gettigIDMap.put("idNo", certNo);
            Map<String, Object> custTag = crmManageService.getCustTag(token, gettigIDMap);
            if (custTag == null) {
                return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
            }
            Map custTagHeadMap = (Map<String, Object>) custTag.get("head");
            String custTagHeadMapHeadFlag = (String) custTagHeadMap.get("retFlag");
            if (!"00000".equals(custTagHeadMapHeadFlag)) {
                String retMsg2 = (String) custTagHeadMap.get("retMsg");
                return fail(ConstUtil.ERROR_CODE, retMsg2);
            }
            List<Map<String, Object>> tiglist = (List<Map<String, Object>>) custTag.get("body");
            String tagId = cashloanConfig.getIserviceTagId();
            boolean flag2 = false;
            for (Map<String, Object> aTiglist : tiglist) {
                String tagIdData = (String) aTiglist.get("tagId");
                if (tagId.equals(tagIdData)) {
                    flag2 = true;
                    break;
                }
            }
            //3.调用crm  setCustTag
            if (!flag2) {
                Map<String, Object> settigIDMap = new HashMap<>();
                settigIDMap.put("certNo", certNo);
                settigIDMap.put("tagId", tagId);
                Map<String, Object> setcustTag = crmManageService.setCustTag(token, settigIDMap);
                if (setcustTag == null) {
                    return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
                }
                Map setcustTagHeadMap = (Map<String, Object>) custTag.get("head");
                String setcustTagHeadMapFlag = (String) setcustTagHeadMap.get("retFlag");
                if (!"00000".equals(setcustTagHeadMapFlag)) {
                    String retMsg3 = (String) custTagHeadMap.get("retMsg");
                    return fail(ConstUtil.ERROR_CODE, retMsg3);
                }
            }

        }
        Map<String, Object> edApplInfo = appServerService.getEdApplInfo(token, mapEd);
        if (StringUtils.isEmpty(edApplInfo)) {
            logger.info("额度申请,app后台返回为空result" + edApplInfo);
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        String resultEd = JSONObject.toJSONString(edApplInfo);
        JSONObject jb = JSONObject.parseObject(resultEd);
        JSONObject head = jb.getJSONObject("head");
        retflag = head.getString("retFlag");
        retmsg = head.getString("retMsg");
        if (!"00000".equals(retflag)) {
            logger.info("额度申请出现异常！" + retmsg);
            return fail(ConstUtil.ERROR_CODE, retmsg);
        }

        JSONObject body = jb.getJSONObject("body");
        String applSeq = body.getString("applSeq");
        //上传风险数据 经纬度
        ArrayList<Map<String, Object>> arrayList = new ArrayList<>();
        ArrayList<String> listOne = new ArrayList<>();
        ArrayList<String> listTwo = new ArrayList<>();
        HashMap<String, Object> hashMap = new HashMap<>();
        HashMap<String, Object> hashMapOne = new HashMap<>();
        HashMap<String, Object> hashMapTwo = new HashMap<>();
        String longLatitude = "经度" + longitude + "维度" + latitude;
        logger.info("经维度解析前:" + longLatitude);
        String longLatitudeEncrypt = EncryptUtil.simpleEncrypt(longLatitude);
        logger.info("经维度解析后:" + longLatitudeEncrypt);
        listOne.add(longLatitudeEncrypt);
        hashMapOne.put("idNo", EncryptUtil.simpleEncrypt(certNo));
        hashMapOne.put("name", EncryptUtil.simpleEncrypt(name));
        hashMapOne.put("mobile", EncryptUtil.simpleEncrypt(phoneNo));
        hashMapOne.put("dataTyp", "04");
        hashMapOne.put("source", "2");
        hashMapOne.put("applSeq", applSeq);
        hashMapOne.put("Reserved6", applSeq);
        hashMapOne.put("content", listOne);
        listTwo.add(EncryptUtil.simpleEncrypt(area));
        hashMapTwo.put("idNo", EncryptUtil.simpleEncrypt(certNo));
        hashMapTwo.put("name", EncryptUtil.simpleEncrypt(name));
        hashMapTwo.put("mobile", EncryptUtil.simpleEncrypt(phoneNo));
        hashMapTwo.put("dataTyp", "A504");
        hashMapTwo.put("source", "2");
        hashMapTwo.put("applSeq", applSeq);
        hashMapTwo.put("Reserved6", applSeq);
        hashMapTwo.put("content", listTwo);
        arrayList.add(hashMapOne);
        arrayList.add(hashMapTwo);
        hashMap.put("list", arrayList);
        appServerService.updateListRiskInfo(token, hashMap);
        if (!"46".equals(getChannelNo())) {
            HashMap<String, Object> outMap = new HashMap<>();
            outMap.put("applseq", applSeq);
            outMap.put("name", name);
            outMap.put("certNo", certNo);
            outMap.put("mobileOne", userId);
            outMap.put("mobileTwo", phoneNo);
            outMap.put("appid", outreachConfig.getAppid());
            outMap.put("channelNo", outreachConfig.getChannelNo());
            outMap.put("businessChannelNo", channelNo);
            logger.info("==============芝麻授权开始==============");
            outreachService.protocolauth(outMap);//芝麻授权
            logger.info("==============芝麻授权结束==============");
        }
        cacheMap.put("crdSeq", applSeq);
        RedisUtils.setExpire(token, cacheMap);
        return success();
    }

    //页面缓存
    public Map<String, Object> cache(Map<String, Object> params, HttpServletRequest request) {
        String token = request.getHeader("token");
        if (token == null || "".equals(token)) {
            logger.info("token为空");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        Map<String, Object> retMap = null;
        String paramNames = (String) params.get("params");
        if (paramNames != null) {
            paramNames = EncryptUtil.simpleDecrypt(paramNames);
            String[] paramArr = paramNames.split(",");
            String type = (String) params.get("type");
            if (type == null || type.equals("get")) {
                retMap = new HashMap<>();
                Map<String, Object> sessionMap = RedisUtils.getExpireMap(token);
                if (sessionMap == null) {
                    return success();
                }
                for (String param : paramArr) {
                    if (sessionMap.get(param) != null) {
                        retMap.put(EncryptUtil.simpleEncrypt(param), EncryptUtil.simpleEncrypt(JSONObject.toJSON(sessionMap.get(param)).toString()));
                    } else {
                        retMap.put(EncryptUtil.simpleEncrypt(param), null);
                    }
                }
            } else if (type.equals("set")) {
                retMap = RedisUtils.getExpireMap(token);
                if (retMap == null) {
                    retMap = new HashMap<>();
                }
                for (String param : paramArr) {
                    Object value = params.get(EncryptUtil.simpleEncrypt(param));
                    if (value != null) {
                        retMap.put(param, EncryptUtil.simpleDecrypt(JSONObject.toJSON(value).toString()));
                    }/* else {
                        retMap.put(param, null);
                    }*/
                }
                RedisUtils.setExpire(token, retMap);
                return success();
            }
        }
        return success(retMap);
    }

    //修改支付密码（记得支付密码）
    public Map<String, Object> updatePayPasswd(String token, Map<String, Object> params, String channel, String channelNo) {
        String oldpassword = (String) params.get("oldpassword");//旧密码
        String newpassword = (String) params.get("newpassword");//新密码
        if (token == null || "".equals(token)) {
            logger.info("获取的token为空" + token);
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        if (StringUtils.isEmpty(oldpassword) || StringUtils.isEmpty(newpassword)) {
            logger.info("获取的参数为空");
            logger.info("payPasswd" + oldpassword + " newpassword" + newpassword);
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        Map<String, Object> cacheMap = RedisUtils.getExpireMap(token);
        String userId = (String) cacheMap.get("userId");
        if (StringUtils.isEmpty(userId)) {
            logger.info("缓存中userID为空");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        //userId="18325423979";
        Map<String, Object> map = new HashMap<>();
        map.put("token", token);
        map.put("channel", channel);
        map.put("channelNo", channelNo);
        map.put("userId", EncryptUtil.simpleEncrypt(userId));
        map.put("payPasswd", EncryptUtil.simpleEncrypt(oldpassword));
        map.put("newPayPasswd", EncryptUtil.simpleEncrypt(newpassword));
        Map<String, Object> verifyresultmap = appServerService.updatePayPasswd(token, map);
        if (verifyresultmap.isEmpty()) {
            logger.info("修改支付密码（记得支付密码）verifyresultmap" + verifyresultmap);
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        String verifyresult = JSONObject.toJSONString(verifyresultmap);
        //JSONObject jb0 = new JSONObject(verifyresult);
        JSONObject jb0 = JSONObject.parseObject(verifyresult);
        JSONObject head = jb0.getJSONObject("head");
        String retflag0 = head.getString("retFlag");
        String retmsg0 = head.getString("retMsg");
        if (!"00000".equals(retflag0)) {
            logger.info("修改支付密码异常！" + retmsg0);
            return fail(retflag0, retmsg0);
        }
        return success();
    }

    //实名认证找回密码
    public Map<String, Object> updPwdByIdentity(Map<String, Object> params) {
        String token = super.getToken();
        if (token == null || "".equals(token)) {
            logger.info("获取的token为空" + token);
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        Map<String, Object> cacheMap = RedisUtils.getExpireMap(token);
        if (StringUtils.isEmpty(cacheMap)) {
            logger.info("Jedi获取的数据weikong");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        String userId = (String) cacheMap.get("userId");//账号
        //userId = "18325423979";
        String certNo = (String) cacheMap.get("idNo");//身份证号
        //certNo = "372926198911178630";
        String custName = (String) cacheMap.get("name");//客户姓名
        String cardNo = (String) params.get("cardNo");//银行卡号
        String bankCode = (String) params.get("bankCode");//银行号
        String mobile = (String) params.get("mobile");//预留手机号
        String verifyNo = (String) params.get("verifyNo");//验证码
        String newPayPasswd = (String) params.get("newPassword");//密码
        String channel = super.getChannel();//系统标识
        String channelNo = super.getChannelNo();//渠道编码

        if (StringUtils.isEmpty(userId) || StringUtils.isEmpty(certNo) || StringUtils.isEmpty(cardNo) ||
                StringUtils.isEmpty(custName) || StringUtils.isEmpty(mobile) || StringUtils.isEmpty(verifyNo) ||
                StringUtils.isEmpty(newPayPasswd)) {
            logger.info("userId" + userId + "  certNo" + certNo + "  cardNo" + cardNo + "  mobile" + mobile + "verifyNo" + verifyNo + "  newPayPasswd" + newPayPasswd);
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        Map<String, Object> map = new HashMap();
        map.put("userId", EncryptUtil.simpleEncrypt(userId));//用户账号
        map.put("certNo", EncryptUtil.simpleEncrypt(certNo));//身份证号
        map.put("cardNo", EncryptUtil.simpleEncrypt(cardNo));//银行卡号
        map.put("custName", EncryptUtil.simpleEncrypt(custName));//客户姓名
        map.put("mobile", EncryptUtil.simpleEncrypt(mobile));//预留手机号
        map.put("bankCode", bankCode);//银行号
        map.put("channel", channel);//
        map.put("channelNo", channelNo);//
        map.put("verifyNo", verifyNo);//验证码
        map.put("newPayPasswd", EncryptUtil.simpleEncrypt(newPayPasswd));//新支付密码
        String url = EurekaServer.OUTREACHPLATFORM + "/Outreachplatform/api/chinaPay/identifyByFlag";
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("accountName", custName);
        jsonMap.put("accountNo", cardNo);
        jsonMap.put("bankCode", bankCode);
        jsonMap.put("id", certNo);
        jsonMap.put("cardPhone", mobile);
        jsonMap.put("flag", "1");
        jsonMap.put("channelNo", "payplat");
        logger.info("实名认证(外联)参数==>" + jsonMap.toString());
        String resData = CommonRestUtils.postForString(url, jsonMap);
        logger.info("实名认证(外联)响应数据==>" + resData);
        if (resData == null || "".equals(resData)) {
            logger.info("修改密码的实名认证(外联)接口，返回数据为空");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        JSONObject jb = JSONObject.parseObject(resData);
        String retFlag = jb.getString("RET_CODE");
        String retMsg = jb.getString("RET_MSG");
        if ("00000".equals(retFlag)) {
            Map<String, Object> resultmap = appServerService.updPwdByIdentity(token, map);
            if (MapUtils.isEmpty(resultmap)) {
                logger.info("实名认证修改密码接口，返回数据为空" + resultmap);
                return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
            }
            String result = JSONObject.toJSONString(resultmap);
            jb = JSONObject.parseObject(result);
            JSONObject head = jb.getJSONObject("head");
            retFlag = head.getString("retFlag");
            retMsg = head.getString("retMsg");
            if ("00000".equals(retFlag)) {
                return success();
            } else {
                return fail(retFlag, retMsg);
            }
        } else {
            return fail(retFlag, retMsg);
        }
    }

    //确认支付密码（额度申请）
    public Map<String, Object> paymentPwdConfirm(String token, String channel, String channelNo, Map<String, Object> params) {
        String payPasswd = (String) params.get("payPasswd");
        if (StringUtils.isEmpty(token) || StringUtils.isEmpty(channel) || StringUtils.isEmpty(channelNo) || StringUtils.isEmpty(payPasswd)) {
            logger.info("获取前端的参数为空token：" + token + "  ,channel" + channel + "  ,channelNo" + channelNo + "  ,payPasswd" + payPasswd);
            String retMsg = "获取前端的参数为空";
            return fail(ConstUtil.ERROR_CODE, retMsg);
        }
        Map<String, Object> cacheMap = RedisUtils.getExpireMap(token);
        if (MapUtils.isEmpty(cacheMap)) {
            logger.info("Jedis获取获取数据为空" + cacheMap);
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        String userId = (String) cacheMap.get("userId");
        logger.info("userId是" + userId);
        HashMap<String, Object> map = new HashMap<>();
        String userIdEncrypt = EncryptUtil.simpleEncrypt(userId);
        String payPasswdEncrypt = EncryptUtil.simpleEncrypt(payPasswd);
        map.put("userId", userIdEncrypt);
        map.put("payPasswd", payPasswdEncrypt);
        map.put("channel", channel);
        map.put("channelNo", channelNo);
        Map<String, Object> resStrmap = appServerService.validatePayPasswd(token, map);// 验证支付密码
        logger.info("确认支付密码（额度申请）,返回数据为：" + resStrmap);
        if (MapUtils.isEmpty(resStrmap)) {
            logger.info("接口返回数据为空！" + resStrmap);
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        String resStr = JSONObject.toJSONString(resStrmap);
        //JSONObject jsonObj = new JSONObject(resStr);
        JSONObject jsonObj = JSONObject.parseObject(resStr);
        JSONObject head = jsonObj.getJSONObject("head");
        if ("00000".equals(head.getString("retFlag"))) {
            cacheMap.put("payPasswd", payPasswd);
            cacheMap.put("pageFlag", "1");
            RedisUtils.setExpire(token, cacheMap);
            return success();
        } else {
            String tetFlag = head.getString("tetFlag");
            String retMsg = (head.getString("retMsg"));
            logger.info("确认支付密码（额度申请）失败,返回数据为：" + retMsg);
            return fail(tetFlag, retMsg);
        }

    }


    //贷款详情查询
    public Map<String, Object> queryLoanDetailInfo(String token, String applSeq) {
        BigDecimal psNormIntAmt;
        BigDecimal feeAmt;
        BigDecimal apprvAmt;
        if (StringUtils.isEmpty(token)) {
            logger.info("获取的token为空" + token);
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        String channel = super.getChannel();//系统标识
        //String channel="11";
        logger.info("系统标识：channel" + channel);
        String channelNo = super.getChannelNo();//渠道编码
        //String channelNo="46";
        logger.info("渠道编码channelNo" + channelNo);
        Map<String, Object> cacheMap = RedisUtils.getExpireMap(token);
        if (StringUtils.isEmpty(cacheMap)) {
            logger.info("Redis获取缓存失败");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        //String applSeq = (String) cacheMap.get("applSeq");//申请流水号
        //String   applSeq="1263841";//1265216   918653
        //String outSts = (String) cacheMap.get("outSts");//审批状态
        //String outSts = "04";
        if (StringUtils.isEmpty(applSeq)) {
            logger.info("获取数据为空申请流水号：" + applSeq);
            String retMsg = "从Redis获取的数据为空";
            return fail(ConstUtil.ERROR_CODE, retMsg);
        }
//        String channelNo = "19";
//        String channel = "11";
//        Map<String, Object> req = new HashMap<>();
//        req.put("channelNo", channelNo);
//        req.put("channel", channel);
//        req.put("applSeq", applSeq);
//        logger.info("查询贷款详情接口，请求数据：" + req.toString());
//        Map<String, Object> map = appServerService.queryApplLoanDetail(token, req);//查询贷款详情
        //applSeq="1265566";
        Map<String, Object> map = acquirerService.getOrderFromAcquirer(applSeq, channel, channelNo, null, null, "2");
        logger.info("查询贷款详情接口，响应数据：" + map);
        if (MapUtils.isEmpty(map)) {
            logger.info("网络异常,查询贷款详情接口,响应数据为空！" + map);
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        String res = JSONObject.toJSONString(map);
        //JSONObject jsonObject = new JSONObject(res);
        JSONObject jsonObject = JSONObject.parseObject(res);
        JSONObject head = jsonObject.getJSONObject("head");
        String code = head.getString("retFlag");
        String message = head.getString("retMsg");
        if ("00000".equals(code)) {//查询贷款详情成功
            logger.info("查询贷款详情接口，响应数据：" + jsonObject.getJSONObject("body").toString());
            JSONObject json = jsonObject.getJSONObject("body");
            String totfee;
            String apprvTotal;
            String psNormIntAmtStr = String.valueOf(json.get("totalnormint"));//总利息金额
            if (StringUtils.isEmpty(psNormIntAmtStr)) {
                psNormIntAmt = new BigDecimal(0);
                String psNormIntAmtStr_ = "0";
                json.put("totalnormint", psNormIntAmtStr_);
            } else {
                psNormIntAmt = new BigDecimal(psNormIntAmtStr);
            }
            String feeAmtStr = String.valueOf(json.get("totalfeeamt"));//费用总额
            if (StringUtils.isEmpty(feeAmtStr)) {
                feeAmt = new BigDecimal(0);
            } else {
                feeAmt = new BigDecimal(feeAmtStr);
            }
//                String apprvAmtStr = String.valueOf(json.get("apprvAmt"));//贷款审批金额
            String apprvAmtStr = String.valueOf(json.get("apply_amt"));//申请金额（元）
            if (StringUtils.isEmpty(apprvAmtStr)) {
                apprvAmt = new BigDecimal(0);
            } else {
                apprvAmt = new BigDecimal(apprvAmtStr);
            }

            BigDecimal tot;
            tot = psNormIntAmt.add(feeAmt);//总利息+总费用
            totfee = tot.divide(new BigDecimal(1), 2, BigDecimal.ROUND_HALF_UP) + "";
            json.put("totfee", totfee);
            BigDecimal total;
            total = tot.add(apprvAmt);//总利息+总费用+贷款审批金额
            apprvTotal = total.divide(new BigDecimal(1), 2, BigDecimal.ROUND_HALF_UP) + "";
            json.put("ordertotal", apprvTotal);

            //}
            String outStsNew = json.getString("outSts");
            if (!"WS".equals(outStsNew)) {
                switch (outStsNew) {
                    case "1":
                        json.put("outSts", "待提交");
                        break;
                    case "2":
                        json.put("outSts", "待确认");
                        break;
                    case "3":
                        json.put("outSts", "商户退回");
                        break;
                    case "01":
                        json.put("outSts", "审批中");
                        break;
                    case "02":
                        json.put("outSts", "贷款被拒绝");
                        break;
                    case "03":
                        json.put("outSts", "贷款已取消");
                        break;
                    case "04":
                        json.put("outSts", "等待放款");
                        break;
                    case "05":
                        json.put("outSts", "审批通过，等待放款");
                        break;
                    case "06":
                        json.put("outSts", "已放款");
                        break;
                    case "20":
                        json.put("outSts", "待放款");
                        break;
                    case "22":
                        json.put("outSts", "审批退回");
                        break;
                    case "23":
                        json.put("outSts", "等待放款");
                        break;
                    case "24":
                        json.put("outSts", "放款审核中");
                        break;
                    case "25":
                        json.put("outSts", "额度申请被拒");
                        break;
                    case "26":
                        json.put("outSts", "额度申请已取消");
                        break;
                    case "27":
                        json.put("outSts", "已通过");
                        break;
                    case "AA":
                        json.put("outSts", "取消放款");
                        break;
                    case "0D":
                        json.put("outSts", "已逾期");
                        break;
                    default:
                        break;
                }
            }
                /*if(outSts.equals("待还款")||outSts.equals("已放款")||outSts.equals("已逾期")){
                    String loanNo = json.getString("loanNo");
					if(loanNo == null || "".equals(loanNo)){
						retflag = "100003";
						throw new CommonException("借据号为空");
					}
					resultMap.put("loanNo", loanNo);
					redisBaseDAO.saveObject(token, resultMap);
				}*/
            return success(json);
        } else {
            return fail(code, message);
        }
    }


    //贷款详情查询
    public Map<String, Object> queryLoanDetailInfoForApp(String token, String applSeq) {
        BigDecimal psNormIntAmt;
        BigDecimal feeAmt;
        BigDecimal apprvAmt;
        if (StringUtils.isEmpty(token)) {
            logger.info("获取的token为空" + token);
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        String channel = super.getChannel();//系统标识
        //String channel="11";
        logger.info("系统标识：channel" + channel);
        String channelNo = super.getChannelNo();//渠道编码
        //String channelNo="46";
        logger.info("渠道编码channelNo" + channelNo);
        Map<String, Object> cacheMap = RedisUtils.getExpireMap(token);
        if (StringUtils.isEmpty(cacheMap)) {
            logger.info("Redis获取缓存失败");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        if (StringUtils.isEmpty(applSeq)) {
            logger.info("获取数据为空申请流水号：" + applSeq);
            String retMsg = "从Redis获取的数据为空";
            return fail(ConstUtil.ERROR_CODE, retMsg);
        }
        Map<String, Object> req = new HashMap<>();
        req.put("channelNo", channelNo);
        req.put("channel", channel);
        req.put("applSeq", applSeq);
        logger.info("查询贷款详情接口，请求数据：" + req.toString());
        Map<String, Object> map = appServerService.queryApplLoanDetail(token, req);//查询贷款详情
        //applSeq="1265566";
//        Map<String, Object> map = acquirerService.getOrderFromAcquirer(applSeq, channel, channelNo, null, null, "2");
        logger.info("查询贷款详情接口，响应数据：" + map);
        if (MapUtils.isEmpty(map)) {
            logger.info("网络异常,查询贷款详情接口,响应数据为空！" + map);
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        String res = JSONObject.toJSONString(map);
        //JSONObject jsonObject = new JSONObject(res);
        JSONObject jsonObject = JSONObject.parseObject(res);
        JSONObject head = jsonObject.getJSONObject("head");
        String code = head.getString("retFlag");
        String message = head.getString("retMsg");
        if ("00000".equals(code)) {//查询贷款详情成功
            logger.info("查询贷款详情接口，响应数据：" + jsonObject.getJSONObject("body").toString());
            JSONObject json = jsonObject.getJSONObject("body");
            String outSts = String.valueOf(json.get("outSts"));//贷款状态
            String preShouldAmount = "";
            if ("06".equals(outSts)) {//放款状态，查询借据号
                String loanNo = String.valueOf(json.get("loanNo"));//loanNo
                if (StringUtils.isEmpty(loanNo)) {
                    logger.info("放款状态借据号为空！");
                    return fail(ConstUtil.ERROR_CODE, "放款状态借据号为空！");
                }
                //查询待还金额&还款明细查询
                HashMap<String, Object> queryApplAmtAndRepayByloanNoOneMap = new HashMap<>();
                String custNo = (String) cacheMap.get("custNo");// 客户号
                logger.info("获取的客户号：" + custNo);
                queryApplAmtAndRepayByloanNoOneMap.put("custNo", custNo);
                queryApplAmtAndRepayByloanNoOneMap.put("loanNo", loanNo);
                queryApplAmtAndRepayByloanNoOneMap.put("applSeq", applSeq);
                queryApplAmtAndRepayByloanNoOneMap.put("isSelectCoupon", "0");
                queryApplAmtAndRepayByloanNoOneMap.put("paymInd", "N");
                queryApplAmtAndRepayByloanNoOneMap.put("setlTyp", "NM");
                queryApplAmtAndRepayByloanNoOneMap.put("relPerdCnt", "0");
                logger.info("第一次查询待还金额&还款明细查询请求数据：" + queryApplAmtAndRepayByloanNoOneMap);
                Map<String, Object> resultOneMap = crmService.queryApplAmtAndRepayByloanNo(queryApplAmtAndRepayByloanNoOneMap);
                logger.info("第一次查询待还金额&还款明细查询返回数据：" + resultOneMap);
                Map resultOneMaphead = (Map<String, Object>) resultOneMap.get("head");
                String resultOneMapheadflag = (String) resultOneMaphead.get("retFlag");
                if (!"00000".equals(resultOneMapheadflag)) {
                    String retMsg = (String) resultOneMaphead.get("retMsg");
                    return fail(ConstUtil.ERROR_CODE, retMsg);
                }
                Map resultOneMapbody = (Map<String, Object>) resultOneMap.get("body");
                BigDecimal preShouldAmount_one = Convert.toDecimal(resultOneMapbody.get("preShouldAmount"));//提前还款应还金额
                HashMap<String, Object> queryApplAmtAndRepayByloanNoTwoMap = new HashMap<>();
                queryApplAmtAndRepayByloanNoTwoMap.put("custNo", custNo);
                queryApplAmtAndRepayByloanNoTwoMap.put("loanNo", loanNo);
                queryApplAmtAndRepayByloanNoTwoMap.put("applSeq", applSeq);
                queryApplAmtAndRepayByloanNoTwoMap.put("isSelectCoupon", "0");
                queryApplAmtAndRepayByloanNoTwoMap.put("paymInd", "N");
                queryApplAmtAndRepayByloanNoTwoMap.put("setlTyp", "NM");
                queryApplAmtAndRepayByloanNoTwoMap.put("relPerdCnt", "0");
                queryApplAmtAndRepayByloanNoTwoMap.put("setlMode", "FS");
                queryApplAmtAndRepayByloanNoTwoMap.put("actvPayAmt", preShouldAmount_one);
                queryApplAmtAndRepayByloanNoTwoMap.put("inputAmount", preShouldAmount_one);
                logger.info("第二次查询待还金额&还款明细查询请求数据：" + queryApplAmtAndRepayByloanNoTwoMap);
                Map<String, Object> resultTwoMap = crmService.queryApplAmtAndRepayByloanNo(queryApplAmtAndRepayByloanNoTwoMap);
                logger.info("第二次查询待还金额&还款明细查询返回数据：" + resultTwoMap);
                Map resultTwoMaphead = (Map<String, Object>) resultTwoMap.get("head");
                String resultTwoMapheadflag = (String) resultTwoMaphead.get("retFlag");
                if (!"00000".equals(resultTwoMapheadflag)) {
                    String retMsg = (String) resultTwoMaphead.get("retMsg");
                    return fail(ConstUtil.ERROR_CODE, retMsg);
                }
                Map resultTwoMapbody = (Map<String, Object>) resultTwoMap.get("body");
//                BigDecimal preShouldAmount = Convert.toDecimal(resultTwoMapbody.get("preShouldAmount"));//提前还款应还金额
                preShouldAmount = Convert.toString(resultTwoMapbody.get("preShouldAmount"));//提前还款应还金额
            }
            json.put("preShouldAmount", preShouldAmount);
//            String applyTnrTyp = json.getString("apply_tnr_typ");
            String totfee;
            String apprvTotal;
//            String setlTotalAmt = "";
            //if (!"D".equals(applyTnrTyp) && !"d".equals(applyTnrTyp) && (applyTnrTyp != null && !"".equals(applyTnrTyp))) {
            String psNormIntAmtStr = String.valueOf(json.get("psNormIntAmt"));//总利息金额
            if (StringUtils.isEmpty(psNormIntAmtStr)) {
                psNormIntAmt = new BigDecimal(0);
                String psNormIntAmtStr_ = "0";
                json.put("totalnormint", psNormIntAmtStr_);
            } else {
                psNormIntAmt = new BigDecimal(psNormIntAmtStr);
            }
            String feeAmtStr = String.valueOf(json.get("feeAmt"));//费用总额
            if (StringUtils.isEmpty(feeAmtStr)) {
                feeAmt = new BigDecimal(0);
            } else {
                feeAmt = new BigDecimal(feeAmtStr);
            }
//                String apprvAmtStr = String.valueOf(json.get("apprvAmt"));//贷款审批金额
            String apprvAmtStr = String.valueOf(json.get("apprvAmt"));//申请金额（元）
            if (StringUtils.isEmpty(apprvAmtStr)) {
                apprvAmt = new BigDecimal(0);
            } else {
                apprvAmt = new BigDecimal(apprvAmtStr);
            }

            BigDecimal tot;
            tot = psNormIntAmt.add(feeAmt);//总利息+总费用
            totfee = tot.divide(new BigDecimal(1), 2, BigDecimal.ROUND_HALF_UP) + "";
            json.put("totfee", totfee);
            BigDecimal total;
            total = tot.add(apprvAmt);//总利息+总费用+贷款审批金额
            apprvTotal = total.divide(new BigDecimal(1), 2, BigDecimal.ROUND_HALF_UP) + "";
            json.put("ordertotal", apprvTotal);
            //已还金额、待还金额、息费管控
            BigDecimal setlFeeAmt = Convert.toDecimal(json.get("setlFeeAmt"));
            BigDecimal setlPrcpAmt = Convert.toDecimal(json.get("setlPrcpAmt"));
            BigDecimal setlIntAmt = Convert.toDecimal(json.get("setlIntAmt"));
            BigDecimal setlTotalAmt = setlFeeAmt.add(setlPrcpAmt).add(setlIntAmt);
            json.put("setlTotalAmt", setlTotalAmt.toString());//已还金额
            BigDecimal repayPrcpAmt = Convert.toDecimal(json.get("repayPrcpAmt"));
            BigDecimal repayFeeAmt = Convert.toDecimal(json.get("repayFeeAmt"));
            BigDecimal repayIntAmt = Convert.toDecimal(json.get("repayIntAmt"));
            BigDecimal repayAmt = repayPrcpAmt.add(repayFeeAmt).add(repayIntAmt);
            json.put("repayAmt", repayAmt);
            json.put("xfje", tot.toString());
            String outStsNew = json.getString("outSts");
            if (!"WS".equals(outStsNew)) {
                switch (outStsNew) {
                    case "1":
                        json.put("outSts", "待提交");
                        break;
                    case "2":
                        json.put("outSts", "待确认");
                        break;
                    case "3":
                        json.put("outSts", "商户退回");
                        break;
                    case "01":
                        json.put("outSts", "审批中");
                        break;
                    case "02":
                        json.put("outSts", "贷款被拒绝");
                        break;
                    case "03":
                        json.put("outSts", "贷款已取消");
                        break;
                    case "04":
                        json.put("outSts", "等待放款");
                        break;
                    case "05":
                        json.put("outSts", "审批通过，等待放款");
                        break;
                    case "06":
                        json.put("outSts", "已放款");
                        break;
                    case "20":
                        json.put("outSts", "待放款");
                        break;
                    case "22":
                        json.put("outSts", "审批退回");
                        break;
                    case "23":
                        json.put("outSts", "等待放款");
                        break;
                    case "24":
                        json.put("outSts", "放款审核中");
                        break;
                    case "25":
                        json.put("outSts", "额度申请被拒");
                        break;
                    case "26":
                        json.put("outSts", "额度申请已取消");
                        break;
                    case "27":
                        json.put("outSts", "已通过");
                        break;
                    case "AA":
                        json.put("outSts", "取消放款");
                        break;
                    case "0D":
                        json.put("outSts", "已逾期");
                        break;
                    default:
                        break;
                }
            }
                /*if(outSts.equals("待还款")||outSts.equals("已放款")||outSts.equals("已逾期")){
                    String loanNo = json.getString("loanNo");
					if(loanNo == null || "".equals(loanNo)){
						retflag = "100003";
						throw new CommonException("借据号为空");
					}
					resultMap.put("loanNo", loanNo);
					redisBaseDAO.saveObject(token, resultMap);
				}*/
            return success(json);
        } else {
            return fail(code, message);
        }
    }

    //贷款详情页面:按贷款申请查询分期账单
    public Map<String, Object> queryApplListBySeq(String token, String channel, String channelNo, String applSeq) {
        String loanNo = "";
        String retflag;
        if (StringUtils.isEmpty(token) || StringUtils.isEmpty(channel) || StringUtils.isEmpty(channelNo)) {
            logger.info("获取的参数为空：token：" + token + "  ,channel" + channel + "  ," + channelNo);
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
//        Map<String, Object> cacheMap = RedisUtils.getExpireMap(token);
//        if (StringUtils.isEmpty(cacheMap)) {
//            logger.info("Redi获取的数据weikong");
//            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
//        }
        //String applSeq = (String) cacheMap.get("applSeq");//申请流水号
        //String applSeq="1263841";
        //String outSts = (String) cacheMap.get("outSts");//审批状态
        //String  outSts="00";
        if (StringUtils.isEmpty(applSeq)) {
            logger.info("从前端获取数据失败applSeq:" + applSeq);
            String retMsg = "从前端获取数据失败";
            return fail(ConstUtil.ERROR_CODE, retMsg);
        }
//        Map<String, Object> req = new HashMap<>();
//        req.put("channelNo", "19");
//        req.put("channel", channel);
//        req.put("applSeq", applSeq);
//        logger.info("查询贷款详情接口，请求数据：" + req.toString());
//        Map<String, Object> map = appServerService.queryApplLoanDetail(token, req);//查询贷款详情
//        logger.info("查询贷款详情接口，响应数据：" + map);
//        if (MapUtils.isEmpty(map)) {
//            logger.info("网络异常,查询贷款详情接口,响应数据为空！");
//            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
//        }
//        String resData = JSONObject.toJSONString(map);
//        ///JSONObject jsonObject = new JSONObject(resData);
//        JSONObject jsonObject = JSONObject.parseObject(resData);
//        JSONObject head = jsonObject.getJSONObject("head");
//        String code = head.getString("retFlag");
//        String message = head.getString("retMsg");
//        if ("00000".equals(code)) {//查询贷款详情成功
//            logger.info("查询贷款详情接口，响应数据：" + jsonObject.getJSONObject("body").toString());
//            JSONObject jsonData = jsonObject.getJSONObject("body");
//            String outSts = jsonData.getString("outSts");
//            if (outSts.equals("待还款") || outSts.equals("已放款") || outSts.equals("已逾期")) {
//                loanNo = jsonData.getString("loanNo");
//                if (loanNo == null || "".equals(loanNo)) {
//                    logger.info("借据号为空");
//                    String retMsg = "借据号为空";
//                    return fail(ConstUtil.ERROR_CODE, retMsg);
//                }
//            }
        //applSeq="1265566";
        HashMap<String, Object> queryApplListMap = new HashMap<>();
        queryApplListMap.put("channelNo", channelNo);
        queryApplListMap.put("channel", channel);
        queryApplListMap.put("applseq", applSeq);
        //token="f294c5ad-1b63-4340-8ddb-7de9d0366ed7";
        Map<String, Object> mapOne = appServerService.queryApplListBySeq(token, queryApplListMap);//按贷款申请查询分期账单接口
        logger.info("按贷款申请查询分期账单接口，响应数据：" + mapOne);
        if (MapUtils.isEmpty(mapOne)) {
            logger.info("网络异常，app后台,按贷款申请查询分期账单接口,响应数据为空！");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        String resOne = JSONObject.toJSONString(mapOne);
        //JSONObject jsonOne = new JSONObject(resOne);
        JSONObject jsonOne = JSONObject.parseObject(resOne);
        JSONObject jsonOneHead = jsonOne.getJSONObject("head");
        String retOneFlag = jsonOneHead.getString("retFlag");
        String retOneMsg = jsonOneHead.getString("retMsg");
        if (retOneFlag.equals("00000")) {
            //JSONArray body = jsonOne.getJSONArray("body");
            JSONArray body = jsonOne.getJSONArray("body");
            BigDecimal bigDecimal = new BigDecimal(0);
            for (Object aBody : body) {
                JSONObject jobj = (JSONObject) aBody;
                String amount = jobj.getString("amount");
                BigDecimal amount_int = new BigDecimal(amount);
                bigDecimal = bigDecimal.add(amount_int);
            }
            HashMap<String, Object> resultMap = new HashMap<>();
            resultMap.put("fqze", bigDecimal);
            resultMap.put("list", body);
            return success(resultMap);
        } else {
            retflag = retOneFlag;
            return fail(retflag, retOneMsg);
        }
//        } else {
//            retflag = code;
//            return fail(retflag, message);
//        }
    }

    //贷款详情页面:还款总额
    public Map<String, Object> queryApplAmtBySeqAndOrederNo(String token, String channel, String channelNo, String applSeq) {
        logger.info("待还款-贷款详情页面:还款总额接口，开始");
        String retflag = "";
        String retmsg = "";
        String loanNo;
        BigDecimal odAmt;
        BigDecimal totalAmt = new BigDecimal(0);
        BigDecimal odTotalAmt = new BigDecimal(0);
        int flag = 0;// 记录逾期数
        String PAYM_MODE = "";// 还款模式
        String ACTV_PAY_AMT = "";// 主动还款金额

//        if (StringUtils.isEmpty(token) || StringUtils.isEmpty(channel) || StringUtils.isEmpty(channelNo)) {
//            logger.info("获取的数据为空：token" + token + "  ,channel" + channel + "  ,channelNo" + channelNo);
//            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
//        }
//        Map<String, Object> cacheMap = RedisUtils.getExpireMap(token);
//        if (StringUtils.isEmpty(cacheMap)) {
//            logger.info("贷款详情页面:还款总额接口，Jedis失效，cacheMap" + cacheMap);
//            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
//        }
        //     String applSeq = (String) cacheMap.get("applSeq");// 申请流水号----需要放开
        //      String outSts = (String) cacheMap.get("outSts");//审批状态
//        String applSeq = "1265216";//1265216   930201
//        String outSts="待还款";
        // channelNo="46";
        if (StringUtils.isEmpty(applSeq)) {
            logger.info("前端获取的数据为空：applSeq=" + applSeq);
            return fail(ConstUtil.ERROR_CODE, retmsg);
        }
        Map<String, Object> req = new HashMap<>();
        req.put("channelNo", channelNo);
        req.put("channel", channel);
        req.put("applSeq", applSeq);
        logger.info("查询贷款详情接口，请求数据：" + req.toString());
        Map<String, Object> map = acquirerService.getOrderFromAcquirer(applSeq, channel, channelNo, null, null, "2");
        //Map<String, Object> map = appServerService.queryApplLoanDetail(token, req);//查询贷款详情
        logger.info("查询贷款详情接口，响应数据：" + map);
        if (StringUtils.isEmpty(map)) {
            logger.info("网络异常,查询贷款详情接口,响应数据为空！");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        String resData = JSONObject.toJSONString(map);
        //JSONObject jsonObject = new JSONObject(resData);
        JSONObject jsonObject = JSONObject.parseObject(resData);
        JSONObject head = jsonObject.getJSONObject("head");
        String code = (String) head.get("retFlag");
        String message = (String) head.get("retMsg");
        if (code.equals("00000")) {//查询贷款详情成功
            logger.info("查询贷款详情接口，响应数据：" + jsonObject.getJSONObject("body").toString());
            JSONObject jsonData = jsonObject.getJSONObject("body");
            String outSts = jsonData.getString("outSts");
            String hkSts = jsonData.getString("hkSts");//逾期标识
            if ("20".equals(outSts) || "06".equals(outSts) || "1".equals(hkSts)) {//20-待还款    06-已放款
                loanNo = jsonData.getString("loan_no");
                //loanNo="HCF-HAPA0120160320795362001";
//                if (StringUtils.isEmpty(loanNo)) {
//                    logger.info("借据号为空");
//                    retflag = "借据号为空";
//                    return fail(ConstUtil.ERROR_CODE, retflag);
//                }
//                cacheMap.put("loanNo", loanNo);//借据号
//                RedisUtils.set(token, cacheMap);
//                JSONObject reqJson = new JSONObject();
//                reqJson.put("LOAN_NO", loanNo);
//                String params = reqJson.toString();
                HashMap<String, Object> qfmap = new HashMap<>();
                qfmap.put("LOAN_NO", loanNo);
                qfmap.put("channel", channel);
                qfmap.put("channelNo", channelNo);
                logger.info("欠款查询接口，请求数据：" + qfmap);
                Map<String, Object> qfCheck = appServerService.getQFCheck(token, qfmap);// 欠款查询
                logger.info("欠款查询接口，响应数据：" + qfCheck);
                if (MapUtils.isEmpty(qfCheck)) {
                    logger.info("网络异常，app后台,欠款查询接口,响应数据为空！" + qfCheck);
                    return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
                }
                String res = JSONObject.toJSONString(qfCheck);
                //JSONObject json = new JSONObject(res);
                JSONObject json = JSONObject.parseObject(res);
                JSONObject jsonRes = json.getJSONObject("msgall");
                String retFlag = jsonRes.getString("errorCode");
                String retMsg = jsonRes.getString("errorMsg");
                if ("00000".equals(retFlag)) {// 欠款查询接口成功
                    System.out.println(String.valueOf(json.get("OD_AMT")));
                    String odAmtStr = String.valueOf(json.get("OD_AMT"));
                    if (odAmtStr.equals("null")) {
                        odAmt = new BigDecimal(0);
                    } else {
                        odAmt = new BigDecimal(odAmtStr);
                    }
                        /*if (odAmtStr == null || "".equals(odAmtStr)) {
                            retflag = "100002";
							throw new CommonException("网络异常，app后台,欠款查询接口,逾期费用为空！");
						}*/
                    //applSeq="930201";
                    HashMap<String, Object> queryApplListmap = new HashMap<>();
                    queryApplListmap.put("channelNo", channelNo);
                    queryApplListmap.put("channel", channel);
                    queryApplListmap.put("applseq", applSeq);
                    Map<String, Object> queryApplListBySeqmap = appServerService.queryApplListBySeq(token, queryApplListmap);// 按贷款申请查询分期账单接口
                    logger.info("按贷款申请查询分期账单接口，响应数据：" + queryApplListBySeqmap);
                    if (MapUtils.isEmpty(queryApplListBySeqmap)) {
                        logger.info("网络异常，app后台,按贷款申请查询分期账单接口,响应数据为空！" + queryApplListBySeqmap);
                        return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
                    }
                    //JSONObject jsonOne = new JSONObject(resOne);
                    String resOne = JSON.toJSONString(queryApplListBySeqmap);
                    JSONObject jsonOne = JSONObject.parseObject(resOne);
                    JSONObject jsonOneHead = jsonOne.getJSONObject("head");
                    String retOneFlag = jsonOneHead.getString("retFlag");
                    String retOneMsg = jsonOneHead.getString("retMsg");
                    if (retOneFlag.equals("00000")) {// 按贷款申请查询分期账单接口成功
                        //retflag = "0000";
//                        JSONArray resArr = new JSONArray();
                        List array = (List) jsonOne.get("body");
                        if (array != null && array.size() > 0) {
                            for (Object anArray : array) {
                                JSONObject resJson = (JSONObject) anArray;
                                String setlInd = String.valueOf(resJson.get("setlInd"));
                                String daysStr = String.valueOf(resJson.get("days"));
                                int days = Integer.parseInt(daysStr);
                                String amountStr = String.valueOf(resJson.get("amount"));
                                BigDecimal amount = new BigDecimal(amountStr);
                                if (setlInd.equals("N") && days < 0) {// N：未结清，已逾期
                                    odTotalAmt = odTotalAmt.add(amount);
                                    flag++;
                                } else if (setlInd.equals("N") && days >= 0) {// N：未结清，正常
                                    totalAmt = totalAmt.add(amount);
                                }
                            }
                            if (flag == 0) {// 不存在逾期金额
                                ACTV_PAY_AMT = totalAmt + "";
                                Map<String, Object> reqTwoMap = new HashMap<>();
                                reqTwoMap.put("loanNo", loanNo);
                                reqTwoMap.put("actvPayAmt", ACTV_PAY_AMT);
                                reqTwoMap.put("channel", channel);
                                reqTwoMap.put("channelNo", channelNo);
                                reqTwoMap.put("setlMode", "FS");
                                reqTwoMap.put("paymInd", "N");
                                Map<String, Object> resThreemap = appServerService.refundTrialAll(token, reqTwoMap);// 全部还款试算
                                logger.info("全部还款试算接口，响应数据：" + resThreemap);
                                if (StringUtils.isEmpty(resThreemap)) {
                                    logger.info("网络异常,全部还款试算接口,响应数据为空！");
                                    return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
                                }
                                String resThree = JSON.toJSONString(resThreemap);
                                //JSONObject jsonObjectresThree = new JSONObject(resThree);
                                JSONObject jsonObjectresThree = JSONObject.parseObject(resThree);
                                String codeOne = jsonObjectresThree.getString("retMsg");
                                String messageOne = jsonObjectresThree.getString("retMsg");
                                if (codeOne.equals("00000")) {// 全部还款试算成功
                                    JSONObject jsonThree = jsonObjectresThree.getJSONObject("body");

                                    //Map<String, Object> resTwoMap = DataConverUtil.jsonToMap(jsonThree.toString());
                                    /*
                                     * String ze = jsonThree.getString("ze"); if(ze
                                     * == null || "".equals(ze)){ retflag =
                                     * "100002"; throw new
                                     * CommonException("网络异常,全部还款试算接口,总额为空！"); }
                                     * Map<String,Object> resTwoMap = new
                                     * HashMap<String,Object>(); resTwoMap.put("ze",
                                     * ze);
                                     */
                                    logger.info("全部还款试算接口，响应数据：" + jsonThree.toString());
                                    String ze = jsonThree.getString("ze");
                                    Map<String, Object> resTwoMap = new HashMap<>();
                                    resTwoMap.put("zdhkFee", ze);
                                    return success(resTwoMap);
                                    // return success(jsonThree);
                                } else {
                                    retflag = codeOne;
                                    return fail(retflag, messageOne);
                                }
                            } else if (flag > 0) {// 存在逾期金额

                                int odFlag = odAmt.compareTo(BigDecimal.ZERO); // 和0，Zero比较
                                switch (odFlag) {
                                    case 0: // 等于，逾期金额为0
                                        PAYM_MODE = "ER";
                                        ACTV_PAY_AMT = odTotalAmt + "";
                                        break;
                                    case 1: // 大于,逾期金额大于0
                                        PAYM_MODE = "NM";
                                        odTotalAmt = odTotalAmt.add(odAmt);
                                        ACTV_PAY_AMT = odTotalAmt + "";
                                        break;
                                    case -1: // 小于
                                        String retMsgOne = "按贷款申请查询分期账单接口，逾期金额为负数";
                                        return fail(ConstUtil.ERROR_CODE, retMsgOne);
                                }
                                HashMap<String, Object> checkZdhkMoney = new HashMap<>();
                                checkZdhkMoney.put("LOAN_NO", loanNo);
                                checkZdhkMoney.put("PAYM_MODE", PAYM_MODE);
                                checkZdhkMoney.put("ACTV_PAY_AMT", ACTV_PAY_AMT);
                                Map<String, Object> resTwomap = appServerService.checkZdhkMoney(token, checkZdhkMoney);// 主动还款金额查询
                                logger.info("主动还款金额查询接口，响应数据：" + resTwomap);
                                if (StringUtils.isEmpty(resTwomap)) {
                                    logger.info("网络异常，app后台,主动还款金额查询接口,响应数据为空！" + resTwomap);
                                    return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
                                }
                                //JSONObject jsonTwo = new JSONObject(resTwo);
                                String resTwo = JSONObject.toJSONString(resTwomap);
                                JSONObject jsonTwo = JSONObject.parseObject(resTwo);
                                JSONObject jsonTwoRes = jsonTwo.getJSONObject("msgall");
                                String retFlagTwo = jsonTwoRes.getString("errorCode");
                                String retMsgTwo = jsonTwoRes.getString("errorMsg");
                                if (retFlagTwo.equals("00000")) {// 主动还款金额查询接口成功
                                    JSONObject jsonTwoBody = jsonTwo.getJSONObject("body");
                                    String zdhkFee = jsonTwoBody.getString("zdhkFee");
                                    if (zdhkFee == null || "".equals(zdhkFee)) {
                                        logger.info("网络异常，app后台,主动还款金额查询接口,主动还款金额为空！" + zdhkFee);
                                        return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
                                    }
                                    Map<String, Object> resTwoMap = new HashMap<>();
                                    resTwoMap.put("zdhkFee", zdhkFee);
                                    return success(resTwoMap);
                                } else {
                                    retflag = retFlagTwo;
                                    return fail(retflag, retMsgTwo);
                                }
                            } else {
                                logger.info("按贷款申请查询分期账单接口，数据flag出现异常，为负数");
                                return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
                            }
                        } else {
                            logger.info("按贷款申请查询分期账单接口，返回数据为空" + array);
                            String retMsgone = "按贷款申请查询分期账单接口，返回数据为空";
                            return fail(ConstUtil.ERROR_CODE, retMsgone);

                        }
                    } else {
                        retflag = retOneFlag;
                        return fail(retflag, retOneMsg);
                    }
                } else {
                    retflag = retFlag;
                    return fail(retflag, retMsg);
                }
            } else {
                String retmsgone = "审批状态不符合";
                return fail(ConstUtil.ERROR_CODE, retmsgone);
            }
        } else {
            return fail(retflag, message);
        }
    }

    //查询额度
    public Map<String, Object> edCheck(String token) {
        if (StringUtils.isEmpty(token)) {
            logger.info("获取token失败token:" + token);
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        Map<String, Object> cacheMap = RedisUtils.getExpireMap(token);
        if (StringUtils.isEmpty(cacheMap)) {
            logger.info("Redis获取缓存失败");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        String idNo = (String) cacheMap.get("idNo");//客户证件号码
        String channel = super.getChannel();//系统标识
        String channelNo = super.getChannelNo();//渠道编码
        String idTyp = "20";//证件类型  身份证：20
        if (StringUtils.isEmpty(idNo) || StringUtils.isEmpty(channel) || StringUtils.isEmpty(channelNo)) {
            logger.info("获取的数据为空：idNo=" + idNo + "  ,channel=" + channel + "  ,channelNO=" + channelNo);
            String retMsg = "获取的数据为空";
            return fail(ConstUtil.ERROR_CODE, retMsg);
        }
        HashMap<String, Object> edCheckmap = new HashMap<>();
        edCheckmap.put("idNo", idNo);
        edCheckmap.put("channel", channel);
        edCheckmap.put("channelNo", channelNo);
        edCheckmap.put("idTyp", idTyp);
        Map<String, Object> edCheck = appServerService.getEdCheck(token, edCheckmap);// 获取额度剩余额度=crdComAvailAnt+crdNorAvailAmt
        if (StringUtils.isEmpty(edCheck)) {
            logger.info("调用接口返回的数据为空");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        String limit = JSONObject.toJSONString(edCheck);
        //JSONObject jsonObject = new JSONObject(limit);
        JSONObject jsonObject = JSONObject.parseObject(limit);
        JSONObject head = jsonObject.getJSONObject("head");
        String retFlag = (String) head.get("retFlag");
        String retMsg = (String) head.get("retMsg");
        if ("00000".equals(retFlag)) {
            JSONObject limitRes = jsonObject.getJSONObject("body");
            double crdComAvailAnt = limitRes.getDouble("crdComAvailAnt");// 剩余额度（受托支付可用额度金额）
            double crdNorAvailAmt = limitRes.getDouble("crdNorAvailAmt");// 自主支付可用额度金额(现金)
            double crdAmt = limitRes.getDouble("crdAmt");// 总额度
            double crdComAmt = limitRes.getDouble("crdComAmt");
            //crdComAvailAntSum = crdComAvailAnt+crdNorAvailAmt;可用额度
            JSONObject jb = new JSONObject();
            jb.put("crdComAvailAnt", crdComAvailAnt);
            jb.put("crdNorAvailAmt", crdNorAvailAmt);
            jb.put("crdAmt", crdAmt);
            jb.put("crdComAmt", crdComAmt);
            // jb.put("crdComAvailAntSum", crdComAvailAntSum);
            cacheMap.put("crdNorAvailAmt", crdNorAvailAmt);//存储redis
            cacheMap.put("crdComAvailAnt", crdComAvailAnt);
            RedisUtils.setExpire(token, cacheMap);
            return success(jb);
        } else {
            return fail(ConstUtil.ERROR_CODE, retMsg);
        }
    }

    /**
     * @Title billCheck
     * @Description: 账单查询
     * @author yu jianwei
     * @date 2017/9/14 12:29
     */
    public Map<String, Object> billCheck(String token) {
        if (StringUtils.isEmpty(token)) {
            logger.info("获取token失败token:" + token);
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        Map<String, Object> cacheMap = RedisUtils.getExpireMap(token);
        if (StringUtils.isEmpty(cacheMap)) {
            logger.info("Redis获取缓存失败");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        String userId = (String) cacheMap.get("userId");//用户Id
        String channel = super.getChannel();//系统标识
        String channelNo = super.getChannelNo();//渠道编码
        String crdNorAvailAmt = String.valueOf(cacheMap.get("crdNorAvailAmt"));// 自主支付可用额度金额(现金)
        String crdComAvailAnt = String.valueOf(cacheMap.get("crdComAvailAnt"));// 剩余额度（受托支付可用额度金额）
        if (StringUtils.isEmpty(userId) || StringUtils.isEmpty(channel) || StringUtils.isEmpty(channelNo) || StringUtils.isEmpty(crdNorAvailAmt) || StringUtils.isEmpty(crdComAvailAnt)) {
            logger.info("获取的数据为空：userId=" + userId + "  ,channel=" + channel + "  ,channelNO=" + channelNo + "  ,crdNorAvailAmt=" + crdNorAvailAmt + "  ,crdComAvailAnt=" + crdComAvailAnt);
            String retMsg = "获取的数据为空";
            return fail(ConstUtil.ERROR_CODE, retMsg);
        }
        HashMap<String, Object> billCheckMap = new HashMap<>();
        billCheckMap.put("channel", channel);
        billCheckMap.put("channelNo", channelNo);
        billCheckMap.put("userId", userId);
        Map<String, Object> billCheck = appServerService.getBillCheck(token, billCheckMap);// 获取额度剩余额度=crdComAvailAnt+crdNorAvailAmt
        if (StringUtils.isEmpty(billCheck)) {
            logger.info("调用接口返回的数据为空");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        String limit = JSONObject.toJSONString(billCheck);
        //JSONObject jsonObject = new JSONObject(limit);
        JSONObject jsonObject = JSONObject.parseObject(limit);
        JSONObject head = jsonObject.getJSONObject("head");
        String retFlag = (String) head.get("retFlag");
        String retMsg = (String) head.get("retMsg");
        if ("00000".equals(retFlag)) {
            JSONObject limitRes = jsonObject.getJSONObject("body");
            String mounthAmount = limitRes.getString("mounthAmount");//每月待还
            //crdComAvailAntSum = crdComAvailAnt+crdNorAvailAmt;可用额度
            JSONObject jb = new JSONObject();
            jb.put("mounthAmount", mounthAmount);
            jb.put("crdNorAvailAmt", crdNorAvailAmt);
            jb.put("crdComAvailAnt", crdComAvailAnt);
            // jb.put("crdComAvailAntSum", crdComAvailAntSum);
            return success(jb);
        } else {
            return fail(ConstUtil.ERROR_CODE, retMsg);
        }
    }

    //根据流水号查询额度审批进度
    public Map<String, Object> approvalProcessInfo(String token, String channel, String channelNo, Map<String, Object> params) {
        if (StringUtils.isEmpty(token) || StringUtils.isEmpty(channel) || StringUtils.isEmpty(channelNo)) {
            logger.info("获取的参数为空token=" + token + "  ,channel=" + channel + "  ,channelNo=" + channelNo);
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        Map<String, Object> cacheMap = RedisUtils.getExpireMap(token);
        if (StringUtils.isEmpty(cacheMap)) {
            logger.info("Redis为空：" + cacheMap);
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
//        String applSeq = (String) params.get("applSeq");//申请流水号
        String applSeq = (String) cacheMap.get("crdSeq");//在途的申请流水号
        //String   applSeq = "1097515";
        if (StringUtils.isEmpty(applSeq)) {
            logger.info("请求的数据为空：applSeq=" + applSeq);
            String retmsg = "请求的数据为空：applSeq";
            return fail(ConstUtil.ERROR_CODE, retmsg);
        }
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("applSeq", applSeq);
        paramMap.put("channel", channel);
        paramMap.put("channelNo", channelNo);
        logger.info("根据流水号查询额度审批进度,请求参数：" + paramMap);
        Map<String, Object> map = appServerService.approvalProcessInfo(token, paramMap);
        if (StringUtils.isEmpty(map)) {
            logger.info("根据流水号查询额度审批进度返回参数为空");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        //JSONObject jsonObject = new JSONObject(result);
        String result = JSONObject.toJSONString(map);
        JSONObject jsonObject = JSONObject.parseObject(result);
        JSONObject head = jsonObject.getJSONObject("head");
        String flag = (String) head.get("retFlag");
        String retMsg = (String) head.get("retMsg");
        if (!"00000".equals(flag)) {
            return fail(flag, retMsg);
        } else {
            //JSONObject body = jsonObject.getJSONObject("body");
            JSONArray body = jsonObject.getJSONArray("body");
            return success(body);
        }
    }

    //根据流水号查询贷款审批进度
    public Map<String, Object> queryDkProcessInfo(String token, String channel, String channelNo, Map<String, Object> params) {
        if (StringUtils.isEmpty(token) || StringUtils.isEmpty(channel) || StringUtils.isEmpty(channelNo)) {
            logger.info("获取的参数为空token:" + token + "  ,channel" + channel + "  ,channelNO" + channelNo);
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        Map<String, Object> cacheMap = RedisUtils.getExpireMap(token);
        if (StringUtils.isEmpty(cacheMap)) {
            logger.info("Redis为空：" + cacheMap);
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        String applSeq = (String) params.get("applSeq");//申请流水号
        //String   applSeq = "1097515";
        if (StringUtils.isEmpty(applSeq)) {
            logger.info("请求的数据为空：applSeq=" + applSeq);
            String retmsg = "请求的数据为空：applSeq";
            return fail(ConstUtil.ERROR_CODE, retmsg);
        }
        List procList;
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("applSeq", applSeq);
        paramMap.put("channel", channel);
        paramMap.put("channelNo", channelNo);
        Map<String, Object> result = AcqUtil.getAcqResponse(EurekaServer.ACQUIRER + "/api/appl/getApprovalProcess", "ACQ-1151", super.getChannel(), super.getChannelNo(), null, null, paramMap);
        logger.info("根据流水号查询额度贷款进度,请求参数：" + paramMap);
        if (!CmisUtil.isSuccess(result)) {
            logger.info("调用收单系统查询贷款审批进度失败, 返回结果：" + result);
            return (Map<String, Object>) result.get("response");
        }
        Map<String, Object> responseMap = (Map<String, Object>) result.get("response");
        procList = (List<Map<String, Object>>) ((Map<String, Object>) responseMap.get("body")).get("info");
        if (procList.size() == 1) {
            Map<String, Object> map = acquirerService.getOrderFromAcquirer(applSeq, channel, channelNo, null, null, "2");
            logger.info("查询贷款详情接口，响应数据：" + map);
            if (MapUtils.isEmpty(map)) {
                logger.info("网络异常,查询贷款详情接口,响应数据为空！" + map);
                return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
            }
            String res = JSONObject.toJSONString(map);
            //JSONObject jsonObject = new JSONObject(res);
            JSONObject jsonObject = JSONObject.parseObject(res);
            JSONObject head = jsonObject.getJSONObject("head");
            JSONObject json = jsonObject.getJSONObject("body");
            String code = head.getString("retFlag");
            String message = head.getString("retMsg");
            if ("00000".equals(code)) {//查询贷款详情成功
                String outStsNew = json.getString("outSts");
                String app_out_advice = json.getString("app_out_advice");
                if (!"WS".equals(outStsNew)) {
                    if ("01".equals(outStsNew)) {
                        return success(procList);
                    }
                    procList.clear();
                    HashMap<String, Object> param = new HashMap<>();
                    param.put("operateTime", "");//办理时间
                    param.put("appOutAdvice", "【退回】");//外部意见
                    param.put("appConclusion", "");//审批结论标识
                    param.put("wfiNodeName", "退回");//审批状态
                    param.put("appConclusionDesc", app_out_advice);//审批结论
                    procList.add(param);
                }
                return success(procList);
            } else {
                logger.info("查询贷款详情接口失败！失败码：" + code + ",错误信息：" + message);
                return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
            }
        } else {
            return success(procList);
        }


//        Map<String, Object> map = appServerService.approvalProcessInfo(token, paramMap);
//        if (StringUtils.isEmpty(map)) {
//            logger.info("根据流水号查询贷款审批进度返回参数为空");
//            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
//        }
//        //JSONObject jsonObject = new JSONObject(result);
//        String result = JSONObject.toJSONString(map);
//        JSONObject jsonObject = JSONObject.parseObject(result);
//        JSONObject head = jsonObject.getJSONObject("head");
//        String flag = (String) head.get("retFlag");
//        String retMsg = (String) head.get("retMsg");
//        if (!"00000".equals(flag)) {
//            return fail(flag, retMsg);
//        } else {
//            //JSONObject body = jsonObject.getJSONObject("body");
//            JSONArray body = jsonObject.getJSONArray("body");
//            return success(body);
//        }
    }


    @Override
    public Map<String, Object> landPasswd(String token, String channelNo, String channel, Map<String, Object> params) {
        logger.info("*********设置登陆密码开始**************开始");
        Map<String, Object> resultparamMap = new HashMap<>();
        if (token.isEmpty()) {
            logger.info("token为空");
            return fail(ConstUtil.ERROR_CODE, "参数token为空!");
        }
        if (channel.isEmpty()) {
            logger.info("channel为空");
            return fail(ConstUtil.ERROR_CODE, "参数channel为空!");
        }
        if (channelNo.isEmpty()) {
            logger.info("channelNo为空");
            return fail(ConstUtil.ERROR_CODE, "参数channelNo为空!");
        }
        if (params.get("userId") == null || "".equals(params.get("userId"))) {
            logger.info("userId为空");
            return fail(ConstUtil.ERROR_CODE, "参数userId为空!");
        }
        if (params.get("verifyNo") == null || "".equals(params.get("verifyNo"))) {
            logger.info("verifyNo为空");
            return fail(ConstUtil.ERROR_CODE, "参数verifyNo为空!");
        }
        if (params.get("pwd") == null || "".equals(params.get("pwd"))) {
            logger.info("pwd为空");
            return fail(ConstUtil.ERROR_CODE, "参数pwd为空!");
        }
        String userId = (String) params.get("userId");
        String verifyNo = (String) params.get("verifyNo");
        String pwd = (String) params.get("pwd");
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("phone", userId);
        paramMap.put("verifyNo", verifyNo);
        paramMap.put("channel", channel);
        paramMap.put("channelNo", channelNo);
        Map<String, Object> stringObjectMap = appServerService.smsVerify(token, paramMap);
        if (stringObjectMap == null) {
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        Map resultmapjsonMap = (Map<String, Object>) stringObjectMap.get("head");
        String resultmapFlag = (String) resultmapjsonMap.get("retFlag");
        if (!"00000".equals(resultmapFlag)) {
            resultparamMap.put("flag", "1");//校验码错误或已经失效
        }
        Map<String, Object> landparamMap = new HashMap<>();
        landparamMap.put("userId", EncryptUtil.simpleEncrypt(userId));
        landparamMap.put("verifyNo", EncryptUtil.simpleEncrypt(verifyNo));
        landparamMap.put("newPassword", EncryptUtil.simpleEncrypt(pwd));
        landparamMap.put("channel", channel);
        landparamMap.put("channelNo", channelNo);
        Map<String, Object> landparamreturnMap = appServerService.custUpdatePwd(token, landparamMap);
        if (landparamreturnMap == null) {
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        Map landparamreturnHeadMap = (Map<String, Object>) landparamreturnMap.get("head");
        String reFlag = (String) landparamreturnHeadMap.get("retFlag");
        if (!"00000".equals(reFlag)) {
            String retMsg = (String) landparamreturnHeadMap.get("retMsg");
            return fail(ConstUtil.ERROR_CODE, retMsg);
        } else {
            resultparamMap.put("flag", "2");//设置成功
        }
        return success(resultparamMap);
    }


    @Override
    public Map<String, Object> getPersonalCenterInfo(String token, String channelNo, String channel) {
        logger.info("*********获取个人信息中心**************开始");

        if (StringUtils.isEmpty(token) || StringUtils.isEmpty(channel) || StringUtils.isEmpty(channelNo)) {
            logger.info("获取的参数为空token:" + token + "  ,channel:" + channel + "  ,channelNO:" + channelNo);
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        Map<String, Object> cacheMap = RedisUtils.getExpireMap(token);
        if (StringUtils.isEmpty(cacheMap)) {
            logger.info("Redis为空：" + cacheMap);
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        String userId = (String) cacheMap.get("userId");//用户ID
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("channel", channel);
        paramMap.put("channelNo", channelNo);
        paramMap.put("userId", userId);
        Map<String, Object> resultMap = appServerService.getPersonalCenterInfo(token, paramMap);
        if(HttpUtil.isSuccess(resultMap)){//自主支付可用额度进行缓存
            Map bodymap = (Map) resultMap.get("body");
            String crdNorAvailAmt = Convert.toString(bodymap.get("crdNorAvailAmt"));
            cacheMap.put("crdNorAvailAmt",crdNorAvailAmt);
            RedisUtils.setExpire(token, cacheMap);
        }

        return resultMap;
    }

    //返回实名认证需要的数据
    public Map<String, Object> queryCustNameByUId(String token) {
        if (token == null || "".equals(token)) {
            logger.info("获取的token为空" + token);
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        Map<String, Object> cacheMap = RedisUtils.getExpireMap(token);
        if (StringUtils.isEmpty(cacheMap)) {
            logger.info("Redi获取的数据为空");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_MSG);
        }
        String idNo = (String) cacheMap.get("idNo");//身份证号
        String name = (String) cacheMap.get("name");//客户姓名
        HashMap<Object, Object> map = new HashMap<>();
        map.put("idNo", idNo);
        map.put("name", name);
        return success(map);
    }


    @Override
    public Map<String, Object> resetPayPasswdForHaier(String token, String channelNo, String channel, Map<String, Object> param) {
        logger.info("支付密码未设置，进行密码的设置");
        String payPasswd = (String) param.get("payPasswd");//密码
        Map<String, Object> cacheMap = RedisUtils.getExpireMap(token);
        if (MapUtils.isEmpty(cacheMap)) {
            logger.info("Jedis获取失败");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        String userId = (String) cacheMap.get("userId");
        logger.info("获取的userId为：" + userId);
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("userId", EncryptUtil.simpleEncrypt(userId));
        paramsMap.put("payPasswd", EncryptUtil.simpleEncrypt(payPasswd));
        paramsMap.put("channel", channel);
        paramsMap.put("channelNo", channelNo);
        //paramsMap.put("access_token", token);
        Map<String, Object> map = appServerService.resetPayPasswd(token, paramsMap);
        if (StringUtils.isEmpty(map)) {
            logger.info("设置支付密码失败，app后台返回数据为空");
            String resultMsg = "设置支付密码失败，app后台返回数据为空";
            return fail(ConstUtil.ERROR_CODE, resultMsg);
        }
        String result = JSONObject.toJSONString(map);
        JSONObject jsonObject = JSONObject.parseObject(result);
        JSONObject resultHead = jsonObject.getJSONObject("head");
        String retFlag = resultHead.getString("retFlag");
        String retMsg = resultHead.getString("retMsg");
        if (!"00000".equals(retFlag)) {
            logger.info("设置支付密码失败" + retMsg);
            return fail(retFlag, retMsg);
        }
        return success();
    }
}