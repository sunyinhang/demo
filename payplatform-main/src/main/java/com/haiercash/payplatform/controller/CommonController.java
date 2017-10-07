package com.haiercash.payplatform.controller;

import com.alibaba.fastjson.JSONObject;
import com.bestvike.lang.Base64Utils;
import com.bestvike.lang.Convert;
import com.bestvike.lang.DateUtils;
import com.bestvike.lang.StringUtils;
import com.bestvike.serialization.JsonSerializer;
import com.haiercash.payplatform.common.dao.ChannelTradeLogDao;
import com.haiercash.payplatform.common.dao.CooperativeBusinessDao;
import com.haiercash.payplatform.common.data.ChannelTradeLog;
import com.haiercash.payplatform.common.data.CooperativeBusiness;
import com.haiercash.payplatform.common.entity.HaiercashPayApplyBean;
import com.haiercash.payplatform.common.entity.QueryLimitMessage;
import com.haiercash.payplatform.common.entity.ReturnMessage;
import com.haiercash.payplatform.config.EurekaServer;
import com.haiercash.payplatform.service.PaymentServiceInterface;
import com.haiercash.payplatform.utils.BusinessException;
import com.haiercash.payplatform.utils.ConstUtil;
import com.haiercash.payplatform.utils.RSAUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Created by 许崇雷 on 2017-10-07.
 */
@RestController
public class CommonController extends BaseController {
    private Log xmllog = LogFactory.getLog(CommonController.class);

    @Autowired
    private CooperativeBusinessDao cooperativeBusinessDao;

    @Autowired
    private PaymentServiceInterface paymentServiceInterface;

    @Autowired
    private ChannelTradeLogDao channelTradeLogDao;

    @Autowired
    private RestTemplate restTemplate;

    public CommonController(String moduleNo) {
        super("10");
    }

