package com.haiercash.payplatform.common.service.impl;

import com.haiercash.payplatform.common.service.PayPasswdService;
import com.haiercash.payplatform.common.utils.ConstUtil;
import com.haiercash.commons.util.EncryptUtil;
import com.haiercash.payplatform.common.utils.HttpUtil;
import com.haiercash.payplatform.service.AppServerService;
import com.haiercash.payplatform.service.BaseService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.haiercash.commons.redis.Cache;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static com.haiercash.payplatform.common.utils.RestUtil.fail;
import static com.haiercash.payplatform.common.utils.RestUtil.success;


/**
 * Created by Administrator on 2017/8/7.
 */
@Service
public class PayPasswdServiceImpl extends BaseService implements PayPasswdService {
    public Log logger = LogFactory.getLog(getClass());
    @Autowired
    private Cache cache;
    @Autowired
    private AppServerService appServerService;

    public Map<String, Object> resetPayPasswd(String token, String payPasswd, String verifyNo, String channelNo, String channel) {
        logger.info("顺逛******额度提交接口******开始");
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
        String custNo = (String) cacheMap.get("custNo");// 客户号
        if (StringUtils.isEmpty(custNo)) {
            logger.info("custNo:" + custNo);
            logger.info("从前端获取的客户号为空");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.FAILED_INFO);
        }
        String crdSeq = (String) cacheMap.get("crdSeq");//在途的申请流水号

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
        } else if ("1".equals(flag)) {// 支付密码验证
            n = "1";
            HashMap<String, Object> map = new HashMap<>();
            String userIdEncrypt = EncryptUtil.simpleEncrypt(userId);
            String payPasswdEncrypt = EncryptUtil.simpleEncrypt(payPasswd);
            map.put("userId", userIdEncrypt);
            map.put("payPasswd", payPasswdEncrypt);
            map.put("channel", channel);
            map.put("channelNo", channelNo);
            String result = appServerService.validatePayPasswd(token, map);// 验证支付密码
            if (StringUtils.isEmpty(result)) {
                logger.info("顺逛,支付密码验证失败,app后台返回为空result" + result);
                return fail(ConstUtil.ERROR_CODE, ConstUtil.FAILED_INFO);
            }
            JSONObject jb = new JSONObject(result);
            retflag = jb.getString("retFlag");
            retmsg = jb.getString("retMsg");
            if (!"00000".equals(retflag)) {
                logger.info("美分期,支付密码验证失败！" + retmsg);
                return fail(ConstUtil.ERROR_CODE, retmsg);
            }
        } else {
            logger.info("顺逛,是否设置过支付密码标志无效");
            String retMsg = "顺逛,是否设置过支付密码标志无效";
            return fail(ConstUtil.ERROR_CODE, retMsg);
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
            return fail(ConstUtil.ERROR_CODE, resDateMsg);
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
//            if ("00000".equals(retflag)) {
//                String opType = "1"; // 个人版订单提交给商户确认时传2，其余传1
//                Map<String, Object> commitmMap = new HashMap<String, Object>();
//                commitmMap.put("orderNo", orderNo);
//                commitmMap.put("source", channel);
//                commitmMap.put("channel", channel);
//                commitmMap.put("channelNo", channelNo);
//                commitmMap.put("opType", opType);
//                commitmMap.put("token", token);
//                commitmMap.put("msgCode", verifyNo);
//                commitmMap.put("expectCredit", "expectCredit");
//                String conData = appServerService.commitAppOrder(token, commitmMap);
//                logger.info("美分期,订单提交，响应数据：" + conData);
//                if (conData == null || "".equals(conData)) {
//                    logger.info("美分期,订单提交接口,响应数据为空");
//                    String conDataMsg = "美分期,订单提交接口,响应数据为空";
//                    return fail(ConstUtil.ERROR_CODE, conDataMsg);
//                }
//                jsonCon = new JSONObject(conData);
//                jsonConHead = jsonCon.getJSONObject("head");
//                retflag = jsonConHead.getString("retFlag");
//                retmsg = jsonConHead.getString("retMsg");
//                if (("00000").equals(retflag)) {// 订单提交 成功：00000
//                    logger.info("美分期,订单提交成功,跳转额度进度查询页面");
//                } else {
//                    logger.info("美分期,订单提交失败,跳转个人资料页面");
//                    return success(retflag);
//                }
//            } else {
//                logger.info("美分期,提交订单失败!");
//                return fail(retflag, retmsg);
//            }
        Map<String, Object> mapEd = new HashMap();
        mapEd.put("token", token);
        mapEd.put("channel", channel);
        mapEd.put("channelNo", channelNo);
        mapEd.put("custNo", custNo);
        if (StringUtils.isEmpty(crdSeq)) {//新增
            mapEd.put("flag", "0");//额度申请
        } else {//有在途的流水号(修改)
            mapEd.put("flag", "2");
            mapEd.put("applSeq", "crdSeq");//额度申请
        }
        String resultEd = appServerService.getEdApplInfo(token, mapEd).toString();
        JSONObject jb = new JSONObject(resultEd);
        JSONObject head = jb.getJSONObject("head");
        retflag = head.getString("retFlag");
        retmsg = head.getString("retMsg");
        if (!"00000".equals(retflag)) {
            logger.info("H5现金贷,额度申请出现异常！" + retmsg);
            return fail(ConstUtil.ERROR_CODE, retmsg);
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
        Map<String, Object> map = new HashMap();
        map.put("userId", userId);
        map.put("certNo", certNo);
        map.put("cardNo", cardNo);
        map.put("custName", custName);
        map.put("mobile", mobile);
        map.put("verifyNo", verifyNo);
        map.put("newPassword", newPassword);
        map.put("channel", channel);
        map.put("channelNo", channelNo);
        String result = appServerService.updPwdByIdentity(token, map);
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

    //确认支付密码（额度申请）
    public Map<String, Object> paymentPwdConfirm(String token, String channel, String channelNo, String payPasswd) {
        if (StringUtils.isEmpty(token) || StringUtils.isEmpty(channel) || StringUtils.isEmpty(channelNo)) {
            logger.info("获取前端的参数为空token：" + token + "  ,channel" + channel + "  ,channelNo" + channelNo);
            String retMsg = "获取前端的参数为空";
            return fail(ConstUtil.FAILED_INFO, retMsg);
        }
        Map<String, Object> cacheMap = cache.get(token);
        if (cacheMap.isEmpty()) {
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
        String resStr = appServerService.validatePayPasswd(token, map);// 验证支付密码
        logger.info("确认支付密码（额度申请）,返回数据为：" + resStr);
        if (resStr == null || "".equals(resStr)) {
            logger.info("接口返回数据为空！");
            String retMsg = "确认支付密码（额度申请）接口返回数据为空";
            return fail(ConstUtil.ERROR_CODE, retMsg);
        }
        JSONObject jsonObj = new JSONObject(resStr);
        JSONObject head = jsonObj.getJSONObject("head");
        if ("00000".equals(head.getString("retFlag"))) {
            cacheMap.put("payPasswd", payPasswd);
            cacheMap.put("pageFlag", "1");
            cache.set(token, cacheMap);
            return success();
        } else {
            String tetFlag = head.getString("tetFlag");
            String retMsg = (head.getString("retMsg"));
            logger.info("确认支付密码（额度申请）失败,返回数据为：" + retMsg);
            return fail(tetFlag, retMsg);
        }

    }

    //额度申请提交
    public Map<String, Object> edApply(String token, String verifyNo, String payPasswd, String channel, String channelNo) {
        return null;
    }

    //贷款详情查询
    public Map<String, Object> queryLoanDetailInfo(String token) {
        BigDecimal psNormIntAmt = new BigDecimal(0);
        BigDecimal feeAmt = new BigDecimal(0);
        BigDecimal apprvAmt = new BigDecimal(0);
        if (StringUtils.isEmpty(token)) {
            logger.info("获取的token为空" + token);
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        String channel = super.getChannel();//系统标识
        logger.info("系统标识：channel" + channel);
        String channelNo = super.getChannelNo();//渠道编码
        logger.info("渠道编码channelNo" + channelNo);
        Map<String, Object> cacheMap = cache.get(token);
        if (StringUtils.isEmpty(cacheMap)) {
            logger.info("Jedis获取缓存失败");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        String applSeq = (String) cacheMap.get("applSeq");//申请流水号
        String outSts = (String) cacheMap.get("outSts");//审批状态
        if (StringUtils.isEmpty(applSeq) || StringUtils.isEmpty(outSts)) {
            logger.info("获取数据为空申请流水号：" + applSeq + "审批状态：" + outSts);
            String retMsg = "从jedis获取的数据为空";
            return fail(ConstUtil.ERROR_CODE, retMsg);
        }

        Map<String, Object> req = new HashMap<>();
        req.put("channelNo", channelNo);
        req.put("channel", channel);
        req.put("applSeq", applSeq);
        logger.info("查询贷款详情接口，请求数据：" + req.toString());
        String res = appServerService.queryApplLoanDetail(token,req).toString();//查询贷款详情
        logger.info("查询贷款详情接口，响应数据：" + res);
        if (res == null || "".equals(res)) {
            logger.info("网络异常,查询贷款详情接口,响应数据为空！" + res);
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        }
        JSONObject jsonObject = new JSONObject(res);
        JSONObject head = jsonObject.getJSONObject("head");
        String code = head.getString("retFlag");
        String message = head.getString("retMsg");
        if ("0000".equals(code)) {//查询贷款详情成功
            logger.info("查询贷款详情接口，响应数据：" + jsonObject.getJSONObject("body").toString());
            JSONObject json = jsonObject.getJSONObject("body");
            String applyTnrTyp = json.get("applyTnrTyp").toString();
            String totfee = "";
            String apprvTotal = "";
            if (!applyTnrTyp.equals("D") && !applyTnrTyp.equals("d") && (applyTnrTyp != null && !"".equals(applyTnrTyp))) {
                String psNormIntAmtStr = String.valueOf(json.get("psNormIntAmt"));//总利息金额
                if (psNormIntAmtStr.equals("null")) {
                    psNormIntAmt = new BigDecimal(0);
                } else {
                    psNormIntAmt = new BigDecimal(psNormIntAmtStr);
                }
                String feeAmtStr = String.valueOf(json.get("feeAmt"));//费用总额
                if (feeAmtStr.equals("null")) {
                    feeAmt = new BigDecimal(0);
                } else {
                    feeAmt = new BigDecimal(feeAmtStr);
                }
                String apprvAmtStr = String.valueOf(json.get("apprvAmt"));//贷款审批金额
                if (apprvAmtStr.equals("null")) {
                    apprvAmt = new BigDecimal(0);
                } else {
                    apprvAmt = new BigDecimal(apprvAmtStr);
                }

                BigDecimal tot = new BigDecimal(0);
                tot = psNormIntAmt.add(feeAmt);//总利息+总费用
                totfee = tot.divide(new BigDecimal(1), 2, BigDecimal.ROUND_HALF_UP) + "";
                json.put("totfee", totfee);
                BigDecimal total = new BigDecimal(0);
                total = tot.add(apprvAmt);//总利息+总费用+贷款审批金额
                apprvTotal = total.divide(new BigDecimal(1), 2, BigDecimal.ROUND_HALF_UP) + "";
                json.put("apprvTotal", apprvTotal);

            }
            String outStsNew = json.getString("outSts");
            if (!"WS".equals(outStsNew)) {
                if (outStsNew.equals("1")) {
                    json.put("outSts", "待提交");
                } else if (outStsNew.equals("2")) {
                    json.put("outSts", "待确认");
                } else if (outStsNew.equals("3")) {
                    json.put("outSts", "商户退回");
                } else if (outStsNew.equals("01")) {
                    json.put("outSts", "审批中");
                } else if (outStsNew.equals("02")) {
                    json.put("outSts", "贷款被拒绝");
                } else if (outStsNew.equals("03")) {
                    json.put("outSts", "贷款已取消");
                } else if (outStsNew.equals("04")) {
                    json.put("outSts", "等待放款");
                } else if (outStsNew.equals("05")) {
                    json.put("outSts", "审批通过，等待放款");
                } else if (outStsNew.equals("06")) {
                    json.put("outSts", "已放款");
                } else if (outStsNew.equals("20")) {
                    json.put("outSts", "待放款");
                } else if (outStsNew.equals("22")) {
                    json.put("outSts", "审批退回");
                } else if (outStsNew.equals("23")) {
                    json.put("outSts", "等待放款");
                } else if (outStsNew.equals("24")) {
                    json.put("outSts", "放款审核中");
                } else if (outStsNew.equals("25")) {
                    json.put("outSts", "额度申请被拒");
                } else if (outStsNew.equals("26")) {
                    json.put("outSts", "额度申请已取消");
                } else if (outStsNew.equals("27")) {
                    json.put("outSts", "已通过");
                } else if (outStsNew.equals("AA")) {
                    json.put("outSts", "取消放款");
                } else if (outStsNew.equals("0D")) {
                    json.put("outSts", "已逾期");
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
}
