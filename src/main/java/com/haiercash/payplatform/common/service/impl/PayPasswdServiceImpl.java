package com.haiercash.payplatform.common.service.impl;

import com.haiercash.payplatform.common.service.PayPasswdService;
import com.haiercash.payplatform.common.utils.ConstUtil;
import com.haiercash.commons.util.EncryptUtil;
import com.haiercash.payplatform.service.AppServerService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.haiercash.commons.redis.Cache;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

import static com.haiercash.payplatform.common.utils.RestUtil.fail;
import static com.haiercash.payplatform.common.utils.RestUtil.success;


/**
 * Created by Administrator on 2017/8/7.
 */
@Service
public class PayPasswdServiceImpl implements PayPasswdService {
    public Log logger = LogFactory.getLog(getClass());
    @Autowired
    private Cache cache;
    @Autowired
    private AppServerService appServerService;

    public Map<String, Object> resetPayPasswd(String token, String payPasswd, String verifyNo, String channelNo, String channel) {
        logger.info("顺逛******提交订单接口******开始");
        String retflag = "";
        String retmsg = "";

        if (StringUtils.isEmpty(token)) {
            logger.info("token:" + token);
            logger.info("从前端获取的的token为空");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.FAILED_INFO);
        }
        if (StringUtils.isEmpty(payPasswd)) {
            logger.info("payPasswd:" + payPasswd);
            logger.info("从前端获取的支付密码为空");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.FAILED_INFO);
        }
        Map<String, Object> cacheMap = cache.get(token);
        if (cacheMap.isEmpty()) {
            logger.info("Jedis获取失败");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        String userId = (String) cacheMap.get("userId");
        String flag = (String) cacheMap.get("payPasswdFlag");
        String orderNo = (String) cacheMap.get("orderNo");
        String n = "3";// 签订注册 + 征信
        if ("0".equals(flag)) {//0  密码未设置
            logger.info("支付密码未设置，进行密码的设置");
            Map<String, Object> paramsMap = new HashMap<String, Object>();
            paramsMap.put("userId", EncryptUtil.simpleEncrypt(userId));
            paramsMap.put("payPasswd", EncryptUtil.simpleEncrypt(payPasswd));
            paramsMap.put("token", token);
            paramsMap.put("access_token", token);
            String result = appServerService.resetPayPasswd(token, paramsMap).toString();
            if (StringUtils.isEmpty(result)) {
                logger.info("顺逛设置支付密码失败，app后台返回数据为空");
                String resultMsg = "顺逛设置支付密码失败，app后台返回数据为空";
                return fail(ConstUtil.ERROR_CODE, resultMsg);
            }
            JSONObject jsonObject = new JSONObject(result);
            JSONObject resultHead = jsonObject.getJSONObject("head");
            String retFlag = resultHead.getString("retFlag");
            String retMsg = resultHead.getString("retMsg");
            if ("00000".equals(retFlag)) {
                logger.info("顺逛设置支付密码失败" + retMsg);
                return fail(retFlag, retMsg);
            }
            // 3、签订注册 + 征信
            HashMap<String, Object> reqSignMap = new HashMap<>();
            reqSignMap.put("orderNo", orderNo);
            reqSignMap.put("msgCode", verifyNo);
            reqSignMap.put("type", n);// 1：征信协议 2：注册协议 3：征信和注册协议
            reqSignMap.put("channel", channel);
            reqSignMap.put("channelNo", channelNo);
            reqSignMap.put("token", token);
            String resData = appServerService.updateOrderAgreement(token, reqSignMap);// 订单协议确认
            logger.info("美分期,订单协议确认接口,响应数据：" + resData);
            if (StringUtils.isEmpty(resData)) {
                logger.info("网络异常，app后台,订单协议确认接口,响应数据为空！");
                String resDateMsg = "网络异常，app后台,订单协议确认接口,响应数据为空！";
                return fail(ConstUtil.ERROR_CODE, retMsg);
            }
            JSONObject jsonCon = new JSONObject(resData);
            JSONObject jsonConHead = jsonCon.getJSONObject("head");
            retflag = jsonConHead.getString("retFlag");
            retmsg = jsonConHead.getString("retMsg");
            if (!"00000".equals(retflag)) {// 订单协议确认接口 失败，返回给前台
                logger.info("美分期,校验短信验证码接口及订单提交接口,校验短信验证码失败" + retmsg);
                return fail(retflag, retmsg);
            }
            // 签订合同
            Map<String, Object> reqConMap = new HashMap<>();
            reqConMap.put("orderNo", orderNo);
            reqConMap.put("channel", channel);
            reqConMap.put("channelNo", channelNo);
            reqConMap.put("token", token);
            String retCon = appServerService.updateOrderContract(token, reqConMap);// 订单合同确认
            logger.info("订单合同确认接口，响应数据：" + retCon);
            if (retCon == null || "".equals(retCon)) {
                logger.info("美分期,订单合同确认接口,订单合同确认接口,响应数据为空");
                String retConMsg = "美分期,订单合同确认接口,订单合同确认接口,响应数据为空";
                return fail(ConstUtil.ERROR_CODE, retConMsg);
            }
            JSONObject retjsonCon = new JSONObject(retCon);
            JSONObject retjsonConHead = retjsonCon.getJSONObject("head");
            retflag = retjsonConHead.getString("retFlag");
            retmsg = retjsonConHead.getString("retMsg");
            if ("00000".equals(retflag)) {
                String opType = "1"; // 个人版订单提交给商户确认时传2，其余传1
                Map<String, Object> commitmMap = new HashMap<String, Object>();
                commitmMap.put("orderNo", orderNo);
                commitmMap.put("source", channel);
                commitmMap.put("channel", channel);
                commitmMap.put("channelNo", channelNo);
                commitmMap.put("opType", opType);
                commitmMap.put("token", token);
                commitmMap.put("msgCode", verifyNo);
                commitmMap.put("expectCredit", "expectCredit");
                String conData = appServerService.commitAppOrder(token, commitmMap);
                logger.info("美分期,订单提交，响应数据：" + conData);
                if (conData == null || "".equals(conData)) {
                    logger.info("美分期,订单提交接口,响应数据为空");
                    String conDataMsg = "美分期,订单提交接口,响应数据为空";
                    return fail(ConstUtil.ERROR_CODE, conDataMsg);
                }
                jsonCon = new JSONObject(conData);
                jsonConHead = jsonCon.getJSONObject("head");
                retflag = jsonConHead.getString("retFlag");
                retmsg = jsonConHead.getString("retMsg");
                if (("00000").equals(retflag)) {// 订单提交 成功：00000
                    logger.info("美分期,订单提交成功,跳转额度进度查询页面");
                } else {
                    logger.info("美分期,订单提交失败,跳转个人资料页面");
                    return success(retflag);
                }
            } else {
                logger.info("美分期,提交订单失败!");
                return fail(retflag, retmsg);
            }
        }
        return success();


    }

    //页面缓存
    public Map<String, Object> cache(HttpServletRequest request) {
        String token = request.getHeader("token");
        if (token == null || "".equals(token)) {
            logger.info("token为空");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        Map<String, Object> retMap = null;
        String params = request.getParameter("params");
        if (params != null) {
            String[] paramArr = params.split(",");
            String type = request.getParameter("type");
            if (type == null || type.equals("get")) {
                retMap = new HashMap<String, Object>();
                Map<String, Object> sessionMap = (Map<String, Object>) cache.get(token);
                for (String param : paramArr) {
                    retMap.put(param, sessionMap.get(param));
                }
            } else if (type.equals("set")) {
                retMap = (Map<String, Object>) cache.get(token);
                for (String param : paramArr) {
                    retMap.put(param, request.getParameter(param));
                }
                cache.set(token, retMap);
                return success();
            }
        }
        return success(retMap);
    }

    //修改支付密码（记得支付密码）
    public Map<String, Object> updatePayPasswd(String token, String oldpassword, String newpassword, String channel, String channelNo) {
        if (token == null || "".equals(token)) {
            logger.info("获取的token为空" + token);
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        if (StringUtils.isEmpty(oldpassword) || StringUtils.isEmpty(newpassword)) {
            logger.info("获取的参数为空");
            logger.info("payPasswd" + oldpassword + " newpassword" + newpassword);
            return fail(ConstUtil.ERROR_CODE, ConstUtil.FAILED_INFO);
        }
        Map<String, Object> cacheMap = cache.get(token);
        String userId = (String) cacheMap.get("userId");
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("token", token);
        map.put("channel", channel);
        map.put("channelNo", channelNo);
        map.put("userId", EncryptUtil.simpleEncrypt(userId));
        map.put("payPasswd", EncryptUtil.simpleEncrypt(oldpassword));
        map.put("newPayPasswd", EncryptUtil.simpleEncrypt(newpassword));
        String verifyresult = appServerService.updatePayPasswd(token, map);
        JSONObject jb0 = new JSONObject(verifyresult);
        JSONObject head = jb0.getJSONObject("head");
        String retflag0 = head.getString("retFlag");
        String retmsg0 = head.getString("retMsg");
        if (!"00000".equals(retflag0)) {
            logger.info("H5现金贷,修改支付密码异常！" + retmsg0);
            return fail(retflag0, retmsg0);
        }
        return success();
    }

    //实名认证找回密码
    public Map<String, Object> updPwdByIdentity(HttpServletRequest request) {
        String token = request.getHeader("token");
        if (token == null || "".equals(token)) {
            logger.info("获取的token为空" + token);
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        String userId = (String) request.getParameter("userId");//账号
        String certNo = (String) request.getParameter("certNo");//身份证号
        String cardNo = (String) request.getParameter("cardNo");//银行卡号
        String custName = (String) request.getParameter("custName");//客户姓名
        String mobile = (String) request.getParameter("mobile");//预留手机号
        String verifyNo = (String) request.getParameter("verifyNo");//验证码
        String newPassword = (String) request.getParameter("newPassword");//密码
        String channel = request.getHeader("channel");//
        String channelNo = request.getHeader("channelNo");//
        String bankCode = "";
        if (StringUtils.isEmpty(userId) || StringUtils.isEmpty(certNo) || StringUtils.isEmpty(cardNo) ||
                StringUtils.isEmpty(custName) || StringUtils.isEmpty(mobile) || StringUtils.isEmpty(verifyNo) ||
                StringUtils.isEmpty(newPassword)) {
            logger.info("userId" + userId + "  certNo" + certNo + "  cardNo" + cardNo + "  mobile" + mobile + "verifyNo" + verifyNo + "  newPassword" + newPassword);
            return fail(ConstUtil.ERROR_CODE, ConstUtil.FAILED_INFO);
        }
        Map<String,Object> map = new HashMap();
        map.put("userId", userId);
        map.put("certNo", certNo);
        map.put("cardNo", cardNo);
        map.put("custName", custName);
        map.put("mobile", mobile);
        map.put("verifyNo", verifyNo);
        map.put("newPassword", newPassword);
        map.put("channel", channel);
        map.put("channelNo", channelNo);
        String result = appServerService.updPwdByIdentity(token,map);
        if (result == null || "".equals(result)) {
            logger.info("实名认证修改密码接口，返回数据为空");
            String retMsg = "实名认证修改密码接口，返回数据为空";
            return fail(ConstUtil.FAILED_INFO, retMsg);
        }
        JSONObject jb = new JSONObject(result);
        JSONObject head = jb.getJSONObject("head");
        String retFlag = head.getString("retFlag");
        String retMsg = head.getString("retMsg");
        if ("00000".equals(retFlag)) {
            return success();
        } else {
            return fail(retFlag, retMsg);
        }
    }
}