    @RequestMapping(value = "/api/HaiercashPayApplyForJson", method = RequestMethod.POST)
    public String doPost(@RequestBody HaiercashPayApplyBean haiercashPayApplyBean) throws Exception {
        String retFlag = null;
        String retMsg = null;
        String ret = null;
        xmllog.info("HaiercashPayApplyForJson，开始");
        xmllog.info("---------------------HaiercashPayApplyForJson-------------------------:");
        xmllog.info("*********HaiercashPayApplyForJson request***************");
        xmllog.info(haiercashPayApplyBean.toString());
        xmllog.info("*********HaiercashPayApplyForJson request***************");
        String channleNo = haiercashPayApplyBean.getChannleNo();
        String applyNo = haiercashPayApplyBean.getApplyNo();
        String tradeCode = haiercashPayApplyBean.getTradeCode();
        String jsonStr = haiercashPayApplyBean.getData();
        String tradetime = DateUtils.nowString();
        xmllog.info("----------------接口请求数据：-----------------");
        xmllog.info(haiercashPayApplyBean.toString());
        xmllog.info("----------------接口请求数据：-----------------");
        if (StringUtils.isEmpty(jsonStr)) {
            xmllog.info("第三方发送的请求报文信息不能为空！！！");
            throw new BusinessException(ConstUtil.ERROR_PARAM_INVALID_CODE, "请确认发送的报文信息是否符合条件！");
        }
        //========
        try {
            CooperativeBusiness cooperativeBusiness = this.cooperativeBusinessDao.selectBycooperationcoed(channleNo);
            jsonStr = new String(RSAUtils.decryptByPublicKey(Base64Utils.decode(jsonStr), cooperativeBusiness.getRsapublic()));
            xmllog.info("HaiercashPayApplyForJson--JSON:" + jsonStr.getBytes("utf-8").length);
            xmllog.info("----------------报文解密明文：-----------------" + jsonStr);
            Map jsonObject = JsonSerializer.deserialize(jsonStr, Map.class);
            Map jsonRequest = (Map) jsonObject.get("request");
            Map jsonbody = (Map) jsonRequest.get("body");
            xmllog.info("************进入收单系统************");
            String acquirerUrl = EurekaServer.getACQUIRER();
            String cmisfrontUrl = EurekaServer.getCMISFRONTSERVER();
            String url = StringUtils.EMPTY;
            switch (tradeCode) {
                case "100001":
                    url = acquirerUrl + "api/appl/saveLcAppl";
                    break;

                case "100021":
                    url = acquirerUrl + "api/appl/getApplInfo";
                    break;

                case "100026":
                    String flag = Convert.toString(jsonbody.get("flag"));//操作标识
                    if ("0".equals(flag) || "1".equals(flag)) {// 0：贷款取消；1:申请提交
                        xmllog.info("----------------收单系统贷款取消-----------------");
                        url = acquirerUrl + "api/appl/commitAppl";
                    } else if ("2".equals(flag)) {//合同提交
                        url = cmisfrontUrl;
                    }
                    break;
                case "100030":
                    if ("47".equals(channleNo)) {//积分时代，额度申请提交接口
                        String applSeq = Convert.toString(jsonbody.get("applSeq"));//申请编号

                        //1、查询额度申请信息
                        xmllog.info("外围渠道" + channleNo + ",查询额度申请信息开始");
                        String userName = "";//申请人姓名
                        String idno = "";//申请人手机号
                        String phone = "";//手机号
                        QueryLimitMessage queryLimitMessage = new QueryLimitMessage();
                        queryLimitMessage.setApplSeq(applSeq);
                        //额度申请信息查询接口
                        ReturnMessage returnMessage_ = paymentServiceInterface.queryLimitMessage(queryLimitMessage);
                        if (returnMessage_ == null) {
                            xmllog.info("申请号为：" + applSeq + "额度申请信息不存在！");
                            throw new BusinessException(ConstUtil.ERROR_PARAM_INVALID_CODE, "额度申请提交，失败！");
                        }
                        List list = returnMessage_.getData();
                        if (list != null && list.size() > 0) {
                            Map map = (Map) list.get(0);
                            userName = (String) ((List) map.get("custName")).get(0);
                            idno = (String) ((List) map.get("idNo")).get(0);
                            phone = (String) ((List) map.get("indivMobile")).get(0);
                        } else {
                            xmllog.info("申请号为：" + applSeq + "额度申请信息不存在！");
                            throw new BusinessException(ConstUtil.ERROR_PARAM_INVALID_CODE, "额度申请提交，失败！");
                        }
                        xmllog.info("外围渠道" + channleNo + ",查询额度申请信息结束");

                        // 2、进行征信、服务协议签名签章
                        xmllog.info("外围渠道" + channleNo + ",征信、服务协议签名签章开始");
                        String caUrl = EurekaServer.APPCA + "/app/appserver/caRequest";// CA签章地址
                        JSONObject reqZXJson = new JSONObject();// 征信
                        JSONObject orderZX = new JSONObject();

                        reqZXJson.put("custName", userName);// 客户姓名
                        reqZXJson.put("custIdCode", idno);// 客户身份证号
                        reqZXJson.put("applseq", applSeq);// 请求流水号
                        reqZXJson.put("signType", "credit");// 征信    签章类型
                        reqZXJson.put("flag", "0");//1 代表合同  0 代表 协议
                        orderZX.put("custName", userName);// 客户姓名
                        orderZX.put("idNo", idno);// 客户身份证号
                        orderZX.put("indivMobile", phone);// 客户手机号码
                        orderZX.put("applseq", applSeq);// 请求流水号

                        JSONObject orderZXJson = new JSONObject();// 订单信息json串
                        orderZXJson.put("order", orderZX.toString());
                        reqZXJson.put("orderJson", "\"" + orderZXJson.toString() + "\"");
                        reqZXJson.put("sysFlag", "11");// 系统标识：支付平台
                        //征信
                        xmllog.info("外围渠道" + channleNo + ",征信签名，请求报文：" + reqZXJson.toString());

                        String resZX = restTemplate.postForObject(caUrl, reqZXJson, String.class);// 征信签名请求
                        xmllog.info("外围渠道" + channleNo + ",征信签名，响应报文：" + resZX);
                        if (resZX != null && resZX.length() > 0) {
                            Map resJson = JsonSerializer.deserialize(resZX, Map.class);
                            Map headJson = (Map) resJson.get("head");
                            retFlag = Convert.toString(headJson.get("retFlag"));
                            if (retFlag.equals("00000")) {
                                xmllog.info("外围渠道" + channleNo + ",征信签名，调用成功！");
                            } else {
                                retMsg = Convert.toString(headJson.get("retMsg"));
                                xmllog.info("外围渠道" + channleNo + ",征信签名，调用失败：" + retMsg);
                                throw new BusinessException(ConstUtil.ERROR_CODE, "额度申请提交，失败！");
                            }
                        } else {
                            xmllog.info("外围渠道" + channleNo + ",征信签名，响应数据为空！");
                            throw new BusinessException(ConstUtil.ERROR_CODE, "额度申请提交，失败！");
                        }
                        //服务协议
                        reqZXJson.put("signType", "register");// 服务协议 签章类型
                        xmllog.info("外围渠道" + channleNo + ",服务协议签名签章，请求报文：" + reqZXJson.toString());
                        String res = restTemplate.postForObject(caUrl, reqZXJson.toString(), String.class);// 服务协议签名签章请求
                        xmllog.info("外围渠道" + channleNo + ",服务协议签名签章，响应报文：" + res);
                        if (res != null && res.length() > 0) {
                            Map resJson = JsonSerializer.deserialize(res, Map.class);
                            Map headJson = (Map) resJson.get("head");
                            retFlag = Convert.toString(headJson.get("retFlag"));
                            if (retFlag.equals("00000")) {
                                xmllog.info("外围渠道" + channleNo + ",服务协议签名签章,调用成功！");
                            } else {
                                retMsg = Convert.toString(headJson.get("retMsg"));
                                xmllog.info("外围渠道" + channleNo + ",服务协议签名签章,调用失败：" + retMsg);
                                throw new BusinessException(ConstUtil.ERROR_CODE, "额度申请提交，失败！");
                            }
                        } else {
                            xmllog.info("外围渠道" + channleNo + ",服务协议签名签章，响应数据为空！");
                            throw new BusinessException(ConstUtil.ERROR_CODE, "额度申请提交，失败！");
                        }
                        xmllog.info("外围渠道" + channleNo + ",征信、服务协议签名签章结束");

                    }

                    url = cmisfrontUrl;
                    break;

                default:
                    url = cmisfrontUrl;
                    break;
            }

            xmllog.info("通用接口JSON格式,请求地址为：" + url);
            xmllog.info("通用接口JSON格式,请求数据为：" + jsonStr);
            if (StringUtils.isEmpty(url))
                throw new BusinessException(ConstUtil.ERROR_CODE, "url 地址为空");
            String responseJson = restTemplate.postForObject(url, jsonStr, String.class);
            xmllog.info("响应数据:" + responseJson);
            if (StringUtils.isEmpty(responseJson)) {
                xmllog.info("响应数据为空！");
                throw new BusinessException(ConstUtil.ERROR_CODE, "网络通讯异常！");
            }

            xmllog.info("----------------接口回复数据：-----------------");
            xmllog.info("applyNo:" + applyNo + "||channleNo:" + channleNo + "||responseJson:" + responseJson);
            xmllog.info("----------------接口回复数据：-----------------");

            // 赋值供写入渠道交易日志表使用
            Map resData = JsonSerializer.deserialize(responseJson, Map.class);
            Map response = (Map) resData.get("response");
            Map head = (Map) response.get("head");
            retFlag = Convert.toString(head.get("retFlag"));
            retMsg = Convert.toString(head.get("retMsg"));
            ret = responseJson;
        } catch (Exception e) {
            retFlag = "ERROR";
            retMsg = e.getMessage();
            xmllog.error("HaiercashPayApplyForJson Post occur exception:" + e.getMessage(), e);
            e.printStackTrace();
        } finally {
            // 如果程序抛出异常，也要返回报文信息
            if ("ERROR".equals(retFlag)) {
                JSONObject errHeadJson = new JSONObject();
                errHeadJson.put("serno", "");
                errHeadJson.put("retFlag", "0001");
                errHeadJson.put("retMsg", retMsg);
                JSONObject errMsgJson = new JSONObject();
                errMsgJson.put("head", errHeadJson);
                errMsgJson.put("body", null);
                JSONObject errMsgResJson = new JSONObject();
                errMsgResJson.put("response", errMsgJson);
                String errorMsg = errMsgResJson.toString();
                xmllog.info("抛异常，响应错误数据：" + errorMsg);
                return errorMsg;
            }
            // 写入渠道交易日志表
            ChannelTradeLog channelTradeLog = new ChannelTradeLog();
            channelTradeLog.setApplyno(applyNo);
            channelTradeLog.setChannelno(channleNo);
            channelTradeLog.setTradecode(tradeCode);
            channelTradeLog.setRetflag(retFlag);
            channelTradeLog.setRetmsg(retMsg);
            channelTradeLog.setTradetime(tradetime);
            try {
                channelTradeLogDao.insert(channelTradeLog);
            } catch (Exception e) {
                xmllog.error("HaiercashPayApplyForJson Post occur sqlException:" + e.getMessage(), e);
                e.printStackTrace();
            }
            xmllog.info("HaiercashPayApplyForJson，结束");
            return ret;
        }
    }
}
