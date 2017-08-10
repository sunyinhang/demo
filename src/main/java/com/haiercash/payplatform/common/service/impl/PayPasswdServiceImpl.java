package com.haiercash.payplatform.common.service.impl;

import com.haiercash.payplatform.common.service.PayPasswdService;
import com.haiercash.payplatform.common.utils.ConstUtil;
import com.haiercash.commons.util.EncryptUtil;
import com.haiercash.payplatform.common.service.AppServerService;
import com.haiercash.payplatform.service.BaseService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.haiercash.commons.redis.Cache;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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

    public Map<String, Object> resetPayPasswd(String token, String channelNo, String channel, Map<String, Object> param) {
        logger.info("顺逛******额度提交接口******开始");
        String retflag = "";
        String retmsg = "";
        String payPasswd = (String) param.get("payPasswd");//密码
        String verifyNo = (String) param.get("verifyNo");//验证码
        if (StringUtils.isEmpty(token)) {
            logger.info("token:" + token);
            logger.info("从前端获取的的token为空");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.FAILED_INFO);
        }
        if (StringUtils.isEmpty(payPasswd) || StringUtils.isEmpty(verifyNo)) {
            logger.info("payPasswd:" + payPasswd+"verifyNo"+verifyNo);
            logger.info("从前端获取的参数为空");
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
            paramsMap.put("channel", channel);
            paramsMap.put("channelNo", channelNo);
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
    public Map<String, Object> cache(Map<String, Object> params, HttpServletRequest request) {
        String token = request.getHeader("token");
        if (token == null || "".equals(token)) {
            logger.info("token为空");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        Map<String, Object> retMap = null;
        String paramNames = (String) params.get("params");
        if (paramNames != null) {
            String[] paramArr = paramNames.split(",");
            String type = (String) params.get("type");
            if (type == null || type.equals("get")) {
                retMap = new HashMap<String, Object>();
                Map<String, Object> sessionMap = (Map<String, Object>) cache.get(token);
                if (sessionMap == null) {
                    return success();
                }
                for (String param : paramArr) {
                    retMap.put(param, sessionMap.get(param));
                }
            } else if (type.equals("set")) {
                retMap = (Map<String, Object>) cache.get(token);
                if (retMap == null) {
                    retMap = new HashMap<>();
                }
                for (String param : paramArr) {
                    retMap.put(param, (String) params.get(param));
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
        String res = appServerService.queryApplLoanDetail(token, req).toString();//查询贷款详情
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

    //贷款详情页面:按贷款申请查询分期账单
    public Map<String, Object> queryApplListBySeq(String token, String channel, String channelNo) {
        String loanNo = "";
        String retflag = "";
        if (StringUtils.isEmpty(token) || StringUtils.isEmpty(channel) || StringUtils.isEmpty(channelNo)) {
            logger.info("获取的参数为空：token：" + token + "  ,channel" + channel + "  ," + channelNo);
            return fail(ConstUtil.ERROR_CODE, ConstUtil.FAILED_INFO);
        }
        Map<String, Object> cacheMap = cache.get(token);
        if (StringUtils.isEmpty(cacheMap)) {
            logger.info("Jedi获取的数据weikong");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        }
        String applSeq = (String) cacheMap.get("applSeq");//申请流水号
        String outSts = (String) cacheMap.get("outSts");//审批状态
        if (StringUtils.isEmpty(applSeq) || StringUtils.isEmpty(outSts)) {
            logger.info("从jedis获取数据失败applSeq:" + applSeq + "  ,outSts" + outSts);
            String retMsg = "从jedis获取数据失败";
            return fail(ConstUtil.ERROR_CODE, retMsg);
        }
        Map<String, Object> req = new HashMap<>();
        req.put("channelNo", channelNo);
        req.put("channel", channel);
        req.put("applSeq", applSeq);
        logger.info("查询贷款详情接口，请求数据：" + req.toString());
        String resData = appServerService.queryApplLoanDetail(token, req).toString();//查询贷款详情
        logger.info("查询贷款详情接口，响应数据：" + resData);
        if (resData == null || "".equals(resData)) {
            logger.info("网络异常,查询贷款详情接口,响应数据为空！");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        }
        JSONObject jsonObject = new JSONObject(resData);
        JSONObject head = jsonObject.getJSONObject("head");
        String code = head.getString("retFlag");
        String message = head.getString("retMsg");
        if ("0000".equals(code)) {//查询贷款详情成功
            logger.info("查询贷款详情接口，响应数据：" + jsonObject.getJSONObject("body").toString());
            JSONObject jsonData = jsonObject.getJSONObject("body");
            if (outSts.equals("待还款") || outSts.equals("已放款") || outSts.equals("已逾期")) {
                loanNo = jsonData.getString("loanNo");
                if (loanNo == null || "".equals(loanNo)) {
                    logger.info("借据号为空");
                    String retMsg = "借据号为空";
                    return fail(ConstUtil.ERROR_CODE, retMsg);
                }
            }
            HashMap<String, Object> queryApplListMap = new HashMap<>();
            queryApplListMap.put("channelNo", channelNo);
            queryApplListMap.put("channel", channel);
            queryApplListMap.put("applSeq", applSeq);

            String resOne = appServerService.queryApplListBySeq(token, queryApplListMap).toString();//按贷款申请查询分期账单接口
            logger.info("按贷款申请查询分期账单接口，响应数据：" + resOne);
            if (resOne == null || "".equals(resOne)) {
                logger.info("网络异常，app后台,按贷款申请查询分期账单接口,响应数据为空！");
                return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);

            }
            JSONObject jsonOne = new JSONObject(resOne);
            JSONObject jsonOneHead = jsonOne.getJSONObject("head");
            String retOneFlag = jsonOneHead.getString("retFlag");
            String retOneMsg = jsonOneHead.getString("retMsg");
            if (retOneFlag.equals("00000")) {
                JSONArray body = jsonOne.getJSONArray("body");
                return success(body);
            } else {
                retflag = retOneFlag;
                return fail(retflag, retOneMsg);
            }
        } else {
            retflag = code;
            return fail(retflag, message);
        }
    }

    //贷款详情页面:还款总额
    public Map<String, Object> queryApplAmtBySeqAndOrederNo(String token, String channel, String channelNo) {
        logger.info("待还款-贷款详情页面:还款总额接口，开始");
        String retflag = "";
        String retmsg = "";
        String loanNo = "";
        String url = "";
        BigDecimal odAmt = new BigDecimal(0);
        BigDecimal totalAmt = new BigDecimal(0);
        BigDecimal odTotalAmt = new BigDecimal(0);
        int flag = 0;// 记录逾期数
        String PAYM_MODE = "";// 还款模式
        String ACTV_PAY_AMT = "";// 主动还款金额

        if (StringUtils.isEmpty(token) || StringUtils.isEmpty(channel) || StringUtils.isEmpty(channelNo)) {
            logger.info("获取的数据为空：token" + token + "  ,channel" + channel + "  ,channelNo" + channelNo);
            return fail(ConstUtil.ERROR_CODE, ConstUtil.FAILED_INFO);
        }
        Map<String, Object> cacheMap = cache.get(token);
        if (StringUtils.isEmpty(cacheMap)) {
            logger.info("贷款详情页面:还款总额接口，Jedis失效，cacheMap" + cacheMap);
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        String applSeq = (String) cacheMap.get("applSeq");// 申请流水号----需要放开
        String outSts = (String) cacheMap.get("outSts");//审批状态
        //String applSeq = "1255979";
        //outSts="待还款";
        if (StringUtils.isEmpty(applSeq) || StringUtils.isEmpty(outSts)) {
            logger.info("Jedis中获取的数据为空：applSeq=" + applSeq + "  ,outSts=" + outSts);
            retflag = "从Jedis中获取的数据为空";
            return fail(ConstUtil.ERROR_CODE, retmsg);
        }
        Map<String, Object> req = new HashMap<>();
        req.put("channelNo", channelNo);
        req.put("channel", channel);
        req.put("applSeq", applSeq);
        logger.info("查询贷款详情接口，请求数据：" + req.toString());
        String resData = appServerService.queryApplLoanDetail(token, req).toString();//查询贷款详情
        logger.info("查询贷款详情接口，响应数据：" + resData);
        if (StringUtils.isEmpty(resData)) {
            logger.info("网络异常,查询贷款详情接口,响应数据为空！");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        }
        JSONObject jsonObject = new JSONObject(resData);
        JSONObject head = jsonObject.getJSONObject("head");
        String code = (String) head.get("retFlag");
        String message = (String) head.get("retMsg");
        if (code.equals("0000")) {//查询贷款详情成功
            logger.info("查询贷款详情接口，响应数据：" + jsonObject.getJSONObject("body").toString());
            JSONObject jsonData = jsonObject.getJSONObject("body");
            if (outSts.equals("待还款") || outSts.equals("已放款") || outSts.equals("已逾期")) {
                //loanNo="HCF-HAPA0120160320795362001";
                loanNo = jsonData.getString("loanNo");
                if (StringUtils.isEmpty(loanNo)) {
                    logger.info("借据号为空");
                    retflag = "借据号为空";
                    return fail(ConstUtil.ERROR_CODE, retflag);
                }
                cacheMap.put("loanNo", loanNo);//借据号
                cache.set(token, cacheMap);
//                JSONObject reqJson = new JSONObject();
//                reqJson.put("LOAN_NO", loanNo);
//                String params = reqJson.toString();
                HashMap<String, Object> qfmap = new HashMap<>();
                qfmap.put("LOAN_NO", loanNo);
                qfmap.put("channel", channel);
                qfmap.put("channelNo", channelNo);
                logger.info("欠款查询接口，请求数据：" + qfmap);
                String res = appServerService.getQFCheck(token, qfmap);// 欠款查询
                logger.info("欠款查询接口，响应数据：" + res);
                if (res == null || "".equals(res)) {
                    logger.info("网络异常，app后台,欠款查询接口,响应数据为空！" + res);
                    return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
                }
                JSONObject json = new JSONObject(res);
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
                    HashMap<String, Object> queryApplListmap = new HashMap<>();
                    queryApplListmap.put("channelNo", channelNo);
                    queryApplListmap.put("channel", channel);
                    queryApplListmap.put("applSeq", applSeq);
                    String resOne = appServerService.queryApplListBySeq(token, queryApplListmap).toString();// 按贷款申请查询分期账单接口
                    logger.info("按贷款申请查询分期账单接口，响应数据：" + resOne);
                    if (resOne == null || "".equals(resOne)) {
                        logger.info("网络异常，app后台,按贷款申请查询分期账单接口,响应数据为空！" + resOne);
                        return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
                    }
                    JSONObject jsonOne = new JSONObject(resOne);
                    JSONObject jsonOneHead = jsonOne.getJSONObject("head");
                    String retOneFlag = jsonOneHead.getString("retFlag");
                    String retOneMsg = jsonOneHead.getString("retMsg");
                    if (retOneFlag.equals("00000")) {// 按贷款申请查询分期账单接口成功
                        //retflag = "0000";
                        JSONArray resArr = new JSONArray();
                        List array = (List) jsonOne.get("body");
                        if (array != null && array.size() > 0) {
                            for (int i = 0; i < array.size(); i++) {
                                JSONObject resJson = (JSONObject) array.get(i);
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
                                PAYM_MODE = "FS";
                                ACTV_PAY_AMT = totalAmt + "";
                                Map<String, Object> reqTwoMap = new HashMap<String, Object>();
                                reqTwoMap.put("loanNo", loanNo);
                                reqTwoMap.put("actvPayAmt", ACTV_PAY_AMT);
                                reqTwoMap.put("channel", channel);
                                reqTwoMap.put("channelNo", channelNo);
                                String resThree = appServerService.refundTrialAll(token, reqTwoMap).toString();// 全部还款试算
                                logger.info("全部还款试算接口，响应数据：" + resThree);
                                if (StringUtils.isEmpty(resThree)) {
                                    logger.info("网络异常,全部还款试算接口,响应数据为空！");
                                    return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
                                }
                                JSONObject jsonObjectresThree = new JSONObject(resThree);
                                String codeOne = jsonObjectresThree.getString("retMsg");
                                String messageOne = jsonObjectresThree.getString("retMsg");
                                if (codeOne.equals("0000")) {// 全部还款试算成功
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
                                    return success(jsonThree);
                                } else {
                                    retflag = codeOne;
                                    return fail(retflag, messageOne);
                                }
                            } else if (flag > 0) {// 存在逾期金额

                                int odFlag = odAmt.compareTo(BigDecimal.ZERO); // 和0，Zero比较
                                if (odFlag == 0) {// 等于，逾期金额为0
                                    PAYM_MODE = "ER";
                                    ACTV_PAY_AMT = odTotalAmt + "";
                                } else if (odFlag == 1) {// 大于,逾期金额大于0
                                    PAYM_MODE = "NM";
                                    odTotalAmt = odTotalAmt.add(odAmt);
                                    ACTV_PAY_AMT = odTotalAmt + "";
                                } else if (odFlag == -1) {// 小于
                                    String retMsgOne = "按贷款申请查询分期账单接口，逾期金额为负数";
                                    return fail(ConstUtil.ERROR_CODE, retMsgOne);
                                }
                                HashMap<String, Object> checkZdhkMoney = new HashMap<>();
                                checkZdhkMoney.put("LOAN_NO", loanNo);
                                checkZdhkMoney.put("PAYM_MODE", PAYM_MODE);
                                checkZdhkMoney.put("ACTV_PAY_AMT", ACTV_PAY_AMT);
                                String resTwo = appServerService.checkZdhkMoney(token, checkZdhkMoney);// 主动还款金额查询
                                logger.info("主动还款金额查询接口，响应数据：" + resTwo);
                                if (StringUtils.isEmpty(resTwo)) {
                                    logger.info("网络异常，app后台,主动还款金额查询接口,响应数据为空！" + resTwo);
                                    return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
                                }
                                JSONObject jsonTwo = new JSONObject(resTwo);
                                JSONObject jsonTwoRes = jsonTwo.getJSONObject("msgall");
                                String retFlagTwo = jsonTwoRes.getString("errorCode");
                                String retMsgTwo = jsonTwoRes.getString("errorMsg");
                                if (retFlagTwo.equals("00000")) {// 主动还款金额查询接口成功
                                    JSONObject jsonTwoBody = jsonTwo.getJSONObject("body");
                                    String zdhkFee = jsonTwoBody.getString("zdhkFee");
                                    if (zdhkFee == null || "".equals(zdhkFee)) {
                                        logger.info("网络异常，app后台,主动还款金额查询接口,主动还款金额为空！" + zdhkFee);
                                        return fail(ConstUtil.ERROR_CODE, ConstUtil.FAILED_INFO);
                                    }
                                    Map<String, Object> resTwoMap = new HashMap<String, Object>();
                                    resTwoMap.put("ze", zdhkFee);
                                    return success(resTwoMap);
                                } else {
                                    retflag = retFlagTwo;
                                    return fail(retflag, retMsgTwo);
                                }
                            } else {
                                logger.info("按贷款申请查询分期账单接口，数据flag出现异常，为负数");
                                return fail(ConstUtil.ERROR_CODE, ConstUtil.FAILED_INFO);
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
            return fail(ConstUtil.ERROR_CODE, ConstUtil.FAILED_INFO);
        }
        Map<String, Object> cacheMap = cache.get(token);
        if (StringUtils.isEmpty(cacheMap)) {
            logger.info("Jedis获取缓存失败");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        String idNo = (String) cacheMap.get("idNo");
        String channel = super.getChannel();//系统标识
        String channelNo = super.getChannelNo();//渠道编码
        String idTyp = "20";//证件类型  身份证：20
        if (StringUtils.isEmpty(idNo) || StringUtils.isEmpty(channel) || StringUtils.isEmpty(channelNo)) {
            logger.info("获取的数据为空：idNo=" + idNo + "  ,channel=" + channel + "  ,channelNO" + channelNo);
            String retMsg = "获取的数据为空";
            return fail(ConstUtil.ERROR_CODE, retMsg);
        }
        HashMap<String, Object> edCheckmap = new HashMap<>();
        edCheckmap.put("idNo", idNo);
        edCheckmap.put("channel", channel);
        edCheckmap.put("channelNo", channelNo);
        edCheckmap.put("idTyp", idTyp);
        String limit = appServerService.getEdCheck(token, edCheckmap);// 获取额度剩余额度=crdComAvailAnt+crdNorAvailAmt
        if (StringUtils.isEmpty(limit)) {
            logger.info("调用接口返回的数据为空");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        }
        JSONObject jsonObject = new JSONObject(limit);
        JSONObject head = jsonObject.getJSONObject("head");
        String retFlag = (String) head.get("retFlag");
        String retMsg = (String) head.get("retMsg");
        if ("0000".equals(retFlag)) {
            JSONObject limitRes = jsonObject.getJSONObject("body");
            double crdComAvailAnt = limitRes.getDouble("crdComAvailAnt");// 剩余额度（受托支付可用额度金额）
            double crdNorAvailAmt = limitRes.getDouble("crdNorAvailAmt");// 自主支付可用额度金额(现金)
            double crdAmt = limitRes.getDouble("crdAmt");// 总额度
            double crdComAmt = limitRes.getDouble("crdComAmt");
            // crdComAvailAntSum = crdComAvailAnt+crdNorAvailAmt;可用额度
            JSONObject jb = new JSONObject();
            jb.put("crdComAvailAnt", crdComAvailAnt);
            jb.put("crdNorAvailAmt", crdNorAvailAmt);
            jb.put("crdAmt", crdAmt);
            jb.put("crdComAmt", crdComAmt);
            // jb.put("crdComAvailAntSum", crdComAvailAntSum);
            return success(jb);
        } else {
            return success(limit);
        }
    }

    //根据流水号查询额度审批进度
    public Map<String, Object> approvalProcessInfo(String token, String channel, String channelNo) {
        if (StringUtils.isEmpty(token) || StringUtils.isEmpty(channel) || StringUtils.isEmpty(channelNo)) {
            logger.info("获取的参数为空token:" + token + "  ,channel" + channel + "  ,channelNO" + channelNo);
            return fail(ConstUtil.ERROR_CODE, ConstUtil.FAILED_INFO);
        }
        Map<String, Object> cacheMap = cache.get(token);
        if (StringUtils.isEmpty(cacheMap)) {
            logger.info("Jedis为空：" + cacheMap);
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        String applSeq = (String) cacheMap.get("applSeq");//申请流水号
        if (StringUtils.isEmpty(applSeq)) {
            logger.info("从Jedis中获取的数据为空：applSeq=" + applSeq);
            String retmsg = "从Jedis中获取的数据为空";
            return fail(ConstUtil.ERROR_CODE, retmsg);
        }
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("applSeq", applSeq);
        paramMap.put("channel", channel);
        paramMap.put("channelNo", channelNo);
        logger.info("根据流水号查询额度审批进度,请求参数：" + paramMap);
        String result = appServerService.approvalProcessInfo(token, paramMap);
        if (StringUtils.isEmpty(result)) {
            logger.info("根据流水号查询额度审批进度返回参数为空");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        }
        JSONObject jsonObject = new JSONObject(result);
        JSONObject head = jsonObject.getJSONObject("head");
        String flag = (String) head.get("retFlag");
        String retMsg = (String) head.get("retMsg");
        if (!"0000".equals(flag)) {
            return fail(flag, retMsg);
        } else {
            JSONObject body = jsonObject.getJSONObject("body");
            return success(body);
        }
    }
}